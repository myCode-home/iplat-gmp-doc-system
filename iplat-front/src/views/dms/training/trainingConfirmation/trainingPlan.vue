<template>
  <div class="">
    <transition :enter-active-class="proxy?.animate.searchAnimate.enter" :leave-active-class="proxy?.animate.searchAnimate.leave">
      <div v-show="showSearch" class="mb-[10px]">
        <el-card shadow="hover">
          <super-search
            code="docCreate-record-search"
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
            <el-button v-hasPermi="['dms:templateApprove:add']" type="primary" plain icon="Plus" @click="handleAdd">新增</el-button>
          </el-col> -->
          <el-col :span="1.5">
            <el-button v-hasPermi="['dms:templateApprove:edit']" type="primary" icon="CircleCheckFilled" :disabled="multiple" @click="trainConfir()">培训确认</el-button>
          </el-col>
          <!-- <el-col :span="1.5">
            <el-button v-hasPermi="['dms:templateApprove:remove']" type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete()"
              >删除</el-button
            >
          </el-col>
          <el-col :span="1.5">
            <el-button v-hasPermi="['dms:templateApprove:export']" type="warning" plain icon="Download" @click="handleExport">导出</el-button>
          </el-col> -->
          <right-toolbar v-model:show-search="showSearch" @query-table="getList"></right-toolbar>
        </el-row>
      </template>
      <super-table 
        v-if="showTable"
        code="training-record-table"
        rowKey="id"
        :dataLoading="loading"
        :data="temApproveList"
        v-model:queryParams="queryParams"
        :total="total"
        @selection-change="handleSelectionChange"
        @getList="getList"
      >
        <!-- 操作列 -->
        <template #operation="scope">
          <div>
            <!-- <el-tooltip content="详情" placement="top" :enterable="false">
              <el-button link type="primary" v-hasPermi="['dms:docCreate:query']" icon="View" @click="handleDetail(scope.row)"></el-button>
            </el-tooltip> -->
            <el-tooltip content="附件上传" placement="top" :enterable="false">
              <el-button link type="primary" v-hasPermi="['dms:docCreate:upload']" icon="UploadFilled" @click="handleUpload(scope.row)"></el-button>
            </el-tooltip>
          </div>
        </template>
      </super-table>
      <!-- <pagination v-show="total > 0" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :total="total" @pagination="getList" /> -->
    </el-card>
    <!-- 添加或编辑模板审核对话框 -->
    <el-dialog v-model="dialog.visible" :title="dialog.title" width="70%" append-to-body>
      <super-form 
        v-if="dialog.visible"
        :loading="dialog.loading"
        ref="superFormRef"
        disabled
        code="templateEdit-add"
        @selectData="selectData"
        v-model="form"
        :form-rules="rules" 
        :methods="formMethods">
      </super-form>
      <template #footer>
        <div class="dialog-footer">
          <!-- <el-button :loading="buttonLoading" type="primary" @click="submitForm">确 定</el-button> -->
          <el-button @click="cancel">关 闭</el-button>
        </div>
      </template>
    </el-dialog>
    <!-- 附件上传弹窗 -->
    <el-dialog v-model="uplaodDialog.visible" :title="uplaodDialog.title" width="90%" append-to-body>
      <biz-file-upload-list
        v-if="uplaodDialog.visible"
        :biz="{
          bizId: uplaodDialog.id,
          bizType: 'Training_Confirm_File',
        }"
      ></biz-file-upload-list>
      <template #footer>
        <div class="dialog-footer">
          <!-- <el-button :loading="buttonLoading" :disabled="Boolean(!selectRows.id)" type="primary" @click="submitCompare">确 定</el-button> -->
          <el-button @click="uplaodDialog.visible = false">关 闭</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="TemplateApprove" lang="ts">
