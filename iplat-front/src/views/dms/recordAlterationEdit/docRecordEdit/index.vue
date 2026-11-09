<template>
  <div class="p-2">
    <transition :enter-active-class="proxy?.animate.searchAnimate.enter" :leave-active-class="proxy?.animate.searchAnimate.leave">
      <div v-show="showSearch" class="mb-[10px]">
        <el-card shadow="hover">
          <super-search
            code="recordAlterationEdit-search"
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
            <el-button v-hasPermi="['dms:docRecordCreate:add']" type="primary" icon="Plus" @click="handleAdd">新增</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button v-hasPermi="['dms:docRecordCreate:remove']" type="danger" icon="Delete" :disabled="multiple" @click="handleDelete()"
              >删除</el-button
            >
          </el-col>
          <!-- <el-col :span="1.5">
            <el-button v-hasPermi="['dms:templateEdit:export']" type="warning" plain icon="Download" @click="handleExport">导出</el-button>
          </el-col> -->
          <right-toolbar v-model:show-search="showSearch" @query-table="getList"></right-toolbar>
        </el-row>
      </template>
      <!-- 离线文件上传 -->
      <input type="file" id="file" ref="selectFileRef" @change="selectFileChange" style="display:none" accept=".doc,.docx,.docm">
      <super-table 
        v-if="showTable"
        code="recordAlterationEdit-table"
        rowKey="id"
        highlightCurrentRow
        :dataLoading="loading"
        :data="templateEditList"
        v-model:queryParams="queryParams"
        :total="total"
        @selection-change="handleSelectionChange"
        @getList="getList"
      >
        <!-- 操作列 -->
        <template #operation="scope">
          <div>
            <el-tooltip content="编辑" placement="top" :enterable="false">
              <el-button link type="primary" v-hasPermi="['dms:docRecordCreate:edit']" icon="EditPen" @click="handleUpdate(scope.row)"></el-button>
            </el-tooltip>
            <el-tooltip content="删除" placement="top" :enterable="false">
              <el-button link type="danger" v-hasPermi="['dms:docRecordCreate:remove']" icon="Delete" @click="handleDelete(scope.row)"></el-button>
            </el-tooltip>
            <el-tooltip content="打开pageOffice" placement="top" :enterable="false">
              <el-button link type="primary" v-hasPermi="['dms:docRecordCreate:open']" v-if="!(scope.row.editway === 'offline' && scope.row.editStatus === 'notUploaded')" icon="Document" @click="openPageOffice(scope.row)"></el-button>
            </el-tooltip>
            <!-- 如果是批准状态 则提交按钮禁用 -->
            <el-tooltip content="提交" placement="top" :enterable="false">
              <el-button link type="primary" v-hasPermi="['dms:docRecordCreate:submit']" v-if="!(scope.row.editWay === 'offline' && scope.row.editStatus === 'notUploaded')" :disabled="scope.row.status == 'approved'" :loading="scope.row.subLoading" icon="SuccessFilled" @click="openApprove(scope.row)"></el-button>
            </el-tooltip>
            <el-tooltip content="离线文件上传" placement="top" :enterable="false">
              <el-button link type="primary" v-hasPermi="['dms:docCreate:onlineUpload']" v-if="canOfflineUpload(scope.row)" icon="Edit" @click="handleOnlineUpload(scope.row)"></el-button>
            </el-tooltip>
          </div>
        </template>
      </super-table>
      <!-- <pagination v-show="total > 0" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :total="total" @pagination="getList" /> -->
    </el-card>
    <!-- 添加或编辑文档记录编制对话框 -->
    <el-dialog v-model="dialog.visible" :title="dialog.title" width="70%" append-to-body>
      <super-form 
        v-if="dialog.visible"
        :loading="dialog.loading"
        ref="superFormRef"
        code="recordAlterationEdit-form"
        @selectData="selectData"
        :otherCondition="formOtherCondition"
        v-model="form"
        :form-rules="rules" 
        :methods="formMethods">
        <!-- 模板库插槽 -->
        <template #tmplLibNameSlot="config">
          <el-input v-model="form[config.prop]" suffix-icon="Document" @click.native="handSelect" v-bind="config" readonly></el-input>
        </template>
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
    <!-- 选择模板或记录 -->
    <SelectGroup
      v-if="selectOption.open"
      :title="selectOption.title"
      :selectType="selectOption.type"
      :multiple="selectOption.multiple"
      :width="selectOption.width"
      :otherCondition="selectOption.otherCondition"
      :achieveData="selectDataAchieve"
      @dialogClose="selectDataCancel"
  ></SelectGroup>
  </div>
