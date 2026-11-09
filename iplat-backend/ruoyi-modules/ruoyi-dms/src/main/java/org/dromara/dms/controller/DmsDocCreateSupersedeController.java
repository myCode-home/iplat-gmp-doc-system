package org.dromara.dms.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.dms.domain.DmsDocCreateSupersede;
import org.dromara.dms.mapper.DmsDocCreateSupersedeMapper;
import org.dromara.dms.mapper.DmsDocumentLibMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 文档替代文件列表
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/dms/doc-create-supersede")
public class DmsDocCreateSupersedeController {

    private final DmsDocCreateSupersedeMapper baseMapper;
    private final DmsDocumentLibMapper documentLibMapper;

    /** 注意：前端期望 data 为数组（非 rows） */
    @GetMapping("/list")
    public R<List<DmsDocCreateSupersede>> list(@RequestParam Long docCreateId) {
        List<DmsDocCreateSupersede> list = baseMapper.selectList(
            new QueryWrapper<DmsDocCreateSupersede>().lambda().eq(DmsDocCreateSupersede::getDocCreateId, docCreateId));
        list.forEach(item -> item.setDocumentLibInfo(
            item.getDocumentLibId() == null ? null : documentLibMapper.selectById(item.getDocumentLibId())));
        return R.ok(list);
    }
}
