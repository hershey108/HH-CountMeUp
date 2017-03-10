import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * This is the webservice to provide the data to the front-end
 */
@Path("/countmeup")
public class CountMeUpService {


    /**
     * The web service method to retrieve the current vote rankings for the candidates.
     * @return
     */
    @GET
    @Produces("application/json")
    public String countMeUp() {
        // Return a JSON String of the total vote count now.
        return CountMeUpController.countMeUp();
    }

    @POST
    @Path("/vote")
    @Produces("application/json")
    public String vote(InputStream data) {
        StringBuilder voteBuilder = new StringBuilder();
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(data));
            String line = null;
            while ((line = in.readLine()) != null) {
                voteBuilder.append(line);
            }

            String voteString = voteBuilder.toString();

            JSONObject voteData = (JSONObject) new JSONParser().parse(voteString);

            return CountMeUpController.vote((String)voteData.get("userId"),(String)voteData.get("candidateId"));


        } catch (Exception e) {
            e.printStackTrace();
        }

        return "Error";
    }

}
