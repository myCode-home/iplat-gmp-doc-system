<template>
  <div class="upload-file">
    <el-row :gutter="20">
      <el-col :span="8">
        <el-card style="width: 100%;min-height: 500px;">
          <template #header v-if="!disabled">
            <div class="card-header">
              <el-button type="primary" @click="handleTypeAdd" icon="Plus"></el-button>
              <el-button type="default" @click="handleTypeUpdate" v-if="currentRow&&currentRow.id != 0" icon="EditPen"></el-button>
              <el-button type="danger" @click="handleTypeDelete" v-if="currentRow&&currentRow.id != 0" icon="Delete"></el-button>
              <el-upload
                style="float: right;"
                ref="fileUploadRef"
                multiple
                :action="uploadFileUrl"
                :before-upload="handleBeforeUpload"
                :file-list="fileList"
                :limit="limit"
                :accept="fileAccept"
                :on-error="handleUploadError"
                :on-exceed="handleExceed"
                :on-success="handleUploadSuccess"
                :show-file-list="false"
                :headers="headers"
                class="upload-file-uploader"
                v-if="!disabled"
              >
                <!-- 上传按钮 -->
                <el-button type="primary">上传附件</el-button>
              </el-upload>
              <!-- 上传提示 -->
              <!-- <div v-if="showTip && !disabled" class="el-upload__tip">
                请上传
                <template v-if="fileSize">
                  大小不超过 <b style="color: #f56c6c">{{ fileSize }}MB</b>
                </template>
                <template v-if="fileType">
                  格式为 <b style="color: #f56c6c">{{ fileType.join('/') }}</b>
                </template>
                的文件
              </div> -->
            </div>
          </template>
          <el-tree
            style="width: 100%;"
            ref="treeRef"
            node-key="id"
            default-expand-all
            :expand-on-click-node="false"
            :props="{ label: 'ossTypeName', children: 'children' }"
            :data="treeData"
            highlight-current
            @node-click="handleNodeClick"
          />
        </el-card>
      </el-col>
      <el-col :span="16">
      <el-card style="width: 100%;min-height: 500px;">
        <!-- <template #header>
          <div class="card-header">
            <span>Card name</span>
          </div>
        </template> -->
        <!-- 文件列表 -->
        <transition-group class="upload-file-list el-upload-list el-upload-list--text" name="el-fade-in-linear" tag="ul">
          <el-table v-loading="loading" :data="fileList" style="width: 100%">
            <el-table-column prop="name" align="center" label="附件名称">
              <template #default="{ row }">
                {{ getFileName(row.name) }}
              </template>
            </el-table-column>
            <!-- <el-table-column prop="name" align="center" label="大小">
              <template #default="{ row }">
                {{ row.ext1 }}
              </template>
            </el-table-column> -->
            <el-table-column prop="createTime" align="center" label="上传时间">
              <template #default="{ row }">
                {{ parseTime(row.createTime) }}
              </template>
            </el-table-column>
            <el-table-column prop="createByNickName" align="center" label="上传人"></el-table-column>
            <el-table-column fixed="right" align="center" label="操作" width="150">
              <template #default="{ row, $index }">
                <template v-if="previewShow">
                  <el-tooltip content="预览" placement="top">
                    <el-button link type="primary"  v-if="checkFileSuffix(row.fileSuffix)" icon="View" @click="handlePreview(row)"></el-button>
                    <el-button link type="primary"  v-if="checkFileSuffixPdf(row.fileSuffix)" icon="View" @click="handlePdfView(row)"></el-button>
                  </el-tooltip>
                </template>
                <el-tooltip v-if="downloadShow" content="下载" placement="top">
                  <el-button link type="primary" icon="Download" @click="handleDownload(row)"></el-button>
                </el-tooltip>
                <el-tooltip v-if="!disabled" content="删除" placement="top">
                  <el-button link type="danger" icon="Delete" @click="handleDelete($index)"></el-button>
                </el-tooltip>
              </template>
            </el-table-column>
          </el-table>
        </transition-group>
      </el-card>
      </el-col>
    </el-row>
    <!--图片预览-->
    <el-image-viewer
      v-if="imgDialog.visible"
      ref="imagePreviewRef"
      style="width: 100px; height: 100px; display: none;"
      :url-list="dialogImageUrlList"
      show-progress
      :initial-index="0"
      @close="imgDialog.visible = false"
      fit="cover">
    </el-image-viewer>
    <!-- pdf预览 -->
    <el-dialog
      :title="pdfDialog.title"
      v-model="pdfDialog.visible"
      width="90%"
    >
      <pdf-view v-if="pdfDialog.visible" :pdfUrl="pdfDialog.url" />
    </el-dialog>
    <!-- 目录弹出窗 -->
    <el-dialog :title="typeDialog.title" v-model="typeDialog.visible" width="500px">
      <el-form
        ref="formRef"
        v-loading="formLoading"
        style="max-width: 600px"
        :model="form"
        :rules="rules"
        label-width="auto"
      >
        <el-form-item label="名称" prop="ossTypeName">
          <el-input v-model="form.ossTypeName" placeholder="请输入名称" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button v-model:buttonLoading="buttonLoading" type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { propTypes } from '@/utils/propTypes';
