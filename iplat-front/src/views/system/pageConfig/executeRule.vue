<template>
  <div class="">
    <el-card class="box-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <span>{{ pageConfig.pageConfigName?`【${pageConfig.pageConfigName}】`:'' }}规则列表</span>
          <el-button v-if="pageConfig.pageConfigId" v-hasPermi="['system:extended:add']" @click="handleAdd" class="button" type="primary" size="small" icon="Plus" style="float: right;">新增</el-button>
          <el-button v-if="pageConfig.pageConfigId" v-hasPermi="['system:extended:remove']" type="danger"  size="small" icon="Delete" :disabled="multiple" @click="handleDelete"  style="float: right;;margin-right: 10px;">
            删除
          </el-button>
        </div>
      </template>
      <!-- <el-row :gutter="10" class="mb8">
        <el-col :span="1.5">
          <el-button v-hasPermi="['system:extended:add']" type="primary" plain icon="Plus" @click="handleAdd">新增</el-button>
        </el-col>
        <el-col :span="1.5">
          <el-button v-hasPermi="['system:extended:edit']" type="success" plain icon="Edit" :disabled="single" @click="handleUpdate(selectRow)"
            >修改</el-button
          >
        </el-col>
        <el-col :span="1.5">
          <el-button v-hasPermi="['system:extended:remove']" type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete">
            删除
          </el-button>
        </el-col>
        <el-col :span="1.5" v-if="false">
          <el-button v-hasPermi="['system:extended:export']" type="warning" plain icon="Download" @click="handleExport">导出</el-button>
        </el-col>
        <right-toolbar v-model:show-search="showSearch" @query-table="getList"></right-toolbar>
      </el-row> -->
      <transition :enter-active-class="proxy?.animate.searchAnimate.enter" :leave-active-class="proxy?.animate.searchAnimate.leave">
        <div v-show="showSearch">
          <el-form ref="queryFormRef" :model="queryParams" size="small" :inline="true" label-width="auto" @submit.native.prevent="">
            <el-form-item label="名称" prop="pageConfigRuleName">
              <el-input v-model="queryParams.pageConfigRuleName" placeholder="请输入名称" clearable @change="handleQuery" @keyup.enter="handleQuery" style="width: 150px;" />
            </el-form-item>
            <!-- <el-form-item label="字段属性" prop="pageConfigAttributeDesc">
              <el-input v-model="queryParams.pageConfigAttributeDesc" placeholder="请输入字段属性" clearable @change="handleQuery" @keyup.enter="handleQuery" style="width: 150px;" />
            </el-form-item>
            <el-form-item :label="pageConfig.pageConfigDesc=='tableArea'?'展示类型':'类型'" prop="pageConfigAttributeType">
              <el-select v-model="queryParams.pageConfigAttributeType" clearable @change="handleQuery" placeholder="请选择" style="width: 150px;">
                <template v-if="pageConfig.pageConfigDesc=='tableArea'">
                  <el-option v-for="dict in system_display_type" :key="dict.value" :label="dict.label" :value="dict.value"></el-option>
                </template>
                <template v-else>
                  <el-option v-for="dict in system_field_type" :key="dict.value" :label="dict.label" :value="dict.value"></el-option>
                </template>
              </el-select>
            </el-form-item> -->
            <el-form-item class="noMarginRight">
              <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
              <el-button icon="Refresh" @click="resetQuery">重置</el-button>
            </el-form-item>
          </el-form>
        </div>
      </transition>
      <el-table v-loading="loading" border :data="extendedAttributeList" size="small" :default-sort="{prop: 'pageConfigAttributeSort', order: 'ascending'}" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="40" align="center" />
        <el-table-column type="expand">
          <template #default="props">
            <el-form label-position="left" inline size="small" label-width="auto" label-suffix="：" class="demo-table-expand p-2">
              <el-form-item label="属性id">
                <span>{{ props.row.pageConfigRuleId }}</span>
              </el-form-item>
              <el-form-item label="备注" style="width: 100%;">
                <span>{{ props.row.remark }}</span>
              </el-form-item>
              <el-form-item label="创建时间">
                <span>{{ props.row.createTime }}</span>
              </el-form-item>
            </el-form>
          </template>
        </el-table-column>
        <el-table-column label="id" align="center" prop="pageConfigRuleId" v-if="false" />
        <el-table-column label="交互规则名称" min-width="110px" align="center" prop="pageConfigRuleName" show-overflow-tooltip />
        <el-table-column label="条件" min-width="110px" align="center" prop="pageConfigRuleCondition" show-overflow-tooltip >
          <template #default="{row}">
            {{ formatCondition(row.pageConfigRuleConditionTran) }}
          </template>
        </el-table-column>
        <el-table-column label="执行动作" min-width="110px" align="center" prop="pageConfigRuleAction" show-overflow-tooltip>
          <template #default="{row}">
            {{ formatAciton(row.pageConfigRuleActionTran) }}
          </template>
        </el-table-column>
        <!-- <el-table-column :label="pageConfig.pageConfigDesc=='tableArea'?'展示类型':'类型'" width="80" align="center" prop="pageConfigAttributeType">
          <template #default="{row}">
            <dict-tag v-if="pageConfig.pageConfigDesc=='tableArea'" :options="system_display_type" :value="row.pageConfigAttributeType" />
            <dict-tag v-else :options="system_field_type" :value="row.pageConfigAttributeType" />
          </template>
        </el-table-column> -->
        <!-- <el-table-column label="规则" align="center" prop="pageConfigAttributeRule" /> -->
        <el-table-column label="是否启用" width="70" align="center" prop="pageConfigRuleEnable">
          <template #default="{row}">
            <el-switch v-model="row.pageConfigRuleEnable" active-value="1" inactive-value="0" @change="enableChange(row)" size="small"></el-switch>
          </template>
        </el-table-column>
        <el-table-column label="排序" width="50" align="center" prop="pageConfigRuleSort" :formatter="formatter" />
        <el-table-column width="70" label="操作" fixed="right" align="center" class-name="small-padding fixed-width">
          <template #default="scope">
            <el-tooltip content="修改" placement="top">
              <el-button v-hasPermi="['system:extended:edit']" link type="primary" icon="Edit" @click="handleUpdate(scope.row)"></el-button>
            </el-tooltip>
            <el-tooltip content="删除" placement="top">
              <el-button v-hasPermi="['system:extended:remove']" link type="danger" icon="Delete" @click="handleDelete(scope.row)"></el-button>
            </el-tooltip>
          </template>
        </el-table-column>
      </el-table>
      <pagination v-show="total > 0" v-model:page="queryParams.pageNum" size="small" v-model:limit="queryParams.pageSize" :total="total" @pagination="getList" />
    </el-card>
    

    <!-- 添加或修改属性对话框 -->
    <el-dialog v-model="dialog.visible" :title="dialog.title" width="850px" append-to-body>
      <el-form ref="formRef" v-if="dialog.visible" :model="form" :rules="rules" label-position="top" label-width="auto">
        <el-form-item label="交互规则名称" prop="pageConfigRuleName">
          <el-input v-model="form.pageConfigRuleName" placeholder="请输入交互规则名称" />
        </el-form-item>
        
        <el-form-item label="当满足以下条件时" prop="conditionFormDataArray">
          <template #label>
            当满足以下条件时  
            <el-button 
              style="margin-left: 15px;"
              @click="addCondition" 
              type="primary" 
              size="small"
              icon="Plus"
            >
              添加条件
            </el-button>
            <!-- <el-link :underline="false" type="primary" @click="addCondition">+ 添加条件</el-link> -->
          </template>
          <!-- <div style="margin-top: 10px; margin-bottom: 20px;">
            <el-button 
              @click="addCondition" 
              type="primary" 
              size="small"
              icon="Plus"
            >
              添加条件
            </el-button>
          </div> -->
          <!-- 使用数组批量渲染ConditionSelector组件 -->
          <el-card class="condition-card" :body-class="'condition-card-body'" shadow="hover" v-for="(conditionItem, index) in conditionFormDataArray" :key="index">
            <!-- <h4>条件 {{ index + 1 }}</h4> -->
            <ConditionSelector
              :ref="el => conditionRefs[index] = el"
              v-model="conditionFormDataArray[index]" 
              v-model:conditionLabel="conditionFormDataLabelArray[index]" 
              :index="index"
              :attr-list="attrList" 
              :system-condition="system_condition" 
            />
            <el-button 
              class="remove-condition-button"
              v-if="conditionFormDataArray.length > 1" 
              @click="removeCondition(index)" 
              type="danger" 
              size="small"
              icon="Delete"
              circle
            ></el-button>
          </el-card>
        </el-form-item>

        <el-form-item label="" prop="actions">
          <template #label>
            则执行动作  
            <el-button 
              style="margin-left: 15px;"
              @click="actionAdd" 
              type="primary" 
              size="small"
              icon="Plus"
            >
              添加动作
            </el-button>
            <!-- <el-link :underline="false" type="primary" @click="actionAdd">+ 添加动作</el-link> -->
          </template>
          <div v-for="(item, index) in actionList" :key="index" style="width: 100%;margin-bottom: 7px;">
            <el-select v-model="item.action" @change="actionChange($event, index)" filterable placeholder="请选择字段" style="margin-right: 5px;width: 15%;">
              <el-option v-for="dict in system_perform_action" :label="dict.label" :key="dict.value" :value="dict.value"></el-option>
            </el-select>
            <el-select v-model="item.target" placeholder="请选择" @change="targetChange($event, index)" multiple filterable clearable class="multipSelect">
              <el-option v-for="dict in attrList" :key="dict.pageConfigAttributeDesc" :label="dict.pageConfigAttributeName" :value="dict.pageConfigAttributeDesc"></el-option>
            </el-select>
            <el-button 
              v-if="actionList.length > 1" 
              @click="actionDel(index)" 
              type="danger" 
              size="small"
              icon="Delete"
              circle
            ></el-button>
            <!-- <el-icon @click="actionDel(index)" v-if="actionList.length > 1" style="color: brown;font-size: 17px;cursor: pointer;transform: translate(3px, 4px);">
              <delete />
            </el-icon> -->
          </div>
        </el-form-item>
        <el-form-item label="排序" prop="pageConfigRuleSort">
          <el-input v-model="form.pageConfigRuleSort" type="number" placeholder="请输入排序" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button :loading="buttonLoading" type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="BusinessObjectAttribute" lang="ts">
