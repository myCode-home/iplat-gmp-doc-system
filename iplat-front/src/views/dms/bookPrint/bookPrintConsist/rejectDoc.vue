<template>
  <super-table 
    code="bookPrintConsist-rejectDoc-table"
    rowKey="docCode"
    :data="tableData"
    :isShowPagination="false"
    height="300px"
  >
  </super-table>
</template>

<script lang="ts" setup>
const props = defineProps({
  data: {
    type: Object,
    default: () => ({})
  }
})

const tableData = ref<any[]>([])

// 设置数据
const setData = (data: any) => {
  const list = Object.keys(data).map(key => {
    return { docCode: key, docName: data[key] }
  })
  tableData.value = list
}

watch(() => props.data, (newVal) => {
  if (newVal && Object.keys(newVal).length) {
    setData(newVal)
  }
}, { immediate: true })
</script>
