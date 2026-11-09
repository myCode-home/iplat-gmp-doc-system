<template>
  <div class="p-2">
    <transition :enter-active-class="proxy?.animate.searchAnimate.enter" :leave-active-class="proxy?.animate.searchAnimate.leave">
      <div class="mb-[10px]">
        <el-card shadow="hover" :class="{ topCard: !showSearch }">
          <template #header>
            所属模块：
            <el-select v-model="queryParams.sysModule"  @change="queryModuleChange" style="width: 250px;" placeholder="请选择">
              <el-option v-for="dict in system_service_type" :key="dict.value" :label="dict.label" :value="dict.value"></el-option>
            </el-select>
          </template>
          <div v-if="showSearch">
            <el-form ref="queryFormRef" :model="queryParams" :inline="true" label-width="auto" >
              <el-form-item label="url" prop="auUri">
                <el-select v-model="queryParams.auUri" filterable clearable @change="handleQuery" placeholder="请选择url">
                  <el-option
                    v-loading="urlLoading"
                    v-for="(item,index) in urlList"
                    :key="item.uri+item.method+index"
                    :label="`${item.uri},${item.method}`"
                    :value="`${item.uri},${item.method}`"
                  >
                    <span style="float: left">{{ item.uri }}</span>
                    <span style="float: right; color: var(--el-text-color-secondary); font-size: 13px">{{ item.method }}
                    </span>
                  </el-option>
                </el-select>
                <!-- <el-input v-model="queryParams.auUri" placeholder="请输入url" clearable @change="handleQuery" @keyup.enter="handleQuery" /> -->
              </el-form-item>
              <el-form-item label="url名称" prop="auName">
                <el-input v-model="queryParams.auName" placeholder="请输入url名称" clearable @change="handleQuery" @keyup.enter="handleQuery" />
              </el-form-item>
              <el-form-item label="是否开启" prop="auEnabled">
                <el-select v-model="queryParams.auEnabled" clearable  @change="handleQuery"  placeholder="请选择是否开启">
                  <el-option v-for="dict in system_yes_no" :key="dict.value" :label="dict.label" :value="dict.value"></el-option>
                </el-select>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
                <el-button icon="Refresh" @click="resetQuery">重置</el-button>
              </el-form-item>
            </el-form>
          </div>
        </el-card>
      </div>
    </transition>

    <el-card shadow="hover">
      <template #header>
        <el-row :gutter="10" class="mb8">
          <el-col :span="1.5">
            <el-button v-hasPermi="['system:auditUri:add']" type="primary" plain icon="Plus" @click="handleAdd">新增</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button v-hasPermi="['system:auditUri:edit']" type="success" plain icon="Edit" :disabled="single" @click="handleUpdate(selectRow)"
              >修改</el-button
            >
          </el-col>
          <el-col :span="1.5">
            <el-button v-hasPermi="['system:auditUri:remove']" type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete">
              删除
            </el-button>
          </el-col>
          <el-col :span="1.5" v-if="true">
            <el-button v-hasPermi="['system:auditUri:export']" type="warning" plain icon="Download" @click="handleExport">
              导出
            </el-button>
          </el-col>
          <right-toolbar v-model:show-search="showSearch" @query-table="getList"></right-toolbar>
        </el-row>
      </template>

      <el-table v-loading="loading" border :data="auditTrackList" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column label="序号" type="index" width="55" align="center" prop="index" />
        <el-table-column label="编号" align="center" v-if="false" min-width="120" prop="auditUriId" />
        <el-table-column label="url" align="center" min-width="130" prop="auUri" />
        <el-table-column label="请求方式" align="center" width="90" prop="auMethod" />
        <el-table-column label="是否开启" width="80" align="center" prop="auEnabled">
          <template #default="{row}">
            <dict-tag :options="system_yes_no" :value="row.auEnabled" />
          </template>
        </el-table-column>
        <el-table-column label="所属服务" width="150" align="center" prop="sysModule">
          <template #default="{row}">
            <dict-tag :options="system_service_type" :value="row.sysModule" />
          </template>
        </el-table-column>
        <el-table-column label="功能标识" align="center" width="120" prop="funcSign"/>
        <el-table-column label="url名称" align="center" min-width="100" prop="auName" show-overflow-tooltip />
        <el-table-column width="120" label="操作" align="center" class-name="small-padding fixed-width">
          <template #default="scope">
            <el-tooltip content="修改" placement="top">
              <el-button v-hasPermi="['system:auditUri:edit']" link type="primary" icon="Edit" @click="handleUpdate(scope.row)"></el-button>
            </el-tooltip>
            <el-tooltip content="删除" placement="top">
              <el-button v-hasPermi="['system:auditUri:remove']" link type="primary" icon="Delete" @click="handleDelete(scope.row)"></el-button>
            </el-tooltip>
          </template>
        </el-table-column>
      </el-table>
      <pagination v-show="total > 0" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :total="total" @pagination="getList" />
    </el-card>
    <!-- 添加或修改审计追踪URL对话框 -->
    <el-dialog v-model="dialog.visible" :title="dialog.title" :before-close="cancel" width="500px" append-to-body>
      <el-form ref="tenantFormRef" v-loading="dialog.loading" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="所属服务" prop="sysModule">
          <el-select v-model="form.sysModule" @change="sysModuleChange" placeholder="请选择">
            <el-option v-for="dict in system_service_type" :key="dict.value" :label="dict.label" :value="dict.value"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="url" prop="auUri">
          <el-select v-model="form.auUri" filterable reserve-keyword @change="urlChange" v-loading="urlLoading" placeholder="请选择url">
            <el-option
              v-for="(item,index) in urlList"
              :key="index"
              :label="`${item.uri},${item.method}`"
              :value="`${item.uri},${item.method}`"
            >
              <span style="float: left">{{ item.uri }}</span>
              <span style="float: right; color: var(--el-text-color-secondary); font-size: 13px">{{ item.method }}
              </span>
            </el-option>
          </el-select>
          <!-- <el-input v-model="form.auUri" placeholder="请输入url" /> -->
        </el-form-item>
        <el-form-item label="请求方式" prop="auMethod">
          <el-input v-model="form.auMethod" disabled placeholder="自动带入" />
        </el-form-item>
        <el-form-item label="是否开启" prop="auEnabled">
          <el-radio-group v-model="form.auEnabled" clearable  placeholder="请选择">
            <el-radio v-for="dict in system_yes_no" :key="dict.value" :value="dict.value">{{ dict.label }}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="功能标识" prop="funcSign">
          <el-input v-model="form.funcSign" placeholder="请输入功能标识" />
        </el-form-item>
        <el-form-item label="url名称" prop="auName">
          <el-input v-model="form.auName" placeholder="请输入url名称" />
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

