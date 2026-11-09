<template>
  <!-- 授权用户 -->
  <el-dialog title="选择用户" v-model="visible" width="80%" top="5vh" append-to-body>
    <el-form :model="queryParams" ref="queryFormRef" :inline="true" label-width="auto">
      <el-form-item label="工号" prop="userName">
        <el-input
          v-model="queryParams.userName"
          placeholder="请输入工号"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="姓名" prop="nickName">
        <el-input
          v-model="queryParams.nickName"
          placeholder="请输入姓名"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="手机号" prop="phonenumber">
        <el-input
          v-model="queryParams.phonenumber"
          placeholder="请输入手机号"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="部门" prop="deptId">
        <el-tree-select
          v-model="queryParams.deptId"
          @change="handleQuery"
          :data="deptOptions"
          filterable
          clearable
          :props="{ value: 'id', label: 'label', children: 'children' }"
          value-key="id"
          placeholder="请选择部门"
          check-strictly
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>

    </el-form>
    <el-table @row-click="clickRow"
      ref="tableRef"
      v-loading="loading"
      row-key="userId"
      :data="userList"
      @selection-change="handleSelectionChange">
      <el-table-column type="selection" reserve-selection width="55"></el-table-column>
      <el-table-column label="序号" type="index" width="55" align="center" />
      <el-table-column label="工号" align="center" prop="userName" :show-overflow-tooltip="true" />
      <el-table-column label="姓名" align="center" prop="nickName" :show-overflow-tooltip="true" />
      <el-table-column label="部门" align="center" key="deptName" prop="deptName" :show-overflow-tooltip="true" />
      <el-table-column label="邮箱" align="center" prop="email" :show-overflow-tooltip="true" />
      <el-table-column label="手机号" align="center" prop="phonenumber" :show-overflow-tooltip="true" />
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
    </el-table>
    <pagination v-show="total > 0" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :total="total" @pagination="getList" />
    <template #footer>
      <div class="dialog-footer">
        <el-button type="primary" @click="handleSelectUser">确认</el-button>
        <el-button @click="cancel">取消</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup name="DeptRoleSelectuser" lang="ts">
import { unallocatedUserList, authUserSelectAll } from "@/api/system/deptRole"
import { deptTreeSelect } from "@/api/system/user"
const props = defineProps({
  deptRoleId: {
    type: [Number, String],
    default: undefined
  }
})
const { proxy } = getCurrentInstance() as ComponentInternalInstance;
const { sys_user_status } = toRefs<any>(proxy?.useDict('sys_user_status'));
const visible = ref(false);
const loading = ref(false);
const userIds = ref([]);
const userList = ref([]);
const total = ref(0);

const data = reactive({
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    deptId: undefined,
    deptRoleId: undefined,
    userName: undefined,
    nickName: undefined,
    phonenumber: undefined
  }
});
const { queryParams } = toRefs(data);
const deptOptions = ref([]);
const tableRef = ref(null);
const queryFormRef = ref(null);
const emit = defineEmits(['ok']);
// 显示弹框
const show = () => {
  queryParams.value.deptRoleId = props.deptRoleId;
  getList();
  visible.value = true;
};
const clickRow = (row) => {
  tableRef.value.toggleRowSelection(row);
};
// 多选框选中数据
const handleSelectionChange = (selection) => {
  userIds.value = selection.map(item => item.userId);
};
// 查询表数据
const getList = () => {
  loading.value = true
  unallocatedUserList(queryParams.value).then(res => {
    userList.value = res.rows;
    total.value = res.total;
    loading.value = false
  });
};
/** 搜索按钮操作 */
const handleQuery = () => {
  queryParams.value.pageNum = 1;
  getList();
};
/** 重置按钮操作 */
const resetQuery = () => {
  queryParams.value.deptId = undefined
  queryFormRef.value.resetFields();
  handleQuery();
};
/** 选择授权用户操作 */
const handleSelectUser = () => {
  const deptRoleId = queryParams.value.deptRoleId;
  const _userIds = userIds.value.join(",");
  if (_userIds == "") {
    proxy.$modal.msgError('请选择要分配的用户');
    return;
  }
  authUserSelectAll({ deptRoleId: deptRoleId, userIds: _userIds }).then(res => {
    proxy.$modal.msgSuccess(res.msg);
    if (res.code === 200) {
      if (tableRef.value) {
        tableRef.value.clearSelection();
      }
      visible.value = false;
      emit("ok");
    }
  });
};

/** 查询部门下拉树结构 */
const getDeptTree = async () => {
  deptTreeSelect({
    authFilter: '0',
  }).then(response => {
    deptOptions.value = response.data;
  });
}

const cancel = () =>{
  if (tableRef.value) {
    tableRef.value.clearSelection();
  }
  visible.value = false;
};
onMounted(() => {
  // 查询部门信息
  getDeptTree();
});
defineExpose({
  show
})
</script>