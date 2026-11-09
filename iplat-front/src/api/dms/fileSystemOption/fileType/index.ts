import request from '@/utils/request';
import { AxiosPromise } from 'axios';

// 查询模板编制列表
export function documentList(query: {}): AxiosPromise<[]> {
  return request({
    url: '/dms/document-type-maintenance/list',
    method: 'get',
    params: query
  });
}
// 新增
export function addDocument(query: {}): AxiosPromise<[]> {
  return request({
    url: '/dms/document-type-maintenance',
    method: 'post',
    data: query
  });
}
export function editTemplate(query: {}): AxiosPromise<[]> {
  return request({
    url: '/dms/document-type-maintenance',
    method: 'put',
    data: query
  });
}

export function deleteTemplate(id): AxiosPromise<[]> {
  return request({
    url: `/dms/document-type-maintenance/${id}`,
    method: 'delete',
  });
}

export function enableTemplate(id): AxiosPromise<[]> {
  return request({
    url: `/dms/document-type-maintenance/enable/${id}`,
    method: 'put'
  });
}

export function disableTemplate(id): AxiosPromise<[]> {
  return request({
    url: `/dms/document-type-maintenance/disable/${id}`,
    method: 'put'
  });
}

export function getAttributeList(query: {}) {
  return request({
    url: '/dms/document-type-attribute-config/list',
    method: 'get',
    params: query
  });
}

export function addAttribute(query: {}) {
  return request({
    url: '/dms/document-type-attribute-config',
    method: 'post',
    data: query
  });
}
export function editAttribute(query: {}) {
  return request({
    url: '/dms/document-type-attribute-config',
    method: 'put',
    data: query
  });
}

export function enableAttribute(id): AxiosPromise<[]> {
  return request({
    url: `/dms/document-type-attribute-config/enable/${id}`,
    method: 'put'
  });
}

export function disableAttribute(id): AxiosPromise<[]> {
  return request({
    url: `/dms/document-type-attribute-config/disable/${id}`,
    method: 'put'
  });
}

export function deleteAttribute(id): AxiosPromise<[]> {
  return request({
    url: `/dms/document-type-attribute-config/${id}`,
    method: 'delete'
  });
}
