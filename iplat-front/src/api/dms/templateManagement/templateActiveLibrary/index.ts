import request from '@/utils/request';
import { AxiosPromise } from 'axios';

// 查询生效模板列表
export function listTmplLibActive(query: {}): AxiosPromise<[]> {
  return request({
    url: '/dms/tmplLib/active/list',
    method: 'get',
    params: query
  });
}

// 查询失效模板列表
export function listTmplLib(query: {}): AxiosPromise<[]> {
  return request({
    url: '/dms/tmplLib/history/list',
    method: 'get',
    params: query
  });
}

// 查询模板编制详细
export function getTmplLib(TmplLibId: string | number): AxiosPromise<[]> {
  return request({
    url: '/dms/tmplLib/' + TmplLibId,
    method: 'get'
  });
}

// 新增模板编制
export function addTmplLib(data: {}) {
  return request({
    url: '/dms/tmplLib',
    method: 'post',
    data: data
  });
}

// 修改模板编制
export function updateTmplLib(data: {}) {
  return request({
    url: '/dms/tmplLib',
    method: 'put',
    data: data
  });
}

// 删除模板编制
export function delTmplLib(TmplLibId: string | number | Array<string | number>) {
  return request({
    url: '/dms/tmplLib/' + TmplLibId,
    method: 'delete'
  });
}

// 申请模板修订
export function reviseTmplLib(data: {}) {
  return request({
    url: `/dms/tmplLib/revise`,
    method: 'post',
    data: data
  });
}

// 申请模板失效
export function invalidTmplLib(data: {}) {
  return request({
    url: `/dms/tmplLib/invalid`,
    method: 'post',
    data: data
  });
}
