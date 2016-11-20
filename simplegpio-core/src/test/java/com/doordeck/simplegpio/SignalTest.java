package com.doordeck.simplegpio;

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

import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class SignalTest {

    @Test
    public void testSignal() {
        assertEquals(Signal.Low, Signal.fromBooleanValue(false));
        assertEquals(Signal.High, Signal.fromBooleanValue(true));

        assertEquals(Signal.Low, Signal.fromNumericValue(0));
        assertEquals(Signal.High, Signal.fromNumericValue(-1));
        assertEquals(Signal.High, Signal.fromNumericValue(1));

        assertEquals(Signal.Low, Signal.fromString("0"));
        assertEquals(Signal.High, Signal.fromString("1"));
        assertEquals(Signal.Low, Signal.fromString("-0"));

        assertEquals(Signal.Low, Signal.fromString("LOW"));
        assertEquals(Signal.High, Signal.fromString("HIGH"));
        assertEquals(Signal.Low, Signal.fromString("low"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSignalFromNullStringThrowsException() {
        Signal.fromString(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSignalFromNonsenseStringThrowsException() {
        Signal.fromString("highlow");
    }
}
