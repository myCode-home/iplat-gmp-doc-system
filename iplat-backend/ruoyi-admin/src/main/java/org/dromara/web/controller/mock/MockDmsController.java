package org.dromara.web.controller.mock;

import jakarta.servlet.http.HttpServletRequest;
import org.dromara.common.core.domain.R;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * DMS 业务演示接口。
 *
 * <p>实习项目的 DMS 业务后端不在开源仓库中，这里按前端实际调用的 URL 返回结构一致的演示数据，
 * 使文件库、记录库、模板库、打印、借阅、培训等页面可以正常展示。</p>
 *
 * <p>该控制器只用于本地演示，不包含真实业务逻辑。</p>
 */
@RestController
@RequestMapping("/dms")
public class MockDmsController {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATETIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final String[] AUTHORS = {"张伟", "李娜", "王强", "赵敏", "陈杰", "刘洋", "杨帆", "周婷"};
    private static final String[] DEPTS = {"生产部", "质量部", "QA部", "QC部", "设备部", "注册部"};
    private static final String[] FILE_NAMES = {
        "生产设备清洁标准操作规程", "洁净区环境监测管理规程", "物料入库验收操作规程", "产品放行管理规程",
        "偏差处理标准操作规程", "变更控制管理规程", "设备维护保养操作规程", "批生产记录填写规程"
    };
    private static final String[] RECORD_NAMES = {
        "设备清洁记录", "洁净区温湿度监测记录", "物料验收记录", "产品放行审核记录",
        "偏差处理记录", "变更控制记录", "设备维护保养记录", "批生产记录"
    };
    private static final String[] TEMPLATE_NAMES = {
        "标准操作规程模板", "管理规程模板", "记录表格模板", "验证方案模板",
        "批生产记录模板", "偏差处理模板", "变更控制模板", "设备确认模板"
    };
    private static final String[] FILE_TYPES = {"操作规程", "管理规程", "记录表格", "验证文件"};
    private static final String[] STATUS = {"生效", "审批中", "草稿", "已失效", "已打印", "待培训"};

    /**
     * 查询类接口：列表返回 rows/total，其余返回 data。
     */
    @GetMapping("/**")
    public Object get(HttpServletRequest request) {
        String path = request.getRequestURI().toLowerCase();
        if (isList(path)) {
            return TableDataInfo.build(rows(path));
        }
        return R.ok(detail(path));
    }

    @PostMapping("/**")
    public R<Void> post() {
        return R.ok();
    }

    @PutMapping("/**")
    public R<Void> put() {
        return R.ok();
    }

    @DeleteMapping("/**")
    public R<Void> delete() {
        return R.ok();
    }

    private boolean isList(String path) {
        // 前端列表接口命名不统一，如 /list、activeList、unTrainList、listwithsnapshot
        return path.contains("list");
    }

    private List<Map<String, Object>> rows(String path) {
        if (path.contains("document-record-lib")) {
            return recordRows();
        }
        if (path.contains("document-lib")) {
            return fileRows(path);
        }
        if (path.contains("tmpllib") || path.contains("tmplpreparation")) {
            return templateRows();
        }
        if (path.contains("print-apply-batch")) {
            return bulkPrintRows();
        }
        if (path.contains("volumes-print")) {
            return bookPrintRows();
        }
        if (path.contains("print-apply") || path.contains("print-doc-info")) {
            return printRows();
        }
        if (path.contains("document-lend")) {
            return lendRows();
        }
        if (path.contains("doc-invalid")) {
            return invalidRows();
        }
        if (path.contains("doc-record-failure")) {
            return recordInvalidRows();
        }
        if (path.contains("doc-record-change")) {
            return recordChangeRows();
        }
        if (path.contains("doc-record-create")) {
            return recordCreateRows();
        }
        if (path.contains("doc-operate-order")) {
            return operateRows();
        }
        if (path.contains("batch-revise")) {
            return batchRevisionRows();
        }
        if (path.contains("train")) {
            return trainingRows();
        }
        if (path.contains("document-type-attribute-config")) {
            return attributeRows();
        }
        if (path.contains("document-type")) {
            return fileTypeRows();
        }
        if (path.contains("doc-create")) {
            return docCreateRows(path);
        }
        if (path.contains("browse-history")) {
            return historyRows();
        }
        return genericRows();
    }

