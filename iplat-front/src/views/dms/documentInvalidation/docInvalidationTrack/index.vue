<template>
  <div class="p-2">
    <transition :enter-active-class="proxy?.animate.searchAnimate.enter"
      :leave-active-class="proxy?.animate.searchAnimate.leave">
      <div v-show="showSearch" class="mb-[10px]">
        <el-card shadow="hover">
          <super-search :showResetButton="true" resetButtonText="刷新" code="docInvalidation-track-index-search-form"
            v-model="queryParams" @getList="getList">
          </super-search>
        </el-card>
      </div>
    </transition>
    <!-- 打印申请审批列表 -->
    <el-card shadow="hover">
      <super-table v-if="showTable" code="docInvalidation-track-index-list" rowKey="id" highlightCurrentRow
        :dataLoading="loading" :data="templateEditList" v-model:queryParams="queryParams" :total="total"
        @getList="getList">
        <!-- 操作列 -->
        <template #operation="scope">
          <div>
            <!-- 操作按钮详情，查看履历，撤回行数据 -->
            <el-tooltip content="详情" placement="top" :enterable="false">
              <el-button link type="primary" v-hasPermi="['dms:docInvalidOrder:query']" icon="View"
                @click="handleDetail(scope.row)"></el-button>
            </el-tooltip>
            <el-tooltip content="查看审批履历" placement="top" :enterable="false">
              <el-button link type="primary" v-hasPermi="['dms:docInvalidOrder:Record']"
                :disabled="scope.row.status === 'edit'" icon="InfoFilled"
                @click="handleApprovalRecord(scope.row)"></el-button>
            </el-tooltip>
            <el-popconfirm title="确定撤回当前行数据吗？" @confirm="submitApprove(scope.row.id)">
              <template #reference>
                <span style="margin-left: 12px;">
                  <el-tooltip content="撤回" placement="top" :enterable="false">
                    <el-button link type="primary" :disabled="scope.row.status !== 'approving'"
                      v-hasPermi="['dms:docInvalidOrder:revoke']" icon="Back"></el-button>
                  </el-tooltip>
                </span>
              </template>
            </el-popconfirm>
          </div>
        </template>
      </super-table>
    </el-card>
    <!-- 详情对话框（详情展示） -->
    <el-dialog v-model="dialog.visible" :title="dialog.title" width="70%" append-to-body>
      <super-form v-if="dialog.visible" :loading="dialog.loading" ref="printApply-check-detail" disabled
        code="docInvalidation-approve-edit-from" v-model="form" :form-rules="rules" :methods="formMethods">
      </super-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="cancel">关 闭</el-button>
        </div>
      </template>
      <PdfView v-if="form.documentLibId" :text="waterText" :id="form.documentLibId"></PdfView>
    </el-dialog>
    <!-- 审批记录 -->
    <approvalRecord ref="approvalRecordRef" />
  </div>
</template>

<script setup name="DocInvalidationTrack" lang="ts">
import { getDocInvalidationApplyList, getDocInvalidationApplyDetail } from '@/api/dms/documentInvalidation/index';
import { cancelProcessApply } from '@/api/workflow/instance';
import { useUserStore } from '@/store/modules/user';
const { proxy } = getCurrentInstance() as ComponentInternalInstance;

const loading = ref(false);
const showSearch = ref(true);
const ids = ref<Array<string | number>>([]);
const showTable = ref(true);
const total = ref(0);
// 打印申请编辑表单
const superFormRef = ref(null);

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
    // const res = await approvalTracking(queryParams.value);
    const res = await getDocInvalidationApplyList(queryParams.value);
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

const waterText = ref('')
/** 详情按钮操作 */
const handleDetail = async (row?: any) => {
  reset();
  dialog.visible = true;
  dialog.loading = true;
  dialog.title = '查看详情';
  const _id = row?.id || ids.value[0];
  const res = await getDocInvalidationApplyDetail(_id);
  // console.log('!!!!!!!!!!!!!!!!!!!!!', res);
  Object.assign(form.value, res.data);
  const { name, nickname } = useUserStore();
  const time = proxy?.parseTime(new Date(), '{y}-{m}-{d}');
  waterText.value = `${name} ${nickname} ${time} 生效`;
  dialog.loading = false;
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
