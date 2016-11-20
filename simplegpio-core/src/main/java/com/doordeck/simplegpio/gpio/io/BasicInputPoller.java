package com.doordeck.simplegpio.gpio.io;

/*
 * (C) Copyright 2016 Doordeck Limited and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

import com.doordeck.simplegpio.gpio.event.BasicPollResult;
import com.doordeck.simplegpio.util.BulldogUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.atomic.AtomicBoolean;

public class BasicInputPoller extends AbstractInputPoller {

    private static final Logger LOG = LoggerFactory.getLogger(BasicInputPoller.class);
    private static final int DEFAULT_POLL_RATE = 200;
    private static final int MILLISECONDS_IN_SECOND = 1000;

    private final AtomicBoolean isSetup = new AtomicBoolean(false);
    private final int sleepInterval;

    private FileChannel channel;

    public BasicInputPoller(Path filename) {
        this(filename, DEFAULT_POLL_RATE);
    }

    public BasicInputPoller(Path filename, int pollFrequency) {
        super(filename);
        if (pollFrequency < 1 || pollFrequency > 1000) {
            throw new IllegalArgumentException("Poll frequency should be between 1Hz and 1000Hz");
        }

        this.sleepInterval = MILLISECONDS_IN_SECOND / pollFrequency;
    }

    @Override
    public void setup() {
        if (isSetup.compareAndSet(false, true)) {
            try {
                this.channel = FileChannel.open(getFilename(), StandardOpenOption.READ);
            } catch (FileNotFoundException e) {
                LOG.error("Unable to open file {}", getFilename(), e);
            } catch (IOException e) {
                LOG.error("Unable to open selector", e);
            }
        }
    }

    @Override
    public void run() {
        final ByteBuffer bb = ByteBuffer.allocate(1);
        while (getRunning().get()) {
            try {
                if (channel.read(bb, 0) > 0) {
                    fireEpollEvent(new BasicPollResult[]{new BasicPollResult(bb.get(0))});
                    bb.clear();
                }
            } catch (IOException e) {
                LOG.error("Exception trying to read from GPIO input", e);
                getRunning().set(false); // Stop immediately
            }
            BulldogUtil.sleepMs(sleepInterval);
        }
    }

    public void teardown() {
        if (isSetup.compareAndSet(true, false)) {
            stop();
            try {
                channel.close();
            } catch (IOException e) { /* ignored */ }
        }
    }

}
