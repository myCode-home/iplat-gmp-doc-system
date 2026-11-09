package org.dromara.dms.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.dms.domain.DmsPrintDocInfo;
import org.dromara.dms.mapper.DmsPrintDocInfoMapper;
import org.dromara.dms.util.DmsPages;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 打印明细（打印分发状态机）
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/dms/print-doc-info")
public class DmsPrintDocInfoController {

    private final DmsPrintDocInfoMapper baseMapper;

    @GetMapping("/list")
    public TableDataInfo<DmsPrintDocInfo> list(DmsPrintDocInfo query, PageQuery pageQuery) {
        return DmsPages.page(baseMapper, pageQuery, wrapper(query));
    }

    /** 待发放 */
    @GetMapping("/list/printed")
    public TableDataInfo<DmsPrintDocInfo> listPrinted(DmsPrintDocInfo query, PageQuery pageQuery) {
        query.setState("printed");
        return DmsPages.page(baseMapper, pageQuery, wrapper(query));
    }

    /** 待交回 */
    @GetMapping("/list/received")
    public TableDataInfo<DmsPrintDocInfo> listReceived(DmsPrintDocInfo query, PageQuery pageQuery) {
        query.setState("received");
        return DmsPages.page(baseMapper, pageQuery, wrapper(query));
    }

    @GetMapping("/list/received/abnormal")
    public TableDataInfo<DmsPrintDocInfo> listReceivedAbnormal(DmsPrintDocInfo query, PageQuery pageQuery) {
        query.setState("received");
        return DmsPages.page(baseMapper, pageQuery, wrapper(query));
    }

    /** 待关闭 */
    @GetMapping("/list/closed")
    public TableDataInfo<DmsPrintDocInfo> listClosed(DmsPrintDocInfo query, PageQuery pageQuery) {
        query.setState("returned");
        return DmsPages.page(baseMapper, pageQuery, wrapper(query));
    }

    /** 待销毁 */
    @GetMapping("/list/return")
    public TableDataInfo<DmsPrintDocInfo> listReturn(DmsPrintDocInfo query, PageQuery pageQuery) {
        query.setState("returned");
        return DmsPages.page(baseMapper, pageQuery, wrapper(query));
    }

    /** 按申请单确认/失败选择 */
    @GetMapping("/list/confirm")
    public TableDataInfo<DmsPrintDocInfo> listConfirm(@RequestParam(required = false) Long printApplyId, PageQuery pageQuery) {
        QueryWrapper<DmsPrintDocInfo> qw = new QueryWrapper<>();
        qw.eq(printApplyId != null, "print_apply_id", printApplyId);
        qw.orderByDesc("create_time");
        return DmsPages.page(baseMapper, pageQuery, qw);
    }

    /** 全程追踪 */
    @GetMapping("/list/track")
    public TableDataInfo<DmsPrintDocInfo> listTrack(DmsPrintDocInfo query, PageQuery pageQuery) {
        return DmsPages.page(baseMapper, pageQuery, wrapper(query));
    }

    /** 按申请单号查询明细 */
    @GetMapping("/list/info")
    public TableDataInfo<DmsPrintDocInfo> listInfo(@RequestParam(required = false) String applyCode, PageQuery pageQuery) {
        QueryWrapper<DmsPrintDocInfo> qw = new QueryWrapper<>();
        qw.eq(applyCode != null && !applyCode.isBlank(), "apply_code", applyCode);
        qw.orderByDesc("create_time");
        return DmsPages.page(baseMapper, pageQuery, qw);
    }

    /** 扫码查询：不存在时返回 null，前端提示“扫描内容不存在” */
    @GetMapping("/{id}")
    public R<DmsPrintDocInfo> detail(@PathVariable Long id) {
        return R.ok(baseMapper.selectById(id));
    }

    @GetMapping("/info/query-confirm-before/{printApplyId}")
    public R<Boolean> queryConfirmBefore(@PathVariable Long printApplyId) {
        return R.ok(true);
    }

    /** 打印成功确认 */
    @PutMapping("/print-success")
    public R<Void> printSuccess(@RequestBody Map<String, Object> body) {
        List<Long> ids = extractIds(body.get("printApplyIds"));
        for (Long applyId : ids) {
            DmsPrintDocInfo update = new DmsPrintDocInfo();
            update.setState("printed");
            update.setRemark(str(body.get("remark")));
            baseMapper.update(update, new QueryWrapper<DmsPrintDocInfo>().lambda()
                .eq(DmsPrintDocInfo::getPrintApplyId, applyId));
        }
        return R.ok();
    }

    /** 打印失败 */
    @PutMapping("/print-failure")
    public R<Void> printFailure(@RequestBody Map<String, Object> body) {
        List<Long> ids = extractIds(body.get("ids"));
        for (Long id : ids) {
            DmsPrintDocInfo update = new DmsPrintDocInfo();
            update.setId(id);
            update.setState("failure");
            update.setFailReason(str(body.get("failReason")));
            baseMapper.updateById(update);
        }
        return R.ok();
    }

