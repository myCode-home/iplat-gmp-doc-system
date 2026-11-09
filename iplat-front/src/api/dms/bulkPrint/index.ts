import request from '@/utils/request';
import { AxiosPromise } from 'axios';

// 批量打印申请主页面查询方法 get /print-apply-batch/list/main
export function listPrintApply(query: {}): AxiosPromise<[]> {
  return request({
    url: '/dms/print-apply-batch/list/main',
    method: 'get',
    params: query
  });
}

// 批量打印申请审核主页面查询方法 get /print-apply-batch/list/approve
export function listPrintApplyApprove(query: {}): AxiosPromise<[]> {
  return request({
    url: '/dms/print-apply-batch/list/approve',
    method: 'get',
    params: query
  });
}

// 批量打印申请跟踪主页面查询方法 get /print-apply-batch/list/track
export function listPrintApplyTrack(query: {}): AxiosPromise<[]> {
  return request({
    url: '/dms/print-apply-batch/list/track',
    method: 'get',
    params: query
  });
}

// 查询批量打印申请列表 get /print-apply-batch/list
export function listPrintApplyBatch(query: {}): AxiosPromise<[]> {
  return request({
    url: '/dms/print-apply-batch/list',
    method: 'get',
    params: query
  });
}

// 获取批量打印申请详细信息 get /print-apply-batch/{id}
export function getPrintApplyBatchDetail(id: string): AxiosPromise<any> {
  return request({
    url: `/dms/print-apply-batch/${id}`,
    method: 'get'
  });
}

// 导出批量打印申请列表 post /print-apply-batch/export
export function exportPrintApplyBatch(data: {}): AxiosPromise<any> {
  return request({
    url: '/dms/print-apply-batch/export',
    method: 'post',
    data
  });
}

// 新增批量打印申请 post /print-apply-batch
export function createPrintApplyBatch(data: {}): AxiosPromise<any> {
  return request({
    url: '/dms/print-apply-batch',
    method: 'post',
    data
  });
}

// 修改批量打印申请 put /print-apply-batch
export function updatePrintApplyBatch(data: {}): AxiosPromise<any> {
  return request({
    url: '/dms/print-apply-batch',
    method: 'put',
    data
  });
}

// 删除批量打印申请 delete /print-apply-batch/{ids}
export function deletePrintApplyBatch(ids: string): AxiosPromise<any> {
  return request({
    url: `/dms/print-apply-batch/${ids}`,
    method: 'delete'
  });
}

// 操作申请流程提交 post /print-apply-batch/submit-and-flow-start
export function submitAndFlowStartPrintApplyBatch(data: {}): AxiosPromise<any> {
  return request({
    url: '/dms/print-apply-batch/submit-and-flow-start',
    method: 'post',
    data
  });
}
