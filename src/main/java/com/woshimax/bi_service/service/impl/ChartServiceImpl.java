package com.woshimax.bi_service.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.woshimax.bi_service.service.ChartService;
import com.woshimax.bi_service.model.entity.Chart;
import com.woshimax.bi_service.mapper.ChartMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
* @author yuheng
* @description 针对表【chart(图表信息表)】的数据库操作Service实现
* @createDate 2024-05-24 15:34:49
*/
@Service
public class ChartServiceImpl extends ServiceImpl<ChartMapper, Chart>
    implements ChartService {
    @Resource
    private ChartMapper chartMapper;
    public List<Map<String,Object>> getChartData(String tableName){
        return chartMapper.selectFromDynamicTable(tableName);
    }
}




