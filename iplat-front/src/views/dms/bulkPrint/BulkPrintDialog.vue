<template>
  <el-dialog :close-on-press-escape="false" v-model="dialog.visible" :title="dialog.title" width="70%" append-to-body>
    <super-form :disabled="isDisabledSearch" v-if="dialog.visible" :loading="dialog.loading" ref="superFormRef"
      code="bulkPrint-apply-detail-search-form" @selectData="handleSelectData" v-model="form" :form-rules="rules"
      :methods="formMethods">
    </super-form>

    <!-- 详情表格操作按钮 -->
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button v-if="!isDisabledSearch" type="primary" icon="Plus"
          @click="handSelect">添加文件/记录</el-button>
      </el-col>
    </el-row>

    <!-- 详情表格：选择的数据中转站 -->
    <super-table v-if="showTable" code="bulkPrint-apply-detail-list" rowKey="id" highlightCurrentRow
      :dataLoading="detailLoading" :data="detailList" :total="detailList.length"
      @selection-change="handleDetailSelectionChange">
      <!-- 操作列 -->
      <template #operation="scope">
        <div>
          <el-tooltip :content="!isDisabledSearch ? '编辑' : '查看'" placement="top" :enterable="false">
            <el-button link type="primary" :icon="!isDisabledSearch ? 'EditPen' : 'View'" @click="handelEdit(scope.row)"
              ></el-button>
          </el-tooltip>
          <el-tooltip v-if="!isDisabledSearch" content="删除" placement="top" :enterable="false">
            <el-button link type="danger" icon="Delete" @click="handleRemoveDetail(scope.row)"></el-button>
          </el-tooltip>
        </div>
      </template>
    </super-table>
    <template #footer>
      <div class="dialog-footer">
        <el-button v-if="!isDisabledSearch" :loading="buttonLoading" type="primary" @click="submitForm">提 交</el-button>
        <el-button @click="cancel">{{ isDisabledSearch ? '关 闭' : '取 消' }}</el-button>
      </div>
    </template>
  </el-dialog>

  <!-- 选择文件/记录组件 -->
  <SelectGroup isShoueCodePlus="true" v-if="selectOption.open" :isShowTemplate="false" :title="selectOption.title"
    :selectType="selectOption.type" :multiple="selectOption.multiple" :width="selectOption.width"
    :otherCondition="selectOption.otherCondition" :achieveData="handleSelectDataAchieve"
    @dialogClose="handleSelectDataCancel">
  </SelectGroup>

  <!-- 编辑详情项对话框 -->
  <el-dialog v-model="editDialog.visible" :title="editDialog.title" width="60%" append-to-body>
    <super-form :disabled="isDisabledSearch" v-if="editDialog.visible" :loading="editDialog.loading" ref="editFormRef"
      code="bulkPrint-apply-add-edit-form" @selectData="handleEditFormSelectData" v-model="editForm">
    </super-form>
    <template #footer>
      <div class="dialog-footer">
        <el-button v-if="!isDisabledSearch" :loading="buttonLoading" type="primary" @click="saveEditForm">保 存</el-button>
        <el-button @click="cancelEdit">{{ isDisabledSearch ? '关 闭' : '取 消' }}</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup name="BulkPrintDialog" lang="ts">
import { createPrintApplyBatch, getPrintApplyBatchDetail, updatePrintApplyBatch } from '@/api/dms/bulkPrint/index'
import SelectGroup from '@/views/dms/operateOrder/addOperateOrder/selectGroup.vue';

const { proxy } = getCurrentInstance() as ComponentInternalInstance;

// Props
const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  title: {
    type: String,
    default: ''
  },
  row: {
    type: Object,
    default: () => ({})
  },
  isEdit: {
    type: Boolean,
    default: false
  },
  // 是否显示搜索框，；来判断是否是查看详情还是编辑
  isDisabledSearch: {
    type: Boolean,
    default: false
  }
});

// Emits
const emit = defineEmits(['update:visible', 'success']);

// 按钮加载状态
const buttonLoading = ref(false);
// 详情表格加载状态
const detailLoading = ref(false);
// 是否显示表格
const showTable = ref(true);
// 表单引用
const superFormRef = ref(null);
// 编辑表单引用
const editFormRef = ref(null);
// 当前编辑项索引
const currentEditIndex = ref(-1);

// 新增/编辑对话框配置
const dialog = reactive({
  title: props.title,
  visible: props.visible,
  loading: false
});

// 编辑详情项对话框配置
const editDialog = reactive({
  title: !props.isDisabledSearch ? '编辑打印项' : '查看详情',
  visible: false,
  loading: false
});

