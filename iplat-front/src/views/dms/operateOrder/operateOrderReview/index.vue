<template>
  <div class="p-2">
    <transition :enter-active-class="proxy?.animate.searchAnimate.enter" :leave-active-class="proxy?.animate.searchAnimate.leave">
      <div v-show="showSearch" class="mb-[10px]">
        <el-card shadow="hover">
          <super-search code="operateOrder-search" v-model="queryParams" @getList="getList" @resetQuery="resetQuery"> </super-search>
        </el-card>
      </div>
    </transition>

    <el-card shadow="hover">
      <super-table
        v-if="showTable"
        code="operateOrder-table"
        rowKey="id"
        highlightCurrentRow
        :dataLoading="loading"
        :data="operateOrderList"
        v-model:queryParams="queryParams"
        :total="total"
        @selection-change="handleSelectionChange"
        @getList="getList"
      >
        <!-- 文件类型 -->
        <template #documentType="scope">
          <dict-tag :options="docTypeOption" :value="scope.row.documentType" valueProp="id" labelProp="sortName" />
        </template>
        <!-- 操作列 -->
        <template #operation="scope">
          <div>
            <el-tooltip content="查看" placement="top" :enterable="false">
              <el-button link type="primary" icon="View" v-hasPermi="['dms:docOperateOrder:query']" @click="handleUpdate(scope.row)"></el-button>
            </el-tooltip>
            <el-tooltip content="附件上传" placement="top" :enterable="false">
              <el-button link type="primary" v-hasPermi="['dms:docOperateOrder:upload']" icon="UploadFilled" @click="handleUpload(scope.row)"></el-button>
            </el-tooltip>
            <el-popconfirm
              title="确定提交当前行数据吗？"
              @confirm="submitApprove(scope.row)"
            >
              <template #reference>
                <span style="margin-left: 12px;">
                  <el-tooltip content="提交" placement="top" :enterable="false">
                    <el-button link type="primary" :disabled="scope.row.status === 'approved'" v-hasPermi="['dms:docOperateOrder:submit']" icon="SuccessFilled"></el-button>
                  </el-tooltip>
                </span>
              </template>
            </el-popconfirm>
          </div>
        </template>
      </super-table>
    </el-card>
    <el-dialog v-model="dialog.visible" :title="dialog.title" width="70%" append-to-body>
      <super-form
        v-if="dialog.visible"
        :loading="dialog.loading"
        ref="superFormRef"
        disabled
        code="operateOrder-review-form"
        v-model="form"
        :form-rules="rules"
        :methods="formMethods"
        @selectData="selectData"
      >
        <template #tmplName="scope">
          <div class="tmpl-name">
            {{ form.tmplLibName }}
          </div>
        </template>
        <div>
          <el-card shadow="hover" class="my-card">
            <template #header>
              <div class="card-header">
                <span>替代文件</span>
                <!-- <el-button @click="addRelation('replace')" type="primary" size="small" style="float: right;" icon="Plus"></el-button> -->
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
                <!-- <el-tooltip content="删除" placement="top" :enterable="false">
                  <el-button link type="danger" icon="Delete" @click="delRelation(scope.$index,'replace')"></el-button>
                </el-tooltip> -->
                <el-tooltip content="查看详情" placement="top" :enterable="false">
                  <el-button link type="primary" icon="View" @click="handleDetail(scope.row,'replace')"></el-button>
                </el-tooltip>
              </template>
            </super-table>
          </el-card>
        </div>
      </super-form>

      <!-- 根据documentType动态显示的属性表单 -->
      <el-form
        v-if="dynamicAttributes.length > 0"
        :model="dynamicForm"
        :rules="dynamicFormRules"
        ref="dynamicFormRef"
        label-width="auto"
        style="margin-top: 20px"
      >
        扩展属性
        <el-row :gutter="20">
          <el-col v-for="attribute in dynamicAttributes" :key="attribute.attributeCode" :span="12">
            <el-form-item :label="attribute.attributeName" :prop="attribute.attributeCode">
              <el-input
                v-if="attribute.valueType === 'input'"
                v-model="dynamicForm[attribute.attributeCode]"
                :placeholder="'请输入' + attribute.attributeName"
                :disabled="true"
              />
              <el-input
                v-else-if="attribute.valueType === 'textarea'"
                v-model="dynamicForm[attribute.attributeCode]"
                type="textarea"
                :placeholder="'请输入' + attribute.attributeName"
                :autosize="{ minRows: 2, maxRows: 4 }"
                :disabled="true"
              />
              <el-date-picker
                v-else-if="attribute.valueType === 'date'"
                v-model="dynamicForm[attribute.attributeCode]"
                type="date"
                value-format="YYYY-MM-DD"
                :placeholder="'请选择' + attribute.attributeName"
                style="width: 100%"
                :disabled="true"
              />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="cancel">关 闭</el-button>
        </div>
      </template>
    </el-dialog>
    <el-dialog v-model="apprDialog.visible" :title="apprDialog.title" width="500px" append-to-body>
      <el-form>
        <el-form-item label="提交意见" prop="approveComments">
          <el-input v-model="apprRow.approveComments" type="textarea" placeholder="请输入提交意见"></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <!-- <el-button :loading="buttonLoading" type="primary" @click="submitApprove">确 定</el-button> -->
          <el-button @click="apprCancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>
    <!-- 附件上传弹窗 -->
    <el-dialog v-model="uplaodDialog.visible" :title="uplaodDialog.title" width="90%" append-to-body>
      <biz-file-upload-list
        v-if="uplaodDialog.visible"
        :biz="{
          bizId: uplaodDialog.id,
          bizType: 'DOC_APPLICATION_FILE',
        }"
      ></biz-file-upload-list>
      <template #footer>
        <div class="dialog-footer">
          <!-- <el-button :loading="buttonLoading" :disabled="Boolean(!selectRows.id)" type="primary" @click="submitCompare">确 定</el-button> -->
          <el-button @click="uplaodDialog.visible = false">关 闭</el-button>
        </div>
      </template>
    </el-dialog>
    <!-- 选择文件列表对话框 -->
    <el-dialog v-model="relationDialog.visible" :title="relationDialog.title" width="90%" append-to-body>
      <active-select v-if="relationDialog.visible" ref="activeRef" :docType="docType" :otherCondition="otherCondition" :multipleChoice="multipleChoice" ></active-select>
      <template #footer>
        <div class="dialog-footer">
          <el-button v-model:buttonLoading="relationLoading" type="primary" @click="submitSelect">确 定</el-button>
          <el-button @click="relationCancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>
    <!-- 查看生效文件详情对话框 -->
    <el-dialog v-model="activeDialog.visible" :title="activeDialog.title" width="90%" append-to-body>
      <super-form
        ref="superFormRef"
        code="file-lib-form"
        disabled
        v-model="activeForm">
        <div>
          <PdfView v-if="activeDialog.visible&&activeForm.id" :id="activeForm.id"></PdfView>
        </div>
      </super-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="activeDialog.visible = false">取 消</el-button>
        </div>
      </template>
    </el-dialog>
    <submitVerify ref="submitVerifyRef" :task-variables="{}" @submit-callback="submitCallback" />
  </div>
