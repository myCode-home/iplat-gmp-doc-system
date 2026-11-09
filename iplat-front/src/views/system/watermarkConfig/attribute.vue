<template>
  <div class="">
    <el-card class="box-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <span>{{ extendedName?`【${extendedName}】`:'' }}水印属性</span>
          <el-button v-if="extendedId" v-hasPermi="['system:extended:add']" @click="handleAdd" class="button" type="primary" size="small" icon="Plus" style="float: right;">新增</el-button>
        </div>
      </template>
      <!-- <el-row :gutter="10" class="mb8">
        <el-col :span="1.5">
          <el-button v-hasPermi="['system:extended:add']" type="primary" plain icon="Plus" @click="handleAdd">新增</el-button>
        </el-col>
        <el-col :span="1.5">
          <el-button v-hasPermi="['system:extended:edit']" type="success" plain icon="Edit" :disabled="single" @click="handleUpdate(selectRow)"
            >修改</el-button
          >
        </el-col>
        <el-col :span="1.5">
          <el-button v-hasPermi="['system:extended:remove']" type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete">
            删除
          </el-button>
        </el-col>
        <el-col :span="1.5" v-if="false">
          <el-button v-hasPermi="['system:extended:export']" type="warning" plain icon="Download" @click="handleExport">导出</el-button>
        </el-col>
        <right-toolbar v-model:show-search="showSearch" @query-table="getList"></right-toolbar>
      </el-row> -->
      <transition :enter-active-class="proxy?.animate.searchAnimate.enter" :leave-active-class="proxy?.animate.searchAnimate.leave">
        <div v-show="showSearch">
          <el-form ref="queryFormRef" :model="queryParams" size="small" :inline="true" label-width="auto" @submit.native.prevent="">
            <el-form-item label="水印属性名称" prop="width">
              <el-input v-model="queryParams.width" placeholder="请输入水印属性名称" clearable @change="handleQuery" @keyup.enter="handleQuery" />
            </el-form-item>
            <el-form-item class="noMarginRight">
              <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
              <el-button icon="Refresh" @click="resetQuery">重置</el-button>
            </el-form-item>
          </el-form>
        </div>
      </transition>
      <el-table v-loading="loading" border :data="extendedAttributeList" size="small" @selection-change="handleSelectionChange">
        <el-table-column type="expand">
          <template #default="props">
            <el-form label-position="left" inline size="small" label-width="auto" label-suffix="：" class="demo-table-expand p-2">
              <el-form-item label="水印属性id">
                <span>{{ props.row.id }}</span>
              </el-form-item>
              <el-form-item label="水印属性规则" style="width: 100%;">
                <span>{{ props.row.positionX }}</span>
              </el-form-item>
              <el-form-item label="水印属性描述" style="width: 100%;">
                <span>{{ props.row.height }}</span>
              </el-form-item>
              <el-form-item label="备注" style="width: 100%;">
                <span>{{ props.row.positionY }}</span>
              </el-form-item>
              <el-form-item label="创建时间">
                <span>{{ props.row.createTime }}</span>
              </el-form-item>
            </el-form>
          </template>
        </el-table-column>
        <el-table-column label="编号" align="center" prop="id" v-if="false" />
        <el-table-column label="水印方向" align="center" prop="aspect">
          <template #default="{row}">
            <dict-tag :options="system_watermark_aspect" :value="row.aspect" />
          </template>
        </el-table-column>
        <el-table-column label="水印类型" align="center" prop="waterType">
          <template #default="{row}">
            <dict-tag :options="system_watermark_type" :value="row.waterType" />
          </template>
        </el-table-column>
        <el-table-column label="备注" align="center" prop="remark" show-overflow-tooltip />
        <!-- <el-table-column label="水印属性规则" align="center" prop="positionX" /> -->
        <!-- <el-table-column label="备注" align="center" prop="positionY" show-overflow-tooltip /> -->
        <el-table-column width="110" label="操作" align="center" class-name="small-padding fixed-width">
          <template #default="scope">
            <el-tooltip content="预览" placement="top">
              <el-button v-hasPermi="['system:extended:edit']" link type="primary" icon="View" @click="submitView(scope.row)"></el-button>
            </el-tooltip>
            <el-tooltip content="修改" placement="top">
              <el-button v-hasPermi="['system:extended:edit']" link type="primary" icon="Edit" @click="handleUpdate(scope.row)"></el-button>
            </el-tooltip>
            <el-tooltip content="删除" placement="top">
              <el-button v-hasPermi="['system:extended:remove']" link type="danger" icon="Delete" @click="handleDelete(scope.row)"></el-button>
            </el-tooltip>
          </template>
        </el-table-column>
      </el-table>
      <pagination v-show="total > 0" v-model:page="queryParams.pageNum" size="small" v-model:limit="queryParams.pageSize" :total="total" @pagination="getList" />
    </el-card>

    <!-- 添加或修改水印属性对话框 -->
    <el-dialog v-model="dialog.visible" :title="dialog.title" width="90%" append-to-body>
      <el-form ref="attributeFormRef" :model="form" v-loading="dialog.loading" :rules="rules" label-width="auto">
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="水印方向" prop="aspect">
              <el-select v-model="form.aspect"  placeholder="请选择水印方向">
                <el-option v-for="dict in system_watermark_aspect" :key="dict.value" :label="dict.label" :value="dict.value"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="水印类型" prop="waterType">
              <el-select v-model="form.waterType"  placeholder="请选择水印类型">
                <el-option v-for="dict in system_watermark_type" :key="dict.value" :label="dict.label" :value="dict.value"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="字号" prop="size">
              <el-input v-model="form.size" placeholder="请输入字号" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="X轴" prop="positionX">
              <template #label>
                <span>
                  X轴
                  <el-tooltip
                    class="box-item"
                    effect="dark"
                    content="左下角为坐标起点"
                    placement="top"
                  >
                    <el-icon><QuestionFilled/></el-icon>
                  </el-tooltip>
                </span>
              </template>
              <el-input v-model="form.positionX" placeholder="请输入X轴" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="Y轴" prop="positionY">
              <template #label>
                <span>
                  Y轴
                  <el-tooltip
                    class="box-item"
                    effect="dark"
                    content="左下角为坐标起点"
                    placement="top"
                  >
                    <el-icon><QuestionFilled/></el-icon>
                  </el-tooltip>
                </span>
              </template>
              <el-input v-model="form.positionY" placeholder="请输入Y轴" />
            </el-form-item>
          </el-col>
          <!-- <el-col :span="8">
            <el-form-item label="宽度" prop="width">
              <el-input v-model="form.width" placeholder="请输入宽度" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="高度" prop="height">
              <el-input v-model="form.height" placeholder="请输入高度" />
            </el-form-item>
          </el-col> -->
          <el-col :span="8">
            <el-form-item label="角度" prop="angle">
              <el-input v-model="form.angle" placeholder="请输入角度" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="透明度" prop="opacity">
              <el-input v-model="form.opacity" placeholder="请输入透明度" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="水印颜色" prop="waterColor">
              <el-color-picker v-model="form.waterColor"></el-color-picker>
              <!-- <el-input v-model="form.waterColor" placeholder="请输入水印颜色" /> -->
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="是否平铺" prop="tile">
              <el-select v-model="form.tile"  placeholder="请选择是否平铺">
                <el-option v-for="dict in system_yes_no" :key="dict.value" :label="dict.label" :value="dict.value"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="水印内容" prop="content">
              <el-input v-model="form.content" type="textarea" autosize placeholder="请输入水印内容" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="其他内容" prop="contentExtra">
              <el-input v-model="form.contentExtra" type="textarea" autosize placeholder="请输入其他内容" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" :autosize="{ minRows: 2 }" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button :loading="buttonLoading" type="warning" @click="submitView">预 览</el-button>
          <el-button :loading="buttonLoading" type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>
    <el-dialog v-model="pdfDialog.visible" :title="pdfDialog.title" width="90%" append-to-body>
      <PdfView v-if="pdfDialog.visible" text="" :pdfUrl="pdfUrl"></PdfView>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="pdfDialog.visible = false">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="ExtendedAttribute" lang="ts">
