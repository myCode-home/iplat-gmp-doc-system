<template>
  <div class="">
    <!-- <el-row :gutter="10" class="p-2">
      <el-col :span="1.5">
        <el-button v-hasPermi="['dms:templateApprove:add']" type="primary" plain icon="Plus" @click="handleAdd">新增</el-button>
      </el-col>
    </el-row> -->
    <super-table 
      code="docCreate-record-table"
      rowKey="id"
      :dataLoading="loading"
      :data="recordList"
    >
      <!-- 操作列 -->
      <template #operation="scope">
        <div>
          <!-- 记录来源为上传、自动生成可查看详情 -->
          <el-tooltip content="详情" placement="top" :enterable="false">
            <el-button link type="primary" v-hasPermi="['dms:docCreate:query']" icon="View" @click="handleDetail(scope.row)"></el-button>
          </el-tooltip>
          <!-- <el-tooltip content="差异对比" placement="top" :enterable="false">
            <el-button link type="primary" v-hasPermi="['dms:tmplSnapshot:list']" icon="Share" @click="handleCompare(scope.row)"></el-button>
          </el-tooltip> -->
          <el-tooltip content="打开pageOffice" placement="top" :enterable="false">
            <el-button link type="primary" v-hasPermi="['dms:pageoffice:open']" icon="Document" @click="openPageOffice(scope.row)"></el-button>
          </el-tooltip>
        </div>
      </template>
    </super-table>
    <!-- 添加或编辑记录对话框 -->
    <el-dialog v-model="dialog.visible" :title="dialog.title" width="90%" append-to-body>
      <super-form 
        v-if="dialog.visible"
        :loading="dialog.loading"
        :disabled="operate == 'detail'"
        ref="superFormRef"
        code="docCreate-record-view-form"
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
    <!-- 差异对比弹窗 -->
    <el-dialog v-model="compareDialog.visible" :title="compareDialog.title" width="75%" append-to-body>
      <super-table
        v-if="compareDialog.visible"
        code="recordAlterationEdit-comparison-table"
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
import { listDocRecord, getDocRecord, delDocRecord, insertFromDocCreate, judgeRecordNameUnique, updateDocRecord, listSnapshot, submitOfflineFile } from '@/api/dms/recordAlterationEdit/docRecordEdit/index';
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

const recordList = ref([]);

// watch(()=>props.docCreateId, ()=>{
//   queryParams.value.docCreateId = props.docCreateId;
//   getList();
// })

/** 查询记录列表 */
const getList = async () => {
  loading.value = true;
  try {
    queryParams.value.docCreateId = props.docCreateId;
    const res = await listBuildNewList(queryParams.value);
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
  documentRecordCode: '',
  documentRecordVersion: '',
  documentRecordVersionMinor: '',
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
  proxy?.POBrowser.openWindow('/docRecordCompare', 'width=1200px;height=800px;', paramString);
}

// 差异对比按钮操作
const handleCompare = (row: any)=>{
  compareDialog.visible = true;
  compareDialog.title = '差异对比';
  compareQueryParams.value.pageNum = 1;
  compareQueryParams.value.documentRecordCode = row.documentRecordCode;
  compareQueryParams.value.documentRecordVersion = row.documentRecordVersion;
  compareQueryParams.value.documentRecordVersionMinor = row.documentRecordVersionMinor;
  compareQueryParams.value.bizId = row.id;
  selectRows.value = {id: ''};
  compareList.value = [];
  getCompareList();
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
  dialog.visible = true;
  dialog.loading = false;
  dialog.title = '添加记录';
};

/** 编辑按钮操作 */
const handleUpdate = async (row?: any) => {
  reset();
  operate.value = 'edit';
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

onMounted(() => {
  getList();
});

defineExpose({
  handleAdd,
  getList
});
</script>