</template>

<script setup name="TemplateEdit" lang="ts">
import {
  docOperateOrderListApprove,
  operateOrderDetail
} from '@/api/dms/operateOrder/addOperateOrder/index';
import { getAttributeList, documentList } from '@/api/dms/fileSystemOption/fileType/index';
import { waitingTaskId } from '@/api/workflow/task';
import { getFileDetail } from '@/api/dms/fileLib/index';
import ActiveSelect from '@/views/dms/docCreate/docEedit/activeSelect.vue';

const { proxy } = getCurrentInstance() as ComponentInternalInstance;
const { dms_transact_type } = toRefs<any>(proxy?.useDict('dms_transact_type'));
const { dms_track_state } = toRefs<any>(proxy?.useDict('dms_track_state'));
const { dms_edit_mode } = toRefs<any>(proxy?.useDict('dms_edit_mode'));

const buttonLoading = ref(false);
const loading = ref(false);
const showSearch = ref(true);
const ids = ref<Array<string | number>>([]);
const names = ref<Array<string | number>>([]);
const single = ref(true);
const multiple = ref(true);
const showTable = ref(true);
const total = ref(0);

const superFormRef = ref(null);
const dynamicFormRef = ref(null);

const dialog = reactive<DialogOption>({
  title: '',
  visible: false,
  loading: false
});

const apprDialog = reactive<DialogOption>({
  title: '请输入提交意见',
  visible: false,
  loading: false
});

