<template>
  <div class="content">
    <div class="content-top">
      <div class="content-top-left">
        <div class="banner-div">
          <img src="@/assets/images/home/banner.png" alt="" srcset="">
          <div class="banner-title">
            欢迎您！{{ userName }}
            <!-- <div class="banner-tip">副标题副标题副标题副标题副标题</div> -->
          </div>
        </div>
        <div class="model-div">
          <div class="model-box">
            <div class="model-item" @click="routerSkip('device-file-query')">
              <div class="model-item-left">
                <img class="model-icon" src="@/assets/images/home/yaopindanganguanli.png" alt="">
                设备档案
              </div>
              <div class="model-item-right">
                {{ info.equipmentArchiveCount || 0 }}
              </div>
            </div>
            <div class="model-item" @click="routerSkip('maintenance-year-plan-archive-new')">
              <div class="model-item-left">
                <img class="model-icon" src="@/assets/images/home/weibao.png" alt="">
                维保计划
              </div>
              <div class="model-item-right">
                16
              </div>
            </div>
          </div>
          <div class="model-box">
            <div class="model-item" @click="routerSkip('special-eqm-archives')">
              <div class="model-item-left">
                <img class="model-icon" src="@/assets/images/home/shebeiguanli-2.png" alt="">
                特种设备档案
              </div>
              <div class="model-item-right">
                {{ info.specialEquipmentArchiveCount || 0 }}
              </div>
            </div>
            <div class="model-item" @click="routerSkip('spot-patrol-check-plan')">
              <div class="model-item-left">
                <img class="model-icon" src="@/assets/images/home/xunjianjilu.png" alt="">
                点巡检计划
              </div>
              <div class="model-item-right">
                16
              </div>
            </div>
          </div>
          <div class="model-box">
            <div class="model-item" @click="routerSkip('spare-parts-code')">
              <div class="model-item-left">
                <img class="model-icon" src="@/assets/images/home/icon_beijianguanli.png" alt="">
                备件档案
              </div>
              <div class="model-item-right">
                {{ info.sparePartCount || 0 }}
              </div>
            </div>
            <div class="model-item">
              <div class="model-item-left" @click="routerSkip('failure-report-search')">
                <img class="model-icon" src="@/assets/images/home/guzhang.png" alt="">
                故障维修
              </div>
              <div class="model-item-right">
                16
              </div>
            </div>
          </div>
          <div class="model-box">
            <div class="model-item" @click="routerSkip('facility-estimate-library')">
              <div class="model-item-left">
                <img class="model-icon" src="@/assets/images/home/jiliangdanwei.png" alt="">
                计量设备
              </div>
              <div class="model-item-right">
                {{ info.measureInstrumentCount || 0 }}
              </div>
            </div>
            <div class="model-item" @click="routerSkip('deviation-manage-search')">
              <div class="model-item-left">
                <img class="model-icon" src="@/assets/images/home/piancha.png" alt="">
                偏差任务
              </div>
              <div class="model-item-right">
                16
              </div>
            </div>
          </div>
        </div>
      </div>
      <div class="content-top-right">
        <div class="top-right-item">
          <div class="right-item fontSize" @click="routerSkip('device-file-query')">
            <img class="right-item-icon" src="@/assets/images/home/shebeidangan.png" alt="">
            设备档案
          </div>
          <div class="right-item fontSize" @click="routerSkip('maintenance-year-plan-archive-new')">
            <img class="right-item-icon" src="@/assets/images/home/weibao_icon.png" alt="">
            维保计划
          </div>
          <div class="right-item fontSize" @click="routerSkip('facility-estimate-library')">
            <img class="right-item-icon" src="@/assets/images/home/qiju_icon.png" alt="">
            器具档案
          </div>
          <div class="right-item fontSize" @click="routerSkip('acceptance-order-search')">
            <img class="right-item-icon" src="@/assets/images/home/daohuo_icon.png" alt="">
            到货验收
          </div>
        </div>
        <div class="top-right-item">
          <div class="right-item fontSize" @click="routerSkip('equipment-failure-report')">
            <img class="right-item-icon" src="@/assets/images/home/guzhang_icon.png" alt="">
            故障报修
          </div>
          <div class="right-item fontSize" @click="routerSkip('spot-patrol-check-plan')">
            <img class="right-item-icon" src="@/assets/images/home/dian.png" alt="">
            点巡检
          </div>
          <div class="right-item fontSize" @click="routerSkip('spare-parts-code')">
            <img class="right-item-icon" src="@/assets/images/home/beijian_icon.png" alt="">
            备件管理
          </div>
          <div class="right-item fontSize" @click="routerSkip('deviation-manage-search')">
            <img class="right-item-icon" src="@/assets/images/home/paincha_icon.png" alt="">
            偏差任务
          </div>
        </div>
      </div>
    </div>
    <div class="content-center">
      <div class="statistics-box">
        <Statistics1>设备维保计划</Statistics1>
      </div>
      <div class="statistics-box">
        <Statistics2>设备故障与维修</Statistics2>
      </div>
      <div class="statistics-box">
        <Statistics3>计量校准计划</Statistics3>
      </div>
    </div>
    <div class="content-foot">
      <div class="task-box">
        <ToDo></ToDo>
      </div>
      <div class="task-box">
        <TaskTodo></TaskTodo>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useUserStore } from '@/store/modules/user';
