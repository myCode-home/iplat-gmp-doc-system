package org.dromara.dms.handler;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.domain.event.ProcessEvent;
import org.dromara.common.core.service.WorkflowService;
import org.dromara.common.json.utils.JsonUtils;
import org.dromara.dms.domain.*;
import org.dromara.dms.mapper.*;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * DMS 审批事件处理：同步业务状态，并在审批通过后执行生效动作
 *
 * <p>状态映射：waiting→approving，finish→approved，back→retreat，cancel→edit，
 * termination/invalid→failure。</p>
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class DmsFlowEventHandler {

    private final WorkflowService workflowService;
    private final DmsFlowBusinessMapper flowBusinessMapper;
    private final DmsBatchReviseOrderMainMapper batchReviseOrderMainMapper;
    private final DmsDocOperateOrderMapper docOperateOrderMapper;
    private final DmsTmplPreparationMapper tmplPreparationMapper;
    private final DmsTmplLibMapper tmplLibMapper;
    private final DmsTmplSnapshotMapper tmplSnapshotMapper;
    private final DmsDocCreateMapper docCreateMapper;
    private final DmsDocCreateSupersedeMapper supersedeMapper;
    private final DmsDocCreateLinkMapper linkMapper;
    private final DmsDocCreateSnapshotMapper docSnapshotMapper;
    private final DmsDocumentLibMapper documentLibMapper;
    private final DmsDocumentLibReplaceMapper replaceMapper;
    private final DmsDocumentLibRelateMapper relateMapper;
    private final DmsDocRecordCreateMapper recordCreateMapper;
    private final DmsDocRecordSnapshotMapper recordSnapshotMapper;
    private final DmsDocumentRecordLibMapper recordLibMapper;
    private final DmsDocInvalidOrderMapper invalidOrderMapper;
    private final DmsDocInvalidOrderItemMapper invalidItemMapper;
    private final DmsDocRecordFailureOrderMapper failureOrderMapper;
    private final DmsDocRecordChangeOrderMapper changeOrderMapper;
    private final DmsDocumentLendOrderMapper lendOrderMapper;
    private final DmsDocumentLendLibMapper lendLibMapper;
    private final DmsPrintApplyMapper printApplyMapper;
    private final DmsPrintDocInfoMapper printDocInfoMapper;
    private final DmsPrintApplyBatchMapper batchMapper;
    private final DmsPrintApplyBatchDetailMapper batchDetailMapper;
    private final DmsVolumesPrintApplyMapper volumesApplyMapper;
    private final DmsVolumesPrintApplyDetailMapper volumesDetailMapper;
    private final DmsVolumesPrintInfoMapper volumesInfoMapper;
    private final DmsTrainingRecordMapper trainingRecordMapper;
    @EventListener(condition = "#event.flowCode == 'dms_common_approval'")
    @Transactional(rollbackFor = Exception.class)
    public void onProcess(ProcessEvent event) {
        String businessId = event.getBusinessId();
        DmsFlowBusiness fb = flowBusinessMapper.selectOne(new LambdaQueryWrapper<DmsFlowBusiness>()
            .eq(DmsFlowBusiness::getBusinessId, businessId));
        if (fb == null) {
            return;
        }
        boolean submit = Boolean.TRUE.equals(event.getSubmit());
        String flowStatus = event.getStatus();
        String bizStatus;
        if (submit || "waiting".equals(flowStatus)) {
            bizStatus = "approving";
        } else if ("finish".equals(flowStatus)) {
            bizStatus = "approved";
        } else if ("back".equals(flowStatus)) {
            bizStatus = "retreat";
        } else if ("cancel".equals(flowStatus)) {
            bizStatus = "edit";
        } else if ("termination".equals(flowStatus) || "invalid".equals(flowStatus)) {
            bizStatus = "failure";
        } else {
            log.info("DMS 审批事件忽略状态: {} ({})", flowStatus, fb.getBusinessType());
            return;
        }
        Long instanceId = workflowService.getInstanceIdByBusinessId(businessId);
        updateBusinessStatus(fb.getBusinessType(), Long.valueOf(businessId), bizStatus, instanceId);
        if ("approved".equals(bizStatus)) {
            applyApprovedEffect(fb.getBusinessType(), Long.valueOf(businessId));
        }
    }

    private void updateBusinessStatus(String type, Long businessId, String status, Long workflowId) {
        switch (type) {
            case "tmplPreparation" -> {
                DmsTmplPreparation e = tmplPreparationMapper.selectById(businessId);
                if (e != null) {
                    e.setStatus(status);
                    e.setWorkflowId(workflowId);
                    tmplPreparationMapper.updateById(e);
                }
            }
            case "docCreate" -> {
                DmsDocCreate e = docCreateMapper.selectById(businessId);
                if (e != null) {
                    e.setStatus(status);
                    e.setWorkflowId(workflowId);
                    docCreateMapper.updateById(e);
                }
            }
            case "docRecordCreate" -> {
                DmsDocRecordCreate e = recordCreateMapper.selectById(businessId);
                if (e != null) {
                    e.setStatus(status);
                    e.setWorkflowId(workflowId);
                    recordCreateMapper.updateById(e);
                }
            }
            case "docRecordChangeOrder" -> {
                DmsDocRecordChangeOrder e = changeOrderMapper.selectById(businessId);
                if (e != null) {
                    e.setStatus(status);
                    e.setWorkflowId(workflowId);
                    changeOrderMapper.updateById(e);
                }
            }
            case "docInvalidOrder" -> {
                DmsDocInvalidOrder e = invalidOrderMapper.selectById(businessId);
                if (e != null) {
                    e.setStatus(status);
                    e.setWorkflowId(workflowId);
                    invalidOrderMapper.updateById(e);
                }
            }
            case "docRecordFailureOrder" -> {
                DmsDocRecordFailureOrder e = failureOrderMapper.selectById(businessId);
                if (e != null) {
                    e.setStatus(status);
                    e.setWorkflowId(workflowId);
                    failureOrderMapper.updateById(e);
                }
            }
            case "documentLendOrder" -> {
                DmsDocumentLendOrder e = lendOrderMapper.selectById(businessId);
                if (e != null) {
                    e.setStatus(status);
                    e.setWorkflowId(workflowId);
                    lendOrderMapper.updateById(e);
                }
            }
            case "printApply" -> {
                DmsPrintApply e = printApplyMapper.selectById(businessId);
                if (e != null) {
                    e.setStatus(status);
                    e.setWorkflowId(workflowId);
                    printApplyMapper.updateById(e);
                }
            }
            case "printApplyBatch" -> {
                DmsPrintApplyBatch e = batchMapper.selectById(businessId);
                if (e != null) {
                    e.setStatus(status);
                    e.setWorkflowId(workflowId);
                    batchMapper.updateById(e);
                }
            }
            case "volumesPrintApply" -> {
                DmsVolumesPrintApply e = volumesApplyMapper.selectById(businessId);
                if (e != null) {
                    e.setStatus(status);
                    e.setWorkflowId(workflowId);
                    volumesApplyMapper.updateById(e);
                }
            }
            case "batchReviseOrderMain" -> {
                DmsBatchReviseOrderMain e = batchReviseOrderMainMapper.selectById(businessId);
                if (e != null) {
                    e.setStatus(status);
                    e.setWorkflowId(workflowId);
                    batchReviseOrderMainMapper.updateById(e);
                }
            }
            case "docOperateOrder" -> {
                DmsDocOperateOrder e = docOperateOrderMapper.selectById(businessId);
                if (e != null) {
                    e.setStatus(status);
                    e.setWorkflowId(workflowId);
                    docOperateOrderMapper.updateById(e);
                }
            }
            default -> log.warn("未知 DMS 业务类型: {}", type);
        }
    }

    /**
     * 审批通过后的业务生效动作
     */
    private void applyApprovedEffect(String type, Long businessId) {
        switch (type) {
            case "tmplPreparation" -> approveTmplPreparation(businessId);
            case "docCreate" -> approveDocCreate(businessId);
            case "docRecordCreate" -> approveDocRecordCreate(businessId);
            case "docInvalidOrder" -> approveDocInvalidOrder(businessId);
            case "docRecordFailureOrder" -> approveRecordFailureOrder(businessId);
            case "docRecordChangeOrder" -> approveRecordChangeOrder(businessId);
            case "documentLendOrder" -> approveLendOrder(businessId);
            case "printApply" -> approvePrintApply(businessId);
            case "printApplyBatch" -> approvePrintApplyBatch(businessId);
            case "volumesPrintApply" -> approveVolumesPrintApply(businessId);
            default -> log.info("业务类型 {} 审批通过，无需生效动作", type);
        }
    }

    private void approveTmplPreparation(Long id) {
        DmsTmplPreparation p = tmplPreparationMapper.selectById(id);
        if (p == null) {
            return;
        }
        DmsTmplLib lib = BeanUtil.toBean(p, DmsTmplLib.class);
        lib.setId(null);
        lib.setStatus("active");
        lib.setActiveStatus("active");
        tmplLibMapper.insert(lib);
        DmsTmplSnapshot snap = new DmsTmplSnapshot();
        snap.setBizId(lib.getId());
        snap.setTmplCode(lib.getTmplCode());
        snap.setTmplVersion(lib.getTmplVersion());
        snap.setTmplVersionMinor(lib.getTmplVersionMinor());
        snap.setSnapshotName(lib.getTmplName() + " " + lib.getTmplVersion());
        snap.setContent(JsonUtils.toJsonString(lib));
        tmplSnapshotMapper.insert(snap);
    }

    private void approveDocCreate(Long id) {
        DmsDocCreate d = docCreateMapper.selectById(id);
        if (d == null) {
            return;
        }
        DmsDocumentLib lib = new DmsDocumentLib();
        lib.setDlCode(d.getDocumentCode());
        lib.setDlName(d.getDocumentName());
        lib.setDlType(d.getDocumentType());
        lib.setDlVersion(d.getDocumentVersion());
        lib.setDlLevel(d.getDocumentLevel());
        lib.setDlDeptId(d.getDocumentDeptId());
        lib.setDlDeptName(d.getDocumentDeptName());
        lib.setIssueDeptName(d.getDistributeDeptName());
        lib.setTmplLibId(d.getTmplLibId());
        lib.setTmplLibCode(d.getTmplLibCode());
        lib.setTmplLibName(d.getTmplLibName());
        lib.setTmplSource(d.getTmplSource());
        lib.setDrafterId(d.getDrafterId());
        lib.setDraftNickName(d.getDrafterName());
        lib.setDistributeDeptId(d.getDistributeDeptId());
        lib.setExt(d.getExt());
        String source = d.getOperateSource();
        boolean revise = "manualRevision".equals(source) || "fromBatchReviseApply".equals(source) || "batchAutoCreate".equals(source);
        lib.setDocSource(revise ? "revise" : "new");
        lib.setDocOperationSource(switch (source == null ? "" : source) {
            case "fromInput" -> "fromInput";
            case "manualRevision" -> "manualRevision";
            default -> "autoCreate";
        });
        lib.setUseState("pending");
        lib.setStatus("active");
        lib.setActiveStatus("active");
        documentLibMapper.insert(lib);
        // 替代关系：新文件替代旧文件
        List<DmsDocCreateSupersede> supersedeList = supersedeMapper.selectList(
            new LambdaQueryWrapper<DmsDocCreateSupersede>().eq(DmsDocCreateSupersede::getDocCreateId, id));
        for (DmsDocCreateSupersede item : supersedeList) {
            DmsDocumentLibReplace replace = new DmsDocumentLibReplace();
            replace.setLibId(lib.getId());
            replace.setReplaceLibId(item.getDocumentLibId());
            replaceMapper.insert(replace);
            DmsDocumentLib old = documentLibMapper.selectById(item.getDocumentLibId());
            if (old != null) {
                old.setUseState("invalid");
                old.setStatus("failure");
                old.setDeactiveSource("replaceFailure");
                old.setDeactiveReason("被新版本替代");
                documentLibMapper.updateById(old);
            }
        }
        // 关联关系
        List<DmsDocCreateLink> linkList = linkMapper.selectList(
            new LambdaQueryWrapper<DmsDocCreateLink>().eq(DmsDocCreateLink::getDocCreateId, id));
        for (DmsDocCreateLink item : linkList) {
            DmsDocumentLibRelate relate = new DmsDocumentLibRelate();
            relate.setLibId(lib.getId());
            relate.setRelateLibId(item.getDocumentLibId());
            relate.setLinkType(item.getLinkType());
            relateMapper.insert(relate);
        }
        DmsDocCreateSnapshot snap = new DmsDocCreateSnapshot();
        snap.setBizId(lib.getId());
        snap.setDocumentCode(lib.getDlCode());
        snap.setDocumentVersion(lib.getDlVersion());
        snap.setSnapshotName(lib.getDlName() + " " + lib.getDlVersion());
        snap.setContent(JsonUtils.toJsonString(lib));
        docSnapshotMapper.insert(snap);
    }

    private void approveDocRecordCreate(Long id) {
        DmsDocRecordCreate r = recordCreateMapper.selectById(id);
        if (r == null) {
            return;
        }
        DmsDocumentRecordLib lib = new DmsDocumentRecordLib();
        lib.setDlrCode(r.getDocumentRecordCode());
        lib.setDlrName(r.getDocumentRecordName());
        lib.setDlrVersion(r.getDocumentRecordVersion() == null ? "V1.0" : r.getDocumentRecordVersion());
        lib.setDocumentCode(r.getDocumentCode());
        lib.setDocumentName(r.getDocumentName());
        lib.setDocumentType(r.getDocumentRecordType());
        lib.setEditWay(r.getEditWay());
        lib.setExt(r.getExt());
        lib.setStatus("active");
        lib.setActiveStatus("active");
        lib.setDlrActiveTime(new Date());
        DmsDocumentLib parent = documentLibMapper.selectOne(new LambdaQueryWrapper<DmsDocumentLib>()
            .eq(DmsDocumentLib::getDlCode, r.getDocumentCode()).last("limit 1"));
        if (parent != null) {
            lib.setDocumentLibId(parent.getId());
        }
        recordLibMapper.insert(lib);
        DmsDocRecordSnapshot snap = new DmsDocRecordSnapshot();
        snap.setBizId(lib.getId());
        snap.setDocumentRecordCode(lib.getDlrCode());
        snap.setDocumentRecordVersion(lib.getDlrVersion());
        snap.setSnapshotName(lib.getDlrName() + " " + lib.getDlrVersion());
        snap.setContent(JsonUtils.toJsonString(lib));
        recordSnapshotMapper.insert(snap);
    }

    private void approveDocInvalidOrder(Long id) {
        DmsDocInvalidOrder order = invalidOrderMapper.selectById(id);
        if (order == null) {
            return;
        }
        List<DmsDocInvalidOrderItem> items = invalidItemMapper.selectList(
            new LambdaQueryWrapper<DmsDocInvalidOrderItem>().eq(DmsDocInvalidOrderItem::getOrderId, id));
        for (DmsDocInvalidOrderItem item : items) {
            DmsDocumentLib lib = documentLibMapper.selectById(item.getDocumentLibId());
            if (lib != null) {
                lib.setUseState("invalid");
                lib.setStatus("failure");
                lib.setDeactiveReason(order.getOrderReason());
                documentLibMapper.updateById(lib);
            }
        }
    }

    private void approveRecordFailureOrder(Long id) {
        DmsDocRecordFailureOrder order = failureOrderMapper.selectById(id);
        if (order == null || order.getDocumentRecordLibraryId() == null) {
            return;
        }
        DmsDocumentRecordLib lib = recordLibMapper.selectById(order.getDocumentRecordLibraryId());
        if (lib != null) {
            lib.setActiveStatus("failure");
            lib.setDlrDeactiveTime(new Date());
            lib.setDlrDeactiveReason(order.getDocumentRecordReason());
            recordLibMapper.updateById(lib);
        }
    }

    private void approveRecordChangeOrder(Long id) {
        DmsDocRecordChangeOrder order = changeOrderMapper.selectById(id);
        if (order == null) {
            return;
        }
        DmsDocRecordCreate create = new DmsDocRecordCreate();
        create.setDocumentCode(order.getDocumentCode());
        create.setDocumentName(order.getDocumentName());
        create.setDocumentRecordCode(order.getDocumentRecordCode());
        create.setDocumentRecordName(order.getDocumentRecordName());
        create.setDocumentRecordType(order.getDocumentRecordType());
        create.setDocumentRecordLibraryId(order.getDocumentRecordLibraryId());
        create.setRecordHandleType("revise");
        create.setReviseSource("revise");
        create.setEditWay(order.getEditWay());
        create.setTmplLibId(order.getTmplLibId());
        create.setTmplLibCode(order.getTmplLibCode());
        create.setTmplLibName(order.getTmplLibName());
        create.setDrafterId(order.getDrafterId());
        create.setDraftUserName(order.getDrafterUserName());
        create.setStatus("edit");
        recordCreateMapper.insert(create);
    }

    private void approveLendOrder(Long id) {
        DmsDocumentLendOrder order = lendOrderMapper.selectById(id);
        if (order == null) {
            return;
        }
        DmsDocumentLendLib lib = new DmsDocumentLendLib();
        lib.setOrderId(order.getId());
        lib.setDocLibId(order.getDocLibId());
        lib.setDocumentCode(order.getDocumentCode());
        lib.setDocumentName(order.getDocumentName());
        lib.setLendingPeriod(order.getLendingPeriod());
        lib.setIsDownload(order.getIsDownload());
        lib.setLendDate(new Date());
        lib.setReturnDate(order.getReturnDate());
        lib.setStatus("lending");
        lendLibMapper.insert(lib);
    }

    private void approvePrintApply(Long id) {
        DmsPrintApply apply = printApplyMapper.selectById(id);
        if (apply == null) {
            return;
        }
        DmsPrintDocInfo info = new DmsPrintDocInfo();
        info.setPrintApplyId(apply.getId());
        info.setApplyCode(apply.getApplyCode());
        info.setDocOrRecordType(apply.getDocOrRecordType());
        info.setDocOrRecordId(apply.getDocOrRecordId());
        info.setDocCode(apply.getDocCode());
        info.setDocName(apply.getDocName());
        info.setDocVersion(apply.getDocVersion());
        info.setDocStatus(apply.getDocStatus());
        info.setPrintType(apply.getPrintType());
        info.setCopies(apply.getCopies());
        info.setPages(apply.getPages());
        info.setState("printed");
        info.setPrintTime(new Date());
        printDocInfoMapper.insert(info);
    }

    private void approvePrintApplyBatch(Long id) {
        DmsPrintApplyBatch batch = batchMapper.selectById(id);
        if (batch == null) {
            return;
        }
        List<DmsPrintApplyBatchDetail> details = batchDetailMapper.selectList(
            new LambdaQueryWrapper<DmsPrintApplyBatchDetail>().eq(DmsPrintApplyBatchDetail::getBatchId, id));
        for (DmsPrintApplyBatchDetail detail : details) {
            DmsPrintDocInfo info = new DmsPrintDocInfo();
            info.setPrintApplyId(batch.getId());
            info.setApplyCode(batch.getApplyCode());
            info.setDocOrRecordType(detail.getDocOrRecordType());
            info.setDocOrRecordId(detail.getDocOrRecordId());
            info.setDocCode(detail.getDocCode());
            info.setDocName(detail.getDocName());
            info.setDocVersion(detail.getDocVersion());
            info.setDocType(detail.getDocType());
            info.setDocStatus(detail.getDocStatus());
            info.setPrintType(detail.getPrintType());
            info.setCopies(detail.getApplyCopies());
            info.setPages(detail.getPages());
            info.setState("printed");
            info.setPrintTime(new Date());
            printDocInfoMapper.insert(info);
        }
    }

    private void approveVolumesPrintApply(Long id) {
        DmsVolumesPrintApply apply = volumesApplyMapper.selectById(id);
        if (apply == null) {
            return;
        }
        List<DmsVolumesPrintApplyDetail> details = volumesDetailMapper.selectList(
            new LambdaQueryWrapper<DmsVolumesPrintApplyDetail>().eq(DmsVolumesPrintApplyDetail::getApplyId, id));
        for (DmsVolumesPrintApplyDetail detail : details) {
            DmsVolumesPrintInfo info = new DmsVolumesPrintInfo();
            info.setVolumesPrintApplyId(apply.getId());
            info.setApplyCode(apply.getApplyCode());
            info.setVolumesName(apply.getVolumesName());
            info.setDocCode(detail.getDocCode());
            info.setDocName(detail.getDocName());
            info.setPages(detail.getPages());
            info.setCopies(detail.getCopies());
            info.setState("printed");
            info.setPrintTime(new Date());
            volumesInfoMapper.insert(info);
        }
    }
}
