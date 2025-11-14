public class Main {
    public static void main(String[] args) {
        ImportDataVersion2 importDataVersion2=new ImportDataVersion2();
       importDataVersion2.importUsersCsv("data/user.csv");
        importDataVersion2.importRecipesCsv("data/recipes.csv");
        importDataVersion2.importReviewsCsv("data/reviews.csv");
        //importDataVersion2.importReviewsCsv3("data/reviews.csv");
    }
}