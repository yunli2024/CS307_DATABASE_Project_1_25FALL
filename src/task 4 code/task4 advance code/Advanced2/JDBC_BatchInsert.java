package task4_advance.Advanced2;

import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.*;

public class JDBC_BatchInsert {
    public static void main(String[] args) throws Exception {
        //String JDBC_URL = "jdbc:mysql://localhost:3306/task4_advance2?rewriteBatchedStatements=true";
        String JDBC_URL = "jdbc:postgresql://localhost:5432/task4advance2";
        /*MySQL 必须加?rewriteBatchedStatements=true，否则 MySQL 会把 batch 当作多条单独执行的 SQL，性能极差。
        PostgreSQL 不需要任何额外参数，PostgreSQL 的 batch 本身就是 native 批量处理，实现更好*/

        //String USER = "root"; //MySQL
        String USER = "postgres";
        String PASSWORD = "WT060519";
        String CSV_FILE = "C:\\Users\\WIN11\\OneDrive\\Desktop\\CS307\\CS307_project1\\useful\\data_origin\\user_300k.csv";  //
        int batchSize = 1000;               // 每多少条执行一次批处理
        Connection conn = DriverManager.getConnection(JDBC_URL, USER, PASSWORD);
        // 批量插入必须关闭自动提交，提高性能
        conn.setAutoCommit(false);
        String sql = "INSERT INTO task4advance2.users " +
                "(AuthorId, AuthorName, Gender, Age, Followers, Following) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        PreparedStatement ps = conn.prepareStatement(sql);
        BufferedReader reader = new BufferedReader(new FileReader(CSV_FILE));
        String line;
        int count = 0;
        long start = System.nanoTime();  // 开始计时
        // 跳过表头
        reader.readLine();
        while ((line = reader.readLine()) != null) {
            String[] v = line.split(",");
            try {
                ps.setInt(1, Integer.parseInt(v[0]));  // AuthorId
                ps.setString(2, v[1]);  // AuthorName
                ps.setString(3, v[2]);  // Gender
                ps.setInt(4, Integer.parseInt(v[3]));  // Age
                ps.setInt(5, Integer.parseInt(v[4]));  // Followers
                ps.setInt(6, Integer.parseInt(v[5]));  // Following
            } catch (NumberFormatException e) {
                System.err.println("Error parsing line: " + line);
                e.printStackTrace();  // 打印出错误详细信息
                continue;  // 跳过当前行
            }
            ps.addBatch();  // 添加到 batch
            count++;
            if (count % batchSize == 0) {
                ps.executeBatch();
                conn.commit();
            }
        }
        // 插入剩余的数据
        ps.executeBatch();
        conn.commit();
        long end = System.nanoTime();
        //System.out.println("MySQL JDBC_BatchInsert experiment 5 completed, 300k inserted");
        System.out.println("PostgreSQL JDBC_BatchInsert experiment 5 completed, 300k inserted");
        System.out.println("Total time: " + (end - start) / 1e6 + " ms");
        ps.close();
        reader.close();
        conn.close();
    }
}

