public class Main {
    public static void main(String[] args) {
        StopWatch watchA=new StopWatch();
        ImportDataVersion2 importDataVersion2=new ImportDataVersion2();
        ImportCompare importCompare=new ImportCompare();
        Optimization optimization=new Optimization();
        watchA.start();

        // group1 normal import
        importDataVersion2.importUsersCsv("data/user.csv");
        importDataVersion2.importRecipesCsv("data/recipes.csv");
        importDataVersion2.importReviewsCsv("data/reviews.csv");


        // group2 batches
        //importCompare.importUsersCsvWith100Batch("data/user.csv");
        //importCompare.importUsersCsvNoBatch("data/user.csv");
        //importCompare.importRecipesCsvNoBatch("data/recipes.csv");
        //importCompare.importReviewsCsvNoBatch("data/reviews.csv");


        // group3 preparedStatement
        //importCompare.importUsersWithoutPreparedStatement("data/user.csv");


        // group4 optimization with hashSet
        // optimization.importUsersCsvWithHashSet("data/user.csv");
        watchA.stop();
        watchA.print("with HashSet method");

    }
}