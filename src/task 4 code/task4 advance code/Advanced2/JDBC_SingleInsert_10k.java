package task4_advance.Advanced2;

import java.io.*;
import java.sql.*;

public class JDBC_SingleInsert_10k {
    public static void main(String[] args) {
        //String url = "jdbc:mysql://localhost:3306/task4_advance2";  // 目标 MySQL 数据库
        String url = "jdbc:postgresql://localhost:5432/task4advance2"; //目标postgres数据库，其他地方不用更改
        String user = "postgres";                                 // 用户名
        String password = "WT060519";                         // 密码
        String csvFile = "C:\\Users\\WIN11\\OneDrive\\Desktop\\CS307\\CS307_project\\useful\\data_origin\\user_10k.csv";
        // 3. 单条 INSERT SQL 模板
        // 每次插入 1 行数据（Single Insert）
        String sql = "INSERT INTO task4advance2.users (AuthorId, AuthorName, Gender, Age, Followers, Following) VALUES (?, ?, ?, ?, ?, ?)";
        long startTime = System.nanoTime();
        int totalCount = 0;
        long totalSingleInsertTime = 0;
        try (
                Connection conn = DriverManager.getConnection(url, user, password);    // 建立数据库连接
                PreparedStatement ps = conn.prepareStatement(sql);                     // 预编译 SQL
                BufferedReader reader = new BufferedReader(new FileReader(csvFile))    // 读取 CSV 文件
        ) {
            // 设置为自动提交（Single INSERT 模式）
            conn.setAutoCommit(true);
            String line;
            // 按行读取 CSV 并逐条插入数据库
            while ((line = reader.readLine()) != null) {
                // 解析 CSV 行（已确认文件无引号、严格逗号分隔 → split(",") 安全）
                String[] v = line.split(",");
                long singleStartTime = System.nanoTime();
                ps.setInt(1, Integer.parseInt(v[0])); // AuthorId
                ps.setString(2, v[1]);                // AuthorName
                ps.setString(3, v[2]);                // Gender
                ps.setInt(4, Integer.parseInt(v[3])); // Age
                ps.setInt(5, Integer.parseInt(v[4])); // Followers
                ps.setInt(6, Integer.parseInt(v[5])); // Following
                ps.executeUpdate();
                long singleEndTime = System.nanoTime();
                totalSingleInsertTime += (singleEndTime - singleStartTime);
                totalCount++;  // 统计插入的条数
            }
        } catch (SQLException | IOException e) {
            // 异常处理：数据库错误或文件读取错误
            e.printStackTrace();
        }
        double averageSingleInsertTime = totalCount > 0 ? totalSingleInsertTime / (double) totalCount : 0;
        long endTime = System.nanoTime();
        System.out.println("PostgreSQL experiment 5 Single INSERT 10k 完成，整体耗时: " + (endTime - startTime) / 1e6 + " ms");
        System.out.println("平均单条插入耗时: " + averageSingleInsertTime / 1e6 + " ms");  // 输出平均单条插入耗时
    }
}
