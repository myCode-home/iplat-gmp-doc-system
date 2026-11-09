<template>
  <div class="file-list-page">
    <el-card shadow="never" class="main-card">
      <template #header>
        <div class="card-header">
          <span>文件清单</span>
          <el-button size="small" @click="handSelect(true)" type="primary" style="float: right;" icon="Plus">批量新增</el-button>
        </div>
      </template>
      <div ref="fileListContainerRef" v-loading="buttonLoading" class="file-list-container">
        <el-card shadow="hover" v-for="(item, index) in fileList" :key="index" style="margin-bottom: 5px;" >
          <super-form 
            ref="superFormChildRef"
            code="bookPrintApplication-form"
            @selectData="selectData"
            :otherCondition="otherCondition"
            v-model="fileList[index]"
            :methods="formMethods">
            <template #docCodeSolt="config">
              <el-input v-model="fileList[index][config.prop]" @click.native="handSelect(false,index)" v-bind="config" readonly></el-input>
            </template>
            <template #deleteBtn>
              <el-button v-if="fileList.length > 1" type="danger" style="transform: translate(0,14px);" icon="Delete" @click="deleteFile(index)"></el-button>
            </template>
          </super-form>
        </el-card>
      </div>    
      <!-- 底部固定按钮 -->
      <div class="fixed-footer">
        <el-button :loading="buttonLoading" type="primary" @click="submitForm" icon="Check">提 交</el-button>
        <el-button @click="handleClear" icon="Delete">清 空</el-button>
      </div>
    </el-card>
    
    <!-- 选择文件或记录 -->
    <SelectGroup
      v-if="selectOption.open"
      :title="selectOption.title"
      :selectType="selectOption.type"
      :multiple="selectOption.multiple"
      :width="selectOption.width"
      :otherCondition="selectOption.otherCondition"
      :activeName="activeName"
      :achieveData="selectDataAchieve"
      @dialogClose="selectDataCancel"
    ></SelectGroup>
  </div>
</template>

<script setup name="FileList" lang="ts">
import SelectGroup from '@/views/dms/bookPrint/bookPrintApplication/selectGroup.vue';
import { addBookPrintTask } from '@/api/dms/bookPrint/bookPrintApplication/index';
import { useUserStore } from '@/store/modules/user';

const { proxy } = getCurrentInstance() as ComponentInternalInstance;

// 接收父组件传递的 activeName
const props = defineProps({
  activeName: {
    type: String,
    default: 'takeEffect'
  }
});

const buttonLoading = ref(false);
const superFormChildRef = ref(null);
const fileListContainerRef = ref<HTMLElement | null>(null);

// 文件清单数据
const fileList = ref([{
  applyDeptKey:useUserStore().deptId,
  source: props.activeName == 'takeEffect' ? 'active' : 'unActive'
}]);

// 筛选条件
const otherCondition = ref({
  documentRecordCode: {
    activeStatus: props.activeName === 'takeEffect' ? 'active' : 'pending'
  }
});

// 选择框配置
const selectOption = ref({
  open: false,
  type: '',
  width: '',
  title: '',
  multiple: false,
  otherCondition: {},
});

const selectIndex = ref(0);

// 选择数据回调
const selectData = (data: any, config) => {
  // 根据需要处理选择的数据
  console.log('selectData:', data, config);
};

// 选择模板库/文件
const handSelect = (multiple: boolean, index?: number) => {
  selectIndex.value = index;
  selectOption.value.open = true;
  selectOption.value.multiple = multiple;
  selectOption.value.title = props.activeName === 'takeEffect' ? '选择生效文件' : '选择待生效文件';
  selectOption.value.type = '';
  selectOption.value.otherCondition = {
    // activeStatus: props.activeName === 'takeEffect' ? 'active' : 'unActive'
  };
};

