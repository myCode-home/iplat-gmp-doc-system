<template>
  <div class="">
    <!-- <el-row :gutter="10" class="p-2">
      <el-col :span="1.5">
        <el-button v-hasPermi="['dms:templateApprove:add']" type="primary" plain icon="Plus" @click="handleAdd">新增</el-button>
      </el-col>
    </el-row> -->
    <!-- 离线文件上传 -->
    <input type="file" id="file" ref="selectFileRef" @change="selectFileChange" style="display:none" accept=".doc,.docx,.docm">
    <super-table 
      code="docCreate-record-table"
      rowKey="id"
      :dataLoading="loading"
      :data="temApproveList"
    >
      <!-- 操作列 -->
      <template #operation="scope">
        <div>
          <el-tooltip content="编辑" placement="top" :enterable="false">
            <el-button link type="primary" v-hasPermi="['dms:docCreate:edit']" icon="Edit" @click="handleUpdate(scope.row)"></el-button>
          </el-tooltip>
          <!-- 记录来源为上传、自动生成可查看详情 -->
          <el-tooltip content="详情" placement="top" :enterable="false">
            <el-button link type="primary" v-if="scope.row.reviseSource === 'fromUpload' || scope.row.reviseSource === 'autoCreate'" v-hasPermi="['dms:docCreate:query']" icon="View" @click="handleDetail(scope.row)"></el-button>
          </el-tooltip>
          <!-- <el-tooltip content="差异对比" placement="top" :enterable="false">
            <el-button link type="primary" v-hasPermi="['dms:tmplSnapshot:list']" icon="Share" @click="handleCompare(scope.row)"></el-button>
          </el-tooltip> -->
          <el-tooltip content="打开pageOffice" placement="top" :enterable="false">
            <el-button link type="primary" v-if="showPageoffice(scope.row)" v-hasPermi="['dms:pageoffice:open']" icon="Document" @click="openPageOffice(scope.row)"></el-button>
          </el-tooltip>
          <!-- 记录操作类型 为新建可删除 -->
          <el-popconfirm
            title="确定删除当前行数据吗？"
            @confirm="handleDelete(scope.row)"
          >
            <template #reference>
              <span style="margin-left: 12px;">
                <el-tooltip content="删除" placement="top" :enterable="false">
                  <el-button link type="danger" v-if="scope.row.recordHandleType === 'new'" v-hasPermi="['dms:docCreate:remove']" icon="Delete"></el-button>
                </el-tooltip>
              </span>
            </template>
          </el-popconfirm>
          <el-tooltip content="离线文件上传" placement="top" :enterable="false">
            <el-button link type="primary" v-hasPermi="['dms:docCreate:onlineUpload']" v-if="canOfflineUpload(scope.row)" icon="Edit" @click="handleOnlineUpload(scope.row)"></el-button>
          </el-tooltip>
          <el-popconfirm
            title="确定失效当前行数据吗？"
            @confirm="handleFailure(scope.row)"
          >
            <template #reference>
              <span style="margin-left: 12px;">
                <el-tooltip content="申请失效" placement="top" :enterable="false">
                  <el-button link type="primary" v-if="scope.row.recordHandleType === 'revise'" v-hasPermi="['dms:docCreate:edit']" icon="CircleCloseFilled"></el-button>
                </el-tooltip>
              </span>
            </template>
          </el-popconfirm>
          <el-popconfirm
            title="确定取消失效当前行数据吗？"
            @confirm="handleCancelFailure(scope.row)"
          >
            <template #reference>
              <span style="margin-left: 12px;">
                <el-tooltip content="取消失效" placement="top" :enterable="false">
                  <el-button link type="primary" v-if="scope.row.recordHandleType === 'revise'" v-hasPermi="['dms:docCreate:edit']" icon="CircleCloseFilled"></el-button>
                </el-tooltip>
              </span>
            </template>
          </el-popconfirm>
        </div>
      </template>
    </super-table>
    <!-- 添加或编辑记录对话框 -->
    <el-dialog v-model="dialog.visible" :title="dialog.title" width="600px" append-to-body>
      <super-form 
        v-if="dialog.visible"
        :loading="dialog.loading"
        :disabled="operate == 'detail'"
        ref="superFormRef"
        code="docCreate-record-form"
        @selectData="selectData"
        v-model="form"
        :form-rules="rules" 
        :methods="formMethods">
        <!-- 模板库插槽 -->
        <template #tmplLibNameSlot="config">
          <el-input v-model="form[config.prop]" @click.native="handSelect" v-bind="config" readonly></el-input>
        </template>
      </super-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button :loading="buttonLoading" type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">关 闭</el-button>
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

