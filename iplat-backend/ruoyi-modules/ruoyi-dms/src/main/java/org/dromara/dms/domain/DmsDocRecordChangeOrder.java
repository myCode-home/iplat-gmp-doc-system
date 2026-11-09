package org.dromara.dms.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.tenant.core.TenantEntity;

/**
 * 记录变更单 dms_doc_record_change_order
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dms_doc_record_change_order")
public class DmsDocRecordChangeOrder extends TenantEntity {

    @TableId(value = "id")
    private Long id;

    private String orderNumber;

    private String documentCode;

    private String documentName;

    /** 记录库 id */
    private Long documentRecordLibraryId;

    private String documentRecordCode;

    private String documentRecordName;

    private String documentRecordType;

    private String editWay;

    private Long tmplLibId;

    private String tmplLibCode;

    private String tmplLibName;

    private Long drafterId;

    private String drafterUserName;

    /** 状态 edit / approving / approved / retreat / failure */
    private String status;

    private Long workflowId;

    private String approveComments;

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
