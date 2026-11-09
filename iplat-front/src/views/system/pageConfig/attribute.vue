<template>
  <div class="">
    <el-card class="box-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <span>{{ pageConfig.pageConfigName?`【${pageConfig.pageConfigName}】`:'' }}属性列表</span>
          <el-button v-if="pageConfig.pageConfigId" v-hasPermi="['system:extended:add']" @click="handleAdd" class="button" type="primary" size="small" icon="Plus" style="float: right;">新增</el-button>
          <el-button v-if="pageConfig.pageConfigId" v-hasPermi="['system:extended:add']" @click="handleTableAdd" class="button" type="primary" size="small" icon="Plus"  style="float: right;margin-right: 10px;">批量新增</el-button>
          <el-button v-if="pageConfig.pageConfigId" v-hasPermi="['system:extended:remove']" type="danger"  size="small" icon="Delete" :disabled="multiple" @click="handleDelete"  style="float: right;">
            删除
          </el-button>
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
            <el-form-item label="名称" prop="pageConfigAttributeName">
              <el-input v-model="queryParams.pageConfigAttributeName" placeholder="请输入名称" clearable @change="handleQuery" @keyup.enter="handleQuery" style="width: 150px;" />
            </el-form-item>
            <el-form-item label="字段属性" prop="pageConfigAttributeDesc">
              <el-input v-model="queryParams.pageConfigAttributeDesc" placeholder="请输入字段属性" clearable @change="handleQuery" @keyup.enter="handleQuery" style="width: 150px;" />
            </el-form-item>
            <el-form-item :label="pageConfig.pageConfigDesc=='tableArea'?'展示类型':'类型'" prop="pageConfigAttributeType">
              <el-select v-model="queryParams.pageConfigAttributeType" clearable @change="handleQuery" placeholder="请选择" style="width: 150px;">
                <template v-if="pageConfig.pageConfigDesc=='tableArea'">
                  <el-option v-for="dict in system_display_type" :key="dict.value" :label="dict.label" :value="dict.value"></el-option>
                </template>
                <template v-else>
                  <el-option v-for="dict in system_field_type" :key="dict.value" :label="dict.label" :value="dict.value"></el-option>
                </template>
              </el-select>
            </el-form-item>
            <el-form-item class="noMarginRight">
              <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
              <el-button icon="Refresh" @click="resetQuery">重置</el-button>
            </el-form-item>
          </el-form>
        </div>
      </transition>
      <el-table v-loading="loading" border :data="extendedAttributeList" size="small" :default-sort="{prop: 'pageConfigAttributeSort', order: 'ascending'}" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="40" align="center" />
        <el-table-column type="expand">
          <template #default="props">
            <el-form label-position="left" inline size="small" label-width="auto" label-suffix="：" class="demo-table-expand p-2">
              <el-form-item label="属性id">
                <span>{{ props.row.pageConfigAttributeId }}</span>
              </el-form-item>
              <el-form-item label="备注" style="width: 100%;">
                <span>{{ props.row.remark }}</span>
              </el-form-item>
              <el-form-item label="创建时间">
                <span>{{ props.row.createTime }}</span>
              </el-form-item>
            </el-form>
          </template>
        </el-table-column>
        <el-table-column label="id" align="center" prop="pageConfigAttributeId" v-if="false" />
        <el-table-column label="名称" min-width="110px" align="center" prop="pageConfigAttributeName" show-overflow-tooltip />
        <el-table-column label="字段属性" min-width="110px" align="center" prop="pageConfigAttributeDesc" show-overflow-tooltip />
        <el-table-column :label="pageConfig.pageConfigDesc=='tableArea'?'展示类型':'类型'" width="80" align="center" prop="pageConfigAttributeType">
          <template #default="{row}">
            <dict-tag v-if="pageConfig.pageConfigDesc=='tableArea'" :options="system_display_type" :value="row.pageConfigAttributeType" />
            <dict-tag v-else :options="system_field_type" :value="row.pageConfigAttributeType" />
          </template>
        </el-table-column>
        <!-- <el-table-column label="规则" align="center" prop="pageConfigAttributeRule" /> -->
        <el-table-column label="是否显示" width="70" align="center" prop="pageConfigAttributeEnable">
          <template #default="{row}">
            <el-switch v-model="row.pageConfigAttributeEnable" active-value="1" inactive-value="0" @change="enableChange(row)" size="small"></el-switch>
          </template>
        </el-table-column>
        <el-table-column label="排序" width="50" align="center" prop="pageConfigAttributeSort" :formatter="formatter" />
        <el-table-column width="70" label="操作" fixed="right" align="center" class-name="small-padding fixed-width">
          <template #default="scope">
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

    <!-- 批量添加或修改属性对话框 -->
    <el-dialog v-model="multipleDialog.visible" :title="multipleDialog.title" width="85%" append-to-body>
      <!-- 表单批量新增 -->
      <attr-form-multiple 
        v-if="multipleDialog.visible&&pageConfig.pageConfigDesc=='formArea'" 
        ref="attrFormMultipleRef" 
        :id="pageConfigAttributeId" 
        :pageConfigId="pageConfig.pageConfigId" 
        :attrList="attrList"
        :tableList="allAttributeList">
      </attr-form-multiple>
      <!-- 列表批量新增 -->
      <attr-table-multiple
        v-else-if="multipleDialog.visible&&pageConfig.pageConfigDesc=='tableArea'" 
        ref="attrFormMultipleRef" 
        :id="pageConfigAttributeId" 
        :pageConfigId="pageConfig.pageConfigId" 
        :attrList="attrList"
        :tableList="allAttributeList">
      </attr-table-multiple>
      <!-- 搜索项批量新增 -->
      <attr-search-multiple
        v-else-if="multipleDialog.visible&&pageConfig.pageConfigDesc=='searchArea'" 
        ref="attrFormMultipleRef" 
        :id="pageConfigAttributeId" 
        :pageConfigId="pageConfig.pageConfigId" 
        :attrList="attrList"
        :tableList="allAttributeList">
      </attr-search-multiple>
      <template #footer>
        <div class="dialog-footer">
          <el-button :loading="buttonLoading" type="primary" @click="submitTable">确 定</el-button>
          <el-button @click="multipleCancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 添加或修改属性对话框 -->
    <el-dialog v-model="dialog.visible" :title="dialog.title" width="85%" append-to-body>
      <attr-form 
        v-if="dialog.visible&&pageConfig.pageConfigDesc=='formArea'" 
        ref="attrFormRef" 
        :id="pageConfigAttributeId" 
        :pageConfigId="pageConfig.pageConfigId" 
        :attrList="attrList">
      </attr-form>
      <attr-table 
        v-if="dialog.visible&&pageConfig.pageConfigDesc=='tableArea'" 
        ref="attrFormRef" 
        :id="pageConfigAttributeId" 
        :pageConfigId="pageConfig.pageConfigId" 
        :attrList="attrList">
      </attr-table>
      <attr-search 
        v-if="dialog.visible&&pageConfig.pageConfigDesc=='searchArea'" 
        ref="attrFormRef" 
        :id="pageConfigAttributeId" 
        :pageConfigId="pageConfig.pageConfigId" 
        :attrList="attrList">
      </attr-search>
      <template #footer>
        <div class="dialog-footer">
          <el-button :loading="buttonLoading" type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="BusinessObjectAttribute" lang="ts">
