<template>
  <div class="p-2">
    <div id="print_iframe">
      <iframe id="print_area" name="print_iframe" @load="loadFrame" style="display:none"></iframe>
    </div>
    <!-- 这是搜索框 -->
    <transition :enter-active-class="proxy?.animate.searchAnimate.enter"
      :leave-active-class="proxy?.animate.searchAnimate.leave">
      <div v-show="showSearch" class="mb-[10px]">
        <el-card shadow="hover">
          <super-search :showResetButton="false" :showSearch="true" resetButtonText="刷新" code="printApply-form-search"
            v-model="queryParams" @getList="getList" @resetQuery="resetQuery">
          </super-search>
        </el-card>
      </div>
    </transition>
    <!-- 打印申请审批列表 -->
    <el-card shadow="hover">
      <super-table v-if="showTable" code="printApply-page-list" rowKey="id" highlightCurrentRow :dataLoading="loading"
        :data="templateEditList" v-model:queryParams="queryParams" :total="total" @getList="getList">
        <!-- 操作列 -->
        <template #operation="scope">
          <div>
            <el-tooltip content="详情" placement="top" :enterable="false">
              <el-button link type="primary" v-hasPermi="['dms:printApply:query']" icon="View"
                @click="handleDetail(scope.row)"></el-button>
            </el-tooltip>
            <el-tooltip content="终止" placement="top" :enterable="false">
              <el-button link type="primary" v-hasPermi="['dms:printApply:expiry']" icon="CircleClose"
                :disabled="scope.row.status === 'stop'" @click="handleStop(scope.row)"></el-button>
            </el-tooltip>
            <el-tooltip content="打印" placement="top" :enterable="false">
              <el-button link type="primary" v-hasPermi="['dms:printApply:print']" icon="Printer"
                :disabled="scope.row.status === 'stop' || printingRowIds.has(scope.row.id)"
                @click="handlePrint(scope.row)"></el-button>
            </el-tooltip>
          </div>
        </template>
      </super-table>
    </el-card>
    <!-- 详情对话框（详情展示） -->
    <el-dialog v-model="dialog.visible" :title="dialog.title" width="70%" append-to-body>
      <super-form v-if="dialog.visible" :loading="dialog.loading" ref="superFormRef" :code="formCode"
        @selectData="selectData" v-model="form" :form-rules="rules" :methods="formMethods">
      </super-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button :loading="buttonLoading" v-if="showSubmitButton" type="primary" @click="submitForm">提 交</el-button>
          <el-button @click="cancel">关 闭</el-button>
        </div>
      </template>
    </el-dialog>
    <!-- 打印结果确认弹框 -->
    <el-dialog :show-close="false" v-model="printResultDialog.visible" :title="printResultDialog.title" width="500px"
      append-to-body :close-on-click-modal="false">
      <div class="p-2">
        <p>请确认本次打印结果（无论实际打印是否成功，均请如实选择）</p>
        <el-form-item label="备注/失败原因" label-width="120px">
          <el-input v-model="printResultForm.failReason" type="textarea" rows="3"
            placeholder="失败请填写具体原因，成功/取消可填写可选备注"></el-input>
        </el-form-item>
      </div>

      <template #footer>
        <div class="dialog-footer">
          <el-button :loading="printResultDialog.loading" @click="handlePrintCancel">取消打印</el-button>
          <el-button :loading="printResultDialog.loading" type="danger" @click="handlePrintFail">打印失败</el-button>
          <el-button :loading="printResultDialog.loading" type="primary" @click="handlePrintSuccess">打印成功</el-button>
        </div>
      </template>
    </el-dialog>
    <el-dialog v-model="printFailDetailDialog.visible" :title="printFailDetailDialog.title" width="80%" append-to-body
      :close-on-click-modal="false" :loading="printFailDetailDialog.loading">
      <div class="p-2">
        <super-table code="printApply-page-fail-list" rowKey="id" highlightCurrentRow :dataLoading="loading"
          :data="failDetailList" v-model:queryParams="queryParamsDetail" :total="totalFail" @getList="getDeatilList"
          @selection-change="handleDetailSelectionChange">
        </super-table>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="printFailDetailDialog.visible = false">取消</el-button>
          <el-button type="danger" @click="confirmPrintFail">确认打印失败</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="PrintApplyEdit" lang="ts">
