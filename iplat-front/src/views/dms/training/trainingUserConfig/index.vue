<template>
  <div class="p-2">
    <transition :enter-active-class="proxy?.animate.searchAnimate.enter" :leave-active-class="proxy?.animate.searchAnimate.leave">
      <div v-show="showSearch" class="mb-[10px]">
        <el-card shadow="hover">
          <super-search
            code="recordAlterationEdit-search"
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
            <el-button v-hasPermi="['dms:docRecordCreate:add']" type="primary" icon="Plus" @click="handleAdd">新增</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button v-hasPermi="['dms:docRecordCreate:remove']" type="danger" icon="Delete" :disabled="multiple" @click="handleDelete()"
              >删除</el-button
            >
          </el-col>
          <!-- <el-col :span="1.5">
            <el-button v-hasPermi="['dms:templateEdit:export']" type="warning" plain icon="Download" @click="handleExport">导出</el-button>
          </el-col> -->
          <right-toolbar v-model:show-search="showSearch" @query-table="getList"></right-toolbar>
        </el-row>
      </template>
      <super-table 
        v-if="showTable"
        code="trainingUserConfig-table"
        rowKey="id"
        highlightCurrentRow
        :dataLoading="loading"
        :data="tringUserList"
        v-model:queryParams="queryParams"
        :total="total"
        @selection-change="handleSelectionChange"
        @getList="getList"
      >
        <!-- 操作列 -->
        <template #operation="scope">
          <div>
            <el-tooltip content="编辑" placement="top" :enterable="false">
              <el-button link type="primary" v-hasPermi="['dms:docRecordCreate:detail']" icon="EditPen" @click="handleUpdate(scope.row)"></el-button>
            </el-tooltip>
            <el-tooltip content="删除" placement="top" :enterable="false">
              <el-button link type="danger" v-hasPermi="['dms:docRecordCreate:remove']" icon="Delete" @click="handleDelete(scope.row)"></el-button>
            </el-tooltip>
          </div>
        </template>
      </super-table>
      <!-- <pagination v-show="total > 0" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :total="total" @pagination="getList" /> -->
    </el-card>
    <!-- 添加或编辑培训人员配置对话框 -->
    <el-dialog v-model="dialog.visible" :title="dialog.title" width="70%" append-to-body>
      <super-form 
        v-if="dialog.visible"
        :loading="dialog.loading"
        ref="superFormRef"
        code="trainingUserConfig-form"
        @selectData="formSelectData"
        v-model="form"
        :form-rules="rules" 
        :methods="formMethods">
        <!-- 人员实例插槽 -->
        <template #userInfoSolt="config">
          <el-input v-model="form.nickName" v-if="form.userCategory == 'personnel'" suffix-icon="User" @click.native="handleSelect" v-bind="config" readonly></el-input>
          <el-select v-model="form[config.prop]" v-else-if="form.userCategory == 'role'" placeholder="请选择角色" @change="(val) => selectDataAchieve(val)">
            <el-option v-for="dict in roleOption" :key="dict.value" :label="dict.label"
              :value="dict.value" />
          </el-select>
          <el-select v-model="form[config.prop]" v-else-if="form.userCategory == 'userGroups'" placeholder="请选择用户组" @change="(val) => selectDataAchieve(val)">
            <el-option v-for="dict in userGroupOption" :key="dict.value" :label="dict.label"
              :value="dict.value" />
          </el-select>
        </template>
      </super-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button :loading="buttonLoading" type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>
    <to-select
      v-if="selectData.open"
      :title="selectData.title"
      :selectType="selectData.type"
      :multiple="selectData.multiple"
      :otherCondition="selectData.otherCondition"
      :achieveData="selectDataAchieve"
      @dialogClose="selectDataCancel"
    ></to-select>
    <!-- 提交组件 -->
    <submitVerify ref="submitVerifyRef" :task-variables="taskVariables" @submit-callback="submitCallback" />
  </div>
</template>

<script setup name="TrainingUserConfig" lang="ts">
import { listOwnerConfig, getOwnerConfig, delOwnerConfig, addOwnerConfig, updateOwnerConfig } from '@/api/dms/training/trainingUserConfig/index';
import { waitingTaskId, fileOpenCheck } from '@/api/workflow/task';
import { useUserStore } from '@/store/modules/user';
import { listRole } from '@/api/system/role';
import { listUserGroupId } from '@/api/system/userGroup';
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
  drafterId: useUserStore().userId,
  draftUserName: useUserStore().nickname,
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

const tringUserList = ref([]);

/** 查询培训人员配置列表 */
const getList = async () => {
  loading.value = true;
  try {
    const res = await listOwnerConfig(queryParams.value);
    tringUserList.value = res.rows;
    total.value = res.total;
  } finally {
    loading.value = false;
  }
};


const formSelectData = (data: any, config) => {
  if(config.prop == 'draftUserName'){
    form.value.drafterId = data.id;
    form.value.draftUserName = data.name;
  }
};

// 获取角色列表
const getRoleList = async () => {
  const res = await listRole({
    pageNum: 1,
    pageSize: 99999,
  });
  roleOption.value = res.rows.map(item => ({
    label: item.roleName,
    value: String(item.roleId)
  }));
};

