<template>
  <div class="p-2">
    <!-- <transition :enter-active-class="proxy?.animate.searchAnimate.enter" :leave-active-class="proxy?.animate.searchAnimate.leave">
      <div v-show="showSearch" class="mb-[10px]">
        <el-card shadow="hover">
          <super-search
            code="recordAlteration-search"
            v-model="queryParams"
            @getList="getList"
            @resetQuery="resetQuery"
          >
          </super-search>
        </el-card>
      </div>
    </transition> -->

    <el-card shadow="hover">
      <!-- <template #header>
        <el-row :gutter="10" class="mb8">
          <el-col :span="1.5">
            <el-button v-hasPermi="['dms:volumesPrintApply:add']" type="primary" icon="Plus" @click="handleAdd">新增</el-button>
          </el-col>
          <right-toolbar v-model:show-search="showSearch" @query-table="getList"></right-toolbar>
        </el-row>
      </template> -->
      <super-table 
        code="bookPrint-apply-track-book-table"
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
        <!-- <template #operation="scope">
          <div>
            <el-tooltip content="编辑" placement="top" :enterable="false">
              <el-button link type="primary" v-hasPermi="['dms:volumesPrintApply:edit']" icon="EditPen" @click="handleUpdate(scope.row)"></el-button>
            </el-tooltip>
            <el-tooltip content="删除" placement="top" :enterable="false">
              <el-button link type="danger" v-hasPermi="['dms:volumesPrintApply:remove']" icon="Delete" @click="handleDelete(scope.row)"></el-button>
            </el-tooltip>
            <el-tooltip content="提交" placement="top" :enterable="false">
              <el-button link type="primary" v-hasPermi="['dms:volumesPrintApply:submit']" icon="SuccessFilled" @click="openApprove(scope.row)"></el-button>
            </el-tooltip>
          </div>
        </template> -->
      </super-table>
      <!-- <pagination v-show="total > 0" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :total="total" @pagination="getList" /> -->
    </el-card>
    <!-- 添加或编辑记录变更申请对话框 -->
    <el-dialog v-model="dialog.visible" :title="dialog.title" width="80%" append-to-body>
      <super-form 
        v-if="dialog.visible"
        :loading="dialog.loading"
        ref="superFormRef"
        code="recordAlteration-form"
        @selectData="selectData"
        :otherCondition="otherCondition"
        v-model="form"
        :form-rules="rules" 
        :methods="formMethods">
      </super-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button :loading="buttonLoading" type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>
    <el-dialog v-model="apprDialog.visible" :title="apprDialog.title" width="500px" append-to-body>
      <el-form>
        <el-form-item label="提交意见" prop="approveComments">
          <el-input v-model="apprRow.approveComments" type="textarea" placeholder="请输入提交意见"></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button :loading="buttonLoading" type="primary" @click="submitApprove">确 定</el-button>
          <el-button @click="apprCancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="BookPrint" lang="ts">
import { getBookPrintFilTrackInfo, getBookPrint, delBookPrint, addBookPrint, updateBookPrint, submitAndFlowStart } from '@/api/dms/bookPrint/bookPrintApply/index';
import { fileOpenCheck } from '@/api/workflow/task';
import { useUserStore } from '@/store/modules/user';
const { proxy } = getCurrentInstance() as ComponentInternalInstance;

const props = defineProps({
  volumesPrintId: {
    type: String,
    default: ''
  }
})


const buttonLoading = ref(false);
const loading = ref(false);
const showSearch = ref(true);
const ids = ref<Array<string | number>>([]);
const names = ref<Array<string | number>>([]);
const single = ref(true);
const multiple = ref(true);
const total = ref(0);

const superFormRef = ref(null);

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
  drafterId: useUserStore().userId,
  drafterUserName: useUserStore().nickname,
  approveComments: '',
  operate: ''
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

/** 查询记录变更申请列表 */
const getList = async () => {
  loading.value = true;
  try {
    const res = await getBookPrintFilTrackInfo(props.volumesPrintId);
    recordList.value = res.data;
    // total.value = res.total;
  } finally {
    loading.value = false;
  }
};

// 筛选条件
const otherCondition = ref({
  documentRecordCode: {
    activeStatus: 'active'
  }
});
const selectData = (data: any, config) => {
  if(config.prop == 'drafterUserName'){
    form.value.drafterId = data.id;
    form.value.drafterUserName = data.name;
  } else if(config.prop == 'documentRecordCode'){
    form.value.documentCode = data.dlCode;
    form.value.documentName = data.dlName;
    form.value.documentRecordLibraryId = data.id;
    form.value.documentRecordCode = data.dlrCode;
    form.value.documentRecordName = data.dlrName;
    form.value.documentRecordType = data.dlrType;
    form.value.editWay = data.editWay;
    form.value.tmplLibId = data.tmplLibId;
    form.value.tmplLibCode = data.tmplLibCode;
    form.value.tmplLibName = data.tmplLibName;
  } else if(config.prop == 'tmplLibName'){
    form.value.tmplLibId = data.id;
    form.value.tmplLibCode = data.tmplCode;
    form.value.tmplLibName = data.tmplName;
  }

};

// 在父组件中预定义所有可能用到的方法
const formMethods = {
  // args参数集合 childFrom参数集合
  editWayChange: (args: Array<any>,childFrom: Object) => {
    // 编辑方式为线下，清除模板库选择
    if(args[0] == 'offline'){
      form.value.tmplLibId = '';
      form.value.tmplLibCode = '';
      form.value.tmplLibName = '';
    }
  },
}

const apprRow = ref();
// 提交审批按钮操作
const openApprove = async (row: any) => {
  apprDialog.visible = true;
  apprRow.value = JSON.parse(JSON.stringify(row));
  apprRow.value.approveComments = '';
};

/** 取消按钮 */
const apprCancel = () => {
  apprDialog.visible = false;
  apprRow.value.approveComments = '';
};

// 提交审批按钮操作
const submitApprove = () => {
  apprDialog.visible = true;
  buttonLoading.value = true;
  submitAndFlowStart(apprRow.value).then(() => {
    proxy?.$modal.msgSuccess('提交成功');
    apprCancel();
    getList();
  }).finally(()=>{
    buttonLoading.value = false;
  })
};



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
  form.value.operate = 'add';
  dialog.visible = true;
  dialog.loading = false;
  dialog.title = '添加记录变更申请';
};

/** 编辑按钮操作 */
const handleUpdate = async (row?: any) => {
  reset();
  dialog.visible = true;
  dialog.loading = true;
  dialog.title = '编辑记录变更申请';
  const _id = row?.id || ids.value[0];
  const res = await getBookPrint(_id);
  Object.assign(form.value, res.data);
  form.value.operate = 'edit';
  // 操作类型是失效时，禁用除原因之外的数据(已配置交互规则)
  // if (form.value.tmplDealType === 'invalid') {
  //   disabled = true;
  // }
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
    `记录变更申请_${new Date().getTime()}.xlsx`
  );
};

onMounted(() => {
  getList();
});
</script>
