# CS307 Project1 Report

## Group Information

12410922 李允臧

12412460 吴 桐

## TASK 1: ER Diagram

I use the tool **draw.io** to finish this **ER Diagram**

![CS307_ER_vesion1](draft_Report.assets/CS307_ER_vesion1.png)

#### Entities: 这一部分可能看情况删，因为task2也有这部分

- **User**
  - Primary Key: AuthorId
  - Attributes: AuthorName、Gender、Age
- **Review**
  - Primary Key: ReviewId
  - Attributes: Rating、Review、DataSubmitted、DateModified、Likes
- **Recipe**
  - Primary Key: RecipeId
  - Attributes: Description、Name、RecipeCategory、RecipeInstructions、RecipeYield、RecipeServings
- **Keyword**
  - Primary Key: KeywordId
  - Attributes: Keyword
- **Ingredient**
  - Primary Key: IngredientId
  - Attributes: Ingredient
- **Nutrition**
  - Primary Key: NutritionId
  - Attributes: Calories、Fat、SaturatedFat、Cholesterol、Sodium、Carbohydrate、Fiber、Sugar、Protein
- **Time**
  - Primary Key: TimeId
  - Attributes: DataPublished、CookTime、PrepTime

#### Relationship:

1. User **reviews** Recipe
   - One user can review multiple recipes
   - One recipe can be reviewed by multiple users
2. User **follows** User
   - Users can follow each other
3. User **favourites** Recipe
   - One user can favourite multiple recipes
   - One recipe can be favourited by multiple users
4. Recipe **has** Keyword
   - One recipe can have multiple keywords
   - One keyword can be associated with multiple recipes
5. Recipe **needs** Ingredient
   - One recipe needs multiple ingredients
   - One ingredient can be used in multiple recipes
6. Ingredient **has** Nutrition
   - Each ingredient has one nutrition profile
7. Recipe **has** Time
   - Each recipe has prep time and cook time

## TASK 2: Database Design

In this TASK, I write DDL statements to create tables and columns according to the ER diagram. The SQL file is uploaded as attached file.Then I generate  the database diagram by using the" Show Visualization" feature of Datagrip. The picture is shown below: 

![visualization_ver2](draft_Report.assets/visualization_ver2.png)



The following part is a brief description of the meaning of tables and columns. We **designed 13 tables** and will explain them separately. Finally, I will explain the reason that this design cater to all the requirements.

##### Description of tables and columns 

