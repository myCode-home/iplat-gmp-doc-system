package org.dromara.dms.handler;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.dms.domain.SysPageConfig;
import org.dromara.dms.domain.SysPageConfigAttribute;
import org.dromara.dms.mapper.SysPageConfigAttributeMapper;
import org.dromara.dms.mapper.SysPageConfigMapper;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * 启动时将内置页面配置（classpath: dms/page-configs.json）导入数据库（仅空表时导入）
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class PageConfigInitializer implements ApplicationRunner {

    private final SysPageConfigMapper configMapper;
    private final SysPageConfigAttributeMapper attributeMapper;
    private final ObjectMapper objectMapper;

    @Override
    public void run(ApplicationArguments args) {
        if (configMapper.selectCount(null) > 0) {
            return;
        }
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("dms/page-configs.json")) {
            if (input == null) {
                log.warn("未找到内置页面配置文件 dms/page-configs.json，跳过初始化");
                return;
            }
            Map<String, Map<String, Object>> data = objectMapper.readValue(input, new TypeReference<>() {
            });
            int count = 0;
            for (Map.Entry<String, Map<String, Object>> entry : data.entrySet()) {
                String code = entry.getKey();
                Map<String, Object> value = entry.getValue();
                SysPageConfig config = new SysPageConfig();
                config.setPageCode(code);
                config.setPageConfigName(code);
                config.setPageConfigAlign(str(value.get("pageConfigAlign"), "center"));
                config.setPageConfigSize(str(value.get("pageConfigSize"), "small"));
                config.setPageConfigDisplay(str(value.get("pageConfigDisplay"), null));
                configMapper.insert(config);
                if (value.get("attributeList") instanceof List<?> attributeList) {
                    for (Object item : attributeList) {
                        if (!(item instanceof Map<?, ?> attributeMap)) {
                            continue;
                        }
                        SysPageConfigAttribute attribute = new SysPageConfigAttribute();
                        attribute.setPageConfigId(config.getPageConfigId());
                        attribute.setPageCode(code);
                        attribute.setAttributeJson(objectMapper.writeValueAsString(attributeMap));
                        Object sort = attributeMap.get("pageConfigAttributeSort");
                        attribute.setPageConfigAttributeSort(sort == null ? null : Integer.valueOf(String.valueOf(sort)));
                        attributeMapper.insert(attribute);
                    }
                }
                count++;
            }
            log.info("内置页面配置初始化完成，共 {} 个", count);
        } catch (Exception e) {
            log.error("内置页面配置初始化失败", e);
        }
    }

    private String str(Object value, String fallback) {
        if (value == null || String.valueOf(value).isBlank()) {
            return fallback;
        }
        return String.valueOf(value);
    }
}
