/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.nativeexecution.support;

import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import javax.swing.SwingUtilities;

public class Logger {

    private static boolean assertionsEnabled = false;
    private static Level nonUIThreadAssertionLevel;
    private static final long startTimeMillis = System.currentTimeMillis();
    private static final java.util.logging.Logger instance =
            java.util.logging.Logger.getLogger(
            "nativeexecution.support.logger"); // NOI18N

    static {
        assert (assertionsEnabled = true);
        String level = System.getProperty("Execution.nonUIThreadAsservionLevel", "INFO").toUpperCase(); // NOI18N
        try {
            nonUIThreadAssertionLevel = Level.parse(level);
        } catch (IllegalArgumentException ex) {
            nonUIThreadAssertionLevel = Level.INFO;
        }
        instance.addHandler(new LoggerHandler());
    }

    private Logger() {
    }

    public static java.util.logging.Logger getInstance() {
        return instance;
    }

    public static void severe(String message) {
        instance.severe(message);
    }

    public static void assertTrue(boolean value) {
        if (assertionsEnabled && !value) {
            String message = "Assertion error"; // NOI18N
            instance.log(Level.SEVERE, message, new Exception(message));
        }
    }

    public static void assertTrue(boolean value, String message) {
        if (assertionsEnabled && !value) {
            instance.log(Level.SEVERE, message, new Exception(message));
        }
    }

    public static void assertFalse(boolean value) {
        if (assertionsEnabled && value) {
            String message = "Assertion error"; // NOI18N
            instance.log(Level.SEVERE, message, new Exception(message));
        }
    }

    public static void assertFalse(boolean value, String message) {
        if (assertionsEnabled && value) {
            instance.log(Level.SEVERE, message, new Exception(message));
        }
    }

    public static void assertNonUiThread(String message) {
        if (assertionsEnabled && SwingUtilities.isEventDispatchThread()) {
            instance.log(nonUIThreadAssertionLevel, message, new Exception(message));
        }
    }

    public static void assertNonUiThread() {
        assertNonUiThread("Should not be called from UI thread"); // NOI18N
    }

    public static void fullThreadDump(String title) {
        final Set<Entry<Thread, StackTraceElement[]>> stack = Thread.getAllStackTraces().entrySet();
        System.err.printf("----- %s Start Thread Dump-----\n", title == null ? "" : title); // NOI18N
        for (Map.Entry<Thread, StackTraceElement[]> entry : stack) {
            System.err.println(entry.getKey().getName());
            for (StackTraceElement element : entry.getValue()) {
                System.err.println("\tat " + element.toString()); // NOI18N
            }
            System.err.println();
        }
        System.err.println("----- End Thread Dump-----"); // NOI18N
    }

    private static class LoggerHandler extends Handler {

        @Override
        public void publish(LogRecord record) {
            record.setMessage("[" + (System.currentTimeMillis() - startTimeMillis) + " ms.] " + record.getMessage()); // NOI18N
        }

        @Override
        public void flush() {
        }

        @Override
        public void close() throws SecurityException {
        }
    }
}
