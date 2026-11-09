package org.dromara.dms.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.dms.domain.DmsPrintApply;
import org.dromara.dms.handler.DmsFlowSupport;
import org.dromara.dms.mapper.DmsPrintApplyMapper;
import org.dromara.dms.util.DmsOrderNo;
import org.dromara.dms.util.DmsPages;
import org.springframework.web.bind.annotation.*;

/**
 * 打印申请
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/dms/print-apply")
public class DmsPrintApplyController {

    private final DmsPrintApplyMapper baseMapper;
    private final DmsFlowSupport flowSupport;

    @GetMapping("/list/main")
    public TableDataInfo<DmsPrintApply> listMain(DmsPrintApply query, PageQuery pageQuery) {
        return DmsPages.page(baseMapper, pageQuery, wrapper(query));
    }

    @GetMapping("/list")
    public TableDataInfo<DmsPrintApply> list(DmsPrintApply query, PageQuery pageQuery) {
        return DmsPages.page(baseMapper, pageQuery, wrapper(query));
    }

    @GetMapping("/list/approve")
    public TableDataInfo<DmsPrintApply> listApprove(DmsPrintApply query, PageQuery pageQuery) {
        query.setStatus("approving");
        return DmsPages.page(baseMapper, pageQuery, wrapper(query));
    }

    @GetMapping("/list/track")
    public TableDataInfo<DmsPrintApply> listTrack(DmsPrintApply query, PageQuery pageQuery) {
        return DmsPages.page(baseMapper, pageQuery, wrapper(query));
    }

    /** 打印页面列表（已审批 + 已终止） */
    @GetMapping("/list/print")
    public TableDataInfo<DmsPrintApply> listPrint(DmsPrintApply query, PageQuery pageQuery) {
        QueryWrapper<DmsPrintApply> qw = wrapper(query);
        qw.in("status", "approved", "stop");
        return DmsPages.page(baseMapper, pageQuery, qw);
    }

    @GetMapping("/{id}")
    public R<DmsPrintApply> detail(@PathVariable Long id) {
        return R.ok(baseMapper.selectById(id));
    }

    @GetMapping("/file/{id}")
    public R<String> file(@PathVariable Long id) {
        return R.ok();
    }

    @PostMapping
    public R<Void> add(@RequestBody DmsPrintApply apply) {
        if (apply.getStatus() == null) {
            apply.setStatus("edit");
        }
        if (apply.getApplyCode() == null) {
            apply.setApplyCode(DmsOrderNo.next("PA"));
        }
        if (apply.getPrintSource() == null) {
            apply.setPrintSource("add");
        }
        baseMapper.insert(apply);
        return R.ok();
    }

    @PutMapping
    public R<Void> edit(@RequestBody DmsPrintApply apply) {
        baseMapper.updateById(apply);
        return R.ok();
    }

    @DeleteMapping("/{ids}")
    public R<Void> remove(@PathVariable String ids) {
        for (String id : ids.split(",")) {
            baseMapper.deleteById(Long.valueOf(id.trim()));
        }
        return R.ok();
    }

    @PostMapping({"/submit-and-flow-start", "/submitAndFlowStart"})
    public R<Void> submitAndFlowStart(@RequestBody DmsPrintApply apply) {
        if (apply.getApproveComments() != null) {
            DmsPrintApply update = new DmsPrintApply();
            update.setId(apply.getId());
            update.setApproveComments(apply.getApproveComments());
            baseMapper.updateById(update);
        }
        flowSupport.start("printApply", apply.getId());
        return R.ok();
    }

    /** 打印文档（占位） */
    @PutMapping("/print/doc")
    public R<Void> printDoc(@RequestBody DmsPrintApply apply) {
        return R.ok();
    }

    /** 终止打印 */
    @PutMapping("/abend/{id}")
    public R<Void> abend(@PathVariable Long id) {
        DmsPrintApply update = new DmsPrintApply();
        update.setId(id);
        update.setStatus("stop");
        baseMapper.updateById(update);
        return R.ok();
    }

    private QueryWrapper<DmsPrintApply> wrapper(DmsPrintApply query) {
        QueryWrapper<DmsPrintApply> qw = new QueryWrapper<>();
        qw.eq(query.getStatus() != null, "status", query.getStatus());
        qw.eq(query.getPrintType() != null, "print_type", query.getPrintType());
        qw.like(query.getApplyCode() != null && !query.getApplyCode().isBlank(), "apply_code", query.getApplyCode());
        qw.like(query.getDocCode() != null && !query.getDocCode().isBlank(), "doc_code", query.getDocCode());
        qw.like(query.getDocName() != null && !query.getDocName().isBlank(), "doc_name", query.getDocName());
        qw.orderByDesc("create_time");
        return qw;
    }
}
