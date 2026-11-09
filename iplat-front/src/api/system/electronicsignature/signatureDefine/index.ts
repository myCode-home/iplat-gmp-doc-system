import request from '@/utils/request';
import { AxiosPromise } from 'axios';

// 查询电子签名定义列表
export function listSignatureDefine(query: {}): AxiosPromise<[]> {
  return request({
    url: '/system/signature-definition/list',
    method: 'get',
    params: query
  });
}

// 查询电子签名定义详细
export function getSignatureDefine(signatureDefinitionId: string | number): AxiosPromise<[]> {
  return request({
    url: '/system/signature-definition/' + signatureDefinitionId,
    method: 'get'
  });
}

// 新增电子签名定义
export function addSignatureDefine(data: {}) {
  return request({
    url: '/system/signature-definition',
    method: 'post',
    data: data
  });
}

// 修改电子签名定义
export function updateSignatureDefine(data: {}) {
  return request({
    url: '/system/signature-definition',
    method: 'put',
    data: data
  });
}


// 删除电子签名定义
export function delSignatureDefine(signatureDefinitionId: string | number | Array<string | number>) {
  return request({
    url: '/system/signature-definition/' + signatureDefinitionId,
    method: 'delete'
  });
}

// 申请一个新的电子签名
export function newSignature(data: {}) {
  return request({
    url: '/system/signature-definition/new-signature',
    // headers: {
    //   isEncrypt: true
    // },
    method: 'post',
    data: data
  });
}

// 获取 指定 URI 和方法的电子签名定义的组合接口，该方法会根据电子签名定义的接口
export function combination(query: {}) {
  return request({
    url: '/system/signature-definition/combination',
    method: 'get',
    params: query
  });
}