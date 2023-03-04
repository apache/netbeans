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

package org.netbeans.modules.java.j2semodule.ui.wizards;

import java.util.prefs.Preferences;
import org.openide.util.NbPreferences;

/**
 * Storage of information application to the new j2semodule wizard.
 */
public class WizardSettings {

    private WizardSettings() {}
    
    private static final String NEW_MODULE_PROJECT_COUNT = "newModuleProjectCount"; //NOI18N


    private static Preferences getPreferences() {
        return NbPreferences.forModule(WizardSettings.class);
    }

    public static int getNewProjectCount() {
        return getPreferences().getInt(NEW_MODULE_PROJECT_COUNT, 0);
    }

    public static void setNewProjectCount(int count) {
        getPreferences().putInt(NEW_MODULE_PROJECT_COUNT, count);
    }

}