import { listAttrWatermark, addAttrWatermark, getAttrWatermark, delAttrWatermark, viewAttrWatermark } from '@/api/system/watermarkConfig/index';

const props = defineProps({
  extendedId: {
    type: String,
    default: '',
    required: true
  },
  extendedName: {
    type: String,
    default: ''
  }
});

const { proxy } = getCurrentInstance() as ComponentInternalInstance;
const { system_yes_no, system_watermark_aspect, system_watermark_type } = toRefs<any>(proxy?.useDict('system_yes_no', 'system_watermark_aspect', 'system_watermark_type'));

const extendedAttributeList = ref([]);
const buttonLoading = ref(false);
const loading = ref(false);
const showSearch = ref(true);
const ids = ref<Array<string | number>>([]);
const names = ref<Array<string | number>>([]);
const single = ref(true);
const selectRow = ref({});
const multiple = ref(true);
const total = ref(0);

const queryFormRef = ref<ElFormInstance>();
const attributeFormRef = ref<ElFormInstance>();

const dialog = reactive({
  visible: false,
  loading: false,
  title: ''
});

const initFormData = {
  extendedId: '',
  watermarkId: '',
  id: null,
  waterType: 'text',
  size: '',
  width: '',
  height: '',
  positionX: '',
  positionY: '',
  angle: '',
  opacity: '',
  tile: '1',
  aspect: '',
  waterColor: '',
  content: '',
  contentExtra: '',
  remark: '',
  orderNum: ''
};

