package com.assetcontrol.logcontrol;

import play.Logger;

import java.util.Random;

/**
 * This logger generates random log messages with different
 * severity in intervals randing from 1 to 500 milliseconds.
 *
 * It uses standard logger in assumption that log configuration
 * will be setup appropriately.
 */
public class DemoLogger implements Runnable {
    private static final Logger.ALogger log = Logger.of(DemoLogger.class);
    private Random random = new Random();

    private boolean alive = true;

    @Override
    public void run() {
        while (alive) {
            logRandomSeverityMessage();
            try {
                // sleep up-to half of seconds
                Thread.sleep(1 + random.nextInt(500));
            } catch (InterruptedException e) {
                // thread was interrupted
                this.alive = false;
            }
        }
    }

    private void logRandomSeverityMessage() {
        switch(random.nextInt() % 3) {
            case 0: log.info("You're doing good!"); break;
            case 1: log.warn("Be aware"); break;
            case 2: log.error("Ooops!!!"); break;
        }
    }
}
