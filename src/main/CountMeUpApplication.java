import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;

//Defines the base URI for all resource URIs.
@ApplicationPath("/service")
//The java class declares root resource and provider classes
public class CountMeUpApplication extends Application{

    /**
     * Initialisation method, used to clean up the database on load.
     */
    public CountMeUpApplication() {

        CountMeUpController.teardownTables();
        CountMeUpController.setupTables();
    }

    //The method returns a non-empty collection with classes, that must be included in the published JAX-RS application
    @Override
    public Set<Class<?>> getClasses() {
        HashSet h = new HashSet<Class<?>>();
        h.add( CountMeUpService.class );
        return h;
    }
}