package com.woshimax.bi_service.service;

import com.woshimax.bi_service.model.entity.Chart;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
* @author yuheng
* @description 针对表【chart(图表信息表)】的数据库操作Service
* @createDate 2024-05-24 15:34:49
*/
public interface ChartService extends IService<Chart> {
    public List<Map<String,Object>> getChartData(String tableName);
}
