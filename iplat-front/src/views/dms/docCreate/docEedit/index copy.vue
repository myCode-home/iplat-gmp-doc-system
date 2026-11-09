<template>
  <div class="p-2">
    <el-card style="">
      <el-tabs v-model="activeName" class="demo-tabs" @tab-click="handleClick">
        <el-tab-pane label="文件" name="doc">
          <doc-index @switchToRecordTab="cutRecord"></doc-index>
        </el-tab-pane>
        <el-tab-pane label="记录" :disabled="disabled" name="record">
          <record-index :docCreateId="docCreateId"></record-index>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup name="DocEedit" lang="ts">
import DocIndex from '@/views/dms/docCreate/docEedit/docIndex.vue';
import RecordIndex from '@/views/dms/docCreate/docEedit/recordIndex.vue';
const activeName = ref('doc');
const disabled = ref(true);

const docCreateId = ref('');
// 切换到记录
const cutRecord = (row: any) => {
  docCreateId.value = row.id
  disabled.value = false;
  activeName.value = 'record';
}

// 处理标签页点击
const handleClick = (tab: any) => {
  if (tab.props.name === 'doc') {
    disabled.value = true;
  }
}

onMounted(() => {

});
</script>
