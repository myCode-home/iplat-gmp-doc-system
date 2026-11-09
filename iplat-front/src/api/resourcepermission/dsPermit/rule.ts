import request from '@/utils/request';
import { AxiosPromise } from 'axios';

// 查询配置规则列表
export function listSubjectRule(query?: {}): AxiosPromise<[]> {
  return request({
    url: '/system/ds-permit-subject-rule/list',
    method: 'get',
    params: query
  });
}

// 查询配置规则详细
export function getSubjectRule(dsPermitSubjectRuleId: string | number): AxiosPromise<[]> {
  return request({
    url: '/system/ds-permit-subject-rule/' + dsPermitSubjectRuleId,
    method: 'get'
  });
}

// 新增配置规则
export function addSubjectRule(data: {}) {
  return request({
    url: '/system/ds-permit-subject-rule',
    method: 'post',
    data: data
  });
}

// 修改配置规则
export function updateSubjectRule(data: {}) {
  return request({
    url: '/system/ds-permit-subject-rule',
    method: 'put',
    data: data
  });
}

// 删除配置规则
export function delSubjectRule(dsPermitSubjectRuleId: string | number | Array<string | number>) {
  return request({
    url: '/system/ds-permit-subject-rule/' + dsPermitSubjectRuleId,
    method: 'delete'
  });
}

// 动态的新增或更新数据集授权配置规则信息
export function dynamicSubjectRule(data: []) {
  return request({
    url: '/system/ds-permit-subject-rule/dynamic',
    method: 'put',
    data: data
  });
}
