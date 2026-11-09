<template>
  <div class="p-2">
    <transition :enter-active-class="proxy?.animate.searchAnimate.enter" :leave-active-class="proxy?.animate.searchAnimate.leave">
      <div v-show="showSearch" class="mb-[10px]">
        <el-card shadow="hover">
          <el-form ref="queryFormRef" :model="queryParams" :inline="true" label-width="auto">
            <el-form-item label="姓名" prop="nickName">
              <el-input v-model="queryParams.nickName" placeholder="请输入姓名" clearable @change="handleQuery" @keyup.enter="handleQuery" />
            </el-form-item>
            <el-form-item label="内容" prop="msgLogContent">
              <el-input v-model="queryParams.msgLogContent" placeholder="请输入内容" clearable @change="handleQuery" @keyup.enter="handleQuery" />
            </el-form-item>
            <el-form-item label="状态" prop="msgLogResult">
              <el-select v-model="queryParams.msgLogResult" clearable  @change="handleQuery"  placeholder="请选择状态">
                <el-option v-for="dict in sys_common_status" :key="dict.value" :label="dict.label" :value="dict.value"></el-option>
                <el-option v-hasRole="['superadmin']" :key="'2'" label="撤回" value="2"></el-option>
              </el-select>
            </el-form-item>
            <el-form-item label="类型" prop="msgTemplateType">
              <el-select v-model="queryParams.msgTemplateType" clearable  @change="handleQuery"  placeholder="请选择类型">
                <el-option v-for="dict in sys_message_type" :key="dict.value" :label="dict.label" :value="dict.value"></el-option>
              </el-select>
            </el-form-item>
            <el-form-item label="模板编号" prop="msgTemplateCode">
              <el-input v-model="queryParams.msgTemplateCode" placeholder="请输入模板编号" clearable @change="handleQuery" @keyup.enter="handleQuery" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
              <el-button icon="Refresh" @click="resetQuery">重置</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </div>
    </transition>

    <el-card shadow="hover">
      <template #header>
        <el-row :gutter="10" class="mb8">
          <!-- <el-col :span="1.5">
            <el-button v-hasPermi="['monitor:msgLog:add']" type="primary" plain icon="Plus" @click="handleAdd">新增</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button v-hasPermi="['monitor:msgLog:edit']" type="success" plain icon="Edit" :disabled="single" @click="handleDelete(selectRow)"
              >修改</el-button
            >
          </el-col> -->
          <el-col :span="1.5" v-hasRole="['superadmin']">
            <el-button v-hasPermi="['monitor:msgLog:remove']" type="primary" plain icon="Delete" :disabled="multiple" @click="handleDelete">
              删除
            </el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button v-hasPermi="['monitor:msgLog:remove']" type="primary" plain icon="Delete" @click="handleClean">
              清空
            </el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button v-hasPermi="['monitor:msgLog:remove']" type="primary" plain icon="Refresh" :disabled="multipleRecall" @click="handleClean">
              撤回
            </el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button v-hasPermi="['monitor:msgLog:export']" type="primary" plain icon="Download" @click="handleExport">导出</el-button>
          </el-col>
          <right-toolbar v-model:show-search="showSearch" @query-table="getList"></right-toolbar>
        </el-row>
      </template>

      <el-table ref="msgLogTableRef" v-loading="loading" :data="msgLogList" border @selection-change="handleSelectionChange" @sort-change="handleSortChange">
        <el-table-column type="selection" width="55" align="center" v-if="checkRole(['superadmin'])" />
        <el-table-column label="编号" align="center" prop="msgLogId" v-if="false"/>
        <el-table-column label="序号" type="index" width="55" align="center" />
        <el-table-column label="内容" align="center" prop="msgLogContent" width="200" :show-overflow-tooltip="true">
          <template #default="scope">
            <el-button class="text" type="text"
              v-if="checkPermi(['monitor:msgLog:query'])"
              @click="handleView(scope.row)">
              <span>{{ scope.row.msgLogContent }}</span>
            </el-button>
            <span v-else>{{ scope.row.msgLogContent }}</span>
          </template>
        </el-table-column>
        <el-table-column label="工号" align="center" prop="userName" width="120" :show-overflow-tooltip="true" />
        <el-table-column label="姓名" align="center" prop="nickName" width="150" :show-overflow-tooltip="true" />
        <el-table-column label="发送时间" align="center" prop="logSendTime" width="160" :show-overflow-tooltip="true"
          sortable="custom" :sort-orders="['descending', 'ascending']"/>
        <el-table-column label="类型" align="center" prop="msgTemplateType" width="150" :show-overflow-tooltip="true">
          <template #default="scope">
            <dict-tag :options="sys_message_type" :value="scope.row.msgTemplateType"/>
          </template>
        </el-table-column>
        <el-table-column label="状态" align="center" prop="msgLogResult" width="150" :show-overflow-tooltip="true">
          <template #default="scope">
            <template v-if="scope.row.msgLogResult === '2'"><el-tag type="warning">撤回</el-tag></template>
            <dict-tag v-else :options="sys_common_status" :value="scope.row.msgLogResult"/>
          </template>
        </el-table-column>
        <el-table-column label="模板编号" align="center" prop="msgTemplateCode" width="200" :show-overflow-tooltip="true" />
        <el-table-column label="失败记录" align="center" prop="msgLogFail" width="150" :show-overflow-tooltip="true" />
        <el-table-column label="备注" align="center" prop="remark" width="150" :show-overflow-tooltip="true"/>
        <el-table-column label="操作" align="center" fixed="right" v-if="checkRole(['superadmin'])" :width="180" class-name="small-padding fixed-width">
          <template #default="scope">
            <el-tooltip content="删除" placement="top">
              <el-button v-hasPermi="['system:msgLog:remove']"  link type="primary" icon="Delete" @click="handleDelete(scope.row)"></el-button>
            </el-tooltip>
            <el-tooltip content="撤回" placement="top">
              <el-button v-hasPermi="['system:msgLog:edit']" v-if="scope.row.msgLogResult === '0'" link type="primary" icon="Refresh" @click="handleRecall(scope.row)"></el-button>
            </el-tooltip>
          </template>
        </el-table-column>
      </el-table>
      <pagination v-show="total > 0" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :total="total" @pagination="getList" />
    </el-card>
    <!-- 消息日志详细 -->
    <el-dialog v-model="dialog.visible" :title="dialog.title" width="550px" append-to-body>
      <el-form ref="tenantFormRef" :model="form" v-loading="dialog.loading" label-width="110px" label-suffix="：">
        <el-row>
          <el-col :span="12">
            <el-form-item label="工号">{{ form.userName }}</el-form-item>
            <el-form-item label="编号">{{ form.msgLogId }}</el-form-item>
            <el-form-item
              label="类型"
            >{{ form.__msgTemplateType }}</el-form-item>
            <el-form-item label="发送时间">{{ form.logSendTime }}</el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="姓名">{{ form.nickName }}</el-form-item>
            <el-form-item label="模板编号">{{ form.msgTemplateCode }}</el-form-item>
            <el-form-item label="状态">
              <!-- <div v-if="form.msgLogResult === '0'">成功</div>
              <div v-else-if="form.msgLogResult === '1'">失败</div> -->
              {{ form.__msgLogResult }}
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="内容">{{ form.msgLogContent }}</el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="失败记录">{{ form.msgLogFail }}</el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="cancel">关 闭</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="MsgLog" lang="ts">
import {
  listMsgLog, 
  delMsgLog, 
  cleanMsglog, 
  recallMsglog
} from '@/api/monitor/msgLog';
import { checkRole, checkPermi } from '@/utils/permission'
import { selectDictLabel } from '@/utils/ruoyi'

