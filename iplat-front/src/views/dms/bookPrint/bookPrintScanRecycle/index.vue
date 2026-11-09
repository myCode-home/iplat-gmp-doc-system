<template>
  <div class="p-2">
    <transition :enter-active-class="proxy?.animate.searchAnimate.enter" :leave-active-class="proxy?.animate.searchAnimate.leave">
      <div v-show="showSearch" class="mb-[10px]">
        <el-card shadow="hover">
          <div style="display: flex; justify-content: space-between; align-items: center; width: 100%;">
            <div class="left-item">
              <el-input style="width: 400px" v-model="detailId" placeholder="请英文键盘下全选此处扫描,扫描之后请按回车键"
                @keyup.enter.stop="searchData($event)"></el-input>
            </div>
            <div class="right-item">
              <el-button type="primary" icon="plus" @click="handleSelect">手动选择</el-button>
              <el-button :loading="loadingSubmit" type="success" v-hasPermi="['dms:printApply:submit']" icon="Check" @click="handleConfirm">确认回收</el-button>
              <el-button :loading="loadingSubmit" type="warning" v-hasPermi="['dms:printApply:submit']" icon="Check" @click="handleOtherConfirm">其他回收</el-button>
              <el-button :loading="loadingSubmit" type="success" v-hasPermi="['dms:printApply:submit']" icon="Check" @click="handleReturnConfirm">交回人确认</el-button>
              <el-button :loading="loadingSubmit" type="info" v-hasPermi="['dms:printApply:submit']" icon="Check" @click="handleReceiveConfirm">回收人确认</el-button>
              <el-button :loading="loadingSubmit" type="warning" v-hasPermi="['dms:printApply:submit']" icon="Check" @click="handleOtherReturnConfirm">其他交回人确认</el-button>
              <el-button :loading="loadingSubmit" type="info" v-hasPermi="['dms:printApply:submit']" icon="Check" @click="handleOtherReceiveConfirm">其他回收人确认</el-button>
            </div>
          </div>
        </el-card>
      </div>
    </transition>

    <el-card shadow="hover">
      <!-- <template #header>
        <el-row :gutter="10" class="mb8">
          <right-toolbar v-model:show-search="showSearch" @query-table="getList"></right-toolbar>
        </el-row>
      </template> -->
      <super-table 
        v-if="showTable"
        code="bookPrint-scann-issue-table"
        rowKey="id"
        highlightCurrentRow
        :dataLoading="loading"
        :data="tableData"
        v-model:queryParams="queryParams"
        :total="total"
        @selection-change="handleSelectionChange"
        @getList="getList"
      >
        <!-- 操作列 -->
        <template #operation="scope">
          <div>
            <!-- <el-tooltip content="编辑" placement="top" :enterable="false">
              <el-button link type="primary" v-hasPermi="['dms:docRecordChangeOrder:edit']" icon="EditPen" @click="handleUpdate(scope.row)"></el-button>
            </el-tooltip> -->
            <el-popconfirm
            title="确定删除当前行数据吗？"
            @confirm="handleDelete(scope.$index)"
            >
            <template #reference>
              <span style="margin: 0 10px;">
                  <el-tooltip content="删除" placement="top" :enterable="false">
                    <el-button link type="danger" v-hasPermi="['dms:docRecordChangeOrder:remove']" icon="Delete"></el-button>
                  </el-tooltip>
                </span>
              </template>
            </el-popconfirm>
          </div>
        </template>
      </super-table>
      <!-- <pagination v-show="total > 0" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :total="total" @pagination="getList" /> -->
    </el-card>
    <!-- 添加或编辑成册扫码发放对话框 -->
    <el-dialog v-model="dialog.visible" :title="dialog.title" width="80%" append-to-body>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button :loading="buttonLoading" type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>
    <el-dialog v-model="apprDialog.visible" :title="apprDialog.title" width="500px" append-to-body>
      <el-form ref="apprFormRef" :model="apprRow" :rules="rules">
        <el-form-item label="原因" prop="approveComments">
          <el-input v-model="apprRow.approveComments" type="textarea" placeholder="请输入原因"></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button :loading="buttonLoading" type="primary" @click="handleAbnormalConfirm">确 定</el-button>
          <el-button @click="apprCancel">取 消</el-button>
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

<script setup name="BookPrintScanRecycle" lang="ts">
import { queryReceived, printReturn, printAbnormal, issueJudgment, returnSignature, issueReceiveJudgment, returnConfirmSignature, abnormalReturnSignature, abnormalReturnConfirm } from '@/api/dms/bookPrint/bookPrintPage/index';
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

