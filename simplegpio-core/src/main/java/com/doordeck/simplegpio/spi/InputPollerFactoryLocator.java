package com.doordeck.simplegpio.spi;

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

import com.doordeck.simplegpio.gpio.io.BasicInputPollerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ServiceLoader;

public class InputPollerFactoryLocator {

    private static final Logger LOG = LoggerFactory.getLogger(InputPollerFactoryLocator.class);

    private InputPollerFactoryLocator() {
        /* static class */
    }

    public static InputPollerFactory locate() {
        ServiceLoader<InputPollerFactory> sl = ServiceLoader.load(InputPollerFactory.class);
        for (InputPollerFactory factory : sl) {
            boolean valid = factory.isValid();
            LOG.debug("Trying to load {}... {}", factory.getClass().getSimpleName(), valid ? "success" : "failed");
            if (valid) {
                return factory;
            }
        }
        return new BasicInputPollerFactory();
    }
}
