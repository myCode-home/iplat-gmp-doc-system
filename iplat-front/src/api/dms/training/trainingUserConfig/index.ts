import request from '@/utils/request';
import { AxiosPromise } from 'axios';

// 查询培训责任人配置列表
export function listOwnerConfig(query: {}): AxiosPromise<[]> {
  return request({
    url: '/dms/training-owner-config/pageList',
    method: 'get',
    params: query
  });
}

// 查询培训责任人配置详细
export function getOwnerConfig(id: string | number): AxiosPromise<[]> {
  return request({
    url: '/dms/training-owner-config/' + id,
    method: 'get'
  });
}

// 新增培训责任人配置
export function addOwnerConfig(data: {}) {
  return request({
    url: '/dms/training-owner-config',
    method: 'post',
    data: data
  });
}

// 修改培训责任人配置
export function updateOwnerConfig(data: {}) {
  return request({
    url: '/dms/training-owner-config',
    method: 'put',
    data: data
  });
}


// 删除培训责任人配置
export function delOwnerConfig(ids: string | number | Array<string | number>) {
  return request({
    url: '/dms/training-owner-config/' + ids,
    method: 'delete'
  });
}
