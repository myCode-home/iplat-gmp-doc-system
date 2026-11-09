<template>
  <el-form ref="queryFormRef" :model="queryParams" :labelPosition="labelPosition" :size="size" :inline="true"
    label-width="auto">
    <div class="query-box">
      <span class="item-left">
        <template v-for="(item, index) in attributeList" :key="item.prop+index">
          <template v-if="item.isEnable == '1'">
            <!-- 整个表单项插槽 -->
            <slot v-if="item.isSlot && item.slotName == `${item.prop}Form`" :name="`${item.prop}Form`" v-bind="item"
              :pageConfigAlign="pageConfigAlign"></slot>
            <el-form-item v-else :prop="item.prop" :label="pageConfigAlign != 'none' ? item.label : ''"
              :style="{ width: pageConfigAlign != 'none' ? '290px' : '200px' }">
              <el-tooltip class="item" effect="dark" :disabled="pageConfigAlign != 'none'" :content="item.label"
                placement="top">
                <!-- 内容插槽 -->
                <slot v-if="item.isSlot" :name="item.slotName" v-bind="arrts(item)">
                </slot>
                <component v-else @change="handleQuery" @keyup.enter="handleEnter(item.fieldType)"
                  :is="getComponent(item.fieldType)" v-model="queryParams[item.prop]" v-bind="arrts(item)" />
              </el-tooltip>
            </el-form-item>
            <slot name="timer"></slot>
          </template>
        </template>
      </span>
      <span class="item-right">
        <el-form-item class="noMarginRight">
          <el-button v-if="showSearch" type="primary" icon="Search" @click="handleQuery">搜索</el-button>
          <el-button v-if="showResetButton" icon="Refresh" @click="resetQuery">{{ resetButtonText }}</el-button>
          <!-- 新增的按钮插槽 -->
          <slot name="rightButton" />
        </el-form-item>
      </span>
    </div>
  </el-form>
</template>

<script setup lang="ts">
import { ElInput, ElDatePicker, ElTimeSelect, ElTimePicker } from 'element-plus'
import { usePageConfigStore } from '@/store/modules/pageConfig'
import DataChoose from '@/components/SuperForm/component/dataChoose.vue'
import DictChoose from '@/components/SuperForm/component/dictChoose.vue'
import DepartmentSelect from '@/components/SuperForm/component/departmentSelect.vue'
import ProvinceSelect from '@/components/SuperForm/component/provinceSelect.vue'
import FileTypeSelect from '@/components/SuperForm/component/fileTypeSelect.vue'

const { proxy } = getCurrentInstance() as ComponentInternalInstance;

const props = defineProps({
  // 双向绑定值（必传）
  modelValue: {
    type: Object,
    required: true,
    default: () => ({
      pageNum: 1,
      pageSize: 10
    })
  },
  code: {
    type: String,
    required: true
  },
  // 是否初始化自动调用emit('getList'), 使用场景：携带筛选配置的默认值查询列表数据，如果筛选项未设置默认值，可不设置
  autoGetList: {
    type: Boolean,
    default: false
  },
  // 是否显示重置按钮
  showResetButton: {
    type: Boolean,
    default: true
  },
  // 是否显示搜索按钮
  showSearch: {
    type: Boolean,
    default: true
  },
  // 重置按钮文本
  resetButtonText: {
    type: String,
    default: '重置'
  }
});

// 定义Emits：支持v-model双向绑定 + 事件透传
const emit = defineEmits(['update:modelValue', 'getList', 'resetQuery']);
const modelValueCopy = computed(() => props.modelValue);
interface QueryParams {
  pageNum: number;
  pageSize: number;
  [key: string]: any;
}
const queryParams = ref<QueryParams>({
  pageNum: 1,
  pageSize: 10,
});
// 表单状态
const formData = computed({
  get() {
    return props.modelValue;
  },
  set(val) {
    const obj = { ...props.modelValue, ...val };
    emit('update:modelValue', obj);
  }
});
const attributeList = ref([]);
const queryFormRef = ref<ElFormInstance>();
const pageConfigAlign = ref('right');
const labelPosition = ref<'right' | 'top' | 'left'>('right');
const size = ref<"" | "default" | "small" | "large">('small');

