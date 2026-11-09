<template>
  <div class="p-2">
    <el-card shadow="hover" style="display: flex;flex-direction: row-reverse;" size="small">
      <el-button type="primary" v-hasPermi="['dms:printApply:submit']" icon="Check" :disabled="multiple" @click="handleConfirm">销毁发放</el-button>
      <el-button type="primary" v-hasPermi="['dms:printApply:submit']" icon="Check" :disabled="multiple" @click="handleIssueConfirm">销毁人确认</el-button>
      <el-button type="danger" v-hasPermi="['dms:printApply:submit']" icon="Check" :disabled="multiple" @click="handleReceiveConfirm">监销人确认</el-button>
    </el-card>
    <transition :enter-active-class="proxy?.animate.searchAnimate.enter" :leave-active-class="proxy?.animate.searchAnimate.leave">
      <div v-show="showSearch" class="mb-[10px]">
        <el-card shadow="hover">
          <super-search
            code="bookPrintDestruction-search"
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
          <right-toolbar v-model:show-search="showSearch" @query-table="getList"></right-toolbar>
        </el-row>
      </template>
      <super-table 
        v-if="showTable"
        code="bookPrintDestruction-table"
        rowKey="id"
        highlightCurrentRow
        :dataLoading="loading"
        :data="recordList"
        v-model:queryParams="queryParams"
        :total="total"
        @selection-change="handleSelectionChange"
        @getList="getList"
      >
      </super-table>
      <!-- <pagination v-show="total > 0" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :total="total" @pagination="getList" /> -->
    </el-card>
    <!-- 成册销毁对话框 -->
    <el-dialog v-model="dialog.visible" :title="dialog.title" width="80%" append-to-body>
      <super-form 
        v-if="dialog.visible"
        :loading="dialog.loading"
        ref="superFormRef"
        code="bookPrintDestruction-form"
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
  </div>
</template>

<script setup name="BookPrintDestruction" lang="ts">
import { listReturn, destroy, destroySignature, destroyConfirmSignature, issueReceiveJudgment } from '@/api/dms/bookPrint/bookPrintPage/index';
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

const dialog = reactive({
  title: '',
  visible: false,
  loading: false,
  type: ''
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

/** 查询成册销毁列表 */
const getList = async () => {
  loading.value = true;
  try {
    const res = await listReturn(queryParams.value);
    recordList.value = res.rows;
    total.value = res.total;
  } finally {
    loading.value = false;
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
/** 销毁确认按钮操作 */
const handleConfirm = () => {
  reset();
  dialog.visible = true;
  dialog.loading = false;
  dialog.type = 'destroy';
  dialog.title = '销毁人确认';
};
/** 销毁人确认按钮操作 */
const handleIssueConfirm = () => {
  reset();
  dialog.visible = true;
  dialog.loading = false;
  dialog.type = 'destroyConfirm';
  dialog.title = '销毁人确认';
};

/** 提交按钮 */
const submitForm = () => {
  superFormRef.value?.validate(async (valid: boolean) => {
    if (valid) {
      if(dialog.type == 'destroy'){
        const data = superFormRef.value.achieveData()
        buttonLoading.value = true;
        const subForm = {
          ...data,
          ids: ids.value,
        }
        destroy(subForm).finally(() => (buttonLoading.value = false));
        proxy?.$modal.msgSuccess('操作成功');
        dialog.visible = false;
        await getList();
      }else {
        const data = superFormRef.value.achieveData()
        buttonLoading.value = true;
        const subForm = {
          ...data,
          ids: ids.value,
          signatureType: 'destroy',
          userId: useUserStore().userId,
          username: useUserStore().nickname
        }
        destroySignature(subForm).finally(() => (buttonLoading.value = false));
        proxy?.$modal.msgSuccess('操作成功');
        dialog.visible = false;
        await getList();
      }
    }
  });
};

// 监销人确认
const handleReceiveConfirm = async ()=>{
  const data = {
    ids: ids.value,
    signatureType: 'destroyConfirm',
    userId: useUserStore().userId,
    username: useUserStore().nickname
  }
  // 发放人，回收人，销毁人签名前判断是否有发放中，回收中，异常回收中， 销毁中的数据
  issueReceiveJudgment(data).then(res=>{
    if(res.code == 200){
      proxy?.$Signature(destroyConfirmSignature).then(({ es, users }: any)=>{
        data.username = users[0].userName;
        destroyConfirmSignature(data, es).then(res=>{
          proxy?.$modal.msgSuccess('操作成功');
          getList();
        })
      })
    }
  })
};

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

// 提交审批按钮操作
const submitApprove = () => {
  apprDialog.visible = true;
  buttonLoading.value = true;
  // submitAndFlowStart(apprRow.value).then(() => {
  //   proxy?.$modal.msgSuccess('提交成功');
  //   apprCancel();
  //   getList();
  // }).finally(()=>{
  //   buttonLoading.value = false;
  // })
};

onMounted(() => {
  getList();
});
</script>
