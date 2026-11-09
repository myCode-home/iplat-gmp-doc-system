<template>
  <div>
    <el-row :gutter="20">
      <!-- 部门树 -->
      <el-col :lg="6" :xs="24" style="">
        <el-card shadow="hover">
          <el-input v-model="deptName" placeholder="请输入部门名称" prefix-icon="Search" clearable />
          <el-tree
            ref="deptTreeRef"
            class="mt-2"
            node-key="id"
            :data="deptOptions"
            :props="{ label: 'label', children: 'children' }"
            :expand-on-click-node="false"
            :filter-node-method="filterNode"
            highlight-current
            default-expand-all
            @node-click="handleNodeClick"
          />
        </el-card>
      </el-col>
      <el-col :lg="18" :xs="24">
        <transition :enter-active-class="proxy?.animate.searchAnimate.enter" :leave-active-class="proxy?.animate.searchAnimate.leave">
          <div v-show="showSearch" class="mb-[10px]">
            <el-card shadow="hover">
              <el-form ref="queryFormRef" :model="queryParams" :inline="true">
                <el-form-item label="用户名称" prop="userName">
                  <el-input v-model="queryParams.userName" placeholder="请输入用户名称" clearable @keyup.enter="handleQuery" />
                </el-form-item>
                <el-form-item label="手机号码" prop="phonenumber">
                  <el-input v-model="queryParams.phonenumber" placeholder="请输入手机号码" clearable @keyup.enter="handleQuery" />
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
                  <el-button icon="Refresh" @click="() => resetQuery()">重置</el-button>
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
          <el-table ref="tableRef" :class="{nomultiple: !multipleChoice}" max-height="400px" row-key="userId" v-loading="loading" border :data="userList" @select="selectChange" @selection-change="handleSelectionChange">
            <el-table-column type="selection" reserve-selection width="55" align="center" />
            <el-table-column label="id" prop="userId" width="55" v-if="false" align="center" />
            <el-table-column label="序号" type="index" width="55" align="center" />
            <el-table-column label="工号" min-width="120" align="center" prop="userName" />
            <el-table-column label="姓名" min-width="120" align="center" prop="nickName" />
            <el-table-column label="部门" width="100" align="center" prop="deptName" />
            <el-table-column label="手机号码" width="100" align="center" prop="phonenumber" />
            <el-table-column label="状态" width="120" align="center" prop="status">
              <template #default="{row}">
                <dict-tag :options="sys_normal_disable" :value="row.status"></dict-tag>
              </template>
            </el-table-column>
            <el-table-column label="备注" align="center" prop="remark" show-overflow-tooltip />
            <el-table-column label="创建时间" width="120" align="center" prop="createTime">
              <template #default="scope">
                <span>{{ proxy.parseTime(scope.row.createTime, '{y}-{m}-{d}') }}</span>
              </template>
            </el-table-column>
          </el-table>

          <pagination
            v-show="total > 0"
            v-model:page="queryParams.pageNum"
            v-model:limit="queryParams.pageSize"
            :total="total"
            @pagination="getList"
          />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script name="UserSelect" lang="ts" setup>
import api from '@/api/system/user';
import { UserQuery, UserVO } from '@/api/system/user/types';
import { DeptTreeVO, DeptVO } from '@/api/system/dept/types';

const props = defineProps({
  multipleChoice: {
    type: Boolean,
    default: true
  },
  otherCondition: {
    type: Object,
    default: () =>{}
  },
  multipleChoiceData: {
    type: Function,
    default: () => ()=>{}
  },
  userIds: {
    type: String,
    defalult: ''
  }
})

const { proxy } = getCurrentInstance() as ComponentInternalInstance;
const { sys_normal_disable } = toRefs<any>(proxy?.useDict('sys_normal_disable'));

const userList = ref<UserVO[]>();
const loading = ref(true);
const showSearch = ref(true);
const total = ref(0);
const dateRange = ref<[DateModelType, DateModelType]>(['', '']);
const deptName = ref('');
const deptOptions = ref<DeptTreeVO[]>([]);
const selectUserList = ref<UserVO[]>([]);

const deptTreeRef = ref<ElTreeInstance>();
const queryFormRef = ref<ElFormInstance>();
const tableRef = ref<ElTableInstance>();


const queryParams = ref<UserQuery>({
  pageNum: 1,
  pageSize: 10,
  userName: '',
  phonenumber: '',
  status: '',
  deptId: '',
  roleId: '',
  userIds: ''
});

watch(() => props.otherCondition, () => {
  Object.keys(props.otherCondition).forEach(key => {
    queryParams.value[key] = props.otherCondition[key];
  });
}, { immediate: true, deep: true });

const ids = ref([]);
const names = ref([]);
const selectData = ref([]);


/** 根据名称筛选部门树 */
watchEffect(
  () => {
    deptTreeRef.value?.filter(deptName.value);
  },
  {
    flush: 'post' // watchEffect会在DOM挂载或者更新之前就会触发，此属性控制在DOM元素更新后运行
  }
);

/** 通过条件过滤节点  */
const filterNode = (value: string, data: any) => {
  if (!value) return true;
  return data.label.indexOf(value) !== -1;
};

/** 查询部门下拉树结构 */
const getTreeSelect = async () => {
  const res = await api.deptTreeSelect();
  deptOptions.value = res.data[0].children;
};

/** 查询用户列表 */
const getList = async () => {
  loading.value = true;
  queryParams.value.userIds = props.userIds;
  const res = await api.listUser(proxy?.addDateRange(queryParams.value, dateRange.value));
  loading.value = false;
  userList.value = res.rows;
  total.value = res.total;
};

/** 节点单击事件 */
const handleNodeClick = (data: DeptVO) => {
  queryParams.value.deptId = data.id;
  handleQuery();
};

/** 搜索按钮操作 */
const handleQuery = () => {
  queryParams.value.pageNum = 1;
  getList();
};
/** 重置按钮操作 */
const resetQuery = (refresh = true) => {
  dateRange.value = ['', ''];
  queryFormRef.value?.resetFields();
  queryParams.value.pageNum = 1;
  queryParams.value.deptId = undefined;
  deptTreeRef.value?.setCurrentKey(undefined);
  refresh && handleQuery();
};

// 多选框选中数据
const handleSelectionChange = (selection:any) => {
  ids.value = selection.map((item:any) => item.userGroupId);
  names.value = selection.map((item:any) => item.userGroupName);
  selectData.value = selection;
};

const selectChange = (selection) =>{
  if(!props.multipleChoice){
    if(selection.length > 1){
      const del_row = selection.shift()
      tableRef.value.toggleRowSelection(del_row, false)
    }
  }
}

/** 提交按钮操作 */
const confirmChoiceData = async () => {
  if(selectData.value.length == 0){
    return proxy?.$modal.msgError('请选择');
  }
  const data = selectData.value.map((item) => {
    item.id = item.userId
    item.name = item.nickName
    return item
  })
  if(props.multipleChoice){
    props.multipleChoiceData(data);
  }else{
    props.multipleChoiceData(data[0]);
  }
};

onMounted(() => {
  getList();
  getTreeSelect();
})

defineExpose({
  confirmChoiceData
});
</script>

<style lang="scss" scoped>
//找到表头，然后隐藏掉复选框
:deep(.nomultiple){
  .el-table__header-wrapper {
    .el-checkbox{
      display:none
    }
  }
} 
</style>
