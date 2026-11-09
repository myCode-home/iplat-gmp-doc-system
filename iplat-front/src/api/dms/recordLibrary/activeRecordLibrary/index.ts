import request from '@/utils/request';
import { AxiosPromise } from 'axios';

// 查询生效记录库列表
export function activeListActiveRecord(query: {}): AxiosPromise<[]> {
  return request({
    url: '/dms/document-record-lib/activeList',
    method: 'get',
    params: query
  });
}

// 查询所有记录库列表
export function listActiveRecord(query: {}): AxiosPromise<[]> {
  return request({
    url: '/dms/document-record-lib/list',
    method: 'get',
    params: query
  });
}

// 查询记录库列表-历史
export function historyListActiveRecord(query: {}): AxiosPromise<[]> {
  return request({
    url: '/dms/document-record-lib/historyList',
    method: 'get',
    params: query
  });
}

// 根据文件库主键Id查询记录库
export function libListActiveRecord(dlId: string | number, query?: {}): AxiosPromise<[]> {
  return request({
    url: `/dms/document-record-lib/document-lib/list/${dlId}`,
    method: 'get',
    params: query
  });
}

// 查询生效记录库详细
export function getActiveRecord(id: string | number): AxiosPromise<[]> {
  return request({
    url: '/dms/document-record-lib/' + id,
    method: 'get'
  });
}

// 获取生效记录库ossId
export function getActiveRecordFile(id: string | number): AxiosPromise<[]> {
  return request({
    url: `/dms/document-record-lib/file/${id}`,
    method: 'get'
  });
}

// 新增生效记录库
export function addActiveRecord(data: {}) {
  return request({
    url: '/dms/document-record-lib',
    method: 'post',
    data: data
  });
}

// 修改生效记录库
export function updateActiveRecord(data: {}) {
  return request({
    url: '/dms/document-record-lib',
    method: 'put',
    data: data
  });
}

// 申请记录修订
export function reviseActiveRecord(data: {}) {
  return request({
    url: '/dms/document-record-lib/revise',
    method: 'post',
    data: data
  });
}

// 申请记录失效
export function invalidActiveRecord(data: {}) {
  return request({
    url: '/dms/document-record-lib/invalid',
    method: 'post',
    data: data
  });
}

// 单个工作流提交
export function submitAndFlowStart(data: {}) {
  return request({
    url: '/dms/document-record-lib/submitAndFlowStart',
    method: 'post',
    data: data
  });
}


// 删除生效记录库
export function delActiveRecord(id: string | number | Array<string | number>) {
  return request({
    url: '/dms/document-record-lib/' + id,
    method: 'delete'
  });
}

