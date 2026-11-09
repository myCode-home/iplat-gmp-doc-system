package org.dromara.dms.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.tenant.core.TenantEntity;

/**
 * 设备检验模板 dms_eqms_inspection_template
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dms_eqms_inspection_template")
public class DmsEqmsInspectionTemplate extends TenantEntity {

    @TableId(value = "id")
    private Long id;

    private String templateCode;

    private String templateName;

    /** 模板类型（字典 eqms_inspection_template_type） */
    private String templateType;

    private String version;

    /** 状态 active / failure */
    private String status;

    private String remark;
}
