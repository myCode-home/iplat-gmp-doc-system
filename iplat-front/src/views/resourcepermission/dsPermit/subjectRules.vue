<template>
  <div class="ruleDiv">
    <tab-transfer
      v-if="showTransfer"
      @updated="emit('updated')"
      :userList="userList"
      :roleList="roleList"
      :deptList="deptList"
      :info="info"
    >
    </tab-transfer>
    <rule-group
      v-if="info.dsPermitSubjectId"
      ref="ruleGroupRef"
      :info="info"
      :attrList="attrList"
      @refresh="getConfigRules"
      :connectors="system_subjects_hyphen"
      :data="rules"
    >
    </rule-group>
  </div>
</template>

<script lang="ts" name="SubjectRuleRules" setup>
import {
  listSubjectRule, 
  getSubjectRule, 
  delSubjectRule, 
  addSubjectRule, 
  updateSubjectRule
} from '@/api/resourcepermission/dsPermit/rule';
const TabTransfer = defineAsyncComponent(() => import('@/views/resourcepermission/dsPermit/tabTransfer.vue'));
const RuleGroup = defineAsyncComponent(() => import('@/views/resourcepermission/dsPermit/ruleGroup.vue'));
const props = defineProps({
  showTransfer: {
    type: Boolean,
    default: false
  },
  userList: {
    type: Array,
    default: ()=> []
  },
  roleList: {
    type: Array,
    default: ()=> []
  },
  deptList: {
    type: Array,
    default: ()=> []
  },
  attrList: {
    type: Array,
    default: ()=> []
  },
  info: {
    type: Object,
    default: ()=> {}
  },
})
const { proxy } = getCurrentInstance() as ComponentInternalInstance;
const { system_subjects_hyphen } = toRefs<any>(proxy.useDict('system_subjects_hyphen'));
const emit = defineEmits(['updated']);
const loading = ref(false);
const ruleGroupRef = ref(null);
const rules = ref([]);

// 获取属性配置规则
const getConfigRules = () => {
  if (!props.info.dsPermitSubjectId) return;
  const query = {
    pageNum: 1,
    pageSize: 999,
    dsPermitSubjectId: props.info.dsPermitSubjectId,
  };
  loading.value = true;
  listSubjectRule(query).then(({ rows }) => {
    rules.value = rows || [];
    loading.value = false;
  });
};
watch(()=> props.info.dsPermitSubjectId, () => {
  getConfigRules();
})

// 保存按钮操作
const saveRules = () => {
  ruleGroupRef.value.saveRules();
}

onMounted(() => {
  getConfigRules();
})
defineExpose({
  saveRules
})
</script>

<style lang="scss" scoped>
.ruleDiv{
  display: flex;
}
</style>