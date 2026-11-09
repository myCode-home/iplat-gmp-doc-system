<template>
  <div class="">
    <transition :enter-active-class="proxy?.animate.searchAnimate.enter" :leave-active-class="proxy?.animate.searchAnimate.leave">
      <div v-show="showSearch" class="mb-[10px]">
        <el-card shadow="hover">
          <super-search
            code="lendApply-search"
            v-model="queryParams"
            @getList="getList"
            @resetQuery="resetQuery"
          >
          <template #lendingPeriodSlot="config">
            <el-input v-model="queryParams[config.prop]" v-bind="config" @change="getList"  @keyup.enter="getList">
              <template #append>天</template>
            </el-input>
          </template>
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
        code="lendApply-table"
        rowKey="id"
        :dataLoading="loading"
        :data="trainList"
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
              <RecordTable :ref="(el) => recordTableRefs[row.id] = el" :docId="row.docLibId"></RecordTable>
            </template>
          </el-table-column>
        </template>
        <!-- 操作列 -->
        <template #operation="scope">
          <div>
            <el-tooltip content="详情" placement="top" :enterable="false">
              <el-button link type="primary" v-hasPermi="['dms:tmplPreparation:query']" icon="View" @click="handleDetail(scope.row)"></el-button>
            </el-tooltip>
            <!-- 导出word -->
            <el-tooltip content="下载word" placement="top" :enterable="false">
              <el-button link type="primary" :disabled="scope.row.isDownload == '0'" icon="Download" @click="handleExportWord(scope.row)"></el-button>
            </el-tooltip>
            <!-- 导出pdf -->
            <el-tooltip content="下载pdf" placement="top" :enterable="false">
              <el-button link type="primary" :disabled="scope.row.isDownload == '0'" icon="SoldOut" @click="handleExportPdf(scope.row)"></el-button>
            </el-tooltip>
            <!-- <el-tooltip content="查看审批履历" placement="top" :enterable="false">
              <el-button link type="primary" v-hasPermi="['dms:tmplPreparation:approvalRecord']" :disabled="scope.row.status === 'edit'" icon="InfoFilled" @click="handleApprovalRecord(scope.row)"></el-button>
            </el-tooltip> -->
            <!-- <el-popconfirm
              title="确定撤回当前行数据吗？"
              @confirm="submitApprove(scope.row.id)"
            >
              <template #reference>
                <span style="margin-left: 12px;">
                  <el-tooltip content="撤回" placement="top" :enterable="false">
                    <el-button link type="primary" :disabled="scope.row.status !== 'approving'" v-hasPermi="['dms:tmplPreparation:revoke']" icon="Back"></el-button>
                  </el-tooltip>
                </span>
              </template>
            </el-popconfirm> -->
          </div>
        </template>
      </super-table>
    </el-card>
    <!-- 添加或编辑借阅申请审核对话框 -->
    <el-dialog v-model="dialog.visible" :title="dialog.title" width="90%" append-to-body>
      <super-form 
        v-if="dialog.visible"
        :loading="dialog.loading"
        ref="superFormRef"
        code="lendApprove-form"
        disabled
        @selectData="selectData"
        v-model="form"
        :form-rules="rules" 
        :methods="formMethods">
        <!-- 借阅天数插槽 -->
        <template #lendingPeriodSlot="config">
          <el-input v-model="form[config.prop]" v-bind="config">
            <template #append>天</template>
          </el-input>
        </template>
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
    <!-- pdf预览附件弹窗 -->
    <el-dialog v-model="pdfDialog.visible" :title="pdfDialog.title" width="90%" append-to-body>
      <pdfView v-if="pdfDialog.visible" text="" :id="pdfDialog.id"></pdfView>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="pdfDialog.visible = false">关 闭</el-button>
        </div>
      </template>
    </el-dialog>
    <!-- 审批记录 -->
    <approvalRecord ref="approvalRecordRef" />
  </div>
</template>

