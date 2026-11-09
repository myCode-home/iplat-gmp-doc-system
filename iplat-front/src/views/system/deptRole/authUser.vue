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
          <el-col :span="1.5">
            <el-button v-hasPermi="['system:deptRole:add']" v-if="props.operate !== 'details'" @click="openSelectUser" type="primary" icon="Plus">
              新增用户
            </el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button v-hasPermi="['system:deptRole:remove']" v-if="operate !== 'details'" type="primary" icon="Delete" :disabled="multiple" @click="cancelAuthUserAll">
              取消授权
            </el-button>
          </el-col>
          <right-toolbar v-model:show-search="showSearch" @query-table="getList"></right-toolbar>
        </el-row>
      </template>

      <el-table v-loading="loading" :data="userList" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" align="center" v-if="props.operate !== 'details'" />
        <el-table-column label="序号" type="index" width="55" align="center" />
        <el-table-column label="工号" prop="userName" align="center" :show-overflow-tooltip="true" />
        <el-table-column label="姓名" prop="nickName" align="center" :show-overflow-tooltip="true" />
        <el-table-column label="邮箱" prop="email" align="center" :show-overflow-tooltip="true" />
        <el-table-column label="手机号" prop="phonenumber" align="center" :show-overflow-tooltip="true" />
        <el-table-column label="部门" align="center" width="130" prop="deptName" :show-overflow-tooltip="true" />
        <el-table-column label="状态" align="center" prop="status">
          <template #default="scope">
            <dict-tag :options="sys_user_status" :value="scope.row.status"/>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" align="center" prop="createTime" width="180">
          <template #default="scope">
            <span>{{ parseTime(scope.row.createTime) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
          <template #default="scope">
            <el-tooltip content="取消授权" placement="top">
              <el-button v-hasPermi="['system:deptRole:remove']" v-if="props.operate !== 'details'" link type="primary" icon="CircleClose" @click="cancelAuthUser(scope.row)"></el-button>
            </el-tooltip>
          </template>
        </el-table-column>
      </el-table>
      <pagination v-show="total > 0" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :total="total" @pagination="getList" />
    </el-card>
    <select-user ref="selectRef" :deptRoleId="queryParams.deptRoleId" @ok="handleQuery" />
  </div>
</template>

<script setup name="DeptRoleUser" lang="ts">
import SelectUser from "@/views/system/deptRole/selectUser.vue";
import { allocatedUserList, authUserCancel, authUserCancelAll } from "@/api/system/deptRole"
const props = defineProps({
  deptRoleId: {
    type: [String, Number],
    required: false
  },
  operate: {
    type: String,
    default: 'add',
  }
})
const { proxy } = getCurrentInstance() as ComponentInternalInstance;

const { sys_user_status } = toRefs<any>(proxy?.useDict('sys_user_status'));

const userList = ref([]);
const loading = ref(true);
const showSearch = ref(false);
const ids = ref<Array<string | number>>([]);
const names = ref<Array<string | number>>([]);
const single = ref(true);
const selectRow = ref({});
const multiple = ref(true);
const total = ref(0);

const queryFormRef = ref<ElFormInstance>();

const dialog = reactive({
  visible: false,
  loading: false,
  title: ''
});

const data = reactive({
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    deptId: undefined,
    deptRoleId: undefined,
    userName: undefined,
    nickName: undefined,
    phonenumber: undefined
  },
  rules: {
    deptRoleId: [
      { required: true, message: "部门角色ID不能为空", trigger: "blur" }
    ],
    deptId: [
      { required: true, message: "部门不能为空", trigger: "change" }
    ],
    roleId: [
      { required: true, message: "角色不能为空", trigger: "change" }
    ],
    deptRoleName: [
      { required: true, message: "部门角色名称不能为空", trigger: "blur" }
    ],
    deptRoleCode: [
      { required: true, message: "部门角色编号不能为空", trigger: "blur" }
    ],
    deptRoleSort: [
      { required: true, message: "显示顺序不能为空", trigger: "blur" }
    ],
    deptRoleVersion: [
      { required: false, message: "版本不能为空", trigger: "blur" }
    ],
    deptRoleStatus: [
      { required: true, message: "状态不能为空", trigger: "change" }
    ],
    deptRoleType: [
      { required: true, message: "类型不能为空", trigger: "change" }
    ],
    deptRoleValidTime: [
      { required: false, message: "有效期不能为空", trigger: "blur" }
    ]
  }
});

const { queryParams, rules } = toRefs(data);
const userIds = ref([]);
const selectRef = ref(null);

/** 查询授权用户列表 */
const getList = ()=> {
  loading.value = true;
  allocatedUserList(queryParams.value).then(response => {
      userList.value = response.rows;
      total.value = response.total;
      loading.value = false;
    }
  );
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
const handleSelectionChange = (selection) => {
  userIds.value = selection.map(item => item.userId)
  multiple.value = !selection.length
};

/** 打开授权用户表弹窗 */
const openSelectUser = () => {
  selectRef.value.show();
};

/** 取消授权按钮操作 */
const cancelAuthUser = async (row)=> {
  const deptRoleId = queryParams.value.deptRoleId;
  await proxy?.$modal.confirm('确认要取消该用户角色吗？');
  loading.value = true;
  await authUserCancel({ userId: row.userId, deptRoleId: deptRoleId });;
  await getList();
  proxy?.$modal.msgSuccess('操作成功');
};

/** 批量取消授权按钮操作 */
const cancelAuthUserAll = (row)=> {
  const deptRoleId = queryParams.value.deptRoleId;
  const _userIds = userIds.value.join(",");
  proxy.$modal.confirm('是否取消选中用户授权数据项？').then(function() {
    return authUserCancelAll({ deptRoleId: deptRoleId, userIds: _userIds });
  }).then(() => {
    getList();
    proxy.$modal.msgSuccess('操作成功');
  }).catch(() => {});
};

onMounted(() => {
  const deptRoleId = proxy.$route.params && proxy.$route.params.deptRoleId;
  if (deptRoleId) {
    queryParams.value.deptRoleId = deptRoleId;
    getList();
  } else {
    if (props.deptRoleId) {
      queryParams.value.deptRoleId = props.deptRoleId;
      getList();
    }
  }
});
</script>

<style scoped lang="scss">

</style>