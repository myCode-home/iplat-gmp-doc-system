<template>
  <div class="p-2">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <el-button icon="Plus" class="header-btn" @click="handleAddSubject"></el-button>
          <el-select v-model="form.dsPermitSubjectType" placeholder="请选择" class="header-select">
            <el-option v-for="dict in system_subjects_type" :key="dict.value" :label="dict.label" :value="dict.value"></el-option>
          </el-select>
          <!-- <el-button class="button" type="text">操作按钮</el-button> -->
        </div>
      </template>
      <div id="subjectBox" :style="{'height': maxHeight+'px','overflow-y': 'auto'}" v-loading="loading">
        <el-card shadow="hover" v-for="(item, index) in subjectsList" :key="index" style="margin-bottom: 10px;">
          <template #header>
            <div class="clearfix1">
              <dict-tag :options="system_subjects_type" :value="item.dsPermitSubjectType" />
              <div>
                <el-button v-if="item.dsPermitSubjectId" @click="saveRules(index)" type="primary" size="small" icon="Check">保存</el-button>
                <el-popconfirm v-if="item.dsPermitSubjectId" title="确定删除当前数据集配置吗？" @confirm="handleConfirmDelete(item)">
                  <template #reference>
                    <el-button type="danger" size="small" icon="Close">删除</el-button>
                  </template>
                </el-popconfirm>
                <el-button v-if="!item.dsPermitSubjectId" circle icon="Close" size="small" @click="handleDiscardSubject(index)"></el-button>
              </div>
            </div>
          </template>
          <subject-rules
            ref="subjectRef"
            :show-transfer="item.dsPermitSubjectType == 2"
            :userList="userList"
            :roleList="roleList"
            :deptList="deptList"
            :attrList="businessList"
            :info="item"
            @updated="getList"
          >
          </subject-rules>
        </el-card>
      </div>
    </el-card>
    <!-- 添加或修改数据集授权配置对话框 -->
    <el-dialog v-model="dialog.visible" :title="dialog.title" width="550px" append-to-body>
      <el-form ref="dsPermitFormRef" :model="form" v-loading="dialog.loading" :rules="rules" label-width="auto">
        <el-form-item label="编号" prop="dsPermitId">
          <el-input v-model="form.dsPermitId" placeholder="请输入编号" />
        </el-form-item>
        <el-form-item label="名称" prop="businessId">
          <el-input v-model="form.businessId" placeholder="请输入名称" />
        </el-form-item>
        <el-form-item label="备注" prop="dsPermitSubjectType">
          <el-input v-model="form.dsPermitSubjectType" type="textarea" autosize placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button :loading="buttonLoading" type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="DsPermitSetting" lang="ts">
import {
  listSubject, 
  listSubjectById, 
  getSubject, 
  delSubject, 
  addSubject, 
  updateSubject
} from '@/api/resourcepermission/dsPermit/settings';
import { listBusinessAttr } from '@/api/resourcepermission/businessObject/attribute';
import { listSimpleSelect as listSimpleUser } from "@/api/system/user";
import { listSimpleSelect as listSimpleRole } from "@/api/system/role";
import { listDept } from '@/api/system/dept';
const SubjectRules = defineAsyncComponent(() => import('@/views/resourcepermission/dsPermit/subjectRules.vue'));

const { proxy } = getCurrentInstance() as ComponentInternalInstance;
const { system_subjects_hyphen, system_subjects_type } = toRefs<any>(proxy.useDict('system_subjects_hyphen', 'system_subjects_type'));

const buttonLoading = ref(false);
const loading = ref(true);

const queryFormRef = ref<ElFormInstance>();
const dsPermitFormRef = ref<ElFormInstance>();

const dialog = reactive({
  visible: false,
  loading: false,
  title: ''
});

const initFormData = {
  dsPermitId: proxy.$route.params.dsPermitId,
  businessId: proxy.$route.query.businessId,
  dsPermitSubjectType: '2'
};
const data = reactive({
  form: { ...initFormData },
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    dsPermitId: '',
    dsPermitCode: '',
    dsPermitName: '',
    businessId: '',
    dsFunId: '',
    dsPermitEnable: ''
  },
  rules: {
    dsPermitCode: [{ required: true, message: '编号不能为空', trigger: 'blur' }],
    dsPermitName: [{ required: true, message: '名称不能为空', trigger: 'blur' }],
    dsFunId: [{ required: true, message: '请选择功能操作', trigger: 'blur' }],
    businessId: [{ required: true, message: '请选择业务对象', trigger: 'blur' }],
  }
});
const dsPermitId = ref<string>('');
// 是否有未保存
const haveUnSave = ref(false);

