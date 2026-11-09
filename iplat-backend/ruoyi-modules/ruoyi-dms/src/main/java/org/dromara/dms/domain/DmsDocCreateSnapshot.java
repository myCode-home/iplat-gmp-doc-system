package org.dromara.dms.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.tenant.core.TenantEntity;

/**
 * 文档快照 dms_doc_create_snapshot
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dms_doc_create_snapshot")
public class DmsDocCreateSnapshot extends TenantEntity {

    @TableId(value = "id")
    private Long id;

    /** 业务 id（文档编制 id） */
    private Long bizId;

    private String documentCode;

    private String documentVersion;

    private String documentVersionMinor;

    /** 快照名称 */
    private String snapshotName;

    /** 快照内容 JSON */
    private String content;
}
