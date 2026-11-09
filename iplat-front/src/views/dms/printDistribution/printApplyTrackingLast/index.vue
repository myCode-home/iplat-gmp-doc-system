<template>
  <div class="p-2">
    <!-- from表单 -->
    <transition :enter-active-class="proxy?.animate.searchAnimate.enter"
      :leave-active-class="proxy?.animate.searchAnimate.leave">
      <div v-show="showSearch" class="mb-[10px]">
        <el-card shadow="hover">
          <super-search :showSearch="true" :showResetButton="false" code="printApply-TrackingLast-search-form"
            v-model="queryParams" @getList="getList">
            <template #rightButton>
              <el-button :loading="isLoadingExport" icon="Upload" type="primary" plain
                @click="exportData">导出</el-button>
            </template>
          </super-search>
        </el-card>
      </div>
    </transition>
    <!-- 打印文件信息列表 -->
    <el-card shadow="hover">
      <super-table v-if="showTable" code="printApply-TrackingLast-index-list" rowKey="id" highlightCurrentRow
        :dataLoading="loading" :data="templateEditList" :queryParams="queryParams" :total="total" @getList="getList">
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
      <super-search :showSearch="true" :showResetButton="false" code="printApply-TrackingLast-detail-search-form"
        v-model="queryDetailParams" @getList="getDetailList">
      </super-search>
      <super-table v-if="showDetailTable" code="printApply-TrackingLast-detail-list" rowKey="id" highlightCurrentRow
        :dataLoading="detailLoading" :data="templateEditListDetail" :queryParams="queryDetailParams"
        :total="totalDetail" @getList="getDetailList">
      </super-table>
    </el-dialog>
  </div>
</template>

<script setup name="PrintApplyTrackingLast" lang="ts">
import { getPrintApplyDocumentTrackingList, getPrintApplyTrackingDetail, exportPrintDocInfoList } from '@/api/dms/printDistribution/printApply/index';
import { getCurrentInstance, ComponentInternalInstance, onMounted } from 'vue';
import { ref, reactive, toRefs } from 'vue';

const { proxy } = getCurrentInstance() as ComponentInternalInstance;

// 主表格相关状态
const loading = ref(false);
const showSearch = ref(true);
// const ids = ref<Array<string | number>>([]);
const showTable = ref(true);
const total = ref(0);
const templateEditList = ref([]);

// 定义DialogOption接口（修复类型提示）
interface DialogOption {
  title: string;
  visible: boolean;
  loading: boolean;
}

const dialog = reactive<DialogOption>({
  title: '查看详情',
  visible: false,
  loading: false
});

// 分页查询参数（主表格）
interface PageData<T, U> {
  form: T;
  queryParams: U;
  rules: Record<string, any>;
}
const initFormData = {};
const data = reactive<PageData<any, any>>({
  form: { ...initFormData },
  queryParams: reactive({
    pageNum: 1,
    pageSize: 10
  }),
  rules: {}
});
const { queryParams } = toRefs(data);

// 详情表格相关状态
const detailLoading = ref(false);
const showDetailTable = ref(true);
const totalDetail = ref(0);
const templateEditListDetail = ref([]);
// 详情表格查询参数
const detaiData = reactive({
  queryDetailParams: reactive({
    pageNum: 1,
    pageSize: 10,
    applyCode: ''
  })
});
const { queryDetailParams } = toRefs(detaiData);
// 表单重置
const reset = () => {
  data.form = { ...initFormData };
};

/** 查看按钮操作 */
const handleDetail = async (row?: any) => {
  reset();
  dialog.visible = true;
  dialog.loading = true;
  dialog.title = '查看详情';
  // // 存储详情ID到查询参数，重置分页
  queryDetailParams.value.applyCode = row.applyCode;
  queryDetailParams.value.pageNum = 1;
  console.log('详情查询参数:', queryDetailParams.value);
  await getDetailList();
};

/** 分页查询打印件追踪主页面列表 */
const getList = async () => {
  loading.value = true;
  try {
    delete queryParams.value.submit_comments;
    const res = await getPrintApplyDocumentTrackingList(queryParams.value);
    templateEditList.value = res.rows;
    total.value = res.total;
  } catch (error) {
    console.error('获取主列表数据失败:', error);
    templateEditList.value = [];
    total.value = 0;
  } finally {
    loading.value = false;
  }
};

// 右上角导出数据的按钮
const isLoadingExport = ref(false);
const exportData = async () => {
  isLoadingExport.value = true;
  try {
    console.log('开始导出数据');
    const res = await exportPrintDocInfoList({});

    // 校验返回的二进制数据
    if (!res || !(res instanceof Blob)) {
      proxy?.$modal.msgError('导出失败：返回数据格式异常');
      return;
    }

    // 处理文件名（优先解析响应头，兜底适配xlsx）
    let fileName = '打印数据导出.xlsx'; // 场景2：直接返回xlsx时的默认名
    // let fileName = '打印数据导出.zip'; // 场景1：返回ZIP包（内含xlsx）时的默认名
    const contentDisposition = res.headers?.['content-disposition'];
    if (contentDisposition) {
      const match = contentDisposition.match(/filename\*?=([^;]+)/i);
      if (match && match[1]) {
        fileName = decodeURIComponent(match[1].replace(/["']/g, ''));
      }
    }

    // 适配xlsx的Blob类型
    const blob = new Blob([res], {
      // xlsx专属MIME类型（场景2用这个，场景1用 'application/zip' 或默认）
      type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
      // type: 'application/zip' // 场景1：ZIP包的MIME类型（可选，不指定也能下载）
    });

    // 创建下载链接并触发下载
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = fileName; // 带.xlsx/.zip后缀的文件名
    link.click();

    // 释放临时URL
    window.URL.revokeObjectURL(url);

    proxy?.$modal.msgSuccess('文件导出成功！');
  } catch (error) {
    proxy?.$modal.msgError('文件导出失败，请稍后重试');
  } finally {
    isLoadingExport.value = false;
  }
};

/** 分页查询详情表格数据 */
const getDetailList = async () => {
  detailLoading.value = true;
  try {
    // 调试日志：打印最终传递给接口的参数，方便排查
    // queryDetailParams.value = {
    // pageNum: queryDetailParams.value.pageNum,
    // pageSize: queryDetailParams.value.pageSize,
    // applyCode: 'DMS-000042'
    // applyCode: queryDetailParams.value.applyCode
    // }
    console.log('详情查询参数:', queryDetailParams.value);
    const res = await getPrintApplyTrackingDetail(queryDetailParams.value);
    // const res = await getPrintApplyTrackingDetail({});
    templateEditListDetail.value = res.rows;
    totalDetail.value = res.total;
    showDetailTable.value = true;
  } catch (error) {
    console.error('获取详情列表数据失败:', error);
    templateEditListDetail.value = [];
    totalDetail.value = 0;
  } finally {
    detailLoading.value = false;
  }
};

onMounted(() => {
  getList();
});
</script>
