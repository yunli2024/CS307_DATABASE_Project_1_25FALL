package task4;//导入Java SQL相关的所有类，包括Connection、Statement、PreparedStatement、ResultSet等用于数据库操作的核心类
import java.sql.*;

public class DatabaseManipulation implements DataManipulation {
    private Connection con = null;//用于表示数据库连接
    private ResultSet resultSet;//用于存储SQL查询的结果集
    //根据个人情况修改
    private String host = "localhost";//存储数据库服务器地址，默认值为"localhost"（本地主机）
    private String dbname = "cs307fall2024";//存储数据库名称
    private String user = "postgres";//存储数据库用户名，默认值为"postgres"
    private String pwd = "000000";//用于存储数据库密码
    private String port = "5432";//存储数据库端口号

    //连接和关闭数据库照抄
    private void getConnection() {//用于建立数据库连接。
        try {
            Class.forName("org.postgresql.Driver");
            //动态加载PostgreSQL JDBC驱动类，这是使用JDBC连接PostgreSQL数据库的必要步骤
        } catch (Exception e) {
            System.err.println("Cannot find the PostgreSQL driver. Check CLASSPATH.");
            System.exit(1);
        }

        try {
            String url = "jdbc:postgresql://" + host + ":" + port + "/" + dbname;
            con = DriverManager.getConnection(url, user, pwd);

        } catch (SQLException e) {
            System.err.println("Database connection failed");
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }


    private void closeConnection() {//用于关闭数据库连接
        if (con != null) {
            try {
                con.close();//关闭数据库连接。
                con = null;//将连接对象设为null，以便垃圾回收。
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public int addOneRecipe(String str) {//增加数据
        long startTime = System.currentTimeMillis();
        getConnection();//调用getConnection方法建立数据库连接
        int result = 0;//用于存储SQL执行结果
        String sql = "insert into movies (RecipeId,Name,AuthorId,AuthorName," +
                "CookTime,PrepTime,TotalTime,DatePublished,Description,RecipeCategory," +
                "Keywords,RecipeIngredientParts,AggregatedRating,ReviewCount,Calories,FatContent," +
                "SaturatedFatContent,CholesterolContent,SodiumContent,CarbohydrateContent,FiberContent," +
                "SugarContent,ProteinContent,RecipeServings,RecipeYield,RecipeInstructions,FavoriteUsers) " +
                "VALUES (?,?,?,?,?,?,?,(?::timestamptz AT TIME ZONE 'UTC'), ?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";//定义插入SQL语句，向movies表中插入电影信息
        String c[] = str.split(";");//将传入的字符串按分号分割
        try {//设置对应参数的值
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1,  parseIntSafe(c[0], 0));
            ps.setString(2,  nullIfEmpty(c[1]));
            ps.setInt(3,  parseIntSafe(c[2], 0));
            ps.setString(4,  nullIfEmpty(c[3]));
            ps.setString(5,  nullIfEmpty(c[4]));
            ps.setString(6,  nullIfEmpty(c[5]));
            ps.setString(7,  nullIfEmpty(c[6]));
            ps.setString(8,  nullIfEmpty(c[7]));
            ps.setString(9,  nullIfEmpty(c[8]));
            ps.setString(10, nullIfEmpty(c[9]));
            ps.setString(11, nullIfEmpty(c[10]));
            ps.setString(12, nullIfEmpty(c[11]));
            ps.setObject(13, parseDoubleSafe(c[12], null), java.sql.Types.DOUBLE);
            ps.setObject(14, parseDoubleSafe(c[13], null), java.sql.Types.DOUBLE);
            ps.setObject(15, parseDoubleSafe(c[14], null), java.sql.Types.DOUBLE);
            ps.setObject(16, parseDoubleSafe(c[15], null), java.sql.Types.DOUBLE);
            ps.setObject(17, parseDoubleSafe(c[16], null), java.sql.Types.DOUBLE);
            ps.setObject(18, parseDoubleSafe(c[17], null), java.sql.Types.DOUBLE);
            ps.setObject(19, parseDoubleSafe(c[18], null), java.sql.Types.DOUBLE);
            ps.setObject(20, parseDoubleSafe(c[19], null), java.sql.Types.DOUBLE);
            ps.setObject(21, parseDoubleSafe(c[20], null), java.sql.Types.DOUBLE);
            ps.setObject(22, parseDoubleSafe(c[21], null), java.sql.Types.DOUBLE);
            ps.setObject(23, parseDoubleSafe(c[22], null), java.sql.Types.DOUBLE);
            ps.setObject(24, parseDoubleSafe(c[23], null), java.sql.Types.DOUBLE);
            ps.setString(25, nullIfEmpty(c[24]));
            ps.setString(26, nullIfEmpty(c[25]));
            ps.setString(27, nullIfEmpty(c[26]));
            System.out.println(ps.toString());//输出最终生成的SQL语句，用于调试

            result = ps.executeUpdate();

        } catch (SQLException e) {//异常
            e.printStackTrace();
        } finally {//关闭数据库
            closeConnection();
            long endTime = System.currentTimeMillis();
            System.out.println("addOneRecipe operation total time: " + (endTime - startTime) + " ms");
        }
        return result;
    }

    @Override
    public String findRecipeById(int recipeId) {
        long startTime = System.currentTimeMillis();
        getConnection();
        StringBuilder sb = new StringBuilder();
        String sql = "SELECT * FROM recipes WHERE RecipeId = ?";//参数化查询（ RecipeId = ? ）
        //PreparedStatement是Java JDBC API中的一个接口，是Statement的子接口，用于执行预编译的SQL语句
        PreparedStatement statement = null;
        try {
            // 创建PreparedStatement执行查询
            statement = con.prepareStatement(sql);
            statement.setInt(1, recipeId);//设置第一个占位符的位置
            resultSet = statement.executeQuery();

            // 处理查询结果
            if (resultSet.next()) {
                // 构建食谱信息字符串
                sb.append("Recipe ID: " + resultSet.getInt("RecipeId") + "\n");
                sb.append("Name: " + resultSet.getString("Name") + "\n");
                sb.append("Author ID: " + resultSet.getInt("AuthorId") + "\n");
                sb.append("Author Name: " + resultSet.getString("AuthorName") + "\n");
                sb.append("Cook Time: " + resultSet.getString("CookTime") + "\n");
                sb.append("Prep Time: " + resultSet.getString("PrepTime") + "\n");
                sb.append("Total Time: " + resultSet.getString("TotalTime") + "\n");
                sb.append("Description: " + resultSet.getString("Description") + "\n");
                sb.append("Category: " + resultSet.getString("RecipeCategory") + "\n");
            } else {
                sb.append("Recipe not found with ID: " + recipeId);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            // 关闭资源
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            closeConnection();
            long endTime = System.currentTimeMillis();
            System.out.println("findRecipeById operation total time: " + (endTime - startTime) + " ms");
        }

        return sb.toString();
    }

    @Override
    public int updateRecipeRating(int recipeId, double newRating) {
        long startTime = System.currentTimeMillis();
        getConnection();
        int result = 0;
        String sql = "UPDATE recipes SET AggregatedRating = ? WHERE RecipeId = ?";
        PreparedStatement statement = null;

        try {
            statement = con.prepareStatement(sql);
            statement.setDouble(1, newRating);
            statement.setInt(2, recipeId);
            result = statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            closeConnection();
            long endTime = System.currentTimeMillis();
            System.out.println("updateRecipeRating operation total time: " + (endTime - startTime) + " ms");
        }

        return result;
    }
    @Override
    public int deleteRecipeById(int recipeId) {
        long startTime = System.currentTimeMillis();
        getConnection();
        int result = 0;
        String sql = "DELETE FROM recipes WHERE RecipeId = ?";
        PreparedStatement statement = null;

        try {
            statement = con.prepareStatement(sql);
            statement.setInt(1, recipeId);

            result = statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            closeConnection();
            long endTime = System.currentTimeMillis();
            System.out.println("deleteRecipeById operation total time: " + (endTime - startTime) + " ms");
        }

        return result;//返回删除数据的行数
    }

    private String nullIfEmpty(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
    // 转化为int 和 double
    private int parseIntSafe(String s, int def) {
        try { return Integer.parseInt(s.trim()); } catch (Exception e) { return def; }
    }

    private Double parseDoubleSafe(String s, Double def) {
        try { return Double.parseDouble(s.trim()); } catch (Exception e) { return def; }
    }


}
