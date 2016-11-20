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

import com.doordeck.simplegpio.spi.InputPoller;
import com.doordeck.simplegpio.spi.InputPollerFactory;

import java.nio.file.Path;

public class BasicInputPollerFactory implements InputPollerFactory {

    @Override
    public boolean isValid() {
        // Basic poller is always valid
        return true;
    }

    @Override
    public InputPoller build(final Path filename) {
        return new BasicInputPoller(filename);
    }


}