import { getPrintApplyDocumentTrackingDetail, printApplyAbend, getPrintApplyDetail, updatePrintApplyEdit, printSubmit, addPrintApplyEdit, delPrintApplyEdit, previewPdf, queryConfirmBefore, printSuccess, printFail, printCancel } from '@/api/dms/printDistribution/printApply/index';
import { useUserStore } from '@/store/modules/user';

const { proxy } = getCurrentInstance() as ComponentInternalInstance;
const buttonLoading = ref(false);
const loading = ref(false);
const showSearch = ref(true);
const ids = ref<Array<string | number>>([]);
const showTable = ref(true);
const total = ref(0);
// 打印申请编辑表单
const superFormRef = ref(null);
// 打印申请编辑表单code
const formCode = ref('printApply-page-detail-ist');
// 是否显示提交按钮
const showSubmitButton = ref(false);
// 是否显示预览PDF的按钮
const showPreviewButton = ref(false);

// 打印失败详情表格相关变量
const showDeatilTable = ref(false);
const failDetailList = ref([]);
const totalFail = ref(0);
const selectedDetailIds = ref<Array<string>>([]);

// 打印失败详情表格查询参数
const queryParamsDetail = reactive({
  pageNum: 1,
  pageSize: 10,
  printApplyId: ''
});

// 打印失败详情弹框响应式对象
const printFailDetailDialog = reactive({
  visible: false,
  loading: false,
  title: '打印失败详情'
});

const dialog = reactive<DialogOption>({
  title: '',
  visible: false,
  loading: false
});

// 打印结果确认弹框相关数据
const printResultDialog = reactive({
  visible: false,
  loading: false,
  title: '打印结果确认'
});

// 打印结果表单数据
const printResultForm = reactive({
  printApplyId: '',
  failReason: '',
  remark: '',
  detailIds: [] as Array<string>
});

// 存储当前打印的行数据（用于获取主键等信息）
const currentPrintRow = ref<any>(null);

// 存储当前正在打印的行ID，用于控制按钮禁用状态
const printingRowIds = ref<Set<string | number>>(new Set());

// 重置
const initFormData = {
};
const data = reactive<PageData<any, any>>({
  form: { ...initFormData },
  queryParams: reactive({
    pageNum: 1,
    pageSize: 10
  }),
  rules: {
    // id: [{ required: true, message: '主键不能为空', trigger: 'blur' }],
  }
});

const { queryParams, form, rules } = toRefs(data);

const templateEditList = ref([]);

/** 查询打印申请审核跟踪列表 */
const getList = async () => {
  loading.value = true;
  try {
    const res = await printSubmit(queryParams.value);
    templateEditList.value = res.rows;
    total.value = res.total;
  } finally {
    loading.value = false;
  }
};

// 处理dataChoose组件返回值,更新form表单内容
const selectData = (data: any, config) => {
  if (config.prop == 'docCode') {
    form.value.docCode = data.dlCode;
    form.value.docVersion = data.dlVersion;
    form.value.docName = data.dlName;
  }
};

// 在父组件中预定义所有可能用到的方法
const formMethods = {
  customChangeHandler: (args: Array<any>, childFrom: Object) => {
    superFormRef.value.updateData({ companyName: 'admin' })
    console.log('自定义change事件触发')
  },
  handleRoleChange: (args: Array<any>, childFrom: Object) => {
    console.log('自定义事件触发', args, childFrom);
  },
  handleSexChange: (args: Array<any>, childFrom: Object) => {
    console.log('自定义事件触发', args);
  },
  processCompanyData: (args: Array<any>, childFrom: Object) => {
    console.log('自定义数据处理', args, childFrom);
  }
};

