import request from '@/utils/request';
import { AxiosPromise } from 'axios';

// 查询模板编制列表
export function docOperateOrderList(query: {}): AxiosPromise<[]> {
  return request({
    url: '/dms/doc-operate-order/list',
    method: 'get',
    params: query
  });
}
export function docOperateOrderListNew(query: {}): AxiosPromise<[]> {
  return request({
    url: '/dms/doc-operate-order/list/new',
    method: 'get',
    params: query
  });
}

export function docOperateOrderListEdit(query: {}): AxiosPromise<[]> {
  return request({
    url: '/dms/doc-operate-order/list/revise',
    method: 'get',
    params: query
  });
}


export function docOperateOrderListApprove(query: {}): AxiosPromise<[]> {
  return request({
    url: '/dms/doc-operate-order/list/approve',
    method: 'get',
    params: query
  });
}

export function docOperateOrderListFollow(query: {}): AxiosPromise<[]> {
  return request({
    url: '/dms/doc-operate-order/list/track',
    method: 'get',
    params: query
  });
}

export function listDocCreateOperate(query: {}): AxiosPromise<[]> {
  return request({
    url: '/dms/doc-operate-order/list/approved/doc-create',
    method: 'get',
    params: query
  });
}

export function addOperateOrder(query: {}): AxiosPromise<[]> {
  return request({
    url: '/dms/doc-operate-order',
    method: 'post',
    data: query
  });
}

// 新增修订操作申请
export function addOperateOrderRevise(query: {}): AxiosPromise<[]> {
  return request({
    url: '/dms/doc-operate-order/revise',
    method: 'post',
    data: query
  });
}



export function editOperateOrder(query: {}): AxiosPromise<[]> {
  return request({
    url: '/dms/doc-operate-order',
    method: 'put',
    data: query
  });
}

// 编辑修订操作申请
export function editOperateOrderRevise(query: {}): AxiosPromise<[]> {
  return request({
    url: '/dms/doc-operate-order/revise',
    method: 'put',
    data: query
  });
}


export function deleteOperateOrder(id): AxiosPromise<[]> {
  return request({
    url: `/dms/doc-operate-order/${id}`,
    method: 'delete',
  });
}
// 删除修订操作申请
export function deleteOperateOrderRevise(id): AxiosPromise<[]> {
  return request({
    url: `/dms/doc-operate-order/revise/${id}`,
    method: 'delete',
  });
}
export function operateOrderDetail(id): AxiosPromise<[]> {
  return request({
    url: `/dms/doc-operate-order/${id}`,
    method: 'get',
  });
}

export function operateOrderSubmit(query): AxiosPromise<[]> {
  return request({
    url: `/dms/doc-operate-order/submit-and-flow-start`,
    method: 'post',
    data: query
  });
}


