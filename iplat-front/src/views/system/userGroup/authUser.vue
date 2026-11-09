<template>
  <div class="p-2">
    <transition :enter-active-class="proxy?.animate.searchAnimate.enter" :leave-active-class="proxy?.animate.searchAnimate.leave">
      <div v-show="showSearch" class="mb-[10px]">
        <el-card shadow="hover">
          <el-form ref="queryFormRef" :model="queryParams" :inline="true" label-width="auto">
            <el-form-item label="工号" prop="userName">
              <el-input v-model="queryParams.userName" placeholder="请输入工号" clearable @change="handleQuery" @keyup.enter="handleQuery" />
            </el-form-item>
            <el-form-item label="姓名" prop="nickName">
              <el-input v-model="queryParams.nickName" placeholder="请输入姓名" clearable @change="handleQuery" @keyup.enter="handleQuery" />
            </el-form-item>
            <el-form-item label="手机号" prop="phonenumber">
              <el-input v-model="queryParams.phonenumber" placeholder="请输入手机号" clearable @change="handleQuery" @keyup.enter="handleQuery" />
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
          <template v-if="operate != 'details'">
            <el-col :span="1.5">
              <el-button v-hasPermi="['system:userGroup:add']" type="primary" icon="Plus" @click="openSelectUser">选择用户</el-button>
            </el-col>
            <el-col :span="1.5">
              <el-button v-hasPermi="['system:userGroup:remove']" type="primary" icon="Delete" :disabled="multiple" @click="cancelAuthUserAll">
                批量取消授权
              </el-button>
            </el-col>
            <el-col :span="1.5" v-if="false">
              <el-button v-hasPermi="['system:userGroup:export']" type="primary" icon="Download" @click="handleExport">导出</el-button>
            </el-col>
          </template>
          <right-toolbar v-model:show-search="showSearch" @query-table="getList"></right-toolbar>
        </el-row>
      </template>

      <el-table v-loading="loading" border :data="userList" @selection-change="handleSelectionChange">
        <el-table-column type="selection" v-if="operate != 'details'" width="55" align="center" />
        <el-table-column label="id" prop="userGroupId" width="55" v-if="false" align="center" />
        <el-table-column label="序号" type="index" width="55" align="center" />
        <el-table-column label="工号" align="center" prop="userName" />
        <el-table-column label="姓名" align="center" prop="nickName" />
        <el-table-column label="邮箱" align="center" prop="email" />
        <el-table-column label="手机号" align="center" prop="phonenumber" />
        <el-table-column label="状态" width="120" align="center" prop="status">
          <template #default="{row}">
            <dict-tag :options="sys_normal_disable" :value="row.status" />
          </template>
        </el-table-column>
        <el-table-column label="创建时间" width="120" align="center" prop="createTime">
          <template #default="scope">
            <span>{{ proxy.parseTime(scope.row.createTime, '{y}-{m}-{d}') }}</span>
          </template>
        </el-table-column>
        <el-table-column width="60" label="操作" v-if="operate != 'details'" align="center" fixed="right" class-name="small-padding fixed-width">
          <template #default="scope">
            <el-tooltip content="取消授权" placement="top">
              <el-button v-hasPermi="['system:userGroup:edit']" link type="primary" icon="CircleClose" @click="cancelAuthUser(scope.row)"></el-button>
            </el-tooltip>
          </template>
        </el-table-column>
      </el-table>
      <pagination v-show="total > 0" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :total="total" @pagination="getList" />
    </el-card>
    <!-- 添加或修改用户组管理对话框 -->
    <!-- <el-dialog v-model="dialog.visible" :title="dialog.title" width="80%" append-to-body>
      <template #footer>
        <div class="dialog-footer">
          <el-button :loading="buttonLoading" type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog> -->

    <!-- 选择用户 -->
    <SelectUser ref="selectUserRef" :userGroupId="queryParams.userGroupId" @ok="handleQuery" />
  </div>
</template>

<script setup name="UserGroupId" lang="ts">
import { 
  allocatedUserList, 
  authUserCancel, 
  authUserCancelAll 
} from "@/api/system/userGroup"
import SelectUser from '@/views/system/userGroup/selectUser.vue'


const props = defineProps({
  userGroupId: {
    type: String,
    default: ''
  },
  operate: {
    type: String,
    default: ''
  }
});

const { proxy } = getCurrentInstance() as ComponentInternalInstance;

