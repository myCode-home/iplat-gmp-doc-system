import request from '@/utils/request';
import { AxiosPromise } from 'axios';

// 查询页面配置规则列表
export function listPageRule(query: {}): AxiosPromise<[]> {
  return request({
    url: '/system/page-config-rule/list',
    method: 'get',
    params: query
  });
}

// 查询页面配置规则详细
export function getPageRule(pageConfigRuleId: string | number): AxiosPromise<[]> {
  return request({
    url: '/system/page-config-rule/' + pageConfigRuleId,
    method: 'get'
  });
}

// 新增页面配置规则
export function addPageRule(data: {}) {
  return request({
    url: '/system/page-config-rule',
    method: 'post',
    data: data
  });
}

// 修改页面配置规则
export function updatePageRule(data: {}) {
  return request({
    url: '/system/page-config-rule',
    method: 'put',
    data: data
  });
}


// 删除页面配置规则
export function delPageRule(pageConfigRuleId: string | number | Array<string | number>) {
  return request({
    url: '/system/page-config-rule/' + pageConfigRuleId,
    method: 'delete'
  });
}
