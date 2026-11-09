<template>
  <el-table 
    ref="tableRef" 
    :class="class" 
    v-loading="loading || dataLoading" 
    :data="tableData"
    :border="border" 
    :size="size" 
    :header-cell-style="{ 'text-align': 'center' }"
    v-bind="$attrs"
    @sort-change="handleSortChange">
    <!-- 动态渲染列 -->
    <template v-for="(item, index) in attributeList" :key="item.prop + index">
      <template v-if="item.isEnable == '1'">
        <!-- 表格列项插槽 -->
        <slot v-if="item.isSlot && item.slotName == `${item.prop}Column`" :name="item.prop + 'Column'" v-bind="item"
          :align="align"></slot>
        <!-- 表格checkbox -->
        <el-table-column v-else-if="item.fieldType === 'selection'" :reserve-selection="reserveSelection" type="selection" :align="align || 'center'"
          :width="item.width || 55" :min-width="item.minWidth" />
        <!-- 表格序号 -->
        <el-table-column v-else-if="item.fieldType === 'index'" type="index" :align="align || 'center'"
          :label="item.label || '序号'" :width="item.width || 55" :min-width="item.minWidth" />
        <!-- 表格项 -->
        <el-table-column v-else-if="item.fieldType != 'operation'" :prop="item.prop" :label="item.label"
          :width="item.width" :min-width="item.minWidth" :align="align || 'center'" show-overflow-tooltip
          :sortable="item.sortable">
          <!-- 自定义单元格内容（支持插槽） -->
          <template #default="scope">
            <!-- 内容项插槽 -->
            <slot v-if="item.isSlot" :name="item.slotName" v-bind="scope"></slot>
            <span v-else-if="item.fieldType == 'dict'">
              <dict-tag :options="getDicts(item)" :value="scope.row[item.prop]" />
            </span>
            <span v-else-if="item.fieldType == 'time'">
              {{ parseTime(scope.row[item.prop], item.formatTime || '{y}-{m}-{d}') }}
            </span>
            <span v-else-if="item.fieldType == 'price'">
              {{ scope.row[item.prop] ? ((item.priceSymbol || '') +
                addThousandSeparator(scope.row[item.prop], item.separator, item.precision, item.forcePadZero)) : '' }}
            </span>
            <span v-else>{{ scope.row[item.prop] }}</span>
          </template>
        </el-table-column>
        <!-- 操作列 -->
        <el-table-column v-else-if="item.fieldType === 'operation'" :label="item.label || '操作'"
          :width="operationColumnWidth" :min-width="item.minWidth || 130" align="left" :fixed="item.fixed || 'right'">
          <template #default="scope">
            <slot :name="item.slotName || 'operation'" v-bind="scope">
              <!-- 默认操作按钮 -->
              <el-tooltip content="修改" placement="top">
                <el-button link type="primary" icon="Edit" @click="emit('edit', scope.row)"></el-button>
              </el-tooltip>
              <el-tooltip content="删除" placement="top">
                <el-button link type="danger" icon="Delete" @click="emit('delete', scope.row);"></el-button>
              </el-tooltip>
            </slot>
          </template>
        </el-table-column>

      </template>
    </template>
  </el-table>
  <div v-if="isShowPagination">
    <pagination v-show="total > 0" :size="size" v-model:page="currentPage" v-model:limit="currentLimit"
      :pageSizes="pageSizes" :total="total" @pagination="getListHandler" />
  </div>
  <div v-if="!isShowPagination && total > 0" style="text-align: right; margin-top: 10px;">
    {{  `共 ${total} 条` }}
  </div>
</template>

<script setup lang="ts">
import { DemoQuery } from '@/api/demo/demo/types';
import { usePageConfigStore } from '@/store/modules/pageConfig'
const props = defineProps({
  isShowPagination: {
    type: Boolean,
    default: true
  },
  code: {
    type: String,
    required: true
  },
  data: {
    type: Array,
    default: () => []
  },
  dataLoading: {
    type: Boolean,
    default: false
  },
  // 是否显示边框
  border: {
    type: Boolean,
    default: true
  },
  // 是否那个分页多选
  reserveSelection: {
    type: Boolean,
    default: true
  },
  // 是否分页
  showPagination: {
    type: Boolean,
    default: true
  },
  // 分页页码
  pageNum: {
    type: Number,
    default: 1
  },
  // 分页条数
  pageSize: {
    type: Number,
    default: 10
  },
  // 查询条件
  queryParams: {
    type: Object,
    default: () => ({})
  },
  class: {
    type: [String || Object]
  },
  // 总条数
  total: {
    type: Number,
    default: 0
  },
  // 分页条数
  pageSizes: { type: Array<number>, default: () => [10, 20, 30, 50] },
  // 是否为差异对比表格
  isDiffTable: {
    type: Boolean,
    default: false
  }
})
// 定义Emits：支持v-model双向绑定 + 事件透传
const emit = defineEmits(['edit', 'delete', 'getList']);
const tableData = computed(() => props.data);
const queryParamsModel = defineModel('queryParams') as Ref<DemoQuery>;
const $attrs = useAttrs();