import {
  listPageRule, 
  getPageRule, 
  delPageRule, 
  addPageRule, 
  updatePageRule
} from '@/api/system/pageConfig/rule';
import { listPageAttribute } from '@/api/system/pageConfig/attribute';
import { usePageConfigStore } from '@/store/modules/pageConfig'
import ConditionSelector from '@/components/ConditionSelector/index.vue'

const props = defineProps({
  pageConfig: {
    type: Object,
    default: '',
    required: true
  },
  pageConfigName: {
    type: String,
    default: ''
  }
});

const { proxy } = getCurrentInstance() as ComponentInternalInstance;
const { system_field_type, system_display_type, system_condition, system_perform_action } = toRefs<any>(proxy?.useDict('system_field_type', 'system_display_type', 'system_condition', 'system_perform_action'));

const extendedAttributeList = ref([]);
const buttonLoading = ref(false);
const loading = ref(false);
const showSearch = ref(true);
const ids = ref<Array<string | number>>([]);
const names = ref<Array<string | number>>([]);
const single = ref(true);
const selectRow = ref({});
const multiple = ref(true);
const total = ref(0);

const queryFormRef = ref<ElFormInstance>();
const formRef = ref<ElFormInstance>();

// 条件组件引用数组
const conditionRefs = ref<any[]>([]);

