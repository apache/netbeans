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
package org.netbeans.modules.php.phing.options;

import java.util.List;
import java.util.prefs.Preferences;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.modules.php.api.util.FileUtils;
import org.netbeans.modules.php.phing.exec.PhingExecutable;
import org.openide.util.NbPreferences;

public final class PhingOptions {

    private static final String PHING_PATH = "phing.path"; // NOI18N

    // Do not change arbitrary - consult with layer's folder OptionsExport
    // Path to Preferences node for storing these preferences
    private static final String PREFERENCES_PATH = "phing"; // NOI18N

    private static final PhingOptions INSTANCE = new PhingOptions();

    private final Preferences preferences;

    private volatile boolean phingSearched = false;


    private PhingOptions() {
        preferences = NbPreferences.forModule(PhingOptions.class).node(PREFERENCES_PATH);
    }

    public static PhingOptions getInstance() {
        return INSTANCE;
    }

    @CheckForNull
    public String getPhing() {
        String path = preferences.get(PHING_PATH, null);
        if (path == null
                && !phingSearched) {
            phingSearched = true;
            List<String> files = FileUtils.findFileOnUsersPath(PhingExecutable.PHING_NAMES);
            if (!files.isEmpty()) {
                path = files.get(0);
                setPhing(path);
            }
        }
        return path;
    }

    public void setPhing(String phing) {
        preferences.put(PHING_PATH, phing);
    }

}
