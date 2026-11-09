package org.dromara.dms.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.dms.domain.DmsDocRecordCreate;
import org.dromara.dms.domain.DmsDocRecordFailureOrder;
import org.dromara.dms.domain.DmsDocumentRecordLib;
import org.dromara.dms.handler.DmsFlowSupport;
import org.dromara.dms.mapper.DmsDocRecordCreateMapper;
import org.dromara.dms.mapper.DmsDocRecordFailureOrderMapper;
import org.dromara.dms.mapper.DmsDocumentRecordLibMapper;
import org.dromara.dms.util.DmsOrderNo;
import org.dromara.dms.util.DmsPages;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

/**
 * 记录库
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/dms/document-record-lib")
public class DmsDocumentRecordLibController {

    private final DmsDocumentRecordLibMapper baseMapper;
    private final DmsDocRecordCreateMapper recordCreateMapper;
    private final DmsDocRecordFailureOrderMapper failureOrderMapper;
    private final DmsFlowSupport flowSupport;

    /** 生效记录库 */
    @GetMapping("/activeList")
    public TableDataInfo<DmsDocumentRecordLib> activeList(DmsDocumentRecordLib query, PageQuery pageQuery) {
        query.setActiveStatus("active");
        return DmsPages.page(baseMapper, pageQuery, wrapper(query));
    }

    /** 全量列表（选择弹窗） */
    @GetMapping("/list")
    public TableDataInfo<DmsDocumentRecordLib> list(DmsDocumentRecordLib query, PageQuery pageQuery) {
        return DmsPages.page(baseMapper, pageQuery, wrapper(query));
    }

    /** 历史记录（同编号所有版本） */
    @GetMapping("/historyList")
    public TableDataInfo<DmsDocumentRecordLib> historyList(DmsDocumentRecordLib query, PageQuery pageQuery) {
        QueryWrapper<DmsDocumentRecordLib> qw = wrapper(query);
        qw.like(query.getDlrCode() != null && !query.getDlrCode().isBlank(), "dlr_code", query.getDlrCode());
        return DmsPages.page(baseMapper, pageQuery, qw);
    }

    /** 文件库下挂记录 */
    @GetMapping("/document-lib/list/{dlId}")
    public TableDataInfo<DmsDocumentRecordLib> listByDocLib(@PathVariable Long dlId, PageQuery pageQuery) {
        QueryWrapper<DmsDocumentRecordLib> qw = new QueryWrapper<>();
        qw.eq("document_lib_id", dlId);
        qw.orderByDesc("create_time");
        return DmsPages.page(baseMapper, pageQuery, qw);
    }

    @GetMapping("/{id}")
    public R<DmsDocumentRecordLib> detail(@PathVariable Long id) {
        return R.ok(baseMapper.selectById(id));
    }

    @GetMapping("/file/{id}")
    public R<String> file(@PathVariable Long id) {
        return R.ok();
    }

    @PostMapping
    public R<Void> add(@RequestBody DmsDocumentRecordLib lib) {
        fillDefaults(lib);
        baseMapper.insert(lib);
        return R.ok();
    }

    @PutMapping
    public R<Void> edit(@RequestBody DmsDocumentRecordLib lib) {
        baseMapper.updateById(lib);
        return R.ok();
    }

    @DeleteMapping("/{id}")
    public R<Void> remove(@PathVariable Long id) {
        baseMapper.deleteById(id);
        return R.ok();
    }

    /** 申请修订：生成记录编制单，记录库进入修订中 */
    @PostMapping("/revise")
    public R<Void> revise(@RequestBody DmsDocumentRecordLib form) {
        DmsDocumentRecordLib lib = baseMapper.selectById(form.getId());
        if (lib == null) {
            return R.fail("记录不存在");
        }
        DmsDocRecordCreate create = new DmsDocRecordCreate();
        create.setDocumentCode(lib.getDocumentCode());
        create.setDocumentName(lib.getDocumentName());
        create.setDocumentRecordCode(lib.getDlrCode());
        create.setDocumentRecordName(form.getDlrName() == null ? lib.getDlrName() : form.getDlrName());
        create.setDocumentRecordType(lib.getDocumentType());
        create.setDocumentRecordLibraryId(lib.getId());
        create.setDocumentRecordVersion(lib.getDlrVersion());
        create.setRecordHandleType("revise");
        create.setReviseSource("revise");
        create.setEditWay(lib.getEditWay());
        create.setStatus("edit");
        recordCreateMapper.insert(create);
        lib.setActiveStatus("revising");
        baseMapper.updateById(lib);
        return R.ok();
    }

    /** 申请失效：生成记录失效单，记录库进入失效中 */
    @PostMapping("/invalid")
    public R<Void> invalid(@RequestBody DmsDocumentRecordLib form) {
        DmsDocumentRecordLib lib = baseMapper.selectById(form.getId());
        if (lib == null) {
            return R.fail("记录不存在");
        }
        DmsDocRecordFailureOrder order = new DmsDocRecordFailureOrder();
        order.setOrderNumber(DmsOrderNo.next("RINV"));
        order.setDocumentRecordLibraryId(lib.getId());
        order.setDocumentCode(lib.getDocumentCode());
        order.setDocumentRecordReason(form.getDlrDeactiveReason() == null ? "申请失效" : form.getDlrDeactiveReason());
        order.setStatus("edit");
        failureOrderMapper.insert(order);
        lib.setActiveStatus("deactiving");
        baseMapper.updateById(lib);
        return R.ok();
    }

    /** 直接提交记录库生效（预留接口） */
    @PostMapping("/submitAndFlowStart")
    public R<Void> submitAndFlowStart(@RequestBody DmsDocumentRecordLib form) {
        return R.ok();
    }

    private QueryWrapper<DmsDocumentRecordLib> wrapper(DmsDocumentRecordLib query) {
        QueryWrapper<DmsDocumentRecordLib> qw = new QueryWrapper<>();
        qw.eq(query.getStatus() != null, "status", query.getStatus());
        qw.eq(query.getActiveStatus() != null, "active_status", query.getActiveStatus());
        qw.eq(query.getDocumentLibId() != null, "document_lib_id", query.getDocumentLibId());
        qw.like(query.getDlrCode() != null && !query.getDlrCode().isBlank(), "dlr_code", query.getDlrCode());
        qw.like(query.getDlrName() != null && !query.getDlrName().isBlank(), "dlr_name", query.getDlrName());
        qw.like(query.getDocumentName() != null && !query.getDocumentName().isBlank(), "document_name", query.getDocumentName());
        qw.orderByDesc("create_time");
        return qw;
    }

    private void fillDefaults(DmsDocumentRecordLib lib) {
        if (lib.getStatus() == null) {
            lib.setStatus("active");
        }
        if (lib.getActiveStatus() == null) {
            lib.setActiveStatus("active");
        }
        if (lib.getDlrActiveTime() == null) {
            lib.setDlrActiveTime(new Date());
        }
    }
}
