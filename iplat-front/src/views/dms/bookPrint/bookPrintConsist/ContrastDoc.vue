<template>
  <super-table 
    code="bookPrintConsist-contrastDoc-table"
    rowKey="id"
    :data="tableData"
    :spanMethod="spanMethod"
    :isShowPagination="false"
    height="400px"
  >
  </super-table>
</template>

<script lang="ts" setup>
const props = defineProps({
  data: {
    type: Array,
    default: () => []
  }
})

const tableData = ref([])

// 合并策略 通用行合并函数（将相同多列数据合并为一行）
const spanMethod = ({ row, column, rowIndex }: any) => {
  // 如果没有 property，直接返回默认值（序号列、操作列等）
  if (!column.property) {
    return { rowspan: 1, colspan: 1 }
  }
  const fields = ['docType', 'applyDept', 'printType']
  const cellValue = row[column.property]
  const visibleData = tableData.value
  if (cellValue && fields.includes(column.property)) {
    const prevRow = visibleData[rowIndex - 1]
    let nextRow = visibleData[rowIndex + 1]
    if (prevRow && prevRow[column.property] === cellValue) {
      return { rowspan: 0, colspan: 0 }
    } else {
      let countRowspan = 1
      while (nextRow && nextRow[column.property] === cellValue) {
        nextRow = visibleData[++countRowspan + rowIndex]
      }
      if (countRowspan > 1) {
        return { rowspan: countRowspan, colspan: 1 }
      }
    }
  }
  return { rowspan: 1, colspan: 1 }
}

// 设置数据并排序
const setData = (data: any[]) => {
  tableData.value = data.sort((a, b) => {
    if (a.docType !== b.docType) {
      return a.docType.localeCompare(b.docType)
    } else if (a.applyDept !== b.applyDept) {
      return a.applyDept.localeCompare(b.applyDept)
    } else {
      return a.printType.localeCompare(b.printType)
    }
  })
}

watch(() => props.data, (newVal) => {
  if (newVal && newVal.length) {
    setData(newVal)
  }
}, { immediate: true })
</script>
