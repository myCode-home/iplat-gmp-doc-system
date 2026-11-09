<template>
  <el-form :model="formData" v-loading="formLoading||loading" :rules="formRules" :label-position="labelPosition||'right'" :size="size" ref="formRef" class="bgForm" label-width="auto">
    <template v-for="formObj in visibleFormItems">
      <template v-if="formObj.type">
        <el-card class="box-card">
          <template #header>
            <div class="card-header clearfix">
              <span>{{formObj.type}}</span>
            </div>
          </template>
          <el-row :gutter="20">
            <template v-for="item in formObj.data" :key="item.prop">
              <el-col :span="item.span||defaultSpan">
                <!-- 整个表单项插槽 -->
                <slot v-if="item.isSlot&&item.slotName==`${item.prop}Form`" :name="`${item.prop}Form`" v-bind="arrts(item)"
                  :otherCondition="otherCondition"
                  :readonly="fieldStates[item.prop].readonly"
                  :disabled="fieldStates[item.prop].disabled">
                </slot>
                <el-form-item
                  v-else
                  :label="proxy?.$tr(item.label)"
                  :prop="item.prop"
                  :rules="getFieldRules(item)"
                >
                  <slot v-if="item.isSlot" :name="item.slotName" v-bind="arrts(item)"
                    :otherCondition="otherCondition"
                    :readonly="fieldStates[item.prop].readonly"
                    :disabled="fieldStates[item.prop].disabled">
                  </slot>
                  <component
                    v-else
                    style="width: 100%;"
                    v-on="getEventHandlers(item)"
                    @selectDataAchieve="selectDataAchieve($event, item)"
                    :is="getComponent(item.fieldType)"
                    v-model="formData[item.prop]"
                    v-bind="arrts(item)"
                    :otherCondition="otherCondition"
                    :readonly="fieldStates[item.prop].readonly"
                    :disabled="fieldStates[item.prop].disabled"
                  />
                </el-form-item>
              </el-col>
            </template>
          </el-row>
        </el-card>
      </template>
      <template v-else>
        <el-row :gutter="20">
          <template v-for="item in formObj.data" :key="item.prop">
            <el-col :span="item.span||defaultSpan">
              <!-- 整个表单项插槽 -->
              <slot v-if="item.isSlot&&item.slotName==`${item.prop}Form`" :name="`${item.prop}Form`" v-bind="arrts(item)"
                :otherCondition="otherCondition"
                :readonly="fieldStates[item.prop].readonly"
                :disabled="fieldStates[item.prop].disabled">
              </slot>
              <el-form-item
                v-else
                :label="proxy?.$tr(item.label)"
                :prop="item.prop"
                :rules="getFieldRules(item)"
              >
                <slot
                  v-if="item.isSlot"
                  :name="item.slotName"
                  v-bind="arrts(item)"
                  :otherCondition="otherCondition"
                  :readonly="fieldStates[item.prop].readonly"
                  :disabled="fieldStates[item.prop].disabled">
                </slot>
                <component
                  v-else
                  style="width: 100%;"
                  v-on="getEventHandlers(item)"
                  @selectDataAchieve="selectDataAchieve($event, item)"
                  :is="getComponent(item.fieldType)"
                  v-model="formData[item.prop]"
                  v-bind="arrts(item)"
                  :otherCondition="otherCondition"
                  :readonly="fieldStates[item.prop].readonly"
                  :disabled="fieldStates[item.prop].disabled"
                />
              </el-form-item>
            </el-col>
          </template>
        </el-row>
      </template>
    </template>
    <slot></slot>
  </el-form>
</template>

<script setup lang="ts">
import { ElInput, ElSelect, ElInputNumber,ElDatePicker,ElTimeSelect,ElTimePicker } from 'element-plus'
// import { getPageConfigPage } from '@/api/system/pageConfig';
import { InteractionManager } from '@/components/SuperForm/interactionManager.js'
import DataChoose from '@/components/SuperForm/component/dataChoose.vue'
import DictChoose from '@/components/SuperForm/component/dictChoose.vue'
import DepartmentSelect from '@/components/SuperForm/component/departmentSelect.vue'
import ProvinceSelect from '@/components/SuperForm/component/provinceSelect.vue'
import FileTypeSelect from '@/components/SuperForm/component/fileTypeSelect.vue'