import { delOss, listByIds, listByBizIdAndType, uploadOssBizData, delOssByBizIdAndType } from '@/api/system/oss';
import { ossTypeListAll, getOssType, addOssType, uploadOssType, deleteOssType } from '@/api/system/oss/ossType';
import { globalHeaders } from '@/utils/request';
import { ElMessageBox, ElCheckbox } from 'element-plus'
import { useUserStore } from '@/store/modules/user';

const props = defineProps({
  modelValue: {
    type: [String, Object, Array],
    default: () => []
  },
  // 业务
  biz: {
    type: Object,
    required: true
  },
  // 数量限制
  limit: propTypes.number.def(500),
  // 大小限制(MB)
  fileSize: propTypes.number.def(30),
  // 文件类型, 例如['png', 'jpg', 'jpeg']
  // fileType: propTypes.array.def(['doc', 'docm', 'docx', 'xls', 'xlsx', 'ppt', 'pptx', 'txt', 'pdf', 'mp4', 'mp3', 'png', 'jpg', 'jpeg', '7z', 'zip', 'rar', 'tar', 'gif']),
  fileType: propTypes.array.def([]),
  // 是否显示提示
  isShowTip: propTypes.bool.def(true),
  // 禁用组件（仅查看文件）
  disabled: propTypes.bool.def(false),
  // 是否可预览
  previewShow: propTypes.bool.def(true),
  // 是否可下载
  downloadShow: propTypes.bool.def(true),
  getFileNumber: {
    type: Function,
    required: false,
  }
});

const { proxy } = getCurrentInstance() as ComponentInternalInstance;
const emit = defineEmits(['update:modelValue']);
const number = ref(0);
const uploadList = ref<any[]>([]);

const baseUrl = import.meta.env.VITE_APP_BASE_API;
const uploadFileUrl = ref(baseUrl + '/resource/oss/upload/relate'); // 上传文件服务器地址
const headers = ref(globalHeaders());

const fileList = ref<any[]>([]);
const showTip = computed(() => props.isShowTip && (props.fileType || props.fileSize));

const fileUploadRef = ref<ElUploadInstance>();

// 监听 fileType 变化，更新 fileAccept
const fileAccept = computed(() => props.fileType.map((type) => `.${type}`).join(','));

const imagePreviewRef = ref(null);
const dialogImageUrlList = ref([]);
// 判断文件类型是否为图片
const checkFileSuffix = (fileSuffix: any) => {
  if (fileSuffix != null) {
    const fileSuffixCopy = fileSuffix.toUpperCase();
    let arr = ["PNG", "JPG", "JPEG"];
    return arr.some(type => {
      return fileSuffixCopy.indexOf(type) > -1;
    });
  }
  return false
};
// 判断文件类型是否为pdf
const checkFileSuffixPdf = (fileSuffix: any) => {
  if (fileSuffix != null) {
    const fileSuffixCopy = fileSuffix.toUpperCase();
    let arr = ["PDF"];
    return arr.some(type => {
      return fileSuffixCopy.indexOf(type) > -1;
    });
  }
  return false
};

