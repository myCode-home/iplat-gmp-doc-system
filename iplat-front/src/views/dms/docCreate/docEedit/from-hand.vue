<template>
  <div class="">
    <super-form
      ref="superFormRef"
      code="docCreate-form-handCreate"
      @selectData="chooseData"
      v-model="form"
      :form-rules="rules" 
      :methods="formMethods">
      <!-- 模板库插槽 -->
      <template #tmplLibNameSlot="config">
        <el-input v-model="form[config.prop]" suffix-icon="Document" @click.native="handSelect" v-bind="config" readonly></el-input>
      </template>
      <div>
        <el-card shadow="hover" class="my-card">
          <template #header>
            <div class="card-header">
              <span>替代文件</span>
              <el-button @click="addRelation('replace')" type="primary" size="small" style="float: right;" icon="Plus"></el-button>
            </div>
          </template>
          <!-- docCreate-replace-list -->
          <super-table
            code="docCreate-replace-table"
            rowKey="id"
            :data="relationList"
            highlight-current-row
          >
            <template #operation="scope">
              <el-tooltip content="删除" placement="top" :enterable="false">
                <el-button link type="danger" icon="Delete" @click="delRelation(scope.$index,'replace')"></el-button>
              </el-tooltip>
              <el-tooltip content="查看详情" placement="top" :enterable="false">
                <el-button link type="primary" icon="View" @click="handleDetail(scope.row,'replace')"></el-button>
              </el-tooltip>
            </template>
          </super-table>
        </el-card>
        <el-card shadow="hover" class="my-card">
          <template #header>
            <div class="card-header">
              <span>关联文件</span>
              <el-button @click="addRelation('relate')" type="primary" size="small" style="float: right;" icon="Plus"></el-button>
            </div>
          </template>
          <!-- docCreate-relate-list -->
          <super-table
            code="docCreate-relate-table"
            rowKey="id"
            :data="relateList"
            highlight-current-row
          >
            <template #associatedTypeSlot="scope">
              <el-switch
                v-model="scope.row.linkType"
                style="--el-switch-on-color: #13ce66; --el-switch-off-color: #409EFF"
                inactive-text="单向"
                active-text="双向"
                inactive-value="1"
                active-value="2"
              />
            </template>
            <template #operation="scope">
              <el-tooltip content="删除" placement="top" :enterable="false">
                <el-button link type="danger" icon="Delete" @click="delRelation(scope.$index,'relate')"></el-button>
              </el-tooltip>
              <el-tooltip content="查看详情" placement="top" :enterable="false">
                <el-button link type="primary" icon="View" @click="handleDetail(scope.row,'relate')"></el-button>
              </el-tooltip>
            </template>
          </super-table>
        </el-card>
      </div>
    </super-form>
    
    <!-- 选择文件列表对话框 -->
    <el-dialog v-model="dialog.visible" :title="dialog.title" width="90%" append-to-body>
      <active-select v-if="dialog.visible" ref="activeRef" :docType="docType" :multipleChoice="multipleChoice" ></active-select>
      <template #footer>
        <div class="dialog-footer">
          <el-button v-model:buttonLoading="buttonLoading" type="primary" @click="submitSelect">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>
    <!-- 查看生效文件详情对话框 -->
    <el-dialog v-model="activeDialog.visible" :title="activeDialog.title" width="90%" append-to-body>
      <super-form
        ref="superFormRef"
        disabled
        code="docCreate-relevantFile-form"
        v-model="activeForm">
        <div>
          <el-card v-if="activeDialog.visible" shadow="hover" style="margin-bottom: 10px;">
            <template #header>
              <el-row :gutter="10" class="mb8">关联文件</el-row>
            </template>
            <super-table code="fileLibrary-attive-correlation-list" rowKey="id"
              highlightCurrentRow :dataLoading="dataLoading" 
              :data="correlationFileList" @getList="getListCorrelation">
            </super-table>
          </el-card>
          <PdfView v-if="activeForm.id" :id="activeForm.id"></PdfView>
        </div>
      </super-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="activeDialog.visible = false">取 消</el-button>
        </div>
      </template>
    </el-dialog>
    <!-- 选择模板或文件 -->
    <SelectGroup
      v-if="selectData.open"
      :title="selectData.title"
      :selectType="selectData.type"
      :multiple="selectData.multiple"
      :width="selectData.width"
      :otherCondition="selectData.otherCondition"
      :achieveData="selectDataAchieve"
      @dialogClose="selectDataCancel"
  ></SelectGroup>
  </div>
