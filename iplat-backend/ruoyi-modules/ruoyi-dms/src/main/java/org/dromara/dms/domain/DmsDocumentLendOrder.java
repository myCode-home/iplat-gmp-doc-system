package org.dromara.dms.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.tenant.core.TenantEntity;

import java.util.Date;

/**
 * 文件借阅单 dms_document_lend_order
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dms_document_lend_order")
public class DmsDocumentLendOrder extends TenantEntity {

    @TableId(value = "id")
    private Long id;

    private String orderNumber;

    /** 借阅文件库 id */
    private Long docLibId;

    private String documentCode;

    private String documentName;

    /** 借阅期限（天） */
    private String lendingPeriod;

    /** 是否允许下载 0 否 / 1 是 */
    private String isDownload;

    /** 计划归还日期 */
    private Date returnDate;

    /** 状态 edit / approving / approved / retreat / failure */
    private String status;

    private Long workflowId;

    private String approveComments;

    /** 扩展属性 JSON */
    private String ext;

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
