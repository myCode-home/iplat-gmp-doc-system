<template>
  <div class="p-2">
    <!-- from表单 -->
    <transition :enter-active-class="proxy?.animate.searchAnimate.enter"
      :leave-active-class="proxy?.animate.searchAnimate.leave">
      <div v-show="showSearch" class="mb-[10px]">
        <el-card shadow="hover">
          <super-search :showResetButton="false" code="printApply-Document-Tracking-form" v-model="queryParams"
            @getList="getList" @resetQuery="resetQuery">
          </super-search>
        </el-card>
      </div>
    </transition>
    <!-- 打印文件信息列表 -->
    <el-card shadow="hover">
      <super-table v-if="showTable" code="printApply-Document-Tracking-index-list" rowKey="id" highlightCurrentRow
        :dataLoading="loading" :data="templateEditList" v-model:queryParams="queryParams" :total="total"
        @selection-change="handleSelectionChange" @getList="getList">
        <!-- 操作列 -->
        <template #operation="scope">
          <div>
            <el-tooltip content="查看" placement="top" :enterable="false">
              <el-button link type="primary" v-hasPermi="['dms:printApply:query']" icon="View"
                @click="handleDetail(scope.row)"></el-button>
            </el-tooltip>
          </div>
        </template>
      </super-table>
    </el-card>
    <!-- 打印申请审核详情对话框 -->
    <el-dialog v-model="dialog.visible" :title="dialog.title" width="70%" append-to-body>
      <super-form :loading="dialog.loading" ref="printApply-check-detail" disabled
        code="printApply-Document-Tracking-detail" v-model="form">
      </super-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="cancel">关 闭</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="PrintApplyDestructionConfirmation" lang="ts">
import { getPrintDocInfoList, getPrintDocInfoDetail } from '@/api/dms/printDistribution/printApply/index';
const { proxy } = getCurrentInstance() as ComponentInternalInstance;

const loading = ref(false);
const showSearch = ref(true);
// 这里的ids是选择框选中的全部数据的id
const ids = ref<Array<string | number>>([]);
const names = ref<Array<string | number>>([]);
const single = ref(true);
const multiple = ref(true);
const showTable = ref(true);
const total = ref(0);

// 销毁确认对话框
const dialog = reactive<DialogOption>({
  title: '销毁确认', // 修正弹窗标题
  visible: false,
  loading: false
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

// 表单重置
const reset = () => {
  data.form = { ...initFormData };
};
const { queryParams, form, rules } = toRefs(data);
/** 查看按钮操作 */
const handleDetail = async (row?: any) => {
  reset();
  dialog.visible = true;
  dialog.loading = true;
  dialog.title = '查看详情';
  const _id = row?.id || ids.value[0];
  const res = await getPrintDocInfoDetail(_id);
  // console.log('!!!!!!!!!!!!!!!!!!!', res);
  Object.assign(form.value, res.data);
  dialog.loading = false;
};

const templateEditList = ref([]);

/** 查询打印申请列表 */
const getList = async () => {
  loading.value = true;
  try {
    delete queryParams.value.submit_comments;
    const res = await getPrintDocInfoList(queryParams.value);
    templateEditList.value = res.rows;
    total.value = res.total;
  } finally {
    loading.value = false;
  }
};

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

// 详情弹框取消按钮
const cancel = () => {
  dialog.visible = false;
}
onMounted(() => {
  getList();
});
</script>
