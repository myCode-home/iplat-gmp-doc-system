import request from '@/utils/request';
import { AxiosPromise } from 'axios';

// 查询页面配置列表
export function listPageConfig(query: {}): AxiosPromise<[]> {
  return request({
    url: '/system/page-config/list',
    method: 'get',
    params: query
  });
}

// 查询页面配置详细
export function getPageConfig(pageConfigId: string | number): AxiosPromise<[]> {
  return request({
    url: '/system/page-config/' + pageConfigId,
    method: 'get'
  });
}

// 根据页面编号获取页面配置详细信息
export function getPageConfigPage(pageCode: string | number): AxiosPromise<[]> {
  return request({
    url: `/system/page-config/page/${pageCode}`,
    method: 'get'
  });
}

// 新增页面配置
export function addPageConfig(data: {}) {
  return request({
    url: '/system/page-config',
    method: 'post',
    data: data
  });
}

// 修改页面配置
export function updatePageConfig(data: {}) {
  return request({
    url: '/system/page-config',
    method: 'put',
    data: data
  });
}


// 删除页面配置
export function delPageConfig(pageConfigId: string | number | Array<string | number>) {
  return request({
    url: '/system/page-config/' + pageConfigId,
    method: 'delete'
  });
}
