package task4;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.*;

public class DatabaseOperations {
    private Connection con;
    private final String host = "localhost";
    private final String dbname = "task4advance2";
    private final String user = "postgres";
    private final String pwd = "WT060519";
    private final String port = "5432";

    // 获取数据库连接
    private void getConnection() {
        try {
            Class.forName("org.postgresql.Driver");
            con = DriverManager.getConnection(
                    "jdbc:postgresql://" + host + ":" + port + "/" + dbname,
                    user, pwd);
            try (Statement st = con.createStatement()) {
                st.execute("SET search_path TO task4advance2, public;");
            }
        } catch (Exception e) {
            System.err.println("DB connection error: " + e.getMessage());
            System.exit(1);
        }
    }

    // 关闭数据库连接
    private void closeConnection() {
        try { if (con != null) con.close(); } catch (Exception ignored) {}
    }

    // 插入数据到数据库并返回执行时间
    public long insertUsers(String csvFilePath) {
        long startTime = System.nanoTime();
        String sql = "INSERT INTO task4advance2.users (authorid, authorname, gender, age, followers, following) VALUES (?, ?, ?, ?, ?, ?)";

        try {
            getConnection();
            BufferedReader br = new BufferedReader(new FileReader(csvFilePath));
            String line;
            while ((line = br.readLine()) != null) {
                String[] userInfo = line.split(",");
                String gender = userInfo[2].trim();
                if (gender.equalsIgnoreCase("male")) {
                    gender = "MALE";
                } else if (gender.equalsIgnoreCase("female")) {
                    gender = "FEMALE";
                } else {
                    throw new IllegalArgumentException("Invalid gender value: " + gender);
                }
                PreparedStatement preparedStatement = con.prepareStatement(sql);
                preparedStatement.setInt(1, Integer.parseInt(userInfo[0]));
                preparedStatement.setString(2, userInfo[1]);
                preparedStatement.setString(3, gender);
                preparedStatement.setInt(4, Integer.parseInt(userInfo[3]));
                preparedStatement.setInt(5, Integer.parseInt(userInfo[4]));
                preparedStatement.setInt(6, Integer.parseInt(userInfo[5]));
                preparedStatement.executeUpdate();
            }
            br.close();
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        } finally {
            closeConnection();
        }
        return (System.nanoTime() - startTime) / 1_000_000;  // 返回执行时间，单位：毫秒
    }

    // 查询与CSV文件ID相同的数据个数，并返回执行时间
    public long queryUsers(String csvFilePath) {
        long startTime = System.nanoTime();
        String sql = "SELECT COUNT(*) FROM task4advance2.users WHERE authorid = ?";

        try {
            getConnection();
            BufferedReader br = new BufferedReader(new FileReader(csvFilePath));
            String line;
            while ((line = br.readLine()) != null) {
                String[] userInfo = line.split(",");
                int authorId = Integer.parseInt(userInfo[0]);
                PreparedStatement preparedStatement = con.prepareStatement(sql);
                preparedStatement.setInt(1, authorId);
                ResultSet rs = preparedStatement.executeQuery();
                if (rs.next()) {
                    rs.getInt(1);  // 获取查询结果，但不需要做其他操作
                }
            }
            br.close();
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        } finally {
            closeConnection();
        }
        return (System.nanoTime() - startTime) / 1_000_000;  // 返回执行时间，单位：毫秒
    }

    // 更新与CSV文件ID相同的记录的age字段，并返回执行时间
    public long updateUsersAge(String csvFilePath) {
        long startTime = System.nanoTime();
        String sql = "UPDATE task4advance2.users SET age = age + 1 WHERE authorid = ?";

        try {
            getConnection();
            BufferedReader br = new BufferedReader(new FileReader(csvFilePath));
            String line;
            while ((line = br.readLine()) != null) {
                String[] userInfo = line.split(",");
                int authorId = Integer.parseInt(userInfo[0]);
                PreparedStatement preparedStatement = con.prepareStatement(sql);
                preparedStatement.setInt(1, authorId);
                preparedStatement.executeUpdate();
            }
            br.close();
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        } finally {
            closeConnection();
        }
        return (System.nanoTime() - startTime) / 1_000_000;  // 返回执行时间，单位：毫秒
    }

    // 删除与CSV文件ID相同的数据，并返回执行时间
    public long deleteUsers(String csvFilePath) {
        long startTime = System.nanoTime();
        String sql = "DELETE FROM task4advance2.users WHERE authorid = ?";

        try {
            getConnection();
            BufferedReader br = new BufferedReader(new FileReader(csvFilePath));
            String line;
            while ((line = br.readLine()) != null) {
                String[] userInfo = line.split(",");
                int authorId = Integer.parseInt(userInfo[0]);
                PreparedStatement preparedStatement = con.prepareStatement(sql);
                preparedStatement.setInt(1, authorId);
                preparedStatement.executeUpdate();
            }
            br.close();
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        } finally {
            closeConnection();
        }
        return (System.nanoTime() - startTime) / 1_000_000;  // 返回执行时间，单位：毫秒
    }
}
