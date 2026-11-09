package org.dromara.dms.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.tenant.core.TenantEntity;

/**
 * 成册打印待组册任务 dms_volumes_print_pending_task
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dms_volumes_print_pending_task")
public class DmsVolumesPrintPendingTask extends TenantEntity {

    @TableId(value = "id")
    private Long id;

    /** 待组册批次（excelName） */
    private String excelName;

    private String rowId;

    private String docCode;

    private String docName;

    /** 文件类型 key */
    private String docType;

    /** 申请部门 key */
    private String applyDeptKey;

    private String applyDept;

    /** active / unActive */
    private String source;

    /** 打印类型 */
    private String printType;

    private Integer pages;

    private Integer applyCopies;

    /** 是否打印页 true / false */
    private String whetherPrintPage;

    /** 受控信息 key */
    private String controlledInfo;

    /** 状态 pending 待接受 / accepted 已接受 / rejected 已拒绝 */
    private String status;
}
