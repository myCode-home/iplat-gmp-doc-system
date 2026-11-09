<template>
  <el-config-provider :locale="appStore.locale" :size="appStore.size">
    <router-view />
  </el-config-provider>
</template>

<script setup lang="ts">
import { useSettingsStore } from '@/store/modules/settings';
import { handleThemeStyle } from '@/utils/theme';
import { useAppStore } from '@/store/modules/app';

const appStore = useAppStore();

onMounted(() => {
  nextTick(() => {
    // 初始化主题样式
    handleThemeStyle(useSettingsStore().theme);
  });
});
</script>

<style lang="scss">
.noMarginRight{
  margin-right: 0px !important;
}
/* 覆盖禁用状态下的文本颜色 */
.el-input.is-disabled .el-input__inner,
.el-textarea.is-disabled .el-textarea__inner,
.el-range-editor.is-disabled,
.el-range-editor.is-disabled input,
.el-select.is-disabled .el-input__inner {
  color: #000000 !important;
  -webkit-text-fill-color: #000000 !important;
  // background-color: #fafafa !important;
}
.el-radio__input.is-disabled + span.el-radio__label{
  color: #000;
  -webkit-text-fill-color: #000000;
}
.el-checkbox__input.is-disabled + span.el-checkbox__label{
  color: #000;
  -webkit-text-fill-color: #000000;
}
.el-select__wrapper.is-disabled{
  // background-color: #fafafa !important;
}
.el-select__wrapper.is-disabled .el-select__selected-item{
  color: #000 !important;
}
.el-radio__input.is-disabled.is-checked .el-radio__inner::after {
  background-color: #000;
  -webkit-text-fill-color: #000000;
}
.el-checkbox__input.is-disabled .el-checkbox__inner::after{
  border-color: #000;
  -webkit-text-fill-color: #000000;
}
.el-button--primary.is-link.is-disabled, .el-button--primary.is-link.is-disabled:active, .el-button--primary.is-link.is-disabled:focus, .el-button--primary.is-link.is-disabled:hover, .el-button--primary.is-plain.is-disabled, .el-button--primary.is-plain.is-disabled:active, .el-button--primary.is-plain.is-disabled:focus, .el-button--primary.is-plain.is-disabled:hover, .el-button--primary.is-text.is-disabled, .el-button--primary.is-text.is-disabled:active, .el-button--primary.is-text.is-disabled:focus, .el-button--primary.is-text.is-disabled:hover{
  color: #a8abb2 !important;
}
.noBorderBottom{
  .el-dialog__header{
    border-bottom: none !important;
    padding: 0 0 5px 0 !important;
  }
}
</style>
