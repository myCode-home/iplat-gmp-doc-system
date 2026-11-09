<template>
  <div class="rule-group" v-loading="loading">
    <RuleItem
      ref="ruleGroupRef"
      v-for="(item, index) in ruleList"
      :key="item.dpsrSort+item.dsPermitSubjectRuleId"
      :info="info"
      :default="item"
      :jointVisible="index>0"
      :attrList="attrList"
      :connectors="connectors"
      @insert="handleInsertRow"
      @remove="handleRemoveRow($event,index)"
      @discard="handleDiscardRow(index)"
    >
    </RuleItem>
  </div>
</template>

<script lang="ts" name="RuleGroup" setup>
import {
  listSubjectRule, 
  getSubjectRule, 
  delSubjectRule, 
  addSubjectRule, 
  dynamicSubjectRule
} from '@/api/resourcepermission/dsPermit/rule';
const RuleItem = defineAsyncComponent(() => import('@/views/resourcepermission/dsPermit/ruleItem.vue'));
const props = defineProps({
  info: {
    type: Object,
    default: ()=> {}
  },
  connectors: {
    type: Array,
    default: ()=> []
  },
  attrList: {
    type: Array,
    default: ()=> []
  },
  data: {
    type: Array,
    default: ()=> []
  },
})
const { proxy } = getCurrentInstance() as ComponentInternalInstance;
const loading = ref(false);
const ruleList = ref([]);
const emit = defineEmits(['updated','refresh']);
const ruleGroupRef = ref([]);

const ruleForm = ref({
  dsPermitSubjectRuleId: '',
  dsPermitSubjectId: props.info.dsPermitSubjectId,
  dsPermitId: props.info.dsPermitId,
  businessId: props.info.businessId,
  businessAttrId: props.attrList[0] ? props.attrList[0]?.businessAttrId : '',
  dpsrAttrPrefix: '',
  dpsrAttrSuffix: '',
  dpsrConnector: 'like',
  dpsrValue: '',
  dpsrPrefix: '',
  dpsrSuffix: '',
  dpsrJoint: 'and',
  dpsrSort: 0,
})

watch(() => props.attrList, (val) => {
  if(val&&val.length){
    ruleForm.value.businessAttrId = val[0]?.businessAttrId;
  }
})

// 新增行
const handleInsertRow = () =>{
  const index = ruleList.value.length ? ruleList.value.length-1 : 1;
  const dpsrSort = ruleList.value.length ? ruleList.value[index].dpsrSort+1 : 1;
  ruleForm.value.dpsrSort = dpsrSort;
  ruleList.value.push(JSON.parse(JSON.stringify(ruleForm.value)));
}
// 删除行数据
const handleRemoveRow = (no, index) => {
  loading.value = true;
  delSubjectRule(no).then(_ => {
    handleDiscardRow(index);
    if(!ruleList.value.length){
      proxy.$nextTick(()=>{
        handleInsertRow();
      });
    }
  }).finally(() => {
      loading.value = false;
  });
};
// 丢弃行
const handleDiscardRow = (index) => {
  ruleList.value.splice(index, 1);
};
// 点击保存按钮操作
const saveRules = async () => {
  let arr = [];
  let success = true;
  for (let i = 0, len = ruleList.value.length; i < len; i++) {
    let rowData = await ruleGroupRef.value[i].getRowData();
    if (!rowData) {
      success = false;
      break;
    }
    arr.push(rowData);
  }
  if (success) {
    serverForModifyRules(arr);
  }
};
const serverForModifyRules = (data) => {
    loading.value = true;
    dynamicSubjectRule(data).then(({ data }) => {
      emit('refresh');
      proxy.$modal.msgSuccess('操作成功');
    }).finally(() => {
      loading.value = false;
    });
}

watch(() => props.data, (val) => {
  if(val&&val.length){
    ruleList.value = [...val]
  }else{
    if(!ruleList.value.length) {
      handleInsertRow();
    }
  }
},{ immediate: true})


// 数据初始化
const initData = () => {
  
};

onMounted(() => {
  initData();
})

defineExpose({
  saveRules
})
</script>