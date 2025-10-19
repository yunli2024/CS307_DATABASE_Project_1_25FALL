public class Main {
    public static void main(String[] args) {
        DatabaseImport databaseImport=new DatabaseImport();
        databaseImport.importRecipesCsv("data/recipes.csv");
        databaseImport.importReviewsCsv("data/reviews.csv");
        databaseImport.importUserCsv("data/user.csv");
    }
}