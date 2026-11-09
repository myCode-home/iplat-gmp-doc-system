<template>
  <div class="p-2">
    <transition :enter-active-class="proxy?.animate.searchAnimate.enter" :leave-active-class="proxy?.animate.searchAnimate.leave">
      <div v-show="showSearch" class="mb-[10px]">
        <el-card shadow="hover">
          <el-form ref="queryFormRef" :model="queryParams" :inline="true" label-width="auto">
            <el-form-item label="编号" prop="msgTemplateCode">
              <el-input v-model="queryParams.msgTemplateCode" placeholder="请输入编号" clearable @change="handleQuery" @keyup.enter="handleQuery" />
            </el-form-item>
            <el-form-item label="名称" prop="msgTemplateName">
              <el-input v-model="queryParams.msgTemplateName" placeholder="请输入名称" clearable @change="handleQuery" @keyup.enter="handleQuery" />
            </el-form-item>
            <el-form-item label="是否开启" prop="msgTemplateEnable">
              <el-select v-model="queryParams.msgTemplateEnable" clearable  @change="handleQuery"  placeholder="请选择是否开启">
                <el-option v-for="dict in sys_yes_no" :key="dict.value" :label="dict.label" :value="dict.value"></el-option>
              </el-select>
            </el-form-item>
            <!-- <el-form-item label="类型" prop="msgTemplateType">
              <el-select v-model="queryParams.msgTemplateType" clearable  @change="handleQuery"  placeholder="请选择类型">
                <el-option v-for="dict in sys_message_type" :key="dict.value" :label="dict.label" :value="dict.value"></el-option>
              </el-select>
            </el-form-item> -->
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
          <el-col :span="1.5">
            <el-button v-hasPermi="['system:msgTemplate:add']" type="primary" plain icon="Plus" @click="handleAdd">新增</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button v-hasPermi="['system:msgTemplate:edit']" type="success" plain icon="Edit" :disabled="single" @click="handleUpdate(selectRow)"
              >修改</el-button
            >
          </el-col>
          <el-col :span="1.5">
            <el-button v-hasPermi="['system:msgTemplate:remove']" type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete">
              删除
            </el-button>
          </el-col>
          <el-col :span="1.5" v-if="false">
            <el-button v-hasPermi="['system:msgTemplate:export']" type="warning" plain icon="Download" @click="handleExport">
              导出
            </el-button>
          </el-col>
          <right-toolbar v-model:show-search="showSearch" @query-table="getList"></right-toolbar>
        </el-row>
      </template>

      <el-table v-loading="loading" border :data="msgTemplateList" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column label="序号" type="index" width="55" align="center" prop="index" />
        <el-table-column label="编号" align="center" min-width="120" prop="msgTemplateCode" />
        <el-table-column label="名称" align="center" min-width="130" prop="msgTemplateName" />
        <el-table-column label="是否开启" align="center" prop="msgTemplateEnable">
          <template #default="{row}">
            <el-switch v-model="row.msgTemplateEnable" @change="handleStatusChange(row)" active-value="Y" inactive-value="N" />
          </template>
        </el-table-column>
        <el-table-column label="类型" width="150" align="center" prop="msgTemplateType">
          <template #default="{row}">
            <dict-tag :options="sys_message_type" :value="row.msgTemplateType" />
          </template>
        </el-table-column>
        <el-table-column label="内容" align="center" min-width="150" prop="msgTemplateContent" show-overflow-tooltip />
        <el-table-column label="备注" align="center" width="80" prop="remark" />
        <el-table-column label="修改时间" align="center" prop="updateTime" width="120">
          <template #default="scope">
            <span>{{ proxy.parseTime(scope.row.updateTime, '{y}-{m}-{d}') }}</span>
          </template>
        </el-table-column>
        <el-table-column width="120" label="操作" align="center" fixed="right" class-name="small-padding fixed-width">
          <template #default="scope">
            <el-tooltip content="修改" placement="top">
              <el-button v-hasPermi="['system:msgTemplate:edit']" link type="primary" icon="Edit" @click="handleUpdate(scope.row)"></el-button>
            </el-tooltip>
            <el-tooltip content="删除" placement="top">
              <el-button v-hasPermi="['system:msgTemplate:remove']" link type="primary" icon="Delete" @click="handleDelete(scope.row)"></el-button>
            </el-tooltip>
            <el-tooltip content="分配用户" placement="top">
              <el-button v-hasPermi="['system:msgTemplate:edit']" link type="primary" icon="User" @click="allocationUser(scope.row)"></el-button>
            </el-tooltip>
          </template>
        </el-table-column>
      </el-table>
      <pagination v-show="total > 0" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :total="total" @pagination="getList" />
    </el-card>
    <!-- 添加或修改消息通知管理对话框 -->
    <el-dialog v-model="dialog.visible" :title="dialog.title" width="500px" append-to-body>
      <el-form ref="tenantFormRef" v-loading="dialog.loading" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="编号" prop="msgTemplateCode">
          <el-input v-model="form.msgTemplateCode" placeholder="请输入编号" />
        </el-form-item>
        <el-form-item label="名称" prop="msgTemplateName">
          <el-input v-model="form.msgTemplateName" placeholder="请输入名称" />
        </el-form-item>
        <el-form-item label="是否开启" prop="msgTemplateEnable">
          <el-select v-model="form.msgTemplateEnable" placeholder="请选择">
            <el-option v-for="dict in sys_yes_no" :key="dict.value" :label="dict.label" :value="dict.value"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="类型" prop="msgTemplateType">
          <el-checkbox-group v-model="form.msgTemplateType" placeholder="请选择">
            <el-checkbox v-for="dict in sys_message_type" :key="dict.value" :label="dict.value" :value="dict.value">{{ dict.label }}</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
        <el-form-item label="内容" prop="msgTemplateContent">
          <el-input v-model="form.msgTemplateContent" type="textarea" :autosize="{ minRows: 2 }" placeholder="请输入内容" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" autosize placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button :loading="buttonLoading" type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>
    
    <!-- 选择用户对话框 -->
    <el-dialog v-model="userDialog.visible" :title="userDialog.title" width="750px" append-to-body>
      <MsgTemplateUser v-if="userDialog.visible" :userCheckValue="userCheckValue" ref="userCheckComponent"/>
      <template #footer>
        <div class="dialog-footer">
        <el-button :loading="buttonLoading" type="primary" @click="saveTemplateUser">保 存</el-button>
        <el-button @click="userDialog.visible = false">关 闭</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="MsgTemplate" lang="ts">
