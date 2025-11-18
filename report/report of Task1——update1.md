# report of Task1——update1

# ER map

tool: draw.io

![image.png](https://resv2.craft.do/user/full/97941634-45dc-f568-13d7-337e127e5c7c/doc/c5e1054b-42c0-4cc6-8ef2-e22c6e2fd8ab/dea7bcc3-2869-4b05-8cf4-73c8d885ecf7)

### Entities:

- **User**: Represents the users in the system
- **Review**: Comments on recipes
- **Recipe**: Information about a recipe
- **Keyword**: Keywords associated with the recipe
- **Ingredient**: Ingredients of the recipe
- **Nutrition**: Nutritional information of the recipe
- **Recipe_Nutrition**: A weak entity dependent on the Recipe, representing the nutritional components of the recipe
- **Recipe_Time**: A weak entity dependent on the Recipe, representing the preparation time related to the recipe

### Relationship:

1. User **generate** Review: one-to-many
2. User **follows** User: many-to-many
3. User **favourites** Recipe: many-to-many
4. User **review** Recipe**:** many-to-many
5. User **create** Recipe**:** one-to-many
6. Recipe **have** Review: one-to-many
7. Recipe **have** Keyword: many-to-many
8. Recipe **have** Ingredient: many-to-many
9. Recipe_Nutrition: one-to-one
10. Recipe_Time: one-to-one

?descriptionFromFileType=function+toLocaleUpperCase()+{+[native+code]+}+File&mimeType=application/octet-stream&fileName=report+of+Task1——update1.md&fileType=undefined&fileExtension=md