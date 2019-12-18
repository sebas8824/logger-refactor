/* import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.text.DateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Properties;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JobLogger {
    private static boolean logToFile;
    private static boolean logToConsole;
    private static boolean logMessage;
    private static boolean logWarning;
    private static boolean logError;
    private static boolean logToDatabase;
    private static Map dbParams;
    private static Logger logger;

    public JobLogger(boolean logToFileParam, boolean logToConsoleParam, boolean logToDatabaseParam,
                     boolean logMessageParam, boolean logWarningParam, boolean logErrorParam, Map dbParamsMap) {
        logger = Logger.getLogger("MyLog");
        logError = logErrorParam;
        logMessage = logMessageParam;
        logWarning = logWarningParam;
        logToDatabase = logToDatabaseParam;
        logToFile = logToFileParam;
        logToConsole = logToConsoleParam;
        dbParams = dbParamsMap;
    }*/

    /* Receives the message and determines if it is a message, a warning or an error
    public static void LogMessage(String messageText, boolean message, boolean warning, boolean error) throws Exception {
        messageText.trim();
        if (messageText == null || messageText.length() == 0) {
            return;
        }

        if (!logToConsole && !logToFile && !logToDatabase) {
            throw new Exception("Invalid configuration");
        }

        if ((!logError && !logMessage && !logWarning) || (!message && !warning && !error)) {
            throw new Exception("Error or Warning or Message must be specified");
        }
*/
        /* This should be done in an utils class
        Connection connection = null;
        Properties connectionProps = new Properties();
        connectionProps.put("user", dbParams.get("userName"));
        connectionProps.put("password", dbParams.get("password"));

        connection = DriverManager.getConnection("jdbc:" + dbParams.get("dbms") + "://" + dbParams.get("serverName")
                + ":" + dbParams.get("portNumber") + "/", connectionProps);

        Statement stmt = connection.createStatement();
*/
        /* Depending to the combination a value to t will be set ?
        int t = 0;
        if (message && logMessage) {
            t = 1;
        }

        if (error && logError) {
            t = 2;
        }

        if (warning && logWarning) {
            t = 3;
        }

        String l = null;
        File logFile = new File(dbParams.get("logFileFolder") + "/logFile.txt");
        if (!logFile.exists()) {
            logFile.createNewFile();
        }

        FileHandler fh = new FileHandler(dbParams.get("logFileFolder") + "/logFile.txt");
        ConsoleHandler ch = new ConsoleHandler();
*/
        /* These validations should be merged with the t assignment
        if (error && logError) {
            l = l + "error " + DateFormat.getDateInstance(DateFormat.LONG).format(new Date()) + messageText;
        }

        if (warning && logWarning) {
            l = l + "warning " +DateFormat.getDateInstance(DateFormat.LONG).format(new Date()) + messageText;
        }

        if (message && logMessage) {
            l = l + "message " +DateFormat.getDateInstance(DateFormat.LONG).format(new Date()) + messageText;
        }
*/
        /* Also these ones, so it can be done as a generic method
        if(logToFile) {
            logger.addHandler(fh);
            logger.log(Level.INFO, messageText);
        }

        if(logToConsole) {
            logger.addHandler(ch);
            logger.log(Level.INFO, messageText);
        }

        if(logToDatabase) {
            stmt.executeUpdate("insert into Log_Values('" + message + "', " + String.valueOf(t) + ")");
        }
    }
}
*/