// 获取用户组列表
const getUserGroupList = async () => {
  const res = await listUserGroupId({
    pageNum: 1,
    pageSize: 99999,
  });
  userGroupOption.value = res.rows.map((item: any) => ({
    label: item.userGroupName,
    value: item.userGroupId
  }));
};

// 在父组件中预定义所有可能用到的方法
const formMethods = {
  // args参数集合 childFrom参数集合
  userCategoryChange: (args: Array<any>,childFrom: Object) => {
    form.value.userInfo = '';
    // form.value.remark = '';
  }
}
const roleOption = ref([]);
const userGroupOption = ref([]);

const selectData = ref({
  open: false,
  type: 'userSelect',
  title: '',
  selectType: '',
  multiple: false,
  otherCondition: {}
})
// 手动添加按钮操作
const handleSelect = () => {
  selectData.value.open = true;
  selectData.value.title = '请选择用户';
};
// 选中数据
const selectDataAchieve = (data) => {
  if (form.value.userCategory == 'personnel') {
    form.value.userInfo = data.id;
    form.value.nickName = data.nickName;
  } else if (form.value.userCategory == 'role') {
    // 查找选中的角色名称
    const selectedRole = roleOption.value.find(role => role.value === data);
    if (selectedRole) {
      form.value.userInfo = selectedRole.value;
      // form.value.remark = selectedRole.label;
    }
  } else if (form.value.userCategory == 'userGroups') {
    // 查找选中的用户组名称
    const selectedGroup = userGroupOption.value.find(group => group.value === data);
    if (selectedGroup) {
      form.value.userInfo = selectedGroup.value;
      // form.value.remark = selectedGroup.label;
    }
  }
  selectDataCancel();
};
// 关闭选择
const selectDataCancel = () => {
  selectData.value = {
    open: false,
    type: 'userSelect',
    title: undefined,
    multiple: false,
    selectType: undefined,
    otherCondition: {}
  };
};

// 打开pageoffice
const openPageOffice = (row: any) => {
  const paramJson = {
    id: row.id
  };
  const paramString = JSON.stringify(paramJson);
  //openWindow()第三个参数用来向弹出的PageOffice浏览器（POBrowser）窗口传递参数(参数长度不限)，支持json格式字符串。
  //此处为了方便演示，我们传递了file_id和file_name两个参数，具体以您实际开发为准。
  proxy?.POBrowser.openWindow('/docRecordEdit', 'width=1200px;height=800px;', paramString);
  // console.log(row)
};

//提交组件
const submitVerifyRef = ref();
const taskVariables = ref<Record<string, any>>({});
// 提交审批按钮操作
const submitApprove = (row: any) => {
  row.subLoading = true;
  fileOpenCheck({fileBusinessId: row.id}).then(res=>{
    waitingTaskId(row.workflowId).then((res) => {
      if(res.data){
        submitVerifyRef.value.openDialog(res.data);
        submitVerifyRef.value.init(row.id);
      }
    });
  }).finally(()=>{
    row.subLoading = false;
  })
};
//提交回调
const submitCallback = async () => {
  getList();
};



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
  ids.value = selection.map((item) => item.id);
  names.value = selection.map((item) => item.documentName);
  single.value = selection.length != 1;
  multiple.value = !selection.length;
};

/** 新增按钮操作 */
const handleAdd = () => {
  reset();
  getRoleList();
  getUserGroupList();
  dialog.visible = true;
  dialog.loading = false;
  dialog.title = '添加培训人员配置';
};

/** 编辑按钮操作 */
const handleUpdate = async (row?: any) => {
  reset();
  getRoleList();
  getUserGroupList();
  dialog.visible = true;
  dialog.loading = true;
  dialog.title = '编辑培训人员配置';
  const _id = row?.id || ids.value[0];
  const res = await getOwnerConfig(_id);
  Object.assign(form.value, res.data);
  dialog.loading = false;
};
/** 详情按钮操作 */
const handleDetail = async (row?: any) => {
  reset();
  dialog.visible = true;
  dialog.loading = true;
  dialog.title = '查看培训人员配置';
  const _id = row?.id || ids.value[0];
  const res = await getOwnerConfig(_id);
  Object.assign(form.value, res.data);
  dialog.loading = false;
};

/** 提交按钮 */
const submitForm = () => {
  superFormRef.value?.validate(async (valid: boolean) => {
    if (valid) {
      const data = superFormRef.value.achieveData()
      buttonLoading.value = true;
      if (data.id) {
        await updateOwnerConfig(data).finally(() => (buttonLoading.value = false));
      } else {
        await addOwnerConfig(data).finally(() => (buttonLoading.value = false));
      }
      proxy?.$modal.msgSuccess('操作成功');
      dialog.visible = false;
      await getList();
    }
  });
};

/** 删除按钮操作 */
const handleDelete = async (row?: any) => {
  const _ids = row?.id || ids.value;
  const _names = row?.documentName || names.value;
  await proxy?.$modal.confirm('是否确认删除选中的数据项？').finally(() => (loading.value = false));
  await delOwnerConfig(_ids);
  proxy?.$modal.msgSuccess('删除成功');
  await getList();
};

/** 导出按钮操作 */
const handleExport = () => {
  proxy?.download(
    'dms/template-edit/export',
    {
      ...queryParams.value
    },
    `培训人员配置_${new Date().getTime()}.xlsx`
  );
};


onMounted(() => {
  getList();
});
</script>
