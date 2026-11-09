<template>
  <el-dialog v-model="dialog.visible" width="96%" draggable :before-close="cancel" :close-on-click-modal="false"
    class="submit-verify-dialog" header-class="submit-verify-header" body-class="submit-verify-body">
    <div class="dialog-title">{{ dialog.title }}</div>
    <div class="content-wrapper">
      
      <div class="right-panel">
        
        <div class="flow-box">
          <div class="right-title">
            <div class="title-tip"></div>
            流程图
          </div>
          <div class="flowChart-box">
            <flowChart v-if="insId" :ins-id="insId" :noBoxShadow="true" />
          </div>
        </div>
      </div>
      <div class="left-panel">
        <div class="left-title">
          <div class="title-tip"></div>
          审批信息
        </div>
        <div class="item-box"> 
          <div class="data-item" v-for="(item, index) in historyList">
            <div class="item-title">
              <div class="circle-icon" :class="{'status-warning': item.flowStatus == 'waiting'}"></div>
              <div class="title-label">
                <div class="name-title">{{ item.nodeName }}
                  <!-- <el-tag v-if="getStatusClass(item.flowStatus) =='success'" type="success">通过</el-tag>
                  <el-tag v-if="getStatusClass(item.flowStatus) =='danger'" type="danger">驳回</el-tag> -->
                  <dict-tag style="margin-left: 5px;" :options="wf_task_status" :value="item.flowStatus" />
                </div>
                <div class="time" v-if="item.updateTime">{{ parseTime(item.updateTime, '{y}.{m}.{d}') }}</div>
              </div>
            </div>
            <div class="item-content">
              <div class="content-box" :class="{ 'is-last': index === historyList.length - 1,'status-warning': item.flowStatus == 'waiting' }">
                <div class="content-time">
                  <div class="name">{{ item.approveName }}</div>
                  
                </div>
                <div class="content-message" :title="item.message">{{ item.message || ''}}</div>
              </div>
            </div>
          </div>
        </div>
        <div class="message-box">
          <div class="left-title" style="margin-bottom: 7px;" :class="getStatusClass(historyList[0] ? historyList[0].flowStatus : '')">
            <div class="title-tip"></div>
            {{ commentsStr }}
          </div>
          <el-form v-loading="loading" :model="form" label-width="auto">
            <el-form-item v-if="task.flowStatus === 'waiting'">
              <el-input type="textarea" v-model="form.message" resize="none" class="message-box-textarea" placeholder="请输入审批意见"/>
            </el-form-item>
          </el-form>
        </div>

      </div>
    </div>

    <template #footer>
      <span class="submit-verify-footer">
        <el-button :disabled="buttonDisabled" type="primary" @click="handleCompleteTask" size="large">
          <img class="btn-icon" src="@/assets/images/submitVerify/commit.png" />
          提交 </el-button>
        <el-button v-if="task.flowStatus === 'waiting' && buttonObj.trust" :disabled="buttonDisabled" type="primary" size="large"
          @click="openDelegateTask">
          <img class="btn-icon" src="@/assets/images/submitVerify/commit.png" />
          委托
        </el-button>
        <el-button v-if="task.flowStatus === 'waiting' && buttonObj.transfer" :disabled="buttonDisabled" type="primary" size="large"
          @click="openTransferTask">
          <img class="btn-icon" src="@/assets/images/submitVerify/commit.png" />
          转办
        </el-button>
        <el-button v-if="task.flowStatus === 'waiting' && Number(task.nodeRatio) > 0 && buttonObj.addSign" size="large"
          :disabled="buttonDisabled" type="primary" @click="openMultiInstanceUser">
          <img class="btn-icon" src="@/assets/images/submitVerify/commit.png" />
          加签
        </el-button>
        <el-button v-if="task.flowStatus === 'waiting' && Number(task.nodeRatio) > 0 && buttonObj.subSign" size="large"
          :disabled="buttonDisabled" type="primary" @click="handleTaskUser">
          <img class="btn-icon" src="@/assets/images/submitVerify/commit.png" />
          减签
        </el-button>
        <el-button v-if="task.flowStatus === 'waiting' && buttonObj.termination" :disabled="buttonDisabled" size="large"
          type="danger" @click="handleTerminationTask">
          <img class="btn-icon" src="@/assets/images/submitVerify/stop.png" />
          终止
        </el-button>
        <el-button v-if="task.flowStatus === 'waiting' && buttonObj.back" :disabled="buttonDisabled" type="danger" size="large"
          @click="handleBackProcessOpen">
          <img class="btn-icon" src="@/assets/images/submitVerify/rollback.png" />
          退回
        </el-button>
        <el-button :disabled="buttonDisabled" @click="cancel" size="large">
          <img class="btn-icon" src="@/assets/images/submitVerify/cancel.png" />取消</el-button>
      </span>
    </template>
    <!-- 抄送 -->
    <UserSelect ref="userSelectCopyRef" :multiple="true" :data="selectCopyUserIds"
      @confirm-call-back="userSelectCopyCallBack"></UserSelect>
    <!-- 转办 -->
    <UserSelect ref="transferTaskRef" :multiple="false" @confirm-call-back="handleTransferTask"></UserSelect>
    <!-- 委托 -->
    <UserSelect ref="delegateTaskRef" :multiple="false" @confirm-call-back="handleDelegateTask"></UserSelect>
    <!-- 加签组件 -->
    <UserSelect ref="multiInstanceUserRef" :multiple="true" @confirm-call-back="addMultiInstanceUser"></UserSelect>
    <!-- 弹窗选人 -->
    <UserSelect ref="porUserRef" :multiple="true" :userIds="popUserIds" @confirm-call-back="handlePopUser"></UserSelect>

    <!-- 驳回开始 -->
    <el-dialog v-model="backVisible" draggable title="驳回" width="450px" :close-on-click-modal="false">
      <el-form v-if="task.flowStatus === 'waiting'" v-loading="backLoading" :model="backForm" label-width="auto">
        <el-form-item label="驳回节点">
          <el-select v-model="backForm.nodeCode" clearable placeholder="请选择">
            <el-option v-for="item in taskNodeList" :key="item.nodeCode" :label="item.nodeName"
              :value="item.nodeCode" />
          </el-select>
        </el-form-item>
        <!-- <el-form-item label="消息提醒">
          <el-checkbox-group v-model="backForm.messageType">
            <el-checkbox label="1" name="type" disabled>站内信</el-checkbox>
            <el-checkbox label="2" name="type">邮件</el-checkbox>
            <el-checkbox label="3" name="type">短信</el-checkbox>
          </el-checkbox-group>
        </el-form-item> -->
        <!-- <el-form-item v-if="task.flowStatus === 'waiting'" label="附件">
          <fileUpload v-model="backForm.fileId"
            :file-type="['png', 'jpg', 'jpeg', 'doc', 'docx', 'xlsx', 'xls', 'ppt', 'txt', 'pdf']" :file-size="20" />
        </el-form-item> -->
        <el-form-item label="审批意见">
          <el-input v-model="backForm.message" type="textarea" resize="none" placeholder="请输入审批意见" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer" style="float: right; padding-bottom: 20px">
          <el-button :disabled="backButtonDisabled" type="primary" @click="handleBackProcess">确认</el-button>
          <el-button :disabled="backButtonDisabled" @click="backVisible = false">取消</el-button>
        </div>
      </template>
    </el-dialog>
    <!-- 驳回结束 -->
    <el-dialog v-model="deleteSignatureVisible" draggable title="减签人员" width="700px" height="400px" append-to-body
      :close-on-click-modal="false">
      <div>
        <el-table :data="deleteUserList" border>
          <el-table-column prop="nodeName" label="任务名称" />
          <el-table-column prop="nickName" label="办理人" />
          <el-table-column label="操作" align="center" width="160">
            <template #default="scope">
              <el-button type="danger" size="small" icon="Delete" @click="deleteMultiInstanceUser(scope.row)">删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-dialog>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { ComponentInternalInstance } from 'vue';
