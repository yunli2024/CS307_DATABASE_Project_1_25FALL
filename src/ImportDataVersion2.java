import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.*;
import java.util.*;
import java.util.regex.Pattern;
// 注意对nutrition的处理！
//仅当 calories 非空时才写入 nutrition（其他列可空）；
// 如果这一行完全没有营养数据或只有其他列而 calories 为空，就跳过这条 nutrition 记录（不影响其它表的插入）
public class ImportDataVersion2 {
    public Connection con = null;
    private ResultSet resultSet;
    private String host = "localhost";
    private String dbname = "project1_25fall";
    private String user = "postgres";
    private String pwd = "000000";
    private String port = "5432";

    // 连接与关闭连接
    void getConnection() {
        try { Class.forName("org.postgresql.Driver"); }
        catch (Exception e) {
            System.err.println("Cannot find the PostgreSQL driver. Check CLASSPATH.");
            System.exit(1);
        }
        try {
            String url = "jdbc:postgresql://" + host + ":" + port + "/" + dbname;
            con = DriverManager.getConnection(url, user, pwd);
        } catch (SQLException e) {
            System.err.println("Database connection failed!");
            e.printStackTrace();
            System.exit(1);
        }
    }
    void closeConnection() {
        if (con != null) {
            try { con.close(); con = null; } catch (Exception e) { e.printStackTrace(); }
        }
    }

    // CSV 读取和拆分解析的工具方法
    // readOneCsvRecord 读取一条csv记录
    // 如果遇到了换行符，就删除掉最后的换行，使之成为纯净的记录
    String readOneCsvRecord(BufferedReader br) throws IOException {
        String s = br.readLine();
        if (s == null) return null;
        if (s.endsWith("\r\n")) s = s.substring(0, s.length()-2);
        else if (s.endsWith("\n")) s = s.substring(0, s.length()-1);
        return s;
    }

    // splitCsvRecord将得到的一行String变成一个String[]
    // 将record根据逗号进行拆分处理
    String[] splitCsvRecord(String record) {
        ArrayList<String> out = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean inQuotes = false; // 标志是否在引号内部
        for (int i = 0; i < record.length(); i++) {
            char ch = record.charAt(i);
            if (ch == '"') {
                if (i + 1 < record.length() && record.charAt(i + 1) == '"') {
                    cur.append('"'); i++;
                } else inQuotes = !inQuotes;
            } else if (ch == ',' && !inQuotes) {
                // 注意只有不在引号内部的逗号才可以进行拆分！
                // 不然一个属性内部会被拆成很多，不允许
                out.add(cur.toString()); cur.setLength(0);
            } else cur.append(ch);
        }
        out.add(cur.toString());
        return out.toArray(new String[0]);
    }

    // parseIntSafe 方法安全把字符串转化成int类型
    // 如果出现为空之类的情况或者转化失败，都返回一个def作为默认值
    int parseIntSafe(String s, int def) {
        if (s == null) return def;
        s = s.trim(); if (s.isEmpty()) return def;
        try { return Integer.parseInt(s); } catch (Exception e){ return def; }
    }
    // 类似，安全转化为double类型
    Double parseDoubleSafe(String s, Double def) {
        if (s == null) return def;
        s = s.trim(); if (s.isEmpty()) return def;
        try { return Double.valueOf(s); } catch (Exception e){ return def; }
    }

    // 工具类，如果为空就赋值为null，防止出现冲突
    String nullIfEmpty(String s){
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }


    // sanitizeTimeCell方法检查字符串s是否符合一个时间的格式
    // 以 ISO 8601 duration作为标准，改成interval形式的