const imgDialog = reactive<DialogOption>({
  visible: false,
  title: ''
});
// 预览图片按钮操作
const handlePreview = (file) => {
  imgDialog.visible = true
  const url = file.url
  dialogImageUrlList.value.sort((x, y) => {
    if (x === url) return -1;
    if (y === url) return 1;
    return 0;
  });
  // proxy?.$nextTick(() => {
  //   imagePreviewRef.value!.showPreview()
  // })
};

const pdfDialog = reactive({
  visible: false,
  title: '',
  url: ''
});
// 预览pdf按钮操作
const handlePdfView = (file) =>{
  pdfDialog.visible = true;
  pdfDialog.title = file.name;
  pdfDialog.url = file.url;
}

watch(
  () => props.modelValue,
  async (val) => {
    if (val) {
      let temp = 1;
      // 首先将值转为数组
      let list: any[] = [];
      if (Array.isArray(val)) {
        list = val;
      } else {
        const res = await listByIds(val as string|number);
        dialogImageUrlList.value = [];
        list = res.data.map((oss) => {
          if (checkFileSuffix(oss.fileSuffix)) {
            dialogImageUrlList.value.push(oss.url)
          }
          return {
            name: oss.originalName,
            url: oss.url,
            createTime: oss['createTime'],
            createByNickName: oss['createByNickName'],
            ossId: oss.ossId,
            fileSuffix: oss.fileSuffix,
            fileName: oss.fileName,
            service: oss.service
          };
        });
      }
      // 然后将数组转为对象数组
      fileList.value = list.map((item) => {
        item = {
          name: item.name,
          url: item.url,
          createTime: item['createTime'],
          createByNickName: item['createByNickName'],
          ossId: item.ossId,
          fileSuffix: item.fileSuffix,
          fileName: item.fileName,
          service: item.service
        };
        item.uid = item.uid || new Date().getTime() + temp++;
        return item;
      });
    } else {
      fileList.value = [];
      return [];
    }
    if (typeof props.getFileNumber === "function") {
      props.getFileNumber(props.biz, fileList.value.length)
    }
  },
  { deep: true, immediate: true }
);

const ossIds = ref([]);
const isAdd = ref(false);
const copyIds = ref([]);
const loading = ref(false);
// 查询OSS对象基于业务编号和业务类型
const getByBizIdAndType = async (bizId, bizType, covertType, isCopy, query = {}) => {
  copyIds.value = []
  let temp = 1;
  // 首先将值转为数组
  let list;
  loading.value = true
  const queryData = { ...query, covertType }
  await listByBizIdAndType(bizId, bizType, queryData).then(res => {
    dialogImageUrlList.value = []
    list = res.data.map(oss => {
      oss = {
        name: oss.originalName,
        url: oss.url,
        createTime: oss['createTime'],
        createByNickName: oss['createByNickName'],
        ossId: oss.ossId,
        fileSuffix: oss.fileSuffix,
        fileName: oss.fileName,
        service: oss.service
      };
      if (checkFileSuffix(oss.fileSuffix)) {
        dialogImageUrlList.value.push(oss.url)
      }
      return oss;
    });
  }).finally(() => {
    loading.value = false
  })
  // 然后将数组转为对象数组
  fileList.value = list.map(item => {
    item = {
      name: item.name,
      url: item.url,
      createTime: item['createTime'],
      createByNickName: item['createByNickName'],
      ossId: item.ossId,
      fileSuffix: item.fileSuffix,
      fileName: item.fileName,
      service: item.service
    };
    item.uid = item.uid || new Date().getTime() + temp++;
    if (isCopy) {
      copyIds.value.push(item.ossId)
      ossIds.value.push(item.ossId)
    }
    return item;
  });
  if (typeof props.getFileNumber === "function") {
    props.getFileNumber(props.biz, fileList.value.length)
  }
}

