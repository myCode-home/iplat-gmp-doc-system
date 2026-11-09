<template>
  <div class="p-2">
    <el-row :gutter="10">
      <el-col :xs="24" :sm="12">
        <el-card class="box-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <span>页面配置列表</span>
              <el-button v-hasPermi="['system:pageConfig:add']" @click="handleAdd" class="button" type="primary" size="small" icon="Plus" style="float: right;">新增</el-button>
            </div>
          </template>
          <!-- <el-row :gutter="10" class="mb8">
            <el-col :span="1.5">
              <el-button v-hasPermi="['system:pageConfig:add']" type="primary" icon="Plus" @click="handleAdd">新增</el-button>
            </el-col>
            <el-col :span="1.5">
              <el-button v-hasPermi="['system:pageConfig:edit']" type="primary" icon="Edit" :disabled="single" @click="handleUpdate(selectRow)"
                >修改</el-button
              >
            </el-col>
            <el-col :span="1.5">
              <el-button v-hasPermi="['system:pageConfig:remove']" type="primary" icon="Delete" :disabled="multiple" @click="handleDelete">
                删除
              </el-button>
            </el-col>
            <el-col :span="1.5" v-if="false">
              <el-button v-hasPermi="['system:pageConfig:export']" type="primary" icon="Download" @click="handleExport">导出</el-button>
            </el-col>
            <right-toolbar v-model:show-search="showSearch" @query-table="getList"></right-toolbar>
          </el-row> -->
          <transition :enter-active-class="proxy?.animate.searchAnimate.enter" :leave-active-class="proxy?.animate.searchAnimate.leave">
            <div v-show="showSearch">
              <el-form ref="queryFormRef" :model="queryParams" size="small" :inline="true" label-width="auto" @submit.native.prevent="">
                <el-form-item label="编码" prop="pageCode">
                  <el-input v-model="queryParams.pageCode" placeholder="请输入编码" clearable @change="handleQuery" @keyup.enter="handleQuery" style="width: 150px;" />
                </el-form-item>
                <el-form-item label="名称" prop="pageConfigName">
                  <el-input v-model="queryParams.pageConfigName" placeholder="请输入名称" clearable @change="handleQuery" @keyup.enter="handleQuery" style="width: 150px;" />
                </el-form-item>
                <el-form-item label="业务对象" prop="pageConfigValue">
                  <el-select v-model="queryParams.pageConfigValue" filterable clearable :disabled="Boolean(form.pageConfigId)" @change="handleQuery" placeholder="请选择"  style="width: 150px;">
                    <el-option v-for="dict in businessList" :key="dict.businessId" :label="dict.businessName" :value="dict.businessId"></el-option>
                  </el-select>
                </el-form-item>
                <el-form-item class="noMarginRight">
                  <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
                  <el-button icon="Refresh" @click="resetQuery">重置</el-button>
                </el-form-item>
              </el-form>
            </div>
          </transition>
          <el-table ref="tableRef" v-loading="loading" border :data="pageConfigList" highlight-current-row size="small" @current-change="handleCurrentChange" @selection-change="handleSelectionChange">
            <!-- <el-table-column type="selection" width="55" align="center" /> -->
            <el-table-column label="id" min-width="100" align="center" prop="pageConfigId" v-if="false" />
            <el-table-column type="expand">
              <template #default="props">
                <el-form label-position="left" inline size="small" label-width="auto" label-suffix="：" class="demo-table-expand p-2">
                  <!-- <el-form-item label="属性id">
                    <span>{{ props.row.businessAttrId }}</span>
                  </el-form-item> -->
                  <el-form-item label="备注" style="width: 100%;">
                    <span>{{ props.row.remark }}</span>
                  </el-form-item>
                  <el-form-item label="创建时间">
                    <span>{{ props.row.createTime }}</span>
                  </el-form-item>
                </el-form>
              </template>
            </el-table-column>
            <el-table-column label="序号" type="index" width="45" align="center" />
            <el-table-column label="编码" min-width="100" align="center" prop="pageCode" show-overflow-tooltip />
            <el-table-column label="名称" min-width="100" align="center" prop="pageConfigName" show-overflow-tooltip />
            <el-table-column label="功能模块" width="80" align="center" prop="pageConfigDesc">
              <template #default="{row}">
                <dict-tag :options="system_functional_area" :value="row.pageConfigDesc" />
              </template>
            </el-table-column>
            <!-- <el-table-column label="值" min-width="120" align="center" prop="pageConfigValue" /> -->
            <!-- <el-table-column label="备注" align="center" prop="remark" /> -->
            <el-table-column width="70" label="操作" fixed="right" align="center" class-name="small-padding fixed-width">
              <template #default="scope">
                <el-tooltip content="修改" placement="top">
                  <el-button v-hasPermi="['system:pageConfig:edit']" link type="primary" icon="Edit" @click="handleUpdate(scope.row)"></el-button>
                </el-tooltip>
                <el-tooltip content="删除" placement="top">
                  <el-button v-hasPermi="['system:pageConfig:remove']" link type="primary" icon="Delete" @click="handleDelete(scope.row)"></el-button>
                </el-tooltip>
              </template>
            </el-table-column>
          </el-table>
          <pagination v-show="total > 0" v-model:page="queryParams.pageNum" size="small" v-model:limit="queryParams.pageSize" :total="total" @pagination="getList" />
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12">
        <el-tabs v-show="selectRow.pageConfigDesc == 'formArea'" v-model="activeName" type="border-card">
          <el-tab-pane label="属性配置" name="attr">
            <Attribute ref="attributeRef" :pageConfig="selectRow"></Attribute>
          </el-tab-pane>
          <el-tab-pane label="交互规则配置" name="execute">
            <execute-rule ref="executeRule" :pageConfig="selectRow"></execute-rule>
          </el-tab-pane>
        </el-tabs>
        <Attribute v-show="selectRow.pageConfigDesc !== 'formArea'" ref="attributeRef" :pageConfig="selectRow"></Attribute>
      </el-col>
    </el-row>
    <!-- <el-card shadow="hover">
      <pagination v-show="total > 0" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :total="total" @pagination="getList" />
    </el-card> -->
    <!-- 添加或修改页面配置对话框 -->
    <el-dialog v-model="dialog.visible" :title="dialog.title" width="550px" append-to-body>
      <el-form ref="pageConfigFormRef" :model="form" v-loading="dialog.loading" :rules="rules" label-width="auto">
        <!-- <el-form-item label="页面id" prop="pageConfigId">
          <el-input v-model="form.pageConfigId" placeholder="请输入页面id" />
        </el-form-item> -->
        <el-form-item label="编号" prop="pageCode">
          <el-input v-model="form.pageCode" placeholder="请输入编码" />
        </el-form-item>
        <el-form-item label="名称" prop="pageConfigName">
          <el-input v-model="form.pageConfigName" placeholder="请输入名称" />
        </el-form-item>
        <el-form-item label="功能模块" prop="pageConfigDesc">
          <el-select v-model="form.pageConfigDesc" @change="descChange" placeholder="请选择功能模块">
            <el-option v-for="dict in system_functional_area" :key="dict.value" :label="dict.label" :value="dict.value"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="关联业务对象" prop="pageConfigValue">
          <el-select v-model="form.pageConfigValue" filterable  placeholder="请选择关联业务对象">
            <el-option v-for="dict in businessList" :key="dict.businessId" :label="dict.businessName" :value="dict.businessId"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="尺寸" prop="pageConfigSize">
          <el-select v-model="form.pageConfigSize"  placeholder="请选择尺寸">
            <el-option v-for="dict in sizeOption" :key="dict.value" :label="dict.label" :value="dict.value"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="对齐方式" prop="pageConfigAlign">
          <el-select v-model="form.pageConfigAlign"  placeholder="请选择对齐方式">
            <template v-if="form.pageConfigDesc == 'formArea'">
              <el-option v-for="dict in formAlignOption" :key="dict.value" :label="dict.label" :value="dict.value"></el-option>
            </template>
            <template v-if="form.pageConfigDesc == 'searchArea'">
              <el-option v-for="dict in searchAlignOption" :key="dict.value" :label="dict.label" :value="dict.value"></el-option>
            </template>
            <template v-if="form.pageConfigDesc == 'tableArea'">
              <el-option v-for="dict in tableAlignOption" :key="dict.value" :label="dict.label" :value="dict.value"></el-option>
            </template>
          </el-select>
        </el-form-item>
        <el-form-item v-if="form.pageConfigDesc == 'formArea'" label="行列数" prop="pageConfigDisplay">
          <el-input-number v-model="form.pageConfigDisplay" :step="1" :min="1" :max="6" style="width: 100%;" :controls="false" placeholder="一行默认展示多少项(默认两项)"></el-input-number>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" autosize placeholder="请输入备注" />
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

