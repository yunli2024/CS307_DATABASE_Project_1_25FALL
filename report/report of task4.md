# report of task4

> CS307 pj1 task4 basic part; first drift

> 4测试需要基于数据，纳入期中后todo-list

### 1.test environment

#### a. hardware specification

CPU model: Intel Core Ultra 5 125H @ 3.60GHz

size of memory: 32GB

#### b. software specification

DBMS version: PostgreSQL 17.6 (64-bit)

operating system: Windows 11

programming language: Java

development environment: Java 17 (JDK 17.0.4.1), javac 17.0.4.1

#### c. necessary information

(开发环境的库和其他要求)

### **2. Test Data Organization Methodology**

**Data Storage Description**

- **recipe.csv**: Stores the complete recipe dataset (provided by the instructor), used for importing initial data into the DBMS `recipe` table and the file system's `recipe.txt` file.
- **recipe_10000.csv**: Stores 10,000 rows of recipe data (created independently), used for insertion performance testing in both the DBMS `recipe` table and the file system's `recipe.txt`.
- **recipe table**: A table within the DBMS (created independently) for storing all recipe data. The data originates from `recipe.csv` and is utilized for insertion, update, query, and deletion tests in the file system context.
- **recipe.txt**: A file within the file system (created independently) for storing all recipe data, used for insertion, update, query, and deletion tests within the file system.

**Data Organization in the DBMS**

Within the PostgreSQL database, test data is organized using a relational table structure. The specific implementation is as follows:

- **Table Schema Design**: The `recipes` table is used to store recipe information. It contains multiple fields such as `RecipeId` (Primary Key), `Name`, `AuthorId`, etc., forming a complete relational data model.
- **Data Types**: Appropriate data types are employed based on the semantics of each field, including Integer (`RecipeId`), String (`Name`), Double Precision (`AggregatedRating`), etc., ensuring data accuracy and optimized storage utilization.
- **Constraints**: `RecipeId` serves as the primary key, guaranteeing the uniqueness of each record and facilitating rapid location and querying.
- **Index Optimization**: The database automatically creates an index for the primary key field, significantly enhancing query performance, particularly for exact-match queries based on ID.

**Data Organization in the File System**

Within the file system, test data is organized in the form of text files. The specific implementation is as follows:

- **File Format**: A simple text file (`recipes.txt`) is used to store all recipe records.
- **Record Delimitation**: Each record occupies a single line, with different records separated by newline characters (`\n`).
- **Field Delimitation**: Within each record, a semicolon followed by a space (`;`) is used as the field separator to distinguish different attributes.
- **Data Structure**: There is no explicit distinction of data types; all data is stored as strings, necessitating type conversion during program runtime.
- **Absence of Indexing Mechanism**: The file system itself does not provide indexing support, requiring sequential scanning of the entire file during query operations.

**Methodology for Generating Test SQL Statements**

All Create, Read, Update, Delete (CRUD) operation SQL statements are generated uniformly using the prepared statement approach. The specific characteristics are as follows:

- **Parameterized Query Pattern**: Placeholders (`?`) are used instead of direct string concatenation. This is a widely adopted security practice across various programming languages and database systems, effectively preventing SQL injection attacks.
- **Dynamic Parameter Binding Mechanism**: A typed parameter binding method securely maps application variables to SQL parameters, ensuring the correctness and safety of data type conversions.
- **Standardization of SQL Statement Structure**: Standard SQL syntax is employed to uniformly construct all CRUD operation statements. Whether for SELECT queries, INSERT inserts, UPDATE updates, or DELETE deletions, the same prepared statement generation mechanism is adhered to.
- **Error Handling Framework**: A unified exception catching and handling mechanism is implemented, ensuring that errors occurring during SQL execution are properly caught and meaningful error messages are returned.
- **Resource Lifecycle Management**: The standard resource management model of connect-execute-close is followed, ensuring that resources such as database connections are properly released upon operation completion.
- **SQL Statement Reuse Mechanism**: The prepared statement mechanism enhances SQL execution efficiency by reducing the overhead associated with repeated parsing and optimization.

**File Operation Mechanism**

File operations are implemented using the standard Java I/O library:

- **Read Operations**: `BufferedReader` is used to read file content line by line, improving reading efficiency.
- **Write Operations**: `FileWriter` is used for direct file writing, supporting append mode.
- **Temporary Files**: For delete and update operations, a temporary file strategy is adopted to avoid read-write conflicts.
- **File Replacement**: Upon operation completion, updates are finalized by deleting the original file and renaming the temporary file.

### **3. Description of Test SQL Scripts and Program Source Code**

The testing involves five source code files: `Client.java`, `DatabaseManipulation.java`, `DataFactory.java`, `DataManipulation.java`, and `FileManipulation.java`. The functionalities of these files are as follows:

**Client.java**

Invokes the insertion, query, update, and deletion test functions for both file system and DBMS tests, and prints the test results.

**DataManipulation.java**

Contains the interface defining the four test functionalities: insertion, query, update, and deletion.

