package org.dromara.dms.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.tenant.core.TenantEntity;

/**
 * 文档失效单明细 dms_doc_invalid_order_item
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dms_doc_invalid_order_item")
public class DmsDocInvalidOrderItem extends TenantEntity {

    @TableId(value = "id")
    private Long id;

    /** 失效单 id */
    private Long orderId;

    /** 文件库 id */
    private Long documentLibId;
}
