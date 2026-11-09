<template>
  <div class="p-2">
    <el-card style="">
      <el-tabs v-model="activeName" class="demo-tabs" @tab-click="handleClick">
        <el-tab-pane label="文件" name="editApply">
          <edit-index :getListByFather="getListByFather" @switchToRecordTab="cutRecord" :minaTableCode="minaTableCode">
            <slot name="operation"></slot>
          </edit-index>
        </el-tab-pane>
        <el-tab-pane label="记录" :disabled="disabled" name="recordEdit">
          <record-index :daId="daId"></record-index>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup name="DocApprove" lang="ts">
import { ref } from 'vue';
import EditIndex from '@/views/dms/fileLib/BaseEditIndex.vue';
import RecordIndex from '@/views/dms/fileLib/BaseRecordIndex.vue';
const activeName = ref('editApply');
const disabled = ref(true);

const props = defineProps({
  minaTableCode: {
    type: String
  },
  getListByFather: {
    type: Function,
    default: () => {}
  }
})
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
</script>