import {
  listMsgTemplate, 
  getMsgTemplate, 
  delMsgTemplate, 
  addMsgTemplate,
  updateMsgTemplate, 
  changeMsgTemplateStatus, 
  saveMsgTemplateUser
} from '@/api/system/msgTemplate';

import MsgTemplateUser from '@/views/system/msgTemplate/msgTemplateUser.vue';

const { proxy } = getCurrentInstance() as ComponentInternalInstance;
const { sys_yes_no, sys_message_type } = toRefs<any>(proxy?.useDict('sys_yes_no', 'sys_message_type'));
const userCheckComponent = ref();

const msgTemplateList = ref([]);
const buttonLoading = ref(false);
const loading = ref(true);
const showSearch = ref(true);
const ids = ref<Array<string | number>>([]);
const names = ref<Array<string | number>>([]);
const single = ref(true);
const selectRow = ref({});
const multiple = ref(true);
const total = ref(0);
const userCheckValue = ref({msgTemplateId:''});

const queryFormRef = ref<ElFormInstance>();
const tenantFormRef = ref<ElFormInstance>();

const dialog = reactive<DialogOption>({
  visible: false,
  title: '',
  loading: false
});
const userDialog = reactive<DialogOption>({
  visible: false,
  title: '',
  loading: false
});

const initFormData = {
  msgTemplateId: '',
  msgTemplateCode: '',
  msgTemplateName: '',
  msgTemplateEnable: '',
  msgTemplateType: ['10'],
  msgTemplateContent: '',
  remark: ''
};
const data = reactive({
  form: { ...initFormData },
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    msgTemplateId: '',
    msgTemplateCode: '',
    msgTemplateName: '',
    msgTemplateEnable: '',
    msgTemplateType: ''
  },
  rules: {
    msgTemplateCode: [{ required: true, message: '编号不能为空', trigger: 'blur' }],
    msgTemplateName: [{ required: true, message: '名称不能为空', trigger: 'blur' }],
    msgTemplateEnable: [{ required: true, message: '是否开启', trigger: 'blur' }],
    msgTemplateType: [{ required: true, message: '请选择类型', trigger: 'blur' }],
    msgTemplateContent: [{ required: true, message: '内容不能为空', trigger: 'blur' }],
  }
});

