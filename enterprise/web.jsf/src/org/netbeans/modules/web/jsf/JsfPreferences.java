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

package org.netbeans.modules.web.jsf;

import java.util.prefs.Preferences;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.web.jsf.wizards.JSFConfigurationPanel.PreferredLanguage;

/**
 * Helper class to get the JSF's preferences.
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public final class JsfPreferences {

    private static final String PREFERRED_LANGUAGE = "jsf.language";        //NOI18N
    private static final String JSF_PRESENT_PROPERTY = "jsf.present";       //NOI18N

    private final Preferences preferences;

    private JsfPreferences(Preferences preferences) {
        this.preferences = preferences;
    }

    /**
     * Gets project related preferences.
     * @param project related project
     * @return preferences of the project
     */
    public static JsfPreferences forProject(Project project) {
        Preferences preferences = ProjectUtils.getPreferences(project, ProjectUtils.class, true);
        return new JsfPreferences(preferences);
    }

    /**
     * Gets information whether the JSF is present inside the project (based on project preferences).
     * @return {@code true} if JSF is present, {@code false} otherwise (if not sure or not present)
     */
    public boolean isJsfPresent() {
        return preferences.get(JSF_PRESENT_PROPERTY, "").equals("true"); //NOI18N
    }

    /**
     * Stores information that the JSF is present inside the project.
     * @param value value to store
     */
    public void setJsfPresent(boolean value) {
        String valueToStore = value ? "true" : "false"; //NOI18N
        preferences.put(JSF_PRESENT_PROPERTY, valueToStore);
    }

    /**
     * Gets preferred language of the project.
     * @return preferred language, {@code null} if the language was not set yet
     */
    public PreferredLanguage getPreferredLanguage() {
        String lang = preferences.get(PREFERRED_LANGUAGE, "");
        if (lang.isEmpty()) {
            return null;
        }

        for (PreferredLanguage preferredLanguage : PreferredLanguage.values()) {
            if (preferredLanguage.getName().equals(lang)) {
                return preferredLanguage;
            }
        }

        return null;
    }

    /**
     * Sets preferred language of the project.
     * @param preferredLanguage to be set or {@code null} if it should be removed from preferences
     */
    public void setPreferredLanguage(PreferredLanguage preferredLanguage) {
        if (preferredLanguage == null) {
            preferences.remove(PREFERRED_LANGUAGE);
        } else {
            preferences.put(PREFERRED_LANGUAGE, preferredLanguage.getName());
        }
    }

}
