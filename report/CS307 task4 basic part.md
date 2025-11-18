# CS307 task4 basic EN

### 1. Test Environment

#### a. Hardware Specifications

- CPU Model: Intel Core Ultra 5 125H @ 3.60GHz
- Memory Capacity: 32GB
- Storage: 1 TB NVMe SSD

#### b. Software Specifications

- DBMS Version: PostgreSQL 17.6 (64-bit)
- Operating System: Windows 11
- Programming Language: Java
- Development Environment: Java 17 (JDK 17.0.4.1), javac 17.0.4.1
- JDBC Driver: postgresql-42.2.5.jar

### 2. Test Data Organization

#### Data Storage Description

- **user.csv**: Contains all user data (provided by the instructor), used for importing initial data into the DBMS `user` table and file system.
- **user_10000.csv**: Contains 10,000 rows of user data (created by the user), randomly selected 10,000 entries from `user.csv` with an added 3,000,000 to the `author_id` to prevent violations of primary key uniqueness. Used for DBMS `recipe` table insertion tests and file system `recipe.txt` insertion tests.
- **users table**: The table in the DBMS that stores user data (created by the user), containing data from `user.csv`, used for insert, update, query, and delete tests in the database system.
- **users.txt**: A file in the file system that stores all user data (created by the user), used for insert, update, query, and delete tests in the file system.

#### Data Organization in DBMS

In the PostgreSQL database, test data is organized in relational table structures. The specific implementation is as follows:

- **Table Structure Design**: The `users` table stores recipe information, which includes the following fields: `author_id` (primary key), `author_name`, `gender`, `age`, `followers_count`, `following_count`.
- **Data Types**: Different data types are used according to the field meaning, such as integer (`author_id`), text (`author_name`), and enum (`gender`), ensuring the accuracy of data types and optimization of storage space.
- **Constraints**: `author_id` as the primary key ensures the uniqueness of each record, allowing for fast location and querying.
- **Index Optimization**: The database automatically creates indexes for primary key fields, significantly improving query performance, especially for exact queries on the ID field.

#### Data Organization in File System

In the file system, test data is organized as text files. The specific implementation is as follows:

- **File Format**: Simple text files (e.g., `users.txt`) store all user records.
- **Record Separation**: Each record occupies one line, separated by a newline character (`\n`).
- **Field Separation**: Each record uses a semicolon (`;`) as a field separator, dividing different attributes.
- **Data Structure**: There are no explicit data type distinctions, all data is stored as strings, and type conversion is performed during program execution.
- **No Index Mechanism**: The file system itself does not provide index support, and queries require sequential scanning of the entire file.

#### SQL Statement Generation Method

All CRUD (Create, Read, Update, Delete) SQL statements are generated using a unified prepared statement method. The specific characteristics are as follows:

- **Parameterized Query Mode**: Placeholders (`?`) replace direct string concatenation, which is a widely used practice in various programming languages and database systems to prevent SQL injection attacks.
- **Dynamic Parameter Binding Mechanism**: Application variables are securely mapped to SQL parameters via type-safe parameter binding methods, ensuring correct and safe data type conversions.
- **Standardized SQL Statement Structure**: Standard SQL syntax is used to construct all CRUD operation statements, whether it is a `SELECT`, `INSERT`, `UPDATE`, or `DELETE`, all following the same prepared statement generation mechanism.
- **Error Handling Framework**: A unified exception handling mechanism is implemented to ensure that errors during SQL execution are correctly captured and meaningful error messages are returned.
- **Resource Lifecycle Management**: The standard connection-execute-close resource management pattern is followed to ensure that database connections and other resources are properly released after the operation is complete.
- **SQL Statement Reuse**: The prepared statement mechanism improves SQL execution efficiency by reducing redundant parsing and optimization overhead.

#### File Operation Mechanism

File operations are implemented using the standard Java I/O library:

- **Read Operation**: BufferedReader is used to read file content line by line, improving reading efficiency.
- **Write Operation**: FileWriter is used to write to the file, supporting append mode.
- **Temporary Files**: For delete and update operations, a temporary file strategy is employed to avoid conflicts while reading and writing simultaneously.
- **File Replacement**: After the operation is completed, the original file is deleted, and the temporary file is renamed to the original file name.

### 3. Test SQL Scripts and Program Source Code Description

The test involves 5 source code files: `DatabaseTest.java`, `FileTest.java`, `DatabaseOperations.java`, `FileOperation.java`, `DatabaseBetter.java`, and `user_10000.java`. Below is the functionality description of each file (the specific operation code is in the attachments):

- **DatabaseTest.java**:
   - Calls the DBMS test functions for insertion, query, update, and deletion, and prints the test results. Repeats the experiment five times, printing the result of each experiment and calculating the average.
- **FileTest.java**:
   - Calls the file test functions for insertion, query, update, and deletion, and prints the test results. Repeats the experiment five times, printing the result of each experiment and calculating the average.
- **DatabaseOperations.java**:
   - Interacts with PostgreSQL through JDBC, with functionalities including connection management and performing insert, update, query, and delete operations.
   - **Insert Operation (addUsers)**: Uses a parameterized SQL `INSERT INTO` statement to insert data.
   - **Query Operation (queryUsers)**: Uses a parameterized SQL `SELECT COUNT(*) FROM` statement to count matching records.
   - **Update Operation (updateUsersAge)**: Uses a parameterized SQL `UPDATE` statement to increment the age of matching users.
   - **Delete Operation (deleteUsers)**: Uses a parameterized SQL `DELETE FROM` statement to delete matching records.
- **FileManipulation.java**:
   - Manages user data through file I/O operations, with functionalities including file path management, data format, and CRUD operations for insert, query, update, and delete.

### 4. Performance Comparison Analysis

| Operation Type      | DBMS Test Time (ms) | DBMS Optimized Test Time (ms) | File I/O Test Time (ms) |
| ------------------- | ------------------- | ----------------------------- | ----------------------- |
| Insert (insertTest) | 1681                | 268                           | 1208                    |
| Delete (deleteTest) | 1556                | 118                           | 9857                    |
| Query (selectTest)  | 1022                | 533                           | 8547                    |
| Update (updateTest) | 2020                | 237                           | 10231                   |

**Analysis:**

1. When processing data (especially large-scale or complex datasets), using a DBMS such as PostgreSQL is far superior to managing data with a file system. The performance difference between DBMS and file I/O is significant. In conclusion, database systems have a clear advantage over direct file use when handling data.
2. For frequently operated attributes, introducing indexes, implementing transaction control, and using batch operations can significantly improve database performance.

?descriptionFromFileType=function+toLocaleUpperCase()+{+[native+code]+}+File&mimeType=application/octet-stream&fileName=CS307+task4+basic+EN.md&fileType=undefined&fileExtension=md
