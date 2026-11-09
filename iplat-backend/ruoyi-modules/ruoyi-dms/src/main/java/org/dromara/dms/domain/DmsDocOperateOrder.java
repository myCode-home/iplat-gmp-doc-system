package org.dromara.dms.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.tenant.core.TenantEntity;

import java.util.List;

/**
 * 操作单（新增/修订申请） dms_doc_operate_order
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dms_doc_operate_order")
public class DmsDocOperateOrder extends TenantEntity {

    @TableId(value = "id")
    private Long id;

    private String orderNumber;

    /** 文件类型 */
    private String documentType;

    private String documentCode;

    private String documentName;

    /** 操作类型 add 新增 / revise 修订 */
    private String operateType;

    /** 被修订文件库 id */
    private Long documentLibId;

    /** 替代文件 id（逗号分隔，持久化） */
    private String replaceLibIds;

    /** 来源 template / document */
    private String tmplSource;

    private Long tmplLibId;

    private String tmplLibCode;

    private String tmplLibName;

    private Long drafterId;

    private String draftNickName;

    /** 编制部门 */
    private Long docDeptId;

    /** 分发部门（逗号分隔） */
    private String distributeDeptId;

    /** 扩展属性 JSON */
    private String ext;

    /** 状态 edit / approving / approved / retreat / failure */
    private String status;

    private Long workflowId;

    private String approveComments;

    /** 替代文件（详情返回） */
    @TableField(exist = false)
    private List<DmsDocumentLib> libs;

    /** 替代文件 id 集合（提交入参） */
    @TableField(exist = false)
    private List<Long> replaceIds;

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

    public String getOperateTypeName() {
        return switch (operateType == null ? "" : operateType) {
            case "add" -> "新增";
            case "revise" -> "修订";
            default -> operateType == null ? "" : operateType;
        };
    }
}
