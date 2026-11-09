<template>
  <el-form ref="attributeFormRef" :model="form" v-loading="loading" :rules="rules" label-width="auto">
    <el-card class="box-card" shadow="hover">
      <template #header>
        <div class="clearfix">
          <span>基础配置</span>
        </div>
      </template>
      <el-row :gutter="20">
        <el-col :span="8">
          <el-form-item label="字段名称" prop="label">
            <el-input v-model="form.label" clearable placeholder="请输入字段名称" />
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="表字段" prop="businessAttrId">
            <el-select v-model="form.businessAttrId" filterable clearable @change="businessChange" placeholder="请选择表字段">
              <el-option v-for="dict in attrListTemp" :key="dict.businessAttrId" :label="dict.businessAttrName" :value="dict.businessAttrId"></el-option>
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="字段属性" prop="prop">
            <el-input v-model="form.prop" :disabled="Boolean(form.businessAttrId)" placeholder="请输入字段属性" />
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="排序" prop="sort">
            <el-input v-model="form.sort" type="number" placeholder="排序" />
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="固定宽度" prop="width">
            <el-input v-model.number="form.width" style="width: 100%;" placeholder="请输入宽度">
              <template #append>px</template>
            </el-input>
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="最小宽度" prop="minWidth">
            <el-input v-model.number="form.minWidth" style="width: 100%;" placeholder="请输入最小宽度">
              <template #append>px</template>
            </el-input>
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="展示类型" prop="type">
            <el-select v-model="form.fieldType" filterable placeholder="请选择展示类型">
              <el-option v-for="dict in system_display_type" :key="dict.value" :label="dict.label" :value="dict.value"></el-option>
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="是否显示" prop="isEnable">
            <el-radio v-model="form.isEnable" value="1">是</el-radio>
            <el-radio v-model="form.isEnable" value="0">否</el-radio>
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="是否排序" prop="sortable">
            <el-radio v-model="form.sortable" :value="true">是</el-radio>
            <el-radio v-model="form.sortable" :value="false">否</el-radio>
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="是否插槽" prop="isSlot">
            <el-radio v-model="form.isSlot" :value="true">是</el-radio>
            <el-radio v-model="form.isSlot" :value="false">否</el-radio>
          </el-form-item>
        </el-col>
        <el-col :span="8" v-if="form.isSlot">
          <el-form-item label="插槽名称" prop="slotName">
          <el-input v-model="form.slotName" placeholder="请输入插槽关键字" />
          </el-form-item>
        </el-col>
        <el-col :span="24">
          <el-form-item label="备注" prop="remark">
            <el-input v-model="form.remark" type="textarea" :autosize="{ minRows: 2 }" placeholder="请输入备注" />
          </el-form-item>
        </el-col>
      </el-row>
    </el-card>
    <el-card class="box-card" shadow="hover" v-if="form.fieldType !== 'selection'&&form.fieldType !== 'index'&&form.fieldType !== 'text'&&form.fieldType !== 'operation'">
      <template #header>
        <div class="clearfix">
          <span>其他配置</span>
        </div>
      </template>
      <el-row :gutter="20">
        <template v-if="form.fieldType == 'dict'">
          <el-col :span="24">
            <el-row :gutter="20">
              <el-col :span="8">
                <el-form-item label="是否字典" prop="isDict">
                  <el-radio v-model="form.isDict" :value="true">是</el-radio>
                  <el-radio v-model="form.isDict" :value="false">否</el-radio>
                </el-form-item>
              </el-col>
              <el-col :span="8" v-if="form.isDict">
                <el-form-item label="关联字典" prop="dict">
                  <el-select v-model="form.dict" filterable clearable>
                    <el-option v-for="item in typeOptions" :key="item.dictId" :label="item.dictName+'('+item.dictType+')'" :value="item.dictType" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="16" v-else>
                <attr-tag v-model="form.options"></attr-tag>
              </el-col>
            </el-row>
          </el-col>
        </template>
        <el-col :span="8" v-if="form.fieldType == 'time'">
          <el-form-item label="格式化日期" prop="formatTime">
            <el-autocomplete
              class="inline-input"
              v-model="form.formatTime"
              :fetch-suggestions="querySearch"
              placeholder="日期格式{y}年{m}月{d}日 {h}时{i}分{s}秒"
            ></el-autocomplete>
          </el-form-item>
        </el-col>
        <el-col :span="8" v-if="form.fieldType == 'price'">
          <el-form-item label="金额符号" prop="priceSymbol">
            <el-autocomplete
              class="inline-input"
              v-model="form.priceSymbol"
              :fetch-suggestions="queryPriceSymbol"
              placeholder="金额符号"
            ></el-autocomplete>
          </el-form-item>
          <el-form-item label="是否显示千分符" prop="separator">
              <el-radio v-model="form.separator" :value="true">是</el-radio>
              <el-radio v-model="form.separator" :value="false">否</el-radio>
          </el-form-item>
          <el-form-item label="保留几位小数" prop="separator">
            <el-input-number v-model="form.precision" :min="0" :max="20"></el-input-number>
          </el-form-item>
          <el-form-item label="是否强制补0" prop="forcePadZero">
              <el-radio v-model="form.forcePadZero" :value="true">是</el-radio>
              <el-radio v-model="form.forcePadZero" :value="false">否</el-radio>
          </el-form-item>
        </el-col>
      </el-row>
    </el-card>
  </el-form>
</template>