<script setup name="TemplateApprove" lang="ts">
import { listDocRecord, getDocRecord, delDocRecord, insertFromDocCreate, judgeRecordNameUnique, updateDocRecord, submitAndFlowStart, submitOfflineFile, recordFailure, cancelRecordFailure } from '@/api/dms/recordAlterationEdit/docRecordEdit/index';
import { listBuildNewList } from '@/api/dms/recordAlterationEdit/docRecordEdit/index';
import SelectGroup from '@/views/dms/recordAlterationEdit/docRecordEdit/selectGroup.vue';
import { useUserStore } from '@/store/modules/user';
const props = defineProps({
  docCreateId: {
    type: String,
    default: ''
  }
})

const emit = defineEmits(['saved']);

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
    pageSize: 9999,
  }),
  rules: {
    // id: [{ required: true, message: '主键不能为空', trigger: 'blur' }],
  }
});

const { queryParams, form, rules } = toRefs(data);

const temApproveList = ref([]);

// watch(()=>props.docCreateId, ()=>{
//   queryParams.value.docCreateId = props.docCreateId;
//   getList();
// })

// 是否显示pageoffice
const showPageoffice = computed(()=>{
  return (row) => {
    const booleanOpe = row.editWay === 'offline' && (row.status === 'edit' || row.status === 'retreat') && row.editStatus === 'notUploaded';
    return !booleanOpe;
  }
});
// 是否显示 离线文件上传
const canOfflineUpload = computed(()=>{
  return function (row: any){
    if(row.editWay === 'online' || row.status === 'commit'){
      return false;
      // 编辑方式为offline,文件状态不为编辑和退回的,隐藏
    } else if(row.editWay === 'offline' && (row.status !== 'edit' && row.status !== 'retreat')){
      return false;
      // 创建方式为文件上传,隐藏
    } else if (row.recordHandleType === 'fromUpload') {
      return false;
    }  else {
      return true;
    }
  }
});

const offlineRow = ref({});
const selectFileRef = ref(null);
// 离线文件上传按钮操作
const handleOnlineUpload = (row: any)=>{
  offlineRow.value = row;
  selectFileRef.value.click();
};
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
};

// 失效按钮操作
const handleFailure = (row)=>{
  recordFailure(row.id).then(res=>{
    proxy.$modal.msgSuccess('操作成功');
    getList();
  });
};
// 取消失效按钮操作
const handleCancelFailure = (row)=>{
  cancelRecordFailure(row.id).then(res=>{
    proxy.$modal.msgSuccess('操作成功');
    getList();
  });
};

