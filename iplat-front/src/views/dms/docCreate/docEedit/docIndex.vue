<template>
  <div class="p-2">
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
          <el-col :span="1.5">
            <el-button v-hasPermi="['dms:docCreate:add']" type="primary" icon="Plus" @click="handleAdd">新增</el-button>
          </el-col>
          <!-- <el-col :span="1.5">
            <el-button v-hasPermi="['dms:docCreate:edit']" type="success" plain icon="Edit" :disabled="single" @click="handleUpdate()">编辑</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button v-hasPermi="['dms:docCreate:remove']" type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete()"
              >删除</el-button
            >
          </el-col>
          <el-col :span="1.5">
            <el-button v-hasPermi="['dms:templateEdit:export']" type="warning" plain icon="Download" @click="handleExport">导出</el-button>
          </el-col> -->
          <right-toolbar v-model:show-search="showSearch" @query-table="getList"></right-toolbar>
        </el-row>
      </template>
      <!-- 离线文件上传 -->
      <input type="file" id="file" ref="selectFileRef" @change="selectFileChange" style="display:none" accept=".doc,.docx,.docm">
      <super-table 
        v-if="showTable"
        code="docCreate-Table-Edit"
        rowKey="id"
        highlightCurrentRow
        :dataLoading="loading"
        :data="templateEditList"
        v-model:queryParams="queryParams"
        :total="total"
        @selection-change="handleSelectionChange"
        @getList="getList"
      >
        <!-- 文件类型 -->
        <template #documentTypeSlot="scope">
          <dict-tag :options="docTypeOption" :value="scope.row.documentType" valueProp="id" labelProp="sortName" />
        </template>
        <!-- 操作列 -->
        <template #operation="scope">
          <div>
            <!-- 除上传之外的来源都可编辑 -->
            <el-tooltip content="编辑" placement="top" :enterable="false">
              <el-button link type="primary" v-if="scope.row.operateSource !== 'fromUpload'" v-hasPermi="['dms:docCreate:edit']" icon="EditPen" @click="handleUpdate(scope.row)"></el-button>
            </el-tooltip>
            <!-- 上传可查看详情 -->
            <el-tooltip content="查看详情" placement="top" :enterable="false">
              <el-button link type="primary" v-if="scope.row.operateSource == 'fromUpload'" v-hasPermi="['dms:docCreate:detail']" icon="View" @click="handleUpdate(scope.row)"></el-button>
            </el-tooltip>
            <!-- 除了手动录入和手动修订可以删除其他不能删除 -->
            <el-tooltip content="删除" placement="top" :enterable="false">
              <el-button link type="danger" :disabled="scope.row.operateSource !== 'fromInput' && scope.row.operateSource !== 'manualRevision'" v-hasPermi="['dms:docCreate:remove']" icon="Delete" @click="handleDelete(scope.row)"></el-button>
            </el-tooltip>
            <!-- 如果编辑方式为offline，并且文件状态为编辑或退回，并且编辑状态（trainStatus）为未上传（notUploaded），则隐去PageOffice按钮和提交按钮 -->
            <el-tooltip content="打开pageOffice" placement="top" :enterable="false">
              <el-button link type="primary" v-if="!(scope.row.editWay === 'offline' && (scope.row.status === 'edit' || scope.row.status === 'retreat') && scope.row.trainStatus === 'notUploaded')" v-hasPermi="['dms:docCreate:open']" icon="Document" @click="openPageOffice(scope.row)"></el-button>
            </el-tooltip>
            <el-tooltip content="附件上传" placement="top" :enterable="false">
              <el-button link type="primary" v-hasPermi="['dms:docCreate:upload']" icon="UploadFilled" @click="handleUpload(scope.row)"></el-button>
            </el-tooltip>
            <!-- 如果编辑方式为offline，并且文件状态为编辑或退回，并且编辑状态（trainStatus）为未上传（notUploaded），则隐去PageOffice按钮和提交按钮 -->
            <!-- <el-popconfirm
              title="确定提交当前行数据吗？"
              @confirm="openApprove(scope.row)"
            >
              <template #reference>
                <span style="margin: 0 12px;">
                </span>
              </template>
            </el-popconfirm> -->
            <el-tooltip content="提交" placement="top" :enterable="false">
              <el-button link type="primary" 
                :loading="scope.row.subLoading"
                @click="openApprove(scope.row)"
                v-if="!(scope.row.editWay === 'offline' && (scope.row.status === 'edit' || scope.row.status === 'retreat') && scope.row.trainStatus === 'notUploaded')" 
                v-hasPermi="['dms:docCreate:submit']" icon="SuccessFilled">
              </el-button>
            </el-tooltip>
            <el-tooltip content="记录" placement="top" :enterable="false">
              <el-button link type="primary" v-hasPermi="['dms:documentRecordLib:list']" icon="Memo" @click="handleRecord(scope.row)"></el-button>
            </el-tooltip>
            <!-- 如果文件状态为已提交，隐藏 -->
            <!-- 编辑方式为offline,文件状态不为编辑和退回的,隐藏 -->
            <el-tooltip content="离线文件上传" placement="top" :enterable="false">
              <el-button link type="primary" v-if="canOfflineUpload(scope.row)" v-hasPermi="['dms:docCreate:offlineUpload']" icon="Edit" @click="handleOnlineUpload(scope.row)"></el-button>
            </el-tooltip>
          </div>
        </template>
      </super-table>
      <!-- <pagination v-show="total > 0" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :total="total" @pagination="getList" /> -->
    </el-card>
    <!-- 添加或编辑文档编制对话框 -->
    <el-dialog v-model="dialog.visible" :title="dialog.title" width="90%" append-to-body>
      <doc-operate 
        ref="docOperateRef" 
        v-if="dialog.visible" 
        v-loading="dialog.loading"
        v-model:buttonLoading="buttonLoading"
        :operate="operate" 
        :choseRow="choseRow" 
        :generateDoc="generateDoc" 
        :showBtachApply="showBtachApply"
        @closeDialog="closeDialog">
      </doc-operate>
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
    <!-- 差异对比弹窗 -->
    <el-dialog v-model="compareDialog.visible" :title="compareDialog.title" width="75%" append-to-body>
      <super-table
        v-if="compareDialog.visible"
        code="templateEdit-comparison"
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
  </div>
