<template>
  <div class="">
    <el-tabs v-model="activeName" class="demo-tabs">
      <!-- 申请单 -->
      <el-tab-pane label="申请单" name="applyName" v-if="isShowApply">
        <from-apply ref="applyNameRef" :chose="props.choseRow" :operate="operate" v-model:buttonLoading="buttonLoadingModel" @closeDialog="emit('closeDialog')"></from-apply>
      </el-tab-pane>
      <!-- 手动创建 -->
      <el-tab-pane label="手动创建" name="handName" v-if="isShowHand">
        <from-hand ref="handNameRef" :chose="props.choseRow" :operate="operate" v-model:buttonLoading="buttonLoadingModel" @closeDialog="emit('closeDialog')"></from-hand>
      </el-tab-pane>
      <!-- 手动修订 -->
      <el-tab-pane label="手动修订" name="reviseName" v-if="isShowRevise">
        <from-revise ref="reviseNameRef" :chose="props.choseRow" :operate="operate" v-model:buttonLoading="buttonLoadingModel" @closeDialog="emit('closeDialog')"></from-revise>
      </el-tab-pane>
      <!-- 批量申请单 -->
      <el-tab-pane label="批量申请单" :chose="props.choseRow" :operate="operate" name="batchApplyName" v-if="isShowBatchApply">
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup lang="ts">
import { listDocCreateEdit, getDocCreateEdit, delDocCreateEdit, addDocCreateEdit, updateDocCreateEdit, submitAndFlowStart } from '@/api/dms/docCreate/docEdit/index';
import { useUserStore } from '@/store/modules/user';
import FromApply from '@/views/dms/docCreate/docEedit/from-apply.vue';
import FromHand from '@/views/dms/docCreate/docEedit/from-hand.vue';
import FromRevise from '@/views/dms/docCreate/docEedit/from-revise.vue';
const { proxy } = getCurrentInstance() as ComponentInternalInstance;
const props = defineProps({
  operate: {
    type: String,
    default: ''
  },
  generateDoc: {
    type: String,
    default: ''
  },
  showBtachApply: {
    type: String,
    default: ''
  },
  choseRow: {
    type: Object,
    default: () => {}
  },
  buttonLoading: {
    type: Boolean,
    default: false
  }
});
const emit = defineEmits(['closeDialog']);
const buttonLoadingModel = defineModel<boolean>('buttonLoading');

const activeName = ref('applyName')
// 判断是否隐藏Tab页
const isShowApply = ref(true);
const isShowHand = ref(true);
const isShowRevise = ref(true);
const isShowBatchApply = ref(true);
const applyNameRef = ref(null);
const handNameRef = ref(null);
const reviseNameRef = ref(null);
const row = ref({});
// 判断是否需要自动生成
watch(() => props.generateDoc, (val) => {
  if (val === 'true'){
    isShowApply.value = false;
    isShowHand.value = true;
    isShowRevise.value = true;
    activeName.value = 'handName';
  } else {
    isShowApply.value = true;
    isShowHand.value = true;
    isShowRevise.value = true;
    activeName.value = 'applyName';
  }
},{ immediate: true })

// 判断是否需要隐藏批量申请单
watch(() => props.showBtachApply, (val) => {
  if (val === 'true'){
    isShowBatchApply.value = true;
  } else {
    isShowBatchApply.value = false;
  }
},{ immediate: true })

// 选中的行数据
watch(() => props.choseRow, (val) => {
  if (props.operate === 'edit'&&val.operateSource) {
    switch (val.operateSource) {
    case 'fromInput':
      activeName.value = 'handName';
      isShowApply.value = false;
      isShowRevise.value = false;
      isShowBatchApply.value = false;
      break;
    case 'autoCreate':
    case 'fromApplyDoc':
      activeName.value = 'applyName';
      isShowHand.value = false;
      isShowRevise.value = false;
      isShowBatchApply.value = false;
      break;
    case 'manualRevision':
      activeName.value = 'reviseName';
      isShowApply.value = false;
      isShowHand.value = false;
      isShowBatchApply.value = false;
      break;
    case 'fromBatchReviseApply':
      activeName.value = 'batchApplyName';
      isShowApply.value = false;
      isShowHand.value = false;
      isShowRevise.value = false;
      break;
    case 'batchAutoCreate':
      activeName.value = 'batchApplyName';
      isShowApply.value = false;
      isShowHand.value = false;
      isShowRevise.value = false;
      break;
    }
  }
},{ immediate: true, deep: true })

const submitForm = () => {
  if (activeName.value === 'applyName') {
    applyNameRef.value.submitForm();
  } else if (activeName.value === 'handName') {
    handNameRef.value.submitForm();
  } else if (activeName.value === 'reviseName') {
    reviseNameRef.value.submitForm();
  }
}


defineExpose({
  submitForm
})


</script>


<style lang="scss" scoped>

</style>
