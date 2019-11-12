package com.assetcontrol.logcontrol;

import org.junit.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Verifies correct work of LogSummary aggregation during addition of new records
 * and refreshing.
 */
public class LogSummaryTest {
    private final static Clock sampleFixedClock =
            Clock.fixed(LocalDateTime.of(2016, 9, 20, 16, 23, 15, 994000000).atZone(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault());

    @Test
    public void addLogRecordShouldRecalcSummary() {
        LogSummary logSummary = new LogSummary(sampleFixedClock);
        assertEquals(0, logSummary.getInfoCount());

        logSummary.add("2016-09-20 16:23:11,994 INFO  Some info message");

        assertEquals("Info record should be added", 1, logSummary.getInfoCount());
    }

    @Test
    public void addOutdatedLogRecordShouldBeIgnored() {
        LogSummary logSummary = new LogSummary(sampleFixedClock);
        assertEquals(0, logSummary.getInfoCount());

        logSummary.add("2016-09-20 16:23:09,994 INFO  Some info message");

        assertEquals("No record should be added", 0, logSummary.getInfoCount());
    }

    @Test
    public void logSummaryWithMultipleRecordsShouldGiveAggregatedData() {
        LogSummary logSummary = new LogSummary(sampleFixedClock);

        logSummary.add("2016-09-20 16:23:10,994 INFO  Some info message");
        logSummary.add("2016-09-20 16:23:11,994 INFO  Some other info message");
        logSummary.add("2016-09-20 16:23:12,994 WARNING  Some warning message");
        logSummary.add("2016-09-20 16:23:13,994 WARNING  Some other warning message");
        logSummary.add("2016-09-20 16:23:14,994 ERROR  Some error message");

        assertEquals(1, logSummary.getInfoCount());
        assertEquals(2, logSummary.getWarningCount());
        assertEquals(1, logSummary.getErrorCount());
    }

    @Test
    public void refreshShouldGiveNewResultWithOldRecordsRemoved() {
        Clock clock = mock(Clock.class);

        LogSummary logSummary = new LogSummary(clock);
        when(clock.instant()).thenReturn(sampleFixedClock.instant());

        logSummary.add("2016-09-20 16:23:10,994 INFO  Some info message");
        logSummary.add("2016-09-20 16:23:11,994 INFO  Some other info message");
        logSummary.add("2016-09-20 16:23:12,994 WARNING  Some warning message");
        logSummary.add("2016-09-20 16:23:13,994 WARNING  Some other warning message");
        logSummary.add("2016-09-20 16:23:14,994 ERROR  Some error message");

        when(clock.instant()).thenReturn(sampleFixedClock.instant().plusSeconds(2));

        logSummary.refresh();

        assertEquals(0, logSummary.getInfoCount());
        assertEquals(1, logSummary.getWarningCount());
        assertEquals(1, logSummary.getErrorCount());
    }

}
