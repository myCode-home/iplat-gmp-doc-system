package org.dromara.dms.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.tenant.core.TenantEntity;

/**
 * 成册打印明细 dms_volumes_print_apply_detail
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dms_volumes_print_apply_detail")
public class DmsVolumesPrintApplyDetail extends TenantEntity {

    @TableId(value = "id")
    private Long id;

    /** 成册打印申请 id */
    private Long applyId;

    private Long docOrRecordId;

    /** document / record */
    private String docOrRecordType;

    private Long docDeptId;

    private Long issueDeptId;

    private String docCode;

    private String docName;

    private String docLevel;

    private String docType;

    private Integer copies;

    private Integer pages;

    private String printType;
}
