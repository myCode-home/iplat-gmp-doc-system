<template>
  <div class="">
    <el-card class="box-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <span>{{ businessName?`【${businessName}】`:'' }}对象属性列表</span>
          <el-button v-if="businessId" v-hasPermi="['system:extended:add']" @click="handleAdd" class="button" type="primary" size="small" icon="Plus" style="float: right;">新增</el-button>
          <el-button v-hasPermi="['system:extended:remove']" type="danger"  size="small" icon="Delete" :disabled="multiple" @click="handleDelete"  style="float: right;margin-right: 10px;">
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
            <el-form-item label="编号" prop="businessAttrCode">
              <el-input v-model="queryParams.businessAttrCode" placeholder="请输入编号" style="width: 150px;" clearable @change="handleQuery" @keyup.enter="handleQuery" />
            </el-form-item>
            <el-form-item label="名称" prop="businessAttrName">
              <el-input v-model="queryParams.businessAttrName" placeholder="请输入名称" style="width: 150px;" clearable @change="handleQuery" @keyup.enter="handleQuery" />
            </el-form-item>
            <el-form-item class="noMarginRight">
              <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
              <el-button icon="Refresh" @click="resetQuery">重置</el-button>
            </el-form-item>
          </el-form>
        </div>
      </transition>
      <el-table v-loading="loading" border :data="extendedAttributeList" size="small" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="40" align="center" />
        <el-table-column type="expand">
          <template #default="props">
            <el-form label-position="left" inline size="small" label-width="auto" label-suffix="：" class="demo-table-expand p-2">
              <el-form-item label="属性id">
                <span>{{ props.row.businessAttrId }}</span>
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
        <el-table-column label="id" align="center" prop="businessAttrId" v-if="false" />
        <el-table-column label="编号" min-width="120px" align="center" prop="businessAttrCode" show-overflow-tooltip />
        <el-table-column label="名称" min-width="120px" align="center" prop="businessAttrName" show-overflow-tooltip />
        <!-- <el-table-column label="表字段名" align="center" prop="businessAttrFiled" show-overflow-tooltip /> -->
        <el-table-column label="字段类型" align="center" prop="businessAttrType">
          <template #default="{row}">
            <dict-tag :options="system_business_attribute_type" :value="row.businessAttrType" />
          </template>
        </el-table-column>
        <el-table-column label="排序" width="60" align="center" prop="businessAttrSort" />
        <!-- <el-table-column label="备注" align="center" prop="remark" show-overflow-tooltip /> -->
        <el-table-column width="80" label="操作" fixed="right" align="center" class-name="small-padding fixed-width">
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
    <el-dialog v-model="dialog.visible" :title="dialog.title" width="70%" append-to-body>
      <el-form ref="attributeFormRef" :model="form" v-loading="dialog.loading" :rules="rules" label-width="auto">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="编号" prop="businessAttrCode">
              <el-input v-model="form.businessAttrCode" placeholder="请输入编号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="表字段名" prop="businessAttrFiled">
              <el-input v-model="form.businessAttrFiled" placeholder="请输入表字段名" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="名称" prop="businessAttrName">
              <el-input v-model="form.businessAttrName" placeholder="请输入名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="别名" prop="businessAttrAlias">
              <el-input v-model="form.businessAttrAlias" placeholder="请输入别名(显示用)" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="字段类型" prop="businessAttrType">
              <el-select v-model="form.businessAttrType"  placeholder="请选择字段类型">
                <el-option v-for="dict in system_business_attribute_type" :key="dict.value" :label="dict.label" :value="dict.value"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="默认值" prop="businessAttrDefault">
              <el-input v-model="form.businessAttrDefault" placeholder="请输入默认值" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="长度" prop="businessAttrLength">
              <el-input v-model="form.businessAttrLength" placeholder="请输入长度" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="数据格式" prop="businessAttrFormat">
              <el-input v-model="form.businessAttrFormat" placeholder="请输入数据格式(YYYY-MM-DD，千分位)" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="允许为空" prop="businessAttrIsNull">
              <el-radio-group v-model="form.businessAttrIsNull">
                <el-radio v-for="dict in system_yes_no" :key="dict.value" :value="dict.value">{{ dict.label }}</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="是否主键" prop="businessAttrIsKey">
              <el-radio-group v-model="form.businessAttrIsKey">
                <el-radio v-for="dict in system_yes_no" :key="dict.value" :value="dict.value">{{ dict.label }}</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="字段类型" prop="businessAttrFelidType">
              <el-select v-model="form.businessAttrFelidType"  placeholder="请选择字段类型">
                <el-option v-for="dict in system_key_attributes" :key="dict.value" :label="dict.label" :value="dict.value"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="排序" prop="businessAttrSort">
              <el-input v-model="form.businessAttrSort" type="number" placeholder="请输入排序" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="备注" prop="remark">
              <el-input v-model="form.remark" type="textarea" :autosize="{ minRows: 2 }" placeholder="请输入备注" />
            </el-form-item>
          </el-col>
        </el-row>
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
  listBusinessAttr, 
  getBusinessAttr, 
  delBusinessAttr, 
  addBusinessAttr, 
  updateBusinessAttr
} from '@/api/resourcepermission/businessObject/attribute';

