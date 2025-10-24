### **Report of Task 3**

> draft 1024 liyunzang@SUSTech

In this task, I implemented a Java program to import data from CSV files into the database designed in Task 2.

### **Prerequisites**

1. The database tables must already exist before running this script. (We will discuss a possible automated creation approach in the advanced part.)

2. The PostgreSQL **JDBC driver** must be added as a **project dependency**.

   In IntelliJ IDEA: open *File → Project Structure → Modules → Dependencies*, and add the JAR file `postgresql-42.2.5.jar`.

### Code Execution Steps

**Step 1. Connect to the database**

In the `getConnection()` method, the class `org.postgresql.Driver` is loaded to establish a connection to the local PostgreSQL server.
The method uses the basic connection parameters — host, database name, user, password, and port — to build the JDBC URL and call `DriverManager.getConnection()`.
Once the dependency is correctly imported, the driver initializes the connection channel using the PostgreSQL protocol; otherwise, the connection attempt will fail. So please you have down import dependency properly in the prerequisites part.

**Step 2. Read data from CSV files**
 In the `readOneCsvRecord()` method, I use a `BufferedReader` to read the CSV file line by line. Specifically, the newline character (\n) is treated as the end of a record only when it appears outside quotation marks, ensuring that multi-line records are read correctly.
 After obtaining a complete CSV record, the `splitCsvRecord()` method is used to split the record into a string array, which then provides the values for the SQL `INSERT` statement in the next step.

This design guarantees the robustness of CSV parsing when handling quoted fields, commas, and embedded newlines.

**Step 3. Import data**

In the `importRecipesCsv()` method, I use a **parameterized** `PreparedStatement` to efficiently insert multiple rows into the database.

First, I define a SQL template `INSERT INTO recipes (...) VALUES (?,?,?,?,...)`, where each `?` serves as a placeholder for a column value. This design helps prevent SQL injection and improves performance through precompilation.

Then, for each record obtained from Step 2, I set the corresponding parameters of the `PreparedStatement` using the parsed string array, and call `ps.addBatch()` to temporarily store the statement in memory.

When the number of accumulated statements reaches the predefined **batch size**, the program executes all of them together using `ps.executeBatch()`. This **batch execution** greatly reduces the number of database round-trips and improves import speed compared to executing each `INSERT` individually.

还有一步，解释预编译机制和batch的方法为什么能做到efficient 但是在这里写还是在下面“优化”的部分写？感觉已经做完优化了，是否进行与不预编译的效率对比？？？

Similarly, we use `importReviewsCsv()` and ` importUsersCsv()` to import the other data.

**Step 4.Check the import correctness**

After the data import process completed successfully, I verified the correctness by counting the total number of records in each entity table using SQL `COUNT(*)` queries.The results are as follows:

| Table name | Number of records (rows) |
| ---------- | ------------------------ |
| `recipes`  | 532,108                  |
| `reviews`  | TBD                      |
| `users`    | TBD                      |

These numbers are consistent with the line counts of the original CSV files, indicating that all data have been successfully imported without loss or duplication.









后续todolist：

1. 完成导入部分的另外两个的代码，在数据库设计好之后再进行。

2. 设计不同的导入方法（分块，不进行预处理……）或者对当前的导入方法进行优化( 这里是对比还是优化？如果优化需要做什么方面？）需用用图表和数据呈现，描述区别和实际运行时间。这是比较难的这部分 advanced

   感觉既要对比又要优化，早知道一开始写一个低一点了的。接下来需要找出更高效的方法，然后与低的对比，与高的检验。

3. 需要给出验证是否导入正确的方法：number of records In each table 一定要有，还有其他比如null检验？

4. 需要尝试自动化方法，呜呜。







