package org.dromara.dms.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.tenant.core.TenantEntity;

import java.util.List;

/**
 * 文档失效单 dms_doc_invalid_order
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dms_doc_invalid_order")
public class DmsDocInvalidOrder extends TenantEntity {

    @TableId(value = "id")
    private Long id;

    private String orderNumber;

    /** 失效原因 */
    private String orderReason;

    /** 详情表单回显用：首个文件库 id */
    private Long documentLibId;

    /** 状态 edit / approving / approved / retreat / failure */
    private String status;

    private Long workflowId;

    private String approveComments;

    /** 失效文件明细（前端 body 中的 documentIds） */
    @TableField(exist = false)
    private List<Long> documentIds;

    /** 兼容前端拼写 documentlds（小写 L） */
    @TableField(exist = false)
    private List<Long> documentlds;

    @TableField(exist = false)
    private List<DmsDocumentLib> documentList;

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
