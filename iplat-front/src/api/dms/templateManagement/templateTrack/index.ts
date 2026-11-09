import request from '@/utils/request';
import { AxiosPromise } from 'axios';

// 查询模板跟踪列表
export function listwithsnapshot(query: {}): AxiosPromise<[]> {
  return request({
    url: '/dms/tmplPreparation/listwithsnapshot',
    method: 'get',
    params: query
  });
}

// 查询模板跟踪详细
export function getTemplateApproval(templateId: string | number): AxiosPromise<[]> {
  return request({
    url: '/dms/tmplPreparation/' + templateId,
    method: 'get'
  });
}

// 新增模板跟踪
export function addTemplateApproval(data: {}) {
  return request({
    url: '/dms/tmplPreparation',
    method: 'post',
    data: data
  });
}

// 修改模板跟踪
export function updateTemplateApproval(data: {}) {
  return request({
    url: '/dms/tmplPreparation',
    method: 'put',
    data: data
  });
}

// 单个工作流提交
export function submitAndFlowStart(data: {}) {
  return request({
    url: '/dms/tmplPreparation/submitAndFlowStart',
    method: 'post',
    data: data
  });
}


// 删除模板跟踪
export function delTemplateApproval(templateId: string | number | Array<string | number>) {
  return request({
    url: '/dms/tmplPreparation/' + templateId,
    method: 'delete'
  });
}
