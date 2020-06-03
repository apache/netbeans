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

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 *
 */
public final class UIGesturesSupport {
    
    // Utility class
    private UIGesturesSupport() {
    }

    private static final Logger USG_LOGGER = Logger.getLogger("org.netbeans.ui.metrics.cnd"); // NOI18N

    public static void submit(String type, Object... params) {
        // Do not track from unit tests
        if (CndUtils.isUnitTestMode()) {
            return;
        }
        LogRecord record = new LogRecord(Level.INFO, type);
//        record.setResourceBundle(NbBundle.getBundle(UIGesturesSupport.class));
//        record.setResourceBundleName(UIGesturesSupport.class.getPackage().getName() + ".Bundle"); // NOI18N
        record.setLoggerName(USG_LOGGER.getName());

        record.setParameters(params);

        USG_LOGGER.log(record);
    }
}