<script setup name="Lendlibrary" lang="ts">
import { listLendOrderLib, getLendLib, delLendOrder, addLendOrder, updateLendOrder, submitAndFlowStart } from '@/api/dms/fileLendOrder/lendApply/index';
import { cancelProcessApply } from '@/api/workflow/instance';
import RecordTable from '@/views/dms/fileLendOrder/lendApply/recordTable.vue'
import { useUserStore } from '@/store/modules/user';
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
const total = ref(0);
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

const trainList = ref([]);

/** 查询借阅申请审核审核列表 */
const getList = async () => {
  loading.value = true;
  try {
    const res = await listLendOrderLib(queryParams.value);
    trainList.value = res.rows;
    total.value = res.total;
  } finally {
    loading.value = false;
  }
};

const recordTableRefs = ref({});
// 展开或者关闭行
const handleExpandChange = (...args: any[])=>{
  const [row, expandedRows] = args;
  const recordTableRef = recordTableRefs.value[row.id];
  if(expandedRows.findIndex(item => item.id === row.id) !== -1){
    if (recordTableRef) {
      
    }
  }
}


const selectData = (data: any, config) => {
  if(config.prop == 'documentName'){
    form.value.docLibId = data.id;
    form.value.documentCode = data.dlCode;
    form.value.documentName = data.dlName;
  }
};

// 在父组件中预定义所有可能用到的方法
const formMethods = {
  // args参数集合 childFrom参数集合
  customChangeHandler: (args: Array<any>,childFrom: Object) => {
    superFormRef.value.updateData({ companyName: 'admin' })
    console.log('自定义change事件触发')
  }
}

const pdfDialog = reactive({
  title: '查看附件',
  visible: false,
  loading: false,
  id: '',
});
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

//审批记录组件
const approvalRecordRef = ref();
//审批记录
const handleApprovalRecord = (row: any) => {
  approvalRecordRef.value.init(row.id);
};
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
  dialog.title = '添加借阅申请审核';
};

/** 编辑按钮操作 */
const handleUpdate = async (row?: any) => {
  reset();
  dialog.visible = true;
  dialog.loading = true;
  dialog.title = '编辑借阅申请审核';
  const _id = row?.id || ids.value[0];
  const res = await getLendLib(_id);
  Object.assign(form.value, res.data);
  dialog.loading = false;
};
/** 查看按钮操作 */
const handleDetail = async (row?: any) => {
  reset();
  dialog.visible = true;
  dialog.loading = true;
  dialog.title = '查看详情';
  const _id = row?.id || ids.value[0];
  const res = await getLendLib(_id);
  Object.assign(form.value, res.data);
  dialog.loading = false;
};

// 下载word
const handleExportWord = async (row: any) => {
  // const success = await downloadFileToLocal(
  //   [row.id].join(','),
  //   'DOC_FILE',
  //   `${row.dlName || 'file'}.doc`,
  //   'word'
  // );
  // if (success) {
  //   proxy?.$modal.msgSuccess('WORD导出成功');
  // } else {
  //   proxy?.$modal.msgError('WORD导出失败');
  // }
};

// 下载PDF
const handleExportPdf = async (row: any) => {
  // const success = await downloadFileToLocal(
  //   [row.id].join(','),
  //   'PDF_FILE',
  //   `${row.dlName || 'file'}.pdf`,
  //   'pdf'
  // );
  // if (success) {
  //   proxy?.$modal.msgSuccess('PDF导出成功');
  // } else {
  //   proxy?.$modal.msgError('PDF导出失败');
  // }
};


/** 提交按钮 */
const submitForm = () => {
  superFormRef.value?.validate(async (valid: boolean) => {
    if (valid) {
      const data = superFormRef.value.achieveData()
      buttonLoading.value = true;
      if (data.id) {
        await updateLendOrder(data).finally(() => (buttonLoading.value = false));
      } else {
        await addLendOrder(data).finally(() => (buttonLoading.value = false));
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
  await proxy?.$modal.confirm('是否确认删除模板名称为"' + _names + '"的数据项？').finally(() => (loading.value = false));
  await delLendOrder(_ids);
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
    `借阅申请审核_${new Date().getTime()}.xlsx`
  );
};

onMounted(() => {
  getList();
});
</script>

<style lang="scss" scoped>
.my-card{
  margin-top: 10px;
}
</style>