/** 查看详情按钮操作 */
const handleDetail = async (row?: any) => {
  reset();
  showSubmitButton.value = false;
  showPreviewButton.value = false;
  formCode.value = 'printApply-page-check-detail';
  dialog.visible = true;
  dialog.loading = true;
  dialog.title = '查看详情';
  const _id = row?.id || ids.value[0];
  const res = await getPrintApplyDetail(_id);
  Object.assign(form.value, res.data);
  dialog.loading = false;
};

/** 提交按钮 */
const submitForm = () => {
  superFormRef.value?.validate(async (valid: boolean) => {
    if (valid) {
      const data = superFormRef.value.achieveData();
      delete data.approveComments;
      buttonLoading.value = true;
      if (data.id) {
        await updatePrintApplyEdit(data).finally(() => (buttonLoading.value = false));
      } else {
        await addPrintApplyEdit(data).finally(() => (buttonLoading.value = false));
      }
      proxy?.$modal.msgSuccess('操作成功');
      dialog.visible = false;
      await getList();
    }
  });
};

// 用于存储Blob URL，避免被提前回收
const pdfBlobUrl = ref('');
// 打印按钮操作
const handlePrint = async (row?: any) => {
  const id = row?.id || ids.value[0];
  if (!id) {
    proxy?.$modal.msgError('请选择有效的打印单据');
    return;
  }
  // 将当前行ID添加到打印中集合，禁用打印按钮
  printingRowIds.value.add(id);

  // 存储当前打印行数据，用于后续API传参
  currentPrintRow.value = row;
  printResultForm.printApplyId = id;
  // 重置表单数据
  printResultForm.failReason = '';
  printResultForm.remark = '';
  printResultForm.detailIds = [];

  try {
    // 校验打印权限
    const confirmRes = await queryConfirmBefore(row.id);
    if (!confirmRes.data) {
      proxy?.$modal.msgError('无打印权限');
      return;
    }

    // 获取PDF文档流（后端返回blob格式）
    const userStore = useUserStore();
    const pdfBlob = await previewPdf({
      id: id,
      userId: userStore.userId
    });

    // 校验PDF blob有效性
    if (!pdfBlob || !(pdfBlob instanceof Blob)) {
      proxy?.$modal.msgError('获取PDF失败，返回数据格式异常');
      return;
    }
    const pdfValidBlob = new Blob([pdfBlob], { type: 'application/pdf' });

    // 调用打印方法
    await printPdfFromBlob(pdfValidBlob);

  } catch (error) {
    // console.error('打印操作失败:', error);
    proxy?.$modal.msgError('打印操作失败，请重试');
    // 打印流程异常时，也弹出结果弹框让用户确认
    printResultDialog.visible = true;
  } finally {
    // 如果打印流程异常且没有打开结果弹框，则手动解除按钮禁用
    if (!printResultDialog.visible && currentPrintRow.value) {
      printingRowIds.value.delete(currentPrintRow.value.id);
    }
  }
};

// 打印成功 - 点击事件
const handlePrintSuccess = async () => {
  if (!printResultForm.printApplyId) {
    proxy?.$modal.msgError('打印申请主键不能为空');
    return;
  }

  const reqData = {
    printApplyIds: [printResultForm.printApplyId], // 数组格式，传递当前打印申请主键
    remark: printResultForm.failReason || '用户确认打印成功' // 备注可选，若未填写则默认值
  };

  try {
    printResultDialog.loading = true;
    // 调用打印成功API
    await printSuccess(reqData);
    proxy?.$modal.msgSuccess('已确认打印成功');
    // 关闭弹框并重置表单
    printResultDialog.visible = false;
    resetPrintResultForm();
  } catch (error) {
    proxy?.$modal.msgError('打印成功确认失败，请重试');
  } finally {
    printResultDialog.loading = false;
  }
};