1. `users` table stores all the users' information. It includes **author_id** as primary key, **author_name** as the name which is NOT NULL, **gender** which is an enum type and **age**.
2. `recipes`  table stores the recipe information. It includes **recipe_id** as primary key, **author_id** which is the author of this recipe as foreign key reference to **author_id** in `users`,the **recipe_name** which is NOT NULL, the **description**, the **recipe_category** which is this recipe belong to,the **recipe_yield** which is the output of this recipe, the **aggregated_rating** which is the score obtained of this recipe, the **review_count** which is the reviewer number of this recipe, and **recipe_servings** indicating the number of servings.
3. `reviews` table stores the review information. It includes **review_id** as primary key, **recipe_id** as foreign key reference to `recipes` , **user_id** as foreign key reference to `users` , **rating** which is the score given to this recipe, **review** which is the detailed review content, **date_submitted** with type DATE which is the date of review submitted,**date_modified** which is the latest modification.
4. `following` table stores the follow relationships among users. It includes a composite primary key (**follower_id**, **followee_id**), both as foreign keys referencing `users(author_id)`, recording each directed follow relationship. 
5. `recipe_favorite` table stores the user that favorites one recipe. It includes a composite primary key of(**user_id,recipe_id**), as foreign key to `users` and `recipes` , respectively.
6. `like_relationship` stores the user that likes one review. It includes a composite primary key of( **user_id,review_id**), as foreign key to `users` and `reviews` , respectively.
7. `keyword` stores a list of all possible keywords. It includes **keyword_id** as primary key, **keyword** as the content which is UNIQUE NOT NULL.
8. `recipe_keyword` stores the relationship between `recipes` and `keyword`, It includes a composite primary key of(**recipe_id,keyword_id**), as foreign key to `recipes` and `keyword` , respectively.
9. `ingredient` stores a list of all possible ingredients. It includes **ingredient_id** as primary key, **ingredient** as the content which is UNIQUE NOT NULL.
10. `recipe_ingredient` stores the relationship between `recipes` and `ingredient`, It includes a composite primary key of(**recipe_id,ingredient_id**), as foreign key to `recipes` and `ingredient`, respectively.
11. `nutrition` table stores the nutrition information for each recipe. It includes **recipe_id** as both the primary key and foreign key referencing `recipes(recipe_id)`, along with **calories**, **fat**, **carbohydrates**, **protein**, etc. Each numeric value uses a suitable type.
12. `recipe_time` stores the time information relevant to each recipe. It includes **recipe_id** as primary key and foreign key reference to`recipes`, **prepare_time** which is the time need to prepare with type interval, **cook_time** which is the time need to cook with type interval, **total_time **which is the sum of prepare and cook time with type interval, **date_published** which is the date of this recipe published.
13. `recipe_instruction` table stores detailed step-by-step cooking instructions. It includes **recipe_id** as foreign key referencing recipes and **instruction** as NOT NULL. The composite primary key (**recipe_id**, **instruction**) ensures no duplicate steps for the same recipe.

##### Compliance with Requirements and Normal Forms

​	The database design meets all the requirements and normal forms. It can manage all the information mentioned in the document. It contains primary key and foreign key which uniquely identify each row. Every table is not isolated and the database contains no circular links. What's more, each table has at least one NOT NULL column, and has suitable data type for columns.

​	The database design also satisfies the three normal forms.

- **1NF:** All attributes are atomic. Multivalued columns in the original dataset (such as ingredients, keywords, likes, following and instructions) are separated into relationship tables to remove repeating groups.  
- **2NF:** Each non-key attribute fully depends on the whole primary key. Composite tables (`recipe_keyword`, `recipe_ingredient`) have no partial dependencies.  
- **3NF:** No transitive dependencies exist among non-key attributes. Derived attributes like `review_count` are optional and can be omitted to avoid redundancy.  

In summary, this design achieves data integrity, consistency, and scalability, satisfying the requirements of Task 2.

## TASK 3 : Data Import 

In this task, I execute some data preprocess to clean the row data, and implemented a Java program to import data from CSV files into the database which designed in Task 2.

### Data Preprocess

Before truly importing the data into the relational database designed in TASK 2, we preprocess the data in the three csv files and identified several data quality issues that might lead to incosistencies or meaningless. To ensure the correctness and reliablity of subsequent steps, it is necessary to preprocess the data and normalize it before importing into database. So This section focus on these three aspects of preprocessing: deleting confused or meaningless records, correcting inconsistent attributes using other fields within the same record, and resolving data type problems.

The first problem is confused or meaningless records. For example, in the `review.csv` file, there is a total number of **19 lines** that have a **redundent comma** between reviewId and recipeId, which will lead to confusion in the following steps. Therefore, the data preprocess stage must remove one of them and stay the same structure with other records. In the following part, we consider these lines lack of authorId,and it is contradict with foreign key should be not null.So we **delete these 19 lines** when importing and the final number of reviews should be 140,1963. Other confused or meaningless records are treated similarly.

The second problem is attributes that contradict other attributes within the same line. This issue may appear in `recipe.csv`. Some lines may have both CookTime and PrepareTime in correct form, but the TotalTime is either missing or inconsistent with the sum of two. The data preprocess procedures will treat CookTime and PrepareTime as correct and check the TotalTime. If the value is missing, it will be filled with the sum of other two times. However, if the value is inconsistent, we will use the sum of others to fill the blank instead of collisioned one. Similar procedures will also be used in the `followers_count` and `following_count` arrtibutes in the users table. However, in practical programming, we found that following relationship is quite complex with many conflicts. As a result, we treat following_users as correct version and process them when execute Java importing code.

