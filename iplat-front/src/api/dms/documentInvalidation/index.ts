import request from '@/utils/request';
import { AxiosPromise } from 'axios';

// 查询失效文档编制列表（首页列表） get /doc-invalid-order/editList
export function getDocInvalidationApplyEditList(data: {}): AxiosPromise<[]> {
  return request({
    url: '/dms/doc-invalid-order/editList',
    method: 'get',
    params: data
  })
}

// 查询失效文档跟踪列表 get /doc-invalid-order/list
export function getDocInvalidationApplyList(data: {}): AxiosPromise<[]> {
  return request({
    url: '/dms/doc-invalid-order/list',
    method: 'get',
    params: data
  })
}

// 查询失效文档审核列表 get /doc-invalid-order/approvalList
export function getDocInvalidationApplyApprovalList(data: {}): AxiosPromise<[]> {
  return request({
    url: '/dms/doc-invalid-order/approvalList',
    method: 'get',
    params: data
  })
}

// 获取失效文档详细信息 get /doc-invalid-order/{id}
export function getDocInvalidationApplyDetail(id: string | number): AxiosPromise<[]> {
  return request({
    url: '/dms/doc-invalid-order/' + id,
    method: 'get',
  })
}

// 新增失效文档 post /doc-invalid-order
export function addDocInvalidationApply(data: {}): AxiosPromise<[]> {
  return request({
    url: '/dms/doc-invalid-order',
    method: 'post',
    data: data
  })
}

// 删除失效文档 delete /doc-invalid-order/{ids}
/**
 *
 * @param ids 主键串
 * @returns
 */
export function deleteDocInvalidationApply(ids: string | number): AxiosPromise<[]> {
  return request({
    url: '/dms/doc-invalid-order/' + ids,
    method: 'delete',
  })
}

// 修改失效文档 put /doc-invalid-order
export function updateDocInvalidationApply(data: {}): AxiosPromise<[]> {
  return request({
    url: '/dms/doc-invalid-order',
    method: 'put',
    data: data
  })
}

// 导出失效文档列表 post /doc-invalid-order/export
export function exportDocInvalidationApplyList(data: {}): AxiosPromise<[]> {
  return request({
    url: '/dms/doc-invalid-order/export',
    method: 'post',
    data: data
  })
}

// 单个工作流提交 post /doc-invalid-order/submitAndFlowStart
export function submitAndFlowStartDocInvalidationApply(data: {}): AxiosPromise<[]> {
  return request({
    url: '/dms/doc-invalid-order/submitAndFlowStart',
    method: 'post',
    data: data
  })
}
