package org.dromara.dms.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.dms.domain.DmsBatchReviseOrderDetail;
import org.dromara.dms.domain.DmsBatchReviseOrderMain;
import org.dromara.dms.domain.DmsDocumentLib;
import org.dromara.dms.handler.DmsFlowSupport;
import org.dromara.dms.mapper.DmsBatchReviseOrderDetailMapper;
import org.dromara.dms.mapper.DmsBatchReviseOrderMainMapper;
import org.dromara.dms.mapper.DmsDocumentLibMapper;
import org.dromara.dms.util.DmsOrderNo;
import org.dromara.dms.util.DmsPages;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 批量修订单
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/dms/batch-revise-order-main")
public class DmsBatchReviseOrderController {

    private final DmsBatchReviseOrderMainMapper baseMapper;
    private final DmsBatchReviseOrderDetailMapper detailMapper;
    private final DmsDocumentLibMapper documentLibMapper;
    private final DmsFlowSupport flowSupport;

    @GetMapping("/editList")
    public TableDataInfo<DmsBatchReviseOrderMain> editList(DmsBatchReviseOrderMain query, PageQuery pageQuery) {
        QueryWrapper<DmsBatchReviseOrderMain> qw = wrapper(query);
        qw.eq("status", "edit");
        qw.eq("create_by", LoginHelper.getUserId());
        return DmsPages.page(baseMapper, pageQuery, qw);
    }

    @GetMapping("/approvalList")
    public TableDataInfo<DmsBatchReviseOrderMain> approvalList(DmsBatchReviseOrderMain query, PageQuery pageQuery) {
        QueryWrapper<DmsBatchReviseOrderMain> qw = wrapper(query);
        qw.eq("status", "approving");
        return DmsPages.page(baseMapper, pageQuery, qw);
    }

    @GetMapping("/list")
    public TableDataInfo<DmsBatchReviseOrderMain> list(DmsBatchReviseOrderMain query, PageQuery pageQuery) {
        return DmsPages.page(baseMapper, pageQuery, wrapper(query));
    }

    /** 详情（includeDetails=true 时带明细） */
    @GetMapping("/{id}")
    public R<DmsBatchReviseOrderMain> detail(@PathVariable Long id,
                                             @RequestParam(value = "includeDetails", required = false) Boolean includeDetails) {
        DmsBatchReviseOrderMain main = baseMapper.selectById(id);
        if (main != null && Boolean.TRUE.equals(includeDetails)) {
            main.setDetailList(detailMapper.selectList(
                new QueryWrapper<DmsBatchReviseOrderDetail>().lambda().eq(DmsBatchReviseOrderDetail::getOrderId, id)));
        }
        return R.ok(main);
    }

    /** 提交前校验 */
    @GetMapping("/{id}/validate")
    public R<Void> validate(@PathVariable Long id) {
        return R.ok();
    }

    @PostMapping
    public R<Void> add(@RequestBody DmsBatchReviseOrderMain main) {
        fillDefaults(main);
        baseMapper.insert(main);
        saveDetails(main);
        return R.ok();
    }

    @PutMapping
    public R<Void> edit(@RequestBody DmsBatchReviseOrderMain main) {
        baseMapper.updateById(main);
        saveDetails(main);
        return R.ok();
    }

    @DeleteMapping("/{ids}")
    public R<Void> remove(@PathVariable String ids) {
        for (String id : ids.split(",")) {
            baseMapper.deleteById(Long.valueOf(id.trim()));
            detailMapper.delete(new QueryWrapper<DmsBatchReviseOrderDetail>().lambda()
                .eq(DmsBatchReviseOrderDetail::getOrderId, Long.valueOf(id.trim())));
        }
        return R.ok();
    }

    @DeleteMapping("/details/{detailId}")
    public R<Void> removeDetail(@PathVariable Long detailId) {
        detailMapper.deleteById(detailId);
        return R.ok();
    }

    @PostMapping("/submitAndFlowStart")
    public R<Void> submitAndFlowStart(@RequestBody DmsBatchReviseOrderMain main) {
        if (main.getApproveComments() != null) {
            DmsBatchReviseOrderMain update = new DmsBatchReviseOrderMain();
            update.setId(main.getId());
            update.setApproveComments(main.getApproveComments());
            baseMapper.updateById(update);
        }
        flowSupport.start("batchReviseOrderMain", main.getId());
        return R.ok();
    }

    private QueryWrapper<DmsBatchReviseOrderMain> wrapper(DmsBatchReviseOrderMain query) {
        QueryWrapper<DmsBatchReviseOrderMain> qw = new QueryWrapper<>();
        qw.eq(query.getStatus() != null, "status", query.getStatus());
        qw.like(query.getOrderNumber() != null && !query.getOrderNumber().isBlank(), "order_number", query.getOrderNumber());
        qw.like(query.getOrderReason() != null && !query.getOrderReason().isBlank(), "order_reason", query.getOrderReason());
        qw.orderByDesc("create_time");
        return qw;
    }

    private void fillDefaults(DmsBatchReviseOrderMain main) {
        if (main.getStatus() == null) {
            main.setStatus("edit");
        }
        if (main.getOrderNumber() == null) {
            main.setOrderNumber(DmsOrderNo.next("BR"));
        }
        if (main.getApplyUser() == null) {
            main.setApplyUser(LoginHelper.getUsername());
        }
    }

    /**
     * 兼容三种入参：documentLibIds / details（新建时为文件 id）/ docLibIds（JSON 数组字符串）
     */
    private void saveDetails(DmsBatchReviseOrderMain main) {
        Set<Long> libIds = new LinkedHashSet<>();
        if (main.getDocumentLibIds() != null) {
            libIds.addAll(main.getDocumentLibIds());
        }
        if (main.getDetails() != null) {
            libIds.addAll(main.getDetails());
        }
        if (main.getDocLibIds() != null && !main.getDocLibIds().isBlank()) {
            String json = main.getDocLibIds().replace("[", ",").replace("]", ",").replace("\"", "");
            for (String part : json.split(",")) {
                if (!part.isBlank()) {
                    libIds.add(Long.valueOf(part.trim()));
                }
            }
        }
        if (libIds.isEmpty()) {
            return;
        }
        detailMapper.delete(new QueryWrapper<DmsBatchReviseOrderDetail>().lambda()
            .eq(DmsBatchReviseOrderDetail::getOrderId, main.getId()));
        for (Long libId : libIds) {
            DmsDocumentLib lib = documentLibMapper.selectById(libId);
            DmsBatchReviseOrderDetail detail = new DmsBatchReviseOrderDetail();
            detail.setOrderId(main.getId());
            detail.setDocumentLibId(libId);
            if (lib != null) {
                detail.setDocumentCode(lib.getDlCode());
                detail.setDocumentName(lib.getDlName());
                detail.setDocumentVersion(lib.getDlVersion());
                detail.setDocumentLevel(lib.getDlLevel());
                detail.setDlCode(lib.getDlCode());
                detail.setDlName(lib.getDlName());
                detail.setDlVersion(lib.getDlVersion());
                detail.setDlLevel(lib.getDlLevel());
                detail.setDrafterDeptName(lib.getDlDeptName());
                detail.setIssueDeptName(lib.getIssueDeptName());
                detail.setUserDeptName(lib.getDlDeptName());
            }
            detailMapper.insert(detail);
        }
    }
}
