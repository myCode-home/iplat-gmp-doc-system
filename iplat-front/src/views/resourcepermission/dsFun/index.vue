<template>
  <div class="p-2">
    <transition :enter-active-class="proxy?.animate.searchAnimate.enter" :leave-active-class="proxy?.animate.searchAnimate.leave">
      <div v-show="showSearch" class="mb-[10px]">
        <el-card shadow="hover">
          <el-form ref="queryFormRef" :model="queryParams" :inline="true" label-width="auto">
            <el-form-item label="编号" prop="dsFunCode">
              <el-input v-model="queryParams.dsFunCode" placeholder="请输入编号" clearable @change="handleQuery" @keyup.enter="handleQuery" />
            </el-form-item>
            <el-form-item label="名称" prop="dsFunName">
              <el-input v-model="queryParams.dsFunName" placeholder="请输入名称" clearable @change="handleQuery" @keyup.enter="handleQuery" />
            </el-form-item>
            <el-form-item label="mapper值" prop="dsFunMapper">
              <el-input v-model="queryParams.dsFunMapper" placeholder="请输入mapper值" clearable @change="handleQuery" @keyup.enter="handleQuery" />
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
            <el-button v-hasPermi="['system:dsFun:add']" type="primary" icon="Plus" @click="handleAdd">新增</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button v-hasPermi="['system:dsFun:edit']" type="primary" icon="Edit" :disabled="single" @click="handleUpdate(selectRow)"
              >修改</el-button
            >
          </el-col>
          <el-col :span="1.5">
            <el-button v-hasPermi="['system:dsFun:remove']" type="primary" icon="Delete" :disabled="multiple" @click="handleDelete">
              删除
            </el-button>
          </el-col>
          <el-col :span="1.5" v-if="false">
            <el-button v-hasPermi="['system:dsFun:export']" type="primary" icon="Download" @click="handleExport">导出</el-button>
          </el-col>
          <right-toolbar v-model:show-search="showSearch" @query-table="getList"></right-toolbar>
        </el-row>
      </template>

      <el-table v-loading="loading" border :data="dsFunList" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column label="序号" type="index" width="55" align="center" />
        <el-table-column label="编号" width="100" align="center" prop="dsFunCode" />
        <el-table-column label="名称" min-width="100" align="center" prop="dsFunName" />
        <el-table-column label="mapper值" min-width="120" align="center" prop="dsFunMapper" />
        <el-table-column label="URL" min-width="120" align="center" prop="dsFunUri" />
        <el-table-column label="菜单编号" width="120" align="center" prop="dsFunPage" />
        <el-table-column label="备注" align="center" prop="remark" show-overflow-tooltip />
        <el-table-column width="100" label="操作" align="center" fixed="right" class-name="small-padding fixed-width">
          <template #default="scope">
            <el-tooltip content="修改" placement="top">
              <el-button v-hasPermi="['system:dsFun:edit']" link type="primary" icon="Edit" @click="handleUpdate(scope.row)"></el-button>
            </el-tooltip>
            <el-tooltip content="删除" placement="top">
              <el-button v-hasPermi="['system:dsFun:remove']" link type="primary" icon="Delete" @click="handleDelete(scope.row)"></el-button>
            </el-tooltip>
          </template>
        </el-table-column>
      </el-table>
      <pagination v-show="total > 0" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :total="total" @pagination="getList" />
    </el-card>
    <!-- 添加或修改业务对象操作对话框 -->
    <el-dialog v-model="dialog.visible" :title="dialog.title" width="550px" append-to-body>
      <el-form ref="tenantFormRef" :model="form" v-loading="dialog.loading" :rules="rules" label-width="auto">
        <el-form-item label="编号" prop="dsFunCode">
          <el-input v-model="form.dsFunCode" placeholder="请输入编号" />
        </el-form-item>
        <el-form-item label="名称" prop="dsFunName">
          <el-input v-model="form.dsFunName" placeholder="请输入名称" />
        </el-form-item>
        <el-form-item label="mapper值" prop="dsFunMapper">
          <el-input v-model="form.dsFunMapper" placeholder="请输入mapper值" />
        </el-form-item>
        <el-form-item label="URL" prop="dsFunUri">
          <el-input v-model="form.dsFunUri" placeholder="请输入URL" />
        </el-form-item>
        <el-form-item label="菜单编号" prop="dsFunPage">
          <el-input v-model="form.dsFunPage" placeholder="请输入菜单编号" />
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

<script setup name="DsFun" lang="ts">
import {
  listDsFun, 
  getDsFun, 
  delDsFun, 
  addDsFun, 
  updateDsFun
} from '@/api/resourcepermission/dsFun';

const { proxy } = getCurrentInstance() as ComponentInternalInstance;

const dsFunList = ref([]);
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
  dsFunId: '',
  dsFunCode: '',
  dsFunName: '',
  dsFunMapper: '',
  dsFunUri: '',
  dsFunPage: '',
  remark: ''
};
const data = reactive({
  form: { ...initFormData },
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    dsFunId: '',
    dsFunCode: '',
    dsFunName: '',
    dsFunMapper: ''
  },
  rules: {
    dsFunCode: [{ required: false, message: '编号不能为空', trigger: 'blur' }],
    dsFunName: [{ required: true, message: '名称不能为空', trigger: 'blur' }],
    dsFunMapper: [{ required: true, message: 'mapper值不能为空', trigger: 'blur' }],
    dsFunUri: [{ required: true, message: 'URL不能为空', trigger: 'blur' }],
  }
});

const { queryParams, form, rules } = toRefs(data);


/** 查询业务对象操作列表 */
const getList = async () => {
  loading.value = true;
  const res = await listDsFun(queryParams.value);
  dsFunList.value = res.rows;
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
  ids.value = selection.map((item:any) => item.dsFunId);
  names.value = selection.map((item:any) => item.dsFunName);
  single.value = selection.length != 1;
  multiple.value = !selection.length;
  selectRow.value = selection[0]||{};
};

/** 新增按钮操作 */
const handleAdd = () => {
  reset();
  dialog.visible = true;
  dialog.loading = false;
  dialog.title = '添加业务对象操作';
};

/** 修改按钮操作 */
const handleUpdate = async (row) => {
  reset();
  const _id = row?.dsFunId || ids.value[0];
  dialog.visible = true;
  dialog.loading = true;
  dialog.title = '修改业务对象操作';
  const res = await getDsFun(_id);
  Object.assign(form.value, res.data);
  dialog.loading = false;
};

/** 提交按钮 */
const submitForm = () => {
  tenantFormRef.value?.validate(async (valid: boolean) => {
    if (valid) {
      buttonLoading.value = true;
      if (form.value.dsFunId) {
        await updateDsFun(form.value).finally(() => (buttonLoading.value = false));
      } else {
        await addDsFun(form.value).finally(() => (buttonLoading.value = false));
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
  const _ids = row?.dsFunId || ids.value;
  const _names = row?.dsFunName || names.value;
  await proxy?.$modal.confirm('是否确认删除名称为"' + _names + '"的数据项？');
  loading.value = true;
  await delDsFun(_ids).finally(() => (loading.value = false));
  await getList();
  proxy?.$modal.msgSuccess('删除成功');
};

/** 导出按钮操作 */
const handleExport = () => {
  proxy?.download(
    'system/ds-fun/export',
    {
      ...queryParams.value
    },
    `dsFun_${new Date().getTime()}.xlsx`
  );
};

onMounted(() => {
  getList();
});
</script>
