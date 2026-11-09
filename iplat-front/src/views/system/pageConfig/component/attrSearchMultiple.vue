<template>
  <el-table ref="tableRef" v-loading="loading" border :data="arrtTable" row-key="prop" :max-height="maxHeight+ 'px'" size="small" @selection-change="handleSelectionChange">
    <el-table-column type="selection" width="50" align="center" />
    <el-table-column label="id" min-width="100" align="center" prop="businessAttrId" v-if="false" />
    <el-table-column label="序号" type="index" width="45" align="center" />
    <el-table-column label="名称" min-width="120" align="center" prop="label">
      <template #default="{row}">
        <el-input v-model="row.label" placeholder="请输入名称" />
      </template>
    </el-table-column>
    <el-table-column label="表字段" min-width="120" align="center" prop="businessAttrId">
      <template #default="{row}">
        <el-select v-model="row.businessAttrId" disabled filterable clearable placeholder="请选择表字段">
          <el-option v-for="dict in attrList" :key="dict.businessAttrId" :label="dict.businessAttrName" :value="dict.businessAttrId"></el-option>
        </el-select>
      </template>
    </el-table-column>
    <el-table-column label="字段属性" width="100" align="center" prop="prop">
      <!-- <template #default="{row}">
        <el-input v-model="row.prop" :disabled="Boolean(row.businessAttrId)" placeholder="请输入字段属性" />
      </template> -->
    </el-table-column>
    <el-table-column label="排序" width="100" align="center" prop="sort">
      <template #default="{row}">
        <el-input v-model="row.sort" @change="handleSortChange" type="number" placeholder="排序" />
      </template>
    </el-table-column>
    <el-table-column label="是否显示" width="70" align="center" prop="isEnable">
      <template #default="{row}">
        <el-checkbox v-model="row.isEnable" true-value="1" false-value="0"></el-checkbox>
      </template>
    </el-table-column>
    <el-table-column label="是否禁用" width="70" align="center" prop="disabled">
      <template #default="{row}">
        <el-checkbox v-model="row.disabled"></el-checkbox>
      </template>
    </el-table-column>
    <el-table-column label="是否只读" width="70" align="center" prop="readonly">
      <template #default="{row}">
        <el-checkbox v-model="row.readonly"></el-checkbox>
      </template>
    </el-table-column>
    <el-table-column label="能否清空" width="70" align="center" prop="clearable">
      <template #default="{row}">
        <el-checkbox v-model="row.clearable"></el-checkbox>
      </template>
    </el-table-column>
    <el-table-column label="备注" min-width="110" align="center" prop="remark">
      <template #default="{row}">
        <el-input v-model="row.remark" type="textarea" autosize placeholder="请输入备注" />
      </template>
    </el-table-column>
    <!-- <el-table-column width="70" v-if="false" label="操作" fixed="right" align="center" class-name="small-padding fixed-width">
      <template #default="scope">
        <el-tooltip content="修改" placement="top">
          <el-button v-hasPermi="['system:pageConfig:edit']" link type="primary" icon="Edit" @click="handleUpdate(scope.row)"></el-button>
        </el-tooltip>
        <el-tooltip content="删除" placement="top">
          <el-button v-hasPermi="['system:pageConfig:remove']" link type="primary" icon="Delete" @click="handleDelete(scope.row)"></el-button>
        </el-tooltip>
      </template>
    </el-table-column> -->
  </el-table>
</template>

<script name="attrSearchMultiple" lang="ts" setup>
import { optionselect as getDictOptionselect, getType } from '@/api/system/dict/type';
interface BusinessAttr {
  businessAttrCode: string;
  businessAttrName: string;
  [key: string]: any; // 允许其他属性
}
const props = defineProps({
  id: {
    type: String,
    default: ''
  },
  pageConfigId: {
    type: String,
    default: ''
  },
  attrList: {
    type: Array as PropType<BusinessAttr[]>,
    default: () => []
  },
  tableList:{
    type: Array as PropType<{[key: string]: any;}[]>,
    default: () => []
  },
});
const { proxy } = getCurrentInstance() as ComponentInternalInstance;

const { system_field_type } = toRefs<any>(proxy?.useDict('system_field_type'));

const initFormData = {
  pageConfigId: props.pageConfigId,
  pageConfigAttributeId: '',
  pageConfigAttributeName: '',
  pageConfigAttributeType: '',
  label: '',
  businessAttrId: '',
  prop: '',
  fieldType: 'input',
  placeholder: '',
  sort: undefined,
  isEnable: '1',
  disabled: false,
  readonly: false,
  clearable: true,
  remark: '',
};

const loading = ref(false)

const ids = ref([]);
const names = ref([]);
const single = ref(true);
const multiple = ref(true);
const selectRows = ref([]);
const tableRef = ref(null);

