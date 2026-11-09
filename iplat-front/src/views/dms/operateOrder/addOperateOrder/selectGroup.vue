<template>
  <div>
    <el-dialog v-model="selectDialog.visible.value" :title="selectDialog.title.value" :before-close="handCloseDialog"
      :width="width || '90%'" append-to-body>
      <el-tabs v-model="activeName">
        <el-tab-pane :label="!isShowTemplate ? '全部记录' : '模板'" name="template">
          <!-- 生效模板选择 -->
          <ActionTemplate v-if="isShowTemplate" ref="templateRef" :multipleChoice="multiple"
            :otherCondition="{}" :multipleChoiceData="multipleChoiceData">
          </ActionTemplate>
          <ActiveRecordLibrary v-else ref="templateRef" :multipleChoice="multiple" :otherCondition="otherCondition"
            :multipleChoiceData="multipleChoiceData">
          </ActiveRecordLibrary>
        </el-tab-pane>
        <el-tab-pane :label="!isShowTemplate ? '全部文件' : '文件'" name="document">
          <!-- 生效文件选择 -->
          <ActiveFileLibSelect :isShoueCodePlus="isShoueCodePlus" ref="documentRef" :multipleChoice="multiple" :otherCondition="otherCondition"
            :multipleChoiceData="multipleChoiceData"></ActiveFileLibSelect>
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
import { toRefs } from 'vue';
const ActionTemplate = defineAsyncComponent(() => import('@/views/dms/templateManagement/templateActiveLibrary/select.vue'));
const ActiveFileLibSelect = defineAsyncComponent(() => import('@/views/dms/fileLib/activeFileLib/select.vue'));
const ActiveRecordLibrary = defineAsyncComponent(() => import('@/views/dms/recordLibrary/activeRecordLibrary/select.vue'));
const UserSelect = defineAsyncComponent(() => import('@/views/system/user/select.vue'));
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
    default: () => () => { }
  },
  // 显示记录还是模板
  isShowTemplate: {
    type: Boolean,
    default: true
  },
  // 是否使用code-plus的search
  isShoueCodePlus: {
    type: Boolean,
    default: false
  }
});

// 解构props，确保模板中可以正确访问
const { isShowTemplate, isShoueCodePlus, multiple, otherCondition, buttonLoading } = toRefs(props);
const activeName = ref('template');
const templateRef = ref(null);
const documentRef = ref(null);
const emit = defineEmits(['dialogClose', 'confirmCallBack']);

const router = useRouter();
const { proxy } = getCurrentInstance() as ComponentInternalInstance;
const { sys_normal_disable } = toRefs<any>(proxy?.useDict('sys_normal_disable'));

const selectDialog = useDialog({
  title: props.title,
});

// 确认按钮操作
const submitForm = () => {
  if (activeName.value == 'template') {
    templateRef.value.confirmChoiceData();
    // // 判断isShowTemplate是否为true,判断是模板还是记录
    // if (isShowTemplate.value) {

    // }
  } else if (activeName.value == 'document') {
    documentRef.value.confirmChoiceData();
  }
};

// 确认按钮操作
const multipleChoiceData = (data) => {
  props.achieveData(data, activeName.value)
  console.log('选中了数据', data);

  // handCloseDialog()
};

// 关闭按钮操作
const handCloseDialog = () => {
  selectDialog.closeDialog();
  emit('dialogClose');
};

onMounted(() => {
  selectDialog.openDialog();
  // if(props.otherCondition?.type){
  //   activeName.value = props.otherCondition?.type;
  // }
});

defineExpose({
  open: selectDialog.openDialog,
  close: selectDialog.closeDialog
});
</script>
