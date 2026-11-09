<template>
  <el-card>
    <template #header>
      <div ref="docActiveRef">
        <el-breadcrumb :separator-icon="ArrowRight" separator-class="el-icon-arrow-right" style="display: inline-block">
          <el-breadcrumb-item><span @click="handleCancel" class="title-clickable-item">{{activeName=='takeEffect'?'生效组册':'待生效组册'}}</span></el-breadcrumb-item>
          <el-breadcrumb-item>{{excelName}}</el-breadcrumb-item>
        </el-breadcrumb>
        <el-button style="float: right; padding: 0px 0px 14px 0" type="text" @click="handleCancel">返回</el-button>
      </div>
    </template>
    <super-search
      code="bookPrintConsist-task-search"
      v-model="queryParams"
      @getList="getList"
      @resetQuery="resetQuery"
    >
      <template #docTypeKeySlot="config">
        <el-select v-model="queryParams[config.prop]" v-bind="config" placeholder="请选择记录类型" clearable filterable>
          <el-option
            v-for="item in recordData"
            :key="item.dictValue"
            :label="item.dictLabel"
            :value="item.dictValue"
          ></el-option>
        </el-select>
      </template>
      <template #controlledInfoSlot="config">
        <el-select v-model="queryParams[config.prop]" v-bind="config" placeholder="请选择受控信息" clearable filterable>
          <el-option
            v-for="item in controllData"
            :key="item.dictValue"
            :label="item.dictLabel"
            :value="item.dictValue"
          ></el-option>
        </el-select>
      </template>
    </super-search>
    <super-table 
      code="bookPrintConsist-task-table"
      rowKey="id"
      height="calc(100vh - 240px)"
      highlightCurrentRow
      :dataLoading="loading"
      :data="dataList"
      v-model:queryParams="queryParams"
      :total="total"
      @selection-change="handleSelectionChange"
      @getList="getList"
    >
    </super-table>  
    <!-- 底部固定按钮 -->
    <div class="fixed-footer">
      <el-button :loading="buttonLoading" size="small" type="primary" @click="completeVolume" icon="Check">成册</el-button>
      <el-button :loading="buttonLoading" size="small" type="danger" @click="refuseVolume" icon="Close">拒绝</el-button>
    </div>

    <!-- 类型对比不一致对话框 -->
    <el-dialog v-model="showContrastDoc" :title="contrastTitle" width="70%" append-to-body>
      <el-alert title="记录类型、申请部门、打印类型 不一致的文件无法进行成册" type="warning" show-icon :closable="false" style="margin-bottom: 8px;" />
      <ContrastDoc v-if="showContrastDoc" :data="contrastDocList" />
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="showContrastDoc = false">关闭</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 成册失败对话框 -->
    <el-dialog v-model="showRejectDoc" :title="rejectTitle" width="500px" append-to-body>
      <el-alert :title="warTitle" type="warning" show-icon :closable="false" style="margin-bottom: 8px;" />
      <RejectDoc v-if="showRejectDoc" :data="rejectDocList" />
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="showRejectDoc = false">关闭</el-button>
        </div>
      </template>
    </el-dialog>
  </el-card>
</template>

<script lang="ts" setup>
import { ArrowRight } from '@element-plus/icons-vue'
import { listManualTask, listDocType, listControlledInfo, acceptActive, acceptUnactive, taskReject } from '@/api/dms/bookPrint/bookPrintApplication/index'
import ContrastDoc from '@/views/dms/bookPrint/bookPrintConsist/ContrastDoc.vue'
import RejectDoc from '@/views/dms/bookPrint/bookPrintConsist/rejectDoc.vue'

const { proxy } = getCurrentInstance() as ComponentInternalInstance

// 接收父组件传递的 activeName
const props = defineProps({
  activeName: {
    type: String,
    default: 'takeEffect'
  },
  excelName: {
    type: String,
    default: ''
  }
});

const emit = defineEmits(['back']);

const loading = ref(false);
const buttonLoading = ref(false);
const total = ref(0);
const dataList = ref([]);
const manual = ref(false);

// 查询参数
const queryParams = ref({
  pageNum: 1,
  pageSize: 10,
  range: props.activeName == 'takeEffect' ? 'active' : 'unActive'
});

// 当前选中行
const selectedRow = ref<any[]>([]);
// 当前选中的文件编号集合
const codes = ref<Array<string | number>>([]);
// 当前选中的文件编号和名称集合
const docList = ref<any>({});

