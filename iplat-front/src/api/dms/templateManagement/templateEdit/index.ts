import request from '@/utils/request';
import { AxiosPromise } from 'axios';

// 查询模板编制列表
export function listTemplateEdit(query: {}): AxiosPromise<[]> {
  return request({
    url: '/dms/tmplPreparation/edit/list',
    method: 'get',
    params: query
  });
}

// 查询模板审批列表
export function listTemplateApproval(query: {}): AxiosPromise<[]> {
  return request({
    url: '/dms/tmplPreparation/approval/list',
    method: 'get',
    params: query
  });
}

// 查询模板跟踪列表
export function listTemplate(query: {}): AxiosPromise<[]> {
  return request({
    url: '/dms/tmplPreparation/list',
    method: 'get',
    params: query
  });
}

// 查询模板编制详细
export function getTemplateEdit(templateId: string | number): AxiosPromise<[]> {
  return request({
    url: '/dms/tmplPreparation/' + templateId,
    method: 'get'
  });
}

// 获取模板编制ossId
export function getTemplateFile(id: string | number): AxiosPromise<[]> {
  return request({
    url: `/dms/tmplPreparation/file/${id}`,
    method: 'get'
  });
}

// 新增模板编制
export function addTemplateEdit(data: {}) {
  return request({
    url: '/dms/tmplPreparation',
    method: 'post',
    data: data
  });
}

// 修改模板编制
export function updateTemplateEdit(data: {}) {
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


// 删除模板编制
export function delTemplateEdit(templateId: string | number | Array<string | number>) {
  return request({
    url: '/dms/tmplPreparation/' + templateId,
    method: 'delete'
  });
}

