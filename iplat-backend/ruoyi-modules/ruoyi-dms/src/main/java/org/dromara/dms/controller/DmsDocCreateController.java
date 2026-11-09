package org.dromara.dms.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.dms.domain.DmsDocCreate;
import org.dromara.dms.domain.DmsDocCreateLink;
import org.dromara.dms.domain.DmsDocCreateSupersede;
import org.dromara.dms.handler.DmsFlowSupport;
import org.dromara.dms.mapper.DmsDocCreateLinkMapper;
import org.dromara.dms.mapper.DmsDocCreateMapper;
import org.dromara.dms.mapper.DmsDocCreateSupersedeMapper;
import org.dromara.dms.util.DmsOrderNo;
import org.dromara.dms.util.DmsPages;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;

/**
 * 文档编制 / 审批 / 追踪
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/dms/doc-create")
public class DmsDocCreateController {

    private final DmsDocCreateMapper baseMapper;
    private final DmsDocCreateSupersedeMapper supersedeMapper;
    private final DmsDocCreateLinkMapper linkMapper;
    private final org.dromara.dms.mapper.DmsDocumentLibMapper documentLibMapper;
    private final DmsFlowSupport flowSupport;

    /** 编制列表（本人草稿） */
    @GetMapping("/editList")
    public TableDataInfo<DmsDocCreate> editList(DmsDocCreate query, PageQuery pageQuery) {
        QueryWrapper<DmsDocCreate> qw = wrapper(query);
        qw.eq("status", "edit");
        qw.eq("create_by", LoginHelper.getUserId());
        return DmsPages.page(baseMapper, pageQuery, qw);
    }

    /** 审批列表 */
    @GetMapping("/approvalList")
    public TableDataInfo<DmsDocCreate> approvalList(DmsDocCreate query, PageQuery pageQuery) {
        QueryWrapper<DmsDocCreate> qw = wrapper(query);
        qw.eq("status", "approving");
        return DmsPages.page(baseMapper, pageQuery, qw);
    }

    /** 全量列表（追踪/培训等） */
    @GetMapping("/list")
    public TableDataInfo<DmsDocCreate> list(DmsDocCreate query, PageQuery pageQuery) {
        return DmsPages.page(baseMapper, pageQuery, wrapper(query));
    }

    @GetMapping("/{id}")
    public R<DmsDocCreate> detail(@PathVariable Long id,
                                  @RequestParam(value = "isQuerySupersedeAndLink", required = false) Boolean withRelations) {
        DmsDocCreate doc = baseMapper.selectById(id);
        if (doc != null && Boolean.TRUE.equals(withRelations)) {
            List<DmsDocCreateSupersede> supersedeList = supersedeMapper.selectList(
                new QueryWrapper<DmsDocCreateSupersede>().lambda().eq(DmsDocCreateSupersede::getDocCreateId, id));
            supersedeList.forEach(item -> item.setDocumentLibInfo(
                item.getDocumentLibId() == null ? null : documentLibMapper.selectById(item.getDocumentLibId())));
            doc.setSupersedeList(supersedeList);
            List<DmsDocCreateLink> linkList = linkMapper.selectList(
                new QueryWrapper<DmsDocCreateLink>().lambda().eq(DmsDocCreateLink::getDocCreateId, id));
            linkList.forEach(item -> item.setDocumentLibInfo(
                item.getDocumentLibId() == null ? null : documentLibMapper.selectById(item.getDocumentLibId())));
            doc.setDocCreateLinkList(linkList);
        }
        return R.ok(doc);
    }

    @GetMapping("/file/{id}")
    public R<String> file(@PathVariable Long id) {
        return R.ok();
    }

    @PostMapping
    public R<Void> add(@RequestBody DmsDocCreate doc) {
        fillDefaults(doc);
        baseMapper.insert(doc);
        saveRelations(doc);
        return R.ok();
    }

    @PutMapping
    public R<Void> edit(@RequestBody DmsDocCreate doc) {
        baseMapper.updateById(doc);
        saveRelations(doc);
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
    public R<Void> submitAndFlowStart(@RequestBody DmsDocCreate doc) {
        if (doc.getApproveComments() != null) {
            DmsDocCreate update = new DmsDocCreate();
            update.setId(doc.getId());
            update.setApproveComments(doc.getApproveComments());
            baseMapper.updateById(update);
        }
        flowSupport.start("docCreate", doc.getId());
        return R.ok();
    }

    /** 离线文件上传 */
    @PostMapping("/offline/upload/{id}")
    public R<Void> offlineUpload(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        DmsDocCreate update = new DmsDocCreate();
        update.setId(id);
        update.setEditStatus("uploaded");
        baseMapper.updateById(update);
        return R.ok();
    }

    private QueryWrapper<DmsDocCreate> wrapper(DmsDocCreate query) {
        QueryWrapper<DmsDocCreate> qw = new QueryWrapper<>();
        qw.eq(query.getStatus() != null, "status", query.getStatus());
        qw.eq(query.getTrainStatus() != null, "train_status", query.getTrainStatus());
        qw.eq(query.getOperateSource() != null, "operate_source", query.getOperateSource());
        qw.eq(query.getDocumentType() != null, "document_type", query.getDocumentType());
        qw.like(query.getDocumentCode() != null && !query.getDocumentCode().isBlank(), "document_code", query.getDocumentCode());
        qw.like(query.getDocumentName() != null && !query.getDocumentName().isBlank(), "document_name", query.getDocumentName());
        qw.orderByDesc("create_time");
        return qw;
    }

    private void fillDefaults(DmsDocCreate doc) {
        if (doc.getStatus() == null) {
            doc.setStatus("edit");
        }
        if (doc.getOrderNumber() == null) {
            doc.setOrderNumber(DmsOrderNo.next("DOC"));
        }
        if (doc.getDocumentDeptName() == null) {
            doc.setDocumentDeptName(LoginHelper.getDeptName());
        }
        if (doc.getDocumentDeptId() == null) {
            doc.setDocumentDeptId(LoginHelper.getDeptId());
        }
        if (doc.getDrafterId() == null) {
            doc.setDrafterId(LoginHelper.getUserId());
        }
        if (doc.getDrafterName() == null) {
            doc.setDrafterName(LoginHelper.getUsername());
        }
    }

    private void saveRelations(DmsDocCreate doc) {
        supersedeMapper.delete(new QueryWrapper<DmsDocCreateSupersede>().lambda().eq(DmsDocCreateSupersede::getDocCreateId, doc.getId()));
        if (doc.getSupersedeDocumentIds() != null && !doc.getSupersedeDocumentIds().isBlank()) {
            Arrays.stream(doc.getSupersedeDocumentIds().split(","))
                .map(String::trim).filter(s -> !s.isEmpty()).map(Long::valueOf)
                .forEach(libId -> {
                    DmsDocCreateSupersede item = new DmsDocCreateSupersede();
                    item.setDocCreateId(doc.getId());
                    item.setDocumentLibId(libId);
                    supersedeMapper.insert(item);
                });
        }
        linkMapper.delete(new QueryWrapper<DmsDocCreateLink>().lambda().eq(DmsDocCreateLink::getDocCreateId, doc.getId()));
        if (doc.getRelateDocumentIds() != null && !doc.getRelateDocumentIds().isBlank()) {
            List<String> ids = Arrays.stream(doc.getRelateDocumentIds().split(",")).map(String::trim).toList();
            List<String> types = doc.getRelateDocumentTypes() == null ? List.of()
                : Arrays.stream(doc.getRelateDocumentTypes().split(",")).map(String::trim).toList();
            for (int i = 0; i < ids.size(); i++) {
                if (ids.get(i).isEmpty()) {
                    continue;
                }
                DmsDocCreateLink item = new DmsDocCreateLink();
                item.setDocCreateId(doc.getId());
                item.setDocumentLibId(Long.valueOf(ids.get(i)));
                item.setLinkType(i < types.size() ? types.get(i) : "1");
                linkMapper.insert(item);
            }
        }
    }
}
