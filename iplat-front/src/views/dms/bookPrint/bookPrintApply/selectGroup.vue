<template>
  <div>
    <el-dialog v-model="selectDialog.visible.value" :title="selectDialog.title.value" :before-close="handCloseDialog" :width="width||'90%'" append-to-body>
      <el-tabs v-model="activeName">
        <el-tab-pane label="生效文件" name="document">
          <!-- 生效文件选择 -->
          <ActiveFileLibSelect ref="documentRef"
            :multipleChoice="multiple"
            :otherCondition="otherCondition"
            :multipleChoiceData="multipleChoiceData"
          ></ActiveFileLibSelect>
        </el-tab-pane>
        <el-tab-pane label="未生效文件" name="notEffectiveFile">
          <!-- 未生效文件选择 -->
          <NotEffectiveFileSelect ref="notEffectiveFileRef"
            :multipleChoice="multiple"
            :otherCondition="otherCondition"
            :multipleChoiceData="multipleChoiceData"
          ></NotEffectiveFileSelect>
        </el-tab-pane>
        <el-tab-pane label="失效文件" name="InvalidFile">
          <!-- 未生效文件选择 -->
          <InvalidFileSelect ref="InvalidFileRef"
            :multipleChoice="multiple"
            :otherCondition="otherCondition"
            :multipleChoiceData="multipleChoiceData"
          ></InvalidFileSelect>
        </el-tab-pane>
      </el-tabs>
      <template #footer>
        <div class="dialog-footer">
          <el-button :loading="buttonLoading" type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="handCloseDialog">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script name="SignatureGroup" setup lang="ts">
import useDialog from '@/hooks/useDialog';
const ActiveFileLibSelect = defineAsyncComponent(() => import('@/views/dms/fileLib/activeFileLib/activeFileSelect.vue'));
const NotEffectiveFileSelect = defineAsyncComponent(() => import('@/views/dms/fileLib/notActiveFileLib/notEffectiveFileSelect.vue'));
const InvalidFileSelect = defineAsyncComponent(() => import('@/views/dms/fileLib/invalidFileLib/invalidFileSelect.vue'));
const props = defineProps({
  width: {
    type: String,
    default: ''
  },
  title: {
    type: String,
    default: '请选择'
  },
  selectType: {
    type: String,
    default: ''
  },
  multiple: {
    type: Boolean,
    default: false
  },
  otherCondition: {
    type: Object,
    default: () => ({})
  },
  buttonLoading: {
    type: Boolean,
    default: false
  },
  achieveData: {
    type: Function,
    default: () => () =>{}
  },
})

const activeName = ref('document');
const documentRef = ref(null);
const notEffectiveFileRef = ref(null);
const InvalidFileRef = ref(null);
const emit = defineEmits(['dialogClose', 'confirmCallBack']);

const router = useRouter();
const { proxy } = getCurrentInstance() as ComponentInternalInstance;
const { sys_normal_disable } = toRefs<any>(proxy?.useDict('sys_normal_disable'));

const selectDialog = useDialog({
  title: props.title,
});

// 确认按钮操作
const submitForm = () => {
  if(activeName.value == 'document'){
    documentRef.value.confirmChoiceData();
  }else if(activeName.value == 'notEffectiveFile'){
    notEffectiveFileRef.value.confirmChoiceData();
  }else if(activeName.value == 'InvalidFile'){
    InvalidFileRef.value.confirmChoiceData();
  }
};

// 确认按钮操作
const multipleChoiceData = (data) => {
  props.achieveData(data, activeName.value)
  handCloseDialog()
};

// 关闭按钮操作
const handCloseDialog = () => {
  selectDialog.closeDialog();
  emit('dialogClose');
};

onMounted(() => {
  selectDialog.openDialog();
});

defineExpose({
  open: selectDialog.openDialog,
  close: selectDialog.closeDialog
});
</script>
