import request from '@/utils/request';
import { AxiosPromise } from 'axios';

// 获取Excel文件列表-生效注册页面
export function listActiveExcel(query?: {}): AxiosPromise<[]> {
  return request({
    url: '/dms/volumes-print-pending-task/excel-list/active',
    method: 'get',
    params: query
  });
}

// 获取Excel文件列表-待生效注册页面
export function listInActiveExcel(query?: {}): AxiosPromise<[]> {
  return request({
    url: '/dms/volumes-print-pending-task/excel-list/inactive',
    method: 'get',
    params: query
  });
}

// 新增成册打印-待打印Excel信息
export function addBookPrintTask(data: {}) {
  return request({
    url: '/dms/volumes-print-pending-task/batch',
    method: 'post',
    data: data
  });
}

// 刷新Excel文件中单条数据
export function taskRefresh(query: {}) {
  return request({
    url: '/dms/volumes-print-pending-task/refresh',
    method: 'post',
    params: query
  });
}

// 查询指定的Excel对应的待打印任务（包括手工提记录）
export function listManualTask(excelName, query?: {}): AxiosPromise<[]> {
  return request({
    url: `/dms/volumes-print-pending-task/list/manual/${excelName}`,
    method: 'get',
    params: query
  });
}

// 获取记录类型下拉框
export function listDocType(excelName, query?: {}) {
  return request({
    url: `/dms/volumes-print-pending-task/drop-down/doc-type/${excelName}`,
    method: 'get',
    params: query
  });
}

// 获取记录类型下拉框
export function listControlledInfo(excelName, query?: {}) {
  return request({
    url: `/dms/volumes-print-pending-task/drop-down/controlled-info/${excelName}`,
    method: 'get',
    params: query
  });
}

// 待打印任务-生效文件接受并成册
export function acceptActive(data: Array<string | number>) {
  return request({
    url: '/dms/volumes-print-pending-task/accept/active',
    method: 'put',
    data: data
  });
}

// 待生效成册
export function acceptUnactive(data: Array<string | number>) {
  return request({
    url: '/dms/volumes-print-pending-task/accept/inactive',
    method: 'put',
    data: data
  });
}

// 待打印任务-待生效文件接受并成册
export function taskReject(data: Array<string | number>) {
  return request({
    url: '/dms/volumes-print-pending-task/accept/inactive',
    method: 'put',
    data: data
  });
}
