package task4;

import java.io.IOException;

public class DatabaseTest {

    public static void main(String[] args) throws IOException {
        // 创建数据库操作对象
        DatabaseBetter dbOps = new DatabaseBetter();

        // 设置CSV文件路径
        String csvFilePath = "C:\\Users\\WIN11\\OneDrive\\Desktop\\CS307\\CS307_project1\\useful\\data_origin\\user_10000.csv";  // 替换为实际的CSV文件路径
        // 进行5次实验并计算每次的时间
        int insertTotalTime = 0;
        int queryTotalTime = 0;
        int updateTotalTime = 0;
        int deleteTotalTime = 0;
        // 执行5次实验
        for (int i = 1; i <= 5; i++) {
            System.out.println("Experiment " + i + ":");
            // 执行插入操作并计算时间
            long insertTime = dbOps.insertUsers(csvFilePath);
            System.out.println("Insert Time: " + insertTime + " ms");
            insertTotalTime += insertTime;
            // 执行查询操作并计算时间
            long queryTime = dbOps.queryUsers(csvFilePath);
            System.out.println("Query Time: " + queryTime + " ms");
            queryTotalTime += queryTime;
            // 执行更新操作并计算时间
            long updateTime = dbOps.updateUsersAge(csvFilePath);
            System.out.println("Update Time: " + updateTime + " ms");
            updateTotalTime += updateTime;
            // 执行删除操作并计算时间
            long deleteTime = dbOps.deleteUsers(csvFilePath);
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