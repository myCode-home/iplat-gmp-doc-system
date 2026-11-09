package org.dromara.dms.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.tenant.core.TenantEntity;

import java.util.List;

/**
 * 文档编制 dms_doc_create
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dms_doc_create")
public class DmsDocCreate extends TenantEntity {

    @TableId(value = "id")
    private Long id;

    /** 文件编号 */
    private String documentCode;

    /** 文件名称 */
    private String documentName;

    /** 文件类型 */
    private String documentType;

    /** 版本 */
    private String documentVersion;

    /** 次版本 */
    private String documentVersionMinor;

    /** 文件级别 */
    private String documentLevel;

    /** 编制部门 */
    private Long documentDeptId;

    private String documentDeptCode;

    private String documentDeptName;

    /** 编制方式 online / offline */
    private String editWay;

    /** 操作来源 fromInput / fromApplyDoc / manualRevision / autoCreate / fromBatchReviseApply / batchAutoCreate / fromUpload */
    private String operateSource;

    private String operateRemark;

    private String operateCause;

    /** 来源 template / document */
    private String tmplSource;

    private Long tmplLibId;

    private String tmplLibCode;

    private String tmplLibName;

    /** 起草人 */
    private Long drafterId;

    private String drafterName;

    /** 分发部门（逗号分隔） */
    private String distributeDeptId;

    private String distributeDeptCode;

    private String distributeDeptName;

    private String pdDeptId;

    private String pdDeptCode;

    private String pdDeptName;

    private Long docApplyId;

    private String orderNumber;

    /** 状态 edit / approving / approved / retreat / failure */
    private String status;

    /** 培训状态 notUploaded / needTrain / noNeedTrain / trained / active */
    private String trainStatus;

    /** 离线文件状态 notUploaded / uploaded */
    private String editStatus;

    /** 流程实例 id */
    private Long workflowId;

    private String approveComments;

    /** 扩展属性 JSON */
    private String ext;

    /** 替代文件（详情返回） */
    @TableField(exist = false)
    private List<DmsDocCreateSupersede> supersedeList;

    /** 关联文件（详情返回） */
    @TableField(exist = false)
    private List<DmsDocCreateLink> docCreateLinkList;

    /** 被替代文件 id（逗号分隔，提交入参） */
    @TableField(exist = false)
    private String supersedeDocumentIds;

    /** 关联文件 id（逗号分隔，提交入参） */
    @TableField(exist = false)
    private String relateDocumentIds;

    /** 关联文件类型（逗号分隔，提交入参） */
    @TableField(exist = false)
    private String relateDocumentTypes;

    public String getStatusName() {
        return switch (status == null ? "" : status) {
            case "edit" -> "编制中";
            case "commit" -> "已提交";
            case "approving" -> "审批中";
            case "approved" -> "已生效";
            case "retreat" -> "已退回";
            case "failure" -> "已失效";
            default -> status;
        };
    }

    public String getTrainStatusName() {
        return switch (trainStatus == null ? "" : trainStatus) {
            case "needTrain" -> "待培训";
            case "noNeedTrain" -> "无需培训";
            case "trained" -> "已培训";
            case "active" -> "已生效";
            case "readyActive" -> "待生效";
            case "notUploaded" -> "未上传";
            default -> trainStatus == null ? "" : trainStatus;
        };
    }
}
