<template>
  <div class="">
    <el-card class="box-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <span>{{ extendedName?`【${extendedName}】`:'' }}扩展属性</span>
          <el-button v-if="extendedId" v-hasPermi="['system:extended:add']" @click="handleAdd" class="button" type="primary" size="small" icon="Plus" style="float: right;">新增</el-button>
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
            <el-form-item label="扩展属性名称" prop="extendedAttributeName">
              <el-input v-model="queryParams.extendedAttributeName" placeholder="请输入扩展属性名称" clearable @change="handleQuery" @keyup.enter="handleQuery" />
            </el-form-item>
            <el-form-item class="noMarginRight">
              <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
              <el-button icon="Refresh" @click="resetQuery">重置</el-button>
            </el-form-item>
          </el-form>
        </div>
      </transition>
      <el-table v-loading="loading" border :data="extendedAttributeList" size="small" @selection-change="handleSelectionChange">
        <el-table-column type="expand">
          <template #default="props">
            <el-form label-position="left" inline size="small" label-width="auto" label-suffix="：" class="demo-table-expand p-2">
              <el-form-item label="扩展属性id">
                <span>{{ props.row.extendedAttributeId }}</span>
              </el-form-item>
              <el-form-item label="扩展属性规则" style="width: 100%;">
                <span>{{ props.row.extendedAttributeRule }}</span>
              </el-form-item>
              <el-form-item label="扩展属性描述" style="width: 100%;">
                <span>{{ props.row.extendedAttributeDesc }}</span>
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
        <el-table-column label="编号" align="center" prop="extendedAttributeId" v-if="false" />
        <el-table-column label="扩展属性名称" min-width="120px" align="center" prop="extendedAttributeName" show-overflow-tooltip />
        <el-table-column label="扩展属性值" align="center" prop="extendedAttributeValue" show-overflow-tooltip />
        <el-table-column label="扩展属类型" align="center" prop="extendedAttributeType">
          <template #default="{row}">
            <dict-tag :options="system_expand_attribute_type" :value="row.extendedAttributeType" />
          </template>
        </el-table-column>
        <!-- <el-table-column label="扩展属性规则" align="center" prop="extendedAttributeRule" /> -->
        <!-- <el-table-column label="备注" align="center" prop="remark" show-overflow-tooltip /> -->
        <el-table-column width="80" label="操作" align="center" class-name="small-padding fixed-width">
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

    <!-- 添加或修改扩展属性对话框 -->
    <el-dialog v-model="dialog.visible" :title="dialog.title" width="550px" append-to-body>
      <el-form ref="attributeFormRef" :model="form" v-loading="dialog.loading" :rules="rules" label-width="auto">
        <el-form-item label="扩展属性名称" prop="extendedAttributeName">
          <el-input v-model="form.extendedAttributeName" placeholder="请输入扩展属性名称" />
        </el-form-item>
        <el-form-item label="扩展属性值" prop="extendedAttributeValue">
          <el-input v-model="form.extendedAttributeValue" placeholder="请输入扩展属性值" />
        </el-form-item>
        <el-form-item label="扩展属性类型" prop="extendedAttributeType">
          <el-select v-model="form.extendedAttributeType"  placeholder="请选择扩展属性类型">
            <el-option v-for="dict in system_expand_attribute_type" :key="dict.value" :label="dict.label" :value="dict.value"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="扩展属性规则" prop="extendedAttributeRule">
          <el-input v-model="form.extendedAttributeRule" type="textarea" :autosize="{ minRows: 2 }" placeholder="请输入扩展属性规则" />
        </el-form-item>
        <el-form-item label="扩展属性描述" prop="extendedAttributeDesc">
          <el-input v-model="form.extendedAttributeDesc" type="textarea" :autosize="{ minRows: 2 }" placeholder="请输入扩展属性描述" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" :autosize="{ minRows: 2 }" placeholder="请输入备注" />
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

<script setup name="ExtendedAttribute" lang="ts">
import {
  listExtendedAttribute, 
  getExtendedAttribute, 
  delExtendedAttribute, 
  addExtendedAttribute, 
  updateExtendedAttribute
} from '@/api/system/extended/attribute';

const props = defineProps({
  extendedId: {
    type: String,
    default: '',
    required: true
  },
  extendedName: {
    type: String,
    default: ''
  }
});

const { proxy } = getCurrentInstance() as ComponentInternalInstance;
const { system_expand_attribute_type } = toRefs<any>(proxy?.useDict('system_expand_attribute_type'));

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
  extendedId: '',
  extendedAttributeId: '',
  extendedAttributeValue: '',
  extendedAttributeName: '',
  extendedAttributeType: '',
  extendedAttributeDesc: '',
  extendedAttributeRule: '',
  remark: ''
};
const data = reactive({
  form: { ...initFormData },
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    extendedId: '',
    extendedAttributeId: '',
    extendedAttributeValue: '',
    extendedAttributeName: '',
    extendedAttributeType: '',
    extendedAttributeDesc: '',
    extendedAttributeRule: '',
    remark: ''
  },
  rules: {
    extendedAttributeName: [{ required: true, message: '扩展属性名称不能为空', trigger: 'blur' }],
    extendedContent: [{ required: true, message: '内容不能为空', trigger: 'blur' }],
  }
});

const { queryParams, form, rules } = toRefs(data);


watch(() => props.extendedId,(newValue) => {
    if (newValue) {
      getList();
    }else{
      extendedAttributeList.value = [];
    }
  }
);

/** 查询扩展属性列表 */
const getList = async () => {
  loading.value = true;
  queryParams.value.extendedId = props.extendedId;
  const res = await listExtendedAttribute(queryParams.value);
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
  ids.value = selection.map((item:any) => item.extendedAttributeId);
  names.value = selection.map((item:any) => item.extendedAttributeName);
  single.value = selection.length != 1;
  multiple.value = !selection.length;
  selectRow.value = selection[0]||{};
};

/** 新增按钮操作 */
const handleAdd = () => {
  reset();
  form.value.extendedId = props.extendedId;
  dialog.visible = true;
  dialog.loading = false;
  dialog.title = `添加【${props.extendedName}】扩展属性`;
};

/** 修改按钮操作 */
const handleUpdate = async (row) => {
  reset();
  const _id = row?.extendedAttributeId || ids.value[0];
  dialog.visible = true;
  dialog.loading = true;
  dialog.title = `修改【${props.extendedName}】扩展属性`;
  const res = await getExtendedAttribute(_id);
  Object.assign(form.value, res.data);
  dialog.loading = false;
};

/** 提交按钮 */
const submitForm = () => {
  attributeFormRef.value?.validate(async (valid: boolean) => {
    if (valid) {
      buttonLoading.value = true;
      if (form.value.extendedAttributeId) {
        await updateExtendedAttribute(form.value).finally(() => (buttonLoading.value = false));
      } else {
        await addExtendedAttribute(form.value).finally(() => (buttonLoading.value = false));
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
  const _ids = row?.extendedAttributeId || ids.value;
  const _names = row?.extendedAttributeName || names.value;
  await proxy?.$modal.confirm('是否确认删除扩展名称为"' + _names + '"的数据项？');
  loading.value = true;
  await delExtendedAttribute(_ids).finally(() => (loading.value = false));
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
