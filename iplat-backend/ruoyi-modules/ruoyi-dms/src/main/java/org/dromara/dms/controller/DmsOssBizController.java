package org.dromara.dms.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.dms.domain.DmsOssBiz;
import org.dromara.dms.mapper.DmsOssBizMapper;
import org.dromara.dms.util.SimplePdfWriter;
import org.dromara.system.domain.vo.SysOssVo;
import org.dromara.system.service.ISysOssService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * 业务附件关联（基于官方 sys_oss 存储 + dms_oss_biz 关联表）
 */
@RequiredArgsConstructor
@RestController
public class DmsOssBizController {

    private final DmsOssBizMapper bizMapper;
    private final ISysOssService ossService;

    /** 上传并关联业务（data: {fileName,url,ossId}） */
    @PostMapping("/resource/oss/upload/relate")
    public R<Map<String, Object>> uploadRelate(@RequestParam("file") MultipartFile file,
                                               @RequestParam(required = false) String bizId,
                                               @RequestParam(required = false) String bizType,
                                               @RequestParam(required = false) String bizSecondType,
                                               @RequestParam(required = false) String covertType) {
        SysOssVo oss = ossService.upload(file);
        DmsOssBiz biz = new DmsOssBiz();
        biz.setBizId(bizId);
        biz.setBizType(bizType);
        biz.setBizSecondType(bizSecondType);
        biz.setOssId(oss.getOssId());
        biz.setCovertType(covertType);
        bizMapper.insert(biz);
        return R.ok(Map.of("fileName", oss.getFileName(), "url", oss.getUrl() == null ? "" : oss.getUrl(),
            "ossId", String.valueOf(oss.getOssId())));
    }

    /** 业务附件列表（data 数组） */
    @GetMapping("/resource/oss/list/{bizId}/{bizType}")
    public R<List<Map<String, Object>>> listByBiz(@PathVariable String bizId, @PathVariable String bizType) {
        List<Map<String, Object>> data = new ArrayList<>();
        for (DmsOssBiz biz : selectBiz(bizId, bizType)) {
            SysOssVo oss = ossService.getById(biz.getOssId());
            if (oss == null) {
                continue;
            }
            data.add(Map.of("ossId", String.valueOf(oss.getOssId()),
                "url", oss.getUrl() == null ? "" : oss.getUrl(),
                "originalName", nullSafe(oss.getOriginalName()),
                "fileSuffix", nullSafe(oss.getFileSuffix()),
                "fileName", nullSafe(oss.getFileName()),
                "service", nullSafe(oss.getService()),
                "quoted", false));
        }
        return R.ok(data);
    }

    /** 保存关联关系 */
    @PostMapping("/resource/oss/biz")
    public R<Void> saveBiz(@RequestBody Map<String, Object> body) {
        String bizId = str(body.get("bizId"));
        String bizType = str(body.get("bizType"));
        Object ossIds = body.get("ossIds");
        if (ossIds instanceof List<?> list) {
            for (Object item : list) {
                insertBiz(bizId, bizType, Long.valueOf(String.valueOf(item)), str(body.get("covertType")));
            }
        } else if (ossIds != null) {
            for (String part : String.valueOf(ossIds).split(",")) {
                if (!part.isBlank()) {
                    insertBiz(bizId, bizType, Long.valueOf(part.trim()), str(body.get("covertType")));
                }
            }
        }
        return R.ok();
    }

    /** 删除关联关系 */
    @DeleteMapping("/resource/oss/biz/{bizId}/{bizType}")
    public R<Void> deleteBiz(@PathVariable String bizId, @PathVariable String bizType) {
        bizMapper.delete(new QueryWrapper<DmsOssBiz>().lambda()
            .eq(DmsOssBiz::getBizId, bizId).eq(DmsOssBiz::getBizType, bizType));
        return R.ok();
    }

    /** 按业务 id 批量下载（zip，内含占位 PDF） */
    @GetMapping({"/resource/oss/bizs/download", "/dms/oss/bizs/download"})
    public void bizsDownload(@RequestParam String bizIds,
                             @RequestParam(required = false) String bizType,
                             @RequestParam(required = false) String bizSecondType,
                             HttpServletResponse response) throws IOException {
        Set<String> ids = new LinkedHashSet<>();
        for (String part : bizIds.split(",")) {
            if (!part.isBlank()) {
                ids.add(part.trim());
            }
        }
        response.setContentType("application/zip");
        String fileName = URLEncoder.encode("dms-export.zip", StandardCharsets.UTF_8);
        response.setHeader("Content-Disposition", "attachment; filename=" + fileName);
        try (ZipOutputStream zip = new ZipOutputStream(response.getOutputStream())) {
            for (String bizId : ids) {
                byte[] pdf = SimplePdfWriter.write("DMS Export",
                    List.of("BizId: " + bizId, "BizType: " + nullSafe(bizType),
                        "Note: placeholder export generated by local demo backend."));
                zip.putNextEntry(new ZipEntry(bizId + ".pdf"));
                zip.write(pdf);
                zip.closeEntry();
            }
            if (ids.isEmpty()) {
                zip.putNextEntry(new ZipEntry("empty.txt"));
                zip.write("no attachment selected".getBytes(StandardCharsets.UTF_8));
                zip.closeEntry();
            }
        }
    }

    private void insertBiz(String bizId, String bizType, Long ossId, String covertType) {
        DmsOssBiz biz = new DmsOssBiz();
        biz.setBizId(bizId);
        biz.setBizType(bizType);
        biz.setOssId(ossId);
        biz.setCovertType(covertType);
        bizMapper.insert(biz);
    }

    private List<DmsOssBiz> selectBiz(String bizId, String bizType) {
        return bizMapper.selectList(new QueryWrapper<DmsOssBiz>().lambda()
            .eq(DmsOssBiz::getBizId, bizId).eq(bizType != null, DmsOssBiz::getBizType, bizType));
    }

    private String nullSafe(String value) {
        return value == null ? "" : value;
    }

    private String str(Object value) {
        return value == null ? null : String.valueOf(value);
    }
}
