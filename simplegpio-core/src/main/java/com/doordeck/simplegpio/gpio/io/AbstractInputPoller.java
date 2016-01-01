package com.doordeck.simplegpio.gpio.io;

import com.doordeck.simplegpio.gpio.event.PollResult;
import com.doordeck.simplegpio.spi.InputPoller;
import com.doordeck.simplegpio.util.BulldogUtil;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class AbstractInputPoller implements InputPoller, Runnable {

    private static final AtomicInteger THREAD_COUNT = new AtomicInteger();

    private final Thread listenerThread = new Thread(this, "GPIO-Poller-" + THREAD_COUNT);
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final Path filename;

    private List<InputPollListener> listeners = new ArrayList<>();

    public AbstractInputPoller(Path filename) {
        listenerThread.setDaemon(true);

        if (filename == null) {
            throw new IllegalArgumentException("Filename cannot be null");
        }

        this.filename = filename;
    }

    public abstract void setup();

    public void start() {
        setup();
        if (running.compareAndSet(false, true) && !listenerThread.isAlive()) {
            listenerThread.start();
        }
    }

    public void stop() {
        if (running.compareAndSet(true, false)) {
            //block until thread is dead
            while (listenerThread.isAlive()) {
                BulldogUtil.sleepMs(10);
            }

            teardown();
        }
    }

    public abstract void teardown();

    protected Path getFilename() {
        return filename;
    }

    protected AtomicBoolean getRunning() {
        return running;
    }

    public boolean isRunning() {
        return listenerThread.isAlive();
    }

    public void addListener(InputPollListener listener) {
        this.listeners.add(listener);
    }

    public void removeListener(InputPollListener listener) {
        this.listeners.remove(listener);
    }

    public void clearListeners() {
        this.listeners.clear();
    }

    protected void fireEpollEvent(PollResult[] results) {
        for (InputPollListener listener : this.listeners) {
            listener.processEpollResults(results);
        }
    }
}