const dialog = reactive({
  visible: false,
  loading: false,
  title: ''
});

const initForm = {
  pageConfigRuleId: '',
  pageConfigId: '',
  pageConfigRuleCode: '',
  pageConfigRuleName: '',
  pageConfigRuleCondition: '',
  pageConfigRuleAction: '',
  pageConfigRuleSort: '',
  pageConfigRuleEnable: '1',
  name: '',
  remark: '',
};

const data = reactive({
  form: { ...initForm },
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    pageConfigRuleId: '',
    pageConfigId: '',
    pageConfigRuleCode: '',
    pageConfigRuleName: '',
    pageConfigRuleCondition: '',
    pageConfigRuleAction: '',
    pageConfigRuleSort: '',
    pageConfigRuleEnable: '',
    remark: ''
  },
  rules: {
    pageConfigRuleName: [{ required: true, message: '交互规则名称不能为空', trigger: 'change' }],
    field: [{ required: true, message: '请选择字段', trigger: 'change' }],
    values: [{ required: true, message: '值不能为空', trigger: 'change' }]
  }
});

const { queryParams, form, rules } = toRefs(data);

// 格式化显示条件内容
const formatCondition = computed(() => {
  return (val) =>{
    if (!val) return '';
    const condition = JSON.parse(val);
    let conditionStr = '';
    condition.forEach((item, index) => {
      if (index > 0) conditionStr += ` [${item.relation}]`;
      conditionStr += `【${item.field}】  ${item.condition}`;
      if (item.operator === 'fixed') {
        conditionStr += item.values ? `  (${item.values})` : '';
      } else if (item.operator === 'dynamics') {
        conditionStr += item.values ? ` 【${item.values}】` : '';
      }
    })
    return conditionStr
  }
});
// 格式化显示执行动作内容
const formatAciton = computed(() => {
  return (val) =>{
    if (!val) return '';
    const actions = JSON.parse(val);
    let conditionStr = '';
    actions.forEach(item => {
      conditionStr += `${item.action}`;
      conditionStr += item.target ? ` 【${item.target.join(',')}】` : '';
    })
    return conditionStr
  }
});

