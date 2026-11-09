<template>
  <div class="p-2">
    <transition :enter-active-class="proxy?.animate.searchAnimate.enter" :leave-active-class="proxy?.animate.searchAnimate.leave">
      <div v-show="showSearch" class="mb-[10px]">
        <el-card shadow="hover">
          <super-search
            code="bookPrint-apply-search"
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
            <el-button v-hasPermi="['dms:volumesPrintApply:add']" type="primary" icon="Plus" @click="handleAdd">新增</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button v-hasPermi="['dms:volumesPrintApply:remove']" type="danger" icon="Delete" :disabled="multiple" @click="handleDelete()"
              >删除</el-button
            >
          </el-col>
          <right-toolbar v-model:show-search="showSearch" @query-table="getList"></right-toolbar>
        </el-row>
      </template>
      <super-table 
        v-if="showTable"
        code="bookPrint-apply-table"
        rowKey="id"
        highlightCurrentRow
        :dataLoading="loading"
        :data="recordList"
        v-model:queryParams="queryParams"
        :total="total"
        @selection-change="handleSelectionChange"
        @getList="getList"
      >
        <!-- 操作列 -->
        <template #operation="scope">
          <div>
            <el-tooltip content="编辑" placement="top" :enterable="false">
              <el-button link type="primary" v-hasPermi="['dms:volumesPrintApply:edit']" icon="EditPen" @click="handleUpdate(scope.row)"></el-button>
            </el-tooltip>
            <el-tooltip content="删除" placement="top" :enterable="false">
              <el-button link type="danger" v-hasPermi="['dms:volumesPrintApply:remove']" icon="Delete" @click="handleDelete(scope.row)"></el-button>
            </el-tooltip>
            <el-tooltip content="提交" placement="top" :enterable="false">
              <el-button link type="primary" v-hasPermi="['dms:volumesPrintApply:submit']" icon="SuccessFilled" @click="openApprove(scope.row)"></el-button>
            </el-tooltip>
            <el-tooltip content="预览pdf" placement="top" :enterable="false">
              <el-button link type="primary" v-hasPermi="['dms:volumesPrintApply:open']" icon="Location" @click="handleViewPdf(scope.row)"></el-button>
            </el-tooltip>
          </div>
        </template>
      </super-table>
      <!-- <pagination v-show="total > 0" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :total="total" @pagination="getList" /> -->
    </el-card>
    <!-- 添加或编辑成册打印申请对话框 -->
    <el-dialog v-model="dialog.visible" :title="dialog.title" width="95%" append-to-body>
      <super-form 
        v-if="dialog.visible"
        :loading="dialog.loading"
        ref="superFormRef"
        code="bookPrint-apply-form"
        @selectData="selectData"
        :otherCondition="otherCondition"
        v-model="form"
        :form-rules="rules" 
        :methods="formMethods">
        <el-card shadow="never">
          <template #header>
            <div class="card-header">
              <span>文件清单</span>
              <el-button size="small" @click="handSelect(true)" type="primary" style="float: right;" icon="Plus">批量新增</el-button>
            </div>
          </template>
          <div ref="fileListContainerRef" class="file-list-container">
            <el-card shadow="hover" v-for="(item, index) in fileList" :key="index" style="margin-bottom: 5px;" >
            <super-form 
              v-if="dialog.visible"
              ref="superFormChildRef"
              code="bookPrint-apply-filelist-form"
              @selectData="selectData"
              :otherCondition="otherCondition"
              v-model="fileList[index]"
              :methods="formMethods">
              <template #docCodeSolt="config">
                <el-input v-model="fileList[index][config.prop]" @click.native="handSelect(false,index)" v-bind="config" readonly></el-input>
              </template>
              <template #deleteBtn>
                <el-button v-if="fileList.length > 1" type="danger" style="transform: translate(0,14px);" icon="Delete" @click="deleteFile(index)"></el-button>
              </template>
            </super-form>
            </el-card>
          </div>
        </el-card>
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
    <!-- pdf预览附件弹窗 -->
    <el-dialog v-model="pdfDialog.visible" :title="pdfDialog.title" width="90%" append-to-body>
      <pdfView v-if="pdfDialog.visible" text="" :id="pdfDialog.id"></pdfView>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="pdfDialog.visible = false">关 闭</el-button>
        </div>
      </template>
    </el-dialog>
    <!-- 选择文件或记录 -->
    <SelectGroup
      v-if="selectOption.open"
      :title="selectOption.title"
      :selectType="selectOption.type"
      :multiple="selectOption.multiple"
      :width="selectOption.width"
      :otherCondition="selectOption.otherCondition"
      :achieveData="selectDataAchieve"
      @dialogClose="selectDataCancel"
    ></SelectGroup>
  </div>
