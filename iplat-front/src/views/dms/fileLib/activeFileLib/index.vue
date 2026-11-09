<template>
  <div class="p-2">
    <el-card style="">
      <el-tabs v-model="activeName" class="demo-tabs" @tab-click="handleClick">
        <el-tab-pane label="文件" name="editApply">
          <edit-index :isactive="true" :getListByFather="getListByFather" ref="editIndexRef" @switchToRecordTab="cutRecord">
            <template #operation="{ scope }">
              <el-tooltip content="编辑" placement="top" :enterable="false">
                <el-button :disabled="!canEditableStatus(scope.row)" link type="primary" icon="EditPen"
                  v-hasPermi="['dms:docOperateOrder:edit']" @click="handleUpdate(scope.row)"></el-button>
              </el-tooltip>
              <el-tooltip content="终止" placement="top" :enterable="false">
                <el-button link type="primary" :disabled="!canInvalidStatus(scope.row)" icon="CircleClose"
                  v-hasPermi="['dms:docOperateOrder:remove']" @click="handleDelete(scope.row)"></el-button>
              </el-tooltip>
              <el-tooltip content="预览PDF" placement="top" :enterable="false">
                <el-button link type="primary" icon="Search" @click="handlePreviewPDF(scope.row)"></el-button>
              </el-tooltip>
              <!-- 导出word -->
              <el-tooltip content="浏览历史" placement="top" :enterable="false">
                <el-button link type="primary" icon="Connection" @click="handleBrowseHistory(scope.row)"></el-button>
              </el-tooltip>
              <el-tooltip content="查看附件" placement="top" :enterable="false">
                <el-button link type="primary" icon="UploadFilled" v-hasPermi="['dms:docOperateOrder:upload']"
                  @click="handleUpload(scope.row)"></el-button>
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
import { getActiveFileList } from '@/api/dms/fileLib';
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
/**
 * 判断文件是否可申请失效
 * 条件：docSource为new/revise + useState为active/replaceDeactiving
 */
const canInvalidStatus = (row: any) => {
  const validSource = ['new', 'revise'].includes(row.docSource);
  const validState = ['active', 'replaceDeactiving'].includes(row.useState);
  return validSource && validState;
};
/**
 * 判断文件是否可编辑
 * 条件：docSource为new/revise + docOperationSource为fromInput/autoCreate/manualRevision
 */
const canEditableStatus = (row: any) => {
  const validSource = ['new', 'revise'].includes(row.docSource);
  const validOperation = ['fromInput', 'autoCreate', 'manualRevision'].includes(row.docOperationSource);
  return validSource && validOperation;
}

// 附件上传按钮操作
const handleUpload = (row: any) => {
  editIndexRef.value.handleUpload(row);
}
// 编辑按钮操作
const handleUpdate = () => {
  editIndexRef.value.handleUpdate();
}
// 浏览历史按钮操作
const handleBrowseHistory = (row: any) => {
  editIndexRef.value.handleBrowseHistory(row);
}
// 预览pdf按钮操作
const handlePreviewPDF = (row: any) => {
  editIndexRef.value.handlePreviewPDF(row);
}
// 终止按钮操作
const handleDelete = (row: any) => {
  editIndexRef.value.handleDelete(row);
}
// 给子组件传的查询历史文件库的getList
const getListByFather = async (params: any) => {
  const res = await getActiveFileList(params);
  return res;
}

</script>
