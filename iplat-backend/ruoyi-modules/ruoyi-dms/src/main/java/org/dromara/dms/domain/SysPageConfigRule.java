package org.dromara.dms.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.tenant.core.TenantEntity;

/**
 * 页面配置规则 sys_page_config_rule
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_page_config_rule")
public class SysPageConfigRule extends TenantEntity {

    @TableId(value = "id")
    private Long id;

    /** 页面配置 id */
    private Long pageConfigId;

    private String pageCode;

    private String pageConfigRuleName;

    /** 是否启用 1 / 0 */
    private String pageConfigRuleEnable;

    /** 条件 JSON */
    private String pageConfigRuleCondition;

    /** 动作 JSON */
    private String pageConfigRuleAction;

    private Integer pageConfigRuleSort;
}
