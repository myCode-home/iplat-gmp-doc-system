import request from '@/utils/request';
import { AxiosPromise } from 'axios';

// 查询文档快照列表
export function listSnapshot(query: {}): AxiosPromise<[]> {
  return request({
    url: '/dms/doc-create-snapshot/list',
    method: 'get',
    params: query
  });
}

// 查询文档快照详细
export function getSnapshot(id: string | number): AxiosPromise<[]> {
  return request({
    url: '/dms/doc-create-snapshot/' + id,
    method: 'get'
  });
}

// 新增文档快照
export function addSnapshot(data: {}) {
  return request({
    url: '/dms/doc-create-snapshot',
    method: 'post',
    data: data
  });
}

// 修改文档快照
export function updateSnapshot(data: {}) {
  return request({
    url: '/dms/doc-create-snapshot',
    method: 'put',
    data: data
  });
}

// 删除文档快照
export function delSnapshot(id: string | number | Array<string | number>) {
  return request({
    url: '/dms/doc-create-snapshot/' + id,
    method: 'delete'
  });
}
