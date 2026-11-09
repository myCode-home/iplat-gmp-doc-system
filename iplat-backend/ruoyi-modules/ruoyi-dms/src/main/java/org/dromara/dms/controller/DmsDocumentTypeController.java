package org.dromara.dms.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.dms.domain.DmsDocumentType;
import org.dromara.dms.mapper.DmsDocumentTypeMapper;
import org.dromara.dms.util.DmsPages;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 文件类型（树形维护）
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/dms/document-type-maintenance")
public class DmsDocumentTypeController {

    private final DmsDocumentTypeMapper baseMapper;

    /** 扁平列表（前端自行组树，pageSize 传 9999） */
    @GetMapping("/list")
    public TableDataInfo<DmsDocumentType> list(DmsDocumentType query, PageQuery pageQuery) {
        QueryWrapper<DmsDocumentType> qw = new QueryWrapper<>();
        qw.eq(query.getStatus() != null, "status", query.getStatus());
        qw.like(query.getSortName() != null && !query.getSortName().isBlank(), "sort_name", query.getSortName());
        qw.orderByAsc("sort_code");
        return DmsPages.page(baseMapper, pageQuery, qw);
    }

    @PostMapping
    public R<Void> add(@RequestBody DmsDocumentType type) {
        if (type.getParentId() == null || type.getParentId().isBlank()) {
            type.setParentId("root");
        }
        if (type.getParentName() != null && !type.getParentName().isBlank() && !"root".equals(type.getParentId())) {
            DmsDocumentType parent = baseMapper.selectById(type.getParentId());
            type.setParentName(parent == null ? null : parent.getSortName());
        } else if ("root".equals(type.getParentId())) {
            type.setParentName(null);
        }
        if (type.getStatus() == null) {
            type.setStatus("active");
        }
        baseMapper.insert(type);
        return R.ok();
    }

    @PutMapping
    public R<Void> edit(@RequestBody DmsDocumentType type) {
        baseMapper.updateById(type);
        return R.ok();
    }

    @DeleteMapping("/{id}")
    public R<Void> remove(@PathVariable String id) {
        baseMapper.deleteById(id);
        return R.ok();
    }

    @PutMapping("/enable/{id}")
    public R<Void> enable(@PathVariable String id) {
        DmsDocumentType update = new DmsDocumentType();
        update.setId(id);
        update.setStatus("active");
        baseMapper.updateById(update);
        return R.ok();
    }

    @PutMapping("/disable/{id}")
    public R<Void> disable(@PathVariable String id) {
        DmsDocumentType update = new DmsDocumentType();
        update.setId(id);
        update.setStatus("failure");
        baseMapper.updateById(update);
        return R.ok();
    }
}
