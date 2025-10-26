# report of Task1

> first drift CS307 pj1 task1

> 这一部分的文字说明在要求是没有的，example有，到时候可以根据篇幅调整保留/删除

# ER map

tool: draw.io

![image.png](https://resv2.craft.do/user/full/97941634-45dc-f568-13d7-337e127e5c7c/doc/c5e1054b-42c0-4cc6-8ef2-e22c6e2fd8ab/963f3545-0cb2-4080-8d9c-9a8fa3e44cbe)

### Entities:

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

### Relationship:

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

?descriptionFromFileType=function+toLocaleUpperCase()+{+[native+code]+}+File&mimeType=application/octet-stream&fileName=report+of+Task1.md&fileType=undefined&fileExtension=md