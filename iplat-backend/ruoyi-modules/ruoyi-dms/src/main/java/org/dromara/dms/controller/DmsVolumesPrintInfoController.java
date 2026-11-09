package org.dromara.dms.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.dms.domain.DmsVolumesPrintInfo;
import org.dromara.dms.mapper.DmsVolumesPrintInfoMapper;
import org.dromara.dms.util.DmsPages;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 成册打印件（发放/领用/交回/销毁状态机）
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/dms/volumes-print-info")
public class DmsVolumesPrintInfoController {

    private final DmsVolumesPrintInfoMapper baseMapper;

    @GetMapping("/list/track")
    public TableDataInfo<DmsVolumesPrintInfo> listTrack(DmsVolumesPrintInfo query, PageQuery pageQuery) {
        return DmsPages.page(baseMapper, pageQuery, wrapper(query));
    }

    @GetMapping("/list/all")
    public TableDataInfo<DmsVolumesPrintInfo> listAll(DmsVolumesPrintInfo query, PageQuery pageQuery) {
        return DmsPages.page(baseMapper, pageQuery, wrapper(query));
    }

    /** 待发放 */
    @GetMapping("/list/received")
    public TableDataInfo<DmsVolumesPrintInfo> listReceived(DmsVolumesPrintInfo query, PageQuery pageQuery) {
        query.setState("printed");
        return DmsPages.page(baseMapper, pageQuery, wrapper(query));
    }

    /** 待销毁（已交回） */
    @GetMapping("/list/return")
    public TableDataInfo<DmsVolumesPrintInfo> listReturn(DmsVolumesPrintInfo query, PageQuery pageQuery) {
        query.setState("returned");
        return DmsPages.page(baseMapper, pageQuery, wrapper(query));
    }

    /** 按申请单追踪（data 数组） */
    @GetMapping("/track/apply/{volumesPrintId}")
    public R<List<DmsVolumesPrintInfo>> trackByApply(@PathVariable Long volumesPrintId) {
        return R.ok(baseMapper.selectList(new QueryWrapper<DmsVolumesPrintInfo>().lambda()
            .eq(DmsVolumesPrintInfo::getVolumesPrintApplyId, volumesPrintId)));
    }

    /** 打印异常信息（占位空列表） */
    @GetMapping("/print-error/{volumesPrintId}")
    public R<List<Map<String, Object>>> printError(@PathVariable Long volumesPrintId) {
        return R.ok(List.of());
    }

    /** 扫码查询单个打印件 */
    @GetMapping("/issue/{id}")
    public R<DmsVolumesPrintInfo> issueDetail(@PathVariable Long id) {
        return R.ok(baseMapper.selectById(id));
    }

    /** 打印前校验 */
    @GetMapping("/info/query-confirm-before/{volumesPrintApplyId}")
    public R<Boolean> queryConfirmBefore(@PathVariable Long volumesPrintApplyId) {
        return R.ok(true);
    }

    /** 发放 */
    @PutMapping("/issue")
    public R<Void> issue(@RequestBody Map<String, Object> body) {
        for (Long id : extractIds(body.get("ids"))) {
            DmsVolumesPrintInfo update = new DmsVolumesPrintInfo();
            update.setId(id);
            update.setState("issued");
            update.setIssueUserName(str(body.get("issueUserName")));
            update.setReceiveUserName(str(body.get("receiveUserName")));
            baseMapper.updateById(update);
        }
        return R.ok();
    }

    /** 领用确认 */
    @PutMapping("/receive")
    public R<Void> receive(@RequestBody Map<String, Object> body) {
        for (Long id : extractIds(body.get("ids"))) {
            DmsVolumesPrintInfo update = new DmsVolumesPrintInfo();
            update.setId(id);
            update.setState("received");
            update.setReceiveUserName(str(body.get("receiveUserName")));
            baseMapper.updateById(update);
        }
        return R.ok();
    }

    /** 交回 */
    @PutMapping("/return")
    public R<Void> returnBack(@RequestBody Map<String, Object> body) {
        Object details = body.get("details");
        if (details instanceof List<?> list) {
            for (Object item : list) {
                if (item instanceof Map<?, ?> map) {
                    Long id = map.get("id") == null ? null : Long.valueOf(String.valueOf(map.get("id")));
                    if (id != null) {
                        DmsVolumesPrintInfo update = new DmsVolumesPrintInfo();
                        update.setId(id);
                        update.setState("returned");
                        update.setReturnUserName(str(body.get("userName")));
                        baseMapper.updateById(update);
                    }
                }
            }
        }
        return R.ok();
    }

    /** 异常交回 */
    @PutMapping("/return/abnormal")
    public R<Void> returnAbnormal(@RequestBody Map<String, Object> body) {
        for (Long id : extractIds(body.get("ids"))) {
            DmsVolumesPrintInfo update = new DmsVolumesPrintInfo();
            update.setId(id);
            update.setState("abnormalReturned");
            update.setAbnormalReason(str(body.get("abnormalReason")));
            update.setReturnUserName(str(body.get("userName")));
            baseMapper.updateById(update);
        }
        return R.ok();
    }

    /** 扫码交回查询单个打印件 */
    @GetMapping("/return/{id}")
    public R<DmsVolumesPrintInfo> returnDetail(@PathVariable Long id) {
        return R.ok(baseMapper.selectById(id));
    }

    /** 销毁 */
    @PutMapping("/destroy")
    public R<Void> destroy(@RequestBody Map<String, Object> body) {
        for (Long id : extractIds(body.get("ids"))) {
            DmsVolumesPrintInfo update = new DmsVolumesPrintInfo();
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

    /** 取消打印 */
    @PostMapping("/print-cancel")
    public R<Void> printCancel(@RequestBody Map<String, Object> body) {
        for (Long id : extractIds(body.get("printApplyIds"))) {
            DmsVolumesPrintInfo update = new DmsVolumesPrintInfo();
            update.setId(id);
            update.setState("cancelled");
            baseMapper.updateById(update);
        }
        return R.ok();
    }

    /** 签名校验（全部放行，code 200 即可） */
    @PostMapping({"/issue/signature/judgment", "/receive/signature/judgment",
        "/issue/signature", "/receive/signature", "/return/signature", "/returnConfirm/signature",
        "/abnormalReturn/signature", "/abnormalReturnConfirm/signature",
        "/destroy/signature", "/destroyConfirm/signature"})
    public R<Void> signature(@RequestBody Map<String, Object> body) {
        return R.ok();
    }

    private QueryWrapper<DmsVolumesPrintInfo> wrapper(DmsVolumesPrintInfo query) {
        QueryWrapper<DmsVolumesPrintInfo> qw = new QueryWrapper<>();
        qw.eq(query.getState() != null, "state", query.getState());
        qw.eq(query.getVolumesPrintApplyId() != null, "volumes_print_apply_id", query.getVolumesPrintApplyId());
        qw.like(query.getApplyCode() != null && !query.getApplyCode().isBlank(), "apply_code", query.getApplyCode());
        qw.like(query.getVolumesName() != null && !query.getVolumesName().isBlank(), "volumes_name", query.getVolumesName());
        qw.like(query.getDocName() != null && !query.getDocName().isBlank(), "doc_name", query.getDocName());
        qw.orderByDesc("create_time");
        return qw;
    }

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
