<template>
  <div class="p-2">
    <el-card style="">
      <el-tabs v-model="activeName" class="demo-tabs" @tab-click="handleClick">
        <el-tab-pane label="文件信息" name="doc">
          <doc-index @switchToRecordTab="cutPlan"></doc-index>
        </el-tab-pane>
        <el-tab-pane label="培训计划" :disabled="disabled" name="plan">
          <training-plan :docCreateId="docCreateId"></training-plan>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup name="TrainingConfirmation" lang="ts">
import DocIndex from '@/views/dms/training/trainingConfirmation/docIndex.vue';
import TrainingPlan from '@/views/dms/training/trainingConfirmation/trainingPlan.vue';
const activeName = ref('doc');
const disabled = ref(true);

const docCreateId = ref('');
// 切换到记录
const cutPlan = (row: any) => {
  docCreateId.value = row.id
  disabled.value = false;
  activeName.value = 'plan';
}

// 处理标签页点击
const handleClick = (tab: any) => {
  if (tab.props.name === 'doc') {
    disabled.value = true;
  } else if (tab.props.name === 'record') {
    disabled.value = false;
  }
}

onMounted(() => {

});
</script>