</template>

<script setup name="DocEedit" lang="ts">
import { listDocCreateEdit, getDocCreateEdit, delDocCreateEdit, addDocCreateEdit, updateDocCreateEdit, submitAndFlowStart, submitOfflineFile } from '@/api/dms/docCreate/docEdit/index';
import { fileOpenCheck } from '@/api/workflow/task';
import { useUserStore } from '@/store/modules/user';
import { listSnapshot } from '@/api/dms/templateManagement/templateSnapshot/index';
import DocOperate from '@/views/dms/docCreate/docEedit/doc-operate.vue';
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

const docOperateRef = ref(null);

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
const templateEditList = ref([]);
/** 查询文档编制列表 */
const getList = async () => {
  loading.value = true;
  try {
    const res = await listDocCreateEdit(queryParams.value);
    templateEditList.value = res.rows;
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
    docOperateRef.value.updateData({ companyName: 'admin' })
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
    id: row.id
  };
  const paramString = JSON.stringify(paramJson);
  //openWindow()第三个参数用来向弹出的PageOffice浏览器（POBrowser）窗口传递参数(参数长度不限)，支持json格式字符串。
  //此处为了方便演示，我们传递了file_id和file_name两个参数，具体以您实际开发为准。
  proxy?.POBrowser.openWindow('/docEdit', 'width=1200px;height=800px;', paramString);
  // console.log(row)
};