// 动态属性相关数据
const dynamicAttributes = ref([]);
const dynamicForm = ref({});
const dynamicFormRules = ref({});

// 用于临时存储从后端获取的动态表单数据，防止被watch重置
const tempDynamicFormData = ref({});

const initFormData = {};
const data = reactive<PageData<any, any>>({
  form: { ...initFormData },
  queryParams: reactive({
    pageNum: 1,
    pageSize: 10
  }),
  rules: {
    // id: [{ required: true, message: '主键不能为空', trigger: 'blur' }],
  }
});

const { queryParams, form, rules } = toRefs(data);

const operateOrderList = ref([]);

/** 查询列表 */
const getList = async () => {
  loading.value = true;
  try {
    const res = await docOperateOrderListApprove(queryParams.value);
    operateOrderList.value = res.rows;
    total.value = res.total;
  } finally {
    loading.value = false;
  }
};

// 监听documentType字段变化
watch(
  () => form.value.documentType,
  async (newVal) => {
    console.log('watchwatchwatchwatch');
    if (newVal) {
      try {
        const res = await getAttributeList({ sortId: newVal });
        dynamicAttributes.value = res.rows.filter(item=>{
          return item.status == 'active'
        }) || [];

        // 初始化动态表单字段和校验规则
        // 检查是否有临时存储的数据，如果有则使用临时数据，否则重置
        if (Object.keys(tempDynamicFormData.value).length > 0) {
          dynamicForm.value = { ...tempDynamicFormData.value };
          // 清空临时数据
          tempDynamicFormData.value = {};
        } else {
          dynamicForm.value = {};
        }
        dynamicFormRules.value = {};

        dynamicAttributes.value.forEach((attr) => {
          // 如果字段不存在于临时数据中，则初始化为空字符串
          if (!(attr.attributeCode in dynamicForm.value)) {
            dynamicForm.value[attr.attributeCode] = '';
          }

          // 如果字段是必填的，添加校验规则
          if (attr.ifRequired) {
            dynamicFormRules.value[attr.attributeCode] = [{ required: true, message: `${attr.attributeName}不能为空`, trigger: 'blur' }];
          }
        });
      } catch (error) {
        console.error('获取属性列表失败:', error);
        dynamicAttributes.value = [];
        dynamicForm.value = {};
      }
    } else {
      // 当documentType为空时清空动态属性
      dynamicAttributes.value = [];
      dynamicForm.value = {};
    }
  }
);

// 在父组件中预定义所有可能用到的方法
const formMethods = {
  // args参数集合 childFrom参数集合
  customChangeHandler: (args: Array<any>, childFrom: Object) => {
    superFormRef.value.updateData({ companyName: 'admin' });
    console.log('自定义change事件触发');
  },
  handleRoleChange: (args: Array<any>, childFrom: Object) => {
    console.log('自定义事件触发', args, childFrom);
  },
  handleSexChange: (args: Array<any>, childFrom: Object) => {
    // if(args[0] == '2'){
    //   const index = formConfig.findIndex(item=>item.prop == 'zdy')
    //   formConfig[index].visible = true
    // }
    console.log('自定义事件触发', args);
  },
  processCompanyData: (args: Array<any>, childFrom: Object) => {
    console.log('自定义数据处理', args, childFrom);
  }
};

const apprRow = ref();
// 提交审批按钮操作
const openApprove = (row: any) => {
  apprDialog.visible = true;
  apprRow.value = JSON.parse(JSON.stringify(row));
  apprRow.value.approveComments = '';
  // console.log(row)
};

// 替代文件
const relationList = ref([]);
const relationDialog = reactive<DialogOption>({
  title: '',
  visible: false,
  loading: false
});
const activeRef = ref();
const relationLoading = ref(false);
const multipleChoice = ref(true);
const docType = ref('');
const otherCondition = ref({
  status: 'active',
  activeStatus: 'active'
});
// 选择替代或关联文件 replace:替代 relate:关联
const addRelation = (type) => {
  docType.value = type;
  
  relationDialog.title = type == 'relate' ? '选择关联文件' : '选择替代文件';
  relationDialog.visible = true;
};
const submitSelect = () =>{
  const data = activeRef.value.selectRow;
  if(data.length == 0){
    return proxy?.$modal.msgError('请选择文件');
  }
  if(docType.value == 'relate'){
    relationList.value = data;
  }else{
    relationList.value = data;
  }
  relationCancel();
};
const delRelation = (index, type) => {
  relationList.value.splice(index, 1);
};
const relationCancel = () =>{
  relationDialog.visible = false;
}

