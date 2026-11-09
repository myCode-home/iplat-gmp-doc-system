<template>
  <div class="p-2">
    <el-card style="">
      <el-tabs v-model="activeName" class="demo-tabs" @tab-click="handleClick">
        <el-tab-pane label="文件" name="editApply">
          <edit-index :isShowOtherVersion="false" :getListByFather="getListByFather"
            minaTableCode="invalidFileLib-index-list" ref="editIndexRef" @switchToRecordTab="cutRecord">
            <template #operation="{ scope }">
              <el-tooltip content="定位替代文件" placement="top" :enterable="false">
                <!-- :disabled="scope.row.deactiveSource !== 'replaceFailure'" -->
                <el-button :disabled="scope.row.deactiveSource !== 'replaceFailure'" link type="primary" icon="Location"
                  v-hasPermi="['dms:docOperateOrder:upload']" @click="handleReplaceFile(scope.row)" />
              </el-tooltip>
            </template>
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
import { getHistoryFileIndexList } from '@/api/dms/fileLib';
import EditIndex from '@/views/dms/fileLib/BaseEditIndex.vue';
import RecordIndex from '@/views/dms/fileLib/BaseRecordIndex.vue';
const activeName = ref('editApply');
const disabled = ref(true);
const editIndexRef = ref();

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
// 定位替代文件按钮操作
const handleReplaceFile = (row: any) => {
  editIndexRef.value.handleReplaceFile(row);
}
// 给子组件传的查询历史文件库的getList
const getListByFather = async (params: any) => {
  const res = await getHistoryFileIndexList(params);
  return res;
}

</script>
