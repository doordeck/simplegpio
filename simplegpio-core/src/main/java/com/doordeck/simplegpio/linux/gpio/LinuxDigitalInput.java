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

import com.doordeck.simplegpio.Edge;
import com.doordeck.simplegpio.Pin;
import com.doordeck.simplegpio.Signal;
import com.doordeck.simplegpio.gpio.base.AbstractDigitalInput;
import com.doordeck.simplegpio.gpio.event.InterruptEventArgs;
import com.doordeck.simplegpio.gpio.event.InterruptListener;
import com.doordeck.simplegpio.gpio.event.PollResult;
import com.doordeck.simplegpio.gpio.io.InputPollListener;
import com.doordeck.simplegpio.linux.sysfs.SysFsPin;
import com.doordeck.simplegpio.spi.InputPoller;
import com.doordeck.simplegpio.spi.InputPollerFactory;
import com.doordeck.simplegpio.util.BulldogUtil;

public class LinuxDigitalInput extends AbstractDigitalInput implements InputPollListener {

    private InputPoller interruptControl;
    private SysFsPin sysFsPin;
    private Edge lastEdge;
    private volatile long lastInterruptTime;

    public LinuxDigitalInput(Pin pin, InputPollerFactory gpioPollerFactory) {
        super(pin);
        sysFsPin = createSysFsPin(getPin());
        interruptControl = gpioPollerFactory.build(sysFsPin.getValueFilePath());
        interruptControl.addListener(this);
    }

    protected SysFsPin createSysFsPin(Pin pin) {
        return new SysFsPin(pin.getAddress());
    }

    public Signal read() {
        return sysFsPin.getValue();
    }

    @Override
    public void addInterruptListener(InterruptListener listener) {
        super.addInterruptListener(listener);
        if (areInterruptsEnabled() && !interruptControl.isRunning()) {
            interruptControl.start();
        }
    }

    @Override
    public void removeInterruptListener(InterruptListener listener) {
        super.removeInterruptListener(listener);
        if (getInterruptListeners().size() == 0) {
            interruptControl.stop();
        }
    }

    @Override
    public void clearInterruptListeners() {
        super.clearInterruptListeners();
        interruptControl.stop();
    }

    protected void enableInterruptsImpl() {
        if (getInterruptListeners().size() > 0 && !interruptControl.isRunning()) {
            interruptControl.start();
        }
    }

    protected void disableInterruptsImpl() {
        interruptControl.stop();
    }

    @Override
    protected void setupImpl() {
        exportPinIfNecessary();
    }

    @Override
    protected void teardownImpl() {
        disableInterrupts();
        unexportPin();
    }

    protected void exportPinIfNecessary() {
        sysFsPin.exportIfNecessary();
        sysFsPin.setDirection("in");
        sysFsPin.setEdge(getInterruptTrigger().toString().toLowerCase());
    }

    protected void unexportPin() {
        sysFsPin.unexport();
    }

    @Override
    public void setInterruptTrigger(Edge edge) {
        super.setInterruptTrigger(edge);
        sysFsPin.setEdge(getInterruptTrigger().toString().toLowerCase());
    }

    @Override
    public void processEpollResults(PollResult[] results) {
        for (PollResult result : results) {
            Edge edge = getEdge(result);
            if (lastEdge != null && lastEdge.equals(edge)) {
                continue;
            }

            long delta = System.currentTimeMillis() - lastInterruptTime;
            if (delta <= this.getInterruptDebounceMs()) {
                continue;
            }

            lastInterruptTime = System.currentTimeMillis();
            lastEdge = edge;
            fireInterruptEvent(new InterruptEventArgs(getPin(), edge));
        }
    }

    private Edge getEdge(PollResult result) {
        if (result.getData() == null) {
            return null;
        }
        if ( BulldogUtil.bytesToString(result.getData()).charAt(0) == '1') {
            return Edge.Rising;
        }

        return Edge.Falling;
    }

    public SysFsPin getSysFsPin() {
        return sysFsPin;
    }
}
