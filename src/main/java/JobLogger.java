import constant.Constants;
import model.Log;
import constant.LogLocation;
import constant.State;
import utils.PropertyLoader;
import utils.SetUpDBConnection;

import java.sql.Statement;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Properties;
import java.util.Set;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JobLogger {

    private static Logger logger = Logger.getLogger(Constants.LOG_NAME_PROP);

    private static String log = null;
    private static int type = 0;

    static State logMessage(Set<LogLocation> logLocations, Log logEntry, boolean logAsError, boolean logAsWarning) throws Exception {
        ArrayList<State> stateList = new ArrayList<>();

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
            logLocations.forEach(logLocation -> {
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

    private static State handleState(ArrayList<State> stateList) {
        State state = State.NO_ACTION;

        if(stateList.size() >= 2) {
            if(stateList.contains(State.LOGGED_IN_CONSOLE) && stateList.contains(State.LOGGED_IN_DATABASE) && stateList.contains(State.LOGGED_IN_FILE)) {
                state = State.LOGGED_IN_ALL_LOCATIONS;
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

    private static State handleLogging(LogLocation logLocation, String messageText, int type, String fullLogEntry) throws Exception {
        Properties prop = PropertyLoader.mapPropertiesFromFile(Constants.LOGGER_PROPERTIES);
        State state;
        switch(logLocation.ordinal()) {
            case 0:
                FileHandler fh = new FileHandler(prop.getProperty(Constants.LOG_FILE_FOLDER_PROP) + "logFile.txt");
                logger.addHandler(fh);
                logger.log(Level.INFO, "Type: " + type + ", Message: "+ messageText +", Log: "+ fullLogEntry);
                fh.close();
                state = State.LOGGED_IN_FILE;
            break;
            case 1:
                ConsoleHandler ch = new ConsoleHandler();
                logger.addHandler(ch);
                logger.log(Level.INFO, "Type: " + type + ", Message: "+ messageText +", Log: "+ fullLogEntry);
                state = State.LOGGED_IN_CONSOLE;
            break;
            case 2:
                Statement stmt = SetUpDBConnection.setUpConnection().createStatement();
                stmt.executeUpdate("insert into Log_Values('" + type + "', " + fullLogEntry + ")");
                stmt.close();
                state = State.LOGGED_IN_DATABASE;
            break;
            default:
                state = State.NO_ACTION;
        }
        return state;
    }
}
