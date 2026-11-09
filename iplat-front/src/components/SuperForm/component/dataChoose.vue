<template>
  <div style="width: 100%;">
    <el-input 
      v-model="innerValue"
      v-bind="arrts"
      @click.native="handleClick"
    />
    <to-select
      v-if="selectData.open&&$attrs.fieldType == 'dataChoose'"
      :title="selectData.title"
      :selectType="selectData.type"
      :multiple="selectData.multiple"
      :width="selectData.width"
      :otherCondition="selectData.otherCondition"
      :achieveData="selectDataAchieve"
      @dialogClose="selectDataCancel"
    ></to-select>
  </div>
</template>

<script lang="ts" setup>
const props = defineProps({
  // 双向绑定值（必传）
  modelValue: {
    type: [String, Number],
    default: '',
  },
  // 数据筛选其他条件
  otherCondition: {
    type: Object,
    default: () => ({}),
  }
});
// 定义Emits：支持v-model双向绑定 + 事件透传
const emit = defineEmits(['update:modelValue', 'selectDataAchieve', 'click']);
const $attrs = useAttrs();
// 内部值（解耦props，避免直接修改）
const innerValue = computed({
  get() {
    return props.modelValue;
  },
  set(val) {
    emit('update:modelValue', val); // 触发v-model更新
  },
});

// 文件数据计算属性
const fileData = computed(() => {
  if ($attrs.selectType === 'fileSelect') {
    return Array.isArray(props.modelValue) ? props.modelValue : [];
  }
  return [];
});

const placeholder = computed(() => {
  if($attrs.placeholder) return $attrs.placeholder as string
  return '请输入' + $attrs.label
})
const arrts = computed(() => {
  return {
    ...$attrs,
    placeholder: placeholder.value
  };
});

const selectData = ref({
  open: false,
  type: '',
  width: '',
  title: '',
  selectType: undefined,
  multiple: false,
  otherCondition: {},
})
const inst = getCurrentInstance()
// 判断父组件是否有定义resetQuery事件 注意事件名转成 on + 驼峰（kebab-case -> CamelCase）
const hasClick = computed(() => !!inst?.vnode?.props?.onClick)
// 点击事件
const handleClick = (val) => {
  if(hasClick.value){
    return emit('click', val)
  }
  selectData.value.open = true;
  selectData.value.title = '请选择';
  selectData.value.type = $attrs.selectType as string;
  selectData.value.multiple = $attrs.isMultiple as boolean;
  selectData.value.selectType = $attrs.prop + 'Select';
  const prop = $attrs.prop as string;
  if(props.otherCondition?.hasOwnProperty(prop)){
    selectData.value.otherCondition = props.otherCondition[prop];
  }
};
// 选中数据
const selectDataAchieve = (data) => {
  emit('selectDataAchieve', data, selectData.value)
  selectDataCancel();
};
// 关闭选择
const selectDataCancel = () => {
  selectData.value = {
    open: false,
    title: undefined,
    width: '',
    multiple: false,
    type: undefined,
    selectType: undefined,
    otherCondition: {},
  };
};

// 删除文件项
const handleDelete = (index) => {
  if ($attrs.selectType === 'fileSelect' && Array.isArray(props.modelValue)) {
    const newData = [...props.modelValue];
    newData.splice(index, 1);
    emit('update:modelValue', newData);
  }
};

// console.log(arrts.value)
</script>

<style scoped>

</style>