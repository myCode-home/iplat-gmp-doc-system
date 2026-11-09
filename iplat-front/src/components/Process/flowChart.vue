<template>
  <div style="width: 100%; height: 100%;position: relative;">
    <div class="top-text" v-if="defJson.topTextShow">{{defJson.topText}}</div>
    <el-header :style="headerStyle">
      <div style="padding: 5px 0; display: flex; align-items: center;">
        <!-- 左侧按钮组 -->
        <div style="display: flex; align-items: center; margin-left: 50px;">
          <el-button size="small" :style="`border: 1px solid rgb(${statusColors.notDone})`">未完成</el-button>
          <el-button size="small" :style="`background-color: rgb(${statusColors.todo}, 0.15); border: 1px solid rgb(${statusColors.todo})`">进行中</el-button>
          <el-button size="small" :style="`background-color: rgb(${statusColors.done}, 0.15); border: 1px solid rgb(${statusColors.done})`">已完成</el-button>
        </div>

        <!-- 右侧状态按钮组 -->
        <div style="margin-left: auto; display: flex; align-items: center;">
          <el-button size="small" icon="ZoomIn" @click="zoomViewport(true)">放大</el-button>
          <el-button size="small" icon="Rank" v-if="isClassics(defJson.modelValue)" @click="zoomViewport(2)">自适应</el-button>
          <el-button size="small" icon="ZoomOut" @click="zoomViewport(false)">缩小</el-button>
          <el-button size="small" icon="Download" @click="downLoad">下载流程图</el-button>
        </div>
      </div>
    </el-header>
    <div class="containerView" ref="containerRef"></div>

    <div
        v-if="visible"
        :style="{ left: tooltipPosition.x + 'px', top: tooltipPosition.y + 'px' }"
        ref="tooltipContainerRef">
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch, computed, render, h, nextTick } from "vue";
import LogicFlow from "@logicflow/core";
import { Snapshot } from "@logicflow/extension";
import "@logicflow/core/lib/style/index.css";
import StartC from "@/components/warmFlow/design/classics/js/start";
import BetweenC from "@/components/warmFlow/design/classics/js/between";
import SerialC from "@/components/warmFlow/design/classics/js/serial";
import ParallelC from "@/components/warmFlow/design/classics/js/parallel";
import InclusiveC from "@/components/warmFlow/design/classics/js/inclusive";
import EndC from "@/components/warmFlow/design/classics/js/end";
import SkipC from "@/components/warmFlow/design/classics/js/skip";
import StartM from "@/components/warmFlow/design/mimic/js/start";
import BetweenM from "@/components/warmFlow/design/mimic/js/between";
import SerialM from "@/components/warmFlow/design/mimic/js/serial";
import ParallelM from "@/components/warmFlow/design/mimic/js/parallel";
import InclusiveM from "@/components/warmFlow/design/mimic/js/inclusive";
import EndM from "@/components/warmFlow/design/mimic/js/end";
import SkipM from "@/components/warmFlow/design/mimic/js/skip";
// import useWarmFlowStore from "@/store/modules/warmFlow";
import {isClassics, json2LogicFlowJson} from "@/components/warmFlow/design/common/js/tool";
import { queryFlowChart } from "@/api/warmFlow/flow/definition";

const props = defineProps({
  insId: {
    type: [String, Number],
    default: null
  },
  noBoxShadow: {
    type: Boolean,
    default: false
  },
});

// const appStore = useWarmFlowStore();
// const appParams = computed(() => useWarmFlowStore().appParams);
const instanceId = ref(null);
const defJson = ref({});
const containerRef = ref(null);
const tooltipPosition = ref({ x: 0, y: 0 });
const tooltipContainerRef = ref(null);
const visible = ref(false);

const promptContent = ref({
  dialogStyle: {},
  info: []
});

const statusColors = ref({
  done: "",
  todo: "",
  notDone: ""
});

const isDark = ref(false);
const headerStyle = computed(() => {
  return {
    top: "5px",
    right: "50px",
    zIndex: "2",
    height: "auto",
    backgroundColor: isDark.value ? "#333" : "#fff"
  };
});

const lf = ref(null);

