package org.dromara.dms.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.tenant.core.TenantEntity;

/**
 * 操作单-记录修订 dms_document_record_revise
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dms_document_record_revise")
public class DmsDocumentRecordRevise extends TenantEntity {

    @TableId(value = "id")
    private Long id;

    /** 操作单 id（前端 daId） */
    private Long daId;

    private String documentCode;

    private String documentName;

    private String documentRecordCode;

    private String documentRecordName;

    private String documentRecordType;

    private Long tmplLibId;

    private String tmplLibCode;

    private String tmplLibName;

    /** 是否修订 0 否 / 1 是 */
    private String whetherToRevise;

    /** 编制方式 online / offline */
    private String editWay;

    /** 状态 edit / approving / approved / retreat / failure */
    private String status;

    private Long workflowId;

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