watch(
  () => props.biz,
  async (val) => {
    const { bizId, bizType, covertType, copyBizId } = val
    if (bizId) {
      isAdd.value = false;
      uploadFileUrl.value = baseUrl + "/resource/oss/upload/relate?bizId=" + bizId + "&bizType=" + bizType;
      if (null != covertType) {
        uploadFileUrl.value += '&covertType='
        uploadFileUrl.value += covertType
      }
      getByBizIdAndType(bizId, bizType, covertType, false)
    } else {
      isAdd.value = true;
      if (null != copyBizId) {
        getByBizIdAndType(copyBizId, bizType, covertType, true)
      } else {
        fileList.value = [];
      }
      uploadFileUrl.value = baseUrl + "/resource/oss/upload/relate" // 上传文件服务器地址
    }
  },
  { deep: true, immediate: true }
);

// 上传前校检格式和大小
const handleBeforeUpload = (file: any) => {
  // 校检文件类型
  if (props.fileType.length) {
    const fileName = file.name.split('.');
    const fileExt = fileName[fileName.length - 1];
    const fileTypeCase = fileExt.toUpperCase();
    const allowedFileTypes = props.fileType.map((item: String)=>item.toUpperCase())
    const isTypeOk = !props.fileType.length || allowedFileTypes.indexOf(fileTypeCase) >= 0;
    if (!isTypeOk) {
      proxy?.$modal.msgError(`文件格式不正确, 请上传${props.fileType.join('/')}格式文件!`);
      return false;
    }
  }
  // 校检文件名是否包含特殊字符
  if (file.name.includes(',')) {
    proxy?.$modal.msgError('文件名不正确，不能包含英文逗号!');
    return false;
  }
  // 校检文件大小
  if (props.fileSize) {
    const isLt = file.size / 1024 / 1024 < props.fileSize;
    if (!isLt) {
      proxy?.$modal.msgError(`上传文件大小不能超过 ${props.fileSize} MB!`);
      return false;
    }
  }
  proxy?.$modal.loading('正在上传文件，请稍候...');
  number.value++;
  return true;
};

// 文件个数超出
const handleExceed = () => {
  proxy?.$modal.msgError(`上传文件数量不能超过 ${props.limit} 个!`);
};

// 上传失败
const handleUploadError = () => {
  proxy?.$modal.msgError('上传文件失败');
};

// 上传成功回调
const handleUploadSuccess = (res: any, file: UploadFile) => {
  if (res.code === 200) {
    // 获取文件后缀
    const getFileSuffix = (filename: string) => {
      const lastDot = filename.lastIndexOf('.');
      return lastDot > -1 ? filename.substring(lastDot + 1).toLowerCase() : '';
    };

    const fileSuffix = getFileSuffix(file.name);
    const newFile = {
      name: res.data.fileName,
      url: res.data.url,
      ossId: res.data.ossId,
      createTime: new Date().toISOString(), // 添加当前时间
      createByNickName: useUserStore().nickname, // 添加当前用户昵称
      fileSuffix: fileSuffix,
      fileName: file.name
    };

    // 如果是图片文件，添加到预览列表
    if (checkFileSuffix(fileSuffix)) {
      dialogImageUrlList.value.push(res.data.url);
    }

    uploadList.value.push(newFile);
    uploadedSuccessfully();
  } else {
    number.value--;
    proxy?.$modal.closeLoading();
    proxy?.$modal.msgError(res.msg);
    fileUploadRef.value?.handleRemove(file);
    uploadedSuccessfully();
  }
};


/** 下载按钮操作 */
const handleDownload = (file) => {
  proxy?.$download.oss(file.ossId);
};

// 删除文件
const handleDelete = (index: number) => {
  proxy.$modal.confirm('是否确认删除?').then(() => {
    const removeData = fileList.value[index]
    const ossId = removeData.ossId;
    const { bizId, bizType, covertType } = props.biz
    if(null != covertType){
      if (null != bizId) {
        return delOssByBizIdAndType(bizId, bizType, {
          covertType,
          delRelate: 'N'
        })
      } else {
        const index = copyIds.value.findIndex(id => id === ossId)
        //console.log(index)
        if (index === -1) {
          return delOss(ossId)
        }
      }
    }else{
      if (removeData.quoted === '1') {
        return delOssByBizIdAndType(bizId, bizType, {
          covertType,
          delRelate: 'N'
        })
      } else {
        return delOss(ossId)
      }
    }
  }).then(() => {
    const removeData = fileList.value[index];
    const ossId = removeData.ossId;
    fileList.value.splice(index, 1);
    emit('update:modelValue', listToString(fileList.value));
    if (isAdd.value && ossIds.value.length > 0) {
      ossIds.value = ossIds.value.filter(id => id !== ossId);
    }
  })
};

