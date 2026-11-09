<template>
  <div class="p-2">
    <transition :enter-active-class="proxy?.animate.searchAnimate.enter"
      :leave-active-class="proxy?.animate.searchAnimate.leave">
      <div v-show="showSearch" class="mb-[10px]">
        <el-card shadow="hover">
          <el-form ref="queryFormRef" :model="queryParams" :inline="true" label-width="auto">
            <el-form-item label="模板名称" prop="noticeTitle">
              <el-input v-model="queryParams.noticeTitle" placeholder="请输入模板名称" clearable @keyup.enter="handleQuery" />
            </el-form-item>
            <el-form-item label="操作人员" prop="createByName">
              <el-input v-model="queryParams.createByName" placeholder="请输入操作人员" clearable @keyup.enter="handleQuery" />
            </el-form-item>
            <el-form-item label="类型" prop="noticeType">
              <el-select v-model="queryParams.noticeType" placeholder="检查项模板类型" clearable>
                <el-option v-for="dict in eqms_inspection_template_type" :key="dict.value" :label="dict.label"
                  :value="dict.value" />
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
        <el-row :gutter="10" class="mb8">
          <el-col :span="1.5">
            <el-button v-hasPermi="['system:inspectionTemplate:add']" type="primary" plain icon="Plus"
              @click="handleAdd">新增</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button v-hasPermi="['system:inspectionTemplate:edit']" type="success" plain icon="Edit"
              :disabled="single" @click="handleUpdate()">修改</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button v-hasPermi="['system:inspectionTemplate:remove']" type="danger" plain icon="Delete"
              :disabled="multiple" @click="handleDelete()">
              删除
            </el-button>
          </el-col>
          <right-toolbar v-model:show-search="showSearch" @query-table="getList"></right-toolbar>
        </el-row>
      </template>

      <el-table v-loading="loading" border :data="noticeList" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column v-if="false" label="id" align="center" prop="templateId" width="100" />
        <el-table-column label="序号" type="index" align="center" width="55" />
        <el-table-column label="模板编号" align="center" prop="templateCode" />
        <el-table-column label="模板名称" align="center" prop="templateName" :show-overflow-tooltip="true" />
        <el-table-column label="模板英文名" align="center" prop="templateNameEn" :show-overflow-tooltip="true" />
        <el-table-column label="版本号" align="center" prop="versionNo" />
        <el-table-column label="模板排序" align="center" prop="templateSort" />
        <el-table-column label="模板类别" align="center" prop="templateCategory" width="100">
          <template #default="scope">
            <dict-tag :options="eqms_inspection_template_type" :value="scope.row.noticeType" />
          </template>
        </el-table-column>
        <!-- <el-table-column label="状态" align="center" prop="status" width="100">
          <template #default="scope">
            <dict-tag :options="sys_notice_status" :value="scope.row.status" />
          </template>
        </el-table-column>
        <el-table-column label="创建者" align="center" prop="createByName" width="100" />
        <el-table-column label="创建时间" align="center" prop="createTime" width="100">
          <template #default="scope">
            <span>{{ proxy.parseTime(scope.row.createTime, '{y}-{m}-{d}') }}</span>
          </template>
        </el-table-column> -->
        <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
          <template #default="scope">
            <el-tooltip content="修改" placement="top">
              <el-button v-hasPermi="['system:inspectionTemplate:edit']" link type="primary" icon="Edit"
                @click="handleUpdate(scope.row)"></el-button>
            </el-tooltip>
            <el-tooltip content="删除" placement="top">
              <el-button v-hasPermi="['system:inspectionTemplate:remove']" link type="primary" icon="Delete"
                @click="handleDelete(scope.row)"></el-button>
            </el-tooltip>
          </template>
        </el-table-column>
      </el-table>

      <pagination v-show="total > 0" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize"
        :total="total" @pagination="getList" />
    </el-card>
    <!-- 添加或修改检查项模板对话框 -->
    <el-dialog v-model="dialog.visible" :title="dialog.title" width="60%">
      <el-form ref="noticeFormRef" :model="form" :rules="rules" label-width="auto">
        <el-row>
          <!-- <el-col :span="12">
            <el-form-item label="模板编号" prop="templateCode">
              <el-input v-model="form.templateCode" placeholder="请输入模板编号" />
            </el-form-item>
          </el-col> -->
          <el-col :span="12">
            <el-form-item label="模板名称" prop="templateName">
              <el-input v-model="form.templateName" placeholder="请输入模板名称" style="width: 300px" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="模板英文名" prop="templateNameEn">
              <el-input v-model="form.templateNameEn" placeholder="请输入模板英文名" style="width: 300px" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="模板排序" prop="templateSort">
              <el-input v-model="form.templateSort" placeholder="请输入模板排序" style="width: 300px" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="模板类别" prop="templateCategory">
              <el-select v-model="form.templateCategory" placeholder="请选择模板类别" style="width: 300px">
                <el-option v-for="dict in eqms_inspection_template_type" :key="dict.value" :label="dict.label"
                  :value="dict.value"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <el-row :gutter="10" style="margin-bottom: 10px;">
        <el-col :span="1.5">
          <el-button type="primary" icon="Plus" @click="handleAddItems()">新增
          </el-button>
        </el-col>
      </el-row>
      <el-table ref="itemTableRef" :data="form.items" row-key="itemId" border
        :tree-props="{ children: 'children', hasChildren: 'hasChildren' }">
        <el-table-column prop="itemCode" label="条目编码" :show-overflow-tooltip="true"></el-table-column>
        <el-table-column prop="itemName" label="条目名称" :show-overflow-tooltip="true"></el-table-column>
        <el-table-column prop="itemNameEn" label="条目英文名" :show-overflow-tooltip="true"></el-table-column>
        <el-table-column prop="itemSort" label="条目排序" width="80"></el-table-column>
        <el-table-column prop="confirmMethod" label="确认方式"></el-table-column>
        <el-table-column label="操作" width="230">
          <template #default="scope">
            <el-button link type="primary" icon="Edit" @click="editItemHandle(scope.row)">修改</el-button>
            <el-button link type="primary" icon="Delete" @click="deleteItemHandle(scope.row)">删除</el-button>
            <el-button link type="primary" icon="Plus" @click="handleAddChild(scope.row)">新增</el-button>
          </template>
        </el-table-column>
      </el-table>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 添加子项对话框 -->
    <el-dialog v-model="addChildDialog.visible" :title="addChildDialog.title" width="600px" append-to-body>
      <el-form ref="addChildFormRef" :model="addChildForm" :rules="addChildRules" label-width="120px">
        <!-- 在修改节点时显示树形选择器 -->
        <el-form-item v-if="isEditing" label="移动到" prop="targetParent">
          <el-tree-select v-model="addChildForm.targetParent" :data="treeData"
            :props="{ value: 'itemId', label: 'itemName', children: 'children' }" check-strictly
            :render-after-expand="false" placeholder="请选择新的父节点" clearable />
        </el-form-item>
        <!-- 在新增根节点时显示树形选择器 -->
        <el-form-item v-if="!isEditing && isAddingToParent" label="添加到" prop="targetParent">
          <el-tree-select v-model="addChildForm.targetParent" :data="allTreeData"
            :props="{ value: 'itemId', label: 'itemName', children: 'children' }" check-strictly
            :render-after-expand="false" placeholder="请选择父节点" clearable />
        </el-form-item>
        <el-form-item label="条目编码" prop="itemCode">
          <el-input v-model="addChildForm.itemCode" placeholder="请输入条目编码" />
        </el-form-item>
        <el-form-item label="条目名称" prop="itemName">
          <el-input v-model="addChildForm.itemName" placeholder="请输入条目名称" />
        </el-form-item>
        <el-form-item label="条目英文名" prop="itemNameEn">
          <el-input v-model="addChildForm.itemNameEn" placeholder="请输入条目英文名" />
        </el-form-item>
        <el-form-item label="条目排序" prop="itemSort">
          <el-input v-model="addChildForm.itemSort" placeholder="请输入条目排序" />
        </el-form-item>
        <el-form-item label="确认方式" prop="confirmMethod">
          <el-input v-model="addChildForm.confirmMethod" placeholder="请输入确认方式" />
        </el-form-item>
        <el-form-item label="检查内容" prop="content">
          <el-input v-model="addChildForm.content" type="textarea" placeholder="请输入检查内容" />
        </el-form-item>
        <el-form-item label="检查结果" prop="result">
          <el-input v-model="addChildForm.result" placeholder="请输入检查结果" />
        </el-form-item>
        <el-form-item label="单选框字典" prop="remark">
          <el-input v-model="addChildForm.remark" placeholder="请输入单选框字典" />
        </el-form-item>
        
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitAddChildForm">确 定</el-button>
          <el-button @click="cancelAddChild">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="InspectionTemplate" lang="ts">
