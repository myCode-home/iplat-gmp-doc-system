<template>
  <div class="task-config-container">
    <el-button type="primary" @click="addConfigRow" style="margin-bottom: 10px;">新增</el-button>
    <el-table :data="localNodeConfigs" style="width: 100%">
      <el-table-column type="index" label="序号" width="50" />
      <el-table-column prop="taskStatus" label="任务状态">
        <template #default="scope">
          <el-select v-model="scope.row.taskStatus" placeholder="请选择任务状态" @change="onTaskStatusChange(scope.row)">
            <el-option v-for="dict in system_task_status" :key="dict.value" :label="dict.label+'('+dict.value+')'" :value="dict.value"></el-option>
          </el-select>
        </template>
      </el-table-column>
      <el-table-column prop="remark" label="备注">
        <template #default="scope">
          <el-input v-model="scope.row.remark"></el-input>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="130">
        <template #default="scope">
          <el-button type="primary" size="small" @click="editConfig(scope.$index, scope.row)">编辑</el-button>
          <el-button type="danger" size="small" @click="deleteConfigModule(scope.$index)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <div class="module-form-container" v-show="selectConfigRow && selectConfigRow.actions">
      <div class="actions-container" style="margin-top: 30px">
        <el-button type="primary" @click="addAction" style="margin-bottom: 10px;">新增</el-button>
        <div v-for="(action, index) in selectConfigRow.actions" :key="index" class="action-form-item">
          <el-divider content-position="left">动作 {{ index + 1 }}</el-divider>
          <el-form :model="action" label-width="120px" style="margin-top: 10px;">
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="执行类型：">
                  <el-select v-model="action.actionType" placeholder="请选择执行类型">
                    <el-option label="sql" value="sql"></el-option>
                    <el-option label="url接口" value="url"></el-option>
                    <el-option label="实体" value="entity"></el-option>
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="实体类：">
                  <el-input v-model="action.entityClass" placeholder="请输入实体类"></el-input>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="表名：">
                  <el-input v-model="action.tableName" placeholder="请输入表名"></el-input>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="主键字段：">
                  <el-input v-model="action.pkField" placeholder="请输入主键字段"></el-input>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="主键表达式：">
                  <el-input v-model="action.pkExpr" placeholder="请输入主键表达式"></el-input>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="执行表达式：">
                  <el-input v-model="action.actionExpr" placeholder='格式为: {"xx": "xxx","xx": "xxx"}'></el-input>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="执行顺序：">
                  <el-input-number v-model="action.actionSort" :min="0" placeholder="请输入执行顺序"></el-input-number>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="是否开启：">
                  <el-radio-group v-model="action.enabled">
                    <el-radio label="0">否</el-radio>
                    <el-radio label="1">是</el-radio>
                  </el-radio-group>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="数据库地址：">
                  <el-input v-model="action.dbUrl" placeholder="请输入数据库地址"></el-input>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="数据库用户名：">
                  <el-input v-model="action.dbUsername" placeholder="请输入数据库用户名"></el-input>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="数据库密码：">
                  <el-input v-model="action.dbPassword" placeholder="请输入数据库密码"></el-input>
                </el-form-item>
              </el-col>
              <el-col :span="24">
                <el-form-item label="备注：">
                  <el-input v-model="action.remark" type="textarea" placeholder="请输入备注"></el-input>
                </el-form-item>
              </el-col>
            </el-row>
          </el-form>
          <div style="text-align: right;">
            <el-button type="danger" @click="deleteAction(index)">删除</el-button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup name="TaskConfig">
import { ref, watch, getCurrentInstance } from 'vue';
import { toRefs } from 'vue';

const { proxy } = getCurrentInstance();
const { system_task_status } = toRefs(proxy?.useDict('system_task_status'));

const props = defineProps({
  nodeConfigs: {
    type: Array,
    default: () => []
  }
});

// 定义 emits 事件
const emit = defineEmits(['update:nodeConfigs']);

// 使用本地响应式变量，避免直接修改 props
const localNodeConfigs = ref([]);

// 监听本地数据变化，同步给父组件
watch(
  () => localNodeConfigs.value,
  (newVal) => {
    emit('update:nodeConfigs', newVal);
  },
  { deep: true }
);

const selectConfigRow = ref({});

// 增加配置行
const addConfigRow = () => {
  localNodeConfigs.value.push({
    configType: 'finish',
    taskStatus: '',
    remark: '',
    actions: []
  });
};

// 删除配置
const deleteConfigModule = (index) => {
  localNodeConfigs.value.splice(index, 1);
};

// 编辑配置
const editConfig = (index, row) => {
  selectConfigRow.value = row
  if (!selectConfigRow.value.actions) {
    selectConfigRow.value.actions = [];
  }
};

// 新增动作
const addAction = () => {
  if (!selectConfigRow.value.actions) {
    selectConfigRow.value.actions = [];
  }
  selectConfigRow.value.actions.push({
    actionType: '',
    entityClass: '',
    tableName: '',
    pkField: '',
    pkExpr: '',
    actionExpr: '',
    actionSort: 0,
    enabled: '0',
    dbUrl: '',
    dbUsername: '',
    dbPassword: '',
    remark: ''
  });
};

// 删除动作
const deleteAction = (index) => {
  selectConfigRow.value.actions.splice(index, 1);
};

// 任务状态改变事件
const onTaskStatusChange = (row) => {
};

onMounted(() => {
  localNodeConfigs.value = JSON.parse(JSON.stringify(props.nodeConfigs));
})

</script>

<style scoped>
.task-config-container {
  border-top: 0;
}

.module-form-container {
  margin-top: 15px;
}

.action-form-item {
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  padding: 15px;
  margin-bottom: 15px;
  background-color: #fafafa;
}
</style>