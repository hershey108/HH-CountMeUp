import org.json.simple.JSONObject;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Random;
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

    /**
     * Calls the CMUDatabase manager to return the current tally of votes for all candidates.
     * @return String JSON string of the results from the votes table
     */
    public static String countMeUp() {

        JSONObject result = CMUDatabaseManager.getVotes();

        return result.toString();
    }

    /**
     * Simulates a large number of votes, to demonstrate realtime updates on the frontend.
     */
    public static void simulateVotes() {
        logger.info("Starting simulating votes");
        // track how long this takes.
        long start = System.currentTimeMillis();
        int simulations = 10000;

        // Get a random number generator, and define the min and max values. Bear in mind, the max is exclusive of the
        // calculation, hence we pick one higher than the CANDIDATE_COUNT
        Random gen = new Random();
        int min = 1;
        int max = CANDIDATE_COUNT+1;

        int currentCandidate;

        for (int i = 0; i < simulations; i++) {
            currentCandidate = gen.nextInt(max - min) +1;
            vote("sim"+i,"candidate-"+currentCandidate);
        }

        long executionTime = System.currentTimeMillis() - start;
        logger.info("Simulation complete. Execution Time: " + (executionTime/1000) + "s");
    }
}
