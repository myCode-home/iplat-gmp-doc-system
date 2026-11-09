<template>
  <div class="p-2">
    <transition :enter-active-class="proxy?.animate.searchAnimate.enter" :leave-active-class="proxy?.animate.searchAnimate.leave">
      <div v-show="showSearch" class="mb-[10px]">
        <el-card shadow="hover">
          <el-form ref="queryFormRef" :model="queryParams" :inline="true">
            <el-form-item label="编号" prop="serialRuleCode">
              <el-input v-model="queryParams.serialRuleCode" placeholder="请输入编号" clearable @keyup.enter="handleQuery" />
            </el-form-item>
            <el-form-item label="名称" prop="serialRuleName">
              <el-input v-model="queryParams.serialRuleName" placeholder="请输入名称" clearable @keyup.enter="handleQuery" />
            </el-form-item>
            <el-form-item label="内容" prop="serialRuleContent">
              <el-input v-model="queryParams.serialRuleContent" placeholder="请输入内容" clearable @keyup.enter="handleQuery" />
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
            <el-button type="primary" plain icon="Plus" @click="handleAdd">新增</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button v-hasPermi="['system:tenant:edit']" type="success" plain icon="Edit" :disabled="single" @click="handleUpdate(selectRow)"
              >修改</el-button
            >
          </el-col>
          <el-col :span="1.5">
            <el-button v-hasPermi="['system:tenant:remove']" type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete(selectRow)">
              删除
            </el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button v-hasPermi="['system:tenant:export']" type="warning" plain icon="Download" @click="handleExport">导出</el-button>
          </el-col>
          <right-toolbar v-model:show-search="showSearch" @query-table="getList"></right-toolbar>
        </el-row>
      </template>

      <el-table v-loading="loading" border :data="serialRuleList" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column label="编号" align="center" prop="serialRuleCode" />
        <el-table-column label="名称" align="center" prop="serialRuleName" />
        <el-table-column label="内容" align="center" prop="serialRuleContent" />
        <el-table-column label="备注" align="center" prop="companyName" />
        <el-table-column label="创建者" align="center" prop="licenseNumber" />
        <el-table-column label="创建时间" align="center" prop="licenseNumber" />
        <el-table-column label="更新者" align="center" prop="licenseNumber" />
        <el-table-column label="更新时间" align="center" prop="expireTime" width="180">
          <template #default="scope">
            <span>{{ proxy.parseTime(scope.row.expireTime, '{y}-{m}-{d}') }}</span>
          </template>
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
      </el-table>
      <pagination v-show="total > 0" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :total="total" @pagination="getList" />
    </el-card>
    <!-- 添加或修改序列号管理对话框 -->
    <el-dialog v-model="dialog.visible" :title="dialog.title" width="70%" append-to-body>
      <DynamicForm 
        ref="formRef"
        v-if="dialog.visible"
        :config="config" 
        :form-rules="formRules" 
        :methods="formMethods"
      >
        <template #zdy="{ formData, prop }">
          <el-input v-model="formData[prop]" placeholder="请输入编号" />
        </template>
      </DynamicForm>
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
import DynamicForm from './DynamicForm.vue'

const { proxy } = getCurrentInstance() as ComponentInternalInstance;

const serialRuleList = ref([]);
const packageList = ([]);
const buttonLoading = ref(false);
const loading = ref(true);
const showSearch = ref(true);
const ids = ref<Array<string | number>>([]);
const single = ref(true);
const selectRow = ref({});
const multiple = ref(true);
const total = ref(0);

const queryFormRef = ref<ElFormInstance>();
const tenantFormRef = ref<ElFormInstance>();
const formRef = ref(null);

const dialog = reactive<DialogOption>({
  visible: false,
  title: ''
});
const config = {
  span: 12,
  formItems: [
    {
      type: 'select',
      label: {'zh': '用户类型', 'en': 'User Type'},
      prop: 'userType',
      required: true,
      options: [
        { label: '个人', value: 'personal' },
        { label: '企业', value: 'enterprise' }
      ],
      region:'基本信息',
      events: {
        "change": "handleRoleChange"
      }
    },
    {
      type: 'input',
      label: '个人名称',
      required: true,
      region:'基本信息',
      prop: 'personName'
    },
    {
      type: 'select',
      label: '性别',
      prop: 'sex',
      region:'基本信息',
      code: 'sys_user_sex',
    },
    {
      type: 'input',
      label: '年龄',
      prop: 'age',
      region:'基本信息',
    },
    {
      type: 'input',
      label: '职务',
      prop: 'zhiwu',
      region:'公司职务',
    },
    {
      type: 'input',
      label: '工号',
      prop: 'gonhao',
      region:'公司职务',
    },
    {
      type: 'input',
      label: '公司名称',
      prop: 'companyName'
    }
  ],
  interactionRules: [
    {
      name: '显示企业相关字段',
      condition: "userType == 'enterprise'",
      actions: [
        { target: 'companyName', action: 'show' },
        { target: 'companyName', action: 'required' },
        { target: 'personName', action: 'optional' },
        { target: "sex", action: "hide"},
        { target: "gonhao", action: "show"},
        { target: "zhiwu", action: "show"},
        { target: 'companyName', action: 'editable' },
        { target: "companyData", action: "custom", method: "processCompanyData"}
      ]
    },{
      name: '隐藏企业相关字段',
      condition: "userType == 'personal'",
      actions: [
        { target: 'personName', action: 'required' },
        { target: 'sex', action: 'show' },
        { target: "zhiwu", action: "hide"},
        { target: "gonhao", action: "hide"},
        { target: 'companyName', action: 'readonly' },
        { target: "companyName", action: "custom", method: "customChangeHandler"}
      ]
    }
  ],
}
// const formConfig = ref({formItems:[],interactionRules:[]})
setTimeout(()=>{
  // formConfig.value = config
  // formConfig.length = 0
  // formConfig.push(...config)
},2000)

// 在父组件中预定义所有可能用到的方法
const formMethods = {
  // args参数集合 childFrom参数集合
  customChangeHandler: (args: Array<any>,childFrom: Object) => {
    formRef.value.updateData({ companyName: 'admin' })
    console.log('自定义change事件触发')
  },
  handleRoleChange: (args: Array<any>,childFrom: Object) => {
    console.log('自定义事件触发', args)
  },
  handleSexChange: (args: Array<any>,childFrom: Object) => {
    // if(args[0] == '2'){
    //   const index = formConfig.findIndex(item=>item.prop == 'zdy')
    //   formConfig[index].visible = true
    // }
    console.log('自定义事件触发', args)
  },
  processCompanyData: (args: Array<any>,childFrom: Object) => {
    console.log('自定义数据处理',args,childFrom)
  }
}

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
