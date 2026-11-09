<template>
  <div class="p-2">
    <!-- 搜索区域 -->
    <transition :enter-active-class="proxy?.animate.searchAnimate.enter"
      :leave-active-class="proxy?.animate.searchAnimate.leave">
      <div v-show="showSearch" class="mb-[10px]">
        <el-card shadow="hover">
          <super-search :showResetButton="false" code="batchRevision-apply-index-search-from" v-model="queryParams"
            @getList="getList" @resetQuery="resetQuery" />
        </el-card>
      </div>
    </transition>

    <!-- 数据表格区域 -->
    <el-card shadow="hover">
      <template #header>
        <el-row :gutter="10" class="mb8">
          <el-col :span="1.5">
            <el-button v-hasPermi="['dms:docRecordFailureOrder:add']" type="primary" icon="Plus"
              @click="handleAdd">新增</el-button>
          </el-col>
          <right-toolbar v-model:show-search="showSearch" @query-table="getList" />
        </el-row>
      </template>
      <super-table v-if="showTable" code="batchRevision-apply-index-list" rowKey="id" highlightCurrentRow
        :dataLoading="loading" :data="templateEditList" v-model:queryParams="queryParams" :total="total"
        @selection-change="handleSelectionChange" @getList="getList">
        <!-- 操作列 -->
        <template #operation="scope">
          <div class="operation-group">
            <el-tooltip content="编辑" placement="top" :enterable="false">
              <el-button link type="primary" v-hasPermi="['dms:docRecordFailureOrder:edit']" icon="EditPen"
                @click="handleUpdate(scope.row)" />
            </el-tooltip>
            <el-tooltip content="删除" placement="top" :enterable="false">
              <el-button link type="danger" v-hasPermi="['dms:docRecordFailureOrder:remove']" icon="Delete"
                @click="handleDelete(scope.row)" />
            </el-tooltip>
            <el-tooltip content="文件清单" placement="top" :enterable="false">
              <el-button link type="primary" v-hasPermi="['dms:docRecordFailureOrder:remove']" icon="Document"
                @click="handleFileList(scope.row)" />
            </el-tooltip>
            <!-- 提交审批按钮 -->
            <el-popconfirm width="200px" hide-icon title="确定提交当前行数据吗？" @confirm="openApprove(scope.row)"
              placement="left-start">
              <template #reference>
                <span class="submit-btn">
                  <el-tooltip content="提交" placement="top" :enterable="false">
                    <el-button link type="primary" v-hasPermi="['dms:docRecordFailureOrder:submit']"
                      icon="SuccessFilled" />
                  </el-tooltip>
                </span>
              </template>
            </el-popconfirm>
          </div>
        </template>
      </super-table>
    </el-card>

    <!-- 新增/编辑失效申请弹窗 -->
    <el-dialog v-model="dialog.visible" :title="dialog.title" width="70%" append-to-body @close="resetDialogData">
      <super-form v-if="dialog.visible" :loading="dialog.loading" ref="superFormRef"
        code="batchRevision-apply-add-detail-form" @selectData="selectData" v-model="form" :form-rules="rules"
        :methods="formMethods">
        <template #documentCodeSolt="config">
          <el-input v-model="form[config.prop]" suffix-icon="Document" @click.native="handSelect" v-bind="config"
            readonly></el-input>
        </template>
      </super-form>
      <el-card shadow="hover">
        <template #header>
          <el-row :gutter="10" class="mb8">
            <el-col :span="1.5">
              <el-button v-hasPermi="['dms:docRecordFailureOrder:add']" type="primary" icon="Plus"
                @click="handSelect">添加文件/记录</el-button>
            </el-col>
          </el-row>
        </template>
        <super-search :showResetButton="false" code="batchRevision-apply-file-search-form" v-model="queryParams"
          @getList="getList" @resetQuery="resetQuery" />
        <super-table v-if="showTable" code="batchRevision-apply-add-detail-list" rowKey="id" highlightCurrentRow
          :dataLoading="loading" :data="detailList" :total="detailList.length"
          @selection-change="handleSelectionChange">
          <template #operation="scope">
            <div class="operation-group">
              <el-tooltip content="查看" placement="top" :enterable="false">
                <el-button link type="primary" v-hasPermi="['dms:docRecordFailureOrder:remove']" icon="View"
                  @click="handleViewFile(scope.row)" />
              </el-tooltip>
              <el-tooltip content="删除" placement="top" :enterable="false">
                <el-button link type="danger" v-hasPermi="['dms:docRecordFailureOrder:remove']" icon="Delete"
                  @click="handleDeleteFile(scope.row)" />
              </el-tooltip>
            </div>
          </template>
        </super-table>
      </el-card>
      <template #footer>
        <el-button type="primary" @click="handleSubmit"
          :loading="buttonLoading">保存</el-button>
        <el-button @click="handleCancel">取消</el-button>
      </template>
    </el-dialog>

    <!-- 文件选择弹窗 -->
    <el-dialog v-model="fileSelectDialog.visible" :title="fileSelectDialog.title" width="80%" append-to-body
      :z-index="9999">
      <ActiveFileLibSelect :isBatchRevision="true" ref="documentRef" :multipleChoice="multiple"
        :otherCondition="otherCondition" :multipleChoiceData="multipleChoiceData" :useBy="true"></ActiveFileLibSelect>
      <template #footer>
        <div class="dialog-footer">
          <el-button :loading="buttonLoading" type="primary" @click="handleSelectDataConfirm"
            :disabled="isConfirmButtonDisabled">确 定</el-button>
          <el-button @click="handleSelectDataCancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 提交审批弹窗 -->
    <el-dialog v-model="apprDialog.visible" :title="apprDialog.title" width="500px" append-to-body>
      <el-form>
        <el-form-item label="提交意见" prop="approveComments">
          <el-input v-model="apprRow.approveComments" v-hasPermi="['dms:docRecordFailureOrder:submit']" type="textarea"
            placeholder="请输入提交意见"></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button v-hasPermi="['dms:docRecordFailureOrder:submit']" :loading="buttonLoading" type="primary"
            @click="submitApprove">确 定</el-button>
          <el-button @click="apprCancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 文件清单弹窗 -->
    <el-dialog v-model="fileListDialog.visible" :title="fileListDialog.title" width="90%" append-to-body>
      <BaseIndex :getListByFather="getListForSon" minaTableCode="batchRevision-apply-file-list" />
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="fileListDialog.visible = false">关闭</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 文件详情弹窗 -->
    <ViewDetail :isBatchRevision="true" :visible="viewDetailVisible" :title="'文件清单详情'" :rowData="viewDetailRowData"
      :isreplace="false" @update:visible="(val) => viewDetailVisible = val" />
  </div>
