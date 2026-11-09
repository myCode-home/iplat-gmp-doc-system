import request from '@/utils/request';

// 查询部门角色信息列表
export function listDeptRole(query) {
  return request({
    url: '/system/dept-role/list',
    method: 'get',
    params: query
  })
}

// 查询部门角色信息详细
export function getDeptRole(deptRoleId) {
  return request({
    url: '/system/dept-role/' + deptRoleId,
    method: 'get'
  })
}

// 新增部门角色信息
export function addDeptRole(data) {
  return request({
    url: '/system/dept-role',
    method: 'post',
    data: data
  })
}

// 修改部门角色信息
export function updateDeptRole(data) {
  return request({
    url: '/system/dept-role',
    method: 'put',
    data: data
  })
}

// 复制部门角色信息
export function copyDeptRole(roleId, data) {
  return request({
    url: '/system/dept-role/copy/' + roleId,
    method: 'post',
    data: data
  })
}

// 删除部门角色信息
export function delDeptRole(deptRoleId) {
  return request({
    url: '/system/dept-role/' + deptRoleId,
    method: 'delete'
  })
}

// 取消用户授权角色
export function authUserCancel(data) {
  return request({
    url: '/system/dept-role/authUser/cancel',
    method: 'put',
    data: data
  })
}

// 批量取消用户授权角色
export function authUserCancelAll(data) {
  return request({
    url: '/system/dept-role/authUser/cancelAll',
    method: 'put',
    params: data
  })
}

// 授权用户选择
export function authUserSelectAll(data) {
  return request({
    url: '/system/dept-role/authUser/selectAll',
    method: 'put',
    params: data
  })
}

// 查询角色已授权用户列表
export function allocatedUserList(query) {
  return request({
    url: '/system/dept-role/authUser/allocatedList',
    method: 'get',
    params: query
  })
}

// 查询角色未授权用户列表
export function unallocatedUserList(query) {
  return request({
    url: '/system/dept-role/authUser/unallocatedList',
    method: 'get',
    params: query
  })
}

// 升级部门角色信息
export function upgradeDeptRole(deptRoleId) {
  return request({
    url: '/system/dept-role/upgrade/' + deptRoleId,
    method: 'post'
  })
}

// 生效部门角色信息
export function validDeptRole(deptRoleId) {
  return request({
    url: '/system/dept-role/valid/' + deptRoleId,
    method: 'put'
  })
}