const { proxy } = getCurrentInstance() as ComponentInternalInstance;
const { sys_common_status, sys_message_type } = toRefs<any>(proxy.useDict('sys_common_status', 'sys_message_type'));

const msgLogList = ref([]);
const buttonLoading = ref(false);
const loading = ref(true);
const showSearch = ref(true);
const ids = ref<Array<string | number>>([]);
const names = ref<Array<string | number>>([]);
const single = ref(true);
const selectRow = ref({});
const multiple = ref(true);
const multipleRecall = ref(true);
const total = ref(0);

const queryFormRef = ref<ElFormInstance>();
const tenantFormRef = ref<ElFormInstance>();
const msgLogTableRef = ref<ElTableInstance>();

const dialog = reactive({
  visible: false,
  loading: false,
  title: ''
});

const initFormData = {
  msgLogId: undefined,
  msgTemplateCode: undefined,
  msgTemplateType: undefined,
  __msgTemplateType: undefined,
  msgLogContent: undefined,
  msgLogResult: undefined,
  __msgLogResult: undefined,
  msgLogFail: undefined,
  delFlag: undefined,
  createBy: undefined,
  createTime: undefined,
  updateBy: undefined,
  updateTime: undefined,
  userName: undefined,
  logSendTime: undefined,
  nickName: undefined,
  remark: undefined
};
const data = reactive({
  form: { ...initFormData },
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    nickName: undefined,
    msgTemplateCode: undefined,
    msgTemplateType: undefined,
    msgLogContent: undefined,
    msgLogResult: undefined,
    orderByColumn: undefined,
    isAsc: undefined,
  },
  rules: {
    serialRuleCode: [{ required: true, message: '编号不能为空', trigger: 'blur' }],
    serialRuleName: [{ required: true, message: '名称不能为空', trigger: 'blur' }],
    serialRuleContent: [{ required: true, message: '内容不能为空', trigger: 'blur' }],
  }
});

