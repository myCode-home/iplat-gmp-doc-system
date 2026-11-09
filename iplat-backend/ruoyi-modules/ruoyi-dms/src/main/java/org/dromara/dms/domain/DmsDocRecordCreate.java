package org.dromara.dms.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.tenant.core.TenantEntity;

/**
 * 记录编制 dms_doc_record_create
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dms_doc_record_create")
public class DmsDocRecordCreate extends TenantEntity {

    @TableId(value = "id")
    private Long id;

    /** 所属文件编号 */
    private String documentCode;

    /** 所属文件名称 */
    private String documentName;

    /** 记录编号 */
    private String documentRecordCode;

    /** 记录名称 */
    private String documentRecordName;

    /** 记录类型 */
    private String documentRecordType;

    /** 记录版本 */
    private String documentRecordVersion;

    /** 次版本 */
    private String documentRecordVersionMinor;

    /** 记录库 id */
    private Long documentRecordLibraryId;

    /** 记录处理类型 new / revise / fromUpload */
    private String recordHandleType;

    /** 修订来源 revise / fromUpload / autoCreate */
    private String reviseSource;

    /** 编制方式 online / offline */
    private String editWay;

    /** 离线文件状态 notUploaded / uploaded */
    private String editStatus;

    /** 所属文档编制 id */
    private Long docCreateId;

    private String tmplSource;

    private Long tmplLibId;

    private String tmplLibCode;

    private String tmplLibName;

    private Long drafterId;

    private String draftUserName;

    /** 状态 edit / approving / approved / retreat / failure */
    private String status;

    private Long workflowId;

    private String approveComments;

    /** 扩展属性 JSON */
    private String ext;

    public String getStatusName() {
        return switch (status == null ? "" : status) {
            case "edit" -> "编制中";
            case "approving" -> "审批中";
            case "approved" -> "已生效";
            case "retreat" -> "已退回";
            case "failure" -> "已失效";
            default -> status;
        };
    }
}