// 上传结束处理
const uploadedSuccessfully = () => {
  if (number.value > 0 && uploadList.value.length === number.value) {
    fileList.value = fileList.value.filter((f) => f.url !== undefined).concat(uploadList.value);
    uploadList.value = [];
    number.value = 0;
    emit('update:modelValue', listToString(fileList.value));
    proxy?.$modal.closeLoading();
  }
};

// 获取文件名称
const getFileName = (name: string) => {
  // 如果是url那么取最后的名字 如果不是直接返回
  if (name.lastIndexOf('/') > -1) {
    return name.slice(name.lastIndexOf('/') + 1);
  } else {
    return name;
  }
};

// 对象转成指定字符串分隔
const listToString = (list: any[], separator?: string) => {
  let strs = '';
  separator = separator || ',';
  list.forEach((item) => {
    if (item.ossId) {
      strs += item.ossId + separator;
    }
  });
  return strs != '' ? strs.substring(0, strs.length - 1) : '';
};

/** 子组件获取数据方法 - 给父组件调用 - 新增取消时保存附件关联信息 */
const saveOssBizData = (bizId, bizType, covertType = undefined) => {
  if (isAdd.value && ossIds.value.length > 0) {
    uploadOssBizData({
      bizId, bizType, 'ossIds': ossIds.value, covertType
    }).finally(() => {
      ossIds.value = []
    });
  }
};

/** 子组件获取数据方法 - 给父组件调用 - 新增取消时删除附件 */
const cancelSaveOssBizData = () => {
  if (isAdd.value && ossIds.value.length > 0) {
    if (copyIds.value && copyIds.value.length > 0) {
      ossIds.value = ossIds.value.filter(element => !copyIds.value.includes(element));
    }
    if (ossIds.value.length > 0) {
      delOss(ossIds.value).finally(() => {
        ossIds.value = []
      });
    }
  }
};

// 返回文件列表数量
const existFiles = () => {
  return fileList.value.length;
};
// 返回文件列表数据
const getFileList = () => {
  return fileList.value;
};

const treeData = ref([]);
const currentRow = ref<any>({});
const treeRef = ref();
// 获取文件目录
const getOssTypeList = (callBack?: Function) =>{
  ossTypeListAll({bizId: props.biz.bizId, bizType: props.biz.bizType}).then(res => {
    const rootData = [{
      ossTypeName: '附件目录',
      id: '0',
      ossTypeValue: ''
    }]
    const data = proxy?.handleTree(rootData.concat(res.data), 'id', 'parentId');
    treeData.value = data;
    if(res.data&&res.data.length==0||currentRow.value?.id == undefined){
      setCurrentById('0');
    }
    if(callBack&&callBack instanceof Function){
      callBack();
    }
  })
}
// 根据id默认选中
const setCurrentById = (id)=>{
  proxy?.$nextTick(() => {
    if (treeRef.value && id) {
      // 使用 setCurrentKey 方法设置当前选中的节点
      treeRef.value.setCurrentKey(id);
      // 获取当前选中节点的数据
      const currentNode = treeRef.value.getCurrentNode();
      if (currentNode) {
        currentRow.value = currentNode;
      }
    }
  });
};

const typeDialog = reactive({
  visible: false,
  title: ''
});
const formRef = ref();
const initFormData = {
  ossTypeName: '',
  bizId: props.biz.bizId,
  bizType: props.biz.bizType
};
const data = reactive<PageData<any, any>>({
  form: { ...initFormData },
  queryParams: reactive({
    pageNum: 1,
    pageSize: 10,
  }),
  rules: {
    ossTypeName: [{ required: true, message: '名称不能为空', trigger: 'blur' }],
  }
});

