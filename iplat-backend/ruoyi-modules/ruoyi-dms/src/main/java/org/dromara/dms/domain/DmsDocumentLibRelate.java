package org.dromara.dms.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.tenant.core.TenantEntity;

/**
 * 文件关联关系 dms_document_lib_relate
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dms_document_lib_relate")
public class DmsDocumentLibRelate extends TenantEntity {

    @TableId(value = "id")
    private Long id;

    /** 文件库 id */
    private Long libId;

    /** 关联文件 id */
    private Long relateLibId;

    /** 关联类型 1 单向 / 2 双向 */
    private String linkType;
}
