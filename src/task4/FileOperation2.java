package task4;

import java.io.*;
import java.util.*;

public class FileOperation2 {
    private final String filePath = "C:\\Users\\WIN11\\OneDrive\\Desktop\\CS307\\CS307_project1\\data_from teacher\\final_data\\user.csv"; // CSV文件路径

    // 插入用户（Add）
    public int insertUsers(String csvFilePath) {
        long startTime = System.nanoTime();
        int rowsInserted = 0;
        int invalidGenderCount = 0;  // 无效性别计数
        List<String> linesToWrite = new ArrayList<>();

        // 使用临时文件策略
        String tempFilePath = filePath + ".temp";
        try (BufferedReader br = new BufferedReader(new FileReader(csvFilePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] userInfo = line.split(",");  // 按逗号分割
                String gender = userInfo[2].trim();
                // 检查性别是否有效
                if (gender.equalsIgnoreCase("male")) {
                    gender = "MALE";
                } else if (gender.equalsIgnoreCase("female")) {
                    gender = "FEMALE";
                } else {
                    System.err.println("无效的性别值: " + gender + "，跳过此行.");
                    invalidGenderCount++;  // 增加无效性别计数
                    continue;  // 跳过无效的行
                }
                // 处理有效的性别
                String user = String.join(";", userInfo[0], userInfo[1], gender, userInfo[3], userInfo[4], userInfo[5]);
                linesToWrite.add(user);
                rowsInserted++;
            }

            // 使用临时文件写入数据
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(tempFilePath))) {
                for (String user : linesToWrite) {
                    bw.write(user);
                    bw.newLine();  // 每条记录后添加新行
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 最后将临时文件的数据写入原文件
        try {
            File originalFile = new File(filePath);
            File tempFile = new File(tempFilePath);
            if (originalFile.delete()) { // 删除原文件
                tempFile.renameTo(originalFile); // 将临时文件重命名为原文件
            } else {
                System.err.println("无法删除原文件");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 输出无效值的统计
        System.out.println("无效性别记录数量: " + invalidGenderCount);

        // 计算操作时间
        long endTime = System.nanoTime();
        long duration = (endTime - startTime) / 1_000_000; // 转换为毫秒
        return (int) duration;
    }

    // 查询用户（按ID统计匹配记录）
    public int queryUsers(String csvFilePath) {
        long startTime = System.nanoTime();
        int count = 0;
        int invalidIdCount = 0;  // 无效ID计数

        Set<Integer> idsToQuery = new HashSet<>();
        List<String> queryResults = new ArrayList<>();

        // 使用临时文件策略
        String tempFilePath = filePath + ".temp";
        // 读取CSV文件中的所有ID到集合中
        try (BufferedReader br = new BufferedReader(new FileReader(csvFilePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] userInfo = line.split(",");  // 按逗号分割
                try {
                    int authorId = Integer.parseInt(userInfo[0].trim());  // 假设ID是第一个字段
                    idsToQuery.add(authorId);
                } catch (NumberFormatException e) {
                    System.err.println("无效的AuthorId，跳过此行: " + line);
                    invalidIdCount++;  // 增加无效ID计数
                    continue;  // 跳过此行
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 在文件中查询与CSV文件ID相同的数据
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] userInfo = line.split(";");  // 按分号分割
                try {
                    int authorId = Integer.parseInt(userInfo[0].trim());
                    if (idsToQuery.contains(authorId)) {
                        queryResults.add(line);  // 添加到查询结果列表
                        count++;
                    }
                } catch (NumberFormatException e) {
                    System.err.println("无效的AuthorId，跳过此行: " + line);
                    invalidIdCount++;  // 增加无效ID计数
                    continue;  // 跳过此行
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 使用临时文件写入查询结果
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(tempFilePath))) {
            for (String result : queryResults) {
                bw.write(result);
                bw.newLine();  // 每条查询结果后添加新行
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 最后将查询结果写入硬盘
        try {
            File originalFile = new File(filePath);
            File tempFile = new File(tempFilePath);
            if (originalFile.delete()) { // 删除原文件
                tempFile.renameTo(originalFile); // 将临时文件重命名为原文件
            } else {
                System.err.println("无法删除原文件");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 输出无效值的统计
        System.out.println("无效ID记录数量: " + invalidIdCount);

        // 计算查询操作时间
        long endTime = System.nanoTime();
        long duration = (endTime - startTime) / 1_000_000; // 转换为毫秒
        return (int) duration;
    }

    // 更新用户年龄（根据ID更新记录）
    public int updateUsersAge(String csvFilePath) {
        long startTime = System.nanoTime();
        int rowsUpdated = 0;
        int invalidIdCount = 0;  // 无效ID计数
        Set<Integer> idsToUpdate = new HashSet<>();
        List<String> linesToWrite = new ArrayList<>();

        // 使用临时文件策略
        String tempFilePath = filePath + ".temp";
        // 读取CSV文件中的所有ID到集合中
        try (BufferedReader br = new BufferedReader(new FileReader(csvFilePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] userInfo = line.split(",");  // 按逗号分割
                try {
                    int authorId = Integer.parseInt(userInfo[0].trim());
                    idsToUpdate.add(authorId);
                } catch (NumberFormatException e) {
                    System.err.println("无效的AuthorId，跳过此行: " + line);
                    invalidIdCount++;  // 增加无效ID计数
                    continue;  // 跳过此行
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 更新记录
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] userInfo = line.split(";");  // 按分号分割
                int authorId = Integer.parseInt(userInfo[0].trim());
                if (idsToUpdate.contains(authorId)) {
                    int age = Integer.parseInt(userInfo[3].trim());  // 假设年龄是第四列
                    userInfo[3] = String.valueOf(age + 1);  // 增加年龄
                    rowsUpdated++;
                }
                String updatedLine = String.join(";", userInfo);
                linesToWrite.add(updatedLine);
            }

            // 使用临时文件写入更新后的数据
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(tempFilePath))) {
                for (String updatedLine : linesToWrite) {
                    bw.write(updatedLine);
                    bw.newLine();  // 每条更新后的记录后添加新行
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 最后将更新后的数据写入硬盘
        try {
            File originalFile = new File(filePath);
            File tempFile = new File(tempFilePath);
            if (originalFile.delete()) { // 删除原文件
                tempFile.renameTo(originalFile); // 将临时文件重命名为原文件
            } else {
                System.err.println("无法删除原文件");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 输出无效值的统计
        System.out.println("无效ID记录数量: " + invalidIdCount);

        // 计算更新操作时间
        long endTime = System.nanoTime();
        long duration = (endTime - startTime) / 1_000_000; // 转换为毫秒
        return (int) duration;
    }

    // 删除用户（根据ID删除记录）
    public int deleteUsers(String csvFilePath) {
        long startTime = System.nanoTime();
        int rowsDeleted = 0;
        int invalidIdCount = 0;  // 无效ID计数
        Set<Integer> idsToDelete = new HashSet<>();
        List<String> remainingLines = new ArrayList<>();

        // 使用临时文件策略
        String tempFilePath = filePath + ".temp";
        // 读取CSV文件中的所有ID到集合中
        try (BufferedReader br = new BufferedReader(new FileReader(csvFilePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] userInfo = line.split(",");  // 按逗号分割
                try {
                    int authorId = Integer.parseInt(userInfo[0].trim());
                    idsToDelete.add(authorId);
                } catch (NumberFormatException e) {
                    System.err.println("无效的AuthorId，跳过此行: " + line);
                    invalidIdCount++;  // 增加无效ID计数
                    continue;  // 跳过此行
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 删除记录
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] userInfo = line.split(";");  // 按分号分割
                int authorId = Integer.parseInt(userInfo[0].trim());
                if (!idsToDelete.contains(authorId)) {
                    remainingLines.add(line);  // 保留不在删除列表中的记录
                } else {
                    rowsDeleted++;  // 删除符合条件的记录
                }
            }

            // 使用临时文件写入剩余的有效数据
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(tempFilePath))) {
                for (String line1 : remainingLines) {
                    bw.write(line1);
                    bw.newLine();  // 每条保留的记录后添加新行
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 最后将剩余的数据写入硬盘
        try {
            File originalFile = new File(filePath);
            File tempFile = new File(tempFilePath);
            if (originalFile.delete()) { // 删除原文件
                tempFile.renameTo(originalFile); // 将临时文件重命名为原文件
            } else {
                System.err.println("无法删除原文件");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 输出无效值的统计
        System.out.println("无效ID记录数量: " + invalidIdCount);

        // 计算删除操作时间
        long endTime = System.nanoTime();
        long duration = (endTime - startTime) / 1_000_000; // 转换为毫秒
        return (int) duration;
    }
}
