import request from '@/utils/request';
import { AxiosPromise } from 'axios';

// 查询文档记录失效申请单列表  get /doc-record-failure-order/editList
export function getRecordInvalidationApplyEditList(data: {}): AxiosPromise<[]> {
  return request({
    url: '/dms/doc-record-failure-order/editList',
    method: 'get',
    params: data
  })
}

// 查询文档记录失效审核单列表 get /doc-record-failure-order/approvalList
export function getRecordInvalidationApplyApprovalList(data: {}): AxiosPromise<[]> {
  return request({
    url: '/dms/doc-record-failure-order/approvalList',
    method: 'get',
    params: data
  })
}

// 查询文档记录失效申请单列表 get /doc-record-failure-order/list（全部列表）
export function getRecordInvalidationApplyList(data: {}): AxiosPromise<[]> {
  return request({
    url: '/dms/doc-record-failure-order/list',
    method: 'get',
    params: data
  })
}

// 导出文档记录失效申请单列表 post /doc-record-failure-order/export
export function exportRecordInvalidationApplyList(data: {}): AxiosPromise<[]> {
  return request({
    url: '/dms/doc-record-failure-order/export',
    method: 'post',
    data: data
  })
}

// 获取文档记录失效申请单详细信息 /doc-record-failure-order/{id}
export function getRecordInvalidationApplyDetail(id: string | number): AxiosPromise<[]> {
  return request({
    url: '/dms/doc-record-failure-order/' + id,
    method: 'get',
  })
}

// 新增文档记录失效申请单 post /doc-record-failure-order
export function addRecordInvalidationApply(data: {}): AxiosPromise<[]> {
  return request({
    url: '/dms/doc-record-failure-order',
    method: 'post',
    data: data
  })
}

// 修改文档记录失效申请单 put /doc-record-failure-order
export function updateRecordInvalidationApply(data: {}): AxiosPromise<[]> {
  return request({
    url: '/dms/doc-record-failure-order',
    method: 'put',
    data: data
  })
}

// 删除文档记录失效申请单 delete  /doc-record-failure-order/{ids}
export function deleteRecordInvalidationApply(id: string | number | Array<string | number>) {
  return request({
    url: '/dms/doc-record-failure-order/' + id,
    method: 'delete'
  });
}

// 单个工作流提交 post /doc-record-failure-order/submitAndFlowStart
export function submitAndFlowStartRecordInvalidationApply(data: {}): AxiosPromise<[]> {
  return request({
    url: '/dms/doc-record-failure-order/submitAndFlowStart',
    method: 'post',
    data: data
  })
}
