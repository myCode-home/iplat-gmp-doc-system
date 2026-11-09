package org.dromara.dms.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.dms.domain.DmsEqmsNumberApplication;
import org.dromara.dms.mapper.DmsEqmsNumberApplicationMapper;
import org.dromara.dms.util.DmsOrderNo;
import org.dromara.dms.util.DmsPages;
import org.springframework.web.bind.annotation.*;

/**
 * 设备编号申请（EQMS 最小实现）
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/eqms/device_acceptance/pre_apply")
public class DmsEqmsNumberApplicationController {

    private final DmsEqmsNumberApplicationMapper baseMapper;

    @GetMapping("/list")
    public TableDataInfo<DmsEqmsNumberApplication> list(DmsEqmsNumberApplication query, PageQuery pageQuery) {
        QueryWrapper<DmsEqmsNumberApplication> qw = new QueryWrapper<>();
        qw.eq(query.getStatus() != null, "status", query.getStatus());
        qw.like(query.getApplyCode() != null && !query.getApplyCode().isBlank(), "apply_code", query.getApplyCode());
        qw.like(query.getDeviceName() != null && !query.getDeviceName().isBlank(), "device_name", query.getDeviceName());
        qw.orderByDesc("create_time");
        return DmsPages.page(baseMapper, pageQuery, qw);
    }

    @GetMapping("/{id}")
    public R<DmsEqmsNumberApplication> detail(@PathVariable Long id) {
        return R.ok(baseMapper.selectById(id));
    }

    @PostMapping
    public R<Void> add(@RequestBody DmsEqmsNumberApplication apply) {
        if (apply.getStatus() == null) {
            apply.setStatus("edit");
        }
        if (apply.getApplyCode() == null) {
            apply.setApplyCode(DmsOrderNo.next("DEV"));
        }
        baseMapper.insert(apply);
        return R.ok();
    }

    @PutMapping
    public R<Void> edit(@RequestBody DmsEqmsNumberApplication apply) {
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
}
