import request from '@/utils/request';
import { AxiosPromise } from 'axios';

// 查询扩展列表
export function listExtendedAttribute(query: {}): AxiosPromise<[]> {
  return request({
    url: '/system/extended-attribute/list',
    method: 'get',
    params: query
  });
}

// 查询扩展详细
export function getExtendedAttribute(extendedAttributeId: string | number): AxiosPromise<[]> {
  return request({
    url: '/system/extended-attribute/' + extendedAttributeId,
    method: 'get'
  });
}

// 新增扩展
export function addExtendedAttribute(data: {}) {
  return request({
    url: '/system/extended-attribute',
    method: 'post',
    data: data
  });
}

// 修改扩展
export function updateExtendedAttribute(data: {}) {
  return request({
    url: '/system/extended-attribute',
    method: 'put',
    data: data
  });
}


// 删除扩展
export function delExtendedAttribute(extendedAttributeId: string | number | Array<string | number>) {
  return request({
    url: '/system/extended-attribute/' + extendedAttributeId,
    method: 'delete'
  });
}