import { ElForm } from 'element-plus';
import {
  completeTask,
  backProcess,
  getTask,
  taskOperation,
  terminationTask,
  getBackTaskNode,
  currentTaskAllUser,
  getNextNodeList
} from '@/api/workflow/task';
import UserSelect from '@/components/UserSelect/index.vue';
import FlowChart from '@/components/Process/flowChart.vue';
import { flowHisTaskList } from '@/api/workflow/instance';
import { listByIds } from '@/api/system/oss';

const { proxy } = getCurrentInstance() as ComponentInternalInstance;
import { UserVO } from '@/api/system/user/types';
import { FlowTaskVO, TaskOperationBo } from '@/api/workflow/task/types';

const userSelectCopyRef = ref<InstanceType<typeof UserSelect>>();
const transferTaskRef = ref<InstanceType<typeof UserSelect>>();
const delegateTaskRef = ref<InstanceType<typeof UserSelect>>();
const multiInstanceUserRef = ref<InstanceType<typeof UserSelect>>();
const porUserRef = ref<InstanceType<typeof UserSelect>>();
  
const props = defineProps({
  taskVariables: {
    type: Object as () => Record<string, any>,
    default: () => { }
  }
});
//遮罩层
const loading = ref(true);
//按钮
const buttonDisabled = ref(true);
//任务id
const taskId = ref<string>('');
//抄送人
const selectCopyUserList = ref<UserVO[]>([]);
//抄送人id
const selectCopyUserIds = ref<string>(undefined);
//可减签的人员
const deleteUserList = ref<any>([]);
//弹窗可选择的人员id
const popUserIds = ref<any>([]);
//驳回是否显示
const backVisible = ref(false);
const backLoading = ref(true);
const backButtonDisabled = ref(true);
// 可驳回得任务节点
const taskNodeList = ref([]);
const nickName = ref({});
const commentsStr = ref<string>('');
//节点编码
const nodeCode = ref<string>('');
const buttonObj = ref<any>({
  pop: false,
  trust: false,
  transfer: false,
  addSign: false,
  subSign: false,
  termination: false,
  back: false
});
//下一节点列表
const nestNodeList = ref([]);
//任务
const task = ref<FlowTaskVO>({
  id: undefined,
  createTime: undefined,
  updateTime: undefined,
  tenantId: undefined,
  definitionId: undefined,
  instanceId: undefined,
  flowName: undefined,
  businessId: undefined,
  nodeCode: undefined,
  nodeName: undefined,
  flowCode: undefined,
  flowStatus: undefined,
  formCustom: undefined,
  formPath: undefined,
  nodeType: undefined,
  nodeRatio: undefined,
  applyNode: false,
  buttonList: []
});
const dialog = reactive<DialogOption>({
  visible: false,
  title: '流程办理'
});
//减签弹窗
const deleteSignatureVisible = ref(false);
const form = ref<Record<string, any>>({
  taskId: undefined,
  message: undefined,
  assigneeMap: {},
  variables: {},
  messageType: ['1'],
  flowCopyList: []
});
const backForm = ref<Record<string, any>>({
  taskId: undefined,
  nodeCode: undefined,
  message: undefined,
  variables: {},
  messageType: ['1']
});

