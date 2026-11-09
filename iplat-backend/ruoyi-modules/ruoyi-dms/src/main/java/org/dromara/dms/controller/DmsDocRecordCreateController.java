package org.dromara.dms.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.dms.domain.DmsDocRecordCreate;
import org.dromara.dms.handler.DmsFlowSupport;
import org.dromara.dms.mapper.DmsDocRecordCreateMapper;
import org.dromara.dms.util.DmsOrderNo;
import org.dromara.dms.util.DmsPages;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 记录编制 / 审批 / 追踪
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/dms/doc-record-create")
public class DmsDocRecordCreateController {

    private final DmsDocRecordCreateMapper baseMapper;
    private final DmsFlowSupport flowSupport;

    /** 编制列表（本人草稿） */
    @GetMapping("/editList")
    public TableDataInfo<DmsDocRecordCreate> editList(DmsDocRecordCreate query, PageQuery pageQuery) {
        QueryWrapper<DmsDocRecordCreate> qw = wrapper(query);
        qw.eq("status", "edit");
        qw.eq("create_by", LoginHelper.getUserId());
        return DmsPages.page(baseMapper, pageQuery, qw);
    }

    /** 审批列表 */
    @GetMapping("/approvalList")
    public TableDataInfo<DmsDocRecordCreate> approvalList(DmsDocRecordCreate query, PageQuery pageQuery) {
        QueryWrapper<DmsDocRecordCreate> qw = wrapper(query);
        qw.eq("status", "approving");
        return DmsPages.page(baseMapper, pageQuery, qw);
    }

    /** 全量列表（追踪） */
    @GetMapping("/list")
    public TableDataInfo<DmsDocRecordCreate> list(DmsDocRecordCreate query, PageQuery pageQuery) {
        return DmsPages.page(baseMapper, pageQuery, wrapper(query));
    }

    /** 文档编制下的记录 */
    @GetMapping("/buildNewList")
    public TableDataInfo<DmsDocRecordCreate> buildNewList(DmsDocRecordCreate query, PageQuery pageQuery) {
        QueryWrapper<DmsDocRecordCreate> qw = wrapper(query);
        qw.eq(query.getDocCreateId() != null, "doc_create_id", query.getDocCreateId());
        qw.orderByDesc("create_time");
        return DmsPages.page(baseMapper, pageQuery, qw);
    }

    @GetMapping("/{id}")
    public R<DmsDocRecordCreate> detail(@PathVariable Long id) {
        return R.ok(baseMapper.selectById(id));
    }

    @GetMapping("/file/{id}")
    public R<String> file(@PathVariable Long id) {
        return R.ok();
    }

    @PostMapping
    public R<Void> add(@RequestBody DmsDocRecordCreate record) {
        fillDefaults(record);
        baseMapper.insert(record);
        return R.ok();
    }

    /** 从文档编制页新增记录 */
    @PostMapping("/insert/fromDocCreate")
    public R<Void> insertFromDocCreate(@RequestBody DmsDocRecordCreate record) {
        fillDefaults(record);
        baseMapper.insert(record);
        return R.ok();
    }

    /** 记录名称是否重复：false = 已存在 */
    @PostMapping("/judgeRecordNameUnique")
    public R<Boolean> judgeRecordNameUnique(@RequestBody DmsDocRecordCreate record) {
        Long count = baseMapper.selectCount(new QueryWrapper<DmsDocRecordCreate>().lambda()
            .eq(DmsDocRecordCreate::getDocumentRecordCode, record.getDocumentRecordCode())
            .eq(DmsDocRecordCreate::getDocumentRecordName, record.getDocumentRecordName()));
        return R.ok(count == 0);
    }

    @PutMapping
    public R<Void> edit(@RequestBody DmsDocRecordCreate record) {
        baseMapper.updateById(record);
        return R.ok();
    }

    @DeleteMapping("/{ids}")
    public R<Void> remove(@PathVariable String ids) {
        for (String id : ids.split(",")) {
            baseMapper.deleteById(Long.valueOf(id.trim()));
        }
        return R.ok();
    }

    /** 提交并启动审批流 */
    @PostMapping("/submitAndFlowStart")
    public R<Void> submitAndFlowStart(@RequestBody DmsDocRecordCreate record) {
        if (record.getApproveComments() != null) {
            DmsDocRecordCreate update = new DmsDocRecordCreate();
            update.setId(record.getId());
            update.setApproveComments(record.getApproveComments());
            baseMapper.updateById(update);
        }
        flowSupport.start("docRecordCreate", record.getId());
        return R.ok();
    }

    /** 离线文件上传 */
    @PostMapping("/offline/upload/{id}")
    public R<Void> offlineUpload(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        DmsDocRecordCreate update = new DmsDocRecordCreate();
        update.setId(id);
        update.setEditStatus("uploaded");
        baseMapper.updateById(update);
        return R.ok();
    }

    /** 申请失效 */
    @PutMapping("/failure/{id}")
    public R<Void> failure(@PathVariable Long id) {
        DmsDocRecordCreate update = new DmsDocRecordCreate();
        update.setId(id);
        update.setStatus("failure");
        baseMapper.updateById(update);
        return R.ok();
    }

    /** 取消失效 */
    @PutMapping("/cancel/failure/{id}")
    public R<Void> cancelFailure(@PathVariable Long id) {
        DmsDocRecordCreate update = new DmsDocRecordCreate();
        update.setId(id);
        update.setStatus("edit");
        baseMapper.updateById(update);
        return R.ok();
    }

    private QueryWrapper<DmsDocRecordCreate> wrapper(DmsDocRecordCreate query) {
        QueryWrapper<DmsDocRecordCreate> qw = new QueryWrapper<>();
        qw.eq(query.getStatus() != null, "status", query.getStatus());
        qw.eq(query.getDocCreateId() != null, "doc_create_id", query.getDocCreateId());
        qw.eq(query.getRecordHandleType() != null, "record_handle_type", query.getRecordHandleType());
        qw.like(query.getDocumentCode() != null && !query.getDocumentCode().isBlank(), "document_code", query.getDocumentCode());
        qw.like(query.getDocumentName() != null && !query.getDocumentName().isBlank(), "document_name", query.getDocumentName());
        qw.like(query.getDocumentRecordCode() != null && !query.getDocumentRecordCode().isBlank(), "document_record_code", query.getDocumentRecordCode());
        qw.like(query.getDocumentRecordName() != null && !query.getDocumentRecordName().isBlank(), "document_record_name", query.getDocumentRecordName());
        qw.orderByDesc("create_time");
        return qw;
    }

    private void fillDefaults(DmsDocRecordCreate record) {
        if (record.getStatus() == null) {
            record.setStatus("edit");
        }
        if (record.getDocumentRecordCode() == null || record.getDocumentRecordCode().isBlank()) {
            record.setDocumentRecordCode(DmsOrderNo.next("REC"));
        }
        if (record.getDrafterId() == null) {
            record.setDrafterId(LoginHelper.getUserId());
        }
        if (record.getDraftUserName() == null) {
            record.setDraftUserName(LoginHelper.getUsername());
        }
    }
}
