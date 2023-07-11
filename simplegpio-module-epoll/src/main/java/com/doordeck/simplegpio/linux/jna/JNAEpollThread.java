package com.doordeck.simplegpio.linux.jna;

/*
 * (C) Copyright 2016 Doordeck (https://doordeck.com/) and others.
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
import com.doordeck.simplegpio.gpio.event.PollResult;
import com.doordeck.simplegpio.gpio.io.AbstractInputPoller;
import com.doordeck.simplegpio.util.BulldogUtil;
import com.sun.jna.LastErrorException;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileDescriptor;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class JNAEpollThread extends AbstractInputPoller {

    private static final Logger LOG = LoggerFactory.getLogger(JNAEpollThread.class);
    private static final int SIZE_OF_EVENT = new LibEpoll.EpollEvent().size() + new LibEpoll.EpollData().size();
    private static final int MAX_EVENTS = 1;

    private final AtomicBoolean isSetup = new AtomicBoolean(false);
    private final Memory ptr = new Memory(MAX_EVENTS * SIZE_OF_EVENT);

    private int epollFd = 0;
    private int fileDescriptor = 0;
    private RandomAccessFile channel;

    public JNAEpollThread(Path filename) {
        super(filename);
    }

    @Override
    public void setup() {
        if (isSetup.compareAndSet(false, true)) {
            try {
                channel = new RandomAccessFile(getFilename().toFile(), "r");

                // Reflectively access the "fd" field in FileDescriptor (the actual integer)
                FileDescriptor fd = channel.getFD();
                Field field = fd.getClass().getDeclaredField("fd");
                field.setAccessible(true);
                fileDescriptor = field.getInt(fd);
                epollFd = LibEpoll.epoll_create(1);
                LibEpoll.EpollEvent.ByReference epollEvent = new LibEpoll.EpollEvent.ByReference(fileDescriptor, LibEpoll.EPOLLPRI | LibEpoll.EPOLLIN | LibEpoll.EPOLLET);
                LibEpoll.epoll_ctl(epollFd, LibEpoll.EPOLL_CTL_ADD, fileDescriptor, epollEvent);
            } catch (IOException|LastErrorException|NoSuchFieldException|IllegalAccessException e) {
                LOG.error("Unable to open {} for epoll", getFilename(), e);
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void run() {
        while (getRunning().get()) {
            try {
                fireEpollEvent(waitForInterrupt(epollFd));
            } catch (IOException e) {
                LOG.error("Error polling for events", e);
                BulldogUtil.sleepMs(1000); // Breathing space
            }
        }
    }

    private PollResult[] waitForInterrupt(int epollFd) throws IOException {
        int readyCount = 0;

        while (!Thread.interrupted() && readyCount <= 0) {
            readyCount = LibEpoll.epoll_wait(epollFd, ptr, MAX_EVENTS, 1000);
        }

        List<PollResult> pollResults = new ArrayList<>(readyCount);
        for (int i = 0; i < readyCount; i++) {
            byte[] buffer = new byte[1];
            channel.seek(0);
            int bytesRead = channel.read(buffer, 0, buffer.length);
            if (bytesRead > 0) {
                pollResults.add(new BasicPollResult(buffer));
            }
        }

        return pollResults.toArray(new PollResult[0]);
    }

    @Override
    public void teardown() {
        if (isSetup.compareAndSet(true, false)) {
            stop();
            LibEpoll.epoll_ctl(epollFd, LibEpoll.EPOLL_CTL_DEL, fileDescriptor, (Pointer)null);
            LibEpoll.close(epollFd);
            try {
                channel.close();
            } catch (IOException e) {
                /* ignored */
            }
        }
    }

}