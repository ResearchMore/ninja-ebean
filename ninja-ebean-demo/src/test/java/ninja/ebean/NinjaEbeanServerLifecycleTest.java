package ninja.ebean;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.sql.SQLException;

import ninja.lifecycle.Dispose;
import ninja.lifecycle.Start;
import ninja.utils.NinjaProperties;
import ninja.utils.NinjaPropertiesImpl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

@RunWith(MockitoJUnitRunner.class)
public class NinjaEbeanServerLifecycleTest {

    @Mock
    private Logger logger;

    /**
     * This tests does three things 
     * 1) Test that stuff in {@link NinjaEbeanProperties} is correct and without typos 
     * 2) Test that default properties are not altered accidentally. 
     * 3) All properties get called assuming they are ready to use inside the lifecycle.
     */
    @Test
    public void makeSureCorrectPropertiesGetParsed() {

        try {
            // /////////////////////////////////////////////////////////////////////
            // Setup and spy on the properties
            // /////////////////////////////////////////////////////////////////////
            NinjaProperties ninjaProperties = spy(new NinjaPropertiesImpl());

            NinjaEbeanServerLifecycle ninjaEbeanServerLifecycle = new NinjaEbeanServerLifecycle(
                    logger, ninjaProperties);

            // /////////////////////////////////////////////////////////////////////
            // Execute the server startup
            // /////////////////////////////////////////////////////////////////////
            ninjaEbeanServerLifecycle.startServer();

            // /////////////////////////////////////////////////////////////////////
            // Verify that properties are correct
            // /////////////////////////////////////////////////////////////////////
            verify(ninjaProperties).getBooleanWithDefault("ebean.ddl.generate",
                    true);
            verify(ninjaProperties)
                    .getBooleanWithDefault("ebean.ddl.run", true);

            verify(ninjaProperties).getWithDefault("ebean.datasource.name",
                    "default");
            verify(ninjaProperties).getWithDefault("ebean.datasource.username",
                    "test");
            verify(ninjaProperties).getWithDefault("ebean.datasource.password",
                    "test");

            verify(ninjaProperties).getWithDefault(
                    "ebean.datasource.databaseUrl",
                    "jdbc:h2:mem:tests;DB_CLOSE_DELAY=-1");

            verify(ninjaProperties).getWithDefault(
                    "ebean.datasource.databaseDriver", "org.h2.Driver");
            verify(ninjaProperties).getIntegerWithDefault(
                    "ebean.datasource.minConnections", 1);
            verify(ninjaProperties).getIntegerWithDefault(
                    "ebean.datasource.maxConnections", 25);

            verify(ninjaProperties).getWithDefault(
                    "ebean.datasource.heartbeatsql", "select 1");

            // be nice and stop the server afterwards
            ninjaEbeanServerLifecycle.stopServer();
        } catch (Exception e) {
            // we are getting:
            //java.sql.SQLException: Trying to access the Connection Pool when it is shutting down
            //this exception is expected and ignored
            //and happens if startup and shutdown is too fast (what this test is).
        }
    }

    @Test
    public void makeSureLifecycleAnnotationsAreCorrect() throws Exception {

        // test startup annotation
        Method method = NinjaEbeanServerLifecycle.class.getMethod("startServer");
        Annotation annotation = method.getAnnotation(Start.class);

        assertNotNull(annotation);

        // test stop annotation
        method = NinjaEbeanServerLifecycle.class.getMethod("stopServer");
        annotation = method.getAnnotation(Dispose.class);

        assertNotNull(annotation);

    }

}