const compareDialog = reactive<DialogOption>({
  title: '差异对比',
  visible: false,
  loading: false
});
const compareList = ref([]);
const compareTotal = ref(0);
const compareLoading= ref(false);
const selectRows = ref({id: ''});
const compareQueryParams = ref({
  pageNum: 1,
  pageSize: 10,
  bizId: '',
  tmplCode: '',
  tmplVersion: '',
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
  proxy?.POBrowser.openWindow('/tempCompare', 'width=1200px;height=800px;', paramString);
}

const uplaodDialog = reactive({
  title: '上传附件',
  visible: false,
  loading: false,
  id: '',
});
// 附件上传按钮操作
const handleUpload = (row: any)=>{
  uplaodDialog.id = row.id;
  uplaodDialog.visible = true;
}

// 判断是否可以离线上传
const canOfflineUpload = computed(() => {
  return function (row: any){
    if(row.editWay === 'online' || row.status === 'commit'){
      return false;
    } else if(row.editWay === 'offline' && (row.status !== 'edit' && row.status !== 'retreat')){
      return false;
    } else {
      return true;
    }
  }
})
const offlineRow = ref({});
const selectFileRef = ref(null);
// 离线文件上传按钮操作
const handleOnlineUpload = (row: any)=>{
  offlineRow.value = row;
  offlineRow.value['document'] = 1;
  selectFileRef.value.click();
}

// 离线文件上传按钮操作
const selectFileChange = (e: any)=>{
  let limitType = '.doc,.docx,.docm';
  let fileName = selectFileRef.value.value;
  let extName = fileName.substr(fileName.lastIndexOf('.')).toLowerCase(); // 获取后缀名
  if (limitType.indexOf(extName) <= -1) {
    proxy.$modal.msgError('只能上传doc、docx、docm文件');
    return false;
  }
  let files = e.target.files;
  let selectedFilesData = Array.prototype.slice.call(files);
  loading.value = true;
  submitOfflineFile(offlineRow.value['id'],{file: selectedFilesData[0]}).then(res=>{
    proxy.$modal.msgSuccess('上传成功');
  }).finally(()=>{
    loading.value = false;
  })
}


// 差异对比按钮操作
const handleCompare = (row: any)=>{
  compareDialog.visible = true;
  compareDialog.title = '差异对比';
  compareQueryParams.value.pageNum = 1;
  compareQueryParams.value.tmplCode = row.tmplCode;
  compareQueryParams.value.tmplVersion = row.tmplVersion;
  compareQueryParams.value.bizId = row.id;
  selectRows.value = {id: ''};
  compareList.value = [];
  getCompareList();
}

const handleRecord = (row: any)=>{
  emits('switchToRecordTab', row);
}

const apprRow = ref();
// 提交审批按钮操作
const openApprove = (row: any) => {
  row.subLoading = true;
  fileOpenCheck({fileBusinessId: row.id}).then(res=>{
    apprDialog.visible = true;
    apprRow.value = JSON.parse(JSON.stringify(row));
    apprRow.value.approveComments = '';
  }).finally(()=>{
    row.subLoading = false;
  })
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
  choseRow.value = {};
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
  names.value = selection.map((item) => item.documentName);
  single.value = selection.length != 1;
  multiple.value = !selection.length;
};

const choseRow = ref({});
const operate = ref('');
const generateDoc = ref();
const showBtachApply = ref();
/** 新增按钮操作 */
const handleAdd = async () => {
  reset();
  dialog.visible = true;
  dialog.loading = true;
  generateDoc.value = await querySystemKey('dms_isProduceDoc');
  showBtachApply.value = await querySystemKey('dms_isBatchapplyForm');
  dialog.loading = false;
  operate.value = 'add';
  dialog.title = '添加文档编制';
};

/** 编辑按钮操作 */
const handleUpdate = async (row?: any) => {
  reset();
  dialog.visible = true;
  dialog.loading = true;
  dialog.title = '编辑文档编制';
  operate.value = 'edit';
  const _id = row?.id || ids.value[0];
  const res = await getDocCreateEdit(_id);
  choseRow.value = res.data
  dialog.loading = false;
};

// 关闭弹窗
const closeDialog = ()=>{
  dialog.visible = false;
  getList();
};

/** 提交按钮 */
const submitForm = () => {
  docOperateRef.value?.submitForm();
};

/** 删除按钮操作 */
const handleDelete = async (row?: any) => {
  const _ids = row?.id || ids.value;
  const _names = row?.documentName || names.value;
  await proxy?.$modal.confirm('是否确认删除文件名称为"' + _names + '"的数据项？').finally(() => (loading.value = false));
  await delDocCreateEdit(_ids);
  proxy?.$modal.msgSuccess('删除成功');
  await getList();
};


// 查询系统配置
const querySystemKey = (key)=> {
  return new Promise((resolve) => {
    proxy?.getConfigKey(key).then(({ data }) => {
      resolve(data);
    })
  });
};

/** 导出按钮操作 */
const handleExport = () => {
  proxy?.download(
    'dms/template-edit/export',
    {
      ...queryParams.value
    },
    `文档编制_${new Date().getTime()}.xlsx`
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