// 编辑表单数据
const editForm = reactive({});

// 初始化表单数据
const initFormData = {
  applyReason: '' // 申请原因
};

// 响应式数据
const data = reactive({
  // 表单数据
  form: { ...initFormData },
  // 表单验证规则
  rules: {
    applyReason: [
      { required: true, message: '申请原因不能为空', trigger: 'blur' }
    ]
  }
});

const { form, rules } = toRefs(data);

// 详情表格数据（临时存储选择的数据）
const detailList = ref([]);
// 详情表格选中项ID
const detailSelectedIds = ref<Array<string | number>>([]);

// 选择组件配置
const selectOption = ref({
  open: false,
  type: '',
  width: '',
  title: '',
  multiple: false,
  otherCondition: {}
});

// Watch for props changes
watch(
  () => props.visible,
  (newVal) => {
    dialog.visible = newVal;
    // 当对话框打开且处于编辑模式时，重新加载数据
    if (newVal && props.isEdit && props.row && props.row.id) {
      loadDetailData(props.row.id);
    }
  }
);

watch(
  () => props.title,
  (newVal) => {
    dialog.title = newVal;
  }
);

watch(
  () => props.row,
  (newVal) => {
    if (newVal && props.isEdit) {
      loadDetailData(newVal.id);
    }
  },
  { deep: true }
);

// 加载详情数据
const loadDetailData = async (id) => {
  dialog.loading = true;
  try {
    const res = await getPrintApplyBatchDetail(id);
    Object.assign(form.value, res.data);

    // 如果有详情数据，也需要加载到detailList中
    if (res.data.details && Array.isArray(res.data.details)) {
      detailList.value = res.data.details;
    }
  } catch (error) {
    proxy?.$modal.msgError(error.msg || '获取详情失败');
  } finally {
    dialog.loading = false;
  }
};

/** 表单数据选择回调 */
const handleSelectData = (data: any, config) => {
  form.value.docOrRecordType = data.dlType;
  form.value.docCode = data.dlCode;
  form.value.docName = data.dlName;
  form.value.docVersion = data.dlVersion;
};

