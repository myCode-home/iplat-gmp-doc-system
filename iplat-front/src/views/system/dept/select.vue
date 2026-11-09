<template>
  <div class="p-2">
    <el-card shadow="hover">
      <el-input v-model="deptName" placeholder="请输入部门名称" prefix-icon="Search" clearable />
      <el-tree
        ref="deptTreeRef"
        class="mt-2"
        node-key="id"
        :show-checkbox="multipleChoice"
        :data="deptOptions"
        :props="{ label: 'label', children: 'children' }"
        :current-node-key="currentNodeKey"
        :expand-on-click-node="false"
        check-strictly
        :filter-node-method="filterNode"
        highlight-current
        default-expand-all
        @current-change="handleCurrentChange"
      />
    </el-card>
  </div>
</template>

<script setup name="DeptSelect" lang="ts">
import api from '@/api/system/user';
import { DeptTreeVO, DeptVO } from '@/api/system/dept/types';
const props = defineProps({
  multipleChoice: {
    type: Boolean,
    default: false
  },
  // 其他条件 传入 {id: id,label: label} 可实现默认选中
  otherCondition: {
    type: Object,
    default: () => {}
  },
  multipleChoiceData: {
    type: Function,
    default: () => ()=>{}
  },

})

const deptTreeRef = ref(null);
const deptName = ref('');
const deptOptions = ref<DeptTreeVO[]>([]);
const currentSelect = ref();
const currentNodeKey = ref('');

const { proxy } = getCurrentInstance() as ComponentInternalInstance;

/** 根据名称筛选部门树 */
watchEffect(
  () => {
    deptTreeRef.value?.filter(deptName.value);
  },
  {
    flush: 'post' // watchEffect会在DOM挂载或者更新之前就会触发，此属性控制在DOM元素更新后运行
  }
);

/** 通过条件过滤节点  */
const filterNode = (value: string, data: any) => {
  if (!value) return true;
  return data.label.indexOf(value) !== -1;
};

/** 查询部门下拉树结构 */
const getTreeSelect = async () => {
  const res = await api.deptTreeSelect();
  deptOptions.value = res.data[0].children;
  if(props.otherCondition&&props.otherCondition.id){
    // 多选默认选中
    if(props.multipleChoice&&props.otherCondition&&props.otherCondition.id){
      const ids = props.otherCondition.id.split(',');
      deptTreeRef.value.setCheckedKeys(ids);
    }else{
      // 多选默认选中
      currentNodeKey.value = props.otherCondition.id;
      currentSelect.value = props.otherCondition;
    }
  }
};


/** 单选选中节点变化事件触发 */
const handleCurrentChange = (data: DeptVO) => {
  currentSelect.value = data;
};

/** 提交按钮操作 */
const confirmChoiceData = async () => {
  if(props.multipleChoice){
    const nodes = deptTreeRef.value.getCheckedNodes();
    if(!nodes.length){
      return proxy?.$modal.msgError('请选择部门');
    }
    const id = nodes.map((item: any) => item.id).join(',');
    const name = nodes.map((item: any) => item.label).join(',');
    props.multipleChoiceData({
      id,
      name,
      data: nodes
    })
  }else{
    if(!currentSelect.value?.id){
      return proxy?.$modal.msgError('请选择部门');
    }
    props.multipleChoiceData({
      name: currentSelect.value?.label,
      ...currentSelect.value
    });
  }
  // if(!dataSelectRef.value||!dataSelectRef.value.getRowData().dictCode){
  //   return proxy?.$modal.msgError('请选择字典键值');
  // }
  // props.multipleChoiceData(dataSelectRef.value.getRowData());
};

onMounted(() => {
  getTreeSelect();
});

defineExpose({
  confirmChoiceData
})
</script>