// 修改打印失败按钮逻辑
const handlePrintFail = async () => {
  // 校验必填项
  if (!printResultForm.printApplyId) {
    proxy?.$modal.msgError('打印申请主键不能为空');
    return;
  }
  if (!printResultForm.failReason.trim()) {
    proxy?.$modal.msgError('请填写打印失败原因');
    return;
  }

  // 打开打印失败详情弹框
  printFailDetailDialog.visible = true;
  // 新增：设置查询参数中的打印申请ID
  queryParamsDetail.printApplyId = printResultForm.printApplyId;
  // console.log('打印失败详情查询参数:', queryParamsDetail);
  // 调用获取详情列表方法
  await getDeatilList();

  // 关闭原打印结果确认弹框
  printResultDialog.visible = false;
};

// 获取打印失败详情列表
const getDeatilList = async () => {
  loading.value = true;
  try {
    // 调用获取打印文档信息列表API
    const res = await getPrintApplyDocumentTrackingDetail(queryParamsDetail);
    failDetailList.value = res.rows;
    totalFail.value = res.total;
  } catch (error) {
    // console.error('获取打印失败详情列表失败:', error);
    proxy?.$modal.msgError('获取打印失败详情列表失败，请重试');
  } finally {
    loading.value = false;
  }
};

// 处理打印失败详情表格选择变化
const handleDetailSelectionChange = (selection: any[]) => {
  // 携带的是id组成的数组和printApplyId，以及failReason（失败原因）
  selectedDetailIds.value = selection.map((item) => item.id);
  // console.log('选中的打印申请ID:', selectedDetailIds.value);
};

// 修改确认打印失败逻辑
const confirmPrintFail = async () => {
  if (selectedDetailIds.value.length === 0) {
    proxy?.$modal.msgError('请选择要确认打印失败的记录');
    return;
  }

  // 构造printFail API所需参数
  const reqData = {
    ids: selectedDetailIds.value, // 数组格式，传递选中的打印申请主键集合
    printApplyId: printResultForm.printApplyId, // 数组格式，传递选中的打印申请主键集合
    failReason: printResultForm.failReason.trim() // 失败原因（必填）
  };

  // console.log('打印失败详情查询参数reqdata！！！！！！！！！:', reqData);
  try {
    printFailDetailDialog.loading = true; // 弹框加载状态
    // 调用打印失败API
    await printFail(reqData);
    proxy?.$modal.msgSuccess('已确认打印失败');
    // 关闭详情弹框
    printFailDetailDialog.visible = false;
    // 重置表单和选中的ID
    resetPrintResultForm();
    selectedDetailIds.value = [];
  } catch (error) {
    // console.error('打印失败确认失败:', error);
    // proxy?.$modal.msgError('打印失败确认失败，请重试');
  } finally {
    printFailDetailDialog.loading = false;
  }
};

const handlePrintCancel = async () => {
  if (!printResultForm.printApplyId) {
    proxy?.$modal.msgError('打印申请主键不能为空');
    return;
  }

  // 构造printCancel API所需参数
  const reqData = {
    printApplyIds: [printResultForm.printApplyId],
    remark: printResultForm.failReason || '用户取消打印确认'
  };

  try {
    printResultDialog.loading = true;
    // 调用打印取消API
    await printCancel(reqData);
    proxy?.$modal.msgSuccess('已确认打印取消');
    // 关闭弹框并重置表单
    printResultDialog.visible = false;
    resetPrintResultForm();
  } catch (error) {
    proxy?.$modal.msgError('打印取消确认失败，请重试');
  } finally {
    printResultDialog.loading = false;
  }
};

// 重置打印结果表单数据
const resetPrintResultForm = () => {
  // 移除当前打印ID，解除打印按钮禁用状态
  if (printResultForm.printApplyId) {
    printingRowIds.value.delete(printResultForm.printApplyId);
  }
  printResultForm.printApplyId = '';
  printResultForm.failReason = '';
  printResultForm.remark = '';
  printResultForm.detailIds = [];
  currentPrintRow.value = null;
};

