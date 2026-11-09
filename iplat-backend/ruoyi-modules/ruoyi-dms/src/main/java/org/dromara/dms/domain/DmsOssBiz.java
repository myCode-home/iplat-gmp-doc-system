package org.dromara.dms.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.tenant.core.TenantEntity;

/**
 * 业务附件关联 dms_oss_biz
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dms_oss_biz")
public class DmsOssBiz extends TenantEntity {

    @TableId(value = "id")
    private Long id;

    /** 业务 id */
    private String bizId;

    /** 业务类型 DOC_APPLICATION_FILE / DOC_ATTACHMENT_FILE / Training_Confirm_File / DOC_FILE / PDF_FILE ... */
    private String bizType;

    /** 子类型 */
    private String bizSecondType;

    /** sys_oss 表 ossId */
    private Long ossId;

    /** 转换类型 */
    private String covertType;
}
