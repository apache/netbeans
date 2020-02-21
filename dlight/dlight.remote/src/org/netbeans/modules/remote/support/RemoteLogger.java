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
package org.netbeans.modules.remote.support;

import java.util.logging.Level;
import javax.swing.SwingUtilities;

public class RemoteLogger {

    private static final java.util.logging.Logger instance =
            java.util.logging.Logger.getLogger("remote.support.logger"); // NOI18N
    
    private static boolean assertionsEnabled = false;
    
    /** for test purposes */
    private static volatile Exception lastAssertion;

    static {
        assert (assertionsEnabled = true);
    }

    public static Exception getLastAssertion() {
        return lastAssertion;
    }
    
    public static boolean isDebugMode() {
        return assertionsEnabled;
    }

    private RemoteLogger() {}


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

    public static void assertNonUiThread(String message) {
        if (assertionsEnabled && SwingUtilities.isEventDispatchThread()) {
            instance.log(Level.SEVERE, message, lastAssertion = new Exception(message));
        }
    }

    public static void assertNonUiThread() {
        assertNonUiThread("Should not be called from UI thread"); //NOI18N
    }

    public static void finest(Exception exception) {
        instance.log(Level.FINEST, "FYI:", exception);
    }
}
