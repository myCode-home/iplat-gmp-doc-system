package org.dromara.dms.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.dms.domain.DmsVolumesPrintApply;
import org.dromara.dms.domain.DmsVolumesPrintApplyDetail;
import org.dromara.dms.handler.DmsFlowSupport;
import org.dromara.dms.mapper.DmsVolumesPrintApplyDetailMapper;
import org.dromara.dms.mapper.DmsVolumesPrintApplyMapper;
import org.dromara.dms.util.DmsOrderNo;
import org.dromara.dms.util.DmsPages;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 成册打印申请
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/dms/volumes-print-apply")
public class DmsVolumesPrintApplyController {

    private final DmsVolumesPrintApplyMapper baseMapper;
    private final DmsVolumesPrintApplyDetailMapper detailMapper;
    private final DmsFlowSupport flowSupport;

    @GetMapping("/list/main")
    public TableDataInfo<DmsVolumesPrintApply> listMain(DmsVolumesPrintApply query, PageQuery pageQuery) {
        return DmsPages.page(baseMapper, pageQuery, wrapper(query));
    }

    @GetMapping("/list/approve")
    public TableDataInfo<DmsVolumesPrintApply> listApprove(DmsVolumesPrintApply query, PageQuery pageQuery) {
        query.setStatus("approving");
        return DmsPages.page(baseMapper, pageQuery, wrapper(query));
    }

    @GetMapping("/list/track")
    public TableDataInfo<DmsVolumesPrintApply> listTrack(DmsVolumesPrintApply query, PageQuery pageQuery) {
        return DmsPages.page(baseMapper, pageQuery, wrapper(query));
    }

    /** 打印页面列表（已审批） */
    @GetMapping("/list/print")
    public TableDataInfo<DmsVolumesPrintApply> listPrint(DmsVolumesPrintApply query, PageQuery pageQuery) {
        query.setStatus("approved");
        return DmsPages.page(baseMapper, pageQuery, wrapper(query));
    }

    @GetMapping("/{id}")
    public R<DmsVolumesPrintApply> detail(@PathVariable Long id) {
        DmsVolumesPrintApply apply = baseMapper.selectById(id);
        if (apply != null) {
            apply.setDetails(detailMapper.selectList(
                new QueryWrapper<DmsVolumesPrintApplyDetail>().lambda().eq(DmsVolumesPrintApplyDetail::getApplyId, id)));
        }
        return R.ok(apply);
    }

    @GetMapping("/file/{id}")
    public R<String> file(@PathVariable Long id) {
        return R.ok();
    }

    /** 成册明细文件列表（data 数组） */
    @GetMapping("/volumes-print-apply-detail/list/{id}")
    public R<List<DmsVolumesPrintApplyDetail>> detailList(@PathVariable Long id) {
        return R.ok(detailMapper.selectList(
            new QueryWrapper<DmsVolumesPrintApplyDetail>().lambda().eq(DmsVolumesPrintApplyDetail::getApplyId, id)));
    }

    @PostMapping
    public R<Void> add(@RequestBody DmsVolumesPrintApply apply) {
        if (apply.getStatus() == null) {
            apply.setStatus("edit");
        }
        if (apply.getApplyCode() == null) {
            apply.setApplyCode(DmsOrderNo.next("BOOK"));
        }
        if (apply.getPrintSource() == null) {
            apply.setPrintSource("add");
        }
        baseMapper.insert(apply);
        saveDetails(apply);
        return R.ok();
    }

    @PutMapping
    public R<Void> edit(@RequestBody DmsVolumesPrintApply apply) {
        baseMapper.updateById(apply);
        saveDetails(apply);
        return R.ok();
    }

    @DeleteMapping("/{ids}")
    public R<Void> remove(@PathVariable String ids) {
        for (String id : ids.split(",")) {
            baseMapper.deleteById(Long.valueOf(id.trim()));
            detailMapper.delete(new QueryWrapper<DmsVolumesPrintApplyDetail>().lambda()
                .eq(DmsVolumesPrintApplyDetail::getApplyId, Long.valueOf(id.trim())));
        }
        return R.ok();
    }

    @PostMapping("/submitAndFlowStart")
    public R<Void> submitAndFlowStart(@RequestBody DmsVolumesPrintApply apply) {
        if (apply.getApproveComments() != null) {
            DmsVolumesPrintApply update = new DmsVolumesPrintApply();
            update.setId(apply.getId());
            update.setApproveComments(apply.getApproveComments());
            baseMapper.updateById(update);
        }
        flowSupport.start("volumesPrintApply", apply.getId());
        return R.ok();
    }

    private QueryWrapper<DmsVolumesPrintApply> wrapper(DmsVolumesPrintApply query) {
        QueryWrapper<DmsVolumesPrintApply> qw = new QueryWrapper<>();
        qw.eq(query.getStatus() != null, "status", query.getStatus());
        qw.eq(query.getState() != null, "state", query.getState());
        qw.like(query.getApplyCode() != null && !query.getApplyCode().isBlank(), "apply_code", query.getApplyCode());
        qw.like(query.getVolumesName() != null && !query.getVolumesName().isBlank(), "volumes_name", query.getVolumesName());
        qw.orderByDesc("create_time");
        return qw;
    }

    private void saveDetails(DmsVolumesPrintApply apply) {
        List<DmsVolumesPrintApplyDetail> details = apply.getDetails();
        if (details == null || details.isEmpty()) {
            return;
        }
        detailMapper.delete(new QueryWrapper<DmsVolumesPrintApplyDetail>().lambda()
            .eq(DmsVolumesPrintApplyDetail::getApplyId, apply.getId()));
        for (DmsVolumesPrintApplyDetail detail : details) {
            detail.setId(null);
            detail.setApplyId(apply.getId());
            detailMapper.insert(detail);
        }
    }
}
