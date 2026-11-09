package org.dromara.dms.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.tenant.core.TenantEntity;

import java.util.List;

/**
 * 批量打印申请 dms_print_apply_batch
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dms_print_apply_batch")
public class DmsPrintApplyBatch extends TenantEntity {

    @TableId(value = "id")
    private Long id;

    private String applyCode;

    private String applyReason;

    /** document / record */
    private String docOrRecordType;

    private String docCode;

    private String docName;

    private String docVersion;

    private String docStatus;

    /** 状态 edit / approving / approved / retreat / failure */
    private String status;

    private Long workflowId;

    private String approveComments;

    /** 明细（详情返回） */
    @TableField(exist = false)
    private List<DmsPrintApplyBatchDetail> details;

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
