import request from '@/utils/request';
import { AxiosPromise } from 'axios';

// getPdfList
export function getFileList(query): AxiosPromise<[]> {
  return request({
    url: `/dms/document-lib/list`,
    method: 'get',
    params: query
  });
}

// 查询生效文件列表
export function getActiveFileList(query): AxiosPromise<[]> {
  return request({
    url: `/dms/document-lib/list/active`,
    method: 'get',
    params: query
  });
}

// 查询过期文件
export function getOverdueFileList(query): AxiosPromise<[]> {
  return request({
    url: `/dms/document-lib/list/overdue`,
    method: 'get',
    params: query
  });
}

export function getFileDetail(id): AxiosPromise<[]> {
  return request({
    url: `/dms/document-lib/${id}`,
    method: 'get'
  });
}

export function deleteFile(id): AxiosPromise<[]> {
  return request({
    url: `/dms/document-lib/${id}`,
    method: 'delete'
  });
}


export function addFile(query): AxiosPromise<[]> {
  return request({
    url: `/dms/document-lib`,
    method: 'post',
    data: query
  });
}

export function editFile(query): AxiosPromise<[]> {
  return request({
    url: `/dms/document-lib`,
    method: 'put',
    data: query
  });
}

export function getFileListActive(query): AxiosPromise<[]> {
  return request({
    url: `/dms/document-lib/list/operate-order/active`,
    method: 'get',
    params: query
  });
}

// 获取文件库详细信息 get /document-lib/{id}
export function getFileDetailActive(id): AxiosPromise<[]> {
  return request({
    url: `/dms/document-lib/${id}`,
    method: 'get'
  });
}

// 文档编制页面查询可替代的生效文件
export function getReplaceList(query): AxiosPromise<[]> {
  return request({
    url: `/dms/document-lib/list/replace`,
    method: 'get',
    params: query
  });
}

// 文件编制页面查询可关联的生效文件
export function getRelateList(query): AxiosPromise<[]> {
  return request({
    url: `/dms/document-lib/list/relate`,
    method: 'get',
    params: query
  });
}
// 查询该文件的其他版本信息 get /document-lib/list/ohter/{id} /document-lib/list/ohter/{id}
export function getOtherVersionFileList(id): AxiosPromise<[]> {
  return request({
    url: `/dms/document-lib/list/ohter/${id}`,
    method: 'get'
  });
}

// 文件库关联文件的查询方法 get /document-lib/list/relate/{id}
export function getRelateFileList(id): AxiosPromise<[]> {
  return request({
    url: `/dms/document-lib/list/relate/${id}`,
    method: 'get'
  });
}

// 查询浏览历史文件库 get /dms/doc-browse-history/list
export function getHistoryFileList(query): AxiosPromise<[]> {
  return request({
    url: `/dms/doc-browse-history/list`,
    method: 'get',
    params: query
  });
}

// 修改文件库 put /document-lib
export function updateFileLib(query): AxiosPromise<[]> {
  return request({
    url: `/dms/document-lib`,
    method: 'put',
    data: query
  });
}

// 修订时判断数据状态是否允许被修订 get /document-lib/judge/revise/{id}
export function judgeReviseFileLib(id): AxiosPromise<[]> {
  return request({
    url: `/dms/document-lib/judge/revise/${id}`,
    method: 'get'
  });
}
export interface ExportFileListRequest {
  /**
   * 业务id集合,拼接
   */
  bizIds: string;
  /**
   * 业务(文件)子类型，不必填
   */
  bizSecondType?: string;
  /**
   * 业务(文件)类型
   */
  bizType: string;
  [property: string]: any;
}
// 根据bizId和类型下载，多个时压缩zip下载（文件库导出） get /oss/bizs/download
export function exportFileList(query: ExportFileListRequest): AxiosPromise<[]> {
  return request({
    url: `/dms/oss/bizs/download`,
    method: 'get',
    params: query
  });
}

// 查询历史文件库 get /document-lib/list/history
export function getHistoryFileIndexList(query): AxiosPromise<[]> {
  return request({
    url: `/dms/document-lib/list/history`,
    method: 'get',
    params: query
  });
}

// 新增失效文档 post /doc-invalid-order
export function addInvalidFile(query, es?: string): AxiosPromise<[]> {
  return request({
    url: `/dms/doc-invalid-order`,
    method: 'post',
    data: query,
    headers: {
      'X-Signature': es
    }
  });
}

// 根据文件库主键Id查询记录库 get /document-record-lib/document-lib/list/{dlId}
/**
 *
 * @param path参数： dlId string
 * @param query参数{ .... }
 * @returns
 */
export function getRecordListByFileLibId(dlId, query): AxiosPromise<[]> {
  return request({
    url: `/dms/document-record-lib/document-lib/list/${dlId}`,
    method: 'get',
    params: query
  });
}

// 查询借阅申请生效文件
export function getBorrowActiveList(query): AxiosPromise<[]> {
  return request({
    url: `/dms/document-lib/list/borrow/active`,
    method: 'get',
    params: query
  });
}