/** 查询记录列表 */
const getList = async () => {
  loading.value = true;
  try {
    queryParams.value.docCreateId = props.docCreateId;
    const res = await listBuildNewList(queryParams.value);
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

const selectOption = ref({
  open: false,
  type: '',
  width: '',
  title: '',
  multiple: false,
  otherCondition: {
    status: 'active',
    activeStatus: 'active',
    tmplPurpose: 'record'
  },
})
// 选择模板库
const handSelect = (index) =>{
  selectOption.value.open = true;
  selectOption.value.title = '选择生效模板';
  selectOption.value.type = '';
  selectOption.value.otherCondition = {
    status: 'active',
    activeStatus: 'active',
    tmplPurpose: 'record'
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
    form.value.tmplLibCode = data.dlrCode;
    form.value.tmplLibName = data.dlrName;
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
      activeStatus: 'active',
      tmplPurpose: 'record'
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
    id: row.id,
    file_id: 1,
    file_name: "test.doc",
  };
  const paramString = JSON.stringify(paramJson);
  //openWindow()第三个参数用来向弹出的PageOffice浏览器（POBrowser）窗口传递参数(参数长度不限)，支持json格式字符串。
  //此处为了方便演示，我们传递了file_id和file_name两个参数，具体以您实际开发为准。
  proxy?.POBrowser.openWindow('/docRecordEdit', 'width=1200px;height=800px;', paramString);
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

/** 多选框选中数据 */
const handleSelectionChange = (selection: any[]) => {
  ids.value = selection.map((item) => item.id);
  names.value = selection.map((item) => item.tmplName);
  single.value = selection.length != 1;
  multiple.value = !selection.length;
};

const operate = ref('add');
/** 新增按钮操作 */
const handleAdd = (row?: any) => {
  reset();
  // form.value.orderUser = row.orderUser;
  // form.value.orderUserName = row.orderUserName;
  // form.value.documentRecordCode = row.documentRecordCode;
  // form.value.documentRecordName = row.documentRecordName;
  // form.value.documentRecordType = row.documentRecordType;
  form.value.documentCode = row.documentCode;
  form.value.documentName = row.documentName;
  form.value.editWay = row.editWay;
  // form.value.tmplLibId = row.tmplLibId;
  // form.value.tmplLibCode = row.tmplLibCode;
  // form.value.tmplLibName = row.tmplLibName;
  form.value.recordHandleType = row.recordHandleType;
  form.value.tmplSource = row.tmplSource;
  form.value.docCreateId = row.id;
  operate.value = 'add';
  form.value.operate = 'add';
  dialog.visible = true;
  dialog.loading = false;
  dialog.title = '添加记录';
};

/** 编辑按钮操作 */
const handleUpdate = async (row?: any) => {
  reset();
  operate.value = 'edit';
  form.value.operate = 'edit';
  dialog.visible = true;
  dialog.loading = true;
  dialog.title = '编辑记录';
  const _id = row?.id || ids.value[0];
  const res = await getDocRecord(_id);
  Object.assign(form.value, res.data);
  dialog.loading = false;
};

/** 查看按钮操作 */
const handleDetail = async (row?: any) => {
  reset();
  operate.value = 'detail';
  form.value.operate = 'detail';
  dialog.visible = true;
  dialog.loading = true;
  dialog.title = '查看记录详情';
  const _id = row?.id || ids.value[0];
  const res = await getDocRecord(_id);
  Object.assign(form.value, res.data);
  dialog.loading = false;
};

/** 提交按钮 */
const submitForm = async() => {
  superFormRef.value?.validate(async (valid: boolean) => {
    if (valid) {
      // 名称查重
      const data = superFormRef.value.achieveData()
      judgeRecordNameUnique({ 
        documentRecordCode: data.documentRecordCode,
        documentRecordName: data.documentRecordName
      }).then(async res=>{
        if(res.data == false){
          await proxy?.$modal.confirm('该记录名称已存在，是否继续提交').finally(() => (loading.value = false));
          submitData();
        }else{
          submitData();
        }
      })
    }
  });
};
// 提交数据
const submitData = async() =>{
  const data = superFormRef.value.achieveData()
  buttonLoading.value = true;
  if (data.id) {
    await updateDocRecord(data).finally(() => (buttonLoading.value = false));
  } else {
    await insertFromDocCreate(data).finally(() => (buttonLoading.value = false));
  }
  proxy?.$modal.msgSuccess('操作成功');
  dialog.visible = false;
  await getList();
  emit('saved', data.docCreateId);
}

/** 删除按钮操作 */
const handleDelete = async (row?: any) => {
  const _ids = row?.id || ids.value;
  // const _names = row?.tmplName || names.value;
  // await proxy?.$modal.confirm('是否确认删除记录名称为"' + _names + '"的数据项？').finally(() => (loading.value = false));
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
    `记录_${new Date().getTime()}.xlsx`
  );
};

onMounted(() => {
  getList();
});

defineExpose({
  handleAdd,
  getList
});
</script>
