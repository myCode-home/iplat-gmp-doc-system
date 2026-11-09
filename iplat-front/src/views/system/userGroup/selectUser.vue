<template>
  <!-- 授权用户 -->
  <el-dialog title="选择用户" v-model="visible" width="80%" top="5vh" append-to-body>
    <transition :enter-active-class="proxy?.animate.searchAnimate.enter" :leave-active-class="proxy?.animate.searchAnimate.leave">
      <div v-show="showSearch" class="mb-[10px]">
        <el-card shadow="hover">
          <el-form ref="queryFormRef" :model="queryParams" :inline="true" label-width="auto">
            <el-form-item label="工号" prop="userName">
              <el-input
                v-model="queryParams.userName"
                placeholder="请选择工号"
                clearable
                @change="handleQuery"
                @keyup.enter.native="handleQuery"
              />
            </el-form-item>
            <el-form-item label="姓名" prop="nickName">
              <el-input
                v-model="queryParams.nickName"
                placeholder="请选择姓名"
                clearable
                @change="handleQuery"
                @keyup.enter.native="handleQuery"
              />
            </el-form-item>
            <el-form-item label="手机号" prop="phonenumber">
              <el-input
                v-model="queryParams.phonenumber"
                placeholder="请选择手机号"
                clearable
                @change="handleQuery"
                @keyup.enter.native="handleQuery"
              />
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
          <right-toolbar v-model:show-search="showSearch" @query-table="getList"></right-toolbar>
        </el-row>
      </template>
      <el-table ref="tableRef" v-loading="loading" border :data="userList" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" align="center" />
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
      </el-table>
    </el-card>
    <pagination
      v-show="total>0"
      :total="total"
      :page.sync="queryParams.pageNum"
      :limit.sync="queryParams.pageSize"
      @pagination="getList"
    />
    <template #footer>
      <el-button type="primary" @click="handleSelectUser">确认</el-button>
      <el-button @click="visible = false">取消</el-button>
    </template>
  </el-dialog>
</template>

<script lang="ts" name="UserGroupSelectUser" setup>
import { unallocatedUserList, authUserSelectAll } from "@/api/system/userGroup"

const props = defineProps({
  userGroupId: {
    type: [Number, String]
  }
})
const emit = defineEmits(['ok']);
const visible = ref(false);
const loading = ref(true);
const userList = ref([]);
const userIds = ref([]);
const total = ref(0);
const queryParams = ref({
  pageNum: 1,
  pageSize: 10,
  userGroupId: props.userGroupId,
  userName: undefined,
  nickName: undefined,
  phonenumber: undefined
});
const showSearch = ref(false);
const { proxy } = getCurrentInstance() as ComponentInternalInstance;
const { sys_normal_disable } = toRefs<any>(proxy?.useDict('sys_normal_disable'));
const queryFormRef = ref<ElFormInstance>();
const tableRef = ref();
// 显示弹框
const show = () => {
  queryParams.value.userGroupId = props.userGroupId;
  visible.value = true;
  getList();
};

const clickRow = (row) => {
  tableRef.value.toggleRowSelection(row);
};

// 多选框选中数据
const handleSelectionChange = (selection)=> {
  userIds.value = selection.map(item => item.userId);
};
// 查询表数据
const getList = () => {
  loading.value = true;
  unallocatedUserList(queryParams.value).then(res => {
    userList.value = res.rows;
    loading.value = false;
    total.value = res.total;
  });
};
/** 搜索按钮操作 */
const handleQuery = () =>  {
  queryParams.value.pageNum = 1;
  getList();
};
/** 重置按钮操作 */
const resetQuery = () => {
  queryFormRef.value?.resetFields();
  handleQuery();
};
/** 选择授权用户操作 */
const handleSelectUser = () => {
  const userGroupId = queryParams.value.userGroupId;
  const ids = userIds.value.join(",");
  if (ids == "") {
    proxy?.$modal.msgError('请选择要分配的用户');
    return;
  }
  authUserSelectAll({ userGroupId: userGroupId, userIds: ids }).then(res => {
    proxy?.$modal.msgSuccess(res.msg);
    if (res.code === 200) {
      visible.value = false;
      emit("ok");
    }
  });
};
defineExpose({
  show
})
</script>
