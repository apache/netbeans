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
package org.netbeans.modules.javascript.bower.util;

import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.web.common.api.UsageLogger;

public final class BowerUtils {

    private static final String USAGE_LOGGER_NAME = "org.netbeans.ui.metrics.javascript.bower"; // NOI18N
    private static final UsageLogger BOWER_INSTALL_USAGE_LOGGER = new UsageLogger.Builder(USAGE_LOGGER_NAME)
            .message(BowerUtils.class, "USG_BOWER_INSTALL") // NOI18N
            .create();
    private static final UsageLogger BOWER_LIBRARY_USAGE_LOGGER = new UsageLogger.Builder(USAGE_LOGGER_NAME)
            .message(BowerUtils.class, "USG_BOWER_LIBRARY") // NOI18N
            .firstMessageOnly(false)
            .create();


    private BowerUtils() {
    }

    public static void logUsageBowerInstall() {
        BOWER_INSTALL_USAGE_LOGGER.log();
    }

    public static void logUsageBowerLibrary(String type, String name, String version) {
        BOWER_LIBRARY_USAGE_LOGGER.log(type, name, version);
    }

    public static String getProjectDisplayName(Project project) {
        return ProjectUtils.getInformation(project).getDisplayName();
    }

}
