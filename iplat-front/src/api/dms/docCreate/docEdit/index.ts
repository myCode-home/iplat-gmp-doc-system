import request from '@/utils/request';
import { AxiosPromise } from 'axios';

// 查询文档编制列表
export function listDocCreateEdit(query: {}): AxiosPromise<[]> {
  return request({
    url: '/dms/doc-create/editList',
    method: 'get',
    params: query
  });
}

// 查询模板审批列表
export function listDocCreateApproval(query: {}): AxiosPromise<[]> {
  return request({
    url: '/dms/doc-create/approvalList',
    method: 'get',
    params: query
  });
}

// 查询模板跟踪列表
export function listDocCreate(query: {}): AxiosPromise<[]> {
  return request({
    url: '/dms/doc-create/list',
    method: 'get',
    params: query
  });
}

// 查询文档编制详细
export function getDocCreateEdit(DocCreateId: string | number, query?: {}): AxiosPromise<[]> {
  return request({
    url: '/dms/doc-create/' + DocCreateId,
    method: 'get',
    params: query
  });
}

// 获取文档编制ossId
export function getDocCreateFile(id: string | number): AxiosPromise<[]> {
  return request({
    url: `/dms/doc-create/file/${id}`,
    method: 'get'
  });
}

// 新增文档编制
export function addDocCreateEdit(data: {}) {
  return request({
    url: '/dms/doc-create',
    method: 'post',
    data: data
  });
}

// 修改文档编制
export function updateDocCreateEdit(data: {}) {
  return request({
    url: '/dms/doc-create',
    method: 'put',
    data: data
  });
}

// 单个工作流提交
export function submitAndFlowStart(data: {}) {
  return request({
    url: '/dms/doc-create/submitAndFlowStart',
    method: 'post',
    data: data
  });
}

// 离线文件上传
export function submitOfflineFile(id, data) {
  return request({
    url: `/dms/doc-create/offline/upload/${id}`,
    method: 'post',
    headers: {
      'Content-Type': 'multipart/form-data'
    },
    data: data
  });
}


// 删除文档编制
export function delDocCreateEdit(DocCreateId: string | number | Array<string | number>) {
  return request({
    url: '/dms/doc-create/' + DocCreateId,
    method: 'delete'
  });
}

// 查询替代文件列表
export function listDocCreateSupersede(query: {}): AxiosPromise<[]> {
  return request({
    url: '/dms/doc-create-supersede/list',
    method: 'get',
    params: query
  });
}

// 查询关联文件列表
export function listDocCreateLink(query: {}): AxiosPromise<[]> {
  return request({
    url: '/dms/doc-create-link/list',
    method: 'get',
    params: query
  });
}
