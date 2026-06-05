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
package org.netbeans.modules.kotlin.editor.lsp;

import java.io.File;
import java.util.prefs.Preferences;
import org.openide.modules.ModuleInfo;
import org.openide.modules.Places;
import org.openide.util.Lookup;
import org.openide.util.NbPreferences;
import org.openide.util.Utilities;

public class Settings {

    public static final String KOTLIN_LSP_MODULE_CNB = "org.netbeans.libs.kotlin.lsp";
    private static final String KEY_KOTLIN_LSP_PATH = "lsp-path";

    public static String getKotlinLSPPath() {
        String path = getRawKotlinLSPPath();

        if (path.isBlank()) {
            return getDefaultPath();
        } else {
            return path;
        }
    }

    public static String getRawKotlinLSPPath() {
        return settings().get(KEY_KOTLIN_LSP_PATH, "");
    }

    public static void setRawKotlinLSPPath(String path) {
        settings().put(KEY_KOTLIN_LSP_PATH, path);
    }

    public static Preferences settings() {
        return NbPreferences.forModule(Settings.class);
    }

    private static String getDefaultPath() {
        String version = null;

        for (ModuleInfo mi : Lookup.getDefault().lookupAll(ModuleInfo.class)) {
            if (KOTLIN_LSP_MODULE_CNB.equals(mi.getCodeNameBase())) {
                version = mi.getSpecificationVersion().toString();
            }
        }

        if (version == null) {
            return null;
        }

        File serverDir = Places.getCacheSubdirectory("kotlin-lsp/" + version);

        return new File(new File(serverDir, "bin"), "kotlin-language-server" + (Utilities.isWindows() ? ".bat" : "")).getAbsolutePath();
    }
}
