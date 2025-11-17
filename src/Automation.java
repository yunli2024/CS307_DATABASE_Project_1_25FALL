import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class Automation extends ImportDataVersion2{
    private String host   = "localhost";
    private String dbname = "project1_auto";
    private String user   = "postgres";
    private String pwd    = "000000";
    private String port   = "5432";

    // 重写父类的 getConnection，因为用做实验的dbname改掉了
    @Override
    void getConnection() {
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
            e.printStackTrace();
            System.exit(1);
        }
    }
    public static void main(String[] args) {
        if (args.length < 3) {
            System.err.println("path or file name error~");
            return;
        }

        String usersCsv   = args[0];
        String recipesCsv = args[1];
        String reviewsCsv = args[2];
        String ddlPath = "SQL/create_table_ddl_statements.sql";
        ImportDataVersion2 importer = new ImportDataVersion2();
        Automation auto=new Automation();
        try {
            auto.getConnection();
            auto.runSchemaFromFile(ddlPath);
            auto.importUsersCsv(usersCsv);
            auto.importRecipesCsv(recipesCsv);
            auto.importReviewsCsv(reviewsCsv);
            System.out.println("successful!");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (importer.con != null && !importer.con.isClosed()) {
                    importer.con.close();
                }
            } catch (SQLException ignore) {}
        }
    }



    public void runSchemaFromFile(String ddlFilePath) {
        getConnection();

        String sqlText;
        try {
            sqlText = new String(Files.readAllBytes(Paths.get(ddlFilePath)));
        } catch (IOException e) {
            throw new RuntimeException("Failed to read DDL file: " + ddlFilePath, e);
        }
        StringBuilder sb = new StringBuilder();
        for (String line : sqlText.split("\n")) {
            String trimmed = line.trim();
            if (trimmed.startsWith("--") || trimmed.isEmpty()) continue;
            sb.append(line).append('\n');
        }

        String cleaned = sb.toString();
        String[] statements = cleaned.split(";");
        try (Statement st = con.createStatement()) {
            for (String s : statements) {
                String sql = s.trim();
                if (sql.isEmpty()) continue;

                try {
                    st.executeUpdate(sql);
                } catch (SQLException ex) {
                    throw ex;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to execute schema DDL", e);
        }
    }

}
