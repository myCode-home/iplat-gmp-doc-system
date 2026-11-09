import request from '@/utils/request';
import { AxiosPromise } from 'axios';

// 查询培训确认列表
export function listTrainConfirm(query: {}): AxiosPromise<[]> {
  return request({
    url: '/dms/train-confirm/unTrainList',
    method: 'get',
    params: query
  });
}

// 查询已培训列表
export function trainedListTrainConfirm(query: {}): AxiosPromise<[]> {
  return request({
    url: '/dms/train-confirm/trainedList',
    method: 'get',
    params: query
  });
}

// 查询培训确认详细
export function getTrainConfirm(DocCreateId: string | number, query?: {}): AxiosPromise<[]> {
  return request({
    url: '/dms/doc-create/' + DocCreateId,
    method: 'get',
    params: query
  });
}

// 新增培训确认
export function addTrainConfirm(data: {}) {
  return request({
    url: '/dms/doc-create',
    method: 'post',
    data: data
  });
}

// 修改培训确认
export function updateTrainConfirm(data: {}) {
  return request({
    url: '/dms/doc-create',
    method: 'put',
    data: data
  });
}


// 删除培训确认
export function delTrainConfirm(DocCreateId: string | number | Array<string | number>) {
  return request({
    url: '/dms/doc-create/' + DocCreateId,
    method: 'delete'
  });
}

// 查询文件下的培训记录
export function trainingRecordList(id: string | number, query?: {}): AxiosPromise<[]> {
  return request({
    url: `/dms/training-record/trainingRecordList/${id}`,
    method: 'get',
    params: query
  });
}

// 批量培训确认
export function mulTrainConfirm(data: {}) {
  return request({
    url: '/dms/training-record/trainConfirm',
    method: 'put',
    data: data
  });
}

// 标记需要培训
export function trainingNeedTrain(id: string | number, data?: {}): AxiosPromise<[]> {
  return request({
    url: `/dms/train-confirm/needTrain/${id}`,
    method: 'put',
    data: data
  });
}

// 标记不需要培训
export function trainingNoNeedTrain(id: string | number, data?: {}): AxiosPromise<[]> {
  return request({
    url: `/dms/train-confirm/noNeedTrain/${id}`,
    method: 'put',
    data: data
  });
}

// 文件生效
export function trainingActive(data?: {}): AxiosPromise<[]> {
  return request({
    url: `/dms/train-confirm/active`,
    method: 'post',
    data: data
  });
}

// 查询待生效文件列表
export function unActiveList(query: {}): AxiosPromise<[]> {
  return request({
    url: '/dms/train-confirm/unActiveList',
    method: 'get',
    params: query
  });
}

// 查询培训跟踪列表
export function trainTrackList(query: {}): AxiosPromise<[]> {
  return request({
    url: '/dms/train-confirm/trainTrackList',
    method: 'get',
    params: query
  });
}