const activeDialog = reactive<DialogOption>({
  title: '',
  visible: false,
  loading: false
});
const activeForm = ref<any>({});
// 查看详情
const handleDetail = async (row?: any, type?: string) => {
  activeDialog.visible = true;
  activeDialog.loading = true;
  activeDialog.title = type == 'relate' ? '关联文件查看详情' : '替代文件查看详情';
  const _id = row?.id;
  const res = await getFileDetail(_id);
  activeForm.value = res.data
  activeDialog.loading = false;
};

const selectData = (data: any, config) => {
  if (config.prop == 'draftNickName') {
    form.value.drafterId = data.id;
    form.value.draftNickName = data.userName;
  } else if (config.prop == 'tmplLibName') {
    form.value.tmplLibId = data.id;
    form.value.tmplLibName = data.tmplName;
  }
};

/** 取消按钮 */
const apprCancel = () => {
  apprDialog.visible = false;
  apprRow.value.approveComments = '';
};


/** 取消按钮 */
const cancel = () => {
  reset();
  dialog.visible = false;
};

// 表单重置
const reset = () => {
  form.value = { ...initFormData };
  // 重置动态表单
  dynamicForm.value = {};
  dynamicAttributes.value = [];
  dynamicFormRules.value = {};
};

/** 搜索按钮操作 */
const handleQuery = () => {
  queryParams.value.pageNum = 1;
  getList();
};

/** 重置按钮操作 */
const resetQuery = () => {
  handleQuery();
};

/** 多选框选中数据 */
const handleSelectionChange = (selection: any[]) => {
  ids.value = selection.map((item) => item.id);
  names.value = selection.map((item) => item.tmplName);
  single.value = selection.length != 1;
  multiple.value = !selection.length;
};

/** 新增按钮操作 */
const handleAdd = () => {
  reset();
  dialog.visible = true;
  dialog.loading = false;
  dialog.title = '添加';
};

/** 编辑按钮操作 */
const handleUpdate = async (row?: any) => {
  reset();
  dialog.visible = true;
  dialog.loading = true;
  dialog.title = '查看';
  const res = (await operateOrderDetail(row?.id || ids.value[0])) as any;
  // 如果distributeDeptId是字符串，将其转换为数组用于回显
  // if (res.data.distributeDeptId && typeof res.data.distributeDeptId === 'string') {
  //   res.data.distributeDeptId = res.data.distributeDeptId.split(',');
  // }
  // 如果返回的数据中包含ext字段，则将ext中的数据先临时存储
  if (res.data.ext) {
    console.log('res.data.extres.data.extres.data.ext');
    console.log(JSON.parse(res.data.ext));
    // 先临时存储数据，等待watch处理时使用
    tempDynamicFormData.value = JSON.parse(res.data.ext);
  }
  if(res.data.libs){
    relationList.value = res.data.libs;
  }
  Object.assign(form.value, res.data);
  proxy.$nextTick(() => {
    superFormRef.value.formatForm();
  })
  console.log(form.value);

  dialog.loading = false;
};
const submitVerifyRef = ref();
// 提交审批按钮操作
const submitApprove = (row: any) => {
  waitingTaskId(row.workflowId).then((res) => {
    if(res.data){
      submitVerifyRef.value.openDialog(res.data);
      submitVerifyRef.value.init(row.id);
    }
  });
};

//提交回调
const submitCallback = async () => {
  getList();
};

const uplaodDialog = reactive({
  title: '上传附件',
  visible: false,
  loading: false,
  id: '',
});
// 附件上传按钮操作
const handleUpload = (row: any)=>{
  uplaodDialog.id = row.id;
  uplaodDialog.visible = true;
}

const docTypeOption = ref([]);
const getDocumentList = () => {
  documentList({
    pageNum: 1,
    pageSize: 9999
  }).then((res) => {
    docTypeOption.value = res.rows;
  });
};

onMounted(() => {
  getList();
  getDocumentList();
});
</script>
<style scoped lang="scss">
.tmpl-name {
  background-color: #f5f7fa;
  width: 100%;border-radius: 4px;
  padding: 1px 11px;box-shadow: 0 0 0 1px #e4e7ed inset;
  color: rgb(0, 0, 0);
}
</style>