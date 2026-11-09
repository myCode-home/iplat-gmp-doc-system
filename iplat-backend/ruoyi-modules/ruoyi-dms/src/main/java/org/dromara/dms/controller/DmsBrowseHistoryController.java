package org.dromara.dms.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.dms.domain.DmsDocBrowseHistory;
import org.dromara.dms.mapper.DmsDocBrowseHistoryMapper;
import org.dromara.dms.util.DmsPages;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 文件浏览历史
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/dms/doc-browse-history")
public class DmsBrowseHistoryController {

    private final DmsDocBrowseHistoryMapper baseMapper;

    @GetMapping("/list")
    public TableDataInfo<DmsDocBrowseHistory> list(DmsDocBrowseHistory query, PageQuery pageQuery) {
        QueryWrapper<DmsDocBrowseHistory> qw = new QueryWrapper<>();
        qw.eq(query.getDocumentLibId() != null, "document_lib_id", query.getDocumentLibId());
        qw.like(query.getDlName() != null && !query.getDlName().isBlank(), "dl_name", query.getDlName());
        qw.like(query.getDlCode() != null && !query.getDlCode().isBlank(), "dl_code", query.getDlCode());
        qw.orderByDesc("browse_time");
        return DmsPages.page(baseMapper, pageQuery, qw);
    }
}
