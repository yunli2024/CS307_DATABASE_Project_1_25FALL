package task4;

import java.io.*;
import java.util.*;
/**
 * FileManipulation类实现了DataManipulation接口，提供了基于文件的食谱数据操作功能。
 * 该类通过文件I/O操作来管理食谱数据，支持添加、删除、更新和查询食谱记录。
 * 数据存储在文本文件中，使用分号分隔字段。
 */
public class FileManipulation implements DataManipulation {
    // 食谱数据文件路径
    //换行是通过换行符实现的，而不是视觉上的"换行"。每个记录通过换行符分隔
    private static final String RECIPES_FILE = "recipes.txt";
    @Override
    public int addOneRecipe(String recipeData) {//完成
        // 使用try-with-resources自动关闭文件资源
        try (FileWriter writer = new FileWriter(RECIPES_FILE, true)) {  // 以追加模式打开文件
            // 直接使用FileWriter写入，不使用缓冲
            /* 可选：使用缓冲写入提高性能的版本
            try (FileWriter writer = new FileWriter(RECIPES_FILE, true);
                 BufferedWriter bufferedWriter = new BufferedWriter(writer)) {
                bufferedWriter.write(recipeData);
            */
            // 确保每行数据以换行符结尾，避免记录连接在一起
            if (!recipeData.endsWith("\n")) {
                recipeData += "\n";
            }
            // 写入食谱数据到文件
            writer.write(recipeData);
            return 1; // 成功添加一条记录
        } catch (IOException e) {
            // 捕获并打印I/O异常，如文件不存在或无权限访问
            e.printStackTrace();
            return 0; // 添加失败
        }
    }
    @Override
    public int deleteRecipeById(int recipeId) {//完成
        File inputFile = new File(RECIPES_FILE);  // 原始数据文件
        File tempFile = new File("temp_recipes.txt");  // 临时文件，用于存储修改后的数据
        boolean found = false;  // 标记是否找到要删除的记录
        // 使用try-with-resources自动关闭文件资源
        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile));  // 读取原始文件
             FileWriter writer = new FileWriter(tempFile)) {  // 直接使用FileWriter写入，不使用缓冲
            String line;  // 存储当前读取的行
            // 遍历文件中的每一行
            while ((line = reader.readLine()) != null) {
                // 假设每行的第一个字段是RecipeId，以分号空格分隔
                // limit=2 表示最多分割成两部分，避免后续字段中的分号干扰ID识别，即一部分为RecipeID,另一部分为剩余其他
                String[] parts = line.split(";", 2);
                if (parts.length > 0) {  // 确保行不为空且至少有一个字段
                    try {
                        // 尝试将第一个字段解析为整数ID
                        //.trim()可有可无，根据数据预处理的结果，ID字段前后可能有空格
                        int currentId = Integer.parseInt(parts[0].trim());
                        // 如果当前行的ID不等于要删除的ID，则将其写入临时文件
                        if (currentId != recipeId) {
                            writer.write(line);  // 写入完整的原始行
                            writer.write("\n");  // 确保行尾有换行符
                        } else {
                            found = true;  // 找到要删除的记录
                            // 不写入该行，相当于删除
                        }
                    } catch (NumberFormatException e) {
                        // 如果第一部分无法解析为数字（可能不是有效的记录行），保留该行
                        //这一步可有可无，在数据预处理的时候已经处理了
                        writer.write(line);
                        writer.write("\n");
                    }
                }
            }
        } catch (IOException e) {
            // 捕获并打印I/O异常
            e.printStackTrace();
            return 0; // 删除失败
        }
        // 文件替换操作：删除原文件并将临时文件重命名为原文件名
        if (inputFile.delete() && tempFile.renameTo(inputFile)) {
            return found ? 1 : 0; // 返回是否找到并删除了记录
        }
        return 0; // 文件替换失败
    }
    @Override
    public int updateRecipeRating(int recipeId, double newRating) {//完成
        File inputFile = new File(RECIPES_FILE);  // 原始数据文件
        File tempFile = new File("temp_recipes.txt");  // 临时文件
        boolean updated = false;  // 标记是否更新了记录
        // 使用try-with-resources自动关闭文件资源
        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile));
             FileWriter writer = new FileWriter(tempFile)) {  // 直接使用FileWriter写入，不使用缓冲
            String line;  // 当前读取的行
            // 遍历文件中的每一行
            while ((line = reader.readLine()) != null) {
                // 将行按分号分割成多个字段
                String[] parts = line.split(";");
                if (parts.length > 0) {  // 确保行不为空且至少有一个字段
                    try {
                        // 解析第一个字段为食谱ID
                        int currentId = Integer.parseInt(parts[0].trim());
                        if (currentId == recipeId) {  // 找到要更新的记录
                            // 使用StringBuilder构建更新后的行
                            StringBuilder updatedLine = new StringBuilder();
                            // 遍历所有字段
                            for (int i = 0; i < parts.length; i++) {
                                if (i > 0) {
                                    updatedLine.append("; ");  // 添加字段分隔符
                                }
                                // 评分字段是第十三个，将其替换为新的评分值
                                if (i == 12) {
                                    updatedLine.append(newRating);
                                } else {
                                    updatedLine.append(parts[i]);  // 保留其他字段不变
                                }
                            }
                            // 写入更新后的行到临时文件
                            writer.write(updatedLine.toString());
                            writer.write("\n");//添加行尾换行符
                            //使用 BufferedReader.readLine() 方法读取文件内容时，该方法会自动去除行尾的换行符
                            updated = true;  // 标记已更新
                        } else {
                            // 不是目标记录，直接写入原始行
                            writer.write(line);
                            writer.write("\n");
                        }
                    } catch (NumberFormatException e) {
                        // 如果第一部分不是数字，保留原始行
                        writer.write(line);
                        writer.write("\n");
                    }
                }
            }
        } catch (IOException e) {
            // 捕获并打印I/O异常
            e.printStackTrace();
            return 0; // 更新失败
        }
        // 文件替换操作
        if (inputFile.delete() && tempFile.renameTo(inputFile)) {
            return updated ? 1 : 0; // 返回是否更新了记录
        }
        return 0; // 文件替换失败
    }
    @Override
    public String findRecipeById(int recipeId) {
        File inputFile = new File(RECIPES_FILE);  // 原始数据文件
        File tempFile = new File("temp_recipes.txt");
        // 使用try-with-resources自动关闭文件资源
        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile));
             FileWriter writer = new FileWriter(tempFile)){
            String line;  // 当前读取的行
            // 遍历文件中的每一行
            while ((line = reader.readLine()) != null) {
                // 分割行，提取ID部分
                String[] parts = line.split("; ", 2);  // limit=2 只分割第一个分号
                if (parts.length > 0) {  // 确保行不为空
                    try {
                        // 解析ID字段
                        int currentId = Integer.parseInt(parts[0].trim());
                        // 找到匹配的ID
                        if (currentId == recipeId) {
                            return line; // 返回完整的记录行
                        }
                    } catch (NumberFormatException e) {
                        // 忽略无法解析为数字的行
                        // 继续查找下一行
                    }
                }
            }
            // 遍历完所有行仍未找到，返回未找到消息
            return "Recipe not found";
        } catch (IOException e) {
            // 捕获并打印I/O异常
            e.printStackTrace();
            return "Error reading file";
        }
    }
}