</template>

<script setup name="DocRecordEdit" lang="ts">
import { listDocRecordEdit, getDocRecord, delDocRecord, addDocRecord, updateDocRecord, submitAndFlowStart, submitOfflineFile } from '@/api/dms/recordAlterationEdit/docRecordEdit/index';
import SelectGroup from '@/views/dms/recordAlterationEdit/docRecordEdit/selectGroup.vue';
import { fileOpenCheck } from '@/api/workflow/task';
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
  draftUserName: useUserStore().nickname,
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

const templateEditList = ref([]);

/** 查询文档记录编制列表 */
const getList = async () => {
  loading.value = true;
  try {
    const res = await listDocRecordEdit(queryParams.value);
    templateEditList.value = res.rows;
    total.value = res.total;
  } finally {
    loading.value = false;
  }
};

const formOtherCondition = ref({
  documentRecordCode: {
    status: 'active',
    activeStatus: 'active'
  }
})
const selectData = (data: any, config) => {
  if(config.prop == 'draftUserName'){
    form.value.drafterId = data.id;
    form.value.draftUserName = data.name;
  }else if(config.prop == 'documentRecordCode'){
    // 记录选中数据
    form.value.documentCode = data.dlCode;
    form.value.documentName = data.dlName;
    form.value.documentRecordLibraryId = data.id;
    form.value.documentRecordCode = data.dlrCode;
    form.value.documentRecordName = data.dlrName;
    form.value.documentRecordType = data.dlrType;
    form.value.tmplLibId = data.tmplLibId;
    form.value.tmplLibCode = data.tmplLibCode;
    form.value.tmplLibName = data.tmplLibName;
    form.value.reviseSource = 'revise';
  }else if(config.prop == 'tmplLibName'){
    // 模板来源
    // form.value.tmplSource = data.id;
    form.value.tmplLibId = data.id;
    form.value.tmplLibCode = data.tmplLibCode;
    form.value.tmplLibName = data.tmplLibName;
  }
};


const selectOption = ref({
  open: false,
  type: '',
  width: '',
  title: '',
  multiple: false,
  otherCondition: {
    status: 'active',
    activeStatus: 'active'
  },
})
// 选择模板库
const handSelect = (index) =>{
  selectOption.value.open = true;
  selectOption.value.title = '选择生效模板';
  selectOption.value.type = '';
  selectOption.value.otherCondition = {
    status: 'active',
    activeStatus: 'active'
  }
}
// 选中数据
const selectDataAchieve = (data, type) => {
  form.value.tmplSource = type;
  // 生效模板库
  if(type == 'template'){
    form.value.tmplLibId = data.id;
    form.value.tmplLibCode = data.tmplCode;
    form.value.tmplLibName = data.tmplName;
  }else{
    // 文件
    form.value.tmplLibId = data.id;
    form.value.tmplLibCode = data.dlCode;
    form.value.tmplLibName = data.dlName;
  }
  selectDataCancel();
};
// 关闭选择
const selectDataCancel = () => {
  selectOption.value = {
    open: false,
    title: undefined,
    width: '',
    multiple: false,
    type: undefined,
    otherCondition: {
      status: 'active',
      activeStatus: 'active'
    },
  };
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
  proxy?.POBrowser.openWindow('/docRecordEdit', 'width=1200px;height=800px;', paramString);
  // console.log(row)
};

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
    getList();
  }).finally(()=>{
    loading.value = false;
  })
}

const apprRow = ref();
// 提交审批按钮操作
const openApprove = async (row: any) => {
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

/** 新增按钮操作 */
const handleAdd = () => {
  reset();
  dialog.visible = true;
  dialog.loading = false;
  dialog.title = '添加文档记录编制';
  form.value.operate = 'add';
};

/** 编辑按钮操作 */
const handleUpdate = async (row?: any) => {
  reset();
  dialog.visible = true;
  dialog.loading = true;
  dialog.title = '编辑文档记录编制';
  const _id = row?.id || ids.value[0];
  const res = await getDocRecord(_id);
  Object.assign(form.value, res.data);
  form.value.operate = 'edit';
  dialog.loading = false;
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
  const _names = row?.documentName || names.value;
  await proxy?.$modal.confirm('是否确认删除文件名称为"' + _names + '"的数据项？').finally(() => (loading.value = false));
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
    `文档记录编制_${new Date().getTime()}.xlsx`
  );
};

onMounted(() => {
  getList();
});
</script>
