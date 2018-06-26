/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
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
