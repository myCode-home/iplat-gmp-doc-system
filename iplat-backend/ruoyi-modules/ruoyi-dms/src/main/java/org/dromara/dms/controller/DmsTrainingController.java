package org.dromara.dms.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.dms.domain.DmsDocCreate;
import org.dromara.dms.domain.DmsDocumentLib;
import org.dromara.dms.domain.DmsTrainingRecord;
import org.dromara.dms.mapper.DmsDocCreateMapper;
import org.dromara.dms.mapper.DmsDocumentLibMapper;
import org.dromara.dms.mapper.DmsTrainingRecordMapper;
import org.dromara.dms.util.DmsPages;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 培训确认 / 生效 / 追踪（基于文档编制的培训状态）
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/dms/train-confirm")
public class DmsTrainingController {

    private final DmsDocCreateMapper docCreateMapper;
    private final DmsDocumentLibMapper documentLibMapper;
    private final DmsTrainingRecordMapper trainingRecordMapper;

    /** 待培训 */
    @GetMapping("/unTrainList")
    public TableDataInfo<DmsDocCreate> unTrainList(DmsDocCreate query, PageQuery pageQuery) {
        QueryWrapper<DmsDocCreate> qw = docWrapper(query);
        qw.eq("train_status", "needTrain");
        return DmsPages.page(docCreateMapper, pageQuery, qw);
    }

    /** 已培训 */
    @GetMapping("/trainedList")
    public TableDataInfo<DmsDocCreate> trainedList(DmsDocCreate query, PageQuery pageQuery) {
        QueryWrapper<DmsDocCreate> qw = docWrapper(query);
        qw.eq("train_status", "trained");
        return DmsPages.page(docCreateMapper, pageQuery, qw);
    }

    /** 待生效文件（已培训/无需培训，尚未生效） */
    @GetMapping("/unActiveList")
    public TableDataInfo<DmsDocCreate> unActiveList(DmsDocCreate query, PageQuery pageQuery) {
        QueryWrapper<DmsDocCreate> qw = docWrapper(query);
        qw.in("train_status", "trained", "noNeedTrain");
        return DmsPages.page(docCreateMapper, pageQuery, qw);
    }

    /** 培训追踪（全部已审批文档） */
    @GetMapping("/trainTrackList")
    public TableDataInfo<DmsDocCreate> trainTrackList(DmsDocCreate query, PageQuery pageQuery) {
        QueryWrapper<DmsDocCreate> qw = docWrapper(query);
        qw.isNotNull("train_status");
        return DmsPages.page(docCreateMapper, pageQuery, qw);
    }

    /** 标记需要培训 */
    @PutMapping("/needTrain/{id}")
    public R<Void> needTrain(@PathVariable Long id) {
        return updateTrainStatus(id, "needTrain");
    }

    /** 标记无需培训 */
    @PutMapping("/noNeedTrain/{id}")
    public R<Void> noNeedTrain(@PathVariable Long id) {
        return updateTrainStatus(id, "noNeedTrain");
    }

    /** 文件生效（培训完成后激活文件库） */
    @PostMapping("/active")
    public R<Void> active(@RequestBody Map<String, Object> body) {
        Object idsObj = body.get("ids");
        if (!(idsObj instanceof List<?> ids)) {
            return R.fail("参数错误");
        }
        String effectiveTime = body.get("effectiveTime") == null ? null : String.valueOf(body.get("effectiveTime"));
        for (Object item : ids) {
            Long id = Long.valueOf(String.valueOf(item));
            DmsDocCreate doc = docCreateMapper.selectById(id);
            if (doc == null) {
                continue;
            }
            DmsDocCreate update = new DmsDocCreate();
            update.setId(id);
            update.setTrainStatus("active");
            docCreateMapper.updateById(update);
            DmsDocumentLib lib = documentLibMapper.selectOne(new QueryWrapper<DmsDocumentLib>().lambda()
                .eq(DmsDocumentLib::getDlCode, doc.getDocumentCode()).last("limit 1"));
            if (lib != null) {
                DmsDocumentLib libUpdate = new DmsDocumentLib();
                libUpdate.setId(lib.getId());
                libUpdate.setUseState("active");
                documentLibMapper.updateById(libUpdate);
            }
            DmsTrainingRecord record = new DmsTrainingRecord();
            record.setDocCreateId(id);
            record.setDocumentCode(doc.getDocumentCode());
            record.setDocumentName(doc.getDocumentName());
            record.setTrainUserName(LoginHelper.getUsername());
            record.setTrainStatus("active");
            record.setRemark("生效时间：" + (effectiveTime == null ? "-" : effectiveTime));
            record.setTrainTime(new Date());
            trainingRecordMapper.insert(record);
        }
        return R.ok();
    }

    private R<Void> updateTrainStatus(Long id, String trainStatus) {
        DmsDocCreate doc = docCreateMapper.selectById(id);
        if (doc == null) {
            return R.fail("文档不存在");
        }
        DmsDocCreate update = new DmsDocCreate();
        update.setId(id);
        update.setTrainStatus(trainStatus);
        docCreateMapper.updateById(update);
        DmsTrainingRecord record = new DmsTrainingRecord();
        record.setDocCreateId(id);
        record.setDocumentCode(doc.getDocumentCode());
        record.setDocumentName(doc.getDocumentName());
        record.setTrainUserName(LoginHelper.getUsername());
        record.setTrainStatus(trainStatus);
        record.setTrainTime(new Date());
        trainingRecordMapper.insert(record);
        return R.ok();
    }

    private QueryWrapper<DmsDocCreate> docWrapper(DmsDocCreate query) {
        QueryWrapper<DmsDocCreate> qw = new QueryWrapper<>();
        qw.eq("status", "approved");
        qw.like(query.getDocumentCode() != null && !query.getDocumentCode().isBlank(), "document_code", query.getDocumentCode());
        qw.like(query.getDocumentName() != null && !query.getDocumentName().isBlank(), "document_name", query.getDocumentName());
        qw.orderByDesc("create_time");
        return qw;
    }
}
