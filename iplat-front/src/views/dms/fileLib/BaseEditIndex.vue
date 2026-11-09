<template>
  <div class="">
    <transition :enter-active-class="proxy?.animate.searchAnimate.enter"
      :leave-active-class="proxy?.animate.searchAnimate.leave">
      <div v-show="showSearch" class="mb-[10px]">
        <el-card shadow="hover">
          <super-search
            :code="isactive ? 'file-lib-search-active' : (isbatchRevisionUse ? 'batchRevision-apply-file-search-form' : 'file-lib-search')"
            v-model="queryParams" @getList="getList" @resetQuery="resetQuery">
          </super-search>
        </el-card>
      </div>
    </transition>

    <el-card shadow="hover">
      <template #header v-if="!isbatchRevisionUse">
        <el-row :gutter="10" class="mb8">
          <el-col :span="1.5">
            <el-button type="primary" icon="Upload" v-hasPermi="['dms:docOperateOrder:export']"
              :disabled="ids.length === 0" @click="handleExport">导出</el-button>
          </el-col>
          <right-toolbar v-model:show-search="showSearch" @query-table="getList"></right-toolbar>
        </el-row>
      </template>
      <super-table v-if="showTable" ref="tableRef" :code="minaTableCode" rowKey="id" highlightCurrentRow
        :dataLoading="loading" :data="operateOrderList" v-model:queryParams="queryParams" :total="total"
        @selection-change="handleSelectionChange" @getList="getList" @expand-change="handleExpandChange">
        <!-- 文件类型 -->
        <template #indexColumn>
          <el-table-column type="expand" fixed="left">
            <template #default="{ row }">
              <super-table code="fileLibrary-attive-other-version-list" rowKey="id" highlightCurrentRow
                :dataLoading="loading" :data="otherVersionFileList" v-model:queryParams="queryParamsOther"
                :total="totalOther" @selection-change="handleSelectionChangeOther" @getList="getListOther">
                <template #operation="scope">
                  <el-tooltip content="详情" placement="top" :enterable="false">
                    <el-button link type="primary" v-hasPermi="['dms:docInvalidOrder:query']" icon="View"
                      @click="handleAllDetailOther(scope.row)"></el-button>
                  </el-tooltip>
                </template>
              </super-table>
            </template>
          </el-table-column>
        </template>
        <template #documentType="scope">
          <dict-tag :options="docTypeOption" :value="scope.row.documentType" valueProp="id" labelProp="sortName" />
        </template>
        <!-- 操作列 -->
        <template #operation="scope">
          <slot name="operation" :scope="scope"></slot>
          <!-- 查看详情 -->
          <el-tooltip v-if="!isbatchRevisionUse" content="详情" placement="top" :enterable="false">
            <el-button link type="primary" v-hasPermi="['dms:docInvalidOrder:query']" icon="View"
              @click="handleAllDetail(scope.row)"></el-button>
          </el-tooltip>
          <!-- 导出pdf -->
          <el-tooltip v-if="!isbatchRevisionUse" content="导出pdf" placement="top" :enterable="false">
            <el-button link type="primary" icon="SoldOut" @click="handleExportPDF(scope.row)"></el-button>
          </el-tooltip>
          <!-- 导出word -->
          <el-tooltip v-if="!isbatchRevisionUse" content="导出word" placement="top" :enterable="false">
            <el-button link type="primary" icon="Download" @click="handleExportWORD(scope.row)"></el-button>
          </el-tooltip>
          <!-- 是否是isbatchRevisionUse才显示 -->
          <el-tooltip v-if="isbatchRevisionUse" content="编辑" placement="top" :enterable="false">
            <el-button link type="primary" icon="EditPen" v-hasPermi="['dms:docOperateOrder:edit']"
              @click="handleUpdate(scope.row)"></el-button>
          </el-tooltip>
          <!-- 是否是isbatchRevisionUse才显示 -->
          <el-tooltip v-if="isbatchRevisionUse" content="查看记录" placement="top" :enterable="false">
            <el-button link type="primary" icon="Memo" v-hasPermi="['dms:documentRecordRevise:list']"
              @click="handleRecord(scope.row)"></el-button>
            <!-- 是否是isbatchRevisionUse才显示 -->
          </el-tooltip>
          <el-tooltip v-if="isbatchRevisionUse" content="查看附件" placement="top" :enterable="false">
            <el-button link type="primary" icon="UploadFilled" v-hasPermi="['dms:docOperateOrder:upload']"
              @click="handleUpload(scope.row)"></el-button>
          </el-tooltip>
        </template>
      </super-table>
    </el-card>
    <!-- 添加或编辑对话框 -->
    <el-dialog v-model="dialog.visible" :title="dialog.title" width="80%" append-to-body>
      <!-- 根据documentType动态显示的属性表单 -->
      <el-form v-if="dynamicAttributes.length > 0" :model="dynamicForm" :rules="dynamicFormRules" ref="dynamicFormRef"
        label-width="auto" style="margin-top: 20px">
        扩展属性
        <el-row :gutter="20">
          <el-col v-for="attribute in dynamicAttributes" :key="attribute.attributeCode" :span="12">
            <el-form-item :label="attribute.attributeName" :prop="attribute.attributeCode">
              <el-input v-if="attribute.valueType === 'input'" v-model="dynamicForm[attribute.attributeCode]"
                :placeholder="'请输入' + attribute.attributeName" />
              <el-input v-else-if="attribute.valueType === 'textarea'" v-model="dynamicForm[attribute.attributeCode]"
                type="textarea" :placeholder="'请输入' + attribute.attributeName" :autosize="{ minRows: 2, maxRows: 4 }" />
              <el-date-picker v-else-if="attribute.valueType === 'date'" v-model="dynamicForm[attribute.attributeCode]"
                type="date" value-format="YYYY-MM-DD" :placeholder="'请选择' + attribute.attributeName"
                style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button :loading="buttonLoading" type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>
    <!-- 查看生效文件详情对话框 -->
    <el-dialog v-model="activeDialog.visible" :title="activeDialog.title" width="90%" append-to-body>
      <super-form ref="superFormRef" code="file-lib-form" disabled v-model="activeForm">
        <div>
          <PdfView v-if="activeDialog.visible && activeForm.id" :id="activeForm.id"></PdfView>
        </div>
      </super-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="activeDialog.visible = false">取 消</el-button>
        </div>
      </template>
    </el-dialog>
    <!-- 历史文件浏览对话框 -->
    <el-dialog v-model="historyDialog.visible" :title="historyDialog.title" width="80%" append-to-body>
      <el-card shadow="hover">
        <super-search code="fileLibrary-attive-history-search-from" v-model="historyQueryParams"
          @getList="getHistoryList" @resetQuery="getHistoryList">
        </super-search>
      </el-card>
      <br />
      <el-card>
        <super-table v-if="historyDialog.visible" ref="historyTableRef" code="fileLibrary-attive-history-list"
          rowKey="id" highlightCurrentRow :dataLoading="historyDialog.loading" :data="historyFileList"
          v-model:queryParams="historyQueryParams" :total="historyTotal" @getList="getHistoryList">
        </super-table>
      </el-card>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="cancelHistory">关 闭</el-button>
        </div>
      </template>
    </el-dialog>
    <!-- 附件查看 -->
    <el-dialog v-model="uplaodDialog.visible" :title="uplaodDialog.title" width="90%" append-to-body>
      <biz-file-upload-list :disabled="true" v-if="uplaodDialog.visible" :biz="{
        bizId: uplaodDialog.id,
        bizType: 'DOC_APPLICATION_FILE',
      }"></biz-file-upload-list>
      <template #footer>
        <div class="dialog-footer">
          <!-- <el-button :loading="buttonLoading" :disabled="Boolean(!selectRows.id)" type="primary" @click="submitCompare">确 定</el-button> -->
          <el-button @click="uplaodDialog.visible = false">关 闭</el-button>
        </div>
      </template>
    </el-dialog>
    <ViewDetail :isHandlePreviewPDF="isHandlePreviewPDF" v-model:visible="viewDetailVisible" :title="viewDetailTitle"
      :rowData="viewDetailRowData">
    </ViewDetail>
    <!-- 其他版本详情对话框 -->
    <el-dialog v-model="dialog.visible" :title="dialog.title" width="90%" append-to-body>
      <super-form v-if="dialog.visible" :loading="dialog.loading" ref="superFormRef"
        code="fileLibrary-attive-other-version-detail-form" disabled v-model="dialog.form">
        <div>
          <PdfView v-if="dialog.form.id" :id="dialog.form.id"></PdfView>
        </div>
      </super-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialog.visible = false">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { updateFileLib, judgeReviseFileLib, getFileDetail, exportFileList, deleteFile, getActiveFileList, getHistoryFileList, addInvalidFile, getOtherVersionFileList } from '@/api/dms/fileLib/index';
