package com.woshimax.bi_service.totest;

import com.woshimax.bi_service.model.entity.Chart;
import com.woshimax.bi_service.service.ChartService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest
class GenTestingDataTest {
    @Resource
    private ChartService chartService;
    @Resource
    private GenTestingData testData;
    @Test
    void doTest() {
        //todo 记得把mybatisplus映射表改回来，还有chart类的id字段注释
        for(int i = 0;i < 600000;i++){
            Chart chart = testData.doTest();
            chartService.save(chart);
        }
    }
}