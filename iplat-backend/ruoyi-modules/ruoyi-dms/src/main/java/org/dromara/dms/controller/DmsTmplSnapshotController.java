package org.dromara.dms.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.dms.domain.DmsTmplSnapshot;
import org.dromara.dms.mapper.DmsTmplSnapshotMapper;
import org.dromara.dms.util.DmsPages;
import org.springframework.web.bind.annotation.*;

/**
 * 模板快照（差异对比）
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/dms/tmplSnapshot")
public class DmsTmplSnapshotController {

    private final DmsTmplSnapshotMapper baseMapper;

    @GetMapping("/list")
    public TableDataInfo<DmsTmplSnapshot> list(DmsTmplSnapshot query, PageQuery pageQuery) {
        QueryWrapper<DmsTmplSnapshot> qw = new QueryWrapper<>();
        qw.eq(query.getBizId() != null, "biz_id", query.getBizId());
        qw.eq(query.getTmplCode() != null, "tmpl_code", query.getTmplCode());
        qw.eq(query.getTmplVersion() != null, "tmpl_version", query.getTmplVersion());
        qw.eq(query.getTmplVersionMinor() != null, "tmpl_version_minor", query.getTmplVersionMinor());
        qw.orderByDesc("create_time");
        return DmsPages.page(baseMapper, pageQuery, qw);
    }

    @GetMapping("/{id}")
    public R<DmsTmplSnapshot> detail(@PathVariable Long id) {
        return R.ok(baseMapper.selectById(id));
    }

    @PostMapping
    public R<Void> add(@RequestBody DmsTmplSnapshot snapshot) {
        baseMapper.insert(snapshot);
        return R.ok();
    }

    @PutMapping
    public R<Void> edit(@RequestBody DmsTmplSnapshot snapshot) {
        baseMapper.updateById(snapshot);
        return R.ok();
    }

    @DeleteMapping("/{id}")
    public R<Void> remove(@PathVariable Long id) {
        baseMapper.deleteById(id);
        return R.ok();
    }
}
