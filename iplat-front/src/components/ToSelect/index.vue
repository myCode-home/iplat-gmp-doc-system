<template>
  <div>
    <el-dialog v-model="selectDialog.visible.value" :title="selectDialog.title.value" :before-close="handCloseDialog" :width="width||'90%'" append-to-body>
      <!-- 用户选择 -->
      <UserSelect ref="selectRef"
        v-if="selectType == 'userSelect'"
        :multipleChoice="multiple"
        :otherCondition="otherCondition"
        :multipleChoiceData="multipleChoiceData"
      ></UserSelect>
      <!-- 键值对选择 -->
      <DictSelect ref="selectRef"
        v-if="selectType == 'dictSelect'"
        :multipleChoice="multiple"
        :otherCondition="otherCondition"
        :multipleChoiceData="multipleChoiceData"
      ></DictSelect>
      <!-- 登陆人属性选择 -->
      <AttributeSelect ref="selectRef"
        v-if="selectType == 'attributeSelect'"
        :multipleChoice="multiple"
        :otherCondition="otherCondition"
        :multipleChoiceData="multipleChoiceData"
      ></AttributeSelect>
      <!-- 部门选择 -->
      <DeptSelect ref="selectRef"
        v-if="selectType == 'deptSelect'"
        :multipleChoice="multiple"
        :otherCondition="otherCondition"
        :multipleChoiceData="multipleChoiceData"
      ></DeptSelect>
      <!-- 用户组选择 -->
      <UserGroupSelect ref="selectRef"
        v-if="selectType == 'userGroupSelect'"
        :multipleChoice="multiple"
        :otherCondition="otherCondition"
        :multipleChoiceData="multipleChoiceData"
      ></UserGroupSelect>
      <!-- 角色选择 -->
      <RoleSelect ref="selectRef"
        v-if="selectType == 'roleSelect'"
        :multipleChoice="multiple"
        :otherCondition="otherCondition"
        :multipleChoiceData="multipleChoiceData"
      ></RoleSelect>
      <!-- 电子签名-url维护选择 -->
      <UriManagerSelect ref="selectRef"
        v-if="selectType == 'uriManagerSelect'"
        :multipleChoice="multiple"
        :otherCondition="otherCondition"
        :multipleChoiceData="multipleChoiceData"
      ></UriManagerSelect>
      <!-- 电子签名-电子签名定义选择 -->
      <SignatureDefineSelect ref="selectRef"
        v-if="selectType == 'SignatureDefineSelect'"
        :multipleChoice="multiple"
        :otherCondition="otherCondition"
        :multipleChoiceData="multipleChoiceData"
      ></SignatureDefineSelect>
      <!-- 文件选择 -->
      <FileSelect ref="selectRef"
        v-if="selectType == 'fileSelect'"
        :multipleChoice="multiple"
        :otherCondition="otherCondition"
        :multipleChoiceData="multipleChoiceData"
      ></FileSelect>
      <TempleteSelect ref="selectRef"
        v-if="selectType == 'templeteSelect'"
        :multipleChoice="multiple"
        :otherCondition="otherCondition"
        :multipleChoiceData="multipleChoiceData"
      ></TempleteSelect>
      <!-- 生效模板选择 -->
      <ActionTemplateSelect ref="selectRef"
      v-if="selectType == 'actionTemplateSelect'"
      :multipleChoice="multiple"
      :otherCondition="otherCondition"
      :multipleChoiceData="multipleChoiceData"
      ></ActionTemplateSelect>
      <!-- 生效模板(记录)选择 -->
      <ActionTemplateRecordSelect ref="selectRef"
      v-if="selectType == 'actionTemplateRecordSelect'"
      :multipleChoice="multiple"
      :otherCondition="otherCondition"
      :multipleChoiceData="multipleChoiceData"
      ></ActionTemplateRecordSelect>
      <!-- 生效文件库选择 -->
      <ActiveFileLibSelect ref="selectRef"
        v-if="selectType == 'activeFileLibSelect'"
        :multipleChoice="multiple"
        :otherCondition="otherCondition"
        :multipleChoiceData="multipleChoiceData"
      ></ActiveFileLibSelect>
      <!-- 生效记录库选择 -->
      <ActiveRecordLibrarySelect ref="selectRef"
        v-if="selectType == 'activeRecordLibrary'"
        :multipleChoice="multiple"
        :otherCondition="otherCondition"
        :multipleChoiceData="multipleChoiceData"
      ></ActiveRecordLibrarySelect>
      <!-- 文档创建编制页面查询审核通过的申请单选择 -->
      <OperateOrderSelect ref="selectRef"
        v-if="selectType == 'operateOrderSelect'"
        :multipleChoice="multiple"
        :otherCondition="otherCondition"
        :multipleChoiceData="multipleChoiceData"
      ></OperateOrderSelect>
      <!-- 成册打印待发放列表选择 -->
      <ToIssueListSelect ref="selectRef"
        v-if="selectType == 'toIssueListSelect'"
        :multipleChoice="multiple"
        :otherCondition="otherCondition"
        :multipleChoiceData="multipleChoiceData"
      ></ToIssueListSelect>
      <!-- 成册打印待回收列表选择 -->
      <ToRecycleListSelect ref="selectRef"
        v-if="selectType == 'toRecycleListSelect'"
        :multipleChoice="multiple"
        :otherCondition="otherCondition"
        :multipleChoiceData="multipleChoiceData"
      ></ToRecycleListSelect>
      <!-- 借阅申请生效文件选择 -->
      <LendActiveFileSelect ref="selectRef"
        v-if="selectType == 'lendActiveFileSelect'"
        :multipleChoice="multiple"
        :otherCondition="otherCondition"
        :multipleChoiceData="multipleChoiceData"
      ></LendActiveFileSelect>
      <template #footer>
        <div class="dialog-footer">
          <el-button :loading="buttonLoading" type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="handCloseDialog">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import useDialog from '@/hooks/useDialog';
