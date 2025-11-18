package task4;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class DataGenerate {

    // 用于生成100条和1000条数据并分别保存为两个CSV文件
    public static void generateData(int numRecords100, int numRecords1000) {
        // 数据生成的起始 recipe_id（大于1000000）
        final int START_ID = 1000001;
        // 存储已生成的 recipe_id，确保唯一性
        Set<Integer> generatedIds = new HashSet<>();
        Random random = new Random();

        // 生成足够大的 recipe_id 集合，确保100条和1000条数据不重复
        while (generatedIds.size() < (numRecords100 + numRecords1000)) {
            int recipeId = START_ID + random.nextInt(1000000);  // 生成大于1000000的 recipe_id
            generatedIds.add(recipeId);  // 添加到 Set 中，Set 会自动确保唯一性
        }

        // 创建两个文件路径，用于存储100条数据和1000条数据
        String outputPath100 = "recipes_100.csv";
        String outputPath1000 = "recipes_1000.csv";

        // 分别生成100条和1000条数据
        try (
                BufferedWriter writer100 = new BufferedWriter(new FileWriter(outputPath100));
                BufferedWriter writer1000 = new BufferedWriter(new FileWriter(outputPath1000))
        ) {
            // 写入 CSV 表头
            writer100.write("recipe_id,author_id,recipe_name,description,recipe_category,recipe_yield,recipe_servings,aggregated_rating,review_count\n");
            writer1000.write("recipe_id,author_id,recipe_name,description,recipe_category,recipe_yield,recipe_servings,aggregated_rating,review_count\n");

            // 将 generatedIds 中的 recipe_id 分配给两个文件
            int count = 0;
            for (int recipeId : generatedIds) {
                String csvLine = String.format(
                        "%d,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL", recipeId);
                if (count < numRecords100) {
                    // 将100条数据写入recipes_100.csv
                    writer100.write(csvLine + "\n");
                } else {
                    // 将1000条数据写入recipes_1000.csv
                    writer1000.write(csvLine + "\n");
                }
                count++;
            }

            System.out.println("Generated " + numRecords100 + " records and saved to " + outputPath100);
            System.out.println("Generated " + numRecords1000 + " records and saved to " + outputPath1000);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        // 生成100条数据并保存到 recipes_100.csv，1000条数据保存到 recipes_1000.csv
        generateData(100, 1000);
    }
}