/** 查询打印件跟踪列表 */
const getList = async () => {
  loading.value = true;
  try {
    const res = await listManualTask(props.excelName, queryParams.value);
    dataList.value = res.rows;
    total.value = res.total;
  } finally {
    loading.value = false;
  }
};

/** 重置按钮操作 */
const resetQuery = () => {
  queryParams.value.pageNum = 1;
  getList();
};

/** 多选框选中数据 */
const handleSelectionChange = (selection: any[]) => {
  selectedRow.value = selection;
  codes.value = selection.map((item) => item.rowId);
  docList.value = {};
  selection.forEach(item => {
    docList.value[item.rowId] = item.docName;
  });
};

const recordData = ref([]);
const controllData = ref([]);
const getOption = () => {
  const query = {
    pageNum: 1,
    pageSize: 999999,
    includeManual: manual.value,
    range: props.activeName == 'takeEffect' ? 'active' : 'unActive'
  }
  listDocType(props.excelName, query).then(res => {
    recordData.value = res.data
  })
  listControlledInfo(props.excelName, query).then(res => {
    controllData.value = res.data
  })
};

// 对比不一致对话框
const showContrastDoc = ref(false);
const contrastTitle = ref('类型对比不一致');
const contrastDocList = ref<any[]>([]);

// 拒绝成册对话框
const showRejectDoc = ref(false);
const rejectTitle = ref('');
const warTitle = ref('');
const rejectDocList = ref<any>({});

// 成册
const completeVolume = async () => {
  if (!Object.keys(docList.value).length) {
    proxy?.$modal.msgWarning('请选择！');
    return;
  }
  // 校验所选文件 记录类型、申请部门、打印类型 是否一致
  if (selectedRow.value.length > 1) {
    let pass = true;
    const docType = selectedRow.value[0].docType;
    const applyDept = selectedRow.value[0].applyDept;
    const printType = selectedRow.value[0].printType;
    selectedRow.value.forEach(item => {
      if (item.docType != docType || item.applyDept != applyDept || item.printType != printType) {
        pass = false;
      }
    });
    if (!pass) {
      contrastDocList.value = selectedRow.value;
      showContrastDoc.value = true;
      return;
    }
  }
  // 提示消息
  await proxy?.$modal.confirm(`您确认要将选中的 ${selectedRow.value.length} 个文件进行成册吗？`);
  try {
    proxy?.$modal.loading('');
    buttonLoading.value = true;
    let method = props.activeName == 'takeEffect' ? acceptActive : acceptUnactive;
    const { data } = await method(codes.value);
    if (data && Object.keys(data).length) {
      // 校验失败
      const content = props.activeName == 'takeEffect' ? '非生效文件' : '非待生效文件';
      rejectTitle.value = '成册失败';
      warTitle.value = `您选择的以下文件为${content}：`;
      rejectDocList.value = data;
      showRejectDoc.value = true;
      setTimeout(() => {
        proxy?.$modal.msgWarning('成册失败');
      }, 500);
    } else {
      // 成册成功
      proxy?.$modal.msgSuccess('操作成功');
      resetDataList();
    }
  } finally {
    proxy?.$modal.closeLoading();
    buttonLoading.value = false;
  }
};

// 拒绝成册
const refuseVolume = async () => {
  if (!codes.value.length) {
    proxy?.$modal.msgWarning('请选择！');
    return;
  }
  // 提示消息
  await proxy?.$modal.confirm('确定拒绝该任务！');
  try {
    buttonLoading.value = true;
    await taskReject(codes.value);
    proxy?.$modal.msgSuccess('操作成功');
    resetDataList();
  } finally {
    buttonLoading.value = false;
  }
};

// 查询数据并清除选中状态
const resetDataList = () => {
  getList();
  getOption();
  // 清空选中数据
  codes.value = [];
  docList.value = {};
  selectedRow.value = [];
};

// 返回
const handleCancel = () => {
  emit('back');
};

// 初始化
onMounted(() => {
  getList();
  getOption();
})
</script>

<style scoped>
.title-clickable-item{
	font-weight: 700;
	color: #303133;
}
.title-clickable-item:hover{
	color: #409eff;
	cursor: pointer;
}
/* 固定在底部的按钮 */
.fixed-footer {
  display: flex;
  justify-content: center;
  padding-top: 8px;
}
:deep(.el-card__body){
  padding: 15px 20px 8px 20px !important;
}
</style>