const printPdfFromBlob = async (pdfBlob: Blob) => {
  // 1. 清理旧的Blob URL，避免内存泄漏
  if (pdfBlobUrl.value) {
    window.URL.revokeObjectURL(pdfBlobUrl.value);
  }

  // 2. 校验Blob有效性（额外兜底，避免无效数据）
  if (!pdfBlob || pdfBlob.size <= 0) {
    proxy?.$modal.msgError('PDF数据无效，无法执行打印');
    return;
  }

  // 3. 创建Blob URL（临时本地URL，浏览器可访问）
  pdfBlobUrl.value = window.URL.createObjectURL(pdfBlob);

  // 4. 直接调用隐藏iframe打印（不打开新窗口，无预览页，直接弹打印设置）
  try {
    await printByIframe(pdfBlobUrl.value);
  } catch (error) {
    // console.error('打印执行异常:', error);
    proxy?.$modal.msgError('打印触发失败，请重试');
    // 异常时主动释放Blob URL，避免内存泄漏
    if (pdfBlobUrl.value) {
      window.URL.revokeObjectURL(pdfBlobUrl.value);
      pdfBlobUrl.value = '';
    }
  }
};

/** iframe打印（兼容浏览器阻止弹窗场景） */
// const printByIframe = (blobUrl: string) => {
//   return new Promise((resolve, reject) => {
//     let iframe = document.getElementById('print_area') as HTMLIFrameElement;
//     // 不存在则创建iframe
//     if (!iframe) {
//       iframe = document.createElement('iframe');
//       iframe.id = 'print_area';
//       iframe.style.display = 'none';
//       document.body.appendChild(iframe);
//     }

//     // 定义打印完成处理函数
//     const handlePrintComplete = () => {
//       printResultDialog.visible = true;
//       // 移除事件监听器，避免内存泄漏
//       window.removeEventListener('afterprint', handlePrintComplete);
//       if (iframe.contentWindow) {
//         iframe.contentWindow.removeEventListener('afterprint', handlePrintComplete);
//       }
//     };

//     // iframe加载完成触发打印
//     const handleIframeLoad = () => {
//       try {
//         const iframeWindow = iframe.contentWindow;
//         if (iframeWindow) {
//           // 在调用print()之前就绑定afterprint事件监听器
//           // 同时在主窗口和iframe窗口上绑定，确保兼容性
//           window.addEventListener('afterprint', handlePrintComplete);
//           iframeWindow.addEventListener('afterprint', handlePrintComplete);
//           // 触发原生打印弹窗
//           iframeWindow.print();

//           // 增加一个备用方案，确保即使afterprint事件不触发，结果确认弹框也会打开
//           setTimeout(() => {
//             // 检查printResultDialog是否已经可见，如果不可见，手动打开
//             if (!printResultDialog.visible) {
//               handlePrintComplete();
//             }
//           }, 1000);
//           setTimeout(() => {
//             window.URL.revokeObjectURL(pdfBlobUrl.value);
//             pdfBlobUrl.value = '';
//             resolve('打印操作完成');
//           }, 5000);
//         } else {
//           throw new Error('无法获取iframe窗口对象');
//         }
//       } catch (error) {
//         reject(error);
//         proxy?.$modal.msgError('iframe打印失败，请手动处理');
//         window.URL.revokeObjectURL(pdfBlobUrl.value);
//         pdfBlobUrl.value = '';
//         // 移除事件监听器
//         window.removeEventListener('afterprint', handlePrintComplete);
//         if (iframe.contentWindow) {
//           iframe.contentWindow.removeEventListener('afterprint', handlePrintComplete);
//         }
//         // 打印执行异常时，也弹出结果弹框
//         printResultDialog.visible = true;
//       } finally {
//         iframe.removeEventListener('load', handleIframeLoad);
//       }
//     };
//     // 绑定加载事件+设置src
//     iframe.removeEventListener('load', handleIframeLoad);
//     iframe.addEventListener('load', handleIframeLoad);
//     iframe.src = blobUrl;
//   });
// };