const { wf_task_status } = toRefs<any>(proxy?.useDict('wf_task_status'));

const insId = ref(null);
const historyList = ref<Array<any>>([]);
const tabActiveName = ref('image');

// 根据flowStatus获取状态类名
const getStatusClass = (flowStatus: string) => {
  if (!flowStatus || !wf_task_status.value || !Array.isArray(wf_task_status.value)) {
    return {};
  }
  
  const statusItem = wf_task_status.value.find(item => item.value === flowStatus);
  if (statusItem && statusItem.elTagType) {
    // 如果需要返回特定的CSS类，可以根据elTagType返回对应的类名
    return statusItem.elTagType
    // return {
    //   [`status-${statusItem.elTagType}`]: true
    // };
  }
  
  return '';
};

//初始化查询审批记录
const init = async (businessId: string | number) => {
  tabActiveName.value = 'image';
  historyList.value = [];
  insId.value = null;
  flowHisTaskList(businessId).then((resp) => {
    if (resp.data) {
      historyList.value = resp.data.list;
      console.log('historyListhistoryListhistoryListhistoryListhistoryList')
      console.log(historyList.value)
      insId.value = resp.data.instanceId;
      if (historyList.value.length > 0) {
        historyList.value.forEach((item) => {
          if(item.flowStatus == 'waiting') {
            commentsStr.value = `${item.nodeName}意见` 
          }
          if (item.ext) {
            getIds(item.ext).then((res) => {
              item.attachmentList = res.data;
            });
          } else {
            item.attachmentList = [];
          }
        });
      }
      loading.value = false;
    }
  });
};
const getIds = async (ids: string | number) => {
  const res = await listByIds(ids);
  return res;
};

