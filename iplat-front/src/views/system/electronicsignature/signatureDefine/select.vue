<template>
  <div class="p-2">
    <transition :enter-active-class="proxy?.animate.searchAnimate.enter" :leave-active-class="proxy?.animate.searchAnimate.leave">
      <div class="mb-[10px]">
        <el-card shadow="hover" :class="{ topCard: !showSearch }">
          <!-- <template #header>
            所属模块：
            <el-select v-model="queryParams.sysModule"  @change="queryModuleChange" style="width: 250px;" placeholder="请选择">
              <el-option v-for="dict in system_service_type" :key="dict.value" :label="dict.label" :value="dict.value"></el-option>
            </el-select>
          </template> -->
          <div v-if="showSearch">
            <el-form ref="queryFormRef" :model="queryParams" :inline="true" label-width="auto" >
              <el-form-item label="所属服务" prop="sysModule">
                <el-select v-model="queryParams.sysModule"  @change="queryModuleChange" style="width: 250px;" placeholder="请选择">
                  <el-option v-for="dict in system_service_type" :key="dict.value" :label="dict.label" :value="dict.value"></el-option>
                </el-select>
              </el-form-item>
              <el-form-item label="名称" prop="sdName">
                <el-input v-model="queryParams.sdName" placeholder="请输入名称" clearable @change="handleQuery" @keyup.enter="handleQuery" />
              </el-form-item>
              <el-form-item label="是否启用" prop="sdEnabled">
                <el-select v-model="queryParams.sdEnabled" clearable  @change="handleQuery"  placeholder="请选择是否启用">
                  <el-option v-for="dict in system_yes_no" :key="dict.value" :label="dict.label" :value="dict.value"></el-option>
                </el-select>
              </el-form-item>
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
          <!-- <el-col :span="1.5">
            <el-button v-hasPermi="['system:signatureDefinition:add']" type="primary" icon="Plus" @click="handleAdd">新增</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button v-hasPermi="['system:signatureDefinition:edit']" type="primary" icon="Edit" :disabled="single" @click="handleUpdate(selectRow)"
              >修改</el-button
            >
          </el-col>
          <el-col :span="1.5">
            <el-button v-hasPermi="['system:signatureDefinition:remove']" type="primary" icon="Delete" :disabled="multiple" @click="handleDelete">
              删除
            </el-button>
          </el-col>
          <el-col :span="1.5" v-if="true">
            <el-button v-hasPermi="['system:signatureDefinition:export']" type="primary" icon="Download" @click="handleExport">
              导出
            </el-button>
          </el-col> -->
          <right-toolbar v-model:show-search="showSearch" @query-table="getList"></right-toolbar>
        </el-row>
      </template>

      <el-table ref="tableRef" :class="{nomultiple: !multipleChoice}" row-key="signatureDefinitionId" v-loading="loading" border :data="uriManagerList" @select="selectChange" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column label="编号" align="center" v-if="false" min-width="120" prop="signatureDefinitionId" />
        <el-table-column label="序号" type="index" width="55" align="center" prop="index" />
        <el-table-column label="名称" align="center" min-width="150" prop="sdName" show-overflow-tooltip />
        <el-table-column label="所属服务" min-width="100" align="center" prop="sysModule">
          <template #default="{row}">
            <dict-tag :options="system_service_type" :value="row.sysModule" />
          </template>
        </el-table-column>
        <el-table-column label="是否启用" min-width="80" align="center" prop="sdEnabled">
          <template #default="{row}">
            <dict-tag :options="system_yes_no" :value="row.sdEnabled" />
          </template>
        </el-table-column>
        <el-table-column width="120" label="操作" v-if="false" align="center" class-name="small-padding fixed-width">
          <template #default="scope">
            <el-tooltip content="修改" placement="top">
              <el-button v-hasPermi="['system:signatureDefinition:edit']" link type="primary" icon="Edit" @click="handleUpdate(scope.row)"></el-button>
            </el-tooltip>
            <el-tooltip content="删除" placement="top">
              <el-button v-hasPermi="['system:signatureDefinition:remove']" link type="primary" icon="Delete" @click="handleDelete(scope.row)"></el-button>
            </el-tooltip>
          </template>
        </el-table-column>
      </el-table>
      <pagination v-show="total > 0" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :total="total" @pagination="getList" />
    </el-card>
    <!-- 添加或修改电子签名定义对话框 -->
    <el-dialog v-model="dialog.visible" :title="dialog.title" :before-close="cancel" width="70%" append-to-body>
      <div class="signDiv">
        <el-form ref="tenantFormRef" v-loading="dialog.loading" :model="form" :rules="rules" label-width="auto">
          <el-form-item label="名称" prop="sdName">
            <el-input v-model="form.sdName" placeholder="请输入名称" />
          </el-form-item>
          <el-form-item label="是否启用" prop="sdEnabled">
            <el-radio-group v-model="form.sdEnabled" clearable  placeholder="请选择">
              <el-radio v-for="dict in system_yes_no" :key="dict.value" :value="dict.value">{{ dict.label }}</el-radio>
            </el-radio-group>
          </el-form-item>
          <div class="paneDiv">
            <div class="paneHeader">
              签名人员
            </div>
            <div class="paneContent" v-for="(item, index) in form.definitionUsers">
              <el-divider v-if="index > 0" style="margin: -13px 0 15px 0;"></el-divider>
              <el-row :gutter="20">
                <el-col :span="21">
                  <el-form-item
                    label="含义"
                    :key="index"
                    :prop="'definitionUsers.' + index + '.sduImplication'"
                    :rules="{
                      required: true, message: '含义不能为空', trigger: 'blur'
                    }"
                  >
                    <el-input v-model="item.sduImplication"  placeholder="请输入含义" />
                  </el-form-item>
                  <el-form-item label="含义是否可改" :key="index">
                    <el-radio-group v-model="item.sduImplFlag">
                      <el-radio value="1">是</el-radio>
                      <el-radio value="0">否</el-radio>
                    </el-radio-group>
                  </el-form-item>
                  <el-form-item label="理由是否必填" :key="index">
                    <el-radio-group v-model="item.sduReasonFlag">
                      <el-radio value="1">是</el-radio>
                      <el-radio value="0">否</el-radio>
                    </el-radio-group>
                  </el-form-item>
                  <el-form-item label="人员类别" :key="index">
                    <el-radio-group v-model="item.sduType">
                      <el-radio value="LOGUSER">登录人</el-radio>
                      <el-radio value="ALL">所有人</el-radio>
                      <el-radio :value="category[index]">其它</el-radio>
                    </el-radio-group>
                  </el-form-item>
                  <template v-if="item.sduType !== 'LOGUSER' && item.sduType !== 'ALL'">
                    <el-form-item label="签名组" 
                      :key="index"
                      :prop="'definitionUsers.' + index + '.remark'"
                      :rules="{
                        required: true, message: '请选择签名组', trigger: 'blur'
                      }">
                      <el-input v-model="item.remark" @click.native="handSelect(index)" readonly placeholder="点击选择签名组"></el-input>
                    </el-form-item>
                    <el-form-item label="输入方式" :key="index">
                      <el-radio-group v-model="item.sduInputType">
                        <el-radio value="IMPUT">手动</el-radio>
                        <el-radio value="SELECT">选择</el-radio>
                      </el-radio-group>
                    </el-form-item>
                  </template>
                </el-col>
                <el-col :span="3" style="display: flex;align-items: center;">
                  <el-button type="text" icon="Plus" @click.prevent="createAttr"></el-button>
                  <el-button type="text" icon="Delete" class="el-button--danger" @click.prevent="removeAttr(item)"></el-button>
                </el-col>
              </el-row>
            </div>
          </div>
        </el-form>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button :loading="buttonLoading" type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>
    <SignatureGroup
      v-if="selectData.open"
      :title="selectData.title"
      :selectType="selectData.type"
      :multiple="selectData.multiple"
      :width="selectData.width"
      :otherCondition="selectData.otherCondition"
      :achieveData="selectDataAchieve"
      @dialogClose="selectDataCancel"
  ></SignatureGroup>
  </div>
