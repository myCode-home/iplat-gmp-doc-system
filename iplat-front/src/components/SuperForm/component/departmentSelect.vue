<template>
  <div style="width: 100%">
    <!-- <template v-if="$attrs.fieldType == 'select'">
      <el-select v-bind="arrts" filterable clearable @change="handleChange" v-model="innerValue" style="width: 100%">
        <el-option v-for="opt in getDicts($attrs)" :key="opt.value" :label="opt.label" :value="opt.value" />
      </el-select>
    </template> -->
    <el-tree-select
      v-model="innerValue"
      :data="deptOptions"
      :show-checkbox="$attrs.multiple"
      :props="{ value: 'id', label: 'label', children: 'children' } as any"
      :default-expanded-keys="expandedKeys"
      value-key="id"
      placeholder="请选择归属部门"
      check-strictly
      @change="handleChange"
      v-bind="arrts"
    />
  </div>
</template>

<script setup lang="ts">
import { DeptTreeVO } from '@/api/system/dept/types';
import api from '@/api/system/user';
import { number } from 'vue-types';
const deptOptions = ref<DeptTreeVO[]>([]);
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

// 内部值（解耦props，避免直接修改）
const innerValue = computed({
  get() {
    if($attrs.multiple) {
      // 多选时返回数组
      return Array.isArray(props.modelValue) ? props.modelValue.map(v=>formatId(v)) : [];
    } else {
      return formatId(props.modelValue);

    }
  },
  set(val) {
    emit('update:modelValue', val); // 触发v-model更新
  }
});

const formatId = (val) => {
  if(val && val <= 1000000){
      return Number(val)
  }
  return val
}


/** 查询部门下拉树结构 */
const getDeptTree = async () => {
  const res = await api.deptTreeSelect();
  deptOptions.value = res.data[0].children;
};

// 计算需要展开的节点
const expandedKeys = computed(() => {
  if (!innerValue.value) return [];
  // 如果是多选，返回所有选中的值
  if (Array.isArray(innerValue.value)) {
    return innerValue.value;
  }
  // 如果是单选，返回当前选中的值
  return [innerValue.value];
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
const handleChange = (val) => {
  emit('change', val);
};
onMounted(() => {
  getDeptTree(); // 初始化部门数据
});
</script>

<style scoped></style>