// 添加自适应缩放函数
const fitView = () => {
  if (lf.value) {
    nextTick(() => {
      // 使用LogicFlow自带的fitView功能，这会自动缩放到合适大小以显示全部内容
      try {
        lf.value.fitView();
        // 在fitView后再稍微缩小一点，确保有边距
        lf.value.zoom(-0.1, { x: 0, y: 0 }); // 轻微缩小一点以增加边距
      } catch (error) {
        console.warn('LogicFlow fitView failed, using manual calculation:', error);
        
        // 如果fitView失败，使用手动计算的方式
        const container = containerRef.value;
        if (container) {
          const containerWidth = container.clientWidth;
          const containerHeight = container.clientHeight;
          
          const graphModel = lf.value.graphModel;
          const elements = graphModel.nodes.concat(graphModel.edges);
          
          if (elements.length > 0) {
            let minX = Infinity, minY = Infinity;
            let maxX = -Infinity, maxY = -Infinity;
            
            elements.forEach(element => {
              if (element.x !== undefined && element.y !== undefined) {
                if (element.width && element.height) {
                  minX = Math.min(minX, element.x - element.width / 2);
                  minY = Math.min(minY, element.y - element.height / 2);
                  maxX = Math.max(maxX, element.x + element.width / 2);
                  maxY = Math.max(maxY, element.y + element.height / 2);
                } else {
                  minX = Math.min(minX, element.x);
                  minY = Math.min(minY, element.y);
                  maxX = Math.max(maxX, element.x);
                  maxY = Math.max(maxY, element.y);
                }
              }
            });
            
            const contentWidth = maxX - minX;
            const contentHeight = maxY - minY;
            
            const scaleX = (containerWidth * 0.8) / contentWidth;
            const scaleY = (containerHeight * 0.8) / contentHeight;
            const scale = Math.min(scaleX, scaleY, 1);
            
            lf.value.setZoomScale(scale);
            lf.value.translateCenter();
          }
        }
      }
    });
  }
};

const use = () => {
  LogicFlow.use(Snapshot);
};

const register = () => {
  if (isClassics(defJson.value.modelValue)) {
    lf.value.register(StartC);
    lf.value.register(BetweenC);
    lf.value.register(SerialC);
    lf.value.register(ParallelC);
    lf.value.register(InclusiveC);
    lf.value.register(EndC);
    lf.value.register(SkipC);
  } else {
    lf.value.register(StartM);
    lf.value.register(BetweenM);
    lf.value.register(SerialM);
    lf.value.register(ParallelM);
    lf.value.register(InclusiveM);
    lf.value.register(EndM);
    lf.value.register(SkipM);
  }
};

const initEvent = () => {
  const { eventCenter } = lf.value.graphModel;
  eventCenter.on('node:click', (data) => {
    const promptArr = data.data.properties.promptContent;
    if (promptArr) {
      visible.value = true;

      nextTick(() => {
        if (tooltipContainerRef.value) {
          // 构建 HTML 内容
          promptContent.value = data.data.properties.promptContent
          
          // 获取节点对应的DOM元素
          const nodeElement = document.querySelector(`[data-id="${data.data.id}"]`);
          
          if (nodeElement) {
            // 获取节点元素在视口中的位置
            const nodeRect = nodeElement.getBoundingClientRect();
            
            // 计算tooltip位置，放在节点右下角
            const tooltipX = nodeRect.right + 5; // 节点右边界的右侧5px
            const tooltipY = nodeRect.bottom + 5; // 节点底边界的下方5px
            
            // 确保tooltip不会超出视窗边界
            const tooltipWidth = tooltipContainerRef.value.offsetWidth || 200;
            const tooltipHeight = tooltipContainerRef.value.offsetHeight || 100;
            const viewportWidth = window.innerWidth;
            const viewportHeight = window.innerHeight;
            
            let finalX = tooltipX;
            let finalY = tooltipY;
            
            if (tooltipX + tooltipWidth > viewportWidth) {
              finalX = Math.max(tooltipX - tooltipWidth - 10, 10); // 如果右边超出，则显示在节点左边
            }
            
            if (tooltipY + tooltipHeight > viewportHeight) {
              finalY = Math.max(tooltipY - tooltipHeight - 10, 10); // 如果底部超出，则显示在节点上方
            }
            
            tooltipPosition.value = { x: finalX, y: finalY };
          }
        }
      });
    }
  });

  eventCenter.on('blank:click', () => {
    visible.value = false;
  });
};

