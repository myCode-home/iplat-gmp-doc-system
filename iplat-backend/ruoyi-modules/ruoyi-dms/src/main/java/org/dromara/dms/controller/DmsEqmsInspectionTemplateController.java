package org.dromara.dms.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.dms.domain.DmsEqmsInspectionTemplate;
import org.dromara.dms.mapper.DmsEqmsInspectionTemplateMapper;
import org.dromara.dms.util.DmsPages;
import org.springframework.web.bind.annotation.*;

/**
 * 设备检验模板（EQMS 最小实现）
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/eqms/template/confirm")
public class DmsEqmsInspectionTemplateController {

    private final DmsEqmsInspectionTemplateMapper baseMapper;

    @GetMapping("/list")
    public TableDataInfo<DmsEqmsInspectionTemplate> list(DmsEqmsInspectionTemplate query, PageQuery pageQuery) {
        QueryWrapper<DmsEqmsInspectionTemplate> qw = new QueryWrapper<>();
        qw.eq(query.getStatus() != null, "status", query.getStatus());
        qw.eq(query.getTemplateType() != null, "template_type", query.getTemplateType());
        qw.like(query.getTemplateName() != null && !query.getTemplateName().isBlank(), "template_name", query.getTemplateName());
        qw.orderByDesc("create_time");
        return DmsPages.page(baseMapper, pageQuery, qw);
    }

    @GetMapping("/{id}")
    public R<DmsEqmsInspectionTemplate> detail(@PathVariable Long id) {
        return R.ok(baseMapper.selectById(id));
    }

    @PostMapping
    public R<Void> add(@RequestBody DmsEqmsInspectionTemplate template) {
        if (template.getStatus() == null) {
            template.setStatus("active");
        }
        baseMapper.insert(template);
        return R.ok();
    }

    @PutMapping
    public R<Void> edit(@RequestBody DmsEqmsInspectionTemplate template) {
        baseMapper.updateById(template);
        return R.ok();
    }

    @DeleteMapping("/{ids}")
    public R<Void> remove(@PathVariable String ids) {
        for (String id : ids.split(",")) {
            baseMapper.deleteById(Long.valueOf(id.trim()));
        }
        return R.ok();
    }
}