</template>

<!-- 手动创建 -->
<script setup lang="ts">
import { listDocCreateEdit, getDocCreateEdit, delDocCreateEdit, addDocCreateEdit, updateDocCreateEdit, submitAndFlowStart } from '@/api/dms/docCreate/docEdit/index';
import { getFileDetail, getRelateFileList } from '@/api/dms/fileLib/index';
import { useUserStore } from '@/store/modules/user';
import SelectGroup from '@/views/dms/docCreate/docEedit/selectGroup.vue';
import ActiveSelect from '@/views/dms/docCreate/docEedit/activeSelect.vue';

const props = defineProps({
  buttonLoading: {
    type: Boolean,
    default: false
  },
  chose: {
    type: Object,
    default: () => ({})
  },
  operate: {
    type: String,
    default: ''
  }
});
const buttonLoadingModel = defineModel<boolean>('buttonLoading');
const emit = defineEmits(['closeDialog']);

const { proxy } = getCurrentInstance() as ComponentInternalInstance;
interface FormData {
  drafterId?: string|number;
  drafterName?: string;
  documentDeptId?: string;
  [key: string]: any; // 允许其他属性
}
const form = ref<FormData>({
  drafterId: useUserStore().userId,
  drafterName: useUserStore().nickname,
  documentDeptId: useUserStore().deptId,
  operateSource: 'fromInput'
});
const rules = ref({});
// 在父组件中预定义所有可能用到的方法
const formMethods = {
  // args参数集合 childFrom参数集合
  editWayChange: (args: Array<any>,childFrom: Object) => {
    // 编辑方式为线下，清除模板库选择
    if(args[0] == 'offline'){
      form.value.tmplLibId = '';
      form.value.tmplLibCode = '';
      form.value.tmplLibName = '';
    }
  },
};
const selectData = ref({
  open: false,
  type: '',
  width: '',
  title: '',
  multiple: false,
  otherCondition: {},
})
// 替代文件
const relationList = ref([]);
// 关联文件
const relateList = ref([]);

// 选择模板库
const handSelect = (index) =>{
  selectData.value.open = true;
  selectData.value.title = '选择生效模板';
  selectData.value.type = '';
  selectData.value.otherCondition = {
    status: 'active'
  }
}
// 选中数据
const selectDataAchieve = (data, type) => {
  // 生效模板库
  if(type == 'template'){
    form.value.tmplLibId = data.id;
    form.value.tmplLibCode = data.tmplCode;
    form.value.tmplLibName = data.name;
  }else{
    // 文件
    form.value.tmplLibId = data.id;
    form.value.tmplLibCode = data.dlCode;
    form.value.tmplLibName = data.dlName;
  }
  selectDataCancel();
};
// 关闭选择
const selectDataCancel = () => {
  selectData.value = {
    open: false,
    title: undefined,
    width: '',
    multiple: false,
    type: undefined,
    otherCondition: {},
  };
};

const chooseData = (data: any, config) => {
  console.log(data)
  // 起草人
  if(config.prop == 'drafterName'){
    form.value.drafterId = data.id;
    form.value.drafterName = data.name;
  }
  // setTimeout(() => {
  //   form.value.drafterName = 'aaaa';
  // }, 2000);
};

/** 删除按钮操作 */
const handleDelete = async (row?: any) => {
  // const _ids = row?.id || ids.value;
  // const _names = row?.documentName || names.value;
  // await proxy?.$modal.confirm('是否确认删除文件名称为"' + _names + '"的数据项？').finally(() => (loading.value = false));
  // await delDocCreateEdit(_ids);
  // proxy?.$modal.msgSuccess('删除成功');
  // await getList();
};

/** 编辑按钮操作 */
const handleUpdate = async (row?: any) => {
  // reset();
  // dialog.visible = true;
  // dialog.loading = true;
  // dialog.title = '编辑文档编制';
  // operate.value = 'edit';
  // const _id = row?.id || ids.value[0];
  // const res = await getDocCreateEdit(_id);
  // choseRow.value = res.data
  // dialog.loading = false;
};

