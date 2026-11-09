<template>
  <div class="p-2">
    <transition :enter-active-class="proxy?.animate.searchAnimate.enter" :leave-active-class="proxy?.animate.searchAnimate.leave">
      <div v-show="showSearch" class="mb-[10px]">
        <el-card shadow="hover">
          <!-- <el-form ref="queryFormRef" :model="queryParams" :inline="true">
            <el-form-item label="key键" prop="testKey">
              <el-input v-model="queryParams.testKey" placeholder="请输入key键" clearable @keyup.enter="handleQuery" />
            </el-form-item>
            <el-form-item label="值" prop="value">
              <el-input v-model="queryParams.value" placeholder="请输入值" clearable @keyup.enter="handleQuery" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
              <el-button icon="Refresh" @click="resetQuery">重置</el-button>
            </el-form-item>
          </el-form> -->
          <super-search
            code="leaveApplySearch"
            v-model="queryParams"
            auto-get-list
            @getList="getList"
            @resetQuery="resetQuery"
          >
            <template #leaveDaysForm="config">
              <el-tooltip
                class="item"
                effect="dark"
                :disabled="config.pageConfigAlign!='none'"
                :content="config.label"
                placement="top"
              >
                <span>
                  <el-form-item :label="config.pageConfigAlign!='none'?config.label:''" prop="config.prop">
                    <el-input v-model="queryParams.startLeaveDays" placeholder="请输入请假天数" clearable @keyup.enter="handleQuery" />
                  </el-form-item>
                  <el-form-item prop="endLeaveDays"> 至 </el-form-item>
                  <el-form-item prop="endLeaveDays">
                    <el-input v-model="queryParams.endLeaveDays" placeholder="请输入请假天数" clearable @keyup.enter="handleQuery" />
                  </el-form-item>
                </span>
              </el-tooltip>
            </template>
          </super-search>
        </el-card>
      </div>
    </transition>

    <el-card shadow="hover">
      <template #header>
        <el-row :gutter="10" class="mb8">
          <el-col :span="1.5">
            <el-button v-hasPermi="['demo:demo:add']" type="primary" plain icon="Plus" @click="handleAdd">新增</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button v-hasPermi="['demo:demo:edit']" type="success" plain icon="Edit" :disabled="single" @click="handleUpdate()">修改</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button v-hasPermi="['demo:demo:remove']" type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete()"
              >删除</el-button
            >
          </el-col>
          <el-col :span="1.5">
            <el-button v-hasPermi="['demo:demo:export']" type="warning" plain icon="Download" @click="handleExport">导出</el-button>
          </el-col>
          <right-toolbar v-model:show-search="showSearch" @query-table="refresh"></right-toolbar>
        </el-row>
      </template>
      <super-table 
        v-if="showTable"
        code="leaveApply"
        :dataLoading="loading"
        :data="leaveList"
        v-model:queryParams="queryParams"
        :total="total"
        @selection-change="handleSelectionChange"
        @getList="getList"
      >
        <!-- 开始时间表格列插槽 -->
        <template #startDateColumn="config">
          <el-table-column v-bind="config">
            <template #header="scope">
              <span style="color: #f56c6c;margin-right: 3px;">*</span>{{ scope.column.label }}
            </template>
            <template #default="scope">
              {{ parseTime(scope.row[config.prop], '{y}-{m}-{d}') }}
            </template>
          </el-table-column>
        </template>
        <!-- 操作列 -->
        <template #operation="scope">
          <el-tooltip content="修改" placement="top" v-if="scope.row.status === 'draft' || scope.row.status === 'cancel' || scope.row.status === 'back'">
            <el-button link type="primary" v-hasPermi="['workflow:leave:edit']" icon="Edit" @click="handleUpdate(scope.row)"></el-button>
          </el-tooltip>
          <el-tooltip content="删除" placement="top" v-if="scope.row.status === 'draft' || scope.row.status === 'cancel' || scope.row.status === 'back'">
            <el-button link type="danger" v-hasPermi="['workflow:leave:remove']" icon="Delete" @click="handleDelete(scope.row)"></el-button>
          </el-tooltip>
          <el-tooltip content="查看" placement="top">
            <el-button link type="primary" icon="View" @click="handleView(scope.row)"></el-button>
          </el-tooltip>
          <el-tooltip content="撤销" placement="top" v-if="scope.row.status === 'waiting'">
            <el-button link type="danger" icon="Notification" @click="handleCancelProcessApply(scope.row.id)"></el-button>
          </el-tooltip>
        </template>
      </super-table>
      <!-- <pagination v-show="total > 0" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :total="total" @pagination="getList" /> -->
      </el-card>
    <!-- 添加或修改测试单对话框 -->
    <el-dialog v-model="dialog.visible" :title="dialog.title" width="70%" append-to-body>
      <super-form 
        v-if="dialog.visible"
        ref="superFormRef"
        code="userAdd"
        @selectData="selectData"
        v-model="form"
        :form-rules="rules" 
        :methods="formMethods">
        <template #modelSlot="{ prop, disabled, readonly }">
          <el-select v-model="form[prop]" placeholder="请选择" @change="modelChange" :disabled="disabled" :readonly="readonly">
            <el-option label="选项1" value="1" />
          </el-select>
        </template>
      </super-form>
      <!-- <el-form ref="demoFormRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="部门id" prop="deptId">
          <el-input v-model="form.deptId" placeholder="请输入部门id" />
        </el-form-item>
        <el-form-item label="用户id" prop="userId">
          <el-input v-model="form.userId" placeholder="请输入用户id" />
        </el-form-item>
        <el-form-item label="排序号" prop="orderNum">
          <el-input v-model="form.orderNum" placeholder="请输入排序号" />
        </el-form-item>
        <el-form-item label="key键" prop="testKey">
          <el-input v-model="form.testKey" placeholder="请输入key键" />
        </el-form-item>
        <el-form-item label="值" prop="value">
          <el-input v-model="form.value" placeholder="请输入值" />
        </el-form-item>
      </el-form> -->
      <template #footer>
        <div class="dialog-footer">
          <el-button :loading="buttonLoading" type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="Demo" lang="ts">
