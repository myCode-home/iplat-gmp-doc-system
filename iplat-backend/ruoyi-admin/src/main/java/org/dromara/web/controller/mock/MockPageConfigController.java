package org.dromara.web.controller.mock;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.domain.R;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 页面/表格动态配置演示接口。
 *
 * <p>前端 SuperTable / SuperSearch / SuperForm 组件通过
 * {@code /system/page-config/page/{code}} 获取列配置。未配置的 code 返回空列配置，
 * 保证页面可以正常渲染，不会出现空指针。</p>
 */
@Slf4j
@RestController
@RequestMapping("/system/page-config")
public class MockPageConfigController {

    private static final Map<String, Object> EMPTY_CONFIG = Map.of(
        "pageConfigAlign", "center",
        "pageConfigSize", "small",
        "attributeList", List.of()
    );

    private final Map<String, Object> configs = new HashMap<>();

    public MockPageConfigController(ObjectMapper objectMapper) {
        try (InputStream input = new ClassPathResource("mock/page-configs.json").getInputStream()) {
            Map<String, Object> loaded = objectMapper.readValue(input, new TypeReference<>() {
            });
            configs.putAll(loaded);
            log.info("Mock 页面配置加载完成，共 {} 个", configs.size());
        } catch (IOException e) {
            log.warn("Mock 页面配置加载失败", e);
        }
    }

    /**
     * 按页面编码获取配置，未配置的返回按模块推断的通用列配置或空配置。
     */
    @GetMapping("/page/{code}")
    public R<Object> page(@PathVariable("code") String code) {
        Object config = configs.get(code);
        return R.ok(config != null ? config : fallbackConfig(code));
    }

    /**
     * 未显式配置的页面按编码前缀推断一套通用列，避免业务页面因缺少配置而显示空表。
     */
    private Object fallbackConfig(String code) {
        if (code.startsWith("training")) {
            return tableConfig(
                col("docNo", "文件编号", 150), col("docName", "文件名称", 240), col("version", "版本", 90),
                col("trainDate", "培训日期", 120), col("statusName", "状态", 100));
        }
        if (code.startsWith("templateEdit")) {
            return tableConfig(
                col("tmplNo", "模板编号", 150), col("tmplName", "模板名称", 240), col("version", "版本", 90),
                col("templateTypeName", "模板类型", 120), col("deptName", "所属部门", 120), col("statusName", "状态", 100));
        }
        if (code.startsWith("recordAlterationEdit")) {
            return tableConfig(
                col("recordNo", "记录编号", 150), col("recordName", "记录名称", 240), col("version", "版本", 90),
                col("docName", "所属文件", 220), col("statusName", "状态", 100));
        }
        if (code.startsWith("bookPrint")) {
            return tableConfig(
                col("applyNo", "申请单号", 160), col("applyName", "成册名称", 240), col("applicant", "申请人", 100),
                col("applyDate", "申请日期", 120), col("statusName", "状态", 100));
        }
        if (code.startsWith("printApply")) {
            return tableConfig(
                col("applyNo", "申请单号", 160), col("docNo", "文件编号", 150), col("docName", "文件名称", 220),
                col("applicant", "申请人", 100), col("applyDate", "申请日期", 120), col("statusName", "状态", 100));
        }
        if (code.startsWith("numberApplication")) {
            return tableConfig(
                col("applyNo", "申请单号", 160), col("applicant", "申请人", 100),
                col("applyDate", "申请日期", 120), col("statusName", "状态", 100));
        }
        return EMPTY_CONFIG;
    }

    private Map<String, Object> tableConfig(Map<String, Object>... columns) {
        List<Map<String, Object>> attrs = new ArrayList<>();
        attrs.add(attr("index", "序号", "index", 55, 1));
        int sort = 2;
        for (Map<String, Object> column : columns) {
            column.put("pageConfigAttributeSort", sort++);
            column.put("isEnable", "1");
            attrs.add(column);
        }
        attrs.add(attr("operation", "操作", "operation", 160, 99));
        attrs.get(attrs.size() - 1).put("slotName", "operation");
        Map<String, Object> config = new HashMap<>();
        config.put("pageConfigAlign", "center");
        config.put("pageConfigSize", "small");
        config.put("attributeList", attrs);
        return config;
    }

    private Map<String, Object> col(String prop, String label, int width) {
        Map<String, Object> attr = new HashMap<>();
        attr.put("prop", prop);
        attr.put("label", label);
        attr.put("fieldType", "input");
        attr.put("width", width);
        return attr;
    }

    private Map<String, Object> attr(String prop, String label, String fieldType, int width, int sort) {
        Map<String, Object> attr = new HashMap<>();
        attr.put("prop", prop);
        attr.put("label", label);
        attr.put("fieldType", fieldType);
        attr.put("width", width);
        attr.put("pageConfigAttributeSort", sort);
        attr.put("isEnable", "1");
        return attr;
    }

    @GetMapping("/list")
    public TableDataInfo<Object> list() {
        List<Object> list = new ArrayList<>();
        configs.keySet().forEach(code -> list.add(Map.of("pageConfigId", code, "pageCode", code)));
        return TableDataInfo.build(list);
    }

    @GetMapping("/{id}")
    public R<Object> detail(@PathVariable("id") String id) {
        return R.ok(configs.getOrDefault(id, EMPTY_CONFIG));
    }
}
