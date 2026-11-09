<template>
  <div class="upload-file">
    <el-upload
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
      <el-button type="primary">选取文件</el-button>
    </el-upload>
    <!-- 上传提示 -->
    <div v-if="showTip && !disabled" class="el-upload__tip">
      请上传
      <template v-if="fileSize">
        大小不超过 <b style="color: #f56c6c">{{ fileSize }}MB</b>
      </template>
      <template v-if="fileType">
        格式为 <b style="color: #f56c6c">{{ fileType.join('/') }}</b>
      </template>
      的文件
    </div>
    <!-- 文件列表 -->
    <transition-group class="upload-file-list el-upload-list el-upload-list--text" name="el-fade-in-linear" tag="ul">
      <li v-for="(file, index) in fileList" :key="file.uid" class="el-upload-list__item ele-upload-list__item-content">
        <span class="el-icon-document"> {{ getFileName(file.name) }} </span>
        <!-- <el-link :href="`${file.url}`" :underline="false" target="_blank">
          <span class="el-icon-document"> {{ getFileName(file.name) }} </span>
        </el-link> -->
        <div class="ele-upload-list__item-content-action">
          <template v-if="previewShow">
            <el-button type="primary" v-if="checkFileSuffix(file.fileSuffix)" link @click="handlePreview(file)">预览</el-button>
            <el-button type="primary" v-if="checkFileSuffixPdf(file.fileSuffix)" link @click="handlePdfView(file)">预览</el-button>
          </template>
          <el-button type="primary" v-if="downloadShow" link @click="handleDownload(file)">下载</el-button>
          <el-button type="danger" v-if="!disabled" link @click="handleDelete(index)">删除</el-button>
        </div>
      </li>
    </transition-group>
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
  </div>
</template>

<script setup lang="ts">
import { propTypes } from '@/utils/propTypes';
import { delOss, listByIds, listByBizIdAndType, uploadOssBizData, delOssByBizIdAndType } from '@/api/system/oss';
import { globalHeaders } from '@/utils/request';
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
  fileType: propTypes.array.def(['doc', 'docx', 'xls', 'xlsx', 'ppt', 'pptx', 'txt', 'pdf', 'png', 'jpg', 'jpeg']),
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
  },
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

const copyIds = ref([]);
const loading = ref(false);
// 查询OSS对象基于业务编号和业务类型
const getByBizIdAndType = async (bizId, bizType, covertType, isCopy) => {
  copyIds.value = []
  let temp = 1;
  // 首先将值转为数组
  let list;
  loading.value = true
  await listByBizIdAndType(bizId, bizType, {
    covertType
  }).then(res => {
    dialogImageUrlList.value = []
    list = res.data.map(oss => {
      oss = {
        name: oss.originalName,
        url: oss.url,
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