import { ElDialog } from 'element-plus';
import { listConfirmTemplate, getConfirmTemplate, delConfirmTemplate, addConfirmTemplate, updateConfirmTemplate } from '@/api/eqms/inspectionTemplate';

const { proxy } = getCurrentInstance() as ComponentInternalInstance;
const { sys_notice_status, eqms_inspection_template_type } = toRefs<any>(proxy?.useDict('sys_notice_status', 'eqms_inspection_template_type'));

const noticeList = ref([]);
const loading = ref(true);
const showSearch = ref(true);
const ids = ref<Array<string | number>>([]);
const single = ref(true);
const multiple = ref(true);
const total = ref(0);

const queryFormRef = ref<ElFormInstance>();
const noticeFormRef = ref<ElFormInstance>();
const addChildFormRef = ref<ElFormInstance>();

const dialog = reactive<DialogOption>({
  visible: false,
  title: ''
});

const initFormData = {
  templateId: '',
  templateCode: undefined,
  templateName: '',
  templateNameEn: '',
  templateSort: '',
  templateCategory: ''
};
const data = reactive<PageData<any, any>>({
  form: { ...initFormData },
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    noticeTitle: '',
    createByName: '',
    status: '',
    noticeType: ''
  },
  rules: {
    noticeTitle: [{ required: true, message: '模板名称不能为空', trigger: 'blur' }],
    noticeType: [{ required: true, message: '检查项模板类型不能为空', trigger: 'change' }]
  }
});

