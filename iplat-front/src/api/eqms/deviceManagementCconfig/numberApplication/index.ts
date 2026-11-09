import request from '@/utils/request';
import { AxiosPromise } from 'axios';
// 查询验收单列表
export function listPreApply(query: {}) {
  return request({
    url: '/eqms/device_acceptance/pre_apply',
    method: 'get',
    params: query
  });
}

// 查询验收单详细
export function getPreApply(id: string | number) {
  return request({
    url: '/eqms/device_acceptance/pre_apply/' + id,
    method: 'get'
  });
}

// 新增验收单
export function addPreApply(data: {}) {
  return request({
    url: '/eqms/device_acceptance/pre_apply',
    method: 'post',
    data: data
  });
}

// 修改验收单
export function updatePreApply(data: {}) {
  return request({
    url: '/eqms/device_acceptance/pre_apply',
    method: 'put',
    data: data
  });
}

// 删除验收单
export function delPreApply(ids: string | number | Array<string | number>) {
  return request({
    url: '/eqms/device_acceptance/pre_apply/' + ids,
    method: 'delete'
  });
}
