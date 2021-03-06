package com.doordeck.simplegpio.gpio;

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

import com.doordeck.simplegpio.Signal;

/**
 * This interface specified the operations that can be used on a Pin that is
 * configured as a digital output. This includes setting the state on the pin
 * and also some convenience features.
 */
public interface DigitalOutput extends PinFeature {

    /**
     * Writes a state to the pin.
     *
     * @param signal
     *       the signal
     */
    void write(Signal signal);

    /**
     * Writes a state to the pin.
     *
     * @param signal
     *       the signal
     */
    void applySignal(Signal signal);

    /**
     * Gets the signal that is currently applied to the pin.
     *
     * @return the applied signal
     */
    Signal getAppliedSignal();

    /**
     * Applies a high level signal to the pin.
     */
    void high();

    /**
     * Applies a low level signal to the pin.
     */
    void low();

    /**
     * Toggles the current signal on the pin.
     */
    void toggle();

    /**
     * Queries if the signal on the pin
     * is currently high.
     *
     * @return true, if the signal is high
     */
    boolean isHigh();

    /**
     * Queries if the signal on the pin
     * is currently low.
     *
     * @return true, if the signal is low
     */
    boolean isLow();
}