import {
  listPageAttribute, 
  getPageAttribute, 
  delPageAttribute, 
  addPageAttribute, 
  updatePageAttribute
} from '@/api/system/pageConfig/attribute';
import { listBusinessAttr } from '@/api/resourcepermission/businessObject/attribute';
import AttrForm from '@/views/system/pageConfig/component/attrForm.vue'
import AttrFormMultiple from '@/views/system/pageConfig/component/attrFormMultiple.vue'
import AttrSearch from '@/views/system/pageConfig/component/attrSearch.vue'
import AttrSearchMultiple from '@/views/system/pageConfig/component/attrSearchMultiple.vue'
import AttrTable from '@/views/system/pageConfig/component/attrTable.vue'
import AttrTableMultiple from '@/views/system/pageConfig/component/attrTableMultiple.vue'
import { usePageConfigStore } from '@/store/modules/pageConfig'

const props = defineProps({
  pageConfig: {
    type: Object,
    default: '',
    required: true
  },
  pageConfigName: {
    type: String,
    default: ''
  }
});

const { proxy } = getCurrentInstance() as ComponentInternalInstance;
const { system_field_type, system_display_type } = toRefs<any>(proxy?.useDict('system_field_type', 'system_display_type'));

const extendedAttributeList = ref([]);
const allAttributeList = ref([]);
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
const attrFormRef = ref(null);
const attrFormMultipleRef = ref(null);

const dialog = reactive({
  visible: false,
  loading: false,
  title: ''
});

const multipleDialog = reactive({
  visible: false,
  loading: false,
  title: ''
});

const data = reactive({
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    pageConfigId: '',
    pageConfigAttributeId: '',
    pageConfigAttributeName: '',
    pageConfigAttributeType: '',
    pageConfigAttributeValue: '',
    pageConfigAttributeDesc: '',
    pageConfigAttributeRule: '',
    remark: ''
  }
});

const { queryParams } = toRefs(data);


watch(() => props.pageConfig.pageConfigId,(newValue) => {
    if (newValue) {
      getList();
      getAttrList();
    }else{
      extendedAttributeList.value = [];
    }
  }
);

/** 查询属性列表 */
const getList = async () => {
  loading.value = true;
  queryParams.value.pageConfigId = props.pageConfig.pageConfigId;
  const res = await listPageAttribute(queryParams.value);
  extendedAttributeList.value = res.rows;
  total.value = res.total;
  loading.value = false;
};

