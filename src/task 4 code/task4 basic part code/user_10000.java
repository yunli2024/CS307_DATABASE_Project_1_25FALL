package task4_basic_version2;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.*;

public class user_10000 {
    public static void main(String[] args) throws IOException {
        // 输入文件路径
        String inputFile = "C:\\Users\\WIN11\\OneDrive\\Desktop\\CS307\\CS307_project1\\useful\\data_origin\\user.csv";
        // 输出文件路径
        String outputFile10k = "C:\\Users\\WIN11\\OneDrive\\Desktop\\CS307\\CS307_project1\\useful\\data_origin\\user_1000k.csv";
        // 读取原始 CSV 文件
        List<String> lines = Files.readAllLines(Paths.get(inputFile));
        lines.remove(0);  // 移除表头
        // 去除 'FollowerUsers' 和 'FollowingUsers' 列，并且增加 3000000 到 'author_id'
        List<String> cleanedLines = cleanColumns(lines);
        // 打乱数据顺序
        Collections.shuffle(cleanedLines);  // 这里确保了抽样不重复
        // 随机抽取 10,000 行数据
        List<String> sampled10k = randomSample(cleanedLines, 1000000);
        writeToFile(sampled10k, outputFile10k);
    }

    // 去除 'FollowerUsers' 和 'FollowingUsers' 列，并且对 'author_id' 加 3000000
    private static List<String> cleanColumns(List<String> lines) {
        List<String> cleanedLines = new ArrayList<>();
        for (String line : lines) {
            // 按逗号分隔数据
            String[] columns = line.split(";");
            // 获取 'author_id' 并加上 3000000
            int authorId = Integer.parseInt(columns[0]) + 100000000;
            // 保留需要的列，并修改 'author_id'
            cleanedLines.add(String.join(";", String.valueOf(authorId), columns[1], columns[2], columns[3], columns[4], columns[5]));
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