// iframe加载完成事件处理函数
const printByIframe = (blobUrl: string) => {
  return new Promise((resolve, reject) => {
    let iframe = document.getElementById('print_area') as HTMLIFrameElement;
    if (!iframe) {
      iframe = document.createElement('iframe');
      iframe.id = 'print_area';
      iframe.style.display = 'none';
      document.body.appendChild(iframe);
    }

    // 定义打印完成后的统一处理函数
    const handlePrintComplete = () => {
      // 确保只弹出一次
      if (!printResultDialog.visible) {
        printResultDialog.visible = true;
      }
      // 释放Blob URL，避免内存泄漏
      if (pdfBlobUrl.value) {
        window.URL.revokeObjectURL(pdfBlobUrl.value);
        pdfBlobUrl.value = '';
      }
      // 移除事件监听，避免内存泄漏
      window.removeEventListener('afterprint', handlePrintComplete);
      if (iframe.contentWindow) {
        iframe.contentWindow.removeEventListener('afterprint', handlePrintComplete);
      }
      resolve('打印操作完成');
    };

    const handleIframeLoad = () => {
      try {
        const iframeWindow = iframe.contentWindow;
        if (iframeWindow) {
          window.addEventListener('afterprint', handlePrintComplete);
          iframeWindow.addEventListener('afterprint', handlePrintComplete);

          // 触发打印窗口
          iframeWindow.print();

          // 如果afterprint事件没触发（极少数浏览器兼容问题），1.5秒后兜底显示
          setTimeout(() => {
            if (!printResultDialog.visible) {
              handlePrintComplete();
            }
          }, 1500);
        } else {
          throw new Error('无法获取iframe窗口对象');
        }
      } catch (error) {
        reject(error);
        proxy?.$modal.msgError('iframe打印失败，请手动处理');
        if (pdfBlobUrl.value) {
          window.URL.revokeObjectURL(pdfBlobUrl.value);
          pdfBlobUrl.value = '';
        }
        window.removeEventListener('afterprint', handlePrintComplete);
        if (iframe.contentWindow) {
          iframe.contentWindow.removeEventListener('afterprint', handlePrintComplete);
        }
        printResultDialog.visible = true;
      } finally {
        iframe.removeEventListener('load', handleIframeLoad);
      }
    };

    iframe.removeEventListener('load', handleIframeLoad);
    iframe.addEventListener('load', handleIframeLoad);
    iframe.src = blobUrl;
  });
};
const loadFrame = () => {
  // console.log('iframe加载完成事件触发');
};

/** 终止打印申请 */
// const names = ref<Array<string | number>>([]);
const handleStop = async (row?: any) => {
  const _ids = row?.id || ids.value;
  await proxy?.$modal.confirm('是否确认终止申请单编号为"' + row.applyCode + '"的数据项？').finally(() => (loading.value = false));
  // console.log('终止打印申请', row.id, row);
  const confirmRes = await queryConfirmBefore(row.id);
  console.log(confirmRes);

  // console.log('打印页面前的结果是否确认信息查询', confirmRes, row.id, row.applyCode);
  if (!confirmRes.data) {
    proxy?.$modal.msgError(confirmRes.msg || '操作失败');
    return;
  }
  await printApplyAbend(_ids);
  proxy?.$modal.msgSuccess('操作成功');
  await getList();
};

/** 取消按钮 */
const cancel = () => {
  reset();
  dialog.visible = false;
};

// 表单重置
const reset = () => {
  form.value = { ...initFormData };
};

/** 搜索按钮操作 */
const handleQuery = () => {
  queryParams.value.pageNum = 1;
  getList();
};

/** 重置按钮操作 */
const resetQuery = () => {
  handleQuery();
};

onMounted(() => {
  getList();
});
</script>
