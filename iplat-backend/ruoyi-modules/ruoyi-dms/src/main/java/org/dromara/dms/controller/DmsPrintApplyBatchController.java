package org.dromara.dms.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.dms.domain.DmsPrintApplyBatch;
import org.dromara.dms.domain.DmsPrintApplyBatchDetail;
import org.dromara.dms.handler.DmsFlowSupport;
import org.dromara.dms.mapper.DmsPrintApplyBatchDetailMapper;
import org.dromara.dms.mapper.DmsPrintApplyBatchMapper;
import org.dromara.dms.util.DmsOrderNo;
import org.dromara.dms.util.DmsPages;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 批量打印申请
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/dms/print-apply-batch")
public class DmsPrintApplyBatchController {

    private final DmsPrintApplyBatchMapper baseMapper;
    private final DmsPrintApplyBatchDetailMapper detailMapper;
    private final DmsFlowSupport flowSupport;

    @GetMapping("/list/main")
    public TableDataInfo<DmsPrintApplyBatch> listMain(DmsPrintApplyBatch query, PageQuery pageQuery) {
        return DmsPages.page(baseMapper, pageQuery, wrapper(query));
    }

    @GetMapping("/list")
    public TableDataInfo<DmsPrintApplyBatch> list(DmsPrintApplyBatch query, PageQuery pageQuery) {
        return DmsPages.page(baseMapper, pageQuery, wrapper(query));
    }

    @GetMapping("/list/approve")
    public TableDataInfo<DmsPrintApplyBatch> listApprove(DmsPrintApplyBatch query, PageQuery pageQuery) {
        query.setStatus("approving");
        return DmsPages.page(baseMapper, pageQuery, wrapper(query));
    }

    @GetMapping("/list/track")
    public TableDataInfo<DmsPrintApplyBatch> listTrack(DmsPrintApplyBatch query, PageQuery pageQuery) {
        return DmsPages.page(baseMapper, pageQuery, wrapper(query));
    }

    @GetMapping("/{id}")
    public R<DmsPrintApplyBatch> detail(@PathVariable Long id) {
        DmsPrintApplyBatch batch = baseMapper.selectById(id);
        if (batch != null) {
            batch.setDetails(detailMapper.selectList(
                new QueryWrapper<DmsPrintApplyBatchDetail>().lambda().eq(DmsPrintApplyBatchDetail::getBatchId, id)));
        }
        return R.ok(batch);
    }

    @PostMapping
    public R<Void> add(@RequestBody DmsPrintApplyBatch batch) {
        if (batch.getStatus() == null) {
            batch.setStatus("edit");
        }
        if (batch.getApplyCode() == null) {
            batch.setApplyCode(DmsOrderNo.next("BP"));
        }
        baseMapper.insert(batch);
        saveDetails(batch);
        return R.ok();
    }

    @PutMapping
    public R<Void> edit(@RequestBody DmsPrintApplyBatch batch) {
        baseMapper.updateById(batch);
        saveDetails(batch);
        return R.ok();
    }

    @DeleteMapping("/{ids}")
    public R<Void> remove(@PathVariable String ids) {
        for (String id : ids.split(",")) {
            baseMapper.deleteById(Long.valueOf(id.trim()));
            detailMapper.delete(new QueryWrapper<DmsPrintApplyBatchDetail>().lambda()
                .eq(DmsPrintApplyBatchDetail::getBatchId, Long.valueOf(id.trim())));
        }
        return R.ok();
    }

    @PostMapping("/export")
    public R<Void> export() {
        return R.ok();
    }

    @PostMapping("/submit-and-flow-start")
    public R<Void> submitAndFlowStart(@RequestBody DmsPrintApplyBatch batch) {
        if (batch.getApproveComments() != null) {
            DmsPrintApplyBatch update = new DmsPrintApplyBatch();
            update.setId(batch.getId());
            update.setApproveComments(batch.getApproveComments());
            baseMapper.updateById(update);
        }
        flowSupport.start("printApplyBatch", batch.getId());
        return R.ok();
    }

    private QueryWrapper<DmsPrintApplyBatch> wrapper(DmsPrintApplyBatch query) {
        QueryWrapper<DmsPrintApplyBatch> qw = new QueryWrapper<>();
        qw.eq(query.getStatus() != null, "status", query.getStatus());
        qw.like(query.getApplyCode() != null && !query.getApplyCode().isBlank(), "apply_code", query.getApplyCode());
        qw.like(query.getApplyReason() != null && !query.getApplyReason().isBlank(), "apply_reason", query.getApplyReason());
        qw.like(query.getDocCode() != null && !query.getDocCode().isBlank(), "doc_code", query.getDocCode());
        qw.like(query.getDocName() != null && !query.getDocName().isBlank(), "doc_name", query.getDocName());
        qw.orderByDesc("create_time");
        return qw;
    }

    private void saveDetails(DmsPrintApplyBatch batch) {
        List<DmsPrintApplyBatchDetail> details = batch.getDetails();
        if (details == null || details.isEmpty()) {
            return;
        }
        detailMapper.delete(new QueryWrapper<DmsPrintApplyBatchDetail>().lambda()
            .eq(DmsPrintApplyBatchDetail::getBatchId, batch.getId()));
        for (DmsPrintApplyBatchDetail detail : details) {
            detail.setId(null);
            detail.setBatchId(batch.getId());
            detailMapper.insert(detail);
        }
    }
}
