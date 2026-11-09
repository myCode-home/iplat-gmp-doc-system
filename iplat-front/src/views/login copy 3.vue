<template>
  <div class="p-2">
    <el-card shadow="hover">
      <sup-table 
        :columns="columns"
        :data="serialRuleList"
        @selection-change="handleSelectionChange"
      >
      </sup-table>
      <!-- <el-table v-loading="loading" border :data="serialRuleList" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column 
          v-for="col in columns" 
          :key="col.prop"
          :prop="col.prop" 
          :label="col.label" 
          :width="col.width"
          :min-width="col.minWidth"
          :sortable="col.sortable">
        </el-table-column>
        <el-table-column width="150" label="操作" align="center" fixed="right" class-name="small-padding fixed-width">
          <template #default="scope">
            <el-tooltip content="修改" placement="top">
              <el-button v-hasPermi="['system:tenant:edit']" link type="primary" icon="Edit" @click="handleUpdate(scope.row)"></el-button>
            </el-tooltip>
            <el-tooltip content="删除" placement="top">
              <el-button v-hasPermi="['system:tenant:remove']" link type="primary" icon="Delete" @click="handleDelete(scope.row)"></el-button>
            </el-tooltip>
          </template>
        </el-table-column>
      </el-table> -->
      <pagination v-show="total > 0" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :total="total" @pagination="getList" />
    </el-card>
  </div>
</template>

<script setup name="SerialRule" lang="ts">
import DynamicForm from './DynamicForm.vue'
import SupTable from './supTable.vue'
const { proxy } = getCurrentInstance() as ComponentInternalInstance;

const serialRuleList = ref([
  { id: 1, name: '张三', age: 25, gender: '男', address: '北京市朝阳区' },
  { id: 2, name: '李四', age: 30, gender: '女', address: '上海市浦东新区' },
  { id: 3, name: '王五', age: 28, gender: '男', address: '广州市天河区' },
  { id: 4, name: '赵六', age: 35, gender: '女', address: '深圳市南山区' }
]);
const packageList = ([]);
const buttonLoading = ref(false);
const loading = ref(false);
const showSearch = ref(true);
const ids = ref<Array<string | number>>([]);
const single = ref(true);
const selectRow = ref({});
const multiple = ref(true);
const total = ref(0);

const queryFormRef = ref<ElFormInstance>();
// 表格列配置
const columns = ref([
  { prop: 'name', label: '姓名', width: '120', sortable: true },
  { prop: 'age', label: '年龄', width: '100', sortable: true },
  { prop: 'gender', label: '性别', width: '100' },
  { prop: 'address', label: '地址', minWidth: '200' }
]);

const initFormData = {
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
    serialRuleCode: '',
    serialRuleName: '',
    serialRuleContent: ''
  },
  formRules: {
    serialRuleCode: [{ required: true, message: '编号不能为空', trigger: 'blur' }],
    serialRuleName: [{ required: true, message: '名称不能为空', trigger: 'blur' }],
    serialRuleContent: [{ required: true, message: '内容不能为空', trigger: 'blur' }],
  }
});

const { queryParams, form, formRules } = toRefs(data);

const inputChange = (e) => {
  console.log(111,e)
};

/** 查询所有序列号管理套餐 */
const getTenantPackage = async () => {
  const res = await selectTenantPackage();
  packageList.value = res.data;
};

/** 查询序列号管理列表 */
const getList = async () => {
  loading.value = true;
  const res = await listTenant(queryParams.value);
  serialRuleList.value = res.rows;
  total.value = res.total;
  loading.value = false;
};

// 取消按钮
const cancel = () => {
  reset();
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
  console.log(selection)
  ids.value = selection.map((item:any) => item.id);
  single.value = selection.length != 1;
  multiple.value = !selection.length;
  selectRow.value = selection[0]||{};
};

/** 新增按钮操作 */
const handleAdd = () => {
  reset();
  // getTenantPackage();
  dialog.visible = true;
  dialog.title = '添加序列号管理';
};

/** 修改按钮操作 */
const handleUpdate = async (row) => {
  reset();
  await getTenantPackage();
  const _id = row?.id || ids.value[0];
  // const res = await getTenant(_id);
  // Object.assign(form.value, res.data);
  const res = await getTenant(_id);
  Object.assign(form.value, {...row});
  dialog.visible = true;
  dialog.title = '修改序列号管理';
};

/** 提交按钮 */
const submitForm = () => {
  formRef.value.validate(async (valid: boolean) => {
    if (valid) {
      const data = formRef.value.getFormData();
      buttonLoading.value = true;
      if (data.serialRuleCode) {
        await updateTenant(data).finally(() => (buttonLoading.value = false));
      } else {
        await addTenant(data).finally(() => (buttonLoading.value = false));
      }
      proxy?.$modal.msgSuccess('操作成功');
      dialog.visible = false;
      await getList();
    }
  })
  // tenantFormRef.value?.validate(async (valid: boolean) => {
  //   if (valid) {
  //     buttonLoading.value = true;
  //     if (form.value.serialRuleCode) {
  //       await updateTenant(form.value).finally(() => (buttonLoading.value = false));
  //     } else {
  //       await addTenant(form.value).finally(() => (buttonLoading.value = false));
  //     }
  //     proxy?.$modal.msgSuccess('操作成功');
  //     dialog.visible = false;
  //     await getList();
  //   }
  // });
};

/** 删除按钮操作 */
const handleDelete = async (row) => {
  const _ids = row?.id || ids.value;
  await proxy?.$modal.confirm('是否确认删除序列号管理编号为"' + _ids + '"的数据项？');
  loading.value = true;
  await delTenant(_ids).finally(() => (loading.value = false));
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
  // getList();
});
</script>
