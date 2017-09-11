/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
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
