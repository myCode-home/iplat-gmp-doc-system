import request from '@/utils/request';
import { AxiosPromise } from 'axios';
// 查询检查项模板列表
export function listConfirmTemplate(query: {}) {
  return request({
    url: '/eqms/template/confirm/list',
    method: 'get',
    params: query
  });
}

// 查询检查项模板详细
export function getConfirmTemplate(templateId: string | number) {
  return request({
    url: '/eqms/template/confirm/' + templateId,
    method: 'get'
  });
}

// 新增检查项模板
export function addConfirmTemplate(data: {}) {
  return request({
    url: '/eqms/template/confirm',
    method: 'post',
    data: data
  });
}

// 修改检查项模板
export function updateConfirmTemplate(data: {}) {
  return request({
    url: '/eqms/template/confirm',
    method: 'put',
    data: data
  });
}

// 删除检查项模板
export function delConfirmTemplate(templateIds: string | number | Array<string | number>) {
  return request({
    url: '/eqms/template/confirm/' + templateIds,
    method: 'delete'
  });
}
