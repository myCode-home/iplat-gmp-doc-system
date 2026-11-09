<template>
  <div class="p-2">
    <transition :enter-active-class="proxy?.animate.searchAnimate.enter" :leave-active-class="proxy?.animate.searchAnimate.leave">
      <div v-show="showSearch" class="mb-[10px]">
        <el-card shadow="hover">
          <super-search
            code="numberApplication-search"
            v-model="queryParams"
            @getList="getList"
            @resetQuery="resetQuery"
          >
          </super-search>
        </el-card>
      </div>
    </transition>

    <el-card shadow="hover">
      <template #header>
        <el-row :gutter="10" class="mb8">
          <el-col :span="1.5">
            <el-button v-hasPermi="['eqms:deviceAcceptance:add']" type="primary" icon="Plus" @click="handleAdd">新增</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button v-hasPermi="['eqms:deviceAcceptance:remove']" type="danger" icon="Delete" @click="handleBatchDelete" :disabled="ids.length === 0">删除</el-button>
          </el-col>
          <right-toolbar v-model:show-search="showSearch" @query-table="getList"></right-toolbar>
        </el-row>
      </template>
      <super-table 
        v-if="showTable"
        code="numberApplication-table"
        rowKey="id"
        highlightCurrentRow
        :dataLoading="loading"
        :data="numberApplicationList"
        v-model:queryParams="queryParams"
        :total="total"
        @selection-change="handleSelectionChange"
        @getList="getList"
      >
        <!-- 操作列 -->
        <template #operation="scope">
          <div>
            <el-tooltip content="编辑" placement="top">
              <el-button link type="primary" v-hasPermi="['eqms:deviceAcceptance:edit']" icon="EditPen" @click="handleUpdate(scope.row)"></el-button>
            </el-tooltip>
            <el-tooltip content="删除" placement="top">
              <el-button link type="danger" v-hasPermi="['eqms:deviceAcceptance:remove']" icon="Delete" @click="handleDelete(scope.row)"></el-button>
            </el-tooltip>
            <!-- <el-popconfirm
              title="确定提交当前行数据吗？"
              @confirm="openApprove(scope.row)"
            >
              <template #reference>
                <span style="margin-left: 12px;">
                  <el-tooltip content="提交" placement="top">
                    <el-button link type="primary" v-hasPermi="['eqms:deviceAcceptance:submit']" icon="SuccessFilled"></el-button>
                  </el-tooltip>
                </span>
              </template>
            </el-popconfirm> -->
          </div>
        </template>
      </super-table>
      <!-- <pagination v-show="total > 0" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :total="total" @pagination="getList" /> -->
    </el-card>
    <!-- 添加或编辑申请单对话框 -->
    <el-dialog v-model="dialog.visible" :title="dialog.title" width="90%" append-to-body>
      <super-form 
        v-if="dialog.visible"
        :loading="dialog.loading"
        ref="superFormRef"
        code="numberApplication-form"
        @selectData="selectData"
        v-model="form"
        :form-rules="rules" 
        :methods="formMethods">
      </super-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button :loading="buttonLoading" type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="NumberApplication" lang="ts">
import { listPreApply, getPreApply, delPreApply, addPreApply, updatePreApply } from '@/api/eqms/deviceManagementCconfig/numberApplication';
const { proxy } = getCurrentInstance() as ComponentInternalInstance;


const buttonLoading = ref(false);
const loading = ref(false);
const showSearch = ref(true);
const ids = ref<Array<string | number>>([]);
const names = ref<Array<string | number>>([]);
const single = ref(true);
const multiple = ref(true);
const showTable = ref(true);
const total = ref(0);

const superFormRef = ref(null);

const dialog = reactive<DialogOption>({
  title: '',
  visible: false,
  loading: false
});

const apprDialog = reactive<DialogOption>({
  title: '请输入提交意见',
  visible: false,
  loading: false
});

const initFormData = {
};
const data = reactive<PageData<any, any>>({
  form: { ...initFormData },
  queryParams: reactive({
    pageNum: 1,
    pageSize: 10,
  }),
  rules: {
    // id: [{ required: true, message: '主键不能为空', trigger: 'blur' }],
  }
});

const { queryParams, form, rules } = toRefs(data);

const numberApplicationList = ref([]);

/** 查询申请单列表 */
const getList = async () => {
  loading.value = true;
  try {
    const res = await listPreApply(queryParams.value);
    numberApplicationList.value = res.rows;
    total.value = res.total;
  } finally {
    loading.value = false;
  }
};


const selectData = (data: any, config) => {
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

// 在父组件中预定义所有可能用到的方法
const formMethods = {
  // args参数集合 childFrom参数集合
  customChangeHandler: (args: Array<any>,childFrom: Object) => {
    superFormRef.value.updateData({ companyName: 'admin' })
    console.log('自定义change事件触发')
  },
  handleRoleChange: (args: Array<any>,childFrom: Object) => {
    console.log('自定义事件触发', args, childFrom)
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

// 表单重置
const reset = () => {
  form.value = { ...initFormData };
};

/** 搜索按钮操作 */
const handleQuery = () => {
  queryParams.value.pageNum = 1;
  getList();
};

/** 重置按钮操作 */
const resetQuery = () => {
  handleQuery();
};

/** 多选框选中数据 */
const handleSelectionChange = (selection: any[]) => {
  ids.value = selection.map((item) => item.applyId);
  names.value = selection.map((item) => item.deviceName);
  single.value = selection.length != 1;
  multiple.value = !selection.length;
  console.log('ids')
  console.log(ids)
};

/** 新增按钮操作 */
const handleAdd = () => {
  reset();
  dialog.visible = true;
  dialog.loading = false;
  dialog.title = '添加申请单';
};

/** 编辑按钮操作 */
const handleUpdate = async (row?: any) => {
  reset();
  dialog.visible = true;
  dialog.loading = true;
  dialog.title = '编辑申请单';
  const _id = row?.applyId || ids.value[0];
  const res = await getPreApply(_id);
  Object.assign(form.value, res.data);
  dialog.loading = false;
};

/** 提交按钮 */
const submitForm = () => {
  superFormRef.value?.validate(async (valid: boolean) => {
    if (valid) {
      const data = superFormRef.value.achieveData()
      buttonLoading.value = true;
      if (data.applyId) {
        await updatePreApply(data).finally(() => (buttonLoading.value = false));
      } else {
        await addPreApply(data).finally(() => (buttonLoading.value = false));
      }
      proxy?.$modal.msgSuccess('操作成功');
      dialog.visible = false;
      await getList();
    }
  });
};

/** 删除按钮操作 */
const handleDelete = async (row?: any) => {
  const _ids = row?.applyId || ids.value;
  const _names = row?.deviceName || names.value;
  await proxy?.$modal.confirm('是否确认删除设备名称为"' + _names + '"的数据项？').finally(() => (loading.value = false));
  await delPreApply(_ids);
  proxy?.$modal.msgSuccess('删除成功');
  await getList();
};

/** 批量删除按钮操作 */
const handleBatchDelete = async () => {
  if (ids.value.length === 0) {
    proxy?.$modal.msgError('请至少选择一条数据');
    return;
  }
  
  const idsStr = ids.value.join(',');
  await proxy?.$modal.confirm('是否确认删除选中的' + ids.value.length + '项数据？');
  await delPreApply(idsStr);
  proxy?.$modal.msgSuccess('删除成功');
  await getList();
  ids.value = [];
  names.value = [];
};

onMounted(() => {
  getList();
});
</script>
