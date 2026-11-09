package org.dromara.dms.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.dms.domain.DmsDocCreateSnapshot;
import org.dromara.dms.mapper.DmsDocCreateSnapshotMapper;
import org.dromara.dms.util.DmsPages;
import org.springframework.web.bind.annotation.*;

/**
 * 文档快照（差异对比）
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/dms/doc-create-snapshot")
public class DmsDocCreateSnapshotController {

    private final DmsDocCreateSnapshotMapper baseMapper;

    @GetMapping("/list")
    public TableDataInfo<DmsDocCreateSnapshot> list(DmsDocCreateSnapshot query, PageQuery pageQuery) {
        QueryWrapper<DmsDocCreateSnapshot> qw = new QueryWrapper<>();
        qw.eq(query.getBizId() != null, "biz_id", query.getBizId());
        qw.eq(query.getDocumentCode() != null, "document_code", query.getDocumentCode());
        qw.eq(query.getDocumentVersion() != null, "document_version", query.getDocumentVersion());
        qw.eq(query.getDocumentVersionMinor() != null, "document_version_minor", query.getDocumentVersionMinor());
        qw.orderByDesc("create_time");
        return DmsPages.page(baseMapper, pageQuery, qw);
    }

    @GetMapping("/{id}")
    public R<DmsDocCreateSnapshot> detail(@PathVariable Long id) {
        return R.ok(baseMapper.selectById(id));
    }

    @PostMapping
    public R<Void> add(@RequestBody DmsDocCreateSnapshot snapshot) {
        baseMapper.insert(snapshot);
        return R.ok();
    }

    @PutMapping
    public R<Void> edit(@RequestBody DmsDocCreateSnapshot snapshot) {
        baseMapper.updateById(snapshot);
        return R.ok();
    }

    @DeleteMapping("/{id}")
    public R<Void> remove(@PathVariable Long id) {
        baseMapper.deleteById(id);
        return R.ok();
    }
}
