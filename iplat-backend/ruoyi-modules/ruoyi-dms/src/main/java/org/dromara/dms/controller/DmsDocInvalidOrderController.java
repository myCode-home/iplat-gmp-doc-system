package org.dromara.dms.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.dms.domain.DmsDocInvalidOrder;
import org.dromara.dms.domain.DmsDocInvalidOrderItem;
import org.dromara.dms.domain.DmsDocumentLib;
import org.dromara.dms.handler.DmsFlowSupport;
import org.dromara.dms.mapper.DmsDocInvalidOrderItemMapper;
import org.dromara.dms.mapper.DmsDocInvalidOrderMapper;
import org.dromara.dms.mapper.DmsDocumentLibMapper;
import org.dromara.dms.util.DmsOrderNo;
import org.dromara.dms.util.DmsPages;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 文档失效单
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/dms/doc-invalid-order")
public class DmsDocInvalidOrderController {

    private final DmsDocInvalidOrderMapper baseMapper;
    private final DmsDocInvalidOrderItemMapper itemMapper;
    private final DmsDocumentLibMapper documentLibMapper;
    private final DmsFlowSupport flowSupport;

    @GetMapping("/editList")
    public TableDataInfo<DmsDocInvalidOrder> editList(DmsDocInvalidOrder query, PageQuery pageQuery) {
        QueryWrapper<DmsDocInvalidOrder> qw = wrapper(query);
        qw.eq("status", "edit");
        qw.eq("create_by", LoginHelper.getUserId());
        return DmsPages.page(baseMapper, pageQuery, qw);
    }

    @GetMapping("/approvalList")
    public TableDataInfo<DmsDocInvalidOrder> approvalList(DmsDocInvalidOrder query, PageQuery pageQuery) {
        QueryWrapper<DmsDocInvalidOrder> qw = wrapper(query);
        qw.eq("status", "approving");
        return DmsPages.page(baseMapper, pageQuery, qw);
    }

    @GetMapping("/list")
    public TableDataInfo<DmsDocInvalidOrder> list(DmsDocInvalidOrder query, PageQuery pageQuery) {
        return DmsPages.page(baseMapper, pageQuery, wrapper(query));
    }

    @GetMapping("/{id}")
    public R<DmsDocInvalidOrder> detail(@PathVariable Long id) {
        DmsDocInvalidOrder order = baseMapper.selectById(id);
        if (order != null) {
            List<Long> libIds = itemMapper.selectList(
                    new QueryWrapper<DmsDocInvalidOrderItem>().lambda().eq(DmsDocInvalidOrderItem::getOrderId, id))
                .stream().map(DmsDocInvalidOrderItem::getDocumentLibId).toList();
            List<DmsDocumentLib> libs = new ArrayList<>();
            for (Long libId : libIds) {
                DmsDocumentLib lib = documentLibMapper.selectById(libId);
                if (lib != null) {
                    libs.add(lib);
                }
            }
            order.setDocumentList(libs);
            if (order.getDocumentLibId() == null && !libIds.isEmpty()) {
                order.setDocumentLibId(libIds.get(0));
            }
        }
        return R.ok(order);
    }

    /** 新增（兼容 documentIds 与前端拼写 documentlds） */
    @PostMapping
    public R<Void> add(@RequestBody DmsDocInvalidOrder order) {
        fillDefaults(order);
        baseMapper.insert(order);
        saveItems(order);
        return R.ok();
    }

    @PutMapping
    public R<Void> edit(@RequestBody DmsDocInvalidOrder order) {
        baseMapper.updateById(order);
        saveItems(order);
        return R.ok();
    }

    @DeleteMapping("/{ids}")
    public R<Void> remove(@PathVariable String ids) {
        for (String id : ids.split(",")) {
            baseMapper.deleteById(Long.valueOf(id.trim()));
            itemMapper.delete(new QueryWrapper<DmsDocInvalidOrderItem>().lambda().eq(DmsDocInvalidOrderItem::getOrderId, Long.valueOf(id.trim())));
        }
        return R.ok();
    }

    @PostMapping("/export")
    public R<Void> export() {
        return R.ok();
    }

    @PostMapping("/submitAndFlowStart")
    public R<Void> submitAndFlowStart(@RequestBody DmsDocInvalidOrder order) {
        if (order.getApproveComments() != null) {
            DmsDocInvalidOrder update = new DmsDocInvalidOrder();
            update.setId(order.getId());
            update.setApproveComments(order.getApproveComments());
            baseMapper.updateById(update);
        }
        flowSupport.start("docInvalidOrder", order.getId());
        return R.ok();
    }

    private QueryWrapper<DmsDocInvalidOrder> wrapper(DmsDocInvalidOrder query) {
        QueryWrapper<DmsDocInvalidOrder> qw = new QueryWrapper<>();
        qw.eq(query.getStatus() != null, "status", query.getStatus());
        qw.like(query.getOrderNumber() != null && !query.getOrderNumber().isBlank(), "order_number", query.getOrderNumber());
        qw.like(query.getOrderReason() != null && !query.getOrderReason().isBlank(), "order_reason", query.getOrderReason());
        qw.orderByDesc("create_time");
        return qw;
    }

    private void fillDefaults(DmsDocInvalidOrder order) {
        if (order.getStatus() == null) {
            order.setStatus("edit");
        }
        if (order.getOrderNumber() == null) {
            order.setOrderNumber(DmsOrderNo.next("INV"));
        }
    }

    private void saveItems(DmsDocInvalidOrder order) {
        List<Long> ids = new ArrayList<>();
        if (order.getDocumentIds() != null) {
            ids.addAll(order.getDocumentIds());
        }
        if (order.getDocumentlds() != null) {
            ids.addAll(order.getDocumentlds());
        }
        if (ids.isEmpty()) {
            return;
        }
        itemMapper.delete(new QueryWrapper<DmsDocInvalidOrderItem>().lambda().eq(DmsDocInvalidOrderItem::getOrderId, order.getId()));
        for (Long libId : ids) {
            DmsDocInvalidOrderItem item = new DmsDocInvalidOrderItem();
            item.setOrderId(order.getId());
            item.setDocumentLibId(libId);
            itemMapper.insert(item);
        }
        if (order.getDocumentLibId() == null) {
            DmsDocInvalidOrder update = new DmsDocInvalidOrder();
            update.setId(order.getId());
            update.setDocumentLibId(ids.get(0));
            baseMapper.updateById(update);
        }
    }
}
