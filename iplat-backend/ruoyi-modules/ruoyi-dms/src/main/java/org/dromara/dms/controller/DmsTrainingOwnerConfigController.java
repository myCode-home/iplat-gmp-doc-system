package org.dromara.dms.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.dms.domain.DmsTrainingOwnerConfig;
import org.dromara.dms.mapper.DmsTrainingOwnerConfigMapper;
import org.dromara.dms.util.DmsPages;
import org.springframework.web.bind.annotation.*;

/**
 * 培训责任人配置
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/dms/training-owner-config")
public class DmsTrainingOwnerConfigController {

    private final DmsTrainingOwnerConfigMapper baseMapper;

    @GetMapping("/pageList")
    public TableDataInfo<DmsTrainingOwnerConfig> pageList(DmsTrainingOwnerConfig query, PageQuery pageQuery) {
        QueryWrapper<DmsTrainingOwnerConfig> qw = new QueryWrapper<>();
        qw.eq(query.getUserCategory() != null, "user_category", query.getUserCategory());
        qw.like(query.getNickName() != null && !query.getNickName().isBlank(), "nick_name", query.getNickName());
        qw.orderByDesc("create_time");
        return DmsPages.page(baseMapper, pageQuery, qw);
    }

    @GetMapping("/{id}")
    public R<DmsTrainingOwnerConfig> detail(@PathVariable Long id) {
        return R.ok(baseMapper.selectById(id));
    }

    @PostMapping
    public R<Void> add(@RequestBody DmsTrainingOwnerConfig config) {
        if (config.getDrafterId() == null) {
            config.setDrafterId(LoginHelper.getUserId());
        }
        if (config.getDraftUserName() == null) {
            config.setDraftUserName(LoginHelper.getUsername());
        }
        baseMapper.insert(config);
        return R.ok();
    }

    @PutMapping
    public R<Void> edit(@RequestBody DmsTrainingOwnerConfig config) {
        baseMapper.updateById(config);
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