const { sys_normal_disable } = toRefs<any>(proxy?.useDict('sys_normal_disable'));

const userList = ref([]);
const buttonLoading = ref(false);
const loading = ref(true);
const showSearch = ref(false);
const ids = ref<Array<string | number>>([]);
const names = ref<Array<string | number>>([]);
const single = ref(true);
const selectRow = ref({});
const multiple = ref(true);
const total = ref(0);
const selectUserRef = ref();

const queryFormRef = ref<ElFormInstance>();
const tenantFormRef = ref<ElFormInstance>();

const dialog = reactive({
  visible: false,
  loading: false,
  title: ''
});

const authUser = reactive({
  visible: false,
  title: '',
  userGroupId: '',
});

const initFormData = {
  userGroupId: props.userGroupId,
  userGroupName: '',
  status: '0',
  userGroupSort: null,
  remark: ''
};
const data = reactive({
  form: { ...initFormData },
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    userGroupId: props.userGroupId,
    userName: '',
    nickName: '',
    phonenumber: '',
    remark: ''
  },
  rules: {
    userGroupName: [{ required: true, message: '用户组名称不能为空', trigger: 'blur' }],
    serialRuleContent: [{ required: true, message: '内容不能为空', trigger: 'blur' }],
  }
});

const { queryParams, form, rules } = toRefs(data);


/** 查询用户组管理列表 */
const getList = async () => {
  loading.value = true;
  const res = await allocatedUserList(queryParams.value);
  userList.value = res.rows;
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
  ids.value = selection.map((item:any) => item.userId);
  names.value = selection.map((item:any) => item.nickName);
  single.value = selection.length != 1;
  multiple.value = !selection.length;
  selectRow.value = selection[0]||{};
};

/** 取消授权按钮操作 */
const cancelAuthUser = (row) => {
  const userGroupId = queryParams.value.userGroupId;
  proxy?.$modal.confirm('确认要取消该用户授权？').then(function() {
    return authUserCancel({ userId: row.userId, userGroupId: userGroupId });
  }).then(() => {
    getList();
    proxy?.$modal.msgSuccess('操作成功');
  }).catch(() => {});
};

/** 批量取消授权按钮操作 */
const cancelAuthUserAll = (row) => {
  const userGroupId = queryParams.value.userGroupId;
  const userIds = ids.value.join(",");
  proxy?.$modal.confirm('是否取消选中用户授权数据项？').then(function() {
    return authUserCancelAll({ userGroupId: userGroupId, userIds: userIds });
  }).then(() => {
    getList();
    proxy?.$modal.msgSuccess('操作成功');
  }).catch(() => {});
};

/** 新增按钮操作 */
const handleAdd = () => {
  reset();
  dialog.visible = true;
  dialog.loading = false;
  dialog.title = '添加用户组管理';
};

/** 提交按钮 */
// const submitForm = () => {
//   tenantFormRef.value?.validate(async (valid: boolean) => {
//     if (valid) {
//       buttonLoading.value = true;
//       if (form.value.userGroupId) {
//         await updateUserGroupId(form.value).finally(() => (buttonLoading.value = false));
//       } else {
//         await addUserGroupId(form.value).finally(() => (buttonLoading.value = false));
//       }
//       proxy?.$modal.msgSuccess('操作成功');
//       dialog.visible = false;
//       dialog.loading = false;
//       await getList();
//     }
//   });
// };

// 选择用户
const openSelectUser = () => {
  selectUserRef.value.show();
}

/** 删除按钮操作 */
// const handleDelete = async (row) => {
//   const _ids = row?.userGroupId || ids.value;
//   const _names = row?.userGroupName || names.value;
//   await proxy?.$modal.confirm('是否确认删除用户组用户组名称为"' + _names + '"的数据项？');
//   loading.value = true;
//   await delUserGroupId(_ids).finally(() => (loading.value = false));
//   await getList();
//   proxy?.$modal.msgSuccess('删除成功');
// };

/** 分配用户按钮操作 */
const handleAuthUser = async (row) => {
  // row.userGroupId
  // row.userGroupName

};

/** 导出按钮操作 */
const handleExport = () => {
  proxy?.download(
    'system/tenant/export',
    {
      ...queryParams.value
    },
    `用户组_${new Date().getTime()}.xlsx`
  );
};

onMounted(() => {
  getList();
});
</script>
