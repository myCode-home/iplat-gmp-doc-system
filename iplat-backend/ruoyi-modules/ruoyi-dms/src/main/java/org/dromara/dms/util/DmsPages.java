package org.dromara.dms.util;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;

import java.util.List;

/**
 * 分页查询工具
 */
public class DmsPages {

    /**
     * 按条件分页查询
     */
    public static <T> TableDataInfo<T> page(BaseMapper<T> mapper, PageQuery pageQuery, QueryWrapper<T> wrapper) {
        return TableDataInfo.build(mapper.selectPage(pageQuery.build(), wrapper));
    }

    /**
     * 按 id 集合分页查询
     */
    public static <T> TableDataInfo<T> pageByIds(BaseMapper<T> mapper, PageQuery pageQuery, List<Long> ids) {
        QueryWrapper<T> qw = new QueryWrapper<>();
        qw.in(!ids.isEmpty(), "id", ids);
        qw.orderByDesc("create_time");
        return page(mapper, pageQuery, qw);
    }

    private DmsPages() {
    }
}
