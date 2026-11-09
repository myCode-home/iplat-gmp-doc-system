package org.dromara.dms.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.dms.domain.DmsDocumentLendLib;
import org.dromara.dms.mapper.DmsDocumentLendLibMapper;
import org.dromara.dms.util.DmsPages;
import org.springframework.web.bind.annotation.*;

/**
 * 借阅库
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/dms/document-lend-lib")
public class DmsDocumentLendLibController {

    private final DmsDocumentLendLibMapper baseMapper;

    @GetMapping("/list")
    public TableDataInfo<DmsDocumentLendLib> list(DmsDocumentLendLib query, PageQuery pageQuery) {
        QueryWrapper<DmsDocumentLendLib> qw = new QueryWrapper<>();
        qw.eq(query.getStatus() != null, "status", query.getStatus());
        qw.like(query.getDocumentCode() != null && !query.getDocumentCode().isBlank(), "document_code", query.getDocumentCode());
        qw.like(query.getDocumentName() != null && !query.getDocumentName().isBlank(), "document_name", query.getDocumentName());
        qw.orderByDesc("create_time");
        return DmsPages.page(baseMapper, pageQuery, qw);
    }

    @GetMapping("/{id}")
    public R<DmsDocumentLendLib> detail(@PathVariable Long id) {
        return R.ok(baseMapper.selectById(id));
    }
}
