import request from '@/utils/request';

// 查询OSS对象存储对象类型列表
export function ossTypeList(query: {}) {
  return request({
    url: '/resource/oss-type/list',
    method: 'get',
    params: query
  })
}

// 查询OSS对象存储对象类型列表-不分页
export function ossTypeListAll(query: {}) {
  return request({
    url: '/resource/oss-type/list/all',
    method: 'get',
    params: query
  })
}

// 获取OSS对象存储对象类型详细信息
export function getOssType(id) {
  return request({
    url: `/resource/oss-type/${id}`,
    method: 'get'
  })
}

// 新增OSS对象存储对象类型
export function addOssType(data: {}) {
  return request({
    url: '/resource/oss-type',
    method: 'post',
    data: data
  })
}

// 修改OSS对象存储对象类型
export function uploadOssType(data: {}) {
  return request({
    url: '/resource/oss-type',
    method: 'put',
    data: data
  })
}

// 删除OSS对象存储对象类型
export function deleteOssType(ids, query: {}) {
  return request({
    url: `/resource/oss-type/${ids}`,
    method: 'delete',
    params: query
  })
}