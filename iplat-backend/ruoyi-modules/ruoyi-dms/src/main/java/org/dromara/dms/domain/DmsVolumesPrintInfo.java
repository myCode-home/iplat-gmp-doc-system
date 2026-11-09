package org.dromara.dms.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.tenant.core.TenantEntity;

import java.util.Date;

/**
 * 成册打印件 dms_volumes_print_info
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dms_volumes_print_info")
public class DmsVolumesPrintInfo extends TenantEntity {

    @TableId(value = "id")
    private Long id;

    /** 成册打印申请 id */
    private Long volumesPrintApplyId;

    private String applyCode;

    private String volumesName;

    private String docCode;

    private String docName;

    private Integer pages;

    private Integer copies;

    /** 状态 printed 已打印 / issuing 发放中 / issued 已发放 / receiving 领用中 /
     * received 已领用 / returning 交回中 / returned 已交回 / abnormalReturning 异常交回中 /
     * abnormalReturned 异常已交回 / destroying 销毁中 / destroyed 已销毁 / cancelled 已取消 */
    private String state;

    private Date printTime;

    private String issueUserName;

    private Date issueTime;

    private String receiveUserName;

    private Date receiveTime;

    private String returnUserName;

    private Date returnTime;

    private String confirmUserName;

    private String destroyUserName;

    private String confirmDestroyUserName;

    private String destroyAction;

    private String destroyPlace;

    private Date destroyTime;

    private String abnormalReason;
}
