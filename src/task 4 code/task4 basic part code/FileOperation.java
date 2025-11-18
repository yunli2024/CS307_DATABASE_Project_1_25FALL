package task4;

import java.io.*;
import java.util.*;

public class FileOperation {
    private final String filePath = "C:\\Users\\WIN11\\OneDrive\\Desktop\\CS307\\CS307_project1\\data\\user.csv"; // CSV文件路径
    private final String queryResultFilePath = "queryResult.csv";
    public int insertUsers(String csvFilePath) {
        long startTime = System.nanoTime();
        int rowsInserted = 0;
        try (FileReader fr = new FileReader(csvFilePath);
             BufferedReader br = new BufferedReader(fr)) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] userInfo = splitLine(line);
                String gender = userInfo[2].trim();
                if (gender.equalsIgnoreCase("male")) {
                    gender = "MALE";
                } else if (gender.equalsIgnoreCase("female")) {
                    gender = "FEMALE";
                } else {
                    System.err.println("无效的性别值: " + gender + "，跳过此行.");
                    continue;
                }
                String user = String.join(";", userInfo[0], userInfo[1], gender, userInfo[3], userInfo[4], userInfo[5]);
                try (FileWriter fw = new FileWriter(filePath, true)) {
                    fw.write(user);
                    fw.write("\n");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                rowsInserted++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        long endTime = System.nanoTime();
        long duration = (endTime - startTime) / 1_000_000;
        return (int) duration;
    }
    public int deleteUsers(String csvFilePath) {
        long startTime = System.nanoTime();
        int rowsDeleted = 0;
        Set<Integer> idsToDelete = new HashSet<>();
        try (FileReader fr = new FileReader(csvFilePath);
             BufferedReader br = new BufferedReader(fr)) {
            br.readLine();
            String line;
            while ((line = br.readLine()) != null) {
                String[] userInfo = line.split(",");
                int authorId = Integer.parseInt(userInfo[0].trim());
                idsToDelete.add(authorId);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try (FileReader fr = new FileReader(filePath);
             BufferedReader br = new BufferedReader(fr)) {
            List<String> remainingLines = new ArrayList<>();
            String line;
            while ((line = br.readLine()) != null) {
                String[] userInfo = line.split(";");
                int authorId = Integer.parseInt(userInfo[0].trim());
                if (!idsToDelete.contains(authorId)) {
                    remainingLines.add(line);
                } else {
                    rowsDeleted++;
                }
            }
            try (FileWriter fw = new FileWriter(filePath)) {
                for (String remainingLine : remainingLines) {
                    fw.write(remainingLine);
                    fw.write("\n");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        long endTime = System.nanoTime();
        long duration = (endTime - startTime) / 1_000_000; // 转换为毫秒
        return (int) duration;
    }
    public int updateUsersAge(String csvFilePath) {
        long startTime = System.nanoTime();
        int rowsUpdated = 0;
        Set<Integer> idsToUpdate = new HashSet<>();
        try (FileReader fr = new FileReader(csvFilePath);
             BufferedReader br = new BufferedReader(fr)) {
            br.readLine();
            String line;
            while ((line = br.readLine()) != null) {
                String[] userInfo = line.split(",");
                int authorId = Integer.parseInt(userInfo[0].trim());
                idsToUpdate.add(authorId);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try (FileReader fr = new FileReader(filePath);
             BufferedReader br = new BufferedReader(fr)) {
            List<String> updatedLines = new ArrayList<>();
            String line;
            while ((line = br.readLine()) != null) {
                String[] userInfo = line.split(";");
                int authorId = Integer.parseInt(userInfo[0].trim());
                if (idsToUpdate.contains(authorId)) {
                    int age = Integer.parseInt(userInfo[3].trim());
                    userInfo[3] = String.valueOf(age + 1);
                    rowsUpdated++;
                }
                String updatedLine = String.join(";", userInfo);
                updatedLines.add(updatedLine);
            }
            try (FileWriter fw = new FileWriter(filePath)) {
                for (String updatedLine : updatedLines) {
                    fw.write(updatedLine);
                    fw.write("\n");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        long endTime = System.nanoTime();
        long duration = (endTime - startTime) / 1_000_000;
        return (int) duration;
    }
    public int queryUsers(String csvFilePath) {
        long startTime = System.nanoTime();
        int count = 0;
        Set<Integer> idsToQuery = new HashSet<>();
        try (FileReader fr = new FileReader(csvFilePath);
             BufferedReader br = new BufferedReader(fr)) {
            br.readLine();
            String line;
            while ((line = br.readLine()) != null) {
                String[] userInfo = line.split(",");
                try {
                    int authorId = Integer.parseInt(userInfo[0].trim());
                    idsToQuery.add(authorId);
                } catch (NumberFormatException e) {
                    System.err.println("无效的AuthorId，跳过此行: " + line);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try (FileReader fr = new FileReader(filePath);
             BufferedReader br = new BufferedReader(fr);
             FileWriter fw = new FileWriter(queryResultFilePath)) {
            br.readLine();
            String line;
            while ((line = br.readLine()) != null) {
                String[] userInfo = line.split(";");
                try {
                    int authorId = Integer.parseInt(userInfo[0].trim());
                    if (idsToQuery.contains(authorId)) {
                        count++;
                        fw.write(line);
                        fw.write("\n");
                    }
                } catch (NumberFormatException e) {
                    System.err.println("无效的AuthorId，跳过此行: " + line);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        long endTime = System.nanoTime();
        long duration = (endTime - startTime) / 1_000_000; // 转换为毫秒
        return (int) duration;
    }
    private String[] splitLine(String line) {
        if (line.contains(",")) {
            return line.split(",");
        } else {
            return line.split(";");
        }
    }
}