const UserSelect = defineAsyncComponent(() => import('@/views/system/user/select.vue'));
const DictSelect = defineAsyncComponent(() => import('@/views/system/dict/select.vue'));
const AttributeSelect = defineAsyncComponent(() => import('@/views/system/user/attributeSelect.vue'));
const DeptSelect = defineAsyncComponent(() => import('@/views/system/dept/select.vue'));
const RoleSelect = defineAsyncComponent(() => import('@/views/system/role/selectRole.vue'));
const UserGroupSelect = defineAsyncComponent(() => import('@/views/system/userGroup/select.vue'));
const UriManagerSelect = defineAsyncComponent(() => import('@/views/system/electronicsignature/uriManager/select.vue'));
const SignatureDefineSelect = defineAsyncComponent(() => import('@/views/system/electronicsignature/signatureDefine/select.vue'));
const FileSelect = defineAsyncComponent(() => import('@/views/system/oss/select.vue'));
const TempleteSelect = defineAsyncComponent(() => import('@/views/system/templete/select.vue'));
const ActiveFileLibSelect = defineAsyncComponent(() => import('@/views/dms/fileLib/activeFileLib/select.vue'));
const ActionTemplateSelect = defineAsyncComponent(() => import('@/views/dms/templateManagement/templateActiveLibrary/select.vue'));
const ActionTemplateRecordSelect = defineAsyncComponent(() => import('@/views/dms/templateManagement/templateActiveLibrary/selectRecord.vue'));
const ActiveRecordLibrarySelect = defineAsyncComponent(() => import('@/views/dms/recordLibrary/activeRecordLibrary/select.vue'));
const OperateOrderSelect = defineAsyncComponent(() => import('@/views/dms/operateOrder/operateOrderFollow/select.vue'));
const ToIssueListSelect = defineAsyncComponent(() => import('@/views/dms/bookPrint/bookPrintScanIssue/toIssueList.vue'));
const ToRecycleListSelect = defineAsyncComponent(() => import('@/views/dms/bookPrint/bookPrintScanRecycle/toRecycleList.vue'));
const LendActiveFileSelect = defineAsyncComponent(() => import('@/views/dms/fileLendOrder/lendApply/activeFileSelect.vue'));
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

const selectRef = ref(null);
const emit = defineEmits(['dialogClose', 'confirmCallBack']);

const router = useRouter();
const { proxy } = getCurrentInstance() as ComponentInternalInstance;
const { sys_normal_disable } = toRefs<any>(proxy?.useDict('sys_normal_disable'));

const selectDialog = useDialog({
  title: props.title,
});

// 确认按钮操作
const submitForm = () => {
  selectRef.value.confirmChoiceData();
  // console.log(selectRef.value)
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