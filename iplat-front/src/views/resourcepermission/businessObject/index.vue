<template>
  <div class="p-2">
    <el-row :gutter="10">
      <el-col :xs="24" :sm="12">
        <el-card class="box-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <span>业务对象列表</span>
              <el-button v-hasPermi="['system:business:add']" @click="handleAdd" class="button" type="primary" size="small" icon="Plus" style="float: right;">新增</el-button>
            </div>
          </template>
          <!-- <el-row :gutter="10" class="mb8">
            <el-col :span="1.5">
              <el-button v-hasPermi="['system:business:add']" type="primary" plain icon="Plus" @click="handleAdd">新增</el-button>
            </el-col>
            <el-col :span="1.5">
              <el-button v-hasPermi="['system:business:edit']" type="success" plain icon="Edit" :disabled="single" @click="handleUpdate(selectRow)"
                >修改</el-button
              >
            </el-col>
            <el-col :span="1.5">
              <el-button v-hasPermi="['system:business:remove']" type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete">
                删除
              </el-button>
            </el-col>
            <el-col :span="1.5" v-if="false">
              <el-button v-hasPermi="['system:business:export']" type="warning" plain icon="Download" @click="handleExport">导出</el-button>
            </el-col>
            <right-toolbar v-model:show-search="showSearch" @query-table="getList"></right-toolbar>
          </el-row> -->
          <transition :enter-active-class="proxy?.animate.searchAnimate.enter" :leave-active-class="proxy?.animate.searchAnimate.leave">
            <div v-show="showSearch">
              <el-form ref="queryFormRef" :model="queryParams" size="small" :inline="true" label-width="auto" @submit.native.prevent="">
                <el-form-item label="编号" prop="businessCode">
                  <el-input v-model="queryParams.businessCode" placeholder="请输入业务对象编号" style="width: 150px;" clearable @change="handleQuery" @keyup.enter="handleQuery" />
                </el-form-item>
                <el-form-item label="名称" prop="businessName">
                  <el-input v-model="queryParams.businessName" placeholder="请输入业务对象名称" style="width: 150px;" clearable @change="handleQuery" @keyup.enter="handleQuery" />
                </el-form-item>
                <el-form-item class="noMarginRight">
                  <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
                  <el-button icon="Refresh" @click="resetQuery">重置</el-button>
                </el-form-item>
              </el-form>
            </div>
          </transition>
          <el-table ref="businessTableRef" v-loading="loading" border :data="BusinessList" size="small" highlight-current-row @current-change="handleCurrentChange">
            <el-table-column type="expand">
              <template #default="props">
                <el-form label-position="left" inline size="small" label-width="auto" label-suffix="：" class="demo-table-expand p-2">
                  <el-form-item label="业务对象ID">
                    <span>{{ props.row.businessId }}</span>
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
            <el-table-column label="编号" align="center" prop="businessId" v-if="false" />
            <el-table-column label="业务对象编号" width="100px" align="center" prop="businessCode" show-overflow-tooltip />
            <el-table-column label="业务对象名称" min-width="120px" align="center" prop="businessName" show-overflow-tooltip />
            <el-table-column label="业务表名" align="center" prop="businessTable" show-overflow-tooltip />
            <el-table-column width="80" label="操作" fixed="right" align="center" class-name="small-padding fixed-width">
              <template #default="scope">
                <el-tooltip content="修改" placement="top">
                  <el-button v-hasPermi="['system:business:edit']" link type="primary" icon="Edit" @click="handleUpdate(scope.row)"></el-button>
                </el-tooltip>
                <el-tooltip content="删除" placement="top">
                  <el-button v-hasPermi="['system:business:remove']" link type="danger" icon="Delete" @click="handleDelete(scope.row)"></el-button>
                </el-tooltip>
              </template>
            </el-table-column>
          </el-table>
          <pagination v-show="total > 0" v-model:page="queryParams.pageNum" size="small" v-model:limit="queryParams.pageSize" :total="total" @pagination="getList" />
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12">
        <Attribute ref="attributeRef" :businessId="selectRow.businessId" :businessName="selectRow.businessName"></Attribute>
      </el-col>
    </el-row>

    <!-- 添加或修改业务对象对话框 -->
    <el-dialog v-model="dialog.visible" :title="dialog.title" width="550px" append-to-body>
      <el-form ref="businessFormRef" :model="form" v-loading="dialog.loading" :rules="rules" label-width="auto">
        <el-form-item label="业务对象编号" prop="businessCode">
          <el-input v-model="form.businessCode" placeholder="请输入业务对象编号" />
        </el-form-item>
        <el-form-item label="业务对象名称" prop="businessName">
          <el-input v-model="form.businessName" placeholder="请输入业务对象名称" />
        </el-form-item>
        <el-form-item label="业务表名" prop="businessTable">
          <el-input v-model="form.businessTable" placeholder="请输入业务表名" />
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

