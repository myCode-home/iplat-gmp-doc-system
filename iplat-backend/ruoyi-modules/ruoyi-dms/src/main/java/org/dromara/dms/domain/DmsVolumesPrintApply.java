package org.dromara.dms.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.tenant.core.TenantEntity;

import java.util.List;

/**
 * 成册打印申请 dms_volumes_print_apply
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dms_volumes_print_apply")
public class DmsVolumesPrintApply extends TenantEntity {

    @TableId(value = "id")
    private Long id;

    private String volumesName;

    private String applyCode;

    /** 打印来源 add */
    private String printSource;

    /** 打印件状态 approved */
    private String state;

    /** 状态 edit / approving / approved / retreat / failure */
    private String status;

    private Long workflowId;

    /** online / offline */
    private String editWay;

    /** template / document */
    private String tmplSource;

    private Long tmplLibId;

    private String tmplLibCode;

    private String tmplLibName;

    private Long drafterId;

    private String drafterUserName;

    private String documentRecordLibraryId;

    private String documentRecordCode;

    private String documentRecordName;

    private String documentRecordType;

    private String documentCode;

    private String documentName;

    private String approveComments;

    /** 成册明细（详情返回） */
    @TableField(exist = false)
    private List<DmsVolumesPrintApplyDetail> details;

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
