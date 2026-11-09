package org.dromara.dms.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.dms.domain.DmsTmplLib;
import org.dromara.dms.domain.DmsTmplPreparation;
import org.dromara.dms.mapper.DmsTmplLibMapper;
import org.dromara.dms.mapper.DmsTmplPreparationMapper;
import org.dromara.dms.util.DmsPages;
import org.springframework.web.bind.annotation.*;

/**
 * 模板库
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/dms/tmplLib")
public class DmsTmplLibController {

    private final DmsTmplLibMapper baseMapper;
    private final DmsTmplPreparationMapper preparationMapper;

    /** 生效模板库 */
    @GetMapping("/active/list")
    public TableDataInfo<DmsTmplLib> activeList(DmsTmplLib query, PageQuery pageQuery) {
        query.setStatus("active");
        query.setActiveStatus("active");
        return DmsPages.page(baseMapper, pageQuery, wrapper(query));
    }

    /** 历史模板库（支持按编号展开版本） */
    @GetMapping("/history/list")
    public TableDataInfo<DmsTmplLib> historyList(DmsTmplLib query, PageQuery pageQuery) {
        return DmsPages.page(baseMapper, pageQuery, wrapper(query));
    }

    @GetMapping("/{id}")
    public R<DmsTmplLib> detail(@PathVariable Long id) {
        return R.ok(baseMapper.selectById(id));
    }

    @PostMapping
    public R<Void> add(@RequestBody DmsTmplLib lib) {
        if (lib.getStatus() == null) {
            lib.setStatus("active");
        }
        if (lib.getActiveStatus() == null) {
            lib.setActiveStatus("active");
        }
        baseMapper.insert(lib);
        return R.ok();
    }

    @PutMapping
    public R<Void> edit(@RequestBody DmsTmplLib lib) {
        baseMapper.updateById(lib);
        return R.ok();
    }

    @DeleteMapping("/{ids}")
    public R<Void> remove(@PathVariable String ids) {
        for (String id : ids.split(",")) {
            baseMapper.deleteById(Long.valueOf(id.trim()));
        }
        return R.ok();
    }

    /** 申请修订：生成模板编制单 */
    @PostMapping("/revise")
    public R<Void> revise(@RequestBody DmsTmplLib form) {
        DmsTmplLib lib = baseMapper.selectById(form.getId());
        if (lib == null) {
            return R.fail("模板不存在");
        }
        DmsTmplPreparation prep = new DmsTmplPreparation();
        prep.setTmplCode(lib.getTmplCode());
        prep.setTmplName(form.getTmplName() == null ? lib.getTmplName() : form.getTmplName());
        prep.setTmplCategory(lib.getTmplCategory());
        prep.setTmplVersion(lib.getTmplVersion());
        prep.setTmplVersionMinor(lib.getTmplVersionMinor());
        prep.setTmplPurpose(lib.getTmplPurpose());
        prep.setTmplDept(lib.getTmplDept());
        prep.setTmplDeptName(lib.getTmplDeptName());
        prep.setStatus("edit");
        preparationMapper.insert(prep);
        lib.setActiveStatus("revising");
        baseMapper.updateById(lib);
        return R.ok();
    }

    /** 申请失效 */
    @PostMapping("/invalid")
    public R<Void> invalid(@RequestBody DmsTmplLib form) {
        DmsTmplLib lib = baseMapper.selectById(form.getId());
        if (lib == null) {
            return R.fail("模板不存在");
        }
        lib.setStatus("failure");
        lib.setActiveStatus("deactiving");
        baseMapper.updateById(lib);
        return R.ok();
    }

    private QueryWrapper<DmsTmplLib> wrapper(DmsTmplLib query) {
        QueryWrapper<DmsTmplLib> qw = new QueryWrapper<>();
        qw.eq(query.getStatus() != null, "status", query.getStatus());
        qw.eq(query.getActiveStatus() != null, "active_status", query.getActiveStatus());
        qw.eq(query.getTmplCategory() != null, "tmpl_category", query.getTmplCategory());
        qw.eq(query.getTmplPurpose() != null, "tmpl_purpose", query.getTmplPurpose());
        qw.like(query.getTmplCode() != null && !query.getTmplCode().isBlank(), "tmpl_code", query.getTmplCode());
        qw.like(query.getTmplName() != null && !query.getTmplName().isBlank(), "tmpl_name", query.getTmplName());
        qw.orderByDesc("create_time");
        return qw;
    }
}
