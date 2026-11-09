<template>
  <div class="p-2">
    <el-card style="">
      <el-tabs v-model="activeName" class="demo-tabs" @tab-click="handleClick">
        <el-tab-pane label="修订申请" name="editApply">
          <edit-index @switchToRecordTab="cutRecord"></edit-index>
        </el-tab-pane>
        <el-tab-pane label="记录修订" :disabled="disabled" name="recordEdit">
          <record-index :daId="daId"></record-index>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup name="DocApprove" lang="ts">
import EditIndex from '@/views/dms/operateOrder/editOperateOrder/editIindex.vue';
import RecordIndex from '@/views/dms/operateOrder/editOperateOrder/recordIndex.vue';
const activeName = ref('editApply');
const disabled = ref(true);

const daId = ref('');
// 切换到记录
const cutRecord = (row) => {
  disabled.value = false;
  activeName.value = 'recordEdit';
  daId.value = row.id;
}

// 处理标签页点击
const handleClick = (tab: any) => {
  if (tab.props.name === 'editApply') {
    disabled.value = true;
    daId.value = '';
  } else if (tab.props.name === 'recordEdit') {
    disabled.value = false;
  }
}

onMounted(() => {

});
</script>