import { documentList } from '@/api/dms/fileSystemOption/fileType/index';
import ViewDetail from '@/views/dms/fileLib/ViewDetail.vue';
import { useUserStore } from '@/store/modules/user';
import { downloadFile } from '@/api/system/oss/index'
const props = defineProps({
  minaTableCode: {
    type: String,
    default: 'fileLibrary-attive-index-list'
  },
  getListByFather: {
    type: Function,
    default: () => { }
  },
  isactive: {
    type: Boolean,
    default: false
  },
})

// 是否是批量修订使用来决定是否显示一些东西和内容
const isbatchRevisionUse = inject('isbatchRevisionUse', false);
// console.log(isbatchRevisionUse);


const expandedRowIds = ref<Set<string | number>>(new Set());
// 展开或者关闭行
const handleExpandChange = (...args: any[]) => {
  const [row, expandedRows] = args;
  const isExpanded = expandedRows.findIndex(item => item.id === row.id) !== -1;
  if (isExpanded) {
    expandedRowIds.value.add(row.id);
    // 展开时获取其他版本数据
    if (row?.id) {
      queryParamsOther.value.id = row.id;
      getListOther(row.id);
    }
  } else {
    expandedRowIds.value.delete(row.id);
  }
}
const { proxy } = getCurrentInstance() as ComponentInternalInstance;
const emits = defineEmits(['switchToRecordTab']);

