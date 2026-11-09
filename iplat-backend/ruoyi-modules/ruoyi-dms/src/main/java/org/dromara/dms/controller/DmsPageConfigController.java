package org.dromara.dms.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.domain.R;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.dms.domain.SysPageConfig;
import org.dromara.dms.domain.SysPageConfigAttribute;
import org.dromara.dms.domain.SysPageConfigRule;
import org.dromara.dms.mapper.SysPageConfigAttributeMapper;
import org.dromara.dms.mapper.SysPageConfigMapper;
import org.dromara.dms.mapper.SysPageConfigRuleMapper;
import org.dromara.dms.util.DmsPages;
import org.springframework.web.bind.annotation.*;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 页面/表格动态配置（SuperTable / SuperSearch / SuperForm 数据源）
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/system/page-config")
public class DmsPageConfigController {

    private final SysPageConfigMapper configMapper;
    private final SysPageConfigAttributeMapper attributeMapper;
    private final SysPageConfigRuleMapper ruleMapper;
    private final ObjectMapper objectMapper;

    /** 前端核心接口：按页面编码取配置 */
    @GetMapping("/page/{code}")
    public R<Map<String, Object>> page(@PathVariable String code) {
        SysPageConfig config = configMapper.selectOne(new QueryWrapper<SysPageConfig>().lambda()
            .eq(SysPageConfig::getPageCode, code).last("limit 1"));
        if (config == null) {
            return R.ok(emptyConfig());
        }
        return R.ok(assemble(config));
    }

    @GetMapping("/list")
    public TableDataInfo<SysPageConfig> list(SysPageConfig query, PageQuery pageQuery) {
        QueryWrapper<SysPageConfig> qw = new QueryWrapper<>();
        qw.like(query.getPageCode() != null && !query.getPageCode().isBlank(), "page_code", query.getPageCode());
        qw.orderByDesc("create_time");
        return DmsPages.page(configMapper, pageQuery, qw);
    }

    @GetMapping("/{id}")
    public R<Map<String, Object>> detail(@PathVariable Long id) {
        SysPageConfig config = configMapper.selectById(id);
        if (config == null) {
            return R.ok(emptyConfig());
        }
        return R.ok(assemble(config));
    }

    @PostMapping
    public R<Void> add(@RequestBody Map<String, Object> body) {
        SysPageConfig config = toConfig(body);
        configMapper.insert(config);
        saveAttributes(config, body.get("attributeList"));
        return R.ok();
    }

    @PutMapping
    public R<Void> edit(@RequestBody Map<String, Object> body) {
        SysPageConfig config = toConfig(body);
        configMapper.updateById(config);
        saveAttributes(config, body.get("attributeList"));
        return R.ok();
    }

    @DeleteMapping("/{id}")
    public R<Void> remove(@PathVariable Long id) {
        configMapper.deleteById(id);
        attributeMapper.delete(new QueryWrapper<SysPageConfigAttribute>().lambda()
            .eq(SysPageConfigAttribute::getPageConfigId, id));
        ruleMapper.delete(new QueryWrapper<SysPageConfigRule>().lambda()
            .eq(SysPageConfigRule::getPageConfigId, id));
        return R.ok();
    }

    private Map<String, Object> assemble(SysPageConfig config) {
        Map<String, Object> result = new HashMap<>();
        result.put("pageConfigId", config.getPageConfigId());
        result.put("pageCode", config.getPageCode());
        result.put("pageConfigAlign", config.getPageConfigAlign());
        result.put("pageConfigSize", config.getPageConfigSize());
        result.put("pageConfigDisplay", config.getPageConfigDisplay());
        List<Object> attributeList = new ArrayList<>();
        for (SysPageConfigAttribute attribute : attributeMapper.selectList(new QueryWrapper<SysPageConfigAttribute>().lambda()
            .eq(SysPageConfigAttribute::getPageConfigId, config.getPageConfigId())
            .orderByAsc(SysPageConfigAttribute::getPageConfigAttributeSort))) {
            try {
                attributeList.add(objectMapper.readValue(attribute.getAttributeJson(), new TypeReference<Map<String, Object>>() {
                }));
            } catch (Exception e) {
                log.warn("页面配置属性解析失败: {}", attribute.getId(), e);
            }
        }
        result.put("attributeList", attributeList);
        List<Map<String, Object>> ruleList = new ArrayList<>();
        for (SysPageConfigRule rule : ruleMapper.selectList(new QueryWrapper<SysPageConfigRule>().lambda()
            .eq(SysPageConfigRule::getPageConfigId, config.getPageConfigId())
            .orderByAsc(SysPageConfigRule::getPageConfigRuleSort))) {
            Map<String, Object> ruleMap = new HashMap<>();
            ruleMap.put("pageConfigRuleName", rule.getPageConfigRuleName());
            ruleMap.put("pageConfigRuleEnable", rule.getPageConfigRuleEnable());
            ruleMap.put("pageConfigRuleCondition", rule.getPageConfigRuleCondition());
            ruleMap.put("pageConfigRuleAction", rule.getPageConfigRuleAction());
            ruleMap.put("pageConfigRuleSort", rule.getPageConfigRuleSort());
            ruleList.add(ruleMap);
        }
        result.put("ruleList", ruleList);
        return result;
    }

    private Map<String, Object> emptyConfig() {
        Map<String, Object> result = new HashMap<>();
        result.put("pageConfigAlign", "center");
        result.put("pageConfigSize", "small");
        result.put("attributeList", List.of());
        result.put("ruleList", List.of());
        return result;
    }

    private SysPageConfig toConfig(Map<String, Object> body) {
        SysPageConfig config = new SysPageConfig();
        if (body.get("pageConfigId") != null) {
            config.setPageConfigId(Long.valueOf(String.valueOf(body.get("pageConfigId"))));
        }
        config.setPageCode(str(body.get("pageCode")));
        config.setPageConfigName(str(body.get("pageConfigName")));
        config.setPageConfigAlign(str(body.get("pageConfigAlign")));
        config.setPageConfigSize(str(body.get("pageConfigSize")));
        config.setPageConfigDisplay(str(body.get("pageConfigDisplay")));
        config.setRemark(str(body.get("remark")));
        return config;
    }

    @SuppressWarnings("unchecked")
    private void saveAttributes(SysPageConfig config, Object attributeList) {
        attributeMapper.delete(new QueryWrapper<SysPageConfigAttribute>().lambda()
            .eq(SysPageConfigAttribute::getPageConfigId, config.getPageConfigId()));
        ruleMapper.delete(new QueryWrapper<SysPageConfigRule>().lambda()
            .eq(SysPageConfigRule::getPageConfigId, config.getPageConfigId()));
        if (!(attributeList instanceof List<?> list)) {
            return;
        }
        for (Object item : list) {
            if (!(item instanceof Map<?, ?> map)) {
                continue;
            }
            try {
                SysPageConfigAttribute attribute = new SysPageConfigAttribute();
                attribute.setPageConfigId(config.getPageConfigId());
                attribute.setPageCode(config.getPageCode());
                attribute.setAttributeJson(objectMapper.writeValueAsString(map));
                Object sort = map.get("pageConfigAttributeSort");
                attribute.setPageConfigAttributeSort(sort == null ? null : Integer.valueOf(String.valueOf(sort)));
                attributeMapper.insert(attribute);
            } catch (Exception e) {
                log.warn("页面配置属性保存失败", e);
            }
        }
    }

    private String str(Object value) {
        return value == null ? null : String.valueOf(value);
    }
}
