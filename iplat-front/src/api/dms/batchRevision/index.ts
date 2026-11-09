import request from '@/utils/request';
import { AxiosPromise } from 'axios';

// 查询批量修订申请主表创建列表 get /batch-revise-order-main/editList
export function getBatchReviseOrderMainEditList(data: any): AxiosPromise<any> {
  return request({
    url: '/dms/batch-revise-order-main/editList',
    method: 'get',
    params: data
  });
}

// 新增批量修订申请主表 post /batch-revise-order-main
export function addBatchReviseOrderMain(data: any): AxiosPromise<any> {
  return request({
    url: '/dms/batch-revise-order-main',
    method: 'post',
    data: data
  });
}

// 获取批量修订申请主表详细信息 get /batch-revise-order-main/{id}
export function getBatchReviseOrderMainDetail(id: number, params?: any): AxiosPromise<any> {
  return request({
    url: `/dms/batch-revise-order-main/${id}`,
    method: 'get',
    params: params
  });
}

// 修改批量修订申请主表 put /batch-revise-order-main
export function updateBatchReviseOrderMain(data: any): AxiosPromise<any> {
  return request({
    url: '/dms/batch-revise-order-main',
    method: 'put',
    data: data
  });
}

// 操作申请查询生效文件 get /document-lib/list/operate-order/active
export function getOperateOrderActiveFileList(data: any): AxiosPromise<any> {
  return request({
    url: '/dms/document-lib/list/operate-order/active',
    method: 'get',
    params: data
  });
}

// 提交批量修订申请主表并启动流程 post /batch-revise-order-main/submitAndFlowStart
export function submitAndFlowStart(data: any): AxiosPromise<any> {
  return request({
    url: '/dms/batch-revise-order-main/submitAndFlowStart',
    method: 'post',
    data: data
  });
}

// 删除批量修订申请主表 DELETE /batch-revise-order-main/{ids}
export function deleteBatchReviseOrderMain(ids: string): AxiosPromise<any> {
  return request({
    url: `/dms/batch-revise-order-main/${ids}`,
    method: 'delete'
  });
}

// 查询批量修订申请主表审核列表 get /batch-revise-order-main/approvalList
export function getBatchReviseOrderMainApprovalList(data: any): AxiosPromise<any> {
  return request({
    url: '/dms/batch-revise-order-main/approvalList',
    method: 'get',
    params: data
  });
}

// 查询批量修订申请主表跟踪列表 get /batch-revise-order-main/list
export function getBatchReviseOrderMainTrackingList(data: any): AxiosPromise<any> {
  return request({
    url: '/dms/batch-revise-order-main/list',
    method: 'get',
    params: data
  });
}
//  删除单个文件修订申请详情 delete /batch-revise-order-main/details/{detailId}
export function deleteBatchReviseOrderDetail(detailId: number): AxiosPromise<any> {
  return request({
    url: `/dms/batch-revise-order-main/details/${detailId}`,
    method: 'delete'
  });
}

// 查询批量修订订单详情列表 get /batch-revise-order-detail/list
export function getBatchReviseOrderDetailList(data: any): AxiosPromise<any> {
  return request({
    url: '/dms/batch-revise-order-detail/list',
    method: 'get',
    params: data
  });
}

// 获取批量修订订单详情详细信息 get /batch-revise-order-detail/{id}
export function getBatchReviseOrderDetailDetail(id: number, params?: any): AxiosPromise<any> {
  return request({
    url: `/dms/batch-revise-order-detail/${id}`,
    method: 'get',
    params: params
  });
}

// 提交前校验 get /batch-revise-order-main/{batchReviseOrderId}/validate
export function validateBatchReviseOrderMain(batchReviseOrderId: number | string): AxiosPromise<any> {
  return request({
    url: `/dms/batch-revise-order-main/${batchReviseOrderId}/validate`,
    method: 'get'
  });
}