// 选中数据
const selectDataAchieve = (data, type) => {
  if (selectOption.value.multiple) {
    // 批量新增
    data.forEach(item => {
      const obj = {
        docCode: item.dlCode,
        docName: item.dlName,
        applyDeptKey:useUserStore().deptId,
        source: props.activeName == 'takeEffect' ? 'active' : 'unActive'
      };
      fileList.value.push(obj);
    });
    // 批量新增后滚动到底部
    proxy?.$nextTick(() => {
      scrollToBottom();
    });
  } else {
    // 单个选择
    const obj = {
      docCode: data.dlCode,
      docName: data.dlName,
    };
    Object.assign(fileList.value[selectIndex.value], obj);
  }
  selectDataCancel();
};

// 关闭选择
const selectDataCancel = () => {
  selectOption.value = {
    open: false,
    title: undefined,
    width: '',
    multiple: false,
    type: undefined,
    otherCondition: {},
  };
};

// 滚动到文件清单底部
const scrollToBottom = () => {
  if (fileListContainerRef.value) {
    setTimeout(() => {
      if (fileListContainerRef.value) {
        fileListContainerRef.value.scrollTop = fileListContainerRef.value.scrollHeight;
      }
    }, 100);
  }
};

// 删除文件
const deleteFile = (index: number) => {
  fileList.value.splice(index, 1);
};

// 在父组件中预定义所有可能用到的方法
const formMethods = {
  // args参数集合 childFrom参数集合
  editWayChange: (args: Array<any>, childFrom: Object) => {
    console.log('editWayChange:', args, childFrom);
  },
};

// 子表单验证
const childValidate = async () => {
  let allChildFormsValid = true;
  if (superFormChildRef.value && superFormChildRef.value.length > 0) {
    const validationPromises = superFormChildRef.value.map((ref: any) => {
      if (ref && typeof ref.validate === 'function') {
        return new Promise<boolean>((resolve) => {
          ref.validate((valid: boolean) => {
            resolve(valid);
          });
        });
      }
      return Promise.resolve(true);
    });
    const childFormResults = await Promise.all(validationPromises);
    allChildFormsValid = childFormResults.every((valid) => valid);
    if (!allChildFormsValid) {
      return false;
    }
    return true;
  }
  return true;
};

// 提交表单
const submitForm = async () => {
  // 校验所有子表单
  const result = await childValidate();
  if (!result) {
    proxy?.$modal.msgError('文件清单未填写完整！');
    return false;
  }
  buttonLoading.value = true;
  try {
    // 根据实际需求调用添加或更新接口
    await addBookPrintTask(fileList.value);
    proxy?.$modal.msgSuccess('操作成功');
    fileList.value = [{
      applyDeptKey:useUserStore().deptId,
      source: props.activeName == 'takeEffect' ? 'active' : 'unActive'
    }];
  } catch (error) {
    console.error('提交失败:', error);
  } finally {
    buttonLoading.value = false;
  }
};

// 清空表单
const handleClear = () => {
  proxy?.$modal.confirm('确认要清空所有文件清单吗？').then(() => {
    fileList.value = [{
      applyDeptKey:useUserStore().deptId,
      source: props.activeName == 'takeEffect' ? 'active' : 'unActive'
    }];
    proxy?.$modal.msgSuccess('清空成功');
  }).catch(() => {});
};

onMounted(() => {
  // 初始化逻辑
});
</script>

<style lang="scss" scoped>
.file-list-page {
  height: calc(100vh - 155px);
  display: flex;
  flex-direction: column;
  position: relative;
}

.main-card {
  flex: 1;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  
  :deep(.el-card__body) {
    flex: 1;
    overflow: hidden;
    display: flex;
    flex-direction: column;
  }
}

.card-header {
  height: 25px;
}

.file-list-container {
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden;
  padding-right: 5px;
  padding-bottom: 10px;
  height: calc(100% - 16px);
  
  /* 自定义滚动条样式 */
  &::-webkit-scrollbar {
    width: 8px;
  }
  
  &::-webkit-scrollbar-track {
    background: #f1f1f1;
    border-radius: 4px;
  }
  
  &::-webkit-scrollbar-thumb {
    background: #888;
    border-radius: 4px;
    
    &:hover {
      background: #555;
    }
  }
}
:deep(.el-card__body){
  padding: 15px 20px 5px 20px !important;
}

/* 固定在底部的按钮 */
.fixed-footer {
  display: flex;
  justify-content: center;
  padding-top: 8px;
}
</style>
