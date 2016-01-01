package com.doordeck.simplegpio.linux.io;

import com.doordeck.simplegpio.gpio.event.PollResult;
import com.doordeck.simplegpio.gpio.io.AbstractInputPoller;
import com.doordeck.simplegpio.util.BulldogUtil;
import io.silverspoon.bulldog.linux.jni.NativeEpoll;

import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicBoolean;

/*
 * (C) Copyright 2014 libbulldog (http://libbulldog.org/) and others.
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

public class LinuxEpollThread extends AbstractInputPoller {

    private final AtomicBoolean isSetup = new AtomicBoolean(false);

    private int epollFd = 0;
    private int fileDescriptor = 0;

    public LinuxEpollThread(Path filename) {
        super(filename);
    }

    @Override
    public void setup() {
        if (isSetup.compareAndSet(false, true)) {
            epollFd = NativeEpoll.epollCreate();
            fileDescriptor = NativeEpoll.addFile(epollFd, NativeEpoll.EPOLL_CTL_ADD, getFilename().toString(), NativeEpoll.EPOLLPRI | NativeEpoll.EPOLLIN | NativeEpoll.EPOLLET);
        }
    }

    @Override
    public void stop() {
        if (getRunning().compareAndSet(true, false)) {
            NativeEpoll.stopWait(epollFd);

            //block until thread is dead
            while (isRunning()) {
                BulldogUtil.sleepMs(10);
            }

            teardown();
        }
    }

    public void run() {
        while (getRunning().get()) {
            PollResult[] results = NativeEpoll.waitForInterrupt(epollFd);
            if (results != null) {
                fireEpollEvent(results);
            }
        }
    }

    public void teardown() {
        if (isSetup.compareAndSet(true, false)) {
            stop();
            NativeEpoll.removeFile(epollFd, fileDescriptor);
            NativeEpoll.shutdown(epollFd);
        }
    }

}