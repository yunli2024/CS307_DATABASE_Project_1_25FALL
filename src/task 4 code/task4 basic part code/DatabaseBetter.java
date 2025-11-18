package task4;


import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.*;

public class DatabaseBetter {
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
        try {
            if (con != null) con.close();
        } catch (SQLException ignored) {}
    }

    // 插入数据到数据库并返回执行时间
    public long insertUsers(String csvFilePath) {
        long startTime = System.nanoTime();
        String sql = "INSERT INTO task4advance2.users (authorid, authorname, gender, age, followers, following) VALUES (?, ?, ?, ?, ?, ?)";
        try {
            getConnection();
            con.setAutoCommit(false);  // 禁用自动提交，手动控制事务

            BufferedReader br = new BufferedReader(new FileReader(csvFilePath));
            String line;
            PreparedStatement preparedStatement = con.prepareStatement(sql);
            int count = 0;
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

                preparedStatement.setInt(1, Integer.parseInt(userInfo[0]));
                preparedStatement.setString(2, userInfo[1]);
                preparedStatement.setString(3, gender);
                preparedStatement.setInt(4, Integer.parseInt(userInfo[3]));
                preparedStatement.setInt(5, Integer.parseInt(userInfo[4]));
                preparedStatement.setInt(6, Integer.parseInt(userInfo[5]));
                preparedStatement.addBatch();

                // 批量插入，每1000条执行一次
                if (++count % 1000 == 0) {
                    preparedStatement.executeBatch();
                }
            }
            // 执行剩余的批次
            preparedStatement.executeBatch();
            con.commit();  // 提交事务
            br.close();
        } catch (SQLException | IOException e) {
            try {
                con.rollback();  // 出现错误时回滚
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
        } finally {
            try {
                con.setAutoCommit(true);  // 恢复默认的自动提交模式
            } catch (SQLException e) {
                e.printStackTrace();
            }
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
            PreparedStatement preparedStatement = con.prepareStatement(sql);
            while ((line = br.readLine()) != null) {
                String[] userInfo = line.split(",");
                int authorId = Integer.parseInt(userInfo[0]);
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
            con.setAutoCommit(false);  // 禁用自动提交，手动控制事务

            BufferedReader br = new BufferedReader(new FileReader(csvFilePath));
            String line;
            PreparedStatement preparedStatement = con.prepareStatement(sql);
            int count = 0;
            while ((line = br.readLine()) != null) {
                String[] userInfo = line.split(",");
                int authorId = Integer.parseInt(userInfo[0]);
                preparedStatement.setInt(1, authorId);
                preparedStatement.addBatch();

                // 批量更新，每1000条执行一次
                if (++count % 1000 == 0) {
                    preparedStatement.executeBatch();
                }
            }
            // 执行剩余的批次
            preparedStatement.executeBatch();
            con.commit();  // 提交事务
            br.close();
        } catch (SQLException | IOException e) {
            try {
                con.rollback();  // 出现错误时回滚
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
        } finally {
            try {
                con.setAutoCommit(true);  // 恢复默认的自动提交模式
            } catch (SQLException e) {
                e.printStackTrace();
            }
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
            con.setAutoCommit(false);  // 禁用自动提交，手动控制事务

            BufferedReader br = new BufferedReader(new FileReader(csvFilePath));
            String line;
            PreparedStatement preparedStatement = con.prepareStatement(sql);
            int count = 0;
            while ((line = br.readLine()) != null) {
                String[] userInfo = line.split(",");
                int authorId = Integer.parseInt(userInfo[0]);
                preparedStatement.setInt(1, authorId);
                preparedStatement.addBatch();

                // 批量删除，每1000条执行一次
                if (++count % 1000 == 0) {
                    preparedStatement.executeBatch();
                }
            }
            // 执行剩余的批次
            preparedStatement.executeBatch();
            con.commit();  // 提交事务
            br.close();
        } catch (SQLException | IOException e) {
            try {
                con.rollback();  // 出现错误时回滚
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
        } finally {
            try {
                con.setAutoCommit(true);  // 恢复默认的自动提交模式
            } catch (SQLException e) {
                e.printStackTrace();
            }
            closeConnection();
        }
        return (System.nanoTime() - startTime) / 1_000_000;  // 返回执行时间，单位：毫秒
    }
}