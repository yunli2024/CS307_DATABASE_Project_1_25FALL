package task4;


import java.io.*;
import java.nio.file.*;

public class FileIOPerformanceTest {

    public static void main(String[] args) {
        // CSV 文件的路径
        String inputFilePath = "C:\\Users\\WIN11\\Downloads\\data (2)\\final_data\\user.csv";  // 输入文件路径
        String outputFilePath = "C:\\Users\\WIN11\\Downloads\\data (2)\\final_data\\user.csv1"; // 输出文件路径

        // 测量读取时间
        long startTime = System.nanoTime();
        readAndWriteFile(inputFilePath, outputFilePath);
        long endTime = System.nanoTime();

        // 计算时间差
        long duration = endTime - startTime;
        System.out.println("File I/O (read and write) took " + duration + " nanoseconds");
        System.out.println("Which is approximately " + duration / 1_000_000 + " milliseconds");
    }

    private static void readAndWriteFile(String inputFilePath, String outputFilePath) {
        BufferedReader reader = null;
        BufferedWriter writer = null;

        try {
            // 创建读取文件的 BufferedReader
            reader = new BufferedReader(new FileReader(inputFilePath));
            // 创建写入文件的 BufferedWriter
            writer = new BufferedWriter(new FileWriter(outputFilePath));

            String line;
            // 按行读取文件，并写入到新的文件中
            while ((line = reader.readLine()) != null) {
                writer.write(line);
                writer.newLine();  // 写入换行符
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // 关闭资源
            try {
                if (reader != null) {
                    reader.close();
                }
                if (writer != null) {
                    writer.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}


