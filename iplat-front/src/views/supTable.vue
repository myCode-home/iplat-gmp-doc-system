<template>
  <el-table 
    ref="thisRef"
    v-loading="loading"
    :data="props.data" 
    :border="props.border" 
    :size="props.size" 
    :highlight-current-row="props.highlightCurrentRow"
    :stripe="stripe"
    v-bind="$attrs">
    <template v-for="(column,index) in columnOptions" :key="index">
      <el-table-column v-if="column.type === 'index'" type="index" :width="column.width" :align="column.align" :fixed="column.fixed" />
      <el-table-column v-if="column.type === 'selection'" :reserve-selection="column.reserveSelection||false" type="selection" :width="column.width" :min-width="column.minWidth" :align="column.align" :fixed="column.fixed" />
      <el-table-column v-if="column.type === 'option'" :reserve-selection="column.reserveSelection||false" type="selection" :width="column.width" :min-width="column.minWidth" :align="column.align" :fixed="column.fixed" />
      <el-table-column 
        :prop="column.prop" 
        :label="column.label" 
        :width="column.width"
        :min-width="column.minWidth"
        :sortable="column.sortable">
      </el-table-column>
    </template>
    <el-table-column width="150" label="操作" align="center" fixed="right" class-name="small-padding fixed-width">
      <template #default="scope">
        <el-tooltip content="修改" placement="top">
          <el-button v-hasPermi="['system:tenant:edit']" link type="primary" icon="Edit" @click="handleUpdate(scope.row)"></el-button>
        </el-tooltip>
        <el-tooltip content="删除" placement="top">
          <el-button v-hasPermi="['system:tenant:remove']" link type="primary" icon="Delete" @click="handleDelete(scope.row)"></el-button>
        </el-tooltip>
      </template>
    </el-table-column>
  </el-table>
</template>

<script setup name="SupTable" lang="ts">
const { proxy } = getCurrentInstance() as ComponentInternalInstance;
const props = defineProps({
  columns: {
    type: Object,
    default: () => {},
    required: true
  },
  data: {
    type: Array,
    default: () => [],
    required: true
  },
  border: {
    type: Boolean,
    default: true
  },
  size: {
    type: String as PropType<'' | 'default' | 'small' | 'large'>,
    default: '',
    validator: (value: string) => ['', 'default', 'small', 'large'].includes(value)
  },
  // 是否要高亮当前行
  highlightCurrentRow: {
    type: Boolean,
    default: true
  },
  // 是否为斑马纹 table
  stripe: {
    type: Boolean,
    default: false
  },

})
const thisRef = ref(null);
const loading = ref(false);
const columnOptions = computed(() => {
  return props.columns;
})
</script>

<style lang="scss" scoped>
</style>