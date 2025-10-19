import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.sql.*;
/*
用于将csv数据导入指定的数据库
 */
public class DatabaseImport {

    // 这里的我的数据库的信息，可能需要更改成你的
    private Connection con = null;
    private ResultSet resultSet;
    private String host = "localhost";
    private String dbname = "project1_25fall";
    private String user = "postgres";
    private String pwd = "000000";
    private String port = "5432";


    // 连接和断开
    private void getConnection() {
        try {
            Class.forName("org.postgresql.Driver");
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
    private void closeConnection() {
        if (con != null) {
            try {
                con.close();
                con = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // 导入recipes.csv 到数据库
    public void importRecipesCsv(String csvPath) {
        getConnection();
        final int BATCH = 1000; // 分块处理
        String sql = "INSERT INTO recipes (RecipeId,Name,AuthorId,AuthorName," +
                "CookTime,PrepTime,TotalTime,DatePublished,Description,RecipeCategory," +
                "Keywords,RecipeIngredientParts,AggregatedRating,ReviewCount,Calories,FatContent," +
                "SaturatedFatContent,CholesterolContent,SodiumContent,CarbohydrateContent,FiberContent," +
                "SugarContent,ProteinContent,RecipeServings,RecipeYield,RecipeInstructions,FavoriteUsers) " +
                "VALUES (?,?,?,?,?,?,?,(?::timestamptz AT TIME ZONE 'UTC'), ?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

        try (BufferedReader br = new BufferedReader(new FileReader(csvPath));
             PreparedStatement ps = con.prepareStatement(sql)) {

            con.setAutoCommit(false);
            boolean first = true;
            int pending = 0;
            String record;

            while ((record = readOneCsvRecord(br)) != null) {
                if (first && record.length() > 0 && record.charAt(0) == '\uFEFF') {
                    record = record.substring(1);
                }
                String[] c = splitCsvRecord(record);
                if (first) {
                    first = false;
                    if (c.length == 0) continue;
                    try { Integer.parseInt(c[0].trim()); }
                    catch (Exception e) { continue; }
                }
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
                ps.addBatch();
                if (++pending % BATCH == 0) ps.executeBatch();
                if (pending % 10000 == 0) {
                    System.out.println("done "+pending+" lines...");
                    ps.executeBatch();
                }
            }
            if (pending % BATCH != 0) ps.executeBatch();
            con.commit();
            System.out.println("finish! total "+pending);
        } catch (SQLException|IOException e) {
            throw new RuntimeException(e);
        }
        finally {
            closeConnection();
        }
    }

    public void importReviewsCsv(String csvPath){
        // todo 完成这两个的设计和导入
    }

    public void importUserCsv(String csvPath){

    }


    // 辅助函数
    // 如果是空则设置为null值
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


    // 这里需要读取csv还是挺坑的
    // 因为一条有27个属性，所以不能简单使用逗号隔开trim(',')，需要解决一条数据行内换行的问题
    private String readOneCsvRecord(Reader r) throws IOException {
        StringBuilder rec = new StringBuilder();
        boolean inQuotes = false;
        int c;
        while ((c = r.read()) != -1) {
            char ch = (char) c;
            rec.append(ch);
            if (ch == '"') {
                r.mark(1);
                int nc = r.read();
                if (nc == '"') {
                    rec.append('"');
                } else {
                    inQuotes = !inQuotes;
                    if (nc != -1) r.reset();
                }
            } else if ((ch == '\n') && !inQuotes) {
                break;
            }
        }
        String s = rec.toString();
        if (s.isEmpty()) return null;
        // 去掉末尾的 \r\n 或 \n
        if (s.endsWith("\r\n")) s = s.substring(0, s.length()-2);
        else if (s.endsWith("\n")) s = s.substring(0, s.length()-1);
        return s;
    }

    // 把“完整记录字符串”拆成字段数组
    private String[] splitCsvRecord(String record) {
        java.util.ArrayList<String> out = new java.util.ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < record.length(); i++) {
            char ch = record.charAt(i);
            if (ch == '"') {
                // 看是否为转义 ""
                if (i + 1 < record.length() && record.charAt(i + 1) == '"') {
                    cur.append('"'); i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (ch == ',' && !inQuotes) {
                out.add(cur.toString());
                cur.setLength(0);
            } else {
                cur.append(ch);
            }
        }
        out.add(cur.toString());
        return out.toArray(new String[0]);
    }

    /*
    private String[] parseCsvLine(String line) {
        if (line == null) return new String[0];
        java.util.ArrayList<String> out = new java.util.ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);
            if (inQuotes) {
                if (ch == '"') {
                    if (i + 1 < line.length() && line.charAt(i + 1) == '"') { // 转义 ""
                        cur.append('"'); i++;
                    } else inQuotes = false;
                } else cur.append(ch);
            } else {
                if (ch == '"') inQuotes = true;
                else if (ch == ',') { out.add(cur.toString()); cur.setLength(0); }
                else cur.append(ch);
            }
        }
        out.add(cur.toString());
        return out.toArray(new String[0]);
    }

    public int addOneRecipe(String str) {
        getConnection();
        int result = 0;
        String sql = "INSERT INTO recipes (\n" +
                "  RecipeId, Name, AuthorId, AuthorName,\n" +
                "  CookTime, PrepTime, TotalTime,\n" +
                "  DatePublished,\n" +
                "  Description, RecipeCategory,\n" +
                "  Keywords, RecipeIngredientParts,\n" +
                "  AggregatedRating, ReviewCount, Calories, FatContent,\n" +
                "  SaturatedFatContent, CholesterolContent, SodiumContent,\n" +
                "  CarbohydrateContent, FiberContent, SugarContent, ProteinContent,\n" +
                "  RecipeServings, RecipeYield, RecipeInstructions, FavoriteUsers\n" +
                ") VALUES (\n" +
                "  ?, ?, ?, ?,\n" +
                "  (?::interval), (?::interval), (?::interval),\n" +
                "  (?::timestamptz AT TIME ZONE 'UTC'),\n" +
                "  ?, ?,\n" +
                "  ?, ?,\n" +
                "  ?, ?, ?, ?,\n" +
                "  ?, ?, ?,\n" +
                "  ?, ?, ?, ?,\n" +
                "  ?, ?, ?, ?\n" +
                ");\n";
        String[] recipeInfo = str.split(",", -1); // -1 保留空字段
        if (recipeInfo.length < 27) {
            System.err.println("CSV列不足27列:" + recipeInfo.length);
            return 0;
        }
        try {
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, parseIntSafe(recipeInfo[0], 0));          // RecipeId
            ps.setString(2, nullIfEmpty(recipeInfo[1]));           // Name
            ps.setInt(3, parseIntSafe(recipeInfo[2], 0));          // AuthorId
            ps.setString(4, nullIfEmpty(recipeInfo[3]));           // AuthorName
            ps.setString(5, nullIfEmpty(recipeInfo[4]));           // CookTime
            ps.setString(6, nullIfEmpty(recipeInfo[5]));           // PrepTime
            ps.setString(7, nullIfEmpty(recipeInfo[6]));           // TotalTime
            ps.setString(8, nullIfEmpty(recipeInfo[7]));           // DatePublished
            ps.setString(9, nullIfEmpty(recipeInfo[8]));           // Description
            ps.setString(10, nullIfEmpty(recipeInfo[9]));          // RecipeCategory
            ps.setString(11, nullIfEmpty(recipeInfo[10]));         // Keywords
            ps.setString(12, nullIfEmpty(recipeInfo[11]));         // RecipeIngredientParts
            ps.setObject(13, parseDoubleSafe(recipeInfo[12], null), java.sql.Types.DOUBLE); // AggregatedRating
            ps.setObject(14, parseDoubleSafe(recipeInfo[13], null), java.sql.Types.DOUBLE); // ReviewCount
            ps.setObject(15, parseDoubleSafe(recipeInfo[14], null), java.sql.Types.DOUBLE); // Calories
            ps.setObject(16, parseDoubleSafe(recipeInfo[15], null), java.sql.Types.DOUBLE); // FatContent
            ps.setObject(17, parseDoubleSafe(recipeInfo[16], null), java.sql.Types.DOUBLE); // SaturatedFatContent
            ps.setObject(18, parseDoubleSafe(recipeInfo[17], null), java.sql.Types.DOUBLE); // CholesterolContent
            ps.setObject(19, parseDoubleSafe(recipeInfo[18], null), java.sql.Types.DOUBLE); // SodiumContent
            ps.setObject(20, parseDoubleSafe(recipeInfo[19], null), java.sql.Types.DOUBLE); // CarbohydrateContent
            ps.setObject(21, parseDoubleSafe(recipeInfo[20], null), java.sql.Types.DOUBLE); // FiberContent
            ps.setObject(22, parseDoubleSafe(recipeInfo[21], null), java.sql.Types.DOUBLE); // SugarContent
            ps.setObject(23, parseDoubleSafe(recipeInfo[22], null), java.sql.Types.DOUBLE); // ProteinContent
            ps.setObject(24, parseDoubleSafe(recipeInfo[23], null), java.sql.Types.DOUBLE); // RecipeServings
            ps.setString(25, nullIfEmpty(recipeInfo[24]));          // RecipeYield
            ps.setString(26, nullIfEmpty(recipeInfo[25]));          // RecipeInstructions
            ps.setString(27, nullIfEmpty(recipeInfo[26]));          // FavoriteUsers
            result = ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeConnection();
        }
        return result;
    }
*/

}



