import request from '@/utils/request';
import { AxiosPromise } from 'axios';

// 查询打印申请列表
export function listPrintApplyEdit(query: {}): AxiosPromise<[]> {
  return request({
    url: '/dms/print-apply/list',
    method: 'get',
    params: query
  });
}

// 查询打印申请审批列表
export function listPrintApplyApproval(query: {}): AxiosPromise<[]> {
  return request({
    url: '/dms/print-apply/list',
    method: 'get',
    params: query
  });
}

// 查询打印申请列表
export function listPrintApply(query: {}): AxiosPromise<[]> {
  return request({
    url: '/dms/print-apply/list',
    method: 'get',
    params: query
  });
}

// 获取打印申请ossId
export function getPrintApplyFile(id: string | number): AxiosPromise<[]> {
  return request({
    url: `/dms/print-apply/file/${id}`,
    method: 'get'
  });
}

// 新增打印申请
export function addPrintApplyEdit(data: {}) {
  return request({
    url: '/dms/print-apply',
    method: 'post',
    data: data
  });
}

// 修改打印申请
export function updatePrintApplyEdit(data: {}) {
  return request({
    url: '/dms/print-apply',
    method: 'put',
    data: data
  });
}

// 单个工作流提交 操作申请流程提交
export function submitAndFlowStart(data: {}) {
  return request({
    // url: '/dms/print-apply/submitAndFlowStart',
    url: '/dms/print-apply/submit-and-flow-start',
    method: 'post',
    data: data
  });
}

// 删除打印申请
export function delPrintApplyEdit(PrintApplyId: string | number | Array<string | number>) {
  return request({
    url: '/dms/print-apply/' + PrintApplyId,
    method: 'delete'
  });
}

/** ******************************************************************************************************* */
// 打印申请页面的API
// 打印申请审核API(get方法) 打印申请审核主页面查询方法
export function approvalSubmit(data: {}) {
  return request({
    url: '/dms/print-apply/list/approve',
    method: 'get',
    params: data
  });
}

// 打印申请审核跟踪API(get方法) 打印申请审核跟踪查询方法
export function approvalTracking(data: {}) {
  return request({
    url: '/dms/print-apply/list/track',
    method: 'get',
    params: data
  });
}

// 获取打印申请详细信息
export function getPrintApplyDetail(PrintApplyId: string | number): AxiosPromise<[]> {
  return request({
    url: '/dms/print-apply/' + PrintApplyId,
    method: 'get'
  });
}

// 打印申请打印主页面查询方法
export function printSubmit(data: {}) {
  return request({
    url: '/dms/print-apply/list/print',
    method: 'get',
    params: data
  });
}
// 打印申请主页面查询方法 get /print-apply/list/main
export function printApplyMain(data: {}) {
  return request({
    url: '/dms/print-apply/list/main',
    method: 'get',
    params: data
  });
}

// 新增打印申请 POST /dms/print-apply
export function addPrintApply(data: {}) {
  return request({
    url: '/dms/print-apply',
    method: 'post',
    data: data
  });
}

// 打印页面打印方法 PUT /dms/print-apply/print/doc
export function printApply(data: {}) {
  return request({
    url: '/dms/print-apply/print/doc',
    method: 'put',
    data: data
  });
}

// 浏览器打印PDF get /print/browser/preview-pdf
export const previewPdf = (data: {}) => {
  return request({
    // url: '/dms/print/browser/preview-pdf',
    url: '/dms/print/browser/preview-pdf',
    method: 'get',
    params: data,
    responseType: 'blob'
  });
};

// 扫码发放打印明细信息查询 get /print-doc-info/list/printed
export function getPrintApplyScanDistributeDetail(data: {}) {
  return request({
    url: '/dms/print-doc-info/list/printed',
    method: 'get',
    params: data
  });
}

