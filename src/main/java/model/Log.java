package model;

public class Log {

    private String logMessage;
    private boolean isMessage;
    private boolean isWarning;
    private boolean isError;

    public Log(String logMessage, boolean isMessage, boolean isWarning, boolean isError) {
        this.logMessage = logMessage.trim();
        this.isMessage = isMessage;
        this.isWarning = isWarning;
        this.isError = isError;
    }

    public String getLogMessage() {
        return logMessage;
    }

    public boolean isMessage() {
        return isMessage;
    }

    public boolean isWarning() {
        return isWarning;
    }

    public boolean isError() {
        return isError;
    }
}