const dialog = reactive<DialogOption>({
  title: '',
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
    approveComments: [{ required: true, message: '原因不能为空', trigger: 'blur' }],
  }
});

const { queryParams, form, rules } = toRefs(data);

const tableData = ref([]);

// 扫码添加数据操作
const detailId = ref('');
const searchData = (event: any) =>{
  if (event.keyCode === 13) {
    const ids = tableData.value.map((e) => e.id);
    if(ids.indexOf(detailId.value) >= 0){
      return proxy?.$modal.msgWarning('请勿重复扫描');
    }
    selectById(detailId.value);
  }
};
const selectById = (id: string | number)=>{
  queryReceived(id).then(res=>{
    if(res.data){
      tableData.value.push(res.data);
    }else{
      detailId.value = '';
      return proxy?.$modal.msgWarning('信息不存在，请重新扫码！');
    }
  })
};

const loadingSubmit = ref(false);
// 确认回收
const handleConfirm = async ()=>{
  if(tableData.value.length == 0){
    return proxy?.$modal.msgWarning('请选择要回收的文件！');
  }
  const data = {
    details: tableData.value.map(e=>{
      return {
        id: e.id,
        printApplyId: e.volumesPrintApplyId,
        pages: e.pages
      }
    }),
    userId: undefined,
    userName: undefined,
  }
  proxy?.$Signature(printReturn).then(({ es, users }: any)=>{
    data.userName = users[1].userName;
    printReturn(data, es).then(res=>{
      tableData.value = [];
      proxy?.$modal.msgSuccess('回收成功');
    })
  })
};
const apprDialog = reactive({
  title: '填写其它原因',
  visible: false,
  loading: false,
  type: 'other'
});
const apprRow = ref();
const apprFormRef = ref();
// 其他回收按钮操作
const handleOtherConfirm = ()=>{
  if(tableData.value.length == 0){
    return proxy?.$modal.msgWarning('请选择要回收的文件！');
  }
  apprDialog.visible = true;
  apprRow.value.approveComments = '';
  apprRow.value.type = 'other';
};
// 其他回收(异常回收)
const handleAbnormalConfirm = async ()=>{
  apprFormRef.value?.validate((valid: boolean)=>{
    if(valid){
      if(apprRow.value.type == 'other'){
        // 异常回收
        const data = {
          details: tableData.value.map(e=>{
            return {
              id: e.id,
              printApplyId: e.volumesPrintApplyId,
              pages: e.pages
            }
          }),
          userId: undefined,
          userName: undefined,
          abnormalReason: apprRow.value.approveComments
        }
        proxy?.$Signature(printAbnormal).then(({ es, users }: any)=>{
          data.userName = users[1].userName;
          printAbnormal(data, es).then(res=>{
            tableData.value = [];
            proxy?.$modal.msgSuccess('回收成功');
          })
        })
      }else{
        // 其他交回人确认
        const data = {
          ids: tableData.value.map(e=>e.id),
          signatureType: 'abnormalReturn',
          userId: undefined,
          userName: undefined,
          abnormalReason: apprRow.value.approveComments
        }
        proxy?.$Signature(abnormalReturnSignature).then(({ es, users }: any)=>{
          data.userName = users[0].userName;
          abnormalReturnSignature(data, es).then(res=>{
            tableData.value = [];
            proxy?.$modal.msgSuccess('回收成功');
          })
        })
      }
    }
  })
};
// 交回人确认
const handleReturnConfirm = async ()=>{
  if(tableData.value.length == 0){
    return proxy?.$modal.msgWarning('请选择要交回的文件！');
  }
  const data = {
    ids: tableData.value.map(e=>e.id),
    signatureType: 'return',
    userId: useUserStore().userId,
    username: useUserStore().name
  }
  // 发放人，回收人，销毁人签名前判断是否有发放中，回收中，异常回收中， 销毁中的数据
  issueJudgment(data).then(res=>{
    if(res.code == 200){
      proxy?.$Signature(returnSignature).then(({ es, users }: any)=>{
        data.username = users[0].userName;
        returnSignature(data, es).then(res=>{
          tableData.value = [];
          proxy?.$modal.msgSuccess('交回成功');
        })
      })
    }
  })
};
// 回收确认人确认
const handleReceiveConfirm = async ()=>{
  if(tableData.value.length == 0){
    return proxy?.$modal.msgWarning('请选择要回收的文件！');
  }
  const data = {
    ids: tableData.value.map(e=>e.id),
    signatureType: 'returnConfirm',
    userId: useUserStore().userId,
    username: useUserStore().name
  }
  issueReceiveJudgment(data).then(res=>{
    if(res.code == 200){
      proxy?.$Signature(returnConfirmSignature).then(({ es, users }: any)=>{
        data.username = users[0].userName;
        returnConfirmSignature(data, es).then(res=>{
          tableData.value = [];
          proxy?.$modal.msgSuccess('操作成功');
        })
      })
    }
  })
};

