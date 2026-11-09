import request from '@/utils/request';
import { AxiosPromise } from 'axios';

// 成册打印页面前的结果是否确认信息查询
export function queryConfirmBefore(volumesPrintApplyId: number): AxiosPromise<[]> {
  return request({
    url: `/dms/volumes-print-info/info/query-confirm-before/${volumesPrintApplyId}`,
    method: 'get'
  });
}

// 打印取消方法
export function printCancel(data: {}): AxiosPromise<[]> {
  return request({
    url: `/dms/volumes-print-info/print-cancel`,
    method: 'post',
    data: data
  });
}

// 查询成册打印明细表所有数据
export function printInfoListAll(query: {}): AxiosPromise<[]> {
  return request({
    url: `/dms/volumes-print-info/list/all`,
    method: 'get',
    params: query
  });
}

// 扫码回收打印明细信息查询
export function queryReceived(query: {}): AxiosPromise<[]> {
  return request({
    url: `/dms/volumes-print-info/list/received`,
    method: 'get',
    params: query
  });
}

// 扫码发放单条查询
export function queryIssue(id: string | number): AxiosPromise<[]> {
  return request({
    url: `/dms/volumes-print-info/issue/${id}`,
    method: 'get'
  });
}

// 打印件发放
export function printIssue(data: {}, es?: ''): AxiosPromise<[]> {
  return request({
    url: `/dms/volumes-print-info/issue`,
    method: 'put',
    data: data,
    headers: {
      'X-Signature': es
    }
  });
}
// 扫码发放，回收，销毁分开签名接口
// 发放人，回收人，销毁人签名前判断是否有发放中，回收中，异常回收中， 销毁中的数据
export function issueJudgment(data: {}, es?: ''): AxiosPromise<[]> {
  return request({
    url: `/dms/volumes-print-info/issue/signature/judgment`,
    method: 'post',
    data: data,
    headers: {
      'X-Signature': es
    }
  });
}
// 打印件发放-发放人确认
export function issueSignature(data: {}, es?: ''): AxiosPromise<[]> {
  return request({
    url: `/dms/volumes-print-info/issue/signature`,
    method: 'post',
    data: data,
    headers: {
      'X-Signature': es
    }
  });
}
// 扫码发放，回收，销毁分开签名接口
// 领用人，回收确认人，异常回收确认人，销毁确认人点击签名前的判断接口
export function issueReceiveJudgment(data: {}, es?: ''): AxiosPromise<[]> {
  return request({
    url: `/dms/volumes-print-info/receive/signature/judgment`,
    method: 'post',
    data: data,
    headers: {
      'X-Signature': es
    }
  });
}
// 打印件发放-领取人确认
export function receiveSignature(data: {}, es?: ''): AxiosPromise<[]> {
  return request({
    url: `/dms/volumes-print-info/receive/signature`,
    method: 'post',
    data: data,
    headers: {
      'X-Signature': es
    }
  });
}

// 扫码回收单条查询
export function queryReturn(id: string | number): AxiosPromise<[]> {
  return request({
    url: `/dms/volumes-print-info/return/${id}`,
    method: 'get'
  });
}
// 打印件回收
export function printReturn(data: {}, es?: ''): AxiosPromise<[]> {
  return request({
    url: `/dms/volumes-print-info/return`,
    method: 'put',
    data: data,
    headers: {
      'X-Signature': es
    }
  });
}
// 打印件异常回收
export function printAbnormal(data: {}, es?: ''): AxiosPromise<[]> {
  return request({
    url: `/dms/volumes-print-info/return/abnormal`,
    method: 'put',
    data: data,
    headers: {
      'X-Signature': es
    }
  });
}
// 打印件发放-交回人确认
export function returnSignature(data: {}, es?: ''): AxiosPromise<[]> {
  return request({
    url: `/dms/volumes-print-info/return/signature`,
    method: 'post',
    data: data,
    headers: {
      'X-Signature': es
    }
  });
}
// 打印件发放-回收人确认
export function returnConfirmSignature(data: {}, es?: ''): AxiosPromise<[]> {
  return request({
    url: `/dms/volumes-print-info/returnConfirm/signature`,
    method: 'post',
    data: data,
    headers: {
      'X-Signature': es
    }
  });
}
// 打印件发放-其他回收人确认
export function abnormalReturnSignature(data: {}, es?: ''): AxiosPromise<[]> {
  return request({
    url: `/dms/volumes-print-info/abnormalReturn/signature`,
    method: 'post',
    data: data,
    headers: {
      'X-Signature': es
    }
  });
}
// 打印件发放-其他回收人确认人
export function abnormalReturnConfirm(data: {}, es?: ''): AxiosPromise<[]> {
  return request({
    url: `/dms/volumes-print-info/abnormalReturnConfirm/signature`,
    method: 'post',
    data: data,
    headers: {
      'X-Signature': es
    }
  });
}
// 销毁确认打印明细信息查询
export function listReturn(query: {}): AxiosPromise<[]> {
  return request({
    url: `/dms/volumes-print-info/list/return`,
    method: 'get',
    params: query
  });
}
// 打印件销毁
export function destroy(data: {}, es?: ''): AxiosPromise<[]> {
  return request({
    url: `/dms/volumes-print-info/destroy`,
    method: 'put',
    data: data,
    headers: {
      'X-Signature': es
    }
  });
}
// 打印件销毁-销毁人确认
export function destroySignature(data: {}, es?: ''): AxiosPromise<[]> {
  return request({
    url: `/dms/volumes-print-info/destroy/signature`,
    method: 'post',
    data: data,
    headers: {
      'X-Signature': es
    }
  });
}
// 打印件销毁-监销人确认
export function destroyConfirmSignature(data: {}, es?: ''): AxiosPromise<[]> {
  return request({
    url: `/dms/volumes-print-info/destroyConfirm/signature`,
    method: 'post',
    data: data,
    headers: {
      'X-Signature': es
    }
  });
}
