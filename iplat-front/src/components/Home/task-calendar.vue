<template>
  <div class="plan-zone">
    <span class="title">
      <div class="title-label">工单日历</div>
      <div class="plan-top">
        <div class="plan-search">
          <img @click="prevMonth" src="@/assets/images/home/fanye_left_icon.png" alt="">
          <el-date-picker class="timePicker" :clearable="false" style="width: 120px;" @change="changeDate"
            v-model="current" type="month" format="YYYY-MM" placeholder="选择月">
          </el-date-picker>
          <img @click="nextMonth" src="@/assets/images/home/fanye_right_icon.png" alt="">
        </div>
      </div>
    </span>

    <div class="btn-group">
      <el-button type="primary" @click="goToCurrentDay">回到今天</el-button>
    </div>
    <!-- <el-calendar v-model="value"> </el-calendar> -->
    <table class="parent-table">
      <thead>
        <th>周一</th>
        <th>周二</th>
        <th>周三</th>
        <th>周四</th>
        <th>周五</th>
        <th>周六</th>
        <th>周日</th>
      </thead>
      <tbody>
        <tr v-for="(week, windex) in weeks" :key="windex">
          <td v-for="(day, dindex) in week"
            :class="{ highlight: isToday(day.date), active: active == new Date(day.date).getTime() }"
            @click="handClick(day)" :key="dindex">
            <el-popover :disabled="!day.data.length" :key="new Date(day.date).getTime()" placement="right" width="555"
              trigger="click">
              <el-table :data="day.data" border class="calendarTable" max-height="400px"
                :header-cell-style="{ textAlign: 'center' }">
                <el-table-column type="index" width="50" align="center" label="序号"></el-table-column>
                <el-table-column min-width="150" property="businessContent" align="left" label="标题"
                  show-overflow-tooltip></el-table-column>
                <el-table-column min-width="70" property="name" label="状态" align="center" show-overflow-tooltip>
                  <template #default>
                    <el-tag type="warning">待审批</el-tag>
                  </template>
                </el-table-column>
                <el-table-column width="60" property="address" label="耗时" align="center" show-overflow-tooltip>
                  <template #default="scope">
                    <span>{{ timeAgo(scope.row.createTime) }}</span>
                  </template>
                </el-table-column>
                <el-table-column width="140" property="createTime" align="center" label="创建时间"
                  show-overflow-tooltip></el-table-column>
                <el-table-column label="操作" fixed="right" align="center" min-width="80">
                  <template #default="scope">
                    <span class="todo-col" @click="routerSkip(scope.row)">
                      <img src="@/assets/images/home/banli_icon.png" style="width: 13px;" alt="">
                      办理
                    </span>
                  </template>
                </el-table-column>
              </el-table>
              <template #reference>
                <div class="content" :class="{ faded: !isCurrentMonth(day.date) }">
                  <div class="top-day">
                    <span class="day">{{ day.date.getDate() }}</span>
                    <span>{{ convertDate(day.date).IDayCn }}</span>
                  </div>
                  <div class="middle-event">
                    <template v-for="(item, itemIndex) in day.data">
                      <div class="middle-item" v-if="itemIndex <= 2" :key="item.taskId"
                        :title="item.processDisplayName">{{
                          item.processDisplayName }}</div>
                    </template>
                    <!-- <div class="middle-item">维保计划审批</div> -->
                  </div>
                  <!-- <div class="bottom-event"></div> -->
                </div>
              </template>

            </el-popover>
          </td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import calendar_converter from 'js-calendar-converter';
import { ElTable } from 'element-plus';

