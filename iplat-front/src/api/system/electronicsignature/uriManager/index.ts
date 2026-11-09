import request from '@/utils/request';
import { AxiosPromise } from 'axios';

// 查询url维护列表
export function listUriManager(query: {}): AxiosPromise<[]> {
  return request({
    url: '/system/signature-uri/list',
    method: 'get',
    params: query
  });
}

// 查询url维护详细
export function getUriManager(signatureUriId: string | number): AxiosPromise<[]> {
  return request({
    url: '/system/signature-uri/' + signatureUriId,
    method: 'get'
  });
}

// 新增url维护
export function addUriManager(data: {}) {
  return request({
    url: '/system/signature-uri',
    method: 'post',
    data: data
  });
}

// 修改url维护
export function updateUriManager(data: {}) {
  return request({
    url: '/system/signature-uri',
    method: 'put',
    data: data
  });
}


// 删除url维护
export function delUriManager(signatureUriId: string | number | Array<string | number>) {
  return request({
    url: '/system/signature-uri/' + signatureUriId,
    method: 'delete'
  });
}