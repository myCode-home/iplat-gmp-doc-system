package org.dromara.dms.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.dms.domain.DmsDocBrowseHistory;
import org.dromara.dms.domain.DmsDocumentLib;
import org.dromara.dms.domain.DmsDocumentLibRelate;
import org.dromara.dms.domain.DmsDocumentLibReplace;
import org.dromara.dms.mapper.DmsDocBrowseHistoryMapper;
import org.dromara.dms.mapper.DmsDocumentLibMapper;
import org.dromara.dms.mapper.DmsDocumentLibRelateMapper;
import org.dromara.dms.mapper.DmsDocumentLibReplaceMapper;
import org.dromara.dms.util.DmsPages;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 文件库
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/dms/document-lib")
public class DmsDocumentLibController {

    private final DmsDocumentLibMapper baseMapper;
    private final DmsDocumentLibReplaceMapper replaceMapper;
    private final DmsDocumentLibRelateMapper relateMapper;
    private final DmsDocBrowseHistoryMapper historyMapper;

    /** 全量列表（供选择弹窗使用，支持 status/activeStatus/useState 过滤） */
    @GetMapping("/list")
    public TableDataInfo<DmsDocumentLib> list(DmsDocumentLib query, PageQuery pageQuery) {
        return DmsPages.page(baseMapper, pageQuery, wrapper(query));
    }

    /** 生效文件库 */
    @GetMapping("/list/active")
    public TableDataInfo<DmsDocumentLib> listActive(DmsDocumentLib query, PageQuery pageQuery) {
        query.setUseState("active");
        return DmsPages.page(baseMapper, pageQuery, wrapper(query));
    }

    /** 未生效文件库 */
    @GetMapping("/list/overdue")
    public TableDataInfo<DmsDocumentLib> listPending(DmsDocumentLib query, PageQuery pageQuery) {
        query.setUseState("pending");
        return DmsPages.page(baseMapper, pageQuery, wrapper(query));
    }

    /** 失效/历史文件库 */
    @GetMapping("/list/history")
    public TableDataInfo<DmsDocumentLib> listHistory(DmsDocumentLib query, PageQuery pageQuery) {
        query.setUseState("invalid");
        return DmsPages.page(baseMapper, pageQuery, wrapper(query));
    }

    /** 可关联的生效文件 */
    @GetMapping("/list/relate")
    public TableDataInfo<DmsDocumentLib> listRelateCandidates(DmsDocumentLib query, PageQuery pageQuery) {
        query.setUseState("active");
        return DmsPages.page(baseMapper, pageQuery, wrapper(query));
    }

    /** 可替代的生效文件 */
    @GetMapping("/list/replace")
    public TableDataInfo<DmsDocumentLib> listReplaceCandidates(DmsDocumentLib query, PageQuery pageQuery) {
        query.setUseState("active");
        return DmsPages.page(baseMapper, pageQuery, wrapper(query));
    }

    /** 借阅申请可选生效文件 */
    @GetMapping("/list/borrow/active")
    public TableDataInfo<DmsDocumentLib> listBorrowActive(DmsDocumentLib query, PageQuery pageQuery) {
        query.setUseState("active");
        return DmsPages.page(baseMapper, pageQuery, wrapper(query));
    }

    /** 操作单/批量修订可选生效文件 */
    @GetMapping("/list/operate-order/active")
    public TableDataInfo<DmsDocumentLib> listOperateOrderActive(DmsDocumentLib query, PageQuery pageQuery) {
        query.setUseState("active");
        return DmsPages.page(baseMapper, pageQuery, wrapper(query));
    }

    /** 其他版本（同编号文件） */
    @GetMapping("/list/ohter/{id}")
    public TableDataInfo<DmsDocumentLib> listOtherVersion(@PathVariable Long id, DmsDocumentLib query, PageQuery pageQuery) {
        DmsDocumentLib current = baseMapper.selectById(id);
        QueryWrapper<DmsDocumentLib> qw = wrapper(query);
        if (current != null) {
            qw.eq("dl_code", current.getDlCode());
            qw.ne("id", id);
        }
        return DmsPages.page(baseMapper, pageQuery, qw);
    }

    /** 关联文件列表 */
    @GetMapping("/list/relate/{id}")
    public TableDataInfo<DmsDocumentLib> listRelate(@PathVariable Long id, PageQuery pageQuery) {
        List<DmsDocumentLibRelate> relations = relateMapper.selectList(
            new QueryWrapper<DmsDocumentLibRelate>().lambda().eq(DmsDocumentLibRelate::getLibId, id));
        return DmsPages.pageByIds(baseMapper, pageQuery,
            relations.stream().map(DmsDocumentLibRelate::getRelateLibId).toList());
    }

