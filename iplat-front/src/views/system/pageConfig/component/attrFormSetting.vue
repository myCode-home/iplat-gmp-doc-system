<template>
  <el-card class="box-card" shadow="hover">
    <template #header>
      <div class="clearfix">
        <span>其他配置</span>
      </div>
    </template>

    <template v-if="localForm.fieldType == 'select' || localForm.fieldType == 'radio' || localForm.fieldType == 'checkBox'">
      <el-row :gutter="20">
        <el-col :span="8">
          <el-form-item label="是否字典" prop="isDict" @change="isDictChange">
            <el-radio v-model="localForm.isDict" :value="true">是</el-radio>
            <el-radio v-model="localForm.isDict" :value="false">否</el-radio>
          </el-form-item>
        </el-col>
        <el-col :span="8" v-if="localForm.isDict">
          <el-form-item label="字典名称" prop="dict">
            <el-select v-model="localForm.dict" filterable clearable @change="dictChange">
              <el-option v-for="item in typeOptions" :key="item.dictId" :label="item.dictName + '(' + item.dictType + ')'" :value="item.dictType" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="16" v-else>
          <attr-tag v-model="localForm.options"></attr-tag>
        </el-col>
        <el-col :span="24" v-if="localForm.fieldType == 'select'">
          <el-form-item label="创建条目" prop="allowCreate">
            <el-radio v-model="localForm.allowCreate" :value="true">是</el-radio>
            <el-radio v-model="localForm.allowCreate" :value="false">否</el-radio>
          </el-form-item>
          <el-form-item label="是否可筛选" prop="filterable">
            <el-radio v-model="localForm.filterable" :value="true">是</el-radio>
            <el-radio v-model="localForm.filterable" :value="false">否</el-radio>
          </el-form-item>
          <el-form-item label="是否多选" prop="multiple">
            <el-radio v-model="localForm.multiple" :value="true">是</el-radio>
            <el-radio v-model="localForm.multiple" :value="false">否</el-radio>
          </el-form-item>
          <el-form-item label="最多勾选几项" prop="checkoutMax" v-show="localForm.multiple == true">
            <el-input-number v-model="localForm.multipleLimit" />
          </el-form-item>
          <el-form-item label="是否将选中值按文字的形式展示" prop="collapseTags" v-show="localForm.multiple == true" label-width="240">
            <el-radio v-model="localForm.collapseTags" :value="true">是</el-radio>
            <el-radio v-model="localForm.collapseTags" :value="false">否</el-radio>
          </el-form-item>
          <el-form-item
            label="当鼠标悬停于折叠标签的文本时,是否显示所有选中的标签"
            prop="collapseTagsTooltip"
            v-show="localForm.multiple == true && localForm.collapseTags == true"
            label-width="240"
          >
            <el-radio v-model="localForm.collapseTagsTooltip" :value="true">是</el-radio>
            <el-radio v-model="localForm.collapseTagsTooltip" :value="false">否</el-radio>
          </el-form-item>
        </el-col>
        <!-- radio -->
        <el-col :span="24" v-if="localForm.fieldType == 'radio'">
          <el-form-item label="激活时的文本颜色" prop="textColor" label-width="200">
            <el-input v-model="localForm.textColor" clearable placeholder="激活时的文本颜色" />
          </el-form-item>
          <el-form-item label="激活时的填充色和边框色" prop="fill" label-width="200">
            <el-input v-model="localForm.fill" clearable placeholder="激活时的填充色和边框色" />
          </el-form-item>
          <el-form-item label="是否以按钮形式展示" prop="isRadioButton" >
            <el-radio v-model="localForm.isRadioButton" :value="true">是</el-radio>
            <el-radio v-model="localForm.isRadioButton" :value="false">否</el-radio>
          </el-form-item>
        </el-col>
        <!-- 多选框 -->
        <el-col :span="24" v-if="localForm.fieldType == 'checkBox'">
          <el-form-item label="最少勾选几项" prop="checkoutMin">
            <el-input-number v-model="localForm.min" />
          </el-form-item>
          <el-form-item label="最多勾选几项" prop="checkoutMax">
            <el-input-number v-model="localForm.max" />
          </el-form-item>
          <el-form-item label="是否带有边框" prop="checkoutBorder">
            <el-radio v-model="localForm.isBorder" :value="true">是</el-radio>
            <el-radio v-model="localForm.isBorder" :value="false">否</el-radio>
          </el-form-item>
          <el-form-item label="是否以按钮形式展示" prop="isRadioButton" >
            <el-radio v-model="localForm.isRadioButton" :value="true">是</el-radio>
            <el-radio v-model="localForm.isRadioButton" :value="false">否</el-radio>
          </el-form-item>
        </el-col>
      </el-row>
    </template>
    <template v-if="localForm.fieldType == 'input'">
      <el-row :gutter="20">
        <el-col :span="24">
          <el-form-item label="输入框的类型type" prop="type">
            <el-input
              v-model="localForm.type"
              clearable
              placeholder="请输入类型 可选值: 'text' | 'textarea' | 'number' | 'password' | 'email' | 'search' | 'tel' | 'url'"
            />
          </el-form-item>
        </el-col>
        <el-col :span="24" >
          <el-form-item label="最大输入长度" prop="maxlength">
            <el-input
              v-model="localForm.maxlength"
              clearable
              type="number"
              placeholder="最大输入长度"
            />
          </el-form-item>
        </el-col>
        <el-col :span="24">
          <el-form-item label="是否显示统计字数" prop="showWordLimit">
            <el-radio v-model="localForm.showWordLimit" :value="true">是</el-radio>
            <el-radio v-model="localForm.showWordLimit" :value="false">否</el-radio>
          </el-form-item>
        </el-col>
        <el-col :span="24" v-show="localForm.type == 'textarea'">
          <el-form-item label="是否自动调整高度" prop="isAutosize">
            <el-radio v-model="localForm.isAutosize" :value="true">是</el-radio>
            <el-radio v-model="localForm.isAutosize" :value="false">否</el-radio>
          </el-form-item>
        </el-col>
        <template v-if="localForm.type == 'textarea'&&localForm.isAutosize">
          <el-form-item label="最小行数" prop="autosizeMin">
            <el-input-number v-model="localForm.autosizeMin" :min="1" />
          </el-form-item>
          <el-form-item label="最大行数" prop="checkoutMax">
            <el-input-number v-model="localForm.autosizeMax" :min="localForm.autosizeMin||1" />
          </el-form-item>
        </template>
        <template v-if="localForm.type == 'number'">
          <el-form-item label="最小值" prop="min">
            <el-input-number v-model="localForm.min" />
          </el-form-item>
          <el-form-item label="最大值" prop="max">
            <el-input-number v-model="localForm.max" />
          </el-form-item>
          <el-form-item label="计数器步长" prop="step">
            <el-input-number v-model="localForm.step" />
          </el-form-item>
          <el-form-item label="数值精度" prop="precision">
            <el-input-number v-model="localForm.precision" />
          </el-form-item>
          <el-form-item label="是显示控制按钮" prop="controls">
            <el-radio v-model="localForm.isMultiple" :value="true">是</el-radio>
            <el-radio v-model="localForm.isMultiple" :value="false">否</el-radio>
          </el-form-item>
        </template>
        <el-col :span="24" v-show="localForm.type == 'textarea'&&!localForm.isAutosize">
          <el-form-item label="固定行数" prop="rows">
            <el-input
              v-model.number="localForm.rows"
              clearable
              type="number"
              placeholder="输入框固定行数，仅 type 为 'textarea' 时有效"
            />
          </el-form-item>
        </el-col>
      </el-row>
    </template>
    <!-- 数据选择 -->
    <template v-if="localForm.fieldType == 'dataChoose'">
      <el-row :gutter="20">
        <el-col :span="8">
          <el-form-item label="是否多选" prop="isMultiple">
            <el-radio v-model="localForm.isMultiple" :value="true">是</el-radio>
            <el-radio v-model="localForm.isMultiple" :value="false">否</el-radio>
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="数据来源" prop="selectType">
            <el-select v-model="localForm.selectType" filterable placeholder="请选择数据来源">
              <el-option v-for="dict in system_data_source" :key="dict.value" :label="dict.label+'('+dict.value+')'" :value="dict.value"></el-option>
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="选择图标" prop="suffixIcon">
            <icon-choose v-model="localForm.suffixIcon"></icon-choose>
              <!-- <el-icon><AlarmClock /></el-icon> -->
            <!-- <el-input v-model="localForm.suffixIcon" :suffix-icon="form.suffixIcon" placeholder="请输入图标" /> -->
          </el-form-item>
        </el-col>
      </el-row>
    </template>
    <!-- 时间选择器 -->
    <template v-else-if="localForm.fieldType == 'timePicker'">
      <el-row :gutter="20">
        <el-col :span="24">
          <el-form-item label="是否为时间范围选择" prop="isRange" label-width="200">
            <el-radio v-model="localForm.isRange" :value="true">是</el-radio>
            <el-radio v-model="localForm.isRange" :value="false">否</el-radio>
          </el-form-item>
        </el-col>
        <el-col :span="24">
          <el-form-item label="是否使用箭头进行时间选择" prop="arrowControl" label-width="200">
            <el-radio v-model="localForm.arrowControl" :value="true">是</el-radio>
            <el-radio v-model="localForm.arrowControl" :value="false">否</el-radio>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="显示在输入框中的格式" prop="format">
            <el-input v-model="localForm.format" clearable placeholder="时间格式 推荐：hh:mm:ss" />
          </el-form-item>
        </el-col>
      </el-row>
    </template>
    <!-- 时间选择 -->
    <template v-else-if="localForm.fieldType == 'timeSelect'">
      <el-row :gutter="20">
        <el-col :span="8">
          <el-form-item label="开始时间" prop="start">
            <el-input v-model="localForm.start" clearable placeholder="请输入开始时间 格式: 09:00" />
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="结束时间" prop="end">
            <el-input v-model="localForm.end" clearable placeholder="请输入结束时间 格式: 09:00" />
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="间隔时间" prop="step">
            <el-input v-model="localForm.step" clearable placeholder="请输入间隔时间 格式: 00:30" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="时间格式" prop="format">
            <el-input v-model="localForm.format" clearable placeholder="时间格式 推荐：hh:mm:ss" />
          </el-form-item>
        </el-col>
      </el-row>
    </template>
    <!-- 日期选择器 -->
 
    <template v-else-if="localForm.fieldType == 'datePicker'">
      <el-row :gutter="20">
        <el-col :span="24">
          <el-form-item label="日期选择器的类型" prop="datePickerAttrType" label-width="200">
            <el-input
              v-model="localForm.type"
              clearable
              placeholder="请输入类型 可选值: 'year' | 'years' |'month' | 'months' | 'date' | 'dates' | 'datetime' | 'week' | 'datetimerange' | 'daterange' | 'monthrange' | 'yearrange'"
            />
          </el-form-item>

          <el-col :span="12">
            <el-form-item label="下拉框出现的位置" prop="placement">
              <el-input v-model="localForm.placement" clearable placeholder="下拉框出现的位置" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="显示格式" prop="format">
              <el-autocomplete
                class="inline-input"
                v-model="localForm.format"
                :fetch-suggestions="querySearch"
                placeholder="显示格式，默认:YYYY-MM-DD"
              ></el-autocomplete>
              <!-- <el-input v-model="localForm.format" clearable placeholder="默认:YYYY-MM-DD" /> -->
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="格式化值" prop="valueFormat">
              <el-autocomplete
                class="inline-input"
                v-model="localForm.valueFormat"
                :fetch-suggestions="valueQuerySearch"
                placeholder="格式化值"
              ></el-autocomplete>
              <!-- <el-input v-model="localForm.format" clearable placeholder="格式化值" /> -->
            </el-form-item>
          </el-col>
          <!-- 需要element-plus 2.11.0 才支持 -->
          <!-- <el-form-item label="是否显示 footer" prop="showFooter">
            <el-radio v-model="localForm.showFooter" :value="true">是</el-radio>
            <el-radio v-model="localForm.showFooter" :value="false">否</el-radio>
          </el-form-item>
          <el-form-item label="是否显示确定按钮" prop="showConfirm">
            <el-radio v-model="localForm.showConfirm" :value="true">是</el-radio>
            <el-radio v-model="localForm.showConfirm" :value="false">否</el-radio>
          </el-form-item>
          <el-form-item label="是否显示周数(除周外)" prop="showWeekNumber">
            <el-radio v-model="localForm.showWeekNumber" :value="true">是</el-radio>
            <el-radio v-model="localForm.showWeekNumber" :value="false">否</el-radio>
          </el-form-item> -->
          <el-form-item label="文本框可输入" prop="editable">
            <el-radio v-model="localForm.editable" :value="true">是</el-radio>
            <el-radio v-model="localForm.editable" :value="false">否</el-radio>
          </el-form-item>
        </el-col>
      </el-row>
    </template>
    <!-- 部门选择 -->
    <template v-else-if="localForm.fieldType == 'departmentSelect'|| localForm.fieldType == 'fileTypeSelect'">
      <el-row :gutter="20">
        <el-col :span="24">
          <el-form-item label="是否可筛选" prop="filterable">
            <el-radio v-model="localForm.filterable" :value="true">是</el-radio>
            <el-radio v-model="localForm.filterable" :value="false">否</el-radio>
          </el-form-item>
          <el-form-item label="是否多选" prop="multiple">
            <el-radio v-model="localForm.multiple" :value="true">是</el-radio>
            <el-radio v-model="localForm.multiple" :value="false">否</el-radio>
          </el-form-item>
          <el-form-item label="默认展开" prop="defaultExpandAll">
            <el-radio v-model="localForm.defaultExpandAll" :value="true">是</el-radio>
            <el-radio v-model="localForm.defaultExpandAll" :value="false">否</el-radio>
          </el-form-item>
        </el-col>
      </el-row>
    </template>
    <template v-else-if="localForm.fieldType == 'province'">
      <el-row :gutter="20">
        <el-col :span="24">
          <el-form-item label="是否多选" prop="multiple">
            <el-radio v-model="localForm.multiple" :value="true">是</el-radio>
            <el-radio v-model="localForm.multiple" :value="false">否</el-radio>
          </el-form-item>
          <el-form-item label="是否可筛选" prop="filterable">
            <el-radio v-model="localForm.filterable" :value="true">是</el-radio>
            <el-radio v-model="localForm.filterable" :value="false">否</el-radio>
          </el-form-item>

        </el-col>
      </el-row>
    </template>
  </el-card>
