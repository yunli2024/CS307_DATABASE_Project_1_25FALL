# CS307_task4_advanced2

Performance Comparison Between PostgreSQL and MySQL

### 1. Experiment Environment

- **MySQL Version**: 8.0.44
- **MySQL JDBC Driver**: mysql-connector-j-9.5.0.jar
- Other environmental settings are consistent with the basic part of Task 4

### 2. Data & Schema

**Test Data Source**:

For this experiment, the dataset `user.csv` is used, which contains about 300,000 rows of user data, provided by the course project. The `user.csv` file includes the following fields:

- `AuthorId`: Unique user identifier
- `AuthorName`: User's name
- `Gender`, `Age`: Basic attributes
- `Followers`, `Following`: User relationships
- `FollowerUsers`, `FollowingUsers` (ignored in this experiment)

**Table Creation (Cross-database Compatible Version)**:

```javascript
CREATE TABLE users (
    AuthorId    INT PRIMARY KEY,
    AuthorName  VARCHAR(100),
    Gender      VARCHAR(10),
    Age         INT,
    Followers   INT,
    Following   INT
);
```

### 3. Experiment Design

This experiment performs a comprehensive comparison of the two databases based on the following metrics:

- **Metrics**:
   1. Single INSERT
   2. Batch INSERT
   3. UPDATE
   4. Range Query (WHERE age BETWEEN)
   5. DELETE
- **Timing Method**:

   `System.nanoTime()` in Java is used for timing each operation.

- **Repetitions**:

   Each test is repeated 5 times.

   The first few runs are affected by JVM JIT compilation, cache misses, etc., commonly known as "cold start" factors. To mitigate initial fluctuations and ensure reliable, reproducible results, the first two runs serve as JVM warm-ups. The average is taken from the last three runs.

- **Inter-Experiment Interval**:

   There is an interval of approximately 1 second between each experiment to avoid interference from the previous experiment’s garbage collection (GC) and I/O flush.

### 4. Test and Results

All experiment codes and results are available in the appendix.

**(1) INSERT Performance**

Test data is generated using the following steps:

- Data is read and cleaned from the original `user.csv` file.
- To improve data processing efficiency and focus on query performance, the `FollowerUsers` and `FollowingUsers` columns, which are irrelevant to the query conditions, are removed.
- Random sampling is used to generate datasets of 10,000, 100,000, and 300,000 unique rows, resulting in the test files `user_10k.csv`, `user_100k.csv`, and `user_300k.csv`.

**JDBC Single INSERT (10k rows)**

Using a Java program, each INSERT is executed one at a time. After each insert, the database processes the transaction. Results from five repeated experiments are as follows:

| Total Time（ms） | 1st   | 2nd   | 3rd   | 4th   | 5th   | Average |
| -------------- | ----- | ----- | ----- | ----- | ----- | ------- |
| MySQL          | 12255 | 11371 | 12187 | 12744 | 11257 | 12029   |
| PostgreSQL     | 1672  | 1554  | 1608  | 1577  | 1653  | 1613    |

| Average Time（ms） | 1st   | 2nd   | 3rd   | 4th   | 5th   | Average |
| ---------------- | ----- | ----- | ----- | ----- | ----- | ------- |
| MySQL            | 1.178 | 1.090 | 1.172 | 1.226 | 1.072 | 1.156   |
| PostgreSQL       | 0.138 | 0.127 | 0.134 | 0.129 | 0.135 | 0.133   |

**PostgreSQL** is faster and more stable, with smaller time fluctuations compared to **MySQL**, which shows significant variation between runs.

For tasks that involve frequent single-row inserts, **PostgreSQL is recommended** for better performance and lower latency.

**JDBC Batch INSERT (batch size = 1000)**

Using `addBatch()` and `executeBatch()`, multiple SQL statements are sent to the database for batch processing. The results are as follows (time in milliseconds):

| Data Volume:10k | 1st | 2nd | 3rd | 4th | 5th | Average |
| --------------- | --- | --- | --- | --- | --- | ------- |
| MySQL           | 284 | 269 | 253 | 282 | 241 | 259     |
| PostgreSQL      | 157 | 154 | 155 | 165 | 147 | 156     |

