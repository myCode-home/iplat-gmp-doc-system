<template>
  <div class="p-2">
    <transition :enter-active-class="proxy?.animate.searchAnimate.enter" :leave-active-class="proxy?.animate.searchAnimate.leave">
      <div v-show="showSearch" class="mb-[10px]">
        <el-card shadow="hover">
          <super-search
            code="action_template_select-search"
            v-model="queryParams"
            autoGetList
            @resetQuery="resetQuery"
          >
          </super-search>
        </el-card>
      </div>
    </transition>

    <el-card shadow="hover">
      <template #header>
        <el-row :gutter="10" class="mb8">
          <right-toolbar v-model:show-search="showSearch" @query-table="getList"></right-toolbar>
        </el-row>
      </template>
      <super-table 
        v-if="showTable"
        code="action_template-select-table"
        :class="{nomultiple: !multipleChoice}"
        ref="superTableRef"
        rowKey="id"
        :dataLoading="loading"
        :data="temApproveList"
        v-model:queryParams="queryParams"
        :total="total"
        @getList="getList"
        @select="selectChange"
        @selection-change="handleSelectionChange"
      >
      </super-table>
      <!-- <pagination v-show="total > 0" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :total="total" @pagination="getList" /> -->
    </el-card>
    <!-- 添加或编辑模板审核对话框 -->
    <el-dialog v-model="dialog.visible" :title="dialog.title" width="70%" append-to-body>
      <super-form 
        v-if="dialog.visible"
        :loading="dialog.loading"
        ref="superFormRef"
        code="templateLibrary-form-edit"
        v-model="form"
        :form-rules="rules" 
        :methods="formMethods">
      </super-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button :loading="buttonLoading" type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">关 闭</el-button>
        </div>
      </template>
    </el-dialog>
    <!-- 查看生效模板详情对话框 -->
    <el-dialog v-model="detailDialog.visible" :title="detailDialog.title" width="90%" append-to-body>
      <super-form 
        v-if="detailDialog.visible"
        :loading="detailDialog.loading"
        ref="superFormRef"
        code="templateLibrary-form-view"
        v-model="form"
        :form-rules="rules" 
        :methods="formMethods">
        <PdfView v-if="form.id" :text="waterText" :id="form.id"></PdfView>
      </super-form>
      <template #footer>
        <div class="dialog-footer">
          <!-- <el-button :loading="buttonLoading" type="primary" @click="submitForm">确 定</el-button> -->
          <el-button @click="detailDialog.visible = false">关 闭</el-button>
        </div>
      </template>
    </el-dialog>
    <!-- 查看历史模板详情对话框 -->
    <el-dialog v-model="historyDialog.visible" :title="historyDialog.title" width="90%" append-to-body>
      <super-form 
        v-if="historyDialog.visible"
        :loading="historyDialog.loading"
        ref="superFormRef"
        code="templateLibrary-history-form-view"
        v-model="form"
        :form-rules="rules" 
        :methods="formMethods">
        <PdfView v-if="form.id" :text="waterText" :id="form.id"></PdfView>
      </super-form>
      <template #footer>
        <div class="dialog-footer">
          <!-- <el-button :loading="buttonLoading" type="primary" @click="submitForm">确 定</el-button> -->
          <el-button @click="historyDialog.visible = false">关 闭</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="TemplateActiveLibrarySelect" lang="ts">
import { listTmplLibActive, listTmplLib, getTmplLib, delTmplLib, reviseTmplLib, invalidTmplLib } from '@/api/dms/templateManagement/templateActiveLibrary/index';
import { getTemplateFile } from '@/api/dms/templateManagement/templateEdit/index';
import { useUserStore } from '@/store/modules/user';
import BizFileUpload from '@/components/FileUpload/bizFileUpload.vue'
const props = defineProps({
  multipleChoice: {
    type: Boolean,
    default: true
  },
  otherCondition: {
    type: Object,
    default: () =>{}
  },
  multipleChoiceData: {
    type: Function,
    default: () => ()=>{}
  }
})

const { proxy } = getCurrentInstance() as ComponentInternalInstance;
const { dms_template_type } = toRefs<any>(proxy?.useDict('dms_template_type'));


const buttonLoading = ref(false);
const loading = ref(false);
const showSearch = ref(true);
const ids = ref<Array<string | number>>([]);
const names = ref<Array<string | number>>([]);
const single = ref(true);
const multiple = ref(true);
const showTable = ref(true);
const total = ref(0);
const taskVariables = ref<Record<string, any>>({});

const superFormRef = ref(null);

const dialog = reactive<DialogOption>({
  title: '',
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
    activeStatus: 'active',
    tmplPurpose: 'record',
  }),
  rules: {
    // id: [{ required: true, message: '主键不能为空', trigger: 'blur' }],
  }
});

const { queryParams, form, rules } = toRefs(data);

watch(() => props.otherCondition, () => {
  Object.keys(props.otherCondition).forEach(key => {
    queryParams.value[key] = props.otherCondition[key];
  });
}, { immediate: true, deep: true });

const temApproveList = ref([]);

