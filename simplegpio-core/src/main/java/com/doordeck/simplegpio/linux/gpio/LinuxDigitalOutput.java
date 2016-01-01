package com.doordeck.simplegpio.linux.gpio;

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

import com.doordeck.simplegpio.Pin;
import com.doordeck.simplegpio.Signal;
import com.doordeck.simplegpio.gpio.base.AbstractDigitalOutput;
import com.doordeck.simplegpio.linux.sysfs.SysFsPin;

public class LinuxDigitalOutput extends AbstractDigitalOutput {

    private SysFsPin sysFsPin;

    public LinuxDigitalOutput(Pin pin) {
        super(pin);
        sysFsPin = createSysFsPin(pin);
    }

    protected SysFsPin createSysFsPin(Pin pin) {
        return new SysFsPin(pin.getAddress());
    }

    @Override
    protected void setupImpl() {
        exportPinIfNecessary();
    }

    @Override
    protected void teardownImpl() {
        unexportPin();
    }

    protected void exportPinIfNecessary() {
        sysFsPin.exportIfNecessary();
        sysFsPin.setDirection("out");
    }

    protected void unexportPin() {
        sysFsPin.unexport();
    }

    @Override
    protected void applySignalImpl(Signal signal) {
        sysFsPin.setValue(signal);
    }

}