<template>
  <div class="p-2">
    <!-- 打印申请操作按钮 -->
    <transition :enter-active-class="proxy?.animate.searchAnimate.enter"
      :leave-active-class="proxy?.animate.searchAnimate.leave">
      <div v-show="showSearch" class="mb-[10px]">
        <el-card shadow="hover">
          <super-search :showResetButton="false" code="printApply-form-search" v-model="queryParams" @getList="getList"
            @resetQuery="resetQuery">
          </super-search>
        </el-card>
      </div>
    </transition>

    <!-- 打印申请表格 -->
    <el-card shadow="hover">
      <template #header>
        <el-row :gutter="10" class="mb8">
          <el-col :span="1.5">
            <el-button type="primary" icon="Plus" @click="handleAdd">新增</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button type="danger" :disabled="multiple" icon="Delete" @click="handleBatch">批量删除</el-button>
          </el-col>
          <right-toolbar v-model:show-search="showSearch" @query-table="getList"></right-toolbar>
        </el-row>
      </template>
      <super-table v-if="showTable" code="printApply-table" rowKey="id" highlightCurrentRow :dataLoading="loading"
        :data="templateEditList" v-model:queryParams="queryParams" :total="total"
        @selection-change="handleSelectionChange" @getList="getList">
        <!-- 操作列 -->
        <template #operation="scope">
          <div>
            <el-tooltip content="编辑" placement="top" :enterable="false">
              <el-button link type="primary" v-hasPermi="['dms:printApply:edit']" icon="EditPen"
                @click="handleUpdate(scope.row)"></el-button>
            </el-tooltip>
            <el-tooltip content="删除" placement="top" :enterable="false">
              <el-button link type="danger" v-hasPermi="['dms:printApply:remove']" icon="Delete"
                @click="handleDelete(scope.row)"></el-button>
            </el-tooltip>
            <!-- 提交按钮的提示信息 -->
            <el-popconfirm width="200px" hide-icon title="确定提交当前行数据吗？" @confirm="openApprove(scope.row)"
              placement="left-start">
              <template #reference>
                <span style="margin-left: 12px;">
                  <el-tooltip content="提交" placement="top" :enterable="false">
                    <el-button link type="primary" icon="SuccessFilled" v-hasPermi="['dms:printApply:submit']">
                    </el-button>
                  </el-tooltip>
                </span>
              </template>
            </el-popconfirm>
          </div>
        </template>
      </super-table>
    </el-card>
    <!-- 添加或编辑模板编制对话框 -->
    <el-dialog v-model="dialog.visible" :title="dialog.title" width="70%" append-to-body>
      <super-form v-if="dialog.visible" :loading="dialog.loading" ref="superFormRef" code="printApply-form-apply"
        @selectData="selectData" v-model="form" :form-rules="rules" :methods="formMethods">
        <!-- 文件编号插槽 -->
        <template #documentCodeSolt="config">
          <el-input v-model="form[config.prop]" suffix-icon="Document" @click.native="handSelect" v-bind="config"
            readonly></el-input>
        </template>
      </super-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button :loading="buttonLoading" type="primary" @click="submitForm">提 交</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>
    <!-- 提交审批对话框 -->
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
    <!-- 选择模板或记录 -->
    <SelectGroup isShoueCodePlus="true" v-if="selectOption.open" :isShowTemplate="false" :title="selectOption.title"
      :selectType="selectOption.type" :multiple="selectOption.multiple" :width="selectOption.width"
      :otherCondition="selectOption.otherCondition" :achieveData="selectDataAchieve" @dialogClose="selectDataCancel">
    </SelectGroup>
  </div>
</template>

<script setup name="PrintApplyEdit" lang="ts">
import { printApplyMain, getPrintApplyDetail, delPrintApplyEdit, addPrintApplyEdit, updatePrintApplyEdit, submitAndFlowStart } from '@/api/dms/printDistribution/printApply/index';
const { proxy } = getCurrentInstance() as ComponentInternalInstance;
import SelectGroup from '@/views/dms/operateOrder/addOperateOrder/selectGroup.vue';

const buttonLoading = ref(false);
const loading = ref(false);
const showSearch = ref(true);
// 这里的ids是选择框选中的全部数据的id
const ids = ref<Array<string | number>>([]);
const names = ref<Array<string | number>>([]);
const single = ref(true);
const multiple = ref(true);
const showTable = ref(true);
const total = ref(0);
// 打印申请编辑表单
const superFormRef = ref(null);

const dialog = reactive<DialogOption>({
  title: '',
  visible: false,
  loading: false
});

const initFormData = {
};
const data = reactive<PageData<any, any>>({
  form: { ...initFormData },
  queryParams: reactive({
    pageNum: 1,
    pageSize: 10
  }),
  rules: {
    // id: [{ required: true, message: '主键不能为空', trigger: 'blur' }],
  }
});
const apprDialog = reactive<DialogOption>({
  title: '请输入提交意见',
  visible: false,
  loading: false
});

const { queryParams, form, rules } = toRefs(data);

const templateEditList = ref([]);

/** 查询打印申请列表 */
const getList = async () => {
  loading.value = true;
  try {
    // const res = await listPrintApplyEdit(queryParams.value);
    const res = await printApplyMain(queryParams.value);
    templateEditList.value = res.rows;
    total.value = res.total;
  } finally {
    loading.value = false;
  }
};

