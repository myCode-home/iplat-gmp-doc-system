<template>
  <div class="p-2">
    <transition :enter-active-class="proxy?.animate.searchAnimate.enter"
      :leave-active-class="proxy?.animate.searchAnimate.leave">
      <div v-show="showSearch" class="mb-[10px]">
        <el-card shadow="hover" class="header-form">
          <div style="display: flex; justify-content: space-between; align-items: center; width: 100%;">
            <div class="left-item">
              <el-input style="width: 400px" v-model="detailId" placeholder="请英文键盘下全选此处扫描,扫描之后请按回车键"
                @keyup.enter="handleDetail($event)"></el-input>
            </div>
            <div class="right-item">
              <el-button type="primary" icon="plus" @click="handleManualSelect">手动选择</el-button>
              <el-button :loading="loadingSubmit" type="success" icon="DocumentChecked"
                @click="handleConfirm">确认回收</el-button>
              <el-button :loading="loadingSubmit" type="warning" @click="handleOtherConfirm">其他回收</el-button>
            </div>
          </div>
        </el-card>
      </div>
    </transition>
    <!-- 打印申请扫描回收列表 -->
    <el-card shadow="hover">
      <super-table v-if="showTable" :isShowPagination="false" code="printApply-ScanDistribute-index-list" rowKey="id"
        highlightCurrentRow :dataLoading="mainLoading" :data="recycleTableData" v-model:queryParams="queryParams"
        :total="recycleTotal">
        <!-- 操作列 -->
        <template #operation="scope">
          <!-- 操作按钮详情，查看履历，撤回行数据 -->
          <el-tooltip content="删除" placement="top" :enterable="false">
            <el-button link type="danger" v-hasPermi="['dms:printApply:remove']" icon="Delete"
              @click="handleDelete(scope.row)"></el-button>
          </el-tooltip>
        </template>
      </super-table>
    </el-card>
    <!-- 手动选择数据弹窗 -->
    <el-dialog v-model="dialog.visible" :title="dialog.title" width="70%" append-to-body @open="handleDialogOpen"
      @close="handleDialogClose">
      <super-search :showResetButton="true" resetButtonText="刷新" code="printApply-ScanRecycling-search-form"
        v-model="dialogQueryParams" @resetQuery="resetDialogQuery" @getList="getDialogList">
      </super-search>
      <super-table v-if="showTable && dialog.visible" code="printApply-ScanRecycling-all-list" rowKey="id"
        highlightCurrentRow :dataLoading="dialogLoading" :data="dialogTableData" v-model:queryParams="dialogQueryParams"
        :total="dialogTotal" ref="dialogTableRef" @selection-change="handleSelectionChange" @getList="getDialogList">
      </super-table>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="handleSubmit">确 认</el-button>
          <el-button @click="cancel">关 闭</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 异常回收原因填写弹窗 -->
    <el-dialog v-model="abnormalRecycleDialog.visible" title="异常回收原因" width="400px" append-to-body
      :close-on-click-modal="false" :show-close="false">
      <el-form ref="abnormalFormRef" :model="abnormalForm" :rules="abnormalFormRules" label-width="100px">
        <el-form-item label="异常原因" prop="abnormalReason">
          <el-input v-model="abnormalForm.abnormalReason" type="textarea" rows="4"
            placeholder="请填写异常回收原因（必填）"></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="abnormalRecycleDialog.visible = false">取 消</el-button>
          <el-button type="primary" :loading="loadingSubmit" @click="submitAbnormalRecycle">
            确 认
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="printApplyScanRecycling" lang="ts">
import { ref, reactive, toRefs, onMounted, getCurrentInstance } from 'vue';
import { getPrintDocInfoDetail, printReturn, getPrintApplyScanRecyclingDetail, printReturnAbnormal } from '@/api/dms/printDistribution/printApply/index';
import { useUserStore } from '@/store/modules/user';

// 使用 getCurrentInstance 的正确方式
const instance = getCurrentInstance();
const proxy = instance?.proxy as any;

