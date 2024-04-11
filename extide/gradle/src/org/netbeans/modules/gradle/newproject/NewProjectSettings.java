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
package org.netbeans.modules.gradle.newproject;

import java.util.prefs.Preferences;
import org.netbeans.modules.gradle.spi.newproject.GradleInitWizard;
import org.openide.util.NbPreferences;

/**
 *
 * @author lkishalmi
 */
public final class NewProjectSettings {

    public static final String PROP_PACKAGE_PREFIX = "packagePrefix";

    private static final NewProjectSettings INSTANCE = new NewProjectSettings(NbPreferences.forModule(NewProjectSettings.class));
    private final Preferences preferences;

    public static NewProjectSettings getDefault() {
        return INSTANCE;
    }

    NewProjectSettings(Preferences preferences) {
        this.preferences = preferences;
    }

    public Preferences getPreferences() {
        return preferences;
    }

    public void setPackagePrefix(String prefix) {
        getPreferences().put(PROP_PACKAGE_PREFIX, prefix);
    }

    public String getPackagePrefix() {
        return getPreferences().get(PROP_PACKAGE_PREFIX, "com.example."); //NOI18N
    }

    public void setGradleDSL(GradleInitWizard.GradleDSL dsl) {
        getPreferences().put(GradleInitWizard.PROP_DSL, dsl.name());
    }

    public GradleInitWizard.GradleDSL getGradleDSL() {
        String dsl = getPreferences().get(GradleInitWizard.PROP_DSL, GradleInitWizard.GradleDSL.GROOVY.name());
        try {
            return GradleInitWizard.GradleDSL.valueOf(dsl);
        } catch (IllegalArgumentException ex) {
        }
        return GradleInitWizard.GradleDSL.GROOVY;
    }

    public void setGenerateComments(boolean b) {
        getPreferences().putBoolean(GradleInitWizard.PROP_COMMENTS, b);
    }

    public boolean getGenerateComments() {
        return getPreferences().getBoolean(GradleInitWizard.PROP_COMMENTS, true);
    }
}
