package com.woshimax.bi_service.totest;


import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class CSVToListMap {

    public List<Map<String, Object>> csvToListMap(String csvData) throws IOException {
        List<Map<String, Object>> list = new ArrayList<>();
        BufferedReader br = new BufferedReader(new StringReader(csvData));
        String line;
        boolean isFirstLine = true;

        while ((line = br.readLine()) != null) {
            if (isFirstLine) {
                isFirstLine = false;
                continue; // 跳过第一行（标题行）
            }
            String[] values = line.split(",");
            Map<String, Object> map = new HashMap<>();
            map.put("日期", values[0]);
            map.put("人数", values[1]);
            list.add(map);
        }
        br.close();
        return list;
    }

    @Bean
    public CSVToListMap csvToListMap(){
        return new CSVToListMap();
    }
}