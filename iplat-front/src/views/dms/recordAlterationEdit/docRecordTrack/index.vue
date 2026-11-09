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
          <!-- <el-col :span="1.5">
            <el-button v-hasPermi="['dms:docRecordCreate:add']" type="primary" icon="Plus" @click="handleAdd">新增</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button v-hasPermi="['dms:docRecordCreate:remove']" type="danger" icon="Delete" :disabled="multiple" @click="handleDelete()"
              >删除</el-button
            >
          </el-col> -->
          <!-- <el-col :span="1.5">
            <el-button v-hasPermi="['dms:templateEdit:export']" type="warning" plain icon="Download" @click="handleExport">导出</el-button>
          </el-col> -->
          <right-toolbar v-model:show-search="showSearch" @query-table="getList"></right-toolbar>
        </el-row>
      </template>
      <super-table 
        v-if="showTable"
        code="recordAlterationEdit-track-table"
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
            <el-tooltip content="详情" placement="top" :enterable="false">
              <el-button link type="primary" v-hasPermi="['dms:docRecordCreate:trackDetail']" icon="View" @click="handleDetail(scope.row)"></el-button>
            </el-tooltip>
            <el-tooltip content="查看审批履历" placement="top" :enterable="false">
              <el-button link type="primary" v-hasPermi="['dms:docRecordCreate:Record']" :disabled="scope.row.status === 'edit'" icon="InfoFilled" @click="handleApprovalRecord(scope.row)"></el-button>
            </el-tooltip>
            <el-popconfirm
              title="确定撤回当前行数据吗？"
              @confirm="submitApprove(scope.row.id)"
            >
              <template #reference>
                <span style="margin-left: 12px;">
                  <el-tooltip content="撤回" placement="top" :enterable="false">
                    <el-button link type="primary" :disabled="scope.row.status !== 'approving'" v-hasPermi="['dms:docRecordCreate:revoke']" icon="Back"></el-button>
                  </el-tooltip>
                </span>
              </template>
            </el-popconfirm>
            <!-- <el-tooltip content="打开pageOffice" placement="top" :enterable="false">
              <el-button link type="primary" v-hasPermi="['dms:docRecordCreate:open']" icon="Document" @click="openPageOffice(scope.row)"></el-button>
            </el-tooltip> -->
            <!-- <el-tooltip content="差异对比" placement="top" :enterable="false">
              <el-button link type="primary" v-hasPermi="['dms:docRecordCreate:compare']" icon="Share" @click="handleCompare(scope.row)"></el-button>
            </el-tooltip> -->
            <!-- 如果是批准状态 则提交按钮禁用 -->
            <!-- <el-tooltip content="提交" placement="top" :enterable="false">
              <el-button link type="primary" v-hasPermi="['dms:docRecordCreate:submit']" :disabled="scope.row.status == 'approved'" :loading="scope.row.subLoading"  icon="SuccessFilled" @click="submitApprove(scope.row)"></el-button>
            </el-tooltip> -->
          </div>
        </template>
      </super-table>
      <!-- <pagination v-show="total > 0" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :total="total" @pagination="getList" /> -->
    </el-card>
    <!-- 添加或编辑记录编制审核对话框 -->
    <el-dialog v-model="dialog.visible" :title="dialog.title" width="70%" append-to-body>
      <super-form 
        v-if="dialog.visible"
        :loading="dialog.loading"
        disabled
        ref="superFormRef"
        code="recordAlterationEdit-form"
        @selectData="selectData"
        v-model="form"
        :form-rules="rules" 
        :methods="formMethods">
        <!-- 模板库插槽 -->
        <template #tmplLibNameSlot="config">
          <el-input v-model="form[config.prop]" suffix-icon="Document" v-bind="config" readonly></el-input>
        </template>
      </super-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button :loading="buttonLoading" type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
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
    <!-- 提交组件 -->
    <submitVerify ref="submitVerifyRef" :task-variables="taskVariables" @submit-callback="submitCallback" />
    <!-- 审批记录 -->
    <approvalRecord ref="approvalRecordRef" />
  </div>
</template>

<script setup name="DocRecordTrack" lang="ts">
import { listDocRecord, getDocRecord, delDocRecord, addDocRecord, updateDocRecord, submitAndFlowStart } from '@/api/dms/recordAlterationEdit/docRecordEdit/index';
import { cancelProcessApply } from '@/api/workflow/instance';
import { useUserStore } from '@/store/modules/user';
import { listSnapshot } from '@/api/dms/templateManagement/templateSnapshot/index';
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

/** 查询记录编制审核列表 */
const getList = async () => {
  loading.value = true;
  try {
    const res = await listDocRecord(queryParams.value);
    templateEditList.value = res.rows;
    total.value = res.total;
  } finally {
    loading.value = false;
  }
};

//审批记录组件
const approvalRecordRef = ref();
//审批记录
const handleApprovalRecord = (row: any) => {
  approvalRecordRef.value.init(row.id);
};

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

//提交组件
const taskVariables = ref<Record<string, any>>({});
// 撤回按钮操作
const submitApprove = async (id) => {
  const data = {
    businessId: id,
    message: '申请人撤销流程！'
  };
  try {
    loading.value = true;
    await cancelProcessApply(data).finally(() => (loading.value = false));
    getList();
    proxy?.$modal.msgSuccess('撤销成功');
  } finally{    
    loading.value = false;
  }
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
  names.value = selection.map((item) => item.documentName);
  single.value = selection.length != 1;
  multiple.value = !selection.length;
};

/** 新增按钮操作 */
const handleAdd = () => {
  reset();
  dialog.visible = true;
  dialog.loading = false;
  dialog.title = '添加记录编制审核';
};

/** 编辑按钮操作 */
const handleUpdate = async (row?: any) => {
  reset();
  dialog.visible = true;
  dialog.loading = true;
  dialog.title = '编辑记录编制审核';
  const _id = row?.id || ids.value[0];
  const res = await getDocRecord(_id);
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
  dialog.title = '查看记录编制审核';
  const _id = row?.id || ids.value[0];
  const res = await getDocRecord(_id);
  Object.assign(form.value, res.data);
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
    `记录编制审核_${new Date().getTime()}.xlsx`
  );
};

onMounted(() => {
  getList();
});
</script>