import {isNumeric} from '@/utils/index'
import { usePageConfigStore } from '@/store/modules/pageConfig'

const { proxy } = getCurrentInstance() as ComponentInternalInstance;

const props = defineProps({
  // 双向绑定值（必传）
  modelValue: {
    type: Object,
    required: true,
    default: () => ({}),
  },
  code: {
    type: String,
    required: true
  },
  loading: {
    type: Boolean,
    default: false
  },
  methods: {
    type: Object,
    default: () => ({})
  },
  // 数据筛选其他条件
  otherCondition: {
    type: Object,
    default: () => ({}),
  }
})
const getComponent = (item) => {
  // console.log('getComponent', item)
  if(item == 'input'){
    return ElInput
  } else if(item == 'dataChoose'){
    return DataChoose
  } else if(['select','radio','checkBox'].includes(item)){
    return DictChoose
  } else if(item == 'datePicker' ){
    return ElDatePicker
  } else if(item == 'timeSelect' ){
    return ElTimeSelect
  } else if(item == 'timePicker' ){
    return ElTimePicker
  } else if(item == 'departmentSelect' ){
    return DepartmentSelect
  } else if(item == 'province'){
    return ProvinceSelect
  } else if(item == 'fileTypeSelect'){
    return FileTypeSelect
  }
  return item
}
// 定义Emits：支持v-model双向绑定 + 事件透传
const emit = defineEmits(['update:modelValue', 'change', 'selectData']);
// 表单状态
const formData = computed(() => props.modelValue)
const formRules = reactive({})
const formRef = ref(null)
const fieldStates = reactive({})
const interactionManager = new InteractionManager(formData.value, fieldStates, props.methods)
const attributeList = ref([]);
const ruleList = ref([]);
const defaultSpan = ref(12);
const labelPosition = ref<'right' | 'top' | 'left'>('right');
const size = ref<"" | "default" | "small" | "large">('small');

const formLoading = ref(false)
// 获取页面配置
const getConfig = async () => {
  formLoading.value = true;
  usePageConfigStore().getPageConfig(props.code).then((data: any) => {
    // 设置表单项占比
    if(data.pageConfigDisplay){
      defaultSpan.value = 24 / Number(data.pageConfigDisplay||2)
    }
    labelPosition.value = data.pageConfigAlign || 'right'
    size.value = data.pageConfigSize || 'small'
    if(data&&data.attributeList) {
      attributeList.value = data.attributeList.map((item: any)=>{
        item.pageConfigAttributeValue = item.pageConfigAttributeValue ? JSON.parse(item.pageConfigAttributeValue) : {}
        return { ...item, ...item.pageConfigAttributeValue,
          // events: {
          //   "change": "handleRoleChange"
          // }
        }
      }).sort((a,b)=>a.pageConfigAttributeSort-b.pageConfigAttributeSort);
      initForm();
    }else{
      proxy?.$modal.msgError('未查询到表单配置，请联系管理员进行配置！')
    }
    // 过滤掉未启用的规则
    ruleList.value = data.ruleList ? data.ruleList.filter(val=>val.pageConfigRuleEnable == '1') : [];
    initRules();
  }).finally(()=>{
    formLoading.value = false;
    interactionManager.applyAllRules(formRef.value);
    emit('update:modelValue', formData.value); // 触发v-model更新
  })
  // console.log(usePageConfigStore().getPageConfig(props.code))
}

// 初始化表单
const initForm = () => {
  // 初始化字段数据和状态
  attributeList.value.forEach(item => {
    if(item.multiple){
      if(formData.value[item.prop]){
        formData.value[item.prop] = formData.value[item.prop].split(',')
      }else{
        formData.value[item.prop] = []
      }
    }else{
      formData.value[item.prop] = formData.value[item.prop] || item.defaultValue || ''
    }
    fieldStates[item.prop] = {
      visible: item.isEnable=='1' ? true: false,
      required: item.required ?? false,
      readonly: item.readonly ?? false,
      disabled: item.disabled ?? false
    }
    if (item.rules) formRules[item.prop] = item.rules.map(item=>{
      if(item.pattern){
        // 去除可能存在的首尾斜杠
        const pattern = item.pattern.replace(/^\/|\/$/g, '');
        item.pattern = new RegExp(pattern || '')
      }
      return item
    })
  })
};

