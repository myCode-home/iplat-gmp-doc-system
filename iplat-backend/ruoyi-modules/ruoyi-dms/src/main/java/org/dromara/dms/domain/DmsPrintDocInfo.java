package org.dromara.dms.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.tenant.core.TenantEntity;

import java.util.Date;

/**
 * 打印明细（打印分发状态机） dms_print_doc_info
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dms_print_doc_info")
public class DmsPrintDocInfo extends TenantEntity {

    @TableId(value = "id")
    private Long id;

    /** 打印申请 id */
    private Long printApplyId;

    private String applyCode;

    /** document / record */
    private String docOrRecordType;

    private Long docOrRecordId;

    private String docCode;

    private String docName;

    private String docVersion;

    private String docType;

    private String docStatus;

    private Integer copies;

    private Integer pages;

    /** 打印类型 docPrint / recPrint */
    private String printType;

    /** 状态 printed 已打印 / issued 已发放 / received 已领用 / returned 已交回 /
     * abnormal 异常 / closed 已关闭 / destroyed 已销毁 / failure 打印失败 / cancelled 已取消 */
    private String state;

    private Date printTime;

    private String issueUserId;

    private String issueUserName;

    private Date issueTime;

    private String receiveUserName;

    private Date receiveTime;

    private String returnUserName;

    private Date returnTime;

    private String confirmUserId;

    private String confirmUserName;

    private String closeUserName;

    private Date closeTime;

    private String destroyUserName;

    private String confirmDestroyUserName;

    private String destroyAction;

    private String destroyPlace;

    private Date destroyTime;

    private String abnormalReason;

    private String failReason;

    private String remark;

    public String getStateName() {
        return switch (state == null ? "" : state) {
            case "printed" -> "已打印";
            case "issued" -> "已发放";
            case "received" -> "已领用";
            case "returned" -> "已交回";
            case "abnormal" -> "异常交回";
            case "closed" -> "已关闭";
            case "destroyed" -> "已销毁";
            case "failure" -> "打印失败";
            case "cancelled" -> "已取消";
            default -> state == null ? "" : state;
        };
    }
}
