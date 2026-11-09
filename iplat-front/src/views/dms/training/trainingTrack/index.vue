<template>
  <div class="p-2">
    <el-card style="">
      <el-tabs v-model="activeName" class="demo-tabs" @tab-click="handleClick">
        <el-tab-pane label="文件信息" name="doc">
          <doc-index @switchToRecordTab="cutTrain"></doc-index>
        </el-tab-pane>
        <el-tab-pane label="培训确认信息" :disabled="disabled" name="trainingConfirmation">
          <training-confirmation :docCreateId="docCreateId"></training-confirmation>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup name="TrainingTrack" lang="ts">
import DocIndex from '@/views/dms/training/trainingTrack/docIndex.vue';
import TrainingConfirmation from '@/views/dms/training/trainingTrack/trainingConfirmation.vue';
const activeName = ref('doc');
const disabled = ref(true);

const docCreateId = ref('');
// 切换到记录
const cutTrain = (row: any) => {
  docCreateId.value = row.id
  disabled.value = false;
  activeName.value = 'trainingConfirmation';
}

// 处理标签页点击
const handleClick = (tab: any) => {
  if (tab.props.name === 'doc') {
    disabled.value = true;
  } else if (tab.props.name === 'trainingConfirmation') {
    disabled.value = false;
  }
}

onMounted(() => {

});
</script>