const mainLoading = ref(false);
const dialogLoading = ref(false);
const showSearch = ref(true);
// 打印申请扫描回收列表选中的数据
const selectedIds = ref<Array<string | number>>([]);
const single = ref(true);
const multiple = ref(true);
const showTable = ref(true);
// 打印申请扫描回收列表总条数
const recycleTotal = ref(0);
const dialogTotal = ref(0);
// 表单ref
const superFormRef = ref(null);
const abnormalFormRef = ref(null); // 异常回收表单ref
// 对话框表格引用
const dialogTableRef = ref(null);

// 存储已回收的ID，防止重复添加
const recycledIds = ref(new Set());

// 搜索框的打印申请编号
const searchValue = ref('');

interface DialogOption {
  title: string;
  visible: boolean;
  loading: boolean;
}

interface PageData<T, U> {
  form: T;
  queryParams: U;
  rules: Record<string, any>;
}

const dialog = reactive<DialogOption>({
  title: '手动选择回收数据',
  visible: false,
  loading: false
});

// 新增：异常回收弹窗配置
const abnormalRecycleDialog = reactive({
  visible: false,
  loading: false
});

// 新增：异常回收表单数据
const abnormalForm = reactive({
  abnormalReason: '' // 异常回收原因
});

// 新增：异常回收表单校验规则
const abnormalFormRules = reactive({
  abnormalReason: [
    { required: true, message: '请填写异常回收原因', trigger: 'blur' },
    { min: 1, max: 500, message: '原因长度需在1-500字符之间', trigger: 'blur' }
  ]
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

// 对话框查询参数（独立于主表格）
const dialogQueryParamsData = reactive({
  dialogQueryParams: reactive({
    pageNum: 1,
    pageSize: 10
  })
});

// 打印申请回收列表
const { queryParams, form, rules } = toRefs(data);
const { dialogQueryParams } = toRefs(dialogQueryParamsData);
// 主表格数据（页面上显示的回收列表）
const recycleTableData = ref<any[]>([]);
// 弹窗表格数据
const dialogTableData = ref<any[]>([]);
// 弹窗中选中的数据
const selectedDialogData = ref<any[]>([]);

// 打印申请审批列表选择数据
const selectData = (data: any, config) => {
  if (config.prop == 'name') {
    form.value.userId = data.id;
    form.value.name = data.name;
    form.value.sex = data.sex;
    form.value.phone = data.phonenumber;
    form.value.email = data.email;
    form.value.deptId = data.deptId;
    form.value.deptName = data.deptName;
  }
};

// 在父组件中预定义所有可能用到的方法
const formMethods = {
  // args参数集合 childFrom参数集合
  customChangeHandler: (args: Array<any>, childFrom: Object) => {
    superFormRef.value?.updateData({ companyName: 'admin' })
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

// 主表格删除按钮操作
const handleDelete = async (row?: any) => {
  if (!row) return;

  try {
    await proxy?.$modal.confirm(`确认删除编号为"${row.applyCode}"的数据项？`);
    // 从主表格中删除数据
    const index = recycleTableData.value.findIndex((item: any) => item.id === row.id);
    if (index > -1) {
      recycleTableData.value.splice(index, 1);
      recycleTotal.value = recycleTableData.value.length;

      // 从已回收ID集合中移除（如果存在）
      if (recycledIds.value.has(row.id)) {
        recycledIds.value.delete(row.id);
      }
    }
    proxy?.$modal.msgSuccess('删除成功');
  } catch (error) {
    // 用户取消删除操作
  }
};

// 弹窗中的确认选择按钮操作
const handleSubmit = () => {
  if (selectedDialogData.value.length === 0) {
    proxy?.$modal.msgWarning('请先选择数据');
    return;
  }

  // 过滤掉已回收的数据
  const newData = selectedDialogData.value.filter((item: any) => {
    // 检查是否已在主表格中
    const existsInMain = recycleTableData.value.some((mainItem: any) => mainItem.id === item.id);
    // 检查是否已回收过
    const isRecycled = recycledIds.value.has(item.id);

    if (existsInMain) {
      proxy?.$modal.msgWarning(`编号"${item.applyCode || item.id}"的数据已在列表中`);
    } else if (isRecycled) {
      proxy?.$modal.msgWarning(`编号"${item.applyCode || item.id}"的数据已回收过`);
    }

    return !existsInMain && !isRecycled;
  });

  if (newData.length === 0) {
    proxy?.$modal.msgWarning('没有可添加的新数据');
    return;
  }

  recycleTableData.value = [...recycleTableData.value, ...newData];
  recycleTotal.value = recycleTableData.value.length;

  proxy?.$modal.msgSuccess(`成功添加 ${newData.length} 条数据`);
  dialog.visible = false;
  // 清空选中数据
  selectedDialogData.value = [];
  updateSelectionInfo();
};

/** 取消按钮 */
const cancel = () => {
  reset();
  dialog.visible = false;
  // 清空选中数据
  selectedDialogData.value = [];
  updateSelectionInfo();
};

// 表单重置
const reset = () => {
  form.value = { ...initFormData };
};

/** 搜索按钮操作 */
const handleQuery = () => {
  queryParams.value.pageNum = 1;
};

/** 重置按钮操作 */
const resetQuery = () => {
  handleQuery();
};

/** 对话框搜索重置 */
const resetDialogQuery = () => {
  dialogQueryParams.value.pageNum = 1;
  getDialogList();
};

// 对话框打开时处理
const handleDialogOpen = () => {
  // 清空选中数据
  selectedDialogData.value = [];
  // 清空表格选中状态
  if (dialogTableRef.value && dialogTableRef.value.$refs.elTableRef) {
    dialogTableRef.value.$refs.elTableRef.clearSelection();
  }
  // 重置对话框查询参数
  dialogQueryParams.value.pageNum = 1;
  dialogQueryParams.value.pageSize = 10;
  // 加载对话框数据
  getDialogList();
};

// 对话框关闭时处理
const handleDialogClose = () => {
  // 清空选中数据
  selectedDialogData.value = [];
  // 重置对话框数据
  dialogTableData.value = [];
  dialogTotal.value = 0;
  updateSelectionInfo();
};

// 确认回收按钮
// ids是recycleTableData中的每一个数据id组成的数组
const ids = ref<Array<string | number>>([]);
// 交回人账号(必填)
const userId = ref();
// 交回确认人账号
const confirmUserId = ref('');
// 异常原因
const abnormalReason = ref('');
const loadingSubmit = ref(false);
const handleConfirm = async () => {
  // 如果没有选中数据，提示用户选择数据
  if (recycleTableData.value.length === 0) {
    proxy?.$modal.msgWarning('请先选择需要回收的数据');
    return;
  }
  console.log('确认回收的按钮点击', recycleTableData.value);
  const ids = recycleTableData.value.map((item: any) => item.id);
  // userId.value = 1;
  // 请求的数据
  const data = {
    ids: ids,
    userName: userId.value
    // confirmUserId: confirmUserId.value,
    // abnormalReason: abnormalReason.value
  };
  try {
    loadingSubmit.value = true;
    // 拿到电子签名的值
    const res = await proxy?.$Signature(printReturn) as any;
    data.userName = res?.users[1].userName;
    // console.log('打印申请回收交回接口参数', res, data);
    await printReturn(data, res?.es);

    // 将回收的ID添加到已回收集合中
    recycleTableData.value.forEach((item: any) => {
      recycledIds.value.add(item.id);
    });

    proxy?.$modal.msgSuccess('交回成功');
    // 清空回收列表
    recycleTableData.value = [];
    recycleTotal.value = 0;
    // 清空选中数据
    selectedDialogData.value = [];
    updateSelectionInfo();

    // 如果对话框是打开的，重新加载对话框数据以更新状态
    if (dialog.visible) {
      getDialogList();
    }
  } catch (error) {
    proxy?.$modal.msgError('交回失败，请重试');
  } finally {
    loadingSubmit.value = false;
    ids.value = [];
  }
};

// 手动选择按钮
const handleManualSelect = () => {
  // 打开弹窗
  dialog.visible = true;
};

/** 多选框选中数据 */
const handleSelectionChange = (selection: any[]) => {
  selectedDialogData.value = selection;
  updateSelectionInfo();
};

// 更新选择信息
const updateSelectionInfo = () => {
  selectedIds.value = selectedDialogData.value.map((item: any) => item.id);
  single.value = selectedDialogData.value.length !== 1;
  multiple.value = selectedDialogData.value.length === 0;
};

// 打印申请详情列表（弹窗表格）- 独立的数据获取
const getDialogList = async () => {
  dialogLoading.value = true;
  try {
    const res = await getPrintApplyScanRecyclingDetail(dialogQueryParams.value);

    // 过滤掉已回收的数据
    const filteredData = res.rows.filter((item: any) => !recycledIds.value.has(item.id));

    dialogTableData.value = filteredData;
    dialogTotal.value = res.total;
  } catch (error) {
    proxy?.$modal.msgError('数据加载失败，请重试');
  } finally {
    dialogLoading.value = false;
  }
};

// 其他回收按钮 - 点击打开异常原因弹窗
const handleOtherConfirm = async () => {
  if (recycleTableData.value.length === 0) {
    proxy?.$modal.msgWarning('请先选择需要回收的数据');
    return;
  }
  // 重置异常回收表单
  abnormalForm.abnormalReason = '';
  // 打开异常原因填写弹窗
  abnormalRecycleDialog.visible = true;
};

// 新增：提交异常回收请求
const submitAbnormalRecycle = async () => {
  // 表单校验
  const valid = await abnormalFormRef.value?.validate();
  if (!valid) return;

  // 构造接口参数
  const ids = recycleTableData.value.map((item: any) => item.id);
  const userStore = useUserStore();
  const reqData = {
    ids: ids, // 打印明细主键集合
    userId: userStore.userId, // 交回人账号（当前登录用户）
    // confirmUserId: '', // 确认交回人账号（非必填）
    abnormalReason: abnormalForm.abnormalReason.trim() // 异常回收原因
  };

  try {
    loadingSubmit.value = true;
    await printReturnAbnormal(reqData);

    // 将回收的ID添加到已回收集合中
    recycleTableData.value.forEach((item: any) => {
      recycledIds.value.add(item.id);
    });

    proxy?.$modal.msgSuccess('异常回收成功');
    // 关闭弹窗
    abnormalRecycleDialog.visible = false;
    // 清空回收列表
    recycleTableData.value = [];
    recycleTotal.value = 0;
    // 清空选中数据
    selectedDialogData.value = [];
    updateSelectionInfo();

    // 如果对话框是打开的，重新加载对话框数据以更新状态
    if (dialog.visible) {
      getDialogList();
    }
  } catch (error) {
    proxy?.$modal.msgError('异常回收失败，请重试');
  } finally {
    loadingSubmit.value = false;
  }
};

const detailId = ref('');
const handleDetail = async (e: Event) => {
  // 阻止表单默认提交行为，解决页面刷新问题
  e.preventDefault();

  // 去除首尾空格，判断输入是否为空
  const inputVal = detailId.value.trim();
  if (!inputVal) {
    proxy?.$modal.msgWarning('请输入扫描内容后再按回车');
    return;
  }
  try {
    const res = await getPrintDocInfoDetail(inputVal);
    detailId.value = '';
    // 打印接口返回结果
    if (res.data === null) {
      proxy?.$modal.msgWarning('扫描内容不存在');
      return;
    }

    // 检查是否已存在或已回收
    const existsInMain = recycleTableData.value.some((item: any) => item.id === res.data.id);
    const isRecycled = recycledIds.value.has(res.data.id);

    if (existsInMain) {
      proxy?.$modal.msgWarning('该内容已在列表中');
      return;
    }

    if (isRecycled) {
      proxy?.$modal.msgWarning('该内容已回收过，不能重复添加');
      return;
    }

    // 说明扫描到的内容存在，并且类型是表格数据的一行，所以直复现到表格中
    recycleTableData.value = [res.data];
    recycleTotal.value = 1;
    proxy?.$modal.msgSuccess('扫描成功');
  } catch (error) {
    proxy?.$modal.msgError('查询扫描详情失败，请重试');
  }
};

onMounted(() => {
  // 主表格默认为空，不加载数据
  recycleTableData.value = [];
  recycleTotal.value = 0;
  // 初始化已回收ID集合
  recycledIds.value = new Set();
});
</script>

<style scoped lang="scss">
.header-form {
  display: flex;
  justify-content: space-between;
}

.right-item {
  position: absolute;
  right: 50px;
}
</style>
