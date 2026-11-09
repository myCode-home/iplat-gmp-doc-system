package org.dromara.dms.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.tenant.core.TenantEntity;

/**
 * 页面配置属性项 sys_page_config_attribute
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_page_config_attribute")
public class SysPageConfigAttribute extends TenantEntity {

    @TableId(value = "id")
    private Long id;

    /** 页面配置 id */
    private Long pageConfigId;

    private String pageCode;

    /** 原始属性 JSON（完整保留前端需要的所有键） */
    private String attributeJson;

    /** 排序 */
    private Integer pageConfigAttributeSort;
}
