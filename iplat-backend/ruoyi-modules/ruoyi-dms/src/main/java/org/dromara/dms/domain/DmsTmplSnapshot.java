package org.dromara.dms.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.tenant.core.TenantEntity;

/**
 * 模板快照 dms_tmpl_snapshot
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dms_tmpl_snapshot")
public class DmsTmplSnapshot extends TenantEntity {

    @TableId(value = "id")
    private Long id;

    /** 业务 id（模板编制/模板库 id） */
    private Long bizId;

    private String tmplCode;

    private String tmplVersion;

    private String tmplVersionMinor;

    /** 快照名称 */
    private String snapshotName;

    /** 快照内容 JSON */
    private String content;
}
