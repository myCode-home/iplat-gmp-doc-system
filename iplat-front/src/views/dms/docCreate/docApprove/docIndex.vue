<template>
  <div class="">
    <transition :enter-active-class="proxy?.animate.searchAnimate.enter" :leave-active-class="proxy?.animate.searchAnimate.leave">
      <div v-show="showSearch" class="mb-[10px]">
        <el-card shadow="hover">
          <super-search
            code="docCreate-search"
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
          <right-toolbar v-model:show-search="showSearch" @query-table="getList"></right-toolbar>
        </el-row>
      </template>
      <super-table 
        v-if="showTable"
        code="docCreate-Table-Approve"
        rowKey="id"
        :dataLoading="loading"
        :data="temApproveList"
        v-model:queryParams="queryParams"
        :total="total"
        @selection-change="handleSelectionChange"
        @getList="getList"
      >
        <template #documentTypeSlot="scope">
          <dict-tag :options="docTypeOption" :value="scope.row.documentType" valueProp="id" labelProp="sortName" />
        </template>
        <!-- 操作列 -->
        <template #operation="scope">
          <div>
            <el-tooltip content="详情" placement="top" :enterable="false">
              <el-button link type="primary" v-hasPermi="['dms:tmplPreparation:query']" icon="View" @click="handleDetail(scope.row)"></el-button>
            </el-tooltip>
            <el-tooltip content="打开pageOffice" placement="top" :enterable="false">
              <el-button link type="primary" v-hasPermi="['dms:pageoffice:open']" icon="Document" @click="openPageOffice(scope.row)"></el-button>
            </el-tooltip>
            <el-tooltip content="查看附件" placement="top" :enterable="false">
              <el-button link type="primary" v-hasPermi="['dms:docCreate:upload']" icon="UploadFilled" @click="handleUpload(scope.row)"></el-button>
            </el-tooltip>
            <el-tooltip content="提交" placement="top" :enterable="false">
              <el-button link type="primary" :disabled="scope.row.status === 'approved'" :loading="scope.row.subLoading" v-hasPermi="['dms:tmplPreparation:approvalSubmit']" icon="SuccessFilled" @click="submitApprove(scope.row)"></el-button>
            </el-tooltip>
            <el-tooltip content="查看记录" placement="top" :enterable="false">
              <el-button link type="primary" v-hasPermi="['dms:docCreate:record']" icon="Memo" @click="handleRecord(scope.row)"></el-button>
            </el-tooltip>
            <el-tooltip content="差异对比" placement="top" :enterable="false">
              <el-button link type="primary" v-hasPermi="['dms:docCreateSnapshot:list']" icon="Share" @click="handleCompare(scope.row)"></el-button>
            </el-tooltip>
          </div>
        </template>
      </super-table>
      <!-- <pagination v-show="total > 0" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :total="total" @pagination="getList" /> -->
    </el-card>
    <!-- 添加或编辑模板审核对话框 -->
    <el-dialog v-model="dialog.visible" :title="dialog.title" width="90%" append-to-body>
      <super-form 
        v-if="dialog.visible"
        :loading="dialog.loading"
        ref="superFormRef"
        disabled
        code="docCreate-docApprove-form"
        @selectData="selectData"
        v-model="form"
        :form-rules="rules" 
        :methods="formMethods">
        <div>
          <el-card shadow="hover" class="my-card">
            <template #header>
              <div class="card-header">
                <span>替代文件</span>
              </div>
            </template>
            <!-- docCreate-replace-list -->
            <super-table
              code="docCreate-replace-table"
              rowKey="id"
              :dataLoading="supersedeLoading"
              :data="relationList"
              highlight-current-row
            >
            </super-table>
          </el-card>
          <el-card shadow="hover" class="my-card">
            <template #header>
              <div class="card-header">
                <span>关联文件</span>
              </div>
            </template>
            <!-- docCreate-relate-list -->
            <super-table
              code="docCreate-relate-table"
              rowKey="id"
              :dataLoading="linkLoading"
              :data="relateList"
              highlight-current-row
            >
              <template #associatedTypeSlot="scope">
                <el-switch
                  v-model="scope.row.dlType"
                  disabled
                  style="--el-switch-on-color: #13ce66; --el-switch-off-color: #409EFF"
                  active-text="单向"
                  inactive-text="双向"
                />
              </template>
            </super-table>
          </el-card>
        </div>
      </super-form>
      <template #footer>
        <div class="dialog-footer">
          <!-- <el-button :loading="buttonLoading" type="primary" @click="submitForm">确 定</el-button> -->
          <el-button @click="cancel">关 闭</el-button>
        </div>
      </template>
    </el-dialog>
    <el-dialog v-model="compareDialog.visible" :title="compareDialog.title" width="75%" append-to-body>
      <super-table
        v-if="compareDialog.visible"
        code="docCreate-comparison"
        rowKey="id"
        :dataLoading="compareLoading"
        :data="compareList"
        v-model:queryParams="compareQueryParams"
        :total="compareTotal"
        highlight-current-row
        @getList="getCompareList"
        @current-change="handleCurrentChange"
      >
      </super-table>
      <template #footer>
        <div class="dialog-footer">
          <el-button :loading="buttonLoading" :disabled="Boolean(!selectRows.id)" type="primary" @click="submitCompare">确 定</el-button>
          <el-button @click="compareDialog.visible = false">取 消</el-button>
        </div>
      </template>
    </el-dialog>
    <!-- 附件上传弹窗 -->
    <el-dialog v-model="uplaodDialog.visible" :title="uplaodDialog.title" width="90%" append-to-body>
      <biz-file-upload-list
        v-if="uplaodDialog.visible"
        :biz="{
          bizId: uplaodDialog.id,
          bizType: 'DOC_ATTACHMENT_FILE',
        }"
      ></biz-file-upload-list>
      <template #footer>
        <div class="dialog-footer">
          <!-- <el-button :loading="buttonLoading" :disabled="Boolean(!selectRows.id)" type="primary" @click="submitCompare">确 定</el-button> -->
          <el-button @click="uplaodDialog.visible = false">关 闭</el-button>
        </div>
      </template>
    </el-dialog>
    <!-- 提交组件 -->
    <submitVerify ref="submitVerifyRef" :task-variables="taskVariables" @submit-callback="submitCallback" />
  </div>