import model.Log;
import constant.LogLocation;
import constant.State;
import utils.SetUpDBConnection;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Statement;
import java.text.DateFormat;
import java.util.*;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JobLogger {

    private static final String LOGGER_PROPERTIES = "logparams.properties";
    private static final String LOG_NAME_PROPERTY = "logName";

    private static Logger logger;
    private static Properties prop;
    private static String log = null;
    private static int type = 0;

    public static State logMessage(Set<LogLocation> logLocations, Log logEntry, boolean logAsError, boolean logAsWarning) throws Exception {

        ArrayList<State> stateList = new ArrayList<>();
        loggerDefinition();

        if(!logEntry.getLogMessage().isEmpty()) {
            if((logEntry.isError() && logAsError) && (logEntry.isWarning() && logAsWarning)) {
                type = 2;
                log = "error " + DateFormat.getDateInstance(DateFormat.LONG).format(new Date()) + logEntry.getLogMessage();
            } else if(logEntry.isError() && logAsError) {
                type = 1;
                log = "error " + DateFormat.getDateInstance(DateFormat.LONG).format(new Date()) + logEntry.getLogMessage();
                log += ", warning " + DateFormat.getDateInstance(DateFormat.LONG).format(new Date()) + logEntry.getLogMessage();
            } else if (!logEntry.isWarning() && !logEntry.isError() && !logEntry.isMessage()){
                throw new Exception("Error or Warning or Message must be specified");
            } else {
                return State.NO_ACTION;
            }
        }

        if(!logLocations.isEmpty()) {
            logLocations.stream().forEach(logLocation -> {
                try {
                    State s = handleLogging(logLocation, logEntry.getLogMessage(), type, log);
                    stateList.add(s);
                } catch (Exception e) {
                    logger.log(Level.INFO, "There was an error trying to save the log entry: " + e.getMessage());
                }
            });
        } else {
            throw new Exception("Invalid configuration");
        }

        return handleState(stateList);
    }

    static State handleState(ArrayList<State> stateList) {
        State state = State.NO_ACTION;

        if(stateList.size() >= 2) {
            if(stateList.contains(State.LOGGED_IN_CONSOLE) && stateList.contains(State.LOGGED_IN_DATABASE) && stateList.contains(State.LOGGED_IN_FILE)) {
                state = State.LOGGED_IN_ALL;
            } else if (stateList.contains(State.LOGGED_IN_DATABASE) && stateList.contains(State.LOGGED_IN_FILE)) {
                state = State.LOGGED_IN_FILE_AND_DATABASE;
            } else if (stateList.contains(State.LOGGED_IN_DATABASE) && stateList.contains(State.LOGGED_IN_CONSOLE)) {
                state = State.LOGGED_IN_CONSOLE_AND_DATABASE;
            } else if (stateList.contains(State.LOGGED_IN_CONSOLE) && stateList.contains(State.LOGGED_IN_FILE)) {
                state = State.LOGGED_IN_FILE_AND_CONSOLE;
            }
        } else {
            state = stateList.get(0);
        }
        return state;
    }

    static State handleLogging(LogLocation logLocation, String messageText, int type, String longLogEntry) throws Exception {
        State state;
        switch(logLocation.ordinal()) {
            case 0:
                FileHandler fh = new FileHandler(prop.getProperty("logFileFolder") + "logFile.txt");
                logger.addHandler(fh);
                logger.log(Level.INFO, "Type: " + type + ", Message: "+ messageText +", Log: "+ longLogEntry);
                fh.close();
                state = State.LOGGED_IN_FILE;
            break;
            case 1:
                ConsoleHandler ch = new ConsoleHandler();
                logger.addHandler(ch);
                logger.log(Level.INFO, "Type: " + type + ", Message: "+ messageText +", Log: "+ longLogEntry);
                state = State.LOGGED_IN_CONSOLE;
            break;
            case 2:
                Statement stmt = SetUpDBConnection.setUpConnection().createStatement();
                stmt.executeUpdate("insert into Log_Values('" + String.valueOf(type) + "', " + longLogEntry + ")");
                stmt.close();
                state = State.LOGGED_IN_DATABASE;
            break;
            default:
                state = State.NO_ACTION;
        }
        return state;
    }

    private static void loggerDefinition() throws IOException {
        InputStream logFile = new FileInputStream(LOGGER_PROPERTIES);
        prop = new Properties();
        prop.load(logFile);
        logger = Logger.getLogger(prop.getProperty(LOG_NAME_PROPERTY));
    }
}
