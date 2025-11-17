-- select statement
SELECT 'users'               AS table, COUNT(*) FROM users
UNION ALL
SELECT 'following'           AS table, COUNT(*) FROM following
UNION ALL
SELECT 'recipes'             AS table, COUNT(*) FROM recipes
UNION ALL
SELECT 'recipe_time'         AS table, COUNT(*) FROM recipe_time
UNION ALL
SELECT 'nutrition'           AS table, COUNT(*) FROM nutrition
UNION ALL
SELECT 'reviews'             AS table, COUNT(*) FROM reviews
UNION ALL
SELECT 'likes_relationship'  AS table, COUNT(*) FROM likes_relationship
UNION ALL
SELECT 'keyword'             AS table, COUNT(*) FROM keyword
UNION ALL
SELECT 'ingredient'          AS table, COUNT(*) FROM ingredient
UNION ALL
SELECT 'recipe_keyword'      AS table, COUNT(*) FROM recipe_keyword
UNION ALL
SELECT 'recipe_ingredient'   AS table, COUNT(*) FROM recipe_ingredient
UNION ALL
SELECT 'recipe_favorite'     AS table, COUNT(*) FROM recipe_favorite
UNION ALL
SELECT 'recipe_instruction'  AS table, COUNT(*) FROM recipe_instruction;
