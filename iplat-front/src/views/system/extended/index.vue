<template>
  <div class="p-2">
    <el-row :gutter="10">
      <el-col :xs="24" :sm="12">
        <el-card class="box-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <span>扩展列表</span>
              <el-button v-hasPermi="['system:extended:add']" @click="handleAdd" class="button" type="primary" size="small" icon="Plus" style="float: right;">新增</el-button>
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
                <el-form-item label="扩展名称" prop="extendedName">
                  <el-input v-model="queryParams.extendedName" placeholder="请输入扩展名称" clearable @change="handleQuery" @keyup.enter="handleQuery" />
                </el-form-item>
                <el-form-item class="noMarginRight">
                  <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
                  <el-button icon="Refresh" @click="resetQuery">重置</el-button>
                </el-form-item>
              </el-form>
            </div>
          </transition>
          <el-table ref="extendedTableRef" v-loading="loading" border :data="extendedList" size="small" highlight-current-row @current-change="handleCurrentChange">
            <el-table-column type="expand">
              <template #default="props">
                <el-form label-position="left" inline size="small" label-width="auto" label-suffix="：" class="demo-table-expand p-2">
                  <el-form-item label="页面id">
                    <span>{{ props.row.pageId }}</span>
                  </el-form-item>
                  <el-form-item label="页面编号">
                    <span>{{ props.row.pageCode }}</span>
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
            <el-table-column label="编号" align="center" prop="extendedId" v-if="false" />
            <el-table-column label="扩展名称" min-width="120px" align="center" prop="extendedName" show-overflow-tooltip />
            <el-table-column label="扩展描述" align="center" prop="extendedDesc" show-overflow-tooltip />
            <el-table-column label="备注" align="center" prop="remark" show-overflow-tooltip />
            <el-table-column width="80" label="操作" align="center" class-name="small-padding fixed-width">
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
      </el-col>
      <el-col :xs="24" :sm="12">
        <Attribute ref="attributeRef" :extendedId="selectRow.extendedId" :extendedName="selectRow.extendedName"></Attribute>
      </el-col>
    </el-row>

    <!-- 添加或修改扩展管理对话框 -->
    <el-dialog v-model="dialog.visible" :title="dialog.title" width="550px" append-to-body>
      <el-form ref="extendedFormRef" :model="form" v-loading="dialog.loading" :rules="rules" label-width="auto">
        <el-form-item label="扩展名称" prop="extendedName">
          <el-input v-model="form.extendedName" placeholder="请输入扩展名称" />
        </el-form-item>
        <el-form-item label="页面id" prop="pageId">
          <el-input v-model="form.pageId" placeholder="请输入页面id" />
        </el-form-item>
        <el-form-item label="页面编号" prop="pageCode">
          <el-input v-model="form.pageCode" placeholder="请输入页面编号" />
        </el-form-item>
        <el-form-item label="扩展描述" prop="extendedDesc">
          <el-input v-model="form.extendedDesc" type="textarea" :autosize="{ minRows: 2 }" placeholder="请输入扩展描述" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" :autosize="{ minRows: 2 }" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button :loading="buttonLoading" type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="Extended" lang="ts">
import {
  listExtended, 
  getExtended, 
  delExtended, 
  addExtended, 
  updateExtended
} from '@/api/system/extended';
import Attribute from '@/views/system/extended/attribute.vue';

const { proxy } = getCurrentInstance() as ComponentInternalInstance;

const extendedList = ref([]);
const buttonLoading = ref(false);
const loading = ref(true);
const showSearch = ref(true);
const ids = ref<Array<string | number>>([]);
const names = ref<Array<string | number>>([]);
const single = ref(true);
const selectRow = ref({
  extendedId: '',
  extendedName: ''
});
const multiple = ref(true);
const total = ref(0);

const queryFormRef = ref<ElFormInstance>();
const extendedFormRef = ref<ElFormInstance>();
const extendedTableRef = ref<ElTableInstance>();
const attributeRef = ref();

const dialog = reactive({
  visible: false,
  loading: false,
  title: ''
});

const initFormData = {
  extendedId: '',
  pageId: '',
  pageCode: '',
  extendedName: '',
  extendedDesc: '',
  remark: ''
};
const data = reactive({
  form: { ...initFormData },
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    extendedId: '',
    pageId: '',
    pageCode: '',
    extendedName: '',
    extendedDesc: '',
    remark: ''
  },
  rules: {
    extendedName: [{ required: true, message: '扩展名称不能为空', trigger: 'blur' }],
    extendedName: [{ required: true, message: '名称不能为空', trigger: 'blur' }],
    extendedContent: [{ required: true, message: '内容不能为空', trigger: 'blur' }],
  }
});

const { queryParams, form, rules } = toRefs(data);


/** 查询扩展管理列表 */
const getList = async () => {
  loading.value = true;
  const res = await listExtended(queryParams.value);
  extendedList.value = res.rows;
  total.value = res.total;
  loading.value = false;
  setCurrentRow();
};

// 设置默认选中
const setCurrentRow = () => {
  // 默认选中第一条
  let index = 0;
  if(selectRow.value.extendedId){
    index = extendedList.value.findIndex(item=>item.extendedId==selectRow.value.extendedId)||0;
    index = index >=0 ? index : 0;
  }
  selectRow.value = extendedList.value[index]||{}
  extendedTableRef.value.setCurrentRow(extendedList.value[index]);
}

// 取消按钮
const cancel = () => {
  reset();
  dialog.loading = false;
  dialog.visible = false;
};

// 表单重置
const reset = () => {
  form.value = { ...initFormData };
  extendedFormRef.value?.resetFields();
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
  ids.value = selection.map((item:any) => item.extendedId);
  names.value = selection.map((item:any) => item.extendedName);
  single.value = selection.length != 1;
  multiple.value = !selection.length;
  selectRow.value = selection[0]||{};
};

// 选中数据
const handleCurrentChange = (val:any) => {
  selectRow.value = val||{};
};


/** 新增按钮操作 */
const handleAdd = () => {
  reset();
  dialog.visible = true;
  dialog.loading = false;
  dialog.title = '添加扩展管理';
};

/** 修改按钮操作 */
const handleUpdate = async (row) => {
  reset();
  const _id = row?.extendedId || ids.value[0];
  dialog.visible = true;
  dialog.loading = true;
  dialog.title = '修改扩展管理';
  const res = await getExtended(_id);
  Object.assign(form.value, res.data);
  dialog.loading = false;
};

/** 提交按钮 */
const submitForm = () => {
  extendedFormRef.value?.validate(async (valid: boolean) => {
    if (valid) {
      buttonLoading.value = true;
      if (form.value.extendedId) {
        await updateExtended(form.value).finally(() => (buttonLoading.value = false));
      } else {
        await addExtended(form.value).finally(() => (buttonLoading.value = false));
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
  const _ids = row?.extendedId || ids.value;
  const _names = row?.extendedName || names.value;
  await proxy?.$modal.confirm('是否确认删除扩展属性名称为"' + _names + '"的数据项？');
  loading.value = true;
  await delExtended(_ids).finally(() => (loading.value = false));
  await getList();
  proxy?.$modal.msgSuccess('删除成功');
};

/** 导出按钮操作 */
const handleExport = () => {
  proxy?.download(
    'system/extended/export',
    {
      ...queryParams.value
    },
    `extended_${new Date().getTime()}.xlsx`
  );
};

onMounted(() => {
  getList();
});
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
