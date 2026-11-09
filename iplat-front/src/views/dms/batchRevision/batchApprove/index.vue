<template>
  <div class="p-2">
    <transition :enter-active-class="proxy?.animate.searchAnimate.enter"
      :leave-active-class="proxy?.animate.searchAnimate.leave">
      <div v-show="showSearch" class="mb-[10px]">
        <el-card shadow="hover">
          <super-search :showResetButton="true" resetButtonText="刷新" code="batchRevision-approve-search-form"
            v-model="queryParams" @getList="getList">
          </super-search>
        </el-card>
      </div>
    </transition>
    <!-- 打印申请审批列表 -->
    <el-card shadow="hover">
      <super-table v-if="showTable" code="batchRevision-approve-index-list" rowKey="id" highlightCurrentRow
        :dataLoading="loading" :data="templateEditList" v-model:queryParams="queryParams" :total="total"
        @getList="getList">
        <!-- 操作列 -->
        <template #operation="scope">
          <div>
            <!-- 详情按钮 -->
            <el-tooltip content="详情" placement="top" :enterable="false">
              <el-button link type="primary" v-hasPermi="['dms:printApply:query']" icon="View"
                @click="handleDetail(scope.row)"></el-button>
            </el-tooltip>
            <!-- 提交按钮，加确认框 -->
            <el-popconfirm width="200px" hide-icon title="确定提交当前行数据吗？" @confirm="submitApprove(scope.row)"
              placement="left-start">
              <template #reference>
                <span style="margin-left: 12px;">
                  <el-tooltip content="提交" placement="top" :enterable="false">
                    <el-button link type="primary" v-hasPermi="['dms:printApply:submit']"
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
    <el-dialog v-model="dialog.visible" :title="dialog.title" width="70%" append-to-body>
      <super-form v-if="dialog.visible" :loading="dialog.loading" ref="superFormRef" disabled
        code="printApply-check-detail" v-model="form" :form-rules="rules" :methods="formMethods">
      </super-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="cancel">关 闭</el-button>
        </div>
      </template>
    </el-dialog>
    <!-- 提交组件 -->
    <submitVerify ref="submitVerifyRef" :task-variables="taskVariables" @submit-callback="submitCallback" />
  </div>
</template>

<script setup name="PrintApplyApprove" lang="ts">
import { approvalSubmit, getPrintApplyDetail } from '@/api/dms/printDistribution/printApply/index';
import { getBatchReviseOrderMainApprovalList } from '@/api/dms/batchRevision/index';
const { proxy } = getCurrentInstance() as ComponentInternalInstance;
import { waitingTaskId } from '@/api/workflow/task';

const loading = ref(false);
const showSearch = ref(true);
const ids = ref<Array<string | number>>([]);
const showTable = ref(true);
const total = ref(0);
// 打印申请审核详情表单
const superFormRef = ref(null);

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

// 查询打印申请审核列表
const getList = async () => {
  loading.value = true;
  try {
    // const res = await approvalSubmit(queryParams.value);
    const res = await getBatchReviseOrderMainApprovalList(queryParams.value);
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
const handleDetail = async (row?: any) => {
  reset();
  dialog.visible = true;
  dialog.loading = true;
  dialog.title = '查看详情';
  const _id = row?.id || ids.value[0];
  const res = await getPrintApplyDetail(_id);
  Object.assign(form.value, res.data);
  dialog.loading = false;
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
