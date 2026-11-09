package org.dromara.dms.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.tenant.core.TenantEntity;

/**
 * 模板编制/审批 dms_tmpl_preparation
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dms_tmpl_preparation")
public class DmsTmplPreparation extends TenantEntity {

    @TableId(value = "id")
    private Long id;

    /** 模板编号 */
    private String tmplCode;

    /** 模板名称 */
    private String tmplName;

    /** 模板类型（字典 dms_template_type） */
    private String tmplCategory;

    /** 版本 */
    private String tmplVersion;

    /** 次版本 */
    private String tmplVersionMinor;

    /** 用途 document / record */
    private String tmplPurpose;

    /** 所属部门 */
    private Long tmplDept;

    private String tmplDeptName;

    /** 状态 edit / approving / approved / retreat / failure */
    private String status;

    private String activeStatus;

    /** 流程实例 id */
    private Long workflowId;

    /** 审批意见 */
    private String approveComments;

    /** 扩展属性 JSON */
    private String ext;
}
