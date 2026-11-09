import request from '@/utils/request';
import { AxiosPromise } from 'axios';

// 查询记录修订列表
export function listDocRecordRevise(query: {}): AxiosPromise<[]> {
  return request({
    url: '/dms/document-record-revise/list',
    method: 'get',
    params: query
  });
}

// 查询记录修订详细
export function getDocRecordRevise(id: string | number): AxiosPromise<[]> {
  return request({
    url: '/dms/document-record-revise/' + id,
    method: 'get'
  });
}

// 新增记录修订
export function addDocRecordRevise(data: {}) {
  return request({
    url: '/dms/document-record-revise',
    method: 'post',
    data: data
  });
}

// 修改记录修订
export function updateDocRecordRevise(data: {}) {
  return request({
    url: '/dms/document-record-revise',
    method: 'put',
    data: data
  });
}


// 删除记录修订
export function delDocRecordRevise(id: string | number | Array<string | number>) {
  return request({
    url: '/dms/document-record-revise/' + id,
    method: 'delete'
  });
}

