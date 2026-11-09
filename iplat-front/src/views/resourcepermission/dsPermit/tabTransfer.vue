<template>
  <el-tabs v-model="active" v-loading="loading" type="border-card" style="width: 555px;margin-right: 15px;">
    <el-tab-pane name="dsPermitSubjectUser" label="用户">
      <el-transfer
        v-model="userValue"
        filterable
        filter-placeholder="请输入姓名"
        :filter-method="filterMethod"
        @change="handleTransferChange"
        :data="userOrigin"
        :titles="['待选用户', '已选用户']"
      />
    </el-tab-pane>
    <el-tab-pane name="dsPermitSubjectRole" label="角色">
      <el-transfer
        v-model="roleValue"
        filterable
        filter-placeholder="请输入角色名称"
        :filter-method="filterMethod"
        @change="handleTransferChange"
        :data="roleOrigin"
        :titles="['待选角色', '已选角色']"
      />
    </el-tab-pane>
    <el-tab-pane name="dsPermitSubjectUserGroup" label="部门">
      <el-transfer
        v-model="deptValue"
        filterable
        filter-placeholder="请输入部门名称"
        :filter-method="filterMethod"
        @change="handleTransferChange"
        :data="deptOrigin"
        :titles="['待选部门', '已选部门']"
      />
    </el-tab-pane>
  </el-tabs>
</template>

<script lang="ts" name="TabTransfer" setup>
import {
  addSubject, 
  updateSubject
} from '@/api/resourcepermission/dsPermit/settings';
const props = defineProps({
  info: {
    type: Object,
    default: ()=> {}
  },
  userList: {
    type: Array,
    default: ()=> []
  },
  roleList: {
    type: Array,
    default: ()=> []
  },
  deptList: {
    type: Array,
    default: ()=> []
  },
})
const { proxy } = getCurrentInstance() as ComponentInternalInstance;
const loading = ref(false);
const emit = defineEmits(['updated']);

const active = ref('dsPermitSubjectUser')
const filterMethod = (query, item) => {
  return item.pinyin.indexOf(query) > -1
}
const userOrigin = computed(() => {
  return [].concat(props.userList)
})
const roleOrigin = computed(() => {
  return [].concat(props.roleList)
})
const deptOrigin = computed(() => {
  return [].concat(props.deptList)
})
const userValue = ref([])
const roleValue = ref([])
const deptValue = ref([])
const param = ref()

watch(() => props.info, (val) => {
  initData()
},{deep: true})

// 数据初始化
const initData = () => {
  userValue.value = props.info.dsPermitSubjectUser ? props.info.dsPermitSubjectUser.split(',').map(transNumber) : [];
  roleValue.value = props.info.dsPermitSubjectRole ? props.info.dsPermitSubjectRole.split(',').map(transNumber) : [];
  deptValue.value = props.info.dsPermitSubjectUserGroup ? props.info.dsPermitSubjectUserGroup.split(',').map(transNumber) : [];
  param.value = {
    ...props.info
  }
};
// 处理转数字精度丢失问题
const transNumber = (id) =>{
  if(id<1000000000000000){
    return Number(id)
  }
  return id
}

// 数据发生改变时
const handleTransferChange = (val) => {
  if (!userValue.value.length && !roleValue.value.length && !deptValue.value.length) {
    proxy.$modal.msgError('用户、角色、部门不能同时为空!');
    initData();
  } else {
    loading.value = true;
    console.log(param.value)
    param.value[active.value] = val.join(',');
    if (param.value?.dsPermitSubjectId) {
      updateSubject(param.value).then(() => {
        emit('updated');
        loading.value = false;
      }).catch(()=>{
        loading.value = false;
        // 失败,重置数据
        initData();
      })
    } else {
      addSubject(param.value).then(() => {
        emit('updated');
        loading.value = false;
      }).finally(()=>{
        loading.value = false;
        // 失败,重置数据
        initData();
      })
    }
  }
}

onMounted(() => {
  initData();
})

</script>

<style lang="scss" scoped>
:deep(.el-transfer__buttons){
  padding: 0 10px;
}
</style>