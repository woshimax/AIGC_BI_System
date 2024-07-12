package com.yupi.springbootinit.utils;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.support.ExcelTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Excel工具类——使用相关库：easyexcel
 */

@Slf4j
public class ExcelUtils {
    public static String excelToCsv(MultipartFile multipartFile){

        /*File file = null;
        try {
            file = ResourceUtils.getFile("classpath:test_excel.xlsx");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }*/
        //读入list
        List<Map<Integer, String>> list = null;
        try {
            list = EasyExcel.read(multipartFile.getInputStream())
                    .excelType(ExcelTypeEnum.XLSX)
                    .sheet()
                    .headRowNumber(0)
                    .doReadSync();
        } catch (IOException e) {
            log.info("读取文件异常");
            e.printStackTrace();
        }

        //读入数据转为csv，注意先校验！！！
        if(CollUtil.isEmpty(list)){
            return"";
        }
        StringBuilder stringBuilder = new StringBuilder();
        //用LinkedHashMap能保证有序读取
        //get（0）第0个元素是表头（第一行）
        LinkedHashMap<Integer, String>  headerMap= (LinkedHashMap) list.get(0);
        List<String> headerList = headerMap.values().stream().filter(ObjectUtils::isNotEmpty).collect(Collectors.toList());

        stringBuilder.append(StringUtils.join(headerList,",")).append("\n");
        //读取数据
        //get（i）读取后面一行行的元素
        for(int i = 1;i < list.size();i++){
            LinkedHashMap<Integer,String> dataMap = (LinkedHashMap) list.get(i);
            List<String> dataList = dataMap.values().stream().filter(ObjectUtils::isNotEmpty).collect(Collectors.toList());
            stringBuilder.append(StringUtils.join(dataList,",")).append("\n");
        }


        return stringBuilder.toString();
    }

    public static void main(String[] args) {
        ExcelUtils excelUtils = new ExcelUtils();
        excelUtils.excelToCsv(null);
    }
}
