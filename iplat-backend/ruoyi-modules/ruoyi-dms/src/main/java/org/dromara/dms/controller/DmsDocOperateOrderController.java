package org.dromara.dms.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.dms.domain.DmsDocOperateOrder;
import org.dromara.dms.domain.DmsDocumentLib;
import org.dromara.dms.handler.DmsFlowSupport;
import org.dromara.dms.mapper.DmsDocOperateOrderMapper;
import org.dromara.dms.mapper.DmsDocumentLibMapper;
import org.dromara.dms.util.DmsOrderNo;
import org.dromara.dms.util.DmsPages;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 操作单（新增/修订申请）
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/dms/doc-operate-order")
public class DmsDocOperateOrderController {

    private final DmsDocOperateOrderMapper baseMapper;
    private final DmsDocumentLibMapper documentLibMapper;
    private final DmsFlowSupport flowSupport;

    @GetMapping("/list")
    public TableDataInfo<DmsDocOperateOrder> list(DmsDocOperateOrder query, PageQuery pageQuery) {
        return DmsPages.page(baseMapper, pageQuery, wrapper(query));
    }

    @GetMapping("/list/new")
    public TableDataInfo<DmsDocOperateOrder> listNew(DmsDocOperateOrder query, PageQuery pageQuery) {
        query.setOperateType("add");
        return DmsPages.page(baseMapper, pageQuery, wrapper(query));
    }

    @GetMapping("/list/revise")
    public TableDataInfo<DmsDocOperateOrder> listRevise(DmsDocOperateOrder query, PageQuery pageQuery) {
        query.setOperateType("revise");
        return DmsPages.page(baseMapper, pageQuery, wrapper(query));
    }

    @GetMapping("/list/approve")
    public TableDataInfo<DmsDocOperateOrder> listApprove(DmsDocOperateOrder query, PageQuery pageQuery) {
        query.setStatus("approving");
        return DmsPages.page(baseMapper, pageQuery, wrapper(query));
    }

    @GetMapping("/list/track")
    public TableDataInfo<DmsDocOperateOrder> listTrack(DmsDocOperateOrder query, PageQuery pageQuery) {
        return DmsPages.page(baseMapper, pageQuery, wrapper(query));
    }

    /** 已审批操作单（文档编制引用） */
    @GetMapping("/list/approved/doc-create")
    public TableDataInfo<DmsDocOperateOrder> listApproved(DmsDocOperateOrder query, PageQuery pageQuery) {
        query.setStatus("approved");
        return DmsPages.page(baseMapper, pageQuery, wrapper(query));
    }

    @GetMapping("/{id}")
    public R<DmsDocOperateOrder> detail(@PathVariable Long id) {
        DmsDocOperateOrder order = baseMapper.selectById(id);
        if (order != null && order.getReplaceLibIds() != null && !order.getReplaceLibIds().isBlank()) {
            List<DmsDocumentLib> libs = new ArrayList<>();
            for (String libId : order.getReplaceLibIds().split(",")) {
                if (libId.isBlank()) {
                    continue;
                }
                DmsDocumentLib lib = documentLibMapper.selectById(Long.valueOf(libId.trim()));
                if (lib != null) {
                    libs.add(lib);
                }
            }
            order.setLibs(libs);
        }
        return R.ok(order);
    }

    /** 新增操作单 */
    @PostMapping
    public R<Void> add(@RequestBody DmsDocOperateOrder order) {
        fillDefaults(order, "add");
        baseMapper.insert(order);
        return R.ok();
    }

    /** 修订申请 */
    @PostMapping("/revise")
    public R<Void> addRevise(@RequestBody DmsDocOperateOrder order) {
        fillDefaults(order, "revise");
        baseMapper.insert(order);
        return R.ok();
    }

    @PutMapping
    public R<Void> edit(@RequestBody DmsDocOperateOrder order) {
        baseMapper.updateById(order);
        return R.ok();
    }

    @PutMapping("/revise")
    public R<Void> editRevise(@RequestBody DmsDocOperateOrder order) {
        baseMapper.updateById(order);
        return R.ok();
    }

    @DeleteMapping("/{id}")
    public R<Void> remove(@PathVariable Long id) {
        baseMapper.deleteById(id);
        return R.ok();
    }

    @DeleteMapping("/revise/{id}")
    public R<Void> removeRevise(@PathVariable Long id) {
        baseMapper.deleteById(id);
        return R.ok();
    }

    @PostMapping({"/submit-and-flow-start", "/submitAndFlowStart"})
    public R<Void> submitAndFlowStart(@RequestBody DmsDocOperateOrder order) {
        if (order.getApproveComments() != null) {
            DmsDocOperateOrder update = new DmsDocOperateOrder();
            update.setId(order.getId());
            update.setApproveComments(order.getApproveComments());
            baseMapper.updateById(update);
        }
        flowSupport.start("docOperateOrder", order.getId());
        return R.ok();
    }

    private QueryWrapper<DmsDocOperateOrder> wrapper(DmsDocOperateOrder query) {
        QueryWrapper<DmsDocOperateOrder> qw = new QueryWrapper<>();
        qw.eq(query.getStatus() != null, "status", query.getStatus());
        qw.eq(query.getOperateType() != null, "operate_type", query.getOperateType());
        qw.eq(query.getDocumentType() != null, "document_type", query.getDocumentType());
        qw.like(query.getOrderNumber() != null && !query.getOrderNumber().isBlank(), "order_number", query.getOrderNumber());
        qw.like(query.getDocumentCode() != null && !query.getDocumentCode().isBlank(), "document_code", query.getDocumentCode());
        qw.like(query.getDocumentName() != null && !query.getDocumentName().isBlank(), "document_name", query.getDocumentName());
        qw.orderByDesc("create_time");
        return qw;
    }

    private void fillDefaults(DmsDocOperateOrder order, String operateType) {
        if (order.getStatus() == null) {
            order.setStatus("edit");
        }
        if (order.getOperateType() == null) {
            order.setOperateType(operateType);
        }
        if (order.getOrderNumber() == null) {
            order.setOrderNumber(DmsOrderNo.next("OP"));
        }
        if (order.getDrafterId() == null) {
            order.setDrafterId(LoginHelper.getUserId());
        }
        if (order.getDraftNickName() == null) {
            order.setDraftNickName(LoginHelper.getUsername());
        }
        if (order.getDocDeptId() == null) {
            order.setDocDeptId(LoginHelper.getDeptId());
        }
        if (order.getReplaceIds() != null && !order.getReplaceIds().isEmpty()) {
            order.setReplaceLibIds(String.join(",", order.getReplaceIds().stream().map(String::valueOf).toList()));
        }
    }
}
