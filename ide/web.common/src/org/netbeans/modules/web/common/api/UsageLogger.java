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

package org.netbeans.modules.web.common.api;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.NullAllowed;
import org.openide.util.NbBundle;
import org.openide.util.Parameters;

/**
 * Helper class for <a href="http://wiki.netbeans.org/UsageLoggingSpecification">usage logging</a>.
 * <p>
 * By default, logger logs just the first message.
 * <p>
 * This class is thread safe.
 * @see Builder#firstMessageOnly(boolean)
 * @since 1.59
 */
public final class UsageLogger {

    private final Logger logger;
    private final Class<?> srcClass;
    private final String message;
    // != null if one-time logger; == null if log all messages
    @NullAllowed
    private final AtomicBoolean firstMessageOnly;


    private UsageLogger(Builder builder) {
        assert builder != null;
        assert builder.loggerName != null;
        this.logger = Logger.getLogger(builder.loggerName);
        this.srcClass = builder.srcClass;
        this.message = builder.message;
        this.firstMessageOnly = builder.firstMessageOnly ? new AtomicBoolean(false) : null;
    }

    /**
     * Is logging enabled?
     * @return {@code true} if logging is enabled, {@code false} otherwise (one-time logger with one message already logged)
     * @see #reset()
     * @since 1.89
     */
    public boolean isLoggingEnabled() {
        if (firstMessageOnly == null) {
            return true;
        }
        return !firstMessageOnly.get();
    }

    /**
     * Reset this logger to its initial state.
     * <p>
     * Currently, <tt>firstMessageOnly</tt> flag is cleared so the next
     * message is always logged.
     */
    public void reset() {
        if (firstMessageOnly != null) {
            firstMessageOnly.set(false);
        }
    }

    /**
     * Log the given message.
     * @param srcClass source class used to get {@link LogRecord#setResourceBundle(java.util.ResourceBundle) resource bundle}
     *        and its {@link LogRecord#setResourceBundleName(java.lang.String) name}
     * @param message message
     * @param params message parameters
     * @since 1.89
     */
    public void log(Class<?> srcClass, String message, Object... params) {
        Parameters.notNull("srcClass", srcClass); // NOI18N
        Parameters.notNull("message", message); // NOI18N
        logInternal(srcClass, message, params);
    }

    /**
     * Log the default message.
     * @param params message parameters
     * @since 1.89
     */
    public void log(Object... params) {
        if (srcClass == null) {
            throw new IllegalStateException("No srcClass set");
        }
        if (message == null) {
            throw new IllegalStateException("No message set");
        }
        logInternal(srcClass, message, params);
    }

    private void logInternal(Class<?> srcClass, String message, Object... params) {
        assert srcClass != null;
        assert message != null;
        if (!isLoggingEnabled()) {
            return;
        }
        if (firstMessageOnly != null
                && !firstMessageOnly.compareAndSet(false, true)) {
            return;
        }
        LogRecord logRecord = new LogRecord(Level.INFO, message);
        logRecord.setLoggerName(logger.getName());
        logRecord.setResourceBundle(NbBundle.getBundle(srcClass));
        logRecord.setResourceBundleName(srcClass.getPackage().getName() + ".Bundle"); // NOI18N
        logRecord.setParameters(params);
        logger.log(logRecord);
    }

    //~ Factories

    /**
     * Create usage logger for project browser.
     * @param loggerName name of usage logger name, e.g. &quot;org.netbeans.ui.metrics.php"
     * @return firstMessageOnly usage logger for project browser.
     */
    public static UsageLogger projectBrowserUsageLogger(String loggerName) {
        return new Builder(loggerName)
                .message(UsageLogger.class, "USG_PROJECT_BROWSER") // NOI18N
                .create();
    }

    /**
     * Create usage logger for JS test run.
     * @param loggerName name of usage logger name, e.g. &quot;org.netbeans.ui.metrics.php"
     * @return firstMessageOnly usage logger for JS test run.
     * @since 1.70
     */
    public static UsageLogger jsTestRunUsageLogger(String loggerName) {
        return new Builder(loggerName)
                .message(UsageLogger.class, "USG_TEST_RUN_JS") // NOI18N
                .create();
    }

    //~ Inner classes

    /**
     * Builder for {@link UsageLogger}.
     * <p>
     * <b>By default, such logger logs just the first message.</b>
     * @see #firstMessageOnly(boolean)
     */
    public static final class Builder {

        final String loggerName;

        Class<?> srcClass;
        String message;
        boolean firstMessageOnly = true;


        /**
         * Create new builder.
         * @param loggerName name of usage logger name, e.g. &quot;org.netbeans.ui.metrics.php"
         */
        public Builder(String loggerName) {
            Parameters.notNull("loggerName", loggerName); // NOI18N
            this.loggerName = loggerName;
        }

        /**
         * Set usage logger to one-time only, it means that only the first message
         * will be logged until logger is {@link UsageLogger#reset() reset}.
         * @param firstMessageOnly {@code true} for one-time logger
         * @return configured builder instance.
         * @since 1.89
         */
        public Builder firstMessageOnly(boolean firstMessageOnly) {
            this.firstMessageOnly = firstMessageOnly;
            return this;
        }

        /**
         * Set default message, used in {@link UsageLogger#log(java.lang.String[])}.
         * @param srcClass source class used to get {@link LogRecord#setResourceBundle(java.util.ResourceBundle) resource bundle}
         *        and its {@link LogRecord#setResourceBundleName(java.lang.String) name}
         * @param message message
         * @return configured builder instance.
         */
        public Builder message(Class<?> srcClass, String message) {
            Parameters.notNull("srcClass", srcClass); // NOI18N
            Parameters.notNull("message", message); // NOI18N
            this.srcClass = srcClass;
            this.message = message;
            return this;
        }

        /**
         * Create usage logger.
         * @return usage logger instance.
         */
        public UsageLogger create() {
            return new UsageLogger(this);
        }

    }

}
