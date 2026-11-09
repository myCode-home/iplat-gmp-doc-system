package org.dromara.dms.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.tenant.core.TenantEntity;

/**
 * 批量修订单明细 dms_batch_revise_order_detail
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dms_batch_revise_order_detail")
public class DmsBatchReviseOrderDetail extends TenantEntity {

    @TableId(value = "id")
    private Long id;

    /** 批量修订单 id */
    private Long orderId;

    /** 文件库 id */
    private Long documentLibId;

    private String documentCode;

    private String documentName;

    private String documentVersion;

    private String documentLevel;

    private String dlCode;

    private String dlName;

    private String dlVersion;

    private String dlLevel;

    private String drafterDeptName;

    private String issueDeptName;

    private String createDeptName;

    private String userDeptName;
}
