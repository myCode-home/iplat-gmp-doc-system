<template>
  <div class="p-2">
    <transition :enter-active-class="proxy?.animate.searchAnimate.enter" :leave-active-class="proxy?.animate.searchAnimate.leave">
      <div v-show="showSearch" class="mb-[10px]">
        <el-card shadow="hover">
          <el-form ref="queryFormRef" :model="queryParams" :inline="true" label-width="auto">
            <el-form-item label="编号" prop="serialRuleCode">
              <el-input v-model="queryParams.serialRuleCode" placeholder="请输入编号" clearable @change="handleQuery" @keyup.enter="handleQuery" />
            </el-form-item>
            <el-form-item label="名称" prop="serialRuleName">
              <el-input v-model="queryParams.serialRuleName" placeholder="请输入名称" clearable @change="handleQuery" @keyup.enter="handleQuery" />
            </el-form-item>
            <el-form-item label="内容" prop="serialRuleContent">
              <el-input v-model="queryParams.serialRuleContent" placeholder="请输入内容" clearable @change="handleQuery" @keyup.enter="handleQuery" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
              <el-button icon="Refresh" @click="resetQuery">重置</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </div>
    </transition>

    <el-card shadow="hover">
      <template #header>
        <el-row :gutter="10" class="mb8">
          <el-col :span="1.5">
            <el-button v-hasPermi="['system:serialRule:add']" type="primary" icon="Plus" @click="handleAdd">新增</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button v-hasPermi="['system:serialRule:edit']" type="primary" icon="Edit" :disabled="single" @click="handleUpdate(selectRow)"
              >修改</el-button
            >
          </el-col>
          <el-col :span="1.5">
            <el-button v-hasPermi="['system:serialRule:remove']" type="primary" icon="Delete" :disabled="multiple" @click="handleDelete">
              删除
            </el-button>
          </el-col>
          <el-col :span="1.5" v-if="false">
            <el-button v-hasPermi="['system:serialRule:export']" type="primary" icon="Download" @click="handleExport">导出</el-button>
          </el-col>
          <right-toolbar v-model:show-search="showSearch" @query-table="getList"></right-toolbar>
        </el-row>
      </template>

      <el-table v-loading="loading" border :data="serialRuleList" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column label="序号" type="index" width="55" align="center" />
        <el-table-column label="编号" min-width="100" align="center" prop="serialRuleCode" />
        <el-table-column label="名称" min-width="100" align="center" prop="serialRuleName" />
        <el-table-column label="内容" min-width="120" align="center" prop="serialRuleContent" />
        <el-table-column label="备注" align="center" prop="remark" />
        <el-table-column label="创建者" align="center" prop="createBy" />
        <el-table-column label="创建时间" width="120" align="center" prop="createTime">
          <template #default="scope">
            <span>{{ proxy.parseTime(scope.row.createTime, '{y}-{m}-{d}') }}</span>
          </template>
        </el-table-column>
        <el-table-column label="更新者" align="center" prop="updateBy" />
        <el-table-column label="更新时间" width="120" align="center" prop="updateTime">
          <template #default="scope">
            <span>{{ proxy.parseTime(scope.row.updateTime, '{y}-{m}-{d}') }}</span>
          </template>
        </el-table-column>
        <el-table-column width="100" label="操作" align="center" fixed="right" class-name="small-padding fixed-width">
          <template #default="scope">
            <el-tooltip content="修改" placement="top">
              <el-button v-hasPermi="['system:serialRule:edit']" link type="primary" icon="Edit" @click="handleUpdate(scope.row)"></el-button>
            </el-tooltip>
            <el-tooltip content="删除" placement="top">
              <el-button v-hasPermi="['system:serialRule:remove']" link type="primary" icon="Delete" @click="handleDelete(scope.row)"></el-button>
            </el-tooltip>
          </template>
        </el-table-column>
      </el-table>
      <pagination v-show="total > 0" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :total="total" @pagination="getList" />
    </el-card>
    <!-- 添加或修改序列号管理对话框 -->
    <el-dialog v-model="dialog.visible" :title="dialog.title" width="550px" append-to-body>
      <el-form ref="tenantFormRef" :model="form" v-loading="dialog.loading" :rules="rules" label-width="70px">
        <el-form-item label="编号" prop="serialRuleCode">
          <el-input v-model="form.serialRuleCode" placeholder="请输入编号" />
        </el-form-item>
        <el-form-item label="名称" prop="serialRuleName">
          <el-input v-model="form.serialRuleName" placeholder="请输入名称" />
        </el-form-item>
        <el-form-item label="内容" prop="serialRuleContent">
          <el-input v-model="form.serialRuleContent" type="textarea" :autosize="{ minRows: 2 }" placeholder="请输入内容" />
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

