package com.woshimax.bi_service.model.vo;

import lombok.Data;

/*
    bi返回结果
 */
@Data
public class BiResponse {
    private String genChart;
    private String genResult;
    private Long chartId;
}
