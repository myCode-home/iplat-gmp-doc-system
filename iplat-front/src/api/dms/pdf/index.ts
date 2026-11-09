import request from '@/utils/request';
import { AxiosPromise } from 'axios';

// getPdfList
export function getPdfStream(id): AxiosPromise<[]> {
  return request({
    url: `/dms/pdf/get/${id}`,
    method: 'get',
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
    responseType: 'blob'
  });
}

// 浏览器打印PDF
export function previewPdf(query: {}): AxiosPromise<[]> {
  return request({
    url: `/dms/print/browser/preview-pdf`,
    method: 'get',
    params: query,
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
    responseType: 'blob'
  });
}

// pageoffice打印PDF
export function previewPageoffice(query: {}): AxiosPromise<[]> {
  return request({
    url: `/dms/print/pageoffice/preview-pdf`,
    method: 'get',
    params: query,
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
    responseType: 'blob'
  });
}