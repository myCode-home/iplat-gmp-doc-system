<template>
  <div class="chart-content">
    <div class="chart-top">
      <span class="chart-title">
        <img src="@/assets/images/home/biaoti.png" alt="">
        <slot></slot>
      </span>
      <div>
        <span class="centerText">年度：</span>
        <el-date-picker style="width: 100px" v-model="currentYear" type="year" placeholder="选择年" @change="selectYear">
        </el-date-picker>
      </div>
    </div>
    <div class="chart" ref="chartRef" />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount, nextTick } from 'vue';
import * as echarts from 'echarts'
import statisticsChartData from '@/mock/statistics-chart-data.json';

interface ChartDataItem {
  planCount: number;
  completeCount: number;
}

const animationDuration = 6000

// 定义props
interface Props {
  className?: string;
  width?: string;
  height?: string;
}

const props = withDefaults(defineProps<Props>(), {
  className: 'chart',
  width: '100%',
  height: '300px'
});

// 响应式数据
const chartRef = ref<HTMLElement | null>(null);
let chart: echarts.ECharts | null = null;
const orderNum = ref<number[]>([]);
const planNum = ref<number[]>([]);
// 当前年
const currentYear = ref<string>('');
// 开始时间
const startDate = ref<string>('');
// 结束时间
const endDate = ref<string>('');

// 节流定时器
let resizeHandler: number | null = null;

// 监听窗口大小变化
const handleResize = () => {
  if (chart) {
    chart.resize();
  }
};

// 节流函数 - 避免频繁触发
const throttle = (func: () => void, delay: number) => {
  if (resizeHandler) {
    window.clearTimeout(resizeHandler);
  }
  resizeHandler = window.setTimeout(() => {
    func();
  }, delay);
};

// 选择年
const selectYear = (val?: string) => {
  currentYear.value = val || '';
  const date = new Date();
  let year = '';
  if (val) {
    year = val;
  } else {
    year = date.getFullYear().toString();
  }
  currentYear.value = year;
  startDate.value = year + '-01-01';
  endDate.value = year + '-12-31';
  getMaintenanceData();
};

// 获取预维护统计数据
const getMaintenanceData = () => {
  // 注意：这里需要替换为实际的API调用
  // 由于无法找到$Proxy.Statistics.calibration()的具体实现，暂时使用注释
  // $Proxy.Statistics.calibration([startDate.value, endDate.value]).then(({ data }) => {
  //   if (!data) return;
  //   data.forEach(item => {
  //     planNum.value.push(item.planCount);
  //     orderNum.value.push(item.completeCount);
  //   });
  //   nextTick(() => {
  //     initChart();
  //   });
  // });
  
  // 临时模拟数据
  planNum.value = statisticsChartData.statistics3.planNum;
  orderNum.value = statisticsChartData.statistics3.orderNum;
  nextTick(() => {
    initChart();
  });
};

const initChart = () => {
  if (!chartRef.value) return;
  chart = echarts.init(chartRef.value, 'macarons');
  chart.setOption({
    color: ['#63CB8C', '#7F99EF'],
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow',
        crossStyle: {
          color: '#999'
        }
      }
    },
    legend: {
      x: 'center',
      y: 'bottom',
      data: [{ icon: 'circle', name: '完成工单数量' }, { icon: 'circle', name: '计划工单数量' }]
    },
    grid: {
      top: 10,
      left: '2%',
      right: '2%',
      bottom: 25,
      containLabel: true
    },
    xAxis: [{
      type: 'category',
      data: ['1', '2', '3', '4', '5', '6', '7', '8', '9', '10', '11', '12'],
      axisTick: {
        alignWithLabel: true
      }
    }],
    yAxis: [{
      type: 'value',
      axisLine: {
        show: false
      },
      axisTick: {
        show: false
      },
      splitLine: {
        show: true, // 显示分割线
        lineStyle: {
          color: '#CDD3E2', // 分割线颜色
          type: 'dashed', // 设置分割线为虚线
          width: 1, // 分割线宽度
        }
      }
    }],
    series: [{
      name: '完成工单数量',
      type: 'bar',
      data: orderNum.value,
      itemStyle: {
        normal: {
          label: {
            show: true,
            position: 'top'
          }
        }
      },
      animationDuration
    }, {
      name: '计划工单数量',
      type: 'bar',
      data: planNum.value,
      itemStyle: {
        normal: {
          label: {
            show: true,
            position: 'top'
          }
        }
      },
      animationDuration
    }]
  });

  // 监听窗口大小变化事件，使用节流处理
  window.addEventListener('resize', () => {
    throttle(handleResize, 100);
  });
};

// 组件挂载后执行
onMounted(() => {
  selectYear();
});

// 组件卸载前清理
onBeforeUnmount(() => {
  if (chart) {
    chart.dispose();
    chart = null;
  }

  // 移除事件监听器
  if (resizeHandler) {
    window.clearTimeout(resizeHandler);
    resizeHandler = null;
  }
  window.removeEventListener('resize', handleResize);
});
</script>

<style scoped>
.chart-content {
  width: 100%;
  height: 100%;
  padding: 10px 13px;
  box-sizing: border-box;
}

.chart-top {
  display: flex;
  justify-content: space-between;
}

.chart-title {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
}

.chart-title img {
  width: 17px;
  margin-right: 2px;
}

.chart {
  width: 100%;
  height: 230px;
}
</style>