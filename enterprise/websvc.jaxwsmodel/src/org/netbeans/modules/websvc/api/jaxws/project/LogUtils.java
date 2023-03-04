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

package org.netbeans.modules.websvc.api.jaxws.project;

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.openide.util.NbBundle;
import org.openide.util.Parameters;

/**
 *
 * @author mkuchtiak
 */
public class LogUtils {

    private static final Logger USG_LOGGER_WEBSVC = Logger.getLogger("org.netbeans.ui.metrics.websvc"); // NOI18N
    //private static final Logger USG_LOGGER_WEBSVC = Logger.getLogger("org.netbeans.modules.websvc");

    public static final String USG_WEBSVC_DETECTED = "USG_WEBSVC_DETECTED"; //NOI18N

    public static final String WS_STACK_JAXWS = "JAX-WS"; //NOI18N
    public static final String WS_STACK_JAXRS = "JAX-RS"; //NOI18N
    public static final String WS_STACK_JAXRPC = "JAX-RPC"; //NOI18N

    public static void logWsDetect(Object[] params) {
        log(USG_WEBSVC_DETECTED, params);
    }

    private static void log(String message, Object[] params) {
        Parameters.notNull("params", params); // NOI18N
        LogRecord logRecord = new LogRecord(Level.INFO, message);
        logRecord.setLoggerName(USG_LOGGER_WEBSVC.getName());
        logRecord.setResourceBundle(NbBundle.getBundle(LogUtils.class));
        logRecord.setResourceBundleName(LogUtils.class.getPackage().getName() + ".Bundle"); // NOI18N
        if (params != null) {
            logRecord.setParameters(params);
        }
        USG_LOGGER_WEBSVC.log(logRecord);
    }

}
