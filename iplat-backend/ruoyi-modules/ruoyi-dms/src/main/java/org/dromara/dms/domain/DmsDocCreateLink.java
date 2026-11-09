package org.dromara.dms.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.tenant.core.TenantEntity;

/**
 * 文档关联关系 dms_doc_create_link
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dms_doc_create_link")
public class DmsDocCreateLink extends TenantEntity {

    @TableId(value = "id")
    private Long id;

    /** 文档编制 id */
    private Long docCreateId;

    /** 关联文件库 id */
    private Long documentLibId;

    /** 关联类型 1 单向 / 2 双向 */
    private String linkType;

    /** 文件详情（查询返回） */
    @TableField(exist = false)
    private DmsDocumentLib documentLibInfo;
}
