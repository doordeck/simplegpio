package com.doordeck.simplegpio.gpio.base;

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
import com.doordeck.simplegpio.gpio.DigitalOutput;

public abstract class AbstractDigitalOutput extends AbstractPinFeature implements DigitalOutput {

    private static final String NAME_FORMAT = "Digital Output - Signal '%s' on Pin %s";

    private Signal signal = Signal.Low;

    public AbstractDigitalOutput(Pin pin) {
        super(pin);
    }

    public String getName() {
        return String.format(NAME_FORMAT, signal, getPin().getName());
    }

    public void write(Signal signal) {
        applySignal(signal);
    }

    public void applySignal(Signal signal) {
        this.signal = signal;
        applySignalImpl(this.signal);
    }

    public void high() {
        applySignal(Signal.High);
    }

    public void low() {
        applySignal(Signal.Low);
    }

    public boolean isHigh() {
        return signal == Signal.High;
    }

    public boolean isLow() {
        return signal == Signal.Low;
    }

    public void toggle() {
        applySignal(signal.inverse());
    }

    public Signal getAppliedSignal() {
        return signal;
    }

    protected abstract void applySignalImpl(Signal signal);
}
