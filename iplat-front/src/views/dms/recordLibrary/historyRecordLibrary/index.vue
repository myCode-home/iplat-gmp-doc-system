<template>
  <div class="p-2">
    <transition :enter-active-class="proxy?.animate.searchAnimate.enter" :leave-active-class="proxy?.animate.searchAnimate.leave">
      <div v-show="showSearch" class="mb-[10px]">
        <el-card shadow="hover">
          <super-search
            code="recordLibrary-active-search"
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
            <el-button v-hasPermi="['dms:documentRecordLib:mulDownloadHistoryWord']" type="primary" icon="Download" :disabled="multiple" @click="mulDownloadWord(selectRows, 'DOC_FILE')"
              >批量导出word</el-button
            >
          </el-col>
          <el-col :span="1.5">
            <el-button v-hasPermi="['dms:documentRecordLib:mulDownloadHistoryPdf']" type="primary" icon="SoldOut" :disabled="multiple" @click="mulDownloadWord(selectRows, 'PDF_FILE')">批量导出pdf</el-button>
          </el-col>
          <right-toolbar v-model:show-search="showSearch" @query-table="getList"></right-toolbar>
        </el-row>
      </template>
      <super-table 
        v-if="showTable"
        code="recordLibrary-active-table"
        rowKey="id"
        highlightCurrentRow
        :dataLoading="loading"
        :data="activeRecordLibList"
        v-model:queryParams="queryParams"
        :total="total"
        @selection-change="handleSelectionChange"
        @getList="getList"
      >
        <!-- 操作列 -->
        <template #operation="scope">
          <div>
            <el-tooltip content="详情" placement="top">
              <el-button link type="primary" v-hasPermi="['dms:documentRecordLib:query']" icon="View" @click="handleDetail(scope.row)"></el-button>
            </el-tooltip>
            <el-tooltip content="导出word" placement="top">
              <el-button link type="primary" v-hasPermi="['dms:documentRecordLib:downloadHistoryWord']" icon="Download" @click="downloadWord(scope.row, 'DOC_FILE')"></el-button>
            </el-tooltip>
            <el-tooltip content="导出pdf" placement="top">
              <el-button link type="primary" v-hasPermi="['dms:documentRecordLib:downloadHistoryPdf']" icon="SoldOut" @click="downloadWord(scope.row, 'PDF_FILE')"></el-button>
            </el-tooltip>
          </div>
        </template>
      </super-table>
      <!-- <pagination v-show="total > 0" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :total="total" @pagination="getList" /> -->
    </el-card>
    <!-- 添加或编辑历史记录库对话框 -->
    <el-dialog v-model="dialog.visible" :title="dialog.title" width="90%" append-to-body>
      <super-form 
        v-if="dialog.visible"
        :loading="dialog.loading"
        ref="superFormRef"
        code="recordLibrary-history-form"
        disabled
        @selectData="selectData"
        v-model="form"
        :form-rules="rules" 
        :methods="formMethods">
        <PdfView v-if="form.id" :text="waterText" :id="form.id"></PdfView>
      </super-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button :loading="buttonLoading" type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="ActiveRecordLibrary" lang="ts">
import { historyListActiveRecord, getActiveRecord, delActiveRecord, addActiveRecord, updateActiveRecord, submitAndFlowStart } from '@/api/dms/recordLibrary/activeRecordLibrary/index';
import { listByBizIdAndType, downloadFile } from '@/api/system/oss';
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

const activeRecordLibList = ref([]);

/** 查询历史记录库列表 */
const getList = async () => {
  loading.value = true;
  try {
    const res = await historyListActiveRecord(queryParams.value);
    activeRecordLibList.value = res.rows;
    total.value = res.total;
  } finally {
    loading.value = false;
  }
};


const selectData = (data: any, config) => {
  if(config.prop == 'draftUserName'){
    form.value.drafterId = data.id;
    form.value.draftUserName = data.name;
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

const selectRows = ref([]);
/** 多选框选中数据 */
const handleSelectionChange = (selection: any[]) => {
  ids.value = selection.map((item) => item.id);
  names.value = selection.map((item) => item.tmplName);
  single.value = selection.length != 1;
  multiple.value = !selection.length;
  selectRows.value = selection;
};

/** 新增按钮操作 */
const handleAdd = () => {
  reset();
  dialog.visible = true;
  dialog.loading = false;
  dialog.title = '添加历史记录库';
};

const waterText = ref('')
/** 编辑按钮操作 */
const handleDetail = async (row?: any) => {
  reset();
  dialog.visible = true;
  dialog.loading = true;
  dialog.title = '历史记录库详情';
  const _id = row?.id || ids.value[0];
  const res = await getActiveRecord(_id);
  Object.assign(form.value, res.data);
  const { name, nickname } = useUserStore();
  const time = proxy?.parseTime(new Date(), '{y}-{m}-{d}')
  waterText.value = `${name} ${nickname} ${time} 生效`
  dialog.loading = false;
};

/** 提交按钮 */
const submitForm = () => {
  superFormRef.value?.validate(async (valid: boolean) => {
    if (valid) {
      const data = superFormRef.value.achieveData()
      buttonLoading.value = true;
      if (data.id) {
        await updateActiveRecord(data).finally(() => (buttonLoading.value = false));
      } else {
        await addActiveRecord(data).finally(() => (buttonLoading.value = false));
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
  await delActiveRecord(_ids);
  proxy?.$modal.msgSuccess('删除成功');
  await getList();
};

// 导出文件
const downloadWord = (row: any, bizType: any) =>{
  loading.value = true;
  listByBizIdAndType(row.id, bizType).then((res: any)=>{
    if(res.data && res.data.length){
      res.data.forEach((item: any) => {
        proxy?.$download.oss(item.ossId);
      })
    }else{
      proxy?.$modal.msgError('未查询到文件');
    }
  }).finally(()=>{
    loading.value = false;
  })
};
// 批量导出文件
const mulDownloadWord = (row: any, bizType: any) =>{
  const data = {
    bizIds: row.map(item=>item.id).join(','),
    bizType: bizType
  }
  loading.value = true;
  downloadFile(data).then((res: any)=>{
    const blob = new Blob([res]);
    const fileName = `历史记录库_${new Date().getTime()}`// 创建下载链接
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.setAttribute('download', fileName+'.zip');
    document.body.appendChild(link);
    link.click();
    link.remove();
    window.URL.revokeObjectURL(url);
  }).finally(()=>{
    loading.value = false;
  })
};

/** 导出按钮操作 */
const handleExport = () => {
  proxy?.download(
    'dms/template-edit/export',
    {
      ...queryParams.value
    },
    `历史记录库_${new Date().getTime()}.xlsx`
  );
};

onMounted(() => {
  getList();
});
</script>
