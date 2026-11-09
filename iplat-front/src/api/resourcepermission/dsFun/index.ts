import request from '@/utils/request';
import { AxiosPromise } from 'axios';

// 查询业务对象查询配置列表
export function listDsFun(query: {}): AxiosPromise<[]> {
  return request({
    url: '/system/ds-fun/list',
    method: 'get',
    params: query
  });
}

// 查询业务对象查询配置列表全部
export function listDsFunAll(query?: {}): AxiosPromise<[]> {
  return request({
    url: '/system/ds-fun/list/all',
    method: 'get',
    params: query
  });
}

// 查询业务对象查询配置详细
export function getDsFun(dsFunId: string | number): AxiosPromise<[]> {
  return request({
    url: '/system/ds-fun/' + dsFunId,
    method: 'get'
  });
}

// 新增业务对象查询配置
export function addDsFun(data: {}) {
  return request({
    url: '/system/ds-fun',
    method: 'post',
    data: data
  });
}

// 修改业务对象查询配置
export function updateDsFun(data: {}) {
  return request({
    url: '/system/ds-fun',
    method: 'put',
    data: data
  });
}


// 删除业务对象查询配置
export function delDsFun(dsFunId: string | number | Array<string | number>) {
  return request({
    url: '/system/ds-fun/' + dsFunId,
    method: 'delete'
  });
}
