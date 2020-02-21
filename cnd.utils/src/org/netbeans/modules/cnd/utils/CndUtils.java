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

package org.netbeans.modules.cnd.utils;

import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.netbeans.modules.dlight.libs.common.DLightLibsCommonLogger;
import org.openide.filesystems.FileSystem;
import org.openide.util.CharSequences;

/**
 *
 */
public class CndUtils {

    private static final Logger LOG = Logger.getLogger("cnd.logger"); // NOI18N

    private static boolean releaseMode;
    private static volatile Throwable lastAssertion;
    private static final int STACK_THREASHOLD = Integer.parseInt(System.getProperty("cnd.utils.same.stacks", "3")); // NOI18N
    static {
        String text = System.getProperty("cnd.release.mode");
        if (text == null) {
            releaseMode = true;
            assert ((releaseMode = false) == false);
        } else {
            releaseMode = Boolean.parseBoolean(text);
        }
    }

    private CndUtils() {
    }

    public static boolean isStandalone() {
        if ("true".equals(System.getProperty ("cnd.command.line.utility"))) { // NOI18N
            return true;
        }
        // headless is the same as standalone
        if (getBoolean("java.awt.headless", false)) { // NOI18N
            return true;
        }
        return !CndUtils.class.getClassLoader().getClass().getName().startsWith("org.netbeans."); // NOI18N
    }
    
    public static boolean isReleaseMode() {
        return releaseMode;
    }

    public static boolean isDebugMode() {
        return ! isReleaseMode();
    }

    public static boolean isUnitTestMode() {
        return Boolean.getBoolean("cnd.mode.unittest"); // NOI18N
    }

    public static boolean isCodeCompletionUnitTestMode() {
        return Boolean.getBoolean("cnd.mode.completion.unittest"); // NOI18N
    }

    public static boolean getBoolean(String name, boolean result) {
        String text = System.getProperty(name);
        if (text != null) {
            result = Boolean.parseBoolean(text);
        }
        return result;
    }

    public static void threadsDump(){
        final Set<Entry<Thread, StackTraceElement[]>> stack = Thread.getAllStackTraces().entrySet();
        System.err.println("-----Start Thread Dump-----");
        for (Map.Entry<Thread, StackTraceElement[]> entry : stack) {
            System.err.println(entry.getKey().getName());
            for (StackTraceElement element : entry.getValue()) {
                System.err.println("\tat " + element.toString());
            }
            System.err.println();
        }
        System.err.println("-----End Thread Dump-----");
    }

    public static void assertTrue(boolean value) {
        if (!value && isDebugMode()) {
            severe("Assertion error"); //NOI18N
        }
    }

    public static void assertNotNull(Object object, String message) {
        if (object == null && isDebugMode()) {
            severe(message);
        }
    }

    public static void assertUnconditional(String message) {
        if (isDebugMode()) {
            severe(message);
        }
    }

    public static void assertNotNull(Object object, CharSequence prefix, Object message) {
        if (object == null && isDebugMode()) {
            severe(prefix.toString() + message);
        }
    }

    public static void assertNotNullInConsole(Object object, String message) {
        if (object == null && isDebugMode()) {
            info(message);
        }
    }

    public static void assertNull(Object object, String message) {
        if (object != null && isDebugMode()) {
            severe(message);
        }
    }

    public static int getNumberCndWorkerThreads() {
        int threadCount = Math.min(4, Runtime.getRuntime().availableProcessors()-2);
        if (System.getProperty("cnd.modelimpl.parser.threads") != null) { // NOI18N
            threadCount = Integer.getInteger("cnd.modelimpl.parser.threads"); // NOI18N
        }
        return Math.max(threadCount, 1);
    }

    public static int getConcurrencyLevel() {
        return getNumberCndWorkerThreads();
    }

    public static void assertFalse(boolean value) {
       if (value && isDebugMode()) {
           severe("Assertion error"); //NOI18N
       }
   }

    public static void assertFalse(boolean value, String message) {
        assertTrue(!value, message);
    }

    public static void assertTrue(boolean value, String message) {
        if (isDebugMode() && !value) {
            severe(message);
        }
    }

    private static String addThreadName(String msg) {
        return msg + "\n@[" + Thread.currentThread().getName() + "]"; // NOI18N
    }
    
    public static void assertTrue(boolean value, String prefix, Object message) {
        if (isDebugMode() && !value) {
            printStackTraceOnce(lastAssertion = new Exception(addThreadName(prefix + message)));
        }
    }

    public static void severe(Exception exception) {
         LOG.log(Level.SEVERE, exception.getMessage(), lastAssertion = exception);
    }

    private static void severe(String message) {
        LOG.log(Level.SEVERE, message, lastAssertion = new Exception(addThreadName(message)));
    }

    private static void info(String message) {
        if (LOG.isLoggable(Level.INFO)) {
            LOG.log(Level.INFO, message, lastAssertion = new Exception(addThreadName(message)));
        }
    }
    