// 条件表单数据数组
const conditionFormDataArray = ref([{
  field: '',
  condition: '==',
  values: '',
  operator: 'fixed',
  relation: '&&'
}]);
// 条件表单数据翻译数组
const conditionFormDataLabelArray = ref([{
  field: '',
  condition: '等于',
  values: '',
  operator: 'fixed',
  relation: '且'
}]);

/** 查询属性列表 */
const getList = async() => {
  loading.value = true;
  queryParams.value.pageConfigId = props.pageConfig.pageConfigId;
  const res = await listPageRule(queryParams.value);
  extendedAttributeList.value = res.rows;
  total.value = res.total;
  loading.value = false;
};

const attrList = ref([]);
// 查询业务对象属性
const getAttrList = () => {
  listPageAttribute({ pageConfigId: props.pageConfig.pageConfigId,pageNum: 1, pageNum: 999}).then(res=>{
    attrList.value = res.rows;
  })
};
watch(() => props.pageConfig.pageConfigId,(newValue) => {
    if (newValue) {
      getList();
      getAttrList();
    }else{
      extendedAttributeList.value = [];
    }
  }
);

const actionList = ref([{ target: "", action: "show"}]);
const actionLabels = ref([{ target: "", action: "显示"}]);
// 添加动作
const actionAdd = () => {
  actionList.value.push({ target: "", action: "show"})
  actionLabels.value.push({ target: "", action: "显示"})
};
const actionDel = (index) => {
  actionList.value.splice(index, 1)
  actionLabels.value.splice(index, 1)
};
const actionChange = (val, index) => {
  actionLabels.value[index].action = system_perform_action.value.find(item => item.value === val).label;
};
const targetChange = (values, index) => {
  actionLabels.value[index].target = values.map(val=>{
    return attrList.value.find(item => item.pageConfigAttributeDesc == val).pageConfigAttributeName;
  })
};

