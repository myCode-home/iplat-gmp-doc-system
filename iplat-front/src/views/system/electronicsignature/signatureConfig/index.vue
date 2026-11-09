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
              <el-form-item label="电子签名名称" prop="sdName">
                <el-input v-model="queryParams.sdName" placeholder="请输入电子签名名称" clearable @change="handleQuery" @keyup.enter="handleQuery" />
              </el-form-item>
              <el-form-item label="URL" prop="suUri">
                <el-input v-model="queryParams.suUri" placeholder="请输入URL" clearable @change="handleQuery" @keyup.enter="handleQuery" />
                <!-- <el-select v-model="queryParams.suUri" filterable reserve-keyword clearable @change="handleQuery" placeholder="请选择url">
                  <el-option
                    v-loading="urlLoading"
                    v-for="(item,index) in urlList"
                    :key="index"
                    :label="`${item.uri},${item.method}`"
                    :value="`${item.uri},${item.method}`"
                  >
                    <span style="float: left">{{ item.uri }}</span>
                    <span style="float: right; color: var(--el-text-color-secondary); font-size: 13px">{{ item.method }}
                    </span>
                  </el-option>
                </el-select> -->
              </el-form-item>
              <el-form-item label="URL名称" prop="suName">
                <el-input v-model="queryParams.suName" placeholder="请输入URL名称" clearable @change="handleQuery" @keyup.enter="handleQuery" />
              </el-form-item>
              <!-- <el-form-item label="是否启用" prop="sdEnabled">
                <el-select v-model="queryParams.sdEnabled" clearable  @change="handleQuery"  placeholder="请选择是否启用">
                  <el-option v-for="dict in system_yes_no" :key="dict.value" :label="dict.label" :value="dict.value"></el-option>
                </el-select>
              </el-form-item> -->
              <el-form-item class="noMarginRight">
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
            <el-button v-hasPermi="['system:signatureConfig:add']" type="primary" icon="Plus" @click="handleAdd">新增</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button v-hasPermi="['system:signatureConfig:edit']" type="primary" icon="Edit" :disabled="single" @click="handleUpdate(selectRow)"
              >修改</el-button
            >
          </el-col>
          <el-col :span="1.5">
            <el-button v-hasPermi="['system:signatureConfig:remove']" type="primary" icon="Delete" :disabled="multiple" @click="handleDelete">
              删除
            </el-button>
          </el-col>
          <el-col :span="1.5" v-if="true">
            <el-button v-hasPermi="['system:signatureConfig:export']" type="primary" icon="Download" @click="handleExport">
              导出
            </el-button>
          </el-col>
          <right-toolbar v-model:show-search="showSearch" @query-table="getList"></right-toolbar>
        </el-row>
      </template>

      <el-table v-loading="loading" border :data="SignatureConfigList" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column label="编号" align="center" v-if="false" min-width="120" prop="signatureConfigId" />
        <el-table-column label="序号" type="index" width="55" align="center" prop="index" />
        <el-table-column label="电子签名名称" align="center" min-width="120" prop="sdName" show-overflow-tooltip />
        <el-table-column label="URL" align="center" min-width="130" prop="suUri" show-overflow-tooltip />
        <el-table-column label="URL名称" align="center" min-width="130" prop="suName" show-overflow-tooltip />
        <el-table-column label="是否启用" width="80" align="center" prop="sdEnabled">
          <template #default="{row}">
            <dict-tag :options="system_yes_no" :value="row.sdEnabled" />
          </template>
        </el-table-column>
        <el-table-column label="所属服务" width="150" align="center" prop="sysModule">
          <template #default="{row}">
            <dict-tag :options="system_service_type" :value="row.sysModule" />
          </template>
        </el-table-column>
        <!-- <el-table-column label="功能标识" align="center" width="120" prop="funcSign"/> -->
        <el-table-column width="120" label="操作" align="center" class-name="small-padding fixed-width">
          <template #default="scope">
            <el-tooltip content="修改" placement="top">
              <el-button v-hasPermi="['system:signatureConfig:edit']" link type="primary" icon="Edit" @click="handleUpdate(scope.row)"></el-button>
            </el-tooltip>
            <el-tooltip content="删除" placement="top">
              <el-button v-hasPermi="['system:signatureConfig:remove']" link type="primary" icon="Delete" @click="handleDelete(scope.row)"></el-button>
            </el-tooltip>
          </template>
        </el-table-column>
      </el-table>
      <pagination v-show="total > 0" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :total="total" @pagination="getList" />
    </el-card>
    <!-- 添加或修改电子签名配置对话框 -->
    <el-dialog v-model="dialog.visible" :title="dialog.title" :before-close="cancel" width="550px" append-to-body>
      <el-form ref="tenantFormRef" v-loading="dialog.loading" :model="form" :rules="rules" label-width="auto">
        <el-form-item label="选择url" prop="suName">
          <el-input v-model="form.suName" @click.native="handleSelectData('uriManagerSelect')" readonly placeholder="请选择url" />
          <!-- <el-select v-model="form.suUri" filterable reserve-keyword @change="urlChange" v-loading="urlLoading" placeholder="请选择url">
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
          </el-select> -->
        </el-form-item>
        <el-form-item label="URL" prop="suUri">
          <el-input v-model="form.suUri" disabled placeholder="自动带入" />
        </el-form-item>
        <el-form-item label="请求方式" prop="suMethod">
          <el-input v-model="form.suMethod" disabled placeholder="自动带入" />
        </el-form-item>
        <el-form-item label="电子签名名称" prop="sdName">
          <el-input v-model="form.sdName" @click.native="handleSelectData('SignatureDefineSelect')" readonly placeholder="请选择URL名称" />
        </el-form-item>
        <el-form-item label="是否启用" prop="sdEnabled">
          <el-radio-group v-model="form.sdEnabled" clearable  placeholder="请选择">
            <el-radio v-for="dict in system_yes_no" :key="dict.value" :value="dict.value">{{ dict.label }}</el-radio>
          </el-radio-group>
        </el-form-item>
        <!-- <el-form-item label="功能标识" prop="funcSign">
          <el-input v-model="form.funcSign" placeholder="请输入功能标识" />
        </el-form-item> -->
      </el-form>
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
      :width="selectData.width"
      :otherCondition="selectData.otherCondition"
      :achieveData="selectDataAchieve"
      @dialogClose="selectDataCancel"
    ></to-select>
  </div>
