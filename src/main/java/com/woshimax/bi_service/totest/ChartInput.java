package com.woshimax.bi_service.totest;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ChartInput{
    // 数据库连接参数
    private static final String URL = "jdbc:mysql://localhost:3306/bi_db?useUnicode=true&characterEncoding=UTF-8&useSSL=false&allowPublicKeyRetrieval=true";
    private static final String USER = "root";
    private static final String PASSWORD = "lyh59439";

    public static void main(String[] args) {
        String excelFilePath = "data1.xlsx";

        try (FileInputStream fileInputStream = new FileInputStream(excelFilePath);
             Workbook workbook = new XSSFWorkbook(fileInputStream)) {

            // 获取第一个工作表
            Sheet sheet = workbook.getSheetAt(0);

            // 获取数据库连接
            Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);

            // 清空表
            clearTable(connection);

            // 遍历行并插入数据
            for (Row row : sheet) {
                // 跳过表头行
                if (row.getRowNum() == 0) {
                    continue;
                }

                int day = (int) row.getCell(0).getNumericCellValue();
                int consumers = (int) row.getCell(1).getNumericCellValue();

                insertData(connection, day, consumers);
            }

            // 关闭数据库连接
            connection.close();

            System.out.println("数据插入成功！");
        } catch (IOException | SQLException e) {
            e.printStackTrace();
        }
    }

    private static void clearTable(Connection connection) throws SQLException {
        String clearSQL = "TRUNCATE TABLE chart_1813621224148865026";
        try (PreparedStatement preparedStatement = connection.prepareStatement(clearSQL)) {
            preparedStatement.executeUpdate();
        }
    }

    private static void insertData(Connection connection, int day, int consumers) throws SQLException {
        String insertSQL = "INSERT INTO chart_1813621224148865026 (日期, 用户数) VALUES (?, ?)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(insertSQL)) {
            preparedStatement.setInt(1, day);
            preparedStatement.setInt(2, consumers);
            preparedStatement.executeUpdate();
        }
    }
}
