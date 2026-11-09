package org.dromara.dms.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.tenant.core.TenantEntity;

/**
 * 文件类型（树） dms_document_type
 * 注意：id/parentId 使用 varchar，保证前端树拼接时类型一致（parentId 顶层为 'root'）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dms_document_type")
public class DmsDocumentType extends TenantEntity {

    @TableId(value = "id")
    private String id;

    /** 父节点 id，顶层为 root */
    private String parentId;

    /** 父节点名称 */
    private String parentName;

    /** 分类代码 */
    private String sortCode;

    /** 分类名称 */
    private String sortName;

    /** 状态 active / failure */
    private String status;

    private String remark;
}
