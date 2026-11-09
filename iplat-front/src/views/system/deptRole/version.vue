<template>
  <el-table v-loading="loading" :data="deptRoleList">
    <!-- <el-table-column type="selection" width="55" align="center" /> -->
    <el-table-column label="编号" align="center" prop="deptRoleId" v-if="false"/>
    <el-table-column label="序号" type="index" width="50" align="center" />
    <el-table-column label="版本" align="center" prop="deptRoleVersion" width="200" :show-overflow-tooltip="true"/>
    <el-table-column label="状态" align="center" prop="deptRoleStatus" width="200" :show-overflow-tooltip="true">
      <template #default="scope">
        <dict-tag :options="sys_normal_disable" :value="scope.row.deptRoleStatus"/>
      </template>
    </el-table-column>
    <el-table-column label="有效期" align="center" prop="deptRoleValidTime" width="250" :show-overflow-tooltip="true">
      <template #default="scope">
        <span>{{ parseTime(scope.row.deptRoleValidTime, '{y}-{m}-{d} {h}:{i}:{s}') }}</span>
      </template>
    </el-table-column>
    <el-table-column label="创建时间" align="center" prop="createTime" width="250" :show-overflow-tooltip="true"/>
    <el-table-column label="操作" align="center" width="100" fixed="right" class-name="small-padding fixed-width">
      <template #default="scope">
        <template v-if="scope.row.deptRoleStatus === '0'">
          <el-tooltip content="升级" placement="top">
            <el-button v-hasPermi="['system:deptRole:add']" link type="primary" icon="Plus" @click="handleUpgrade(scope.row)"></el-button>
          </el-tooltip>
          <el-tooltip content="分配用户" placement="top">
            <el-button v-hasPermi="['system:deptRole:edit']" link type="primary" icon="User" @click="handleAuthUser(scope.row, 'add')"></el-button>
          </el-tooltip>
          <!-- <el-button
            size="mini"
            type="text"
            icon="el-icon-plus"
            @click="handleUpgrade(scope.row)"
            v-hasPermi="['system:deptRole:add']"
          >升级</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-user"
            @click="handleAuthUser(scope.row, 'add')"
            v-hasPermi="['system:deptRole:edit']"
          >分配用户</el-button> -->
        </template>
        <template v-else>
          <el-tooltip content="生效" placement="top">
            <el-button v-hasPermi="['system:testQuestions:edit']" link type="primary" icon="Check" @click="handleTakeEffect(scope.row)"></el-button>
          </el-tooltip>
          <el-tooltip content="查询用户" placement="top">
            <el-button v-hasPermi="['system:deptRole:edit']" link type="primary" icon="User" @click="handleAuthUser(scope.row, 'details')"></el-button>
          </el-tooltip>
          <!-- <el-button
            size="mini"
            type="text"
            icon="el-icon-circle-check"
            v-if="scope.row.validTime"
            @click="handleTakeEffect(scope.row)"
            v-hasPermi="['system:testQuestions:edit']"
          >生效</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-user"
            @click="handleAuthUser(scope.row, 'details')"
            v-hasPermi="['system:deptRole:edit']"
          >查询用户</el-button> -->
        </template>
      </template>
    </el-table-column>
  </el-table>
  <pagination v-show="total > 0" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :total="total" @pagination="getList" />
  
  <!-- 添加或修改部门角色信息对话框 -->
  <el-dialog :title="authUser.title" v-model="authUser.open" width="80%" :before-close="closeAuthUser" append-to-body>
    <dept-auth-user
      v-if="authUser.open"
      :deptRoleId="authUser.deptRoleId"
      :operate="authUser.operate"
      ref="deptAuthUser"/>
  </el-dialog>

</template>

<script setup name="DeptRoleVersion" lang="ts">
import { listDeptRole, upgradeDeptRole, validDeptRole } from "@/api/system/deptRole"
import DeptAuthUser from "@/views/system/deptRole/authUser.vue"
const props = defineProps({
  deptRoleCode: {
    type: String,
    required: true
  },
  deptOptions: {
    type: Array,
    default: () => []
  }
});
const { proxy } = getCurrentInstance() as ComponentInternalInstance;
const { sys_normal_disable } = proxy.useDict('sys_normal_disable');
const loading = ref(false);
const deptRoleList = ref([]);
const total = ref(0);
const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  deptRoleCode: props.deptRoleCode
});
// 授权用户
const authUser = reactive({
  title: undefined,
  open: false,
  deptRoleId: undefined,
  operate: undefined
})

/** 查询部门角色信息列表 */
const getList = ()=> {
  loading.value = true;
  listDeptRole(queryParams).then(response => {
    deptRoleList.value = response.rows;
    total.value = response.total;
    if (total.value > 0) {
      deptRoleList.value.forEach(item => {
        item.validTime = true
        const validTime = item.deptRoleValidTime
        if (null != validTime && new Date(validTime) <= new Date()) {
          item.validTime = false
        }
      })
    }
    loading.value = false;
  });
};

/** 升级按钮操作 */
const handleUpgrade = (row) => {
  const deptRoleIds = row.deptRoleId
  proxy.$modal.confirm('是否确认升级部门角色信息为"' + row.deptRoleName + '"的数据项？').then(() => {
    loading.value = true;
    return upgradeDeptRole(deptRoleIds);
  }).then(() => {
    loading.value = false;
    getList();
    proxy.$modal.msgSuccess("操作成功");
  }).catch(() => {
  }).finally(() => {
    loading.value = false;
  });
};

/** 生效按钮操作 */
const handleTakeEffect = (row) => {
  const deptRoleIds = row.deptRoleId
  proxy.$modal.confirm('是否确认生效部门角色版本为"' + row.deptRoleVersion + '"的数据项？').then(() => {
    loading.value = true;
    return validDeptRole(deptRoleIds);
  }).then(() => {
    loading.value = false;
    getList();
    proxy.$modal.msgSuccess("生效成功");
  }).catch(() => {
  }).finally(() => {
    loading.value = false;
  });
};

/** 分配用户操作 */
const handleAuthUser = (row, operate)=> {
  Object.assign(authUser, {
    title: row.deptRoleName,
    open: true,
    deptRoleId: row.deptRoleId,
    operate
  })
};

/** 关闭分配用户 */
const closeAuthUser = ()=> {
  Object.assign(authUser, {
    title: undefined,
    open: false,
    deptRoleId: undefined,
    operate: undefined
  })
};

onMounted(() => {
  getList();
});
</script>

<style lang="scss" scoped>

</style>