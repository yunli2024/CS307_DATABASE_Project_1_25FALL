-- 建表DDL statement

-- users
create type gender_enum as enum ('Male','Female');
create table users (
    author_id int primary key ,
    author_name text not null ,
    gender gender_enum,
    age int
);

-- follow关系，成对出现的形式
create table following (
    follower_id int not null references users(author_id),
    followee_id int not null references users(author_id),
    primary key (follower_id, followee_id)
);

-- recipes
create table recipes(
    recipe_id int primary key ,
    author_id int not null references users(author_id),
    recipe_name text not null,
    description text,
    recipe_instructions text,
    recipe_category text,
    recipe_yield text,
    recipe_servings int,
    aggregated_rating numeric(3,2),
    review_count int
);
create table recipe_favorite (
    user_id   int not null references users(author_id),
    recipe_id int not null references recipes(recipe_id),
    primary key (user_id, recipe_id)
);

--reviews
create table reviews(
    review_id int primary key ,
    recipe_id int not null references recipes(recipe_id),
    user_id int not null references users(author_id),
    rating int not null,-- 数据类型？
    review text not null,
    date_submitted date, --导入的时候注意
    date_modified date
);

-- likes 不需要实体表吗？
--关系表
create table likes_relationship (
    author_id  int not null references users(author_id),
    review_id  int not null references reviews(review_id),
    primary key (author_id, review_id)
);

--keyword 实体表
create table keyword(
    keyword_id int primary key ,
    keyword text not null unique  -- 防止重复！
);
-- 关系表
create table recipe_keyword(
    recipe_id int not null references recipes(recipe_id),
    keyword_id int not null references keyword(keyword_id),
    primary key (recipe_id,keyword_id)
);

-- ingredient 实体表
create table ingredient(
    ingredient_id int primary key ,
    ingredient text not null unique --fangzhi chongfu
);
-- 关系表
create table recipe_ingredient(
    recipe_id int not null references recipes(recipe_id),
    ingredient_id int not null references ingredient(ingredient_id),
    primary key (recipe_id,ingredient_id)
);

--nutrition

create table nutrition(
    recipe_id int primary key references recipes(recipe_id),
    calories numeric(7,1) not null,-- 999999.9 希望不要超范围
    fat numeric(7,1),
    saturated_fat numeric(7,1),
    cholesterol numeric(7,1),
    protein numeric(7,1),
    sugar numeric(7,1),
    fiber numeric(7,1),
    carbohydrate numeric(7,1),
    sodium numeric(7,1)
);


--time
create table recipe_time(
    recipe_id int primary key references recipes(recipe_id), -- 1对1主外键
    prepare_time interval, --这两个类型应该还要改，思考一下导入数据的时候怎么转化？？
    cook_time interval,
    date_published date
);



