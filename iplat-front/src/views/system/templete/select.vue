<template>
  <div class="p-2">
    <transition :enter-active-class="proxy?.animate.searchAnimate.enter"
      :leave-active-class="proxy?.animate.searchAnimate.leave">
      <div v-show="showSearch" class="mb-[10px]">
        <el-card shadow="hover">
          <el-form ref="queryFormRef" :model="queryParams" :inline="true">
            <el-form-item label="模版编号" prop="tmplCode">
              <el-input v-model="queryParams.tmplCode" placeholder="请输入模版编号" clearable @change="handleQuery"
                @keyup.enter="handleQuery" />
            </el-form-item>
            <el-form-item label="模版名称" prop="tmplName">
              <el-input v-model="queryParams.tmplName" placeholder="请输入模版名称" clearable @change="handleQuery"
                @keyup.enter="handleQuery" />
            </el-form-item>
            <el-form-item label="模版类型" prop="tmplCategory">
              <el-select v-model="queryParams.tmplCategory" filterable placeholder="请选择模版类型" clearable>
                <el-option v-for="dict in dms_template_type" :key="dict.value" :label="dict.label"
                  :value="dict.value"></el-option>
              </el-select>
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
        :data="fileList" height="400px" @select="selectChange" @selection-change="handleSelectionChange">
        <el-table-column type="selection" reserve-selection width="55" align="center" />
        <el-table-column label="模版编号" prop="tmplCode" align="center" :show-overflow-tooltip="true" min-width="100" />
        <el-table-column label="模版名称" prop="tmplName" align="center" :show-overflow-tooltip="true" min-width="100" />
        <el-table-column label="状态" prop="status" align="center">
          <template #default="{ row }">
            <dict-tag :options="dms_user_state" :value="row.status" />
          </template>
        </el-table-column>
        <el-table-column label="模版类型" prop="tmplCategory" align="center">
          <template #default="{ row }">
            <dict-tag :options="dms_template_type" :value="row.tmplCategory" />
          </template>
        </el-table-column>
        <el-table-column label="版本号" prop="snapshotVersion" align="center" />
        <el-table-column label="生效时间" prop="tmplEffectiveTime" align="center" />
        <el-table-column label="模版用途" prop="tmplPurpose" align="center" >
          <template #default="{ row }">
            <dict-tag :options="dms_template_action" :value="row.tmplPurpose" />
          </template>
        </el-table-column>
        <el-table-column label="起草人" prop="drafterName" align="center" />
        <el-table-column label="模版描述" prop="tmplDesc" align="center" />
        <el-table-column label="模版原因" prop="tmplReason" align="center" />
      </el-table>

      <pagination v-if="total > 0" v-model:total="total" v-model:page="queryParams.pageNum"
        v-model:limit="queryParams.pageSize" @pagination="getList" />
    </el-card>
  </div>
</template>

<script setup name="FileSelect" lang="ts">
import { getCurrentInstance, reactive, ref, nextTick, onMounted, toRefs } from 'vue';
import { ComponentInternalInstance } from '@vue/runtime-core';
import { DeptTreeVO } from '@/api/system/dept/types';
import api from '@/api/system/user';
import { listTmplLibActive } from '@/api/dms/templateManagement/templateActiveLibrary/index';


const props = defineProps({
  multipleChoice: {
    type: Boolean,
    default: true
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
const { dms_template_type } = toRefs<any>(proxy?.useDict('dms_template_type'));
const { dms_template_action } = toRefs<any>(proxy?.useDict('dms_template_action'));
const { dms_user_state } = toRefs<any>(proxy?.useDict('dms_user_state'));


const fileList = ref<any[]>([]);
const loading = ref(true);
const showSearch = ref(true);
const ids = ref<Array<string | number>>([]);
const total = ref(0);
const reviewDateRange = ref<[string, string]>(['', '']);
const effectiveDateRange = ref<[string, string]>(['', '']);

const queryFormRef = ref<any>();
const fileTableRef = ref<any>();

const selectData = ref<any[]>([]);

// 部门选择相关
const deptOptions = ref<DeptTreeVO[]>([]);

const data = reactive({
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    tmplCode: '',
    tmplName: '',
    tmplCategory: '', // 新增部门ID查询参数

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

/**
 * 查询文件列表
 */
const getList = () => {
  loading.value = true;
  // 模拟数据，实际应调用API接口
  listTmplLibActive(queryParams.value).then((res) => {
    fileList.value = res.rows;
    total.value = res.total;
    loading.value = false;
  });
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
  reviewDateRange.value = ['', ''];
  effectiveDateRange.value = ['', ''];
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