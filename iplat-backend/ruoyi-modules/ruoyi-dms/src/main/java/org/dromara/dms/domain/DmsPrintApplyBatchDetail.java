package org.dromara.dms.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.tenant.core.TenantEntity;

/**
 * 批量打印明细 dms_print_apply_batch_detail
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dms_print_apply_batch_detail")
public class DmsPrintApplyBatchDetail extends TenantEntity {

    @TableId(value = "id")
    private Long id;

    /** 批量打印申请 id */
    private Long batchId;

    private Integer applyCopies;

    /** docPrint / recPrint */
    private String printType;

    /** document / record */
    private String docOrRecordType;

    private Long docOrRecordId;

    private Integer pages;

    /** 是否打印页 true / false */
    private String whetherPrintPage;

    private String docCode;

    private String docName;

    private String docVersion;

    private String docDept;

    private String docType;

    private String docStatus;
}
