package org.dromara.dms.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.tenant.core.TenantEntity;

import java.util.Date;

/**
 * 文件浏览历史 dms_doc_browse_history
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dms_doc_browse_history")
public class DmsDocBrowseHistory extends TenantEntity {

    @TableId(value = "id")
    private Long id;

    /** 文件库 id */
    private Long documentLibId;

    private String dlCode;

    private String dlName;

    private String dlVersion;

    /** 浏览人 */
    private String browseUserName;

    /** 浏览时间 */
    private Date browseTime;
}
