package task4_advance.Advanced2;

import java.sql.*;

public class JBDC_QueryAge {
    public static void main(String[] args) {
        System.out.println("experiment 5");
        String mysqlUrl = "jdbc:mysql://localhost:3306/task4_advance2";
        String mysqlUser = "root";
        String mysqlPassword = "WT060519";
        String postgresUrl = "jdbc:postgresql://localhost:5432/task4advance2";
        String postgresUser = "postgres";
        String postgresPassword = "WT060519";
        String query1 = "SELECT COUNT(*) FROM users WHERE Age BETWEEN 20 AND 40";
        String query2 = "SELECT COUNT(*) FROM task4advance2.users WHERE Age BETWEEN 20 AND 40";
        //MySQL
        System.out.println("正在执行 MySQL 查询");
        long mysqlStartTime = System.nanoTime();
        int mysqlUserCount = executeQuery(mysqlUrl, mysqlUser, mysqlPassword, query1);
        long mysqlEndTime = System.nanoTime();
        long mysqlQueryTime = (mysqlEndTime - mysqlStartTime) / 1_000_000; // 毫秒
        System.out.println("MySQL 查询耗时: " + mysqlQueryTime + " ms, 符合条件的用户数量: " + mysqlUserCount);
        // PostgreSQL
        System.out.println("正在执行 PostgreSQL 查询...");
        long postgresStartTime = System.nanoTime();
        int postgresUserCount = executeQuery(postgresUrl, postgresUser, postgresPassword, query2);
        long postgresEndTime = System.nanoTime();
        long postgresQueryTime = (postgresEndTime - postgresStartTime) / 1_000_000; // 毫秒
        System.out.println("PostgreSQL 查询耗时: " + postgresQueryTime + " ms, 符合条件的用户数量: " + postgresUserCount);
    }
    private static int executeQuery(String dbUrl, String dbUser, String dbPassword, String query) {
        int userCount = 0;
        try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            if (rs.next()) {
                userCount = rs.getInt(1); // 获取查询结果中的用户数量
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return userCount;
    }
}