// 打印文件发放 put /print-doc-info/print-issue
export function printApplyScanDistribute(data: {}) {
  return request({
    url: '/dms/print-doc-info/print-issue',
    method: 'put',
    data: data
  });
}
// 打印页面前的结果是否确认信息查询 GET /print-doc-info/info/query-confirm-before/{printApplyId}
/**
 * @param printApplyId 打印申请主键
 * @returns
 */
export function queryConfirmBefore(printApplyId: string | number): AxiosPromise<[]> {
  return request({
    url: `/dms/print-doc-info/info/query-confirm-before/${printApplyId}`,
    method: 'get'
  });
}

// 打印成功 put /print-doc-info/print-success
/**
 * 打印成功
 * @param printApplyIds array[string] 打印申请主键
 * @param remark string 打印取消备注
 * @returns
 */
export function printSuccess(data: {}) {
  return request({
    url: '/dms/print-doc-info/print-success',
    method: 'put',
    data: data
  });
}

// 打印失败 put /print-doc-info/print-failure
/**
 * 打印失败
 * @param ids array[string] 打印明细主键集合
 * @param printApplyId string 打印申请主键
 * @param failReason string 失败原因
 * @returns
 */
export function printFail(data: {}) {
  return request({
    url: '/dms/print-doc-info/print-failure',
    method: 'put',
    data: data
  });
}

// 打印取消 put /print-doc-info/print-cancel
/**
 * 打印取消
 * @param printApplyIds array[string] 打印申请主键
 * @param remark string 打印取消备注
 * @returns
 */
export function printCancel(data: {}) {
  return request({
    url: '/dms/print-doc-info/print-cancel',
    method: 'put',
    data: data
  });
}

// 打印文件发放 put /print-doc-info/print-issue
/**
 * 打印文件发放
 * @param ids array[string] 打印明细主键集合
 * @param receiveUserId integer 领取人账号(必填之一)
 * @param receiveUserName string 领取人账号(必填之一)
 * @param issueUserId integer 发放人账号(非必填)
 * @returns
 */
export const printIssue = (data: {}, es?: '') => {
  return request({
    url: '/dms/print-doc-info/print-issue',
    method: 'put',
    headers: {
      'X-Signature': es
    },
    data: data
  });
};

// 扫码回收打印明细信息查询 get /print-doc-info/list/received
export function getPrintApplyScanRecyclingDetail(data: {}) {
  return request({
    url: '/dms/print-doc-info/list/received',
    method: 'get',
    params: data
  });
}
// 扫码回收打印明细信息查询（用于异常回收）get /print-doc-info/list/received/abnormal
export function getPrintApplyScanRecyclingAbnormalDetail(data: {}) {
  return request({
    url: '/dms/print-doc-info/list/received/abnormal',
    method: 'get',
    params: data
  });
}

// 打印文件回收 put /print-doc-info/print-return
/**
 * 打印文件回收
 * @param ids array[string] 打印明细主键集合
 * @param userId integer 交回人账号(必填之一)
 * @param userName string 交回人账号(必填之一)
 * @param confirmUserId string 确认交回人账号id(非必填)
 * @param confirmUserName string 确认交回人账号(非必填)
 * @param abnormalReason string 异常回收原因(非必填)
 * @returns
 */
export function printReturn(data: {}, es?: string) {
  return request({
    url: '/dms/print-doc-info/print-return',
    method: 'put',
    headers: {
      'X-Signature': es
    },
    data: data
  });
}

// 异常打印文件回收 put /print-doc-info/print-return/abnormal
/**
 * 打印文件回收
 * @param ids array[string] 打印明细主键集合
 * @param userId integer 交回人账号(必填之一)
 * @param userName string 交回人账号(必填之一)
 * @param confirmUserId string 确认交回人账号id(非必填)
 * @param confirmUserName string 确认交回人账号(非必填)
 * @param abnormalReason string 异常回收原因(非必填)
 * @returns
 */
export function printReturnAbnormal(data: {}) {
  return request({
    url: '/dms/print-doc-info/print-return/abnormal',
    method: 'put',
    data: data
  });
}

