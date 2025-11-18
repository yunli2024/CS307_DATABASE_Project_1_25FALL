package task4_advance.Advanced2;

import java.sql.*;

public class JDBC_UpdateAge {
    public static void main(String[] args) {
        //System.out.println("PostgreSQL updateAge experiment5");
        String url = "jdbc:mysql://localhost:3306/task4_advance2";
        //String url = "jdbc:postgresql://localhost:5432/task4advance2";
        String user = "root";
        //String user = "postgres";
        String password = "WT060519";
        String sql = "UPDATE users SET Age = Age -25  WHERE AuthorId = ?";
        //小规模
        long startTime = System.nanoTime();
        long smallUpdateStartTime = System.nanoTime();
        try (
                Connection conn = DriverManager.getConnection(url, user, password);
                PreparedStatement ps = conn.prepareStatement(sql);
        ) {
            conn.setAutoCommit(true);
            System.out.println("开始更新 1k 条记录");
            for (int i = 1; i <= 1000; i++) {
                ps.setInt(1, i);  //  AuthorId 是 1 到 1000
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        long smallUpdateEndTime = System.nanoTime();
        System.out.println("小规模更新（1k条）完成，耗时: " + (smallUpdateEndTime - smallUpdateStartTime) / 1e6 + " ms");
        // 大规模
        long largeUpdateStartTime = System.nanoTime();
        try (
                Connection conn = DriverManager.getConnection(url, user, password);    // 建立数据库连接
                PreparedStatement ps = conn.prepareStatement(sql);                     // 预编译 SQL
        ) {
            conn.setAutoCommit(true);
            System.out.println("开始更新 100k 条记录");
            for (int i = 1001; i <= 100000; i++) {
                ps.setInt(1, i);  // AuthorId 是 1001 到 100000
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        long largeUpdateEndTime = System.nanoTime();
        System.out.println("大规模更新（100k条）完成，耗时: " + (largeUpdateEndTime - largeUpdateStartTime) / 1e6 + " ms");
        // 记录总结束时间并计算整体耗时
    }
}