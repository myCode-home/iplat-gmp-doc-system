import request from '@/utils/request';
import { AxiosPromise } from 'axios';

// 查询电子签名配置列表
export function listSignatureConfig(query: {}): AxiosPromise<[]> {
  return request({
    url: '/system/signature-config/list',
    method: 'get',
    params: query
  });
}

// 查询电子签名配置详细
export function getSignatureConfig(signatureConfigId: string | number): AxiosPromise<[]> {
  return request({
    url: '/system/signature-config/' + signatureConfigId,
    method: 'get'
  });
}

// 新增电子签名配置
export function addSignatureConfig(data: {}) {
  return request({
    url: '/system/signature-config',
    method: 'post',
    data: data
  });
}

// 修改电子签名配置
export function updateSignatureConfig(data: {}) {
  return request({
    url: '/system/signature-config',
    method: 'put',
    data: data
  });
}


// 删除电子签名配置
export function delSignatureConfig(signatureConfigId: string | number | Array<string | number>) {
  return request({
    url: '/system/signature-config/' + signatureConfigId,
    method: 'delete'
  });
}