// 监听 promptContent 变化并动态渲染
watch(
    () => promptContent.value,
    (contentData) => {
      if (!tooltipContainerRef.value) return;
      if (!contentData) return;

      // 更新 tooltipContainerRef 的样式
      Object.entries(contentData.dialogStyle || {}).forEach(([key, value]) => {
        tooltipContainerRef.value.style[key] = value;
      });

      // 生成 <p> 元素数组
      const children = contentData.info.map((item) =>
          h("p", {
            style: item.rowStyle || {},
          }, [
            h("span", {
              style: item.prefixStyle || {}
            }, item.prefix),
            h("span", {
              style: item.contentStyle || {}
            }, item.content)
          ])
      );

      // 调用 render 方法
      const wrapper = h("div", children);
      render(wrapper, tooltipContainerRef.value);
    },
    { deep: true, immediate: true }
);

const zoomViewport = async (zoom) => {
  lf.value.zoom(zoom);
  lf.value.translateCenter();
  if(zoom == '2'){
    fitView();
  }
};

onMounted(async () => {
  // if (!appParams.value) await appStore.fetchTokenName();
  instanceId.value =  props.insId // appParams.value.id;

  if (instanceId.value) {
    queryFlowChart(instanceId.value)
        .then((res) => {
          defJson.value = res.data;

          if (defJson.value) {
            if(defJson.value.topText && defJson.value.topText.indexOf(':') >= 0) {
              defJson.value.topText = `${defJson.value.topText.split(':')[1]}流程`;
            }
            [
              statusColors.value.done,
              statusColors.value.todo,
              statusColors.value.notDone,
            ]  = (res.data.chartStatusColor &&
                res.data.chartStatusColor.length === 3) ? res.data.chartStatusColor :
                ["82,196,26", "255,173,20", isClassics(defJson.value.modelValue) ? "0,0,0" : "144,147,153"]; // 提供默认值

            const data = json2LogicFlowJson(defJson.value);
            // document.body.style.overflow = 'hidden';
            use();
            lf.value = new LogicFlow({
              container: containerRef.value,
              plugins: [Snapshot],
              isSilentMode: true,
              textEdit: false,
              hoverOutline: isClassics(defJson.value.modelValue),   // 鼠标 hover 的时候是否显示节点的外框。
              nodeSelectedOutline: isClassics(defJson.value.modelValue),    // 节点被选中时是否显示节点的外框。
              edgeSelectedOutline: isClassics(defJson.value.modelValue),    //	边被选中时是否显示边的外框。
              grid: {
                size: 20,
                visible: false, // 'true' === appParams.value.showGrid
                type: 'dot',
                config: {
                  color: "#ccc",
                  thickness: 1,
                },
              },
              background: {
                backgroundColor: "#fff",
              },
            });
            register();
            initEvent();
            lf.value.render(data);
            if (isClassics(defJson.value.modelValue)) {
              lf.value.translateCenter();
            }
            
            // 数据渲染完成后执行自适应缩放
            nextTick(() => {
              setTimeout(() => {
                fitView();
              }, 100);
            });
          }
        })
        .catch(() => {
          lf.value.render({});
        });
  }

  // 监听窗口大小变化，自动调整流程图大小
  window.addEventListener('resize', fitView);
});

watch(isDark, (v) => {
  if (!lf.value) return;
  lf.value.graphModel.background = {
    background: v ? "#333" : "#fff"
  };
});

/**
 * 下载流程图
 */
function downLoad() {
  lf.value.getSnapshot(defJson.value.flowName, {
    fileType: 'png',
    backgroundColor: '#f5f5f5',
    padding: 30,
    partial: false,
    quality: 0.92
  });
}

/**
 * 监听消息
 */
function listeningMessage(e) {
  const { data } = e;
  switch (data.type) {
    case "theme-dark": {
      isDark.value = true;
      return;
    }
    case "theme-light": {
      isDark.value = false;
      return;
    }
  }
}

onMounted(() => {
  window.addEventListener("message", listeningMessage);
});
onUnmounted(() => {
  window.removeEventListener("message", listeningMessage);
  window.removeEventListener('resize', fitView); // 移除resize监听
});
</script>

<style scoped>
.containerView {
  width: 100%;
  height: 100%;
}

.top-text {
  position: absolute;
  font-weight: bold;
  right: 20px;
  top: -35px;
  border: 1px solid #d1e9ff;
  background-color: #e8f4ff;
  padding: 4px 8px;
  border-radius: 4px;
  max-width: 300px;
  font-size: 15px;
  color: #333;
  z-index: 1;
}

.log-text {
  position: absolute;
  font-weight: bold;
  right: 10px;
  bottom: 10px;
  font-size: 15px;
  color: #333;
  z-index: 1;
}
</style>