// 其他交回人确认
const handleOtherReturnConfirm = async ()=>{
  if(tableData.value.length == 0){
    return proxy?.$modal.msgWarning('请选择要交回的文件！');
  }
  apprDialog.visible = true;
  apprRow.value.approveComments = '';
  apprRow.value.type = 'double';
};
// 其他回收确认人确认
const handleOtherReceiveConfirm = async ()=>{
  if(tableData.value.length == 0){
    return proxy?.$modal.msgWarning('请选择要回收的文件！');
  }
  const data = {
    ids: tableData.value.map(e=>e.id),
    signatureType: 'returnConfirm',
    userId: useUserStore().userId,
    username: useUserStore().name
  }
  issueReceiveJudgment(data).then(res=>{
    if(res.code == 200){
      proxy?.$Signature(abnormalReturnConfirm).then(({ es, users }: any)=>{
        data.username = users[0].userName;
        abnormalReturnConfirm(data, es).then(res=>{
          tableData.value = [];
          proxy?.$modal.msgSuccess('操作成功');
        })
      })
    }
  })
};

const selectData = ref({
  open: false,
  type: 'toRecycleListSelect',
  width: '',
  title: '',
  selectType: '',
  multiple: true,
  otherCondition: {
    status: 'success'
  },
})
// 手动添加按钮操作
const handleSelect = (val) => {
  selectData.value.open = true;
  selectData.value.title = '请选择';
};
// 选中数据
const selectDataAchieve = (data) => {
  // 过滤掉已经在 tableData 中存在的数据
  const filteredData = data.filter(newItem => {
    return !tableData.value.some(item => item.id === newItem.id);
  });
  tableData.value = tableData.value.concat(filteredData);
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
    selectType: undefined,
    otherCondition: {
      status: 'success'
    },
  };
};

/** 查询成册扫码发放列表 */
const getList = async () => {
  // loading.value = true;
  // try {
  //   const res = await listRecordAlterationApply(queryParams.value);
  //   tableData.value = res.rows;
  //   total.value = res.total;
  // } finally {
  //   loading.value = false;
  // }
};

// 筛选条件
const otherCondition = ref({
  documentRecordCode: {
    activeStatus: 'active'
  }
});

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

/** 取消按钮 */
const apprCancel = () => {
  apprDialog.visible = false;
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
  form.value.operate = 'add';
  dialog.visible = true;
  dialog.loading = false;
  dialog.title = '添加成册扫码发放';
};

/** 编辑按钮操作 */
const handleUpdate = async (row?: any) => {
  reset();
  // dialog.visible = true;
  // dialog.loading = true;
  // dialog.title = '编辑成册扫码发放';
  // const _id = row?.id || ids.value[0];
  // // const res = await getRecordAlterationApply(_id);
  // Object.assign(form.value, res.data);
  // form.value.operate = 'edit';
  // dialog.loading = false;
};

/** 提交按钮 */
const submitForm = () => {
  // superFormRef.value?.validate(async (valid: boolean) => {
  //   if (valid) {
  //     const data = superFormRef.value.achieveData()
  //     buttonLoading.value = true;
  //     if (data.id) {
  //       await updateRecordAlterationApply(data).finally(() => (buttonLoading.value = false));
  //     } else {
  //       await addRecordAlterationApply(data).finally(() => (buttonLoading.value = false));
  //     }
  //     proxy?.$modal.msgSuccess('操作成功');
  //     dialog.visible = false;
  //     await getList();
  //   }
  // });
};

/** 删除按钮操作 */
const handleDelete = async (index?: any) => {
  tableData.value.splice(index, 1)
  // const _names = row?.tmplName || names.value;
  // await proxy?.$modal.confirm('是否确认删除模板名称为"' + _names + '"的数据项？').finally(() => (loading.value = false));
  // await delRecordAlterationApply(_ids);
  // proxy?.$modal.msgSuccess('删除成功');
  // await getList();
};

/** 导出按钮操作 */
const handleExport = () => {
  proxy?.download(
    'dms/template-edit/export',
    {
      ...queryParams.value
    },
    `成册扫码发放_${new Date().getTime()}.xlsx`
  );
};

onMounted(() => {
  // getList();
});
</script>

<style lang="scss" scoped>
.right-item{
  flex: 1;
  margin-left: 20px;
  display: flex;
  flex-wrap: wrap;
  button{
    margin-bottom: 2px;
  }
}
</style>