<script setup name="PageConfig" lang="ts">
import {
  listPageConfig, 
  getPageConfig, 
  delPageConfig, 
  addPageConfig, 
  updatePageConfig,
  getPageConfigPage
} from '@/api/system/pageConfig';
import { listBusiness } from '@/api/resourcepermission/businessObject';
import Attribute from '@/views/system/pageConfig/attribute.vue' 
import ExecuteRule from '@/views/system/pageConfig/executeRule.vue' 
import { usePageConfigStore } from '@/store/modules/pageConfig'

const { proxy } = getCurrentInstance() as ComponentInternalInstance;

const { system_functional_area } = toRefs<any>(proxy?.useDict('system_functional_area'));

const pageConfigList = ref([]);
const businessList = ref([]);
const buttonLoading = ref(false);
const loading = ref(true);
const showSearch = ref(true);
const ids = ref<Array<string | number>>([]);
const names = ref<Array<string | number>>([]);
const pageCodes = ref<Array<string | number>>([]);
const single = ref(true);
const selectRow = ref({
  pageConfigId: '',
  pageConfigName: '',
  pageConfigDesc: ''
});
const multiple = ref(true);
const total = ref(0);
const sizeOption = [
  { label: '大', value: 'large' },
  { label: '中(默认)', value: 'default' },
  { label: '小', value: 'small' },
]
const formAlignOption = [
  { label: '左对齐', value: 'left' },
  { label: '右对齐(默认)', value: 'right' },
  { label: '顶部对齐', value: 'top' },
]
const searchAlignOption = [
  { label: '左对齐', value: 'left' },
  { label: '右对齐(默认)', value: 'right' },
  { label: '顶部对齐', value: 'top' },
  { label: '不显示', value: 'none' },
]
const tableAlignOption = [
  { label: '左对齐', value: 'left' },
  { label: '右对齐', value: 'right' },
  { label: '居中对齐(默认)', value: 'center' },
]