/** 下载按钮操作 */
const handleDownload = (ossId: string) => {
  proxy?.$download.oss(ossId);
};

//打开弹窗
const openDialog = async (id?: string) => {
  // init(id);
  selectCopyUserIds.value = undefined;
  selectCopyUserList.value = [];
  form.value.fileId = undefined;
  taskId.value = id;
  form.value.message = undefined;
  dialog.visible = true;
  loading.value = true;
  buttonDisabled.value = true;
  const response = await getTask(taskId.value);
  task.value = response.data;
  buttonObj.value = {};
  task.value.buttonList.forEach((e) => {
    buttonObj.value[e.code] = e.show;
  });
  buttonDisabled.value = false;
  const data = {
    taskId: taskId.value,
    variables: props.taskVariables
  };
  console.log(data)
  const nextData = await getNextNodeList(data);
  nestNodeList.value = nextData.data;
  loading.value = false;
};

onMounted(() => { });
const emits = defineEmits(['submitCallback', 'cancelCallback']);

/** 办理流程 */
const handleCompleteTask = async () => {
  form.value.taskId = taskId.value;
  form.value.variables = props.taskVariables;
  let verify = false;
  if (buttonObj.value.pop && nestNodeList.value && nestNodeList.value.length > 0) {
    nestNodeList.value.forEach((e) => {
      if (
        Object.keys(form.value.assigneeMap).length === 0 ||
        form.value.assigneeMap[e.nodeCode] === '' ||
        form.value.assigneeMap[e.nodeCode] === null ||
        form.value.assigneeMap[e.nodeCode] === undefined
      ) {
        verify = true;
      }
    });
    if (verify) {
      proxy?.$modal.msgWarning('请选择审批人！');
      return false;
    }
  } else {
    form.value.assigneeMap = {};
  }
  if (selectCopyUserList.value && selectCopyUserList.value.length > 0) {
    const flowCopyList = [];
    selectCopyUserList.value.forEach((e) => {
      const copyUser = {
        userId: e.userId,
        userName: e.nickName
      };
      flowCopyList.push(copyUser);
    });
    form.value.flowCopyList = flowCopyList;
  }
  await proxy?.$modal.confirm('是否确认提交？');
  loading.value = true;
  buttonDisabled.value = true;
  try {
    await completeTask(form.value);
    dialog.visible = false;
    emits('submitCallback');
    proxy?.$modal.msgSuccess('操作成功');
  } finally {
    loading.value = false;
    buttonDisabled.value = false;
  }
};

