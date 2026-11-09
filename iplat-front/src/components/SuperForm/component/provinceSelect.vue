<template>
  <div style="width: 100%">
    <el-cascader 
      v-model="innerValue" 
      :options="options" 
      :props="cascaderProps"
      :filterable="!!$attrs.filterable"
      @change="handleChange"
    />
  </div>
</template>

<script setup lang="ts">
import provinceData from '@/utils/provinceData.json';
const props = defineProps({
  // 双向绑定值（必传）
  modelValue: {
    type: null,
    default: ''
  }
});
const $attrs = useAttrs();
const cascaderProps = ref({
  multiple: !!$attrs.multiple
});
const options = provinceData;

// 定义Emits：支持v-model双向绑定 + 事件透传
const emit = defineEmits(['update:modelValue', 'change']);

// 内部值（解耦props，避免直接修改）
const innerValue = computed({
  get() {
    return Array.isArray(props.modelValue) ? props.modelValue : [];
  },
  set(val) {
    emit('update:modelValue', val); // 触发v-model更新
  }
});

const placeholder = computed(() => {
  if ($attrs.placeholder) return $attrs.placeholder as string;
  return '请选择' + $attrs.label;
});

const arrts = computed(() => {
  return {
    ...$attrs,
    placeholder: placeholder.value
  };
});
console.log('arrtsarrtsarrts')
console.log(arrts.value)

const handleChange = (val) => {
  emit('change', val);
};
</script>

<style scoped></style>
