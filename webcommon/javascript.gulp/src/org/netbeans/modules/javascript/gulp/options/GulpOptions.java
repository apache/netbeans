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
package org.netbeans.modules.javascript.gulp.options;

import java.util.List;
import java.util.prefs.Preferences;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.modules.javascript.gulp.exec.GulpExecutable;
import org.netbeans.modules.javascript.gulp.util.FileUtils;
import org.openide.util.NbPreferences;

public final class GulpOptions {

    private static final String GULP_PATH = "gulp.path"; // NOI18N

    // Do not change arbitrary - consult with layer's folder OptionsExport
    // Path to Preferences node for storing these preferences
    private static final String PREFERENCES_PATH = "gulp"; // NOI18N

    private static final GulpOptions INSTANCE = new GulpOptions();

    private final Preferences preferences;

    private volatile boolean gulpSearched = false;


    private GulpOptions() {
        preferences = NbPreferences.forModule(GulpOptions.class).node(PREFERENCES_PATH);
    }

    public static GulpOptions getInstance() {
        return INSTANCE;
    }

    @CheckForNull
    public String getGulp() {
        String path = preferences.get(GULP_PATH, null);
        if (path == null
                && !gulpSearched) {
            gulpSearched = true;
            List<String> files = FileUtils.findFileOnUsersPath(GulpExecutable.GULP_NAME);
            if (!files.isEmpty()) {
                path = files.get(0);
                setGulp(path);
            }
        }
        return path;
    }

    public void setGulp(String gulp) {
        preferences.put(GULP_PATH, gulp);
    }

}