| Data Volume:100k | 1st  | 2nd  | 3rd  | 4th  | 5th  | Average |
| ---------------- | ---- | ---- | ---- | ---- | ---- | ------- |
| MySQL            | 1654 | 1829 | 1980 | 1853 | 1816 | 1883    |
| PostgreSQL       | 893  | 893  | 930  | 879  | 920  | 910     |

| Data Volume:300k | 1st  | 2nd  | 3rd  | 4th  | 5th  | Average |
| ---------------- | ---- | ---- | ---- | ---- | ---- | ------- |
| MySQL            | 4875 | 5285 | 5022 | 4815 | 5240 | 5026    |
| PostgreSQL       | 2698 | 2747 | 2505 | 2625 | 2811 | 2647    |

**PostgreSQL** outperforms **MySQL** in batch insert performance, especially when dealing with larger datasets.

For large data processing, **PostgreSQL's performance is more stable**, maintaining a lower insertion time, while **MySQL's insertion time increases significantly** as the data volume grows.

**(2) UPDATE Performance**

The performance of MySQL and PostgreSQL is compared for small-scale (1k records) and large-scale (100k records) update operations. The results are as follows (time in milliseconds):

| Data Volume:1k | 1st | 2nd | 3rd | 4th | 5th | Average |
| -------------- | --- | --- | --- | --- | --- | ------- |
| MySQL          | 703 | 696 | 837 | 830 | 761 | 809     |
| PostgreSQL     | 398 | 436 | 455 | 456 | 369 | 427     |

| Data Volume:100k | 1st   | 2nd   | 3rd   | 4th   | 5th   | Average |
| ---------------- | ----- | ----- | ----- | ----- | ----- | ------- |
| MySQL            | 13376 | 14770 | 14248 | 15847 | 15021 | 15039   |
| PostgreSQL       | 3871  | 5224  | 5020  | 5047  | 4893  | 4987    |

**PostgreSQL** performs better than **MySQL** in both small and large-scale update operations, especially in large data updates.

**(3) Query Performance (SELECT)**

The task was to count users with ages between 20 and 40. Execution times and matching user counts were recorded for each database:

| Time(ms)   | 1st | 2nd | 3rd | 4th | 5th | Average |
| ---------- | --- | --- | --- | --- | --- | ------- |
| MySQL      | 479 | 459 | 469 | 473 | 469 | 470     |
| PostgreSQL | 172 | 166 | 166 | 173 | 184 | 174     |

The results show that **PostgreSQL** performs significantly better for range queries, especially with large datasets.

**(4) DELETE Performance (DELETE)**

The DELETE operations for MySQL and PostgreSQL were compared by deleting users older than 40. Each delete operation used transactions and was rolled back after each execution to avoid modifying data. The results are as follows (time in milliseconds):

| Time(ms)   | 1st  | 2nd  | 3rd  | 4th  | 5th  | Average |
| ---------- | ---- | ---- | ---- | ---- | ---- | ------- |
| MySQL      | 1717 | 1286 | 1226 | 1264 | 1298 | 1263    |
| PostgreSQL | 505  | 252  | 205  | 199  | 175  | 193     |

**PostgreSQL** performs far better than **MySQL**, especially in deleting large amounts of data. The delete speed is faster, and the response time is shorter.

### 5.Summary

- **PostgreSQL** consistently outperforms **MySQL** across all four operations (INSERT, Batch INSERT, UPDATE, DELETE), especially when handling large datasets. It offers greater stability and efficiency.
- **MySQL** can be competitive in certain scenarios but generally lags behind **PostgreSQL**, especially when handling complex queries and large data volumes.
- For projects requiring efficient data handling, especially frequent inserts, queries, and deletions, **PostgreSQL is the recommended database** due to its superior performance and lower latency.

?descriptionFromFileType=function+toLocaleUpperCase()+{+[native+code]+}+File&mimeType=application/octet-stream&fileName=CS307_task4_advanced2.md&fileType=undefined&fileExtension=md