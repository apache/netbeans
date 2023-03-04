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
package org.netbeans.modules.php.codeception.options;

import java.util.List;
import java.util.prefs.Preferences;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.modules.php.api.util.FileUtils;
import org.netbeans.modules.php.codeception.commands.Codecept;
import org.openide.util.NbPreferences;

public final class CodeceptionOptions {

    private static final String PREFERENCES_PATH = "codeception"; // NOI18N
    private static final CodeceptionOptions INSTANCE = new CodeceptionOptions();
    private static final String CODECEPTION_PATH = "codeception.path"; // NOI18N

    private volatile boolean codeceptionSearched = false;


    public static CodeceptionOptions getInstance() {
        return INSTANCE;
    }

    @CheckForNull
    public String getCodeceptionPath() {
        String codeceptionPath = getPreferences().get(CODECEPTION_PATH, null);
        if (codeceptionPath == null && !codeceptionSearched) {
            List<String> scripts = FileUtils.findFileOnUsersPath(Codecept.SCRIPT_NAME, Codecept.SCRIPT_NAME_LONG, Codecept.SCRIPT_NAME_PHAR);
            if (!scripts.isEmpty()) {
                codeceptionPath = scripts.get(0);
                setCodeceptionPath(codeceptionPath);
            }
        }
        return codeceptionPath;
    }

    public void setCodeceptionPath(String codeceptionPath) {
        getPreferences().put(CODECEPTION_PATH, codeceptionPath);
    }

    private Preferences getPreferences() {
        return NbPreferences.forModule(CodeceptionOptions.class).node(PREFERENCES_PATH);
    }

}
