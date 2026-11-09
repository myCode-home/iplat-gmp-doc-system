<template>
  <div class="relative" :style="{ 'width': 400 }">
    <el-input v-model="modelValue" readonly placeholder="点击选择图标" @click="visible = !visible">
      <template #prepend>
        <el-icon><component v-if="modelValue" :is="modelValue"></component></el-icon>
      </template>
    </el-input>

    <el-popover shadow="none" :visible="visible" placement="top-end" trigger="click" :width="450">
      <template #reference>
        <div class="cursor-pointer text-[#999] absolute right-[10px] top-0 height-[32px] leading-[32px]" @click="visible = !visible">
          <el-icon v-if="modelValue" @click.navite.stop="selectedIcon('')">
            <close-bold />
          </el-icon>
          <template v-else>
            <i-ep-caret-top v-show="visible"></i-ep-caret-top>
            <i-ep-caret-bottom v-show="!visible"></i-ep-caret-bottom>
          </template>
        </div>
      </template>

      <el-input v-model="filterValue" class="p-2" placeholder="搜索图标" autofocus clearable @input="filterIcons" />

      <el-scrollbar height="w-[200px]">
        <ul class="icon-list">
          <el-tooltip v-for="(iconName, index) in iconNames" :key="index" :content="iconName" placement="bottom" effect="light">
            <li :class="['icon-item', { active: modelValue == iconName }]" @click="selectedIcon(iconName)">
              <el-icon><component :is="iconName"></component></el-icon>
              <!-- <svg-icon color="var(--el-text-color-regular)" :icon-class="iconName" /> -->
            </li>
          </el-tooltip>
        </ul>
      </el-scrollbar>
    </el-popover>
  </div>
</template>

<script setup  lang="ts">
import * as ElementPlusIconsVue from '@element-plus/icons-vue'

const props = defineProps({
  // 双向绑定值（必传）
  modelValue: {
    type: String,
    default: () => '',
  }
});
// 定义Emits：支持v-model双向绑定 + 事件透传
const emit = defineEmits(['update:modelValue']);
// 内部值（解耦props，避免直接修改）
// const iconValue = computed({
//   get() {
//     return props.modelValue;
//   },
//   set(val) {
//     emit('update:modelValue', val);
//   }
// });
const icons = Object.keys(ElementPlusIconsVue)

const visible = ref(false);
const { modelValue } = toRefs(props);
const iconNames = ref<string[]>(icons);

const filterValue = ref('');

/**
 * 筛选图标
 */
const filterIcons = () => {
  if (filterValue.value) {
    iconNames.value = icons.filter((iconName) => iconName.toLowerCase().includes(filterValue.value.toLowerCase()));
  } else {
    iconNames.value = icons;
  }
};
/**
 * 选择图标
 * @param iconName 选择的图标名称
 */
const selectedIcon = (iconName: string) => {
  emit('update:modelValue', iconName);
  visible.value = false;
};
</script>

<style lang="scss" scoped>
.el-scrollbar {
  max-height: calc(50vh - 100px) !important;
  overflow-y: auto;
}
.el-divider--horizontal {
  margin: 10px auto !important;
}
.icon-list {
  display: flex;
  flex-wrap: wrap;
  padding-left: 10px;
  margin-top: 10px;

  .icon-item {
    cursor: pointer;
    width: 10%;
    margin: 0 10px 10px 0;
    padding: 5px;
    display: flex;
    flex-direction: column;
    justify-items: center;
    align-items: center;
    border: 1px solid #ccc;
    &:hover {
      border-color: var(--el-color-primary);
      color: var(--el-color-primary);
      transition: all 0.2s;
      transform: scaleX(1.1);
    }
  }
  .active {
    border-color: var(--el-color-primary);
    color: var(--el-color-primary);
  }
}
</style>
