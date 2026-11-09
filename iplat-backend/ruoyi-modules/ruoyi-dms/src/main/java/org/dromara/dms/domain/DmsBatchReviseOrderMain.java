package org.dromara.dms.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.tenant.core.TenantEntity;

import java.util.List;

/**
 * 批量修订单 dms_batch_revise_order_main
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dms_batch_revise_order_main")
public class DmsBatchReviseOrderMain extends TenantEntity {

    @TableId(value = "id")
    private Long id;

    private String orderNumber;

    private String orderReason;

    private String remark;

    private String applyUser;

    /** 状态 edit / approving / approved / retreat / failure */
    private String status;

    private Long workflowId;

    private String approveComments;

    /** 文件 id 集合（提交入参，兼容 documentLibIds/details/docLibIds） */
    @TableField(exist = false)
    private List<Long> documentLibIds;

    @TableField(exist = false)
    private List<Long> details;

    @TableField(exist = false)
    private String docLibIds;

    /** 明细（详情返回） */
    @TableField(exist = false)
    private List<DmsBatchReviseOrderDetail> detailList;

    public String getStatusName() {
        return switch (status == null ? "" : status) {
            case "edit" -> "编制中";
            case "commit" -> "已提交";
            case "approving" -> "审批中";
            case "approved" -> "已审批";
            case "retreat" -> "已退回";
            case "failure" -> "已失效";
            case "stop" -> "已终止";
            default -> status == null ? "" : status;
        };
    }
}
