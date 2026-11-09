<template>
  <div class="p-2">
    <!-- 搜索区域 -->
    <transition :enter-active-class="proxy?.animate.searchAnimate.enter"
      :leave-active-class="proxy?.animate.searchAnimate.leave">
      <div v-show="showSearch" class="mb-[10px]">
        <el-card shadow="hover">
          <super-search :showResetButton="false" code="bulkPrint-apply-index-search-form" v-model="mainQueryParams"
            @getList="getMainList" @resetQuery="resetMainQuery">
            <template #rightButton>
              <el-button icon="Plus" @click="handleAdd">新增</el-button>
            </template>
          </super-search>
        </el-card>
      </div>
    </transition>

    <!-- 主表格：批量打印申请列表 -->
    <el-card shadow="hover">
      <super-table v-if="showTable" code="bulkPrint-apply-index-list" rowKey="id" highlightCurrentRow
        :dataLoading="mainLoading" :data="mainList" v-model:queryParams="mainQueryParams" :total="mainTotal"
        @selection-change="handleMainSelectionChange" @getList="getMainList">
        <!-- 操作列 -->
        <template #operation="scope">
          <div>
            <el-tooltip content="编辑" placement="top" :enterable="false">
              <el-button link type="primary" v-hasPermi="['dms:printApplyBatch:edit']" icon="EditPen"
                @click="handleUpdate(scope.row)"></el-button>
            </el-tooltip>
            <el-tooltip content="删除" placement="top" :enterable="false">
              <el-button link type="danger" v-hasPermi="['dms:printApplyBatch:remove']" icon="Delete"
                @click="handleDelete(scope.row)"></el-button>
            </el-tooltip>
            <!-- 提交按钮 -->
            <el-popconfirm width="200px" hide-icon title="确定提交当前申请吗？" @confirm="openApprove(scope.row)"
              placement="left-start">
              <template #reference>
                <span style="margin-left: 12px;">
                  <el-tooltip content="提交审批" placement="top" :enterable="false">
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

    <!-- 新增/编辑对话框 -->
    <BulkPrintDialog v-model:visible="dialogVisible" :title="dialogTitle" :row="selectedRow" :is-edit="isEdit"
      @success="handleDialogSuccess" />

    <!-- 提交审批对话框 -->
    <el-dialog v-model="apprDialog.visible" :title="apprDialog.title" width="500px" append-to-body>
      <el-form>
        <el-form-item label="提交意见" prop="approveComments">
          <el-input v-model="apprRow.approveComments" v-hasPermi="['dms:printApplyBatch:submit']" type="textarea"
            placeholder="请输入提交意见"></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button :loading="buttonLoading" type="primary" @click="submitApprove">确 定</el-button>
          <el-button @click="apprCancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="BulkPrintApply" lang="ts">
import { submitAndFlowStartPrintApplyBatch, listPrintApply, deletePrintApplyBatch } from '@/api/dms/bulkPrint/index'
import BulkPrintDialog from '../BulkPrintDialog.vue';
import { ref, reactive, toRefs, onMounted, getCurrentInstance, type ComponentInternalInstance, nextTick } from 'vue';

const { proxy } = getCurrentInstance() as ComponentInternalInstance;

// 按钮加载状态
const buttonLoading = ref(false);
// 主表格加载状态
const mainLoading = ref(false);
// 是否显示搜索区域
const showSearch = ref(true);
// 主表格选中项ID
const mainSelectedIds = ref<Array<string | number>>([]);
// 是否显示表格
const showTable = ref(true);
// 主表格总条数
const mainTotal = ref(0);

// 对话框状态
const dialogVisible = ref(false);
const dialogTitle = ref('');
const selectedRow = ref({});
const isEdit = ref(false);

// 审批对话框配置
const apprDialog = reactive({
  title: '请输入提交意见',
  visible: false,
  loading: false
});

// 响应式数据
const data = reactive({
  // 主表格查询参数
  mainQueryParams: reactive({
    pageNum: 1,
    pageSize: 10
  })
});

const { mainQueryParams } = toRefs(data);

// 主表格数据
const mainList = ref([]);

// 审批行数据
const apprRow = ref({});

/** 查询批量打印申请列表（主表格） */
const getMainList = async () => {
  mainLoading.value = true;
  try {
    const res = await listPrintApply(mainQueryParams.value);
    mainList.value = res.rows || [];
    mainTotal.value = res.total || 0;
  } catch (error) {
    // console.error('查询批量打印申请列表失败:', error);
    proxy?.$modal.msgError(error.message || '查询批量打印申请列表失败');
  } finally {
    mainLoading.value = false;
  }
};

/** 主表格多选框选中数据 */
const handleMainSelectionChange = (selection) => {
  mainSelectedIds.value = selection.map(item => item.id);
};

/** 详情表格多选框选中数据 */
const handleDetailSelectionChange = (selection) => {
  detailSelectedIds.value = selection.map(item => item.id);
  // console.log('详情表格选中项ID:', detailSelectedIds.value);
};

/** 打开审批对话框 */
const openApprove = (row) => {
  apprDialog.visible = true;
  apprRow.value = { ...row };
  apprRow.value.approveComments = '';
};

/** 提交审批 */
const submitApprove = () => {
  apprDialog.visible = true;
  buttonLoading.value = true;
  // 提交意见的接口
  submitAndFlowStartPrintApplyBatch(apprRow.value).then(() => {
    proxy?.$modal.msgSuccess('提交成功!!');
    apprCancel();
    getMainList();
  }).catch((res) => {
    proxy?.$modal.msgError('提交失败!!');
  }).finally(() => {
    buttonLoading.value = false;
  })
};
/** 关闭审批对话框 */
const apprCancel = () => {
  apprDialog.visible = false;
  apprRow.value = {};
};

/** 搜索按钮操作 */
const handleQuery = () => {
  mainQueryParams.value.pageNum = 1;
  getMainList();
};

/** 重置搜索 */
const resetMainQuery = () => {
  handleQuery();
};

/** 新增按钮操作 */
const handleAdd = () => {
  // 重置对话框状态
  dialogTitle.value = '新增批量打印申请';
  selectedRow.value = {};
  isEdit.value = false;
  // 确保先将dialogVisible设置为false，再设置为true，以触发重新渲染
  dialogVisible.value = false;
  // 使用nextTick确保状态更新后再打开对话框
  nextTick(() => {
    dialogVisible.value = true;
  });
};

/** 编辑按钮操作 */
const handleUpdate = (row) => {
  dialogTitle.value = '编辑批量打印申请';
  selectedRow.value = row;
  isEdit.value = true;
  dialogVisible.value = true;
};

/** 对话框成功回调 */
const handleDialogSuccess = () => {
  getMainList();
};

/** 删除申请 */
const handleDelete = async (row) => {
  const deleteIds = row?.id || mainSelectedIds.value;
  const deleteCount = Array.isArray(deleteIds) ? deleteIds.length : 1;

  try {
    if (row) {
      await proxy?.$modal.confirm(`是否确认删除申请单编号为"${row.applyCode}"的数据项？`);
    } else {
      await proxy?.$modal.confirm(`是否确认删除选中的${deleteCount}个数据项？`);
    }

    await deletePrintApplyBatch(deleteIds);
    proxy?.$modal.msgSuccess('删除成功');
    await getMainList();
  } catch (error) {
    // 用户取消删除
    // console.log('取消删除');
    proxy?.$modal.msgInfo('删除操作已取消');
  }
};

// 组件挂载时获取主表格数据
onMounted(() => {
  getMainList();
});
</script>
