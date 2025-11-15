# CS307_task4_advanced1_HighConcurrency

## High-Concurrency Performance Testing

### **Experiment Objectives**

- Test PostgreSQL’s response performance under different levels of concurrent load.
- Observe how throughput changes as the number of threads increases.
- Compare database performance with pure File I/O (sequential CSV scanning).

### **Experimental Environment**

Same as Task 4 Basic Part.

### **Methodology**

To cover the full range from low load to high load, this experiment uses the following thread counts:

**1, 5, 10, 20, 50, 100**

- **1:** Single-thread baseline
- **5–10:** Light concurrency (common in typical applications)
- **20–50:** Noticeable concurrent pressure (similar to API server scenarios)
- **100:** Required “high concurrency” level

**Operations per thread:** 5,000 query operations
To reduce randomness, **each thread count is tested three times**, and the average result is used.

### **SQL Statement**

A random-access approach is adopted to avoid cache effects. Each query randomly selects a `RecipeId` in the range **1–100000**: `SELECT RecipeId, Name FROM recipes WHERE RecipeId = ?;`

### **Code Explanation**

#### **DBMS:**

A Java thread pool is used to simulate high-concurrency database access.
In each round of testing, a fixed number of threads is created, and each thread independently executes multiple database queries. Queries use random `recipe_id` values to avoid caching effects and ensure fairness. The thread pool schedules tasks concurrently, generating a large amount of simultaneous requests to the database.
After all threads finish execution, total execution time and throughput (QPS) are computed to evaluate PostgreSQL performance under various concurrency levels.
(The Java thread-pool code is included in the appendix.)

#### **File I/O:**

A Java thread pool simulates high-concurrency file-reading scenarios.
In each test round, a fixed number of threads is created, and each thread independently performs multiple file queries. Each query generates a random `recipe_id` and scans the preloaded CSV content to find the corresponding row. The CSV file is fully loaded into memory at startup to avoid repeated disk access.
The thread pool schedules tasks concurrently so multiple threads scan the file at the same time. After all threads complete, the program calculates total execution time and throughput (QPS), which reflects File I/O performance under different concurrency levels.

(The Java thread-pool code is included in the appendix.)

### **Experimental Results**

| Threads | DBMS Avg QPS (ops/sec) | File I/O Avg QPS (ops/sec) |
| ------- | ---------------------- | -------------------------- |
| 1       | 8,068                  | 323                        |
| 5       | 30,821                 | 2,468                      |
| 10      | 41,462                 | 4,869                      |
| 20      | 57,424                 | 5,080                      |
| 50      | 72,132                 | 5,306                      |
| 100     | 91,145                 | 3,869                      |

(Screenshots from each execution are included in the appendix.)

### **Conclusion**

The experiment confirms that PostgreSQL scales well under multi-user concurrent query workloads and maintains high throughput even at high thread counts.

In contrast, pure File I/O—lacking indexing and caching mechanisms—hits a performance bottleneck quickly as concurrency increases.

These results demonstrate that database systems provide essential performance advantages in high-concurrency application scenarios, making them far superior to raw file-based data access.

?descriptionFromFileType=function+toLocaleUpperCase()+{+[native+code]+}+File&mimeType=application/octet-stream&fileName=CS307_task4_advanced1_HighConcurrency.md&fileType=undefined&fileExtension=md