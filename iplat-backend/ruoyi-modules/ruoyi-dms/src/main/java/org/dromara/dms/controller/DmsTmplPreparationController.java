package org.dromara.dms.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.dms.domain.DmsTmplPreparation;
import org.dromara.dms.handler.DmsFlowSupport;
import org.dromara.dms.mapper.DmsTmplPreparationMapper;
import org.dromara.dms.util.DmsPages;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 模板编制 / 审批 / 追踪
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/dms/tmplPreparation")
public class DmsTmplPreparationController {

    private final DmsTmplPreparationMapper baseMapper;
    private final DmsFlowSupport flowSupport;

    /** 编制列表（本人草稿） */
    @GetMapping("/edit/list")
    public TableDataInfo<DmsTmplPreparation> editList(DmsTmplPreparation query, PageQuery pageQuery) {
        QueryWrapper<DmsTmplPreparation> qw = wrapper(query);
        qw.eq("status", "edit");
        qw.eq("create_by", LoginHelper.getUserId());
        return DmsPages.page(baseMapper, pageQuery, qw);
    }

    /** 审批列表 */
    @GetMapping("/approval/list")
    public TableDataInfo<DmsTmplPreparation> approvalList(DmsTmplPreparation query, PageQuery pageQuery) {
        QueryWrapper<DmsTmplPreparation> qw = wrapper(query);
        qw.eq("status", "approving");
        return DmsPages.page(baseMapper, pageQuery, qw);
    }

    /** 全量列表（追踪） */
    @GetMapping("/list")
    public TableDataInfo<DmsTmplPreparation> list(DmsTmplPreparation query, PageQuery pageQuery) {
        return DmsPages.page(baseMapper, pageQuery, wrapper(query));
    }

    @GetMapping("/listwithsnapshot")
    public TableDataInfo<DmsTmplPreparation> listWithSnapshot(DmsTmplPreparation query, PageQuery pageQuery) {
        return list(query, pageQuery);
    }

    @GetMapping("/{id}")
    public R<DmsTmplPreparation> detail(@PathVariable Long id) {
        return R.ok(baseMapper.selectById(id));
    }

    @GetMapping("/file/{id}")
    public R<String> file(@PathVariable Long id) {
        return R.ok();
    }

    @PostMapping
    public R<Void> add(@RequestBody DmsTmplPreparation tmpl) {
        fillDefaults(tmpl);
        baseMapper.insert(tmpl);
        return R.ok();
    }

    @PutMapping
    public R<Void> edit(@RequestBody DmsTmplPreparation tmpl) {
        baseMapper.updateById(tmpl);
        return R.ok();
    }

    @DeleteMapping("/{ids}")
    public R<Void> remove(@PathVariable String ids) {
        for (String id : ids.split(",")) {
            baseMapper.deleteById(Long.valueOf(id.trim()));
        }
        return R.ok();
    }

    /** 提交并启动审批流 */
    @PostMapping("/submitAndFlowStart")
    public R<Void> submitAndFlowStart(@RequestBody DmsTmplPreparation tmpl) {
        if (tmpl.getApproveComments() != null) {
            DmsTmplPreparation update = new DmsTmplPreparation();
            update.setId(tmpl.getId());
            update.setApproveComments(tmpl.getApproveComments());
            baseMapper.updateById(update);
        }
        flowSupport.start("tmplPreparation", tmpl.getId());
        return R.ok();
    }

    private QueryWrapper<DmsTmplPreparation> wrapper(DmsTmplPreparation query) {
        QueryWrapper<DmsTmplPreparation> qw = new QueryWrapper<>();
        qw.eq(query.getStatus() != null, "status", query.getStatus());
        qw.eq(query.getTmplCategory() != null, "tmpl_category", query.getTmplCategory());
        qw.eq(query.getTmplPurpose() != null, "tmpl_purpose", query.getTmplPurpose());
        qw.like(query.getTmplCode() != null && !query.getTmplCode().isBlank(), "tmpl_code", query.getTmplCode());
        qw.like(query.getTmplName() != null && !query.getTmplName().isBlank(), "tmpl_name", query.getTmplName());
        qw.orderByDesc("create_time");
        return qw;
    }

    private void fillDefaults(DmsTmplPreparation tmpl) {
        if (tmpl.getStatus() == null) {
            tmpl.setStatus("edit");
        }
        if (tmpl.getTmplDeptName() == null) {
            tmpl.setTmplDeptName(LoginHelper.getDeptName());
        }
        if (tmpl.getTmplDept() == null) {
            tmpl.setTmplDept(LoginHelper.getDeptId());
        }
    }
}
