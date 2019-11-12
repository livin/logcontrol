package com.assetcontrol.logcontrol;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represent log record with date/time, severity and message.
 *
 * Provides ability to parse string log record in following format:
 * "2016-09-20 16:23:10,994 INFO  Some info message"
 */
public class LogRecord {
    private static final Pattern LOG_PATTERN = Pattern.compile("(\\S+\\s+\\S+)\\s+(\\w+)\\s+(.*)");
    private static final DateTimeFormatter DATETIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss,SSS");

    /**
     * Logging Severity
     */
    public enum Severity {
        INFO,
        WARNING,
        ERROR;

        public static Severity parse(String severity) {
            // allow to parse WARN
            if ("WARN".equalsIgnoreCase(severity))
                return WARNING;

            return valueOf(severity);
        }
    }

    private Instant dateTime;
    private Severity severity;
    private String message;

    public LogRecord(Instant dateTime, Severity severity, String message) {
        this.dateTime = dateTime;
        this.severity = severity;
        this.message = message;
    }

    public static LogRecord parse(String line) {
        Matcher m = LOG_PATTERN.matcher(line);
        if (m.find()) {
            String dateTimeString = m.group(1);
            String severity = m.group(2);
            String message = m.group(3);

            Instant dateTime;
            try {
                dateTime = LocalDateTime.parse(dateTimeString, DATETIME_FORMAT).atZone(ZoneId.systemDefault()).toInstant();
            } catch (DateTimeParseException e) {
                return null;
            }

            return new LogRecord(dateTime, Severity.parse(severity), message);
        }
        return null;
    }

    public Instant getDateTime() {
        return dateTime;
    }

    public void setDateTime(Instant dateTime) {
        this.dateTime = dateTime;
    }

    public Severity getSeverity() {
        return severity;
    }

    public void setSeverity(Severity severity) {
        this.severity = severity;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
