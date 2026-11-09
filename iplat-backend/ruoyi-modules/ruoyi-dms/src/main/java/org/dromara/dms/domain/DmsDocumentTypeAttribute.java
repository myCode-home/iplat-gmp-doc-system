package org.dromara.dms.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.tenant.core.TenantEntity;

/**
 * 文件类型属性配置 dms_document_type_attribute
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dms_document_type_attribute")
public class DmsDocumentTypeAttribute extends TenantEntity {

    @TableId(value = "id")
    private Long id;

    /** 所属文件类型 id */
    private String sortId;

    private String attributeCode;

    private String attributeName;

    /** 是否必填 true / false */
    private String ifRequired;

    /** 值类型 input / textarea / date */
    private String valueType;

    /** 适用标记 printApply / documentFlow */
    private String applicableMark;

    /** 状态 active / failure */
    private String status;

    private Integer sort;
}
