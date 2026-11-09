<template>
  <div class="p-2">
    <transition :enter-active-class="proxy?.animate.searchAnimate.enter" :leave-active-class="proxy?.animate.searchAnimate.leave">
      <div v-show="showSearch" class="mb-[10px]">
        <el-card shadow="hover">
          <el-form ref="queryFormRef" :model="queryParams" :inline="true" label-width="auto">
            <el-form-item label="名称" prop="deptRoleName">
              <el-input v-model="queryParams.deptRoleName" placeholder="请输入名称" clearable @change="handleQuery" @keyup.enter="handleQuery" />
            </el-form-item>
            <el-form-item label="角色" prop="roleId">
              <el-select v-model="queryParams.roleId"
                style="width: 100%;"
                @change="handleQuery"
                filterable
                clearable
                placeholder="请选择角色">
                <el-option
                  v-for="item in roleOptions"
                  :key="item.roleId"
                  :label="item.roleName"
                  :value="item.roleId">
                </el-option>
              </el-select>
            </el-form-item>
            <el-form-item label="备注" prop="remark">
              <el-input v-model="queryParams.remark" placeholder="请输入备注" clearable @change="handleQuery" @keyup.enter="handleQuery" />
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
        </el-card>
      </div>
    </transition>

    <el-card shadow="hover">
      <template #header>
        <el-row :gutter="10" class="mb8">
          <el-col :span="1.5">
            <el-button v-hasPermi="['system:deptRole:add']" type="primary" icon="Plus" @click="handleAdd">新增</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button v-hasPermi="['system:deptRole:edit']" type="primary" icon="Edit" :disabled="single" @click="handleUpdate(selectRow)"
              >修改</el-button
            >
          </el-col>
          <el-col :span="1.5">
            <el-button v-hasPermi="['system:deptRole:edit']" type="primary" icon="document-copy" :disabled="single" @click="handleCopy(selectRow)"
              >复制</el-button
            >
          </el-col>
          <!-- <el-col :span="1.5">
            <el-button v-hasPermi="['system:deptRole:remove']" type="primary" icon="Delete" :disabled="multiple" @click="handleDelete">
              删除
            </el-button>
          </el-col> -->
          <right-toolbar v-model:show-search="showSearch" @query-table="getList"></right-toolbar>
        </el-row>
      </template>

      <el-table v-loading="loading" border :data="deptRoleList" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column label="序号" type="index" width="55" align="center" fixed="left" />
        <el-table-column label="编号" min-width="100" align="center" prop="deptRoleId" v-if="false" />
        <el-table-column label="名称" min-width="350" align="center" prop="deptRoleName" :show-overflow-tooltip="true" fixed="left" />
        <el-table-column label="部门" width="180" align="center" prop="deptName" show-overflow-tooltip />
        <el-table-column label="角色" width="180" align="center" prop="roleName" show-overflow-tooltip />
        <el-table-column label="状态" align="center" prop="deptRoleStatus" width="80" :show-overflow-tooltip="true">
          <template  #default="{row}">
            <dict-tag :options="sys_normal_disable" :value="row.deptRoleStatus"/>
          </template>
        </el-table-column>
        <el-table-column label="类型" align="center" prop="deptRoleType" width="80" :show-overflow-tooltip="true">
          <template  #default="{row}">
            <dict-tag :options="system_deptrole_type" :value="row.deptRoleType"/>
          </template>
        </el-table-column>
        <el-table-column label="有效期" align="center" prop="deptRoleValidTime" width="170" :show-overflow-tooltip="true">
          <template #default="{row}">
            <span>{{ parseTime(row.deptRoleValidTime, '{y}-{m}-{d} {h}:{i}:{s}') }}</span>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" align="center" prop="createTime" width="180" :show-overflow-tooltip="true"/>
        <el-table-column label="备注" align="center" prop="remark" width="150" :show-overflow-tooltip="true"/>
        <el-table-column width="150" label="操作" align="center" fixed="right" class-name="small-padding fixed-width">
          <template #default="scope">
            <el-tooltip content="修改" placement="top">
              <el-button v-hasPermi="['system:deptRole:edit']" link type="primary" icon="Edit" @click="handleUpdate(scope.row)"></el-button>
            </el-tooltip>
            <el-tooltip content="复制" placement="top">
              <el-button v-hasPermi="['system:deptRole:edit']" link type="primary" icon="document-copy" @click="handleCopy(scope.row)"></el-button>
            </el-tooltip>
            <el-tooltip content="删除" placement="top">
              <el-button v-hasPermi="['system:deptRole:remove']" link type="primary" icon="Delete" @click="handleDelete(scope.row)"></el-button>
            </el-tooltip>
            <el-tooltip content="版本" placement="top">
              <el-button v-hasPermi="['system:deptRole:query']" link type="primary" icon="PriceTag" @click="handleVersion(scope.row)"></el-button>
            </el-tooltip>
          </template>
        </el-table-column>
      </el-table>
      <pagination v-show="total > 0" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :total="total" @pagination="getList" />
    </el-card>
    <!-- 添加或修改部门角色信息对话框 -->
    <el-dialog v-model="dialog.visible" :title="dialog.title" width="550px" append-to-body>
      <el-form ref="formRef" :model="form" v-loading="dialog.loading" :rules="rules" label-width="70px">
        <el-form-item label="名称" prop="deptRoleName">
          <el-input v-model="form.deptRoleName" placeholder="请输入部门角色名称" />
        </el-form-item>
        <el-form-item label="类型" prop="deptRoleType">
          <el-select v-model="form.deptRoleType"
            style="width: 100%;"
            placeholder="请选择类型">
            <el-option
              v-for="dict in system_deptrole_type"
              :key="dict.value"
              :label="dict.label"
              :value="dict.value"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="部门" prop="deptId" v-if="form.deptRoleType == 'Department Level'">
          <el-tree-select
            v-model="form.deptId"
            :data="deptOptions"
            filterable
            :props="{ value: 'id', label: 'label', children: 'children' }"
            value-key="id"
            placeholder="请选择部门"
            check-strictly
          />
        </el-form-item>
        <el-form-item label="角色" prop="roleId">
          <el-select v-model="form.roleId"
            style="width: 100%;"
            filterable
            :disabled="form.deptRoleId"
            placeholder="请选择类型">
            <el-option
              v-for="item in roleOptions"
              :key="item.roleId"
              :label="item.roleName"
              :value="item.roleId">
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="有效期" prop="deptRoleValidTime">
          <el-date-picker clearable
            v-model="form.deptRoleValidTime"
            style="width: 100%;"
            type="datetime"
            :default-time="defaultTime"
            value-format="YYYY-MM-DD HH:mm:ss"
            placeholder="请选择有效期">
          </el-date-picker>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button :loading="buttonLoading" type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 版本对话框 -->
    <el-dialog :title="version.title" v-model="version.open" width="80%" :before-close="cancelVersion" append-to-body>
      <dept-role-version
        :deptRoleCode="version.deptRoleCode"
        v-if="version.open"
        ref="deptRoleVersion" />
    </el-dialog>
  </div>