</template>

<script setup name="BookPrintApply" lang="ts">
import { listBookPrint, getBookPrint, delBookPrint, addBookPrint, updateBookPrint, submitAndFlowStart } from '@/api/dms/bookPrint/bookPrintApply/index';
import SelectGroup from '@/views/dms/bookPrint/bookPrintApply/selectGroup.vue'
import { fileOpenCheck } from '@/api/workflow/task';
import { useUserStore } from '@/store/modules/user';
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
const superFormChildRef = ref(null);
const fileListContainerRef = ref<HTMLElement | null>(null);

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
  drafterId: useUserStore().userId,
  drafterUserName: useUserStore().nickname,
  approveComments: '',
  operate: ''
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

const recordList = ref([]);

/** 查询成册打印申请列表 */
const getList = async () => {
  loading.value = true;
  try {
    const res = await listBookPrint(queryParams.value);
    recordList.value = res.rows;
    total.value = res.total;
  } finally {
    loading.value = false;
  }
};

// 筛选条件
const otherCondition = ref({
  documentRecordCode: {
    activeStatus: 'active'
  }
});
const selectData = (data: any, config) => {
  if(config.prop == 'drafterUserName'){
    form.value.drafterId = data.id;
    form.value.drafterUserName = data.name;
  } else if(config.prop == 'documentRecordCode'){
    form.value.documentCode = data.dlCode;
    form.value.documentName = data.dlName;
    form.value.documentRecordLibraryId = data.id;
    form.value.documentRecordCode = data.dlrCode;
    form.value.documentRecordName = data.dlrName;
    form.value.documentRecordType = data.dlrType;
    form.value.editWay = data.editWay;
    form.value.tmplLibId = data.tmplLibId;
    form.value.tmplLibCode = data.tmplLibCode;
    form.value.tmplLibName = data.tmplLibName;
  } else if(config.prop == 'tmplLibName'){
    form.value.tmplLibId = data.id;
    form.value.tmplLibCode = data.tmplCode;
    form.value.tmplLibName = data.volumesName;
  }

};


const selectOption = ref({
  open: false,
  type: '',
  width: '',
  title: '',
  multiple: false,
  otherCondition: {},
})
const selectIndex = ref(0);
// 选择模板库
const handSelect = (multiple, index?: number) =>{
  selectIndex.value = index;
  selectOption.value.open = true;
  selectOption.value.multiple = multiple;
  selectOption.value.title = '选择文件或记录';
  selectOption.value.type = '';
  selectOption.value.otherCondition = {}
}
// 选中数据
const selectDataAchieve = (data, type) => {
  form.value.tmplSource = type;
  if(selectOption.value.multiple){
    data.forEach(item => {
      const obj = {}
      obj['docOrRecordId'] = item.id;
      obj['docDeptId'] = item.dlDeptId;
      obj['issueDeptId'] = item.issueDeptId;
      obj['docOrRecordType'] = 'document';
      obj['docCode'] = item.dlCode;
      obj['docName'] = item.dlName;
      obj['docLevel'] = item.dlLevel;
      obj['docType'] = item.dlType;
      fileList.value.push(obj);
    })
    // 批量新增后滚动到底部
    proxy?.$nextTick(() => { 
      scrollToBottom();
    })
  }else{
    const obj = {}
    obj['docOrRecordId'] = data.id;
    obj['docDeptId'] = data.dlDeptId;
    obj['issueDeptId'] = data.issueDeptId;
    obj['docOrRecordType'] = 'document';
    obj['docCode'] = data.dlCode;
    obj['docName'] = data.dlName;
    obj['docLevel'] = data.dlLevel;
    obj['docType'] = data.dlType;
    Object.assign(fileList.value[selectIndex.value], obj);
  }
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
    otherCondition: {},
  };
};

// 滚动到文件清单底部
const scrollToBottom = () => {
  if (fileListContainerRef.value) {
    // 使用 setTimeout 确保 DOM 完全渲染后再滚动
    setTimeout(() => {
      if (fileListContainerRef.value) {
        fileListContainerRef.value.scrollTop = fileListContainerRef.value.scrollHeight;
      }
    }, 100);
  }
};


// 在父组件中预定义所有可能用到的方法
const formMethods = {
  // args参数集合 childFrom参数集合
  editWayChange: (args: Array<any>,childFrom: Object) => {
    // 编辑方式为线下，清除模板库选择
    if(args[0] == 'offline'){
      form.value.tmplLibId = '';
      form.value.tmplLibCode = '';
      form.value.tmplLibName = '';
    }
  },
}