<script setup name="BusinessObject" lang="ts">
import {
  listBusiness, 
  getBusiness, 
  delBusiness, 
  addBusiness, 
  updateBusiness
} from '@/api/resourcepermission/businessObject';
import Attribute from '@/views/resourcepermission/businessObject/attribute.vue';

const { proxy } = getCurrentInstance() as ComponentInternalInstance;

const BusinessList = ref([]);
const buttonLoading = ref(false);
const loading = ref(true);
const showSearch = ref(true);
const ids = ref<Array<string | number>>([]);
const names = ref<Array<string | number>>([]);
const single = ref(true);
const selectRow = ref({
  businessId: '',
  businessName: ''
});
const multiple = ref(true);
const total = ref(0);

const queryFormRef = ref<ElFormInstance>();
const businessFormRef = ref<ElFormInstance>();
const businessTableRef = ref<ElTableInstance>();
const attributeRef = ref();

const dialog = reactive({
  visible: false,
  loading: false,
  title: ''
});

const initFormData = {
  businessId: '',
  businessCode: '',
  businessName: '',
  businessTable: '',
  remark: ''
};
const data = reactive({
  form: { ...initFormData },
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    businessId: '',
    businessCode: '',
    businessName: '',
    businessTable: '',
    remark: ''
  },
  rules: {
    businessCode: [{ required: true, message: '业务对象编号不能为空', trigger: 'blur' }],
    businessName: [{ required: true, message: '业务对象名称不能为空', trigger: 'blur' }],
    businessTable: [{ required: false, message: '业务表名不能为空', trigger: 'blur' }],
  }
});

const { queryParams, form, rules } = toRefs(data);


/** 查询业务对象列表 */
const getList = async () => {
  loading.value = true;
  const res = await listBusiness(queryParams.value);
  BusinessList.value = res.rows;
  total.value = res.total;
  loading.value = false;
  setCurrentRow();
};

// 设置默认选中
const setCurrentRow = () => {
  // 默认选中第一条
  let index = 0;
  if(selectRow.value?.businessId){
    index = BusinessList.value.findIndex(item=>item.businessId==selectRow.value.businessId)||0;
    index = index >=0 ? index : 0;
  }
  selectRow.value = BusinessList.value[index]||{}
  businessTableRef.value.setCurrentRow(BusinessList.value[index]);
}

// 取消按钮
const cancel = () => {
  reset();
  dialog.loading = false;
  dialog.visible = false;
};

// 表单重置
const reset = () => {
  form.value = { ...initFormData };
  businessFormRef.value?.resetFields();
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
  ids.value = selection.map((item:any) => item.businessId);
  names.value = selection.map((item:any) => item.businessName);
  single.value = selection.length != 1;
  multiple.value = !selection.length;
  selectRow.value = selection[0]||{};
};

// 选中数据
const handleCurrentChange = (val:any) => {
  selectRow.value = val||{};
};


/** 新增按钮操作 */
const handleAdd = () => {
  reset();
  dialog.visible = true;
  dialog.loading = false;
  dialog.title = '添加业务对象';
};

/** 修改按钮操作 */
const handleUpdate = async (row) => {
  reset();
  const _id = row?.businessId || ids.value[0];
  dialog.visible = true;
  dialog.loading = true;
  dialog.title = '修改业务对象';
  const res = await getBusiness(_id);
  Object.assign(form.value, res.data);
  dialog.loading = false;
};

/** 提交按钮 */
const submitForm = () => {
  businessFormRef.value?.validate(async (valid: boolean) => {
    if (valid) {
      buttonLoading.value = true;
      if (form.value.businessId) {
        await updateBusiness(form.value).finally(() => (buttonLoading.value = false));
      } else {
        await addBusiness(form.value).finally(() => (buttonLoading.value = false));
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
  const _ids = row?.businessId || ids.value;
  const _names = row?.businessName || names.value;
  await proxy?.$modal.confirm('是否确认删除业务对象名称为"' + _names + '"的数据项？');
  loading.value = true;
  await delBusiness(_ids).finally(() => (loading.value = false));
  await getList();
  proxy?.$modal.msgSuccess('删除成功');
};

/** 导出按钮操作 */
const handleExport = () => {
  proxy?.download(
    'system/Business/export',
    {
      ...queryParams.value
    },
    `Business_${new Date().getTime()}.xlsx`
  );
};

onMounted(() => {
  getList();
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
