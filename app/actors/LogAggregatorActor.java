package actors;

import akka.actor.AbstractActor;
import akka.actor.Props;
import com.assetcontrol.logcontrol.LogSummary;

/**
 * This actor handles new log records addition and allows to refresh
 * summary.
 *
 * Actor ensures consistent updates.
 */
public class LogAggregatorActor extends AbstractActor {
    static public Props getProps() {
        return Props.create(LogAggregatorActor.class, () -> new LogAggregatorActor());
    }

    public final static Refresh REFRESH = new Refresh();

    public static class Refresh {
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(String.class, line -> LogSummary.getInstance().add(line))
                .match(Refresh.class, refresh -> LogSummary.getInstance().refresh())
                .build();
    }
}
