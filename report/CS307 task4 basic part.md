# CS307 task4 basic part

1. ### 测试环境

#### a. 硬件规格

CPU 型号：Intel Core Ultra 5 125H @ 3.60GHz

内存容量：32GB

存储：1 TB NVMe SSD

#### b. 软件规格

DBMS 版本：PostgreSQL 17.6 (64-bit)

操作系统：Windows 11

编程语言：Java

开发环境：Java 17 (JDK 17.0.4.1), javac 17.0.4.1

JDBC 驱动: postgresql-42.2.5.jar

### 2.测试数据组织方式

#### 数据存储说明

- user.csv：存储全部用户数据（由教师提供），用于向 DBMS 的user表和文件系统导入初始数据。
- user_10000.csv：存储 10000 行用户数据（自行创建），随机抽取user中的10000条，在author_id上加上3000000，防止违反主键的唯一性导致插入失败。用于 DBMS 的 recipe 表插入测试和文件系统的 recipe.txt 插入测试。
- users表：DBMS 中存储用户数据的表（自行创建），数据均来源于 user.csv，用于数据库系统的插入、更新、查询和删除测试
- users.txt：文件系统中存储全部菜谱数据的文件（自行创建），用于文件系统的插入、更新、查询和删除测试。

#### DBMS中的数据组织

在PostgreSQL数据库中，测试数据通过关系型表结构进行组织。具体实现如下：

**表结构设计**：使用users表存储食谱信息，表包含以下字段authou_id（主键）、author_name、gender、age、followers_count、following_count。

- **数据类型**：根据字段含义使用不同的数据类型，如整数型（author_id）、文本型（author_name）、枚举型（gender）等，确保数据类型的准确性和存储空间的优化。
- **约束条件**：author_id作为主键，确保每条记录的唯一性，便于快速定位和查询。
- **索引优化**：数据库自动为主键字段创建索引，显著提升查询性能，尤其是针对ID的精确查询操作。

#### 文件系统中的数据组织

文件系统中，测试数据以文本文件的形式组织，具体实现如下：

- **文件格式**：使用简单的文本文件（users.txt）存储所有用户记录。
- **记录分隔**：每条记录占一行，通过换行符（\n）分隔不同记录。
- **字段分隔**：每条记录内部使用分号（;）作为字段分隔符，将不同属性分开。
- **数据结构**：没有显式的数据类型区分，所有数据以字符串形式存储，需要在程序运行时进行类型转换。
- **无索引机制**：文件系统本身不提供索引支持，查询时需要顺序扫描整个文件。

#### 测试SQL语句的生成方法

所有增删改查（CRUD）操作的SQL语句均采用统一的预编译语句（PreparedStatement）方式生成，具体特点如下：

- **参数化查询模式**：使用占位符（?）替代直接字符串拼接，这是一种广泛应用于各种编程语言和数据库系统的安全实践，能够有效防止SQL注入攻击。
- **动态参数绑定机制**：通过类型化的参数绑定方法将应用程序变量安全地映射到SQL参数，确保数据类型转换的正确性和安全性。
- **SQL语句结构标准化**：采用标准SQL语法统一构建所有CRUD操作语句，无论是SELECT查询、INSERT插入、UPDATE更新还是DELETE删除，均遵循相同的预编译语句生成机制。
- **错误处理框架**：实现统一的异常捕获和处理机制，确保SQL执行过程中的错误能够被正确捕获并返回有意义的错误信息。
- **资源生命周期管理**：遵循连接-执行-关闭的标准资源管理模式，确保数据库连接等资源在操作完成后被正确释放。
- **SQL语句复用机制**：通过预编译语句机制提高SQL执行效率，减少重复解析和优化开销。

#### 文件操作机制

文件操作采用标准的Java I/O库实现：

- **读取操作**：使用`BufferedReader`逐行读取文件内容，提高读取效率。
- **写入操作**：使用`FileWriter`直接写入文件，支持追加模式。
- **临时文件**：对于删除和更新操作，采用临时文件策略，避免边读边写的冲突。
- **文件替换**：操作完成后通过删除原文件并重命名临时文件的方式完成更新。

### 3.测试SQL脚本与程序源代码描述

测试涉及 5 个源代码文件：Client.java、DatabaseManipulation.java、DataFactory.java、DataManipulation.java、FileManipulation.java，文件功能如下：

#### Client.java

调用文件测试和 DBMS 测试的插入、查询、更新、删除函数，并打印测试结果。

