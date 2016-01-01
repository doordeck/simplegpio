package io.silverspoon.bulldog.linux.jni;

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

import com.doordeck.simplegpio.gpio.event.PollResult;
import com.doordeck.simplegpio.util.BulldogUtil;

public class NativePollResult implements PollResult {

    private int events;
    private int fd;
    private byte[] data;

    public NativePollResult(int fd, int events, byte[] data) {
        this.fd = fd;
        this.events = events;
        this.data = data;
    }

    public NativePollResult(byte... data) {
        this.data = data;
    }

    public int getEvents() {
        return events;
    }

    public int getFileDescriptor() {
        return fd;
    }

    public byte[] getData() {
        return data;
    }

    public String getDataAsString() {
        return BulldogUtil.bytesToString(getData());
    }
}