    /** 取消打印 */
    @PutMapping("/print-cancel")
    public R<Void> printCancel(@RequestBody Map<String, Object> body) {
        List<Long> ids = extractIds(body.get("printApplyIds"));
        for (Long applyId : ids) {
            DmsPrintDocInfo update = new DmsPrintDocInfo();
            update.setState("cancelled");
            update.setRemark(str(body.get("remark")));
            baseMapper.update(update, new QueryWrapper<DmsPrintDocInfo>().lambda()
                .eq(DmsPrintDocInfo::getPrintApplyId, applyId));
        }
        return R.ok();
    }

    /** 发放 */
    @PutMapping("/print-issue")
    public R<Void> printIssue(@RequestBody Map<String, Object> body) {
        List<Long> ids = extractIds(body.get("ids"));
        for (Long id : ids) {
            DmsPrintDocInfo update = new DmsPrintDocInfo();
            update.setId(id);
            update.setState("received");
            update.setReceiveUserName(str(body.get("receiveUserName")));
            update.setIssueUserName(str(body.get("issueUserName")));
            update.setIssueUserId(str(body.get("issueUserId")));
            baseMapper.updateById(update);
        }
        return R.ok();
    }

    /** 交回 */
    @PutMapping("/print-return")
    public R<Void> printReturn(@RequestBody Map<String, Object> body) {
        List<Long> ids = extractIds(body.get("ids"));
        for (Long id : ids) {
            DmsPrintDocInfo update = new DmsPrintDocInfo();
            update.setId(id);
            update.setState("returned");
            update.setReturnUserName(str(body.get("userName")));
            update.setConfirmUserId(str(body.get("confirmUserId")));
            update.setConfirmUserName(str(body.get("confirmUserName")));
            baseMapper.updateById(update);
        }
        return R.ok();
    }

    /** 异常交回 */
    @PutMapping("/print-return/abnormal")
    public R<Void> printReturnAbnormal(@RequestBody Map<String, Object> body) {
        List<Long> ids = extractIds(body.get("ids"));
        for (Long id : ids) {
            DmsPrintDocInfo update = new DmsPrintDocInfo();
            update.setId(id);
            update.setState("abnormal");
            update.setReturnUserName(str(body.get("userId")));
            update.setAbnormalReason(str(body.get("abnormalReason")));
            baseMapper.updateById(update);
        }
        return R.ok();
    }

    /** 关闭 */
    @PutMapping("/print-close")
    public R<Void> printClose(@RequestBody Map<String, Object> body) {
        List<Long> ids = extractIds(body.get("ids"));
        for (Long id : ids) {
            DmsPrintDocInfo update = new DmsPrintDocInfo();
            update.setId(id);
            update.setState("closed");
            update.setCloseUserName(str(body.get("username")));
            update.setConfirmUserName(str(body.get("confirmUsername")));
            baseMapper.updateById(update);
        }
        return R.ok();
    }

    /** 销毁 */
    @PutMapping("/print-destroy")
    public R<Void> printDestroy(@RequestBody Map<String, Object> body) {
        List<Long> ids = extractIds(body.get("ids"));
        for (Long id : ids) {
            DmsPrintDocInfo update = new DmsPrintDocInfo();
            update.setId(id);
            update.setState("destroyed");
            update.setDestroyUserName(str(body.get("userName")));
            update.setConfirmDestroyUserName(str(body.get("confirmUserName")));
            update.setDestroyAction(str(body.get("destroyAction")));
            update.setDestroyPlace(str(body.get("destroyPlace")));
            baseMapper.updateById(update);
        }
        return R.ok();
    }

    /** 导出（占位：返回空数据集，前端按 blob 处理时为空文件） */
    @PostMapping("/export")
    public R<Void> export() {
        return R.ok();
    }

    private QueryWrapper<DmsPrintDocInfo> wrapper(DmsPrintDocInfo query) {
        QueryWrapper<DmsPrintDocInfo> qw = new QueryWrapper<>();
        qw.eq(query.getState() != null, "state", query.getState());
        qw.eq(query.getPrintApplyId() != null, "print_apply_id", query.getPrintApplyId());
        qw.eq(query.getPrintType() != null, "print_type", query.getPrintType());
        qw.like(query.getApplyCode() != null && !query.getApplyCode().isBlank(), "apply_code", query.getApplyCode());
        qw.like(query.getDocCode() != null && !query.getDocCode().isBlank(), "doc_code", query.getDocCode());
        qw.like(query.getDocName() != null && !query.getDocName().isBlank(), "doc_name", query.getDocName());
        qw.orderByDesc("create_time");
        return qw;
    }

    @SuppressWarnings("unchecked")
    private List<Long> extractIds(Object value) {
        List<Long> ids = new java.util.ArrayList<>();
        if (value instanceof List<?> list) {
            for (Object item : list) {
                ids.add(Long.valueOf(String.valueOf(item)));
            }
        } else if (value != null) {
            for (String part : String.valueOf(value).split(",")) {
                if (!part.isBlank()) {
                    ids.add(Long.valueOf(part.trim()));
                }
            }
        }
        return ids;
    }

    private String str(Object value) {
        return value == null ? null : String.valueOf(value);
    }
}
