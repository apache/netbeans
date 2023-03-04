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
package org.netbeans.modules.php.atoum.options;

import java.util.List;
import java.util.prefs.Preferences;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.modules.php.api.util.FileUtils;
import org.netbeans.modules.php.atoum.commands.Atoum;
import org.openide.util.NbPreferences;

public final class AtoumOptions {

    // Do not change arbitrary - consult with layer's folder OptionsExport
    // Path to Preferences node for storing these preferences
    private static final String PREFERENCES_PATH = "atoum"; // NOI18N

    private static final AtoumOptions INSTANCE = new AtoumOptions();

    // path
    private static final String ATOUM_PATH = "atoum.path"; // NOI18N

    private volatile boolean atoumSearched = false;


    private AtoumOptions() {
    }

    public static AtoumOptions getInstance() {
        return INSTANCE;
    }

    @CheckForNull
    public String getAtoumPath() {
        String path = getPreferences().get(ATOUM_PATH, null);
        if (path == null && !atoumSearched) {
            atoumSearched = true;
            List<String> scripts = FileUtils.findFileOnUsersPath(Atoum.PHAR_FILE_NAME, Atoum.ATOUM_FILE_NAME);
            if (!scripts.isEmpty()) {
                path = scripts.get(0);
                setAtoumPath(path);
            }
        }
        return path;
    }

    public void setAtoumPath(String path) {
        getPreferences().put(ATOUM_PATH, path);
    }

    private Preferences getPreferences() {
        return NbPreferences.forModule(AtoumOptions.class).node(PREFERENCES_PATH);
    }

}
