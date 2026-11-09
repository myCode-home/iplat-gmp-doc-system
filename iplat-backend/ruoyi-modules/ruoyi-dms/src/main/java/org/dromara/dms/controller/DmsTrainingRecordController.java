package org.dromara.dms.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.dms.domain.DmsDocCreate;
import org.dromara.dms.domain.DmsTrainingRecord;
import org.dromara.dms.mapper.DmsDocCreateMapper;
import org.dromara.dms.mapper.DmsTrainingRecordMapper;
import org.dromara.dms.util.DmsPages;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.Map;

/**
 * 培训记录
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/dms/training-record")
public class DmsTrainingRecordController {

    private final DmsTrainingRecordMapper baseMapper;
    private final DmsDocCreateMapper docCreateMapper;

    /** 某文档编制下的培训记录 */
    @GetMapping("/trainingRecordList/{id}")
    public TableDataInfo<DmsTrainingRecord> list(@PathVariable Long id, PageQuery pageQuery) {
        QueryWrapper<DmsTrainingRecord> qw = new QueryWrapper<>();
        qw.eq("doc_create_id", id);
        qw.orderByDesc("create_time");
        return DmsPages.page(baseMapper, pageQuery, qw);
    }

    /** 完成培训 */
    @PutMapping("/trainConfirm")
    public R<Void> trainConfirm(@RequestBody Map<String, Object> body) {
        Long docCreateId = body.get("docCreateId") == null ? null : Long.valueOf(String.valueOf(body.get("docCreateId")));
        if (docCreateId == null) {
            return R.fail("参数错误");
        }
        DmsDocCreate doc = docCreateMapper.selectById(docCreateId);
        if (doc != null) {
            DmsDocCreate update = new DmsDocCreate();
            update.setId(docCreateId);
            update.setTrainStatus("trained");
            docCreateMapper.updateById(update);
        }
        DmsTrainingRecord record = new DmsTrainingRecord();
        record.setDocCreateId(docCreateId);
        record.setDocumentCode(doc == null ? null : doc.getDocumentCode());
        record.setDocumentName(doc == null ? null : doc.getDocumentName());
        record.setTrainUserName(body.get("trainUserName") == null
            ? LoginHelper.getUsername() : String.valueOf(body.get("trainUserName")));
        record.setTrainStatus("trained");
        record.setTrainTime(new Date());
        baseMapper.insert(record);
        return R.ok();
    }
}
