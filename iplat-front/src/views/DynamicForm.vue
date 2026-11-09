<template>
  <el-form :model="formData" :rules="formRules" ref="formRef" label-width="90px">
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
              <el-col :span="props.config.span||12">
                <el-form-item
                  :label="proxy?.$tr(item.label)"
                  :prop="item.prop"
                  :rules="getFieldRules(item)"
                >
                  <component
                    v-on="getEventHandlers(item)"
                    :is="getComponent(item.type)"
                    v-model="formData[item.prop]"
                    v-bind="item"
                    :readonly="fieldStates[item.prop].readonly"
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
            <el-col :span="props.config.span||12">
              <el-form-item
                :label="proxy?.$tr(item.label)"
                :prop="item.prop"
                :rules="getFieldRules(item)"
              >
                <component
                  v-on="getEventHandlers(item)"
                  :is="getComponent(item.type)"
                  v-model="formData[item.prop]"
                  v-bind="item"
                  :readonly="fieldStates[item.prop].readonly"
                />
              </el-form-item>
            </el-col>
          </template>
        </el-row>
      </template>
    </template>
  </el-form>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch, onMounted } from 'vue'
import { InteractionManager } from './interactionManager'
import select from './components/select.vue'
import input from './components/input.vue'
const { proxy } = getCurrentInstance() as ComponentInternalInstance;


const props = defineProps({
  config: {
    type: Object,
    required: true
  },
  methods: {
    type: Object,
    default: () => ({})
  }
})
const getComponent = (item) => {
  if(item == 'input'){
    return input
  }else if(item == 'select'){
    return select
  }
  return item
}
// 表单状态
const formData = reactive({})
const formRules = reactive({})
const formRef = ref(null)
const fieldStates = reactive({})
const interactionManager = new InteractionManager(formData, fieldStates, props.methods)
// 初始化表单
const initForm = () => {
  // 初始化字段数据和状态
  props.config.formItems.forEach(item => {
    formData[item.prop] = item.defaultValue ?? ''
    fieldStates[item.prop] = {
      visible: item.visible ?? true,
      required: item.required ?? false,
      readonly: item.readonly ?? false
    }
    if (item.rules) formRules[item.prop] = item.rules
  })
  
  // 添加交互规则
  if (props.config.interactionRules) {
    interactionManager.addRules(props.config.interactionRules)
  }
}

// 在父组件中预定义所有可能用到的方法
const formMethods = {
  // args参数集合 childFrom参数集合
  customChangeHandler: (args: Array<any>,childFrom: Object) => {
    updateData({ companyName: 'admin' })
    console.log('自定义change事件触发')
  },
  handleRoleChange: (args: Array<any>,childFrom: Object) => {
    console.log('自定义事件触发', args, childFrom)
  },
  handleSexChange: (args: Array<any>,childFrom: Object) => {
    console.log('自定义事件触发', args)
  }
}

// 动态绑定事件处理器
const getEventHandlers = (item:any) => {
  if (!item.events) return {}
  const handlers = {}
  for (const [eventName, methodName] of Object.entries(item.events)) {
    handlers[eventName] = (...args: any[]) => {
      const func = formMethods?.[methodName]
      if (func) {
        func(args, {...formData})
      } else {
        console.warn(`未定义的方法: ${methodName}`)
      }
    }
  }
  return handlers
}

// 计算可见的表单项
const visibleFormItems = computed(() => {
  const visibleProp = props.config.formItems.filter(item => fieldStates[item.prop]?.visible)
  return groupAdjacent(visibleProp)
})

const groupAdjacent = (data) => {
  if (data.length === 0) return [];
  const result = [];
  let currentGroup = {
    type: data[0].region,
    data: [data[0]]
  };
  for (let i = 1; i < data.length; i++) {
    if (data[i].region === currentGroup.type) {
      currentGroup.data.push(data[i]);
    } else {
      result.push(currentGroup);
      currentGroup = {
        type: data[i].region,
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
  if (item.type === 'select' && item.options) {
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
watch(() => props.config.formItems.map(item => fieldStates[item.prop]?.required), 
  () => {
    // 必填状态变化时强制更新验证
    nextTick(() => {
      formRef.value?.clearValidate()
    })
  },
  { deep: true }
)

// 监听表单数据变化
watch(formData, () => {
  interactionManager.applyAllRules(formRef.value)
}, { deep: true })

// 初始化
onMounted(() => {
  initForm()
  interactionManager.applyAllRules(formRef.value)
})

// 暴露给父组件的方法,用于父组件修改表单数据
const updateData = (newData) => {
  Object.keys(newData).forEach(key => {
    formData[key] = newData[key]
  })
}
// 暴露方法
defineExpose({
  updateData,
  getFormData: () => formData,
  validate: (callBack) => formRef.value?.validate((valid) => callBack(valid)),
  applyRules: () => interactionManager.applyAllRules(formRef.value)
})
</script>

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