const { queryParams, form, rules } = toRefs(data);
const subjectsList = ref([]);
const maxHeight = ref(0);
const subjectRef = ref([]);
// 用户列表
const userList = ref([]);
// 角色列表
const roleList = ref([]);
// 部门列表
const deptList = ref([]);
// 业务配置列表
const businessList = ref([]);


/** 查询数据集授权配置列表 */
const getList = async () => {
  loading.value = true;
  dsPermitId.value = proxy.$route.params.dsPermitId
  const res = await listSubjectById(dsPermitId.value);
  haveUnSave.value = false;
  subjectsList.value = res.data;
  loading.value = false;
};

// 点击按钮操作
const handleAddSubject = () => {
  if (haveUnSave.value) {
    proxy.$modal.msgError('当前还有未配置的【指定人】!请先配置!');
    return;
  }
  if(form.value.dsPermitSubjectType != '2'){
    serverForAddSubject();
  }else {
    subjectsList.value.push({
      dsPermitSubjectId: '',
      dsPermitSubjectType: form.value.dsPermitSubjectType,
      dsPermitId: proxy.$route.params.dsPermitId,
      businessId: proxy.$route.query.businessId,
    });
    haveUnSave.value = true;
    subjectBoxScrollToBottom();
  }
}

// 新增主体操作
const serverForAddSubject = () => {
  addSubject(form.value).then(()=>{
    getList();
    serverForAddSubject();
  })
}

// 删除主体
const handleConfirmDelete = (data) => {
    loading.value = true;
    delSubject(data.dsPermitSubjectId).then(()=>{
      getList();
      proxy.$modal.msgSuccess('操作成功');
    }).finally(()=>{
      loading.value = false;
    })
};

// 删除主体操作
const handleDiscardSubject = (index: number) => {
  subjectsList.value.splice(index, 1);
  haveUnSave.value = false;
}

// 取消按钮
const cancel = () => {
  reset();
  dialog.loading = false;
  dialog.visible = false;
};

// 表单重置
const reset = () => {
  form.value = { ...initFormData };
  dsPermitFormRef.value?.resetFields();
};

/** 新增按钮操作 */
const handleAdd = () => {
  reset();
  dialog.visible = true;
  dialog.loading = false;
  dialog.title = '添加数据集授权配置';
};

/** 提交按钮 */
const submitForm = () => {
  dsPermitFormRef.value?.validate(async (valid: boolean) => {
    if (valid) {
      buttonLoading.value = true;
      if (form.value.dsPermitId) {
        await updateSubject(form.value).finally(() => (buttonLoading.value = false));
      } else {
        await addSubject(form.value).finally(() => (buttonLoading.value = false));
      }
      proxy?.$modal.msgSuccess('操作成功');
      dialog.visible = false;
      dialog.loading = false;
      await getList();
    }
  });
};

// 获取配置项
const getOptions = () => {
  // 获取用户列表
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
  })
  // 获取角色列表
  listSimpleRole().then(response => {
    const roles = [];
    response.data.forEach((role) => {
      roles.push({
        label: role.roleName,
        key: role.roleId,
        pinyin: role.roleName
      });
    });
    roleList.value = roles;
  })
  // 获取部门列表
  listDept().then(response => {
    const depts = [];
    response.data.forEach((role) => {
      depts.push({
        label: role.deptName,
        key: role.deptId,
        pinyin: role.deptName
      });
    });
    deptList.value = depts;
  })
  const query = {
    pageNum: 1,
    pageSize: 9999,
    businessId: proxy.$route.query.businessId,
  }
  // 获取业务数据列表
  listBusinessAttr(query).then(response => {
    businessList.value = response.rows;
  })
};

// 设置视图高度
const setViewHeight = () => {
  let height =  190;
  maxHeight.value = document.getElementById('app').clientHeight - height;
};

// 滚动到底部
const subjectBoxScrollToBottom = () => {
  proxy.$nextTick(() => {
    let subjectBox = document.getElementById('subjectBox');
    if (subjectBox) {
      setTimeout(() => {
        subjectBox.scrollTo({
          top: subjectBox.scrollHeight,
          behavior: 'smooth'
        });
      }, 100);
    }
  });
};

// 保存按钮操作
const saveRules = (index) => {
  subjectRef.value[index].saveRules();
}

onMounted(() => {
  getList();
  // 获取各项配置
  getOptions();
  // 自适应窗口
  window.addEventListener('resize', setViewHeight);
  setViewHeight();
});
onUnmounted(() => {
  window.removeEventListener('resize', setViewHeight)
})
</script>

<style lang="scss" scoped>
  .clearfix1{
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
  .header-btn{
    border-right: 0px;
    border-top-right-radius: 0px !important;
    border-bottom-right-radius: 0px !important;
  }
  :deep(.header-select){
    width: 200px;
    .el-select__wrapper{
      border-top-left-radius: 0px !important;
      border-bottom-left-radius: 0px !important;
    }
  }
</style>