</template>

<script setup name="SignatureDefineSelect" lang="ts">
import {
  listSignatureDefine, 
  getSignatureDefine, 
  delSignatureDefine, 
  addSignatureDefine,
  updateSignatureDefine
} from '@/api/system/electronicsignature/signatureDefine';
import SignatureGroup from '@/views/system/electronicsignature/signatureDefine/signatureGroup.vue';

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
  }
});

const { proxy } = getCurrentInstance() as ComponentInternalInstance;
const { system_yes_no, system_service_type } = toRefs<any>(proxy?.useDict('system_yes_no', 'system_service_type'));

const uriManagerList = ref([]);
const buttonLoading = ref(false);
const loading = ref(true);
const showSearch = ref(true);
const ids = ref<Array<string | number>>([]);
const names = ref<Array<string | number>>([]);
const single = ref(true);
const selectRow = ref({});
const selectDatas = ref([]);
const tableRef = ref<ElTableInstance>();
const multiple = ref(true);
const total = ref(0);

const queryFormRef = ref<ElFormInstance>();
const tenantFormRef = ref<ElFormInstance>();

const dialog = reactive<DialogOption>({
  visible: false,
  title: '',
  loading: false
});

const initFormData = {
  signatureDefinitionId: '',
  suUri: '',
  suMethod: '',
  sdName: '',
  sdEnabled: '1',
  sysModule: '',
  definitionUsers: [{
    sduImplication:'',
    sduImplFlag: '0',
    sduReasonFlagShow: '1',
    sduReasonFlag: '0',
    sduType: 'LOGUSER',
    sduInputType: 'IMPUT',
    sduValue: '',
    remark: ''
  }]
};
const data = reactive({
  form: { ...initFormData },
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    signatureDefinitionId: '',
    suUri: '',
    suMethod: '',
    sysModule: '',
    sdName: '',
    sdEnabled: '1'
  },
  rules: {
    sdName: [{ required: true, message: '名称不能为空', trigger: 'blur' }],
    sysModule: [{ required: true, message: '请选择所属服务', trigger: 'blur' }],
  }
});
const category = ref(['ROLE'])
const { queryParams, form, rules } = toRefs(data);

