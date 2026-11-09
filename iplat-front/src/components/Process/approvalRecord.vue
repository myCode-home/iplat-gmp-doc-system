<template>
  <el-dialog v-model="visible" draggable title="" :width="props.width" :height="props.height" :close-on-click-modal="false"
  class="submit-verify-dialog" header-class="submit-verify-header" body-class="submit-verify-body">
    <div class="dialog-title">审批记录</div>
    <div class="content-wrapper">
      <div class="right-panel">
        <div class="flow-box">
          <div class="right-title">
            <div class="title-tip"></div>
            流程图
          </div>
          <div class="flowChart-box">
            <flowChart :ins-id="insId" v-if="insId" />
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

      </div>
    </div>
  </el-dialog>
</template>
<script setup lang="ts">
import { flowHisTaskList } from '@/api/workflow/instance';
import { propTypes } from '@/utils/propTypes';
import { listByIds } from '@/api/system/oss';
import FlowChart from '@/components/Process/flowChart.vue';
const { proxy } = getCurrentInstance() as ComponentInternalInstance;
const { wf_task_status } = toRefs<any>(proxy?.useDict('wf_task_status'));
const props = defineProps({
  width: propTypes.string.def('90%'),
  height: propTypes.string.def('100%')
});
const loading = ref(false);
const visible = ref(false);
const historyList = ref<Array<any>>([]);
const tabActiveName = ref('image');
const insId = ref(null);

//初始化查询审批记录
const init = async (businessId: string | number) => {
  visible.value = true;
  loading.value = true;
  tabActiveName.value = 'image';
  historyList.value = [];
  insId.value = null
  flowHisTaskList(businessId).then((resp) => {
    if (resp.data) {
      historyList.value = resp.data.list;
      insId.value = resp.data.instanceId;
      if (historyList.value.length > 0) {
        historyList.value.forEach((item) => {
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

/**
 * 对外暴露子组件方法
 */
defineExpose({
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
  height: 636px;

  .left-panel {
    height: 100%;
    width: 370px;
    flex-shrink: 0;
    background: #FFFFFF;
    border-radius: 12px;
    overflow: auto;
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
  }

  .data-item {
    margin-left: 17px;
    width: 320px;
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
          padding: 14px 11px;
          font-size: 14px;
          color: #222222;
          margin-bottom: 2px;
          overflow: auto;
          max-height: 48px;
        }
      }
    }
  }

  .right-panel {
    flex-grow: 1;
    display: flex;
    flex: 1;
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
    margin-bottom: 13px;

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
      position: relative;
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