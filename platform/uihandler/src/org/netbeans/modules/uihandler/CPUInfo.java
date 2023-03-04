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
package org.netbeans.modules.uihandler;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 *
 * @author Jindrich Sedek
 */
public class CPUInfo {
    static final String MESSAGE = "CPU INFO";

    static void logCPUInfo() {
        LogRecord log = new LogRecord(Level.FINEST, MESSAGE); // NOI18N
        OperatingSystemMXBean bean = ManagementFactory.getOperatingSystemMXBean();
        Object [] params = new Object[]{bean.getAvailableProcessors()};
        log.setParameters(params);
        Logger.getLogger(Installer.UI_PERFORMANCE_LOGGER_NAME).log(log);
    }
}
