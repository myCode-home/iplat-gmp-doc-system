package org.dromara.dms.mapper;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * warm-flow 任务查询（用于审批页获取当前待办任务）
 */
public interface DmsFlowTaskMapper {

    /**
     * 查询流程实例当前待办任务 id（审批节点）
     */
    @Select("select id from flow_task where instance_id = #{instanceId} and node_code = 'approve' and del_flag = '0' order by create_time desc limit 1")
    Long selectWaitingTaskId(@Param("instanceId") Long instanceId);
}