</template>

<script setup name="SignatureConfig" lang="ts">
import {
  listSignatureConfig, 
  getSignatureConfig, 
  delSignatureConfig, 
  addSignatureConfig,
  updateSignatureConfig
} from '@/api/system/electronicsignature/signatureConfig';
import { listModulUri } from '@/api/system/audittrail/auditTrackUri';

const { proxy } = getCurrentInstance() as ComponentInternalInstance;
const { system_yes_no, system_service_type } = toRefs<any>(proxy?.useDict('system_yes_no', 'system_service_type'));

const SignatureConfigList = ref([]);
const buttonLoading = ref(false);
const loading = ref(true);
const showSearch = ref(true);
const ids = ref<Array<string | number>>([]);
const names = ref<Array<string | number>>([]);
const single = ref(true);
const selectRow = ref({});
const multiple = ref(true);
const total = ref(0);
const userCheckValue = ref({signatureConfigId:''});
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
  signatureConfigId: '',
  signatureUriId: '',
  signatureDefinitionId: '',
  suUri: '',
  suMethod: '',
  sdName: '',
  suName: '',
  sdEnabled: '1',
  funcSign: '',
  sysModule: ''
};
const data = reactive({
  form: { ...initFormData },
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    sdName: '',
    signatureConfigId: '',
    suUri: '',
    suMethod: '',
    sysModule: 'system',
    suName: '',
    sdEnabled: '',
    funcSign: ''
  },
  rules: {
    suUri: [{ required: true, message: 'url不能为空', trigger: 'change' }],
    suMethod: [{ required: true, message: '请求方式不能为空', trigger: 'blur' }],
    suName: [{ required: true, message: 'URL名称不能为空', trigger: 'blur' }],
    sdName: [{ required: true, message: '电子签名名称不能为空', trigger: 'change' }],
    sysModule: [{ required: true, message: '请选择所属服务', trigger: 'blur' }],
  }
});

const { queryParams, form, rules } = toRefs(data);

