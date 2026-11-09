<template>
  <div id="pdf-container">
    <WaterMark :text="waterText">
      <iframe
        class="pdf-frame"
        style="width: 100%; height: 800px"
        :src="viewerUrl"
        frameborder="0"
      ></iframe>
    </WaterMark>
  </div>
</template>

<script setup>
import { computed } from 'vue'

// PDF 文件 URL，可以是远程链接，也可以是 Blob / base64
const pdfUrl = "https://mozilla.github.io/pdf.js/web/compressed.tracemonkey-pldi-09.pdf"

// 如果是文本流，可以先生成 Blob URL
// const pdfBlob = new Blob([pdfTextData], { type: 'application/pdf' })
// const pdfUrl = URL.createObjectURL(pdfBlob)

const viewerUrl = computed(() => {
  return `/pdfjs/web/viewer.html?file=${encodeURIComponent(pdfUrl)}`
})
const waterText = 'admin 超级管理员 2025/12/23 生效'
onMounted(() => {
  const iframe = document.querySelector('.pdf-frame')
  // 禁止复制pdf及点击右键
  iframe.onload = () => {
    const doc = iframe.contentDocument || iframe.contentWindow.document;
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
  overflow: hidden;
}
.pdf-frame {
  width: 100%;
  height: 100%;
}
</style>
