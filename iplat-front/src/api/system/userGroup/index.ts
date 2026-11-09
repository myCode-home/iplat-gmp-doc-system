import request from '@/utils/request';
import { AxiosPromise } from 'axios';

// 查询用户组列表
export function listUserGroupId(query: {}): AxiosPromise<[]> {
  return request({
    url: '/system/user-group/list',
    method: 'get',
    params: query
  });
}

// 查询用户组详细
export function getUserGroupId(userGroupId: string | number): AxiosPromise<[]> {
  return request({
    url: '/system/user-group/' + userGroupId,
    method: 'get'
  });
}

// 新增用户组
export function addUserGroupId(data: {}) {
  return request({
    url: '/system/user-group',
    method: 'post',
    data: data
  });
}

// 修改用户组
export function updateUserGroupId(data: {}) {
  return request({
    url: '/system/user-group',
    method: 'put',
    data: data
  });
}


// 删除用户组
export function delUserGroupId(userGroupId: string | number | Array<string | number>) {
  return request({
    url: '/system/user-group/' + userGroupId,
    method: 'delete'
  });
}

// 查询角色已授权用户列表
export function allocatedUserList(query) {
  return request({
    url: '/system/user-group/authUser/allocatedList',
    method: 'get',
    params: query
  })
}

// 查询用户组未授权用户列表
export function unallocatedUserList(query) {
  return request({
    url: '/system/user-group/authUser/unallocatedList',
    method: 'get',
    params: query
  })
}

// 授权用户选择
export function authUserSelectAll(data) {
  return request({
    url: '/system/user-group/authUser/selectAll',
    method: 'put',
    params: data
  })
}

// 取消用户授权角色
export function authUserCancel(data) {
  return request({
    url: '/system/user-group/authUser/cancel',
    method: 'put',
    data: data
  })
}

// 批量取消用户授权角色
export function authUserCancelAll(data = {}) {
  return request({
    url: '/system/user-group/authUser/cancelAll',
    method: 'put',
    params: data
  })
}

// 通过用户组ID查询用户
export function getUserListByGroupIds(userGroupIds): AxiosPromise<[]> {
  return request({
    url: `/system/user/user-group/user/${userGroupIds}`,
    method: 'get'
  });
}