<template>
  <div class="p-2">
    <transition :enter-active-class="proxy?.animate.searchAnimate.enter" :leave-active-class="proxy?.animate.searchAnimate.leave">
      <div v-show="showSearch" class="mb-[10px]">
        <el-card shadow="hover">
          <super-search
            code="bookPrint-apply-search"
            v-model="queryParams"
            @getList="getList"
            @resetQuery="resetQuery"
          >
          </super-search>
        </el-card>
      </div>
    </transition>

    <el-card shadow="hover">
      <template #header>
        <el-row :gutter="10" class="mb8">
          <!-- <el-col :span="1.5">
            <el-button v-hasPermi="['dms:volumesPrintApply:edit']" type="success" plain icon="Edit" :disabled="single" @click="handleUpdate()">编辑</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button v-hasPermi="['dms:volumesPrintApply:remove']" type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete()"
              >删除</el-button
            >
          </el-col>
          <el-col :span="1.5">
            <el-button v-hasPermi="['dms:templateEdit:export']" type="warning" plain icon="Download" @click="handleExport">导出</el-button>
          </el-col> -->
          <right-toolbar v-model:show-search="showSearch" @query-table="getList"></right-toolbar>
        </el-row>
      </template>
      <super-table 
        v-if="showTable"
        code="bookPrint-apply-table"
        rowKey="id"
        highlightCurrentRow
        :dataLoading="loading"
        :data="recordList"
        v-model:queryParams="queryParams"
        :total="total"
        @selection-change="handleSelectionChange"
        @getList="getList"
      >
        <!-- 操作列 -->
        <template #operation="scope">
          <div>
            <el-tooltip content="详情" placement="top" :enterable="false">
              <el-button link type="primary" v-hasPermi="['dms:volumesPrintApply:detail']" icon="View" @click="handleDetail(scope.row)"></el-button>
            </el-tooltip>
            <el-tooltip content="提交" placement="top" :enterable="false">
              <el-button link type="primary" v-hasPermi="['dms:volumesPrintApply:approSubmit']" icon="SuccessFilled" @click="submitApprove(scope.row)"></el-button>
            </el-tooltip>
          </div>
        </template>
      </super-table>
      <!-- <pagination v-show="total > 0" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :total="total" @pagination="getList" /> -->
    </el-card>
    <!-- 添加或编辑成册打印申请对话框 -->
    <el-dialog v-model="dialog.visible" :title="dialog.title" width="95%" append-to-body>
      <super-form 
        v-if="dialog.visible"
        :loading="dialog.loading"
        disabled
        ref="superFormRef"
        code="bookPrint-apply-detail-form"
        @selectData="selectData"
        v-model="form"
        :form-rules="rules" 
        :methods="formMethods">
        <el-card shadow="never">
          <template #header>
            <div class="card-header">
              <span>文件清单</span>
              <!-- <el-button size="small" @click="handSelect(true)" type="primary" style="float: right;" icon="Plus">新增</el-button> -->
            </div>
          </template>
          <div ref="fileListContainerRef" class="file-list-container">
            <el-card shadow="hover" v-for="(item, index) in fileList" :key="index" style="margin-bottom: 5px;" >
            <super-form 
              v-if="dialog.visible"
              disabled
              ref="superFormChildRef"
              code="bookPrint-apply-filelist-form"
              @selectData="selectData"
              v-model="fileList[index]"
              :methods="formMethods">
              <template #docCodeSolt="config">
                <el-input v-model="fileList[index][config.prop]" v-bind="config" readonly></el-input>
              </template>
              <template #deleteBtn>
                <!-- <el-button :loading="buttonLoading" v-if="fileList.length > 1" type="danger" style="transform: translate(0,14px);" icon="Delete" @click="deleteFile(index)"></el-button> -->
              </template>
            </super-form>
            </el-card>
          </div>
        </el-card>
      </super-form>
      <template #footer>
        <div class="dialog-footer">
          <!-- <el-button :loading="buttonLoading" type="primary" @click="submitForm">确 定</el-button> -->
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>
    <!-- 提交组件 -->
    <submitVerify ref="submitVerifyRef" :task-variables="taskVariables" @submit-callback="submitCallback" />
  </div>
</template>

<script setup name="BookPrintApprove" lang="ts">
import { listBookPrintApproval, getBookPrint, delBookPrint, addBookPrint, updateBookPrint, submitAndFlowStart } from '@/api/dms/bookPrint/bookPrintApply/index';
import { waitingTaskId, fileOpenCheck } from '@/api/workflow/task';
import { useUserStore } from '@/store/modules/user';
const { proxy } = getCurrentInstance() as ComponentInternalInstance;


const buttonLoading = ref(false);
const loading = ref(false);
const showSearch = ref(true);
const ids = ref<Array<string | number>>([]);
const names = ref<Array<string | number>>([]);
const single = ref(true);
const multiple = ref(true);
const showTable = ref(true);
const total = ref(0);

const superFormRef = ref(null);
const fileListContainerRef = ref<HTMLElement | null>(null);

const dialog = reactive<DialogOption>({
  title: '',
  visible: false,
  loading: false
});

const apprDialog = reactive<DialogOption>({
  title: '请输入提交意见',
  visible: false,
  loading: false
});