    String sanitizeTimeCell(String s) {
        s = nullIfEmpty(s);
        if (s == null) return null;

        s = s.trim();
        if (s.isEmpty() || s.equalsIgnoreCase("none")) return null;
        // 必须以 P 开头
        char first = s.charAt(0);
        if (first != 'P' && first != 'p') {
            return null;
        }

        long weeks = 0;
        long days = 0;
        long hours = 0;
        long minutes = 0;
        long seconds = 0;
        boolean inTime = false;      // 遇到 T 之后是时间部分
        StringBuilder number = new StringBuilder();
        boolean hasUnit = false;     // 是否出现过任何单位
        for (int i = 1; i < s.length(); i++) {
            char ch = s.charAt(i);
            if (ch == 'T' || ch == 't') {
                inTime = true;
                continue;
            }
            if (Character.isDigit(ch)) {
                number.append(ch);
                continue;
            }
            if (number.length() == 0) {
                return null;
            }
            long value;
            try {
                value = Long.parseLong(number.toString());
            } catch (NumberFormatException e) {
                return null;
            }
            number.setLength(0);
            hasUnit = true;
            switch (ch) {
                case 'W': case 'w':
                    weeks += value;
                    break;
                case 'D': case 'd':
                    days += value;
                    break;
                case 'H': case 'h':
                    hours += value;
                    break;
                case 'M': case 'm':
                    if (inTime) {
                        // 时间部分的 M：分钟
                        minutes += value;
                    } else {
                        // 日期部分的 M：月份，这里简单粗暴地按 30 天折算
                        days += value * 30L;
                    }
                    break;
                case 'S': case 's':
                    seconds += value;
                    break;
                default:
                    return null;
            }
        }
        if (!hasUnit) return null;
        long totalSeconds = seconds
                + minutes * 60L
                + hours   * 3600L
                + days    * 86400L
                + weeks   * 7L * 86400L;
        return totalSeconds + " seconds";
    }
    // 类似的，解析日期的方法，只接受XXXX-YY-ZZ 这种日期的格式
    String sanitizeDateCell(String s) {
        s = nullIfEmpty(s);
        if (s == null || s.length() < 10) return null;

        String sub = s.substring(0, 10); // 先取前 10 位
        // 必须是 4位年-2位月-2位日
        if (sub.matches("\\d{4}-\\d{2}-\\d{2}")) {
            return sub;
        }
        return null;
    }


    // 一些工具
    // 表头的映射
    Map<String,Integer> buildIndex(String[] header){
        Map<String,Integer> mp = new HashMap<>();
        for(int i=0;i<header.length;i++) mp.put(header[i].trim(), i);
        return mp;
    }
    Integer getIdx(Map<String, Integer> mp, String... names){
        for(String k: names){ Integer i = mp.get(k); if(i!=null) return i; }
        return null;
    }
    String getCell(String[] row, Integer idx){
        return (idx==null || idx>=row.length) ? null : row[idx];
    }

    private static final String SEP_REGEX = "[,;|、，]";
    private static final Pattern INT_EXTRACT = Pattern.compile("-?\\d+");
    private List<String> parseStringList(String cell){
        ArrayList<String> out = new ArrayList<>();
        if (cell == null) return out;
        String s = cell.trim();
        if (s.isEmpty() || "[]".equals(s)) return out;
        if (s.startsWith("[") && s.endsWith("]")) s = s.substring(1, s.length()-1);
        s = s.replace("\"","").replace("'","");
        for (String p : s.split(SEP_REGEX)){
            String t = p.trim(); if (!t.isEmpty()) out.add(t);
        }
        return out;
    }
    List<Integer> parseIntList(String cell){
        ArrayList<Integer> out = new ArrayList<>();
        if (cell == null) return out;
        var m = INT_EXTRACT.matcher(cell);
        while (m.find()){
            try { out.add(Integer.parseInt(m.group())); } catch (Exception ignore){}
        }
        return out;
    }
    int parseIdLoose(String s){
        if (s == null) return 0;
        s = s.trim();
        if (s.isEmpty()) return 0;
        try { return Integer.parseInt(s); } catch (Exception ignore) {}
        try {
            double d = Double.parseDouble(s);
            if (!Double.isNaN(d) && !Double.isInfinite(d)) {
                return (int) Math.round(d);
            }
        } catch (Exception ignore) {}
        java.util.regex.Matcher m = INT_EXTRACT.matcher(s); // 已有的 Pattern "-?\\d+"
        if (m.find()) {
            try { return Integer.parseInt(m.group()); } catch (Exception ignore) {}
        }
        return 0;
    }