// hex颜色转rgb颜色
const hexToRgb = (str: string): string[] => {
  str = str.replace('#', '');
  const hexs = str.match(/../g);
  for (let i = 0; i < 3; i++) {
    if (hexs) {
      hexs[i] = String(parseInt(hexs[i], 16));
    }
  }
  return hexs ? hexs : [];
};

// rgb颜色转Hex颜色
const rgbToHex = (r: string, g: string, b: string) => {
  const hexs = [Number(r).toString(16), Number(g).toString(16), Number(b).toString(16)];
  for (let i = 0; i < 3; i++) {
    if (hexs[i].length == 1) {
      hexs[i] = `0${hexs[i]}`;
    }
  }
  return `#${hexs.join('')}`;
};

const data = reactive({
  form: { ...initFormData },
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    extendedId: '',
    watermarkId: '',
    id: '',
    waterType: '',
    size: '',
    width: '',
    height: '',
    positionX: '',
    positionY: '',
    angle: '',
    opacity: '',
    tile: '',
    aspect: '',
    waterColor: '',
    content: '',
    contentExtra: '',
    orderNum: ''
  },
  rules: {
    aspect: [{ required: true, message: '水印方向不能为空', trigger: 'blur' }],
    waterType: [{ required: true, message: '水印类型不能为空', trigger: 'blur' }],
    size: [{ required: true, message: '字号不能为空', trigger: 'blur' }],
    positionX: [{ required: true, message: 'X轴不能为空', trigger: 'blur' }],
    positionY: [{ required: true, message: 'Y轴不能为空', trigger: 'blur' }],
    angle: [{ required: true, message: '角度不能为空', trigger: 'blur' }],
    opacity: [{ required: true, message: '透明度不能为空', trigger: 'blur' }],
    tile: [{ required: true, message: '是否平铺不能为空', trigger: 'change' }],
    // content: [{ required: true, message: '水印内容不能为空', trigger: 'change' }]
  }
});

const { queryParams, form, rules } = toRefs(data);


watch(() => props.extendedId,(newValue) => {
    if (newValue) {
      getList();
    }else{
      extendedAttributeList.value = [];
    }
  }
);

/** 查询水印属性列表 */
const getList = async () => {
  loading.value = true;
  // queryParams.value.extendedId = props.extendedId;
  const res = await listAttrWatermark(props.extendedId);
  extendedAttributeList.value = res.data;
  total.value = res.total;
  loading.value = false;
};

// 取消按钮
const cancel = () => {
  reset();
  dialog.loading = false;
  dialog.visible = false;
};

// 表单重置
const reset = () => {
  form.value = { ...initFormData };
  attributeFormRef.value?.resetFields();
};

/** 搜索按钮操作 */
const handleQuery = () => {
  queryParams.value.pageNum = 1;
  getList();
};

