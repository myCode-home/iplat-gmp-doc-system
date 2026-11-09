<template>
  <div class="p-2">
    <transition :enter-active-class="proxy?.animate.searchAnimate.enter" :leave-active-class="proxy?.animate.searchAnimate.leave">
      <div v-show="showSearch" class="mb-[10px]">
        <el-card shadow="hover">
          <super-search
            code="templateEdit-edit-search"
            v-model="queryParams"
            @getList="getList"
            @resetQuery="resetQuery"
          >
          </super-search>
        </el-card>
      </div>
    </transition>

    <el-card shadow="hover">
      <template #header>
        <el-row :gutter="10" class="mb8">
          <el-col :span="1.5">
            <el-button v-hasPermi="['dms:tmplPreparation:add']" type="primary" icon="Plus" @click="handleAdd">新增</el-button>
          </el-col>
          <!-- <el-col :span="1.5">
            <el-button v-hasPermi="['dms:tmplPreparation:edit']" type="success" plain icon="Edit" :disabled="single" @click="handleUpdate()">编辑</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button v-hasPermi="['dms:tmplPreparation:remove']" type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete()"
              >删除</el-button
            >
          </el-col>
          <el-col :span="1.5">
            <el-button v-hasPermi="['dms:templateEdit:export']" type="warning" plain icon="Download" @click="handleExport">导出</el-button>
          </el-col> -->
          <right-toolbar v-model:show-search="showSearch" @query-table="getList"></right-toolbar>
        </el-row>
      </template>
      <super-table 
        v-if="showTable"
        code="templateEdit-table"
        rowKey="id"
        highlightCurrentRow
        :dataLoading="loading"
        :data="templateEditList"
        v-model:queryParams="queryParams"
        :total="total"
        @selection-change="handleSelectionChange"
        @getList="getList"
      >
        <!-- 操作列 -->
        <template #operation="scope">
          <div>
            <el-tooltip content="编辑" placement="top" :enterable="false">
              <el-button link type="primary" v-hasPermi="['dms:tmplPreparation:edit']" icon="EditPen" @click="handleUpdate(scope.row)"></el-button>
            </el-tooltip>
            <el-tooltip content="删除" placement="top" :enterable="false">
              <el-button link type="danger" v-hasPermi="['dms:tmplPreparation:remove']" icon="Delete" @click="handleDelete(scope.row)"></el-button>
            </el-tooltip>
            <el-tooltip content="打开pageOffice" placement="top" :enterable="false">
              <el-button link type="primary" v-hasPermi="['dms:tmplPreparation:open']" icon="Document" @click="openPageOffice(scope.row)"></el-button>
            </el-tooltip>
            <el-tooltip content="差异对比" placement="top" :enterable="false">
              <el-button link type="primary" v-hasPermi="['dms:tmplPreparation:compare']" icon="Share" @click="handleCompare(scope.row)"></el-button>
            </el-tooltip>
            <el-tooltip content="查看审批履历" placement="top" :enterable="false">
              <el-button link type="primary" v-hasPermi="['dms:tmplPreparation:editRecord']" :disabled="scope.row.status === 'edit'" icon="InfoFilled" @click="handleApprovalRecord(scope.row)"></el-button>
            </el-tooltip>
            <el-tooltip content="提交" placement="top" :enterable="false">
              <el-button link type="primary" :loading="scope.row.subLoading" v-hasPermi="['dms:tmplPreparation:submit']" icon="SuccessFilled" @click="openApprove(scope.row)"></el-button>
            </el-tooltip>
            <!-- <el-popconfirm
              title="确定提交当前行数据吗？"
              @confirm="openApprove(scope.row)"
            >
              <template #reference>
                <span style="margin-left: 12px;">
                  <el-tooltip content="提交" placement="top" :enterable="false">
                    <el-button link type="primary" v-hasPermi="['dms:tmplPreparation:submit']" icon="SuccessFilled"></el-button>
                  </el-tooltip>
                </span>
              </template>
            </el-popconfirm> -->
          </div>
        </template>
      </super-table>
      <!-- <pagination v-show="total > 0" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :total="total" @pagination="getList" /> -->
    </el-card>
    <!-- 添加或编辑模板编制对话框 -->
    <el-dialog v-model="dialog.visible" :title="dialog.title" width="70%" append-to-body>
      <super-form 
        v-if="dialog.visible"
        :loading="dialog.loading"
        ref="superFormRef"
        code="templateEdit-add"
        @selectData="selectData"
        v-model="form"
        :form-rules="rules" 
        :methods="formMethods">
          <!-- <biz-file-upload
            v-if="dialog.visible"
            :biz="{
              bizId: form.id,
              bizType: 'templateEdit',
            }"
            v-model="form.files"
            :limit="5"
            :multiple="true"
          >
        </biz-file-upload> -->
      </super-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button :loading="buttonLoading" type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>
    <el-dialog v-model="apprDialog.visible" :title="apprDialog.title" width="500px" append-to-body>
      <el-form>
        <el-form-item label="提交意见" prop="approveComments">
          <el-input v-model="apprRow.approveComments" type="textarea" placeholder="请输入提交意见"></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button :loading="buttonLoading" type="primary" @click="submitApprove">确 定</el-button>
          <el-button @click="apprCancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>
    <!-- 差异对比弹窗 -->
    <el-dialog v-model="compareDialog.visible" :title="compareDialog.title" width="75%" append-to-body>
      <super-table
        v-if="compareDialog.visible"
        class="nomultiple"
        code="templateEdit-comparison"
        ref="superTableRef"
        rowKey="id"
        :dataLoading="compareLoading"
        :data="compareList"
        v-model:queryParams="compareQueryParams"
        :total="compareTotal"
        @getList="getCompareList"
        @select="selectChange"
      >
      </super-table>
      <template #footer>
        <div class="dialog-footer">
          <el-button :loading="buttonLoading" :disabled="Boolean(!selectRows)" type="primary" @click="submitCompare">确 定</el-button>
          <el-button @click="compareDialog.visible = false">取 消</el-button>
        </div>
      </template>
    </el-dialog>
    <!-- 审批记录 -->
    <approvalRecord ref="approvalRecordRef" />
  </div>
