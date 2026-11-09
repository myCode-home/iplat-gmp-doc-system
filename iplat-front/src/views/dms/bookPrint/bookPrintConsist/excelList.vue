<template>
  <el-button :loading="refreshLoading" @click="getExcelList" type="primary" style="float: right;margin-bottom: 5px;" icon="Refresh">刷新中转库</el-button>
  <super-table 
    code="bookPrint-apply-table"
    rowKey="id"
    height="calc(100vh - 228px)"
    highlightCurrentRow
    :dataLoading="loading"
    :data="excelList"
    @selection-change="handleSelectionChange"
    @getList="getList"
  >
    <!-- 操作列 -->
    <template #operation="scope">
      <div>
        <el-tooltip content="查看任务" placement="top" :enterable="false">
          <el-button link type="primary" v-hasPermi="['dms:volumesPrintApply:trackDetail']" icon="View" @click="handleDetail(scope.row)"></el-button>
        </el-tooltip>
        <el-tooltip content="刷新" placement="top" :enterable="false">
          <el-button link type="primary" :loading="scope.row.reLoading" v-hasPermi="['dms:volumesPrintApply:trackDetail']" icon="Refresh" @click="refresh(scope.row)"></el-button>
        </el-tooltip>
      </div>
    </template>
  </super-table>
</template>

<script lang="ts" setup>
import { listActiveExcel, listInActiveExcel, taskRefresh } from '@/api/dms/bookPrint/bookPrintApplication/index';

const { proxy } = getCurrentInstance() as ComponentInternalInstance;
const emit = defineEmits(['goto']);

// 接收父组件传递的 activeName
const props = defineProps({
  activeName: {
    type: String,
    default: 'takeEffect'
  }
});

const loading = ref(false);
const excelList = ref([]);

const handleSelectionChange = (val) => {
  console.log(val);
};
const getList = async () => {
  loading.value = true;
  if(props.activeName === 'takeEffect'){
    const res = await listActiveExcel();
    excelList.value = res.data;
  }else{
    const res = await listInActiveExcel();
    excelList.value = res.data;
  }
  loading.value = false;
};

// 查看任务按钮
const handleDetail = (row?: any) => {
  emit('goto', {...row, page: 'printingAdd'});
};
// 刷新按钮
const refresh = async (row?: any) => {
  row.reLoading = true;
  await proxy?.$modal.confirm('确认刷新任务吗，此过程可能花费较长时间，请耐心等待？').finally(() => (loading.value = false));
  taskRefresh({ excelName: row }).then(res=>{
    proxy?.$modal.msgSuccess('操作成功');
    getList();
  }).finally(()=>{
    row.reLoading = false;
  });
}

const refreshLoading = ref(false);
const getExcelList = async () => {
  // emit('goto', { excelName: '制剂生产部批记录基础表.xlsx', page: 'printingAdd'});
  // return 
  refreshLoading.value = true;
  if(props.activeName === 'takeEffect'){
    const res = await listActiveExcel();
    excelList.value = res.data;
  }else{
    const res = await listInActiveExcel();
    excelList.value = res.data;
  }
  refreshLoading.value = false;
};

onMounted(() => {
  getList();
});

</script>


<style scoped>

</style>
