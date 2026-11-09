<template>
  <div>
    <el-dialog v-model="dialog.visible" :title="dialog.title" width="70%" append-to-body @close="resetDialogData">
      <super-form v-if="dialog.visible" :loading="dialog.loading" ref="superFormRef"
        code="batchRevision-apply-add-detail-form" @selectData="selectData" v-model="form" :form-rules="rules"
        :methods="formMethods" :disabled="disabled">
        <template #documentCodeSolt="config">
          <el-input v-model="form[config.prop]" suffix-icon="Document" @click.native="handSelect" v-bind="config"
            readonly></el-input>
        </template>
      </super-form>
      <el-card shadow="hover">
        <template #header>
          <el-row :gutter="10" class="mb8">
            <el-col :span="1.5">
              <el-button v-if="showAddButton" v-hasPermi="['dms:docRecordFailureOrder:add']" type="primary" icon="Plus"
                @click="handSelect">添加文件/记录</el-button>
            </el-col>
          </el-row>
        </template>
        <super-search v-if="showSearchForm" :showResetButton="false" code="batchRevision-apply-file-search-form" v-model="searchQueryParams"
          @getList="handleSearch" @resetQuery="resetSearchQuery" />
        <super-table v-if="showTable" code="batchRevision-apply-add-detail-list" rowKey="id" highlightCurrentRow
          :dataLoading="loading" :data="detailList" :total="detailList.length"
          @selection-change="handleSelectionChange">
          <template #operation="scope">
            <div class="operation-group">
              <el-tooltip content="删除" placement="top" :enterable="false">
                <el-button link type="danger" v-hasPermi="['dms:docRecordFailureOrder:remove']" icon="Delete"
                  @click="handleDeleteFile(scope.row)" :disabled="disabled" />
              </el-tooltip>
            </div>
          </template>
        </super-table>
      </el-card>
      <template #footer>
        <el-button v-hasPermi="['dms:docRecordFailureOrder:submit']" type="primary" @click="handleSubmit"
          :loading="buttonLoading" :disabled="disabled">保存</el-button>
        <el-button @click="handleCancel">取消</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="BaseView" lang="ts">
import { ref, reactive, toRefs, defineProps, defineEmits, getCurrentInstance, watch, type ComponentInternalInstance } from 'vue';
import { getOperateOrderActiveFileList } from '@/api/dms/batchRevision/index';
import { useUserStore } from '@/store/modules/user';

// 全局代理
const { proxy } = getCurrentInstance() as ComponentInternalInstance;

// 定义props
const props = defineProps({
  showAddButton: {
    type: Boolean,
    default: true
  },
  showSearchForm: {
    type: Boolean,
    default: true
  },
  disabled: {
    type: Boolean,
    default: false
  }
});

// 定义emits
const emit = defineEmits(['submit', 'cancel', 'close']);

// 基础状态管理
const buttonLoading = ref(false);
const loading = ref(false);
const showTable = ref(true);
const total = ref(0);
const ids = ref<Array<string | number>>([]);
const names = ref<Array<string | number>>([]);
const single = ref(true);
const multiple = ref(true);

// 选中记录数据（用于校验）
const selectedRecordData = ref<{ id: number | string } | null>(null);

// 数据中转表格数据
const detailList = ref<any[]>([]);

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

// 文件选择弹窗配置
const fileSelectDialog = reactive({
  title: '选择文件/记录',
  visible: false,
  loading: false
});

// 搜索查询参数 - 使用ref定义
const searchQueryParams = ref({
  pageNum: 1,
  pageSize: 10
});

// 表单相关配置
const rules = ref({});
const formMethods = ref({});

// 表单数据
const form = reactive({
  docLibIds: [], // 主键ID（区分新增/编辑）
  orderReason: '', // 失效原因
  remark: '' // 备注
});

// 表单校验结果
const validateResult = reactive({
  orderReason: '', // 失效原因校验提示
  selectedRecord: '' // 记录选择校验提示
});

// 监听选中文件变化，控制确认按钮状态
watch(
  () => selectedFiles.value,
  (newVal) => {
    const selectedItems = Array.isArray(newVal) ? newVal : [newVal];
    isConfirmButtonDisabled.value = selectedItems.length === 0;
  },
  { deep: true }
);

