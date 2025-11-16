import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.*;
import java.util.*;

public class Optimization extends ImportDataVersion2 {
    public void importUsersCsvWithHashSet(String csvPath) {
        getConnection();
        final int BATCH = 1000;

        final String SQL_USER =
                "INSERT INTO users(author_id,author_name,gender,age,followers_count,following_count) " +
                        "VALUES(?,?,?::gender_enum,?,?,?) ON CONFLICT (author_id) DO NOTHING";

        // 这里就使用简单插入，是否存在用哈希表检查
        final String SQL_FOLLOW =
                "INSERT INTO following(follower_id,followee_id) " +
                        "VALUES (?, ?) ON CONFLICT DO NOTHING";

        List<int[]> edges = new ArrayList<>(2_500_000);

        try (BufferedReader br = new BufferedReader(new FileReader(csvPath));
             PreparedStatement psU = con.prepareStatement(SQL_USER);
             PreparedStatement psF = con.prepareStatement(SQL_FOLLOW)) {

            con.setAutoCommit(false);

            String first = readOneCsvRecord(br);
            if (first == null) { con.commit(); return; }
            if (first.length() > 0 && first.charAt(0) == '\uFEFF') first = first.substring(1);
            String[] header = splitCsvRecord(first);
            Map<String,Integer> H = buildIndex(header);

            Integer iAuthorId        = getIdx(H, "AuthorId","UserId","author_id");
            Integer iAuthorName      = getIdx(H, "AuthorName","UserName","author_name");
            Integer iGender          = getIdx(H, "Gender","gender");
            Integer iAge             = getIdx(H, "Age","age");
            Integer iFollowersCount  = getIdx(H, "Followers","followers_count");
            Integer iFollowingCount  = getIdx(H, "Following","following_count");
            Integer iFollowerUsers   = getIdx(H, "FollowerUsers","FollowersList","followers_users");
            Integer iFollowingUsers  = getIdx(H, "FollowingUsers","FollowingList","following_users");

            int pendingUsers = 0;
            String line;
            while ((line = readOneCsvRecord(br)) != null){
                String[] c = splitCsvRecord(line);

                int uid = parseIntSafe(getCell(c, iAuthorId), 0);
                if (uid <= 0 || uid >= 300000) continue; // 保留你原来的过滤条件

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

            System.out.println("[users] import finished. users=" + pendingUsers +
                    ", raw edges=" + edges.size());

            if (edges.isEmpty()) {
                System.out.println("[following] no edges to insert.");
                return;
            }

            con.setAutoCommit(false);

            // 手机所有id并且存入HashSet里面
            Set<Integer> userOk = new HashSet<>(350_000, 0.75f);
            try (Statement st = con.createStatement();
                 ResultSet rs = st.executeQuery("SELECT author_id FROM users")) {
                while (rs.next()) {
                    userOk.add(rs.getInt(1));
                }
            }
            System.out.println("[following] preload users from DB = " + userOk.size());

            // 使用hashSet去重
            Set<Long> seenEdges = new HashSet<>(2_500_000, 0.75f);
            int pendingEdges = 0;
            int uniqueEdges = 0;

            for (int[] e : edges) {
                int follower = e[0];
                int followee = e[1];

                // 如果没有contains就直接跳过了，起到筛选的作用
                if (!userOk.contains(follower) || !userOk.contains(followee)) {
                    continue;
                }

                // 避免哈希冲突的神秘小手段
                long key = (((long) follower) << 32) | (followee & 0xffffffffL);
                if (!seenEdges.add(key)) {
                    continue;
                }

                psF.setInt(1, follower);
                psF.setInt(2, followee);
                psF.addBatch();
                uniqueEdges++;

                if (++pendingEdges % BATCH == 0) psF.executeBatch();
                if (pendingEdges % 100000 == 0)
                    System.out.println("[users] done following " + pendingEdges);
            }
            psF.executeBatch();
            con.commit();

            System.out.println("[users + following] import finished. users=" + pendingUsers +
                    ", edges(insertedUnique)=" + uniqueEdges +
                    ", edges(raw)=" + edges.size());
        } catch (Exception e){
            try { if (con != null) con.rollback(); } catch (Exception ignore) {}
            throw new RuntimeException(e);
        } finally { closeConnection(); }
    }
}