const { queryParams, form, rules } = toRefs(data);

/** 查询消息通知管理列表 */
const getList = async () => {
  loading.value = true;
  const res = await listMsgTemplate(queryParams.value);
  msgTemplateList.value = res.rows;
  total.value = res.total;
  loading.value = false;
};

// 取消按钮
const cancel = () => {
  reset();
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
  handleQuery();
};

// 多选框选中数据
const handleSelectionChange = (selection:any) => {
  ids.value = selection.map((item:any) => item.msgTemplateId);
  names.value = selection.map((item:any) => item.msgTemplateName);
  single.value = selection.length != 1;
  multiple.value = !selection.length;
  selectRow.value = selection[0]||{};
};

/** 新增按钮操作 */
const handleAdd = () => {
  reset();
  dialog.visible = true;
  dialog.title = '添加消息通知管理';
};

// 模板状态修改
const handleStatusChange = async (row) => {
  try {
    await proxy?.$modal.confirm(row.msgTemplateEnable === "N" ? "是否确认关闭？" : "是否确认开启？");
    await changeMsgTemplateStatus(row.msgTemplateId, row.msgTemplateCode, row.msgTemplateEnable);
    await getList();
    proxy?.$modal.msgSuccess('操作成功');
  } catch (error) {
    row.msgTemplateEnable = row.msgTemplateEnable === "N" ? "Y" : "N"
    await getList();
  }
};

/** 修改按钮操作 */
const handleUpdate = async (row) => {
  reset();
  dialog.visible = true;
  dialog.loading = true;
  dialog.title = '修改消息通知管理';
  const _id = row?.msgTemplateId || ids.value[0];
  const res = await getMsgTemplate(_id);
  dialog.loading = false;
  Object.assign(form.value, {...res.data});
  if(form.value.msgTemplateType){
    form.value.msgTemplateType = form.value.msgTemplateType.split(',')
  }else{
    form.value.msgTemplateType = []
  }
};

/** 提交按钮 */
const submitForm = () => {
  tenantFormRef.value?.validate(async (valid: boolean) => {
    if (valid) {
      buttonLoading.value = true;
      const data = JSON.parse(JSON.stringify(form.value));
      if(data.msgTemplateType&&data.msgTemplateType.length){
        data.msgTemplateType = data.msgTemplateType.join(',')
      }
      if (data.msgTemplateId) {
        await updateMsgTemplate(data).finally(() => (buttonLoading.value = false));
      } else {
        await addMsgTemplate(data).finally(() => (buttonLoading.value = false));
      }
      proxy?.$modal.msgSuccess('操作成功');
      dialog.visible = false;
      await getList();
    }
  });
};

/** 删除按钮操作 */
const handleDelete = async (row) => {
  const _ids = row?.msgTemplateId || ids.value;
  const _names = row?.msgTemplateName || names.value;
  await proxy?.$modal.confirm('是否确认删除消息通知管理名称为"' + _names + '"的数据项？');
  loading.value = true;
  await delMsgTemplate(_ids).finally(() => (loading.value = false));
  await getList();
  proxy?.$modal.msgSuccess('删除成功');
};

/** 根据模板编号获取关联用户 */
const allocationUser = (row) => {
  userCheckValue.value = row;
  userDialog.title = '分配角色';
  userDialog.visible = true;
};

/** 保存模板关联用户 */
const saveTemplateUser = () => {
  buttonLoading.value = true
  const data = userCheckComponent.value.achiveMsgTemplateUserData()
  saveMsgTemplateUser(userCheckValue.value?.msgTemplateId, data).then(response => {
    proxy.$modal.msgSuccess(response.msg);
    userDialog.visible = false
  }).finally(() => {
    buttonLoading.value = false
  });
}

/** 导出按钮操作 */
const handleExport = () => {
  proxy?.download(
    'system/tenant/export',
    {
      ...queryParams.value
    },
    `serialRule_${new Date().getTime()}.xlsx`
  );
};

onMounted(() => {
  getList();
});
</script>
