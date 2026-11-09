<template>
  <div class="watermark-container" ref="parentRef">
    <slot></slot>
  </div>
</template>
 
<script setup>
import { onMounted, onUnmounted, ref, watchEffect } from 'vue';
 
const props = defineProps({
  // 水印内容
  text: {
    type: String,
    required: true,
    default: 'watermark',
  },
  // 默认文字大小
  fontSize: {
    type: Number,
    default: 20,
  },
  // 水印间距
  gap: {
    type: Number,
    default: 20,
  },
});

function useWatermarkBg (props) {
  return computed(() => {
    // 创建一个 canvas
    const canvas = document.createElement('canvas');
    const devicePixelRatio = window.devicePixelRatio || 0.1;
    // 设置字体大小
    const fontSize = props.fontSize * devicePixelRatio;
    const font = fontSize + 'px serif';
    const ctx = canvas.getContext('2d');
    // 获取文字宽度
    ctx.font = font;
    const { width } = ctx.measureText(props.text);
    const canvasSize = Math.max(100, width) + props.gap * devicePixelRatio;
    canvas.width = canvasSize;
    canvas.height = canvasSize;
    ctx.translate(canvas.width / 2, canvas.height / 2);
    // 旋转 20 度让文字变倾斜
    ctx.rotate((Math.PI / 180) * -20);
    ctx.fillStyle = 'rgba(0, 0, 0, 0.34)';
    ctx.font = font;
    ctx.textAlign = 'center';
    ctx.textBaseline = 'middle';
    // 将文字画出来
    ctx.fillText(props.text, 0, 0);
    return {
        base64: canvas.toDataURL(),
        size: canvasSize,
        styleSize: canvasSize / devicePixelRatio,
    };
  });
}
 
const bg = useWatermarkBg(props);
const parentRef = ref(null);
const flag = ref(0); // 声明一个依赖
let div;
 
watchEffect(() => {
  flag.value; // 将依赖放在 watchEffect 里
  if (!parentRef.value) {
    return;
  }
  if (div) {
    div.remove();
  }
  const { base64, styleSize } = bg.value;
 
  div = document.createElement('div');
  div.style.backgroundImage = `url(${base64})`;
  div.style.backgroundSize = `${styleSize}px ${styleSize}px`;
  div.style.backgroundRepeat = 'repeat';
  div.style.zIndex = 9999;
  div.style.position = 'absolute';
  div.style.inset = 0;
  // 元素不会接收鼠标事件，鼠标事件会透过元素传递到下层的元素上
  div.style.pointerEvents = 'none';
  parentRef.value.appendChild(div);
});
 
// 防篡改处理
let ob;
onMounted(() => {
  ob = new MutationObserver((records) => {
    for (const record of records) {
      for (const dom of record.removedNodes) {
        if (dom === div) {
          flag.value++; // 删除节点的时候更新依赖
          return;
        }
      }
      if (record.target === div) {
        flag.value++; // 修改属性的时候更新依赖
        return;
      }
    }
  });
  ob.observe(parentRef.value, {
    childList: true,
    attributes: true,
    subtree: true,
  });
});
 
onUnmounted(() => {
  ob && ob.disconnect();
  div = null;
});
</script>