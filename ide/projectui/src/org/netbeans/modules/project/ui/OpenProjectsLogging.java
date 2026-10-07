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
package org.netbeans.modules.project.ui;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.netbeans.api.project.Project;
import org.openide.util.NbBundle;

/** Logging utilities related to {@link OpenProjectList}. Some of these
 * methods help with gathering telemetry data, which are not really processed
 * anywhere anymore. 
 */
final class OpenProjectsLogging {
    static final Logger LOGGER = Logger.getLogger(OpenProjectList.class.getName());

    static LogRecord[] createRecord(String msg, Project[] projects) {
        if (projects.length == 0) {
            return null;
        }
        var counts = new HashMap<String, int[]>();
        for (Project p : projects) {
            String n = p.getClass().getName();
            int[] cnt = counts.get(n);
            if (cnt == null) {
                cnt = new int[1];
                counts.put(n, cnt);
            }
            cnt[0]++;
        }
        Logger logger = Logger.getLogger("org.netbeans.ui.projects"); // NOI18N
        LogRecord[] arr = new LogRecord[counts.size()];
        int i = 0;
        for (Map.Entry<String, int[]> entry : counts.entrySet()) {
            LogRecord rec = new LogRecord(Level.CONFIG, msg);
            rec.setParameters(new Object[]{entry.getKey(), afterLastDot(entry.getKey()), entry.getValue()[0]});
            rec.setLoggerName(logger.getName());
            rec.setResourceBundle(NbBundle.getBundle(OpenProjectList.class));
            rec.setResourceBundleName(OpenProjectList.class.getPackage().getName() + ".Bundle");
            arr[i++] = rec;
        }
        return arr;
    }

    static LogRecord[] createRecordMetrics(String msg, Project[] projects) {
        if (projects.length == 0) {
            return null;
        }
        Logger logger = Logger.getLogger("org.netbeans.ui.metrics.projects"); // NOI18N
        LogRecord[] arr = new LogRecord[projects.length];
        int i = 0;
        for (Project p : projects) {
            LogRecord rec = new LogRecord(Level.INFO, msg);
            rec.setParameters(new Object[]{p.getClass().getName()});
            rec.setLoggerName(logger.getName());
            arr[i++] = rec;
        }
        return arr;
    }

    private static String afterLastDot(String s) {
        int index = s.lastIndexOf('.');
        if (index == -1) {
            return s;
        }
        return s.substring(index + 1);
    }

    static void log(LogRecord r) {
        LOGGER.log(r);
    }

    static void log(Level l, String msg, Object... params) {
        LOGGER.log(l, msg, params);
    }

    static void log(Level l, String msg, Throwable e) {
        LOGGER.log(l, msg, e);
    }

    static void log(LogRecord[] arr, String loggerName) {
        if (arr == null) {
            return;
        }
        Logger logger = Logger.getLogger(loggerName); // NOI18N
        for (LogRecord r : arr) {
            logger.log(r);
        }
    }

    static void logProjects(String message, Project[] projects) {
        if (projects.length == 0) {
            return;
        }
        for (Project p : projects) {
            LOGGER.log(Level.FINER, "{0} {1}", new Object[]{message, p == null ? null : p.toString()});
        }
    }
}
