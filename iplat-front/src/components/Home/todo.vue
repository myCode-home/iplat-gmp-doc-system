<template>
  <div class="todo-content">
    <div class="todo-top">
      <div class="todo-title">
        待办事项
      </div>
      <div class="todo-title-right" @click="viewMore">
        查看更多 <img class="right-icon" src="@/assets/images/home/jiantou-zuo_icon.png" alt="">
      </div>
    </div>
    <div class="todo-box" ref="todoBoxRef">
      <div :class="{ 'todo-btn': true, active: processId == item.processId }" v-for="item in btnList"
        :key="item.processId" @click="processId = item.processId">{{ item.name }}</div>
    </div>
    <div class="todo-table">
      <div class="table-box">
        <el-table :data="tableData" height="100%" border :header-cell-style="{ textAlign: 'center' }"
          style="width: 100%">
          <el-table-column prop="businessContent" align="left" label="名称" show-overflow-tooltip> </el-table-column>
          <el-table-column prop="taskDisplayName" width="100" align="center" label="状态" show-overflow-tooltip>
          </el-table-column>
          <el-table-column prop="createTime" align="center" width="70" label="耗时" show-overflow-tooltip>
            <template #default="scope">
              <span>{{ timeAgo(scope.row.createTime) }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" align="center" width="70" label="期限" show-overflow-tooltip>
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
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, nextTick, watch, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { store } from '@/store';
import todoTaskData from '@/mock/todo-task-data.json';

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
  [key: string]: any;
}

interface PendingDataItem {
  processId: string;
  taskInstanceList: TableDataItem[];
}

// 响应式数据
const router = useRouter();
const tableData = ref<TableDataItem[]>([]);
const processId = ref<string>('all');
const btnList = ref<TaskItem[]>([
  {
    processId: 'all',
    name: '全部',
  },
  {
    processId: 'ee85c08974ae4b48adde2ee6079528ec',
    name: '设备维护',
  },
  {
    processId: '35908a480c7c493187eb1fa684e7aa4f',
    name: '维保计划',
  },
  {
    processId: '3494f95517c9474684f35a1adcf5018f',
    name: '点巡检计划',
  },
  {
    processId: '19bb161f8e1d475f912474a732c54e68',
    name: '备件入库',
  },
  {
    processId: '55e945fe3c28485bbd0a903df70b2216',
    name: '备件出库',
  },
  {
    processId: '25461ccd4b824041af0e0ccdc87937bb',
    name: '校准计划',
  },
  {
    processId: '7044a516ca894800a5588b0bc7ab7f23',
    name: '校验计划',
  }
]);
const pendingData = ref<PendingDataItem[]>([]);
const maxHeight = ref<string>('');
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
      taskInstanceList: todoTaskData.todoTasks
    }
  ];

  tableData.value = pendingData.value[0].taskInstanceList;
};

const resetHeight = () => {
  nextTick(() => {
    if (todoBoxRef.value) {
      maxHeight.value = `${345 - todoBoxRef.value.clientHeight - 60}px`;
    }
  });
};

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

const viewMore = () => {
  // 查看更多功能实现
  console.log('view more clicked');
};

// 生命周期
onMounted(() => {
  getList();
  resetHeight();
});
</script>

<style scoped lang="scss">
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
  font-size: 14px;
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