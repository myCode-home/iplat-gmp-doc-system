<template>
  <div class="p-2">
    <transition :enter-active-class="proxy?.animate.searchAnimate.enter"
      :leave-active-class="proxy?.animate.searchAnimate.leave">
      <div v-show="showSearch" class="mb-[10px]">
        <el-card v-if="isShowSearch" shadow="hover">
          <super-search :code="searchCode" v-model="queryParams" @getList="getList" @resetQuery="resetQuery">
          </super-search>
        </el-card>
      </div>
    </transition>

    <el-card shadow="hover">
      <template #header>
        <el-row :gutter="10" class="mb8">
          <!-- <el-col :span="1.5">
            <el-button v-hasPermi="['dms:documentRecordLib:add']" type="primary" icon="Plus" @click="handleAdd">新增</el-button>
          </el-col> -->
          <!-- <el-col :span="1.5">
            <el-button v-hasPermi="['dms:documentRecordLib:edit']" type="success" plain icon="Edit" :disabled="single" @click="handleUpdate()">编辑</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button v-hasPermi="['dms:documentRecordLib:remove']" type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete()"
              >删除</el-button
            >
          </el-col>
          <el-col :span="1.5">
            <el-button v-hasPermi="['dms:documentRecordLib:export']" type="warning" plain icon="Download" @click="handleExport">导出</el-button>
          </el-col> -->
          <right-toolbar v-model:show-search="showSearch" @query-table="getList"></right-toolbar>
        </el-row>
      </template>
      <super-table v-if="showTable" ref="superTableRef" code="recordLibrary-active-select-table"
        :class="{ nomultiple: !multipleChoice }" rowKey="id" highlightCurrentRow :dataLoading="loading"
        :data="activeRecordLibList" v-model:queryParams="queryParams" :total="total" @select="selectChange"
        @selection-change="handleSelectionChange" @getList="getList">
        <!-- 操作列 -->
        <template #operation="scope">
          <div>
            <el-tooltip content="编辑" placement="top" :enterable="false">
              <el-button link type="primary" v-hasPermi="['dms:documentRecordLib:edit']" icon="EditPen"
                @click="handleUpdate(scope.row)"></el-button>
            </el-tooltip>
            <el-tooltip content="删除" placement="top" :enterable="false">
              <el-button link type="danger" v-hasPermi="['dms:documentRecordLib:remove']" icon="Delete"
                @click="handleDelete(scope.row)"></el-button>
            </el-tooltip>
          </div>
        </template>
      </super-table>
      <!-- <pagination v-show="total > 0" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :total="total" @pagination="getList" /> -->
    </el-card>
    <!-- 添加或编辑生效记录库对话框 -->
    <el-dialog v-model="dialog.visible" :title="dialog.title" width="70%" append-to-body>
      <super-form v-if="dialog.visible" :loading="dialog.loading" ref="superFormRef" code="recordLibrary-active-form"
        @selectData="selectData" v-model="form" :form-rules="rules" :methods="formMethods">
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
import { listActiveRecord, getActiveRecord, delActiveRecord, addActiveRecord, updateActiveRecord, submitAndFlowStart } from '@/api/dms/recordLibrary/activeRecordLibrary/index';
import { fileOpenCheck } from '@/api/workflow/task';
import { useUserStore } from '@/store/modules/user';
import { listSnapshot } from '@/api/dms/templateManagement/templateSnapshot/index';
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
  },
  // 是否显示搜索框
  isShowSearch: {
    type: Boolean,
    default: true
  },
  // 搜索框code
  searchCode: {
    type: String,
    default: 'recordLibrary-active-search'
  },
  // 是否查询生效记录库
  isActiveStatus: {
    type: Boolean,
    default: false
  },
  useBy: {
    type: Boolean,
    default: false
  }
})

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
    // 判断组件使用的地方来判断是否默认查询生效记录库
    activeStatus: props.isActiveStatus ? 'active' : '',
    status: props.isActiveStatus ? 'active' : '',
  }),
  rules: {
    // id: [{ required: true, message: '主键不能为空', trigger: 'blur' }],
  }
});