/** 重置按钮操作 */
const resetQuery = () => {
  queryFormRef.value?.resetFields();
  handleQuery();
};

// 多选框选中数据
const handleSelectionChange = (selection:any) => {
  ids.value = selection.map((item:any) => item.id);
  names.value = selection.map((item:any) => item.width);
  single.value = selection.length != 1;
  multiple.value = !selection.length;
  selectRow.value = selection[0]||{};
};

/** 新增按钮操作 */
const handleAdd = () => {
  reset();
  form.value.watermarkId = props.extendedId;
  dialog.visible = true;
  dialog.loading = false;
  dialog.title = `添加【${props.extendedName}】水印属性`;
};

/** 修改按钮操作 */
const handleUpdate = async (row) => {
  reset();
  const _id = row?.id || ids.value[0];
  dialog.visible = true;
  dialog.loading = true;
  dialog.title = `修改【${props.extendedName}】水印属性`;
  const res = await getAttrWatermark(_id);
  
  // 如果后端存储的是RGB值（逗号分隔），则需要转换为16进制用于颜色选择器显示
  if (res.data.waterColor) {
    const colorParts = res.data.waterColor.split(',');
    if (colorParts.length === 3) {
      // 这是一个RGB值，转换为十六进制
      res.data.waterColor = rgbToHex(colorParts[0], colorParts[1], colorParts[2]);
    }
  }
  
  Object.assign(form.value, res.data);
  dialog.loading = false;
};
const pdfDialog = reactive({
  visible: false,
  loading: false,
  title: '水印预览'
});
const pdfUrl = ref('');
const submitView = async (row?: any) =>{
  const submitData = row?.id ? { ...row} : { ...form.value };
  if (submitData.waterColor) {
    // 将16进制颜色值转换为RGB并用逗号连接
    const rgbValues = hexToRgb(submitData.waterColor);
    if (rgbValues.length === 3) {
      submitData.waterColor = rgbValues.join(',');
    }
  }
  await viewAttrWatermark(submitData).then(async (res: any) => {
    const pdfBlob = new Blob([res], { type: 'application/pdf' })
    pdfUrl.value = URL.createObjectURL(pdfBlob)
    pdfDialog.visible = true;
  });
};

/** 提交按钮 */
const submitForm = () => {
  attributeFormRef.value?.validate(async (valid: boolean) => {
    if (valid) {
      buttonLoading.value = true;
      
      // 创建临时对象用于提交，将16进制颜色值转换为RGB逗号分隔格式
      const submitData = { ...form.value };
      if (submitData.waterColor) {
        // 将16进制颜色值转换为RGB并用逗号连接
        const rgbValues = hexToRgb(submitData.waterColor);
        if (rgbValues.length === 3) {
          submitData.waterColor = rgbValues.join(',');
        }
      }
      
      if (form.value.id) {
        await addAttrWatermark(submitData).finally(() => (buttonLoading.value = false));
      } else {
        await addAttrWatermark(submitData).finally(() => (buttonLoading.value = false));
      }
      proxy?.$modal.msgSuccess('操作成功');
      dialog.visible = false;
      dialog.loading = false;
      await getList();
    }
  });
};

/** 删除按钮操作 */
const handleDelete = async (row) => {
  const _ids = row?.id || ids.value;
  const _names = row?.width || names.value;
  await proxy?.$modal.confirm('是否确认选中的数据项？');
  loading.value = true;
  await delAttrWatermark(_ids).finally(() => (loading.value = false));
  await getList();
  proxy?.$modal.msgSuccess('删除成功');
};

/** 导出按钮操作 */
const handleExport = () => {
  proxy?.download(
    'system/extended-attribute/export',
    {
      ...queryParams.value
    },
    `extended-attribute_${new Date().getTime()}.xlsx`
  );
};

// onMounted(() => {
//   getList();
// });
</script>

<style scoped>
.demo-table-expand {
  font-size: 0;
}
.demo-table-expand label {
  width: 90px;
  color: #99a9bf;
}
.demo-table-expand .el-form-item {
  margin-right: 0;
  margin-bottom: 0;
  width: 50%;
}
</style>
