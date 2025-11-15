public class Main {
    public static void main(String[] args) {
        StopWatch watchA=new StopWatch();
        ImportDataVersion2 importDataVersion2=new ImportDataVersion2();
        ImportCompare importCompare=new ImportCompare();
        watchA.start();
        //importCompare.importUsersCsvWith100Batch("data/user.csv");
          //importCompare.importUsersCsvNoBatch("data/user.csv");
          //importCompare.importRecipesCsvNoBatch("data/recipes.csv");
          //importCompare.importReviewsCsvNoBatch("data/reviews.csv");
          importDataVersion2.importUsersCsv("data/user.csv");
          importDataVersion2.importRecipesCsv("data/recipes.csv");
        importDataVersion2.importReviewsCsv("data/reviews.csv");
        //importCompare.importUsersWithoutPreparedStatement("data/user.csv");
        watchA.stop();
        watchA.print("with no preparedStatement method");
        //importDataVersion2.importReviewsCsv3("data/reviews.csv");
    }
}