// 添加条件
const addCondition = () => {
  conditionFormDataArray.value.push({
    field: '',
    condition: '==',
    values: '',
    operator: 'fixed',
    relation: '&&'
  });
  conditionFormDataLabelArray.value.push({
    field: '',
    condition: '等于',
    values: '',
    operator: 'fixed',
    relation: '且'
  });
};

// 删除条件
const removeCondition = (index: number) => {
  conditionFormDataArray.value.splice(index, 1);
  conditionFormDataLabelArray.value.splice(index, 1);
  conditionRefs.value.splice(index, 1);
};

// 修改状态
const enableChange = async (row) =>{
  const text = row.pageConfigRuleEnable === '0' ? '停用' : '启用';
  try {
    await proxy?.$modal.confirm(`确认要${text} '${row.pageConfigRuleName}' 吗?`);
    await updatePageRule(row);
    // 清除缓存
    usePageConfigStore().removePageConfig(props.pageConfig.pageCode);
  } catch (err) {
    row.pageConfigRuleEnable = row.pageConfigRuleEnable === '0' ? '1' : '0';
  }
};

// 取消按钮
const cancel = () => {
  dialog.loading = false;
  dialog.visible = false;
};

/** 搜索按钮操作 */
const handleQuery = () => {
  queryParams.value.pageNum = 1;
  getList();
};

/** 重置按钮操作 */
const resetQuery = () => {
  queryFormRef.value?.resetFields();
  handleQuery();
};

// 多选框选中数据
const handleSelectionChange = (selection:any) => {
  ids.value = selection.map((item:any) => item.pageConfigRuleId);
  names.value = selection.map((item:any) => item.pageConfigRuleName);
  single.value = selection.length != 1;
  multiple.value = !selection.length;
  selectRow.value = selection[0]||{};
};

const pageConfigRuleId = ref('');
/** 新增按钮操作 */
const handleAdd = () => {
  reset();
  dialog.visible = true;
  dialog.loading = false;
  pageConfigRuleId.value = '';
  dialog.title = `添加【${props.pageConfig.pageConfigName}】交互规则`;
};

/** 修改按钮操作 */
const handleUpdate = async (row) => {
  reset();
  pageConfigRuleId.value = row?.pageConfigRuleId || ids.value[0];

  const res = await getPageRule(row.pageConfigRuleId) as any;
  Object.assign(form.value, res.data);
  if (res.data.pageConfigRuleCondition) {
    // 解析条件数据数组
    const conditions = JSON.parse(res.data.pageConfigRuleCondition);
    const conditionLabels = JSON.parse(res.data.pageConfigRuleConditionTran);
    // 确保是数组格式
    if (Array.isArray(conditions)) {
      conditionFormDataArray.value = conditions;
      conditionFormDataLabelArray.value = conditionLabels;
    } else {
      // 如果是单个对象，放入数组中
      conditionFormDataArray.value = [conditions];
      conditionFormDataLabelArray.value = [conditionLabels];
    }
  } else {
    // 默认初始化一个空条件
    conditionFormDataArray.value = [{
      field: '',
      condition: '==',
      values: '',
      operator: 'fixed',
      relation: '&&'
    }];
    // 默认初始化一个空条件
    conditionFormDataLabelArray.value = [{
      field: '',
      condition: '等于',
      values: '',
      operator: 'fixed',
      relation: '且'
    }];
  }
  dialog.visible = true;
  dialog.title = `修改【${props.pageConfig.pageConfigName}】交互规则`;
  dialog.loading = true;
  if (res.data.pageConfigRuleAction) {
    actionList.value = JSON.parse(res.data.pageConfigRuleAction);
  }
  if (res.data.pageConfigRuleActionTran) {
    actionLabels.value = JSON.parse(res.data.pageConfigRuleActionTran);
  }
};