// 选择用户后，更新表单数据
const selectData = (data: any, config) => {
  console.log('选择的数据', data);
  if (config.prop == 'docCode') {
    console.log('选择的文档', data, config);
    // form.value.docOrRecordType = data.dlType;
    form.value.docCode = data.dlCode;
    form.value.docName = data.dlName;
    form.value.docVersion = data.dlVersion;
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
    console.log('自定义事件触发', args)
  },
  processCompanyData: (args: Array<any>, childFrom: Object) => {
    console.log('自定义数据处理', args, childFrom)
  }
};

const apprRow = ref();

// 审批按钮操作
const openApprove = (row: any) => {
  apprDialog.visible = true;
  apprRow.value = JSON.parse(JSON.stringify(row));
  apprRow.value.approveComments = '';
};
// 提交审批按钮操作
const submitApprove = () => {
  apprDialog.visible = true;
  buttonLoading.value = true;
  // 提交意见的接口
  submitAndFlowStart(apprRow.value).then(() => {
    proxy?.$modal.msgSuccess('提交成功!!');
    apprCancel();
    getList();
  }).catch((res) => {
    if (res.msg === '打印申请流程任务完成失败') { }
    proxy?.$modal.msgError('重复提交失败');
  }).finally(() => {
    buttonLoading.value = false;
  })
};
// 取下取消提交意见的方法
const apprCancel = () => {
  apprDialog.visible = false;
  // 清空提交意见
  apprRow.value.approveComments = '';
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
  dialog.title = '添加打印申请';
};

/** 编辑按钮操作 */
const handleUpdate = async (row?: any) => {
  reset();
  dialog.visible = true;
  dialog.loading = true;
  dialog.title = '打印申请编辑';
  const _id = row?.id || ids.value[0];
  const res = await getPrintApplyDetail(_id);
  Object.assign(form.value, res.data);
  dialog.loading = false;
};

/** 提交按钮 */
const submitForm = () => {
  superFormRef.value?.validate(async (valid: boolean) => {
    if (valid) {
      const data = superFormRef.value.achieveData();
      // delete data.approveComments;
      buttonLoading.value = true;
      // console.log('###############################################', data);

      if (data.id) {
        await updatePrintApplyEdit(data).finally(() => (buttonLoading.value = false));
      } else {
        await addPrintApplyEdit(data).finally(() => (buttonLoading.value = false));
      }
      proxy?.$modal.msgSuccess('操作成功');
      dialog.visible = false;
      await getList();
    }
  });
};

/** 删除打印申请 */
const handleDelete = async (row?: any) => {
  const _ids = row?.id || ids.value;
  // 确定删除的项目数量
  const deleteCount = Array.isArray(_ids) ? _ids.length : 1;
  // 根据是否有row参数判断是单个删除还是批量删除
  if (row) {
    // 单个删除的确认消息
    await proxy?.$modal.confirm('是否确认删除申请单编号为"' + row.applyCode + '"的数据项？').finally(() => (loading.value = false));
  } else {
    // 批量删除的确认消息
    await proxy?.$modal.confirm('是否确认删除选中的' + deleteCount + '个数据项？').finally(() => (loading.value = false));
  }
  // 执行删除操作
  await delPrintApplyEdit(_ids);
  // 显示成功消息
  proxy?.$modal.msgSuccess('删除成功');
  // 刷新列表
  await getList();
};

// 批量操作的删除按钮
const handleBatch = () => {
  // 检查是否有选中的数据
  if (ids.value.length === 0) {
    proxy?.$modal.msgWarning('请先选择要删除的数据项');
    return;
  }

  // 调用删除函数，不传递row参数表示批量删除选中项
  handleDelete();
};

// 选择生效文件或生效记录库
const selectOption = ref({
  open: false,
  type: '',
  width: '',
  title: '',
  multiple: false,
  otherCondition: {
    // status: 'active',
    // activeStatus: 'active'
  }
});

// 选中数据
const selectDataAchieve = (data, type) => {
  form.value.tmplSource = type;
  console.log('选中数据@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@', data, type);
  // 生效文件选择
  // if (type == 'document') {
  //   // 文件
  //   form.value.docOrRecordType = 'document';
  // } else {
  //   // 记录，但是发过来的type=template
  //   form.value.docOrRecordType = 'record';
  // }
  type == 'document' ? form.value.docOrRecordType = 'document' : form.value.docOrRecordType = 'record';
  form.value.docCode = data.dlCode;
  form.value.docName = data.dlName;
  form.value.docVersion = data.dlVersion;
  form.value.docStatus = data.status;
  selectDataCancel();
};
// 关闭选择
const selectDataCancel = () => {
  selectOption.value = {
    open: false,
    title: undefined,
    width: '',
    multiple: false,
    type: undefined,
    otherCondition: {
      // status: 'active',
      // activeStatus: 'active'
    },
  };
};
// 选择模板库
const handSelect = (index) => {
  selectOption.value.open = true;
  selectOption.value.title = '选择文件或记录';
  selectOption.value.type = '';
  selectOption.value.otherCondition = {
    // status: 'active',
    // activeStatus: 'active'
  }
}
onMounted(() => {
  getList();
});
</script>
