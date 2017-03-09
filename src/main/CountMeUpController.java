import org.json.simple.JSONObject;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Logger;

/**
 * Controller class to manage the voting and returning of data for the vote counting. All data is returned as JSON
 * strings, to simplify the web service calls.
 */
public class CountMeUpController {
    private final static Logger logger = Logger.getLogger(CountMeUpController.class.getName());

    // Variable to manage how many candidates we want when we deploy. At some point this should move to config.
    private static int CANDIDATE_COUNT = 5;

    public static void setupTables() {
        try {
            CMUDatabaseManager.setupTables();
            CMUDatabaseManager.populateVoteTable(CANDIDATE_COUNT);
        } catch (Exception e) {
            // Something unexpected happened
            logger.severe("Error when attempting to setup or populate tables.");
            e.printStackTrace();

        }
    }

    /**
     * Teardown the database tables. Useful for testing.
     */
    public static void teardownTables() {
        CMUDatabaseManager.teardownTables();
    }

    /**
     * Attempt to vote. Sends a request through the CMUDatabaseManager, and returns a result based on whether it was
     * succesful.
     * @param userId
     * @param candidateId
     * @return String JSON string with the result of the voting request.
     */
    public static String vote(String userId, String candidateId) {
        String cleanUserId = userId.toLowerCase();
        JSONObject result = new JSONObject();

        try {
            // Attempt to vote
            int voteResult = CMUDatabaseManager.castVote(cleanUserId, candidateId);

            // If we were successful, prepare a successful response. Else not.
            if (voteResult == 0) {
                result.put("success","true");
            } else if (voteResult == 1) {
                result.put("success", "false");
                result.put("reason","vote limit");
            } else {
                result.put("success", "false");
                result.put("reason","exception");
            }

        } catch (Exception e){
            // Something went wrong
            logger.severe("Something went wrong when attempting to vote.");
            e.printStackTrace();

            result.put("success", "false");
            result.put("reason","exception");
        }

        return result.toString();


    }

    public static String countMeUp() {

        JSONObject result = CMUDatabaseManager.getVotes();

        return result.toString();
    }
}
