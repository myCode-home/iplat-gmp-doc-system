import request from '@/utils/request'

// 查询消息日志列表
export function listMsgLog(query) {
  return request({
    url: '/resource/msgLog/list',
    method: 'get',
    params: query
  })
}

// 查询消息日志详细
export function getMsgLog(msgLogId) {
  return request({
    url: '/resource/msgLog/' + msgLogId,
    method: 'get'
  })
}

// 删除消息日志
export function delMsgLog(msgLogId) {
  return request({
    url: '/resource/msgLog/' + msgLogId,
    method: 'delete'
  })
}

// 清空操作日志
export function cleanMsglog() {
  return request({
    url: '/resource/msgLog/clean',
    method: 'delete'
  })
}

// 清空操作日志-自己
export function cleanMineOperlog() {
  return request({
    url: '/resource/msgLog/clean/mine',
    method: 'delete'
  })
}

// 已读消息日志
export function modifyMsgLogToRead(msgLogId) {
  return request({
    url: '/resource/msgLog/to-read/' + msgLogId,
    method: 'put'
  })
}

// 统计消息日志
export function queryMsgLogStatisticsCount(userId) {
  return request({
    url: '/resource/msgLog/statistics/count',
    method: 'get',
    params: {userId}
  })
}

// 全部已读
export function modifyAllRead() {
  return request({
    url: '/resource/msgLog/all-read',
    method: 'put'
  })
}

// 消息撤回
export function recallMsglog(msgLogId) {
  return request({
    url: '/resource/msgLog/recall/' + msgLogId,
    method: 'put'
  })
}
