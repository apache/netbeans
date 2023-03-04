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
package org.netbeans.modules.javafx2.project;

import java.util.prefs.Preferences;
import org.openide.util.NbPreferences;

/**
 * Storage of information application to the new JavaFX Wizard.
 */
public class WizardSettings {

    private static final String NEW_PROJECT_COUNT = "newProjectCount"; // NOI18N
    private static final String NEW_APP_COUNT = "newApplicationCount";  // NOI18N
    private static final String NEW_PRELOADER_COUNT = "newPreloaderCount";  // NOI18N
    private static final String NEW_SWING_COUNT = "newFxSwingAppCount";  // NOI18N
    private static final String NEW_LIB_COUNT = "newLibraryCount"; // NOI18N

    private WizardSettings() {
    }

    private static Preferences getPreferences() {
        return NbPreferences.forModule(WizardSettings.class);
    }

    public static int getNewProjectCount() {
        return getPreferences().getInt(NEW_PROJECT_COUNT, 0);
    }

    public static void setNewProjectCount(int count) {
        getPreferences().putInt(NEW_PROJECT_COUNT, count);
    }

    public static int getNewApplicationCount() {
        return getPreferences().getInt(NEW_APP_COUNT, 0);
    }

    public static void setNewApplicationCount(int count) {
        getPreferences().putInt(NEW_APP_COUNT, count);
    }

    public static int getNewLibraryCount() {
        return getPreferences().getInt(NEW_LIB_COUNT, 0);
    }

    public static void setNewLibraryCount(int count) {
        getPreferences().putInt(NEW_LIB_COUNT, count);
    }

    public static int getNewPreloaderCount() {
        return getPreferences().getInt(NEW_PRELOADER_COUNT, 0);
    }

    public static void setNewPreloaderCount(int count) {
        getPreferences().putInt(NEW_PRELOADER_COUNT, count);
    }

    public static int getNewFxSwingCount() {
        return getPreferences().getInt(NEW_SWING_COUNT, 0);
    }

    public static void setNewFxSwingCount(int count) {
        getPreferences().putInt(NEW_SWING_COUNT, count);
    }
}
