<template>
  <div class="condition-selector">
    <el-form ref="formRef" :model="localForm" :rules="rules" >
      <el-select v-model="localForm.relation" placeholder="请选择" style="width: 80px;margin-right: 30px;" v-show="index > 0">
        <el-option v-for="dict in relationOptions" :label="dict.label" :key="dict.value" :value="dict.value"></el-option>
      </el-select>  
      当
      <el-form-item label="" prop="field" style="display: inline-block;margin-bottom: 15px;">
        <el-select v-model="localForm.field" @change="fieldChange" filterable placeholder="请选择字段" :inline-message="true"
          style="width: 150px;">
          <el-option v-for="dict in attrList" :key="dict.pageConfigAttributeDesc" :label="dict.pageConfigAttributeName+`(${dict.pageConfigAttributeDesc})`"
            :value="dict.pageConfigAttributeDesc"></el-option>
        </el-select>
      
      </el-form-item>

      <el-select v-model="localForm.condition" filterable placeholder="请选择" class="noBorder" style="width: 120px;">
        <el-option v-for="dict in systemCondition" :label="dict.label" :key="dict.value"
          :value="dict.value"></el-option>
      </el-select>
      <el-form-item label="" prop="values">
        <el-select v-model="localForm.operator" @change="operatorChange" filterable placeholder="请选择" :disabled="isDisabled"
          style="margin-right: 5px;width: 15%;">
          <el-option v-for="dict in operatorOptions" :label="dict.label" :key="dict.value"
            :value="dict.value"></el-option>
        </el-select>
        <template v-if="localForm.operator == 'fixed'">
          <el-select v-model="localForm.values"
            v-if="(localFieldRow.fieldType == 'select' || localFieldRow.fieldType == 'checkBox' || localFieldRow.fieldType == 'radio')"
            :disabled="isDisabled" :placeholder="isDisabled ? '' : '请选择'" filterable class="valueClass">
            <el-option v-for="item in localDictData" :key="item.dictValue" :label="item.dictLabel+`(${item.dictValue})`"
              :value="item.dictValue" />
          </el-select>
          <el-input v-model="localForm.values" v-else :disabled="isDisabled" :placeholder="isDisabled ? '' : '请输入'" class="valueClass" />
        </template>
        <template v-else>
          <el-select v-model="localForm.values"
            :disabled="isDisabled" :placeholder="isDisabled ? '' : '请选择'" filterable  class="valueClass">
            <el-option v-for="dict in attrList" :key="dict.pageConfigAttributeDesc" :label="dict.pageConfigAttributeName"
              :value="dict.pageConfigAttributeDesc"></el-option>
          </el-select>
        </template>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup lang="ts">
import { listData } from '@/api/system/dict/data';
import type {  FormRules } from 'element-plus'
// 定义组件属性
const props = defineProps({
  modelValue: {
    type: Object,
    default: () => ({
      field: '',
      condition: '==',
      values: '',
      operator: 'fixed',
      relation: '&&'
    })
  },
  conditionLabel: {
    type: Object,
    default: () => ({
      field: '',
      condition: '等于',
      values: '',
      operator: 'fixed',
      relation: '且'
    })
  },
  attrList: {
    type: Array as any,
    default: () => []
  },
  systemCondition: {
    type: Array as any,
    default: () => []
  },
  index: {
    type: Number,
    default: 0
  }
});

const rules = reactive<FormRules<any>>({
  field: [{ required: true, message: '请选择', trigger: 'change' }],
  values: [{ required: true, message: '不能为空', trigger: 'change' }]
})

const emit = defineEmits(['update:modelValue']);
const conditionLabelCopy = defineModel('conditionLabel') as any;

// 表单引用
const formRef = ref<any>(null);

// 本地表单数据
const localForm = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
});

// 字段行数据
interface FieldRow {
  fieldType?: string;
  isDict?: boolean;
  dict?: string;
  [key: string]: any;
}

const localFieldRow = ref<FieldRow>({});

// 字典数据
const localDictData = ref([]);

// 操作符选项
const operatorOptions = [
  {
    label: '固定值',
    value: 'fixed'
  },
  {
    label: '动态值',
    value: 'dynamics'
  }
];

