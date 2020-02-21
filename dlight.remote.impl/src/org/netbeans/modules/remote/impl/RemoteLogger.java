/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.remote.impl;

import java.text.MessageFormat;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Handler;
import javax.swing.SwingUtilities;
import org.netbeans.modules.remote.impl.fs.RemoteFileObject;

public class RemoteLogger {

    private static final String DIAGNOSTICS_PSEUDO_MESSAGE = "__DIAGNOSTICS__"; //NOI18N
    private static class DiagnosticsHandler extends Handler {
        @Override
        public void publish(LogRecord record) {
            if (record.getMessage().equals(DIAGNOSTICS_PSEUDO_MESSAGE)) {
                boolean recursive = false;
                for (Object obj : record.getParameters()) {
                    if (obj instanceof Boolean) {
                        recursive = ((Boolean) obj).booleanValue();
                    }
                }
                for (Object obj : record.getParameters()) {
                    if (obj instanceof RemoteFileObject) {
                        ((RemoteFileObject) obj).getImplementor().diagnostics(recursive);
                    }
                }
            }
        }

        @Override
        public void flush() {}

        @Override
        public void close() {}
    }

    private static final java.util.logging.Logger instance =
            java.util.logging.Logger.getLogger("remote.support.logger"); // NOI18N

    static {
        instance.addHandler(new DiagnosticsHandler());
    }
    
    private static boolean assertionsEnabled = false;

    private static final boolean THROW_ASSERTIONS = Boolean.getBoolean("remote.throw.assertions");
    
    /** for test purposes */
    private static volatile AssertionError lastAssertion;

    static {
        assert (assertionsEnabled = true);
    }

    public static AssertionError getLastAssertion() {
        return lastAssertion;
    }
    
    public static boolean isDebugMode() {
        return assertionsEnabled;
    }

    private RemoteLogger() {}


    public static java.util.logging.Logger getInstance() {
        return instance;
    }

    public static boolean isLoggable(Level level) {
        return instance.isLoggable(level);
    }

    public static void assertTrueInConsole(boolean value, String message, Object... params) {
        if (assertionsEnabled && !value) {
            instance.log(Level.INFO, format(message, params));
        }
    }

    public static void assertTrue(boolean value) {
        assertTrue(value, "AssertionError");
    }

    public static void assertTrue(boolean value, String message, Object... params) {
        if (assertionsEnabled && !value) {
            message = format(message, params);
            lastAssertion = new AssertionError(message);
            if (THROW_ASSERTIONS) {
                throw lastAssertion;
            } else {
                instance.log(Level.SEVERE, message, lastAssertion);
            }
        }
    }
    
    public static void assertNotNull(Object value, String message, Object... params) {
        assertTrue(value != null, message, params);
    }

    public static void assertNull(Object value, String message, Object... params) {
        assertTrue(value == null, message, params);
    }

    public static void assertFalse(boolean value) {
        assertTrue(!value, "Assertion error"); //NOI18N
    }

    public static void assertFalse(boolean value, String message, Object... params) {
        assertTrue(!value, message, params);
    }

    public static void assertNonUiThread(String message) {
        assertFalse(SwingUtilities.isEventDispatchThread());
    }

    public static void assertNonUiThread() {
        assertNonUiThread("Should not be called from UI thread"); //NOI18N
    }

    public static void log(Level level, String message, Object... args) {
        if (instance.isLoggable(level)) {
            instance.log(level, message, args);
        }
    }
    
    public static void finest(Exception exception) {
        instance.log(Level.FINEST, "FYI:", exception);
    }
    
    public static void finest(Exception exception, Object source) {
        if (instance.isLoggable(Level.FINEST)) {
            instance.log(Level.FINEST, "FYI " + source, exception);
        }
    }
    
    public static void fine(Exception exception) {
        instance.log(Level.FINE, "FYI:", exception);
    }
    
    public static void info(Exception exception) {
        if (instance.isLoggable(Level.INFO)) {
            instance.log(Level.INFO, "Exception occurred:", exception);
        }
    }

    public static void info(Exception exception, Object source) {
        if (instance.isLoggable(Level.INFO)) {
            instance.log(Level.INFO, "Exception from " + source, exception);
        }
    }

    public static void warning(Exception exception) {
        if (instance.isLoggable(Level.WARNING)) {
            instance.log(Level.WARNING, "Exception occurred:", exception);
        }
    }
    
    private static String format(String message, Object... params) {
        try {
            return MessageFormat.format(message, params);
        } catch (Exception e) {
            e.printStackTrace();
            return message;
        }  
    }
            
    public static void logException(Level level, String msg, Object... params) {
        if (instance.isLoggable(level)) {
            String formattedMessage = format(msg, params);
            Exception ex = new Exception(formattedMessage);
            instance.log(level, formattedMessage, ex);
        }        
    }
    
    public static void severeException(String msg, Object... params) {
        logException(Level.SEVERE, msg, params);
    }

    public static void warningException(String msg, Object... params) {
        logException(Level.WARNING, msg, params);
    }

    public static void infoException(String msg, Object... params) {
        logException(Level.INFO, msg, params);
    }

    public static void severe(String msg, Object... params) {
        log(Level.SEVERE, msg, params);
    }

    public static void warning(String msg, Object... params) {
        log(Level.WARNING, msg, params);
    }

    public static void info(String msg, Object... params) {
        log(Level.INFO, msg, params);
    }

    public static void fine(String msg, Object... params) {
        log(Level.FINE, msg, params);
    }

    public static void finer(String msg, Object... params) {
        log(Level.FINER, msg, params);
    }
    
    public static void finest(String msg, Object... params) {
        log(Level.FINEST, msg, params);
    }    
}