watch(() => props.otherCondition, () => {
  Object.keys(props.otherCondition).forEach(key => {
    queryParams.value[key] = props.otherCondition[key];
  });
}, { immediate: true, deep: true });

const selectData = ref({
  open: false,
  type: '',
  width: '',
  title: '',
  multiple: true,
  otherCondition: {},
})
const num = ref(0);

/** 查询电子签名定义列表 */
const getList = async () => {
  loading.value = true;
  const res = await listSignatureDefine(queryParams.value);
  uriManagerList.value = res.rows;
  total.value = res.total;
  loading.value = false;
};

// 选择签名组操作
const handSelect = (index) =>{
  num.value = index;
  selectData.value.open = true;
  selectData.value.title = '选择签名组';
  selectData.value.type = '';
  selectData.value.otherCondition = {
    type: category.value[index]
  }
}

// 选中数据
const selectDataAchieve = (data, type) => {
  form.value.definitionUsers[num.value].sduValue = data.map((item:any) => item.id).join(',');
  form.value.definitionUsers[num.value].remark = data.map((item:any) => item.name).join(',');
  form.value.definitionUsers[num.value].sduType = type;
  category.value[num.value] = type;
  selectDataCancel();
};
// 关闭选择
const selectDataCancel = () => {
  selectData.value = {
    open: false,
    title: undefined,
    width: '',
    multiple: true,
    type: undefined,
    otherCondition: {},
  };
};

// 新增属性配置
const createAttr = () => {
  form.value.definitionUsers.push({
    // 签名含义
    sduImplication:'',
    // 理由是否必填
    sduImplFlag: '0',
    sduReasonFlagShow: '1',
    // 理由是否必填
    sduReasonFlag: '0',
    // 人员类别LOGUSER,USER,ROLE,USERGROUP,ALL
    sduType: 'LOGUSER',
    // 输入方式
    sduInputType: 'IMPUT',
    sduValue: '',
    remark: ''
  });
  category.value.push('ROLE');
};

// 移除属性配置
const removeAttr = (row) => {
  let index = form.value.definitionUsers.indexOf(row);
  if (index !== -1) {
      form.value.definitionUsers.splice(index, 1);
      category.value.splice(index, 1);
  }
  // 保持显示一条属性配置
  if (form.value.definitionUsers.length === 0) {
      createAttr();
  }
};

