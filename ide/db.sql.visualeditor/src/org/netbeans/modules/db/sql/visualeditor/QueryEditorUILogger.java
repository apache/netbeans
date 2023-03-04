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

package org.netbeans.modules.db.sql.visualeditor;

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.openide.util.NbBundle;

/**
 *
 * @author Andrei Badea
 */
public class QueryEditorUILogger {

    private static final Logger LOGGER = Logger.getLogger("org.netbeans.ui.db.sql.visualeditor"); // NOI18N

    private QueryEditorUILogger() {}

    public static void logEditorOpened() {
        log("UI_EDITOR_OPENED"); // NOI18N
    }

    private static void log(String message) {
        LogRecord record = new LogRecord(Level.INFO, message);
        record.setResourceBundle(NbBundle.getBundle(QueryEditorUILogger.class));
        record.setResourceBundleName(QueryEditorUILogger.class.getPackage().getName() + ".Bundle"); // NOI18N
        LOGGER.log(record);
    }
}