// 获取打印文件信息详细信息 get /print-doc-info/{id}
/**
 * 获取打印文件信息详细信息
 * @param id string 打印文件主键
 * @returns
 */
export function getPrintDocInfoDetail(id: string | number): AxiosPromise<[]> {
  return request({
    url: '/dms/print-doc-info/' + id,
    method: 'get'
  });
}

// 扫码关闭打印明细信息查询 get /print-doc-info/list/closed
export function getPrintApplyScanCloseDetail(data: {},  es?: string) {
  return request({
    url: '/dms/print-doc-info/list/closed',
    method: 'get',
    params: data,
    headers: {
      'X-Signature': es
    }
  });
}
// 打印文件关闭 put /print-doc-info/print-close
/**
 * 打印文件关闭
 * @param ids array[string] 打印明细主键集合
 * @param userId integer 关闭人账号(必填之一)
 * @param username string 关闭人账号(必填之一)
 * @param confirmUserId string 确认关闭人账号id(非必填)
 * @param confirmUsername string 确认关闭人账号(非必填)
 * @returns
 */
export function printClose(data: {}, es?: string) {
  return request({
    url: '/dms/print-doc-info/print-close',
    method: 'put',
    headers: {
      'X-Signature': es
    },
    data: data
  });
}

// 销毁确认打印明细信息查询 get /print-doc-info/list/return
export function getPrintApplyDestructionConfirmationDetail(data: {}) {
  return request({
    url: '/dms/print-doc-info/list/return',
    method: 'get',
    params: data
  });
}

// 打印文件销毁 put /print-doc-info/print-destroy
/**
 * 打印文件销毁
 * @param ids array[string] 打印明细主键集合
 * @param userId integer 销毁人账号(非必填)
 * @param userName string 销毁人账号(非必填)
 * @param confirmUserId string 销毁确认人工号(必填之一)
 * @param confirmUserName string 确认销毁人账号(必填之一)
 * @param destroyAction string 销毁方式(必填)
 * @param destroyPlace string 销毁地点(必填)
 * @returns
 */
export function printDestroy(data: {}, es?: string) {
  return request({
    url: '/dms/print-doc-info/print-destroy',
    method: 'put',
    headers: {
      'X-Signature': es
    },
    data: data
  });
}

// 查询打印文件信息列表 get /print-doc-info/list
export function getPrintDocInfoList(data: {}) {
  return request({
    url: '/dms/print-doc-info/list',
    method: 'get',
    params: data
  });
}

// 打印结果确认信息查询 get /print-doc-info/list/confirm
/**
 * 打印结果确认信息查询
 * @param printApplyId string 打印申请主键
 * @returns
 */
export function getPrintApplyDocumentTrackingDetail(data: {}) {
  return request({
    url: '/dms/print-doc-info/list/confirm',
    method: 'get',
    params: data
  });
}

// 打印页面任务终止 put /print-apply/abend/{id}
/**
 * 打印页面任务终止
 * @param id string 打印申请主键
 * @returns
 */
export function printApplyAbend(id: string | number): AxiosPromise<[]> {
  return request({
    url: '/dms/print-apply/abend/' + id,
    method: 'put'
  });
}

// 打印件追踪主页面分页查询 get /print-doc-info/list/track
export function getPrintApplyDocumentTrackingList(data: {}) {
  return request({
    url: '/dms/print-doc-info/list/track',
    method: 'get',
    params: data
  });
}

// 打印件跟踪打印明细查询 get /print-doc-info/list/info
/**
 * 打印件跟踪打印明细查询
 * @param applyCode string 打印申请编号
 * @returns
 */
export function getPrintApplyTrackingDetail(data: {}) {
  return request({
    url: '/dms/print-doc-info/list/info',
    method: 'get',
    params: data
  });
}

// 导出打印文件信息列表 post /print-doc-info/export
export const exportPrintDocInfoList = (data: {}) => {
  return request({
    url: '/dms/print-doc-info/export',
    method: 'post',
    params: data,
    responseType: 'blob'
  });
}