    private List<Map<String, Object>> fileRows(String path) {
        List<Map<String, Object>> list = new ArrayList<>();
        boolean overdue = path.contains("overdue");
        boolean history = path.contains("history");
        for (int i = 0; i < 8; i++) {
            Map<String, Object> row = baseRow("FILE-2026-" + (i + 1));
            row.put("docNo", String.format("GMP-SOP-%03d", i + 1));
            row.put("docName", FILE_NAMES[i]);
            row.put("version", "V" + (i % 3 + 1) + ".0");
            row.put("documentType", "SOP");
            row.put("documentTypeName", FILE_TYPES[i % FILE_TYPES.length]);
            row.put("deptName", DEPTS[i % DEPTS.length]);
            row.put("createBy", AUTHORS[i]);
            row.put("createTime", DATETIME.format(LocalDateTime.now().minusDays(i * 3L)));
            row.put("statusName", overdue ? "已失效" : history ? "历史版本" : "生效");
            row.put("invalidDate", DATE.format(LocalDate.now().minusMonths(i + 1L)));
            row.put("browseTime", DATETIME.format(LocalDateTime.now().minusHours(i + 1L)));
            row.put("docSource", "new");
            row.put("docOperationSource", "fromInput");
            row.put("useState", overdue ? "invalid" : "active");
            row.put("ext", "{}");
            row.put("libs", List.of());
            list.add(row);
        }
        return list;
    }

