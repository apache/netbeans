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
package org.netbeans.modules.css.prep.preferences;

import java.util.List;
import java.util.prefs.Preferences;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.css.prep.CssPreprocessorType;
import org.netbeans.modules.css.prep.util.CssPreprocessorUtils;
import org.openide.util.Pair;

abstract class BasePreferences {

    BasePreferences() {
    }

    protected boolean isConfigured(Project project, String propertyName) {
        return getPreferences(project).getBoolean(propertyName, false);
    }

    protected void setConfigured(Project project, String propertyName, boolean configured) {
        getPreferences(project).putBoolean(propertyName, configured);
    }

    protected boolean isEnabled(Project project, String propertyName) {
        // first, try to find property in private.properties
        String privateEnabled = getPrivatePreferences(project).get(propertyName, null);
        if (privateEnabled != null) {
            return Boolean.parseBoolean(privateEnabled);
        }
        // get property from public project.properties
        return getPreferences(project).getBoolean(propertyName, false);
    }

    protected void setEnabled(Project project, String propertyName, boolean enabled) {
        // delete property from private.properties
        getPrivatePreferences(project).remove(propertyName);
        // set property in public project.properties
        getPreferences(project).putBoolean(propertyName, enabled);
    }

    @CheckForNull
    protected List<Pair<String, String>> getMappings(Project project, String propertyName, CssPreprocessorType type) {
        String mappings = getPreferences(project).get(propertyName, null);
        if (mappings == null) {
            return CssPreprocessorUtils.getDefaultMappings(type);
        }
        return CssPreprocessorUtils.decodeMappings(mappings);
    }

    protected void setMappings(Project project, String propertyName, List<Pair<String, String>> mappings) {
        getPreferences(project).put(propertyName, CssPreprocessorUtils.encodeMappings(mappings));
    }

    protected String getCompilerOptions(Project project, String propertyName) {
        return getPreferences(project).get(propertyName, ""); // NOI18N
    }

    protected void setCompilerOptions(Project project, String propertyName, String compilerOptions) {
        getPreferences(project).put(propertyName, compilerOptions);
    }

    protected Preferences getPreferences(Project project) {
        assert project != null;
        return ProjectUtils.getPreferences(project, BasePreferences.class, true);
    }

    protected Preferences getPrivatePreferences(Project project) {
        assert project != null;
        return ProjectUtils.getPreferences(project, BasePreferences.class, false);
    }

}
