<template>
  <div class="p-2">
    <!-- 搜索区域 -->
    <transition :enter-active-class="proxy?.animate.searchAnimate.enter"
      :leave-active-class="proxy?.animate.searchAnimate.leave">
      <div v-show="showSearch" class="mb-[10px]">
        <el-card shadow="hover">
          <super-search :showResetButton="false" code="docInvalidation-apply-search-form" v-model="queryParams"
            @getList="getList" @resetQuery="resetQuery" />
        </el-card>
      </div>
    </transition>

    <!-- 数据表格区域 -->
    <el-card shadow="hover">
      <template #header>
        <el-row :gutter="10" class="mb8">
          <el-col :span="1.5">
            <el-button type="primary" icon="Plus"
              @click="handleAdd">新增</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button type="danger" :disabled="multiple"
              icon="Delete" @click="handleBatch">批量删除</el-button>
          </el-col>
          <right-toolbar v-model:show-search="showSearch" @query-table="getList" />
        </el-row>
      </template>
      <super-table ref="superTableRef" v-if="showTable" code="docInvalidation-apply-index-list" rowKey="id"
        highlightCurrentRow :dataLoading="loading" :data="templateEditList" v-model:queryParams="queryParams"
        :total="total" @selection-change="handleSelectionChange" @getList="getList">
        <!-- 操作列 -->
        <template #operation="scope">
          <div class="operation-group">
            <el-tooltip content="编辑" placement="top" :enterable="false">
              <el-button link type="primary" v-hasPermi="['dms:docInvalidOrder:edit']" icon="EditPen"
                @click="handleUpdate(scope.row)" />
            </el-tooltip>
            <el-tooltip content="删除" placement="top" :enterable="false">
              <el-button link type="danger" v-hasPermi="['dms:docInvalidOrder:remove']" icon="Delete"
                @click="handleDelete(scope.row)" />
            </el-tooltip>
            <!-- 提交审批按钮 -->
            <el-popconfirm width="200px" hide-icon title="确定提交当前行数据吗？" @confirm="openApprove(scope.row)"
              placement="left-start">
              <template #reference>
                <span class="submit-btn">
                  <el-tooltip content="提交" placement="top" :enterable="false">
                    <el-button link type="primary" v-hasPermi="['dms:docInvalidOrder:submit']"
                      icon="SuccessFilled" />
                  </el-tooltip>
                </span>
              </template>
            </el-popconfirm>
          </div>
        </template>
      </super-table>
    </el-card>

    <!-- 新增失效申请弹窗 -->
    <el-dialog v-model="dialog.visible" :title="dialog.title" width="70%" append-to-body :close-on-click-modal="false"
      @close="handleDialogClose">
      <el-form-item label="失效原因" required>
        <el-input v-model="form.orderReason" required placeholder="请输入失效原因" type="textarea" :rows="3" class="mb-4"
          :error="!!validateResult.orderReason" />
      </el-form-item>
      <div v-if="validateResult.orderReason" class="text-red-500 text-sm mb-4">
        {{ validateResult.orderReason }}
      </div>
      <!-- 文件选择组件 -->
      <ActiveFileLibSelect :useBy="true" :isActiveStatus="true" :searchCode="searchCode" ref="activeFileLibSelectRef"
        :visible.sync="activeFileLibSelectVisible" :title="selectOption.title" :selectType="selectOption.type"
        :multiple="selectOption.multiple" :width="selectOption.width" :otherCondition="selectOption.otherCondition"
        :achieveData="handleSelectDataAchieve" :multipleChoiceData="handleSelectionChangeDetail"
        @selection-change="handleSelectionChangeNew" @dialogClose="handleSelectDataCancel" />

      <div v-if="validateResult.selectedRecord" class="text-red-500 text-sm mb-4">
        {{ validateResult.selectedRecord }}
      </div>

      <template #footer>
        <el-button v-hasPermi="['dms:docInvalidOrder:submit']" type="primary" @click="handleSubmit"
          :loading="buttonLoading">保存</el-button>
        <el-button @click="handleCancel">取消</el-button>
      </template>
    </el-dialog>

    <!-- 编辑失效申请弹窗 -->
    <el-dialog v-model="editDialogVisible" :title="editDialogTitle" width="70%" append-to-body>
      <super-form v-if="editDialogVisible" :loading="editDialogLoading" ref="superFormRef"
        code="docInvalidation-apply-edit-from" @selectData="selectData" v-model="form" :form-rules="rules"
        :methods="formMethods">
      </super-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button v-hasPermi="['dms:docInvalidOrder:submit']" :loading="buttonLoading" type="primary"
            @click="submitForm">提 交</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 提交审批弹窗 -->
    <el-dialog v-model="apprDialog.visible" :title="apprDialog.title" width="500px" append-to-body>
      <el-form>
        <el-form-item label="提交意见" prop="approveComments">
          <el-input v-model="apprRow.approveComments" v-hasPermi="['dms:docInvalidOrder:submit']" type="textarea"
            placeholder="请输入提交意见"></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button v-hasPermi="['dms:docInvalidOrder:submit']" :loading="buttonLoading" type="primary"
            @click="submitApprove">确 定</el-button>
          <el-button @click="apprCancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="DocInvalidationApply" lang="ts">
