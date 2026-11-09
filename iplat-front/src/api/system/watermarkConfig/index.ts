import request from '@/utils/request';
import { AxiosPromise } from 'axios';
// 查询水印号配置列表
export function listWatermark(query: {}) {
  return request({
    url: '/dms/watermark/list',
    method: 'get',
    params: query
  });
}

// 根据水印号ID查询水印号属性配置列表
export function listAttrWatermark(id) {
  return request({
    url: `/dms//watermark/attribute/list/${id}`,
    method: 'get'
  });
}

// 查询水印号配置详细
export function getWatermark(templateId: string | number) {
  return request({
    url: '/dms/watermark/' + templateId,
    method: 'get'
  });
}

// 获取水印号属性配置详细信息
export function getAttrWatermark(id) {
  return request({
    url: `/dms/watermark/attribute/${id}`,
    method: 'get'
  });
}

// 新增水印号配置
export function addWatermark(data: {}) {
  return request({
    url: '/dms/watermark',
    method: 'post',
    data: data
  });
}

// 保存水印号属性配置
export function addAttrWatermark(data: {}) {
  return request({
    url: '/dms/watermark/attribute/save',
    method: 'post',
    data: data
  });
}

// 修改水印号配置
export function updateWatermark(data: {}) {
  return request({
    url: '/dms/watermark',
    method: 'put',
    data: data
  });
}

// 删除水印号配置
export function delWatermark(id: string | number | Array<string | number>) {
  return request({
    url: '/dms/watermark/' + id,
    method: 'delete'
  });
}

// 删除水印号属性配置
export function delAttrWatermark(id: string | number | Array<string | number>) {
  return request({
    url: '/dms/watermark/attribute/' + id,
    method: 'delete'
  });
}

// 水印号配置pdf预览
export function viewAttrWatermark(query: {}) {
  return request({
    url: '/dms/watermark/attribute/preview',
    method: 'get',
    params: query,
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
    responseType: 'blob',
  });
}

// 主配置水印号pdf预览
export function viewWatermark(id) {
  return request({
    url: `/dms/watermark/preview/${id}`,
    method: 'get',
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
    responseType: 'blob',
  });
}
