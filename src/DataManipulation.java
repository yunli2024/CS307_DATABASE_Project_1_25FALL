package task4;
//增删改查各一个，已完成
public interface DataManipulation {//接口实现数据操作中的方法
    public int addOneRecipe(String recipeData);
    public int deleteRecipeById(int recipeId);
    public int updateRecipeRating(int recipeId, double newRating);
    public String findRecipeById(int recipeId);
}