</template>

<script setup name="RecordInvalidationApply" lang="ts">
import { ref, reactive, toRefs, onMounted, getCurrentInstance, watch, type ComponentInternalInstance } from 'vue';
import { validateBatchReviseOrderMain, submitAndFlowStart, getBatchReviseOrderMainEditList, addBatchReviseOrderMain, getBatchReviseOrderMainDetail, updateBatchReviseOrderMain, deleteBatchReviseOrderMain, deleteBatchReviseOrderDetail, getBatchReviseOrderDetailList } from '@/api/dms/batchRevision/index';
const ActiveFileLibSelect = defineAsyncComponent(() => import('@/views/dms/fileLib/activeFileLib/select.vue'));
import { useUserStore } from '@/store/modules/user';
import BaseIndex from '@/views/dms/fileLib/BaseIndex.vue';
import ViewDetail from '@/views/dms/fileLib/ViewDetail.vue';
// 全局代理
const { proxy } = getCurrentInstance() as ComponentInternalInstance;

// 基础状态管理
const buttonLoading = ref(false);
const loading = ref(false);
const showSearch = ref(true);
const showTable = ref(true);
const total = ref(0);
const ids = ref<Array<string | number>>([]);
const names = ref<Array<string | number>>([]);
const single = ref(true);
const multiple = ref(true);
// 编辑模式标记
const isEditMode = ref(false);