The third problem includes data type errors. In `reviews.csv`, every RecipeId column has the incorrent data type with the attached ".0", but the IDs should be integer type. So the data preprocess in this step should convert them into integers and remove the zero in the end. Moreover, the time-relavant   data follows ISO 8601 standard, with capital characters like P, T, H and so on. It should be the type `Time` or `Inteval` in SQL database, so we need to convert it into correct form as well. In our procedures, we decide to finish this convert step when inserting and parse it in Java program instead of preprocess stage.

In practical part, we used a python script to clean and preprocess row csv files and fixed all the problems mentioned previously. The source code will be attached as `data preprocess.py` file. There might be other strange data that haven't been covered, which will be processed in the following inserting part. The preprocess result is shown below:

![9f5e91dbdfc31e8aacffda08bd9ccb6e](draft_Report.assets/9f5e91dbdfc31e8aacffda08bd9ccb6e.png)



### **Prerequisites**

1. The database tables must already exist before running this script. (We will discuss a possible automated creation approach in the advanced part.)

2. The PostgreSQL **JDBC driver** must be added as a **project dependency**.

   In IntelliJ IDEA: open *File → Project Structure → Modules → Dependencies*, and add the JAR file `postgresql-42.2.5.jar`. The JAR file will be provided in the project directory.

### Code Execution Steps

#### **Step 1. Connect to the database**

In the `getConnection()` method, the class `org.postgresql.Driver` is loaded to establish a connection to the local PostgreSQL server.
The method uses the basic connection parameters — host, database name, user, password, and port — to build the JDBC URL and call `DriverManager.getConnection()`.
Once the dependency is correctly imported, the driver initializes the connection channel using the PostgreSQL protocol; otherwise, the connection attempt will fail. So please you have down import dependency properly in the prerequisites part.

#### **Step 2. Read data from CSV files**

The import program reads each CSV file using a lightweight line-based method. In `readOneCsvRecord()`, a `BufferedReader` retrieves one physical line at a time; empty lines are skipped, and `null` indicates the end of file.

After a complete line is obtained, the `splitCsvRecord()` method converts it into an array of fields. Instead of using `String.split(",")`, the method scans the line character by character. A boolean flag tracks whether the parser is currently inside quotation marks. Commas outside quotes are treated as field separators, while text inside quotes—including commas and escaped quotes—is preserved. This ensures that fields containing punctuation or quotation marks are handled safely.

These parsing steps standardize the incoming data and separate generic CSV handling from table-specific logic. All import methods in Step 3 reuse this shared parsing pipeline before converting the processed values into batched SQL insertions.

#### **Step 3. Import data**

##### 3.1 Import `users.csv`

The `importUsersCsv()` method reads the users information from *users.csv* and inserts it into the `users` table and the corresponding `following` relationship table.

After obtaining a parsed CSV record from Step 2, the program extracts fields such as AuthorId, AuthorName, Gender, Age, and the follower/following lists. The follower and following lists are parsed into individual user-to-user relations, which are then inserted into the `following` table.

A parameterized `PreparedStatement` is used for both tables to accelerate. For every valid record, parameters are assigned and inserted via `addBatch()`. Invalid IDs and malformed list fields are skipped to avoid insertion errors. When the batch size limit is reached, the statements are executed together using `executeBatch()`.

##### **3.2 Import `recipes.csv`**

The `importRecipesCsv()` method processes the complex *recipes.csv* file by distributing different fields into multiple tables according to the schema in Task 2.

