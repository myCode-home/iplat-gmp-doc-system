import request from '@/utils/request';
import { OssQuery, OssVO } from './types';
import { AxiosPromise } from 'axios';

// 查询OSS对象存储列表
export function listOss(query: OssQuery): AxiosPromise<OssVO[]> {
  return request({
    url: '/resource/oss/list',
    method: 'get',
    params: query
  });
}

// 查询OSS对象基于id串
export function listByIds(ossId: string | number): AxiosPromise<OssVO[]> {
  return request({
    url: '/resource/oss/listByIds/' + ossId,
    method: 'get'
  });
}

// 删除OSS对象存储
export function delOss(ossId: string | number | Array<string | number>) {
  return request({
    url: '/resource/oss/' + ossId,
    method: 'delete'
  });
}

// 查询OSS对象基于业务编号和业务类型
export function listByBizIdAndType(bizId: string | number, bizType?: string | number, query = {}) {
  return request({
    url: '/resource/oss/list/' + bizId + '/' + bizType,
    method: 'get',
    params: query,
  })
}

// 删除OSS对象存储
export function delOssByBizIdAndType(bizId: string | number, bizType?: string | number, query = {}) {
  return request({
    url: '/resource/oss/biz/' + bizId + '/' + bizType,
    method: 'delete',
    params: query,
  })
}

// 新增对象存储和业务编号
export function uploadOssBizData(data) {
  return request({
    url: '/resource/oss/biz',
    method: 'post',
    data: data
  })
}

// 合并文件
export function mergeFile(data, sync='N') {
  return request({
    url: '/resource/oss/chunk/merge-to-oss',
    method: 'post',
    data: data,
    params: {
      sync
    }
  })
}

// 合并文件
export function downloadFile(query: {}) {
  return request({
    url: '/resource/oss/bizs/download',
    method: 'get',
    params: query,
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
    responseType: 'blob',
  })
}