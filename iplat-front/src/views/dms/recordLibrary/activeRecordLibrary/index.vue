<template>
  <div class="p-2">
    <transition :enter-active-class="proxy?.animate.searchAnimate.enter" :leave-active-class="proxy?.animate.searchAnimate.leave">
      <div v-show="showSearch" class="mb-[10px]">
        <el-card shadow="hover">
          <super-search
            code="recordLibrary-active-search"
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
            <el-button v-hasPermi="['dms:documentRecordLib:add']" type="primary" icon="Plus" @click="handleAdd">新增</el-button>
          </el-col> -->
          <!-- <el-col :span="1.5">
            <el-button v-hasPermi="['dms:documentRecordLib:edit']" type="success" plain icon="Edit" :disabled="single" @click="handleUpdate()">编辑</el-button>
          </el-col> -->
          <el-col :span="1.5">
            <el-button v-hasPermi="['dms:documentRecordLib:remove']" type="primary" icon="Download" :disabled="multiple" @click="mulDownloadWord(selectRows, 'DOC_FILE')"
              >批量导出word</el-button
            >
          </el-col>
          <el-col :span="1.5">
            <el-button v-hasPermi="['dms:documentRecordLib:export']" type="primary" icon="SoldOut" :disabled="multiple" @click="mulDownloadWord(selectRows, 'PDF_FILE')">批量导出pdf</el-button>
          </el-col>
          <right-toolbar v-model:show-search="showSearch" @query-table="getList"></right-toolbar>
        </el-row>
      </template>
      <super-table 
        v-if="showTable"
        code="recordLibrary-active-table"
        rowKey="id"
        highlightCurrentRow
        :dataLoading="loading"
        :data="activeRecordLibList"
        v-model:queryParams="queryParams"
        :total="total"
        @selection-change="handleSelectionChange"
        @getList="getList"
        @expand-change="handleExpandChange"
      >
        <!-- 表格序号 -->
        <template #indexColumn>
          <el-table-column type="expand" fixed="left">
            <template #default="{ row }">
              <el-table v-loading="row.loading" :data="row.itemData" size="small" border>
                <el-table-column label="记录编号" align="center" prop="dlrCode" />
                <el-table-column label="记录名称" align="center" prop="dlrName" />
                <el-table-column label="生效时间" align="center" prop="dlrActiveTime">
                  <template #default="{row}">
                    {{ parseTime(row.dlrActiveTime,'{y}-{m}-{d}') }}
                  </template>
                </el-table-column>
                <el-table-column label="失效时间" align="center" prop="dlrDeactiveTime">
                  <template #default="{row}">
                    {{ parseTime(row.dlrDeactiveTime,'{y}-{m}-{d}') }}
                  </template>
                </el-table-column>
                <el-table-column label="失效理由" align="center" prop="dlrDeactiveReason" show-overflow-tooltip />
                <el-table-column label="记录版本" align="center" prop="dlrVersion" />
                <el-table-column label="操作" width="100px" align="center">
                  <template #default="scope">
                    <el-tooltip content="详情" placement="top" :enterable="false">
                      <el-button 
                        link 
                        type="primary" 
                        v-hasPermi="['dms:documentRecordLib:query']" 
                        icon="View" 
                        @click="handleHistoryDetail(scope.row)"
                      ></el-button>
                    </el-tooltip>
                  </template>
                </el-table-column>
              </el-table>
            </template>
          </el-table-column>
        </template>
        <!-- 操作列 -->
        <template #operation="scope">
          <div>
            <el-tooltip content="详情" placement="top" :enterable="false">
              <el-button link type="primary" v-hasPermi="['dms:documentRecordLib:query']" icon="View" @click="handleDetail(scope.row)"></el-button>
            </el-tooltip>
            <el-tooltip content="申请修订" placement="top" :enterable="false">
              <el-button link type="primary" v-hasPermi="['dms:documentRecordLib:edit']" :disabled="scope.row.activeStatus === 'deactiving' || scope.row.activeStatus === 'revising' || scope.row.activeStatus === 'rechecking'" icon="EditPen" @click="handleUpdate(scope.row)"></el-button>
            </el-tooltip>
            <el-tooltip content="申请失效" placement="top" :enterable="false">
              <el-button link type="primary" v-hasPermi="['dms:documentRecordLib:edit']" :disabled="scope.row.activeStatus === 'deactiving' || scope.row.activeStatus === 'revising' || scope.row.activeStatus === 'rechecking'" icon="CircleCloseFilled" @click="handleFailure(scope.row)"></el-button>
            </el-tooltip>
            <el-tooltip content="导出word" placement="top" :enterable="false">
              <el-button link type="primary" v-hasPermi="['dms:documentRecordLib:downloadWord']" icon="Download" @click="downloadWord(scope.row, 'DOC_FILE')"></el-button>
            </el-tooltip>
            <el-tooltip content="导出pdf" placement="top" :enterable="false">
              <el-button link type="primary" v-hasPermi="['dms:documentRecordLib:downloadPdf']" icon="SoldOut" @click="downloadWord(scope.row, 'PDF_FILE')"></el-button>
            </el-tooltip>
            <!-- <el-tooltip content="删除" placement="top" :enterable="false">
              <el-button link type="danger" v-hasPermi="['dms:documentRecordLib:remove']" icon="Delete" @click="handleDelete(scope.row)"></el-button>
            </el-tooltip> -->
          </div>
        </template>
      </super-table>
      <!-- <pagination v-show="total > 0" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :total="total" @pagination="getList" /> -->
    </el-card>
    <!-- 添加或编辑生效记录库对话框 -->
    <el-dialog v-model="dialog.visible" :title="dialog.title" width="90%" append-to-body>
      <super-form 
        v-if="dialog.visible"
        :loading="dialog.loading"
        ref="superFormRef"
        code="recordLibrary-active-form"
        disabled
        @selectData="selectData"
        v-model="form"
        :form-rules="rules" 
        :methods="formMethods">
        <PdfView v-if="form.id" :text="waterText" :id="form.id"></PdfView>
      </super-form>
      <template #footer>
        <div class="dialog-footer">
          <!-- <el-button :loading="buttonLoading" type="primary" @click="submitForm">确 定</el-button> -->
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>
    <!-- 修订生效记录库对话框 -->
    <el-dialog v-model="editDialog.visible" :title="editDialog.title" width="70%" append-to-body>
      <super-form 
        v-if="editDialog.visible"
        :loading="editDialog.loading"
        ref="superEditRef"
        code="recordLibrary-active-edit-form"
        @selectData="selectData"
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
          <el-button :loading="buttonLoading" type="primary" @click="submitEdit">确 定</el-button>
          <el-button @click="editDialog.visible = false">取 消</el-button>
        </div>
      </template>
    </el-dialog>
    <!-- 申请失效对话框 -->
    <el-dialog v-model="failureDialog.visible" :title="failureDialog.title" width="70%" append-to-body>
      <super-form 
        v-if="failureDialog.visible"
        :loading="failureDialog.loading"
        ref="superFailureRef"
        code="recordLibrary-active-failure-form"
        @selectData="selectData"
        v-model="form"
        :form-rules="rules" 
        :methods="formMethods">
      </super-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button :loading="buttonLoading" type="primary" @click="submitFailure">确 定</el-button>
          <el-button @click="failureDialog.visible = false">取 消</el-button>
        </div>
      </template>
    </el-dialog>
    <!-- 查看历史记录详情对话框 -->
    <el-dialog v-model="historyDialog.visible" :title="historyDialog.title" width="90%" append-to-body>
      <super-form 
        v-if="historyDialog.visible"
        :loading="historyDialog.loading"
        ref="superFormRef"
        code="recordLibrary-history-form"
        disabled
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

