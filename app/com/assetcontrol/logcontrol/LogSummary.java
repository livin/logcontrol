package com.assetcontrol.logcontrol;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.time.Clock;
import java.time.Instant;
import java.util.LinkedList;
import java.util.stream.Collectors;

/**
 * LogSummary aggregates a list of active log records and provides
 * summary of info, warning and error log records in current time interval.
 *
 * Active logs records are records which are not outdated according to current
 * intervalSeconds and current time.
 */
public class LogSummary {
    public static final String INTERVAL_SECONDS = "intervalSeconds";

    private static LogSummary ourInstance = new LogSummary();
    private Clock clock;

    private PropertyChangeSupport propertyChangeSupport;
    private LinkedList<LogRecord> logRecords = new LinkedList<LogRecord>();

    private int[] messageCountBySeverity = new int[LogRecord.Severity.values().length];

    public String logPath;
    private int intervalSeconds = 5;

    public LogSummary(Clock clock) {
        this.clock = clock;

        propertyChangeSupport = new PropertyChangeSupport(this);
    }

    public LogSummary() {
        this(Clock.systemDefaultZone());
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(listener);
    }

    public int getIntervalSeconds() {
        return intervalSeconds;
    }

    public void setIntervalSeconds(int intervalSeconds) {
        if (intervalSeconds < 1)
            throw new IllegalArgumentException("interval should be more than 1 second");

        int old = this.intervalSeconds;
        this.intervalSeconds = intervalSeconds;

        propertyChangeSupport.firePropertyChange(INTERVAL_SECONDS, old, intervalSeconds);
    }

    public static LogSummary getInstance() {
        return ourInstance;
    }

    /**
     * The earliest time after which all records are considered active.
     */
    private Instant getEarliestTime() {
        return clock.instant().minusSeconds(intervalSeconds);
    }

    /**
     * Adds new log record to the summary. If the record is not outdated
     * it will be indexed to provide the log summary.
     *
     * @param logRecord a log record in format like 2016-09-20 16:23:10,994 INFO  Some info message
     */
    public void add(String logRecord) {
        LogRecord record = LogRecord.parse(logRecord);
        if (record != null) {
            add(record);
        }
    }

    /**
     * Validates given parsed log record and if it's not outdated it will be added and indexed.
     *
     * @param record a log record
      */
    private void add(LogRecord record) {
        if (!record.getDateTime().isAfter(getEarliestTime()))
            return;

        indexRecord(record);
        logRecords.add(record);
    }

    private void indexRecord(LogRecord record) {
        messageCountBySeverity[record.getSeverity().ordinal()]++;
    }

    public int getMessageCount(LogRecord.Severity severity) {
        return messageCountBySeverity[severity.ordinal()];
    }

    public void setMessageCount(LogRecord.Severity severity, int count) {
        messageCountBySeverity[severity.ordinal()] = count;
    }

    /**
     * Provides a number info log messages count which is in current log interval.
     *
     * @return number of info log messages
     */
    public int getInfoCount() {
        return getMessageCount(LogRecord.Severity.INFO);
    }


    /**
     * Provides a number warning log messages count which is in current log interval.
     *
     * @return number of warning log messages
     */
    public int getWarningCount() {
        return getMessageCount(LogRecord.Severity.WARNING);
    }

    /**
     * Provides a number error log messages count which is in current log interval.
     *
     * @return number of error log messages
     */
    public int getErrorCount() {
        return getMessageCount(LogRecord.Severity.ERROR);
    }

    /**
     * Refresh summary by removing outdated records
     */
    public void refresh() {
        removeOutdatedRecords();
        reindexAllRecords();
    }

    private void removeOutdatedRecords() {
        logRecords = logRecords.stream()
                .filter(logRecord -> logRecord.getDateTime().isAfter(getEarliestTime()))
                .collect(Collectors.toCollection(LinkedList::new));
    }

    private void reindexAllRecords() {
        clearMessageCounts();
        logRecords.forEach(this::indexRecord);
    }

    private void clearMessageCounts() {
        for (LogRecord.Severity severity: LogRecord.Severity.values())
            setMessageCount(severity, 0);
    }
}
