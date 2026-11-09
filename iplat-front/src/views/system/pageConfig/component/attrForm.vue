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
              <el-option v-for="dict in attrList" :key="dict.businessAttrId" :label="dict.businessAttrName" :value="dict.businessAttrId"></el-option>
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="字段属性" prop="prop">
            <el-input v-model="form.prop" :disabled="Boolean(form.businessAttrId)" placeholder="请输入字段属性" />
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="字段类型" prop="type">
            <el-select v-model="form.fieldType" filterable placeholder="请选择字段类型">
              <el-option v-for="dict in system_field_type" :key="dict.value" :label="dict.label" :value="dict.value"></el-option>
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="提示信息" prop="placeholder">
            <el-input v-model="form.placeholder" placeholder="请输入提示信息" />
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="排序" prop="sort">
            <el-input v-model="form.sort" type="number" placeholder="排序" />
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="默认值" prop="defaultValue">
            <el-select
              v-if="(form.fieldType == 'select' || form.fieldType == 'checkBox' || form.fieldType == 'radio') && form.isDict && form.dict"
              v-model="form.defaultValue"
              :multiple="form.fieldType == 'checkBox' || (form.fieldType == 'select' && form.multiple)"
              filterable
              clearable
            >
              <el-option
                v-for="item in dictData"
                :key="item.dictValue"
                :label="item.dictLabel + '(' + item.dictValue + ')'"
                :value="item.dictValue"
              />
            </el-select>
            <el-select
              v-else-if="(form.fieldType == 'select' || form.fieldType == 'checkBox' || form.fieldType == 'radio') && !form.isDict && form.options && form.options.length > 0"
              v-model="form.defaultValue"
              :multiple="form.fieldType == 'checkBox' || (form.fieldType == 'select' && form.multiple)"
              filterable
              clearable
            >
              <el-option
                v-for="item in form.options"
                :key="item.value"
                :label="item.label + '(' + item.value + ')'"
                :value="item.value"
              />
            </el-select>
            <el-input v-else v-model="form.defaultValue" clearable placeholder="请输入默认值" />
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="所属区域" prop="region">
            <el-input v-model="form.region" placeholder="请输入所属区域" />
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="占位宽度" prop="span">
            <el-input-number
              v-model="form.span"
              :step="1"
              :min="1"
              :max="24"
              style="width: 100%"
              :controls="false"
              placeholder="请输入占位宽度(24等份)"
            ></el-input-number>
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="是否显示" prop="isEnable">
            <el-radio v-model="form.isEnable" value="1">是</el-radio>
            <el-radio v-model="form.isEnable" value="0">否</el-radio>
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="是否必填" prop="required">
            <el-radio v-model="form.required" :value="true">是</el-radio>
            <el-radio v-model="form.required" :value="false">否</el-radio>
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="是否禁用" prop="disabled">
            <el-radio v-model="form.disabled" :value="true">是</el-radio>
            <el-radio v-model="form.disabled" :value="false">否</el-radio>
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="是否只读" prop="readonly">
            <el-radio v-model="form.readonly" :value="true">是</el-radio>
            <el-radio v-model="form.readonly" :value="false">否</el-radio>
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="是否可清除" prop="clearable">
            <el-radio v-model="form.clearable" :value="true">是</el-radio>
            <el-radio v-model="form.clearable" :value="false">否</el-radio>
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="手机端显示" prop="isMobile">
            <el-radio v-model="form.isMobile" :value="true">是</el-radio>
            <el-radio v-model="form.isMobile" :value="false">否</el-radio>
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
          <el-row :gutter="20">
            <el-col :span="24">
              <el-form-item label="校验规则" prop="rulesReg">
                <el-radio-group v-model="form.rulesReg">
                  <el-radio :label="3" @click.native.prevent="radioClick(dict.value)" v-for="dict in system_form_reg" :key="dict.value" :value="dict.value">{{ dict.label }}</el-radio>
                </el-radio-group>
              </el-form-item>
            </el-col>
            <el-col :span="8" v-if="form.rulesReg == 'other'">
              <el-form-item label="正则表达式" prop="rulesPattern">
                <el-input v-model="form.rulesPattern" placeholder="请输入正则表达式" />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="校验失败提示" prop="ruleMsg">
                <el-input v-model="form.ruleMsg" placeholder="请输入规则校验失败提示" />
              </el-form-item>
            </el-col>
          </el-row>
        </el-col>
        <el-col :span="24">
          <el-form-item label="备注" prop="remark">
            <el-input v-model="form.remark" type="textarea" :autosize="{ minRows: 2, maxRows: 5 }" placeholder="请输入备注" />
          </el-form-item>
        </el-col>
      </el-row>
    </el-card>
    <!-- 额外属性配置组件 -->
    <AttrFormSetting v-model="form" :type-options="typeOptions" @dict-change="dictChange" />
    <!-- 事件绑定组件 -->
    <AttrEvent v-model="form" />
  </el-form>
