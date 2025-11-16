import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// 专门用于对比 users 导入性能的类
public class ImportCompare extends ImportDataVersion2 {


    public void importUsersCsvNoBatch(String csvPath) {
        getConnection();

        final String SQL_USER =
                "INSERT INTO users(author_id,author_name,gender,age,followers_count,following_count) " +
                        "VALUES(?,?,?::gender_enum,?,?,?) ON CONFLICT (author_id) DO NOTHING";

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

                psU.executeUpdate();

                pendingUsers++;
                if (pendingUsers % 10000 == 0) System.out.println("[users(no-batch)] done " + pendingUsers);


                for (Integer f : parseIntList(getCell(c, iFollowerUsers))) {
                    if (f != null && f > 0) edges.add(new int[]{f, uid});
                }
                for (Integer fo : parseIntList(getCell(c, iFollowingUsers))) {
                    if (fo != null && fo > 0) edges.add(new int[]{uid, fo});
                }
            }


            con.commit();
            con.setAutoCommit(false);
            int pendingEdges = 0;
            for (int[] e : edges) {
                int follower = e[0], followee = e[1];
                psF.setInt(1, follower);
                psF.setInt(2, followee);
                psF.setInt(3, follower);
                psF.setInt(4, followee);

                psF.executeUpdate();
                pendingEdges++;

                if (pendingEdges % 10000 == 0) System.out.println("[users(no-batch)] done following " + pendingEdges);
            }
            con.commit();

