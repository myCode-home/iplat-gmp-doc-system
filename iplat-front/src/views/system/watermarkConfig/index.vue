<template>
  <div class="p-2">
    <el-row :gutter="10">
      <el-col :xs="24" :sm="12">
        <el-card class="box-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <span>水印号配置列表</span>
              <el-button v-hasPermi="['system:watermark:add']" @click="handleAdd" class="button" type="primary" size="small" icon="Plus" style="float: right;">新增</el-button>
            </div>
          </template>
          <!-- <el-row :gutter="10" class="mb8">
            <el-col :span="1.5">
              <el-button v-hasPermi="['system:watermark:add']" type="primary" plain icon="Plus" @click="handleAdd">新增</el-button>
            </el-col>
            <el-col :span="1.5">
              <el-button v-hasPermi="['system:watermark:edit']" type="success" plain icon="Edit" :disabled="single" @click="handleUpdate(selectRow)"
                >修改</el-button
              >
            </el-col>
            <el-col :span="1.5">
              <el-button v-hasPermi="['system:watermark:remove']" type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete">
                删除
              </el-button>
            </el-col>
            <el-col :span="1.5" v-if="false">
              <el-button v-hasPermi="['system:watermark:export']" type="warning" plain icon="Download" @click="handleExport">导出</el-button>
            </el-col>
            <right-toolbar v-model:show-search="showSearch" @query-table="getList"></right-toolbar>
          </el-row> -->
          <transition :enter-active-class="proxy?.animate.searchAnimate.enter" :leave-active-class="proxy?.animate.searchAnimate.leave">
            <div v-show="showSearch">
              <el-form ref="queryFormRef" :model="queryParams" size="small" :inline="true" label-width="auto" @submit.native.prevent="">
                <el-form-item label="序列编号" prop="seqCode">
                  <el-input v-model="queryParams.seqCode" placeholder="请输入序列编号" clearable @change="handleQuery" @keyup.enter="handleQuery" />
                </el-form-item>
                <el-form-item class="noMarginRight">
                  <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
                  <el-button icon="Refresh" @click="resetQuery">重置</el-button>
                </el-form-item>
              </el-form>
            </div>
          </transition>
          <el-table ref="extendedTableRef" v-loading="loading" border :data="extendedList" size="small" highlight-current-row @current-change="handleCurrentChange">
            <el-table-column type="expand">
              <template #default="props">
                <el-form label-position="left" inline size="small" label-width="auto" label-suffix="：" class="demo-table-expand p-2">
                  <el-form-item label="打印类型">
                    <span><dict-tag :options="dms_print_type" :value="props.row.printType" /></span>
                  </el-form-item>
                  <el-form-item label="状态">
                    <span><span><dict-tag :options="dms_state" :value="props.row.status" /></span></span>
                  </el-form-item>
                  <el-form-item label="创建时间">
                    <span>{{ props.row.createTime }}</span>
                  </el-form-item>
                </el-form>
              </template>
            </el-table-column>
            <el-table-column label="编号" align="center" prop="id" v-if="false" />
            <el-table-column label="使用模块" min-width="120px" align="center" prop="useModules" show-overflow-tooltip>
              <template #default="{row}">
                <dict-tag :options="system_watermark_use_modules" :value="row.useModules" />
              </template>
            </el-table-column>
            <el-table-column label="序列编号" align="center" prop="seqCode" show-overflow-tooltip />
            <el-table-column label="备注" align="center" prop="remark" show-overflow-tooltip />
            <el-table-column width="110" label="操作" align="center" class-name="small-padding fixed-width">
              <template #default="scope">
                <el-tooltip content="预览" placement="top">
                  <el-button v-hasPermi="['system:watermark:edit']" link type="primary" icon="View" @click="submitView(scope.row)"></el-button>
                </el-tooltip>
                <el-tooltip content="修改" placement="top">
                  <el-button v-hasPermi="['system:watermark:edit']" link type="primary" icon="Edit" @click="handleUpdate(scope.row)"></el-button>
                </el-tooltip>
                <el-tooltip content="删除" placement="top">
                  <el-button v-hasPermi="['system:watermark:remove']" link type="danger" icon="Delete" @click="handleDelete(scope.row)"></el-button>
                </el-tooltip>
              </template>
            </el-table-column>
          </el-table>
          <pagination v-show="total > 0" v-model:page="queryParams.pageNum" size="small" v-model:limit="queryParams.pageSize" :total="total" @pagination="getList" />
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12">
        <Attribute ref="attributeRef" :extendedId="selectRow.id" :extendedName="'水印号配置'"></Attribute>
      </el-col>
    </el-row>

    <!-- 添加或修改扩展管理对话框 -->
    <el-dialog v-model="dialog.visible" :title="dialog.title" width="80%" append-to-body>
      <el-form ref="extendedFormRef" :model="form" v-loading="dialog.loading" :rules="rules" label-width="auto">
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="使用模块" prop="useModules">
              <el-select v-model="form.useModules"  placeholder="请选择使用模块">
                <el-option v-for="dict in system_watermark_use_modules" :key="dict.value" :label="dict.label" :value="dict.value"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="文件类型" prop="docType">
              <FileTypeSelect v-model="form.docType" placeholder="请选择文件类型"></FileTypeSelect>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="打印类型" prop="printType">
              <el-select v-model="form.printType"  placeholder="请选择打印类型">
                <el-option v-for="dict in dms_print_type" :key="dict.value" :label="dict.label" :value="dict.value"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="文件级别" prop="docLevel">
              <el-select v-model="form.docLevel"  placeholder="请选择文件级别">
                <el-option v-for="dict in dms_file_level" :key="dict.value" :label="dict.label" :value="dict.value"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="所属部门" prop="docDeptId">
              <DepartmentSelect v-model="form.docDeptId" :filterable="true" :clearable="true" placeholder="请选择所属部门"></DepartmentSelect>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="下发部门" prop="issueDeptId">
              <DepartmentSelect v-model="form.issueDeptId" :multiple="true" :filterable="true" :clearable="true" placeholder="请选择下发部门"></DepartmentSelect>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="序列编号" prop="seqCode">
              <el-select v-model="form.seqCode"  placeholder="请选择序列编号">
                <el-option v-for="dict in ruleList" :key="dict.serialRuleCode" :label="dict.serialRuleCode" :value="dict.serialRuleCode"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="备注" prop="remark">
              <el-input v-model="form.remark" placeholder="请输入备注" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="状态" prop="status">
              <el-select v-model="form.status"  placeholder="请选择状态">
                <el-option v-for="dict in dms_state" :key="dict.value" :label="dict.label" :value="dict.value"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="是否默认" prop="ifDefault">
              <el-select v-model="form.ifDefault"  placeholder="请选择是否默认">
                <el-option v-for="dict in system_yes_no" :key="dict.value" :label="dict.label" :value="dict.value"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button :loading="buttonLoading" type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>
    <el-dialog v-model="pdfDialog.visible" :title="pdfDialog.title" width="90%" append-to-body>
      <PdfView v-if="pdfDialog.visible" text="" :pdfUrl="pdfUrl"></PdfView>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="pdfDialog.visible = false">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="watermarkConfig" lang="ts">
