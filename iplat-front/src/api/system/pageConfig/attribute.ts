import request from '@/utils/request';
import { AxiosPromise } from 'axios';

// 查询页面配置属性列表
export function listPageAttribute(query: {}): AxiosPromise<[]> {
  return request({
    url: '/system/page-config-attribute/list',
    method: 'get',
    params: query
  });
}

// 查询页面配置属性详细
export function getPageAttribute(pageConfigAttributeId: string | number): AxiosPromise<[]> {
  return request({
    url: '/system/page-config-attribute/' + pageConfigAttributeId,
    method: 'get'
  });
}

// 新增页面配置属性
export function addPageAttribute(data: {}) {
  return request({
    url: '/system/page-config-attribute',
    method: 'post',
    data: data
  });
}

// 修改页面配置属性
export function updatePageAttribute(data: {}) {
  return request({
    url: '/system/page-config-attribute',
    method: 'put',
    data: data
  });
}


// 删除页面配置属性
export function delPageAttribute(pageConfigAttributeId: string | number | Array<string | number>) {
  return request({
    url: '/system/page-config-attribute/' + pageConfigAttributeId,
    method: 'delete'
  });
}
