package org.dromara.dms.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.tenant.core.TenantEntity;

/**
 * 记录快照 dms_doc_record_snapshot
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dms_doc_record_snapshot")
public class DmsDocRecordSnapshot extends TenantEntity {

    @TableId(value = "id")
    private Long id;

    /** 业务 id（记录编制 id） */
    private Long bizId;

    private String documentRecordCode;

    private String documentRecordVersion;

    private String documentRecordVersionMinor;

    /** 快照名称 */
    private String snapshotName;

    /** 快照内容 JSON */
    private String content;
}
