<template>
  <div class="todo-content">
    <div class="todo-top">
      <div class="todo-title">
        工单任务
      </div>
      <div @click="open = true" class="todo-title-right">
        <img class="right-icon" style="width: 19px;margin-right: 6px;transform: translate(0px, -1px);"
          src="@/assets/images/home/rili.png" alt="">
        工单日历 <img class="right-icon" src="@/assets/images/home/jiantou-zuo_icon.png" alt="">
      </div>
    </div>
    <div class="todo-box" ref="todoBoxRef">
      <div :class="{ 'todo-btn': true, active: processId == item.processId }" v-for="item in btnList"
        :key="item.processId" @click="processId = item.processId">{{ item.name }}</div>
    </div>
    <div class="todo-table">
      <div class="table-box">
        <el-table :data="tableData" height="100%" :header-cell-style="{ textAlign: 'center' }" style="width: 100%">
          <el-table-column prop="businessContent" align="left" label="名称" show-overflow-tooltip> </el-table-column>
          <el-table-column prop="taskDisplayName" width="100" align="center" label="状态" show-overflow-tooltip>
          </el-table-column>
          <el-table-column prop="createTime" align="center" width="70" label="耗时" show-overflow-tooltip>
            <template #default="scope">
              <span>{{ timeAgo(scope.row.createTime) }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" align="center" width="50" label="期限" show-overflow-tooltip>
            <template #default="scope">
              <span :style="{ color: getColor(scope.row) }">{{ scope.row.deadline ? (scope.row.deadline + '天') : ''
              }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" align="center" width="140" label="创建时间"
            show-overflow-tooltip></el-table-column>
          <el-table-column label="操作" fixed="right" align="center" width="80">
            <template #default="scope">
              <span class="todo-col" @click="routerSkip(scope.row)">
                <img src="@/assets/images/home/banli_icon.png" style="width: 13px;" alt="">
                办理
              </span>
            </template>
          </el-table-column>
        </el-table>
      </div>

    </div>
    <el-dialog v-model="open" title="" width="80%" top="10vh" class="taskTdoDialog noBorderBottom">
      <TaskCalendar :all-data="allData"></TaskCalendar>
      <template #footer>
        <span class="dialog-footer">
          <el-button type="primary" @click="open = false">关 闭</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, nextTick, watch, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { store } from '@/store';
import todoTaskData from '@/mock/todo-task-data.json';
import TaskCalendar from './task-calendar.vue';
import { ElTable } from 'element-plus';

// 定义类型
interface TaskItem {
  processId: string;
  name: string;
}

interface TableDataItem {
  businessContent: string;
  taskDisplayName: string;
  createTime: string;
  deadline?: number;
  businessLink: string;
  busiId: string;
  taskId: string;
  processDisplayName: string;
  [key: string]: any;
}

interface PendingDataItem {
  processId: string;
  taskInstanceList: TableDataItem[];
}

const router = useRouter();
const tableData = ref<TableDataItem[]>([]);
const processId = ref<string>('all');
const btnList = ref<TaskItem[]>([
  {
    processId: 'all',
    name: '全部',
  },
  {
    processId: 'bd341db0ecf4479384ffe843631b50be',
    name: '维修工单',
  },
  {
    processId: '6b2a8e3ee32a4e7da6c1a5ce6f92b74f',
    name: '维保工单',
  },
  {
    processId: '3b431aa2cd564929be40f96fe9d2c42c',
    name: '故障报修单',
  },
  {
    processId: '798553859fed40d094a0e39c070e6b85',
    name: '设备报废单',
  },
  {
    processId: 'cfa6780a9a4046a4a970a86866f83ba5',
    name: '偏差任务',
  }
]);
const pendingData = ref<PendingDataItem[]>([]);
const allData = ref<TableDataItem[]>([]);
const maxHeight = ref<string>('');
const open = ref<boolean>(false);
const todoBoxRef = ref<HTMLElement | null>(null);

// 计算属性
const timeAgo = computed(() => {
  return function (timestamp: string) {
    const now = Date.now();
    const diff = now - new Date(timestamp).getTime();
    // 小于1分钟显示秒
    if (diff < 60000) {
      const seconds = Math.floor(diff / 1000);
      return `${seconds}秒`;
    }
    // 小于1小时显示分钟
    if (diff < 3600000) {
      const minutes = Math.floor(diff / 60000);
      return `${minutes}分钟`;
    }
    // 小于24小时显示小时
    if (diff < 86400000) {
      const hours = Math.floor(diff / 3600000);
      return `${hours}小时`;
    }
    // 显示天数（最多显示到天）
    const days = Math.floor(diff / 86400000);
    return `${days}天`;
  }
});

const getColor = computed(() => {
  return function (row: TableDataItem) {
    if (row.deadline) {
      const nowTim = new Date().getTime();
      const createdTim = new Date(row.createTime).getTime();
      if (nowTim - createdTim > 86400000 * row.deadline) {
        return '#f56c6c'
      } else if (nowTim - createdTim <= 86400000 * 2) {
        return '#e6a23c'
      }
    }
    return '';
  }
});



watch(processId, (val) => {
  if ('all' == val) {
    const processIds = btnList.value.map(item => item.processId)
    let tableDataTemp: TableDataItem[] = []
    pendingData.value.filter(item => processIds.indexOf(item.processId) >= 0).forEach(item =>
      tableDataTemp = tableDataTemp.concat(item.taskInstanceList)
    );
    tableData.value = tableDataTemp.map((item, index) => {
      if (index == 0 || index == 1 || index % 3 == 0 || index % 5 == 0) {
        item.deadline = 3;
      }
      return item;
    })
  } else {
    tableData.value = pendingData.value.find(item => item.processId == val)!.taskInstanceList.map((item, index) => {
      if (index == 0 || index == 1 || index % 3 == 0 || index % 5 == 0) {
        item.deadline = 3;
      }
      return item;
    });
  }
});

// 方法
const getList = () => {
  // $Proxy.Task.moduleTask(store.state.systemId, {}).then(res => {
  //   pendingData.value = res.data.data;
  //   const processIds = btnList.value.map(item=>item.processId)
  //   let tableDataTemp: TableDataItem[] = []
  //   pendingData.value.filter(item => processIds.indexOf(item.processId)>=0).forEach(item => 
  //     tableDataTemp = tableDataTemp.concat(item.taskInstanceList)
  //   );
  //   tableData.value = tableDataTemp.map((item, index) => {
  //     if(index == '0' || index == '1' || index%3 == 0 || index%5 == 0){
  //       item.deadline = 3;
  //     }
  //     return item;
  //   })
  // });
  
  // 临时模拟数据
  pendingData.value = [
    {
      processId: 'all',
      taskInstanceList: todoTaskData.taskTodoTasks
    }
  ];
  
  allData.value = tableData.value = pendingData.value[0].taskInstanceList;
};

// const resetHeight = () => {
//   nextTick(() => {
//     if (todoBoxRef.value) {
//       maxHeight.value = `${345 - todoBoxRef.value.clientHeight - 60}px`;
//     }
//   });
// };

// 待审批任务跳转
const routerSkip = (data: TableDataItem) => {
  if (Object.keys(data).length) {
    router.push({
      name: data.businessLink,
      params: {
        busiId: data.busiId,
        flag: 'agent'
      }
    });
  }
};

onMounted(() => {
  getList();
  // resetHeight();
});
</script>

<style>
.taskTdoDialog .el-dialog {
  border-radius: 10px;
}

.taskTdoDialog .el-dialog__body {
  padding-top: 20px;
}
</style>

<style scoped>
.todo-content {
  width: 100%;
  min-height: 350px;
  padding: 13px 15px;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
}

.todo-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
  flex-shrink: 0;
}

.todo-title {
  font-size: 16px;
  color: #222222;
  font-weight: bold;
}

.todo-title-right {
  display: flex;
  align-items: center;
  font-size: 14px;
  color: #141B55;
  cursor: pointer;
}

.right-icon {
  width: 5px;
  margin-left: 10px;
}

.todo-box {
  /* display: flex;
  flex-wrap: wrap;
  justify-content: space-between; */
  flex-shrink: 0;
}

.todo-btn {
  display: inline-block;
  margin-right: 15px;
  padding: 3px 11px;
  background: #FFFFFF;
  border-radius: 14px;
  border: 1px solid #9A9A9A;
  margin-bottom: 9px;
  font-size: 13px;
  cursor: pointer;
}

.todo-btn.active {
  background: #F1F9F4;
  color: #509269;
  border: 1px solid #509269;
}

.todo-table {
  width: 100%;
  flex-grow: 1;
  position: relative;

  .table-box {
    position: absolute;
    height: 100%;
    width: 100%;
    top: 0;
    left: 0;
  }
}

.todo-col {
  display: flex;
  align-items: center;
  justify-content: center;
  color: #509269;
  cursor: pointer;
}

.todo-col img {
  margin-right: 3px;
}

::v-deep .el-table .warning-row {
  background: #fdf6ec;
}

::v-deep .el-table .danger-row {
  background: #fef0f0;
}
</style>