import {
  listWatermark, 
  getWatermark, 
  delWatermark, 
  addWatermark, 
  updateWatermark,
  viewWatermark
} from '@/api/system/watermarkConfig/index';
import { listSerialRule } from '@/api/system/serialRule';
import Attribute from '@/views/system/watermarkConfig/attribute.vue';
import FileTypeSelect from '@/components/SuperForm/component/fileTypeSelect.vue'
import DepartmentSelect from '@/components/SuperForm/component/departmentSelect.vue'

const { proxy } = getCurrentInstance() as ComponentInternalInstance;
const { system_watermark_use_modules, dms_print_type, dms_file_level, dms_state, system_yes_no } = toRefs<any>(proxy?.useDict('system_watermark_use_modules', 'dms_print_type', 'dms_file_level', 'dms_state', 'system_yes_no'));

const extendedList = ref([]);
const buttonLoading = ref(false);
const loading = ref(true);
const showSearch = ref(true);
const ids = ref<Array<string | number>>([]);
const names = ref<Array<string | number>>([]);
const single = ref(true);
const selectRow = ref({
  id: '',
  extendedName: ''
});
const multiple = ref(true);
const total = ref(0);

const queryFormRef = ref<ElFormInstance>();
const extendedFormRef = ref<ElFormInstance>();
const extendedTableRef = ref<ElTableInstance>();
const attributeRef = ref();

const dialog = reactive({
  visible: false,
  loading: false,
  title: ''
});

