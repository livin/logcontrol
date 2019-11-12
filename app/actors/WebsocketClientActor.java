package actors;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedAbstractActor;
import com.assetcontrol.logcontrol.LogSummary;
import play.Logger;
import play.libs.Json;
import scala.concurrent.duration.Duration;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.concurrent.TimeUnit;

/**
 * Handles all websocket communication as actor.
 */
public class WebsocketClientActor extends UntypedAbstractActor implements PropertyChangeListener {
    private static final Logger.ALogger log = Logger.of(WebsocketClientActor.class);

    private static final String SUBSCRIBE = "subscribe";
    private static final String PUBLISH_UPDATE = "publish-update";

    public static Props props(ActorRef out) {
        return Props.create(WebsocketClientActor.class, out);
    }

    private final ActorRef out;

    public WebsocketClientActor(ActorRef out) {
        this.out = out;
        LogSummary.getInstance().addPropertyChangeListener(this);
        pushIntervalSeconds(LogSummary.getInstance().getIntervalSeconds());
    }

    /**
     * Dispatches different messages received from web-socket.
     */
    public void onReceive(Object messageObj) throws Exception {
        if (messageObj instanceof String) {
            String message = (String) messageObj;
            if (SUBSCRIBE.equalsIgnoreCase(message)) {
                handleSubscribe();
            } else if (PUBLISH_UPDATE.equalsIgnoreCase(message)) {
                out.tell(Json.toJson(LogSummary.getInstance()).toString(), self());
            } else if (message.startsWith("set-interval")) {
                handleSetInterval(message);
            }
        }
    }

    /**
     * Handles set-interval message received from web-socket.
     */
    private void handleSetInterval(String message) {
        String[] strings = message.split(" ", 2);
        int value = 0;
        if (strings.length > 1)
            try {
                value = Integer.parseInt(strings[1]);
            } catch (NumberFormatException e) {
                log.warn("Ignorning invalid interval: " + e.getMessage());
            }

        if ((value > 0) && (value < 3600))
            LogSummary.getInstance().setIntervalSeconds(value);
    }

    /**
     * Handles subcribe request by scheduling periodic updates
     * to using publish-update message with 1 second period.
     */
    private void handleSubscribe() {
        log.info("Received subscribe request...");
        getContext()
            .getSystem()
            .scheduler()
            .schedule(
                Duration.Zero(),
                Duration.create(1, TimeUnit.SECONDS), self(),
                PUBLISH_UPDATE,
                getContext().dispatcher(),
                ActorRef.noSender());
    }

    /**
     * Handles interval seconds change by notifying web-socket.
     *
     * This is required for multiple client to get interval updates.
     */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(LogSummary.INTERVAL_SECONDS)) {
            log.debug("Notifying intervalSeconds changed to " + evt.getNewValue());

            int newValue = (int) evt.getNewValue();
            pushIntervalSeconds(newValue);
        }
    }

    /**
     * Sends interval seconds update to the client.
     */
    private void pushIntervalSeconds(int newValue) {
        out.tell(Json.newObject().put("intervalSeconds", newValue).toString(), self());
    }
}
