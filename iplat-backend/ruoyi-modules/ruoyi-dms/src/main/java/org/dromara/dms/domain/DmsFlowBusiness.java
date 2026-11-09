package org.dromara.dms.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.tenant.core.TenantEntity;

/**
 * 流程业务类型登记 dms_flow_business
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dms_flow_business")
public class DmsFlowBusiness extends TenantEntity {

    @TableId(value = "id")
    private Long id;

    /** 业务 id */
    private String businessId;

    /** 业务类型（模块标识，用于审批事件路由） */
    private String businessType;
}