/** 驳回弹窗打开 */
const handleBackProcessOpen = async () => {
  backForm.value = {};
  backForm.value.messageType = ['1'];
  backVisible.value = true;
  backLoading.value = true;
  backButtonDisabled.value = true;
  const data = await getBackTaskNode(String(task.value.id), task.value.nodeCode);
  taskNodeList.value = data.data;
  backLoading.value = false;
  backButtonDisabled.value = false;
  backForm.value.nodeCode = taskNodeList.value[0].nodeCode;
};
/** 驳回流程 */
const handleBackProcess = async () => {
  backForm.value.taskId = taskId.value;
  await proxy?.$modal.confirm('是否确认驳回到申请人？');
  loading.value = true;
  backLoading.value = true;
  backButtonDisabled.value = true;
  await backProcess(backForm.value).finally(() => {
    loading.value = false;
    buttonDisabled.value = false;
  });
  backVisible.value = false;
  dialog.visible = false;
  backLoading.value = false;
  backButtonDisabled.value = false;
  emits('submitCallback');
  proxy?.$modal.msgSuccess('操作成功');
};
//取消
const cancel = async () => {
  dialog.visible = false;
  buttonDisabled.value = false;
  nickName.value = {};
  form.value.assigneeMap = {};
  emits('cancelCallback');
};
//打开抄送人员
const openUserSelectCopy = () => {
  userSelectCopyRef.value.open();
};
//确认抄送人员
const userSelectCopyCallBack = (data: UserVO[]) => {
  if (data && data.length > 0) {
    selectCopyUserList.value = data;
    selectCopyUserIds.value = selectCopyUserList.value.map((item) => item.userId).join(',');
  }
};
//删除抄送人员
const handleCopyCloseTag = (user: UserVO) => {
  const userId = user.userId;
  // 使用split删除用户
  const index = selectCopyUserList.value.findIndex((item) => item.userId === userId);
  selectCopyUserList.value.splice(index, 1);
  selectCopyUserIds.value = selectCopyUserList.value.map((item) => item.userId).join(',');
};
//加签
const openMultiInstanceUser = async () => {
  multiInstanceUserRef.value.open();
};
//加签
const addMultiInstanceUser = async (data) => {
  if (data && data.length > 0) {
    const taskOperationBo = reactive<TaskOperationBo>({
      userIds: data.map((e) => e.userId),
      taskId: taskId.value,
      message: form.value.message
    });
    await proxy?.$modal.confirm('是否确认提交？');
    loading.value = true;
    buttonDisabled.value = true;
    await taskOperation(taskOperationBo, 'addSignature').finally(() => {
      loading.value = false;
      buttonDisabled.value = false;
    });
    dialog.visible = false;
    emits('submitCallback');
    proxy?.$modal.msgSuccess('操作成功');
  } else {
    proxy?.$modal.msgWarning('请选择用户！');
  }
};
//减签
const deleteMultiInstanceUser = async (row) => {
  await proxy?.$modal.confirm('是否确认提交？');
  loading.value = true;
  buttonDisabled.value = true;
  const taskOperationBo = reactive<TaskOperationBo>({
    userIds: [row.userId],
    taskId: taskId.value,
    message: form.value.message
  });
  await taskOperation(taskOperationBo, 'reductionSignature').finally(() => {
    loading.value = false;
    buttonDisabled.value = false;
  });
  dialog.visible = false;
  emits('submitCallback');
  proxy?.$modal.msgSuccess('操作成功');
};
//打开转办
const openTransferTask = () => {
  transferTaskRef.value.open();
};
//转办
const handleTransferTask = async (data) => {
  if (data && data.length > 0) {
    const taskOperationBo = reactive<TaskOperationBo>({
      userId: data[0].userId,
      taskId: taskId.value,
      message: form.value.message
    });
    await proxy?.$modal.confirm('是否确认提交？');
    loading.value = true;
    buttonDisabled.value = true;
    await taskOperation(taskOperationBo, 'transferTask').finally(() => {
      loading.value = false;
      buttonDisabled.value = false;
    });
    dialog.visible = false;
    emits('submitCallback');
    proxy?.$modal.msgSuccess('操作成功');
  } else {
    proxy?.$modal.msgWarning('请选择用户！');
  }
};