const { queryParams, form, rules } = toRefs(data);

watch(() => props.otherCondition, () => {
  console.log(props.otherCondition)
  Object.keys(props.otherCondition).forEach(key => {
    queryParams.value[key] = props.otherCondition[key];
    // console.log(queryParams.value)
  });
}, { immediate: true, deep: true });

const activeRecordLibList = ref([]);

/** 查询生效记录库列表 */
const getList = async () => {
  loading.value = true;
  try {
    const res = await listActiveRecord(queryParams.value);
    activeRecordLibList.value = res.rows;
    total.value = res.total;
  } finally {
    loading.value = false;
  }
};

const selectData = (data: any, config) => {
  if (config.prop == 'draftUserName') {
    form.value.drafterId = data.id;
    form.value.draftUserName = data.name;
  }
};

// 在父组件中预定义所有可能用到的方法
const formMethods = {
  // args参数集合 childFrom参数集合
  customChangeHandler: (args: Array<any>, childFrom: Object) => {
    superFormRef.value.updateData({ companyName: 'admin' })
    console.log('自定义change事件触发')
  },
  handleRoleChange: (args: Array<any>, childFrom: Object) => {
    console.log('自定义事件触发', args, childFrom)
  },
  handleSexChange: (args: Array<any>, childFrom: Object) => {
    // if(args[0] == '2'){
    //   const index = formConfig.findIndex(item=>item.prop == 'zdy')
    //   formConfig[index].visible = true
    // }
    console.log('自定义事件触发', args)
  },
  processCompanyData: (args: Array<any>, childFrom: Object) => {
    console.log('自定义数据处理', args, childFrom)
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
  // 拿到选中的数据，将选中的数据放给父组件
  // props.multipleChoiceData(selection);
  // console.log('$$$$$$$$$$$$$$$$$$$$$$', selection);
  ids.value = selection.map((item) => item.id);
  names.value = selection.map((item) => item.tmplName);
  single.value = selection.length != 1;
  multiple.value = !selection.length;
  selectRows.value = selection;
  // 选中之后调用父组件的方法(为了破坏原来的调用，涛哥幸苦你了)
  if (!props.useBy) return;
  console.log('ids', ids.value);
  console.log('selectRows.value!!!!!!!!!!!!!!!!!!!!!!!!!');

  if (props.multipleChoice) {
    props.multipleChoiceData(selectRows.value);
  } else {
    props.multipleChoiceData(selectRows.value[0]);
  }
};
const superTableRef = ref<ElTableInstance>();
const selectChange = (selection) => {
  if (!props.multipleChoice) {
    if (selection.length > 1) {
      const del_row = selection.shift()
      superTableRef.value.toggleRowSelection(del_row, false)
    }
  }
}

/** 新增按钮操作 */
const handleAdd = () => {
  reset();
  dialog.visible = true;
  dialog.loading = false;
  dialog.title = '添加生效记录库';
};

/** 编辑按钮操作 */
const handleUpdate = async (row?: any) => {
  reset();
  dialog.visible = true;
  dialog.loading = true;
  dialog.title = '编辑生效记录库';
  const _id = row?.id || ids.value[0];
  const res = await getActiveRecord(_id);
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

/** 导出按钮操作 */
const handleExport = () => {
  proxy?.download(
    'dms/template-edit/export',
    {
      ...queryParams.value
    },
    `生效记录库_${new Date().getTime()}.xlsx`
  );
};

/** 提交按钮操作 */
const confirmChoiceData = async () => {
  if (selectRows.value.length == 0) {
    return proxy?.$modal.msgError('请选择');
  }
  const data = selectRows.value.map((item) => {
    item.name = item.tmplName
    return item
  })
  if (props.multipleChoice) {
    props.multipleChoiceData(data);
  } else {
    props.multipleChoiceData(data[0]);
  }
};

onMounted(() => {
  getList();
});

defineExpose({
  confirmChoiceData,
  getList,
  clearSelection: () => {
    if (superTableRef.value) {
      superTableRef.value.clearSelection();
    }
  }
})
</script>
