import request from '@/utils/request';
import { AxiosPromise } from 'axios';

// 查询消息模板列表
export function listMsgTemplate(query: {}): AxiosPromise<[]> {
  return request({
    url: '/resource/msgTemplate/list',
    method: 'get',
    params: query
  });
}

// 查询消息模板详细
export function getMsgTemplate(msgTemplateId: string | number): AxiosPromise<[]> {
  return request({
    url: '/resource/msgTemplate/' + msgTemplateId,
    method: 'get'
  });
}

// 新增消息模板
export function addMsgTemplate(data: {}) {
  return request({
    url: '/resource/msgTemplate',
    method: 'post',
    data: data
  });
}

// 修改消息模板
export function updateMsgTemplate(data: {}) {
  return request({
    url: '/resource/msgTemplate',
    method: 'put',
    data: data
  });
}


// 删除消息模板
export function delMsgTemplate(msgTemplateId: string | number | Array<string | number>) {
  return request({
    url: '/resource/msgTemplate/' + msgTemplateId,
    method: 'delete'
  });
}

// 模板状态修改
export function changeMsgTemplateStatus(msgTemplateId: string | number, msgTemplateCode: string | number, msgTemplateEnable: string | number) {
  return request({
    url: '/resource/msgTemplate/changeStatus',
    method: 'put',
    data: {
      msgTemplateId,
      msgTemplateCode,
      msgTemplateEnable
    }
  });
}

// 根据模板编号获取关联用户
export function queryTemplateUserByMsgTemplateId(msgTemplateId: string | number) {
  return request({
    url: '/resource/msgTemplate/user/' + msgTemplateId,
    method: 'get'
  });
}

// 关联用户
export function saveMsgTemplateUser(msgTemplateId: string | number, data: string | number) {
  return request({
    url: '/resource/msgTemplate/user/' + msgTemplateId,
    method: 'put',
    data: data
  });
}
