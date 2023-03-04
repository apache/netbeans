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
package org.netbeans.modules.hudson.ui.util;

import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.openide.util.NbBundle;

/**
 * Helper class for UI and Usage logging.
 */
@SuppressWarnings("ClassWithMultipleLoggers")
public final class UsageLogging {

    private static final Logger UI_LOGGER = Logger.getLogger("org.netbeans.ui.hudson"); // NOI18N
    private static final Logger USG_LOGGER = Logger.getLogger("org.netbeans.ui.metrics.hudson"); // NOI18N


    private UsageLogging() {
    }

    /**
     * Log the UI gesture.
     */
    public static void logUI(ResourceBundle bundle, String message, Object... params) {
        assert bundle != null;
        assert message != null;

        LogRecord logRecord = createLogRecord(bundle, message, params);
        logRecord.setLoggerName(UI_LOGGER.getName());
        UI_LOGGER.log(logRecord);
    }

    /**
     * Log <a href="http://wiki.netbeans.org/UsageLoggingSpecification">usage data</a>.
     */
    public static void logUsage(Class<?> srcClass, String message, Object... params) {
        assert srcClass != null;
        assert message != null;

        LogRecord logRecord = createLogRecord(NbBundle.getBundle(srcClass), message, params);
        logRecord.setLoggerName(USG_LOGGER.getName());
        logRecord.setResourceBundleName(srcClass.getPackage().getName() + ".Bundle"); // NOI18N
        USG_LOGGER.log(logRecord);
    }

    private static LogRecord createLogRecord(ResourceBundle bundle, String message, Object... params) {
        LogRecord logRecord = new LogRecord(Level.INFO, message);
        logRecord.setResourceBundle(bundle);
        if (params != null) {
            logRecord.setParameters(params);
        }
        return logRecord;
    }

}