<script setup name="SerialRule" lang="ts">
import {
  listSerialRule, 
  getSerialRule, 
  delSerialRule, 
  addSerialRule, 
  updateSerialRule
} from '@/api/system/serialRule';

const { proxy } = getCurrentInstance() as ComponentInternalInstance;

const serialRuleList = ref([]);
const buttonLoading = ref(false);
const loading = ref(true);
const showSearch = ref(true);
const ids = ref<Array<string | number>>([]);
const names = ref<Array<string | number>>([]);
const single = ref(true);
const selectRow = ref({});
const multiple = ref(true);
const total = ref(0);

const queryFormRef = ref<ElFormInstance>();
const tenantFormRef = ref<ElFormInstance>();

const dialog = reactive({
  visible: false,
  loading: false,
  title: ''
});

const initFormData = {
  serialRuleId: '',
  serialRuleCode: '',
  serialRuleName: '',
  serialRuleContent: '',
  remark: ''
};
const data = reactive({
  form: { ...initFormData },
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    serialRuleId: '',
    serialRuleCode: '',
    serialRuleName: '',
    serialRuleContent: ''
  },
  rules: {
    serialRuleCode: [{ required: true, message: '编号不能为空', trigger: 'blur' }],
    serialRuleName: [{ required: true, message: '名称不能为空', trigger: 'blur' }],
    serialRuleContent: [{ required: true, message: '内容不能为空', trigger: 'blur' }],
  }
});

const { queryParams, form, rules } = toRefs(data);


/** 查询序列号管理列表 */
const getList = async () => {
  loading.value = true;
  const res = await listSerialRule(queryParams.value);
  serialRuleList.value = res.rows;
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
  tenantFormRef.value?.resetFields();
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
  ids.value = selection.map((item:any) => item.serialRuleId);
  names.value = selection.map((item:any) => item.serialRuleName);
  single.value = selection.length != 1;
  multiple.value = !selection.length;
  selectRow.value = selection[0]||{};
};

/** 新增按钮操作 */
const handleAdd = () => {
  reset();
  dialog.visible = true;
  dialog.loading = false;
  dialog.title = '添加序列号管理';
};

/** 修改按钮操作 */
const handleUpdate = async (row) => {
  reset();
  const _id = row?.serialRuleId || ids.value[0];
  dialog.visible = true;
  dialog.loading = true;
  dialog.title = '修改序列号管理';
  const res = await getSerialRule(_id);
  Object.assign(form.value, res.data);
  dialog.loading = false;
};

/** 提交按钮 */
const submitForm = () => {
  tenantFormRef.value?.validate(async (valid: boolean) => {
    if (valid) {
      buttonLoading.value = true;
      if (form.value.serialRuleId) {
        await updateSerialRule(form.value).finally(() => (buttonLoading.value = false));
      } else {
        await addSerialRule(form.value).finally(() => (buttonLoading.value = false));
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
  const _ids = row?.serialRuleId || ids.value;
  const _names = row?.serialRuleName || names.value;
  await proxy?.$modal.confirm('是否确认删除序列号名称为"' + _names + '"的数据项？');
  loading.value = true;
  await delSerialRule(_ids).finally(() => (loading.value = false));
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
    `serialRule_${new Date().getTime()}.xlsx`
  );
};

onMounted(() => {
  getList();
});
</script>
