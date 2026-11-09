import request from '@/utils/request';
import { AxiosPromise } from 'axios';

// 查询业务对象列表
export function listBusiness(query: {}): AxiosPromise<[]> {
  return request({
    url: '/system/business/list',
    method: 'get',
    params: query
  });
}

// 查询业务对象列表全部
export function listBusinessAll(query?: {}): AxiosPromise<[]> {
  return request({
    url: '/system/business/list/all',
    method: 'get',
    params: query
  });
}

// 查询业务对象详细
export function getBusiness(businessId: string | number): AxiosPromise<[]> {
  return request({
    url: '/system/business/' + businessId,
    method: 'get'
  });
}

// 新增业务对象
export function addBusiness(data: {}) {
  return request({
    url: '/system/business',
    method: 'post',
    data: data
  });
}

// 修改业务对象
export function updateBusiness(data: {}) {
  return request({
    url: '/system/business',
    method: 'put',
    data: data
  });
}


// 删除业务对象
export function delBusiness(businessId: string | number | Array<string | number>) {
  return request({
    url: '/system/business/' + businessId,
    method: 'delete'
  });
}
