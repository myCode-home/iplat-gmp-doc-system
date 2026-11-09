<template>
  <div class="p-2">
    <el-card style="">
      <el-tabs v-model="activeName" class="demo-tabs" @tab-click="handleClick">
        <el-tab-pane label="文件" name="editApply">
          <edit-index :getListByFather="getListByFather" minaTableCode="notActiveFileLib-index-list" ref="editIndexRef"
            @switchToRecordTab="cutRecord">
            <template #operation="{ scope }">
              <el-tooltip content="编辑" placement="top" :enterable="false">
                <el-button link type="primary" icon="EditPen" :disabled="!canEditableStatus(scope.row)"
                  v-hasPermi="['dms:docOperateOrder:edit']" @click="handleUpdate(scope.row)"></el-button>
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
import { getOverdueFileList } from '@/api/dms/fileLib';
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
// 给子组件传的查询历史文件库的getList
const getListByFather = async (params: any) => {
  const res = await getOverdueFileList(params);
  return res;
}
const canEditableStatus = (row: any) => {
  return editIndexRef.value.canEditableStatus(row);
}
const handleUpdate = (row: any) => {
  editIndexRef.value.handleUpdate(row);
}
</script>
