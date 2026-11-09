import request from '@/utils/request';
import { AxiosPromise } from 'axios';

// 查询业务对象属性列表
export function listBusinessAttr(query: {}): AxiosPromise<[]> {
  return request({
    url: '/system/business/attr/list',
    method: 'get',
    params: query
  });
}

// 查询业务对象属性详细
export function getBusinessAttr(businessAttrId: string | number): AxiosPromise<[]> {
  return request({
    url: '/system/business/attr/' + businessAttrId,
    method: 'get'
  });
}

// 新增业务对象属性
export function addBusinessAttr(data: {}) {
  return request({
    url: '/system/business/attr',
    method: 'post',
    data: data
  });
}

// 修改业务对象属性
export function updateBusinessAttr(data: {}) {
  return request({
    url: '/system/business/attr',
    method: 'put',
    data: data
  });
}


// 删除业务对象属性
export function delBusinessAttr(businessAttrId: string | number | Array<string | number>) {
  return request({
    url: '/system/business/attr/' + businessAttrId,
    method: 'delete'
  });
}
