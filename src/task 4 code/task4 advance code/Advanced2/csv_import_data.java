package task4_advance.Advanced2;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.*;

public class csv_import_data {
    public static void main(String[] args) throws IOException {
        // 输入文件路径
        String inputFile = "C:\\Users\\WIN11\\OneDrive\\Desktop\\CS307\\CS307_project\\useful\\data_origin\\user.csv";
        // 输出文件路径
        String outputFile10k = "C:\\Users\\WIN11\\OneDrive\\Desktop\\CS307\\CS307_project\\useful\\data_origin\\user_10k.csv";
        String outputFile100k = "C:\\Users\\WIN11\\OneDrive\\Desktop\\CS307\\CS307_project\\useful\\data_origin\\user_100k.csv";
        String outputFile300k = "C:\\Users\\WIN11\\OneDrive\\Desktop\\CS307\\CS307_project\\useful\\data_origin\\user_300k.csv";
        // 读取原始 CSV 文件
        List<String> lines = Files.readAllLines(Paths.get(inputFile));
        lines.remove(0);
        // 去除 'FollowerUsers' 和 'FollowingUsers' 列
        List<String> cleanedLines = cleanColumns(lines);
        // 打乱数据顺序
        Collections.shuffle(cleanedLines);  // 这里确保了抽样不重复
        // 随机抽取 10,000 行数据
        List<String> sampled10k = randomSample(cleanedLines, 10000);
        writeToFile(sampled10k, outputFile10k);
        // 随机抽取 100,000 行数据
        List<String> sampled100k = randomSample(cleanedLines, 100000);
        writeToFile(sampled100k, outputFile100k);
        // 随机抽取 300,000 行数据
        List<String> sampled300k = randomSample(cleanedLines,3000000);
        writeToFile(sampled300k, outputFile300k);
    }
    // 去除 'FollowerUsers' 和 'FollowingUsers' 列
    private static List<String> cleanColumns(List<String> lines) {
        List<String> cleanedLines = new ArrayList<>();
        for (String line : lines) {
            // 按逗号分隔数据并去除 'FollowerUsers' 和 'FollowingUsers' 列
            String[] columns = line.split(",");
            // 保留需要的列（AuthorId, AuthorName, Gender, Age, Followers, Following）
            cleanedLines.add(String.join(",", columns[0], columns[1], columns[2], columns[3], columns[4], columns[5]));
        }
        return cleanedLines;
    }
    // 随机抽取 n 行数据
    private static List<String> randomSample(List<String> lines, int n) {
        return lines.stream().limit(n).collect(Collectors.toList());
    }
    // 将数据写入文件
    private static void writeToFile(List<String> data, String fileName) throws IOException {
        BufferedWriter writer = Files.newBufferedWriter(Paths.get(fileName));
        for (String line : data) {
            writer.write(line);
            writer.newLine();
        }
        writer.close();
    }
}

