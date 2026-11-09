import request from '@/utils/request';
import { AxiosPromise } from 'axios';

// 查询扩展列表
export function listExtended(query: {}): AxiosPromise<[]> {
  return request({
    url: '/system/extended/list',
    method: 'get',
    params: query
  });
}

// 查询扩展详细
export function getExtended(extendedId: string | number): AxiosPromise<[]> {
  return request({
    url: '/system/extended/' + extendedId,
    method: 'get'
  });
}

// 新增扩展
export function addExtended(data: {}) {
  return request({
    url: '/system/extended',
    method: 'post',
    data: data
  });
}

// 修改扩展
export function updateExtended(data: {}) {
  return request({
    url: '/system/extended',
    method: 'put',
    data: data
  });
}


// 删除扩展
export function delExtended(extendedId: string | number | Array<string | number>) {
  return request({
    url: '/system/extended/' + extendedId,
    method: 'delete'
  });
}
