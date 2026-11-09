package org.dromara.dms.controller;

import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.dms.mapper.DmsFlowTaskMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * 工作流补充接口 + 用户组占位接口
 */
@RequiredArgsConstructor
@RestController
public class DmsWorkflowExtraController {

    private final DmsFlowTaskMapper flowTaskMapper;

    /** 前端审批页：按流程实例取当前待办任务 id */
    @GetMapping("/workflow/instance/waitingTaskIdByInstanceId/{instanceId}")
    public R<Long> waitingTaskId(@PathVariable Long instanceId) {
        return R.ok(flowTaskMapper.selectWaitingTaskId(instanceId));
    }

    /** 用户组列表（本地演示返回空集） */
    @GetMapping("/system/user-group/list")
    public TableDataInfo<Void> userGroupList(PageQuery pageQuery) {
        return TableDataInfo.build();
    }
}
