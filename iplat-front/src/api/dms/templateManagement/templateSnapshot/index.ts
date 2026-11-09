import request from '@/utils/request';
import { AxiosPromise } from 'axios';

// 查询模板快照列表
export function listSnapshot(query: {}): AxiosPromise<[]> {
  return request({
    url: '/dms/tmplSnapshot/list',
    method: 'get',
    params: query
  });
}

// 查询模板快照详细
export function getSnapshot(id: string | number): AxiosPromise<[]> {
  return request({
    url: '/dms/tmplSnapshot/' + id,
    method: 'get'
  });
}

// 新增模板快照
export function addSnapshot(data: {}) {
  return request({
    url: '/dms/tmplSnapshot',
    method: 'post',
    data: data
  });
}

// 修改模板快照
export function updateSnapshot(data: {}) {
  return request({
    url: '/dms/tmplSnapshot',
    method: 'put',
    data: data
  });
}

// 删除模板快照
export function delSnapshot(id: string | number | Array<string | number>) {
  return request({
    url: '/dms/tmplSnapshot/' + id,
    method: 'delete'
  });
}
