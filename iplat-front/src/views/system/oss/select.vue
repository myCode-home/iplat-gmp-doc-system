<template>
  <div class="p-2">
    <transition :enter-active-class="proxy?.animate.searchAnimate.enter"
      :leave-active-class="proxy?.animate.searchAnimate.leave">
      <div v-show="showSearch" class="mb-[10px]">
        <el-card shadow="hover">
          <el-form ref="queryFormRef" :model="queryParams" :inline="true">
            <el-form-item label="文件编号" prop="dlCode">
              <el-input v-model="queryParams.dlCode" placeholder="请输入文件编号" clearable @change="handleQuery"
                @keyup.enter="handleQuery" />
            </el-form-item>
            <el-form-item label="文件名称" prop="dlName">
              <el-input v-model="queryParams.dlName" placeholder="请输入文件名称" clearable @change="handleQuery"
                @keyup.enter="handleQuery" />
            </el-form-item>
            <el-form-item label="归属部门" prop="dlDeptId">
              <el-tree-select v-model="queryParams.dlDeptId" :data="deptOptions"
                :props="{ value: 'id', label: 'label', children: 'children' }" :default-expanded-keys="expandedKeys"
                value-key="id" placeholder="请选择归属部门" check-strictly clearable @change="handleQuery" />
            </el-form-item>

            <el-form-item>
              <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
              <el-button icon="Refresh" @click="resetQuery">重置</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </div>
    </transition>

    <el-card shadow="hover">
      <template #header>
        <el-row :gutter="10">
          <right-toolbar v-model:show-search="showSearch" @query-table="getList"></right-toolbar>
        </el-row>
      </template>

      <el-table ref="fileTableRef" border :class="{ nomultiple: !multipleChoice }" row-key="id" v-loading="loading"
        :data="fileList" @select="selectChange" @selection-change="handleSelectionChange">
        <el-table-column type="selection" reserve-selection width="55" align="center" />
        <el-table-column label="文件编号" prop="dlCode" align="center" :show-overflow-tooltip="true" min-width="120" />
        <el-table-column label="文件名称" prop="dlName" align="center" :show-overflow-tooltip="true" min-width="120" />
        <el-table-column label="所属部门" prop="dlDeptName" align="center" width="120" />
        <el-table-column label="文件级别" prop="dlLevel" align="center" width="100" >
          <template #default="scope">
            <dict-tag :options="dms_file_level" :value="scope.row.dlLevel" />
          </template>
        </el-table-column>
        <el-table-column label="状态" align="center" width="100">
          <template #default="scope">
            <dict-tag :options="dms_user_state" :value="scope.row.status" />
          </template>
        </el-table-column>
      </el-table>

      <pagination v-if="total > 0" v-model:total="total" v-model:page="queryParams.pageNum"
        v-model:limit="queryParams.pageSize" @pagination="getList" />
    </el-card>
  </div>
</template>

<script setup name="FileSelect" lang="ts">
import { getCurrentInstance, reactive, ref, onMounted, toRefs } from 'vue';
import { ComponentInternalInstance } from '@vue/runtime-core';
import { DeptTreeVO } from '@/api/system/dept/types';
import { getFileList } from '@/api/dms/fileLib/index';

import api from '@/api/system/user';

const props = defineProps({
  multipleChoice: {
    type: Boolean,
    default: false
  },
  otherCondition: {
    type: Object,
    default: () => { }
  },
  multipleChoiceData: {
    type: Function,
    default: () => () => { }
  }
});

const { proxy } = getCurrentInstance() as ComponentInternalInstance;
const { dms_user_state } = toRefs<any>(proxy?.useDict('dms_user_state'));
const { dms_file_level } = toRefs<any>(proxy?.useDict('dms_file_level'));


const fileList = ref<any[]>([]);
const loading = ref(true);
const showSearch = ref(true);
const ids = ref<Array<string | number>>([]);
const total = ref(0);

const queryFormRef = ref<any>();
const fileTableRef = ref<any>();

const selectData = ref<any[]>([]);

// 部门选择相关
const deptOptions = ref<DeptTreeVO[]>([]);

const data = reactive({
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    dlCode: '',
    dlName: '',
    dlDeptId: undefined, // 新增部门ID查询参数
  }
});

const { queryParams } = toRefs(data);

watch(() => props.otherCondition, () => {
  Object.keys(props.otherCondition).forEach(key => {
    queryParams.value[key] = props.otherCondition[key];
  });
}, { immediate: true, deep: true });

/** 查询部门下拉树结构 */
const getDeptTree = async () => {
  const res = await api.deptTreeSelect();
  deptOptions.value = res.data[0].children;
};

// 计算需要展开的节点
const expandedKeys = computed(() => {
  if (!queryParams.value.dlDeptId) return [];
  // 如果是数组，返回所有选中的值
  if (Array.isArray(queryParams.value.dlDeptId)) {
    return queryParams.value.dlDeptId;
  }
  // 如果是单选，返回当前选中的值
  return [queryParams.value.dlDeptId];
});

/**
 * 查询文件列表
 */
const getList = () => {
  loading.value = true;
  // 模拟数据，实际应调用API接口
  getFileList(queryParams.value).then((res) => {
    fileList.value = res.rows;
    total.value = res.total;
  }).finally(()=>{
    loading.value = false;
  })

};

/**
 * 搜索按钮操作
 */
const handleQuery = () => {
  queryParams.value.pageNum = 1;
  getList();
};

/** 重置 */
const resetQuery = () => {
  queryParams.value.dlDeptId = undefined; // 重置部门选择
  queryFormRef.value?.resetFields();
  handleQuery();
};

/** 多选框选中数据 */
const handleSelectionChange = (selection: any[]) => {
  ids.value = selection.map(item => item.fileId);
  selectData.value = selection;
};

const selectChange = (selection: any[]) => {
  if (!props.multipleChoice) {
    if (selection.length > 1) {
      const del_row = selection.shift();
      fileTableRef.value.toggleRowSelection(del_row, false);
    }
  }
};

/** 提交按钮操作 */
const confirmChoiceData = async () => {
  if (selectData.value.length === 0) {
    return proxy?.$modal.msgError('请选择');
  }
  if (props.multipleChoice) {
    props.multipleChoiceData(selectData.value);
  } else {
    props.multipleChoiceData(selectData.value[0]);
  }
};

onMounted(() => {
  getDeptTree(); // 初始化部门数据
  getList();
});

defineExpose({
  confirmChoiceData
});
</script>

<style lang="scss" scoped>
//找到表头，然后隐藏掉复选框
:deep(.nomultiple) {
  .el-table__header-wrapper {
    .el-checkbox {
      display: none;
    }
  }
}
</style>