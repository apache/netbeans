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

package org.netbeans.modules.dlight.libs.common;

import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;

/**
 */
public class DLightLibsCommonLogger {

    private static final Logger instance = Logger.getLogger("dlight.libs.common.logger"); // NOI18N

    private static boolean assertionsEnabled = false;
    
    /** for test purposes */
    private static volatile Throwable lastAssertion;
    private static final Set<StackElementArray> toStringStacks;

    static {
        assert (assertionsEnabled = true);
        toStringStacks = assertionsEnabled ? StackElementArray.createSet() : null;
    }
    
    public static Throwable getLastAssertion() {
        return lastAssertion;
    }
    
    public static boolean isDebugMode() {
        return assertionsEnabled;
    }

    private DLightLibsCommonLogger() {}


    public static java.util.logging.Logger getInstance() {
        return instance;
    }

    public static void assertTrueInConsole(boolean value, String message) {
        if (assertionsEnabled && !value) {
            instance.log(Level.INFO, message);
        }
    }

    public static void assertTrue(boolean value) {
        if (assertionsEnabled && !value) {
            String message = "Assertion error"; //NOI18N
            instance.log(Level.SEVERE, message, lastAssertion = new Exception(message));
        }
    }

    public static void assertTrue(boolean value, String message) {
        if (assertionsEnabled && !value) {
            instance.log(Level.SEVERE, message, lastAssertion = new Exception(message));
        }
    }

    public static void assertFalse(boolean value) {
        if (assertionsEnabled && value) {
            String message = "Assertion error"; //NOI18N
            instance.log(Level.SEVERE, message, lastAssertion = new Exception(message));
        }
    }

    public static void assertFalse(boolean value, String message) {
        if (assertionsEnabled && value) {
            instance.log(Level.SEVERE, message, lastAssertion = new Exception(message));
        }
    }
    
    public static void printStackTraceOnce(Throwable cause, Level level, boolean once, int stackCompareSize) {
        if (assertionsEnabled) {
            if (!once || StackElementArray.addStackIfNew(cause.getStackTrace(), toStringStacks, stackCompareSize)) {
                instance.log(level, cause.getMessage(), lastAssertion = cause);
            }
        }
    }    
    
    public static void printStackTraceOnce(Throwable cause, Level level, boolean once) {
        printStackTraceOnce(cause, level, once, 6);
    }

    public static void assertNonUiThread(String message, Level level, boolean once) {
        if (assertionsEnabled && SwingUtilities.isEventDispatchThread()) {
            if (!once || StackElementArray.addStackIfNew(toStringStacks, 8)) {
                instance.log(level, message, lastAssertion = new Exception(message));
            }
        }
    }
    
    public static void assertNonUiThread() {
        assertNonUiThread("Should not be called from UI thread", Level.SEVERE, false); //NOI18N
    }

    public static void assertNonUiThreadOnce(Level level) {
        assertNonUiThread("Should not be called from UI thread", level, true); //NOI18N
    }

    public static void finest(Exception exception) {
        instance.log(Level.FINEST, "FYI:", exception);
    }
}
