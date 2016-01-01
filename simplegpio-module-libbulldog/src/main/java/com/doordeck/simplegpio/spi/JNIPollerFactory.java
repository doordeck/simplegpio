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

import com.doordeck.simplegpio.linux.io.LinuxEpollThread;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class JNIPollerFactory implements InputPollerFactory {

    private static final Logger LOG = LoggerFactory.getLogger(JNIPollerFactory.class);
    private static final String LIB_NAME = System.getProperty("bulldog.native", "bulldog-linux-native");

    private static Path loadLibraryFromClasspath(String path) throws IOException {
        File targetFile = File.createTempFile(LIB_NAME, ".so");
        targetFile.deleteOnExit();
        try (InputStream source = JNIPollerFactory.class.getClassLoader().getResourceAsStream(path)) {
            if (source == null) {
                throw new FileNotFoundException("File " + path + " was not found in classpath.");
            }
            Files.copy(source, targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
        // Finally, load the library
        System.load(targetFile.getAbsolutePath());
        return targetFile.toPath();
    }

    private static Path loadLibraryFromLocalPath(String filename) throws IOException {
        Path p = Paths.get(filename).toAbsolutePath();
        System.load(p.toString());
        return p;
    }

    @Override
    public boolean isValid() {
        boolean loaded = false;
        try {
            Path p = loadLibraryFromClasspath(LIB_NAME + ".so");
            loaded = true;
            LOG.trace("Loaded EPoll library from {}", p);

        } catch (UnsatisfiedLinkError | SecurityException | IOException e) {
            LOG.trace("Unable to load EPoll library from classpath: ", e);
        }

        if (!loaded) {
            try {
                Path p = loadLibraryFromLocalPath(LIB_NAME + ".so");
                LOG.trace("Loaded EPoll library from {}", p);
                loaded = true;
            } catch (UnsatisfiedLinkError | SecurityException | IOException e) {
                LOG.trace("Unable to load EPoll library from local path", e);
            }
        }
        return loaded;
    }

    public InputPoller build(final Path gpioPath) {
        return new LinuxEpollThread(gpioPath);
    }
}