// 选中记录数据（用于校验）
const selectedRecordData = ref<{ id: number | string } | null>(null);

// 数据中转表格数据
const detailList = ref<any[]>([]);

// 其他条件配置
const otherCondition = ref({});

// 多选数据回调
const multipleChoiceData = ref((data: any) => {
  selectedFiles.value = data;
});

// 选中的文件数据
const selectedFiles = ref<any[]>([]);

// 确认按钮是否可点击
const isConfirmButtonDisabled = ref(true);

// 弹窗配置
const dialog = reactive({
  title: '',
  visible: false,
  loading: false
});
const apprDialog = reactive({
  title: '请输入提交意见',
  visible: false,
  loading: false
});
const fileListDialog = reactive({
  title: '文件清单',
  visible: false,
  loading: false
});
// 文件选择弹窗配置
const fileSelectDialog = reactive({
  title: '选择文件/记录',
  visible: false,
  loading: false
});

// ViewDetail组件状态管理
const viewDetailVisible = ref(false);
const viewDetailRowData = ref<any>(null);

// 监听选中文件变化，控制确认按钮状态
watch(
  () => selectedFiles.value,
  (newVal) => {
    const selectedItems = Array.isArray(newVal) ? newVal : [newVal];
    isConfirmButtonDisabled.value = selectedItems.length === 0;
  },
  { deep: true }
);

// 表单相关配置
const rules = ref({});
const formMethods = ref({});

// 表单数据
const form = reactive({
  documentLibIds: [], // 主键ID（区分新增/编辑）
  details: [], // 修订详情列表
  orderReason: '', // 失效原因
  remark: '' // 备注
});

// 查询参数
const dataDetail = reactive({
  queryParams: reactive({
    pageNum: 1,
    pageSize: 10
  }),
});
const { queryParams } = toRefs(dataDetail);

// 选择组件配置
const selectOption = ref({
  open: false,
  type: undefined,
  width: '',
  title: undefined,
  multiple: false,
  otherCondition: {}
});

// 表单校验结果
const validateResult = reactive({
  orderReason: '', // 失效原因校验提示
  selectedRecord: '' // 记录选择校验提示
});

// 表格数据
const templateEditList = ref<any[]>([]);

// ActiveFileLibSelect组件引用
const documentRef = ref<any>(null);

/** 查询列表数据 */
const getList = async () => {
  loading.value = true;
  try {
    const res = await getBatchReviseOrderMainEditList(queryParams.value);
    // console.log(queryParams.value, res);
    templateEditList.value = res.rows;
    total.value = res.total;
  } catch (error) {
    proxy?.$modal.msgError('查询列表失败，请重试');
    console.error('查询列表异常：', error);
  } finally {
    loading.value = false;
  }
};