const { form, rules } = toRefs(data);
const formLoading = ref(false);
const handleTypeAdd = () =>{
  reset();
  typeDialog.visible = true;
  typeDialog.title = '新增附件分类';
  form.value.parentId = currentRow.value.id;
};
const handleTypeUpdate = () =>{
  reset();
  formLoading.value = true;
  getOssType(currentRow.value.id).then(res => {
    form.value = res.data;
    typeDialog.visible = true;
    typeDialog.title = '修改附件分类';
  }).finally(()=>{
    formLoading.value = false;
  })
};
const deleteFlag = ref(true);
const handleTypeDelete = () =>{
  const { id, ossTypeName } = currentRow.value as any;
  ElMessageBox({
    title: '删除分类',
    message: () => h('div', null, [
      h('p', { style: 'margin-bottom: 10px' }, `确定要删除 ${ossTypeName} 分类吗？`),
      // 修复 checkbox 绑定问题
      h(ElCheckbox, {
        // Vue 3 中正确的双向绑定方式
        modelValue: deleteFlag.value,
        'onUpdate:modelValue': (val: boolean) => {
          deleteFlag.value = val;
        }
      }, `同时删除 ${ossTypeName} 下附件`)
    ]),
    showCancelButton: true,
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    beforeClose: (action, instance, done) => {
      if (action === 'confirm') {
        done();
      } else {
        done();
      }
    }
  }).then(() => {
    const params = deleteFlag.value ? {
      removeFile: '1'
    } : {
      removeFile: '0'
    };
    return deleteOssType(id, params);
  }).then((res) => {
    getOssTypeList(()=>{
      setCurrentById(currentRow.value.parentId);
    });
    proxy.$modal.msgSuccess("删除成功");
  }).catch(() => {
    // deleteFlag.value = false;
  });
  // proxy.$modal.confirm(`是否确认删除 ${ossTypeName} 分类吗?`).then(() => {
  //   return deleteOssType(id)
  // }).then(() => {
  //   getOssTypeList();
  //   setCurrentById(currentRow.value.parentId);
  // })
};
const buttonLoading = ref(false);
const submitForm = () => {
  formRef.value.validate((valid: boolean) => {
    if (valid) {
      buttonLoading.value = true;
      if(form.value.id){
        uploadOssType(form.value).then(() => {
          getOssTypeList();
          typeDialog.visible = false;
          proxy.$modal.msgSuccess("操作成功");
        }).finally(() => {
          buttonLoading.value = false;
        });
      }else{
        addOssType(form.value).then((res)=>{
          getOssTypeList(()=>{
            setCurrentById(res.data);
          });
          typeDialog.visible = false;
          proxy.$modal.msgSuccess("操作成功");
        }).finally(() => {
          buttonLoading.value = false;
        });
      }
    }
  });
};
// 取消弹窗
const cancel = ()=>{
  typeDialog.visible = false;
}

// 表单重置
const reset = () => {
  form.value = { ...initFormData };
};
// tree 点击事件
const handleNodeClick = (node) =>{
  currentRow.value = node;
}
watch(()=>currentRow.value,() => {
  const { bizId, bizType, covertType, copyBizId } = props.biz
  uploadFileUrl.value = baseUrl + "/resource/oss/upload/relate?bizId=" + bizId + "&bizType=" + bizType;
  if (null != covertType) {
    uploadFileUrl.value += '&covertType='
    uploadFileUrl.value += covertType
  }
  if(currentRow.value.id != '0'){
    uploadFileUrl.value += ('&bizSecondType='+currentRow.value.ossTypeValue)
  }
  getByBizIdAndType(bizId, bizType, covertType, false, { bizSecondType: currentRow.value.id=='0'?'':currentRow.value.ossTypeValue })
},{ immediate: true})

onMounted(() => {
  treeData.value = [];
  // 获取文件目录
  getOssTypeList();
})

defineExpose({
  existFiles,
  getFileList
})

</script>

<style lang="scss" scoped>
.upload-file-uploader {
  margin-bottom: 5px;
}

.upload-file-list .el-upload-list__item {
  border: 1px solid #e4e7ed;
  line-height: 2;
  margin-bottom: 10px;
  position: relative;
}

.upload-file-list .ele-upload-list__item-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: inherit;
  padding: 0 8px;
}

.ele-upload-list__item-content-action .el-link {
  margin-right: 10px;
}
</style>
