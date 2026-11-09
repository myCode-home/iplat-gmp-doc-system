package org.dromara.dms.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.tenant.core.TenantEntity;

/**
 * 文件替代关系 dms_document_lib_replace
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dms_document_lib_replace")
public class DmsDocumentLibReplace extends TenantEntity {

    @TableId(value = "id")
    private Long id;

    /** 新文件 id */
    private Long libId;

    /** 被替代文件 id */
    private Long replaceLibId;
}
