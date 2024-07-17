package com.woshimax.bi_service.totest;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Component
public class CsvToExcel {
    public void doCsv2Excel(String csvData) throws IOException {
        String excelFilePath = "output.xlsx"; // 输出的Excel文件路径

        List<String> lines = Arrays.asList(csvData.split("\n"));

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("消费数据");

        int rowNum = 0;
        for (String line : lines) {
            String[] values = line.split(",");
            Row row = sheet.createRow(rowNum++);
            int cellNum = 0;
            for (String value : values) {
                Cell cell = row.createCell(cellNum++);
                cell.setCellValue(value);
            }
        }

        try (FileOutputStream outputStream = new FileOutputStream(excelFilePath)) {
            workbook.write(outputStream);
            workbook.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    @Bean
    public CsvToExcel csv2excel(){
        return new CsvToExcel();
    }
}