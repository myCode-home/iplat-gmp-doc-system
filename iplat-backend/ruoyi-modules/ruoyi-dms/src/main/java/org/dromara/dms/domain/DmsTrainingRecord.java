package org.dromara.dms.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.tenant.core.TenantEntity;

import java.util.Date;

/**
 * 培训记录 dms_training_record
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dms_training_record")
public class DmsTrainingRecord extends TenantEntity {

    @TableId(value = "id")
    private Long id;

    /** 文档编制 id */
    private Long docCreateId;

    private String documentCode;

    private String documentName;

    /** 培训人 */
    private String trainUserName;

    /** 培训状态 needTrain / noNeedTrain / trained */
    private String trainStatus;

    /** 培训时间 */
    private Date trainTime;

    private String remark;
}
