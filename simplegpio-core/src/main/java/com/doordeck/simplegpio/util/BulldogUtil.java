package com.doordeck.simplegpio.util;

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

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

public final class BulldogUtil {

    private static final Pattern REGEX_NUMERIC = Pattern.compile("-?\\d+(\\.\\d+)?");

    public static void sleepMs(final long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ex) {
            // Preserve interrupt flag
            Thread.currentThread().interrupt();
        }
    }

    public static String bytesToString(byte[] bytes, Charset encoding) {
        if (bytes == null) {
            throw new IllegalArgumentException("bytes may not be null in string conversion");
        }

        if (bytes.length == 0) {
            return null;
        }

        try {
            return new String(bytes, encoding);
        } catch (Exception e) {
            throw new IllegalArgumentException("Unknown encoding");
        }
    }

    public static String bytesToString(byte[] bytes) {
        return bytesToString(bytes, StandardCharsets.US_ASCII);
    }

    public static boolean isStringNumeric(String str) {
        return REGEX_NUMERIC.matcher(str).matches();
    }
}
