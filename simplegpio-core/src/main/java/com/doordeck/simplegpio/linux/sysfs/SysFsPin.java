package com.doordeck.simplegpio.linux.sysfs;

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
import com.doordeck.simplegpio.util.BulldogUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class SysFsPin {

    private static final Logger LOG = LoggerFactory.getLogger(SysFsPin.class);
    private static final String directory = "/sys/class/gpio";
    private static final Path exportPath = Paths.get(directory, "export");
    private static final Path unexportPath = Paths.get(directory, "unexport");

    private int pin;
    private String pinString;
    private Path pinDirectory, valuePath, edgePath, directionPath;

    public SysFsPin(int pin) {
        this.pin = pin;
        updateValues();
    }

    private void updateValues(){
        this.pinString = String.valueOf(pin);
        this.pinDirectory = Paths.get(directory, "gpio" + pinString);
        this.valuePath = Paths.get(pinDirectory + "/value");
        this.edgePath = Paths.get(pinDirectory + "/edge");
        this.directionPath = Paths.get(pinDirectory + "/direction");
    }

    public boolean isExported() {
        LOG.debug("Checking if {} exists... {}", getPinDirectory(), Files.exists(getPinDirectory()));
        return Files.exists(getPinDirectory());
    }

    public void exportIfNecessary() {
        if (!isExported()) {
            echoToFile(getPinString(), exportPath);

            long startTime = System.currentTimeMillis();
            while (!Files.exists(getValueFilePath())) {
                BulldogUtil.sleepMs(10);
                if ((System.currentTimeMillis() - startTime) >= 10000) {
                    throw new RuntimeException("Could not create pin - waited 10 seconds. Aborting.");
                }
            }
        }
    }

    public void unexport() {
        if (isExported()) {
            echoToFile(getPinString(), unexportPath);
        }
    }

    public void setEdge(String edge) {
        echoToFile(edge, this.edgePath);
    }

    public void setDirection(String direction) {
        echoToFile(direction, this.directionPath);
    }

    public Path getPinDirectory() {
        return this.pinDirectory;
    }

    public Path getValueFilePath() {
        return this.valuePath;
    }

    private String getPinString() {
        return pinString;
    }

    public Signal getValue() {
        try {
            return Signal.fromString(new String(Files.readAllBytes(getValueFilePath())));
        } catch (IOException e) {
            LOG.error("IOException fetching value", e);
        }
        return null;
    }

    public void setValue(Signal signal) {
        echoToFile(String.valueOf(signal.getNumericValue()), getValueFilePath());
    }

    private void echoToFile(String value, Path file) {
        try (BufferedWriter writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8, StandardOpenOption.TRUNCATE_EXISTING)) {
            writer.write(value);
        } catch (IOException x) {
            LOG.error("IOException writing to file", x);
        }
    }
}