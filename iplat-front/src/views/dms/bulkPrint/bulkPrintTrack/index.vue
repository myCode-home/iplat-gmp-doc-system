<template>
  <div class="p-2">
    <transition :enter-active-class="proxy?.animate.searchAnimate.enter"
      :leave-active-class="proxy?.animate.searchAnimate.leave">
      <div v-show="showSearch" class="mb-[10px]">
        <el-card shadow="hover">
          <super-search :showResetButton="true" resetButtonText="刷新" code="bulkPrint-track-index-search-form"
            v-model="queryParams" @getList="getList">
          </super-search>
        </el-card>
      </div>
    </transition>
    <!-- 打印申请审批列表 -->
    <el-card shadow="hover">
      <super-table v-if="showTable" code="bulkPrint-track-index-list" rowKey="id" highlightCurrentRow
        :dataLoading="loading" :data="templateEditList" v-model:queryParams="queryParams" :total="total"
        @getList="getList">
        <!-- 操作列 -->
        <template #operation="scope">
          <div>
            <!-- 操作按钮详情，查看履历，撤回行数据 -->
            <el-tooltip content="详情" placement="top">
              <el-button link type="primary" v-hasPermi="['dms:printApplyBatch:query']" icon="View"
                @click="handleDetail(scope.row)"></el-button>
            </el-tooltip>
            <el-tooltip content="查看审批履历" placement="top">
              <el-button link type="primary" v-hasPermi="['dms:printApplyBatch:Record']"
                :disabled="scope.row.status === 'edit'" icon="InfoFilled"
                @click="handleApprovalRecord(scope.row)"></el-button>
            </el-tooltip>
            <el-popconfirm title="确定撤回当前行数据吗？" @confirm="submitApprove(scope.row.id)">
              <template #reference>
                <span style="margin-left: 12px;">
                  <el-tooltip content="撤回" placement="top">
                    <el-button link type="primary" :disabled="scope.row.status !== 'approving'"
                      v-hasPermi="['dms:printApplyBatch:revoke']" icon="Back"></el-button>
                  </el-tooltip>
                </span>
              </template>
            </el-popconfirm>
          </div>
        </template>
      </super-table>
    </el-card>
    <!-- 详情对话框（详情展示） -->
    <!-- <el-dialog v-model="dialog.visible" :title="dialog.title" width="70%" append-to-body>
      <super-form v-if="dialog.visible" :loading="dialog.loading" ref="printApply-check-detail" disabled
        code="recordInvalidation-track-detail-form" v-model="form" :form-rules="rules" :methods="formMethods">
      </super-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="cancel">关 闭</el-button>
        </div>
      </template>
      <PdfView v-if="form.documentRecordLibraryId" :text="waterText" :id="form.documentRecordLibraryId"></PdfView>
    </el-dialog> -->
    <!-- 审批记录 -->
    <approvalRecord ref="approvalRecordRef" />
    <!-- 详情对话框 -->
    <BulkPrintDialog :isDisabledSearch="isDisabledSearch" v-model:visible="dialogVisible" :title="dialogTitle"
      :row="selectedRow" :is-edit="isEdit" @success="handleDialogSuccess" />
  </div>
</template>

<script setup name="PrintApplyApproveTracking" lang="ts">
import { useUserStore } from '@/store/modules/user';
import { getRecordInvalidationApplyList, getRecordInvalidationApplyDetail } from '@/api/dms/recordInvalidation/index';
import { listPrintApplyTrack } from '@/api/dms/bulkPrint/index';
import { cancelProcessApply } from '@/api/workflow/instance';
import BulkPrintDialog from '../BulkPrintDialog.vue';
const { proxy } = getCurrentInstance() as ComponentInternalInstance;

const loading = ref(false);
const showSearch = ref(true);
const ids = ref<Array<string | number>>([]);
const showTable = ref(true);
const total = ref(0);
// 打印申请编辑表单
const superFormRef = ref(null);

// 对话框状态
const dialogVisible = ref(false);
const dialogTitle = ref('');
const selectedRow = ref({});
const isEdit = ref(true);
// 禁用搜索框
const isDisabledSearch = ref(true);

const dialog = reactive<DialogOption>({
  title: '',
  visible: false,
  loading: false
});

const initFormData = {};
const data = reactive<PageData<any, any>>({
  form: { ...initFormData },
  queryParams: reactive({
    pageNum: 1,
    pageSize: 10
  }),
  rules: {
    // id: [{ required: true, message: '主键不能为空', trigger: 'blur' }],
  }
});

const { queryParams, form, rules } = toRefs(data);

const templateEditList = ref([]);

/** 查询打印申请审核跟踪列表 */
const getList = async () => {
  loading.value = true;
  try {
    const res = await listPrintApplyTrack(queryParams.value);
    templateEditList.value = res.rows;
    total.value = res.total;
  } finally {
    loading.value = false;
  }
};

// 在父组件中预定义所有可能用到的方法
const formMethods = {
  // args参数集合 childFrom参数集合
  customChangeHandler: (args: Array<any>, childFrom: Object) => {
    superFormRef.value.updateData({ companyName: 'admin' })
    console.log('自定义change事件触发')
  },
  handleRoleChange: (args: Array<any>, childFrom: Object) => {
    console.log('自定义事件触发', args, childFrom)
  },
  handleSexChange: (args: Array<any>, childFrom: Object) => {
    console.log('自定义事件触发', args);
  },
  processCompanyData: (args: Array<any>, childFrom: Object) => {
    console.log('自定义数据处理', args, childFrom);
  }
};

/** 详情按钮操作 */
const handleDetail = (row?: any) => {
  dialogTitle.value = '查看详情';
  selectedRow.value = row;
  isEdit.value = true;
  dialogVisible.value = true;
};

// 撤回按钮操作
const submitApprove = async (id: any) => {
  const data = {
    businessId: id,
    message: '申请人撤销流程！'
  };
  try {
    loading.value = true;
    await cancelProcessApply(data).finally(() => (loading.value = false));
    getList();
    proxy?.$modal.msgSuccess('撤销成功');
  } finally {
    loading.value = false;
  }
};
//审批记录组件
const approvalRecordRef = ref();
// 审批记录按钮操作
const handleApprovalRecord = (row: any) => {
  approvalRecordRef.value.init(row.id);
};

/** 对话框成功回调 */
const handleDialogSuccess = () => {
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


onMounted(() => {
  getList();
});
</script>
