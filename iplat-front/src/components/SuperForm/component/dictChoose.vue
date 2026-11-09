<template>
  <div style="width: 100%">
    <template v-if="$attrs.fieldType == 'select'">
      <el-select v-bind="arrts" @change="handleChange" v-model="innerValue" style="width: 100%">
        <el-option v-for="opt in getDicts($attrs)" :key="opt.value" :label="opt.label" :value="opt.value" />
      </el-select>
    </template>
    <template v-if="$attrs.fieldType == 'radio'">
      <el-radio-group v-bind="arrts" v-model="innerValue" @change="handleChange" v-if="$attrs.isRadioButton">
        <el-radio-button v-for="opt in getDicts($attrs)" :value="opt.value">{{ opt.label }}</el-radio-button>
      </el-radio-group>
      <el-radio-group v-bind="arrts" v-model="innerValue" @change="handleChange" v-else>
        <el-radio v-for="opt in getDicts($attrs)" :value="opt.value">{{ opt.label }}</el-radio>
      </el-radio-group>
    </template>
    <template v-if="$attrs.fieldType == 'checkBox'">
      <el-checkbox-group v-bind="arrts" v-model="checkboxValue" @change="handleChange" v-if="$attrs.isRadioButton">
        <el-checkbox-button v-for="opt in getDicts($attrs)" :value="opt.value">{{ opt.label }}</el-checkbox-button>
      </el-checkbox-group>
      <el-checkbox-group v-bind="arrts" v-model="checkboxValue" @change="handleChange" v-else>
        <el-checkbox v-for="opt in getDicts($attrs)" :value="opt.value">{{ opt.label }}</el-checkbox>
      </el-checkbox-group>
    </template>
  </div>
</template>

<script setup lang="ts">
const props = defineProps({
  // 双向绑定值（必传）
  modelValue: {
    type: null,
    default: ''
  }
});
const $attrs = useAttrs();
// 定义Emits：支持v-model双向绑定 + 事件透传
const emit = defineEmits(['update:modelValue', 'change']);

// 为 checkbox 类型单独处理值
const checkboxValue = computed({
  get() {
    // 确保 checkbox 类型返回数组
    return Array.isArray(props.modelValue) ? props.modelValue : [];
  },
  set(val) {
    emit('update:modelValue', val);
  }
});

// 内部值（解耦props，避免直接修改）
const innerValue = computed({
  get() {
    return props.modelValue;
  },
  set(val) {
    emit('update:modelValue', val); // 触发v-model更新
  }
});

const { proxy } = getCurrentInstance() as ComponentInternalInstance;
const dicts = reactive([]);
if ($attrs.dict) {
  dicts.push($attrs.dict);
}
const dictOption = toRefs<any>(proxy?.useDict(...dicts));

// 下拉选框数据获取
const getDicts = (item) => {
  // 如果有定义字典关键字，优先使用字典数据
  if (item.isDict && item.dict) {
    return dictOption[item.dict].value;
  } else {
    return item.options.filter((item) => item.status !== '1').sort((a, b) => a.sort - b.sort) || [];
  }
};

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
const handleChange = (val) => {
  emit('change', val);
};
</script>

<style scoped></style>