</template>

<script setup name="TemplateEdit" lang="ts">
import { listTemplateEdit, getTemplateEdit, delTemplateEdit, addTemplateEdit, updateTemplateEdit, submitAndFlowStart } from '@/api/dms/templateManagement/templateEdit/index';
import { fileOpenCheck } from '@/api/workflow/task';
import { useUserStore } from '@/store/modules/user';
import { listSnapshot } from '@/api/dms/templateManagement/templateSnapshot/index';
const { proxy } = getCurrentInstance() as ComponentInternalInstance;


const buttonLoading = ref(false);
const loading = ref(false);
const showSearch = ref(true);
const ids = ref<Array<string | number>>([]);
const names = ref<Array<string | number>>([]);
const single = ref(true);
const multiple = ref(true);
const showTable = ref(true);
const total = ref(0);
//审批记录组件
const approvalRecordRef = ref();

const superFormRef = ref(null);

const dialog = reactive<DialogOption>({
  title: '',
  visible: false,
  loading: false
});

const apprDialog = reactive<DialogOption>({
  title: '请输入提交意见',
  visible: false,
  loading: false
});

const initFormData = {
  tmplDept: useUserStore().deptId,
  approveComments: '',
};
const data = reactive<PageData<any, any>>({
  form: { ...initFormData },
  queryParams: reactive({
    pageNum: 1,
    pageSize: 10,
  }),
  rules: {
    // id: [{ required: true, message: '主键不能为空', trigger: 'blur' }],
  }
});

const { queryParams, form, rules } = toRefs(data);

const templateEditList = ref([]);

/** 查询模板编制列表 */
const getList = async () => {
  loading.value = true;
  try {
    const res = await listTemplateEdit(queryParams.value);
    templateEditList.value = res.rows;
    total.value = res.total;
  } finally {
    loading.value = false;
  }
};


const selectData = (data: any, config) => {
  if(config.prop == 'name'){
    form.value.userId = data.id;
    form.value.name = data.name;
    form.value.sex = data.sex;
    form.value.phone = data.phonenumber;
    form.value.email = data.email;
    form.value.deptId = data.deptId;
    form.value.deptName = data.deptName;
  }
};

// 在父组件中预定义所有可能用到的方法
const formMethods = {
  // args参数集合 childFrom参数集合
  customChangeHandler: (args: Array<any>,childFrom: Object) => {
    superFormRef.value.updateData({ companyName: 'admin' })
    console.log('自定义change事件触发')
  },
  handleRoleChange: (args: Array<any>,childFrom: Object) => {
    console.log('自定义事件触发', args, childFrom)
  },
  handleSexChange: (args: Array<any>,childFrom: Object) => {
    // if(args[0] == '2'){
    //   const index = formConfig.findIndex(item=>item.prop == 'zdy')
    //   formConfig[index].visible = true
    // }
    console.log('自定义事件触发', args)
  },
  processCompanyData: (args: Array<any>,childFrom: Object) => {
    console.log('自定义数据处理',args,childFrom)
  }
}

// 打开pageoffice
const openPageOffice = (row: any) => {
  const paramJson = {
    id: row.id,
    file_id: 1,
    file_name: "test.doc",
  };
  const paramString = JSON.stringify(paramJson);
  //openWindow()第三个参数用来向弹出的PageOffice浏览器（POBrowser）窗口传递参数(参数长度不限)，支持json格式字符串。
  //此处为了方便演示，我们传递了file_id和file_name两个参数，具体以您实际开发为准。
  proxy?.POBrowser.openWindow('/template', 'width=1200px;height=800px;', paramString);
  // console.log(row)
};


