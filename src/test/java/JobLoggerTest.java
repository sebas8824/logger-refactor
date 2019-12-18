import model.Log;
import constant.LogLocation;
import constant.State;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import utils.SetUpDBConnection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.FileHandler;

import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

@RunWith(PowerMockRunner.class)
@PrepareForTest({DriverManager.class, SetUpDBConnection.class, FileHandler.class, Connection.class, Statement.class})
public class JobLoggerTest {

    @Mock
    Connection mockConnection;
    @Mock
    Statement mockStatement;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        mockStatic(SetUpDBConnection.class);
        mockStatic(FileHandler.class);
        mockStatic(Statement.class);
        when(SetUpDBConnection.setUpConnection()).thenReturn(mockConnection);
        when(SetUpDBConnection.setUpConnection().createStatement()).thenReturn(mockStatement);
    }

    @Test
    public void logWhenIsMessage() throws Exception {
        Set<LogLocation> logLocations = new HashSet<LogLocation>();
        Log logEntry = new Log("Error Message", true, false, false);
        logLocations.add(LogLocation.TEXT);
        State state = JobLogger.logMessage(logLocations, logEntry, true, false);
        Assert.assertEquals(state, State.NO_ACTION);
    }

    @Test
    public void logWhenIsWarning() throws Exception {
        Set<LogLocation> logLocations = new HashSet<LogLocation>();
        Log logEntry = new Log("Error Message", true, true, false);
        logLocations.add(LogLocation.TEXT);
        State state = JobLogger.logMessage(logLocations, logEntry, true, false);
        Assert.assertEquals(state, State.NO_ACTION);
    }

    @Test
    public void logWhenIsError() throws Exception {
        Set<LogLocation> logLocations = new HashSet<LogLocation>();
        Log logEntry = new Log("Error Message", true, false, true);
        logLocations.add(LogLocation.DATABASE);
        State state = JobLogger.logMessage(logLocations, logEntry, true, false);
        Assert.assertEquals(state, State.LOGGED_IN_DATABASE);
    }

    @Test(expected = Exception.class)
    public void logWhenIsNoMessageNoWarningNoError() throws Exception {
        Set<LogLocation> logLocations = new HashSet<LogLocation>();
        Log logEntry = new Log("Error Message", false, false, false);
        logLocations.add(LogLocation.DATABASE);
        JobLogger.logMessage(logLocations, logEntry, true, false);
    }

    @Test
    public void logWhenIsOnlyError() throws Exception {
        Set<LogLocation> logLocations = new HashSet<LogLocation>();
        Log logEntry = new Log("Error Message", false, false, true);
        logLocations.add(LogLocation.DATABASE);
        State state = JobLogger.logMessage(logLocations, logEntry, true, false);
        Assert.assertEquals(state, State.LOGGED_IN_DATABASE);
    }

    @Test
    public void logWhenIsWarningAndError() throws Exception {
        Set<LogLocation> logLocations = new HashSet<LogLocation>();
        Log logEntry = new Log("Error Message", false, true, true);
        logLocations.add(LogLocation.CONSOLE);
        State state = JobLogger.logMessage(logLocations, logEntry, true, false);
        Assert.assertEquals(state, State.LOGGED_IN_CONSOLE);
    }

    @Test
    public void logWhenIsFile() throws Exception {
        Set<LogLocation> logLocations = new HashSet<LogLocation>();
        Log logEntry = new Log("Error Message", false, true, true);
        logLocations.add(LogLocation.TEXT);
        State state = JobLogger.logMessage(logLocations, logEntry, true, false);
        Assert.assertEquals(state, State.LOGGED_IN_FILE);
    }

    @Test
    public void logWhenIsConsole() throws Exception  {
        Set<LogLocation> logLocations = new HashSet<LogLocation>();
        Log logEntry = new Log("Error Message", false, true, true);
        logLocations.add(LogLocation.CONSOLE);
        State state = JobLogger.logMessage(logLocations, logEntry, true, false);
        Assert.assertEquals(state, State.LOGGED_IN_CONSOLE);
    }

    @Test
    public void logWhenIsDatabase() throws Exception  {
        Set<LogLocation> logLocations = new HashSet<LogLocation>();
        Log logEntry = new Log("Error Message", false, true, true);
        logLocations.add(LogLocation.DATABASE);
        State state = JobLogger.logMessage(logLocations, logEntry, true, false);
        Assert.assertEquals(state, State.LOGGED_IN_DATABASE);
    }

    @Test
    public void logWhenIsLoggingForDatabaseAndConsole() throws Exception  {
        Set<LogLocation> logLocations = new HashSet<LogLocation>();
        Log logEntry = new Log("Error Message", false, true, true);
        logLocations.add(LogLocation.DATABASE);
        logLocations.add(LogLocation.CONSOLE);
        State state = JobLogger.logMessage(logLocations, logEntry, true, false);
        Assert.assertEquals(state, State.LOGGED_IN_CONSOLE_AND_DATABASE);
    }

    @Test
    public void logWhenIsLoggingForDatabaseAndFile() throws Exception  {
        Set<LogLocation> logLocations = new HashSet<LogLocation>();
        Log logEntry = new Log("Error Message", false, true, true);
        logLocations.add(LogLocation.DATABASE);
        logLocations.add(LogLocation.TEXT);
        State state = JobLogger.logMessage(logLocations, logEntry, true, false);
        Assert.assertEquals(state, State.LOGGED_IN_FILE_AND_DATABASE);
    }

    @Test
    public void logWhenIsLoggingForConsoleAndFile() throws Exception  {
        Set<LogLocation> logLocations = new HashSet<LogLocation>();
        Log logEntry = new Log("Error Message", false, true, true);
        logLocations.add(LogLocation.CONSOLE);
        logLocations.add(LogLocation.TEXT);
        State state = JobLogger.logMessage(logLocations, logEntry, true, false);
        Assert.assertEquals(state, State.LOGGED_IN_FILE_AND_CONSOLE);
    }

    @Test
    public void logWhenIsForAllDestinations() throws Exception  {
        Set<LogLocation> logLocations = new HashSet<LogLocation>();
        Log logEntry = new Log("Error Message", false, true, true);
        logLocations.add(LogLocation.DATABASE);
        logLocations.add(LogLocation.CONSOLE);
        logLocations.add(LogLocation.TEXT);
        State state = JobLogger.logMessage(logLocations, logEntry, true, false);
        Assert.assertEquals(state, State.LOGGED_IN_ALL);
    }
}