            System.out.println("[users + following][no-batch] import finished. users=" + pendingUsers + ", edges=" + edges.size());
        } catch (Exception e){
            try { if (con != null) con.rollback(); } catch (Exception ignore) {}
            throw new RuntimeException(e);
        } finally { closeConnection(); }
    }


    public void importRecipesCsvNoBatch(String csvPath) {
        getConnection();
        //final int BATCH = 1000;

        final String SQL_ENSURE_USER =
                "INSERT INTO users(author_id, author_name, gender, age) " +
                        "VALUES (?, ?, NULL, NULL) ON CONFLICT (author_id) DO NOTHING";

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
             PreparedStatement psInstr = con.prepareStatement(SQL_INSTRUCTION)) {

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

                String authorName = nullIfEmpty(getCell(c, iAuthorName));
                if (authorName == null) authorName = "user_" + authorId;

                psEnsureUser.setInt(1, authorId);
                psEnsureUser.setString(2, authorName);

                psEnsureUser.executeUpdate();
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

                psR.executeUpdate();


                for (String instrText : splitCList(getCell(c, iInstr))) {
                    if (instrText == null || instrText.isBlank()) continue;
                    psInstr.setInt(1, recipeId);
                    psInstr.setString(2, instrText);

                    psInstr.executeUpdate();
                }

                psT.setInt(1, recipeId);

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
                    psT.setString(5, date);
                }

                psT.executeUpdate();

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

                    psN.executeUpdate();
                    nutritionInserted++;
                } else {
                    nutritionSkipped++;
                }


                for (Integer uid: parseIntList(getCell(c, iFavUsers))){
                    if (uid == null || uid <= 0) continue;
                    psF.setInt(1, uid);
                    psF.setInt(2, recipeId);
                    psF.setInt(3, uid);
                    psF.setInt(4, recipeId);
                    psF.executeUpdate();
                }

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
                            psKwIns.executeUpdate();
                        }
                        kwCache.put(kw, kwId);
                    }
                    psRk.setInt(1, recipeId);
                    psRk.setInt(2, kwId);
                    psRk.executeUpdate();
                }

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
                            // 原来是 psIngIns.addBatch();
                            psIngIns.executeUpdate();
                        }
                        ingCache.put(ing, ingId);
                    }
                    psRi.setInt(1, recipeId);
                    psRi.setInt(2, ingId);

                    psRi.executeUpdate();
                }

                pend++;
                if (pend % 10000 == 0) System.out.println("[recipes(no-batch)] done " + pend);
            }

            con.commit();
            System.out.println("[recipes*][no-batch] finished. nutrition inserted=" + nutritionInserted +
                    ", skipped(no calories)=" + nutritionSkipped);
        } catch (Exception e){
            try { if (con != null) con.rollback(); } catch (Exception ignore) {}
            throw new RuntimeException(e);
        } finally { closeConnection(); }
    }



    public void importReviewsCsvNoBatch(String csvPath){
        getConnection();

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


                int rid = parseIdLoose(getCell(c, iReviewId));
                int rec = parseIdLoose(getCell(c, iRecipeId));
                int uid = parseIdLoose(getCell(c, iAuthorId));
                if (rid <= 0 || rec <= 0 || uid <= 0) continue; // ID 不合法就整行丢弃


                String ratStr = nullIfEmpty(getCell(c, iRating));
                if (ratStr == null) continue;
                int rating = parseIntSafe(ratStr, -1);
                if (rating < 0 || rating > 5) continue;


                String reviewText = nullIfEmpty(getCell(c, iReview));
                if (reviewText == null) reviewText = "";


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


                psR.executeUpdate();

                for (Integer liker : parseIntList(getCell(c, iLikes))) {
                    if (liker == null || liker <= 0) continue;
                    psL.setInt(1, liker);
                    psL.setInt(2, rid);
                    psL.setInt(3, liker);
                    psL.setInt(4, rid);
                    psL.executeUpdate();
                }

                if (++pend % 10000 == 0) {
                    System.out.println("[reviews(no-batch)] done " + pend);
                }
            }

            con.commit();
            System.out.println("[reviews + likes][no-batch] import finished.");
        } catch (Exception e) {
            try { if (con != null) con.rollback(); } catch (Exception ignore) {}
            throw new RuntimeException(e);
        } finally {
            closeConnection();
        }
    }



    public void importUsersCsvWith100Batch(String csvPath) {
        getConnection();
        final int BATCH = 100;

        final String SQL_USER =
                "INSERT INTO users(author_id,author_name,gender,age,followers_count,following_count) " +
                        "VALUES(?,?,?::gender_enum,?,?,?) ON CONFLICT (author_id) DO NOTHING";


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




    // 不使用preparedStatement 及其工具~

    private static String toSqlString(String s) {
        if (s == null) return "NULL";
        return "'" + escapeSqlLiteral(s) + "'";
    }
    private static String toSqlGender(String gender) {
        if (gender == null) return "NULL";
        return "'" + escapeSqlLiteral(gender) + "'::gender_enum";
    }

    private static String toSqlInt(Integer v) {
        if (v == null) return "NULL";
        return String.valueOf(v);
    }


    private static String escapeSqlLiteral(String s) {
        return s.replace("'", "''");
    }



    public void importUsersWithoutPreparedStatement(String csvPath) {
        getConnection();
        final int BATCH = 1000;

        List<int[]> edges = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(csvPath));
             Statement stU = con.createStatement();
             Statement stF = con.createStatement()) {

            con.setAutoCommit(false);

            // 读表头
            String first = readOneCsvRecord(br);
            if (first == null) {
                con.commit();
                return;
            }
            // 处理 UTF-8 BOM
            if (first.length() > 0 && first.charAt(0) == '\uFEFF') {
                first = first.substring(1);
            }
            String[] header = splitCsvRecord(first);
            Map<String, Integer> H = buildIndex(header);

            Integer iAuthorId       = getIdx(H, "AuthorId", "UserId", "author_id");
            Integer iAuthorName     = getIdx(H, "AuthorName", "UserName", "author_name");
            Integer iGender         = getIdx(H, "Gender", "gender");
            Integer iAge            = getIdx(H, "Age", "age");
            Integer iFollowersCount = getIdx(H, "Followers", "followers_count");
            Integer iFollowingCount = getIdx(H, "Following", "following_count");
            Integer iFollowerUsers  = getIdx(H, "FollowerUsers", "FollowersList", "followers_users");
            Integer iFollowingUsers = getIdx(H, "FollowingUsers", "FollowingList", "following_users");

            int pendingUsers = 0;
            String line;
            while ((line = readOneCsvRecord(br)) != null) {
                String[] c = splitCsvRecord(line);

                int uid = parseIntSafe(getCell(c, iAuthorId), 0);
                if (uid <= 0 || uid >= 300000) continue;

                String name   = nullIfEmpty(getCell(c, iAuthorName));
                String gender = nullIfEmpty(getCell(c, iGender));

                Integer age = null;
                String ages = nullIfEmpty(getCell(c, iAge));
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

                String sqlUser =
                        "INSERT INTO users(author_id,author_name,gender,age,followers_count,following_count) VALUES (" +
                                uid + "," +
                                toSqlString(name) + "," +
                                toSqlGender(gender) + "," +
                                toSqlInt(age) + "," +
                                toSqlInt(followersCount) + "," +
                                toSqlInt(followingCount) +
                                ") ON CONFLICT (author_id) DO NOTHING";

                stU.addBatch(sqlUser);

                if (++pendingUsers % BATCH == 0) {
                    stU.executeBatch();
                }
                if (pendingUsers % 10000 == 0) {
                    System.out.println("[users(no-prepared)] done " + pendingUsers);
                }

                for (Integer f : parseIntList(getCell(c, iFollowerUsers))) {
                    if (f != null && f > 0) edges.add(new int[]{f, uid});
                }
                for (Integer fo : parseIntList(getCell(c, iFollowingUsers))) {
                    if (fo != null && fo > 0) edges.add(new int[]{uid, fo});
                }
            }

            stU.executeBatch();
            con.commit();

            con.setAutoCommit(false);
            int pendingEdges = 0;
            for (int[] e : edges) {
                int follower = e[0];
                int followee = e[1];

                String sqlFollow =
                        "INSERT INTO following(follower_id,followee_id) " +
                                "SELECT " + follower + ", " + followee + " " +
                                "WHERE EXISTS (SELECT 1 FROM users u WHERE u.author_id = " + follower + ") " +
                                "  AND EXISTS (SELECT 1 FROM users v WHERE v.author_id = " + followee + ") " +
                                "ON CONFLICT DO NOTHING";

                stF.addBatch(sqlFollow);

                if (++pendingEdges % BATCH == 0) {
                    stF.executeBatch();
                }
                if (pendingEdges % 10000 == 0) {
                    System.out.println("[users(no-prepared)] done following " + pendingEdges);
                }
            }
            stF.executeBatch();
            con.commit();

            System.out.println("[users + following(no-prepared)] import finished. users="
                    + pendingUsers + ", edges=" + edges.size());

        } catch (Exception e) {
            try { if (con != null) con.rollback(); } catch (Exception ignore) {}
            throw new RuntimeException(e);
        } finally {
            closeConnection();
        }
    }

}