const buttonLoading = ref(false);
const loading = ref(false);
const showSearch = ref(true);
const ids = ref<Array<string | number>>([]);
const showTable = ref(true);
const total = ref(0);
const superFormRef = ref(null);
const dynamicFormRef = ref(null);
const tableRef = ref(null);

// ViewDetail组件相关状态
const viewDetailVisible = ref(false);
const viewDetailTitle = ref('查看详情');
const viewDetailRowData = ref({});
// const isReplace = ref(false);

const dialog = reactive<DialogOption>({
  title: '',
  visible: false,
  loading: false,
  form: {}
});

const historyDialog = reactive<DialogOption>({
  title: '浏览历史',
  visible: false,
  loading: false
});

const historyFileList = ref([]);
const historyQueryParams = ref({
  pageNum: 1,
  pageSize: 10
});
const historyTotal = ref(0);
const historyTableRef = ref(null);

const cancelHistory = () => {
  historyDialog.visible = false;
  historyFileList.value = [];
  historyQueryParams.value = {
    pageNum: 1,
    pageSize: 10
  };
  historyTotal.value = 0;
};

const getHistoryList = async () => {
  historyDialog.loading = true;
  try {
    const res = await getHistoryFileList(historyQueryParams.value);
    historyFileList.value = res.rows || [];
    historyTotal.value = res.total || 0;
  } finally {
    historyDialog.loading = false;
  }
};

const handleBrowseHistory = (row: any) => {
  historyDialog.visible = true;
  historyDialog.loading = false;
  getHistoryList();
};

onMounted(() => {
  getList();
  getDocumentList();
});

// 动态属性相关数据
const dynamicAttributes = ref([]);
const dynamicForm = ref({});
const dynamicFormRules = ref({});

// 用于临时存储从后端获取的动态表单数据，防止被watch重置
const tempDynamicFormData = ref({});

const initFormData = {
  drafterId: useUserStore().userId,
  draftNickName: useUserStore().nickname,
  docDeptId: useUserStore().deptId,
};
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

// 其他版本相关数据
const otherVersionFileList = ref([]);
const queryParamsOther = ref({ pageNum: 1, pageSize: 10, id: '' });
const totalOther = ref(0);