A single recipe record is decomposed and inserted into the following tables:`recipes` ，`nutrition`，`recipe_time`，`recipe_keyword`，`recipe_ingredient`，`recipe_instruction`，`recipe_favorite`, `keyword` and `ingredient`.Each table uses its own PreparedStatement.
Time-related fields (PrepTime, CookTime, TotalTime) are converted  into SQL TIME values. The DatePublished field is parsed into DATE format with fallback handling for missing or invalid values.

For multi-value fields such as Keywords, Ingredients, Instructions, and Favorites, the program splits the string into lists and inserts each item into its corresponding relationship table. Keywords and ingredients are also inserted into their dictionary tables (`keyword`, `ingredient`) with `ON CONFLICT DO NOTHING` to guarantee uniqueness.

Invalid recipe IDs, empty fields, or unparseable values result in skipping that specific part or the entire record, depending on severity.

Similarly, For every valid record, parameters are assigned and inserted via `addBatch()`.  When the batch size limit is reached, the statements are executed together using `executeBatch()`.

##### **3.3 Import `reviews.csv`**

The `importReviewsCsv()` method loads review data from *reviews.csv* and inserts it into `reviews` and`likes_relationship`.

Each CSV record is parsed into ReviewId, RecipeId, UserId, Rating, Review text, submission date, modification date, and Likes.

Since the database specifies `rating` as NOT NULL, any record with missing or invalid rating values is skipped.
 Date fields are parsed into DATE type with safe null handling.

The Likes field is split and inserted as multiple rows into `likes_relationship`. Duplicate likes and invalid user IDs are automatically filtered using `ON CONFLICT DO NOTHING`.`PreparedStatement` batching is applied here as well to maintain fast insertion speed for the large review dataset.

##### 3.4 Conclusion of import data

Here's the conclusion table for csv files, import method and the corressponding tables :

| CSV File      | Import Method        | Affected Tables                                              |
| ------------- | -------------------- | ------------------------------------------------------------ |
| `users.csv`   | `importUsersCsv()`   | users, following                                             |
| `recipes.csv` | `importRecipesCsv()` | recipes, nutrition, recipe_time, recipe_keyword, recipe_ingredient, recipe_instruction, recipe_favorite, keyword, ingredient |
| `reviews.csv` | `importReviewsCsv()` | reviews, likes_relationship                                  |

This modular, table-aware import pipeline ensures correctness, clarity, and efficiency when handling all three large CSV files.



#### **Step 4.Check the import correctness**

After the data import process completed successfully, I verified the correctness by counting the total number of records in each entity table using SQL `COUNT(*)` queries to find the result table. You can find this SQL query code in the attached file `select_table_records_number.sql` or the project architecture `SQL/select_table_records_number.sql`.

The results are as follows:

| Table name           | Number of records (rows) |
| -------------------- | ------------------------ |
| `users`              | 299,892                  |
| `following`          | 774,121                  |
| `recipes`            | 522,517                  |
| `recipe_time`        | 522,517                  |
| `nutrition`          | 522,517                  |
| `reviews`            | 1,401,963                |
| `like-relationship`  | 4,995,748                |
| `keyword`            | 311                      |
| `ingredient`         | 7358                     |
| `recipe_keyword`     | 2,486,934                |
| `recipe_ingredient`  | 4,003,863                |
| `recipe_favorite`    | 2,588,000                |
| `recipe_instruction` | 3,429,015                |

These numbers in the **main tables** are consistent with the line counts of the original CSV files, and the `reviews` table is **19 lines less** than the csv file, which we delete it in data preprocess, indicating that the csv data have been successfully imported without loss or duplication.



### Advanced Part 

My test environment are shown below.

> CPU: AMD Ryzen 7 8745H (8 cores/16threads, with Radeon 780M Graphics)
>
> RAM size: 24GB
>
> DBMS: PostgreSQL 17.6 on x86_64-windows, compiled by msvc-19.44.35213, 64-bit
>
> Programming language:  Java SE 23.0.2 (64-bit)
>
> Compiler: Oracle JDK 23.0.2 (64-bit)
>
> Operating System: Windows 11 Home Chinese Edition, Version 24H2
>
> Database IDE version: DataGrip 2025.1.3
>
> Programming IDE version: IntelliJ IDEA 2024.3.3 (Ultimate Edition)

