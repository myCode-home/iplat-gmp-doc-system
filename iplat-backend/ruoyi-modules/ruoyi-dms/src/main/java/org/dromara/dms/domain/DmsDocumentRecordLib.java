package org.dromara.dms.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.tenant.core.TenantEntity;

import java.util.Date;

/**
 * 记录库 dms_document_record_lib
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dms_document_record_lib")
public class DmsDocumentRecordLib extends TenantEntity {

    @TableId(value = "id")
    private Long id;

    /** 记录编号 */
    private String dlrCode;

    /** 记录名称 */
    private String dlrName;

    /** 版本 */
    private String dlrVersion;

    /** 生效时间 */
    private Date dlrActiveTime;

    /** 失效时间 */
    private Date dlrDeactiveTime;

    /** 失效原因 */
    private String dlrDeactiveReason;

    /** 所属文件库 id */
    private Long documentLibId;

    /** 所属文件编号 */
    private String documentCode;

    /** 所属文件名称 */
    private String documentName;

    /** 文件类型 */
    private String documentType;

    /** 编制方式 online / offline */
    private String editWay;

    /** 扩展属性 JSON */
    private String ext;

    /** 业务状态 active / failure */
    private String status;

    /** 生效状态 active / deactiving / revising / rechecking */
    private String activeStatus;

    /** 所属部门 */
    private String dlDeptName;

    public String getStatusName() {
        if ("active".equals(activeStatus)) {
            return "生效";
        }
        if ("deactiving".equals(activeStatus)) {
            return "失效中";
        }
        if ("revising".equals(activeStatus)) {
            return "修订中";
        }
        if ("rechecking".equals(activeStatus)) {
            return "复核中";
        }
        return "失效";
    }
}