import { ref, reactive, toRefs, onMounted, getCurrentInstance, type ComponentInternalInstance } from 'vue';
import {
  getDocInvalidationApplyEditList,
  getDocInvalidationApplyDetail,
  addDocInvalidationApply,
  deleteDocInvalidationApply,
  updateDocInvalidationApply,
  submitAndFlowStartDocInvalidationApply
} from '@/api/dms/documentInvalidation/index'
const ActiveFileLibSelect = defineAsyncComponent(() => import('@/views/dms/fileLib/activeFileLib/select.vue'));

// 全局代理
const { proxy } = getCurrentInstance() as ComponentInternalInstance;

// 基础状态管理
const searchCode = ref('recordInvalidation-apply-add-search-from');
const buttonLoading = ref(false);
const loading = ref(false);
const showSearch = ref(true);
const showTable = ref(true);
const total = ref(0);
const activeRecordLibrarySelectVisible = ref(false);
const activeFileLibSelectVisible = ref(false);
const activeFileLibSelectRef = ref();
const ids = ref<Array<string | number>>([]);
const names = ref<Array<string | number>>([]);
const single = ref(true);
const multiple = ref(true);
// 添加super-table的ref
const superTableRef = ref();
// 选中记录数据（用于校验）
const selectedRecordData = ref<{ id: number | string } | null>(null);

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
/** 处理记录选择组件选中数据 */
const handleSelectionChangeNew = (selection: any[]) => {
  console.log('@@@@@@@@@@@@@@@@@@@@@选择记录数据：', selection);
  // return
  if (selection?.length > 0) {
    selectedRecordData.value = { id: selection[0].id };
    form.documentRecordLibraryId = selection[0].id;
  } else {
    selectedRecordData.value = null;
    form.documentRecordLibraryId = '';
  }
};
// 编辑弹窗配置
const editDialogVisible = ref(false);
const editDialogTitle = ref('');
const editDialogLoading = ref(false);

// 表单相关配置
const rules = ref({});
const formMethods = ref({});