/** 新增弹窗-保存操作 */
const handleSubmit = async () => {
  // 先检查表格是否有数据
  if (detailList.value.length === 0) {
    proxy?.$modal.msgWarning('请至少添加一条文件数据');
    return;
  }
  // 设置选中记录数据（如果还未设置）
  if (!selectedRecordData.value && detailList.value.length > 0) {
    selectedRecordData.value = { id: detailList.value[0].id };
    form.details = detailList.value[0].id;
  }
  buttonLoading.value = true;
  try {
    // 构建请求参数
    const requestData = {
      orderReason: form.orderReason?.trim() || '',
      applyUser: useUserStore().nickname,
      documentLibIds: detailList.value.map(item => item.id),
      details: detailList.value.map(item => item.id),
      remark: form.remark?.trim() || ''
    };

    // 数据验证
    if (!requestData.orderReason) return proxy?.$modal.msgWarning('请输入申请原因');
    if (!requestData.documentLibIds) return proxy?.$modal.msgWarning('请至少选择一条文件数据');
    console.log('请求数据：!!!!!!', requestData);
    // 判断是新增还是编辑模式
    let res;
    if (isEditMode.value) {
      // 编辑
      requestData.details = detailList.value.map(item => item.orderId);
      console.log('编辑模式请求数据：#####', requestData);
      res = await updateBatchReviseOrderMain({ ...requestData, id: form.id });
    } else {
      // 新增
      res = await addBatchReviseOrderMain(requestData);
    }

    proxy?.$modal.msgSuccess('提交成功');

    // 关闭弹窗并重置数据
    resetDialogData();
    await getList();
  } catch (error) {
    proxy?.$modal.msgError('操作失败，请重试');
    console.error('提交异常：', error);
  } finally {
    buttonLoading.value = false;
  }
};

/** 关闭选择组件 */
const handleSelectDataCancel = () => {
  fileSelectDialog.visible = false;
  selectOption.value = {
    open: false,
    type: undefined,
    width: '',
    title: undefined,
    multiple: false,
    otherCondition: {}
  };

  // 重置选择组件状态
  selectedFiles.value = [];
  isConfirmButtonDisabled.value = true;

  // 清空ActiveFileLibSelect组件的多选框选中状态
  if (documentRef.value && documentRef.value.clearSelection) {
    documentRef.value.clearSelection();
  }
};

/** 打开选择组件 */
const handSelect = () => {
  fileSelectDialog.visible = true;
};

/** 确认选择文件 */
const handleSelectDataConfirm = () => {
  // 确保data是数组格式，即使单选也转为数组处理
  const selectedItems = Array.isArray(selectedFiles.value) ? selectedFiles.value : [selectedFiles.value];
  if (selectedItems.length === 0) {
    proxy?.$modal.msgWarning('未选择任何数据');
    return;
  }
  // 获取已存在的id，防止重复添加
  const existingIds = detailList.value.map(item => item.id);

  // 处理并添加新数据
  const newItems = selectedItems
    .filter(item => {
      // 检查是否已存在（根据关键字段判断，而不仅仅是id）
      return !existingIds.includes(item.id) &&
        !detailList.value.some(existing =>
          (existing.dlCode || existing.code) === (item.dlCode || item.code)
        );
    })
    .map((item) => {
      return {
        ...item
      };
    });

  if (newItems.length === 0) {
    proxy?.$modal.msgWarning('选择的数据已存在');
  } else {
    // 添加到详情表格 - 这里是多选框数据复现到表格的关键逻辑
    detailList.value = [...detailList.value, ...newItems];
    proxy?.$modal.msgSuccess(`成功添加${newItems.length}条数据`);

    // 设置选中记录数据
    if (newItems.length > 0) {
      selectedRecordData.value = { id: newItems[0].id };
      form.documentRecordLibraryId = newItems[0].id;
    }
  }

  // 关闭弹窗并重置选择状态
  fileSelectDialog.visible = false;
  selectedFiles.value = [];
  isConfirmButtonDisabled.value = true;

  // 清空ActiveFileLibSelect组件的多选框选中状态
  if (documentRef.value && documentRef.value.clearSelection) {
    documentRef.value.clearSelection();
  }
};

/** 表格多选事件 */
const handleSelectionChange = (selection: any[]) => {
  ids.value = selection.map((item) => item.id);
  names.value = selection.map((item) => item.tmplName);
  single.value = selection.length !== 1;
  multiple.value = !selection.length;
};

