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

import java.util.List;
import java.util.Map;

/**
 * EQMS（设备管理）演示接口。
 *
 * <p>前端已经预留设备管理菜单，但业务接口同样不在开源后端中，这里统一返回空列表，
 * 避免页面出现 404 / “访问资源不存在”。</p>
 */
@RestController
@RequestMapping("/eqms")
public class MockEqmsController {

    @GetMapping("/**")
    public Object get(HttpServletRequest request) {
        String path = request.getRequestURI().toLowerCase();
        if (path.contains("list")) {
            return TableDataInfo.build(List.of());
        }
        return R.ok(Map.of());
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
}