const { queryParams, form, rules } = toRefs(data);

// 新增子项对话框相关
const addChildDialog = reactive<DialogOption>({
  visible: false,
  title: ''
});

// 记录当前要添加子项的父行
const currentParentRow = ref<any>(null);

// 是否正在编辑
const isEditing = ref(false);

// 是否是添加到父节点
const isAddingToParent = ref(false);

// 树形选择器的数据
const treeData = ref([]);
// 所有节点的树形选择器数据
const allTreeData = ref([]);

// 新增子项表单数据
const initAddChildFormData = {
  itemCode: '',
  itemName: '',
  itemNameEn: '',
  itemSort: '',
  confirmMethod: '',
  content: '',
  result: '',
  remark: '',
  targetParent: null
};

const addChildData = reactive({
  form: { ...initAddChildFormData },
  rules: {
    itemCode: [{ required: true, message: '条目编码不能为空', trigger: 'blur' }]
  }
});

const { form: addChildForm, rules: addChildRules } = toRefs(addChildData);

/** 查询检查项模板列表 */
const getList = async () => {
  loading.value = true;
  const res = await listConfirmTemplate(queryParams.value);
  noticeList.value = res.rows;
  total.value = res.total;
  loading.value = false;
};
/** 取消按钮 */
const cancel = () => {
  reset();
  dialog.visible = false;
};
/** 表单重置 */
const reset = () => {
  form.value = { ...initFormData };
  noticeFormRef.value?.resetFields();
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
/** 多选框选中数据 */
const handleSelectionChange = (selection: Array<any>) => {
  ids.value = selection.map((item) => item.templateId);
  single.value = selection.length != 1;
  multiple.value = !selection.length;
};
/** 新增按钮操作 */
const handleAdd = () => {
  reset();
  dialog.visible = true;
  dialog.title = '添加检查项模板';
};
/**修改按钮操作 */
const handleUpdate = async (row?: any) => {
  reset();
  const templateId = row?.templateId || ids.value[0];
  const { data } = await getConfirmTemplate(templateId);
  data.items = data.items || []
  Object.assign(form.value, data);
  dialog.visible = true;
  dialog.title = '修改检查项模板';
};
/** 提交按钮 */
const submitForm = () => {
  noticeFormRef.value?.validate(async (valid: boolean) => {
    if (valid) {
      // 递归删除form.value.items中的所有节点的targetParent字段
      const removeTargetParent = (items: any[]) => {
        if (items && items.length > 0) {
          items.forEach(item => {
            // 删除当前节点的targetParent字段
            if (item.hasOwnProperty('targetParent')) {
              delete item.targetParent;
            }
            if (item.hasOwnProperty('itemId') && item.itemId && item.itemId.startsWith('temp_')) {
              delete item.itemId;
            }
            if (item.hasOwnProperty('parentId') && item.parentId && item.parentId.startsWith('temp_')) {
              delete item.parentId;
            }

            // 递归处理子节点
            if (item.children && item.children.length > 0) {
              removeTargetParent(item.children);
            }
          });
        }
      };

      // 如果存在items，则递归删除targetParent字段
      if (form.value.items && form.value.items.length > 0) {
        removeTargetParent(form.value.items);
      }

      form.value.templateId ? await updateConfirmTemplate(form.value) : await addConfirmTemplate(form.value);
      proxy?.$modal.msgSuccess('操作成功');
      dialog.visible = false;
      await getList();
    }
  });
};
/** 删除按钮操作 */
const handleDelete = async (row?: any) => {
  const templateIds = row?.templateId || ids.value;
  await proxy?.$modal.confirm('是否确认删除检查项模板编号为"' + templateIds + '"的数据项？');
  await delConfirmTemplate(templateIds);
  await getList();
  proxy?.$modal.msgSuccess('删除成功');
};

// 处理添加子项
const handleAddChild = (row: any) => {
  isAddingToParent.value = false;
  currentParentRow.value = row;
  resetAddChildForm();
  addChildDialog.visible = true;
  addChildDialog.title = `为 "${row.itemName || row.itemCode}" 添加子项`;
};

// 处理添加条目到顶层
const handleAddItems = () => {
  isAddingToParent.value = true;
  isEditing.value = false;
  // 设置currentParentRow为一个特殊的根对象，用于标识这是添加顶层条目
  currentParentRow.value = { itemId: form.value.templateId, itemName: '模板根节点', itemCode: 'ROOT' };
  resetAddChildForm();

  // 生成所有节点的树形选择器数据
  allTreeData.value = generateAllTreeData(form.value.items);

  addChildDialog.visible = true;
  addChildDialog.title = '添加新条目';
};

// 重置添加子项表单
const resetAddChildForm = () => {
  addChildForm.value = { ...initAddChildFormData };
  addChildFormRef.value?.resetFields();
};

// 编辑条目
const editItemHandle = (row: any) => {
  // 设置当前编辑的节点
  currentParentRow.value = row;
  // 将当前节点的数据填充到表单
  addChildForm.value = {
    itemCode: row.itemCode,
    itemName: row.itemName,
    itemNameEn: row.itemNameEn,
    itemSort: row.itemSort,
    confirmMethod: row.confirmMethod,
    content: row.content,
    result: row.result,
    remark: row.remark,
    targetParent: null // 初始化目标父节点为null
  };

  // 生成树形选择器的数据（排除当前节点及其子节点）
  treeData.value = generateTreeData(form.value.items, row.itemId);

  // 设置为编辑状态
  isEditing.value = true;
  addChildDialog.visible = true;
  addChildDialog.title = `编辑条目 "${row.itemName || row.itemCode}"`;
};

// 生成树形选择器的数据（排除指定节点及其子节点）
const generateTreeData = (items: any[], excludeId: string | number) => {
  const result: any[] = [];

  const traverse = (nodes: any[], parentId: string | number | null = null) => {
    for (const node of nodes) {
      // 如果当前节点不是要排除的节点，则添加到结果中
      if (node.itemId !== excludeId) {
        // 创建一个新对象，避免修改原始数据
        const newNode = { ...node };
        // 只保留需要显示的字段
        newNode.itemName = node.itemName || node.itemCode;

        // 递归处理子节点
        if (node.children && node.children.length > 0) {
          newNode.children = traverse(node.children, node.itemId);
        } else {
          newNode.children = [];
        }

        result.push(newNode);
      }
    }
    return result;
  };

  // 清空临时结果数组
  result.length = 0;
  // 遍历所有根节点
  for (const node of items) {
    // 如果当前节点不是要排除的节点，则添加到结果中
    if (node.itemId !== excludeId) {
      const newNode = { ...node };
      newNode.itemName = node.itemName || node.itemCode;

      // 递归处理子节点
      if (node.children && node.children.length > 0) {
        newNode.children = [];
        for (const child of node.children) {
          addNodeRecursively(child, newNode.children, excludeId);
        }
      } else {
        newNode.children = [];
      }

      result.push(newNode);
    }
  }

  return result;
};

// 递归添加节点到树形数据（排除指定节点）
const addNodeRecursively = (node: any, targetArray: any[], excludeId: string | number) => {
  if (node.itemId === excludeId) {
    return; // 如果是要排除的节点，直接返回
  }

  const newNode = { ...node };
  newNode.itemName = node.itemName || node.itemCode;

  if (node.children && node.children.length > 0) {
    newNode.children = [];
    for (const child of node.children) {
      addNodeRecursively(child, newNode.children, excludeId);
    }
  } else {
    newNode.children = [];
  }

  targetArray.push(newNode);
};

// 生成所有节点的树形选择器数据
const generateAllTreeData = (items: any[]) => {
  const result: any[] = [];

  const traverse = (nodes: any[], targetArray: any[]) => {
    for (const node of nodes) {
      const newNode = { ...node };
      newNode.itemName = node.itemName || node.itemCode;

      // 递归处理子节点
      if (node.children && node.children.length > 0) {
        newNode.children = [];
        traverse(node.children, newNode.children);
      } else {
        newNode.children = [];
      }

      targetArray.push(newNode);
    }
  };

  traverse(items, result);
  return result;
};

// 提交添加子项表单
const submitAddChildForm = () => {
  addChildFormRef.value?.validate(async (valid: boolean) => {
    if (valid) {
      // 获取当前编辑或添加的节点
      const currentEditItem = currentParentRow.value;

      // 更新当前节点的字段
      Object.assign(currentEditItem, {
        itemCode: addChildForm.value.itemCode,
        itemName: addChildForm.value.itemName,
        itemNameEn: addChildForm.value.itemNameEn,
        itemSort: addChildForm.value.itemSort,
        confirmMethod: addChildForm.value.confirmMethod,
        content: addChildForm.value.content,
        result: addChildForm.value.result,
        remark: addChildForm.value.remark,
      });

      // 如果是编辑状态并且选择了新的父节点，则移动节点
      if (isEditing.value) {
        // 移动节点到新的父节点
        if (addChildForm.value.targetParent) {
          moveNodeToNewParent(currentEditItem, addChildForm.value.targetParent);
        } else {
          // 更新当前正在编辑的节点
          // 当前节点的字段已经在 Object.assign 中更新了
          // 现在只需要确保更改能正确反映在UI上
        }

      } else {
        // 处理新增节点的情况
        // 生成临时ID
        const tempId = `temp_${Date.now()}`;

        // 创建子项数据
        const childItem = {
          itemId: tempId,
          // 如果是添加到特定父节点（通过树形选择器选择），则parentId为选择的节点ID，否则为当前行的itemId
          parentId: isAddingToParent.value && addChildForm.value.targetParent ?
            addChildForm.value.targetParent : currentParentRow.value.itemId,
          // 如果是添加到特定父节点，则level为该节点的level+1，否则按原来逻辑处理
          level: isAddingToParent.value && addChildForm.value.targetParent ?
            (findNodeById(form.value.items, addChildForm.value.targetParent)?.level || 0) + 1 :
            (isAddingToParent.value ? 1 : (currentParentRow.value.level || 0) + 1),
          ...addChildForm.value
        };

        // 如果是添加到特定父节点
        if (isAddingToParent.value && addChildForm.value.targetParent) {
          const targetParent = findNodeById(form.value.items, addChildForm.value.targetParent);
          if (targetParent) {
            // 如果目标父节点没有children数组，创建一个
            if (!targetParent.children) {
              targetParent.children = [];
            }
            // 将子项添加到目标父节点的children数组中
            targetParent.children.push(childItem);
          }
        } else if (isAddingToParent.value) {
          // 如果是添加到根节点，则直接添加到form.items数组
          form.value.items = form.value.items || []; // 确保items数组存在
          form.value.items.push(childItem);
        } else {
          // 如果当前行没有children字段，则创建一个空数组
          if (!currentParentRow.value.children) {
            currentParentRow.value.children = [];
          }

          // 将子项添加到父项的children数组中
          currentParentRow.value.children.push(childItem);
        }
      }

      // 关闭对话框并重置表单
      addChildDialog.visible = false;
      resetAddChildForm();
      // 重置编辑状态
      isEditing.value = false;
      isAddingToParent.value = false;
    }
  });
};

// 移动节点到新的父节点
const moveNodeToNewParent = (nodeToMove: any, newParentId: string | number) => {
  // 从原位置删除节点
  removeFromParent(nodeToMove);

  // 查找新的父节点
  const newParent = findNodeById(form.value.items, newParentId);

  if (newParent) {
    // 如果新父节点没有children数组，创建一个
    if (!newParent.children) {
      newParent.children = [];
    }

    // 将节点添加到新父节点的children中
    newParent.children.push(nodeToMove);

    // 更新节点的parentId和level
    updateNodeParentAndLevel(nodeToMove, newParent.itemId, newParent.level + 1);
  } else {
    // 如果newParentId是0或表示根节点，将节点添加到form.items中
    if (newParentId === 0 || newParentId === '0') {
      form.value.items.push(nodeToMove);
      // 设置为根节点
      nodeToMove.parentId = 0;
      nodeToMove.level = 1;
    }
  }
};

// 从父节点中删除节点
const removeFromParent = (nodeToRemove: any) => {
  // 如果是根节点，从form.items中删除
  if (nodeToRemove.level === 1) {
    const index = form.value.items.findIndex((item: any) => item.itemId === nodeToRemove.itemId);
    if (index > -1) {
      form.value.items.splice(index, 1);
    }
  } else {
    // 否则，查找其父节点并从children中删除
    const removeChild = (items: any[]) => {
      for (let i = 0; i < items.length; i++) {
        const item = items[i];
        if (item.children && item.children.some((child: any) => child.itemId === nodeToRemove.itemId)) {
          const childIndex = item.children.findIndex((child: any) => child.itemId === nodeToRemove.itemId);
          if (childIndex > -1) {
            item.children.splice(childIndex, 1);
            return true;
          }
        }
        // 如果当前项不是要删除的项，但有children，则递归查找
        if (item.children && item.children.length) {
          if (removeChild(item.children)) {
            return true;
          }
        }
      }
      return false;
    };

    removeChild(form.value.items);
  }
};

// 查找指定ID的节点
const findNodeById = (items: any[], id: string | number) => {
  for (const item of items) {
    if (item.itemId === id) {
      return item;
    }

    if (item.children && item.children.length) {
      const found = findNodeById(item.children, id);
      if (found) {
        return found;
      }
    }
  }
  return null;
};

// 更新节点的父ID和层级，以及其所有子节点的层级
const updateNodeParentAndLevel = (node: any, newParentId: string | number, newLevel: number) => {
  // 更新当前节点的parentId和level
  node.parentId = newParentId;
  node.level = newLevel;

  // 如果当前节点有子节点，递归更新子节点的层级
  if (node.children && node.children.length > 0) {
    for (const child of node.children) {
      updateNodeParentAndLevel(child, node.itemId, newLevel + 1);
    }
  }
};

// 取消添加子项
const cancelAddChild = () => {
  addChildDialog.visible = false;
  resetAddChildForm();
  // 重置编辑状态
  isEditing.value = false;
};

// 删除节点
const deleteItemHandle = (row: any) => {
  proxy?.$modal.confirm('是否确认删除条目"' + (row.itemName || row.itemCode) + '"？').then(async () => {
    // 如果level为1，表示是根节点，从form.items中删除
    if (row.level === 1) {
      const index = form.value.items.findIndex((item: any) => item.itemId === row.itemId);
      if (index > -1) {
        form.value.items.splice(index, 1);
      }
    } else {
      // 否则是子节点，需要在父节点的children中删除
      // 遍历查找父节点并删除对应的子节点
      const removeChild = (items: any[]) => {
        for (let i = 0; i < items.length; i++) {
          const item = items[i];
          // 如果当前项就是我们要删除的项，直接删除
          if (item.itemId === row.itemId) {
            items.splice(i, 1);
            return true; // 找到并删除了，返回true
          }
          // 如果当前项不是要删除的项，但有children，则递归查找
          if (item.children && item.children.length) {
            if (removeChild(item.children)) {
              return true; // 在子级找到了并删除了，返回true
            }
          }
        }
        return false; // 没有找到要删除的项
      };

      // 从form.items开始递归查找并删除
      removeChild(form.value.items);
    }
  });
};

onMounted(() => {
  getList();
});
</script>