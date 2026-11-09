package org.dromara.dms.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.dms.domain.DmsDocCreateLink;
import org.dromara.dms.mapper.DmsDocCreateLinkMapper;
import org.dromara.dms.mapper.DmsDocumentLibMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 文档关联文件列表
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/dms/doc-create-link")
public class DmsDocCreateLinkController {

    private final DmsDocCreateLinkMapper baseMapper;
    private final DmsDocumentLibMapper documentLibMapper;

    /** 注意：前端期望 data 为数组（非 rows） */
    @GetMapping("/list")
    public R<List<DmsDocCreateLink>> list(@RequestParam Long docCreateId) {
        List<DmsDocCreateLink> list = baseMapper.selectList(
            new QueryWrapper<DmsDocCreateLink>().lambda().eq(DmsDocCreateLink::getDocCreateId, docCreateId));
        list.forEach(item -> item.setDocumentLibInfo(
            item.getDocumentLibId() == null ? null : documentLibMapper.selectById(item.getDocumentLibId())));
        return R.ok(list);
    }
}
