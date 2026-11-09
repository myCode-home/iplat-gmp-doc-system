import request from '@/utils/request';
import { AxiosPromise } from 'axios';

// 查询成册打印申请列表
export function listBookPrint(query: {}): AxiosPromise<[]> {
  return request({
    url: '/dms/volumes-print-apply/list/main',
    method: 'get',
    params: query
  });
}

// 查询成册打印申请审批列表
export function listBookPrintApproval(query: {}): AxiosPromise<[]> {
  return request({
    url: '/dms/volumes-print-apply/list/approve',
    method: 'get',
    params: query
  });
}

// 查询成册打印申请跟踪列表
export function listBookPrintTrack(query: {}): AxiosPromise<[]> {
  return request({
    url: '/dms/volumes-print-apply/list/track',
    method: 'get',
    params: query
  });
}


// 成册打印主页面查询
export function listPrintPage(query: {}): AxiosPromise<[]> {
  return request({
    url: '/dms/volumes-print-apply/list/print',
    method: 'get',
    params: query
  });
}

// 查询成册打印申请详细
export function getBookPrint(id: string | number): AxiosPromise<[]> {
  return request({
    url: '/dms/volumes-print-apply/' + id,
    method: 'get'
  });
}

// 获取成册打印申请ossId
export function getBookPrintFile(id: string | number): AxiosPromise<[]> {
  return request({
    url: `/dms/volumes-print-apply/file/${id}`,
    method: 'get'
  });
}

// 成册打印申请对应打印件跟踪查询
export function getBookPrintFilTrackInfo(volumesPrintId: string | number): AxiosPromise<[]> {
  return request({
    url: `/dms/volumes-print-info/track/apply/${volumesPrintId}`,
    method: 'get'
  });
}

// 新增成册打印申请
export function addBookPrint(data: {}) {
  return request({
    url: '/dms/volumes-print-apply',
    method: 'post',
    data: data
  });
}

// 修改成册打印申请
export function updateBookPrint(data: {}) {
  return request({
    url: '/dms/volumes-print-apply',
    method: 'put',
    data: data
  });
}

// 单个工作流提交
export function submitAndFlowStart(data: {}) {
  return request({
    url: '/dms/volumes-print-apply/submitAndFlowStart',
    method: 'post',
    data: data
  });
}


// 删除成册打印申请
export function delBookPrint(id: string | number | Array<string | number>) {
  return request({
    url: '/dms/volumes-print-apply/' + id,
    method: 'delete'
  });
}

// 成册打印件跟踪查询列表
export function listBookPrintInfoTrack(query: {}): AxiosPromise<[]> {
  return request({
    url: '/dms/volumes-print-info/list/track',
    method: 'get',
    params: query
  });
}
// 查询成册打印申请明细列表
export function listApplyDetail(id: {}): AxiosPromise<[]> {
  return request({
    url: `/dms/volumes-print-apply/volumes-print-apply-detail/list/${id}`,
    method: 'get'
  });
}
// 成册打印申请对应打印件跟踪查询
export function getBookTrackApply(volumesPrintId: string | number): AxiosPromise<[]> {
  return request({
    url: '/dms//volumes-print-info/track/apply/' + volumesPrintId,
    method: 'get'
  });
}

// 根据成册打印件主键ID或打印明细主键ID查询打印异常列表信息
export function getBookPrintError(volumesPrintId: string | number): AxiosPromise<[]> {
  return request({
    url: `/dms/volumes-print-info/print-error/${volumesPrintId}`,
    method: 'get'
  });
}