const { proxy } = getCurrentInstance() as ComponentInternalInstance;

const currentPage = computed({
  get() {
    return props.queryParams?.pageNum;
  },
  set(val) {
    queryParamsModel.value.pageNum = val
  }
});
const currentLimit = computed({
  get() {
    return props.queryParams?.pageSize;
  },
  set(val) {
    queryParamsModel.value.pageSize = val
  }
});
const operationColumnWidth = ref(50); // 设置一个默认值
// 监听表格数据变化，计算操作列宽度
watch(() => props.data, () => {
  setTimeout(() => {
    nextTick(() => {
      let maxButtonCount = 0;
      // 获取当前表格实例的所有行
      const tableElement = tableRef.value?.$el;
      const rows = tableElement?.querySelectorAll('.el-table__body-wrapper .el-table__row') as NodeListOf<HTMLElement>;
      if (rows) {
        rows.forEach(row => {
          const buttons = row.querySelector('.el-table-fixed-column--right')?.querySelectorAll('button') as NodeListOf<HTMLElement>;
          if (buttons) {
            maxButtonCount = Math.max(maxButtonCount, buttons.length);
          }
        });
      }
      if (maxButtonCount > 0) {
        operationColumnWidth.value = 30 * maxButtonCount + 22;
      } else {
        operationColumnWidth.value = 55;
      }
    });
  });
}, { immediate: true });


const attributeList = ref([]);
const size = ref<"" | "default" | "small" | "large">('small');

const dicts = reactive([])
const dictOption = ref({});

const getDicts = computed(() => {
  return (item) => {
    // 如果有定义字典关键字，优先使用字典数据
    if (item.isDict && item.dict) {
      return dictOption.value[item.dict]
    } else {
      return item.options.filter(item => item.status !== '1').sort((a, b) => a.sort - b.sort) || []
    }
  }
})

// 下拉选框数据获取
// const getDicts = (item) => {
//   // 如果有定义字典关键字，优先使用字典数据
//   proxy?.$nextTick(()=>{
//     if (item.isDict && item.dict) {
//       console.log(dictOption,item.dict)
//       return dictOption[item.dict]?.value
//     } else {
//       return item.options.filter(item => item.status !== '1').sort((a,b)=>a.sort-b.sort) || []
//     }
//   })
// }
const align = ref<'right' | 'center' | 'left'>('center');

const loading = ref(false)
// 获取页面配置
const getConfig = async () => {
  loading.value = true;
  usePageConfigStore().getPageConfig(props.code).then((data: any) => {
    align.value = data.pageConfigAlign || 'center'
    size.value = data.pageConfigSize || 'small'
    if (data && data.attributeList) {
      attributeList.value = data.attributeList.map((item: any) => {
        item.pageConfigAttributeValue = item.pageConfigAttributeValue ? JSON.parse(item.pageConfigAttributeValue) : {}
        return { ...item, ...item.pageConfigAttributeValue }
      }).sort((a, b) => a.pageConfigAttributeSort - b.pageConfigAttributeSort);
      // console.log('attributeListattributeList')
      // console.log(attributeList.value)
      initTable();
    } else {
      proxy?.$modal.msgError('未查询到表格配置，请联系管理员进行配置！')
    }
  }).finally(() => {
    loading.value = false;
  })
}

// 初始化表格
const initTable = () => {
  attributeList.value.forEach(item => {
    if (item.fieldType == 'dict' && item.isDict) {
      dicts.push(item.dict)
    }
  })
  dictOption.value = toRefs<any>(proxy?.useDict(...dicts));
}

/** 排序触发事件 */
const handleSortChange = (column, prop, order) => {
  queryParamsModel.value.orderByColumn = column.prop;
  queryParamsModel.value.isAsc = column.order;
  if (!column.order) {
    queryParamsModel.value.orderByColumn = undefined
  }
  getListHandler()
};

const getListHandler = () => {
  emit('getList');
};
const tableRef = ref<ElTableInstance>();

const toggleRowSelection = (row, selected) => {
  tableRef.value.toggleRowSelection(row, selected)
}

const setCurrentRow = (row) => {
  tableRef.value.setCurrentRow(row||null)
}

const clearSelection = () => {
  tableRef.value.clearSelection()
}

// 初始化
onMounted(() => {
  getConfig();
})
// 暴露方法
defineExpose({
  toggleRowSelection,
  setCurrentRow,
  clearSelection,
  tableRef
})
</script>

<style lang="scss">
//找到表头，然后隐藏掉复选框
.nomultiple {
  .el-table__header-wrapper {
    .el-checkbox {
      display: none
    }
  }
}
</style>
