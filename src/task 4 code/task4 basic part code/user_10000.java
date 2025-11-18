package task4_basic_version2;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.*;

public class user_10000 {
    public static void main(String[] args) throws IOException {
        String inputFile = "C:\\Users\\WIN11\\OneDrive\\Desktop\\CS307\\CS307_project1\\useful\\data_origin\\user.csv";
        String outputFile10k = "C:\\Users\\WIN11\\OneDrive\\Desktop\\CS307\\CS307_project1\\useful\\data_origin\\user_1000k.csv";
        List<String> lines = Files.readAllLines(Paths.get(inputFile));
        lines.remove(0);  
        List<String> cleanedLines = cleanColumns(lines);
        Collections.shuffle(cleanedLines); 
        List<String> sampled10k = randomSample(cleanedLines, 1000000);
        writeToFile(sampled10k, outputFile10k);
    }

    private static List<String> cleanColumns(List<String> lines) {
        List<String> cleanedLines = new ArrayList<>();
        for (String line : lines) {
            String[] columns = line.split(";");
            int authorId = Integer.parseInt(columns[0]) + 100000000;
            cleanedLines.add(String.join(";", String.valueOf(authorId), columns[1], columns[2], columns[3], columns[4], columns[5]));
        }
        return cleanedLines;
    }

    private static List<String> randomSample(List<String> lines, int n) {
        return lines.stream().limit(n).collect(Collectors.toList());
    }

    private static void writeToFile(List<String> data, String fileName) throws IOException {
        BufferedWriter writer = Files.newBufferedWriter(Paths.get(fileName));
        for (String line : data) {
            writer.write(line);
            writer.newLine();
        }
        writer.close();
    }
}