const initFormData = {
  tmplDept: useUserStore().deptId,
  approveComments: '',
};
const data = reactive<PageData<any, any>>({
  form: { ...initFormData },
  queryParams: reactive({
    pageNum: 1,
    pageSize: 10,
  }),
  rules: {
    // id: [{ required: true, message: '主键不能为空', trigger: 'blur' }],
  }
});

const { queryParams, form, rules } = toRefs(data);

const recordList = ref([]);

/** 查询成册打印申请列表 */
const getList = async () => {
  loading.value = true;
  try {
    const res = await listBookPrintApproval(queryParams.value);
    recordList.value = res.rows;
    total.value = res.total;
  } finally {
    loading.value = false;
  }
};


const selectData = (data: any, config) => {
  if(config.prop == 'name'){
    form.value.userId = data.id;
    form.value.name = data.name;
    form.value.sex = data.sex;
    form.value.phone = data.phonenumber;
    form.value.email = data.email;
    form.value.deptId = data.deptId;
    form.value.deptName = data.deptName;
  }
};

// 在父组件中预定义所有可能用到的方法
const formMethods = {
  // args参数集合 childFrom参数集合
  customChangeHandler: (args: Array<any>,childFrom: Object) => {
    // superFormRef.value.updateData({ companyName: 'admin' })
    console.log('自定义change事件触发')
  }
}
//提交组件
const submitVerifyRef = ref();
const taskVariables = ref<Record<string, any>>({});
// 提交审批按钮操作
const submitApprove = (row: any) => {
  waitingTaskId(row.workflowId).then((res) => {
    if(res.data){
      submitVerifyRef.value.openDialog(res.data);
      submitVerifyRef.value.init(row.id);
    }
  });
};
//提交回调
const submitCallback = async () => {
  getList();
};

/** 取消按钮 */
const cancel = () => {
  reset();
  dialog.visible = false;
};

// 表单重置
const reset = () => {
  form.value = { ...initFormData };
  fileList.value = [{}];
};

/** 搜索按钮操作 */
const handleQuery = () => {
  queryParams.value.pageNum = 1;
  getList();
};

/** 重置按钮操作 */
const resetQuery = () => {
  handleQuery();
};

/** 多选框选中数据 */
const handleSelectionChange = (selection: any[]) => {
  ids.value = selection.map((item) => item.id);
  names.value = selection.map((item) => item.tmplName);
  single.value = selection.length != 1;
  multiple.value = !selection.length;
};

/** 新增按钮操作 */
const handleAdd = () => {
  reset();
  dialog.visible = true;
  dialog.loading = false;
  dialog.title = '添加成册打印申请';
};

const fileList = ref([{}]);
/** 编辑按钮操作 */
const handleUpdate = async (row?: any) => {
  reset();
  dialog.visible = true;
  dialog.loading = true;
  dialog.title = '编辑成册打印申请';
  const _id = row?.id || ids.value[0];
  const res = await getBookPrint(_id);
  Object.assign(form.value, res.data);
  // 操作类型是失效时，禁用除原因之外的数据(已配置交互规则)
  // if (form.value.tmplDealType === 'invalid') {
  //   disabled = true;
  // }
  dialog.loading = false;
};
/** 详情按钮操作 */
const handleDetail = async (row?: any) => {
  reset();
  dialog.visible = true;
  dialog.loading = true;
  dialog.title = '查看成册打印申请';
  const _id = row?.id || ids.value[0];
  const res: any = await getBookPrint(_id);
  Object.assign(form.value, res.data);
  if(res.data&&res.data.details&&Array.isArray(res.data.details)){
    fileList.value = res.data.details
  }
  dialog.loading = false;
};

/** 提交按钮 */
const submitForm = () => {
  superFormRef.value?.validate(async (valid: boolean) => {
    if (valid) {
      const data = superFormRef.value.achieveData()
      buttonLoading.value = true;
      if (data.id) {
        await updateBookPrint(data).finally(() => (buttonLoading.value = false));
      } else {
        await addBookPrint(data).finally(() => (buttonLoading.value = false));
      }
      proxy?.$modal.msgSuccess('操作成功');
      dialog.visible = false;
      await getList();
    }
  });
};

/** 删除按钮操作 */
const handleDelete = async (row?: any) => {
  const _ids = row?.id || ids.value;
  const _names = row?.tmplName || names.value;
  await proxy?.$modal.confirm('是否确认删除模板名称为"' + _names + '"的数据项？').finally(() => (loading.value = false));
  await delBookPrint(_ids);
  proxy?.$modal.msgSuccess('删除成功');
  await getList();
};

/** 导出按钮操作 */
const handleExport = () => {
  proxy?.download(
    'dms/template-edit/export',
    {
      ...queryParams.value
    },
    `成册打印申请_${new Date().getTime()}.xlsx`
  );
};

onMounted(() => {
  getList();
});
</script>

<style lang="scss" scoped>
.card-header{
  height: 25px;
}

.file-list-container {
  max-height: calc(100vh - 420px);
  overflow-y: auto;
  overflow-x: hidden;
  padding-right: 5px;
  
  /* 自定义滚动条样式 */
  &::-webkit-scrollbar {
    width: 8px;
  }
  
  &::-webkit-scrollbar-track {
    background: #f1f1f1;
    border-radius: 4px;
  }
  
  &::-webkit-scrollbar-thumb {
    background: #888;
    border-radius: 4px;
    
    &:hover {
      background: #555;
    }
  }
}
</style>