const loading = ref(false)
// 获取页面配置
const getConfig = async () => {
  loading.value = true;
  usePageConfigStore().getPageConfig(props.code).then((data: any) => {
    pageConfigAlign.value = data.pageConfigAlign
    labelPosition.value = data.pageConfigAlign == 'none' ? 'right' : data.pageConfigAlign
    size.value = data.pageConfigSize || 'small'
    if (data && data.attributeList) {
      attributeList.value = data.attributeList.map((item: any) => {
        item.pageConfigAttributeValue = item.pageConfigAttributeValue ? JSON.parse(item.pageConfigAttributeValue) : {}
        return { ...item, ...item.pageConfigAttributeValue }
      }).sort((a, b) => a.pageConfigAttributeSort - b.pageConfigAttributeSort);
      initForm();
    } else {
      proxy?.$modal.msgError('未查询到表格搜索配置，请联系管理员进行配置！')
    }
  }).finally(() => {
    loading.value = false;
  })
}
// 初始化表单
const initForm = () => {
  // 初始化字段数据
  attributeList.value.forEach(item => {
    modelValueCopy.value[item.prop] = modelValueCopy.value[item.prop] || item.defaultValue || '';
  });
  queryParams.value = {
    pageNum: modelValueCopy.value.pageNum || 1,
    pageSize: modelValueCopy.value.pageSize || 10,
    ...modelValueCopy.value
  };
  if (props.autoGetList) {
    handleQuery();
  }
}
const arrts = computed(() => {
  return (item: any) => {
    if (!item.placeholder) {
      if (item.fieldType == 'input') {
        item.placeholder = '请输入' + item.label
      } else {
        item.placeholder = '请选择' + item.label
      }
    }
    return item
  }
});

const getComponent = (item) => {
  if (item == 'input') {
    return ElInput
  } else if (item == 'dataChoose') {
    return DataChoose
  } else if (['select', 'radio', 'checkBox'].includes(item)) {
    return DictChoose
  } else if (item == 'datePicker') {
    return ElDatePicker
  } else if (item == 'timeSelect') {
    return ElTimeSelect
  } else if (item == 'timePicker') {
    return ElTimePicker
  } else if (item == 'departmentSelect') {
    return DepartmentSelect
  } else if (item == 'province') {
    return ProvinceSelect
  } else if (item == 'fileTypeSelect') {
    return FileTypeSelect
  }
  return item
}

/** 搜索按钮操作 */
const handleQuery = async () => {
  queryParams.value.pageNum = 1;
  emit('update:modelValue', queryParams.value);
  emit('getList');
};

const handleEnter = (type) => {
  if (type == 'input' && pageConfigAlign.value == 'none') {
    handleQuery();
  }
};

const inst = getCurrentInstance()
// 判断父组件是否有定义resetQuery事件 注意事件名转成 on + 驼峰（kebab-case -> CamelCase）
const hasResetQuery = computed(() => !!inst?.vnode?.props?.onResetQuery)

/** 重置按钮操作 */
const resetQuery = () => {
  queryFormRef.value?.resetFields();
  // 判断父组件有无监听该方法，有则执行父组件方法，无则执行默认方法并调用刷新接口
  if (hasResetQuery.value) {
    queryParams.value.pageNum = 1;
    emit('update:modelValue', queryParams.value);
    emit('resetQuery');
  } else {
    handleQuery();
  }
};

// 监听表单数据变化
// watch(formData.value, () => {
//   // console.log(formData.value)
//   emit('update:modelValue', formData.value); // 触发v-model更新
// }, { deep: true })

// 初始化
onMounted(() => {
  getConfig();
})
// 暴露方法
defineExpose({

})
</script>

<style lang="scss" scoped>
.query-box {
  display: flex;

  .item-left {
    flex: 1;

    :deep(.el-form-item:last-child) {
      margin-right: 0;
    }
  }

  .item-right {
    width: 175px;
  }
}

:deep(.el-form-item) {
  margin-right: 10px;
  margin-bottom: 10px;

  .el-input,
  .el-select,
  .el-date-editor,
  .el-time-select,
  .el-time-picker {
    width: 100%;
  }
}

.noMarginRight {
  margin-right: 0;
}
</style>
