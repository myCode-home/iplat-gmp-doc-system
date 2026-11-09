import request from '@/utils/request';
import { AxiosPromise } from 'axios';

// 查询审计追踪RUL列表
export function listAuditTrackUri(query: {}): AxiosPromise<[]> {
  return request({
    url: '/system/audit-uri/list',
    method: 'get',
    params: query
  });
}

// 查询url列表
export function listModulUri(module): AxiosPromise<[]> {
  return request({
    url: `/${module}/uri-resource/list`,
    method: 'get'
  });
}

// 查询审计追踪RUL详细
export function getAuditTrackUri(auditUriId: string | number): AxiosPromise<[]> {
  return request({
    url: '/system/audit-uri/' + auditUriId,
    method: 'get'
  });
}

// 新增审计追踪RUL
export function addAuditTrackUri(data: {}) {
  return request({
    url: '/system/audit-uri',
    method: 'post',
    data: data
  });
}

// 修改审计追踪RUL
export function updateAuditTrackUri(data: {}) {
  return request({
    url: '/system/audit-uri',
    method: 'put',
    data: data
  });
}


// 删除审计追踪RUL
export function delAuditTrackUri(auditUriId: string | number | Array<string | number>) {
  return request({
    url: '/system/audit-uri/' + auditUriId,
    method: 'delete'
  });
}