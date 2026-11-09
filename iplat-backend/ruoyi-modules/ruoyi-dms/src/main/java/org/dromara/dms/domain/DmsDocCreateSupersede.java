package org.dromara.dms.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.tenant.core.TenantEntity;

/**
 * 文档替代关系 dms_doc_create_supersede
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dms_doc_create_supersede")
public class DmsDocCreateSupersede extends TenantEntity {

    @TableId(value = "id")
    private Long id;

    /** 文档编制 id */
    private Long docCreateId;

    /** 被替代文件库 id */
    private Long documentLibId;

    /** 文件详情（查询返回） */
    @TableField(exist = false)
    private DmsDocumentLib documentLibInfo;
}
