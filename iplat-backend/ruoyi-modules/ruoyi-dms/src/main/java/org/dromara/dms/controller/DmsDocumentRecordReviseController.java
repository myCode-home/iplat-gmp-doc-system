package org.dromara.dms.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.dms.domain.DmsDocumentRecordRevise;
import org.dromara.dms.mapper.DmsDocumentRecordReviseMapper;
import org.dromara.dms.util.DmsPages;
import org.springframework.web.bind.annotation.*;

/**
 * 操作单-记录修订
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/dms/document-record-revise")
public class DmsDocumentRecordReviseController {

    private final DmsDocumentRecordReviseMapper baseMapper;

    /** 前端通过 daId（操作单 id）过滤 */
    @GetMapping("/list")
    public TableDataInfo<DmsDocumentRecordRevise> list(DmsDocumentRecordRevise query, PageQuery pageQuery) {
        QueryWrapper<DmsDocumentRecordRevise> qw = new QueryWrapper<>();
        qw.eq(query.getDaId() != null, "da_id", query.getDaId());
        qw.eq(query.getStatus() != null, "status", query.getStatus());
        qw.like(query.getDocumentRecordCode() != null && !query.getDocumentRecordCode().isBlank(), "document_record_code", query.getDocumentRecordCode());
        qw.like(query.getDocumentRecordName() != null && !query.getDocumentRecordName().isBlank(), "document_record_name", query.getDocumentRecordName());
        qw.orderByDesc("create_time");
        return DmsPages.page(baseMapper, pageQuery, qw);
    }

    @GetMapping("/{id}")
    public R<DmsDocumentRecordRevise> detail(@PathVariable Long id) {
        return R.ok(baseMapper.selectById(id));
    }

    @PostMapping
    public R<Void> add(@RequestBody DmsDocumentRecordRevise revise) {
        if (revise.getStatus() == null) {
            revise.setStatus("edit");
        }
        baseMapper.insert(revise);
        return R.ok();
    }

    @PutMapping
    public R<Void> edit(@RequestBody DmsDocumentRecordRevise revise) {
        baseMapper.updateById(revise);
        return R.ok();
    }

    @DeleteMapping("/{id}")
    public R<Void> remove(@PathVariable Long id) {
        baseMapper.deleteById(id);
        return R.ok();
    }
}
