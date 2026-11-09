package org.dromara.dms.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.dms.domain.DmsDocRecordChangeOrder;
import org.dromara.dms.handler.DmsFlowSupport;
import org.dromara.dms.mapper.DmsDocRecordChangeOrderMapper;
import org.dromara.dms.util.DmsOrderNo;
import org.dromara.dms.util.DmsPages;
import org.springframework.web.bind.annotation.*;

/**
 * 记录变更单
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/dms/doc-record-change-order")
public class DmsDocRecordChangeOrderController {

    private final DmsDocRecordChangeOrderMapper baseMapper;
    private final DmsFlowSupport flowSupport;

    @GetMapping("/editList")
    public TableDataInfo<DmsDocRecordChangeOrder> editList(DmsDocRecordChangeOrder query, PageQuery pageQuery) {
        QueryWrapper<DmsDocRecordChangeOrder> qw = wrapper(query);
        qw.eq("status", "edit");
        qw.eq("create_by", LoginHelper.getUserId());
        return DmsPages.page(baseMapper, pageQuery, qw);
    }

    @GetMapping("/approvalList")
    public TableDataInfo<DmsDocRecordChangeOrder> approvalList(DmsDocRecordChangeOrder query, PageQuery pageQuery) {
        QueryWrapper<DmsDocRecordChangeOrder> qw = wrapper(query);
        qw.eq("status", "approving");
        return DmsPages.page(baseMapper, pageQuery, qw);
    }

    @GetMapping("/list")
    public TableDataInfo<DmsDocRecordChangeOrder> list(DmsDocRecordChangeOrder query, PageQuery pageQuery) {
        return DmsPages.page(baseMapper, pageQuery, wrapper(query));
    }

    @GetMapping("/{id}")
    public R<DmsDocRecordChangeOrder> detail(@PathVariable Long id) {
        return R.ok(baseMapper.selectById(id));
    }

    @GetMapping("/file/{id}")
    public R<String> file(@PathVariable Long id) {
        return R.ok();
    }

    @PostMapping
    public R<Void> add(@RequestBody DmsDocRecordChangeOrder order) {
        fillDefaults(order);
        baseMapper.insert(order);
        return R.ok();
    }

    @PutMapping
    public R<Void> edit(@RequestBody DmsDocRecordChangeOrder order) {
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
    public R<Void> submitAndFlowStart(@RequestBody DmsDocRecordChangeOrder order) {
        if (order.getApproveComments() != null) {
            DmsDocRecordChangeOrder update = new DmsDocRecordChangeOrder();
            update.setId(order.getId());
            update.setApproveComments(order.getApproveComments());
            baseMapper.updateById(update);
        }
        flowSupport.start("docRecordChangeOrder", order.getId());
        return R.ok();
    }

    private QueryWrapper<DmsDocRecordChangeOrder> wrapper(DmsDocRecordChangeOrder query) {
        QueryWrapper<DmsDocRecordChangeOrder> qw = new QueryWrapper<>();
        qw.eq(query.getStatus() != null, "status", query.getStatus());
        qw.like(query.getDocumentRecordCode() != null && !query.getDocumentRecordCode().isBlank(), "document_record_code", query.getDocumentRecordCode());
        qw.like(query.getDocumentRecordName() != null && !query.getDocumentRecordName().isBlank(), "document_record_name", query.getDocumentRecordName());
        qw.like(query.getDocumentCode() != null && !query.getDocumentCode().isBlank(), "document_code", query.getDocumentCode());
        qw.orderByDesc("create_time");
        return qw;
    }

    private void fillDefaults(DmsDocRecordChangeOrder order) {
        if (order.getStatus() == null) {
            order.setStatus("edit");
        }
        if (order.getOrderNumber() == null) {
            order.setOrderNumber(DmsOrderNo.next("RC"));
        }
        if (order.getDrafterId() == null) {
            order.setDrafterId(LoginHelper.getUserId());
        }
        if (order.getDrafterUserName() == null) {
            order.setDrafterUserName(LoginHelper.getUsername());
        }
    }
}
