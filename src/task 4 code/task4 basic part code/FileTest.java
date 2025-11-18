package task4;

import java.io.IOException;

public class FileTest {
    public static void main(String[] args) throws IOException {
        FileOperation2 fileOps = new FileOperation2();
        String csvFilePath = "C:\\Users\\WIN11\\OneDrive\\Desktop\\CS307\\CS307_project1\\useful\\data_origin\\user_10000.csv";

        long insertTotalTime = 0;
        long queryTotalTime = 0;
        long updateTotalTime = 0;
        long deleteTotalTime = 0;
        // 执行5次实验
        for (int i = 1; i <= 5; i++) {
            System.out.println("Experiment " + i + ":");

            // 执行插入操作并计算时间
            long insertTime = fileOps.insertUsers(csvFilePath);
            System.out.println("Insert Time: " + insertTime + " ms");
            insertTotalTime += insertTime;

            // 执行查询操作并计算时间
            long queryTime = fileOps.queryUsers(csvFilePath);
            System.out.println("Query Time: " + queryTime + " ms");
            queryTotalTime += queryTime;

            // 执行更新操作并计算时间
            long updateTime = fileOps.updateUsersAge(csvFilePath);
            System.out.println("Update Time: " + updateTime + " ms");
            updateTotalTime += updateTime;

            // 执行删除操作并计算时间
            long deleteTime = fileOps.deleteUsers(csvFilePath);
            System.out.println("Delete Time: " + deleteTime + " ms");
            deleteTotalTime += deleteTime;

            System.out.println();
        }

        // 输出5次实验的平均时间
        System.out.println("Average Insert Time: " + (insertTotalTime / 5) + " ms");
        System.out.println("Average Query Time: " + (queryTotalTime / 5) + " ms");
        System.out.println("Average Update Time: " + (updateTotalTime / 5) + " ms");
        System.out.println("Average Delete Time: " + (deleteTotalTime / 5) + " ms");
    }
}
