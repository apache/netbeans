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

package org.netbeans.modules.debugger.ui.actions;

import org.netbeans.api.project.Project;
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

    static void logDebugProject(Project project) {
        List<String> params = new ArrayList<>();
        if (project != null) {
            params.add(0, project.getClass().getName());
        }
        log("USG_PROJECT_DEBUG", params); // NOI18N
    }

    static void logAttach(String attachTypeName) {
        List<String> params = new ArrayList<>();
        params.add(attachTypeName);
        log("USG_DEBUG_ATTACH", params); // NOI18N
    }

    private static void log(String type, List<String> params) {
        LogRecord record = new LogRecord(Level.INFO, type);
        record.setResourceBundle(NbBundle.getBundle(GestureSubmitter.class));
        record.setResourceBundleName(GestureSubmitter.class.getPackage().getName() + ".Bundle"); // NOI18N
        record.setLoggerName(USG_LOGGER.getName());

        record.setParameters(params.toArray(new Object[0]));

        USG_LOGGER.log(record);
    }
}
