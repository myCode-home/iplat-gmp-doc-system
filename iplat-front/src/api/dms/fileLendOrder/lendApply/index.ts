import request from '@/utils/request';
import { AxiosPromise } from 'axios';

// 查询借阅申请列表
export function listLendOrder(query: {}): AxiosPromise<[]> {
  return request({
    url: '/dms/document-lend-order/list',
    method: 'get',
    params: query
  });
}

// 查询借阅申请审核列表
export function listLendOrderAppro(query: {}): AxiosPromise<[]> {
  return request({
    url: '/dms/document-lend-order/approvalList',
    method: 'get',
    params: query
  });
}

// 查询借阅申请跟踪列表
export function listLendOrderTrack(query: {}): AxiosPromise<[]> {
  return request({
    url: '/dms/document-lend-order/trackList',
    method: 'get',
    params: query
  });
}

// 查询借阅库列表
export function listLendOrderLib(query: {}): AxiosPromise<[]> {
  return request({
    url: '/dms/document-lend-lib/list',
    method: 'get',
    params: query
  });
}

// 查询借阅申请详细
export function getLendOrder(id: string | number, query?: {}): AxiosPromise<[]> {
  return request({
    url: '/dms/document-lend-order/' + id,
    method: 'get',
    params: query
  });
}

// 获取借阅库详细信息
export function getLendLib(id: string | number, query?: {}): AxiosPromise<[]> {
  return request({
    url: '/dms/document-lend-lib/' + id,
    method: 'get',
    params: query
  });
}

// 新增借阅申请
export function addLendOrder(data: {}) {
  return request({
    url: '/dms/document-lend-order',
    method: 'post',
    data: data
  });
}

// 修改借阅申请
export function updateLendOrder(data: {}) {
  return request({
    url: '/dms/document-lend-order',
    method: 'put',
    data: data
  });
}


// 删除借阅申请
export function delLendOrder(ids: string | number | Array<string | number>) {
  return request({
    url: '/dms/document-lend-order/' + ids,
    method: 'delete'
  });
}

// 删除借阅申请
export function submitAndFlowStart(data: {}) {
  return request({
    url: '/dms/document-lend-order/submitAndFlowStart',
    method: 'post',
    data: data
  });
}