/** 提交按钮 */
const submitForm = async () => {
  // 先验证主表单
  formRef.value?.validate(async (valid: boolean) => {
    if (!valid) return;

    // 再验证所有条件组件
    const conditionValidations = conditionRefs.value.map(ref => {
      return new Promise<boolean>(resolve => {
        if (ref && typeof ref.validate === 'function') {
          ref.validate((valid: boolean) => {
            resolve(valid);
          });
        } else {
          resolve(true);
        }
      });
    });

    const results = await Promise.all(conditionValidations);
    const allValid = results.every(valid => valid);

    if (allValid) {
      buttonLoading.value = true;
      
      const data = {
        ...form.value,
        pageConfigRuleCondition: JSON.stringify(conditionFormDataArray.value),
        pageConfigId: props.pageConfig.pageConfigId,
        pageConfigRuleAction: actionList.value ? JSON.stringify(actionList.value) : '',
        pageConfigRuleConditionTran: JSON.stringify(conditionFormDataLabelArray.value),
        pageConfigRuleActionTran: JSON.stringify(actionLabels.value)
      }
      
      if (data.pageConfigRuleId) {
        await updatePageRule(data).finally(() => (buttonLoading.value = false));
      } else {
        await addPageRule(data).finally(() => (buttonLoading.value = false));
      }
      // 清除缓存
      usePageConfigStore().removePageConfig(props.pageConfig.pageCode);
      proxy?.$modal.msgSuccess('操作成功');
      dialog.visible = false;
      dialog.loading = false;
      await getList();
    }
  });
};

/** 删除按钮操作 */
const handleDelete = async (row) => {
  const _ids = row?.pageConfigRuleId || ids.value;
  const _names = row?.pageConfigRuleName || names.value;
  await proxy?.$modal.confirm('是否确认删除名称为"' + _names + '"的交互规则？');
  loading.value = true;
  await delPageRule(_ids).finally(() => (loading.value = false));
  // 清除缓存
  usePageConfigStore().removePageConfig(props.pageConfig.pageCode);
  await getList();
  proxy?.$modal.msgSuccess('删除成功');
};

/** 重置新增的表单以及其他数据  */
const reset = () => {
  form.value = { ...initForm };
  conditionFormDataArray.value = [{
    field: '',
    condition: '==',
    values: '',
    operator: 'fixed',
    relation: '&&'
  }];
  conditionFormDataLabelArray.value = [{
    field: '',
    condition: '==',
    values: '',
    operator: 'fixed',
    relation: '且'
  }];
  actionList.value = [{ target: "", action: "show"}];
  actionLabels.value = [{ target: "", action: "显示"}];
  formRef.value?.resetFields();
  conditionRefs.value = [];
};

/** 导出按钮操作 */
const handleExport = () => {
  proxy?.download(
    'system/extended-attribute/export',
    {
      ...queryParams.value
    },
    `extended-attribute_${new Date().getTime()}.xlsx`
  );
};

const formatter = (row, column) => {
  return row.pageConfigRuleSort
};

// onMounted(() => {
//   getList();
// });
</script>

<style lang="scss" scoped>
.demo-table-expand {
  font-size: 0;
}
.demo-table-expand label {
  width: 90px;
  color: #99a9bf;
}
.demo-table-expand .el-form-item {
  margin-right: 0;
  margin-bottom: 0;
  width: 50%;
}
:deep(.noBorder){
  .el-select__wrapper{
    box-shadow: 0 0 0 1px transparent inset;
  }
} 
:deep(.multipSelect){
  width: calc(100% - 155px);
  margin-right: 5px;
  .el-tag{
    background: #ccc;
    color: #222;
  }
} 
:deep(.condition-card) {
  margin-bottom: 10px;
  width: 100%;
}
:deep(.condition-card-body) {
  padding: 10px !important;
  padding-bottom: 20px !important;
  position: relative;
}
.remove-condition-button {
  position: absolute;
  right: 10px;
  top: 10px;

}
</style>