<script name="AttrTable" lang="ts" setup>
import { getPageAttribute } from '@/api/system/pageConfig/attribute';
import { optionselect } from '@/api/system/dict/type';
import { listData } from '@/api/system/dict/data';
import AttrTag from '@/views/system/pageConfig/component/attrTag.vue'
interface BusinessAttr {
  businessAttrCode: string;
  businessAttrName: string;
  [key: string]: any; // 允许其他属性
}
const props = defineProps({
  id: {
    type: String,
    default: ''
  },
  pageConfigId: {
    type: String,
    default: ''
  },
  attrList: {
    type: Array as PropType<BusinessAttr[]>,
    default: () => []
  }
});
const attrListTemp = computed(() => {
  let result = []
  if (props.attrList) {
    result = JSON.parse(JSON.stringify(props.attrList))
  }
  result.unshift(
    {
      businessAttrId: 'selection',
      businessAttrName: '选择框',
      businessAttrCode: 'selection',
    },{
      businessAttrId: 'index',
      businessAttrName: '序号',
      businessAttrCode: 'index',
    },{
      businessAttrId: 'operation',
      businessAttrName: '操作',
      businessAttrCode: 'operation'
    }
  )
  return result;
});

const { proxy } = getCurrentInstance() as ComponentInternalInstance;

const attributeFormRef = ref<ElFormInstance>();
const { system_display_type } = toRefs<any>(proxy?.useDict('system_display_type'));

const initFormData = {
  pageConfigId: props.pageConfigId,
  pageConfigAttributeId: '',
  pageConfigAttributeName: '',
  pageConfigAttributeType: '',
  pageConfigAttributeDesc: '',
  label: '',
  businessAttrId: '',
  prop: '',
  fieldType: 'text',
  sort: undefined,
  width: undefined,
  minWidth: undefined,
  isDict: true,
  dict: undefined,
  options: [],
  formatTime: '{y}-{m}-{d}',
  isSlot: false,
  isEnable: '1',
  sortable: false,
  slotName: '',
  priceSymbol: '',
  separator: false,
  forcePadZero: false,
  precision: undefined,
  remark: '',
};
const data = reactive({
  form: { ...initFormData },
  rules: {
    label: [{ required: true, message: '字段名称不能为空', trigger: 'change' }],
    businessAttrFiled: [{ required: true, message: '字段名不能为空', trigger: 'blur' }],
    prop: [{ required: true, message: '字段属性不能为空', trigger: 'change' }],
    fieldType: [{ required: true, message: '展示类型不能为空', trigger: 'blur' }],
    dict: [{ required: false, message: '请选择字典', trigger: 'change' }],
    slotName: [{ required: true, message: '插槽名称不能为空', trigger: 'change' }],
  }
});

const { form, rules } = toRefs(data);
const loading = ref(false)

// 获取详情
const getInfo = ()=>{
  reset();
  if(props.id){
    loading.value = true;
    getPageAttribute(props.id).then(res=>{
      const { pageConfigAttributeId, pageConfigAttributeValue } = res.data as any
      if(pageConfigAttributeValue){
        Object.assign(form.value, JSON.parse(pageConfigAttributeValue));
      }
      form.value.pageConfigAttributeId = pageConfigAttributeId;
    }).finally(()=>{
      loading.value = false;
    })
  }
}

const typeOptions = ref([]);
/** 查询字典类型列表 */
const getTypeList = async () => {
  const res = await optionselect();
  typeOptions.value = res.data;
};
const dictData = ref([]);
watch(()=>form.value.dict, async (val)=>{
  if(val){
    const res = await listData({ pageNum: 1, pageSize: 9999, dictName: '', dictType: val, dictLabel: '' });
    dictData.value = res.rows as any;
  }else{
    dictData.value = []
  }
})
const queryPriceSymbol = (q, cb) => {
  cb([{ value: '￥' },{value: '$'},{value: '€'} ])
}


// 格式化日期提示
const querySearch = (q, cb) => {
  cb([{ value: '{y}-{m}-{d} {h}:{i}:{s}' }])
}

// 表单重置
const reset = () => {
  form.value = { ...initFormData };
  attributeFormRef.value?.resetFields();
};

const businessChange = (val) => {
  const attr = attrListTemp.value.find(item=>item.businessAttrId == val)||{ businessAttrCode: '', businessAttrName: '' }
  form.value.prop = attr.businessAttrCode||''
  form.value.label = form.value.label||attr.businessAttrName||''
}

// 表单校验
const validate = (callBack) =>{
  attributeFormRef.value?.validate((valid: boolean) => {
    callBack(valid)
  });
}

// 获取数据
const achieveData = () => {
  return {
    pageConfigId: form.value.pageConfigId,
    pageConfigAttributeId: form.value.pageConfigAttributeId,
    pageConfigAttributeName: form.value.label,
    pageConfigAttributeType: form.value.fieldType,
    pageConfigAttributeSort: form.value.sort,
    pageConfigAttributeDesc: form.value.prop,
    pageConfigAttributeEnable: form.value.isEnable,
    remark: form.value.remark,
    pageConfigAttributeValue: JSON.stringify(form.value)
  }
}

onMounted(() => {
  getInfo();
  getTypeList();
})

defineExpose({
  achieveData,
  validate
})
</script>

<style lang="scss" scoped>
.box-card{
  margin: 0px 0 10px 0;
  .clearfix {
    text-align: center;
    font-size: 16px;
    color: #1890ff;
    font-weight: bold;
  }
}
</style>
