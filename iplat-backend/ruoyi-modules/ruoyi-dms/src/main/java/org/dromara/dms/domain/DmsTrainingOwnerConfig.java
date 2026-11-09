package org.dromara.dms.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.tenant.core.TenantEntity;

/**
 * 培训责任人配置 dms_training_owner_config
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dms_training_owner_config")
public class DmsTrainingOwnerConfig extends TenantEntity {

    @TableId(value = "id")
    private Long id;

    /** 目标类型 personnel 人员 / role 角色 / userGroups 用户组 */
    private String userCategory;

    /** 目标 id（用户/角色/用户组） */
    private String userInfo;

    /** 目标名称 */
    private String nickName;

    private Long drafterId;

    private String draftUserName;

    private String status;
}