/** 查询模板审核列表 */
const getList = async () => {
  loading.value = true;
  try {
    const res = await listTmplLibActive(queryParams.value);
    temApproveList.value = res.rows;
    total.value = res.total;
  } finally {
    loading.value = false;
  }
};

// 展开或者关闭行
const handleExpandChange = (...args: any[])=>{
  const [row, expandedRows] = args;
  if(expandedRows.findIndex(item => item.id === row.id) !== -1){
    row.loading = true;
    listTmplLib({ tmplCode: row.tmplCode }).then(res=>{
      row.itemData = res.rows;
    }).finally(()=>{
      row.loading = false;
    })
  }
}

//申请失效
const applyInvalidation = async (row: any) => {
  reset();
  dialog.visible = true;
  dialog.loading = true;
  dialog.title = '模板失效';
  const _id = row?.id || ids.value[0];
  const res = await getTmplLib(_id);
  Object.assign(form.value, res.data);
  form.value.status = 'failure',
  dialog.loading = false;
};

//审批记录
const handleDownLoad = async (row: any) => {
  const res = await getTemplateFile(row.id)
  if(res.data){
    const data = typeof res.data === 'string' ? res.data : String(res.data);
    proxy?.$download.oss(data);
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

// 打开pageoffice
const openPageOffice = (row: any) => {
  const paramJson = {
    id: row.id,
    file_id: 1,
    file_name: "test.doc",
  };
  const paramString = JSON.stringify(paramJson);
  //openWindow()第三个参数用来向弹出的PageOffice浏览器（POBrowser）窗口传递参数(参数长度不限)，支持json格式字符串。
  //此处为了方便演示，我们传递了file_id和file_name两个参数，具体以您实际开发为准。
  proxy?.POBrowser.openWindow('/template', 'width=1200px;height=800px;', paramString);
  console.log(row)
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
const superTableRef = ref<ElTableInstance>();
const selectChange = (selection) =>{
  if(!props.multipleChoice){
    if(selection.length > 1){
      const del_row = selection.shift()
      superTableRef.value.toggleRowSelection(del_row, false)
    }
  }
}
const selectData = ref([]);
/** 多选框选中数据 */
const handleSelectionChange = (selection: any[]) => {
  ids.value = selection.map((item) => item.id);
  names.value = selection.map((item) => item.tmplName);
  single.value = selection.length != 1;
  multiple.value = !selection.length;
  selectData.value = selection;
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
  dialog.title = '模板修订';
  const _id = row?.id || ids.value[0];
  const res = await getTmplLib(_id);
  Object.assign(form.value, res.data);
  dialog.loading = false;
};

const detailDialog = reactive<DialogOption>({
  title: '',
  visible: false,
  loading: false
});

const pdfUrl = ref("")
const waterText = ref('')

/** 查看生效模板库按钮操作 */
const handleDetail = async (row?: any) => {
  reset();
  form.value.id = row?.id;
  detailDialog.visible = true;
  detailDialog.loading = true;
  detailDialog.title = '模板详情';
  const _id = row?.id || ids.value[0];
  const res = await getTmplLib(_id);
  Object.assign(form.value, res.data);
  detailDialog.loading = false;
  const { name, nickname } = useUserStore();
  const time = proxy?.parseTime(new Date(), '{y}-{m}-{d}')
  waterText.value = `${name} ${nickname} ${time} 生效`
};

const historyDialog = reactive<DialogOption>({
  title: '',
  visible: false,
  loading: false
});
/** 查看历史模板库按钮操作 */
const handleHistoryDetail = async (row?: any) => {
  reset();
  form.value.id = row?.id;
  historyDialog.visible = true;
  historyDialog.loading = true;
  historyDialog.title = '模板详情';
  const _id = row?.id || ids.value[0];
  const res = await getTmplLib(_id);
  Object.assign(form.value, res.data);
  historyDialog.loading = false;
  const { name, nickname } = useUserStore();
  const time = proxy?.parseTime(new Date(), '{y}-{m}-{d}')
  waterText.value = `${name} ${nickname} ${time} 失效`
};

/** 提交按钮 */
const submitForm = () => {
  superFormRef.value?.validate(async (valid: boolean) => {
    if (valid) {
      const data = superFormRef.value.achieveData()
      buttonLoading.value = true;
      if (data.status == 'failure') {
        // 申请失效
        await invalidTmplLib(data).finally(() => (buttonLoading.value = false));
      } else {
        // 申请修订
        await reviseTmplLib(data).finally(() => (buttonLoading.value = false));
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
  await delTmplLib(_ids);
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

/** 提交按钮操作 */
const confirmChoiceData = async () => {
  if(selectData.value.length == 0){
    return proxy?.$modal.msgError('请选择');
  }
  const data = selectData.value.map((item) => {
    // item.id = item.tmplCode
    item.name = item.tmplName
    return item
  })
  if(props.multipleChoice){
    props.multipleChoiceData(data);
  }else{
    props.multipleChoiceData(data[0]);
  }
};

onMounted(() => {
  getList();
});
// 暴露方法
defineExpose({
  confirmChoiceData
})
</script>

<style lang="scss" scoped>
#pdf-container {
  width: 100%;
  height: 100%;
  overflow: hidden;
}
.pdf-frame {
  width: 100%;
  height: 100%;
}
</style>
