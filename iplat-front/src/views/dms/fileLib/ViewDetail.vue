<template>
  <!-- 详情对话框-->
  <el-dialog v-model="dialog.visible" :title="dialog.title" width="70%" append-to-body :show-close="false">
    <template #header>
      <el-row :gutter="20" class="mb8">
        <el-col :span="18">
          {{ isBatchRevision}}
          <span>{{ dialog.title }}</span>
        </el-col>
        <el-col :span="5" style="position: absolute; right: 20px;">
          <el-button v-if="isShowCloseBtn" type="primary" :icon="dialog.showTableCorrelation ? 'Hide' : 'View'"
            @click="dialog.showTableCorrelation = !dialog.showTableCorrelation">{{ dialog.showTableCorrelation ?
              '收起详情' : '展开详情' }}</el-button>
        </el-col>
      </el-row>
    </template>
    <!-- code="batchRevision-apply-view-detail-form" -->
    <super-form v-if="dialog.visible && dialog.showTableCorrelation"
      :code="isreplace ? 'fileLibrary-history-replace-detail-form' : (isBatchRevision ? 'batchRevision-apply-view-detail-form' : 'fileLibrary-attive-edit-detail-form')"
      :loading="dialog.loading" ref="superFormRef" disabled v-model="form">
    </super-form>
    <el-card v-if="dialog.showTableCorrelation" shadow="hover">
      <template #header>
        <el-row :gutter="10" class="mb8">关联文件</el-row>
      </template>
      <super-table code="fileLibrary-attive-correlation-list" rowKey="id" highlightCurrentRow :dataLoading="loading"
        :data="correlationFileList" v-model:queryParams="queryParamsCorrelation" :total="totalCorrelation"
        @getList="getListCorrelation">
      </super-table>
    </el-card>

    <PdfView style="margin-top: 20px;" v-if="form.id" :text="waterText" :id="form.id"></PdfView>
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="cancel">关 闭</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup name="ViewDetail" lang="ts">
import { ref, reactive, watch } from 'vue';
import { getRelateFileList, getFileDetailActive } from '@/api/dms/fileLib/index';
import { getBatchReviseOrderDetailDetail } from '@/api/dms/batchRevision/index'
import { useUserStore } from '@/store/modules/user';

const { proxy } = getCurrentInstance() as ComponentInternalInstance;

// Props
const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  title: {
    type: String,
    default: '查看详情'
  },
  rowData: {
    type: Object,
    default: () => ({})
  },
  isreplace: {
    type: Boolean,
    default: false
  },
  // 批量修订
  isBatchRevision: {
    type: Boolean,
    default: false
  }
});

// Emits
const emit = defineEmits(['update:visible', 'update:isreplace']);

// 基础状态定义
const loading = ref(false);
const superFormRef = ref(null);
const isShowCloseBtn = ref(true);
const waterText = ref('');

// 对话框状态
const dialog = reactive({
  title: props.title,
  visible: props.visible,
  loading: false,
  showTableCorrelation: true
});

// 数据列表
const correlationFileList = ref([]);
const queryParamsCorrelation = ref({ pageNum: 1, pageSize: 10, id: '' });
const totalCorrelation = ref(0);

// 表单数据
const form = reactive({});

// 监听props变化
watch(
  () => props.visible,
  (newVal) => {
    dialog.visible = newVal;
  }
);

watch(
  () => props.title,
  (newVal) => {
    dialog.title = newVal;
  }
);

watch(
  () => props.rowData,
  (newVal) => {
    if (newVal && dialog.visible) {
      loadDetailData(newVal);
    }
  },
  { deep: true }
);

/** 加载详情数据 */
const loadDetailData = async (row: any) => {
  dialog.loading = true;
  try {
    const _id = row?.id;
    if (_id) {
      queryParamsCorrelation.value.id = _id;
      await getListCorrelation();
      // console.log('批量修订订单详情详细信息！！！！！！！！！！', props.isBatchRevision);
      const res = props.isBatchRevision ? await getBatchReviseOrderDetailDetail(_id) : await getFileDetailActive(_id);
      Object.assign(form, res.data);
      const { name, nickname } = useUserStore();
      const time = proxy?.parseTime(new Date(), '{y}-{m}-{d}');
      waterText.value = `${name} ${nickname} ${time} 生效`;
    }
  } catch (error) {
    proxy?.$modal.msgError('加载详情失败');
  } finally {
    dialog.loading = false;
  }
};

/** 关联文件查询 */
const getListCorrelation = async () => {
  loading.value = true;
  try {
    // 这里需要修改为按照条件来判断
    const res = await getRelateFileList(queryParamsCorrelation.value.id);
    correlationFileList.value = res.rows;
    totalCorrelation.value = res.total;
  } finally {
    loading.value = false;
  }
};

/** 取消 */
const cancel = () => {
  dialog.visible = false;
  emit('update:visible', false);
  emit('update:isreplace', false);
};
</script>
