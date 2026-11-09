<template>
  <div class="p-2">
    <transition :enter-active-class="proxy?.animate.searchAnimate.enter"
      :leave-active-class="proxy?.animate.searchAnimate.leave">
      <div v-show="showSearch" class="mb-[10px]">
        <el-card shadow="hover">
          <super-search :showResetButton="true" resetButtonText="刷新" code="bulkPrint-approve-index-search-form"
            v-model="queryParams" @getList="getList">
          </super-search>
        </el-card>
      </div>
    </transition>
    <!-- 打印申请审批列表 -->
    <el-card shadow="hover">
      <super-table v-if="showTable" code="bulkPrint-approve-index-list" rowKey="id" highlightCurrentRow
        :dataLoading="loading" :data="templateEditList" v-model:queryParams="queryParams" :total="total"
        @getList="getList">
        <!-- 操作列 -->
        <template #operation="scope">
          <div>
            <!-- 详情按钮 -->
            <el-tooltip content="详情" placement="top">
              <el-button link type="primary" v-hasPermi="['dms:printApplyBatch:query']" icon="View"
                @click="handleDetail(scope.row)"></el-button>
            </el-tooltip>
            <!-- 提交按钮，加确认框 -->
            <el-popconfirm width="200px" hide-icon title="确定提交当前行数据吗？" @confirm="submitApprove(scope.row)"
              placement="left-start">
              <template #reference>
                <span style="margin-left: 12px;">
                  <el-tooltip content="提交" placement="top">
                    <el-button link type="primary" v-hasPermi="['dms:printApplyBatch:submit']"
                      icon="SuccessFilled"></el-button>
                  </el-tooltip>
                </span>
              </template>
            </el-popconfirm>
          </div>
        </template>
      </super-table>
    </el-card>
    <!-- 详情对话框 -->
    <BulkPrintDialog :isDisabledSearch="isDisabledSearch" v-model:visible="dialogVisible" :title="dialogTitle"
      :row="selectedRow" :is-edit="isEdit" @success="handleDialogSuccess" />
    <!-- 提交组件 -->
    <submitVerify ref="submitVerifyRef" :task-variables="taskVariables" @submit-callback="submitCallback" />
  </div>
</template>

<script setup name="BulkPrintApprove" lang="ts">
import { listPrintApplyApprove } from '@/api/dms/bulkPrint/index';
import BulkPrintDialog from '../BulkPrintDialog.vue';
const { proxy } = getCurrentInstance() as ComponentInternalInstance;
import { waitingTaskId } from '@/api/workflow/task';

const loading = ref(false);
const showSearch = ref(true);
const showTable = ref(true);
const total = ref(0);

// 对话框状态
const dialogVisible = ref(false);
const dialogTitle = ref('');
const selectedRow = ref({});
const isEdit = ref(true);

// 查询参数
const queryParamsData = reactive({
  queryParams: {
    pageNum: 1,
    pageSize: 10
  }
})
const { queryParams } = toRefs(queryParamsData);
const templateEditList = ref([]);

// 查询打印申请审核列表
const getList = async () => {
  loading.value = true;
  try {
    const res = await listPrintApplyApprove(queryParams.value);
    templateEditList.value = res.rows;
    total.value = res.total;
  } finally {
    loading.value = false;
  }
};
// 禁用搜索框
const isDisabledSearch = ref(true);
// 在父组件中预定义所有可能用到的方法
const formMethods = {
  // args参数集合 childFrom参数集合
  customChangeHandler: (args: Array<any>, childFrom: Object) => {
    superFormRef.value.updateData({ companyName: 'admin' })
    console.log('自定义change事件触发');
  },
  handleRoleChange: (args: Array<any>, childFrom: Object) => {
    console.log('自定义事件触发', args, childFrom);
  },
  handleSexChange: (args: Array<any>, childFrom: Object) => {
    console.log('自定义事件触发', args);
  },
  processCompanyData: (args: Array<any>, childFrom: Object) => {
    console.log('自定义数据处理', args, childFrom);
  }
};
/** 查看按钮操作 */
const handleDetail = (row?: any) => {
  dialogTitle.value = '查看详情';
  selectedRow.value = row;
  isEdit.value = true;
  dialogVisible.value = true;
};
//提交组件
const submitVerifyRef = ref();
const taskVariables = ref<Record<string, any>>({});

// 提交审批按钮加载状态
// 提交审批按钮操作
const submitApprove = (row: any) => {
  // 通过工作流实例ID获取当前等待处理的任务ID
  waitingTaskId(row.workflowId).then((res) => {
    // console.log(res, '打印申请审核详情'); // 这里的res返回的是 "2011620031437795330" 这样的工作流ID
    if (res.data) {
      submitVerifyRef.value.openDialog(res.data);
      submitVerifyRef.value.init(row.id);
    }
  });
};
//提交回调
const submitCallback = async () => {
  getList();
};

/** 对话框成功回调 */
const handleDialogSuccess = () => {
  getList();
};

onMounted(() => {
  getList();
});
</script>
