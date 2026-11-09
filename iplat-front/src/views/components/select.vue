<template>
    <el-select
        v-bind="$attrs"
        style="width: 100%"
    >
        <el-option
            v-for="opt in getDicts($attrs)"
            :key="opt.value"
            :label="opt.label"
            :value="opt.value"
        />
    </el-select>
</template>

<script setup lang="ts">
const $attrs = useAttrs();
const { proxy } = getCurrentInstance() as ComponentInternalInstance;
const dicts = reactive([])
console.log($attrs)
if($attrs.code){
    dicts.push($attrs.code)
}
const dictOption = toRefs<any>(proxy?.useDict(...dicts));
// 下拉选框数据获取
const getDicts = (item) => {
  // 如果有定义字典关键字，优先使用字典数据
    if (item.code) {
        return dictOption[item.code].value
    // return dict.value
    } else {
        return item.options||[]
    }
}
</script>

<style scoped>

</style>