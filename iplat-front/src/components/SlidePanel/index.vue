<template>
    <el-carousel
        :autoplay="false"
        :initial-index="currentView"
        arrow="never"
        height="auto"
        ref="gmpTransitionAliveRef"
        indicator-position="none"
        :loop="false"
    >
        <el-carousel-item
            class="gmp-carousel-item"
            v-for="item in renderName"
            :name="item"
            :key="item"
        >
            <slot :name="item"></slot>
        </el-carousel-item>
    </el-carousel>
</template>

<script setup lang="ts" name="SlidePanel">
import { ref, computed } from 'vue';
import type { CarouselInstance } from 'element-plus';

// Props 定义
interface Props {
    // 激活页面
    activeView?: string;
    // 渲染名称
    renderName?: string[];
}

const props = withDefaults(defineProps<Props>(), {
    activeView: '',
    renderName: () => []
});

// Refs
const gmpTransitionAliveRef = ref<CarouselInstance>();

// 计算当前视图索引
const currentView = computed(() => {
    const index = props.renderName.indexOf(props.activeView);
    return index > -1 ? index : 0;
});

// 切换上一步
const back = (callback?: () => void) => {
    gmpTransitionAliveRef.value?.prev();
    if (typeof callback === 'function') {
        callback();
    }
};

// 前往指定界面
const goto = (name: string | number, callback?: () => void) => {
    gmpTransitionAliveRef.value?.setActiveItem(name);
    if (typeof callback === 'function') {
        callback();
    }
};

// 切换下一步
const next = (callback?: () => void) => {
    gmpTransitionAliveRef.value?.next();
    if (typeof callback === 'function') {
        callback();
    }
};

// 暴露方法给父组件
defineExpose({
    back,
    goto,
    next
});
</script>

<style scoped>
.gmp-carousel-item {
    height: calc(100vh - 100px);
}
</style>
