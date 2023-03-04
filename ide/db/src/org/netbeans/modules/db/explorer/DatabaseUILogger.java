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

package org.netbeans.modules.db.explorer;

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 *
 * @author Andrei Badea
 */
public class DatabaseUILogger {

    private static final Logger LOGGER = Logger.getLogger("org.netbeans.ui.db.explorer"); // NOI18N
    private static final Logger LOGGER_USG = Logger.getLogger("org.netbeans.ui.metrics.db.explorer"); // NOI18N

    private DatabaseUILogger() {}

    public static void logConnection(String driverClass) {
        log("UI_CONNECT_DB", driverClass); // NOI18N
        logUsage("USG_DB_CONNECT", driverClass); // NOI18N
    }

    private static void log(String message, Object parameter) {
        LogRecord record = new LogRecord(Level.INFO, message);
        record.setParameters(new Object[] { parameter });
        LOGGER.log(record);
    }

    private static void logUsage(String message, Object parameter) {
        LogRecord record = new LogRecord(Level.INFO, message);
        record.setParameters(new Object[] { parameter });
        LOGGER_USG.log(record);
    }
}
