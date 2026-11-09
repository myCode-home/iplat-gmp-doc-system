<template>
  <el-form :model="form" v-loading="userRelateLoading" ref="formRef">
    <el-form-item label="所有人" prop="allFlag">
      <el-radio-group v-model="form.allFlag">
        <el-radio label="Y">是</el-radio>
        <el-radio label="N">否</el-radio>
      </el-radio-group>
    </el-form-item>
    <template v-if="form.allFlag === 'N'">
      <el-divider></el-divider>
      <el-form-item label="指定用户">
        <el-transfer
          filterable
          :loading="userLoading"
          :filter-method="filterMethod"
          filter-placeholder="请输入姓名"
          :titles="['待选用户', '已选用户']"
          v-model="form.userIds"
          :data="userList">
        </el-transfer>
      </el-form-item>
      <el-divider></el-divider>
      <el-form-item label="指定角色">
        <el-transfer
          filterable
          :loading="roleLoading"
          :filter-method="filterMethod"
          filter-placeholder="请输入角色名称"
          :titles="['待选角色', '已选角色']"
          v-model="form.roleIds"
          :data="rolesList">
        </el-transfer>
      </el-form-item>
    </template>
  </el-form>
</template>


<script setup name="msgTemplateUser" lang="ts">
import { listSimpleSelect as listSimpleUser } from "@/api/system/user";
import { listSimpleSelect as listSimpleRole } from "@/api/system/role";
import { queryTemplateUserByMsgTemplateId } from "@/api/system/msgTemplate";
const form = reactive({
  allFlag: 'N',
  userIds: [],
  roleIds: [],
  deptIds: []
})
// 用户列表
const userList = ref([]);
// 角色列表
const rolesList = ref([]);
const userRelateLoading = ref(false);
const userLoading = ref(false);
const roleLoading = ref(false);

const props = defineProps({
  userCheckValue: {
    type: Object,
    default: () => ({})
  }
})

const getUserRelateData = () => {
  userRelateLoading.value = true
  queryTemplateUserByMsgTemplateId(props.userCheckValue.msgTemplateId).then(response => {
    const data = response.data;
    if(data && data.length){
      if(data[0].userType == '00'){
        form.allFlag = 'Y'
      } else {
        data.forEach((item) => {
          if(item.userType == '10'){
            form.userIds = item.userValues.split(',').map(transNumber)
          } else if(item.userType == '20'){
            form.roleIds = item.userValues.split(',').map(transNumber)
          } else if(item.userType == '30'){
            form.deptIds = item.userValues.split(',').map(transNumber)
          }
        })
      }
    }
    userRelateLoading.value = false
    // Object.assign(form, ...data);
    // form.userIds = form.userIds ? form.userIds : [];
    // form.roleIds = form.roleIds ? form.roleIds : [];
  })
};
// 处理转数字精度丢失问题
const transNumber = (id) =>{
  if(id<1000000000000000){
    return Number(id)
  }
  return id
}
// 获取用户列表
const listUser = () => {
  userLoading.value = true;
  listSimpleUser().then(response => {
    const users = [];
    response.data.forEach((user) => {
      users.push({
        label: user.nickName,
        key: user.userId,
        pinyin: user.nickName
      });
    });
    userList.value = users;
    userLoading.value = true;
  })
};
// 获取角色列表
const listRole = () => {
  roleLoading.value = true;
  listSimpleRole().then(response => {
    const roles = [];
    response.data.forEach((role) => {
      roles.push({
        label: role.roleName,
        key: role.roleId,
        pinyin: role.roleName
      });
    });
    rolesList.value = roles;
    roleLoading.value = false;
  })
};

// 过滤方法
const filterMethod = (query, item) => {
  return item.pinyin.indexOf(query) > -1;
};

/** 子组件获取数据方法 - 给父组件调用 */
const achiveMsgTemplateUserData = () => {
  // userType: 用户类型 (00所有人 10用户 20角色 30部门 40岗位)
  if ('Y' === form.allFlag) {
    return [{
      msgTemplateId: props.userCheckValue.msgTemplateId,
      userType: '00',
      userValues: ''
    }]
  } else {
    return [{
      msgTemplateId: props.userCheckValue.msgTemplateId,
      userType: '10',
      userValues: form.userIds.join(',')
    },{
      msgTemplateId: props.userCheckValue.msgTemplateId,
      userType: '20',
      userValues: form.roleIds.join(',')
    },{
      msgTemplateId: props.userCheckValue.msgTemplateId,
      userType: '30',
      userValues: form.deptIds.join(',')
    }]
  }
};


onMounted(() => {
  listUser();
  listRole();
  getUserRelateData();
});
// 暴露方法
defineExpose({
  achiveMsgTemplateUserData
})
</script>
