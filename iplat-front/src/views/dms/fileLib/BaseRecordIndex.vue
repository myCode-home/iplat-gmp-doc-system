<template>
  <div class="">
    <div v-show="showSearch" class="mb-[10px]">
      <el-card shadow="hover">
        <super-search code="operateOrder-recordEdit-search" v-model="queryParams" @getList="getList"
          @resetQuery="resetQuery">
        </super-search>
      </el-card>
    </div>

    <el-card shadow="hover">
      <template #header>
        <el-row :gutter="10" class="mb8">
          <right-toolbar v-model:show-search="showSearch" @query-table="getList"></right-toolbar>
        </el-row>
      </template>
      <super-table v-if="showTable" code="operateOrder-recordEdit-table" rowKey="id" :dataLoading="loading"
        :data="temApproveList" v-model:queryParams="queryParams" :total="total"
        @selection-change="handleSelectionChange" @getList="getList">
        <!-- 操作列 -->
        <template #operation="scope">
          <div>
            <el-tooltip content="编辑" placement="top" :enterable="false">
              <el-button link type="primary" v-hasPermi="['dms:docOperateOrder:edit']" icon="EditPen"
                @click="handleUpdate(scope.row)"></el-button>
            </el-tooltip>
          </div>
        </template>
      </super-table>
    </el-card>
    <!-- 添加或编辑记录修订对话框 -->
    <el-dialog v-model="dialog.visible" :title="dialog.title" width="70%" append-to-body>
      <super-form v-if="dialog.visible" :loading="dialog.loading" ref="superFormRef" disabled
        code="operateOrder-recordEdit-form" v-model="form">
      </super-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="cancel">关 闭</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="TemplateApprove" lang="ts">
import { ref, reactive, toRefs, watch } from 'vue';
import { listDocRecordRevise, getDocRecordRevise } from '@/api/dms/operateOrder/addOperateOrder/record';
import { getRecordListByFileLibId } from '@/api/dms/fileLib/index';
const props = defineProps({
  // 修订申请id
  daId: {
    type: String,
    default: ''
  }
});

const loading = ref(false);
const showSearch = ref(true);
const ids = ref<Array<string | number>>([]);
const showTable = ref(true);
const total = ref(0);

const superFormRef = ref(null);

const dialog = reactive({
  title: '',
  visible: false,
  loading: false
});

const data = reactive({
  form: {},
  queryParams: reactive({
    pageNum: 1,
    pageSize: 10,
  })
});

const { queryParams, form } = toRefs(data);

const temApproveList = ref([]);

watch(() => props.daId, (val) => {
  if (val) {
    getList();
  } else {
    temApproveList.value = [];
    total.value = 0;
  }
});

/** 查询记录修订列表 */
const getList = async () => {
  loading.value = true;
  try {
    // queryParams.value.daId = props.daId;
    const res = await getRecordListByFileLibId(props.daId, queryParams.value);
    temApproveList.value = res.rows;
    total.value = res.total;
  } finally {
    loading.value = false;
  }
};

/** 重置按钮操作 */
const resetQuery = () => {
  queryParams.value.pageNum = 1;
  getList();
};

/** 多选框选中数据 */
const handleSelectionChange = (selection: any[]) => {
  ids.value = selection.map((item) => item.id);
};

/** 取消按钮 */
const cancel = () => {
  dialog.visible = false;
};

/** 编辑按钮操作 */
const handleUpdate = async (row?: any) => {
  dialog.visible = true;
  dialog.loading = true;
  dialog.title = '编辑记录修订';
  const _id = row?.id || ids.value[0];
  const res = await getDocRecordRevise(_id);
  Object.assign(form.value, res.data);
  dialog.loading = false;
};
</script>