// 取消按钮
const cancel = () => {
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
  queryParams.value.suUri = queryParams.value.suUri.split(',')[0];
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
  ids.value = selection.map((item:any) => item.signatureDefinitionId);
  names.value = selection.map((item:any) => item.sdName);
  single.value = selection.length != 1;
  multiple.value = !selection.length;
  selectRow.value = selection[0]||{};
  selectDatas.value = selection;
};
// 选中数据
const selectChange = (selection) =>{
  if(!props.multipleChoice){
    if(selection.length > 1){
      const del_row = selection.shift()
      tableRef.value.toggleRowSelection(del_row, false)
    }
  }
}

/** 新增按钮操作 */
const handleAdd = () => {
  reset();
  form.value.sysModule = queryParams.value.sysModule
  dialog.visible = true;
  dialog.title = '添加电子签名定义';
};

/** 修改按钮操作 */
const handleUpdate = async (row) => {
  reset();
  dialog.visible = true;
  dialog.loading = true;
  dialog.title = '修改电子签名定义';
  const _id = row?.signatureDefinitionId || ids.value[0];
  const res = await getSignatureDefine(_id);
  dialog.loading = false;
  Object.assign(form.value, {...res.data});
  form.value.definitionUsers.forEach((item,index)=>{
    if(item.sduType !== 'LOGUSER' && item.sduType !== 'ALL'){
      category.value[index] = item.sduType;
    }else {
      category.value[index] = 'ROLE';
    }
  })
};

/** 提交按钮 */
const submitForm = () => {
  tenantFormRef.value?.validate(async (valid: boolean) => {
    if (valid) {
      buttonLoading.value = true;
      const data = JSON.parse(JSON.stringify(form.value));
      // 重新就行排序，保证顺序
      data.definitionUsers.map((item,index)=>{
        item.sduSort = index
        return item
      })
      if (data.signatureDefinitionId) {
        await updateSignatureDefine(data).finally(() => (buttonLoading.value = false));
      } else {
        await addSignatureDefine(data).finally(() => (buttonLoading.value = false));
      }
      proxy?.$modal.msgSuccess('操作成功');
      dialog.visible = false;
      await getList();
    }
  });
};

/** 删除按钮操作 */
const handleDelete = async (row) => {
  const _ids = row?.signatureDefinitionId || ids.value;
  const _names = row?.sdName || names.value;
  await proxy?.$modal.confirm('是否确认删除名称为"' + _names + '"的数据项？');
  loading.value = true;
  await delSignatureDefine(_ids).finally(() => (loading.value = false));
  await getList();
  proxy?.$modal.msgSuccess('删除成功');
};

/** 导出按钮操作 */
const handleExport = () => {
  proxy?.download(
    'system/signature-definition/export',
    {
      ...queryParams.value
    },
    `电子签名定义_${new Date().getTime()}.xlsx`
  );
};

const queryModuleChange = (val) => {
  handleQuery();
};

/** 提交按钮操作 */
const confirmChoiceData = async () => {
  if(selectDatas.value.length == 0){
    return proxy?.$modal.msgError('请选择');
  }
  const data = selectDatas.value.map((item) => {
    item.id = item.signatureDefinitionId
    item.name = item.sdName
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
});
defineExpose({
  confirmChoiceData
})
</script>

<style lang="scss" scoped>
:deep(.topCard){
  .el-card__body{
    padding: 0 !important;
  }
}
//找到表头，然后隐藏掉复选框
:deep(.nomultiple){
  .el-table__header-wrapper {
    .el-checkbox{
      display:none
    }
  }
} 
.signDiv{
  width: 100%;
  min-width: 200px;
  display: flex;
  justify-content: center;
  .paneDiv{
    width: 600px;
    border: 1px solid rgb(235, 238, 245);
    border-radius: 5px;
    box-shadow: rgba(0, 0, 0, 0.1) 0px 2px 4px 0px;
    .paneHeader{
      background-color: #4495e3;
      border-top-left-radius: 5px;
      border-top-right-radius: 5px;
      padding: 5px 15px;
      color: #fff;
      position: relative;
      font-size: 12px;
    }
    .paneContent{
      padding: 15px 15px 5px;
    }
  }
}
</style>