    private List<Map<String, Object>> historyRows() {
        List<Map<String, Object>> list = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            Map<String, Object> row = baseRow("HIS-2026-" + (i + 1));
            row.put("docName", FILE_NAMES[i]);
            row.put("version", "V" + (i + 1) + ".0");
            row.put("browseTime", DATETIME.format(LocalDateTime.now().minusDays(i)));
            row.put("statusName", "历史版本");
            list.add(row);
        }
        return list;
    }

    private List<Map<String, Object>> recordRows() {
        List<Map<String, Object>> list = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            Map<String, Object> row = baseRow("REC-2026-" + (i + 1));
            row.put("recordNo", String.format("REC-%03d", i + 1));
            row.put("recordName", RECORD_NAMES[i]);
            row.put("docNo", String.format("GMP-SOP-%03d", i + 1));
            row.put("version", "V" + (i % 2 + 1) + ".0");
            row.put("deptName", DEPTS[i % DEPTS.length]);
            row.put("createBy", AUTHORS[i]);
            row.put("createTime", DATETIME.format(LocalDateTime.now().minusDays(i * 2L)));
            row.put("statusName", i % 4 == 0 ? "审批中" : "生效");
            list.add(row);
        }
        return list;
    }

    private List<Map<String, Object>> templateRows() {
        List<Map<String, Object>> list = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            Map<String, Object> row = baseRow("TMPL-2026-" + (i + 1));
            row.put("tmplNo", String.format("TMPL-%03d", i + 1));
            row.put("tmplName", TEMPLATE_NAMES[i]);
            row.put("version", "V" + (i % 3 + 1) + ".0");
            row.put("templateTypeName", FILE_TYPES[i % FILE_TYPES.length]);
            row.put("deptName", DEPTS[i % DEPTS.length]);
            row.put("createBy", AUTHORS[i]);
            row.put("createTime", DATETIME.format(LocalDateTime.now().minusDays(i * 4L)));
            row.put("statusName", i % 3 == 0 ? "审批中" : "生效");
            list.add(row);
        }
        return list;
    }

    private List<Map<String, Object>> docCreateRows(String path) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            Map<String, Object> row = baseRow("DOC-2026-" + (i + 1));
            row.put("docNo", String.format("DOC-%03d", i + 1));
            row.put("docName", FILE_NAMES[i]);
            row.put("version", "V" + (i % 3 + 1) + ".0");
            row.put("documentTypeName", FILE_TYPES[i % FILE_TYPES.length]);
            row.put("deptName", DEPTS[i % DEPTS.length]);
            row.put("createBy", AUTHORS[i]);
            row.put("createTime", DATETIME.format(LocalDateTime.now().minusDays(i * 2L)));
            row.put("statusName", path.contains("approval") ? "待审批" : i % 3 == 0 ? "审批中" : "草稿");
            row.put("ext", "{}");
            row.put("libs", List.of());
            list.add(row);
        }
        return list;
    }

    private List<Map<String, Object>> printRows() {
        List<Map<String, Object>> list = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            Map<String, Object> row = baseRow("PRINT-2026-" + (i + 1));
            row.put("applyNo", String.format("PRINT-%03d", i + 1));
            row.put("applyName", "受控文件打印申请（" + (i + 1) + "）");
            row.put("docNo", String.format("GMP-SOP-%03d", i + 1));
            row.put("docName", FILE_NAMES[i]);
            row.put("applicant", AUTHORS[i]);
            row.put("applyDept", DEPTS[i % DEPTS.length]);
            row.put("applyDate", DATE.format(LocalDate.now().minusDays(i * 2L)));
            row.put("printTypeName", i % 2 == 0 ? "受控打印" : "普通打印");
            row.put("docCount", 2 + i);
            row.put("statusName", STATUS[i % STATUS.length]);
            list.add(row);
        }
        return list;
    }

    private List<Map<String, Object>> bulkPrintRows() {
        List<Map<String, Object>> list = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            Map<String, Object> row = baseRow("BP-2026-" + (i + 1));
            row.put("applyNo", String.format("BP-%03d", i + 1));
            row.put("applyName", "批量打印申请（" + (i + 1) + "）");
            row.put("applicant", AUTHORS[i]);
            row.put("applyDate", DATE.format(LocalDate.now().minusDays(i * 2L)));
            row.put("printTypeName", i % 2 == 0 ? "受控打印" : "普通打印");
            row.put("statusName", STATUS[i % STATUS.length]);
            list.add(row);
        }
        return list;
    }

    private List<Map<String, Object>> bookPrintRows() {
        List<Map<String, Object>> list = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            Map<String, Object> row = baseRow("BOOK-2026-" + (i + 1));
            row.put("applyNo", String.format("BOOK-%03d", i + 1));
            row.put("applyName", "GMP文件成册（" + (i + 1) + "）");
            row.put("applicant", AUTHORS[i]);
            row.put("applyDate", DATE.format(LocalDate.now().minusDays(i * 3L)));
            row.put("statusName", STATUS[(i + 2) % STATUS.length]);
            list.add(row);
        }
        return list;
    }

    private List<Map<String, Object>> lendRows() {
        List<Map<String, Object>> list = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            Map<String, Object> row = baseRow("LEND-2026-" + (i + 1));
            row.put("lendNo", String.format("LEND-%03d", i + 1));
            row.put("lendName", FILE_NAMES[i] + "借阅");
            row.put("docNo", String.format("GMP-SOP-%03d", i + 1));
            row.put("docName", FILE_NAMES[i]);
            row.put("applicant", AUTHORS[i]);
            row.put("lendDept", DEPTS[i % DEPTS.length]);
            row.put("lendDate", DATE.format(LocalDate.now().minusDays(i * 2L)));
            row.put("returnDate", DATE.format(LocalDate.now().plusDays(7 - i)));
            row.put("statusName", i % 3 == 0 ? "待审批" : "已借阅");
            list.add(row);
        }
        return list;
    }

    private List<Map<String, Object>> invalidRows() {
        List<Map<String, Object>> list = new ArrayList<>();
        String[] reasons = {"文件已过期", "内容发生变更", "被新版本替代", "法规要求更新"};
        for (int i = 0; i < 8; i++) {
            Map<String, Object> row = baseRow("INV-2026-" + (i + 1));
            row.put("orderNo", String.format("INV-%03d", i + 1));
            row.put("docNo", String.format("GMP-SOP-%03d", i + 1));
            row.put("docName", FILE_NAMES[i]);
            row.put("invalidReason", reasons[i % reasons.length]);
            row.put("applicant", AUTHORS[i]);
            row.put("applyDate", DATE.format(LocalDate.now().minusDays(i * 2L)));
            row.put("statusName", STATUS[i % STATUS.length]);
            list.add(row);
        }
        return list;
    }

    private List<Map<String, Object>> recordInvalidRows() {
        List<Map<String, Object>> list = new ArrayList<>();
        String[] reasons = {"记录已归档", "记录内容变更", "版本已停用", "记录已过期"};
        for (int i = 0; i < 8; i++) {
            Map<String, Object> row = baseRow("RINV-2026-" + (i + 1));
            row.put("orderNo", String.format("RINV-%03d", i + 1));
            row.put("recordNo", String.format("REC-%03d", i + 1));
            row.put("recordName", RECORD_NAMES[i]);
            row.put("invalidReason", reasons[i % reasons.length]);
            row.put("applicant", AUTHORS[i]);
            row.put("applyDate", DATE.format(LocalDate.now().minusDays(i * 2L)));
            row.put("statusName", STATUS[i % STATUS.length]);
            list.add(row);
        }
        return list;
    }

    private List<Map<String, Object>> recordChangeRows() {
        List<Map<String, Object>> list = new ArrayList<>();
        String[] types = {"内容变更", "版本升级", "格式调整"};
        for (int i = 0; i < 8; i++) {
            Map<String, Object> row = baseRow("RC-2026-" + (i + 1));
            row.put("orderNo", String.format("RC-%03d", i + 1));
            row.put("recordNo", String.format("REC-%03d", i + 1));
            row.put("recordName", RECORD_NAMES[i]);
            row.put("changeTypeName", types[i % types.length]);
            row.put("applicant", AUTHORS[i]);
            row.put("applyDate", DATE.format(LocalDate.now().minusDays(i * 2L)));
            row.put("statusName", STATUS[i % STATUS.length]);
            list.add(row);
        }
        return list;
    }

    private List<Map<String, Object>> recordCreateRows() {
        List<Map<String, Object>> list = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            Map<String, Object> row = baseRow("RCR-2026-" + (i + 1));
            row.put("recordNo", String.format("REC-%03d", i + 1));
            row.put("recordName", RECORD_NAMES[i]);
            row.put("version", "V" + (i % 2 + 1) + ".0");
            row.put("docNo", String.format("GMP-SOP-%03d", i + 1));
            row.put("docName", FILE_NAMES[i]);
            row.put("createTime", DATETIME.format(LocalDateTime.now().minusDays(i * 2L)));
            row.put("statusName", STATUS[i % STATUS.length]);
            list.add(row);
        }
        return list;
    }

    private List<Map<String, Object>> operateRows() {
        List<Map<String, Object>> list = new ArrayList<>();
        String[] types = {"新增", "修订", "复制", "作废"};
        for (int i = 0; i < 8; i++) {
            Map<String, Object> row = baseRow("OP-2026-" + (i + 1));
            row.put("orderNo", String.format("OP-%03d", i + 1));
            row.put("docNo", String.format("GMP-SOP-%03d", i + 1));
            row.put("docName", FILE_NAMES[i]);
            row.put("operateTypeName", types[i % types.length]);
            row.put("applicant", AUTHORS[i]);
            row.put("applyDate", DATE.format(LocalDate.now().minusDays(i * 2L)));
            row.put("statusName", STATUS[i % STATUS.length]);
            list.add(row);
        }
        return list;
    }

    private List<Map<String, Object>> batchRevisionRows() {
        List<Map<String, Object>> list = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            Map<String, Object> row = baseRow("BR-2026-" + (i + 1));
            row.put("orderNo", String.format("BR-%03d", i + 1));
            row.put("orderName", FILE_NAMES[i] + "批量修订");
            row.put("applicant", AUTHORS[i]);
            row.put("applyDate", DATE.format(LocalDate.now().minusDays(i * 2L)));
            row.put("statusName", STATUS[i % STATUS.length]);
            list.add(row);
        }
        return list;
    }

    private List<Map<String, Object>> trainingRows() {
        List<Map<String, Object>> list = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            Map<String, Object> row = baseRow("TRAIN-2026-" + (i + 1));
            row.put("docNo", String.format("GMP-SOP-%03d", i + 1));
            row.put("docName", FILE_NAMES[i]);
            row.put("version", "V" + (i % 3 + 1) + ".0");
            row.put("trainDate", DATE.format(LocalDate.now().minusDays(i * 3L)));
            row.put("statusName", i % 3 == 0 ? "待培训" : i % 3 == 1 ? "已培训" : "已生效");
            list.add(row);
        }
        return list;
    }

    private List<Map<String, Object>> fileTypeRows() {
        List<Map<String, Object>> list = new ArrayList<>();
        String[][] types = {{"SOP", "标准操作规程"}, {"REC", "记录表格"}, {"TMPL", "模板文件"}, {"VAL", "验证文件"}};
        for (int i = 0; i < types.length; i++) {
            Map<String, Object> row = baseRow(String.valueOf(i + 1));
            row.put("parentId", "root");
            row.put("sortCode", types[i][0]);
            row.put("sortName", types[i][1]);
            row.put("typeCode", types[i][0]);
            row.put("typeName", types[i][1]);
            row.put("status", i == 3 ? "failure" : "active");
            row.put("statusName", i == 3 ? "停用" : "启用");
            row.put("parentName", "无");
            row.put("remark", "演示文件分类");
            row.put("createTime", DATETIME.format(LocalDateTime.now().minusDays(i * 10L)));
            list.add(row);
        }
        return list;
    }

    private List<Map<String, Object>> attributeRows() {
        List<Map<String, Object>> list = new ArrayList<>();
        String[][] attrs = {
            {"docNo", "文件编号", "true", "input", "是"},
            {"docName", "文件名称", "true", "input", "是"},
            {"effectiveDate", "生效日期", "false", "date", "是"},
            {"remark", "备注", "false", "textarea", "否"}
        };
        for (int i = 0; i < attrs.length; i++) {
            Map<String, Object> row = baseRow("ATTR-" + (i + 1));
            row.put("attributeCode", attrs[i][0]);
            row.put("attributeName", attrs[i][1]);
            row.put("ifRequired", attrs[i][2]);
            row.put("valueType", attrs[i][3]);
            row.put("applyFlag", attrs[i][4]);
            row.put("status", "active");
            row.put("statusName", "启用");
            row.put("sort", i + 1);
            list.add(row);
        }
        return list;
    }

    private List<Map<String, Object>> genericRows() {
        List<Map<String, Object>> list = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            Map<String, Object> row = baseRow("MOCK-" + (i + 1));
            row.put("code", String.format("MOCK-%03d", i + 1));
            row.put("name", "演示数据 " + (i + 1));
            row.put("statusName", "正常");
            row.put("createTime", DATETIME.format(LocalDateTime.now().minusDays(i)));
            list.add(row);
        }
        return list;
    }

    /**
     * 详情接口：返回一条与列表同结构的演示数据，并保留 URL 中的业务主键。
     */
    private Map<String, Object> detail(String path) {
        String id = extractId(path);
        Map<String, Object> row = rows(path).isEmpty() ? baseRow(id) : new LinkedHashMap<>(rows(path).get(0));
        row.put("id", id);
        row.putIfAbsent("ext", "{}");
        row.putIfAbsent("libs", List.of());
        row.putIfAbsent("children", List.of());
        row.putIfAbsent("attributes", List.of());
        return row;
    }

    private String extractId(String path) {
        String[] segments = path.split("/");
        for (int i = segments.length - 1; i >= 0; i--) {
            String value = segments[i];
            if (!value.isBlank() && value.matches("[A-Za-z0-9_-]+")
                && !List.of("dms", "list", "active", "history", "overdue", "approval", "edit", "track",
                "new", "revise", "print", "main", "approve", "all", "file", "details", "judge",
                "submitandflowstart", "submit-and-flow-start", "status", "dict", "tree").contains(value.toLowerCase())) {
                return value;
            }
        }
        return "MOCK-1";
    }

    private Map<String, Object> baseRow(String id) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", id);
        row.put("status", "0");
        row.put("statusName", "正常");
        row.put("createTime", DATETIME.format(LocalDateTime.now()));
        return row;
    }
}