<script setup name="AuditTrackUri" lang="ts">
import {
  listAuditTrackUri, 
  getAuditTrackUri, 
  delAuditTrackUri, 
  addAuditTrackUri,
  updateAuditTrackUri,
  listModulUri
} from '@/api/system/audittrail/auditTrackUri';

const { proxy } = getCurrentInstance() as ComponentInternalInstance;
const { system_yes_no, system_service_type } = toRefs<any>(proxy?.useDict('system_yes_no', 'system_service_type'));

const auditTrackList = ref([]);
const buttonLoading = ref(false);
const loading = ref(true);
const showSearch = ref(true);
const ids = ref<Array<string | number>>([]);
const names = ref<Array<string | number>>([]);
const single = ref(true);
const selectRow = ref({});
const multiple = ref(true);
const total = ref(0);
const userCheckValue = ref({auditUriId:''});
const urlList = ref([]);
const urlLoading = ref(false);

const queryFormRef = ref<ElFormInstance>();
const tenantFormRef = ref<ElFormInstance>();

const dialog = reactive<DialogOption>({
  visible: false,
  title: '',
  loading: false
});

const initFormData = {
  auditUriId: '',
  auUri: '',
  auMethod: '',
  auName: '',
  auEnabled: '1',
  funcSign: '',
  sysModule: ''
};
const data = reactive({
  form: { ...initFormData },
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    auditUriId: '',
    auUri: '',
    auMethod: '',
    sysModule: 'system',
    auName: '',
    auEnabled: '',
    funcSign: ''
  },
  rules: {
    auUri: [{ required: true, message: 'url不能为空', trigger: 'blur' }],
    auMethod: [{ required: true, message: '请求方式不能为空', trigger: 'blur' }],
    auName: [{ required: true, message: 'url名称不能为空', trigger: 'blur' }],
    sysModule: [{ required: true, message: '请选择所属服务', trigger: 'blur' }],
  }
});

