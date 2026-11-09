<template>
  <el-dialog v-model="dialog.visible" :title="dialog.title" width="800px" append-to-body>
    <el-row :gutter="10" style="margin-bottom: 5px;">
      <el-col :span="1.5">
        <el-button type="primary" icon="Plus" @click="addAttr">新增</el-button>
      </el-col>
    </el-row>
    <el-table ref="tableRef" v-if="dialog.visible" v-loading="loading" border :data="arrtTableCopy" size="small" row-key="businessAttrId" :default-sort="{prop: 'sort', order: 'ascending'}" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="50" align="center" />
      <el-table-column label="序号" type="index" width="45" align="center" />
      <el-table-column label="名称" min-width="120" align="center" prop="label">
        <template #default="{row}">
          <el-input v-model="row.label" placeholder="请输入名称" />
        </template>
      </el-table-column>
      <el-table-column label="值" min-width="120" align="center" prop="value">
        <template #default="{row}">
          <el-input v-model="row.value" placeholder="请输入名称" />
        </template>
      </el-table-column>
      <el-table-column label="是否启用" width="90" align="center" prop="status">
        <template #default="{row}">
          <el-switch v-model="row.status" :active-value="'0'" :inactive-value="'1'"></el-switch>
        </template>
      </el-table-column>
      <el-table-column label="回显样式" width="150" align="center" prop="elTagType">
        <template #default="{row}">
          <el-select v-model="row.elTagType">
            <el-option
              v-for="item in listClassOptions"
              :key="item.value"
              :label="item.label + '(' + item.value + ')'"
              :value="item.value"
            ></el-option>
          </el-select>
        </template>
      </el-table-column>
      <el-table-column label="排序" width="100" align="center" prop="sort" :formatter="formatter">
        <template #default="{row}">
          <el-input v-model="row.sortCopy" @blur="($event:any)=> row.sort = $event.target.value" type="number" placeholder="排序" />
        </template>
      </el-table-column>
      <el-table-column label="操作" width="60" align="center">
        <template #default="scope">
          <el-tooltip  content="删除" placement="top">
            <el-button link type="primary" icon="Delete" @click="removeTag(scope.$index,true)"></el-button>
          </el-tooltip>
        </template>
      </el-table-column>
    </el-table>
    <template #footer>
      <div class="dialog-footer">
        <el-button type="primary" @click="submitAttr">确 定</el-button>
        <el-button @click="dialog.visible = false">取 消</el-button>
      </div>
    </template>
  </el-dialog>
  <el-tag v-for="({ label, value, status, elTagType}, index) in tagTable" class="dict-tag" closable :type="elTagType" :disable-transitions="false" :key="index" @close="removeTag(index)" >
    {{label+`(${value})`}}
  </el-tag>
  <el-button class="button-new-tag" type="primary" size="small" @click="showInput" style="margin-bottom: 3px;" >添加选项</el-button >
</template>

<script name="AttrTag" lang="ts" setup>
interface AttrItem {
  label: string;
  value: string;
  sort?: number;
  status?: String;
  elTagType?: 'primary' | 'success' | 'info' | 'warning' | 'danger';
  sortCopy?: number;
}
const props = defineProps({
  // 双向绑定值（必传）
  modelValue: {
    type: Array as PropType<AttrItem[]>,
    default: () => [],
  }
});
const { proxy } = getCurrentInstance() as ComponentInternalInstance;
// 定义Emits：支持v-model双向绑定 + 事件透传
const emit = defineEmits(['update:modelValue']);
// 内部值（解耦props，避免直接修改）
const arrtTable = computed(() => {
  return props.modelValue.sort((a,b)=>a.sort-b.sort)
});
const tagTable = computed(() => {
  const values = JSON.parse(JSON.stringify(arrtTable.value))
  return values.filter(item=> item.status == '0').map(item=>{
    if(!['primary' , 'success' , 'info' , 'warning' , 'danger'].includes(item.elTagType)){
      item.elTagType = 'primary'
    }
    return item
  })
});

const loading = ref(false)
const dialog = reactive({
  visible: false,
  loading: false,
  title: ''
});

// 数据标签回显样式
const listClassOptions = ref<Array<{ value: string; label: string }>>([
  { value: 'default', label: '默认' },
  { value: 'primary', label: '主要' },
  { value: 'success', label: '成功' },
  { value: 'info', label: '信息' },
  { value: 'warning', label: '警告' },
  { value: 'danger', label: '危险' }
]);

const ids = ref([]);
const names = ref([]);
const single = ref(true);
const multiple = ref(true);
const selectRows = ref([]);

const removeTag = (index, state = false) => {
  if(state == true) {
    arrtTableCopy.value.splice(index, 1);
  }else {
    arrtTable.value.splice(index, 1);
    emit('update:modelValue', arrtTable.value);
  }
};
const arrtTableCopy = ref([]);
const showInput = () => {
  dialog.visible = true;
  dialog.title = '添加选项';
  arrtTableCopy.value = JSON.parse(JSON.stringify(arrtTable.value));
}
const addAttr = () => {
  const sort = Number(arrtTableCopy.value[arrtTableCopy.value.length - 1]?.sort) + 1 || 1;
  arrtTableCopy.value.push({ label: '', value: '', status: '0', elTagType: 'default', sort: sort, sortCopy: sort});
}

// 排序筛选
const formatter = (row, column) => {
  return row.sort
};

const submitAttr = () => {
  const noData = arrtTableCopy.value.filter((item) => !item.label.trim() || !item.value.trim())
  const labelArr = new Set(arrtTableCopy.value.map(item => item.label));
  const valueArr = new Set(arrtTableCopy.value.map(item => item.value));
  if(noData.length > 0) {
    return proxy?.$modal.msgError('名称和值不能为空！');
  }
  if(labelArr.size != arrtTableCopy.value.length || valueArr.size != arrtTableCopy.value.length) {
    return proxy?.$modal.msgError('名称或值不能重复！');
  }
  console.log(arrtTableCopy.value)
  emit('update:modelValue', arrtTableCopy.value.sort((a,b)=>a.sort-b.sort));
  dialog.visible = false;
}

// 多选框选中数据
const handleSelectionChange = (selection:any) => {
  ids.value = selection.map((item:any) => item.businessAttrId);
  names.value = selection.map((item:any) => item.businessAttrName);
  single.value = selection.length != 1;
  multiple.value = !selection.length;
  selectRows.value = selection;
};

// 表单校验
const validate = (callBack) =>{
  
}


onMounted(() => {
  
})

defineExpose({
  
})
</script>


<style lang="scss" scoped>
.dict-tag{
  margin-bottom: 3px;
  margin-right: 5px;
}
</style>
