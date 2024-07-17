package com.woshimax.bi_service.totest;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;

public class ExcelGen {
    public static void main(String[] args) {
        ExcelGen excelGen = new ExcelGen();
        excelGen.genExcel();
    }
    public void genExcel(){
        // 创建工作簿
        Workbook workbook = new XSSFWorkbook();

        // 创建工作表
        Sheet sheet = workbook.createSheet("Data");

        // 创建表头
        Row headerRow = sheet.createRow(0);
        Cell headerCell1 = headerRow.createCell(0);
        headerCell1.setCellValue("天数");
        Cell headerCell2 = headerRow.createCell(1);
        headerCell2.setCellValue("用户");

        // 填充数据
        Random random = new Random();
        int baseConsumers = 50;
        int maxIncrease = 10;

        for (int day = 1; day <= 365; day++) {
            Row row = sheet.createRow(day);
            Cell cell1 = row.createCell(0);
            cell1.setCellValue(day);

            Cell cell2 = row.createCell(1);
            int consumers = baseConsumers + random.nextInt(maxIncrease);
            baseConsumers += random.nextInt(5);  // 随机增加消费人数
            cell2.setCellValue(consumers);
        }

        // 调整列宽
        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);

        // 写入Excel文件
        try (FileOutputStream fileOut = new FileOutputStream("data1.xlsx")) {
            workbook.write(fileOut);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 关闭工作簿
        try {
            workbook.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Excel文件生成成功！");
    }
}
