import java.sql.*;
import java.util.logging.Logger;

/**
 * Class to manage database connections, and any database calls.
 */
public class CMUDatabaseManager {
    
    private final static Logger logger = Logger.getLogger(CMUDatabaseManager.class.getName());

    // The names of our tables for tracking the votes for each candidate, and the votes by given user.
    private static String VOTE_TABLE_NAME = "VOTES_BY_CANDIDATE";
    private static String USER_TABLE_NAME = "VOTES_BY_USER";

    // The connection string used for the database. May update this to test performance.
    private static String JDBC_CONN = "jdbc:sqllite:cmu.db";

    /**
     * Setup our database tables to hold our votes and our counts per user.
     */
    public static void setupTables() {

        logger.info("Setting up tables.");

        // Prepare our variables for assignment
        Connection c = null;
        Statement voteTableStmt = null;
        Statement userTableStmt = null;

        try {
            // Confirm we have the sqlite jdbc driver available
            Class.forName("org.sqlite.JDBC");

            // Open up the connection and statements
            c = DriverManager.getConnection(JDBC_CONN);
            voteTableStmt = c.createStatement();
            userTableStmt = c.createStatement();

            // SQL definitions of our tables for user vote counts and votes per candidate
            String voteTableSQL = "CREATE TABLE " + VOTE_TABLE_NAME + " " +
                                  " (CANDIDATE_ID CHAR(50) PRIMARY KEY NOT NULL," +
                                  " VOTE_COUNT INT NOT NULL)";

            String userTableSQL = "CREATE TABLE " + USER_TABLE_NAME + " " +
                    "(USER_ID CHAR(50) PRIMARY KEY NOT NULL," +
                    " VOTE_COUNT INT NOT NULL)";

            // Execute our statement to create our tables, and then close them out.
            voteTableStmt.executeUpdate(voteTableSQL);
            voteTableStmt.close();
            userTableStmt.executeUpdate(userTableSQL);
            userTableStmt.close();

            // Close out our connection
            c.close();

            logger.info("Tables set up successfully.");

        } catch (Exception e) {
            // Something unexpected happened
            logger.severe("Error when setting up database tables");
            e.printStackTrace();
        }
    }

    /**
     * Populate the vote count table with the rows required for the number of candidates. Initially, their vote counts
     * are 0.
     * @param candidateCount int must be greater than 1
     */
    public static void populateVoteTable(int candidateCount) throws Exception {

        if (candidateCount < 1) {
            logger.warning("You didn't set a candidate count greater than 0.");
            throw new Exception("candidateCount must be a postitive integer greater than or equal to 1.");
        }

        logger.info("Populating vote count table.");

        // Prepare our variables for assignment
        Connection c = null;
        Statement voteTableStmt = null;

        try {
            // Confirm we have the sqlite jdbc driver available
            Class.forName("org.sqlite.JDBC");

            // Open up the connection and statements
            c = DriverManager.getConnection(JDBC_CONN);
            voteTableStmt = c.createStatement();

            // Generate our SQL string to insert the values in our table.
            String sql = "INSERT INTO " + VOTE_TABLE_NAME + " (CANDIDATE_ID, VOTE_COUNT) VALUES ";

            // Loop the number of times as there are candidates - this allows us to have a variable number of candidates
            // when we deploy our application.
            for (int i = 1; i <= candidateCount; i++) {
                sql += "(Candidate-" + i + ", 0)";

                // Add a comma if we're not at the end of the loops
                sql += i == candidateCount ? "" : ", ";
            }

            // Execute our statement
            voteTableStmt.executeUpdate(sql);

            // Close out our statement and connection
            voteTableStmt.close();
            c.close();

            logger.info("Vote count table populated.");

        } catch (Exception e) {
            // Something unexpected happened
            logger.severe("Error when populating vote count table");
            e.printStackTrace();
        }
    }

    /**
     * Completely clear out the database, ready for a new run. Useful for when we are running tests.
     */
    public static void teardownTables() {
        logger.info("Clearing out the database.");

        // Prepare our variables for assignment
        Connection c = null;
        Statement stmt = null;

        try {
            // Confirm we have the sqlite jdbc driver available
            Class.forName("org.sqlite.JDBC");

            // Open up the connection and statements
            c = DriverManager.getConnection(JDBC_CONN);
            stmt = c.createStatement();

            // SQL to drop our tables
            String sql = "DROP TABLE IF EXISTS " + VOTE_TABLE_NAME + ", " + USER_TABLE_NAME;

            // Execute the sql
            stmt.execute(sql);

            // Close our statement and connection
            stmt.close();
            c.close();

            logger.info("Successfully dropped tables. Database clear.");
        } catch (Exception e) {
            // Something unexpected happened
            logger.severe("Error when populating vote count table");
            e.printStackTrace();
        }
    }
}