</template>

<script name="AttrForm" lang="ts" setup>
import { getPageAttribute } from '@/api/system/pageConfig/attribute';
import { optionselect } from '@/api/system/dict/type';
import { listData } from '@/api/system/dict/data';
import AttrFormSetting from '@/views/system/pageConfig/component/attrFormSetting.vue';
import AttrEvent from '@/views/system/pageConfig/component/attrEvent.vue';
import type { FormItemRule } from 'element-plus'

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
const { proxy } = getCurrentInstance() as ComponentInternalInstance;

const attributeFormRef = ref<ElFormInstance>();
const { system_field_type, system_form_reg } = toRefs<any>(proxy?.useDict('system_field_type', 'system_form_reg'));

const initFormData = {
  pageConfigId: props.pageConfigId,
  pageConfigAttributeId: '',
  pageConfigAttributeName: '',
  pageConfigAttributeType: '',
  pageConfigAttributeDesc: '',
  label: '',
  businessAttrId: '',
  prop: '',
  fieldType: 'input',
  type:'',
  placeholder: '',
  sort: undefined,
  isEnable: '1',
  required: false,
  disabled: false,
  readonly: false,
  region: undefined,
  defaultValue: undefined,
  isRadioButton: false,
  span: undefined,
  isDict: true,
  dict: undefined,
  options: [],
  events: [],
  isSlot: false,
  slotName: '',
  clearable: true,
  isMobile: true,
  isMultiple: false,
  selectType: '',
  suffixIcon: '',
  rules: undefined,
  rulesReg: '',
  rulesPattern: '',
  ruleMsg: '',
  remark: '',
  textColor: '',
  fill: '',
  isAutosize: '',
  multiple: false,
  allowCreate: false,
  multipleLimit: 0, // 多选时最多选择几项
  collapseTags: false, //多选时是否将选中值按文字的形式展示
  collapseTagsTooltip: false,
  filterable: false, // Select 组件是否可筛选
  isBorder: false,
  min: 0, // checkout最少勾选几项
  max: 0, // checkout最多勾选几项
  isRange: false, // 是否为时间范围选择
  arrowControl: false,
  format: undefined,
  start: '09:00', // 开始时间
  end: '18:00', // 结束时间
  step: '00:30', // 间隔时间
  placement: 'bottom',
  showFooter: false,
  showConfirm: false,
  showWeekNumber: false,
  editable: false,
} as any;
const validateRule = (rule: any, value: string, callback: (error?: string | Error) => void) => {
  if (!value) {
    callback(new Error('请输入正则表达式'));
  } else {
    try {
      // 使用RegExp构造函数而不是eval来验证正则表达式
      new RegExp(value);
      callback();
    } catch (e) {
      callback(new Error('请输入正确的正则表达式'));
    }
  }
};
const data = reactive({
  form: { ...initFormData },
  rules: {
    label: [{ required: true, message: '字段名称不能为空', trigger: 'change' }],
    businessAttrFiled: [{ required: true, message: '字段名不能为空', trigger: 'blur' }],
    prop: [{ required: true, message: '字段属性不能为空', trigger: 'change' }],
    fieldType: [{ required: true, message: '字段类型不能为空', trigger: 'blur' }],
    dict: [{ required: false, message: '请选择字典', trigger: 'change' }],
    slotName: [{ required: true, message: '插槽名称不能为空', trigger: 'change' }],
    selectType: [{ required: true, message: '请选择数据来源', trigger: 'change' }],
    rulesPattern: [
      { required: true, message: '请输入正则表达式', trigger: 'change' },
      {  validator: validateRule, trigger: ['blur', 'change'] }
    ] as FormItemRule[],
  }
});