#### 1、Comparative Evaluation of Multiple Data Import Strategies

In the data importing part, I use some techniques to load data. In this part, I will try different strategies to evaluate the differences between import methods. Since the data source is large-scale, I will change the batch size and the use of preparedStatement to find their impact on the import time and programming efficiency.

In order to measure the import time in a constant way, I use a new class `StopWatch` to measure the execution time of program. When the import process starts, the timer records the start timestamp, and it records the end timestamp when the import finishes. Finally, it will calculate the time used and print it into console, so that we can get the experiment data. This method is stable and efficient, and it minimizes the influence of initialization and I/O on the timing results.

Note that some importing methods may quite time-consuming, so I only test on the `users.csv` file to compare the time.

**The first group of experiments** focuses on batching techniques. I tried different method of a series of changing batch size, including a "NO BATCH" version, which means each line is inserted into the table individually. I execute every batch size seperately, and record the time they used. Repeat this procedure for 3 times to avoid some random disturb. The results are shown in the following table:

| Method                         | Batch size | Average time | comparision       |
| ------------------------------ | ---------- | ------------ | ----------------- |
| importUsersCsvNoBatch()        | NO         | 190.307s     | 1×                |
| importUsersCsvWith100Batch()   | 100 rows   | 55.947s      | **3.402x faster** |
| importUsersCsvWith1000Batch()  | 1000 rows  | 53.836s      | **3.535x faster** |
| importUsersCsvWith10000Batch() | 10000 rows | 51.693s      | **3.681x faster** |

And here is the picture for each runtime:

![task3experiment1](draft_Report.assets/task3experiment1-1763400855869-2.png)



The results of the group of experiments demonstrate the importance of the batching technique in the data importing process. When batching is disabled, the import procedure becomes significantly slower, taking almost 3.5 times longer than the batched versions. When batching is enabled, the import process complete in roughly the same time, with only small improvement as the batch size increases.Therefore, the conclusion is that: **The batch technique is important and efficiency-friendly in large-scale data importing. Once a reasonable batch size is chosen and rows are grouped together, the size will not affect importing time significantly.**

The reason behind it is clearly to understand: Batching allows multiple insert operations to be grouped into a single request, which reduces communication overhead, minimizes repeated SQL parsing, and lowers the number of round-trip interactions with the database engine. So the import process becomes much more efficient as soon as batching is introduced.

**The second group of experiments** focuses on the use of `PreparedStatement`.  In this part, I tried two importing  strategies: one using `PreparedStatement` with a preprocessing SQL sentence, the other straightforward constructs row SQL statement for each row. The only difference between two methods is the SQL construction method, and other factors stay the same. These experiments are repeated for 3 times to reduce disturb and finally calculate the result, which is shown in the following table:

| Method                                | Type              | Average time | Comparison    |
| ------------------------------------- | ----------------- | ------------ | ------------- |
| importUsersWithoutPreparedStatement() | Statement         | 182.149s     | 1×            |
| importUsersCsv()                      | PreparedStatement | 53.836s      | 3.383× faster |

And here is the picture for each runtime:

![task3experiment2](draft_Report.assets/task3experiment2.png)

The results clearly show that the use of `PreparedStatement` brings a improvement in import speed and efficiency. When raw SQL strings are executed directly, the import becomes much slower because the database must parse and optimize every command separately. In contrast, the prepared version finishes in significantly less time and shows much more stable performance across repeated trials. Therefore, the conclusion is that **PreparedStatement technique is crucial for efficient large-scale imports, and removing it leads to performance degradation.**



#### 2. Optimize import efficiency

Since we have chosen a suitable batch size and apply PreparedStatement in the initial import version, the following part will focus on further efficiency optimization.