const apprRow = ref();
// 提交审批按钮操作
const openApprove = async (row: any) => {
  apprDialog.visible = true;
  apprRow.value = JSON.parse(JSON.stringify(row));
  apprRow.value.approveComments = '';
};

/** 取消按钮 */
const apprCancel = () => {
  apprDialog.visible = false;
  apprRow.value.approveComments = '';
};

const pdfDialog = reactive({
  title: '查看附件',
  visible: false,
  loading: false,
  id: '',
});
// 附件上传按钮操作
const handleViewPdf = (row: any)=>{
  pdfDialog.id = row.id;
  pdfDialog.visible = true;
}

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
  fileList.value = [{}];
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
  names.value = selection.map((item) => item.volumesName);
  single.value = selection.length != 1;
  multiple.value = !selection.length;
};
/** 新增按钮操作 */
const handleAdd = () => {
  reset();
  form.value.operate = 'add';
  dialog.visible = true;
  dialog.loading = false;
  dialog.title = '添加成册打印申请';
};

/** 编辑按钮操作 */
const handleUpdate = async (row?: any) => {
  reset();
  dialog.visible = true;
  dialog.loading = true;
  dialog.title = '编辑成册打印申请';
  const _id = row?.id || ids.value[0];
  const res: any = await getBookPrint(_id);
  Object.assign(form.value, res.data);
  if(res.data&&res.data.details&&Array.isArray(res.data.details)){
    fileList.value = res.data.details
  }
  form.value.operate = 'edit';
  dialog.loading = false;
};

const fileList = ref([{}]);
// 添加文件按钮操作
const addFile = () => {
  fileList.value.push({});
};

const deleteFile = (index) => {
  fileList.value.splice(index, 1);
}
/** 提交按钮 */
const submitForm = async () => {
  superFormRef.value?.validate(async (valid: boolean) => {
    if (valid) {
      // 校验所有子表单
      const result = await childValidate();
      if(!result){
        proxy?.$modal.msgError('文件清单未填写完整！');
        return false;
      }
      const data = superFormRef.value.achieveData()
      data['details'] = fileList.value;
      buttonLoading.value = true;
      if (data.id) {
        await updateBookPrint(data).finally(() => (buttonLoading.value = false));
      } else {
        await addBookPrint(data).finally(() => (buttonLoading.value = false));
      }
      proxy?.$modal.msgSuccess('操作成功');
      dialog.visible = false;
      await getList();
    }else{
      proxy?.$modal.msgError('册子主信息未填写完整！');
    }
  });
};
// 子表单验证
const childValidate = async() => { 
  // 校验所有子表单
  let allChildFormsValid = true;
  if (superFormChildRef.value && superFormChildRef.value.length > 0) {
    // 使用 Promise.all 来等待所有子表单验证完成
    const validationPromises = superFormChildRef.value.map((ref: any) => {
      if (ref && typeof ref.validate === 'function') {
        // 将回调形式的 validate 转换为 Promise 形式
        return new Promise<boolean>((resolve) => {
          ref.validate((valid: boolean) => {
            resolve(valid);
          });
        });
      }
      return Promise.resolve(true); // 如果 ref 不存在，默认认为有效
    });
    const childFormResults = await Promise.all(validationPromises);
    allChildFormsValid = childFormResults.every((valid) => valid);
    if (!allChildFormsValid) {
      return false;
    }
    return true;
  }
};

/** 删除按钮操作 */
const handleDelete = async (row?: any) => {
  const _ids = row?.id || ids.value;
  const _names = row?.volumesName || names.value;
  await proxy?.$modal.confirm('是否确认删除册子名称为"' + _names + '"的数据项？').finally(() => (loading.value = false));
  await delBookPrint(_ids);
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
    `成册打印申请_${new Date().getTime()}.xlsx`
  );
};

onMounted(() => {
  getList();
});
</script>

<style lang="scss" scoped>
.card-header{
  height: 25px;
}

.file-list-container {
  max-height: calc(100vh - 420px);
  overflow-y: auto;
  overflow-x: hidden;
  padding-right: 5px;
  
  /* 自定义滚动条样式 */
  &::-webkit-scrollbar {
    width: 8px;
  }
  
  &::-webkit-scrollbar-track {
    background: #f1f1f1;
    border-radius: 4px;
  }
  
  &::-webkit-scrollbar-thumb {
    background: #888;
    border-radius: 4px;
    
    &:hover {
      background: #555;
    }
  }
}
</style>