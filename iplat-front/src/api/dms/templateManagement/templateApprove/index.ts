import request from '@/utils/request';
import { AxiosPromise } from 'axios';

// 查询模板编制列表
export function listTemplateApproval(query: {}): AxiosPromise<[]> {
  return request({
    url: '/dms/tmplPreparation/approval/list',
    method: 'get',
    params: query
  });
}

// 查询模板编制详细
export function getTemplateApproval(templateId: string | number): AxiosPromise<[]> {
  return request({
    url: '/dms/tmplPreparation/' + templateId,
    method: 'get'
  });
}

// 新增模板编制
export function addTemplateApproval(data: {}) {
  return request({
    url: '/dms/tmplPreparation',
    method: 'post',
    data: data
  });
}

// 修改模板编制
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


// 删除模板编制
export function delTemplateApproval(templateId: string | number | Array<string | number>) {
  return request({
    url: '/dms/tmplPreparation/' + templateId,
    method: 'delete'
  });
}