const attrList = ref([]);
// 查询业务对象属性
const getAttrList = () => {
  listBusinessAttr({ businessId: props.pageConfig.pageConfigValue}).then(res=>{
    attrList.value = res.rows.sort((a: any,b: any)=>a.businessAttrSort-b.businessAttrSort);
  })
};
// 修改状态
const enableChange = async (row) =>{
  const text = row.pageConfigAttributeEnable === '0' ? '隐藏' : '显示';
  try {
    if(row.pageConfigAttributeEnable){
      const pageConfigAttributeValue = JSON.parse(row.pageConfigAttributeValue);
      pageConfigAttributeValue.isEnable = row.pageConfigAttributeEnable;
      row.pageConfigAttributeValue = JSON.stringify(pageConfigAttributeValue);
    }
    await proxy?.$modal.confirm(`确认要${text} '${row.pageConfigAttributeName}' 吗?`);
    await updatePageAttribute(row);
    // 清除缓存
    usePageConfigStore().removePageConfig(props.pageConfig.pageCode);
  } catch (err) {
    row.pageConfigAttributeEnable = row.pageConfigAttributeEnable === '0' ? '1' : '0';
  }
};

// 取消按钮
const multipleCancel = () => {
  multipleDialog.loading = false;
  multipleDialog.visible = false;
};

// 取消按钮
const cancel = () => {
  dialog.loading = false;
  dialog.visible = false;
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
  ids.value = selection.map((item:any) => item.pageConfigAttributeId);
  names.value = selection.map((item:any) => item.pageConfigAttributeName);
  single.value = selection.length != 1;
  multiple.value = !selection.length;
  selectRow.value = selection[0]||{};
};

const pageConfigAttributeId = ref('');
/** 新增按钮操作 */
const handleAdd = () => {
  dialog.visible = true;
  dialog.loading = false;
  pageConfigAttributeId.value = '';
  dialog.title = `添加【${props.pageConfig.pageConfigName}】属性`;
};

/** 批量新增按钮操作 */
const handleTableAdd = async () => {
  // 获取最新表格数据的所有行，避免分页影响数据回填
  const res = await listPageAttribute({
    pageNum: 1,
    pageSize: 999,
    pageConfigId: props.pageConfig.pageConfigId,
  } );
  allAttributeList.value = res.rows;
  multipleDialog.visible = true;
  multipleDialog.loading = false;
  multipleDialog.title = `批量添加【${props.pageConfig.pageConfigName}】属性`;
};

/** 修改按钮操作 */
const handleUpdate = async (row) => {
  pageConfigAttributeId.value = row?.pageConfigAttributeId || ids.value[0];
  dialog.visible = true;
  dialog.title = `修改【${props.pageConfig.pageConfigName}】属性`;
};

/** 批量提交按钮 */
const submitTable = () => {
  attrFormMultipleRef.value.validate(async (valid: boolean) => {
    if (valid) {
      const datas = attrFormMultipleRef.value.achieveData();
      console.log(datas)
      buttonLoading.value = true;
      const PromiseList = []
      datas.forEach(async (item) => {
          if (item.pageConfigAttributeId) {
            PromiseList.push(updatePageAttribute(item))
          } else {
            PromiseList.push(addPageAttribute(item))
          }
      })
      await Promise.all(PromiseList)
      // 清除缓存
      usePageConfigStore().removePageConfig(props.pageConfig.pageCode);
      proxy?.$modal.msgSuccess('操作成功');
      buttonLoading.value = false;
      multipleDialog.visible = false;
      multipleDialog.loading = false;
      await getList();
    }
  });
};

/** 提交按钮 */
const submitForm = () => {
  attrFormRef.value.validate(async (valid: boolean) => {
    if (valid) {
      const data = attrFormRef.value.achieveData();
      buttonLoading.value = true;
      if (data.pageConfigAttributeId) {
        await updatePageAttribute(data).finally(() => (buttonLoading.value = false));
      } else {
        await addPageAttribute(data).finally(() => (buttonLoading.value = false));
      }
      // 清除缓存
      usePageConfigStore().removePageConfig(props.pageConfig.pageCode);
      proxy?.$modal.msgSuccess('操作成功');
      dialog.visible = false;
      dialog.loading = false;
      await getList();
    }
  });
};

/** 删除按钮操作 */
const handleDelete = async (row) => {
  const _ids = row?.pageConfigAttributeId || ids.value;
  const _names = row?.pageConfigAttributeName || names.value;
  await proxy?.$modal.confirm('是否确认删除名称为"' + _names + '"的数据项？');
  loading.value = true;
  await delPageAttribute(_ids).finally(() => (loading.value = false));
  // 清除缓存
  usePageConfigStore().removePageConfig(props.pageConfig.pageCode);
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

const formatter = (row, column) => {
  return row.pageConfigAttributeSort
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