/** 查询电子签名配置列表 */
const getList = async () => {
  loading.value = true;
  const res = await listSignatureConfig(queryParams.value);
  console.log('result',res)
  SignatureConfigList.value = res.rows;
  total.value = res.total;
  loading.value = false;
};

// 取消按钮
const cancel = () => {
  reset();
  dialog.visible = false;
};

// 表单重置
const reset = () => {
  form.value = { ...initFormData };
  form.value.sysModule = queryParams.value.sysModule;
  tenantFormRef.value?.resetFields();
};

/** 搜索按钮操作 */
const handleQuery = () => {
  queryParams.value.pageNum = 1;
  if(queryParams.value.suUri){
    queryParams.value.suUri = queryParams.value.suUri.split(',')[0];
  }else{
    queryParams.value.suUri = '';
  }
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
  ids.value = selection.map((item:any) => item.signatureConfigId);
  names.value = selection.map((item:any) => item.suName);
  single.value = selection.length != 1;
  multiple.value = !selection.length;
  selectRow.value = selection[0]||{};
};

/** 新增按钮操作 */
const handleAdd = () => {
  reset();
  dialog.visible = true;
  dialog.title = '添加电子签名配置';
};

/** 修改按钮操作 */
const handleUpdate = async (row) => {
  reset();
  dialog.visible = true;
  dialog.loading = true;
  dialog.title = '修改电子签名配置';
  const _id = row?.signatureConfigId || ids.value[0];
  const res = await getSignatureConfig(_id);
  dialog.loading = false;
  Object.assign(form.value, {...res.data});
};

/** 提交按钮 */
const submitForm = () => {
  tenantFormRef.value?.validate(async (valid: boolean) => {
    if (valid) {
      buttonLoading.value = true;
      const data = JSON.parse(JSON.stringify(form.value));
      if (data.signatureConfigId) {
        await updateSignatureConfig(data).finally(() => (buttonLoading.value = false));
      } else {
        await addSignatureConfig(data).finally(() => (buttonLoading.value = false));
      }
      proxy?.$modal.msgSuccess('操作成功');
      dialog.visible = false;
      await getList();
    }
  });
};

/** 删除按钮操作 */
const handleDelete = async (row) => {
  const _ids = row?.signatureConfigId || ids.value;
  const _names = row?.suName || names.value;
  await proxy?.$modal.confirm('是否确认删除URL名称为"' + _names + '"的数据项？');
  loading.value = true;
  await delSignatureConfig(_ids).finally(() => (loading.value = false));
  await getList();
  proxy?.$modal.msgSuccess('删除成功');
};

/** 导出按钮操作 */
const handleExport = () => {
  proxy?.download(
    'system/signature-config/export',
    {
      ...queryParams.value
    },
    `电子签名配置_${new Date().getTime()}.xlsx`
  );
};

const queryModuleChange = (val) => {
  handleQuery();
  // getModuleUrlList(val);
};

const urlChange = (val) => {
  form.value.suUri = val.split(',')[0];
  form.value.suMethod = val.split(',')[1];
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

const selectData = ref({
  open: false,
  type: '',
  width: '',
  title: '',
  menuType: '',
  multiple: false,
  otherCondition: {},
})

// 选择数据
const handleSelectData = (type) => {
  selectData.value = {
    open: true,
    type: type,
    width: '',
    title: '请选择',
    menuType: '',
    multiple: false,
    otherCondition: {},
  };
};
// 选中数据
const selectDataAchieve = (data) => {
  if(selectData.value.type == 'uriManagerSelect') {
    form.value.signatureUriId = data.id;
    form.value.suName = data.name;
    form.value.suUri = data.suUri;
    form.value.suMethod = data.suMethod;
  }else {
    form.value.signatureDefinitionId = data.id;
    form.value.sdName = data.name;
  }
  selectDataCancel();
};
// 关闭选择
const selectDataCancel = () => {
  selectData.value = {
    open: false,
    title: undefined,
    width: '',
    multiple: false,
    type: undefined,
    menuType: '',
    otherCondition: {},
  };
};

onMounted(() => {
  getList();
  // getModuleUrlList(queryParams.value.sysModule);
});
</script>

<style lang="scss" scoped>
:deep(.topCard){
  .el-card__body{
    padding: 0 !important;
  }
}
</style>
