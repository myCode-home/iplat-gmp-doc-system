package org.dromara.dms.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.dms.domain.DmsVolumesPrintPendingTask;
import org.dromara.dms.mapper.DmsVolumesPrintPendingTaskMapper;
import org.dromara.dms.util.DmsPages;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 成册打印待组册任务（Excel 导入）
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/dms/volumes-print-pending-task")
public class DmsVolumesPrintPendingTaskController {

    private final DmsVolumesPrintPendingTaskMapper baseMapper;

    /** 待组册批次列表（active / unActive） */
    @GetMapping({"/excel-list/active", "/excel-list/inactive"})
    public R<List<Map<String, Object>>> excelList() {
        List<DmsVolumesPrintPendingTask> rows = baseMapper.selectList(
            new QueryWrapper<DmsVolumesPrintPendingTask>().lambda().eq(DmsVolumesPrintPendingTask::getStatus, "pending"));
        Set<String> names = new LinkedHashSet<>();
        rows.forEach(row -> {
            if (row.getExcelName() != null) {
                names.add(row.getExcelName());
            }
        });
        List<Map<String, Object>> data = names.stream()
            .map(name -> Map.<String, Object>of("excelName", name)).toList();
        return R.ok(data);
    }

    /** 批量导入待组册任务 */
    @PostMapping("/batch")
    public R<Void> batch(@RequestBody List<DmsVolumesPrintPendingTask> tasks) {
        for (DmsVolumesPrintPendingTask task : tasks) {
            if (task.getStatus() == null) {
                task.setStatus("pending");
            }
            baseMapper.insert(task);
        }
        return R.ok();
    }

    /** 刷新批次（占位） */
    @PostMapping("/refresh")
    public R<Void> refresh(@RequestParam String excelName) {
        return R.ok();
    }

    /** 手动录入任务列表 */
    @GetMapping("/list/manual/{excelName}")
    public TableDataInfo<DmsVolumesPrintPendingTask> listManual(@PathVariable String excelName,
                                                                @RequestParam(required = false) String range,
                                                                DmsVolumesPrintPendingTask query,
                                                                PageQuery pageQuery) {
        QueryWrapper<DmsVolumesPrintPendingTask> qw = new QueryWrapper<>();
        qw.eq("excel_name", excelName);
        qw.eq(range != null && !range.isBlank(), "source", range);
        qw.eq("status", "pending");
        qw.like(query.getDocName() != null && !query.getDocName().isBlank(), "doc_name", query.getDocName());
        qw.eq(query.getDocType() != null, "doc_type", query.getDocType());
        qw.eq(query.getControlledInfo() != null, "controlled_info", query.getControlledInfo());
        qw.orderByDesc("create_time");
        return DmsPages.page(baseMapper, pageQuery, qw);
    }

    /** 文件类型下拉 */
    @GetMapping("/drop-down/doc-type/{excelName}")
    public R<List<Map<String, Object>>> docTypeDropDown(@PathVariable String excelName) {
        List<DmsVolumesPrintPendingTask> rows = baseMapper.selectList(
            new QueryWrapper<DmsVolumesPrintPendingTask>().lambda().eq(DmsVolumesPrintPendingTask::getExcelName, excelName));
        Set<String> types = new LinkedHashSet<>();
        rows.forEach(row -> {
            if (row.getDocType() != null) {
                types.add(row.getDocType());
            }
        });
        return R.ok(types.stream().map(v -> Map.<String, Object>of("dictValue", v, "dictLabel", v)).toList());
    }

    /** 受控信息下拉 */
    @GetMapping("/drop-down/controlled-info/{excelName}")
    public R<List<Map<String, Object>>> controlledInfoDropDown(@PathVariable String excelName) {
        List<DmsVolumesPrintPendingTask> rows = baseMapper.selectList(
            new QueryWrapper<DmsVolumesPrintPendingTask>().lambda().eq(DmsVolumesPrintPendingTask::getExcelName, excelName));
        Set<String> values = new LinkedHashSet<>();
        rows.forEach(row -> {
            if (row.getControlledInfo() != null) {
                values.add(row.getControlledInfo());
            }
        });
        return R.ok(values.stream().map(v -> Map.<String, Object>of("dictValue", v, "dictLabel", v)).toList());
    }

    /** 接受（active/inactive 通用，body 为 rowId 数组）；返回被拒绝的 {docCode: docName} 映射 */
    @PutMapping({"/accept/active", "/accept/inactive"})
    public R<Map<String, String>> accept(@RequestBody List<String> rowIds) {
        List<DmsVolumesPrintPendingTask> rejected = new ArrayList<>();
        for (String rowId : rowIds) {
            DmsVolumesPrintPendingTask task = baseMapper.selectList(
                    new QueryWrapper<DmsVolumesPrintPendingTask>().lambda().eq(DmsVolumesPrintPendingTask::getRowId, rowId))
                .stream().findFirst().orElse(null);
            if (task == null) {
                continue;
            }
            task.setStatus("accepted");
            baseMapper.updateById(task);
        }
        return R.ok(Map.of());
    }
}
