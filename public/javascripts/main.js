/*
 * LogWS represents Log Control's Web Socket behavior
 *
 * - when socket is opened it sends subcribe request
 * - when socket is closed it tries to reconnect (in case server is not yet up there)
 * - updates widget model with received data
 */
var LogWS = function LogWS() {
    console.log("Connecting...");

    this.ws = new WebSocket("ws://" + location.host + "/ws");
    var ws = this.ws;

    this.ws.onopen = function(event) {
        console.log("Connected...");
        console.log("Subsribing to updates...");
        ws.send("subscribe");
    };

    this.ws.onclose = function(eventclose) {
        console.log('Connection closed: ' + this.eventclose)
    };
    this.ws.onerror = function(eventclose) {
        console.log('Error: ' + this.eventclose)
    };
    this.ws.onmessage = function (event) {
        var json = JSON.parse(event.data);

        if (json === null)
            return;

        if (json.infoCount != null) {
            // this is log summary update
            logWidget.logSummary = json;
        } else if (json.intervalSeconds != null) {
            // this is intervalSeconds update
            console.log("Received new intervalSeconds " + json.intervalSeconds)
            logWidget.intervalSeconds = json.intervalSeconds;
        }
    };

    this.ws.onclose = function(event) {
        console.log('Connection closed. Reason: ' + this.event);
        console.log("Will attempt to reconnect in 2 seconds");
        setTimeout(logWidget.websocketReconnect, 2000);
    }
};

/*
 * Log Widget component
 *
 * - implements binding with the UI
 * - pushes intervalSeconds to the server when changed, implemented with delaying actual push to server with 1 second delay
 */
var logWidget = new Vue({
    el: '#mainlog',
    data: {
        logSummary: {
            infoCount: 0,
            warningCount: 0,
            errorCount: 0
        },
        intervalSeconds: 3
    },

    watch: {
        intervalSeconds: function(newIntervalSeconds) {
            this.pushIntervalSecondsChange(newIntervalSeconds);
        }
    },

    methods: {
        pushIntervalSecondsChange: $.debounce(1000, function(newIntervalSeconds) {
            if (newIntervalSeconds < 1)
                return;

            console.log("Interval changed by user to " + newIntervalSeconds);
            this.logws.ws.send("set-interval " + newIntervalSeconds);
        }),

        websocketReconnect: function() {
            this.logws = new LogWS();
        }
    },

    mounted: function () {
        this.websocketReconnect();
    }
});