#### DataManipulation.java

包含插入、查询、更新、删除四种测试功能的接口。

#### DataFactory.java

包含 createDataManipulation 函数，根据输入字符串参数创建数据操作对象 —— 输入字符串含 “file” 则返回文件操作对象，含 “database” 则返回数据库操作对象。

#### DatabaseManipulation.java

实现了DataManipulation接口，通过JDBC与PostgreSQL数据库交互，主要功能包括：

**连接管理**：通过`getConnection()`和`closeConnection()`方法管理数据库连接，使用PostgreSQL JDBC驱动。

**增删改查操作**：

**添加操作（addUsers）**：

- 使用参数化SQL插入语句 INSERT INTO task4advance2.users (authorid, authorname, gender, age, followers, following) VALUES (?, ?, ?, ?, ?, ?)
- 实现细节：先通过分号分割输入字符串，然后使用setInt()、setString()方法绑定参数
- 返回总的操作时间作为操作结果

**查询操作（queryUsers）**：

- 使用参数化SQL查询语句 SELECT COUNT(*) FROM task4advance2.users WHERE authorid = ?
- 查找authorid与插入的csv中authorid一样的数据数
- 通过setInt()方法绑定authorId参数
- 返回总的操作时间作为操作结果

**更新操作（**updateUsersAge**）**：

- 使用参数化SQL更新语句 UPDATE task4advance2.users SET age = age + 1 WHERE authorid = ?
- 将与插入的csv中authorid一样的用户年龄加一
- 返回总的操作时间作为操作结果

**删除操作（**deleteUsers**）**：

- 使用参数化SQL删除语句DELETE FROM task4advance2.users WHERE authorid = ? WHERE authorid
 = ?`RecipeId = ?`
- 删除与插入的csv中authorid一样的用户
- 通过setInt()方法绑定RecipeId参数
- 返回受影响的行数，0表示未找到匹配记录

**性能监控**：

- 所有操作方法均实现了耗时统计功能
- 在操作完成（包括异常情况）后打印总耗时

#### FileManipulation.java

实现了`DataManipulation`接口，通过文件I/O操作管理食谱数据，主要功能包括：

**文件路径管理**：通过常量`RECIPES_FILE`（值为"recipes.txt"）统一管理数据文件路径。

**数据格式**：使用分号加空格（"; "）作为字段分隔符，每条记录占一行。

**增删改查操作**：

**添加操作（addUsers）**：

- 使用`FileWriter`以追加模式（append=true）打开文件
- 自动检测并添加换行符，确保每条记录单独占一行
- 返回添加时间

**查询操作（queryUsers）**：

- 使用`BufferedReader`逐行读取文件内容
- 顺序扫描直到找到匹配ID或遍历完文件
- 返回查询时间

**删除操作（**deleteUsers**）**：

- 采用临时文件策略：创建temp_recipes.txt作为临时文件
- 使用`BufferedReader`读取原文件，`FileWriter`写入临时文件
- 逐行检查记录ID，只写入ID不匹配的记录
- 操作完成后，删除原文件并重命名临时文件为原文件名
- 返回操作时间

**更新操作（**updateUsersAge**）**：

- 同样采用临时文件策略
- 找到目标ID记录后，使用`StringBuilder`构建更新后的行
- 保留其他所有字段不变
- 操作完成后进行文件替换
- 返回操作时间

**性能监控**：

- 所有操作方法均实现了耗时统计功能
- 在操作完成（包括异常情况）后打印总耗时

**异常处理与资源管理**：

- 使用try-with-resources语法自动管理文件资源（BufferedReader、FileWriter）
- 捕获并打印所有IOException异常

4，性能对比分析

| 操作类型           | DBMS 测试耗时 | 文件 I/O 测试耗时 |
| -------------- | --------- | ----------- |
| 插入（insertTest） | 1681      |             |
| 删除（deleteTest） | 1556      |             |
| 查询（selectTest） | 1022      |             |
| 更新（updateTest） | 2020      |             |

测试结果表明，处理数据（尤其是大规模或复杂数据集）时，使用 PostgreSQL 等数据库系统远优于文件系统管理数据。DBMS 与文件 I/O 的性能差异显著。综上，数据库系统在数据处理方面较直接使用文件具有明显优势。

?descriptionFromFileType=function+toLocaleUpperCase()+{+[native+code]+}+File&mimeType=application/octet-stream&fileName=CS307+task4+basic+part.md&fileType=undefined&fileExtension=md