</template>

<script setup name="TemplateApprove" lang="ts">
import { listDocCreateApproval, getDocCreateEdit, delDocCreateEdit, addDocCreateEdit, updateDocCreateEdit, listDocCreateSupersede, listDocCreateLink } from '@/api/dms/docCreate/docEdit/index';
import { waitingTaskId, fileOpenCheck } from '@/api/workflow/task';
import { useUserStore } from '@/store/modules/user';
import BizFileUpload from '@/components/FileUpload/bizFileUpload.vue'
import { listSnapshot } from '@/api/dms/docCreate/docSnapshot/index';
import { getAttributeList, documentList } from '@/api/dms/fileSystemOption/fileType/index';
const { proxy } = getCurrentInstance() as ComponentInternalInstance;

const emits = defineEmits(['switchToRecordTab']);


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
  }),
  rules: {
    // id: [{ required: true, message: '主键不能为空', trigger: 'blur' }],
  }
});

const { queryParams, form, rules } = toRefs(data);

const temApproveList = ref([]);

/** 查询模板审核列表 */
const getList = async () => {
  loading.value = true;
  try {
    const res = await listDocCreateApproval(queryParams.value);
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

const handleRecord = (row: any)=>{
  emits('switchToRecordTab', row);
}

const compareList = ref([]);
const compareTotal = ref(0);
const compareLoading= ref(false);
const selectRows = ref({id: ''});
const compareQueryParams = ref({
  pageNum: 1,
  pageSize: 10,
  bizId: '',
  documentVersionMinor: '',
  documentCode: '',
  documentVersion: '',
});
const getCompareList = async () => {
  compareLoading.value = true;
  listSnapshot(compareQueryParams.value).then((res)=>{
    compareList.value = res.rows;
    compareTotal.value = res.total;
  }).finally(()=>{
    compareLoading.value = false;
  })
}
const handleCurrentChange = (rows: any) => {
  selectRows.value = rows;
}
// 差异对比，打开pageoffice
const submitCompare = () => {
  const paramJson = {
    bizId: compareQueryParams.value.bizId,
    compareBizId: selectRows.value.id,
  };
  console.log(paramJson)
  const paramString = JSON.stringify(paramJson);
  //openWindow()第三个参数用来向弹出的PageOffice浏览器（POBrowser）窗口传递参数(参数长度不限)，支持json格式字符串。
  //此处为了方便演示，我们传递了file_id和file_name两个参数，具体以您实际开发为准。
  proxy?.POBrowser.openWindow('/docCompare', 'width=1200px;height=800px;', paramString);
}

// 差异对比按钮操作
const handleCompare = (row: any)=>{
  compareDialog.visible = true;
  compareDialog.title = '差异对比';
  compareQueryParams.value.pageNum = 1;
  compareQueryParams.value.bizId = row.id;
  compareQueryParams.value.documentCode = row.documentCode;
  compareQueryParams.value.documentVersion = row.documentVersion;
  compareQueryParams.value.documentVersionMinor = row.documentVersionMinor;
  selectRows.value = {id: ''};
  compareList.value = [];
  getCompareList();
}

// 打开pageoffice
const openPageOffice = (row: any) => {
  const paramJson = {
    id: row.id
  };
  const paramString = JSON.stringify(paramJson);
  //openWindow()第三个参数用来向弹出的PageOffice浏览器（POBrowser）窗口传递参数(参数长度不限)，支持json格式字符串。
  //此处为了方便演示，我们传递了file_id和file_name两个参数，具体以您实际开发为准。
  proxy?.POBrowser.openWindow('/docEdit', 'width=1200px;height=800px;', paramString);
};

// 提交审批按钮操作
const submitApprove = (row: any) => {
  row.subLoading = true;
  fileOpenCheck({fileBusinessId: row.id}).then(res=>{
    waitingTaskId(row.workflowId).then((res) => {
      if(res.data){
        submitVerifyRef.value.openDialog(res.data);
        submitVerifyRef.value.init(row.id);
      }
    });
  }).finally(()=>{
    row.subLoading = false;
  })
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
  relationList.value = [];
  relateList.value = [];
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
  const res = await getDocCreateEdit(_id);
  Object.assign(form.value, res.data);
  dialog.loading = false;
};
// 替代文件列表
const relationList = ref([]);
// 关联文件列表
const relateList = ref([]);
/** 查看按钮操作 */
const handleDetail = async (row?: any) => {
  reset();
  dialog.visible = true;
  dialog.loading = true;
  dialog.title = '查看模板审核详情';
  const _id = row?.id || ids.value[0];
  getSupersedeList(row?.id);
  getLinkList(row?.id);
  const res = await getDocCreateEdit(_id);
  Object.assign(form.value, res.data);
  dialog.loading = false;
};
const supersedeLoading = ref(false);
// 查询替代文件列表
const getSupersedeList = async (id: string) => {
  supersedeLoading.value = true;
  const res = await listDocCreateSupersede({ docCreateId: id });
  supersedeLoading.value = false;
  relationList.value = res.data;
};
const linkLoading = ref(false);
// 查询关联文件列表
const getLinkList = async (id: string) => {
  linkLoading.value = true;
  const res = await listDocCreateLink({ docCreateId: id });
  linkLoading.value = false;
  relateList.value = res.data;
};

/** 提交按钮 */
const submitForm = () => {
  superFormRef.value?.validate(async (valid: boolean) => {
    if (valid) {
      const data = superFormRef.value.achieveData()
      buttonLoading.value = true;
      if (data.id) {
        await updateDocCreateEdit(data).finally(() => (buttonLoading.value = false));
      } else {
        await addDocCreateEdit(data).finally(() => (buttonLoading.value = false));
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
  await delDocCreateEdit(_ids);
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

const docTypeOption = ref([]);
const getDocumentList = () => {
  documentList({
    pageNum: 1,
    pageSize: 9999
  }).then((res) => {
    docTypeOption.value = res.rows;
  });
};

onMounted(() => {
  getList();
  getDocumentList();
});
</script>

<style lang="scss" scoped>
.my-card{
  margin-top: 10px;
}
</style>