const dialog = reactive({
  title: '',
  visible: false,
  loading: false
});
const activeRef = ref();
const buttonLoading = ref(false);
const multipleChoice = ref(true);
const docType = ref('');
// 选择替代或关联文件 replace:替代 relate:关联
const addRelation = (type) => {
  docType.value = type;
  
  dialog.title = type == 'relate' ? '选择关联文件' : '选择替代文件';
  dialog.visible = true;
};
const submitSelect = () =>{
  const data = activeRef.value.selectRow;
  if(data.length == 0){
    return proxy?.$modal.msgError('请选择文件');
  }
  if(docType.value == 'relate'){
    relateList.value = data;
  }else{
    relationList.value = data;
  }
  cancel();
};
const delRelation = (index, type) => {
  if(type == 'relate'){
    relateList.value.splice(index, 1);
  }else{
    relationList.value.splice(index, 1);
  }
};

const activeDialog = reactive<DialogOption>({
  title: '',
  visible: false,
  loading: false
});
const activeRow = ref({});
const activeForm = ref<any>({});
// 查看详情
const handleDetail = async (row?: any, type?: string) => {
  activeDialog.visible = true;
  activeDialog.loading = true;
  activeDialog.title = type == 'relate' ? '关联文件查看详情' : '替代文件查看详情';
  const _id = row?.id;
  const res = await getFileDetail(_id);
  getListCorrelation(_id);
  activeForm.value = res.data
  activeDialog.loading = false;
};
// 关联文件表格数据
const correlationFileList = ref([]);
const dataLoading = ref(false);
// 获取关联文件列表
const getListCorrelation = async (id) => {
  try {
    dataLoading.value = true;
    // 查询关联文件列表(用文件ID查询)
    // console.log('查询关联文件列表(用文件ID查询)', queryParamsCorrelationData.value.id);
    const res = await getRelateFileList(id);
    correlationFileList.value = res.rows;
  } catch (error: any) {
    proxy?.$modal.msgError(error.message);
  } finally {
    dataLoading.value = false;
  }
};

/** 取消按钮 */
const cancel = () => {
  dialog.visible = false;
};

const superFormRef = ref();
const submitForm = () => {
  superFormRef.value?.validate(async (valid: boolean) => {
    if (valid) {
      const data = superFormRef.value.achieveData();
      if(relationList.value&&relationList.value.length){
        data.SupersedeDocumentIds = relationList.value.map(item => item.id).join(',');
      }else{
        data.SupersedeDocumentIds = '';
      }
      if(relateList.value&&relateList.value.length){
        data.relateDocumentIds = relateList.value.map(item => item.id).join(',');
        data.relateDocumentTypes = relateList.value.map(item => item.linkType).join(',');
      }else{
        data.relateDocumentTypes = '';
        data.relateDocumentIds = '';
      }
      buttonLoadingModel.value = true;
      if (data.id) {
        await updateDocCreateEdit(data).finally(() => (buttonLoadingModel.value = false));
      } else {
        await addDocCreateEdit(data).finally(() => (buttonLoadingModel.value = false));
      }
      proxy?.$modal.msgSuccess('操作成功');
      emit('closeDialog');
    }
  });
}

watch(() =>props.chose,(val) => {
  if(props.operate == 'edit'){
    Object.assign(form.value, val);
    proxy.$nextTick(() => {
      superFormRef.value.formatForm();
    })
    if(form.value.docCreateLinkList&&form.value.docCreateLinkList.length){
      relateList.value = form.value.docCreateLinkList.map(item => {
        return {
          ...item.documentLibInfo,
          linkType: item.linkType
        }
      });
    }else{
      relateList.value = [];
    }
    if(form.value.supersedeList&&form.value.supersedeList.length){
      relationList.value = form.value.supersedeList;
    }else{
      relationList.value = [];
    }
    // if(form.value.distributeDeptId){
    //   form.value.distributeDeptId = form.value.distributeDeptId.split(',');
    // }else{
    //   form.value.distributeDeptId = '';
    // }
    // if(form.value.pdDeptId){
    //   form.value.pdDeptId = form.value.pdDeptId.split(',');
    // }else{
    //   form.value.pdDeptId = '';
    // }
  }
}, { immediate: true, deep: true })

onMounted(() => {
  
})

defineExpose({
  submitForm
})

</script>


<style lang="scss" scoped>
.my-card{
  margin-top: 10px;
}
.card-header{
  height: 27px;
}
</style>