/** 表单校验 */
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
  if (!selectedRecordData.value) {
    validateResult.selectedRecord = '请选择有效的记录数据';
    isValid = false;
  } else {
    validateResult.selectedRecord = '';
  }

  return isValid;
};

/** 保存操作 */
const handleSubmit = async () => {
  // 先检查表格是否有数据
  if (detailList.value.length === 0) {
    proxy?.$modal.msgWarning('请至少添加一条文件数据');
    return;
  }

  // 设置选中记录数据（如果还未设置）
  if (!selectedRecordData.value && detailList.value.length > 0) {
    selectedRecordData.value = { id: detailList.value[0].id };
    form.docLibIds = [detailList.value[0].id];
  }

  const isFormValid = validateForm();

  buttonLoading.value = true;
  try {
    // 构建请求参数
    const requestData = {
      orderReason: form.orderReason?.trim() || '',
      applyUser: useUserStore().nickname,
      docLibIds: JSON.stringify(detailList.value.map(item => item.id)),
      remark: form.remark?.trim() || ''
    };

    // 数据验证
    if (!requestData.orderReason) return proxy?.$modal.msgWarning('请输入申请原因');
    if (!requestData.docLibIds) return proxy?.$modal.msgWarning('请至少选择一条文件数据');

    // 触发提交事件
    emit('submit', requestData);
  } catch (error) {
    proxy?.$modal.msgError('操作失败，请重试');
    console.error('提交异常：', error);
  } finally {
    buttonLoading.value = false;
  }
};

/** 搜索操作 */
const handleSearch = async () => {
  loading.value = true;
  try {
    const res = await getOperateOrderActiveFileList(searchQueryParams.value);
    detailList.value = res.rows || [];
    total.value = res.total || 0;
  } catch (error) {
    proxy?.$modal.msgError('查询失败，请重试');
    console.error('查询异常：', error);
  } finally {
    loading.value = false;
  }
};

/** 重置搜索条件 */
const resetSearchQuery = () => {
  searchQueryParams.value = {
    pageNum: 1,
    pageSize: 10
  };
  handleSearch();
};

/** 打开选择组件 */
const handSelect = () => {
  fileSelectDialog.visible = true;
  emit('openSelect');
};

/** 表格多选事件 */
const handleSelectionChange = (selection: any[]) => {
  ids.value = selection.map((item) => item.id);
  names.value = selection.map((item) => item.tmplName);
  single.value = selection.length !== 1;
  multiple.value = !selection.length;
};

/** 删除文件表格中的行数据 */
const handleDeleteFile = async (row: any) => {
  if (row) {
    // 删除表格中的单行数据
    const index = detailList.value.findIndex(item => item.id === row.id);
    if (index !== -1) {
      await proxy?.$modal.confirm(`是否确认删除数据项？`);
      detailList.value.splice(index, 1);
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
        form.docLibIds = [detailList.value[0].id];
      }
    }
  }
};

/** 取消操作 */
const handleCancel = () => {
  resetDialogData();
  emit('cancel');
};

/** 重置弹窗数据 */
const resetDialogData = () => {
  // 重置表单状态
  form.orderReason = '';
  form.remark = '';
  form.docLibIds = [];

  selectedRecordData.value = null;
  validateResult.orderReason = '';
  validateResult.selectedRecord = '';

  // 重置表格数据
  detailList.value = [];
  ids.value = [];

  // 重置选择组件状态
  selectedFiles.value = [];
  isConfirmButtonDisabled.value = true;

  // 关闭弹窗
  dialog.visible = false;
  fileSelectDialog.visible = false;

  emit('close');
};

/** super-form选择数据回调 */
const selectData = (data: any) => {
  console.log('super-form选择数据：', data);
  emit('selectData', data);
};

// 导出方法
defineExpose({
  dialog,
  form,
  detailList,
  resetDialogData,
  handleSubmit,
  handleCancel
});
</script>

<style scoped>
.operation-group {
  display: flex;
  align-items: center;
}
</style>
