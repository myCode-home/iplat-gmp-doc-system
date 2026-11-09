package org.dromara.dms.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.tenant.core.TenantEntity;

/**
 * 页面/表格动态配置 sys_page_config
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_page_config")
public class SysPageConfig extends TenantEntity {

    @TableId(value = "page_config_id")
    private Long pageConfigId;

    /** 页面编码 */
    private String pageCode;

    /** 页面名称 */
    private String pageConfigName;

    /** 对齐方式 left / center / right / none */
    private String pageConfigAlign;

    /** 尺寸 small / default / large */
    private String pageConfigSize;

    /** 表单列数 */
    private String pageConfigDisplay;

    private String remark;
}
