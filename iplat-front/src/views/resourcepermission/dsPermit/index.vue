<template>
  <div class="p-2">
    <transition :enter-active-class="proxy?.animate.searchAnimate.enter" :leave-active-class="proxy?.animate.searchAnimate.leave">
      <div v-show="showSearch" class="mb-[10px]">
        <el-card shadow="hover">
          <el-form ref="queryFormRef" :model="queryParams" :inline="true" label-width="auto">
            <!-- <el-form-item label="编号" prop="dsPermitCode">
              <el-input v-model="queryParams.dsPermitCode" placeholder="请输入编号" clearable @change="handleQuery" @keyup.enter="handleQuery" />
            </el-form-item> -->
            <el-form-item label="功能操作" prop="dsFunId">
              <el-select v-model="queryParams.dsFunId" clearable filterable  @change="handleQuery"  placeholder="请选择">
                <el-option
                  v-for="item in dsFunOption"
                  :key="item.dsFunId"
                  :label="item.dsFunName"
                  :value="item.dsFunId"
                >
                </el-option>
              </el-select>
            </el-form-item>
            <el-form-item label="业务对象" prop="businessId">
              <el-select v-model="queryParams.businessId" clearable filterable  @change="handleQuery"  placeholder="请选择">
                <el-option
                  v-for="item in businessOptions"
                  :key="item.businessId"
                  :label="item.businessName"
                  :value="item.businessId"
                >
                </el-option>
              </el-select>
            </el-form-item>
            <el-form-item label="是否启用" prop="dsPermitEnable">
              <el-select v-model="queryParams.dsPermitEnable" clearable  @change="handleQuery"  placeholder="请选择是否启用">
                <el-option v-for="dict in system_yes_no" :key="dict.value" :label="dict.label" :value="dict.value"></el-option>
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
              <el-button icon="Refresh" @click="resetQuery">重置</el-button>
              <el-button icon="Refresh" :loading="refreshLoading" @click="refreshCache">刷新缓存</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </div>
    </transition>

    <el-card shadow="hover">
      <template #header>
        <el-row :gutter="10" class="mb8">
          <el-col :span="1.5">
            <el-button v-hasPermi="['system:dsPermit:add']" type="primary" icon="Plus" @click="handleAdd">新增</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button v-hasPermi="['system:dsPermit:edit']" type="primary" icon="Edit" :disabled="single" @click="handleUpdate(selectRow)"
              >修改</el-button
            >
          </el-col>
          <el-col :span="1.5">
            <el-button v-hasPermi="['system:dsPermit:remove']" type="primary" icon="Delete" :disabled="multiple" @click="handleDelete">
              删除
            </el-button>
          </el-col>
          <el-col :span="1.5" v-if="false">
            <el-button v-hasPermi="['system:dsPermit:export']" type="primary" icon="Download" @click="handleExport">导出</el-button>
          </el-col>
          <right-toolbar v-model:show-search="showSearch" @query-table="getList"></right-toolbar>
        </el-row>
      </template>

      <el-table v-loading="loading" border :data="dsPermitList" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column label="id" min-width="100" align="center" prop="dsPermitId" v-if="false" />
        <el-table-column label="序号" type="index" width="55" align="center" />
        <el-table-column label="编号" width="100" align="center" prop="dsPermitCode" />
        <el-table-column label="名称" min-width="100" align="center" prop="dsPermitName" show-overflow-tooltip />
        <el-table-column label="功能操作" min-width="100" align="center" prop="dsFunName" show-overflow-tooltip />
        <el-table-column label="业务对象" min-width="120" align="center" prop="businessName" show-overflow-tooltip />
        <el-table-column label="是否启用" width="80" align="center" prop="dsPermitEnable">
          <template #default="{row}">
            <dict-tag :options="system_yes_no" :value="row.dsPermitEnable" />
          </template>
        </el-table-column>
        <el-table-column label="备注" align="center" prop="remark" show-overflow-tooltip />
        <el-table-column width="120" label="操作" align="center" fixed="right" class-name="small-padding fixed-width">
          <template #default="scope">
            <el-tooltip content="修改" placement="top">
              <el-button v-hasPermi="['system:dsPermit:edit']" link type="primary" icon="Edit" @click="handleUpdate(scope.row)"></el-button>
            </el-tooltip>
            <el-tooltip content="删除" placement="top">
              <el-button v-hasPermi="['system:dsPermit:remove']" link type="danger" icon="Delete" @click="handleDelete(scope.row)"></el-button>
            </el-tooltip>
            <el-tooltip content="配置" placement="top">
              <el-button v-hasPermi="['system:dsPermit:edit']" link type="primary" icon="Setting" @click="handleSetting(scope.row)"></el-button>
            </el-tooltip>
          </template>
        </el-table-column>
      </el-table>
      <pagination v-show="total > 0" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :total="total" @pagination="getList" />
    </el-card>
    <!-- 添加或修改数据集授权配置对话框 -->
    <el-dialog v-model="dialog.visible" :title="dialog.title" width="550px" append-to-body>
      <el-form ref="tenantFormRef" :model="form" v-loading="dialog.loading" :rules="rules" label-width="auto">
        <el-form-item label="编号" prop="dsPermitCode">
          <el-input v-model="form.dsPermitCode" placeholder="请输入编号" />
        </el-form-item>
        <el-form-item label="名称" prop="dsPermitName">
          <el-input v-model="form.dsPermitName" placeholder="请输入名称" />
        </el-form-item>
        <el-form-item label="功能操作" prop="dsFunId">
          <el-select v-model="form.dsFunId" filterable placeholder="请选择">
            <el-option
              v-for="item in dsFunOption"
              :key="item.dsFunId"
              :label="item.dsFunName"
              :value="item.dsFunId"
            >
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="业务对象" prop="businessId">
          <el-select v-model="form.businessId" clearable filterable :disabled="operate == 'edit'" placeholder="请选择">
            <el-option
              v-for="item in businessOptions"
              :key="item.businessId"
              :label="item.businessName"
              :value="item.businessId"
            >
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="启用" prop="dsPermitEnable">
          <el-radio-group v-model="form.dsPermitEnable">
            <el-radio v-for="dict in system_yes_no" :label="dict.value">{{dict.label}}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" autosize placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button :loading="buttonLoading" type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="DsPermit" lang="ts">
