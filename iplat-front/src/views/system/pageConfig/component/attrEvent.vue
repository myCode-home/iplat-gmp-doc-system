<template>
  <el-card class="box-card" shadow="hover">
    <template #header>
      <div class="clearfix">
        <span>事件绑定</span>
      </div>
    </template>
    <el-row>
      <el-button type="primary" icon="Plus" @click="addEvent" size="small" style="margin-bottom: 10px;">添加事件</el-button>
    </el-row>
    <el-row :gutter="20" v-for="(item, index) in localForm.events" :key="index">
      <el-col :span="11">
        <el-form-item label="绑定事件" prop="eventName" label-width="70px">
          <el-select v-model="localForm.events[index]['eventName']" filterable allow-create placeholder="请输入事件名称">
            <el-option v-for="dict in system_attr_event" :key="dict.value" :label="dict.label+'('+dict.value+')'" :value="dict.value"></el-option>
          </el-select>
        </el-form-item>
      </el-col>
      <el-col :span="11">
        <el-form-item label="执行方法" prop="methodName" label-width="70px">
          <el-input v-model="localForm.events[index]['methodName']" placeholder="请输入执行方法(需要在对应页面代码里面预先定义)" />
        </el-form-item>
      </el-col>
      <el-col :span="2">
        <el-button 
          class="remove-condition-button"
          @click="delEvent(index)" 
          type="danger" 
          size="small"
          icon="Delete"
          circle
        ></el-button>
      </el-col>
    </el-row>
  </el-card>
</template>

<script lang="ts" setup>
const { proxy } = getCurrentInstance() as ComponentInternalInstance;
const { system_attr_event } = toRefs<any>(proxy?.useDict('system_attr_event'));

interface FormType {
  fieldType: string;
  isDict: boolean;
  dict: string | undefined;
  options: any[];
  [key: string]: any;
}

const props = defineProps({
  modelValue: {
    type: Object as PropType<FormType>,
    required: true
  }
});

const emit = defineEmits<{
  (e: 'update:modelValue', value: FormType): void;
}>();

// 创建本地响应式数据，用于双向绑定
const localForm = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
});

// 添加事件
const addEvent = () =>{
  if(localForm.value.events){
    localForm.value.events.push({ eventName: '', methodName: '' })
  }else{
    localForm.value.events = []
    localForm.value.events.push({ eventName: '', methodName: '' })
  }
}
// 删除事件
const delEvent = (index) =>{
  localForm.value.events.splice(index, 1)
}
</script>

<style lang="scss" scoped>
.box-card {
  margin: 0px 0 10px 0;
  .clearfix {
    text-align: center;
    font-size: 16px;
    color: #1890ff;
    font-weight: bold;
  }
}
.remove-condition-button {
  position: absolute;
  top: 4px;
}
</style>
