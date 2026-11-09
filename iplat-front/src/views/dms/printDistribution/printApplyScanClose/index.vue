<!-- 扫码发放页面 -->
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
              <el-button :loading="loadingSubmit" type="success" icon="Check" @click="handleConfirm">确认关闭</el-button>
            </div>
          </div>
        </el-card>
      </div>
    </transition>

    <!-- 打印申请扫描关闭列表 -->
    <el-card shadow="hover">
      <super-table v-if="showTable" :isShowPagination="false" code="printApply-ScanClose-index-list" rowKey="id"
        highlightCurrentRow :dataLoading="mainLoading" :data="mainTableData" v-model:queryParams="queryParams"
        :total="mainTotal">
        <!-- 操作列 -->
        <template #operation="scope">
          <div>
            <el-tooltip content="删除" placement="top" :enterable="false">
              <el-button link type="danger" icon="Delete"
                @click="handleDelete(scope.row)"></el-button>
            </el-tooltip>
          </div>
        </template>
      </super-table>
    </el-card>

    <!-- 详情对话框（详情展示） -->
    <el-dialog v-model="dialog.visible" :title="dialog.title" width="70%" append-to-body @open="handleDialogOpen" @close="handleDialogClose">
      <super-search :showResetButton="true" resetButtonText="刷新" code="printApply-ScanDistribute-check"
        v-model="dialogQueryParams" @resetQuery="resetDialogQuery" @getList="getDialogList"></super-search>
      <super-table v-if="showTable && dialog.visible" code="printApply-ScanDistribute-all-list" rowKey="id" highlightCurrentRow
        :dataLoading="dialogLoading" :data="dialogTableData" v-model:queryParams="dialogQueryParams" :total="dialogTotal"
        ref="dialogTableRef" @selection-change="handleSelectionChange" @getList="getDialogList"></super-table>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="handleSubmit">确 认</el-button>
          <el-button @click="cancel">关 闭</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="PrintApplyScanClose" lang="ts">
import { ref, reactive, toRefs, onMounted, getCurrentInstance } from 'vue';
import { printClose, getPrintDocInfoDetail, getPrintApplyScanCloseDetail } from '@/api/dms/printDistribution/printApply/index';
import { useUserStore } from '@/store/modules/user';

// 使用 getCurrentInstance 的正确方式
const instance = getCurrentInstance();
const proxy = instance?.proxy as any;

const mainLoading = ref(false);
const dialogLoading = ref(false);
const showSearch = ref(true);
// 打印申请扫描分配列表选中的数据
const names = ref<Array<string | number>>([]);
const single = ref(true);
const multiple = ref(true);
const showTable = ref(true);
// 打印申请扫描分配列表总条数
const mainTotal = ref(0);
const dialogTotal = ref(0);
// 对话框表格引用
const dialogTableRef = ref(null);

// 存储已关闭的ID，防止重复添加
const closedIds = ref(new Set());

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

const dialogQueryParamsData = reactive({
  dialogQueryParams: reactive({
    pageNum: 1,
    pageSize: 10
  })
});
// 打印申请审批列表
const { queryParams, form, rules } = toRefs(data);
const { dialogQueryParams } = toRefs(dialogQueryParamsData);

// 主表格数据（页面上显示的表格）
const mainTableData = ref<any[]>([]);
// 弹窗表格数据
const dialogTableData = ref<any[]>([]);
// 弹窗中选中的数据
const selectedDialogData = ref<any[]>([]);

// 主表格删除按钮操作
const handleDelete = async (row?: any) => {
  if (!row) return;

  try {
    await proxy?.$modal.confirm(`确认删除编号为"${row.applyCode}"的数据项？`);
    // 从主表格中删除数据
    const index = mainTableData.value.findIndex((item: any) => item.id === row.id);
    if (index > -1) {
      mainTableData.value.splice(index, 1);
      mainTotal.value = mainTableData.value.length;

      // 从已关闭ID集合中移除（如果存在）
      if (closedIds.value.has(row.id)) {
        closedIds.value.delete(row.id);
      }
    }
    proxy?.$modal.msgSuccess('删除成功');
  } catch (error) {
    // 用户取消删除操作
  }
};

