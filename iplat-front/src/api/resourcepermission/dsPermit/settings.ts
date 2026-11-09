import request from '@/utils/request';
import { AxiosPromise } from 'axios';

// 查询数据集授权配置列表
export function listSubject(query: {}): AxiosPromise<[]> {
  return request({
    url: '/system/ds-permit-subject/list',
    method: 'get',
    params: query
  });
}

// 根据数据集授权编号查询数据集授权配置类型列表
export function listSubjectById(dsPermitId): AxiosPromise<[]> {
  return request({
    url: `/system/ds-permit-subject/list/${dsPermitId}`,
    method: 'get'
  });
}

// 查询数据集授权配置详细
export function getSubject(dsPermitSubjectId: string | number): AxiosPromise<[]> {
  return request({
    url: '/system/ds-permit-subject/' + dsPermitSubjectId,
    method: 'get'
  });
}

// 新增数据集授权配置
export function addSubject(data: {}) {
  return request({
    url: '/system/ds-permit-subject',
    method: 'post',
    data: data
  });
}

// 修改数据集授权配置
export function updateSubject(data: {}) {
  return request({
    url: '/system/ds-permit-subject',
    method: 'put',
    data: data
  });
}


// 删除数据集授权配置
export function delSubject(dsPermitSubjectId: string | number | Array<string | number>) {
  return request({
    url: '/system/ds-permit-subject/' + dsPermitSubjectId,
    method: 'delete'
  });
}
