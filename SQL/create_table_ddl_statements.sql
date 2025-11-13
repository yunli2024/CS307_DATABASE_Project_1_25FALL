-- 建表DDL statement
CREATE TYPE gender_enum AS ENUM ('Male','Female');
-- users
CREATE TABLE users (
                       author_id        INT PRIMARY KEY,
                       author_name      TEXT NOT NULL,
                       gender           gender_enum,
                       age              INT,
                       followers_count  INT DEFAULT 0,
                       following_count  INT DEFAULT 0
);

-- following 成对属性
CREATE TABLE following (
                           follower_id INT NOT NULL REFERENCES users(author_id),
                           followee_id INT NOT NULL REFERENCES users(author_id),
                           PRIMARY KEY (follower_id, followee_id)
);

-- recipes
CREATE TABLE recipes(
                        recipe_id           INT PRIMARY KEY,
                        author_id           INT NOT NULL REFERENCES users(author_id),
                        recipe_name         TEXT NOT NULL,
                        description         TEXT,
                        recipe_category     TEXT,
                        recipe_yield        TEXT,
                        recipe_servings     INT,
                        aggregated_rating   NUMERIC(4,2),
                        review_count        INT
);

-- recipe_favorite 关系表 user favorite recipe
CREATE TABLE recipe_favorite (
                                 user_id   INT NOT NULL REFERENCES users(author_id),
                                 recipe_id INT NOT NULL REFERENCES recipes(recipe_id),
                                 PRIMARY KEY (user_id, recipe_id)
);

-- reviews
CREATE TABLE reviews(
                        review_id       INT PRIMARY KEY,
                        recipe_id       INT NOT NULL REFERENCES recipes(recipe_id),
                        user_id         INT NOT NULL REFERENCES users(author_id),
                        rating          INT NOT NULL,
                        review          TEXT NOT NULL,
                        date_submitted  DATE,
                        date_modified   DATE
);

-- likes 关系表
CREATE TABLE likes_relationship (
                                    user_id  INT NOT NULL REFERENCES users(author_id),
                                    review_id  INT NOT NULL REFERENCES reviews(review_id),
                                    PRIMARY KEY (user_id, review_id)
);

-- keyword & 关系
CREATE TABLE keyword(
                        keyword_id INT PRIMARY KEY,
                        keyword    TEXT NOT NULL UNIQUE
);

CREATE TABLE recipe_keyword(
                               recipe_id  INT NOT NULL REFERENCES recipes(recipe_id),
                               keyword_id INT NOT NULL REFERENCES keyword(keyword_id),
                               PRIMARY KEY (recipe_id, keyword_id)
);

-- ingredient & 关系
CREATE TABLE ingredient(
                           ingredient_id INT PRIMARY KEY,
                           ingredient    TEXT NOT NULL UNIQUE
);

CREATE TABLE recipe_ingredient(
                                  recipe_id     INT NOT NULL REFERENCES recipes(recipe_id),
                                  ingredient_id INT NOT NULL REFERENCES ingredient(ingredient_id),
                                  PRIMARY KEY (recipe_id, ingredient_id)
);

-- nutrition
CREATE TABLE nutrition(
                          recipe_id        INT PRIMARY KEY REFERENCES recipes(recipe_id),
                          calories         NUMERIC(7,1) NOT NULL,
                          fat              NUMERIC(7,1),
                          saturated_fat    NUMERIC(7,1),
                          cholesterol      NUMERIC(7,1),
                          protein          NUMERIC(7,1),
                          sugar            NUMERIC(7,1),
                          fiber            NUMERIC(7,1),
                          carbohydrate     NUMERIC(7,1),
                          sodium           NUMERIC(7,1)
);

-- recipe_time
CREATE TABLE recipe_time(
                            recipe_id       INT PRIMARY KEY REFERENCES recipes(recipe_id),
                            prepare_time    TIME,
                            cook_time       TIME,
                            total_time      TIME,
                            date_published  DATE
);

-- recipe_instruction 关系表
CREATE TABLE recipe_instruction (
                                    recipe_id      INT NOT NULL REFERENCES recipes(recipe_id),
                                    instruction    TEXT NOT NULL,
                                    primary key (recipe_id,instruction)
);



