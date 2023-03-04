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

package org.netbeans.modules.projectimport.eclipse.core;

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.openide.util.NbBundle;
import org.openide.util.Parameters;

/**
 * Utilities relating to Ant projects.
 * @author Jesse Glick
 */
public class Util {

    private static final Logger USG_LOGGER = Logger.getLogger("org.netbeans.ui.metrics.projects"); // NOI18N

    private Util() {}

    public static void logUsage(Class srcClass, String message, Object ...params) {
        Parameters.notNull("message", message); // NOI18N

        LogRecord logRecord = new LogRecord(Level.INFO, message);
        logRecord.setLoggerName(USG_LOGGER.getName());
        logRecord.setResourceBundle(NbBundle.getBundle(srcClass));
        logRecord.setResourceBundleName(srcClass.getPackage().getName() + ".Bundle"); // NOI18N
        if (params != null) {
            logRecord.setParameters(params);
        }
        USG_LOGGER.log(logRecord);
    }
    
    public static boolean isAbsolutePath(String path) {
        return (path.startsWith("/") || // NOI18N
                (path.length() >= 3 && path.substring(1, 3).replace('\\', '/').equals(":/")) || // NOI18N
                path.startsWith("\\\\")); // NOI18N
    }
    
}