// 格式化表单数据
const formatForm = ()=>{
  attributeList.value.forEach(item=>{
    if(item.multiple){
      if(formData.value[item.prop]&&typeof formData.value[item.prop] === 'string'){
        formData.value[item.prop] = formData.value[item.prop].split(',')
      }else{
        formData.value[item.prop] = []
      }
    }
  })
};

// 初始化交互规则
const initRules = () =>{
  // 添加交互规则
  if (ruleList.value.length) {
    ruleList.value.sort((a,b)=>a.pageConfigRuleSort-b.pageConfigRuleSort);
    const interactionRules = ruleList.value.map(item => {
      item.name = item.pageConfigRuleName
      if(item.pageConfigRuleCondition){
        const pageConfigRuleCondition = JSON.parse(item.pageConfigRuleCondition)
        pageConfigRuleCondition.forEach((conditionItem,index)=>{
          const { field, condition, values, operator ,relation } = conditionItem
          let tempStr = ''
          if(condition == 'null'){
            tempStr =  `!${field}`
          }else if(condition == 'not null'){
            tempStr =  `${field}`
          }else if(operator == 'fixed'){
            const valuesStr = isNumeric(values) ? values : `'${values}'`
            // 如果条件值是不等于，未填值也可以触发验证
            if(condition == '!=') {
              tempStr =  `!${field} || ${field} ${condition} ${valuesStr}`
            } else {
              tempStr =  `${field} && ${field} ${condition} ${valuesStr}`
            }
          }else if(operator == 'dynamics'){
            if(condition == '!=') {
              tempStr =  `!${field} || (${values} && ${field} ${condition} ${values})`
            } else {
              tempStr =  `${field} && ${values} && ${field} ${condition} ${values}`
            }
          }
          tempStr = `(${tempStr})`
          if(index > 0){
            item.condition = `${item.condition} ${relation} ${tempStr}`
          }else{
            item.condition = tempStr
          }
        })


      }
      if(item.pageConfigRuleAction){
        item.actions =  JSON.parse(item.pageConfigRuleAction)
      }
      return item
    })
    interactionManager.addRules(interactionRules)
  }
};
// 格式化绑定内容
const arrts = computed(() => {
  return (item : any) => {
    if(!item.placeholder){
      if(item.fieldType == 'input'){
        item.placeholder = '请输入' + item.label
      }else{
        item.placeholder = '请选择' + item.label
      }
    }
    return item
  }
});

const selectDataAchieve = (data, item) => {
  // 处理dataChoose组件返回值,更新form表单内容
  if(item.fieldType == 'dataChoose') {
   formData.value[item.prop] = data
  }
  emit('selectData', data, item)
};

// 在父组件中预定义所有可能用到的方法
const formMethods = {
  // args参数集合 childFrom参数集合
  customChangeHandler: (args: Array<any>,childFrom: Object) => {
    updateData({ companyName: 'admin' })
    console.log('自定义change事件触发')
  },
  handleRoleChange: (args: Array<any>,childFrom: Object) => {
    console.log('自定义事件触发1', args, childFrom)
  },
  handleSexChange: (args: Array<any>,childFrom: Object) => {
    console.log('自定义事件触发2', args)
  }
}

// 动态绑定事件处理器
const getEventHandlers = (item:any) => {
  if (!item.events||item.events.length == 0) return {}
  const handlers = {}
  item.events.forEach(({ eventName, methodName }) => {
    handlers[eventName] = (...args: any[]) => {
      const func = props.methods?.[methodName]
      if (func) {
        func(args, {...formData.value})
      } else {
        console.warn(`未定义的方法: ${methodName}`)
      }
    }
  })
  return handlers
}

// 计算可见的表单项
const visibleFormItems = computed(() => {
  const visibleProp = attributeList.value.filter(item => fieldStates[item.prop]?.visible)
  return groupAdjacent(visibleProp)
})