const activeName = ref('attr');

const queryFormRef = ref<ElFormInstance>();
const pageConfigFormRef = ref<ElFormInstance>();
const tableRef = ref<ElTableInstance>();

const dialog = reactive({
  visible: false,
  loading: false,
  title: ''
});

const initFormData = {
  pageConfigId: '',
  pageId: '',
  pageCode: '',
  pageConfigName: '',
  pageConfigDesc: 'formArea',
  pageConfigValue: '',
  pageConfigSize: 'default',
  pageConfigAlign: 'right',
  pageConfigDisplay: undefined,
  remark: ''
};
const data = reactive({
  form: { ...initFormData },
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    businessName: '',
    pageConfigId: '',
    pageCode: '',
    pageConfigName: '',
    pageConfigDesc: '',
    pageConfigValue: ''
  },
  rules: {
    pageCode: [{ required: true, message: '编号不能为空', trigger: 'blur' }],
    pageConfigName: [{ required: true, message: '名称不能为空', trigger: 'blur' }],
    pageConfigDesc: [{ required: true, message: '功能模块不能为空', trigger: 'blur' }],
    pageConfigValue: [{ required: true, message: '关联业务对象不能为空', trigger: 'blur' }],
  }
});

const { queryParams, form, rules } = toRefs(data);


