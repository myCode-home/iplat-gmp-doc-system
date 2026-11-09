<template>
  <div class="p-2">
    <transition :enter-active-class="proxy?.animate.searchAnimate.enter"
      :leave-active-class="proxy?.animate.searchAnimate.leave">
      <div v-show="showSearch" class="mb-[10px]">
        <el-card shadow="hover">
          <super-search :showResetButton="true" resetButtonText="刷新" code="batchRevision-track-search-form"
            v-model="queryParams" @getList="getList">
          </super-search>
        </el-card>
      </div>
    </transition>
    <!-- 打印申请审批列表 -->
    <el-card shadow="hover">
      <super-table v-if="showTable" code="batchRevision-track-index-list" rowKey="id" highlightCurrentRow
        :dataLoading="loading" :data="templateEditList" v-model:queryParams="queryParams" :total="total"
        @getList="getList">
        <!-- 操作列 -->
        <template #operation="scope">
          <div>
            <!-- 操作按钮详情，查看履历，撤回行数据 -->
            <el-tooltip content="详情" placement="top" :enterable="false">
              <el-button link type="primary" v-hasPermi="['dms:printApply:query']" icon="View"
                @click="handleDetail(scope.row)"></el-button>
            </el-tooltip>
            <el-tooltip content="查看审批履历" placement="top" :enterable="false">
              <el-button link type="primary" v-hasPermi="['dms:printApply:record']"
                :disabled="scope.row.status === 'edit'" icon="InfoFilled"
                @click="handleApprovalRecord(scope.row)"></el-button>
            </el-tooltip>
            <el-popconfirm title="确定撤回当前行数据吗？" @confirm="submitApprove(scope.row.id)">
              <template #reference>
                <span style="margin-left: 12px;">
                  <el-tooltip content="撤回" placement="top" :enterable="false">
                    <el-button link type="primary" :disabled="scope.row.status !== 'approving'"
                      v-hasPermi="['dms:printApply:revoke']" icon="Back"></el-button>
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
        code="printApply-check-detail" v-model="form" :form-rules="rules" :methods="formMethods">
      </super-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="cancel">关 闭</el-button>
        </div>
      </template>
    </el-dialog>
    <!-- 审批记录 -->
    <approvalRecord ref="approvalRecordRef" />
    <!-- 详情查看组件 -->
    <BaseView ref="baseViewRef" :showAddButton="false" :showSearchForm="true" :disabled="true" @submit="handleBaseViewSubmit" @cancel="handleBaseViewCancel" @close="handleBaseViewClose" />
  </div>
</template>

<script setup name="PrintApplyApproveTracking" lang="ts">
// import { getPrintApplyDetail, approvalTracking } from '@/api/dms/printDistribution/printApply/index';
import { getBatchReviseOrderMainTrackingList, getBatchReviseOrderMainDetail } from '@/api/dms/batchRevision/index';
import { cancelProcessApply } from '@/api/workflow/instance';
import BaseView from '../BaseView.vue';
const { proxy } = getCurrentInstance() as ComponentInternalInstance;

const loading = ref(false);
const showSearch = ref(true);
const ids = ref<Array<string | number>>([]);
const showTable = ref(true);
const total = ref(0);
// 打印申请编辑表单
const superFormRef = ref(null);
// 详情查看组件引用
const baseViewRef = ref(null);

const dialog = reactive<DialogOption>({
  title: '',
  visible: false,
  loading: false
});

const initFormData = {
};
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
    const res = await getBatchReviseOrderMainTrackingList(queryParams.value);
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

/** 查看按钮操作 */
const handleDetail = async (row?: any) => {
  try {
    const _id = row?.id || ids.value[0];
    if (!_id) {
      proxy?.$modal.msgWarning('请选择要查看的记录');
      return;
    }

    // 显示BaseView组件
    if (baseViewRef.value) {
      // 设置弹窗标题
      baseViewRef.value.dialog.title = '批量修订申请详情';
      baseViewRef.value.dialog.loading = true;
      baseViewRef.value.dialog.visible = true;

      // 获取详情数据
      const res = await getBatchReviseOrderMainDetail(_id, { includeDetails: true });

      // 填充表单数据
      Object.assign(baseViewRef.value.form, res.data || res);

      // 模拟填充表格数据（实际应根据API返回的数据结构进行调整）
      if (res.data?.details) {
        baseViewRef.value.detailList = res.data.details;
      }

      // 设置为只读状态
      // 这里需要在BaseView组件中添加disabled属性支持
      // 暂时通过禁用相关按钮和表单来实现只读效果

      baseViewRef.value.dialog.loading = false;
    }
  } catch (error) {
    proxy?.$modal.msgError('加载详情失败，请重试');
    console.error('加载详情异常：', error);
    if (baseViewRef.value) {
      baseViewRef.value.dialog.loading = false;
    }
  }
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

/** BaseView组件提交事件处理 */
const handleBaseViewSubmit = (data: any) => {
  console.log('BaseView提交数据：', data);
  // 详情查看模式下，不需要提交数据
  proxy?.$modal.msgWarning('详情查看模式下不支持提交操作');
};

/** BaseView组件取消事件处理 */
const handleBaseViewCancel = () => {
  console.log('BaseView取消操作');
  // 取消操作由BaseView组件内部处理
};

/** BaseView组件关闭事件处理 */
const handleBaseViewClose = () => {
  console.log('BaseView关闭操作');
  // 关闭操作由BaseView组件内部处理
};

onMounted(() => {
  getList();
});
</script>
