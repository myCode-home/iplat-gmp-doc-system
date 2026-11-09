<template>
  <div class="p-2">
    <!-- 添加或修改电子签名配置对话框 -->
    <el-dialog v-model="dialog.visible" class="noBorderBottom" v-if="dialog.visible" :title="dialog.title" :before-close="cancel" :width="widthStyle" append-to-body>
      <el-row :gutter="20">
            <el-col :span="widthProportion" v-for="(item, index) in form.historyUsers" :key="index">
                <el-form ref="signatureRef" v-loading="dialog.loading" :model="formDatas[index]" :rules="rules[index]" label-width="auto">
                    <el-form-item label="签名含义" prop="sduImplication">
                        <el-input v-if="item.sduImplFlag == '1'" v-model="formDatas[index].sduChangeImplication" :placeholder="item.sduImplication" />
                        <el-input v-else v-model="item.sduImplication" disabled placeholder="自动带入" />
                    </el-form-item>
                    <el-form-item label="账号" prop="userName">
                        <el-input v-if="item.sduType === 'LOGUSER'" v-model="formDatas[index].userName" disabled placeholder="自动带入" />
                        <el-input v-else-if="item.sduType !== 'LOGUSER' && item.sduInputType === 'IMPUT'" v-model="formDatas[index].userName" placeholder="请输入账号" />
                        <el-select v-else v-model="formDatas[index].userName" style="width: 100%;" filterable @change="userChange($event,item,formDatas[index])" placeholder="请选择账号">
                            <el-option v-for="dict in item.options" :key="dict.value" :label="dict.label" :value="dict.value"></el-option>
                        </el-select>
                    </el-form-item>
                    <el-form-item label="密码" prop="password">
                        <el-input v-model="formDatas[index].password" @keyup.enter.native="submitForm" @paste="handlePaste" autocomplete="new-password" type="password" clearable placeholder="请输入密码" />
                    </el-form-item>
                    <el-form-item label="理由" prop="sduReason">
                        <el-input v-model="formDatas[index].sduReason" type="textarea" placeholder="请输入理由" />
                    </el-form-item>
                </el-form>
            </el-col>
      </el-row>
      <template #footer>
        <div class="dialog-footer">
          <el-button :loading="buttonLoading" type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="SignatureDialog" lang="ts">
import { inject } from 'vue';
import { useUserStore } from '@/store/modules/user';
import { getUserListByRoleids } from '@/api/system/role';
import { getUserListByGroupIds } from '@/api/system/userGroup';
import { useDict } from '@/utils/dict';
const props = defineProps({
    data: {
        type: Object,
        default: ()=> {}
    }
})
const buttonLoading = inject<boolean>('buttonLoading');
const emits = defineEmits(['confirm','cancel'])
const { proxy } = getCurrentInstance() as ComponentInternalInstance;
const { system_yes_no } = toRefs<any>(useDict('system_yes_no'));

const loading = ref(true);
const signatureRef = ref<ElFormInstance>();

const dialog = reactive<DialogOption>({
  visible: false,
  title: '',
  loading: false
});

// 栅格宽度占比
const widthProportion = ref(24);

// 弹出框宽度
const widthStyle = ref('400px');

// 一行最多几个表单
const linage = ref(3);

const formDatas = ref([]);

const initFormData = {
    signatureDefinitionId: '',
    historyUsers: []
    // userName: '',
    // nickName: '',
    // password: '',
    // sduSort: '',
    // sduReason: '',
    // sduType: '',
    // sduValue: '',
    // sduImplication: '',
    // sduChangeImplication: '',
    // signatureDefinitionUserId: '',
};
const data = reactive({
  form: { ...initFormData },
  rules: []
});

const { form, rules } = toRefs(data);