// 合并系统区域字段
const groupAdjacent = (data) => {
  if (data.length === 0) return [];
  const result = [];
  let currentGroup = {
    type: data[0].region||'',
    data: [data[0]]
  };
  for (let i = 1; i < data.length; i++) {
    // 防止所属区域为空或者undefined导致排列出错
    const region = data[i].region||''
    if (region === currentGroup.type) {
      currentGroup.data.push(data[i]);
    } else {
      result.push(currentGroup);
      currentGroup = {
        type: region,
        data: [data[i]]
      };
    }
  }
  result.push(currentGroup);
  return result;
}

// 获取组件props
const getComponentProps = (item) => {
  const baseProps = {
    placeholder: item.placeholder,
    ...(item.props || {})
  }

  // 处理select的options
  if (item.fieldType === 'select' && item.options) {
    baseProps.options = item.options
  }

  return baseProps
}

// 获取字段验证规则
const getFieldRules = (item) => {
  // 复制原始规则（不包含必填规则）
  const baseRules = (item.rules || []).filter(rule => !rule.required)
  // 动态添加必填规则
  if (fieldStates[item.prop]?.required) {
    return [
      ...baseRules,
      {
        required: true,
        message: `${proxy?.$tr(item.label)}不能为空`,
        trigger: ['blur', 'change']
      }
    ]
  }
  return baseRules
}

// 监听字段必填状态变化
watch(() => attributeList.value.map(item => fieldStates[item.prop]?.required),
  () => {
    // 必填状态变化时强制更新验证
    nextTick(() => {
      formRef.value?.clearValidate()
    })
  },
  { deep: true }
)

// 监听表单数据变化
watch(formData.value, () => {
  interactionManager.applyAllRules(formRef.value)
  emit('update:modelValue', formData.value); // 触发v-model更新
}, { deep: true })

// 初始化
onMounted(() => {
  getConfig();
})

// 暴露给父组件的方法,用于父组件修改表单数据
const updateData = (newData) => {
  Object.keys(newData).forEach(key => {
    formData.value[key] = newData[key]
  })
}
const achieveData = () =>{
  const formDataCopy = JSON.parse(JSON.stringify(formData.value))
  Object.keys(formData.value).forEach(key=>{
    attributeList.value.forEach(item=>{
      if(item.prop == key&&item.multiple){
        if(formData.value[key]&&formData.value[key].length){
          formDataCopy[key] = formData.value[key].join(',')
        }else{
          formDataCopy[key] = ''
        }
      }
    })
  })
  return formDataCopy
  // if(Object.keys(formData.value).length){
  //   Object.keys(formData.value).forEach(key=>{

  //   })
  // }else{
    // return {...formData.value}
  // }
}
// 暴露方法
defineExpose({
  updateData,
  getFormData: () => formData.value,
  validate: (callBack) => formRef.value?.validate((valid) => callBack(valid)),
  achieveData,
  formatForm,
  applyRules: () => interactionManager.applyAllRules(formRef.value)
})
</script>

<style lang="scss">
// .bgForm{
//   .el-form-item--default{
//     background: #F2F6F7;
//     padding: 1px 1px 1px 0;
//   }
//   .el-form-item.is-error .el-input-tag__wrapper, .el-form-item.is-error .el-input-tag__wrapper.is-focus, .el-form-item.is-error .el-input-tag__wrapper:focus, .el-form-item.is-error .el-input-tag__wrapper:hover, .el-form-item.is-error .el-input__wrapper, .el-form-item.is-error .el-input__wrapper.is-focus, .el-form-item.is-error .el-input__wrapper:focus, .el-form-item.is-error .el-input__wrapper:hover, .el-form-item.is-error .el-select__wrapper, .el-form-item.is-error .el-select__wrapper.is-focus, .el-form-item.is-error .el-select__wrapper:focus, .el-form-item.is-error .el-select__wrapper:hover, .el-form-item.is-error .el-textarea__inner, .el-form-item.is-error .el-textarea__inner.is-focus, .el-form-item.is-error .el-textarea__inner:focus, .el-form-item.is-error .el-textarea__inner:hover{
//     box-shadow: none;
//   }
//   .el-input__wrapper,.el-textarea__inner,.el-select__wrapper{
//     box-shadow: none;
//   }
// }
</style>

<style lang="scss" scoped>
.box-card{
  margin: 10px 0;
  .clearfix {
    text-align: center;
    font-size: 16px;
    color: #1890ff;
    font-weight: bold;
  }
}
</style>
