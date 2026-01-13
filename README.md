# CS307 Database Project 1 (Fall 2025)

> **Course**: CS307 – Principles of Database
> **University**: Southern University of Science and Technology (SUSTech)
> **Final Score**: ⭐ **99 / 100**

This repository contains **Project 1** for CS307, focusing on **relational database design, data preprocessing, large-scale data import, and performance evaluation** using PostgreSQL and MySQL.

------

## 📁 Project Structure

```text
CS307_DATABASE_Project_1_25FALL/
│
├── SQL/                         # Database schema & validation scripts
│   ├── create_table_ddl_statements.sql
│   └── select_table_records_number.sql
│
├── data_preprocessing/          # Python preprocessing scripts
│   └── data preprocess.py
│
├── diagram/                     # Experimental results & visualizations
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
├── report/                      # Task reports (Markdown & assets)
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
├── src/                         # Java source code (JDBC + experiments)
│   ├── Advanced2/
│   ├── Experimental Results/
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
├── mysql-connector-j-9.5.0.jar
├── postgresql-42.2.5.jar
├── LICENSE
├── README.md
└── .gitignore
```

------

## 🗄️ 1. Database Schema Overview

This project designs a **fully normalized PostgreSQL relational database** with **13 tables**, based on the **SUSTech Recipes Dataset**, containing:

- **500,000+ recipes**
- **1.4 million+ reviews**
- **300,000+ users**

### Normalization Guarantees

- **1NF** – All multi-valued attributes decomposed into relationship tables
- **2NF** – No partial dependency on composite keys
- **3NF** – No transitive dependency; derived attributes removed
- ❌ No circular foreign-key dependencies
- ✅ Every table is reachable and extensible

### Core Entities

- `users`
- `recipes`
- `reviews`
- `keyword`
- `ingredient`
- `nutrition`
- `recipe_time`

### Relationship Tables

- `following`
- `recipe_favorite`
- `likes_relationship`
- `recipe_keyword`
- `recipe_ingredient`
- `recipe_instruction`

📄 **Full schema definition**:
`SQL/create_table_ddl_statements.sql`

------

## 🚀 2. Data Import Pipeline (Java + JDBC)

The data import pipeline is implemented in **Java (JDBC)** and is designed for **robustness, correctness, and performance**.

### Key Features

- Safe CSV parsing
  - Embedded commas
  - Quoted fields
  - Multi-line text
- Automatic type conversion
  - ISO-8601 → SQL `TIME`
  - Numeric field cleanup
- Batch insertion (**1000 rows per batch**)
- `PreparedStatement` reuse for performance
- Automatic dictionary table population (`keyword`, `ingredient`)
- Relationship extraction:
  - Favorites
  - Keywords
  - Ingredients
  - Instructions
  - Likes
  - Followings

------

### Import Workflow

1. Connect to PostgreSQL via JDBC
2. Parse CSV records safely
3. Insert records into multiple tables
4. Execute batch inserts & skip malformed rows
5. Validate correctness using `COUNT(*)`

Validation script included:

```sql
SQL/select_table_records_number.sql
```

------

### CSV → Table Mapping

| CSV File      | Import Method        | Affected Tables                                              |
| ------------- | -------------------- | ------------------------------------------------------------ |
| `users.csv`   | `importUsersCsv()`   | users, following                                             |
| `recipes.csv` | `importRecipesCsv()` | recipes, nutrition, recipe_time, recipe_keyword, recipe_ingredient, recipe_instruction, recipe_favorite, keyword, ingredient |
| `reviews.csv` | `importReviewsCsv()` | reviews, likes_relationship                                  |

------

### ▶ Automated Execution

Run **`Automation.java`** to complete the entire workflow:

- Execute schema DDL
- Import all CSV files
- Validate correctness automatically

**Example input:**

```text
data/users.csv data/recipes.csv data/reviews.csv
```

------

## 🧹 3. Data Preprocessing (Python)

Data preprocessing is handled by:

```text
data_preprocessing/data preprocess.py
```

### Preprocessing Fixes

- ❌ Removed **19 malformed review rows**
  - Caused by extra commas → missing `AuthorId`
- 🔧 Resolved data inconsistencies:
  - `TotalTime` mismatches
  - `followers_count` / `following_count` mismatch
- 🔄 Normalized data types:
  - `RecipeId` ending with `.0`
  - ISO-8601 duration → seconds
- 🧼 Cleaned:
  - Odd Unicode characters
  - Redundant quotes

✅ Cleaned CSV files are saved under `/data`.

------

## ⚡ 4. Performance Evaluation (Task 4)

Performance experiments compare:

- **Database (PostgreSQL / MySQL)** vs **File I/O**
- Operations tested:
  - `INSERT`
  - `DELETE`
  - `UPDATE`
  - `SELECT`
- High-concurrency scenarios:
  - **1 → 100 threads**
- Advanced comparison:
  - PostgreSQL vs MySQL

📊 Results and screenshots are available in the `diagram/` directory.

------

## 🧠 5. Advanced Optimization

### Software-Level Optimizations

- Batch insertion (1000 rows)
- PreparedStatement reuse
- `HashSet` cache for foreign-key existence checks
  → **~27% speedup** on `users.csv` import
- Potential future improvement:
  - PostgreSQL `COPY FROM STDIN` via `CopyManager`

------

### Hardware / DB-Level Optimizations

- NVMe SSD
- WAL optimization
- PostgreSQL tuning:
  - `shared_buffers`
  - `work_mem`
  - `effective_cache_size`

------

## 📄 6. License

This repository will be released under the **MIT License** after the course project deadline.
