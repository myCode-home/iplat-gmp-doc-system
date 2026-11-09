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
        <el-select v-model="row.businessAttrId" disabled filterable clearable @change="businessChange" placeholder="请选择表字段">
          <el-option v-for="dict in attrListTemp" :key="dict.businessAttrId" :label="dict.businessAttrName" :value="dict.businessAttrId"></el-option>
        </el-select>
        <!-- <dict-tag :options="attrList" :value="row.businessAttrId" valueProp="businessAttrId" labelProp="businessAttrName"/> -->
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
    <el-table-column label="固定宽度" width="150" align="center" prop="remark">
      <template #default="{row}">
        <el-input v-model.number="row.width" style="width: 100%;" placeholder="请输入宽度">
          <template #append>px</template>
        </el-input>
      </template>
    </el-table-column>
    <el-table-column label="最小宽度" width="150" align="center" prop="minWidth">
      <template #default="{row}">
        <el-input v-model.number="row.minWidth" style="width: 100%;" placeholder="请输入最小宽度">
          <template #append>px</template>
        </el-input>
      </template>
    </el-table-column>
    <el-table-column label="展示类型" width="120" align="center" prop="type">
      <template #default="{row}">
        <el-select v-model="row.fieldType" filterable placeholder="请选择展示类型">
          <el-option v-for="dict in system_display_type" :key="dict.value" :label="dict.label" :value="dict.value"></el-option>
        </el-select>
      </template>
    </el-table-column>
    <el-table-column label="是否显示" width="70" align="center" prop="isEnable">
      <template #default="{row}">
        <el-checkbox v-model="row.isEnable" true-value="1" false-value="0"></el-checkbox>
      </template>
    </el-table-column>
    <el-table-column label="是否排序" width="70" align="center" prop="sortable">
      <template #default="{row}">
        <el-checkbox v-model="row.sortable"></el-checkbox>
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

<script name="AttrTableMultiple" lang="ts" setup>
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

const { system_display_type } = toRefs<any>(proxy?.useDict('system_display_type'));

const initFormData = {
  pageConfigId: props.pageConfigId,
  pageConfigAttributeId: '',
  pageConfigAttributeName: '',
  pageConfigAttributeType: '',
  pageConfigAttributeDesc: '',
  label: '',
  businessAttrId: '',
  prop: '',
  fieldType: 'text',
  sort: undefined,
  width: undefined,
  minWidth: undefined,
  isDict: true,
  dict: undefined,
  options: [],
  formatTime: '{y}-{m}-{d} {h}:{i}:{s}',
  isSlot: false,
  isEnable: '1',
  sortable: false,
  slotName: '',
  remark: '',
};
const data = reactive({
  form: { ...initFormData }
});

const { form } = toRefs(data);
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

const attrListTemp = computed(() => {
  let result = []
  if (props.attrList) {
    result = JSON.parse(JSON.stringify(props.attrList))
  }
  result.unshift(
    {
      businessAttrName: '选择框',
      businessAttrId: 'selection',
      businessAttrCode: 'selection',
    },{
      businessAttrName: '序号',
      businessAttrId: 'index',
      businessAttrCode: 'index',
    },{
      businessAttrName: '操作',
      businessAttrId: 'operation',
      businessAttrCode: 'operation',
    }
  )
  return result;
});

const arrtTable = ref([]);
const getTableData = () => {
  tableRef.value.clearSelection();
  const clonedTableList = JSON.parse(JSON.stringify(props.tableList));
  console.log('clonedTableListclonedTableList')
  console.log(clonedTableList)
  const clonedTableObj = {} as any;

  clonedTableList.forEach((item, index) => {
    if(item.pageConfigAttributeValue) {
      item.pageConfigAttributeValue = JSON.parse(item.pageConfigAttributeValue)
      clonedTableObj[item.pageConfigAttributeValue.businessAttrId] = item
    }
  });
  const selectionArray = []

  arrtTable.value = attrListTemp.value.map((item, index)=>{
    const result = {
      pageConfigId: props.pageConfigId,
      label: item.businessAttrName,
      businessAttrId: item.businessAttrId,
      prop: item.businessAttrCode,
      fieldType: 'text',
      sort: index+3,
      sortCopy: index+3,
      pageConfigAttributeId: '',
      width: undefined,
      minWidth: undefined,
      isDict: true,
      dict: undefined,
      options: [],
      formatTime: '{y}-{m}-{d}',
      isSlot: false,
      isEnable: '1',
      sortable: false,
      slotName: '',
      remark: '',
      pageConfigAttributeValue: {}
    }
    let tempObj = null as any
    if(item.businessAttrId && clonedTableObj[item.businessAttrId]) {
      tempObj = clonedTableObj[item.businessAttrId]
    }
    // if(item.businessAttrCode == 'selection' && selectionRow) {
    //   tempObj = selectionRow
    // } else if(item.businessAttrCode == 'index' && indexRow) {
    //   tempObj = indexRow
    // } else if(item.businessAttrCode == 'operation' && operationRow) {
    //   tempObj = operationRow
    // }
      
    if(tempObj) {
      result.label = tempObj.pageConfigAttributeName
      result.fieldType = tempObj.pageConfigAttributeType
      result.pageConfigAttributeId = tempObj.pageConfigAttributeId
      result.sort = tempObj.pageConfigAttributeSort
      result.isEnable = tempObj.pageConfigAttributeEnable
      result.remark = tempObj.pageConfigAttributeValue.remark
      result.width = tempObj.pageConfigAttributeValue.width
      result.minWidth = tempObj.pageConfigAttributeValue.minWidth
      result.sortable = tempObj.pageConfigAttributeValue.sortable
      result.pageConfigAttributeValue = tempObj.pageConfigAttributeValue
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
    // handleSortChange()
  })
}


const businessChange = (val) => {
  const attr = attrListTemp.value.find(item=>item.businessAttrId == val)||{ businessAttrCode: '' }
  form.value.prop = attr.businessAttrCode||''
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
      item.pageConfigAttributeValue.remark = item.remark
      item.pageConfigAttributeValue.sort = item.sort
      item.pageConfigAttributeValue.label = item.label
      item.pageConfigAttributeValue.width = item.width
      item.pageConfigAttributeValue.minWidth = item.minWidth
      item.pageConfigAttributeValue.sortable = item.sortable
      item.pageConfigAttributeValue.fieldType = item.fieldType
      
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