import { trainingRecordList } from '@/api/dms/training/trainingConfirmation/index';
import { getDocRecord, delDocRecord, addDocRecord, updateDocRecord, submitAndFlowStart } from '@/api/dms/recordAlterationEdit/docRecordEdit/index';
import { waitingTaskId, fileOpenCheck } from '@/api/workflow/task';
import { useUserStore } from '@/store/modules/user';
import BizFileUpload from '@/components/FileUpload/bizFileUpload.vue'
import { listSnapshot } from '@/api/dms/templateManagement/templateSnapshot/index';
const props = defineProps({
  docCreateId: {
    type: String,
    default: ''
  }
})

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
//提交组件
const submitVerifyRef = ref();
const taskVariables = ref<Record<string, any>>({});

const superFormRef = ref(null);

const dialog = reactive<DialogOption>({
  title: '',
  visible: false,
  loading: false
});
const compareDialog = reactive<DialogOption>({
  title: '差异对比',
  visible: false,
  loading: false
});

const initFormData = {
  tmplDept: useUserStore().deptId
};
const data = reactive<PageData<any, any>>({
  form: { ...initFormData },
  queryParams: reactive({
    pageNum: 1,
    pageSize: 10,
    docCreateId: props.docCreateId||undefined
  }),
  rules: {
    // id: [{ required: true, message: '主键不能为空', trigger: 'blur' }],
  }
});

const { queryParams, form, rules } = toRefs(data);

const temApproveList = ref([]);

watch(()=>props.docCreateId, ()=>{
  queryParams.value.docCreateId = props.docCreateId;
  getList();
})

/** 查询模板审核列表 */
const getList = async () => {
  loading.value = true;
  try {
    const res = await trainingRecordList(props.docCreateId);
    temApproveList.value = res.rows;
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
    superFormRef.value.updateData({ companyName: 'admin' })
    console.log('自定义change事件触发')
  },
  handleRoleChange: (args: Array<any>,childFrom: Object) => {
    console.log('自定义事件触发', args, childFrom)
  },
  handleSexChange: (args: Array<any>,childFrom: Object) => {
    // if(args[0] == '2'){
    //   const index = formConfig.findIndex(item=>item.prop == 'zdy')
    //   formConfig[index].visible = true
    // }
    console.log('自定义事件触发', args)
  },
  processCompanyData: (args: Array<any>,childFrom: Object) => {
    console.log('自定义数据处理',args,childFrom)
  }
}

/** 取消按钮 */
const cancel = () => {
  reset();
  dialog.visible = false;
};

// 表单重置
const reset = () => {
  form.value = { ...initFormData };
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
  dialog.title = '添加模板审核';
};

/** 编辑按钮操作 */
const handleUpdate = async (row?: any) => {
  reset();
  dialog.visible = true;
  dialog.loading = true;
  dialog.title = '编辑模板审核';
  const _id = row?.id || ids.value[0];
  const res = await getDocRecord(_id);
  Object.assign(form.value, res.data);
  dialog.loading = false;
};

/** 查看按钮操作 */
const handleDetail = async (row?: any) => {
  reset();
  dialog.visible = true;
  dialog.loading = true;
  dialog.title = '查看模板审核详情';
  const _id = row?.id || ids.value[0];
  const res = await getDocRecord(_id);
  Object.assign(form.value, res.data);
  dialog.loading = false;
};

const uplaodDialog = reactive({
  title: '查看附件',
  visible: false,
  loading: false,
  id: '',
});
// 附件上传按钮操作
const handleUpload = (row: any)=>{
  uplaodDialog.id = row.id;
  uplaodDialog.visible = true;
}

/** 培训确认按钮操作 */
const trainConfir = async (row?: any) => {
  const _ids = row?.id || ids.value;
  await proxy?.$modal.confirm('确定培训吗？').finally(() => (loading.value = false));
  await delDocRecord(_ids);
  proxy?.$modal.msgSuccess('重置成功');
  await getList();
};

/** 提交按钮 */
const submitForm = () => {
  superFormRef.value?.validate(async (valid: boolean) => {
    if (valid) {
      const data = superFormRef.value.achieveData()
      buttonLoading.value = true;
      if (data.id) {
        await updateDocRecord(data).finally(() => (buttonLoading.value = false));
      } else {
        await addDocRecord(data).finally(() => (buttonLoading.value = false));
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
  await delDocRecord(_ids);
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
    `模板审核_${new Date().getTime()}.xlsx`
  );
};

onMounted(() => {
  // getList();
});
</script>
