package org.dromara.dms.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.dms.domain.DmsDocRecordFailureOrder;
import org.dromara.dms.handler.DmsFlowSupport;
import org.dromara.dms.mapper.DmsDocRecordFailureOrderMapper;
import org.dromara.dms.util.DmsOrderNo;
import org.dromara.dms.util.DmsPages;
import org.springframework.web.bind.annotation.*;

/**
 * 记录失效单
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/dms/doc-record-failure-order")
public class DmsDocRecordFailureOrderController {

    private final DmsDocRecordFailureOrderMapper baseMapper;
    private final DmsFlowSupport flowSupport;

    @GetMapping("/editList")
    public TableDataInfo<DmsDocRecordFailureOrder> editList(DmsDocRecordFailureOrder query, PageQuery pageQuery) {
        QueryWrapper<DmsDocRecordFailureOrder> qw = wrapper(query);
        qw.eq("status", "edit");
        qw.eq("create_by", LoginHelper.getUserId());
        return DmsPages.page(baseMapper, pageQuery, qw);
    }

    @GetMapping("/approvalList")
    public TableDataInfo<DmsDocRecordFailureOrder> approvalList(DmsDocRecordFailureOrder query, PageQuery pageQuery) {
        QueryWrapper<DmsDocRecordFailureOrder> qw = wrapper(query);
        qw.eq("status", "approving");
        return DmsPages.page(baseMapper, pageQuery, qw);
    }

    @GetMapping("/list")
    public TableDataInfo<DmsDocRecordFailureOrder> list(DmsDocRecordFailureOrder query, PageQuery pageQuery) {
        return DmsPages.page(baseMapper, pageQuery, wrapper(query));
    }

    @GetMapping("/{id}")
    public R<DmsDocRecordFailureOrder> detail(@PathVariable Long id) {
        return R.ok(baseMapper.selectById(id));
    }

    @PostMapping
    public R<Void> add(@RequestBody DmsDocRecordFailureOrder order) {
        if (order.getStatus() == null) {
            order.setStatus("edit");
        }
        if (order.getOrderNumber() == null) {
            order.setOrderNumber(DmsOrderNo.next("RINV"));
        }
        baseMapper.insert(order);
        return R.ok();
    }

    @PutMapping
    public R<Void> edit(@RequestBody DmsDocRecordFailureOrder order) {
        baseMapper.updateById(order);
        return R.ok();
    }

    @DeleteMapping("/{ids}")
    public R<Void> remove(@PathVariable String ids) {
        for (String id : ids.split(",")) {
            baseMapper.deleteById(Long.valueOf(id.trim()));
        }
        return R.ok();
    }

    @PostMapping("/export")
    public R<Void> export() {
        return R.ok();
    }

    @PostMapping("/submitAndFlowStart")
    public R<Void> submitAndFlowStart(@RequestBody DmsDocRecordFailureOrder order) {
        if (order.getApproveComments() != null) {
            DmsDocRecordFailureOrder update = new DmsDocRecordFailureOrder();
            update.setId(order.getId());
            update.setApproveComments(order.getApproveComments());
            baseMapper.updateById(update);
        }
        flowSupport.start("docRecordFailureOrder", order.getId());
        return R.ok();
    }

    private QueryWrapper<DmsDocRecordFailureOrder> wrapper(DmsDocRecordFailureOrder query) {
        QueryWrapper<DmsDocRecordFailureOrder> qw = new QueryWrapper<>();
        qw.eq(query.getStatus() != null, "status", query.getStatus());
        qw.like(query.getOrderNumber() != null && !query.getOrderNumber().isBlank(), "order_number", query.getOrderNumber());
        qw.like(query.getDocumentRecordReason() != null && !query.getDocumentRecordReason().isBlank(), "document_record_reason", query.getDocumentRecordReason());
        qw.orderByDesc("create_time");
        return qw;
    }
}