/** 查询列表 */
const getList = async () => {
  loading.value = true;
  try {
    // const res = await getActiveFileList(queryParams.value);
    const res = await props.getListByFather(queryParams.value);
    operateOrderList.value = res.rows;
    total.value = res.total;
  } finally {
    loading.value = false;
  }
};

/** 其他版本查询 */
const getListOther = async (_id: string) => {
  loading.value = true;
  try {
    // const res = await getActiveFileList(_id);
    const res = await getOtherVersionFileList(_id);
    otherVersionFileList.value = res.rows;
    totalOther.value = res.total;
  } finally {
    loading.value = false;
  }
};

// 监听documentType字段变化
watch(
  () => form.value.documentType,
  async (newVal) => {
    if (newVal) {
      try {
        // const res = await getActiveFileList({ sortId: newVal });
        const res = await props.getListByFather({ sortId: newVal });
        dynamicAttributes.value = res.rows.filter(item => { return item.status == 'active' }) || [];
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
    console.log('自定义事件触发', args);
  },
  processCompanyData: (args: Array<any>, childFrom: Object) => {
    console.log('自定义数据处理', args, childFrom);
  }
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
const otherCondition = ref<any>({
  status: 'active',
  activeStatus: 'active'
});
// 选择替代或关联文件 replace:替代 relate:关联
const addRelation = (type) => {
  docType.value = type;
  relationDialog.title = type == 'replace' ? '选择替代文件' : '选择生效文件';
  if (type == 'replace') {
    multipleChoice.value = true;
    otherCondition.value = {
      status: 'active',
      activeStatus: 'active'
    };
  } else {
    multipleChoice.value = false;
    otherCondition.value = {};
  }
  relationDialog.visible = true;
};
const submitSelect = () => {
  const data = activeRef.value.selectRow;
  if (data.length == 0) {
    return proxy?.$modal.msgError('请选择文件');
  }
  if (docType.value == 'replace') {
    relationList.value = data;
  } else {
    const data = activeRef.value.selectRow[0];
    form.value.documentCode = data.dlCode;
    form.value.documentLibId = data.id;
    form.value.documentName = data.dlName;
    form.value.documentType = data.dlType;
  }
  relationCancel();
};
const delRelation = (index, type) => {
  relationList.value.splice(index, 1);
};
const relationCancel = () => {
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
  } else if (config.prop == 'documentCode') {
    form.value.documentCode = data.dlCode;
    form.value.documentLibId = data.id;
    form.value.documentName = data.dlName;
    form.value.documentType = data.dlType;
  }
};
const selectOption = ref({
  open: false,
  type: '',
  width: '',
  title: '',
  multiple: false,
  otherCondition: {
    status: 'active',
    activeStatus: 'active'
  },
})
// 选择模板库
const handSelect = (index) => {
  selectOption.value.open = true;
  selectOption.value.title = '选择生效模板';
  selectOption.value.type = '';
  selectOption.value.otherCondition = {
    status: 'active',
    activeStatus: 'active'
  }
}
// 选中数据
const selectDataAchieve = (data, type) => {
  form.value.tmplSource = type;
  // 生效模板库
  if (type == 'template') {
    form.value.tmplLibId = data.id;
    form.value.tmplLibCode = data.tmplCode;
    form.value.tmplLibName = data.tmplName;
  } else {
    // 文件
    form.value.tmplLibId = data.id;
    form.value.tmplLibCode = data.dlCode;
    form.value.tmplLibName = data.dlName;
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
    otherCondition: {
      status: 'active',
      activeStatus: 'active'
    },
  };
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

/** 其他版本多选框选中 */
const handleSelectionChangeOther = (selection: any[]) => {
  // 处理其他版本列表的选择逻辑
};

/** 新增按钮操作 */
const handleAdd = () => {
  reset();
  dialog.visible = true;
  dialog.loading = false;
  dialog.title = '添加修订申请';
};

/** 编辑按钮操作 */
const handleUpdate = async (row?: any) => {
  // 判断数据状态是否允许被修订
  const judgeRes = (await judgeReviseFileLib(row?.id || ids.value[0])) as any;
  if (judgeRes.code !== 0) {
    return;
  }
  dialog.visible = true;
  dialog.loading = true;
  dialog.title = '编辑修订申请';
  const res = (await getFileDetail(row?.id || ids.value[0])) as any;
  if (res.data.ext) {
    tempDynamicFormData.value = JSON.parse(res.data.ext);
  }
  if (res.data.libs) {
    relationList.value = res.data.libs;
  }
  Object.assign(form.value, res.data);
  proxy.$nextTick(() => {
    superFormRef.value.formatForm();
  })

  dialog.loading = false;
};

/** 提交按钮 */
const submitForm = () => {
  const data = superFormRef.value.achieveData();
  // 如果distributeDeptId是数组，将其转换为逗号分隔的字符串
  if (data.distributeDeptId && Array.isArray(data.distributeDeptId)) {
    data.distributeDeptId = data.distributeDeptId.join(',');
  }
  if (relationList.value && Array.isArray(relationList.value)) {
    data.replaceIds = relationList.value.map(item => item.id);
  }
  superFormRef.value?.validate(async (valid: boolean) => {
    if (valid) {
      // 验证动态表单
      if (dynamicFormRef.value) {
        try {
          await dynamicFormRef.value.validate();
          // 动态表单验证通过，继续提交
        } catch {
          // 动态表单验证失败，不继续提交
          return;
        }
      }

      // 合并动态表单数据到主表单数据
      const mergedData = { ...data, ext: JSON.stringify(dynamicForm.value) };
      buttonLoading.value = true;
      await updateFileLib(mergedData).finally(() => (buttonLoading.value = false));
      proxy?.$modal.msgSuccess('操作成功');
      dialog.visible = false;
      await getList();
    }
  });
};

/** 删除按钮操作 */
const handleDelete = async (row?: any) => {
  await proxy?.$modal.confirm('是否确认终止' + (row?.dlName ? ('名称为：' + row.dlName) : '当前') + '的数据项？').finally(() => (loading.value = false));
  const result = await judgeReviseFileLib(row?.id || ids.value);
  if (result.code !== 0) return proxy?.$modal.msgError(result.msg);
  const res = await proxy?.$Signature(addInvalidFile) as any;
  const orderReason = res?.users[0].sduReason;

  await addInvalidFile({
    documentlds: row?.id || ids.value[0],
    orderReason
  }, res?.es);
  proxy?.$modal.msgSuccess('操作成功');
  await getList();
};

const handleRecord = (row: any) => {
  emits('switchToRecordTab', row);
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

/**
 * 通用文件下载函数
 * @param bizIds 业务ID，单个或多个
 * @param bizType 业务类型
 * @param fileName 文件名
 * @param fileType 文件类型
 * @returns Promise<void>
 */
const downloadFileToLocal = async (bizIds: string, bizType: string, fileName: string, fileType: string) => {
  try {
    const res = await downloadFile({
      bizIds,
      bizType
    });
    if (!res.data) {
      proxy?.$modal.msgError(res.msg || '文件数据不存在!');
      return;
    }

    // 根据文件类型设置MIME类型
    let mimeType = 'application/octet-stream';
    if (fileType === 'pdf') {
      mimeType = 'application/pdf';
    } else if (fileType === 'word') {
      mimeType = 'application/msword';
    } else if (fileType === 'zip') {
      mimeType = 'application/zip';
    }

    // 创建下载链接
    const blob = new Blob([res], { type: mimeType });
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = fileName;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    window.URL.revokeObjectURL(url);

    return true;
  } catch (error) {
    console.error('文件下载失败:', error);
    return false;
  }
};

// 导出文件弹窗确认回调
const dialogMessage = (success: boolean) => {
  if (success) {
    proxy?.$modal.msgSuccess('导出成功');
  } else {
    proxy?.$modal.msgError('导出失败');
  }
};
/** 导出按钮操作 */
const handleExport = async () => {
  // 使用更简单的方式实现三按钮选择
  // 首先显示一个确认框，让用户选择是否导出
  try {
    // 使用ElMessageBox的confirm方法，通过不同的按钮文本实现选择
    const result = await ElMessageBox.confirm(
      '请选择导出格式',
      '导出文件',
      {
        confirmButtonText: '导出PDF',
        cancelButtonText: '导出Word',
        type: 'warning',
        // 添加自定义的取消按钮
        distinguishCancelAndClose: true,
        closeOnClickModal: false,
        closeOnPressEscape: true
      }
    );

    // 用户点击了"导出PDF"
    const success = await downloadFileToLocal(
      ids.value.join(','),
      'PDF_FILE',
      `导出文件_${new Date().getTime()}.zip`,
      'zip'
    );
    dialogMessage(success);
  } catch (error) {
    // 检查错误类型，判断是点击了"导出Word"还是"取消"
    if (error === 'cancel') {
      // 用户点击了"导出Word"
      const success = await downloadFileToLocal(
        ids.value.join(','),
        'DOC_FILE',
        `导出文件_${new Date().getTime()}.zip`,
        'zip'
      );
      dialogMessage(success);
    } else if (error === 'close') {
      // 用户点击了关闭按钮或按了ESC，取消导出
      return;
    }
  }

  // 无论成功还是失败，都清空表格选中数据
  if (tableRef.value) {
    tableRef.value.clearSelection();
  }
  // 同时清空ids数组
  ids.value = [];
};

// 导出PDF
const handleExportPDF = async (row: any) => {
  const success = await downloadFileToLocal(
    [row.id].join(','),
    'PDF_FILE',
    `${row.dlName || 'file'}.pdf`,
    'pdf'
  );
  dialogMessage(success);
};

// 导出WORD
const handleExportWORD = async (row: any) => {
  const success = await downloadFileToLocal(
    [row.id].join(','),
    'DOC_FILE',
    `${row.dlName || 'file'}.doc`,
    'word'
  );
  dialogMessage(success);

};

/** 著表格详情按钮操作 */
const isHandlePreviewPDF = ref(false);

const handleAllDetail = async (row: any, title?: string) => {
  isHandlePreviewPDF.value = false;
  viewDetailTitle.value = title || '查看详情';
  viewDetailRowData.value = row;
  viewDetailVisible.value = true;
};
const handlePreviewPDF = (row: any) => {
  handleAllDetail(row, '预览PDF');
  isHandlePreviewPDF.value = true;
}

/**
 * 判断文件是否可编辑
 * 条件：docSource为new/revise + docOperationSource为fromInput/autoCreate/manualRevision
 */
const canEditableStatus = (row: any) => {
  const validSource = ['new', 'revise'].includes(row.docSource);
  const validOperation = ['fromInput', 'autoCreate', 'manualRevision'].includes(row.docOperationSource);
  return validSource && validOperation;
}

/**
 * 判断文件是否可申请失效
 * 条件：docSource为new/revise + useState为active/replaceDeactiving
 */
const canInvalidStatus = (row: any) => {
  const validSource = ['new', 'revise'].includes(row.docSource);
  const validState = ['active', 'replaceDeactiving'].includes(row.useState);
  return validSource && validState;
};
const uplaodDialog = reactive({
  title: '查看附件',
  visible: false,
  loading: false,
  id: '',
});
// 附件上传按钮操作
const handleUpload = (row: any) => {
  uplaodDialog.id = row.id;
  uplaodDialog.visible = true;
}
/** 其他版本详情按钮操作 */
const handleAllDetailOther = async (row: any, title?: string) => {
  dialog.title = title || '历史版本详情';
  dialog.visible = true;
  dialog.loading = true;
  try {
    const res = await getFileDetail(row.id);
    dialog.form = res.data;
  } catch (error) {
    console.error('获取文件详情失败:', error);
    proxy?.$modal.msgError('获取文件详情失败');
  }
  dialog.loading = false;
};
const handleReplaceFile = (row: any) => {
  isHandlePreviewPDF.value = false;
  viewDetailTitle.value = '定位替代文件';
  viewDetailRowData.value = row;
  viewDetailVisible.value = true;
};

defineExpose({
  handleUpdate,
  handleDelete,
  handlePreviewPDF,
  handleBrowseHistory,
  handleUpload,
  canEditableStatus,
  canInvalidStatus,
  handleReplaceFile,
});
</script>
