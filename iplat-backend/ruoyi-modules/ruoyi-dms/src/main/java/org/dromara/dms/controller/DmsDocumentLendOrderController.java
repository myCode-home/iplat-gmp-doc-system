package org.dromara.dms.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.dms.domain.DmsDocumentLendOrder;
import org.dromara.dms.handler.DmsFlowSupport;
import org.dromara.dms.mapper.DmsDocumentLendOrderMapper;
import org.dromara.dms.util.DmsOrderNo;
import org.dromara.dms.util.DmsPages;
import org.springframework.web.bind.annotation.*;

/**
 * 文件借阅单
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/dms/document-lend-order")
public class DmsDocumentLendOrderController {

    private final DmsDocumentLendOrderMapper baseMapper;
    private final DmsFlowSupport flowSupport;

    /** 全量（追踪页因前端调用的是 approvalList，这里两者等价） */
    @GetMapping("/list")
    public TableDataInfo<DmsDocumentLendOrder> list(DmsDocumentLendOrder query, PageQuery pageQuery) {
        return DmsPages.page(baseMapper, pageQuery, wrapper(query));
    }

    @GetMapping("/approvalList")
    public TableDataInfo<DmsDocumentLendOrder> approvalList(DmsDocumentLendOrder query, PageQuery pageQuery) {
        return DmsPages.page(baseMapper, pageQuery, wrapper(query));
    }

    @GetMapping("/trackList")
    public TableDataInfo<DmsDocumentLendOrder> trackList(DmsDocumentLendOrder query, PageQuery pageQuery) {
        return DmsPages.page(baseMapper, pageQuery, wrapper(query));
    }

    @GetMapping("/{id}")
    public R<DmsDocumentLendOrder> detail(@PathVariable Long id) {
        return R.ok(baseMapper.selectById(id));
    }

    @PostMapping
    public R<Void> add(@RequestBody DmsDocumentLendOrder order) {
        if (order.getStatus() == null) {
            order.setStatus("edit");
        }
        if (order.getOrderNumber() == null) {
            order.setOrderNumber(DmsOrderNo.next("LEND"));
        }
        baseMapper.insert(order);
        return R.ok();
    }

    @PutMapping
    public R<Void> edit(@RequestBody DmsDocumentLendOrder order) {
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

    @PostMapping("/submitAndFlowStart")
    public R<Void> submitAndFlowStart(@RequestBody DmsDocumentLendOrder order) {
        if (order.getApproveComments() != null) {
            DmsDocumentLendOrder update = new DmsDocumentLendOrder();
            update.setId(order.getId());
            update.setApproveComments(order.getApproveComments());
            baseMapper.updateById(update);
        }
        flowSupport.start("documentLendOrder", order.getId());
        return R.ok();
    }

    private QueryWrapper<DmsDocumentLendOrder> wrapper(DmsDocumentLendOrder query) {
        QueryWrapper<DmsDocumentLendOrder> qw = new QueryWrapper<>();
        qw.eq(query.getStatus() != null, "status", query.getStatus());
        qw.like(query.getOrderNumber() != null && !query.getOrderNumber().isBlank(), "order_number", query.getOrderNumber());
        qw.like(query.getDocumentCode() != null && !query.getDocumentCode().isBlank(), "document_code", query.getDocumentCode());
        qw.like(query.getDocumentName() != null && !query.getDocumentName().isBlank(), "document_name", query.getDocumentName());
        qw.orderByDesc("create_time");
        return qw;
    }
}