const { queryParams, form, rules } = toRefs(data);

/** 查询审计追踪URL列表 */
const getList = async () => {
  loading.value = true;
  const res = await listAuditTrackUri(queryParams.value);
  auditTrackList.value = res.rows;
  total.value = res.total;
  loading.value = false;
};

// 取消按钮
const cancel = () => {
  if(form.value.sysModule!=queryParams.value.sysModule){
    // 重新查询列表数据
    getModuleUrlList(queryParams.value.sysModule);
  }
  reset();
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
  queryParams.value.auUri = queryParams.value.auUri.split(',')[0];
  getList();
};

/** 重置按钮操作 */
const resetQuery = () => {
  const sysModule = queryParams.value.sysModule;
  queryFormRef.value?.resetFields();
  queryParams.value.sysModule = sysModule;
  handleQuery();
};

// 多选框选中数据
const handleSelectionChange = (selection:any) => {
  ids.value = selection.map((item:any) => item.auditUriId);
  names.value = selection.map((item:any) => item.auName);
  single.value = selection.length != 1;
  multiple.value = !selection.length;
  selectRow.value = selection[0]||{};
};

/** 新增按钮操作 */
const handleAdd = () => {
  reset();
  form.value.sysModule = queryParams.value.sysModule
  dialog.visible = true;
  dialog.title = '添加审计追踪URL';
};

/** 修改按钮操作 */
const handleUpdate = async (row) => {
  reset();
  dialog.visible = true;
  dialog.loading = true;
  dialog.title = '修改审计追踪URL';
  const _id = row?.auditUriId || ids.value[0];
  const res = await getAuditTrackUri(_id);
  dialog.loading = false;
  Object.assign(form.value, {...res.data});
};

/** 提交按钮 */
const submitForm = () => {
  tenantFormRef.value?.validate(async (valid: boolean) => {
    if (valid) {
      buttonLoading.value = true;
      const data = JSON.parse(JSON.stringify(form.value));
      if (data.auditUriId) {
        await updateAuditTrackUri(data).finally(() => (buttonLoading.value = false));
      } else {
        await addAuditTrackUri(data).finally(() => (buttonLoading.value = false));
      }
      proxy?.$modal.msgSuccess('操作成功');
      dialog.visible = false;
      if(form.value.sysModule!=queryParams.value.sysModule){
        // 重新查询列表数据
        getModuleUrlList(queryParams.value.sysModule);
      }
      await getList();
    }
  });
};

/** 删除按钮操作 */
const handleDelete = async (row) => {
  const _ids = row?.auditUriId || ids.value;
  const _names = row?.auName || names.value;
  await proxy?.$modal.confirm('是否确认删除审计追踪URL名称为"' + _names + '"的数据项？');
  loading.value = true;
  await delAuditTrackUri(_ids).finally(() => (loading.value = false));
  await getList();
  proxy?.$modal.msgSuccess('删除成功');
};

/** 导出按钮操作 */
const handleExport = () => {
  proxy?.download(
    'system/audit-uri/export',
    {
      ...queryParams.value
    },
    `审计追踪URL_${new Date().getTime()}.xlsx`
  );
};

const queryModuleChange = (val) => {
  handleQuery();
  getModuleUrlList(val);
};

const sysModuleChange = (val) => {
  form.value.auUri = '';
  form.value.auMethod = '';
  getModuleUrlList(val);
};

const urlChange = (val) => {
  form.value.auUri = val.split(',')[0];
  form.value.auMethod = val.split(',')[1];
};

// 请求url列表集合
const getModuleUrlList = (val) => {
  urlLoading.value = true;
  listModulUri(val).then((res) => {
    urlList.value = res.data
  }).finally(()=>{
    urlLoading.value = false;
  })
}

onMounted(() => {
  getList();
  getModuleUrlList(queryParams.value.sysModule);
});
</script>

<style lang="scss" scoped>
:deep(.topCard){
  .el-card__body{
    padding: 0 !important;
  }
}
</style>
