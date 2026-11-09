<template>
  <el-form ref="ruleFormRef" :model="final" :inline="true" :rules="rules" style="margin-bottom: 7px;">
    <el-form-item prop="dpsrJoint" :style="{ 'visibility': jointVisible ? 'visible' : 'hidden'}">
      <el-select v-model="final.dpsrJoint" :size="size" placeholder="请选择连接符" style="width: 80px;">
        <el-option v-for="dict in joints" :key="dict.value" :label="dict.label" :value="dict.value"></el-option>
      </el-select>
    </el-form-item>
    <!-- 属性前缀 -->
    <el-form-item prop="dpsrAttrPrefix" v-if="showDpsrAttrPrefix">
      <el-input v-model="final.dpsrAttrPrefix" placeholder="属性前缀" class="labelInput"/>
    </el-form-item>
    <!-- 属性 -->
    <el-form-item prop="businessAttrId">
      <el-select v-model="final.businessAttrId" :size="size" filterable placeholder="请选择属性" class="valueSelect">
        <el-option v-for="dict in attrList" :key="dict.businessAttrId" :label="dict.businessAttrName" :value="dict.businessAttrId"></el-option>
      </el-select>
    </el-form-item>
    <!-- 属性后缀 -->
    <el-form-item prop="dpsrAttrSuffix" v-if="showDpsrAttrSuffix">
      <el-input v-model="final.dpsrAttrSuffix" placeholder="属性后缀" class="labelInput"/>
    </el-form-item>
    <el-form-item prop="dpsrConnector">
      <el-select v-model="final.dpsrConnector" :size="size" filterable placeholder="请选择关系符" class="valueSelect">
        <el-option v-for="dict in connectors" :key="dict.value" :label="dict.label" :value="dict.value"></el-option>
      </el-select>
    </el-form-item>
    <!-- 值前缀 -->
    <el-form-item prop="dpsrPrefix" v-if="showDpsrPrefix">
      <el-input v-model="final.dpsrPrefix" placeholder="值前缀" class="labelInput"/>
    </el-form-item>
    <el-form-item prop="dpsrValue">
      <el-tooltip
        class="item"
        effect="dark"
        :enterable="false"
        :content="final.dpsrDescribe?final.dpsrDescribe:final.dpsrValue"
        placement="top"
      >
        <el-input v-if="final.dpsrDescribe" disabled v-model="final.dpsrDescribe" :title="final.dpsrDescribe" class="valueSelect"/>
        <el-input v-else v-model="final.dpsrValue" placeholder="请输入值" :title="final.dpsrDescribe" class="valueSelect"/>
      </el-tooltip>
    </el-form-item>
    <!-- 值后缀 -->
    <el-form-item prop="dpsrSuffix" v-if="showDpsrSuffix">
      <el-input v-model="final.dpsrSuffix" placeholder="值后缀" class="labelInput"/>
    </el-form-item>
    <el-form-item style="margin-left: 20px">
      <el-dropdown trigger="click" @command="handleCommand">
          <el-button :size="size">
            更多<el-icon><ArrowDown /></el-icon>
          </el-button>
          <template #dropdown>
            <el-dropdown-menu>
                <el-dropdown-item v-for="(value, key) in moreOperateObject" :key="key" :command="key">{{value}}</el-dropdown-item>
            </el-dropdown-menu>
          </template>
      </el-dropdown>
    </el-form-item>
    <el-form-item>
      <el-button icon="Plus" @click="emit('insert')"></el-button>
      <el-button v-if="!final.dsPermitSubjectRuleId" icon="Close" @click="$emit('discard')" :disabled="!jointVisible" style="margin-left: 0px;"></el-button>
      <el-popconfirm
        v-else
        title="是否确认删除？"
        @confirm="handleDelete"
      >
        <template #reference>
          <el-button icon="Close" type="danger" style="margin-left: 0px;"></el-button>
        </template>
      </el-popconfirm>
    </el-form-item>
  </el-form>
  <to-select
    v-if="selectData.open"
    :title="selectData.title"
    :selectType="selectData.type"
    :multiple="selectData.multiple"
    :width="selectData.width"
    :otherCondition="selectData.otherCondition"
    :achieveData="selectDataAchieve"
    @dialogClose="selectDataCancel"
  ></to-select>
