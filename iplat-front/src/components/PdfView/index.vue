<template>
  <div id="pdf-container">
    <WaterMark :text="text">
      <iframe
        class="pdf-frame"
        :style="{ height: height || '800px'}"
        :src="viewerUrl"
        frameborder="0"
      ></iframe>
    </WaterMark>
  </div>
</template>

<script  setup lang="ts">
import { getPdfStream } from '@/api/dms/pdf/index';
const props = defineProps({
  // 通过url直接展示pdf
  pdfUrl: {
    type: String,
    default: ''
  },
  // 通过id获取pdf流
  id: {
    type: String,
    default: ''
  },
  text: {
    type: String,
    default: ''
  },
  height: {
    type: String,
    default: '800px'
  }
});
const viewer = new URL('/pdfjs/web/viewer.html', import.meta.url).href
// PDF 文件 URL，可以是远程链接，也可以是 Blob / base64
// const pdfUrl = "https://mozilla.github.io/pdf.js/web/compressed.tracemonkey-pldi-09.pdf"
// 如果是文本流，可以先生成 Blob URL
// 这里的pdfTextData是打印申请打印返回的pdf流
// const pdfBlob = new Blob([pdfTextData], { type: 'application/pdf' })
// const pdfUrl = URL.createObjectURL(pdfBlob)

const viewerUrl = ref('')
const getPdfUrl = async () => {
  if (props.pdfUrl) {
    viewerUrl.value = `${viewer}?file=${encodeURIComponent(props.pdfUrl)}`
    return viewerUrl.value
  }
  if (props.id) {
    try {
      const response = await getPdfStream(props.id) as any
      const blob = new Blob([response], { type: 'application/pdf' })
      viewerUrl.value = `${viewer}?file=${encodeURIComponent(URL.createObjectURL(blob))}`
      return viewerUrl.value
    } catch (error) {
      viewerUrl.value = `${viewer}?file=''`
      console.error('获取PDF流失败:', error)
      return ''
    }
  }
  return ''
}

onMounted(() => {
  getPdfUrl()
  const iframe = document.querySelector('.pdf-frame') as HTMLIFrameElement;
  // 禁止复制pdf及点击右键
  iframe.onload = () => {
    const doc = iframe.contentDocument || iframe.contentWindow.document;
    const docStyle = doc.documentElement.style;
    // 立即设置主题
    docStyle.setProperty("color-scheme", 'dark');
    const script = doc.createElement('script');
    script.textContent = `
      document.addEventListener('contextmenu', function(e) {
        e.preventDefault();
      });
      document.addEventListener('copy', function(e) {
        e.preventDefault();
      });
      document.onselectstart = function() { return false; };
    `;
    doc.head.appendChild(script);
  };
})
</script>

<style scoped>
#pdf-container {
  width: 100%;
  height: 100%;
  position: relative;
  overflow: hidden;
}
.pdf-frame {
  width: 100%;
}
</style>
