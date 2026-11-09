package org.dromara.dms.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.dms.domain.DmsDocRecordSnapshot;
import org.dromara.dms.mapper.DmsDocRecordSnapshotMapper;
import org.dromara.dms.util.DmsPages;
import org.springframework.web.bind.annotation.*;

/**
 * 记录快照（差异对比）
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/dms/doc-record-snapshot")
public class DmsDocRecordSnapshotController {

    private final DmsDocRecordSnapshotMapper baseMapper;

    @GetMapping("/list")
    public TableDataInfo<DmsDocRecordSnapshot> list(DmsDocRecordSnapshot query, PageQuery pageQuery) {
        QueryWrapper<DmsDocRecordSnapshot> qw = new QueryWrapper<>();
        qw.eq(query.getBizId() != null, "biz_id", query.getBizId());
        qw.eq(query.getDocumentRecordCode() != null, "document_record_code", query.getDocumentRecordCode());
        qw.eq(query.getDocumentRecordVersion() != null, "document_record_version", query.getDocumentRecordVersion());
        qw.eq(query.getDocumentRecordVersionMinor() != null, "document_record_version_minor", query.getDocumentRecordVersionMinor());
        qw.orderByDesc("create_time");
        return DmsPages.page(baseMapper, pageQuery, qw);
    }

    @GetMapping("/{id}")
    public R<DmsDocRecordSnapshot> detail(@PathVariable Long id) {
        return R.ok(baseMapper.selectById(id));
    }

    @PostMapping
    public R<Void> add(@RequestBody DmsDocRecordSnapshot snapshot) {
        baseMapper.insert(snapshot);
        return R.ok();
    }

    @PutMapping
    public R<Void> edit(@RequestBody DmsDocRecordSnapshot snapshot) {
        baseMapper.updateById(snapshot);
        return R.ok();
    }

    @DeleteMapping("/{id}")
    public R<Void> remove(@PathVariable Long id) {
        baseMapper.deleteById(id);
        return R.ok();
    }
}
