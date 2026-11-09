package org.dromara.dms.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.tenant.core.TenantEntity;

import java.util.Date;

/**
 * PageOffice 打开记录 dms_file_open_record
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dms_file_open_record")
public class DmsFileOpenRecord extends TenantEntity {

    @TableId(value = "id")
    private Long id;

    /** 业务 id */
    private String fileBusinessId;

    /** 业务类型 */
    private String fileBusinessType;

    /** 业务名称 */
    private String fileBusinessName;

    /** 打开人 */
    private String openUserName;

    /** 打开时间 */
    private Date openTime;
}
