## README

#### This is course project1 for CS307 Principle of Database 25Fall in SUSTech.  

#### **Project Architecture**

```text
CS307_DATABASE_Project_1_25FALL/
│
├── .idea/
│
├── SQL/
│   ├── create_table_ddl_statements.sql
│   └── select_table_records_number.sql
│
├── data_preprocessing/
│   └── data preprocess.py
│
├── diagram/
│   ├── Task4_Advance1_HighCurrency/
│   ├── task3_experiment1_screen/
│   ├── task3_experiment2_screen/
│   ├── task3_optimization1_screen/
│   ├── result_preprocessing.png
│   ├── task3experiment1.png
│   ├── task3experiment2.png
│   ├── task3optimization1.png
│   └── visualization_ver2.png
│
├── report/
│   ├── Report of Task 3.assets/
│   ├── CS307 task4 basic part.md
│   ├── Data Preprocessing.md
│   ├── Report of Task 3.md
│   ├── draft_Report.md
│   ├── report of TASK2.md
│   ├── report of Task1.md
│   ├── report of Task1——update1.md
│   ├── report of task4 bonus EN.md
│   └── report of task4.md
│
├── src/
│   ├── Advanced2/
│   ├── Experimental Results/
│   ├── task 4 code/
│   ├── task4/
│   ├── task4_basic_version2/
│   ├── Automation.java
│   ├── Client.java
│   ├── DataFactory.java
│   ├── DataManipulation.java
│   ├── DatabaseImport.java
│   ├── DatabaseManipulation.java
│   ├── FileManipulation.java
│   ├── High_currency_DB.java
│   ├── High_currency_File.java
│   ├── ImportCompare.java
│   ├── ImportDataVersion2.java
│   ├── Main.java
│   ├── Optimization.java
│   ├── StopWatch.java
│   └── user_10000.java
│
├── CS307 Fall 2025 Project Part I.pdf
├── Report_final_version.pdf
├── LICENSE
├── README.md
├── cs307_project1_25fall.iml
├── mysql-connector-j-9.5.0.jar
├── postgresql-42.2.5.jar
└── .gitignore

```

#### 1. Database Schema Overview

This project designs a **fully normalized PostgreSQL relational database** (13 tables) based on the SUSTC Recipes dataset (500k+ recipes, 1.4M+ reviews, 300k users).
 The schema satisfies:

- **1NF** — all multi-valued fields normalized into relationship tables
- **2NF** — no partial dependency in composite keys
- **3NF** — no transitive dependency; derived attributes removed
- **No circular foreign-key relationships**
- **Every table reachable & expandable**

#### Main Entities

- **users, recipes, reviews**
- **keyword, ingredient, nutrition, recipe_time**

#### Relationship Tables

- **following**, **recipe_favorite**, **likes_relationship**
- **recipe_keyword**, **recipe_ingredient**, **recipe_instruction**

Full DDL is under:
 `SQL/create_table_ddl_statements.sql`

###  2. Data Import Pipeline

The import workflow is implemented in **Java (JDBC)** and supports:

 Robust CSV parsing
  Type conversion (ISO-8601 time → SQL TIME, numeric cleaning, etc.)
  Batch insertion (1000 rows per batch)
  `PreparedStatement` for performance
  Automatic dictionary-table insertion (`keyword`, `ingredient`)
  Relationship extraction (favorites, keywords, ingredients, instructions, likes, followings)

### Import Steps

1. **Connect to PostgreSQL**
2. **Parse CSV records safely**, supporting embedded commas, quotes, multi-line fields
3. **Insert into multiple tables** using 7 PreparedStatements per recipe
4. **Execute in batches** and skip malformed records
5. **Verify correctness** via `COUNT(*)` (script included)

### CSV → Tables Mapping

| CSV File      | Import Method      | Affected Tables                                              |
| ------------- | ------------------ | ------------------------------------------------------------ |
| `users.csv`   | importUsersCsv()   | users, following                                             |
| `recipes.csv` | importRecipesCsv() | recipes, nutrition, recipe_time, recipe_keyword, recipe_ingredient, recipe_instruction, recipe_favorite, keyword, ingredient |
| `reviews.csv` | importReviewsCsv() | reviews, likes_relationship                                  |

### Automated Workflow

Run **Automation.java**:

- Execute schema DDL
- Import 3 CSVs
- Validate correctness

Input format example:

```
data/users.csv data/recipes.csv data/reviews.csv
```

###  3. Data Preprocessing

Preprocessing is handled with Python (`preprocess/data_preprocess.py`):

### Fixes include:

- Removing **19 malformed review rows** (extra comma → missing AuthorId)
- Adjusting inconsistent attributes:
  - TotalTime mismatch
  - followers_count / following_count inconsistency
- Normalizing datatypes:
  - RecipeId with `.0`
  - ISO-8601 duration → seconds
- Cleaning odd characters, unwanted quotes

Cleaned files saved under `/data`.

###  4. Performance Evaluation (Task 4)

Experiments compare **DBMS vs File I/O** for:

- INSERT
- DELETE
- UPDATE
- SELECT
- High-concurrency (1 → 100 threads)
- PostgreSQL vs MySQL (advanced)

###  5. Advanced Optimization

### **Software-Level**

- Large batch size (1000)
- PreparedStatement reuse
- HashSet cache for foreign-key existence
   → ~27% faster on users.csv import
- Potential improvement: `COPY FROM STDIN` via `CopyManager`

### **Hardware-Level**

- NVMe SSD
- WAL optimization
- cache tuning (`shared_buffers`, `work_mem`, `effective_cache_size`)

### 6. License

This repository will be released under the **MIT License** after the project deadline.