// 弹窗中的确认关闭按钮操作
const handleSubmit = () => {
  if (selectedDialogData.value.length === 0) {
    proxy?.$modal.msgWarning('请先选择数据');
    return;
  }

  // 过滤掉已关闭的数据
  const newData = selectedDialogData.value.filter((item: any) => {
    // 检查是否已在主表格中
    const existsInMain = mainTableData.value.some((mainItem: any) => mainItem.id === item.id);
    // 检查是否已关闭过
    const isClosed = closedIds.value.has(item.id);

    if (existsInMain) {
      proxy?.$modal.msgWarning(`编号"${item.applyCode || item.id}"的数据已在列表中`);
    } else if (isClosed) {
      proxy?.$modal.msgWarning(`编号"${item.applyCode || item.id}"的数据已关闭过`);
    }

    return !existsInMain && !isClosed;
  });

  if (newData.length === 0) {
    proxy?.$modal.msgWarning('没有可添加的新数据');
    return;
  }

  mainTableData.value = [...mainTableData.value, ...newData];
  mainTotal.value = mainTableData.value.length;

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

// 输入框的
const detailId = ref('');
const handleDetail = async (e: Event) => {
  // 核心：阻止表单默认提交行为，解决页面刷新问题
  e.preventDefault();

  // 去除首尾空格，判断输入是否为空
  const inputVal = detailId.value.trim();
  if (!inputVal) {
    proxy?.$modal.msgWarning('请输入扫描关闭内容后再按回车');
    return;
  }

  try {
    const res = await getPrintDocInfoDetail(inputVal);
    // 清空输入框内容
    detailId.value = '';

    if (res.data === null) {
      proxy?.$modal.msgWarning('扫描关闭内容不存在');
      return;
    }

    // 检查是否已存在或已关闭
    const existsInMain = mainTableData.value.some((item: any) => item.id === res.data.id);
    const isClosed = closedIds.value.has(res.data.id);

    if (existsInMain) {
      proxy?.$modal.msgWarning('该内容已在列表中');
      return;
    }

    if (isClosed) {
      proxy?.$modal.msgWarning('该内容已关闭过，不能重复添加');
      return;
    }

    // 说明扫描到的内容存在，并且类型是表格数据的一行，所以直复现到表格中
    mainTableData.value = [res.data];
    mainTotal.value = 1;
    proxy?.$modal.msgSuccess('扫描关闭成功');
  } catch (error) {
    proxy?.$modal.msgError('查询扫描关闭详情失败，请重试');
  }
};

// 确认关闭按钮
const ids = ref<Array<string | number>>([]);
const receiveUserId = ref();
const issueUserId = ref('');
const loadingSubmit = ref(false);
const handleConfirm = async () => {
  console.log('确认关闭的按钮点击', mainTableData.value);
  // 如果没有选中数据，提示用户选择数据
  if (mainTableData.value.length === 0) {
    proxy?.$modal.msgWarning('请先选择数据');
    return;
  }
  ids.value = mainTableData.value.map((item: any) => item.id);
  receiveUserId.value = useUserStore().userId;
  const data = {
    ids: ids.value,
    username: '', // 销毁人(必填)
  };
  try {
    loadingSubmit.value = true;
    // 拿到电子签名的值
    const res = await proxy?.$Signature(printClose) as any;
    console.log(res);

    data.username = res?.users[0].userName;
    console.log(data);
    await printClose(data, res?.es);

    // 将关闭的ID添加到已关闭集合中
    mainTableData.value.forEach((item: any) => {
      closedIds.value.add(item.id);
    });

    proxy?.$modal.msgSuccess('关闭成功');
    // 清空表格数据
    mainTableData.value = [];
    mainTotal.value = 0;
    // 清空选中数据
    selectedDialogData.value = [];
    updateSelectionInfo();

    // 如果对话框是打开的，重新加载对话框数据以更新状态
    if (dialog.visible) {
      getDialogList();
    }
  } catch (error) {
    proxy?.$modal.msgError('关闭失败，请重试');
  } finally {
    loadingSubmit.value = false;
    ids.value = [];
  }
};

// 手动选择按钮
const handleManualSelect = () => {
  dialog.visible = true;
  dialog.title = '手动选择关闭数据';
};

/** 多选框选中数据 */
const handleSelectionChange = (selection: any[]) => {
  selectedDialogData.value = selection;
  updateSelectionInfo();
};

// 更新选择信息
const updateSelectionInfo = () => {
  ids.value = selectedDialogData.value.map((item: any) => item.id);
  names.value = selectedDialogData.value.map((item: any) => item.tmplName || item.name);
  single.value = selectedDialogData.value.length !== 1;
  multiple.value = selectedDialogData.value.length === 0;
};

// 打印申请详情列表（弹窗表格）- 独立的数据获取
const getDialogList = async () => {
  dialogLoading.value = true;
  try {
    const res = await getPrintApplyScanCloseDetail(dialogQueryParams.value);
    // console.log('扫码关闭的返回数据', res, dialogQueryParams.value);

    // 过滤掉已关闭的数据
    const filteredData = res.rows.filter((item: any) => !closedIds.value.has(item.id));

    dialogTableData.value = filteredData;
    dialogTotal.value = res.total;
  } catch (error) {
    proxy?.$modal.msgError('加载关闭数据失败，请重试');
  } finally {
    dialogLoading.value = false;
  }
};

onMounted(() => {
  // 主表格默认为空，不加载数据
  mainTableData.value = [];
  mainTotal.value = 0;
  // 初始化已关闭ID集合
  closedIds.value = new Set();
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
