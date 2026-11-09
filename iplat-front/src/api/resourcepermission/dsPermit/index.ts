import request from '@/utils/request';
import { AxiosPromise } from 'axios';

// 查询业务对象列表
export function listDsPermit(query: {}): AxiosPromise<[]> {
  return request({
    url: '/system/ds-permit/list',
    method: 'get',
    params: query
  });
}

// 查询业务对象详细
export function getDsPermit(businessId: string | number): AxiosPromise<[]> {
  return request({
    url: '/system/ds-permit/' + businessId,
    method: 'get'
  });
}

// 新增业务对象
export function addDsPermit(data: {}) {
  return request({
    url: '/system/ds-permit',
    method: 'post',
    data: data
  });
}

// 修改业务对象
export function updateDsPermit(data: {}) {
  return request({
    url: '/system/ds-permit',
    method: 'put',
    data: data
  });
}


// 删除业务对象
export function delDsPermit(businessId: string | number | Array<string | number>) {
  return request({
    url: '/system/ds-permit/' + businessId,
    method: 'delete'
  });
}

// 刷新缓存
export function refreshDsPermit() {
  return request({
    url: '/system/ds-permit/refresh/cache',
    method: 'put'
  });
}
