<template>
  <div class="p-2">
    <!-- 打印申请操作按钮 -->
    <transition :enter-active-class="proxy?.animate.searchAnimate.enter"
      :leave-active-class="proxy?.animate.searchAnimate.leave">
      <div v-show="showSearch" class="mb-[10px]">
        <el-card shadow="hover">
          <super-search :showResetButton="false" code="printApply-form-search" v-model="queryParams" @getList="getList"
            @resetQuery="resetQuery">
          </super-search>
        </el-card>
      </div>
    </transition>
    <!-- 销毁确认表格 -->
    <el-card shadow="hover">
      <template #header>
        <el-row :gutter="10" class="mb8">
          <el-col :span="1.5">
            <el-button type="primary" :disabled="multiple"
              icon="CircleCloseFilled" @click="handleBatch">销毁确认</el-button>
          </el-col>
          <right-toolbar v-model:show-search="showSearch" @query-table="getList"></right-toolbar>
        </el-row>
      </template>
      <!-- printApply-Destruction-Confirmation-index-list -->
      <super-table v-if="showTable" code="printApply-Destruction-Confirmation-index-list" rowKey="id"
        highlightCurrentRow :dataLoading="loading" :data="templateEditList" v-model:queryParams="queryParams"
        :total="total" @selection-change="handleSelectionChange" @getList="getList" ref="superTableRef">
        <template #destroyAction="{ row }">
          <el-button type="primary" size="mini" @click="handleDestroy(row)">销毁确认</el-button>
        </template>
      </super-table>
    </el-card>
    <!-- 销毁确认对话框 -->
    <el-dialog v-model="dialog.visible" :title="dialog.title" width="70%" append-to-body>
      <el-form ref="destroyFormRef" :model="destroyData" :rules="destroyRules" label-width="100px" class="mt-4">
        <el-form-item label="销毁方式" prop="destroyAction">
          <el-input v-model="destroyData.destroyAction" placeholder="请输入销毁方式" />
        </el-form-item>
        <el-form-item label="销毁地点" prop="destroyPlace">
          <el-input v-model="destroyData.destroyPlace" placeholder="请输入销毁地点" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialog.visible = false">取 消</el-button>
          <el-button type="primary" @click="handleConfirmDestroy" :loading="buttonLoading">确 认 销 毁</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="PrintApplyDestructionConfirmation" lang="ts">
import { printDestroy, getPrintApplyDestructionConfirmationDetail } from '@/api/dms/printDistribution/printApply/index';
import { useUserStore } from '@/store/modules/user';
const { proxy } = getCurrentInstance() as ComponentInternalInstance;

const buttonLoading = ref(false);
const loading = ref(false);
const showSearch = ref(true);
// 这里的ids是选择框选中的全部数据的id
const ids = ref<Array<string | number>>([]);
const names = ref<Array<string | number>>([]);
const single = ref(true);
const multiple = ref(true);
const showTable = ref(true);
const total = ref(0);
// 销毁确认表单Ref（用于校验）
const destroyFormRef = ref(null);

// 销毁确认对话框
const dialog = reactive<DialogOption>({
  title: '销毁确认', // 修正弹窗标题
  visible: false,
  loading: false
});

// 销毁确认请求的data数据
const destroyData = reactive({
  // // 打印明细主键集合
  // ids: [],
  // // 销毁确认人工号(必填之一)
  // confirmUserId: '',
  // // 确认销毁人账号(必填之一)
  // confirmUserName: '',
  // // 销毁方式(必填)
  // destroyAction: '',
  // // 销毁地点(必填)
  // destroyPlace: ''
});

// 销毁表单校验规则
const destroyRules = reactive({
  destroyAction: [{ required: true, message: '请输入销毁方式', trigger: 'blur' }],
  destroyPlace: [{ required: true, message: '请输入销毁地点', trigger: 'blur' }]
});

// 重置表单数据
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

const { queryParams } = toRefs(data);

const templateEditList = ref([]);

/** 查询打印申请列表 */
// const getList = async () => {
//   loading.value = true;
//   try {
//     delete queryParams.value.submit_comments;
//     const res = await getPrintApplyDestructionConfirmationDetail(queryParams.value);
//     templateEditList.value = res.rows;
//     total.value = res.total;
//   } finally {
//     loading.value = false;
//   }
// };
/** 查询打印申请列表 */
const getList = async () => {
  loading.value = true;
  try {
    delete queryParams.value.submit_comments;
    const res = await getPrintApplyDestructionConfirmationDetail(queryParams.value);
    templateEditList.value = res.rows;
    total.value = res.total;

    // +++ 修复：每次获取新数据时，清空选中状态 +++
    ids.value = [];
    names.value = [];
    single.value = true;
    multiple.value = true;
    // +++ 修复结束 +++

  } finally {
    loading.value = false;
  }
};
// 添加super-table的ref
const superTableRef = ref();
/** 搜索按钮操作 */
const handleQuery = () => {
  queryParams.value.pageNum = 1;
  getList();
};