//打开委托
const openDelegateTask = () => {
  delegateTaskRef.value.open();
};
//委托
const handleDelegateTask = async (data) => {
  if (data && data.length > 0) {
    const taskOperationBo = reactive<TaskOperationBo>({
      userId: data[0].userId,
      taskId: taskId.value,
      message: form.value.message
    });
    await proxy?.$modal.confirm('是否确认提交？');
    loading.value = true;
    buttonDisabled.value = true;
    await taskOperation(taskOperationBo, 'delegateTask').finally(() => {
      loading.value = false;
      buttonDisabled.value = false;
    });
    dialog.visible = false;
    emits('submitCallback');
    proxy?.$modal.msgSuccess('操作成功');
  } else {
    proxy?.$modal.msgWarning('请选择用户！');
  }
};
//终止任务
const handleTerminationTask = async () => {
  const params = {
    taskId: taskId.value,
    comment: form.value.message
  };
  await proxy?.$modal.confirm('是否确认终止？');
  loading.value = true;
  buttonDisabled.value = true;
  await terminationTask(params).finally(() => {
    loading.value = false;
    buttonDisabled.value = false;
  });
  dialog.visible = false;
  emits('submitCallback');
  proxy?.$modal.msgSuccess('操作成功');
};
const handleTaskUser = async () => {
  const data = await currentTaskAllUser(taskId.value);
  deleteUserList.value = data.data;
  if (deleteUserList.value && deleteUserList.value.length > 0) {
    deleteUserList.value.forEach((e) => {
      e.nodeName = task.value.nodeName;
    });
  }
  deleteSignatureVisible.value = true;
};
// 选择人员
const choosePeople = async (data) => {
  if (!data.permissionFlag) {
    proxy?.$modal.msgError('没有可选择的人员，请联系管理员！');
  }
  popUserIds.value = data.permissionFlag;
  nodeCode.value = data.nodeCode;
  porUserRef.value.open();
};
//确认选择
const handlePopUser = async (userList) => {
  const userIds = userList.map((item) => {
    return item.userId;
  });
  const nickNames = userList.map((item) => {
    return item.nickName;
  });
  form.value.assigneeMap[nodeCode.value] = userIds.join(',');
  nickName.value[nodeCode.value] = nickNames.join(',');
};

/**
 * 对外暴露子组件方法
 */
defineExpose({
  openDialog,
  init
});
</script>
<style>
.submit-verify-header {
  padding: 0 !important;
  border: none !important;
}

.el-overlay .el-overlay-dialog .el-dialog .el-dialog__body.submit-verify-body {
  padding: 0 !important;

}

