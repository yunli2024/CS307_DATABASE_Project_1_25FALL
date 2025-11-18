package task4;

import java.io.*;
import java.util.*;

public class FileOperation {
    private final String filePath = "C:\\Users\\WIN11\\OneDrive\\Desktop\\CS307\\CS307_project1\\useful\\data_origin\\user.csv"; // CSV文件路径
    public void checkCSVFile(String csvFilePath) throws IOException {
        System.out.println("检查CSV文件内容...");

        try (BufferedReader br = new BufferedReader(new FileReader(csvFilePath))) {
            String header = br.readLine();
            System.out.println("文件头: " + header);

            String line;
            int lineNumber = 1;
            int sampleCount = 0;

            while ((line = br.readLine()) != null && sampleCount < 10) {
                System.out.println("第" + lineNumber + "行: " + line);
                String[] fields = line.split(",");
                if (fields.length > 0) {
                    System.out.println("AuthorID: '" + fields[0] + "'");
                }
                lineNumber++;
                sampleCount++;
            }
        }
    }
    // 插入用户
    public int insertUsers(String csvFilePath) {
        long startTime = System.nanoTime();
        int rowsInserted = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(csvFilePath))) {
            String line;
            List<String> linesToWrite = new ArrayList<>();
            while ((line = br.readLine()) != null) {
                String[] userInfo = line.split(",");  // 使用逗号作为分隔符
                String gender = userInfo[2].trim();
                // 检查性别是否有效
                if (gender.equalsIgnoreCase("male")) {
                    gender = "MALE";
                } else if (gender.equalsIgnoreCase("female")) {
                    gender = "FEMALE";
                } else {
                    System.err.println("无效的性别值: " + gender + "，跳过此行.");
                    continue;  // 跳过当前行，继续处理下一行
                }
                // 处理有效的性别
                String user = String.join(";", userInfo[0], userInfo[1], gender, userInfo[3], userInfo[4], userInfo[5]);
                linesToWrite.add(user);
                rowsInserted++;
            }
            // 批量写回数据
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath, true))) {  // 'true' 表示追加写入
                for (String user : linesToWrite) {
                    bw.write(user);
                    bw.newLine();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 计算整个操作时间（读取、处理、写回文件）
        long endTime = System.nanoTime();
        long duration = (endTime - startTime) / 1_000_000; // 转换为毫秒
        return (int) duration;
    }

    // 查询CSV文件中与ID相同的用户个数
    public int queryUsers(String csvFilePath) {
        long startTime = System.nanoTime();
        int count = 0;

        Set<Integer> idsToQuery = new HashSet<>();
        // 读取CSV文件中的所有ID到一个集合
        try (BufferedReader br = new BufferedReader(new FileReader(csvFilePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] userInfo = line.split(",");  // 使用逗号作为分隔符
                try {
                    int authorId = Integer.parseInt(userInfo[0].trim());  // 假设ID是第一个字段
                    idsToQuery.add(authorId);
                } catch (NumberFormatException e) {
                    System.err.println("无效的AuthorId，跳过此行: " + line);
                    continue;  // 跳过此行，继续处理下一行
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 在文件中查询与CSV文件ID相同的数据
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            br.readLine();  // 跳过表头
            while ((line = br.readLine()) != null) {
                String[] userInfo = line.split(";");  // 使用分号作为分隔符
                try {
                    int authorId = Integer.parseInt(userInfo[0].trim());
                    if (idsToQuery.contains(authorId)) {
                        count++;
                    }
                } catch (NumberFormatException e) {
                    System.err.println("无效的AuthorId，跳过此行: " + line);
                    continue;  // 跳过此行，继续处理下一行
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 计算查询操作的时间
        long endTime = System.nanoTime();
        long duration = (endTime - startTime) / 1_000_000; // 转换为毫秒
        return (int) duration;
    }

    // 根据CSV文件更新与ID相同的记录的年龄
    public int updateUsersAge(String csvFilePath) {
        long startTime = System.nanoTime();
        int rowsUpdated = 0;
        Set<Integer> idsToUpdate = new HashSet<>();

        // 读取CSV文件中的所有ID到一个集合
        try (BufferedReader br = new BufferedReader(new FileReader(csvFilePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] userInfo = line.split(",");  // 使用逗号作为分隔符
                int authorId = Integer.parseInt(userInfo[0].trim());
                idsToUpdate.add(authorId);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 更新与CSV文件ID相同的记录
        List<String> linesToWrite = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] userInfo = line.split(";");  // 使用分号作为分隔符
                int authorId = Integer.parseInt(userInfo[0].trim());
                if (idsToUpdate.contains(authorId)) {
                    int age = Integer.parseInt(userInfo[3].trim());  // 假设年龄字段是第四列
                    userInfo[3] = String.valueOf(age + 1);  // 更新年龄
                    rowsUpdated++;
                }
                String updatedLine = String.join(";", userInfo);
                linesToWrite.add(updatedLine);
            }
            // 写回更新后的数据
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath))) {
                for (String updatedLine : linesToWrite) {
                    bw.write(updatedLine);
                    bw.newLine();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 计算整个更新操作的时间
        long endTime = System.nanoTime();
        long duration = (endTime - startTime) / 1_000_000; // 转换为毫秒
        return (int) duration;
    }

    // 删除CSV文件中ID相同的用户数据
    public int deleteUsers(String csvFilePath) {
        long startTime = System.nanoTime();
        int rowsDeleted = 0;
        Set<Integer> idsToDelete = new HashSet<>();

        // 读取CSV文件中的所有ID到一个集合
        try (BufferedReader br = new BufferedReader(new FileReader(csvFilePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] userInfo = line.split(",");  // 使用逗号作为分隔符
                int authorId = Integer.parseInt(userInfo[0].trim());
                idsToDelete.add(authorId);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 删除与CSV文件ID相同的数据
        List<String> remainingLines = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] userInfo = line.split(";");  // 使用分号作为分隔符
                int authorId = Integer.parseInt(userInfo[0].trim());
                if (!idsToDelete.contains(authorId)) {
                    remainingLines.add(line);  // 保留不在删除列表中的用户
                } else {
                    rowsDeleted++;  // 删除符合条件的用户
                }
            }
            // 写回剩余的有效数据
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath))) {
                for (String line1 : remainingLines) {
                    bw.write(line1);
                    bw.newLine();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 计算整个删除操作的时间
        long endTime = System.nanoTime();
        long duration = (endTime - startTime) / 1_000_000; // 转换为毫秒
        return (int) duration;
    }
}
