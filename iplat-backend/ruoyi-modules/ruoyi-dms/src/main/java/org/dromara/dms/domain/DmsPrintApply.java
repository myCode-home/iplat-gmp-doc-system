package org.dromara.dms.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.tenant.core.TenantEntity;

/**
 * 打印申请 dms_print_apply
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dms_print_apply")
public class DmsPrintApply extends TenantEntity {

    @TableId(value = "id")
    private Long id;

    private String applyCode;

    private String applyReason;

    /** document / record */
    private String docOrRecordType;

    /** 文件库/记录库 id */
    private Long docOrRecordId;

    private String docCode;

    private String docName;

    private String docVersion;

    private String docStatus;

    /** template / document */
    private String tmplSource;

    /** 打印类型 docPrint / recPrint */
    private String printType;

    /** 打印份数 */
    private Integer copies;

    /** 页数 */
    private Integer pages;

    /** 打印来源 add */
    private String printSource;

    /** 状态 edit / approving / approved / stop / retreat / failure */
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