const { queryParams, form, rules } = toRefs(data);


/** 查询序列号管理列表 */
const getList = async () => {
  loading.value = true;
  const msgLogResult = queryParams.value.msgLogResult ?
    queryParams.value.msgLogResult : checkRole(['superadmin']) ? '0,1,2' : '0,1';
  const res = await listMsgLog({...queryParams.value,msgLogResult: msgLogResult});
  msgLogList.value = res.rows;
  total.value = res.total;
  loading.value = false;
};

// 取消按钮
const cancel = () => {
  reset();
  dialog.loading = false;
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
  getList();
};

/** 重置按钮操作 */
const resetQuery = () => {
  queryFormRef.value?.resetFields();
  msgLogTableRef.value?.clearSort()
  handleQuery();
};

// 多选框选中数据
const handleSelectionChange = (selection:any) => {
  ids.value = selection.map((item:any) => item.msgLogId);
  single.value = selection.length != 1;
  multiple.value = !selection.length;
  selectRow.value = {};
  multipleRecall.value = true;
  if(selection.length) {
    if(selection.length == 1){
      selectRow.value = selection[0]||{};
    }
    const canRecallList = selection.filter(row => row.msgLogResult === '0');
    multipleRecall.value = canRecallList.length < selection.length;
  }
};

/** 排序触发事件 */
const handleSortChange = (column, prop, order) => {
  queryParams.value.orderByColumn = column.prop;
  queryParams.value.isAsc = column.order;
  if(!queryParams.value.isAsc){
    queryParams.value.orderByColumn = undefined
  }
  getList();
};

/** 详细按钮操作 */
const handleView = (row) => {
  dialog.visible = true;
  row.__msgTemplateType = selectDictLabel(sys_message_type, row.msgTemplateType)
  row.__msgLogResult = selectDictLabel(sys_common_status, row.msgLogResult)
  Object.assign(form.value, row);
};

/** 删除按钮操作 */
const handleDelete = async (row) => {
  const msgLogIds = row.msgLogId || ids.value;
  proxy.$modal.confirm('是否确认删除?').then(() => {
    loading.value = true;
    return delMsgLog(msgLogIds);
  }).then(() => {
    loading.value = false;
    getList();
    proxy.$modal.msgSuccess('操作成功');
  }).catch(() => {
  }).finally(() => {
    loading.value = false;
  });
};

/** 清空按钮操作 */
const handleClean = () => {
  proxy.$modal.confirm('是否确认清空所有登录日志数据项？').then(function() {
    return cleanMsglog();
  }).then(() => {
    getList();
    proxy.$modal.msgSuccess('操作成功');
  }).catch(() => {});
};

/** 导出按钮操作 */
const handleExport = () => {
  proxy?.download(
    'resource/msgLog/export',
    {
      ...queryParams.value
    },
    `msgLog_${new Date().getTime()}.xlsx`
  );
};

/** 消息撤回 */
const handleRecall = (row) => {
  const msgLogIds = row.msgLogId || ids.value;
  proxy.$modal.confirm('确认是否撤回消息?').then(() => {
    loading.value = true;
    return recallMsglog(msgLogIds);
  }).then(() => {
    loading.value = false;
    getList();
    proxy.$modal.msgSuccess('操作成功');
  }).catch(() => {
  }).finally(() => {
    loading.value = false;
  });
};

onMounted(() => {
  getList();
});
</script>
