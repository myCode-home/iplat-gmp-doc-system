import request from '@/utils/request';
import { AxiosPromise } from 'axios';

// 查询序列号列表
export function listSerialRule(query: {}): AxiosPromise<[]> {
  return request({
    url: '/system/serial-rule/list',
    method: 'get',
    params: query
  });
}

// 查询序列号详细
export function getSerialRule(serialRuleId: string | number): AxiosPromise<[]> {
  return request({
    url: '/system/serial-rule/' + serialRuleId,
    method: 'get'
  });
}

// 新增序列号
export function addSerialRule(data: {}) {
  return request({
    url: '/system/serial-rule',
    method: 'post',
    data: data
  });
}

// 修改序列号
export function updateSerialRule(data: {}) {
  return request({
    url: '/system/serial-rule',
    method: 'put',
    data: data
  });
}


// 删除序列号
export function delSerialRule(serialRuleId: string | number | Array<string | number>) {
  return request({
    url: '/system/serial-rule/' + serialRuleId,
    method: 'delete'
  });
}