<script setup name="ActiveRecordLibrary" lang="ts">
import { activeListActiveRecord, historyListActiveRecord, getActiveRecord, delActiveRecord, invalidActiveRecord, reviseActiveRecord, submitAndFlowStart } from '@/api/dms/recordLibrary/activeRecordLibrary/index';
import SelectGroup from '@/views/dms/operateOrder/addOperateOrder/selectGroup.vue';
import { listByBizIdAndType, downloadFile } from '@/api/system/oss';
import { fileOpenCheck } from '@/api/workflow/task';
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

const activeRecordLibList = ref([]);

/** 查询生效记录库列表 */
const getList = async () => {
  loading.value = true;
  try {
    const res = await activeListActiveRecord(queryParams.value);
    activeRecordLibList.value = res.rows;
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
    historyListActiveRecord({ dlrCode: row.dlrCode }).then(res=>{
      row.itemData = res.rows;
    }).finally(()=>{
      row.loading = false;
    })
  }
}

const selectData = (data: any, config) => {
  if(config.prop == 'draftUserName'){
    form.value.drafterId = data.id;
    form.value.draftUserName = data.name;
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


const selectOption = ref({
  open: false,
  type: '',
  width: '',
  title: '',
  multiple: false,
  otherCondition: {
    status: 'active',
    activeStatus: 'active',
    tmplPurpose: 'record',
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
  // form.value.tmplSource = type;
  form.value.tmplLibId = data.id;
  form.value.tmplLibCode = data.tmplCode;
  form.value.tmplLibName = data.tmplName;
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

const selectRows = ref([]);
/** 多选框选中数据 */
const handleSelectionChange = (selection: any[]) => {
  ids.value = selection.map((item) => item.id);
  names.value = selection.map((item) => item.tmplName);
  single.value = selection.length != 1;
  multiple.value = !selection.length;
  selectRows.value = selection;
};

/** 新增按钮操作 */
const handleAdd = () => {
  reset();
  dialog.visible = true;
  dialog.loading = false;
  dialog.title = '添加生效记录库';
};

const waterText = ref('')
/** 详情按钮操作 */
const handleDetail = async (row?: any) => {
  reset();
  dialog.visible = true;
  dialog.loading = true;
  dialog.title = '生效记录库详情';
  const _id = row?.id || ids.value[0];
  const res = await getActiveRecord(_id);
  Object.assign(form.value, res.data);
  const { name, nickname } = useUserStore();
  const time = proxy?.parseTime(new Date(), '{y}-{m}-{d}')
  waterText.value = `${name} ${nickname} ${time} 生效`
  dialog.loading = false;
};

const superEditRef = ref(null);
const editDialog = reactive<DialogOption>({
  title: '',
  visible: false,
  loading: false
});
/** 申请修订按钮操作 */
const handleUpdate = async (row?: any) => {
  reset();
  editDialog.visible = true;
  editDialog.loading = true;
  editDialog.title = '生效记录库修订';
  const _id = row?.id || ids.value[0];
  const res = await getActiveRecord(_id);
  Object.assign(form.value, res.data);
  // 操作类型是失效时，禁用除原因之外的数据(已配置交互规则)
  // if (form.value.tmplDealType === 'invalid') {
  //   disabled = true;
  // }
  editDialog.loading = false;
};
/** 提交按钮 */
const submitEdit = () => {
  superEditRef.value?.validate(async (valid: boolean) => {
    if (valid) {
      const data = superEditRef.value.achieveData()
      buttonLoading.value = true;
      // 申请修订
      await reviseActiveRecord(data).finally(() => (buttonLoading.value = false));
      proxy?.$modal.msgSuccess('操作成功');
      editDialog.visible = false;
      await getList();
    }
  });
};

const superFailureRef = ref(null);
const failureDialog = reactive<DialogOption>({
  title: '',
  visible: false,
  loading: false
});
/** 申请失效按钮操作 */
const handleFailure = async (row?: any) => {
  reset();
  failureDialog.visible = true;
  failureDialog.loading = true;
  failureDialog.title = '生效记录库申请失效';
  const _id = row?.id || ids.value[0];
  const res = await getActiveRecord(_id);
  Object.assign(form.value, res.data);
  failureDialog.loading = false;
};

/** 失效提交按钮 */
const submitFailure = () => {
  superFailureRef.value?.validate(async (valid: boolean) => {
    if (valid) {
      const data = superFailureRef.value.achieveData()
      buttonLoading.value = true;
      // 申请修订
      await invalidActiveRecord(data).finally(() => (buttonLoading.value = false));
      proxy?.$modal.msgSuccess('操作成功');
      failureDialog.visible = false;
      await getList();
    }
  });
};

/** 删除按钮操作 */
const handleDelete = async (row?: any) => {
  const _ids = row?.id || ids.value;
  const _names = row?.tmplName || names.value;
  await proxy?.$modal.confirm('是否确认删除模板名称为"' + _names + '"的数据项？').finally(() => (loading.value = false));
  await delActiveRecord(_ids);
  proxy?.$modal.msgSuccess('删除成功');
  await getList();
};


const historyDialog = reactive<DialogOption>({
  title: '',
  visible: false,
  loading: false
});
/** 查看历史记录库详情按钮操作 */
const handleHistoryDetail = async (row?: any) => {
  reset();
  form.value.id = row?.id;
  historyDialog.visible = true;
  historyDialog.loading = true;
  historyDialog.title = '模板详情';
  const _id = row?.id || ids.value[0];
  const res = await getActiveRecord(_id);
  Object.assign(form.value, res.data);
  historyDialog.loading = false;
  const { name, nickname } = useUserStore();
  const time = proxy?.parseTime(new Date(), '{y}-{m}-{d}')
  waterText.value = `${name} ${nickname} ${time} 失效`
};

// 导出文件
const downloadWord = (row: any, bizType: any) =>{
  loading.value = true;
  listByBizIdAndType(row.id, bizType).then((res: any)=>{
    if(res.data && res.data.length){
      res.data.forEach((item: any) => {
        proxy?.$download.oss(item.ossId);
      })
    }else{
      proxy?.$modal.msgError('未查询到文件');
    }
  }).finally(()=>{
    loading.value = false;
  })
};
// 批量导出文件
const mulDownloadWord = (row: any, bizType: any) =>{
  const data = {
    bizIds: row.map(item=>item.id).join(','),
    bizType: bizType
  }
  loading.value = true;
  downloadFile(data).then((res: any)=>{
    const blob = new Blob([res]);
    const fileName = `生效记录库_${new Date().getTime()}`// 创建下载链接
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.setAttribute('download', fileName+'.zip');
    document.body.appendChild(link);
    link.click();
    link.remove();
    window.URL.revokeObjectURL(url);
  }).finally(()=>{
    loading.value = false;
  })
};

/** 导出按钮操作 */
const handleExport = () => {
  proxy?.download(
    'dms/template-edit/export',
    {
      ...queryParams.value
    },
    `生效记录库_${new Date().getTime()}.xlsx`
  );
};

onMounted(() => {
  getList();
});
</script>