const props = defineProps({
  businessId: {
    type: String,
    default: '',
    required: true
  },
  businessName: {
    type: String,
    default: ''
  }
});

const { proxy } = getCurrentInstance() as ComponentInternalInstance;
const { system_business_attribute_type, system_yes_no, system_key_attributes } = toRefs<any>(proxy?.useDict('system_business_attribute_type', 'system_yes_no', 'system_key_attributes'));

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
const attributeFormRef = ref<ElFormInstance>();

const dialog = reactive({
  visible: false,
  loading: false,
  title: ''
});

const initFormData = {
  businessId: '',
  businessAttrId: '',
  businessAttrCode: '',
  businessAttrName: '',
  businessAttrAlias: '',
  businessAttrDefault: '',
  businessAttrLength: '',
  businessAttrIsKey: '0',
  businessAttrIsNull: '1',
  businessAttrFormat: '',
  businessAttrFelidType: '0',
  businessAttrSort: '',
  businessAttrFiled: '',
  businessAttrType: '1',
  remark: ''
};
const data = reactive({
  form: { ...initFormData },
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    businessId: '',
    businessAttrId: '',
    businessAttrCode: '',
    businessAttrName: '',
    businessAttrAlias: '',
    businessAttrDefault: '',
    businessAttrLength: '',
    businessAttrIsKey: '',
    businessAttrIsNull: '',
    businessAttrFormat: '',
    businessAttrFelidType: '',
    businessAttrSort: '',
    businessAttrFiled: '',
    businessAttrType: '',
    remark: ''
  },
  rules: {
    businessAttrCode: [{ required: true, message: '编号不能为空', trigger: 'blur' }],
    businessAttrName: [{ required: true, message: '名称不能为空', trigger: 'blur' }],
    businessAttrAlias: [{ required: false, message: '别名不能为空', trigger: 'blur' }],
    businessAttrFiled: [{ required: false, message: '表字段名不能为空', trigger: 'blur' }],
    businessAttrType: [{ required: true, message: '数据类型不能为空', trigger: 'blur' }],
    businessAttrIsNull: [{ required: true, message: '请选择是否允许为空', trigger: 'blur' }],
    businessAttrFelidType: [{ required: true, message: '字段类型不能为空', trigger: 'blur' }],
  }
});

const { queryParams, form, rules } = toRefs(data);


watch(() => props.businessId,(newValue) => {
    if (newValue) {
      getList();
    }else{
      extendedAttributeList.value = [];
    }
  }
);

/** 查询属性列表 */
const getList = async () => {
  loading.value = true;
  queryParams.value.businessId = props.businessId;
  const res = await listBusinessAttr(queryParams.value);
  extendedAttributeList.value = res.rows;
  total.value = res.total;
  loading.value = false;
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
  attributeFormRef.value?.resetFields();
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
  ids.value = selection.map((item:any) => item.businessAttrId);
  names.value = selection.map((item:any) => item.businessAttrName);
  single.value = selection.length != 1;
  multiple.value = !selection.length;
  selectRow.value = selection[0]||{};
};

/** 新增按钮操作 */
const handleAdd = () => {
  reset();
  form.value.businessId = props.businessId;
  dialog.visible = true;
  dialog.loading = false;
  dialog.title = `添加【${props.businessName}】属性`;
};

/** 修改按钮操作 */
const handleUpdate = async (row) => {
  reset();
  const _id = row?.businessAttrId || ids.value[0];
  dialog.visible = true;
  dialog.loading = true;
  dialog.title = `修改【${props.businessName}】属性`;
  const res = await getBusinessAttr(_id);
  Object.assign(form.value, res.data);
  dialog.loading = false;
};

/** 提交按钮 */
const submitForm = () => {
  attributeFormRef.value?.validate(async (valid: boolean) => {
    if (valid) {
      buttonLoading.value = true;
      if (form.value.businessAttrId) {
        await updateBusinessAttr(form.value).finally(() => (buttonLoading.value = false));
      } else {
        await addBusinessAttr(form.value).finally(() => (buttonLoading.value = false));
      }
      proxy?.$modal.msgSuccess('操作成功');
      dialog.visible = false;
      dialog.loading = false;
      await getList();
    }
  });
};

/** 删除按钮操作 */
const handleDelete = async (row) => {
  const _ids = row?.businessAttrId || ids.value;
  const _names = row?.businessAttrName || names.value;
  await proxy?.$modal.confirm('是否确认删除名称为"' + _names + '"的数据项？');
  loading.value = true;
  await delBusinessAttr(_ids).finally(() => (loading.value = false));
  await getList();
  proxy?.$modal.msgSuccess('删除成功');
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

// onMounted(() => {
//   getList();
// });
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
