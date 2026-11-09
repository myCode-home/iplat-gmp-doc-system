package org.dromara.dms.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.tenant.core.TenantEntity;

import java.util.List;

/**
 * 文件库 dms_document_lib
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dms_document_lib")
public class DmsDocumentLib extends TenantEntity {

    @TableId(value = "id")
    private Long id;

    /** 文件编号 */
    private String dlCode;

    /** 文件名称 */
    private String dlName;

    /** 文件类型（dms_document_type.id） */
    private String dlType;

    /** 版本 */
    private String dlVersion;

    /** 文件级别 */
    private String dlLevel;

    /** 来源 new 新建 / revise 修订 */
    private String docSource;

    /** 操作来源 fromInput / autoCreate / manualRevision / fromUpload */
    private String docOperationSource;

    /** 使用状态 pending 未生效 / active 生效 / invalid 失效 */
    private String useState;

    /** 失效来源 replaceFailure 被替代失效 */
    private String deactiveSource;

    /** 失效原因 */
    private String deactiveReason;

    /** 编制部门 */
    private Long dlDeptId;

    private String dlDeptName;

    /** 发放部门 */
    private Long issueDeptId;

    private String issueDeptName;

    /** 模板 */
    private Long tmplLibId;

    private String tmplLibCode;

    private String tmplLibName;

    private String tmplSource;

    /** 起草人 */
    private Long drafterId;

    private String draftNickName;

    /** 分发部门（逗号分隔） */
    private String distributeDeptId;

    /** 扩展属性 JSON */
    private String ext;

    /** 业务状态 active / failure */
    private String status;

    /** 生效状态 active / deactiving / revising / rechecking */
    private String activeStatus;

    /** 替代文件（详情返回） */
    @TableField(exist = false)
    private List<DmsDocumentLib> libs;

    /**
     * documentType 与 dlType 同义，兼容前端取值
     */
    public String getDocumentType() {
        return dlType;
    }

    public void setDocumentType(String documentType) {
        this.dlType = documentType;
    }

    public String getStatusName() {
        if ("active".equals(useState)) {
            return "生效";
        }
        if ("pending".equals(useState)) {
            return "未生效";
        }
        return "失效";
    }
}