</template>

<script setup name="DeptRole" lang="ts">
import { 
  listDeptRole, 
  getDeptRole, 
  delDeptRole,
  addDeptRole, 
  updateDeptRole, 
  copyDeptRole 
} from "@/api/system/deptRole"
import { listRole } from "@/api/system/role"
import { deptTreeSelect } from "@/api/system/user"
import DeptRoleVersion from "@/views/system/deptRole/version.vue";

const { proxy } = getCurrentInstance() as ComponentInternalInstance;

const { sys_normal_disable, system_deptrole_type } = toRefs<any>(proxy?.useDict('sys_normal_disable', 'system_deptrole_type'));

const deptRoleList = ref([]);
const buttonLoading = ref(false);
const loading = ref(true);
const showSearch = ref(true);
const ids = ref<Array<string | number>>([]);
const names = ref<Array<string | number>>([]);
const single = ref(true);
const selectRow = ref({});
const multiple = ref(true);
const total = ref(0);
const deptOptions = ref([]);

const queryFormRef = ref<ElFormInstance>();
const formRef = ref<ElFormInstance>();
const defaultTime = ref(new Date(2000, 0, 1, 23, 59, 59));

const dialog = reactive({
  visible: false,
  loading: false,
  title: ''
});

const initFormData = {
  deptRoleId: undefined,
  copyDeptRoleId: undefined,
  tenantId: undefined,
  deptId: undefined,
  roleId: undefined,
  deptRoleName: undefined,
  deptRoleCode: undefined,
  deptRoleSort: 0,
  deptRoleVersion: undefined,
  deptRoleStatus: undefined,
  deptRoleType: 'Department Level',
  deptRoleValidTime: undefined,
  delFlag: undefined,
  createBy: undefined,
  createTime: undefined,
  createId: undefined,
  createDeptId: undefined,
  updateBy: undefined,
  updateTime: undefined,
  updateId: undefined,
  updateDeptId: undefined,
  remark: undefined
};
const data = reactive({
  form: { ...initFormData },
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    tenantId: undefined,
    deptId: undefined,
    copyDeptRoleId: undefined,
    roleId: undefined,
    deptRoleName: undefined,
    deptRoleCode: undefined,
    deptRoleSort: undefined,
    deptRoleVersion: undefined,
    deptRoleStatus: '0',
    deptRoleType: undefined,
    deptRoleValidTime: undefined,
    createId: undefined,
    createDeptId: undefined,
    updateId: undefined,
    updateDeptId: undefined,
    remark: undefined,
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

const { queryParams, form, rules } = toRefs(data);
const roleOptions = ref([]);
const operate = ref('');
const version = reactive({
  open: false,
  title: undefined,
  deptRoleCode: undefined,
  deptRoleId: undefined
})

/** 查询部门下拉树结构 */
const getRoleOptions = async () => {
  listRole().then(response => roleOptions.value = response.rows);
}

/** 查询部门下拉树结构 */
const getDeptTree = async () => {
  deptTreeSelect({
    authFilter: '0',
  }).then(response => {
    deptOptions.value = response.data;
  });
}

/** 查询部门角色信息列表 */
const getList = async () => {
  loading.value = true;
  const res = await listDeptRole(queryParams.value);
  deptRoleList.value = res.rows;
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
  formRef.value?.resetFields();
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
  ids.value = selection.map((item:any) => item.deptRoleId);
  names.value = selection.map((item:any) => item.deptRoleName);
  single.value = selection.length != 1;
  multiple.value = !selection.length;
  selectRow.value = selection[0]||{};
};

/** 新增按钮操作 */
const handleAdd = () => {
  reset();
  operate.value = 'add';
  dialog.visible = true;
  dialog.loading = false;
  dialog.title = '添加部门角色信息';
};

/** 修改按钮操作 */
const handleUpdate = async (row) => {
  reset();
  operate.value = 'edit';
  const _id = row?.deptRoleId || ids.value[0];
  dialog.loading = true;
  dialog.visible = true;
  dialog.title = '修改部门角色信息';
  const res = await getDeptRole(_id);
  Object.assign(form.value, res.data);
  dialog.loading = false;
};

/** 复制按钮操作 */
const handleCopy = (row) => {
  reset();
  operate.value = 'copy';
  const deptRoleId = row.deptRoleId || ids[0];
  dialog.visible = true;
  dialog.title = "复制部门角色信息";
  dialog.loading = true;
  getDeptRole(deptRoleId).then(response => {
    dialog.loading = false;
    const data = response.data;
    Object.assign(form.value, {
      roleId: data.roleId,
      deptRoleType: data.deptRoleType,
      deptRoleValidTime: data.deptRoleValidTime,
      remark: data.remark,
      copyDeptRoleId: deptRoleId,
    });
  });
};

/** 打开版本 */
const handleVersion = (row)=> {
  const data = row.deptRoleId ? row : selectRow
  Object.assign(version, {
    open: true,
    title: `【${data.deptRoleName}】版本管理`,
    deptRoleCode: data.deptRoleCode,
    deptRoleId: data.deptRoleId
  })
}

/** 关闭版本弹窗 */
const cancelVersion = ()=> {
  Object.assign(version, {
    open: false,
    title: undefined,
    deptRoleCode: undefined,
    deptRoleId: undefined
  })
}

/** 提交按钮 */
const submitForm = () => {
  formRef.value?.validate(async (valid: boolean) => {
    if (valid) {
      buttonLoading.value = true;
      if (form.value.deptRoleId) {
        await updateDeptRole(form.value).finally(() => (buttonLoading.value = false));
      } else {
        await addDeptRole(form.value).finally(() => (buttonLoading.value = false));
      }
      proxy?.$modal.msgSuccess('操作成功');
      dialog.visible = false;
      dialog.loading = false;
      await getList();
    }
  });
};

/** 删除按钮操作 */
const handleDelete = async (row) => {
  const _ids = row?.deptRoleId || ids.value;
  const _names = row?.deptRoleName || names.value;
  await proxy?.$modal.confirm('是否确认删除名称为"' + _names + '"的数据项？');
  loading.value = true;
  await delDeptRole(_ids).finally(() => (loading.value = false));
  await getList();
  proxy?.$modal.msgSuccess('删除成功');
};

onMounted(() => {
  getRoleOptions();
  getDeptTree();
  getList();
});
</script>
