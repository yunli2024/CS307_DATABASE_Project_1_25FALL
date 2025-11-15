package task4_advance.Advanced1;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;

public class High_currency_File {
    static final String FILE_PATH = "C:\\Users\\WIN11\\OneDrive\\Desktop\\CS307\\CS307_project\\useful\\data_origin\\recipes.csv";  // 请确保不在 OneDrive 目录里
    static final int THREAD_COUNT = 100;
    static final int OPS_PER_THREAD = 5000;
    static List<String> fileLines;  // 预加载文件内容，提高稳定性

    public static void main(String[] args) throws Exception {
        System.out.println("线程数: " + THREAD_COUNT);
        System.out.println("每线程查询次数: " + OPS_PER_THREAD);
        // 检查文件
        File f = new File(FILE_PATH);
        if (!f.exists()) {
            System.out.println("文件不存在: " + FILE_PATH);
            return;
        }
        //Step 1：预读取整个文件到内存
        long loadStart = System.nanoTime();
        fileLines = Files.readAllLines(Paths.get(FILE_PATH));
        long loadEnd = System.nanoTime();
        System.out.println("文件加载完成，行数: " + fileLines.size()
                + ", 耗时: " + (loadEnd - loadStart) / 1_000_000 + " ms");
        //创建线程池并发执行实验
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
        List<Future<Long>> results = new ArrayList<>();
        long totalStart = System.nanoTime();
        for (int i = 0; i < THREAD_COUNT; i++) {
            results.add(executor.submit(new FileQueryWorker(i)));
        }
        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.HOURS);
        long totalEnd = System.nanoTime();
        long totalTimeMs = (totalEnd - totalStart) / 1_000_000;
        // 汇总线程执行时间
        long totalThreadTime = 0;
        for (Future<Long> fTime : results) {
            totalThreadTime += fTime.get();
        }
        long avgTimeMs = (totalThreadTime / THREAD_COUNT) / 1_000_000;
        long totalOps = (long) THREAD_COUNT * OPS_PER_THREAD;
        long qps = totalOps * 1000 / totalTimeMs;
        System.out.println("File I/O 总耗时: " + totalTimeMs + " ms");
        System.out.println("平均每线程耗时: " + avgTimeMs + " ms");
        System.out.println("总查询次数: " + totalOps);
        System.out.println("QPS（查询/秒）: " + qps);
    }
    // 工作线程
    static class FileQueryWorker implements Callable<Long> {
        int id;
        Random rand = new Random();
        FileQueryWorker(int id) {
            this.id = id;
        }
        @Override
        public Long call() {
            long start = System.nanoTime();
            for (int i = 0; i < OPS_PER_THREAD; i++) {
                // 随机 ID，模拟查找
                int target = rand.nextInt(100000) + 1;
                String prefix = target + ",";
                // 扫描试图找到行
                for (String line : fileLines) {
                    if (line.startsWith(prefix)) break;
                }
            }
            long end = System.nanoTime();
            System.out.println("线程 " + id + " 完成，用时 " + (end - start) / 1_000_000 + " ms");
            return end - start;
        }
    }
}