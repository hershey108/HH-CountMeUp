import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.logging.Logger;

import static junit.framework.TestCase.fail;
import static org.junit.Assert.assertEquals;


public class CountMeUpTest {

    private final static Logger logger = Logger.getLogger(CMUDatabaseManager.class.getName());

    @Before
    public void setup() {
        // Set up a clean new table
        CountMeUpController.setupTables();
    }

    @After
    public void teardown() {
        // Get rid of the table, in preparation for a new round.
        CountMeUpController.teardownTables();
    }

    @Test
    public void voteTest() {
        // Send a vote request
        String resultString = CountMeUpController.vote("hershiv.haria@gmail.com","candidate-1");

        try {
            // Parse the response, should be successful
            JSONObject result = (JSONObject) new JSONParser().parse(resultString);
            assertEquals((String)result.get("success"),"true");
        } catch (ParseException e) {
            fail("Mangled JSON string");
            logger.severe("Failure when parsing json");
            e.printStackTrace();
        }
    }

    @Test
    public void tooManyVotesTest() {

        // Vote 4 times
        String resultString1 = CountMeUpController.vote("hershiv.haria@gmail.com","candidate-1");
        String resultString2 = CountMeUpController.vote("hershiv.haria@gmail.com","candidate-2");
        String resultString3 = CountMeUpController.vote("hershiv.haria@gmail.com","candidate-3");
        String resultString4 = CountMeUpController.vote("hershiv.haria@gmail.com","candidate-4");

        try {

            JSONParser jsonparser = new JSONParser();

            // Parse the results - the first 3 votes should be fine
            JSONObject result1 = (JSONObject) jsonparser.parse(resultString1);
            assertEquals((String)result1.get("success"),"true");

            JSONObject result2 = (JSONObject) jsonparser.parse(resultString2);
            assertEquals((String)result2.get("success"),"true");

            JSONObject result3 = (JSONObject) jsonparser.parse(resultString3);
            assertEquals((String)result3.get("success"),"true");

            // Fourth vote attempt should be a failure, with a reason for the front end to know why it failed.
            JSONObject result4 = (JSONObject) jsonparser.parse(resultString4);
            assertEquals((String)result4.get("success"),"false");
            assertEquals((String) result4.get("reason"),"vote limit");
        } catch (ParseException e) {
            fail("Mangled JSON string");
            logger.severe("Failure when parsing json");
            e.printStackTrace();
        }

    }

    @Test
    public void countMeUpTest() {

        // Send 30 votes, of the following count
        // Candidate-1: 6
        // Candidate-2: 4
        // Candidate-3: 5
        // Candidate-4: 7
        // Candidate-5: 8
        CountMeUpController.vote("userA","candidate-1");
        CountMeUpController.vote("userA","candidate-1");
        CountMeUpController.vote("userA","candidate-1");
        CountMeUpController.vote("userB","candidate-2");
        CountMeUpController.vote("userB","candidate-2");
        CountMeUpController.vote("userB","candidate-2");
        CountMeUpController.vote("userC","candidate-3");
        CountMeUpController.vote("userC","candidate-3");
        CountMeUpController.vote("userC","candidate-3");
        CountMeUpController.vote("userD","candidate-4");
        CountMeUpController.vote("userD","candidate-4");
        CountMeUpController.vote("userD","candidate-4");
        CountMeUpController.vote("userE","candidate-5");
        CountMeUpController.vote("userE","candidate-5");
        CountMeUpController.vote("userE","candidate-5");
        CountMeUpController.vote("userF","candidate-1");
        CountMeUpController.vote("userF","candidate-2");
        CountMeUpController.vote("userF","candidate-3");
        CountMeUpController.vote("userG","candidate-4");
        CountMeUpController.vote("userG","candidate-5");
        CountMeUpController.vote("userG","candidate-1");
        CountMeUpController.vote("userH","candidate-1");
        CountMeUpController.vote("userH","candidate-5");
        CountMeUpController.vote("userH","candidate-5");
        CountMeUpController.vote("userI","candidate-4");
        CountMeUpController.vote("userI","candidate-4");
        CountMeUpController.vote("userI","candidate-5");
        CountMeUpController.vote("userJ","candidate-4");
        CountMeUpController.vote("userJ","candidate-3");
        CountMeUpController.vote("userJ","candidate-5");

        String resultString = CountMeUpController.countMeUp();
        try {
            JSONObject result = (JSONObject) new JSONParser().parse(resultString);

            // Check that the returned data matches up with the votes as abover
            assertEquals((String) result.get("candidate-1"),"6");
            assertEquals((String) result.get("candidate-2"),"4");
            assertEquals((String) result.get("candidate-3"),"5");
            assertEquals((String) result.get("candidate-4"),"7");
            assertEquals((String) result.get("candidate-5"),"8");
        } catch (ParseException e) {
            fail("Mangled JSON string");
            logger.severe("Failure when parsing json");
            e.printStackTrace();
        }

    }
}