    /** 详情（同时记录浏览历史） */
    @GetMapping("/{id}")
    public R<DmsDocumentLib> detail(@PathVariable Long id) {
        DmsDocumentLib lib = baseMapper.selectById(id);
        if (lib != null) {
            List<DmsDocumentLib> libs = replaceMapper.selectList(
                    new QueryWrapper<DmsDocumentLibReplace>().lambda().eq(DmsDocumentLibReplace::getLibId, id))
                .stream().map(item -> baseMapper.selectById(item.getReplaceLibId())).toList();
            lib.setLibs(libs);
            DmsDocBrowseHistory history = new DmsDocBrowseHistory();
            history.setDocumentLibId(id);
            history.setDlCode(lib.getDlCode());
            history.setDlName(lib.getDlName());
            history.setDlVersion(lib.getDlVersion());
            history.setBrowseUserName(LoginHelper.getUsername());
            history.setBrowseTime(new Date());
            historyMapper.insert(history);
        }
        return R.ok(lib);
    }

    /** 修订/编辑前校验：code 必须为 0 表示允许 */
    @GetMapping("/judge/revise/{id}")
    public Map<String, Object> judgeRevise(@PathVariable Long id) {
        return Map.of("code", 0, "msg", "允许操作");
    }

    @PostMapping
    public R<Void> add(@RequestBody DmsDocumentLib lib) {
        fillDefaults(lib);
        baseMapper.insert(lib);
        saveReplaceRelations(lib);
        return R.ok();
    }

    @PutMapping
    public R<Void> edit(@RequestBody DmsDocumentLib lib) {
        baseMapper.updateById(lib);
        saveReplaceRelations(lib);
        return R.ok();
    }

    @DeleteMapping("/{id}")
    public R<Void> remove(@PathVariable Long id) {
        baseMapper.deleteById(id);
        replaceMapper.delete(new QueryWrapper<DmsDocumentLibReplace>().lambda().eq(DmsDocumentLibReplace::getLibId, id));
        relateMapper.delete(new QueryWrapper<DmsDocumentLibRelate>().lambda().eq(DmsDocumentLibRelate::getLibId, id));
        return R.ok();
    }

    private QueryWrapper<DmsDocumentLib> wrapper(DmsDocumentLib query) {
        QueryWrapper<DmsDocumentLib> qw = new QueryWrapper<>();
        qw.eq(query.getUseState() != null, "use_state", query.getUseState());
        qw.eq(query.getStatus() != null, "status", query.getStatus());
        qw.eq(query.getActiveStatus() != null, "active_status", query.getActiveStatus());
        qw.eq(query.getDlType() != null, "dl_type", query.getDlType());
        qw.like(query.getDlCode() != null && !query.getDlCode().isBlank(), "dl_code", query.getDlCode());
        qw.like(query.getDlName() != null && !query.getDlName().isBlank(), "dl_name", query.getDlName());
        qw.like(query.getDocumentType() != null && !query.getDocumentType().isBlank() && query.getDlType() == null,
            "dl_type", query.getDocumentType());
        qw.orderByDesc("create_time");
        return qw;
    }

    private void fillDefaults(DmsDocumentLib lib) {
        if (lib.getUseState() == null) {
            lib.setUseState("pending");
        }
        if (lib.getStatus() == null) {
            lib.setStatus("active");
        }
        if (lib.getActiveStatus() == null) {
            lib.setActiveStatus("active");
        }
        if (lib.getDlDeptName() == null) {
            lib.setDlDeptName(LoginHelper.getDeptName());
        }
        if (lib.getDlDeptId() == null) {
            lib.setDlDeptId(LoginHelper.getDeptId());
        }
        if (lib.getDrafterId() == null) {
            lib.setDrafterId(LoginHelper.getUserId());
        }
        if (lib.getDraftNickName() == null) {
            lib.setDraftNickName(LoginHelper.getUsername());
        }
    }

    private void saveReplaceRelations(DmsDocumentLib lib) {
        if (lib.getLibs() == null) {
            return;
        }
        replaceMapper.delete(new QueryWrapper<DmsDocumentLibReplace>().lambda().eq(DmsDocumentLibReplace::getLibId, lib.getId()));
        for (DmsDocumentLib item : lib.getLibs()) {
            if (item.getId() == null) {
                continue;
            }
            DmsDocumentLibReplace replace = new DmsDocumentLibReplace();
            replace.setLibId(lib.getId());
            replace.setReplaceLibId(item.getId());
            replaceMapper.insert(replace);
        }
    }
}