const initFormData = {
  id: '',
  useModules: '',
  docType: '',
  printType: '',
  docLevel: '',
  docDeptId: '',
  issueDeptId: [] as string[] | string,
  seqCode: '',
  remark: '',
  status: '',
  ifDefault: '0',
};
const data = reactive({
  form: { ...initFormData },
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    id: '',
    useModules: '',
    docType: '',
    printType: '',
    docLevel: '',
    docDeptId: '',
    issueDeptId: '',
    seqCode: '',
    remark: '',
    status: ''
  },
  rules: {
    seqCode: [{ required: true, message: '序列编号不能为空', trigger: 'blur' }],
    // extendedName: [{ required: true, message: '名称不能为空', trigger: 'blur' }],
    // extendedContent: [{ required: true, message: '内容不能为空', trigger: 'blur' }],
  }
});

const { queryParams, form, rules } = toRefs(data);


/** 查询扩展管理列表 */
const getList = async () => {
  loading.value = true;
  const res = await listWatermark(queryParams.value);
  extendedList.value = res.rows;
  total.value = res.total;
  loading.value = false;
  setCurrentRow();
};

const ruleList = ref([]);
/** 查询序列号列表 */
const getRuleList = async () => {
  const res = await listSerialRule({pageNum: 1,pageSize: 9999,});
  ruleList.value = res.rows;
};

// 设置默认选中
const setCurrentRow = () => {
  // 默认选中第一条
  let index = 0;
  if(selectRow.value.id){
    index = extendedList.value.findIndex(item=>item.id==selectRow.value.id)||0;
    index = index >=0 ? index : 0;
  }
  selectRow.value = extendedList.value[index]||{}
  extendedTableRef.value.setCurrentRow(extendedList.value[index]);
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
  extendedFormRef.value?.resetFields();
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
  ids.value = selection.map((item:any) => item.id);
  names.value = selection.map((item:any) => item.extendedName);
  single.value = selection.length != 1;
  multiple.value = !selection.length;
  selectRow.value = selection[0]||{};
};

// 选中数据
const handleCurrentChange = (val:any) => {
  selectRow.value = val||{};
};


/** 新增按钮操作 */
const handleAdd = () => {
  reset();
  dialog.visible = true;
  dialog.loading = false;
  dialog.title = '添加扩展管理';
};

/** 修改按钮操作 */
const handleUpdate = async (row) => {
  reset();
  const _id = row?.id || ids.value[0];
  dialog.visible = true;
  dialog.loading = true;
  dialog.title = '修改扩展管理';
  const res = await getWatermark(_id);
  Object.assign(form.value, res.data);
  dialog.loading = false;
};

/** 提交按钮 */
const submitForm = () => {
  extendedFormRef.value?.validate(async (valid: boolean) => {
    if (valid) {
      buttonLoading.value = true;
      // 创建临时对象用于提交，避免修改原表单数据类型
      const submitData = { ...form.value };
      if(Array.isArray(submitData.issueDeptId)&&submitData.issueDeptId.length>0){
        submitData.issueDeptId = submitData.issueDeptId.join(',');
      }else{
        submitData.issueDeptId = ''
      }
      if (submitData.id) {
        await updateWatermark(submitData).finally(() => (buttonLoading.value = false));
      } else {
        await addWatermark(submitData).finally(() => (buttonLoading.value = false));
      }
      proxy?.$modal.msgSuccess('操作成功');
      dialog.visible = false;
      dialog.loading = false;
      await getList();
    }
  });
};

const pdfDialog = reactive({
  visible: false,
  loading: false,
  title: '水印预览'
});
const pdfUrl = ref('');
const submitView = async (row?: any) =>{
  await viewWatermark(row.id).then(async (res: any) => {
    const pdfBlob = new Blob([res], { type: 'application/pdf' })
    pdfUrl.value = URL.createObjectURL(pdfBlob)
    pdfDialog.visible = true;
  });
};

/** 删除按钮操作 */
const handleDelete = async (row) => {
  const _ids = row?.id || ids.value;
  const _names = row?.extendedName || names.value;
  await proxy?.$modal.confirm('是否确认删除扩展属性名称为"' + _names + '"的数据项？');
  loading.value = true;
  await delWatermark(_ids).finally(() => (loading.value = false));
  await getList();
  proxy?.$modal.msgSuccess('删除成功');
};

/** 导出按钮操作 */
const handleExport = () => {
  proxy?.download(
    'system/extended/export',
    {
      ...queryParams.value
    },
    `extended_${new Date().getTime()}.xlsx`
  );
};

onMounted(() => {
  getList();
  getRuleList();
});
</script>

<style scoped>
.demo-table-expand {
  font-size: 0;
}
.demo-table-expand label {
  width: 90px;
  color: #99a9bf;
}
.demo-table-expand .el-form-item {
  margin-right: 0;
  margin-bottom: 0;
  width: 50%;
}
</style>