// 定义类型
interface TaskItem {
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

interface DayData {
  date: Date;
  data: TaskItem[];
}

interface CalendarDay {
  date: Date;
  data: TaskItem[];
}

// 定义props
interface Props {
  allData?: TaskItem[];
}

const props = withDefaults(defineProps<Props>(), {
  allData: () => []
});

const router = useRouter();
const current = ref<Date>(new Date());
const today = new Date();
const active = ref<number | string>('');

// 计算属性
const weeks = computed<CalendarDay[][]>(() => {
  return getMonthData(
    current.value.getFullYear(),
    current.value.getMonth() + 1
  );
});

const convertDate = computed(() => {
  return function (dateInput: Date) {
    try {
      return calendar_converter.solar2lunar(
        parseTime(new Date(dateInput), "{y}"),
        parseTime(new Date(dateInput), "{m}"),
        parseTime(new Date(dateInput), "{d}")
      );
    } catch (error) {
      console.error('日期转换错误:', error);
      // alert('日期转换失败，请检查日期是否有效');
      return null;
    }
  }
});

const getDayData = computed(() => {
  return function (time: string | number | Date) {
    if (time) {
      const times = parseTime(time, '{y}-{m}-{d}');
      const data = props.allData.filter(item => parseTime(item.createTime, '{y}-{m}-{d}') == times);
      return data;
    }
    return [];
  }
});

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

// 方法
// 待审批任务跳转
const routerSkip = (data: TaskItem) => {
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

const handClick = (day: DayData) => {
  active.value = new Date(day.date).getTime();
  // if(!this.isCurrentMonth(day.date)){
  //   if(new Date(day.date).getDate() <= 20){
  //     this.nextMonth()
  //   }else {
  //     this.prevMonth()
  //   }
  // }
};

const getCurDate = () => {
  const date = new Date();
  const year = date.getFullYear();
  const month = date.getMonth() + 1; // getMonth() returns a zero-based value (0-11)
  if (month < 10) {
    return year + "-0" + month; // add a leading zero if the month is a single digit
  }
  return year + "-" + month;
};

const isToday = (date: Date) => {
  const today = new Date();
  return (
    date.getDate() === today.getDate() &&
    date.getMonth() === today.getMonth() &&
    date.getFullYear() === today.getFullYear()
  );
};

const goToCurrentDay = () => {
  current.value = new Date(today);
};

const isCurrentMonth = (date: Date) => {
  return date.getMonth() === current.value.getMonth();
};

const prevMonth = () => {
  current.value.setMonth(current.value.getMonth() - 1);
  current.value = new Date(current.value);
  // console.log(this.current.getFullYear(), 'dedede')
};

const nextMonth = () => {
  current.value.setMonth(current.value.getMonth() + 1);
  current.value = new Date(current.value);
};

const changeDate = (val: Date | string) => {
  current.value = new Date(val || today);
};

const goToCurrentMonth = () => {
  current.value = new Date(today);
};

const getMonthData = (year: number, month: number) => {
  const weeks: CalendarDay[][] = [];
  const firstDay = new Date(year, month - 1, 1); // 这个月的第一天
  const lastDayOfCurrentMonth = new Date(year, month, 0); // 这个月的最后一天
  const lastDayOfPrevMonth = new Date(year, month - 1, 0); // 上个月的最后一天

  //这里的0有一个特殊的意义，它可以返回上个月的最后一天。也就是说，如果你想知道某个月有多少天，你可以创建一个日期对象，年份和月份设置为你想知道的月份，日期设置为0，然后调用getDate()方法，返回的就是那个月的天数。
  // new Date(year, month - 1, 0) 最后一个参数，超过当月天数重新排序，比如1月31天，最后一个参数为33返回2
  const startDayOfWeek = firstDay.getDay() === 0 ? 7 : firstDay.getDay(); // 开始是周几
  let dayCount = 1; // 当前日期的变量，初始值为1
  // 上一个月的天数
  let prevMonthDayCount = lastDayOfPrevMonth.getDate() - startDayOfWeek + 2; // 这是为了在日历中填充上个月的日期。
  // console.log(lastDayOfPrevMonth.getDate(), startDayOfWeek, prevMonthDayCount)
  for (let i = 0; i < 6; i++) {
    const week: CalendarDay[] = [];
    for (let j = 0; j < 7; j++) {
      // 日期为上个月的日期，然后将这个日期对象添加到`week`数组中，同时`prevMonthDayCount`加1。
      if (i === 0 && j < startDayOfWeek - 1) {
        const date = new Date(year, month - 2, prevMonthDayCount++);
        week.push({
          date: date,
          data: getDayData.value(date)
        });
        // 日期为下个月的日期，然后将这个日期对象添加到`week`数组中，同时`dayCount`加1
      } else if (dayCount > lastDayOfCurrentMonth.getDate()) {
        const date = new Date(
          year,
          month,
          dayCount++ - lastDayOfCurrentMonth.getDate()
        );
        week.push({
          date: date,
          data: getDayData.value(date)
        });
        // 日期为这个月的日期，然后将这个日期对象添加到`week`数组中，同时`dayCount`加1
      } else {
        const date = new Date(year, month - 1, dayCount++);
        week.push({
          date: date,
          data: getDayData.value(date)
        });
      }
    }
    weeks.push(week);
    // console.log(week)
  }
  return weeks;
};

const parseTime = (time: any, cFormat: string) => {
  if (arguments.length === 0 || !time) {
    return null;
  }
  const format = cFormat || '{y}-{m}-{d} {h}:{i}:{s}';
  let date;
  if (typeof time === 'object') {
    date = time;
  } else {
    if ((typeof time === 'string')) {
      time = time.replace(new RegExp(/-/gm), '/');
    }
    if ((typeof time === 'number') && (time.toString().length === 10)) {
      time = time * 1000;
    }
    date = new Date(time);
  }
  const formatObj: any = {
    y: date.getFullYear(),
    m: date.getMonth() + 1,
    d: date.getDate(),
    h: date.getHours(),
    i: date.getMinutes(),
    s: date.getSeconds(),
    a: date.getDay()
  };
  const time_str = format.replace(/{([ymdhisa])+}/g, (result, key) => {
    const value = formatObj[key];
    // Note: getDay() returns 0 on Sunday
    if (key === 'a') { return ['日', '一', '二', '三', '四', '五', '六'][value]; }
    return value.toString().padStart(2, '0');
  });
  return time_str;
};

onMounted(() => {
  // 组件挂载后执行的逻辑
});
</script>

<style>
.calendarTable th {
  background: #f5f5f5 !important;
  /* 设置表头背景颜色 */
}
</style>

<style scoped>
.faded {
  opacity: 0.3;
}


.plan-zone {
  margin-top: 10px;
}

.plan-zone .title {
  font-weight: bold;
  font-size: 20px;
  color: #222222;
  position: absolute;
  width: calc(100% - 65px);
  top: 18px;
  background: #fff;
}

.plan-zone .plan-top {
  position: absolute;
  width: 80%;
  left: 10%;
  top: 0;
  display: flex;
  justify-content: center;
}

.plan-zone .plan-search {
  display: flex;
  width: 200px;
  justify-content: space-between;
}

.plan-zone .plan-search img {
  width: 24px;
  height: 24px;
  cursor: pointer;
}

.plan-zone .btn-group {
  display: flex;
  justify-content: space-between;
}

.timePicker {
  width: 100px;
}

::v-deep .timePicker input {
  border: none;
  font-size: 16px;
  color: #000;
  padding: 0 20px !important;
}

::v-deep .timePicker .el-input__prefix {
  display: none;
}

.plan-zone .btn-group .right-btn button.new {
  background-color: #fff;
  border: 1px solid #fff;
  color: #409eef;
  position: relative;
}

.plan-zone .btn-group .right-btn button.new::before {
  content: "";
  width: 8px;
  height: 8px;
  border-radius: 50%;
  position: absolute;
  top: 7px;
  left: -3px;
  background-color: #409eef;
}

.plan-zone .btn-group .right-btn button.ing {
  background-color: #fff;
  border: 1px solid #fff;
  color: #ff974a;
  position: relative;
}

.plan-zone .btn-group .right-btn button.ing::before {
  content: "";
  width: 8px;
  height: 8px;
  border-radius: 50%;
  position: absolute;
  top: 7px;
  left: -3px;
  background-color: #ff974a;
}

.plan-zone .btn-group .right-btn button.finish {
  background-color: #fff;
  border: 1px solid #fff;
  color: #3dd599;
  position: relative;
}

.plan-zone .btn-group .right-btn button.finish::before {
  content: "";
  width: 8px;
  height: 8px;
  border-radius: 50%;
  position: absolute;
  top: 7px;
  left: -3px;
  background-color: #3dd599;
}

.parent-table {
  border-collapse: collapse;
  table-layout: fixed;
  width: 100%;
  margin-top: 4px;
}

.parent-table th,
.parent-table td {
  width: 14.4%;
  border: 1px solid #ddd;
}

.highlight {
  border: 2px solid #1890ff !important;
}

.active {
  background: #e8f4ff;
}

.parent-table td {
  padding: 2px 3px;
  vertical-align: top;
}

.parent-table td .content {
  position: relative;
  min-height: 120px;
}

.parent-table td .top-day {
  display: flex;
  justify-content: space-between;
  /* text-align: right; */
  font-size: 15px;
  padding: 6px 8px;
  color: #222222;
}

.parent-table td .top-day .day {
  font-size: 16px;
}

.parent-table td .middle-event .middle-item {
  width: 100%;
  height: 23px;
  background: #E3F0E8;
  margin-bottom: 5px;
  font-size: 13px;
  color: #509269;
  line-height: 24px;
  padding: 0 8px;
  box-sizing: border-box;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  border-left: 2px solid #509269;
}

.table-date {
  display: flex;
}

.table-date>div {
  flex: 1;
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
</style>