</template>

<script lang="ts" name="RuleItem" setup>
import {
  addSubject, 
  updateSubject
} from '@/api/resourcepermission/dsPermit/settings';
import { propTypes } from '@/utils/propTypes';
import type { ComponentSize } from 'element-plus'
const props = defineProps({
  size: propTypes.oneOf<ComponentSize>(['', 'small', 'default', 'large']).def('default'),
  info: {
    type: Object,
    default: ()=> {}
  },
  default: {
    type: Object,
    default: ()=> {}
  },
  attrList: {
    type: Object,
    default: ()=> []
  },
  connectors: {
    type: Object,
    default: ()=> []
  },
  jointVisible: {
    type: Boolean,
    default: false
  },
})
const { proxy } = getCurrentInstance() as ComponentInternalInstance;
const joints = [{
    value: 'and',
    label: '并且'
}, {
    value: 'or',
    label: '或者'
}];
const ruleFormRef = ref(null);
const loading = ref(false);
const showDpsrAttrPrefix = ref(false);
const showDpsrAttrSuffix = ref(false);
const showDpsrPrefix = ref(false);
const showDpsrSuffix = ref(false);
const emit = defineEmits(['updated','insert','discard','remove']);
const final = ref({
  dsPermitSubjectRuleId: '',
  dsPermitSubjectId: props.info.dsPermitSubjectId,
  dsPermitId: props.info.dsPermitId,
  businessId: props.info.businessId,
  businessAttrId: '',
  dpsrAttrPrefix: '',
  dpsrAttrSuffix: '',
  dpsrConnector: 'like',
  dpsrValue: '',
  dpsrDescribe: '',
  dpsrPrefix: '',
  dpsrSuffix: '',
  dpsrJoint: 'and',
  dpsrSort: '',
  remark: ''
});
const rules = ref({
    businessAttrId: [
      { required: true, message: "请选择属性", trigger: "blur" }
    ],
    dpsrValue: [
      { required: true, message: "值不能为空", trigger: "change" }
    ],
    dpsrJoint: [
      { required: true, message: "连接符不能为空", trigger: "change" }
    ]
});

const selectData = ref({
  open: false,
  type: '',
  width: '',
  title: '',
  menuType: '',
  multiple: false,
  otherCondition: {},
})

