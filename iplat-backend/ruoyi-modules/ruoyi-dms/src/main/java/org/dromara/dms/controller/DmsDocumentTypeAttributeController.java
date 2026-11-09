package org.dromara.dms.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.dms.domain.DmsDocumentTypeAttribute;
import org.dromara.dms.mapper.DmsDocumentTypeAttributeMapper;
import org.dromara.dms.util.DmsPages;
import org.springframework.web.bind.annotation.*;

/**
 * 文件类型属性配置
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/dms/document-type-attribute-config")
public class DmsDocumentTypeAttributeController {

    private final DmsDocumentTypeAttributeMapper baseMapper;

    /** 按文件类型查询属性（前端按 status 过滤 active） */
    @GetMapping("/list")
    public TableDataInfo<DmsDocumentTypeAttribute> list(@RequestParam(required = false) String sortId, PageQuery pageQuery) {
        QueryWrapper<DmsDocumentTypeAttribute> qw = new QueryWrapper<>();
        qw.eq(sortId != null && !sortId.isBlank(), "sort_id", sortId);
        qw.orderByAsc("sort");
        return DmsPages.page(baseMapper, pageQuery, qw);
    }

    @PostMapping
    public R<Void> add(@RequestBody DmsDocumentTypeAttribute attribute) {
        if (attribute.getStatus() == null) {
            attribute.setStatus("active");
        }
        baseMapper.insert(attribute);
        return R.ok();
    }

    @PutMapping
    public R<Void> edit(@RequestBody DmsDocumentTypeAttribute attribute) {
        baseMapper.updateById(attribute);
        return R.ok();
    }

    @DeleteMapping("/{id}")
    public R<Void> remove(@PathVariable Long id) {
        baseMapper.deleteById(id);
        return R.ok();
    }

    @PutMapping("/enable/{id}")
    public R<Void> enable(@PathVariable Long id) {
        DmsDocumentTypeAttribute update = new DmsDocumentTypeAttribute();
        update.setId(id);
        update.setStatus("active");
        baseMapper.updateById(update);
        return R.ok();
    }

    @PutMapping("/disable/{id}")
    public R<Void> disable(@PathVariable Long id) {
        DmsDocumentTypeAttribute update = new DmsDocumentTypeAttribute();
        update.setId(id);
        update.setStatus("failure");
        baseMapper.updateById(update);
        return R.ok();
    }
}
