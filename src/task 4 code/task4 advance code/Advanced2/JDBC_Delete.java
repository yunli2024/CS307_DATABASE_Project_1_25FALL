package task4_advance.Advanced2;

import java.sql.*;

public class JDBC_Delete {

    public static void main(String[] args) {
        String mysqlUrl = "jdbc:mysql://localhost:3306/task4_advance2";
        String mysqlUser = "root";
        String mysqlPassword = "WT060519";
        String postgresUrl = "jdbc:postgresql://localhost:5432/task4advance2";
        String postgresUser = "postgres";
        String postgresPassword = "WT060519";
        // MySQL 和 PostgreSQL 的删除查询语句
        String deleteQuery1 = "DELETE FROM users WHERE Age > 40";  // MySQL
        String deleteQuery2 = "DELETE FROM task4advance2.users WHERE Age > 40";  // PostgreSQL
        // 实验次数
        for (int i = 0; i < 5; i++) {
            System.out.println("Experiment " + (i+1));
            // MySQL
            System.out.println("正在执行 MySQL 删除操作...");
            long mysqlStartTime = System.nanoTime();
            int mysqlDeletedCount = executeDeleteWithRollback(mysqlUrl, mysqlUser, mysqlPassword, deleteQuery1, "users");
            long mysqlEndTime = System.nanoTime();
            long mysqlDeleteTime = (mysqlEndTime - mysqlStartTime) / 1_000_000; // 毫秒
            System.out.println("MySQL 删除操作耗时: " + mysqlDeleteTime + " ms, 删除的记录数量: " + mysqlDeletedCount);
            // PostgreSQL
            System.out.println("正在执行 PostgreSQL 删除操作...");
            long postgresStartTime = System.nanoTime();
            int postgresDeletedCount = executeDeleteWithRollback(postgresUrl, postgresUser, postgresPassword, deleteQuery2, "task4advance2.users");
            long postgresEndTime = System.nanoTime();
            long postgresDeleteTime = (postgresEndTime - postgresStartTime) / 1_000_000; // 毫秒
            System.out.println("PostgreSQL 删除操作耗时: " + postgresDeleteTime + " ms, 删除的记录数量: " + postgresDeletedCount);
        }
    }
    private static int executeDeleteWithRollback(String dbUrl, String dbUser, String dbPassword, String query, String tableName) {
        int deletedCount = 0;
        try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
             Statement stmt = conn.createStatement()) {
            // 启动事务
            conn.setAutoCommit(false);
            // 根据数据库类型（表名）查询删除前的记录数
            String countQuery = "SELECT COUNT(*) FROM " + tableName + " WHERE Age > 40";
            ResultSet rs = stmt.executeQuery(countQuery);
            if (rs.next()) {
                deletedCount = rs.getInt(1);
            }
            // 执行删除操作
            int rowsAffected = stmt.executeUpdate(query);
            System.out.println("删除的记录数: " + rowsAffected);
            // 回滚事务
            conn.rollback();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return deletedCount;
    }
}