import { getDemo, delDemo, addDemo, updateDemo } from '@/api/demo/demo';
import { DemoVO, DemoQuery, DemoForm } from '@/api/demo/demo/types';
import { delLeave, listLeave } from '@/api/workflow/leave';
import { cancelProcessApply } from '@/api/workflow/instance';
import type { LeaveVO } from '@/api/workflow/leave/types';
const { proxy } = getCurrentInstance() as ComponentInternalInstance;


const buttonLoading = ref(false);
const loading = ref(false);
const showSearch = ref(true);
const ids = ref<Array<string | number>>([]);
const single = ref(true);
const multiple = ref(true);
const showTable = ref(true);
const total = ref(0);

const queryFormRef = ref<ElFormInstance>();
const demoFormRef = ref<ElFormInstance>();
const superFormRef = ref(null);

const dialog = reactive<DialogOption>({
  visible: false,
  title: ''
});

const initFormData = {};
const data = reactive<PageData<any, DemoQuery>>({
  form: { ...initFormData },
  queryParams: reactive({
    pageNum: 1,
    pageSize: 10,
  }),
  rules: {
    id: [{ required: true, message: '主键不能为空', trigger: 'blur' }],
    deptId: [{ required: true, message: '部门id不能为空', trigger: 'blur' }],
    userId: [{ required: true, message: '用户id不能为空', trigger: 'blur' }],
    orderNum: [{ required: true, message: '排序号不能为空', trigger: 'blur' }],
    testKey: [{ required: true, message: 'key键不能为空', trigger: 'blur' }],
    value: [{ required: true, message: '值不能为空', trigger: 'blur' }]
  }
});

const { queryParams, form, rules } = toRefs(data);
console.log('Initial queryParams:', queryParams.value);

// 添加一个监听器来观察queryParams的变化
watch(queryParams, (newVal, oldVal) => {
  console.log('queryParams changed:', newVal, oldVal);
}, { deep: true });

const leaveList = ref<LeaveVO[]>([]);
const refresh =  () => {
  showTable.value = false
  getList()
  setTimeout(() => {
    showTable.value = true;
  }, 300);
}

/** 查询测试单列表 */
const getList = async () => {
  loading.value = true;
  // const res = await listDemo(queryParams.value);
  // demoList.value = res.rows;
  console.log('getListgetList')
  console.log(queryParams.value)
  const res = await listLeave(queryParams.value);
  console.log(res)
  // leaveList.value = res.rows;
  leaveList.value = [
    {
      applyCode: "1764675394025",
      endDate: "2025-12-05 23:59:59",
      id: "1995819374313316354",
      leaveDays: 5,
      leaveType: "4",
      ramark: 111.77,
      remark: "婚假",
      startDate: "2025-12-01 00:00:00",
      status: "draft",
    },
    {
      applyCode: "1764675394025",
      endDate: "2025-12-05 23:59:59",
      id: "1995819374313316354",
      leaveDays: 5,
      leaveType: "4",
      remark: "婚假",
      ramark: 123123.991,
      startDate: "2025-12-01 00:00:00",
      status: "draft",
    },
    {
      applyCode: "1764675394025",
      endDate: "2025-12-05 23:59:59",
      id: "1995819374313316354",
      leaveDays: 5,
      leaveType: "4",
      remark: "婚假",
      ramark: 11,
      startDate: "2025-12-01 00:00:00",
      status: "draft",
    },
  ]
  total.value = res.total;
  loading.value = false;
};

