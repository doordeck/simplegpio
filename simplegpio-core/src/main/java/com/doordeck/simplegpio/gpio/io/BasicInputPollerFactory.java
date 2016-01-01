package com.doordeck.simplegpio.gpio.io;

import com.doordeck.simplegpio.spi.InputPoller;
import com.doordeck.simplegpio.spi.InputPollerFactory;

import java.nio.file.Path;

public class BasicInputPollerFactory implements InputPollerFactory {

    @Override
    public boolean isValid() {
        // Basic poller is always valid
        return true;
    }

    @Override
    public InputPoller build(final Path filename) {
        return new BasicInputPoller(filename);
    }


}
