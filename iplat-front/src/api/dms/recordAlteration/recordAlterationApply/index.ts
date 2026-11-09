import request from '@/utils/request';
import { AxiosPromise } from 'axios';

// 查询记录变更申请列表
export function listRecordAlterationApply(query: {}): AxiosPromise<[]> {
  return request({
    url: '/dms/doc-record-change-order/editList',
    method: 'get',
    params: query
  });
}

// 查询记录变更申请审批列表
export function listRecordAlterationApproval(query: {}): AxiosPromise<[]> {
  return request({
    url: '/dms/doc-record-change-order/approvalList',
    method: 'get',
    params: query
  });
}

// 查询记录变更申请跟踪列表
export function listRecordAlteration(query: {}): AxiosPromise<[]> {
  return request({
    url: '/dms/doc-record-change-order/list',
    method: 'get',
    params: query
  });
}

// 查询记录变更申请详细
export function getRecordAlterationApply(RecordAlterationId: string | number): AxiosPromise<[]> {
  return request({
    url: '/dms/doc-record-change-order/' + RecordAlterationId,
    method: 'get'
  });
}

// 获取记录变更申请ossId
export function getRecordAlterationFile(id: string | number): AxiosPromise<[]> {
  return request({
    url: `/dms/doc-record-change-order/file/${id}`,
    method: 'get'
  });
}

// 新增记录变更申请
export function addRecordAlterationApply(data: {}) {
  return request({
    url: '/dms/doc-record-change-order',
    method: 'post',
    data: data
  });
}

// 修改记录变更申请
export function updateRecordAlterationApply(data: {}) {
  return request({
    url: '/dms/doc-record-change-order',
    method: 'put',
    data: data
  });
}

// 单个工作流提交
export function submitAndFlowStart(data: {}) {
  return request({
    url: '/dms/doc-record-change-order/submitAndFlowStart',
    method: 'post',
    data: data
  });
}


// 删除记录变更申请
export function delRecordAlterationApply(RecordAlterationId: string | number | Array<string | number>) {
  return request({
    url: '/dms/doc-record-change-order/' + RecordAlterationId,
    method: 'delete'
  });
}

