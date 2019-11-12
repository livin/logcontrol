package controllers;

import actors.LogAggregatorActor;
import actors.WebsocketClientActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.stream.Materializer;
import com.assetcontrol.logcontrol.DemoLogger;
import com.assetcontrol.logcontrol.LogSummary;
import com.typesafe.config.Config;
import org.apache.commons.io.input.Tailer;
import org.apache.commons.io.input.TailerListenerAdapter;
import play.Logger;
import play.libs.streams.ActorFlow;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.WebSocket;
import scala.concurrent.ExecutionContext;
import scala.concurrent.duration.Duration;

import javax.inject.Inject;
import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * This controller contains an action to handle HTTP requests
 * to the application's home page.
 */
public class HomeController extends Controller {
    private static ExecutorService myExecutor = Executors.newFixedThreadPool(3);

    private static final Logger.ALogger log = Logger.of(HomeController.class);
    private final ActorRef logAggregator;
    private final ExecutionContext executionContext;
    private ActorSystem actorSystem;
    private final Materializer materializer;
    private Config configuration;
    private DemoLogger demoLogger;

    @Inject
    public HomeController(ActorSystem actorSystem, ExecutionContext executionContext, Materializer materializer,
                          Config configuration) {
        this.actorSystem = actorSystem;
        this.executionContext = executionContext;
        this.materializer = materializer;
        this.configuration = configuration;
        logAggregator = actorSystem.actorOf(LogAggregatorActor.getProps());

        scheduleRefreshEachSecond();

        initConfigLocation();
        initDemoLogger();
        initTailer();
    }

    private void scheduleRefreshEachSecond() {
        actorSystem.scheduler().schedule(
            Duration.create(0, TimeUnit.SECONDS),
            Duration.create(1, TimeUnit.SECONDS),
            logAggregator,
            LogAggregatorActor.REFRESH,
            executionContext,
            ActorRef.noSender()
        );
    }

    private void initDemoLogger() {
        boolean enableDemoLogger = configuration.getBoolean("demoLogger.enable");
        if (enableDemoLogger) {
            this.demoLogger = new DemoLogger();
            myExecutor.execute(demoLogger);
        }
    }

    private void initTailer() {
        Tailer tailer = Tailer.create(new File(LogSummary.getInstance().logPath), new TailerListenerAdapter() {
            @Override
            public void handle(String line) {
                logAggregator.tell(line, ActorRef.noSender());
            }
        });
        myExecutor.execute(tailer);
    }

    /**
     * Initializes configuration.
     *
     * Loads default logPath and interval.
     */
    private void initConfigLocation() {
        String logpath = configuration.getString("logpath");
        int interval = configuration.getInt("interval");

        log.info("Using logpath = " + logpath);
        LogSummary.getInstance().logPath = logpath;

        log.info("Using interval = " + interval);
        LogSummary.getInstance().setIntervalSeconds(interval);
    }

    /**
     * Main index page.
     */
    public Result index() {
        return ok(views.html.index.render());
    }

    /**
     * Web-socket handler.
     * Creates new web-socket and binds it WebsocketClientActor.
     */
    public WebSocket socket() {
        return WebSocket.Text.accept(request -> ActorFlow.actorRef(WebsocketClientActor::props, actorSystem, materializer));
    }
}
