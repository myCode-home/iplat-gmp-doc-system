<template>
  <div class="p-2">
    <transition :enter-active-class="proxy?.animate.searchAnimate.enter" :leave-active-class="proxy?.animate.searchAnimate.leave">
      <div v-show="showSearch" class="mb-[10px]">
        <el-card shadow="hover">
          <super-search
            code="bookPrint-issue-receiveApplication-search"
            v-model="queryParams"
            @getList="getList"
            @resetQuery="resetQuery"
          >
          </super-search>
        </el-card>
      </div>
    </transition>

    <el-card shadow="hover">
      <template #header>
        <el-row :gutter="10" class="mb8">
          <right-toolbar v-model:show-search="showSearch" @query-table="getList"></right-toolbar>
        </el-row>
      </template>
      <super-table 
        v-if="showTable"
        code="bookPrint-issue-receiveApplication-table"
        :class="{ nomultiple: !multipleChoice }"
        ref="fileTableRef"
        rowKey="id"
        highlightCurrentRow
        :dataLoading="loading"
        :data="recordList"
        v-model:queryParams="queryParams"
        :total="total"
        @select="selectChange"
        @selection-change="handleSelectionChange"
        @getList="getList"
      >
      </super-table>
      <!-- <pagination v-show="total > 0" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :total="total" @pagination="getList" /> -->
    </el-card>
  </div>
</template>

<script setup name="RecordAlterationApply" lang="ts">
import { printInfoListAll } from '@/api/dms/bookPrint/bookPrintPage/index';
import { fileOpenCheck } from '@/api/workflow/task';
import { useUserStore } from '@/store/modules/user';
const { proxy } = getCurrentInstance() as ComponentInternalInstance;
const props = defineProps({
  multipleChoice: {
    type: Boolean,
    default: true
  },
  otherCondition: {
    type: Object,
    default: () => { }
  },
  multipleChoiceData: {
    type: Function,
    default: () => () => { }
  }
});

const buttonLoading = ref(false);
const loading = ref(false);
const showSearch = ref(true);
const ids = ref<Array<string | number>>([]);
const names = ref<Array<string | number>>([]);
const single = ref(true);
const multiple = ref(true);
const showTable = ref(true);
const total = ref(0);

const superFormRef = ref(null);

const dialog = reactive<DialogOption>({
  title: '',
  visible: false,
  loading: false
});

const apprDialog = reactive<DialogOption>({
  title: '请输入提交意见',
  visible: false,
  loading: false
});

const initFormData = {
  drafterId: useUserStore().userId,
  drafterUserName: useUserStore().nickname,
  approveComments: '',
  operate: ''
};
const data = reactive<PageData<any, any>>({
  form: { ...initFormData },
  queryParams: reactive({
    pageNum: 1,
    pageSize: 10,
  }),
  rules: {
    // id: [{ required: true, message: '主键不能为空', trigger: 'blur' }],
  }
});

const { queryParams } = toRefs(data);

watch(() => props.otherCondition, () => {
  Object.keys(props.otherCondition).forEach(key => {
    queryParams.value[key] = props.otherCondition[key];
  });
}, { immediate: true, deep: true });


const recordList = ref([]);

/** 查询成册打印列表列表 */
const getList = async () => {
  loading.value = true;
  try {
    const res = await printInfoListAll(queryParams.value);
    recordList.value = res.rows;
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

const selectRows = ref([]);
const fileTableRef = ref();
/** 多选框选中数据 */
const handleSelectionChange = (selection: any[]) => {
  ids.value = selection.map((item) => item.id);
  names.value = selection.map((item) => item.tmplName);
  single.value = selection.length != 1;
  multiple.value = !selection.length;
  selectRows.value = selection;
};
const selectChange = (selection: any[]) => {
  if (!props.multipleChoice) {
    if (selection.length > 1) {
      const del_row = selection.shift();
      fileTableRef.value.toggleRowSelection(del_row, false);
    }
  }
};

/** 提交按钮操作 */
const confirmChoiceData = async () => {
  if (selectRows.value.length === 0) {
    return proxy?.$modal.msgError('请选择');
  }
  if (props.multipleChoice) {
    props.multipleChoiceData(selectRows.value);
  } else {
    props.multipleChoiceData(selectRows.value[0]);
  }
};

onMounted(() => {
  getList();
});

defineExpose({
  confirmChoiceData
});
</script>