/** 查看按钮操作 */
const handleView = (row?: LeaveVO) => {
  proxy.$tab.closePage(proxy.$route);
  proxy.$router.push({
    path: `/workflow/leaveEdit/index`,
    query: {
      id: row.id,
      type: 'view'
    }
  });
};

/** 撤销按钮操作 */
const handleCancelProcessApply = async (id: string) => {
  await proxy?.$modal.confirm('是否确认撤销当前单据？');
  loading.value = true;
  const data = {
    businessId: id,
    message: '申请人撤销流程！'
  };
  await cancelProcessApply(data).finally(() => (loading.value = false));
  await getList();
  proxy?.$modal.msgSuccess('撤销成功');
};


const modelChange = (val: any) => {
  
};

const selectData = (data: any, config) => {
  console.log(data)
  if(config.prop == 'name'){
    form.value.userId = data.id;
    form.value.name = data.name;
    form.value.sex = data.sex;
    form.value.phone = data.phonenumber;
    form.value.email = data.email;
    form.value.deptId = data.deptId;
    form.value.deptName = data.deptName;
  }
};

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
// 在父组件中预定义所有可能用到的方法
const formMethods = {
  // args参数集合 childFrom参数集合
  customChangeHandler: (args: Array<any>,childFrom: Object) => {
    superFormRef.value.updateData({ companyName: 'admin' })
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

/** 取消按钮 */
const cancel = () => {
  reset();
  dialog.visible = false;
};

/** 表单重置 */
const reset = () => {
  form.value = { ...initFormData };
  demoFormRef.value?.resetFields();
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

/** 多选框选中数据 */
const handleSelectionChange = (selection: DemoVO[]) => {
  ids.value = selection.map((item) => item.id);
  single.value = selection.length != 1;
  multiple.value = !selection.length;
};

/** 新增按钮操作 */
const handleAdd = () => {
  reset();
  dialog.visible = true;
  dialog.title = '添加测试单';
};

/** 修改按钮操作 */
const handleUpdate = async (row?: DemoVO) => {
  reset();
  const _id = row?.id || ids.value[0];
  const res = await getDemo(_id);
  Object.assign(form.value, res.data);
  dialog.visible = true;
  dialog.title = '修改测试单';
};

/** 提交按钮 */
const submitForm = () => {
  superFormRef.value?.validate(async (valid: boolean) => {
    if (valid) {
      const data = superFormRef.value.achieveData()
      console.log(data)
      // buttonLoading.value = true;
      // if (form.value.id) {
      //   await updateDemo(form.value).finally(() => (buttonLoading.value = false));
      // } else {
      //   await addDemo(form.value).finally(() => (buttonLoading.value = false));
      // }
      // proxy?.$modal.msgSuccess('修改成功');
      // dialog.visible = false;
      // await getList();
    }
  });
  // demoFormRef.value?.validate(async (valid: boolean) => {
  //   if (valid) {
  //     buttonLoading.value = true;
  //     if (form.value.id) {
  //       await updateDemo(form.value).finally(() => (buttonLoading.value = false));
  //     } else {
  //       await addDemo(form.value).finally(() => (buttonLoading.value = false));
  //     }
  //     proxy?.$modal.msgSuccess('修改成功');
  //     dialog.visible = false;
  //     await getList();
  //   }
  // });
};

/** 删除按钮操作 */
const handleDelete = async (row?: DemoVO) => {
  const _ids = row?.id || ids.value;
  await proxy?.$modal.confirm('是否确认删除测试单编号为"' + _ids + '"的数据项？').finally(() => (loading.value = false));
  await delDemo(_ids);
  proxy?.$modal.msgSuccess('删除成功');
  await getList();
};

/** 导出按钮操作 */
const handleExport = () => {
  proxy?.download(
    'demo/demo/export',
    {
      ...queryParams.value
    },
    `demo_${new Date().getTime()}.xlsx`
  );
};

onMounted(() => {
  getList();
});
</script>
