<template>
  <div class="classification-page">
    <!-- 第一部分：输入框 -->
    <div class="input-section">
      <el-form :model="formData" label-width="auto" class="input-form">
        <el-form-item label="设备编号：">
          <el-input v-model="formData.deviceId" readonly />
        </el-form-item>
        <el-form-item label="设备名称：">
          <el-input v-model="formData.deviceName" readonly />
        </el-form-item>
        <el-form-item label="用途描述：">
          <el-input v-model="formData.description" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
    </div>

    <!-- 第二部分：循环数组 -->
    <div class="content-section" v-for="(obj, index) in objList" :key="index">
      <h1 class="main-title">{{ obj.templateName }}</h1>
      <RecursiveItem 
        v-for="(item, itemIndex) in obj.items" 
        :key="'item-' + itemIndex" 
        :item="item" 
        :level="0"
      />
    </div>

    <!-- 第三部分：底部按钮 -->
    <div class="button-section">
      <el-button type="primary" @click="handleSave">保存</el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import { getConfirmTemplate } from '@/api/system/inspectionTemplate'; // 导入API
import RecursiveItem from '@/components/RecursiveItem/index.vue'; // 导入递归组件

// 定义数据类型
interface ChildItem {
  itemName: string;
  selectedOption: boolean | null; // null表示未选择
  itemId?: string;
  itemCode?: string;
  itemNameEn?: string;
  itemSort?: string;
  confirmMethod?: string;
  content?: string;
  result?: string;
  children?: ChildItem[];
}

interface Item {
  itemName: string;
  children?: Item[];
  selectedOption?: boolean | null;
}

interface TemplateObj {
  templateName: string;
  items: Item[];
}

interface FormData {
  deviceId: string;
  deviceName: string;
  description: string;
}

// 页面数据
const formData = ref<FormData>({
  deviceId: 'DEV001',
  deviceName: '设备A',
  description: '这是一个示例设备的用途描述'
});

// 定义idList，包含需要获取的模板ID列表
const idList = ref<string[]>(['2003998102675644418','2004001215839068162']); // 示例ID，实际使用时应替换为真实的ID

const objList = ref<TemplateObj[]>([]);

// 初始化数据 - 根据idList获取模板数据
const initTemplateData = async () => {
  try {
    // 创建Promise数组，每个Promise负责获取一个模板的数据
    const promises = idList.value.map(id => getConfirmTemplate(id));
    
    // 并行调用所有接口
    const responses = await Promise.all(promises);
    
    // 转换数据结构并确保顺序与idList一致
    const results: TemplateObj[] = responses.map(response => {
      const templateData = response.data;
      
      // 将API返回的数据结构转换为页面需要的数据结构
      const transformedData: TemplateObj = {
        templateName: templateData.templateName,
        items: templateData.items || [] // 假设API返回的数据结构中items字段直接可用
      };
      
      return transformedData;
    });
    
    // 将获取的数据赋值给objList
    objList.value = results;
  } catch (error) {
    console.error('获取模板数据失败:', error);
    ElMessage.error('获取模板数据失败');
  }
};

// 保存处理函数
const handleSave = () => {
  console.log('保存的数据:', {
    formData: formData.value,
    objList: objList.value
  });

  // 打印每个子项的选择状态
  objList.value.forEach((obj, objIndex) => {
    console.log(`模板 ${objIndex + 1}: ${obj.templateName}`);
    obj.items.forEach((item, itemIndex) => {
      console.log(`  项目 ${itemIndex + 1}: ${item.itemName}`);
      traverseAndLog(item, '    ');
    });
  });

  ElMessage.success('数据已保存到控制台');
};

// 递归遍历并打印选择状态
const traverseAndLog = (item: Item, prefix: string) => {
  if (item.children && item.children.length > 0) {
    item.children.forEach(child => {
      console.log(`${prefix}子项: ${child.itemName}`);
      traverseAndLog(child, prefix + '  ');
    });
  } else {
    console.log(`${prefix}选择: ${item.selectedOption}`);
  }
};

// 页面初始化时调用
onMounted(() => {
  initTemplateData();
});
</script>

<style scoped>
.classification-page {
  padding: 20px;
  max-width: 1100px;
  margin: 0 auto;
}

.input-section {
  margin-bottom: 30px;
}

.input-form {
  background: #f5f5f5;
  padding: 20px;
  border-radius: 4px;
}

.content-section {
  margin-bottom: 40px;
}

.main-title {
  text-align: center;
  color: #303133;
  font-size: 24px;
  margin: 20px 0;
}

.item-container {
  margin: 20px 0;
}

.item-name {
  font-size: 16px;
  font-weight: bold;
  color: #303133;
  margin: 15px 0 10px 0;
  padding: 8px 0;
  border-bottom: 1px solid #e4e7ed;
}

.child-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
  margin: 8px 0;
  font-size: 14px;
  color: #606266;
  border-bottom: 1px dashed #e4e7ed;
}

.child-name {
  flex: 1;
}

.radio-group {
  flex-shrink: 0;
}

.button-section {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 15px 0;
  background: white;
  border-top: 1px solid #e4e7ed;
  text-align: center;
  box-shadow: 0 -2px 6px rgba(0, 0, 0, 0.05);
}
</style>