const relationOptions = [
  {
    label: '且',
    value: '&&'
  },
  {
    label: '或',
    value: '||'
  }
];

// 是否禁用
const isDisabled = computed(() => {
  if (localForm.value.condition == 'null' || localForm.value.condition == 'not null') {
    return true
  }
  return false
});

// 字段变更处理
const fieldChange = (val: any,keepValue: boolean = false) => {
  const pageConfigAttributeValue = (props.attrList as any).find((item: any) => item.pageConfigAttributeDesc == val)?.pageConfigAttributeValue;
  if (pageConfigAttributeValue) {
    localFieldRow.value = JSON.parse(pageConfigAttributeValue);
  } else {
    localFieldRow.value = {};
  }
  if(!keepValue) {
    localForm.value.values = '';
  }
  // 加载字典数据
  loadDictData();
};
// 加载字典数据
const loadDictData = async () => {
  const val = localFieldRow.value;
  if ((val.fieldType == 'select' || val.fieldType == 'checkBox' || val.fieldType == 'radio')) {
    // 如果是字典类型
    if ( val.isDict && val.dict){
      try {
        const res = await listData({ pageNum: 1, pageSize: 9999, dictName: '', dictType: val.dict, dictLabel: '' });
        localDictData.value = res.rows as any;
      } catch (err) {
        localDictData.value = [];
      }
    } else {
      // 从选项中获取
      localDictData.value = val.options.map(item=>{
        item.dictValue = item.value
        item.dictLabel = item.label
        return item
      })
    }
  } else {
    localDictData.value = [];
  }
};

const operatorChange = () =>{
  localForm.value.values = '';
};

// 暴露验证方法给父组件
defineExpose({
  validate: (callback: any) => {
    if (formRef.value) {
      return formRef.value.validate(callback);
    }
    return false;
  },
  resetFields: () => {
    if (formRef.value) {
      return formRef.value.resetFields();
    }
  },
  clearValidate: () => {
    if (formRef.value) {
      return formRef.value.clearValidate();
    }
  }
});

// 监听条件变更
watch(() => localForm.value.condition, (val) => {
  if (val == 'null' || val == 'not null') {
    localForm.value.operator = 'fixed';
    localForm.value.values = '';
    rules.values = [{ required: false, message: '不能为空', trigger: 'change' }];
  } else {
    rules.values = [{ required: true, message: '不能为空', trigger: 'change' }];
  }
  conditionLabelCopy.value.condition = props.systemCondition.find((item: any) => item.value == val)?.label;
}, { immediate: true });

// 监听连接符变化更新label
watch(() => localForm.value.relation, (val) => {
  conditionLabelCopy.value.relation = relationOptions.find((item: any) => item.value == val)?.label;
});
// 监听字段变化更新label
watch(() => localForm.value.field, (val) => {
  conditionLabelCopy.value.field = props.attrList.find((item: any) => item.pageConfigAttributeDesc == val)?.pageConfigAttributeName;
});
// 监听operator变化更新label
watch(() => localForm.value.operator, (val) => {
  conditionLabelCopy.value.operator = val;
});
// 监听值变化更新label
watch(() => localForm.value.values, (val) => {
  if(localForm.value.operator == 'fixed'&&(localFieldRow.value.fieldType == 'select' || localFieldRow.value.fieldType == 'checkBox' || localFieldRow.value.fieldType == 'radio')){
    conditionLabelCopy.value.values = localDictData.value.find((item: any) => item.dictValue == val)?.dictLabel;
  }else if(localForm.value.operator == 'dynamics'){
    conditionLabelCopy.value.values = props.attrList.find((item: any) => item.pageConfigAttributeDesc == val)?.pageConfigAttributeName;
  }else{
    conditionLabelCopy.value.values = val;
  }
});


onMounted(() => {
  fieldChange(localForm.value.field,true)

})
</script>

<style scoped lang="scss">
.noBorder {
  :deep(.el-select__wrapper) {
    box-shadow: 0 0 0 1px transparent inset;
  }
}
.valueClass {
  width: 500px;
}
</style>