**DataFactory.java**

Contains the `createDataManipulation` function, which creates a data manipulation object based on the input string parameter – if the input string contains "file", it returns a file operation object; if it contains "database", it returns a database operation object.

**DatabaseManipulation.java**

Implements the `DataManipulation` interface and interacts with the PostgreSQL database via JDBC. Its main functions include:

- **Connection Management**: Manages database connections through the `getConnection()` and `closeConnection()` methods, utilizing the PostgreSQL JDBC driver.
- **CRUD Operations**:
   - **Add Operation (`addOneRecipe`)**:
      - Uses a parameterized SQL INSERT statement supporting the insertion of complete data for 27 fields.
      - **Implementation Details**: First splits the input string by semicolons, then uses methods like `setInt()`, `setString()`, and `setObject()` to bind parameters.
      - **Special Handling**: Timestamp fields undergo timezone conversion using `(?::timestamptz AT TIME ZONE 'UTC')`.
      - Returns the number of affected rows as the operation result.
   - **Query Operation (`findRecipeById`)**:
      - Uses a parameterized SQL query statement: `SELECT * FROM recipes WHERE RecipeId = ?`.
      - Binds the `RecipeId` parameter using the `setInt()` method.
      - **Result Processing**: Uses `StringBuilder` to construct formatted recipe details, including fields such as ID, Name, Author information, and Cooking Time.
      - Returns a corresponding prompt message if no record is found.
   - **Update Operation (`updateRecipeRating`)**:
      - Uses a parameterized SQL update statement: `UPDATE recipes SET AggregatedRating = ? WHERE RecipeId = ?`.
      - Binds the new rating and the recipe ID using the `setDouble()` and `setInt()` methods, respectively.
      - Returns the number of affected rows, where 0 indicates no matching record was found.
   - **Delete Operation (`deleteRecipeById`)**:
      - Uses a parameterized SQL delete statement: `DELETE FROM recipes WHERE RecipeId = ?`.
      - Binds the `RecipeId` parameter using the `setInt()` method.
      - Returns the number of affected rows, where 0 indicates no matching record was found.
- **Performance Monitoring**:
   - All operation methods implement execution time tracking functionality, using `System.currentTimeMillis()` to record operation start and end times.
   - Upon operation completion (including in cases of exceptions), the total execution time is printed in the format: "`[Method Name] operation total time: [Time] ms`".

**FileManipulation.java**

Implements the `DataManipulation` interface and manages recipe data through file I/O operations. Its main functions include:

- **File Path Management**: The data file path is uniformly managed via the constant `RECIPES_FILE` (value: "`recipes.txt`").
- **Data Format**: Uses a semicolon followed by a space ("`;`") as the field separator, with each record occupying a single line.
- **CRUD Operations**:
   - **Add Operation (`addOneRecipe`)**:
      - Uses `FileWriter` to open the file in append mode (`append=true`).
      - Automatically detects and adds a newline character, ensuring each record occupies a separate line.
      - Returns 1 to indicate successful addition, and 0 to indicate addition failure.
   - **Query Operation (`findRecipeById`)**:
      - Uses `BufferedReader` to read the file content line by line.
      - For each line, uses `split("; ", 2)` to split and extract the first field as the ID.
      - Performs a sequential scan until a matching ID is found or the end of the file is reached.
      - Returns the complete line content if a matching record is found, returns "Recipe not found" if not found, and returns "Error reading file" if an error occurs.
   - **Delete Operation (`deleteRecipeById`)**:
      - Employs a temporary file strategy: creates `temp_recipes.txt` as a temporary file.
      - Uses `BufferedReader` to read the original file and `FileWriter` to write to the temporary file.
      - Checks the record ID line by line, writing only those records whose IDs do not match the target for deletion.
      - After the operation completes, deletes the original file and renames the temporary file to the original filename.
      - Returns 1 to indicate successful deletion, and 0 to indicate the record was not found or the operation failed.
   - **Update Operation (`updateRecipeRating`)**:
      - Also employs a temporary file strategy.
      - After locating the target record by ID, uses `StringBuilder` to construct the updated line, replacing the 13th field (index 12) with the new rating.
      - Preserves all other fields unchanged.
      - Performs file replacement upon operation completion.
      - Returns 1 to indicate successful update, and 0 to indicate the record was not found or the operation failed.
- **Performance Monitoring**:
   - All operation methods implement execution time tracking functionality, using `System.currentTimeMillis()` to record operation start and end times.
   - The total execution time is printed within a `finally` block, in the format: "`File [Method Name] operation completed in [Time] ms`".
- **Exception Handling and Resource Management**:
   - Utilizes the try-with-resources syntax for automatic management of file resources (`BufferedReader`, `FileWriter`).
   - Catches and prints all `IOException` exceptions.
   - Ensures performance monitoring logic is executed within a `finally` block, regardless of whether the operation was successful.
   - For delete and update operations, handles potential file system exceptions that may occur during the file replacement process.