    private static final ConcurrentHashMap<CharSequence,AtomicInteger> restrictLog = new ConcurrentHashMap<CharSequence,AtomicInteger>();
    public static void assertTrueInConsole(boolean value, String message) {
        if (isDebugMode() && !value && LOG.isLoggable(Level.INFO)) {
            Exception exception = new Exception(addThreadName(message));
            Level level = Level.INFO;
            StackTraceElement[] stackTrace = exception.getStackTrace();
            for(StackTraceElement element : stackTrace) {
                String className = element.getClassName();
                if (!className.contains(".CndUtils")) { //NOI18N
                    int lineNumber = element.getLineNumber();
                    CharSequence key = CharSequences.create(className+"-"+lineNumber); //NOI18N
                    AtomicInteger counter = new AtomicInteger();
                    AtomicInteger old = restrictLog.putIfAbsent(key, counter);
                    if (old != null) {
                        counter = old;
                    }
                    int i = counter.incrementAndGet();
                    if (i > STACK_THREASHOLD) {
                        level = Level.FINE;
                    }
                    break;
                }
            }
            lastAssertion = exception;
            LOG.log(level, message, exception);
        }
    }

    private static boolean pathsEqual(CharSequence path1, CharSequence path2) {
        if (path1 == null) {
            return (path2 == null);
        } else if (path2 == null) {
            return false;
        } else {
            if (path1 == path2) {
                return true;
            }
            int len = path1.length();
            if (len == path2.length()) {
                for (int i = len - 1; i >= 0; i--) {
                    char c1 = path1.charAt(i);
                    char c2 = path2.charAt(i);
                    if (c1 == '/' || c1 == '\\') {
                        if (c2 != '/' && c2 != '\\') {
                            return false;
                        }
                    } else {
                        if (c1 != c2) {
                            return false;
                        }
                    }

                }
                return true;
            }
            return false;
        }
    }

    public static void assertPathsEqualInConsole(CharSequence path1, CharSequence path2, String format, Object... args) {
        if (isDebugMode() && ! pathsEqual(path1, path2)) {
            String text = java.text.MessageFormat.format(format, args);
            assertTrueInConsole(false, text);
        }
    }

    public static void assertTrueInConsole(boolean value, String prefix, Object message) {
        if (isDebugMode() && !value && LOG.isLoggable(Level.INFO)) {
            assertTrueInConsole(value, prefix + message);
        }
    }

    public static Throwable getLastAssertion() {
        return lastAssertion;
    }

    public static void clearLastAssertion() {
        lastAssertion = null;
    }

    public static void assertAbsolutePathInConsole(String path) {
        if (CndUtils.isDebugMode()) {
            if (! CndPathUtilities.isPathAbsolute(path)) {
                CndUtils.assertTrueInConsole(false, "path must be absolute " + path);
            }
        }
    }

    public static void assertAbsolutePathInConsole(String path, String message) {
        if (CndUtils.isDebugMode()) {
            if (! CndPathUtilities.isPathAbsolute(path)) {
                CndUtils.assertTrueInConsole(false, message + ' ' + path);
            }
        }
    }

    public static void assertAbsoluteFileInConsole(File file) {
        assertAbsoluteFileInConsole(file, "Absolute path should be used"); //NOI18N
    }

    public static void assertAbsoluteFileInConsole(File file, String message) {
        if (CndUtils.isDebugMode()) {
            if (! file.isAbsolute()) {
                CndUtils.assertTrueInConsole(false, message + ' ' + file.getPath());
            }
        }
    }
    
    
    public static void printStackTraceOnce(Throwable exception, int stackCompareSize) {
        DLightLibsCommonLogger.printStackTraceOnce(exception, Level.INFO, true, stackCompareSize);
    }
    
    public static void printStackTraceOnce(Throwable exception) {
        DLightLibsCommonLogger.printStackTraceOnce(exception, Level.INFO, true);
    }

    public static void assertNonUiThread() {
        if (SwingUtilities.isEventDispatchThread()) {
            DLightLibsCommonLogger.assertNonUiThreadOnce(Level.INFO);
        }
    }

    public static void logMessageOnce(Logger logger, Level level, String message, Object... params) {
        LogOnceSupport.logMessageOnce(logger, level, message, params);
    }

    public static void assertUiThread() {
        assertTrue(SwingUtilities.isEventDispatchThread(), "Should be called only from UI thread"); //NOI18N
    }

    public static void assertNormalized(File file) {
        if (isDebugMode()) {
            File normFile = CndFileUtils.normalizeFile(file);
            if (!file.equals(normFile)) {
                assertTrueInConsole(false, "Parameter file was not normalized. Was " + file + " instead of " + normFile); // NOI18N
            }
        }
    }

    public static void assertNormalized(FileSystem fs, CharSequence absPath) {
        if (isDebugMode()) {
            String normFile = CndFileUtils.normalizeAbsolutePath(fs, absPath.toString());
            if (!normFile.contentEquals(absPath)) {
                assertTrueInConsole(false, "Parameter file was not normalized. Was " + absPath + " instead of " + normFile); // NOI18N
            }
        }
    }

    public static Logger getLogger() {
        return LOG;
    }

    private static class LogOnceSupport {

        private static final Object PRESENT = new Object();
        private static final Map<String, Object> ALREADY_LOGGED = new ConcurrentHashMap<>();

        public static void logMessageOnce(Logger logger, Level level, String message, Object... params) {
            ALREADY_LOGGED.computeIfAbsent(message, s -> {
                logger.log(level, message, params);
                return PRESENT;
            });
        }
    }
}
