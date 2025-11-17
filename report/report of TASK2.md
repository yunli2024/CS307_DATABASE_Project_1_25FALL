## TASK2

### Database Design

In this TASK, I write DDL statements to create tables and columns according to the ER diagram. The SQL file is uploaded as attached file.Then I generate  the database diagram by using the" Show Visualization" feature of Datagrip. The picture is shown below: 

![visualization_ver2](report of TASK2.assets/visualization_ver2.png)



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