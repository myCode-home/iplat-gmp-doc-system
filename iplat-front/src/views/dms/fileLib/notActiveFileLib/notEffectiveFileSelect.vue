<template>
  <div class="p-2">
    <transition :enter-active-class="proxy?.animate.searchAnimate.enter" :leave-active-class="proxy?.animate.searchAnimate.leave">
      <div v-show="showSearch" class="mb-[10px]">
        <el-card shadow="hover">
          <super-search
            code="file-lib-search"
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
          <!-- <el-col :span="1.5">
            <el-button type="primary" icon="Plus" @click="handleAdd">新增</el-button>
          </el-col> -->
          <right-toolbar v-model:show-search="showSearch" @query-table="getList"></right-toolbar>
        </el-row>
      </template>
      <super-table 
        v-if="showTable"
        ref="fileTableRef"
        code="bookPrint-notEffectiveFile-select-table"
        :class="{ nomultiple: !multipleChoice }"
        rowKey="id"
        highlightCurrentRow
        :dataLoading="loading"
        :data="fileDataList"
        v-model:queryParams="queryParams"
        :total="total"
        @select="selectChange"
        @selection-change="handleSelectionChange"
        @getList="getList"
      >
        <!-- 文件类型 -->
        <template #dlTypeSlot="scope">
          <dict-tag :options="docTypeOption" :value="scope.row.dlType" valueProp="id" labelProp="sortName" />
        </template>
      </super-table>
      <!-- <pagination v-show="total > 0" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :total="total" @pagination="getList" /> -->
    </el-card>
    <!-- 添加或编辑文件库对话框 -->
    <el-dialog v-model="dialog.visible" :title="dialog.title" width="70%" append-to-body>
      <super-form 
        v-if="dialog.visible"
        :loading="dialog.loading"
        ref="superFormRef"
        code="file-lib-form"
        @selectData="selectData"
        v-model="form"
        :form-rules="rules" 
        :methods="formMethods">
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

<script setup name="TemplateEdit" lang="ts">
import { getFileList,addFile,editFile,getFileDetail,deleteFile } from '@/api/dms/fileLib/index';
import { getAttributeList, documentList } from '@/api/dms/fileSystemOption/fileType/index';
const { proxy } = getCurrentInstance() as ComponentInternalInstance;

const props = defineProps({
  multipleChoice: {
    type: Boolean,
    default: true
  },
  otherCondition: {
    type: Object,
    default: () => { }
  },
  multipleChoiceData: {
    type: Function,
    default: () => () => { }
  }
});


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

watch(() => props.otherCondition, () => {
  Object.keys(props.otherCondition).forEach(key => {
    queryParams.value[key] = props.otherCondition[key];
  });
}, { immediate: true, deep: true });

const fileDataList = ref([]);

/** 查询文件库列表 */
const getList = async () => {
  loading.value = true;
  try {
    const res = await getFileList(queryParams.value);
    fileDataList.value = res.rows;
    total.value = res.total;
  } finally {
    loading.value = false;
  }
};


const selectData = (data: any, config) => {
  console.log('selectDataselectData')
  console.log(data)
  console.log(config)
  if(config.prop == 'draftUserName') {
    form.value.drafterId = data.id;
    form.value.draftUserName = data.userName
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
const fileTableRef = ref();
/** 多选框选中数据 */
const handleSelectionChange = (selection: any[]) => {
  ids.value = selection.map((item) => item.id);
  names.value = selection.map((item) => item.tmplName);
  single.value = selection.length != 1;
  multiple.value = !selection.length;
  selectRows.value = selection;
};
const selectChange = (selection: any[]) => {
  if (!props.multipleChoice) {
    if (selection.length > 1) {
      const del_row = selection.shift();
      fileTableRef.value.toggleRowSelection(del_row, false);
    }
  }
};

/** 新增按钮操作 */
const handleAdd = () => {
  reset();
  dialog.visible = true;
  dialog.loading = false;
  dialog.title = '添加文件库';
};

/** 编辑按钮操作 */
const handleUpdate = async (row?: any) => {
  reset();
  dialog.visible = true;
  dialog.loading = true;
  dialog.title = '编辑文件库';
  const _id = row?.id || ids.value[0];
  const res = await getFileDetail(_id);
  Object.assign(form.value, res.data);

  dialog.loading = false;
};

/** 提交按钮 */
const submitForm = () => {
  superFormRef.value?.validate(async (valid: boolean) => {
    if (valid) {
      const data = superFormRef.value.achieveData()
      buttonLoading.value = true;
      if (data.id) {
        await editFile(data).finally(() => (buttonLoading.value = false));
      } else {
        await addFile(data).finally(() => (buttonLoading.value = false));
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
  await proxy?.$modal.confirm('是否确认删除文件？').finally(() => (loading.value = false));
  await deleteFile(_ids);
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
    `文件库_${new Date().getTime()}.xlsx`
  );
};

/** 提交按钮操作 */
const confirmChoiceData = async () => {
  if (selectRows.value.length === 0) {
    return proxy?.$modal.msgError('请选择');
  }
  if (props.multipleChoice) {
    props.multipleChoiceData(selectRows.value);
  } else {
    props.multipleChoiceData(selectRows.value[0]);
  }
};


const docTypeOption = ref([]);
const getDocumentList = () => {
  documentList({
    pageNum: 1,
    pageSize: 9999
  }).then((res) => {
    docTypeOption.value = res.rows;
  });
};

onMounted(() => {
  getList();
  getDocumentList();
});

defineExpose({
  confirmChoiceData
});
</script>
