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

package org.netbeans.api.debugger;

import org.openide.util.NbBundle;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;


/**
 * A utility class for submitting UI Gestures Collector records
 * @author Martin Entlicher
 */
class GestureSubmitter {
    //~ Static fields/initializers -----------------------------------------------------------------------------------------------

    private static final Logger USG_LOGGER = Logger.getLogger("org.netbeans.ui.metrics.debugger"); // NOI18N

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    static void logDebugStart(Session s, List<DebuggerEngine> engines) {
        LogRecord record = new LogRecord(Level.INFO, "USG_DEBUG_SESSION_START"); // NOI18N
        record.setResourceBundle(NbBundle.getBundle(GestureSubmitter.class));
        record.setResourceBundleName(GestureSubmitter.class.getPackage().getName() + ".Bundle"); // NOI18N
        record.setLoggerName(USG_LOGGER.getName());
        List<String> params = new ArrayList<>();
        params.add(s.getName());
        params.add(s.getLocationName());
        params.add(s.getCurrentLanguage());
        record.setParameters(params.toArray());
        USG_LOGGER.log(record);
    }
}