const { form, rules } = toRefs(data);
const loading = ref(false);

// 获取详情
const getInfo = () => {
  reset();
  if (props.id) {
    loading.value = true;
    getPageAttribute(props.id)
      .then((res) => {
        const { pageConfigAttributeId, pageConfigAttributeValue } = res.data as any;
        if (pageConfigAttributeValue) {
          form.value = JSON.parse(pageConfigAttributeValue);
          // 防止因为批量添加没有默认属性导致页面显示和实际值不符的情况
          // Object.assign(form.value, JSON.parse(pageConfigAttributeValue));
        }
        form.value.pageConfigAttributeId = pageConfigAttributeId;
      })
      .finally(() => {
        loading.value = false;
      });
  }
};

const typeOptions = ref([]);
/** 查询字典类型列表 */
const getTypeList = async () => {
  const res = await optionselect();
  typeOptions.value = res.data;
};
const dictData = ref([]);
const dictChange = () => {
  form.value.defaultValue = '';
};
watch(
  () => form.value.dict,
  async (val) => {
    if (val) {
      const res = await listData({ pageNum: 1, pageSize: 9999, dictName: '', dictType: val, dictLabel: '' });
      dictData.value = res.rows as any;
    } else {
      dictData.value = [];
    }
  }
);

// 表单重置
const reset = () => {
  form.value = { ...initFormData };
  attributeFormRef.value?.resetFields();
};

const businessChange = (val) => {
  const attr = props.attrList.find((item) => item.businessAttrId == val) || { businessAttrCode: '', businessAttrName: '' };
  form.value.prop = attr.businessAttrCode || '';
  form.value.label = form.value.label || attr.businessAttrName || '';
};

// 表单校验
const validate = (callBack) => {
  attributeFormRef.value?.validate((valid: boolean) => {
    callBack(valid);
  });
};

const radioClick = (e) => {
  e === form.value.rulesReg ? ( form.value.rulesReg = '') : (form.value.rulesReg = e)
};

const handleForm = () =>{
  if(form.value.rulesReg){
    form.value.rules = [
      {
        pattern: form.value.rulesReg=='other' ? form.value.rulesPattern : form.value.rulesReg,
        message: form.value.ruleMsg||'格式校验不通过',
        trigger: ['blur', 'change']
      }
    ]
  }else{
    form.value.rules = '';
  }
  if(form.value.isAutosize){
    if(form.value.autosizeMin||form.value.autosizeMax){
      form.value.autosize = { maxRows: form.value.autosizeMax, minRows: form.value.autosizeMin }
    }else {
      form.value.autosize = true
    }
  }else {
    form.value.autosize = false
  }
};

// 获取数据
const achieveData = () => {
  // 表单数据字段额外处理
  handleForm();
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
  };
};

onMounted(() => {
  getInfo();
  getTypeList();
});

defineExpose({
  achieveData,
  validate
});
</script>

<style lang="scss" scoped>
.box-card {
  margin: 0px 0 10px 0;
  .clearfix {
    text-align: center;
    font-size: 16px;
    color: #1890ff;
    font-weight: bold;
  }
}
</style>
