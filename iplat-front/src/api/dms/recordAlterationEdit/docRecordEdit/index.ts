import request from '@/utils/request';
import { AxiosPromise } from 'axios';

// 查询文档记录编制列表
export function listDocRecordEdit(query: {}): AxiosPromise<[]> {
  return request({
    url: '/dms/doc-record-create/editList',
    method: 'get',
    params: query
  });
}

// 查询记录编制审批列表
export function listDocRecordApproval(query: {}): AxiosPromise<[]> {
  return request({
    url: '/dms/doc-record-create/approvalList',
    method: 'get',
    params: query
  });
}

// 查询记录编制跟踪列表
export function listDocRecord(query: {}): AxiosPromise<[]> {
  return request({
    url: '/dms/doc-record-create/list',
    method: 'get',
    params: query
  });
}

// 查询文档创建记录列表
export function listBuildNewList(query: {}): AxiosPromise<[]> {
  return request({
    url: '/dms/doc-record-create/buildNewList',
    method: 'get',
    params: query
  });
}

// 查询文档记录编制详细
export function getDocRecord(templateId: string | number): AxiosPromise<[]> {
  return request({
    url: '/dms/doc-record-create/' + templateId,
    method: 'get'
  });
}

// 获取文档记录编制ossId
export function getDocRecordFile(id: string | number): AxiosPromise<[]> {
  return request({
    url: `/dms/doc-record-create/file/${id}`,
    method: 'get'
  });
}

// 新增文档记录编制
export function addDocRecord(data: {}) {
  return request({
    url: '/dms/doc-record-create',
    method: 'post',
    data: data
  });
}

// 文档创建 新增记录
export function insertFromDocCreate(data: {}) {
  return request({
    url: '/dms/doc-record-create/insert/fromDocCreate',
    method: 'post',
    data: data
  });
}

// 判断记录编制名称是否重名
export function judgeRecordNameUnique(data: {}) {
  return request({
    url: '/dms/doc-record-create/judgeRecordNameUnique',
    method: 'post',
    data: data
  });
}

// 修改文档记录编制
export function updateDocRecord(data: {}) {
  return request({
    url: '/dms/doc-record-create',
    method: 'put',
    data: data
  });
}

// 单个工作流提交
export function submitAndFlowStart(data: {}) {
  return request({
    url: '/dms/doc-record-create/submitAndFlowStart',
    method: 'post',
    data: data
  });
}


// 删除文档记录编制
export function delDocRecord(templateId: string | number | Array<string | number>) {
  return request({
    url: '/dms/doc-record-create/' + templateId,
    method: 'delete'
  });
}

// 离线文件上传
export function submitOfflineFile(id, data) {
  return request({
    url: `/dms/doc-record-create/offline/upload/${id}`,
    method: 'post',
    headers: {
      'Content-Type': 'multipart/form-data'
    },
    data: data
  });
}

// 查询记录单独修订快照列表
export function listSnapshot(query: {}): AxiosPromise<[]> {
  return request({
    url: '/dms/doc-record-snapshot/list',
    method: 'get',
    params: query
  });
}

// 失效记录申请
export function recordFailure(id: string | number): AxiosPromise<[]> {
  return request({
    url: `/dms/doc-record-create/failure/${id}`,
    method: 'put'
  });
}

// 记录取消失效
export function cancelRecordFailure(id: string | number): AxiosPromise<[]> {
  return request({
    url: `/dms/doc-record-create/cancel/failure/${id}`,
    method: 'put'
  });
}

