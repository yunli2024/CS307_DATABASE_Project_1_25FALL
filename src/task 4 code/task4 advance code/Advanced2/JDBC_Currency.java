package task4_advance.Advanced2;

import java.sql.*;
import java.util.concurrent.*;
import java.util.Random;

public class JDBC_Currency {

    private static final String MYSQL_URL = "jdbc:mysql://localhost:3306/task4_advance2";
    private static final String MYSQL_USER = "root";
    private static final String MYSQL_PASSWORD = "WT060519";
    private static final String POSTGRES_URL = "jdbc:postgresql://localhost:5432/task4advance2";
    private static final String POSTGRES_USER = "postgres";
    private static final String POSTGRES_PASSWORD = "WT060519";

    private static final int QUERY_COUNT = 10000;  // 每个线程查询的次数
    private static final int[] THREAD_COUNTS = {5, 20, 100};  // 线程数

    public static void main(String[] args) {
        for (int i = 0; i < 5; i++) { // 重复实验5次
            try {
                // 每次实验之间间隔 3-5 秒
                Thread.sleep((long) (Math.random() * 2000 + 3000));

                // 分别在 MySQL 和 PostgreSQL 上进行实验
                runExperiment(MYSQL_URL, MYSQL_USER, MYSQL_PASSWORD, THREAD_COUNTS, "MySQL");
                runExperiment(POSTGRES_URL, POSTGRES_USER, POSTGRES_PASSWORD, THREAD_COUNTS, "PostgreSQL");

                // 每个实验完成后，适当等待系统进行回收
                Thread.sleep((long) (Math.random() * 2000 + 3000));  // 每个实验间隔 3-5 秒
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private static void runExperiment(String dbUrl, String dbUser, String dbPassword, int[] threadCounts, String dbType) {
        for (int threadCount : threadCounts) {
            System.out.println("\nRunning experiment with " + threadCount + " threads on " + dbType);

            // 创建线程池
            ExecutorService executor = Executors.newFixedThreadPool(threadCount);
            long startTime = System.nanoTime();  // 记录开始时间

            // 用来记录每个线程的总查询时间
            long[] threadQueryTimes = new long[threadCount];

            // 启动所有线程进行并发查询，线程几乎同时开始
            for (int i = 0; i < threadCount; i++) {
                int threadId = i;  // 保存线程的 ID
                executor.submit(() -> {
                    long threadStartTime = System.nanoTime();  // 记录线程开始时间
                    try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword)) {
                        String query;
                        // 根据数据库类型决定查询语句
                        if (dbType.equals("MySQL")) {
                            query = "SELECT * FROM users WHERE Age BETWEEN 20 AND 40";  // MySQL 查询
                        } else {
                            query = "SELECT * FROM task4advance2.users WHERE Age BETWEEN 20 AND 40";  // PostgreSQL 查询
                        }

                        // 模拟查询 10,000 次
                        Random random = new Random();
                        for (int j = 0; j < QUERY_COUNT; j++) {
                            int randomId = random.nextInt(300000); // 假设有 300k 用户
                            try (Statement stmt = conn.createStatement();
                                 ResultSet rs = stmt.executeQuery(query + " AND AuthorId = " + randomId)) {
                                while (rs.next()) {
                                    // 模拟处理每条查询结果
                                }
                            }
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                    long threadEndTime = System.nanoTime();  // 记录线程结束时间
                    long threadDuration = threadEndTime - threadStartTime;  // 线程执行总时间
                    threadQueryTimes[threadId] = threadDuration;
                });
            }
            executor.shutdown();
            try {
                if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
            }

            // 计算总时间
            long endTime = System.nanoTime();
            long totalDuration = (endTime - startTime) / 1000000;  // 转换为毫秒

            // 计算每个线程的总查询时间
            long totalQueryTime = 0;
            for (int i = 0; i < threadCount; i++) {
                totalQueryTime += threadQueryTimes[i];  // 累加每个线程的时间
            }

            // 计算平均查询时间（每个线程的查询平均时间）
            long totalQueries = threadCount * QUERY_COUNT;  // 总查询数
            long avgQueryTimeMillis = totalQueryTime / totalQueries / 1000000;  // 平均查询时间（单位：毫秒）

            // 计算 QPS（每秒查询数）
            double qps = (totalQueries / (totalDuration / 1000.0)); // 计算 QPS

            // 输出结果：总时间、平均查询时间（毫秒）、QPS（查询每秒）
            System.out.println(dbType + " - " + threadCount + " threads completed.");
            System.out.println("Total time: " + totalDuration + " ms");
            System.out.println("Average query time per thread: " + avgQueryTimeMillis + " ms");
            System.out.println("QPS: " + qps + " queries per second");
        }
    }
}