.submit-verify-dialog {
  background: linear-gradient(180deg, #E8F0FA, #F1F1F1);
  border-radius: 12px;
}
</style>
<style lang="scss" scoped>
.titleLabel {
  border-left: 3px solid #409eff;
  padding-left: 6px;
  margin-bottom: 8px;
}

.dialog-title {
  height: 22px;
  display: flex;
  align-items: center;
  font-weight: bolder;
  font-size: 22px;
  color: #222222;
  padding-left: 21px;
  margin-bottom: 22px;
}

.content-wrapper {
  display: flex;
  // height: 636px;
  height: calc(100vh - 246px);

  .left-panel {
    height: 100%;
    width: 320px;
    flex-shrink: 0;
    background: #FFFFFF;
    border-radius: 12px;
    .item-box {
      // height: 400px;
      height: calc(100% - 197px);
      overflow: auto;
      padding-top: 4px;
    }
  }

  .left-title {
    height: 17px;
    display: flex;
    align-items: center;
    margin-top: 19px;
    padding-left: 12px;
    font-size: 18px;
    color: #222222;
    margin-bottom: 8px;

    .title-tip {
      width: 14px;
      height: 14px;
      background-color: #409eff;
      position: relative;
      transform: rotate(45deg);
      margin-right: 10px;
    }

    .title-tip::after {
      content: '';
      position: absolute;
      top: 3px;
      left: 3px;
      width: 8px;
      height: 8px;
      background-color: #fff;

    }
    &.primary {
      .title-tip {
        background-color: #e6a23c;
      }
      color: #e6a23c;
    }
  }

  .data-item {
    margin-left: 17px;
    width: 270px;
    margin-bottom: 2px;

    .item-title {
      height: 18px;
      display: flex;
      align-items: center;
      margin-bottom: 1px;

      .circle-icon {
        position: relative;
        width: 18px;
        height: 18px;
        // background: #386CFF;
        border-radius: 50%;
        // border: 4px solid #E3ECFF;
        margin-right: 25px;
        flex-shrink: 0;
        background: #909399;
        border: 4px solid rgb(244, 244, 245);
        &.status-success {
          background: #e6a23c;
          border: 4px solid rgb(253, 246, 236);
          //  background: #67c23a;
          //  border: 4px solid rgb(240, 249, 235);
        }
        // &.status-danger {
        //    background: #f56c6c;
        //    border: 4px solid rgb(254, 240, 240);
        // }
        &.status-warning {
           background: #e6a23c;
           border: 4px solid rgb(253, 246, 236);
        }
        // &.status-info {
        //    background: #909399;
        //    border: 4px solid rgb(244, 244, 245);
        // }
      }

      .title-label {
        font-size: 14px;
        color: #222222;
        display: flex;
        align-items: center;
        justify-content: space-between;
        width: 100%;
        .name-title {
          display: flex;
          align-items: center;
          .el-tag {
            margin-left: 5px;
          }
        }
      }
    }

    .item-content {
      width: 100%;

      .content-box {
        margin-left: 8px;
        padding-left: 35px;
        border-left: 1px solid #E8E8E8;
        width: 100%;
        display: inline-block;

        &.is-last {
          border: none;
        }
        &.status-warning {
          border-left-style: dashed;
        }

        .content-time {
          margin-top: 3px;
          margin-bottom: 4px;
          height: 15px;
          display: flex;
          align-items: center;
          justify-content: space-between;
          font-size: 14px;
          color: #222222;

          .name {
            white-space: nowrap;
            overflow: hidden;
            text-overflow: ellipsis;
          }

          .time {
            font-size: 14px;
            color: #999999;
          }
        }

        .content-message {
          background: #F7F7F7;
          border-radius: 4px;
          width: 100%;
          padding: 0 11px;
          font-size: 14px;
          color: #222222;
          margin-bottom: 2px;
          overflow: auto;
          max-height: 34px;
          line-height: 34px;
          word-break: break-all;
        }
      }
    }
  }

  .right-panel {
    flex-grow: 1;
    display: flex;
    flex-direction: column;
    margin-right: 13px;
    .right-title {
      height: 17px;
      display: flex;
      align-items: center;
      margin-top: 18px;
      padding-left: 12px;
      font-size: 18px;
      color: #222222;
      margin-bottom: 15px;

      .title-tip {
        width: 14px;
        height: 14px;
        background-color: #409eff;
        position: relative;
        transform: rotate(45deg);
        margin-right: 10px;
      }

      .title-tip::after {
        content: '';
        position: absolute;
        top: 3px;
        left: 3px;
        width: 8px;
        height: 8px;
        background-color: #fff;

      }
    }
  }

  .message-box {
    width: 100%;
    background: #FFFFFF;
    border-radius: 6px;
    display: inline-block;
    flex-shrink: 0;


    .message-box-textarea {
      width: 97%;
      :deep(.el-input__wrapper) {
        margin-left: 13px;
        // width: 649px;
        // height: 142px;
        background: #F7F7F7;
        border-radius: 4px;
        border: none;
      }
      :deep(.el-textarea__inner) {
        margin-left: 16px;
        width: 289px;
        height: 92px;
        background: #F7F7F7;
        border-radius: 4px;
        border: none;
      }

    }
  }

  .flow-box {
    width: 100%;
    flex-grow: 1;
    display: flex;
    flex-direction: column;
    background: #FFFFFF;
    border-radius: 6px;

    .flowChart-box {
      height: calc(100% - 91px);
      width: 100%;
    }
  }

}

.btn-icon {
  height: 19px;
  width: 19px;
  margin-right: 6px;
}
.submit-verify-footer {
  display: flex;
  width: 100%;
  justify-content: center;
}
</style>