import {
  listDsPermit, 
  getDsPermit, 
  delDsPermit, 
  addDsPermit, 
  updateDsPermit,
  refreshDsPermit
} from '@/api/resourcepermission/dsPermit';
import { listBusinessAll } from '@/api/resourcepermission/businessObject';
import { listDsFunAll } from '@/api/resourcepermission/dsFun';

const { proxy } = getCurrentInstance() as ComponentInternalInstance;
const { system_yes_no } = toRefs<any>(proxy.useDict('system_yes_no'));

const dsPermitList = ref([]);
const buttonLoading = ref(false);
const loading = ref(true);
const refreshLoading = ref(false);
const showSearch = ref(true);
const ids = ref<Array<string | number>>([]);
const names = ref<Array<string | number>>([]);
const single = ref(true);
const selectRow = ref({});
const multiple = ref(true);
const total = ref(0);
const businessOptions = ref([]);
const dsFunOption = ref([]);

const queryFormRef = ref<ElFormInstance>();
const tenantFormRef = ref<ElFormInstance>();

const dialog = reactive({
  visible: false,
  loading: false,
  title: ''
});

const initFormData = {
  dsPermitId: '',
  dsPermitCode: '',
  dsPermitName: '',
  businessId: '',
  dsFunId: '',
  dsPermitEnable: '1',
  remark: ''
};
const data = reactive({
  form: { ...initFormData },
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    dsPermitId: '',
    dsPermitCode: '',
    dsPermitName: '',
    businessId: '',
    dsFunId: '',
    dsPermitEnable: ''
  },
  rules: {
    dsPermitCode: [{ required: true, message: '编号不能为空', trigger: 'blur' }],
    dsPermitName: [{ required: true, message: '名称不能为空', trigger: 'blur' }],
    dsFunId: [{ required: true, message: '请选择功能操作', trigger: 'blur' }],
    businessId: [{ required: true, message: '请选择业务对象', trigger: 'blur' }],
  }
});
const operate = ref('details');

const { queryParams, form, rules } = toRefs(data);


/** 查询数据集授权配置列表 */
const getList = async () => {
  loading.value = true;
  const res = await listDsPermit(queryParams.value);
  dsPermitList.value = res.rows;
  total.value = res.total;
  loading.value = false;
};

// 查询数据集绑定数据
const getOptions = () =>{
  listBusinessAll().then((res) => {
    businessOptions.value = res.data;
  });
  listDsFunAll().then((res) => {
    dsFunOption.value = res.data;
  });
};

const dsFunChange = (val) => {
  const obj = dsFunOption.value.find(item=>item.dsFunId == val)||{}
  form.value.dsPermitName = obj.dsFunName
}

const businChange = (val) => {
  const obj = businessOptions.value.find(item=>item.businessId == val)||{}
  form.value.dsPermitName = obj.businessName
}

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
  ids.value = selection.map((item:any) => item.dsPermitId);
  names.value = selection.map((item:any) => item.dsPermitName);
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
  dialog.title = '添加数据集授权配置';
};

/** 修改按钮操作 */
const handleUpdate = async (row) => {
  reset();
  operate.value = 'edit';
  const _id = row?.dsPermitId || ids.value[0];
  dialog.visible = true;
  dialog.loading = true;
  dialog.title = '修改数据集授权配置';
  const res = await getDsPermit(_id);
  Object.assign(form.value, res.data);
  dialog.loading = false;
};

/** 配置按钮操作 */
const handleSetting = async ({ dsPermitId, businessId, dsPermitName }) => {
  proxy?.$tab.openPage(`/system/resourcepermission/setting/${dsPermitId}`, `【${dsPermitName}】配置`, { businessId });
};

/** 提交按钮 */
const submitForm = () => {
  tenantFormRef.value?.validate(async (valid: boolean) => {
    if (valid) {
      buttonLoading.value = true;
      if (form.value.dsPermitId) {
        await updateDsPermit(form.value).finally(() => (buttonLoading.value = false));
      } else {
        await addDsPermit(form.value).finally(() => (buttonLoading.value = false));
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
  const _ids = row?.dsPermitId || ids.value;
  const _names = row?.dsPermitName || names.value;
  await proxy?.$modal.confirm('是否确认删除名称为"' + _names + '"的数据项？');
  loading.value = true;
  await delDsPermit(_ids).finally(() => (loading.value = false));
  await getList();
  proxy?.$modal.msgSuccess('删除成功');
};

// 刷新缓存
const refreshCache = () =>{
  refreshLoading.value = true;
  refreshDsPermit().then(() => {
    proxy?.$modal.msgSuccess('操作成功');
  }).finally(() => {
    refreshLoading.value = false;
  })
}

/** 导出按钮操作 */
const handleExport = () => {
  proxy?.download(
    'ds-permit/export',
    {
      ...queryParams.value
    },
    `dsPermit_${new Date().getTime()}.xlsx`
  );
};

onMounted(() => {
  getList();
  getOptions();
});
onActivated(() => {
  getOptions();
})
</script>