    // 只处理真正的 c("xxx","yyy") 多值字段：
    List<String> splitCList(String cell) {
        List<String> res = new ArrayList<>();
        if (cell == null) return res;

        cell = cell.trim();
        if (cell.isEmpty() || cell.equalsIgnoreCase("NA")) return res;

        // 去掉整格最外层的引号："...." 或 '....'
        if (cell.length() >= 2 &&
                ((cell.charAt(0) == '"'  && cell.charAt(cell.length() - 1) == '"') ||
                        (cell.charAt(0) == '\'' && cell.charAt(cell.length() - 1) == '\''))) {
            cell = cell.substring(1, cell.length() - 1).trim();
            if (cell.isEmpty()) return res;
        }

        // 到这里必须是 c( ... ) 才认为是多值字段
        if (!(cell.startsWith("c(") && cell.endsWith(")"))) {
            // 不是 c(...)，说明这格不是多值属性，比如 FavoriteUsers 之类
            // 直接返回空列表，避免错误拆分出一堆数字
            return res;
        }

        // 括号内部内容
        String inner = cell.substring(2, cell.length() - 1).trim();
        if (inner.isEmpty()) return res;

        StringBuilder sb = new StringBuilder();
        boolean inQuote = false;

        for (int i = 0; i < inner.length(); i++) {
            char ch = inner.charAt(i);
            if (ch == '"') {
                // 处理转义 "" -> 一个引号
                if (inQuote && i + 1 < inner.length() && inner.charAt(i + 1) == '"') {
                    sb.append('"');
                    i++; // 跳过第二个 "
                } else {
                    inQuote = !inQuote; // 进入/退出引号
                }
            } else if (ch == ',' && !inQuote) {
                // 逗号 & 不在引号内 -> 一个元素结束
                String token = sb.toString().trim();
                if (!token.isEmpty()) res.add(token);
                sb.setLength(0);
            } else {
                sb.append(ch);
            }
        }

        String token = sb.toString().trim();
        if (!token.isEmpty()) res.add(token);

        return res;
    }