// 表单数据
const form = reactive({
  id: '', // 主键ID（区分新增/编辑）
  orderReason: '', // 失效原因
  documentRecordLibraryId: '', // 选中的记录库ID
  documentCode: '' // 文件编号
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

/** 查询列表数据 */
const getList = async () => {
  loading.value = true;
  try {
    const res = await getDocInvalidationApplyEditList(queryParams.value);
    console.log(queryParams.value, res);

    templateEditList.value = res.rows;
    total.value = res.total;
  } catch (error) {
    proxy?.$modal.msgError('查询列表失败，请重试');
    console.error('查询列表异常：', error);
  } finally {
    loading.value = false;
  }
};

/** 表单校验（新增弹窗） */
const validateForm = (): boolean => {
  let isValid = true;

  // 校验失效原因
  if (!form.orderReason?.trim()) {
    validateResult.orderReason = '请输入失效原因';
    isValid = false;
  } else {
    validateResult.orderReason = '';
  }

  // 校验选中记录
  console.log('!!!!!!校验选中记录：258行', selectedRecordData.value);
  if (!selectedRecordData.value) {
    validateResult.selectedRecord = '请选择有效的记录数据';
    isValid = false;
  } else {
    validateResult.selectedRecord = '';
  }

  return isValid;
};

/** 新增弹窗-保存操作 */
const handleSubmit = async () => {
  const isFormValid = validateForm();
  if (!isFormValid) return;

  buttonLoading.value = true;
  try {
    const submitData = {
      documentIds: selectedRecordData.value,
      orderReason: form.orderReason.trim()
    };
    // console.log('!!!!!!提交数据：281行', submitData, selectedRecordData.value);

    // return
    // 新增/编辑判断
    if (submitData.id) {
      await updateDocInvalidationApply(submitData);
      proxy?.$modal.msgSuccess('更新成功');
    } else {
      await addDocInvalidationApply(submitData);
      proxy?.$modal.msgSuccess('新增成功');
    }

    dialog.visible = false;
    await getList();
  } catch (error) {
    proxy?.$modal.msgError('操作失败，请重试');
    console.error('提交异常：', error);
  } finally {
    buttonLoading.value = false;
  }
};

/** 处理记录选择组件选中数据 */
const handleSelectionChangeDetail = (selection: any[]) => {
  console.log('@@@@@@@@@@@@@@@@@@@@@选择记录数据：', selection);
  if (selection?.length > 0) {
    selectedRecordData.value = selection.map((item) => item.id);
    form.documentRecordLibraryId = selection[0].id;
  } else {
    selectedRecordData.value = null;
    form.documentRecordLibraryId = '';
  }
};

/** 选择数据确认回调 */
const handleSelectDataAchieve = (data: any, type: string) => {
  handleSelectDataCancel();
};

/** 关闭选择组件 */
const handleSelectDataCancel = () => {
  selectOption.value = {
    open: false,
    type: undefined,
    width: '',
    title: undefined,
    multiple: false,
    otherCondition: {}
  };
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
  // 重置表单状态
  form.id = '';
  form.orderReason = '';
  form.documentRecordLibraryId = '';
  selectedRecordData.value = null;
  validateResult.orderReason = '';
  validateResult.selectedRecord = '';

  dialog.visible = true;
  dialog.title = '新增失效申请';

  // 延迟调用，确保组件已经渲染完成
  setTimeout(() => {
    // 重新加载ActiveFileLibSelect组件的数据
    if (activeFileLibSelectRef.value && activeFileLibSelectRef.value.getList) {
      activeFileLibSelectRef.value.getList();
    }
  }, 100);
};

/** 编辑按钮操作 */
const handleUpdate = async (row?: any) => {
  editDialogLoading.value = true;
  editDialogTitle.value = '编辑失效申请';

  try {
    const _id = row?.id || ids.value[0];
    const res = await getDocInvalidationApplyDetail(_id);
    Object.assign(form, res.data || res);
    editDialogVisible.value = true;
  } catch (error) {
    proxy?.$modal.msgError('获取详情失败，请重试');
    console.error('获取详情异常：', error);
  } finally {
    editDialogLoading.value = false;
  }
};

/** 新增弹窗-取消操作 */
const handleCancel = () => {
  // 重置表单状态
  form.id = '';
  form.orderReason = '';
  form.documentRecordLibraryId = '';
  selectedRecordData.value = null;
  validateResult.orderReason = '';
  validateResult.selectedRecord = '';

  dialog.visible = false;
};

/** 新增失效申请弹窗关闭事件处理 */
const handleDialogClose = () => {
  // 重置表单状态
  form.id = '';
  form.orderReason = '';
  form.documentRecordLibraryId = '';
  selectedRecordData.value = null;
  validateResult.orderReason = '';
  validateResult.selectedRecord = '';

  // 清空ActiveFileLibSelect组件内super-table的多选选中状态
  if (activeFileLibSelectRef.value && activeFileLibSelectRef.value.$refs.fileTableRef) {
    activeFileLibSelectRef.value.$refs.fileTableRef.clearSelection();
  }
};

/** 编辑弹窗-取消操作 */
const cancel = () => {
  editDialogVisible.value = false;
  // 重置编辑表单
  form.id = '';
  form.orderReason = '';
  form.documentRecordLibraryId = '';
  form.documentCode = '';
};

/** 编辑弹窗-提交操作 */
const submitForm = async () => {
  buttonLoading.value = true;
  try {
    await updateDocInvalidationApply(form);
    proxy?.$modal.msgSuccess('编辑成功');
    editDialogVisible.value = false;
    await getList();
  } catch (error) {
    proxy?.$modal.msgError('提交失败，请重试');
    console.error('编辑提交异常：', error);
  } finally {
    buttonLoading.value = false;
  }
};

/** super-form选择数据回调 */
const selectData = (data: any) => {
  console.log('super-form选择数据：', data);
};

/** 删除操作 */
const handleDelete = async (row?: any) => {
  const _ids = row?.id || ids.value;
  const deleteCount = Array.isArray(_ids) ? _ids.length : 1;

  try {
    if (row) {
      await proxy?.$modal.confirm(`是否确认删除申请单编号为"${row.orderNumber}"的数据项？`);
    } else {
      await proxy?.$modal.confirm(`是否确认删除选中的${deleteCount}个数据项？`);
    }
    await deleteDocInvalidationApply(_ids);
    proxy?.$modal.msgSuccess('删除成功');
    if (superTableRef.value) {
      // 假设super-table有clearSelection方法，具体方法名根据实际组件而定
      superTableRef.value.clearSelection && superTableRef.value.clearSelection();
    }
    await getList();
  } catch (error) {
    if (error !== 'cancel') {
      proxy?.$modal.msgError('删除失败，请重试');
      console.error('删除异常：', error);
    }
  }
};

/** 批量删除操作 */
const handleBatch = () => {
  if (ids.value.length === 0) {
    proxy?.$modal.msgWarning('请先选择要删除的数据项');
    return;
  }
  handleDelete();
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
const submitApprove = () => {
  buttonLoading.value = true;
  submitAndFlowStartDocInvalidationApply(apprRow.value)
    .then(() => {
      proxy?.$modal.msgSuccess('提交成功!!');
      apprCancel();
      getList();
    })
    .catch((res) => {
    })
    .finally(() => {
      buttonLoading.value = false;
    });
};

/** 审批弹窗-取消操作 */
const apprCancel = () => {
  apprDialog.visible = false;
  apprRow.value = null;
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
