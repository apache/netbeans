/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.repository;

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
            "repository.support.logger"); // NOI18N

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
