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

package org.netbeans.modules.maven.j2ee.ui.util;

import java.util.prefs.Preferences;
import org.openide.util.NbPreferences;

/**
 *
 * @author Martin Janicek
 */
public final class WarningPanelSupport {

    private static final String JAVA_EE_VERSION_CHANGE = "showJavaEEVersionChangeWarning"; // NOI18N
    private static final String AUTOMATIC_BUILD = "automaticBuildWarning";                 // NOI18N

    private WarningPanelSupport() {
    }

    private static Preferences getPreferences() {
        return NbPreferences.root().node("org/netbeans/modules/maven/showQuestions"); //NOI18N
    }

    public static boolean isJavaEEChangeWarningActivated() {
        return getPreferences().getBoolean(JAVA_EE_VERSION_CHANGE, true);
    }

    public static void dontShowJavaEEChangeWarning() {
        getPreferences().putBoolean(JAVA_EE_VERSION_CHANGE, false);
    }

    public static boolean isAutomaticBuildWarningActivated() {
        return getPreferences().getBoolean(AUTOMATIC_BUILD, true);
    }

    public static void dontShowAutomaticBuildWarning() {
        getPreferences().putBoolean(AUTOMATIC_BUILD, false);
    }
}
