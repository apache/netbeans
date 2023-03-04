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
import org.netbeans.api.project.Project;
import org.netbeans.modules.css.prep.CssPreprocessorType;
import org.openide.util.Pair;

/**
 * Sass preferences specific for project.
 */
public final class SassPreferences extends BasePreferences implements CssPreprocessorPreferences {

    private static final String CONFIGURED = "sass.configured"; // NOI18N
    private static final String ENABLED = "sass.enabled"; // NOI18N
    private static final String MAPPINGS = "sass.mappings"; // NOI18N
    private static final String COMPILER_OPTIONS = "sass.compiler.options"; // NOI18N

    private static final SassPreferences INSTANCE = new SassPreferences();


    private SassPreferences() {
    }

    public static SassPreferences getInstance() {
        return INSTANCE;
    }

    @Override
    public boolean isConfigured(Project project) {
        return isConfigured(project, CONFIGURED);
    }

    @Override
    public void setConfigured(Project project, boolean configured) {
        setConfigured(project, CONFIGURED, configured);
    }

    @Override
    public boolean isEnabled(Project project) {
        return isEnabled(project, ENABLED);
    }

    @Override
    public void setEnabled(Project project, boolean enabled) {
        setEnabled(project, ENABLED, enabled);
    }

    @Override
    public List<Pair<String, String>> getMappings(Project project) {
        return getMappings(project, MAPPINGS, CssPreprocessorType.SASS);
    }

    @Override
    public void setMappings(Project project, List<Pair<String, String>> mappings) {
        setMappings(project, MAPPINGS, mappings);
    }

    @Override
    public String getCompilerOptions(Project project) {
        return getCompilerOptions(project, COMPILER_OPTIONS);
    }

    @Override
    public void setCompilerOptions(Project project, String compilerOptions) {
        setCompilerOptions(project, COMPILER_OPTIONS, compilerOptions);
    }

}
