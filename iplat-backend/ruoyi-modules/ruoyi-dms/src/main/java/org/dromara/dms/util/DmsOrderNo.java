package org.dromara.dms.util;

import org.dromara.common.core.utils.DateUtils;

import java.util.concurrent.ThreadLocalRandom;

/**
 * 单据编号生成
 */
public class DmsOrderNo {

    /**
     * 生成单据编号：前缀 + yyyyMMddHHmmss + 3位随机数
     */
    public static String next(String prefix) {
        return prefix + DateUtils.dateTimeNow()
            + ThreadLocalRandom.current().nextInt(100, 999);
    }

    private DmsOrderNo() {
    }
}