// 排序筛选
const handleSortChange = () => {
  arrtTable.value.sort((a, b) => {
    return a.sort - b.sort;
  });
};

// 多选框选中数据
const handleSelectionChange = (selection:any) => {
  ids.value = selection.map((item:any) => item.businessAttrId);
  names.value = selection.map((item:any) => item.businessAttrName);
  single.value = selection.length != 1;
  multiple.value = !selection.length;
  selectRows.value = selection;
};

const typeOptions = ref([]);
/** 查询字典类型列表 */
const getTypeList = async () => {
  const res = await getDictOptionselect();
  typeOptions.value = res.data;
};


const arrtTable = ref([]);
const getTableData = () => {
  tableRef.value.clearSelection();
  const clonedTableList = JSON.parse(JSON.stringify(props.tableList));
  const clonedTableObj = {} as any;
  clonedTableList.forEach((item, index) => {
    if(item.pageConfigAttributeValue) {
      item.pageConfigAttributeValue = JSON.parse(item.pageConfigAttributeValue)
      clonedTableObj[item.pageConfigAttributeValue.businessAttrId] = item
    }
  });
  const selectionArray = []
  arrtTable.value = props.attrList.map((item, index)=>{
    const result = {
      pageConfigId: props.pageConfigId,
      label: item.businessAttrName,
      businessAttrId: item.businessAttrId,
      pageConfigAttributeId: '',
      prop: item.businessAttrCode,
      fieldType: 'input',
      sort: index+1,
      isEnable: '1',
      disabled: false,
      readonly: false,
      clearable: true,
      remark: '',
      pageConfigAttributeValue: {}
    }
    if(clonedTableObj[item.businessAttrId]) {
      result.label = clonedTableObj[item.businessAttrId].pageConfigAttributeName
      result.fieldType = clonedTableObj[item.businessAttrId].pageConfigAttributeType
      result.pageConfigAttributeId = clonedTableObj[item.businessAttrId].pageConfigAttributeId
      result.sort = clonedTableObj[item.businessAttrId].pageConfigAttributeSort
      result.isEnable = clonedTableObj[item.businessAttrId].pageConfigAttributeEnable
      result.disabled = clonedTableObj[item.businessAttrId].pageConfigAttributeValue.disabled
      result.readonly = clonedTableObj[item.businessAttrId].pageConfigAttributeValue.readonly
      result.clearable = clonedTableObj[item.businessAttrId].pageConfigAttributeValue.clearable
      result.remark = clonedTableObj[item.businessAttrId].pageConfigAttributeValue.remark
      result.pageConfigAttributeValue = clonedTableObj[item.businessAttrId].pageConfigAttributeValue
      selectionArray.push(index)
    }
    return result
  })
  nextTick(() => {
    selectionArray.forEach(item => {
      tableRef.value.toggleRowSelection(
        arrtTable.value[item],
        true,
      )
    })
    handleSortChange()
  })
}

// 表单校验
const validate = (callBack) =>{
  if(!ids.value.length){
    return proxy.$modal.msgError('请选择要添加的属性');
  }
  callBack(true)
}

// 获取数据
const achieveData = () => {
  return selectRows.value.map((item)=>{
    const result = {
      pageConfigId: item.pageConfigId,
      pageConfigAttributeId: item.pageConfigAttributeId,
      pageConfigAttributeName: item.label,
      pageConfigAttributeType: item.fieldType,
      pageConfigAttributeSort: item.sort,
      pageConfigAttributeDesc: item.prop,
      pageConfigAttributeEnable: item.isEnable,
      remark: item.remark,
      pageConfigAttributeValue: JSON.stringify(item)
    }
    if(item.pageConfigAttributeId) {
      item.pageConfigAttributeValue.isEnable = item.isEnable
      item.pageConfigAttributeValue.disabled = item.disabled
      item.pageConfigAttributeValue.readonly = item.readonly
      item.pageConfigAttributeValue.clearable = item.clearable
      item.pageConfigAttributeValue.remark = item.remark
      item.pageConfigAttributeValue.label = item.label
      item.pageConfigAttributeValue.sort = item.sort
      result.pageConfigAttributeValue = JSON.stringify(item.pageConfigAttributeValue)
    } else {
      result.pageConfigAttributeValue = JSON.stringify(item)
    }
    return result
  })
}

const maxHeight = ref(0);
// 设置视图高度
const setViewHeight = () => {
  maxHeight.value = document.getElementById('app').clientHeight - 240;
};

onMounted(() => {
  getTypeList();
  getTableData();
  // 自适应窗口
  window.addEventListener('resize', setViewHeight);
  setViewHeight();
})
onUnmounted(() => {
  window.removeEventListener('resize', setViewHeight)
})

defineExpose({
  achieveData,
  validate
})
</script>


<style scoped>

</style>
