package org.dromara.dms.handler;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.dromara.dms.common.DmsConstants;
import org.dromara.dms.domain.DmsFlowBusiness;
import org.dromara.dms.mapper.DmsFlowBusinessMapper;
import org.dromara.workflow.domain.bo.CompleteTaskBo;
import org.dromara.workflow.domain.bo.StartProcessBo;
import org.dromara.workflow.service.IFlwTaskService;
import org.dromara.common.core.domain.dto.StartProcessReturnDTO;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;

/**
 * DMS 审批流发起支撑：登记业务类型 + 启动流程 + 提交申请人节点
 */
@RequiredArgsConstructor
@Component
public class DmsFlowSupport {

    private final IFlwTaskService flwTaskService;
    private final DmsFlowBusinessMapper flowBusinessMapper;

    /**
     * 发起审批流，返回流程实例 id
     */
    public Long start(String businessType, Long businessId) {
        String bizId = String.valueOf(businessId);
        StartProcessBo bo = new StartProcessBo();
        bo.setBusinessId(bizId);
        bo.setFlowCode(DmsConstants.FLOW_CODE);
        bo.setVariables(new HashMap<>());
        StartProcessReturnDTO ret = flwTaskService.startWorkFlow(bo);

        Long count = flowBusinessMapper.selectCount(new LambdaQueryWrapper<DmsFlowBusiness>()
            .eq(DmsFlowBusiness::getBusinessId, bizId));
        if (count == 0) {
            DmsFlowBusiness fb = new DmsFlowBusiness();
            fb.setBusinessId(bizId);
            fb.setBusinessType(businessType);
            flowBusinessMapper.insert(fb);
        }

        CompleteTaskBo ctb = new CompleteTaskBo();
        ctb.setTaskId(ret.getTaskId());
        ctb.setMessage("提交申请");
        ctb.setVariables(new HashMap<>());
        ctb.setMessageType(List.of("1"));
        flwTaskService.completeTask(ctb);
        return ret.getProcessInstanceId();
    }
}