/** 新增按钮操作 */
const handleAdd = () => {
  dialog.title = '新增批量修订申请';
  // 重置表单数据 - 确保完全清空所有历史数据
  resetDialogData();
  // 设置为新增模式
  isEditMode.value = false;
  // 打开弹窗
  dialog.visible = true;
};

/** 编辑按钮操作 */
const handleUpdate = async (row?: any) => {
  dialog.loading = true;
  dialog.title = '编辑批量修订申请';

  try {
    const _id = row?.id || ids.value[0];
    dialog.visible = true;
    // 编辑
    isEditMode.value = true;
    // 实际调用API请求函数
    const res = await getBatchReviseOrderMainDetail(_id, { includeDetails: true });
    // 回显表单主数据
    if (res.data) {
      Object.assign(form, res.data);
    }
    // 回显表格数据 - 处理details数组
    if (res.data && res.data.details && Array.isArray(res.data.details)) {
      // 确保数据格式正确，与detailList要求的格式匹配
      detailList.value = res.data.details.map((item: any) => {
        return {
          id: item.id,
          dlCode: item.documentCode,
          dlName: item.documentName,
          issueDeptName: item.drafterDeptName,
          dlLevel: item.documentLevel,
          dlVersion: item.documentVersion,
          userDeptName: item.createDeptName,
          orderId: item.orderId,
        };
      });
      console.log('回显的表格数据：fuck 黄！！！！！', detailList.value);
      // 如果有数据，设置选中记录
      if (detailList.value.length > 0) {
        selectedRecordData.value = { id: detailList.value[0].id };
        form.documentLibIds = detailList.value.map(item => item.id);
        form.details = detailList.value.map(item => item.id);
      }
    } else {
      // 处理空数据情况
      detailList.value = [];
      selectedRecordData.value = null;
      form.documentLibIds = [];
      form.details = [];
    }

  } catch (error) {
    proxy?.$modal.msgError('获取详情失败，请重试');
    console.error('获取详情异常：', error);
    // 异常情况下重置表格数据
    detailList.value = [];
    selectedRecordData.value = null;
  } finally {
    dialog.loading = false;
  }
};

/** 重置弹窗数据 */
const resetDialogData = () => {
  // 重置表单状态 - 完全重置所有可能的字段
  form.orderReason = '';
  form.remark = '';
  form.documentLibIds = [];
  form.details = [];

  form.orderNumber = '';
  form.createTime = '';
  form.createDeptName = '';
  form.orderUserName = '';
  form.orderTime = '';
  form.updateTime = '';

  // 重置编辑模式标记
  isEditMode.value = false;
  selectedRecordData.value = null;
  validateResult.orderReason = '';
  validateResult.selectedRecord = '';
  // 重置表格数据
  detailList.value = [];
  ids.value = [];
  // 重置选择组件状态
  selectedFiles.value = [];
  isConfirmButtonDisabled.value = true;
  // 清空ActiveFileLibSelect组件的多选框选中状态
  if (documentRef.value && documentRef.value.clearSelection) {
    documentRef.value.clearSelection();
  }
  // 关闭弹窗
  dialog.visible = false;
  fileSelectDialog.visible = false;
};

/** 新增弹窗-取消操作 */
const handleCancel = () => {
  resetDialogData();
};
/** super-form选择数据回调 */
const selectData = (data: any) => {
  console.log('super-form选择数据：', data);
};

/** 删除操作 */
const handleDelete = async (row?: any) => {
  console.log('删除操作：', row);
  // 删除点击的行的数据
  try {
    await deleteBatchReviseOrderMain(row.id);
    proxy?.$modal.msgSuccess('删除成功');
    await getList();
  } catch (error) {
    proxy?.$modal.msgError(error?.msg);
    console.error('删除异常：', error);
  }
};

/** 查看文件详情 */
const handleViewFile = (row: any) => {
  if (row && row.id) {
    // 设置要查看的文件数据
    viewDetailRowData.value = row;
    // 显示ViewDetail组件
    viewDetailVisible.value = true;
  } else {
    proxy?.$modal.msgWarning('请选择有效的文件数据');
  }
};

