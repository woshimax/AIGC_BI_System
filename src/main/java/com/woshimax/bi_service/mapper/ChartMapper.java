package com.woshimax.bi_service.mapper;

import com.woshimax.bi_service.model.entity.Chart;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
* @author yuheng
* @description 针对表【chart(图表信息表)】的数据库操作Mapper
* @createDate 2024-05-24 15:34:49
* @Entity com.woshimax.bi_service.model.entity.Chart
*/
@Mapper
public interface ChartMapper extends BaseMapper<Chart> {
    List<Map<String, Object>> selectFromDynamicTable(@Param("tableName") String tableName);
}