const compareDialog = reactive<DialogOption>({
  title: '差异对比',
  visible: false,
  loading: false
});
const compareList = ref([]);
const compareTotal = ref(0);
const compareLoading= ref(false);
const selectRows = ref(null);
const compareQueryParams = ref({
  pageNum: 1,
  pageSize: 10,
  bizId: '',
  tmplCode: '',
  tmplVersion: '',
});
const getCompareList = async () => {
  compareLoading.value = true;
  listSnapshot(compareQueryParams.value).then((res)=>{
    compareList.value = res.rows;
    compareTotal.value = res.total;
  }).finally(()=>{
    compareLoading.value = false;
  })
}
const superTableRef = ref<ElTableInstance>();
const selectChange = (selection) =>{
  if(selection.length > 1){
    const del_row = selection.shift()
    superTableRef.value.toggleRowSelection(del_row, false)
  }
  selectRows.value = selection[0]||null
}
// 差异对比，打开pageoffice
const submitCompare = () => {
  const paramJson = {
    bizId: compareQueryParams.value.bizId,
    compareBizId: selectRows.value.id,
  };
  console.log(paramJson)
  const paramString = JSON.stringify(paramJson);
  //openWindow()第三个参数用来向弹出的PageOffice浏览器（POBrowser）窗口传递参数(参数长度不限)，支持json格式字符串。
  //此处为了方便演示，我们传递了file_id和file_name两个参数，具体以您实际开发为准。
  proxy?.POBrowser.openWindow('/tempCompare', 'width=1200px;height=800px;', paramString);
}

// 差异对比按钮操作
const handleCompare = (row: any)=>{
  compareDialog.visible = true;
  compareDialog.title = '差异对比';
  compareQueryParams.value.pageNum = 1;
  compareQueryParams.value.tmplCode = row.tmplCode;
  compareQueryParams.value.tmplVersion = row.tmplVersion;
  compareQueryParams.value.bizId = row.id;
  selectRows.value = null;
  compareList.value = [];
  getCompareList();
}

//审批记录
const handleApprovalRecord = (row: any) => {
  approvalRecordRef.value.init(row.id);
};

const apprRow = ref();
// 提交审批按钮操作
const openApprove = async (row: any) => {
  row.subLoading = true;
  fileOpenCheck({fileBusinessId: row.id}).then(res=>{
    apprDialog.visible = true;
    apprRow.value = JSON.parse(JSON.stringify(row));
    apprRow.value.approveComments = '';
  }).finally(()=>{
    row.subLoading = false;
  })
};

/** 取消按钮 */
const apprCancel = () => {
  apprDialog.visible = false;
  apprRow.value.approveComments = '';
};

// 提交审批按钮操作
const submitApprove = () => {
  apprDialog.visible = true;
  buttonLoading.value = true;
  submitAndFlowStart(apprRow.value).then(() => {
    proxy?.$modal.msgSuccess('提交成功');
    apprCancel();
    getList();
  }).finally(()=>{
    buttonLoading.value = false;
  })
};



/** 取消按钮 */
const cancel = () => {
  reset();
  dialog.visible = false;
};

// 表单重置
const reset = () => {
  form.value = { ...initFormData };
};

/** 搜索按钮操作 */
const handleQuery = () => {
  queryParams.value.pageNum = 1;
  getList();
};

/** 重置按钮操作 */
const resetQuery = () => {
  handleQuery();
};

/** 多选框选中数据 */
const handleSelectionChange = (selection: any[]) => {
  ids.value = selection.map((item) => item.id);
  names.value = selection.map((item) => item.tmplName);
  single.value = selection.length != 1;
  multiple.value = !selection.length;
};

/** 新增按钮操作 */
const handleAdd = () => {
  reset();
  dialog.visible = true;
  dialog.loading = false;
  dialog.title = '添加模板编制';
};

/** 编辑按钮操作 */
const handleUpdate = async (row?: any) => {
  reset();
  dialog.visible = true;
  dialog.loading = true;
  dialog.title = '编辑模板编制';
  const _id = row?.id || ids.value[0];
  const res = await getTemplateEdit(_id);
  Object.assign(form.value, res.data);
  // 操作类型是失效时，禁用除原因之外的数据(已配置交互规则)
  // if (form.value.tmplDealType === 'invalid') {
  //   disabled = true;
  // }
  dialog.loading = false;
};

/** 提交按钮 */
const submitForm = () => {
  superFormRef.value?.validate(async (valid: boolean) => {
    if (valid) {
      const data = superFormRef.value.achieveData()
      buttonLoading.value = true;
      if (data.id) {
        await updateTemplateEdit(data).finally(() => (buttonLoading.value = false));
      } else {
        await addTemplateEdit(data).finally(() => (buttonLoading.value = false));
      }
      proxy?.$modal.msgSuccess('操作成功');
      dialog.visible = false;
      await getList();
    }
  });
};

/** 删除按钮操作 */
const handleDelete = async (row?: any) => {
  const _ids = row?.id || ids.value;
  const _names = row?.tmplName || names.value;
  await proxy?.$modal.confirm('是否确认删除模板名称为"' + _names + '"的数据项？').finally(() => (loading.value = false));
  await delTemplateEdit(_ids);
  proxy?.$modal.msgSuccess('删除成功');
  await getList();
};

/** 导出按钮操作 */
const handleExport = () => {
  proxy?.download(
    'dms/template-edit/export',
    {
      ...queryParams.value
    },
    `模板编制_${new Date().getTime()}.xlsx`
  );
};

onMounted(() => {
  getList();
});
</script>