/** 删除文件表格中的行数据 */
const handleDeleteFile = async (row: any) => {
  if (row) {
    // 删除表格中的单行数
    const index = detailList.value.findIndex(item => item.id === row.id);
    if (index !== -1) {
      await proxy?.$modal.confirm(`是否确认删除数据项？`);
      detailList.value.splice(index, 1);
      // 调用删除API
      try {
        await deleteBatchReviseOrderDetail(row.id);
        proxy?.$modal.msgSuccess('删除成功');

        // 从选中项中移除
        const idIndex = ids.value.indexOf(row.id);
        if (idIndex !== -1) {
          ids.value.splice(idIndex, 1);
        }

        // 如果删除后表格为空，重置选中记录数据
        if (detailList.value.length === 0) {
          selectedRecordData.value = null;

        } else if (!selectedRecordData.value) {
          // 如果删除后表格不为空但选中记录数据为空，设置为第一个数据项
          selectedRecordData.value = { id: detailList.value[0].id };
          form.documentLibIds = [detailList.value[0].id];
        }
      } catch (error) {
        proxy?.$modal.msgError(error?.msg);
        return;
      }
    }
  }
};

/** 搜索按钮操作 */
const handleQuery = () => {
  queryParams.value.pageNum = 1;
  getList();
};

/** 重置搜索条件 */
const resetQuery = () => {
  queryParams.value.pageNum = 1;
  queryParams.value.pageSize = 10;
  handleQuery();
};

/** 提交审批相关 */
const apprRow = ref();
const openApprove = (row: any) => {
  apprDialog.visible = true;
  apprRow.value = JSON.parse(JSON.stringify(row));
  apprRow.value.approveComments = '';
};

/** 提交审批操作 */
const submitApprove = async () => {
  try {
    await validateBatchReviseOrderMain(apprRow.value.id);
  } catch (error) {
    return;
  }
  buttonLoading.value = true;
  submitAndFlowStart(apprRow.value)
    .then(() => {
      proxy?.$modal.msgSuccess('提交成功!!');
      apprCancel();
      getList();
    })
    .catch((res) => {
      proxy?.$modal.msgError(res.msg || '提交失败，请重试');
    })
    .finally(() => {
      buttonLoading.value = false;
      // 隐藏编辑弹窗
      dialog.visible = false;
    });
};

/** 审批弹窗-取消操作 */
const apprCancel = () => {
  apprDialog.visible = false;
  apprRow.value = null;
};
// 存储当前点击的文件清单行数据
const currentFileListRow = ref<any>(null);

// 使用provide/inject传递isbatchRevisionUse值
provide('isbatchRevisionUse', true);
/** 子组件数据刷新方法 - 确保每次调用都获取最新数据 */
const getListForSon = async () => {
  try {
    if (currentFileListRow.value) {
      // 调用API获取最新的文件清单数据
      const res = await getBatchReviseOrderDetailList({
        orderId: currentFileListRow.value.orderId
      });
      return res;
    }
    return null;
  } catch (error) {
    console.error('获取文件清单数据失败：', error);
    proxy?.$modal.msgError('获取文件清单数据失败，请重试');
    return null;
  }
};
// 文件清单按钮
const handleFileList = (row: any) => {
  console.log('文件清单按钮点击', row);
  // 存储当前点击的行数据，供BaseIndex组件使用
  if (row) {
    currentFileListRow.value = row;
  }
  // 打开文件清单对话框
  fileListDialog.visible = true;
};
// 初始化列表数据
onMounted(() => {
  getList();
});
</script>

<style scoped>
.operation-group {
  display: flex;
  align-items: center;
}

.submit-btn {
  margin-left: 12px;
}

.mb-4 {
  margin-bottom: 16px;
}
</style>
