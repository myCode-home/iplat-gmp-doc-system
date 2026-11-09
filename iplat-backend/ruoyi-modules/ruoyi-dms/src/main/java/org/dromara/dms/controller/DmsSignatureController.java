package org.dromara.dms.controller;

import cn.hutool.core.util.IdUtil;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Set;

/**
 * 电子签名（本地演示：签名定义按 URL 清单返回，签名值为随机令牌）
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/system/signature-definition")
public class DmsSignatureController {

    /** 需要签名的接口（前端已去除 /dms、/eqms 前缀） */
    private static final Set<String> SIGN_URLS = Set.of(
        "/doc-invalid-order",
        "/doc-record-failure-order",
        "/print-doc-info/print-issue",
        "/print-doc-info/print-return",
        "/print-doc-info/print-close",
        "/print-doc-info/print-destroy",
        "/volumes-print-info/issue",
        "/volumes-print-info/receive",
        "/volumes-print-info/return",
        "/volumes-print-info/destroy"
    );

    @PostMapping("/combination")
    public R<Boolean> combination(@RequestBody Map<String, String> body) {
        String url = body.getOrDefault("url", "");
        if (url.startsWith("/dms")) {
            url = url.substring(4);
        }
        if (url.startsWith("/eqms")) {
            url = url.substring(5);
        }
        String finalUrl = url;
        return R.ok(SIGN_URLS.stream().anyMatch(finalUrl::startsWith));
    }

    @PostMapping("/new-signature")
    public R<String> newSignature(@RequestBody Map<String, Object> body) {
        return R.ok(IdUtil.fastSimpleUUID());
    }
}
