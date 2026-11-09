<template>
  <div class="p-2">
    <el-tabs v-model="activeName" @tab-click="handleClick">
      <el-tab-pane label="生效组册界面" name="takeEffect">
        <FileList :key="'takeEffect'" activeName="takeEffect"></FileList>
      </el-tab-pane>
      <el-tab-pane label="待生效组册界面" name="pendingeffectiveness">
        <FileList :key="'pendingeffectiveness'" activeName="pendingeffectiveness"></FileList>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script name="BookPrintApplication" lang="ts" setup>
import FileList from '@/views/dms/bookPrint/bookPrintApplication/fileList.vue';

const activeName = ref('takeEffect');
const handleClick = () => { 

};

</script>


<style scoped>

</style>
