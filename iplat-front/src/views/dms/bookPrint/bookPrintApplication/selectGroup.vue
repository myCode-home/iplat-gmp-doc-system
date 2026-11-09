<template>
  <div>
    <el-dialog v-model="selectDialog.visible.value" :title="selectDialog.title.value" :before-close="handCloseDialog" :width="width||'90%'" append-to-body>
      <!-- 生效文件选择 -->
      <FileSelect ref="fileSelectRef"
        :multipleChoice="multiple"
        :otherCondition="otherCondition"
        :multipleChoiceData="multipleChoiceData"
        :activeName="activeName"
      ></FileSelect>
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
const FileSelect = defineAsyncComponent(() => import('@/views/dms/bookPrint/bookPrintApplication/fileSelect.vue'));
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
  activeName: {
    type: String,
    default: 'takeEffect'
  }
})

const fileSelectRef = ref(null);
const emit = defineEmits(['dialogClose', 'confirmCallBack']);

const { proxy } = getCurrentInstance() as ComponentInternalInstance;

const selectDialog = useDialog({
  title: props.title,
});

// 确认按钮操作
const submitForm = () => {
  fileSelectRef.value.confirmChoiceData();
};

// 确认按钮操作
const multipleChoiceData = (data) => {
  props.achieveData(data)
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
