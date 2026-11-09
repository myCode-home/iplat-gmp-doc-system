package org.dromara.dms.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.tenant.core.TenantEntity;

import java.util.Date;

/**
 * 借阅库 dms_document_lend_lib
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dms_document_lend_lib")
public class DmsDocumentLendLib extends TenantEntity {

    @TableId(value = "id")
    private Long id;

    /** 借阅单 id */
    private Long orderId;

    /** 借阅文件库 id */
    private Long docLibId;

    private String documentCode;

    private String documentName;

    private String lendingPeriod;

    /** 是否允许下载 0 否 / 1 是 */
    private String isDownload;

    /** 借阅时间 */
    private Date lendDate;

    /** 归还日期 */
    private Date returnDate;

    /** 状态 lending / returned */
    private String status;
}
