package com.assetcontrol.logcontrol;

import org.junit.Test;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class LogRecordTest {
    @Test
    public void parseSampleLogRecordShouldBeOk() {
        LogRecord log = LogRecord.parse("2016-09-20 16:23:10,994 INFO  Some info message");
        assertEquals(LogRecord.Severity.INFO, log.getSeverity());
        assertEquals("Some info message", log.getMessage());
        assertEquals(LocalDateTime.of(2016, 9, 20, 16, 23, 10, 994000000).atZone(ZoneOffset.systemDefault()).toInstant(), log.getDateTime());
    }

    @Test
    public void parseBadLogRecordShouldReturnNull() {
        assertNull(LogRecord.parse("Sample bad log record"));
    }
}