</template>

<script lang="ts" setup>
import { computed } from 'vue';
import type { PropType } from 'vue';
import AttrTag from '@/views/system/pageConfig/component/attrTag.vue';
import IconChoose from '@/views/system/pageConfig/component/iconChoose.vue';
const { proxy } = getCurrentInstance() as ComponentInternalInstance;
const { system_data_source } = toRefs<any>(proxy?.useDict('system_data_source'));

interface DictOption {
  dictId: string;
  dictName: string;
  dictType: string;
  [key: string]: any;
}

interface FormType {
  fieldType: string;
  isDict: boolean;
  dict: string | undefined;
  options: any[];
  [key: string]: any;
}

const props = defineProps({
  modelValue: {
    type: Object as PropType<FormType>,
    required: true
  },
  typeOptions: {
    type: Array as PropType<DictOption[]>,
    default: () => []
  }
});

const emit = defineEmits<{
  (e: 'update:modelValue', value: FormType): void;
  (e: 'dictChange'): void;
}>();

// 格式化日期提示
const querySearch = (q, cb) => {
  cb([{ value: 'YYYY-MM-DD' }])
}
// 格式化日期提示
const valueQuerySearch = (q, cb) => {
  cb([{ value: 'YYYY-MM-DD HH:mm:ss' }])
}

// 创建本地响应式数据，用于双向绑定
const localForm = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
});

// 字典改变事件
const dictChange = () => {
  emit('dictChange');
};

const isDictChange = () => {
  if(localForm.value.fieldType == 'select' || localForm.value.fieldType == 'checkBox' || localForm.value.fieldType == 'radio') {
    localForm.value.defaultValue = ""
  }
};
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
