package com.doordeck.simplegpio.platform;

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

import com.doordeck.simplegpio.Pin;
import com.doordeck.simplegpio.gpio.base.DigitalIOFeature;
import com.doordeck.simplegpio.linux.gpio.LinuxDigitalInput;
import com.doordeck.simplegpio.linux.gpio.LinuxDigitalOutput;
import com.doordeck.simplegpio.spi.InputPollerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class PinFactory {

    private final ConcurrentMap<Integer, Pin> pins = new ConcurrentHashMap<>();
    private final InputPollerFactory inputPollerFactory;

    public PinFactory(InputPollerFactory inputPollerFactory) {
        if (inputPollerFactory == null) {
            throw new IllegalArgumentException("Input poller factory cannot be null");
        }
        this.inputPollerFactory = inputPollerFactory;
        createShutdownHook();
    }

    public Pin getPin(int address) {
        Pin p = pins.get(address);
        if (p == null) {
            // Ensure we're the only one creating pins
            synchronized(this) {
                p = pins.get(address);
                if (p == null) {
                    p = createSysFsDigitalIOPin(address);
                    pins.put(address, p);
                }
            }
        }
        return p;
    }

    private Pin createSysFsDigitalIOPin(int address) {
        Pin pin = new Pin(address);
        pin.addFeature(new DigitalIOFeature(pin, new LinuxDigitalInput(pin, inputPollerFactory), new LinuxDigitalOutput(pin)));
        return pin;
    }

    public void cleanup() {
        for (Pin pin : this.pins.values()) {
            if (pin.getActiveFeature() == null) {
                continue;
            }
            if (pin.getActiveFeature().isTorndownOnShutdown()) {
                pin.getActiveFeature().teardown();
            }
        }
    }

    private void createShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                cleanup();
            }
        });
    }
}
