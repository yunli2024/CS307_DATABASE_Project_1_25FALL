package task4_advance.Advanced1;

import java.sql.*;
import java.util.*;
import java.util.concurrent.*;
public class High_currency_DB {
        // ======== 数据库配置 ========
        static final String JDBC_URL = "jdbc:postgresql://localhost:5432/postgres";
        static final String USER = "postgres";
        static final String PASSWORD = "WT060519";

        // ======== 并发参数========
        static final int THREAD_COUNT = 100;       // 并发线程数
        static final int OPS_PER_THREAD = 5000;   // 每线程执行的查询次数
        static final int RANDOM_ID_UPPER = 100000; // 随机 ID 取值范围

        public static void main(String[] args) throws Exception {
            System.out.println(DriverManager.getDrivers());
            System.out.println("开始高并发测试！");
            System.out.println("线程数：" + THREAD_COUNT);
            System.out.println("每线程查询次数：" + OPS_PER_THREAD);
            //创建一个固定大小的线程池 executor，线程池中同时最多有 THREAD_COUNT 个工作线程。
            ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
            //建一个 List 用来保存每个提交任务返回的 Future<Long>
            //Future<Long> 可以在任务异步执行的情况下，在主线程中获取每个线程的执行耗时
            List<Future<Long>> results = new ArrayList<>();//问题一
            long totalStart = System.nanoTime();//记录“全局”开始时间（纳秒）
            // 提交任务
            for (int i = 0; i < THREAD_COUNT; i++) {
                //executor.submit(...) 返回一个 Future<Long>，表示该任务的最终返回值
                results.add(executor.submit(new QueryWorker(i)));
            }
            executor.shutdown();//告诉线程池不再接受新任务，但会继续执行已提交的任务
            //主线程阻塞等待，最多等待 1 小时，1小时内子线程完成，返回true，主线程是main语句下的，子线程为实际查询
            executor.awaitTermination(1, TimeUnit.HOURS);
            long totalEnd = System.nanoTime();//记录全局结束时间
            long totalTimeMs = (totalEnd - totalStart) / 1_000_000;//记录耗时，纳秒转化为毫秒
            // 统计每个线程执行时间
            long sum = 0;//统计每个子线程耗时之和
            for (Future<Long> f : results) {
                sum += f.get();
            }
            long avgTimeMs = sum / THREAD_COUNT / 1_000_000;//每个子线程花费平均时间，毫秒
            // 计算QPS
            long totalOps = (long) THREAD_COUNT * OPS_PER_THREAD;//总查询数
            long qps = totalOps * 1000 / totalTimeMs;//每秒查询数
            System.out.println("=======================================");
            System.out.println("总耗时:              " + totalTimeMs + " ms");
            System.out.println("平均每线程耗时:      " + avgTimeMs + " ms");
            System.out.println("总查询次数:          " + totalOps);
            System.out.println("QPS (查询/秒):        " + qps);
            System.out.println("=======================================");
        }

        // ================================
        // 多线程任务（执行数据库查询）
        // ================================
        static class QueryWorker implements Callable<Long> {
            //Callable<Long> 是一个可以在多线程中执行、并且能返回 Long 结果的任务接口
            private int threadId;//存储每个线程的编号，用于打印日志
            QueryWorker(int threadId) {
                this.threadId = threadId;
            }
            @Override
            public Long call() throws Exception {//线程池执行 submit() 时，就会自动调用 call()
                long start = System.nanoTime();
                Random rand = new Random();
                //线程内部创建数据库连接
                //每个线程独立使用一个 Connection，避免多线程争抢同一连接
                try (Connection conn = DriverManager.getConnection(JDBC_URL, USER, PASSWORD)) {
                    PreparedStatement stmt = conn.prepareStatement(//预编译 SQL 查询，只需编译一次，多次执行
                            "SELECT recipe_id, recipe_name FROM recipes WHERE recipe_id = ?");
                    //每个线程重复执行 OPS_PER_THREAD 次查询
                    for (int i = 0; i < OPS_PER_THREAD; i++) {
                        int id = 1 + rand.nextInt(RANDOM_ID_UPPER);//生成一个随机 RecipeId
                        stmt.setInt(1, id);
                        ResultSet rs = stmt.executeQuery();
                        // 不打印，避免影响性能
                        while (rs.next()) {
                            rs.getInt("recipe_id");
                        }
                    }
                } catch (Exception e) {//捕获线程内部错误（数据库失败、网络错误等）
                    System.out.println("线程 " + threadId + " 出错：");
                    e.printStackTrace();
                }
                long end = System.nanoTime();
                long cost = end - start;
                System.out.println("线程 " + threadId + " 完成，用时 " + cost / 1_000_000 + " ms");
                return cost;
            }
        }
    }