    /*
    正式进入导入的环节了
    第一个方法 importUsersCsv() 导入users表格以及following关注关系表
     */
    public void importUsersCsv(String csvPath) {
        getConnection();
        final int BATCH = 1000;

        final String SQL_USER =
                "INSERT INTO users(author_id,author_name,gender,age,followers_count,following_count) " +
                        "VALUES(?,?,?::gender_enum,?,?,?) ON CONFLICT (author_id) DO NOTHING";

        // 仅当两端用户都存在时才可以导入关注表格，避免 FK 冲突
        final String SQL_FOLLOW_SAFE =
                "INSERT INTO following(follower_id,followee_id) " +
                        "SELECT ?, ? " +
                        "WHERE EXISTS (SELECT 1 FROM users u WHERE u.author_id = ?) " +
                        "  AND EXISTS (SELECT 1 FROM users v WHERE v.author_id = ?) " +
                        "ON CONFLICT DO NOTHING";

        List<int[]> edges = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(csvPath));
             PreparedStatement psU = con.prepareStatement(SQL_USER);
             PreparedStatement psF = con.prepareStatement(SQL_FOLLOW_SAFE)) {

            con.setAutoCommit(false);

            String first = readOneCsvRecord(br);
            if (first == null) { con.commit(); return; }
            if (first.length() > 0 && first.charAt(0) == '\uFEFF') first = first.substring(1);
            String[] header = splitCsvRecord(first);
            Map<String,Integer> H = buildIndex(header);

            Integer iAuthorId       = getIdx(H, "AuthorId","UserId","author_id");
            Integer iAuthorName     = getIdx(H, "AuthorName","UserName","author_name");
            Integer iGender         = getIdx(H, "Gender","gender");
            Integer iAge            = getIdx(H, "Age","age");
            Integer iFollowersCount  = getIdx(H, "Followers","followers_count");
            Integer iFollowingCount  = getIdx(H, "Following","following_count");
            Integer iFollowerUsers  = getIdx(H, "FollowerUsers","FollowersList","followers_users");
            Integer iFollowingUsers = getIdx(H, "FollowingUsers","FollowingList","following_users");

            int pendingUsers = 0;
            String line;
            while ((line = readOneCsvRecord(br)) != null){
                String[] c = splitCsvRecord(line);

                int uid = parseIntSafe(getCell(c, iAuthorId), 0);
                if (uid <= 0 || uid>=300000) continue; //限制在300000以内


                String name   = nullIfEmpty(getCell(c, iAuthorName));
                String gender = nullIfEmpty(getCell(c, iGender));
                Integer age   = null;
                String ages   = nullIfEmpty(getCell(c, iAge));
                if (ages != null) {
                    int a = parseIntSafe(ages, 0);
                    age = (a == 0 ? null : a);
                }

                Integer followersCount = null;
                String followersStr = nullIfEmpty(getCell(c, iFollowersCount));
                if (followersStr != null) {
                    int v = parseIntSafe(followersStr, -1);
                    if (v >= 0) followersCount = v;
                }


                Integer followingCount = null;
                String followingStr = nullIfEmpty(getCell(c, iFollowingCount));
                if (followingStr != null) {
                    int v = parseIntSafe(followingStr, -1);
                    if (v >= 0) followingCount = v;
                }

                psU.setInt(1, uid);
                psU.setString(2, name);
                if (gender == null) psU.setNull(3, Types.VARCHAR); else psU.setString(3, gender);
                if (age == null)    psU.setNull(4, Types.INTEGER); else psU.setInt(4, age);

                if (followersCount == null) psU.setNull(5, Types.INTEGER);
                else psU.setInt(5, followersCount);

                if (followingCount == null) psU.setNull(6, Types.INTEGER);
                else psU.setInt(6, followingCount);

                psU.addBatch();

                if (++pendingUsers % BATCH == 0) psU.executeBatch();
                if (pendingUsers % 10000 == 0) System.out.println("[users] done " + pendingUsers);

                for (Integer f : parseIntList(getCell(c, iFollowerUsers))) {
                    if (f != null && f > 0) edges.add(new int[]{f, uid});
                }
                for (Integer fo : parseIntList(getCell(c, iFollowingUsers))) {
                    if (fo != null && fo > 0) edges.add(new int[]{uid, fo});
                }
            }
            psU.executeBatch();
            con.commit();
            //插 following
            con.setAutoCommit(false);
            int pendingEdges = 0;
            for (int[] e : edges) {
                int follower = e[0], followee = e[1];
                psF.setInt(1, follower);
                psF.setInt(2, followee);
                psF.setInt(3, follower);
                psF.setInt(4, followee);
                psF.addBatch();
                if (++pendingEdges % BATCH == 0) psF.executeBatch();
                if (pendingEdges % 10000 == 0) System.out.println("[users] done following" + pendingEdges);
            }
            psF.executeBatch();
            con.commit();

            System.out.println("[users + following] import finished. users=" + pendingUsers + ", edges=" + edges.size());
        } catch (Exception e){
            try { if (con != null) con.rollback(); } catch (Exception ignore) {}
            throw new RuntimeException(e);
        } finally { closeConnection(); }
    }

    /*2) 与recipes.csv相关的：recipes, recipe_time, nutrition, recipe_favorite,
       keyword, recipe_keyword, ingredient, recipe_ingredient

     */
    public void importRecipesCsv(String csvPath) {
        getConnection();
        final int BATCH = 1000;

        // 占位确保作者存在
        final String SQL_ENSURE_USER =
                "INSERT INTO users(author_id, author_name, gender, age) " +
                        "VALUES (?, ?, NULL, NULL) ON CONFLICT (author_id) DO NOTHING";

        // recipes：仅当作者已存在时才插入，避免外键报错
        final String SQL_RECIPES_SAFE =
                "INSERT INTO recipes(" +
                        " recipe_id, author_id, recipe_name, description," +
                        " recipe_category, recipe_yield, recipe_servings, aggregated_rating, review_count" +
                        ") " +
                        "SELECT ?,?,?,?,?,?,?,?,? " +
                        "WHERE EXISTS (SELECT 1 FROM users u WHERE u.author_id = ?) " +
                        "ON CONFLICT (recipe_id) DO NOTHING";


        final String SQL_TIME =
                "INSERT INTO recipe_time(" +
                        " recipe_id, prepare_time, cook_time, total_time, date_published" +
                        ") VALUES (?, ?::interval, ?::interval, ?::interval, ?::date) " +
                        "ON CONFLICT (recipe_id) DO NOTHING";



        // 只有 calories 非空才插入 nutrition（避免 NOT NULL 报错）
        final String SQL_NUTRITION =
                "INSERT INTO nutrition(recipe_id, calories, fat, saturated_fat, cholesterol, protein, sugar, fiber, carbohydrate, sodium) " +
                        "VALUES (?,?,?,?,?,?,?,?,?,?) ON CONFLICT (recipe_id) DO NOTHING";

        final String SQL_FAV_SAFE =
                "INSERT INTO recipe_favorite(user_id, recipe_id) " +
                        "SELECT ?, ? " +
                        "WHERE EXISTS (SELECT 1 FROM users WHERE author_id = ?) " +
                        "  AND EXISTS (SELECT 1 FROM recipes WHERE recipe_id = ?) " +
                        "ON CONFLICT DO NOTHING";
        final String SQL_INSTRUCTION =
                "INSERT INTO recipe_instruction(recipe_id, instruction) " +
                        "VALUES (?, ?) ON CONFLICT DO NOTHING";


        final String SQL_KW_SEL       = "SELECT keyword_id FROM keyword WHERE keyword = ?";
        final String SQL_KW_INS_EXPL  = "INSERT INTO keyword(keyword_id, keyword) VALUES (?, ?) ON CONFLICT (keyword) DO NOTHING";
        final String SQL_RK           = "INSERT INTO recipe_keyword(recipe_id, keyword_id) VALUES(?,?) ON CONFLICT DO NOTHING";

        final String SQL_ING_SEL      = "SELECT ingredient_id FROM ingredient WHERE ingredient = ?";
        final String SQL_ING_INS_EXPL = "INSERT INTO ingredient(ingredient_id, ingredient) VALUES (?, ?) ON CONFLICT (ingredient) DO NOTHING";
        final String SQL_RI           = "INSERT INTO recipe_ingredient(recipe_id, ingredient_id) VALUES(?,?) ON CONFLICT DO NOTHING";

        try (BufferedReader br = new BufferedReader(new FileReader(csvPath));
             PreparedStatement psEnsureUser = con.prepareStatement(SQL_ENSURE_USER);
             PreparedStatement psR  = con.prepareStatement(SQL_RECIPES_SAFE);
             PreparedStatement psT  = con.prepareStatement(SQL_TIME);
             PreparedStatement psN  = con.prepareStatement(SQL_NUTRITION);
             PreparedStatement psF  = con.prepareStatement(SQL_FAV_SAFE);
             PreparedStatement psKwSel = con.prepareStatement(SQL_KW_SEL);
             PreparedStatement psKwIns = con.prepareStatement(SQL_KW_INS_EXPL);
             PreparedStatement psRk = con.prepareStatement(SQL_RK);
             PreparedStatement psIngSel = con.prepareStatement(SQL_ING_SEL);
             PreparedStatement psIngIns = con.prepareStatement(SQL_ING_INS_EXPL);
             PreparedStatement psRi = con.prepareStatement(SQL_RI);
             PreparedStatement psInstr = con.prepareStatement(SQL_INSTRUCTION)){

            con.setAutoCommit(false);

            String first = readOneCsvRecord(br);
            if (first == null) { con.commit(); return; }
            if (first.length() > 0 && first.charAt(0) == '\uFEFF') first = first.substring(1);
            String[] header = splitCsvRecord(first);
            Map<String,Integer> H = buildIndex(header);

            Integer iRecipeId   = getIdx(H, "RecipeId","recipe_id");
            Integer iName       = getIdx(H, "Name","RecipeName","recipe_name");
            Integer iAuthorId   = getIdx(H, "AuthorId","author_id");
            Integer iAuthorName = getIdx(H, "AuthorName","author_name");   // 可能不存在，空则兜底
            Integer iDesc       = getIdx(H, "Description","description");
            Integer iInstr      = getIdx(H, "RecipeInstructions","Instructions","recipe_instructions");
            Integer iCat        = getIdx(H, "RecipeCategory","Category","recipe_category");
            Integer iYield      = getIdx(H, "RecipeYield","Yield","recipe_yield");
            Integer iServ       = getIdx(H, "RecipeServings","Servings","recipe_servings");
            Integer iRating     = getIdx(H, "AggregatedRating","Rating","aggregated_rating");
            Integer iReviewCnt  = getIdx(H, "ReviewCount","review_count");
            Integer iPrep       = getIdx(H, "PrepTime","prepare_time");
            Integer iCook       = getIdx(H, "CookTime","cook_time");
            Integer iDate       = getIdx(H, "DatePublished","date_published");
            Integer iTotal      = getIdx(H, "TotalTime","total_time");

            Integer iCalories   = getIdx(H, "Calories","calories");
            Integer iFat        = getIdx(H, "FatContent","fat");
            Integer iSat        = getIdx(H, "SaturatedFatContent","saturated_fat");
            Integer iChol       = getIdx(H, "CholesterolContent","cholesterol");
            Integer iProtein    = getIdx(H, "ProteinContent","protein");
            Integer iSugar      = getIdx(H, "SugarContent","sugar");
            Integer iFiber      = getIdx(H, "FiberContent","fiber");
            Integer iCarb       = getIdx(H, "CarbohydrateContent","carbohydrate");
            Integer iSodium     = getIdx(H, "SodiumContent","sodium");

            Integer iKeywords   = getIdx(H, "Keywords","keywords");
            Integer iIngredients= getIdx(H, "RecipeIngredientParts","Ingredients","ingredients");
            Integer iFavUsers   = getIdx(H, "FavoriteUsers","favorite_users");

            Map<String,Integer> kwCache = new HashMap<>();
            Map<String,Integer> ingCache = new HashMap<>();
            int kwNextId = 0, ingNextId = 0;
            try (Statement st = con.createStatement()) {
                try (ResultSet rs = st.executeQuery("SELECT COALESCE(MAX(keyword_id),0) FROM keyword")) {
                    if (rs.next()) kwNextId = rs.getInt(1);
                }
                try (ResultSet rs = st.executeQuery("SELECT COALESCE(MAX(ingredient_id),0) FROM ingredient")) {
                    if (rs.next()) ingNextId = rs.getInt(1);
                }
            }

            int pend = 0;
            long nutritionInserted=0, nutritionSkipped=0;
            String line;
            while ((line = readOneCsvRecord(br)) != null) {
                String[] c = splitCsvRecord(line);

                int recipeId = parseIdLoose(getCell(c, iRecipeId));
                int authorId = parseIdLoose(getCell(c, iAuthorId));
                if (recipeId <= 0 || recipeId > 600000 || authorId <= 0) continue;
                // 这里直接把不合理的数据全部过滤掉了

                // —— 关键修复：author_name 兜底，保证非空 —— //
                String authorName = nullIfEmpty(getCell(c, iAuthorName));
                if (authorName == null) authorName = "user_" + authorId;

                // 先占位确保作者存在（不会破坏已存在记录）
                psEnsureUser.setInt(1, authorId);
                psEnsureUser.setString(2, authorName);
                psEnsureUser.addBatch();

                // recipes（安全插入）
                psR.setInt(1, recipeId);
                psR.setInt(2, authorId);
                psR.setString(3, nullIfEmpty(getCell(c, iName)));
                psR.setString(4, nullIfEmpty(getCell(c, iDesc)));
                psR.setString(5, nullIfEmpty(getCell(c, iCat)));
                psR.setString(6, nullIfEmpty(getCell(c, iYield)));

                Integer serv = parseIntSafe(getCell(c, iServ), 0);
                if (serv == 0) psR.setNull(7, Types.INTEGER); else psR.setInt(7, serv);

                Double rating = parseDoubleSafe(getCell(c, iRating), null);
                if (rating == null) psR.setNull(8, Types.NUMERIC); else psR.setObject(8, rating, Types.NUMERIC);

                String rc = nullIfEmpty(getCell(c, iReviewCnt));
                if (rc == null) psR.setNull(9, Types.INTEGER); else psR.setInt(9, parseIdLoose(rc));

                psR.setInt(10, authorId); // EXISTS(users)
                psR.addBatch();

                // recipe_instruction：把整条指令当成一条记录插进去

                // recipe_instruction：多值 c("step1","step2",...) 拆成多条记录
                for (String instrText : splitCList(getCell(c, iInstr))) {
                    if (instrText == null || instrText.isBlank()) continue;
                    psInstr.setInt(1, recipeId);
                    psInstr.setString(2, instrText);
                    psInstr.addBatch();
                }





                // recipe_time
                psT.setInt(1, recipeId);

// 先过滤一下，明显不是时间的内容全部当成 null
                String prep  = sanitizeTimeCell(getCell(c, iPrep));
                String cook  = sanitizeTimeCell(getCell(c, iCook));
                String total = sanitizeTimeCell(getCell(c, iTotal));
                String date  = sanitizeDateCell(getCell(c, iDate));

                if (prep  == null) psT.setNull(2, java.sql.Types.OTHER); else psT.setString(2, prep);
                if (cook  == null) psT.setNull(3, java.sql.Types.OTHER); else psT.setString(3, cook);
                if (total == null) psT.setNull(4, java.sql.Types.OTHER); else psT.setString(4, total);

                if (date == null || date.length() < 10) {
                    psT.setNull(5, java.sql.Types.DATE);
                } else {
                   // date = date.substring(0, 10);
                    psT.setString(5, date);
                }
                psT.addBatch();



                // nutrition（仅当 calories 非空）
                Double calories = parseDoubleSafe(getCell(c, iCalories), null);
                Double fat      = parseDoubleSafe(getCell(c, iFat), null);
                Double sat      = parseDoubleSafe(getCell(c, iSat), null);
                Double chol     = parseDoubleSafe(getCell(c, iChol), null);
                Double protein  = parseDoubleSafe(getCell(c, iProtein), null);
                Double sugar    = parseDoubleSafe(getCell(c, iSugar), null);
                Double fiber    = parseDoubleSafe(getCell(c, iFiber), null);
                Double carb     = parseDoubleSafe(getCell(c, iCarb), null);
                Double sodium   = parseDoubleSafe(getCell(c, iSodium), null);

                if (calories != null) {
                    psN.setInt(1, recipeId);
                    psN.setObject(2, calories, java.sql.Types.NUMERIC);
                    if (fat     ==null) psN.setNull(3,  java.sql.Types.NUMERIC); else psN.setObject(3,  fat,     java.sql.Types.NUMERIC);
                    if (sat     ==null) psN.setNull(4,  java.sql.Types.NUMERIC); else psN.setObject(4,  sat,     java.sql.Types.NUMERIC);
                    if (chol    ==null) psN.setNull(5,  java.sql.Types.NUMERIC); else psN.setObject(5,  chol,    java.sql.Types.NUMERIC);
                    if (protein ==null) psN.setNull(6,  java.sql.Types.NUMERIC); else psN.setObject(6,  protein, java.sql.Types.NUMERIC);
                    if (sugar   ==null) psN.setNull(7,  java.sql.Types.NUMERIC); else psN.setObject(7,  sugar,   java.sql.Types.NUMERIC);
                    if (fiber   ==null) psN.setNull(8,  java.sql.Types.NUMERIC); else psN.setObject(8,  fiber,   java.sql.Types.NUMERIC);
                    if (carb    ==null) psN.setNull(9,  java.sql.Types.NUMERIC); else psN.setObject(9,  carb,    java.sql.Types.NUMERIC);
                    if (sodium  ==null) psN.setNull(10, java.sql.Types.NUMERIC); else psN.setObject(10, sodium,  java.sql.Types.NUMERIC);
                    psN.addBatch();
                    nutritionInserted++;
                } else {
                    nutritionSkipped++;
                }

                // favorites（安全插入）
                for (Integer uid: parseIntList(getCell(c, iFavUsers))){
                    if (uid == null || uid <= 0) continue;
                    psF.setInt(1, uid);
                    psF.setInt(2, recipeId);
                    psF.setInt(3, uid);
                    psF.setInt(4, recipeId);
                    psF.addBatch();
                }

                // keyword & recipe_keyword —— 显式分配 id

                // keyword & recipe_keyword —— 显式分配 id
                for (String kw : splitCList(getCell(c, iKeywords))) {
                    if (kw == null || kw.isBlank()) continue;
                    Integer kwId = kwCache.get(kw);
                    if (kwId == null) {
                        psKwSel.setString(1, kw);
                        try (ResultSet r = psKwSel.executeQuery()) {
                            if (r.next()) kwId = r.getInt(1);
                        }
                        if (kwId == null) {
                            kwId = ++kwNextId;
                            psKwIns.setInt(1, kwId);
                            psKwIns.setString(2, kw);
                            psKwIns.addBatch();
                        }
                        kwCache.put(kw, kwId);
                    }
                    psRk.setInt(1, recipeId);
                    psRk.setInt(2, kwId);
                    psRk.addBatch();
                }


                // ingredient & recipe_ingredient —— 显式分配 id
                // ingredient & recipe_ingredient —— 显式分配 id
                for (String ing : splitCList(getCell(c, iIngredients))) {
                    if (ing == null || ing.isBlank()) continue;
                    Integer ingId = ingCache.get(ing);
                    if (ingId == null) {
                        psIngSel.setString(1, ing);
                        try (ResultSet r = psIngSel.executeQuery()) {
                            if (r.next()) ingId = r.getInt(1);
                        }
                        if (ingId == null) {
                            ingId = ++ingNextId;
                            psIngIns.setInt(1, ingId);
                            psIngIns.setString(2, ing);
                            psIngIns.addBatch();
                        }
                        ingCache.put(ing, ingId);
                    }
                    psRi.setInt(1, recipeId);
                    psRi.setInt(2, ingId);
                    psRi.addBatch();
                }


                if (++pend % BATCH == 0) {
                    psEnsureUser.executeBatch(); // 先确保 users
                    psR.executeBatch();          // 再 recipes
                    psT.executeBatch(); psN.executeBatch();
                    psKwIns.executeBatch(); psIngIns.executeBatch();
                    psF.executeBatch(); psRk.executeBatch(); psRi.executeBatch();
                    psInstr.executeBatch();
                }
                if (pend % 10000 == 0) System.out.println("[recipes] done " + pend);
            }

            psEnsureUser.executeBatch();
            psR.executeBatch();
            psT.executeBatch(); psN.executeBatch();
            psKwIns.executeBatch(); psIngIns.executeBatch();
            psF.executeBatch(); psRk.executeBatch(); psRi.executeBatch();
            psInstr.executeBatch();

            con.commit();
            System.out.println("[recipes*] finished. nutrition inserted=" + nutritionInserted +
                    ", skipped(no calories)=" + nutritionSkipped);
        } catch (Exception e){
            try { if (con != null) con.rollback(); } catch (Exception ignore) {}
            throw new RuntimeException(e);
        } finally { closeConnection(); }
    }


    // 3) reviews & likes_relationship
    public void importReviewsCsv(String csvPath){
        getConnection();
        final int BATCH = 1000;

        final String SQL_REVIEW =
                "INSERT INTO reviews(review_id, recipe_id, user_id, rating, review, date_submitted, date_modified) " +
                        "VALUES (?,?,?,?,?,?::date,?::date) ON CONFLICT (review_id) DO NOTHING";

        final String SQL_LIKE_SAFE =
                "INSERT INTO likes_relationship(user_id, review_id) " +
                        "SELECT ?, ? " +
                        "WHERE EXISTS (SELECT 1 FROM users   WHERE author_id = ?) " +
                        "  AND EXISTS (SELECT 1 FROM reviews WHERE review_id = ?) " +
                        "ON CONFLICT DO NOTHING";

        try (BufferedReader br = new BufferedReader(new FileReader(csvPath));
             PreparedStatement psR = con.prepareStatement(SQL_REVIEW);
             PreparedStatement psL = con.prepareStatement(SQL_LIKE_SAFE)) {

            con.setAutoCommit(false);

            // 读表头并做字段映射
            String first = readOneCsvRecord(br);
            if (first == null) { con.commit(); return; }
            if (first.length() > 0 && first.charAt(0) == '\uFEFF') first = first.substring(1);
            String[] header = splitCsvRecord(first);
            Map<String,Integer> H = buildIndex(header);

            Integer iReviewId = getIdx(H, "ReviewId","review_id");
            Integer iRecipeId = getIdx(H, "RecipeId","recipe_id");
            Integer iAuthorId = getIdx(H, "AuthorId","UserId","author_id","user_id");
            Integer iRating   = getIdx(H, "Rating","rating");
            Integer iReview   = getIdx(H, "Review","review");
            Integer iDateSub  = getIdx(H, "DateSubmitted","date_submitted");
            Integer iDateMod  = getIdx(H, "DateModified","date_modified");
            Integer iLikes    = getIdx(H, "Likes","likes");

            int pend = 0;
            String line;

            while ((line = readOneCsvRecord(br)) != null){
                String[] c = splitCsvRecord(line);

                // 1) 解析主键和外键
                int rid = parseIdLoose(getCell(c, iReviewId));
                int rec = parseIdLoose(getCell(c, iRecipeId));
                int uid = parseIdLoose(getCell(c, iAuthorId));
                if (rid <= 0 || rec <= 0 || uid <= 0) continue; // ID 不合法就整行丢弃

                // 2) rating 必须存在且在 [0,5] 之间，否则整行丢弃
                String ratStr = nullIfEmpty(getCell(c, iRating));
                if (ratStr == null) continue;       // rating 为空，跳过这一条
                int rating = parseIntSafe(ratStr, -1);
                if (rating < 0 || rating > 5) continue; // 异常数字（比如你截图里的 110721），直接跳过

                // 3) review 文本，允许空但是不能为 null（表里是 NOT NULL）
                String reviewText = nullIfEmpty(getCell(c, iReview));
                if (reviewText == null) reviewText = "";

                // 4) 处理日期，只接受 YYYY-MM-DD，其他全部当作 null
                String ds = sanitizeDateCell(getCell(c, iDateSub));
                String dm = sanitizeDateCell(getCell(c, iDateMod));

                psR.setInt(1, rid);
                psR.setInt(2, rec);
                psR.setInt(3, uid);
                psR.setInt(4, rating);
                psR.setString(5, reviewText);

                if (ds == null) psR.setNull(6, Types.VARCHAR);
                else            psR.setString(6, ds);

                if (dm == null) psR.setNull(7, Types.VARCHAR);
                else            psR.setString(7, dm);

                psR.addBatch();

                // 5) 处理 Likes 列
                for (Integer liker : parseIntList(getCell(c, iLikes))) {
                    if (liker == null || liker <= 0) continue;
                    psL.setInt(1, liker);
                    psL.setInt(2, rid);
                    psL.setInt(3, liker);
                    psL.setInt(4, rid);
                    psL.addBatch();
                }

                if (++pend % BATCH == 0) {
                    psR.executeBatch();
                    psL.executeBatch();
                }
                if (pend % 10000 == 0) {
                    System.out.println("[reviews] done " + pend);
                }
            }

            psR.executeBatch();
            psL.executeBatch();
            con.commit();
            System.out.println("[reviews + likes] import finished.");
        } catch (Exception e) {
            try { if (con != null) con.rollback(); } catch (Exception ignore) {}
            throw new RuntimeException(e);
        } finally {
            closeConnection();
        }
    }

}
