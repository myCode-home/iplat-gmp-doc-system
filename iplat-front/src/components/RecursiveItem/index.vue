<template>
  <div class="recursive-item">
    <div class="item-row">
      <div class="item-name" :style="{ marginLeft: level * 20 + 'px' }" 
      :class="{'is-last': (!item.children || item.children.length == 0) && level != 0}">{{ item.itemName }} </div>
      <!-- 根据item.remark显示对应的字典选项，如果没有remark则不显示 -->
      <div v-if="item.remark && dictOptions && dictOptions.length > 0" class="last-level-item">
        <el-radio-group v-model="item.selectedOption" class="radio-group">
          <el-radio 
            v-for="option in dictOptions" 
            :key="option.value" 
            :label="option.value">
            {{ option.label }}
          </el-radio>
        </el-radio-group>
      </div>
    </div>

    <!-- 如果有子节点且子节点有内容，则递归显示子节点 -->

    <div v-if="item.children && item.children.length > 0">
      <RecursiveItem v-for="(child, childIndex) in item.children" :key="'child-' + childIndex" :item="child"
        :level="level + 1" />
    </div>


  </div>
</template>

<script setup lang="ts">
import RecursiveItem from '@/components/RecursiveItem/index.vue'; // 导入递归组件
import { useDict } from '@/utils/dict';

interface Item {
  itemName: string;
  children?: Item[];
  selectedOption?: boolean | null;
  remark?: string;
}

const props = defineProps<{
  item: Item;
  level: number;
}>();

// 根据item.remark动态获取字典数据
const dictOptions = ref<any[]>([]);
watch(() => props.item.remark, (newRemark) => {
  if (newRemark) {
    // 使用工具函数获取字典
    const dictData = useDict(newRemark);
    if (dictData && dictData[props.item.remark]) {
      dictOptions.value = dictData[newRemark]; // 修复：直接使用 dictData[newRemark] 而不是 dictData[newRemark].value
    }
  } else {
    dictOptions.value = [];
  }
}, { immediate: true });

// 初始化时也获取字典数据
onMounted(() => {
  if (props.item.remark) {
    const dictData = useDict(props.item.remark);
    if (dictData && dictData[props.item.remark]) {
      dictOptions.value = dictData[props.item.remark]; // 修复：直接使用 dictData[props.item.remark] 而不是 dictData[props.item.remark].value
    }
  }
});
</script>

<style scoped lang="scss">
.item-row {
    display: flex;
    width: 100%;
    justify-content: space-between;
    align-items: center;
    height: 40px;
}
.item-name {
  font-size: 16px;
  font-weight: bold;
  color: #303133;
  padding: 8px 0;
  border-bottom: 1px dashed #e4e7ed;
  flex-grow: 1;
  height: 100%;
  display: flex;
  align-items: center;
}

.item-name.is-last {
  font-size: 12px;
  font-weight: normal;
  color: #909399;
}

.last-level-item {
  display: flex;
  justify-content: flex-end;
  border-bottom: 1px dashed #e4e7ed;
    height: 100%;
}

.radio-group {
  flex-shrink: 0;
}
</style>