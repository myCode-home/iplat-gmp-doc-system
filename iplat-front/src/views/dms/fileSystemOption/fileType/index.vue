<template>
  <div class="app-container">
    <el-row :gutter="20">
      <!-- 左侧树形选择器 -->
      <el-col :lg="6" :xs="24" style="margin-bottom: 20px">
        <el-card shadow="hover">
          <template #header>
            <span>文件分类</span>
          </template>
          <el-input v-model="filterText" placeholder="输入关键字进行过滤" prefix-icon="Search" clearable />
          <div class="tree-container">
            <el-tree
              ref="treeRef"
              class="mt-2"
              node-key="id"
              :data="treeData"
              :props="{ label: 'sortName', children: 'children' }"
              :expand-on-click-node="false"
              :filter-node-method="filterNode"
              :highlight-current="true"
              default-expand-all
              @node-click="handleNodeClick"
            >
              <template #default="{ node, data }">
                <span class="tree-node" :class="data.status === 'failure' ? 'disabled-node' : ''">
                  <span v-if="data.status === 'failure'" class="node-status-icon">
                    <el-icon>
                      <CircleClose />
                    </el-icon>
                  </span>
                  <span>{{ node.label }}</span>
                </span>
              </template>
            </el-tree>
          </div>
        </el-card>
      </el-col>

      <!-- 右侧内容区域 -->
      <el-col :lg="18" :xs="24">
        <!-- 上半部分：操作按钮和节点详情 -->
        <el-card shadow="hover" style="margin-bottom: 20px">
          <template #header>
            <div class="card-header">
              <span>分类详情</span>
            </div>
          </template>

          <!-- 操作按钮 -->
          <el-row :gutter="10" class="mb-2">
            <el-col :span="1.5">
              <el-button type="primary" plain icon="Plus" @click="handleAdd">新增</el-button>
            </el-col>
            <el-col :span="1.5">
              <el-button type="success" plain :disabled="!selectedNode" icon="Edit" @click="handleEdit">编辑</el-button>
            </el-col>
            <el-col :span="1.5">
              <el-button type="danger" plain :disabled="!selectedNode" icon="Delete" @click="handleDelete">删除</el-button>
            </el-col>
            <el-col :span="1.5" v-if="selectedNode">
              <el-button
                :type="selectedNode.status === 'active' ? 'info' : 'primary'"
                plain
                :icon="selectedNode.status === 'active' ? 'VideoPause' : 'VideoPlay'"
                @click="handleToggleStatus"
              >
                {{ selectedNode.status === 'active' ? '禁用' : '启用' }}
              </el-button>
            </el-col>
          </el-row>

          <!-- 节点详情 -->
          <div v-if="selectedNode" class="node-details">
            <el-descriptions :column="2" border>
              <el-descriptions-item label="父节点名称">
                {{ selectedNode.parentName || '无' }}
              </el-descriptions-item>
              <el-descriptions-item label="分类代码">
                {{ selectedNode.sortCode }}
              </el-descriptions-item>
              <el-descriptions-item label="分类名称">
                {{ selectedNode.sortName }}
              </el-descriptions-item>
              <el-descriptions-item label="状态">
                <el-tag :type="selectedNode.status === 'active' ? 'success' : 'info'">
                  {{ selectedNode.status === 'active' ? '启用' : '禁用' }}
                </el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="备注" :span="2">
                {{ selectedNode.remark || '无' }}
              </el-descriptions-item>
            </el-descriptions>
          </div>

          <div v-else class="no-selection">
            <el-empty description="请选择左侧分类查看详细信息" />
          </div>
        </el-card>

        <!-- 下半部分：属性表格 -->
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>分类属性列表</span>
            </div>
          </template>

          <el-row :gutter="10" class="mb-2">
            <el-col :span="1.5">
              <el-button type="primary" plain icon="Plus" @click="handleAddAttribute">新增属性</el-button>
            </el-col>
          </el-row>

          <el-table :data="attributeList" v-loading="attrLoading" border>
            <el-table-column prop="attributeCode" label="属性代码" align="center" />
            <el-table-column prop="attributeName" label="属性名称" align="center" />
            <el-table-column prop="ifRequired" label="是否必填" align="center">
              <template #default="{ row }">
                <el-tag :type="row.ifRequired == 'true' ? 'success' : 'danger'">
                  {{ row.ifRequired ? '是' : '否' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="valueType" label="值类型" align="center">
                <template #default="{ row }">
                    {{ valueTypeEnum[row.valueType] }}
                </template>
            </el-table-column>
            <el-table-column prop="applicableMark" label="适用标记" align="center" >
                <template #default="{ row }">
                    {{ applicableMarkEnum[row.applicableMark] }}
                </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" align="center">
              <template #default="{ row }">
                <el-tag :type="row.status === 'active' ? 'success' : 'info'">
                  {{ row.status === 'active' ? '启用' : '禁用' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" align="center" width="150">
              <template #default="{ row }">
                <el-button link type="primary" icon="Edit" @click="handleEditAttribute(row)">修改</el-button>
                <el-button 
                  v-if="row.status === 'active'" 
                  link 
                  type="warning" 
                  @click="handleDisableAttribute(row)"
                >
                  禁用
                </el-button>
                <el-button 
                  v-else-if="row.status === 'failure'" 
                  link 
                  type="success" 
                  @click="handleEnableAttribute(row)"
                >
                  启用
                </el-button>
                <el-button link type="danger" icon="Delete" @click="handleDeleteAttribute(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <!-- 新增分类弹窗 -->
    <el-dialog v-model="addDialogVisible" title="新增分类" width="600px" :close-on-click-modal="false">
      <el-form ref="addFormRef" :model="addFormData" :rules="addFormRules" label-width="100px">
        <el-form-item label="父节点" prop="parentId">
          <el-tree-select
            v-model="addFormData.parentId"
            :data="parentTreeData"
            placeholder="请选择父节点，不选则添加到根目录"
            node-key="id"
            :props="{ label: 'sortName', value: 'id' }"
            :expand-on-click-node="false"
            default-expand-all
            filterable
            :check-strictly="true"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="分类代码" prop="sortCode">
          <el-input v-model="addFormData.sortCode" placeholder="请输入分类代码" />
        </el-form-item>
        <el-form-item label="分类名称" prop="sortName">
          <el-input v-model="addFormData.sortName" placeholder="请输入分类名称" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="addFormData.remark" type="textarea" placeholder="请输入备注信息" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="addDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitAddForm">保存</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 新增属性弹窗 -->
    <el-dialog v-model="attributeDialogVisible" title="新增属性" width="600px" :close-on-click-modal="false">
      <el-form ref="attributeFormRef" :model="attributeFormData" :rules="attributeFormRules" label-width="120px">
        <el-form-item label="属性代码" prop="attributeCode">
          <el-input v-model="attributeFormData.attributeCode" placeholder="请输入属性代码" />
        </el-form-item>
        <el-form-item label="属性名称" prop="attributeName">
          <el-input v-model="attributeFormData.attributeName" placeholder="请输入属性名称" />
        </el-form-item>
        <el-form-item label="是否必填" prop="ifRequired">
          <el-switch v-model="attributeFormData.ifRequired" active-text="是" inactive-text="否" />
        </el-form-item>
        <el-form-item label="值类型" prop="valueType">
          <el-select v-model="attributeFormData.valueType" placeholder="请选择值类型" style="width: 100%">
            <el-option label="文本框" value="input" />
            <el-option label="文本域" value="textarea" />
            <el-option label="日期" value="date" />
          </el-select>
        </el-form-item>
        <el-form-item label="适用标记" prop="applicableMark">
          <el-select v-model="attributeFormData.applicableMark" placeholder="请选择适用标记" style="width: 100%">
            <el-option label="打印申请" value="printApply" />
            <el-option label="文件流程" value="documentFlow" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="attributeDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitAttributeForm">保存</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts" name="FileType">
import { ref, watch, onMounted } from 'vue';
import { ElMessageBox, ElMessage, ElTreeSelect } from 'element-plus';
import {
  documentList,
  addDocument,
  editTemplate,
  deleteTemplate,
  enableTemplate,
  disableTemplate,
  getAttributeList,
  addAttribute,
  enableAttribute,
  disableAttribute,
  editAttribute,
  deleteAttribute
} from '@/api/dms/fileSystemOption/fileType/index';

const valueTypeEnum = {
  input: '文本框',
  textarea: '文本域',
  date: '日期'
};
const applicableMarkEnum = {
  printApply: '打印申请',
  documentFlow: '文件流程'
};
// 定义类型
interface TreeNode {
  id: number;
  parentName?: string;
  sortCode: string;
  sortName: string;
  remark?: string;
  status: 'active' | 'failure';
  parentId?: string | number;
  children?: TreeNode[];
}

interface AttributeItem {
  id: number;
  attributeCode: string;
  attributeName: string;
  ifRequired: boolean;
  valueType: string;
  applicableMark: string;
  status: 'active' | 'failure';
}

// 定义表单数据类型，包含可选的id字段用于编辑
interface AddFormData {
  id?: number;
  parentId: number | null;
  sortCode: string;
  sortName: string;
  remark: string;
}

// 定义属性表单数据类型
interface AttributeFormData {
  id?: number;
  attributeCode: string;
  attributeName: string;
  ifRequired: boolean;
  valueType: string;
  applicableMark: string;
}

// 响应式数据
const filterText = ref('');
const selectedNode = ref<TreeNode | null>(null);
const treeRef = ref();
const treeData = ref<TreeNode[]>([]);
const attributeList = ref<AttributeItem[]>([]);
const attrLoading = ref(false);

// 新增弹窗相关数据
const addDialogVisible = ref(false);
const addFormRef = ref();
const addFormData = ref<AddFormData>({
  parentId: null as number | null,
  sortCode: '',
  sortName: '',
  remark: ''
});
const parentTreeData = ref<TreeNode[]>([]);

// 属性弹窗相关数据
const attributeDialogVisible = ref(false);
const attributeFormRef = ref();
const attributeFormData = ref<AttributeFormData>({
  attributeCode: '',
  attributeName: '',
  ifRequired: false,
  valueType: '',
  applicableMark: ''
});
// 添加标识区分编辑和新增模式
const isEditAttribute = ref(false);
const editingAttributeId = ref<number | null>(null);

// 属性表单验证规则
const attributeFormRules = {
  attributeCode: [{ required: true, message: '请输入属性代码', trigger: 'blur' }],
  attributeName: [{ required: true, message: '请输入属性名称', trigger: 'blur' }],
  valueType: [{ required: true, message: '请选择值类型', trigger: 'change' }]
};

// 新增表单验证规则
const addFormRules = {
  sortCode: [{ required: true, message: '请输入分类代码', trigger: 'blur' }],
  sortName: [{ required: true, message: '请输入分类名称', trigger: 'blur' }]
};

// 监听筛选文本变化
watch(filterText, (val) => {
  treeRef.value?.filter(val);
});

// 过滤树节点方法
const filterNode = (value: string, data: TreeNode) => {
  if (!value) return true;
  return data.sortName.indexOf(value) !== -1;
};

// 节点点击事件
const handleNodeClick = (data: TreeNode) => {
  selectedNode.value = data;
  // 模拟加载属性列表
  loadAttributes(data.id);
};

// 模拟加载属性列表
const loadAttributes = (nodeId: number) => {
  attrLoading.value = true;
  // 这里应该调用API获取属性列表
  getAttributeList({ sortId: nodeId })
    .then((res) => {
      attributeList.value = res.rows || [];
    })
    .finally(() => {
      attrLoading.value = false;
    });
};

// 新增分类
const handleAdd = () => {
  // 初始化父节点树形数据，使用与左侧树形结构一致的数据
  parentTreeData.value = JSON.parse(JSON.stringify(treeData.value));
  addDialogVisible.value = true;

  // 重置表单
  addFormRef.value?.resetFields();
  addFormData.value = {
    parentId: null,
    sortCode: '',
    sortName: '',
    remark: ''
  };
};

// 编辑分类
const handleEdit = () => {
  if (!selectedNode.value) return;

  // 获取除当前节点及其子节点外的其他节点数据
  const filteredTreeData = filterTreeDataExcludingNode(treeData.value, selectedNode.value.id);
  parentTreeData.value = filteredTreeData;

  addDialogVisible.value = true;

  // 重置表单
  addFormRef.value?.resetFields();

  // 设置表单数据为当前选中节点的数据，包含id用于编辑
  addFormData.value = {
    id: selectedNode.value.id,
    parentId: selectedNode.value.parentId !== 'root' ? (selectedNode.value.parentId as number) : null,
    sortCode: selectedNode.value.sortCode,
    sortName: selectedNode.value.sortName,
    remark: selectedNode.value.remark || ''
  };
};

// 过滤掉指定节点及其所有子节点的树形数据
const filterTreeDataExcludingNode = (tree: TreeNode[], excludeId: number): TreeNode[] => {
  const result: TreeNode[] = [];

  for (const node of tree) {
    // 如果当前节点不是要排除的节点，则处理它
    if (node.id !== excludeId) {
      // 创建新节点对象，复制所有属性
      const newNode: TreeNode = { ...node };

      // 如果有子节点，则递归过滤子节点
      if (node.children && node.children.length > 0) {
        newNode.children = filterTreeDataExcludingNode(node.children, excludeId);
      }

      result.push(newNode);
    }
  }

  return result;
};

// 提交新增表单
const submitAddForm = () => {
  addFormRef.value?.validate((valid: boolean) => {
    if (valid) {
      // 输出表单数据到控制台
      console.log('新增/编辑分类表单数据:', {
        ...addFormData.value,
        parentId: addFormData.value.parentId || 'root' // 如果没有选择父节点，parentId为root
      });
      if (addFormData.value.id) {
        editTemplate({
          ...addFormData.value,
          parentId: addFormData.value.parentId || 'root' // 如果没有选择父节点，parentId为root
        }).then((res: any) => {
          console.log('addTemplateEdit');
          console.log(res);
          if (res.code === 200) {
            ElMessage.success('操作成功');
            addDialogVisible.value = false;
            selectedNode.value = null;
            init();
          } else {
            ElMessage.error(res.message);
          }
        });
      } else {
        addDocument({
          ...addFormData.value,
          parentId: addFormData.value.parentId || 'root' // 如果没有选择父节点，parentId为root
        }).then((res: any) => {
          console.log('addDocumentaddDocument');
          console.log(res);
          if (res.code === 200) {
            ElMessage.success('操作成功');
            addDialogVisible.value = false;
            init();
          } else {
            ElMessage.error(res.message);
          }
        });
      }

      // 这里可以调用API保存数据
      // 例如：addFileType(addFormData.value)

      // ElMessage.success('操作成功');
      // addDialogVisible.value = false;
    } else {
      console.log('表单验证失败');
    }
  });
};

const handleDelete = async () => {
  if (!selectedNode.value) return;
  try {
    await ElMessageBox.confirm(`确认删除分类 "${selectedNode.value.sortName}" 吗？`, '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    });

    // 调用删除接口，传入包含节点id的数组
    await deleteTemplate(selectedNode.value.id);
    ElMessage.success('删除成功');

    selectedNode.value = null;
    // 重新加载数据
    init();
  } catch {
    // 取消删除
  }
};

const handleToggleStatus = async () => {
  if (!selectedNode.value) return;
  const currentStatus = selectedNode.value.status;
  const action = currentStatus === 'active' ? '禁用' : '启用';
  const targetStatus = currentStatus === 'active' ? 'failure' : 'active';

  try {
    await ElMessageBox.confirm(`确认要${action} "${selectedNode.value.sortName}" 吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    });

    // 根据当前状态调用相应的接口
    if (currentStatus === 'active') {
      // 当前是启用状态，需要禁用，调用disableTemplate接口
      await disableTemplate(selectedNode.value.id);
    } else {
      // 当前是禁用状态，需要启用，调用enableTemplate接口
      await enableTemplate(selectedNode.value.id);
    }

    selectedNode.value = null;
    ElMessage.success(`${action}成功`);
    init();
  } catch {
    // 取消操作
  }
};

const handleAddAttribute = () => {
  // 重置表单
  attributeFormRef.value?.resetFields();
  attributeFormData.value = {
    attributeCode: '',
    attributeName: '',
    ifRequired: false,
    valueType: '',
    applicableMark: ''
  };

  // 设置为新增模式
  isEditAttribute.value = false;
  editingAttributeId.value = null;

  attributeDialogVisible.value = true;
};

const handleEditAttribute = (row: AttributeItem) => {
  console.log('编辑属性', row);
  
  // 重置表单
  attributeFormRef.value?.resetFields();
  
  // 设置表单数据为当前行的数据
  attributeFormData.value = {
    attributeCode: row.attributeCode,
    attributeName: row.attributeName,
    ifRequired: row.ifRequired,
    valueType: row.valueType,
    applicableMark: row.applicableMark
  };
  
  // 设置为编辑模式，并保存要编辑的ID
  isEditAttribute.value = true;
  editingAttributeId.value = row.id;
  
  // 显示编辑对话框
  attributeDialogVisible.value = true;
};

const submitAttributeForm = () => {
  attributeFormRef.value?.validate((valid: boolean) => {
    if (valid) {
      console.log(isEditAttribute.value ? '编辑属性表单数据:' : '新增属性表单数据:', attributeFormData.value);

      if (isEditAttribute.value && editingAttributeId.value) {
        // 编辑模式：调用editAttribute接口
        const editData = {
          id: editingAttributeId.value,
          ...attributeFormData.value
        };

        editAttribute(editData).then((res: any) => {
          console.log('editAttribute');
          console.log(res);
          if (res.code === 200) {
            ElMessage.success('属性更新成功');
            attributeDialogVisible.value = false;
            // 重新加载属性列表
            if (selectedNode.value) {
              loadAttributes(selectedNode.value.id);
            }
          } else {
            ElMessage.error(res.message);
          }
        }).catch(error => {
          console.error(error);
          ElMessage.error('属性更新失败');
        });
      } else {
        // 新增模式：调用addAttribute接口，需要包含sortId
        const addData = {
          ...attributeFormData.value,
          status: 'active' as const,
          sortId: selectedNode.value?.id
        };

        addAttribute(addData).then((res: any) => {
          console.log('addAttribute');
          console.log(res);
          if (res.code === 200) {
            ElMessage.success('属性添加成功');
            attributeDialogVisible.value = false;
            // 重新加载属性列表
            if (selectedNode.value) {
              loadAttributes(selectedNode.value.id);
            }
          } else {
            ElMessage.error(res.message);
          }
        }).catch(error => {
          console.error(error);
          ElMessage.error('属性添加失败');
        });
      }
    } else {
      console.log('表单验证失败');
    }
  });
};

const handleDeleteAttribute = async (row: AttributeItem) => {
  try {
    await ElMessageBox.confirm(`确认删除属性 "${row.attributeName}" 吗？`, '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    });
    // 从列表中移除该属性
    deleteAttribute(row.id).then(() => {
      ElMessage.success('删除成功');
      loadAttributes(selectedNode.value.id);
    });
  } catch {
    // 取消删除
  }
};

const handleDisableAttribute = async (row: AttributeItem) => {
  try {
    await ElMessageBox.confirm(`确认禁用属性 "${row.attributeName}" 吗？`, '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    });

    await disableAttribute(row.id);
    ElMessage.success('禁用成功');
    loadAttributes(selectedNode.value.id)
  } catch {
    // 取消禁用
  }
};

const handleEnableAttribute = async (row: AttributeItem) => {
  try {
    await ElMessageBox.confirm(`确认启用属性 "${row.attributeName}" 吗？`, '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    });

    await enableAttribute(row.id);
    ElMessage.success('启用成功');
    loadAttributes(selectedNode.value.id)
  } catch {
    // 取消启用
  }
};

// 初始化数据
const init = () => {
  documentList({
    pageNum: 1,
    pageSize: 9999
  }).then((res) => {
    console.log('documentListdocumentListdocumentList');
    console.log(res);
    const tempData = res.rows || [];

    // 将一维数组转换为树形结构的函数
    const convertToTree = (list: TreeNode[], parentId: string | number = 'root') => {
      const tree = [];
      for (const item of list) {
        if (item.parentId === parentId) {
          const newNode = { ...item };
          const children = convertToTree(list, item.id);
          if (children.length > 0) {
            newNode.children = children;
          }
          tree.push(newNode);
        }
      }
      return tree;
    };

    // 使用转换函数将一维数组转换为树形结构
    treeData.value = convertToTree(tempData);
  });
};

onMounted(() => {
  init();
});
</script>

<style scoped>
.app-container {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.tree-container {
  height: calc(100vh - 250px);
  overflow: auto;
  margin-top: 15px;
}

.node-details {
  margin-top: 20px;
}

.no-selection {
  text-align: center;
}

.mb-2 {
  margin-bottom: 15px;
}

.mt-2 {
  margin-top: 8px;
}

.tree-node {
  display: flex;
  align-items: center;
}

.tree-node.disabled-node {
  color: #a8abb2;
}

.node-status-icon {
  margin-right: 4px;
  color: #a8abb2;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
}
</style>
