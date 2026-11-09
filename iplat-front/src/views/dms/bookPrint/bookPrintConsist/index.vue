<template>
  <div class="p-2">
    <SlidePanel
      ref="slidePanelRef"
      activeView="main"
      :renderName="['main','printingAdd']"
    >
      <template #main>
        <el-card>
          <el-tabs v-model="activeName" @tab-click="handleClick">
            <el-tab-pane label="生效组册界面" name="takeEffect" :key="'takeEffect'">
              <ExcelList :key="'takeEffect'" @goto="goto" activeName="takeEffect"></ExcelList>
            </el-tab-pane>
            <el-tab-pane label="待生效组册界面" name="pendingeffectiveness" :key="'pendingeffectiveness'">
              <ExcelList :key="'pendingeffectiveness'" @goto="goto" activeName="pendingeffectiveness"></ExcelList>
            </el-tab-pane>
          </el-tabs>
        </el-card>
      </template>
      <template #printingAdd>
        <PrintingAdd @back="backToMain" v-if="startTask" :activeName="activeName" :excelName="excelName"></PrintingAdd>
      </template>
    </SlidePanel>
  </div>
</template>

<script name="BookPrintConsist" lang="ts" setup>
import ExcelList from '@/views/dms/bookPrint/bookPrintConsist/excelList.vue';
import PrintingAdd from '@/views/dms/bookPrint/bookPrintConsist/printingAdd.vue';

const activeName = ref('takeEffect');
const handleClick = () => { 

};

const slidePanelRef = ref(null);
const excelName = ref(null);
const startTask = ref(false);
// 跳转页面
const goto = (row: any) => {
  excelName.value = row.excelName;
  startTask.value = true;
  slidePanelRef.value.goto(row.page);
};

// 返回
const backToMain = () => {
  startTask.value = false;
  slidePanelRef.value.goto('main');
};

</script>


<style scoped>

</style>
