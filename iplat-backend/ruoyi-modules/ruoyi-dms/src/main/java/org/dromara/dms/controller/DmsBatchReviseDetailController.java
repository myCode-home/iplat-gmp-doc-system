package org.dromara.dms.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.dms.domain.DmsBatchReviseOrderDetail;
import org.dromara.dms.mapper.DmsBatchReviseOrderDetailMapper;
import org.dromara.dms.util.DmsPages;
import org.springframework.web.bind.annotation.*;

/**
 * 批量修订单明细
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/dms/batch-revise-order-detail")
public class DmsBatchReviseDetailController {

    private final DmsBatchReviseOrderDetailMapper baseMapper;

    @GetMapping("/list")
    public TableDataInfo<DmsBatchReviseOrderDetail> list(DmsBatchReviseOrderDetail query, PageQuery pageQuery) {
        QueryWrapper<DmsBatchReviseOrderDetail> qw = new QueryWrapper<>();
        qw.eq(query.getOrderId() != null, "order_id", query.getOrderId());
        qw.like(query.getDocumentCode() != null && !query.getDocumentCode().isBlank(), "document_code", query.getDocumentCode());
        qw.like(query.getDocumentName() != null && !query.getDocumentName().isBlank(), "document_name", query.getDocumentName());
        qw.orderByDesc("create_time");
        return DmsPages.page(baseMapper, pageQuery, qw);
    }

    @GetMapping("/{id}")
    public R<DmsBatchReviseOrderDetail> detail(@PathVariable Long id) {
        return R.ok(baseMapper.selectById(id));
    }
}