/** 查询页面配置列表 */
const getList = async () => {
  loading.value = true;
  const res = await listPageConfig(queryParams.value);
  pageConfigList.value = res.rows;
  total.value = res.total;
  loading.value = false;
  setCurrentRow();
};

// 设置默认选中
const setCurrentRow = () => {
  // 默认选中第一条
  let index = 0;
  if(selectRow.value?.pageConfigId){
    index = pageConfigList.value.findIndex(item=>item.pageConfigId==selectRow.value.pageConfigId)||0;
    index = index >=0 ? index : 0;
  }
  selectRow.value = pageConfigList.value[index]||{}
  tableRef.value.setCurrentRow(pageConfigList.value[index]);
}

/** 查询业务对象列表 */
const getBusinessList = async () => {
  loading.value = true;
  const res = await listBusiness({ pageNum: 1, pageSize: 9999 });
  businessList.value = res.rows;
};

// 取消按钮
const cancel = () => {
  reset();
  dialog.loading = false;
  dialog.visible = false;
};

// 表单重置
const reset = () => {
  form.value = { ...initFormData };
  pageConfigFormRef.value?.resetFields();
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
  ids.value = selection.map((item:any) => item.pageConfigId);
  names.value = selection.map((item:any) => item.pageConfigName);
  pageCodes.value = selection.map((item:any) => item.pageCodes);
  single.value = selection.length != 1;
  multiple.value = !selection.length;
  selectRow.value = selection[0]||{};
};

// 选中数据
const handleCurrentChange = (val:any) => {
  selectRow.value = val||{};
};

const descChange = (val:any) => {
  if(val=='formArea'){
    form.value.pageConfigAlign = 'right';
  }else if(val=='searchArea'){
    form.value.pageConfigAlign = 'right'
  }else if(val=='tableArea'){
    form.value.pageConfigAlign = 'center'
  }
}

/** 新增按钮操作 */
const handleAdd = () => {
  reset();
  dialog.visible = true;
  dialog.loading = false;
  dialog.title = '添加页面配置';
};

/** 修改按钮操作 */
const handleUpdate = async (row) => {
  reset();
  const _id = row?.pageConfigId || ids.value[0];
  dialog.visible = true;
  dialog.loading = true;
  dialog.title = '修改页面配置';
  const res = await getPageConfig(_id);
  Object.assign(form.value, res.data);
  dialog.loading = false;
};

/** 提交按钮 */
const submitForm = () => {
  pageConfigFormRef.value?.validate(async (valid: boolean) => {
    if (valid) {
      buttonLoading.value = true;
      if (form.value.pageConfigId) {
        await updatePageConfig(form.value).finally(() => (buttonLoading.value = false));
      } else {
        await addPageConfig(form.value).finally(() => (buttonLoading.value = false));
      }
      // 清除缓存
      usePageConfigStore().removePageConfig(form.value.pageCode);
      proxy?.$modal.msgSuccess('操作成功');
      dialog.visible = false;
      dialog.loading = false;
      await getList();
    }
  });
};

/** 删除按钮操作 */
const handleDelete = async (row) => {
  const _ids = row?.pageConfigId || ids.value;
  const _names = row?.pageConfigName || names.value;
  const _pageCodes = row?.pageCode ? [row.pageCode] : pageCodes.value;
  await proxy?.$modal.confirm('是否确认删除序列号名称为"' + _names + '"的数据项？');
  loading.value = true;
  await delPageConfig(_ids).finally(() => (loading.value = false));
  // 删除缓存
  _pageCodes.forEach(item => {
    usePageConfigStore().removePageConfig(item);
  })
  await getList();
  proxy?.$modal.msgSuccess('删除成功');
};

/** 导出按钮操作 */
const handleExport = () => {
  proxy?.download(
    'system/tenant/export',
    {
      ...queryParams.value
    },
    `pageConfig_${new Date().getTime()}.xlsx`
  );
};

onMounted(() => {
  getList();
  getBusinessList();
});
</script>

<style scoped>
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
</style>