// 数据初始化
const initData = () => {
  Object.assign(final.value, props.default);
  if (final.value.dpsrPrefix) {
    showDpsrPrefix.value = true;
  }
  if (final.value.dpsrSuffix) {
    showDpsrSuffix.value = true;
  }
  if (final.value.dpsrAttrPrefix) {
    showDpsrAttrPrefix.value = true;
  }
  if (final.value.dpsrAttrSuffix) {
    showDpsrAttrSuffix.value = true;
  }
  // if (!val.describe) {
  //   currentType = 'text';
  //   final.value.dpsrDescribe = final.value.dpsrValue;
  //   inputDisabled = false;
  // } else {
  //   currentType = '';
  //   inputDisabled = true;
  // }
};
watch(() => props.default, (val) => {
  initData()
})
const moreOperateObj = {
  text: '文本',
  keyValue: '键值对',
  loginUser: '登陆人',
  organization: '组织机构',
  organizations: '组织机构及其子部门',
  addDpsrAttrPrefix: '+ 属性前缀',
  removeDpsrAttrPrefix: '- 属性前缀',
  addDpsrAttrSuffix: '+ 属性后缀',
  removeDpsrAttrSuffix: '- 属性后缀',
  addDpsrPrefix: '+ 值前缀',
  removeDpsrPrefix: '- 值前缀',
  addDpsrSuffix: '+ 值后缀',
  removeDpsrSuffix: '- 值后缀'
}
// 更多 下拉框 事件
const handleCommand = (val) => {
  if (val === 'addDpsrPrefix') {
    showDpsrPrefix.value = true;
  } else if (val === 'removeDpsrPrefix') {
    final.value.dpsrPrefix = '';
    showDpsrPrefix.value = false;
  } else if (val === 'addDpsrSuffix') {
    showDpsrSuffix.value = true;
  } else if (val === 'removeDpsrSuffix') {
    final.value.dpsrSuffix = '';
    showDpsrSuffix.value = false;
  } else if (val === 'addDpsrAttrPrefix') {
    showDpsrAttrPrefix.value = true;
  } else if (val === 'removeDpsrAttrPrefix') {
    final.value.dpsrAttrPrefix = '';
    showDpsrAttrPrefix .value= false;
  } else if (val === 'addDpsrAttrSuffix') {
    showDpsrAttrSuffix.value = true;
  } else if (val === 'removeDpsrAttrSuffix') {
    final.value.dpsrAttrSuffix = '';
    showDpsrAttrSuffix.value = false;
  } else {
    if (val === 'text') {
      final.value.dpsrDescribe = '';
      final.value.dpsrValue = '';
    } else if (val === 'keyValue') {
      selectData.value.open = true;
      selectData.value.title = '选择键值对';
      selectData.value.type = 'dictSelect';
      selectData.value.menuType = val;
    } else if (val === 'loginUser') {
      selectData.value.open = true;
      selectData.value.title = '选择登陆人属性';
      selectData.value.type = 'attributeSelect';
      selectData.value.menuType = val;
    } else if (val === 'organization'|| val === 'organizations') {
      selectData.value.open = true;
      selectData.value.title = '选择组织机构';
      selectData.value.width = '600px';
      const dpsrValue = final.value.dpsrValue;
      selectData.value.otherCondition = {
        id: dpsrValue.replace('orgChild$$',''),
        label: final.value.dpsrDescribe,
      };
      selectData.value.multiple = true;
      selectData.value.type = 'deptSelect';
      selectData.value.menuType = val;
    }
  }
};
// 选中数据
const selectDataAchieve = (data) => {
  final.value.dpsrDescribe = data.name;
  final.value.dpsrValue = data.id;
  if(selectData.value.menuType == 'organizations'){
    final.value.dpsrValue = 'orgChild$$'+data.id
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
    menuType: '',
    otherCondition: {},
  };
};
const moreOperateObject = computed(() => {
  let obj = {};
  Object.keys(moreOperateObj).forEach(key => {
    if (key === 'addDpsrPrefix' && showDpsrPrefix.value) {
        return;
    }
    if (key === 'removeDpsrPrefix' && !showDpsrPrefix.value) {
        return;
    }
    if (key === 'addDpsrSuffix' && showDpsrSuffix.value) {
        return;
    }
    if (key === 'removeDpsrSuffix' && !showDpsrSuffix.value) {
        return;
    }
    if (key === 'addDpsrAttrPrefix' && showDpsrAttrPrefix.value) {
        return;
    }
    if (key === 'removeDpsrAttrPrefix' && !showDpsrAttrPrefix.value) {
        return;
    }
    if (key === 'addDpsrAttrSuffix' && showDpsrAttrSuffix.value) {
        return;
    }
    if (key === 'removeDpsrAttrSuffix' && !showDpsrAttrSuffix.value) {
        return;
    }
    obj[key] = moreOperateObj[key];
  });
  return obj;
})

// 调接口删除当前行
const handleDelete = () => {
  emit('remove', final.value.dsPermitSubjectRuleId);
};

// 获取当前数据
const getRowData = () => {
  return new Promise(resolve => {
    ruleFormRef.value.validate(v => {
      let res = {};
      if (v) {
        res = Object.assign({}, final.value);
        // if (this.currentType === 'text') {
        //   res.value = res.describe;
        //   res.describe = '';
        // }
        resolve(res);
      }else{
        resolve(false);
      }
    });
  });
};


onMounted(() => {
  initData();
})
defineExpose({
  getRowData
})
</script>

<style lang="scss" scoped>
.el-form-item{
  margin-bottom: 8px;
  margin-right: 0;
}
.labelInput{
  width: 90px;
}
.valueSelect{
  width: 130px;
}
</style>