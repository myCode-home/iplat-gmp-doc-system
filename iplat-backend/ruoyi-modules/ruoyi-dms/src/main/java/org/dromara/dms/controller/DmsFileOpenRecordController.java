package org.dromara.dms.controller;

import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.dms.domain.DmsFileOpenRecord;
import org.dromara.dms.mapper.DmsFileOpenRecordMapper;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.Map;

/**
 * PageOffice 打开记录（本地环境无 PageOffice，恒放行提交）
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/dms/fileOpenRecord")
public class DmsFileOpenRecordController {

    private final DmsFileOpenRecordMapper baseMapper;

    @PostMapping("/check/docCreate")
    public R<Boolean> check(@RequestBody Map<String, Object> body) {
        DmsFileOpenRecord record = new DmsFileOpenRecord();
        record.setFileBusinessId(body.get("fileBusinessId") == null ? null : String.valueOf(body.get("fileBusinessId")));
        record.setFileBusinessType(body.get("fileBusinessType") == null ? "docCreate" : String.valueOf(body.get("fileBusinessType")));
        record.setFileBusinessName(body.get("fileBusinessName") == null ? null : String.valueOf(body.get("fileBusinessName")));
        record.setOpenUserName(LoginHelper.getUsername());
        record.setOpenTime(new Date());
        baseMapper.insert(record);
        return R.ok(true);
    }
}