/** 重置按钮操作 */
const resetQuery = () => {
  handleQuery();
};

/** 多选框选中数据 */
const handleSelectionChange = (selection: any[]) => {
  ids.value = selection.map((item) => item.id);
  names.value = selection.map((item) => item.tmplName);
  single.value = selection.length != 1;
  multiple.value = !selection.length;
};

/** 销毁打印申请（原删除逻辑，修正命名和注释） */
const handleDestroy = async (row?: any) => {
  const _ids = row?.id || ids.value;
  // 确定销毁的项目数量
  const destroyCount = Array.isArray(_ids) ? _ids.length : 1;
  // 根据是否有row参数判断是单个销毁还是批量销毁
  let confirmMsg = '';
  if (row) {
    // 单个销毁的确认消息
    confirmMsg = '是否确认销毁申请单编号为"' + row.applyCode + '"的数据项？';
  } else {
    // 批量销毁的确认消息
    confirmMsg = '是否确认销毁选中的' + destroyCount + '个数据项？';
  }
  // 先弹确认框
  try {
    await proxy?.$modal.confirm(confirmMsg);
    // 确认后打开销毁信息填写弹窗
    dialog.visible = true;
    // 初始化销毁表单数据
    destroyData.ids = _ids;
    // 获取当前登录用户信息（如需自动填充确认人）
    const userStore = useUserStore();
    // destroyData.confirmUserId = userStore.userId;
    // destroyData.confirmUserName = userStore.userName;
  } catch (error) {
    // 用户取消确认
    return;
  } finally {
    loading.value = false;
  }
};

/** 弹窗确认销毁操作 */
// const handleConfirmDestroy = async () => {
//   // 先校验表单
//   const valid = await destroyFormRef.value.validate();
//   if (!valid) return;

//   buttonLoading.value = true;
//   try {
//     // 拿到电子签名的值
//     const res = await proxy?.$Signature(printDestroy) as any;
//     console.log('电子签名!!!!!!!!!!!!!!!!!!!!!!!!!', res);
//     // 销毁人
//     destroyData.userName = res.users[0].userName;
//     // 确认销毁人（监督人confirmUserName）
//     destroyData.confirmUserName = res.users[1].userName;
//     // 执行销毁操作
//     await printDestroy(destroyData, res?.es);
//     // 显示成功消息
//     proxy?.$modal.msgSuccess('销毁成功');
//     // 关闭弹窗
//     dialog.visible = false;
//     // 清空销毁表单数据
//     destroyData.destroyAction = '';
//     destroyData.destroyPlace = '';
//     destroyData.ids = [];
//     // 刷新列表
//     await getList();
//   } catch (error) {
//     proxy?.$modal.msgError('销毁失败，请重试');
//   } finally {
//     buttonLoading.value = false;
//   }
// };

// 批量操作的销毁确认按钮
/** 弹窗确认销毁操作 */
const handleConfirmDestroy = async () => {
  // 先校验表单
  const valid = await destroyFormRef.value.validate();
  if (!valid) return;

  buttonLoading.value = true;
  try {
    // 拿到电子签名的值
    const res = await proxy?.$Signature(printDestroy) as any;
    console.log('电子签名!!!!!!!!!!!!!!!!!!!!!!!!!', res);
    // 销毁人
    destroyData.userName = res.users[0].userName;
    // 确认销毁人（监督人confirmUserName）
    destroyData.confirmUserName = res.users[1].userName;
    // 执行销毁操作
    await printDestroy(destroyData, res?.es);
    // 显示成功消息
    proxy?.$modal.msgSuccess('销毁成功');
    // 关闭弹窗
    dialog.visible = false;
    // 清空销毁表单数据
    destroyData.destroyAction = '';
    destroyData.destroyPlace = '';
    destroyData.ids = [];

    // +++ 关键修复：清空选中的数据 +++
    ids.value = [];
    names.value = [];
    single.value = true;
    multiple.value = true;
    // +++ 修复结束 +++
    if (superTableRef.value) {
      // 假设super-table有clearSelection方法，具体方法名根据实际组件而定
      superTableRef.value.clearSelection && superTableRef.value.clearSelection();
    }

    // 重置表单验证状态
    destroyFormRef.value.resetFields();

    // 刷新列表
    await getList();
  } catch (error) {
    proxy?.$modal.msgError('销毁失败，请重试');
  } finally {
    buttonLoading.value = false;
  }
};
const handleBatch = () => {
  // 检查是否有选中的数据
  if (ids.value.length === 0) {
    proxy?.$modal.msgWarning('请先选择要销毁的数据项');
    return;
  }
  // 调用销毁函数，不传递row参数表示批量销毁选中项
  handleDestroy();
};

onMounted(() => {
  getList();
});
</script>