In the original version, we used `EXISTS` keyword in the PreparedStatement to check the existence of users, and then insert the following relationship, so that the foreign key won't conflict. This method is safe but might be time-consuming. In order to optimize the importing script, we must find a new way to double-check the existance.

Since the task is essentially checking whether a user ID exists, `HashSet` (or `HashMap`) is an appropriate data structure. We rewrote the `importUsersCsv` method, using simple `String SQL_FOLLOW` statement and leave the existence checking to the Java side. After that, in the stage of inserting to the `following` table, we read all the authorId from database, and store them in a hashSet. Finally, before the batch is executed, we only check whether the hashSet contains the certain user, instead of check them in the SQL statement. We use the batch size of 1000 rows and the other condition stay the same as comparison version (previously mentioned, with the average time of 53.836 seconds). These experiments are repeated for 3 times to reduce disturb and finally calculate the result, which is shown in the following table:

| Method                   | Type                   | Average time | Comparison        |
| ------------------------ | ---------------------- | ------------ | ----------------- |
| importUsersCsv()         | Use SQL EXISTS keyword | 53.836s      | 1x                |
| importUsersWithHashSet() | use HashSet            | 42.228s      | **1.275× faster** |

And here is the picture for each runtime:

![task3optimization1](draft_Report.assets/task3optimization1.png)

The results demonstrate that replacing SQL `EXISTS` checks with a Java `HashSet` significantly improves the user import procedure. The original method using SQL `EXISTS` required an average of **53.836 seconds**, while the optimized version using `HashSet` took only **42.228 seconds** on average. This corresponds to an improvement of approximately **27%**.

The reason why this optimization works is checking foreign-key existence through SQL `EXISTS` requires PostgreSQL to do subqueries repeatedly. These repeated procedures significantly slow down the importing process. However, loading all valid user IDs into a Java `HashSet` allows each existence check to be completed in constant time **without contacting to the database**.Moreover, the application of hashSet filtered the redundency before insertion, which prevents unnecessary SQL statements from being executed. These factors result in observed approximately 27% improvement in overall import performance.

In summary, this optimization is particularly suitable when the reference table can be fully loaded into memory. Since all IDs are stored in a `HashSet`, the lookup cost remains constant even when the number of following relationships becomes large. For data at the scale of several hundred thousand records, memory consumption is negligible and the in-memory checking significantly reduces repeated SQL operations. Therefore, this method works well where the entire IDs set can be cached efficiently.

However, this optimization approach also have limitations. It may become less practical when the data grows to tens of millions of users, as the `HashSet` will require substantial memory. In such cases, the overhead of maintaining a large in-memory structure may offset the expected performance gain. Moreover, if the database schema or user table is frequently updated during import, the cached `HashSet` may be outdated and require refreshing. These factors limit this optimization for extremely large or dynamic datasets.

#### 3. Automated import

In the previous part, we first executed SQL DDL statements to create tables in DataGrip, and then executed import program in Java to complete this task. However, we can also integrate these two steps into a single automated workflow.  That is, the input for the script is a given csv file, and the output is the well-built table in the database.

We use a new class `Automation` that extends the previous import class (You can view the detailed program in the attached files) , and we use "Edit Configuration" in IDEA to read the provided csv files. The argument should be correct file path and divided by a space. In this project architecture, the argument is `data/user.csv data/recipes.csv data/reviews.csv `. In this way we can access csv files by `args[]`. This approach also has flexibility for changing csv files.

Then we specify the path to the SQL DDL file and execute it. In this project architecture, the path is`"SQL/create_table_ddl_statements.sql"`. We write a method `runSchemaFromFile` to execute this SQL file and to create the necessary tables and types.

Finally, after the schema is created, we execute the import procedure, which stays the same as its parent class.

For the correctness check, we compare the row counts of each table with previous manual approach and they are all the same. This confirms that the automated import works as intended: Give this script 3 original csv files, it will create the schema and insert each record into it, producing a complete and consistent database.