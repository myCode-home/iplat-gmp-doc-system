package org.dromara.dms.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.tenant.core.TenantEntity;

/**
 * 模板库 dms_tmpl_lib
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dms_tmpl_lib")
public class DmsTmplLib extends TenantEntity {

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

    /** 业务状态 active / failure */
    private String status;

    /** 生效状态 active / deactiving / revising */
    private String activeStatus;

    /** 扩展属性 JSON */
    private String ext;

    public String getStatusName() {
        if ("active".equals(activeStatus)) {
            return "生效";
        }
        return "失效";
    }
}
