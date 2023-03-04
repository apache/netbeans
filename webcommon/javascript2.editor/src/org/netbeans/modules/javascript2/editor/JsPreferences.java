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
package org.netbeans.modules.javascript2.editor;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.prefs.Preferences;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;


public class JsPreferences {

    private static final JsVersion DEFAULT_JS_VERSION = JsVersion.EMCANEXT;

    private static final String JS_PREF_TAG = "jsversion"; // NOI18N

    public static List<JsVersion> getECMAScriptAvailableVersions() {
        return new ArrayList<>(EnumSet.allOf(JsVersion.class));
    }

    public static JsVersion getECMAScriptVersion(Project project) {
        if (project != null) {
            String strValue = getPreferences(project).get(JS_PREF_TAG, null);
            JsVersion version = JsVersion.fromString(strValue);
            if (version == null) {
                version = DEFAULT_JS_VERSION;
            }
            return version;
        }
        return DEFAULT_JS_VERSION;
    }

    public static void putECMAScriptVersion(Project project, JsVersion version) {
        if (project != null) {
            if (!version.equals(DEFAULT_JS_VERSION)) {
                getPreferences(project).put(JS_PREF_TAG, version.toString());
            } else {
                getPreferences(project).remove(JS_PREF_TAG);
            }
        }
    }

    private static Preferences getPreferences(Project project) {
        return ProjectUtils.getPreferences(project, JsPreferences.class, true);
    }

    public static boolean isPreECMAVersion(Project project, JsVersion target) {
        return getECMAScriptVersion(project).ordinal() < target.ordinal();
    }
}
