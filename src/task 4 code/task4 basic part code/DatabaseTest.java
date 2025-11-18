package task4;

import java.io.IOException;

public class DatabaseTest {
    public static void main(String[] args) throws IOException {
        DatabaseBetter dbOps = new DatabaseBetter();
        String csvFilePath = "C:\\Users\\WIN11\\OneDrive\\Desktop\\CS307\\CS307_project1\\useful\\data_origin\\user_10000.csv";  // 替换为实际的CSV文件路径
        int insertTotalTime = 0;
        int queryTotalTime = 0;
        int updateTotalTime = 0;
        int deleteTotalTime = 0;
        for (int i = 1; i <= 5; i++) {
            System.out.println("Experiment " + i + ":");
            long insertTime = dbOps.insertUsers(csvFilePath);
            System.out.println("Insert Time: " + insertTime + " ms");
            insertTotalTime += insertTime;
            long queryTime = dbOps.queryUsers(csvFilePath);
            System.out.println("Query Time: " + queryTime + " ms");
            queryTotalTime += queryTime;
            long updateTime = dbOps.updateUsersAge(csvFilePath);
            System.out.println("Update Time: " + updateTime + " ms");
            updateTotalTime += updateTime;
            long deleteTime = dbOps.deleteUsers(csvFilePath);
            System.out.println("Delete Time: " + deleteTime + " ms");
            deleteTotalTime += deleteTime;
            System.out.println();
        }
        System.out.println("Average Insert Time: " + (insertTotalTime / 5) + " ms");
        System.out.println("Average Query Time: " + (queryTotalTime / 5) + " ms");
        System.out.println("Average Update Time: " + (updateTotalTime / 5) + " ms");
        System.out.println("Average Delete Time: " + (deleteTotalTime / 5) + " ms");
    }

}