import Statistics1 from '@/components/Home/statistics1.vue';
import Statistics2 from '@/components/Home/statistics2.vue';
import Statistics3 from '@/components/Home/statistics3.vue';
import ToDo from '@/components/Home/todo.vue';
import TaskTodo from '@/components/Home/taskTodo.vue';
import homeStatisticsData from '@/mock/home-statistics.json';

interface StatisticsInfo {
  equipmentArchiveCount?: number;
  specialEquipmentArchiveCount?: number;
  sparePartCount?: number;
  measureInstrumentCount?: number;
  [key: string]: any;
}

const router = useRouter();
const userStore = useUserStore();

const info = ref<StatisticsInfo>({});
const userName = ref(userStore.name);

const getInfo = () => {
  // 注意：这里需要替换为实际的API调用
  // 由于无法找到$Proxy.Statistics.allInOne()的具体实现，暂时使用注释
  // $Proxy.Statistics.allInOne().then(({ data }) => {
  //   info.value = data
  // });

  // 临时模拟数据，实际项目中应替换为真实的API调用
  info.value = homeStatisticsData;
};

// 页面跳转
const routerSkip = (name: string) => {
  router.push({
    name: name,
  });
};

onMounted(() => {
  getInfo();
});
</script>

<style scoped lang="scss">
.content {
  background: #fff;
  padding: 10px 13px;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.content-top {
  flex-shrink: 0;
  display: flex;
}

.content-top-left {
  width: 73%;
  margin-right: 8px;
}

.banner-div {
  height: 92px;
  position: relative;
  overflow: hidden;
  /* 防止图片溢出容器 */
}

.banner-div img {
  width: 100%;
  height: 100%;
  float: right;
  right: 0;
  /* object-fit: cover; */
}

.banner-title {
  position: absolute;
  font-size: 28px;
  font-weight: bold;
  color: #222222;
  margin: 10px 0 0 21px;
}

.banner-tip {
  font-weight: 400;
  font-size: 15px;
  color: #6D6D6D;
  margin-top: 2px;
}

.fontSize {
  font-size: 14px;
  color: #222;
}

.model-div {
  width: 100%;
  display: flex;
  padding: 20px 0;
  border-radius: 10px;
  border: 1px solid #ECECEC;
  margin-top: 9px;
}

.model-box {
  width: 100%;
  height: 60px;
  display: flex;
  flex-direction: column;
}

.model-box:not(:last-child) {
  border-right: 1px solid #E5E5E5;
}

.model-item {
  display: flex;
  justify-content: space-between;
  padding: 0 10%;
  align-items: center;
  flex: 1;
  cursor: pointer;
}

.model-box .model-item:last-child {
  margin-top: 4px;
}

.model-item-left {
  color: #666;
  font-size: 16px;
  display: flex;
  align-items: center;
}

.model-item-right {
  color: #222;
  font-size: 16px;
  font-weight: bold;
}

.model-icon {
  width: 19px;
  margin-right: 8px;
}

.content-top-right {
  flex-grow: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.top-right-item {
  display: flex;
  justify-content: space-between;
}

.right-item {
  width: 23%;
  height: 92px;
  padding: 15px 10px;
  background: #F5F6F7;
  border-radius: 11px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  cursor: pointer;
}

.right-item-icon {
  width: 40%;
  margin-bottom: 10px;
}

.content-center {
  flex-shrink: 0;
  margin-top: 15px;
  display: flex;
  justify-content: space-between;
}

.statistics-box {
  width: 32.9%;
  height: 284px;
  border-radius: 10px;
  border: 1px solid #ECECEC;
}

.content-foot {
  flex-grow: 1;
  margin-top: 15px;
  display: flex;
  justify-content: space-between;
}

.task-box {
  width: 49.5%;
  height: 100%;
  border-radius: 10px;
  border: 1px solid #ECECEC;
  margin-bottom: 20px;
}

@media screen and (max-width: 1330px) {
  .model-item-left {
    font-size: 13px;
  }

  .fontSize {
    font-size: 13px;
  }
}

@media screen and (max-width: 1223px) {
  .model-item-left {
    font-size: 12px;
  }
}

@media screen and (max-width: 1190px) {
  .model-item-left {
    font-size: 11px;
  }

  .model-icon {
    width: 15px;
  }

  .fontSize {
    font-size: 11px;
  }
}
</style>