package org.dromara.dms.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.tenant.core.TenantEntity;

import java.util.Date;

/**
 * 设备编号申请 dms_eqms_number_application
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dms_eqms_number_application")
public class DmsEqmsNumberApplication extends TenantEntity {

    @TableId(value = "id")
    private Long id;

    private String applyCode;

    private String deviceName;

    private String deviceType;

    private String applicant;

    private Date applyDate;

    /** 状态 edit / approving / approved / retreat / failure */
    private String status;

    private String remark;
}