// 表单重置
const reset = () => {
    if (props.data.definitionUsers.length < (linage.value + 1)) {
        widthProportion.value = 24 / props.data.definitionUsers.length;
        widthStyle.value = `${26 * props.data.definitionUsers.length}%`;
    } else {
        widthProportion.value = 24 / linage.value;
        widthStyle.value = `70%`;
    }
    form.value = { ...initFormData };
    form.value.signatureDefinitionId = props.data.signatureDefinitionId;
    rules.value = props.data.definitionUsers.map((item:any) => {
        if(item.sduReasonFlag == '1') {
            return {
                userName: [{ required: true, message: '账号不能为空', trigger: 'blur' }],
                password: [{ required: true, message: '密码不能为空', trigger: 'blur' }],
                sduReason: [{ required: true, message: '理由不能为空', trigger: 'blur' }],
            };
        }else{
            return {
                userName: [{ required: true, message: '账号不能为空', trigger: 'blur' }],
                password: [{ required: true, message: '密码不能为空', trigger: 'blur' }],
            };
        }
    })
    formDatas.value = props.data.definitionUsers.map((item:any) => {
        if(item.sduType === 'LOGUSER'){
            return {
                userName: useUserStore().name,
                nickName: useUserStore().nickname,
                password: '',
                sduReason: '',
            }
        }else {
            return {
                userName: '',
                nickName: '',
                password: '',
                sduReason: '',
            }
        }
    })
    form.value.historyUsers = props.data.definitionUsers.map((item:any, index) => {
        if(item.sduType !== 'LOGUSER' && item.sduType !== 'ALL'){
            getUser(item,index)
        }
        return item;
    })
};

// 获取下拉选框用户
const getUser = (item, index) => {
    const sduValue = item.sduValue.split(',');
    const remark = item.remark.split(',');
    const options = [];
    if(item.sduType == 'ROLE') {
        getUserListByRoleids(sduValue).then((res) => {
            res.data.forEach((user: any) => {
                options.push({
                    value: user.userName,
                    label: user.nickName + `(${user.userName})`,
                    name: user.nickName,
                })
            })
            item.options = options;
        })
    } else if(item.sduType == 'USERGROUP') {
        getUserListByGroupIds(sduValue).then((res) => {
            res.data.forEach((user: any) => {
                options.push({
                    value: user.userName,
                    label: user.nickName + `(${user.userName})`,
                    name: user.nickName,
                })
            })
            item.options = options;
        })
    } else if(item.sduType == 'USER') {
        item.options = sduValue.map((user: any, index: any) => {
            const str = remark[index];
            console.log(str,str.match(/\(([^)]+)\)$/)[1],str.match(/(.*)\(.*\)$/)[1])
            return {
                value: str.match(/\(([^)]+)\)$/)[1],
                label: str,
                name: str.match(/(.*)\(.*\)$/)[1]
            }
        })
    }
}

/** 新增按钮操作 */
const handleAdd = () => {
  reset();
//   form.value.userName = useUserStore().name;
  dialog.visible = true;
  dialog.title = '电子签名';
};

/** 提交按钮 */
const submitForm = () => {
    console.log(1111111111111)
    const allPromise = [];
    Object.keys(signatureRef.value).map(key=>{
        const p = new Promise((resolve, reject) => {
            signatureRef.value[key].validate((valid: boolean) => {
                if (valid) {
                    resolve(true);
                } else {
                    reject(false);
                }
            });
        });
        allPromise.push(p);
    })
    Promise.all(allPromise).then((res) => {
        const formCopy = JSON.parse(JSON.stringify(form.value));
        formCopy.historyUsers = formCopy.historyUsers.map((item, index)=>{
            return { ...item, ...formDatas.value[index] }
        })
        console.log('confirm1')
        emits('confirm', formCopy);
    })
};

const handlePaste = (event) => {
  event.preventDefault(); // 阻止默认的粘贴行为
};

const userChange = (val, item, formItem) => {
    const user = item.options.find((user:any) => user.value === val);
    formItem.nickName = user.label;
}

// 取消按钮
const cancel = () => {
  reset();
  dialog.visible = false;
  emits('cancel', false);
};

onMounted(() => {
  handleAdd();
  console.log(props)
});
</script>

<style lang="scss" scoped>

</style>