/** 选择组件确认回调 - 处理多选数据 */
const handleSelectDataAchieve = (data, type) => {
  // 确保data是数组格式，即使单选也转为数组处理
  const selectedItems = Array.isArray(data) ? data : [data];
  if (selectedItems.length === 0) {
    proxy?.$modal.msgWarning('未选择任何数据');
    handleSelectDataCancel();
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
    .map((item, index) => {
      // 生成唯一id，使用时间戳和索引的组合
      const uniqueId = Date.now() + index;
      return {
        // 唯一标识
        id: uniqueId,
        // 以下是表格可能需要的字段映射
        // 申请编号 - 通常对应文件的编码
        docCode: item.dlCode,
        // 文件名称
        docName: item.dlName,
        // 文件版本
        docVersion: item.dlVersion || item.dlrVersion,
        // 所属部门 - 从选择组件数据中获取或设置默认
        docDept: item.department || item.deptName,
        // 文件类型
        docType: type === 'document' ? 'document' : 'record',
        // 文档类型 - 文档或记录
        docOrRecordType: type === 'document' ? 'document' : 'record',
        // 是否打印标签页
        whetherPrintPage: 'false',
        // 打印页码
        pages: item.pages || 1,
        // 状态
        docStatus: item.status,
        // 申请份数(等会需要用户自己去填写)
        applyCopies: item.applyCopies || 1,
        // 打印类型(等会需要用户自己去填写)
        printType: type === 'document' ? 'docPrint' : 'recPrint',
        // 文档或记录ID
        docOrRecordId: item.id
      };
    });

  if (newItems.length === 0) {
    proxy?.$modal.msgWarning('选择的数据已存在');
  } else {
    // 添加到详情表格 - 这里是多选框数据复现到表格的关键逻辑
    detailList.value = [...detailList.value, ...newItems];
    proxy?.$modal.msgSuccess(`成功添加${newItems.length}条数据`);
  }

  handleSelectDataCancel();
};

/** 关闭选择组件 */
const handleSelectDataCancel = () => {
  selectOption.value = {
    open: false,
    title: '',
    width: '',
    multiple: false,
    type: '',
    otherCondition: {}
  };
};

/** 移除详情表格中的行 */
const handleRemoveDetail = (row) => {
  const index = detailList.value.findIndex(item => item.id === row.id);
  if (index !== -1) {
    detailList.value.splice(index, 1);
    proxy?.$modal.msgSuccess('删除成功');

    // 从选中项中移除
    const detailIndex = detailSelectedIds.value.indexOf(row.id);
    if (detailIndex !== -1) {
      detailSelectedIds.value.splice(detailIndex, 1);
    }
  }
};

/** 编辑详情表格中的数据 */
const handelEdit = (row) => {
  // 检查row是否有效
  if (!row || !row.id) {
    proxy?.$modal.msgError('无效的数据行');
    return;
  }

  const index = detailList.value.findIndex(item => item.id === row.id);
  if (index !== -1) {
    currentEditIndex.value = index;
    // 深拷贝数据到编辑表单
    Object.assign(editForm, JSON.parse(JSON.stringify(row)));
    editDialog.visible = true;
  } else {
    // 找不到索引时给出错误提示
    proxy?.$modal.msgError('找不到对应的数据行');
    console.error('编辑数据失败：找不到对应的数据行', row);
  }
};

/** 保存编辑表单数据 */
const saveEditForm = () => {
  if (currentEditIndex.value !== -1) {
    // 将编辑后的数据更新到详情表格
    Object.assign(detailList.value[currentEditIndex.value], JSON.parse(JSON.stringify(editForm)));
    editDialog.visible = false;
    proxy?.$modal.msgSuccess('编辑成功');
    // 清空编辑表单
    Object.keys(editForm).forEach(key => delete editForm[key]);
    currentEditIndex.value = -1;
  }
};

/** 取消编辑 */
const cancelEdit = () => {
  editDialog.visible = false;
  // 清空编辑表单
  Object.keys(editForm).forEach(key => delete editForm[key]);
  currentEditIndex.value = -1;
};

/** 编辑表单选择数据处理 */
const handleEditFormSelectData = (data, config) => {
  if (config.prop === 'docCode') {
    editForm.docCode = data.dlCode;
    editForm.docName = data.dlName;
    editForm.docVersion = data.dlVersion;
  }
};

/** 详情表格多选框选中数据 */
const handleDetailSelectionChange = (selection) => {
  detailSelectedIds.value = selection.map(item => item.id);
};

/** 取消新增/编辑 */
const cancel = () => {
  resetForm();
  dialog.visible = false;
  emit('update:visible', false);
};

/** 重置表单 */
const resetForm = () => {
  form.value = { ...initFormData };
  detailList.value = [];
  detailSelectedIds.value = [];
};

/** 打开选择组件 */
const handSelect = () => {
  selectOption.value = {
    open: true,
    title: '选择文件或记录',
    type: '', // 可以根据需要指定类型
    width: '80%',
    multiple: true, // 关键：设置为true以支持多选
    otherCondition: {}
  };
};

/** 提交表单（新增/编辑） */
const submitForm = () => {
  superFormRef.value?.validate((valid) => {
    if (!valid) {
      return;
    }
    // 检查详情表格是否有数据
    if (!detailList.value.length) {
      proxy?.$modal.msgError('请至少添加一个文件或记录');
      return;
    }
    // 检查申请原因是否为空
    if (!form.value.applyReason?.trim()) {
      proxy?.$modal.msgError('请填写申请原因');
      return;
    }
    buttonLoading.value = true;
    // 组装提交数据
    const submitData = {
      ...form.value,
      details: detailList.value.map(item => ({
        applyCopies: item.applyCopies,
        printType: item.printType,
        docOrRecordType: item.docOrRecordType,
        docOrRecordId: item.docOrRecordId,
        pages: item.pages,
        whetherPrintPage: item.whetherPrintPage
      }))
    };

    // 根据是否有id判断是新增还是编辑
    const apiCall = submitData.id ? updatePrintApplyBatch(submitData) : createPrintApplyBatch(submitData);

    apiCall
      .then(() => {
        proxy?.$modal.msgSuccess('操作成功');
        // 重置表单状态，参考取消按钮的操作逻辑
        resetForm();
        dialog.visible = false;
        emit('update:visible', false);
        emit('success');
      })
      .catch((error) => {
        proxy?.$modal.msgError(error.message || '提交失败');
      })
      .finally(() => {
        buttonLoading.value = false;
      });
  });
};

/** 表单自定义方法 */
const formMethods = {
  customChangeHandler: (args, childFrom) => {
  },
  handleRoleChange: (args, childFrom) => {
  },
  handleSexChange: (args) => {
  },
  processCompanyData: (args, childFrom) => {
  }
};
</script>

<style scoped>
.mb8 {
  margin-bottom: 8px;
}
</style>
