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
package org.netbeans.modules.php.nette.tester.options;

import java.util.List;
import java.util.prefs.Preferences;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.modules.php.api.util.FileUtils;
import org.netbeans.modules.php.nette.tester.commands.Tester;
import org.openide.util.NbPreferences;

public final class TesterOptions {

    // Do not change arbitrary - consult with layer's folder OptionsExport
    // Path to Preferences node for storing these preferences
    private static final String PREFERENCES_PATH = "nette-tester"; // NOI18N

    private static final TesterOptions INSTANCE = new TesterOptions();

    // path
    private static final String TESTER_PATH = "tester.path"; // NOI18N
    private static final String BINARY_EXECUTABLE = "binary.executable"; // NOI18N
    private static final String PHP_INI_PATH = "php.ini.path"; // NOI18N

    private volatile boolean testerSearched = false;


    private TesterOptions() {
    }

    public static TesterOptions getInstance() {
        return INSTANCE;
    }

    @CheckForNull
    public String getTesterPath() {
        String path = getPreferences().get(TESTER_PATH, null);
        if (path == null && !testerSearched) {
            testerSearched = true;
            List<String> scripts = FileUtils.findFileOnUsersPath(Tester.TESTER_FILE_NAME);
            if (!scripts.isEmpty()) {
                path = scripts.get(0);
                setTesterPath(path);
            }
        }
        return path;
    }

    public void setTesterPath(String path) {
        getPreferences().put(TESTER_PATH, path);
    }

    @CheckForNull
    public String getPhpIniPath() {
        return getPreferences().get(PHP_INI_PATH, null);
    }

    public void setPhpIniPath(String path) {
        getPreferences().put(PHP_INI_PATH, path);
    }

    @CheckForNull
    public String getBinaryExecutable() {
        return getPreferences().get(BINARY_EXECUTABLE, null);
    }

    public void setBinaryExecutable(@NullAllowed String binaryExecutable) {
        if (binaryExecutable == null) {
            getPreferences().remove(BINARY_EXECUTABLE);
        } else {
            getPreferences().put(BINARY_EXECUTABLE, binaryExecutable);
        }
    }

    private Preferences getPreferences() {
        return NbPreferences.forModule(TesterOptions.class).node(PREFERENCES_PATH);
    }

}
