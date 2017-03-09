import org.json.simple.parser.ParseException;

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
    private static String JDBC_CONN = "jdbc:sqlite:cmu.db";

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
                    "(USER_ID CHAR(100) PRIMARY KEY NOT NULL," +
                    " VOTE_COUNT INT NOT NULL)";

            // Execute our statement to create our tables, and then close them out.
            voteTableStmt.executeUpdate(voteTableSQL);
            voteTableStmt.close();
            userTableStmt.executeUpdate(userTableSQL);
            userTableStmt.close();

            // Close out our connection
            c.close();

            logger.info("Tables set up successfully.");

        } catch (SQLException e) {
            // Something unexpected happened
            logger.severe("Error when setting up database tables. Specifically an error with SQL.");
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            // The SQL driver isn't available. This causes problems.
            logger.severe("Error when setting up database tables. Specifically: SQL JDBC driver not found.");
            e.printStackTrace();
        } catch (Exception e) {
            // Something else when wrong
            logger.severe("Error when setting up database tables.");
            e.printStackTrace();
        }
    }

    /**
     * Populate the vote count table with the rows required for the number of candidates. Initially, their vote counts
     * are 0.
     * @param candidateCount int must be greater than 1
     */
    public static void populateVoteTable(int candidateCount) {

        if (candidateCount < 1) {
            logger.warning("You didn't set a candidate count greater than 0.");
            return;
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
                sql += "('candidate-" + i + "', 0)";

                // Add a comma if we're not at the end of the loops
                sql += i == candidateCount ? "" : ", ";
            }

            logger.info(sql);

            // Execute our statement
            voteTableStmt.executeUpdate(sql);

            // Close out our statement and connection
            voteTableStmt.close();
            c.close();

            logger.info("Vote count table populated.");

        } catch (SQLException e) {
            // Something unexpected happened
            logger.severe("Error when populating vote count table. Specifically an error with SQL.");
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            // The SQL driver isn't available. This causes problems.
            logger.severe("Error when populating vote count table. Specifically: SQL JDBC driver not found.");
            e.printStackTrace();
        } catch (Exception e) {
            // Something else when wrong
            logger.severe("Error when populating vote count table.");
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
        Statement dropVoteTblStmt = null;
        Statement dropUserTbjStmt = null;

        try {
            // Confirm we have the sqlite jdbc driver available
            Class.forName("org.sqlite.JDBC");

            // Open up the connection and statements
            c = DriverManager.getConnection(JDBC_CONN);
            dropVoteTblStmt = c.createStatement();
            dropUserTbjStmt = c.createStatement();

            // SQL to drop our tables
            String sql = "DROP TABLE IF EXISTS ";

            // Execute the sql
            dropVoteTblStmt.execute(sql + VOTE_TABLE_NAME);
            dropUserTbjStmt.execute(sql + USER_TABLE_NAME);

            // Close our statements and connection
            dropVoteTblStmt.close();
            dropUserTbjStmt.close();
            c.close();

            logger.info("Successfully dropped tables. Database clear.");
        } catch (SQLException e) {
            // Something unexpected happened
            logger.severe("Error when populating vote count table. Specifically an error with SQL.");
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            // The SQL driver isn't available. This causes problems.
            logger.severe("Error when populating vote count table. Specifically: SQL JDBC driver not found.");
            e.printStackTrace();
        } catch (Exception e) {
            // Something else when wrong
            logger.severe("Error when populating vote count table.");
            e.printStackTrace();
        }
    }

    /**
     * Attempt to cast a vote for a user. Checks to make sure the user is still eligible to
     * vote (less than 3 previous votes), and then attempts to update the tables if true. If not, returns a false to
     * indicate the user is ineligible.
     * @param userId
     * @param candidateId
     * @return int whether it was successful (0), ineligible (1), or there was an error (2)
     * @throws Exception if there is an error when attempting to access the database
     */
    public static int castVote(String userId, String candidateId) throws Exception {
        logger.info("Starting to cast vote");

        // Prepare our variables for assignment
        Connection c = null;
        Statement voteCheckStmt = null;
        PreparedStatement castVoteStmt = null;
        PreparedStatement trackVoteStmt = null;


        boolean oldAutoCommit = true;
        int castRow = 0;
        int trackRow = 0;

        try {

            // Confirm we have the sqlite jdbc driver available
            Class.forName("org.sqlite.JDBC");

            // Open up the connection and statements
            c = DriverManager.getConnection(JDBC_CONN);

            // Get the current setting for autocommit, for the transaction process later.
            oldAutoCommit = c.getAutoCommit();

            voteCheckStmt = c.createStatement();

            String voteCheckSQL = "SELECT VOTE_COUNT FROM " + USER_TABLE_NAME + " WHERE USER_ID IS '" + userId+ "'";

            ResultSet rs = voteCheckStmt.executeQuery(voteCheckSQL);

            boolean userHasVoted = false;

            // Check if the user has voted before
            while (rs.next()) {
                userHasVoted = true;
                logger.info("Checking vote eligibility for user " + userId);
                // Check if the user has voted 3 times already. If so, return without casting a new vote.
                if (rs.getInt("VOTE_COUNT") >= 3) {
                    logger.info(userId + " is ineligible to vote.");
                    return 1;
                }
            }

            // The user is eligible to vote, so let's continue and make it happen.
            logger.info("Casting vote and tracking.");

            String castVoteSQL = "UPDATE " + VOTE_TABLE_NAME + " SET VOTE_COUNT = VOTE_COUNT + 1" +
                    " WHERE CANDIDATE_ID IS ?";
            String trackVoteSQL;
            if (userHasVoted) {
                trackVoteSQL = "UPDATE " + USER_TABLE_NAME + " SET VOTE_COUNT = VOTE_COUNT + 1 WHERE USER_ID IS ?";
            } else {
                trackVoteSQL = "INSERT INTO " + USER_TABLE_NAME + "(USER_ID, VOTE_COUNT) VALUES (?, 1)";
            }


            // Disable auto-committing, so we can treat the updates as a transaction
            c.setAutoCommit(false);

            castVoteStmt = c.prepareStatement(castVoteSQL);
            castVoteStmt.setString(1, candidateId);

            trackVoteStmt = c.prepareStatement(trackVoteSQL);
            trackVoteStmt.setString(1, userId);

            // Cast the vote and track it in our user table
            castRow = castVoteStmt.executeUpdate();
            trackRow = trackVoteStmt.executeUpdate();

            // Close out the connection and transaction
            c.commit();


            logger.info("Voted successfully");

        } catch (SQLException e) {
            // Something unexpected happened
            logger.severe("Error when populating vote count table. Specifically an error with SQL.");
            c.rollback();
            e.printStackTrace();
            return 2;
        } catch (ClassNotFoundException e) {
            // The SQL driver isn't available. This causes problems.
            logger.severe("Error when populating vote count table. Specifically: SQL JDBC driver not found.");
            e.printStackTrace();
            return 2;
        } catch (Exception e) {
            // Something else when wrong
            logger.severe("Error when populating vote count table.");
            e.printStackTrace();
            return 2;
        } finally {

            if (castVoteStmt != null) {
                castVoteStmt.close();
            }

            if (trackVoteStmt != null) {
                trackVoteStmt.close();
            }

            if (c != null) {
                c.setAutoCommit(oldAutoCommit);
                c.close();
            }
        }

        if (trackRow == 1 && castRow == 1) {
            return 0;
        }

        return 3;
    }
}
