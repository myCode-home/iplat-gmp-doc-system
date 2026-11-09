<template>
  <div>
    <el-dialog v-model="selectDialog.visible.value" :title="selectDialog.title.value" :before-close="handCloseDialog" :width="width||'90%'" append-to-body>
      <el-tabs v-model="activeName">
        <el-tab-pane label="角色" name="ROLE">
          <!-- 角色选择 -->
          <RoleSelect ref="ROLERef"
            :multipleChoice="multiple"
            :otherCondition="otherCondition"
            :multipleChoiceData="multipleChoiceData"
          ></RoleSelect>
        </el-tab-pane>
        <el-tab-pane label="用户组" name="USERGROUP">
          <!-- 用户组选择 -->
          <UserGroupSelect ref="USERGROUPRef"
            :multipleChoice="multiple"
            :otherCondition="otherCondition"
            :multipleChoiceData="multipleChoiceData"
          ></UserGroupSelect>
        </el-tab-pane>
        <el-tab-pane label="用户" name="USER">
          <UserSelect ref="USERRef"
            :multipleChoice="multiple"
            :otherCondition="otherCondition"
            :multipleChoiceData="multipleChoiceData"
          ></UserSelect>
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
const RoleSelect = defineAsyncComponent(() => import('@/views/system/role/selectRole.vue'));
const UserGroupSelect = defineAsyncComponent(() => import('@/views/system/userGroup/select.vue'));
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
    default: () => () =>{}
  },
})

const activeName = ref('USER');
const ROLERef = ref(null);
const USERGROUPRef = ref(null);
const USERRef = ref(null);
const emit = defineEmits(['dialogClose', 'confirmCallBack']);

const router = useRouter();
const { proxy } = getCurrentInstance() as ComponentInternalInstance;
const { sys_normal_disable } = toRefs<any>(proxy?.useDict('sys_normal_disable'));

const selectDialog = useDialog({
  title: props.title,
});

// 确认按钮操作
const submitForm = () => {
  if(activeName.value == 'ROLE'){
    ROLERef.value.confirmChoiceData();
  }else if(activeName.value == 'USERGROUP'){
    USERGROUPRef.value.confirmChoiceData();
  }else if(activeName.value == 'USER'){
    USERRef.value.confirmChoiceData();
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
  if(props.otherCondition?.type){
    activeName.value = props.otherCondition?.type;
  }
});

defineExpose({
  open: selectDialog.openDialog,
  close: selectDialog.closeDialog
});
</script>
