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

package org.netbeans.modules.javascript.jstestdriver.preferences;

import java.io.File;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.filesystems.FileUtil;

/**
 * Project specific js-test-driver preferences.
 */
public final class JsTestDriverPreferences {

    public static final String CONFIG = "config"; // NOI18N

    private static final String ENABLED = "enabled"; // NOI18N

    // @GuardedBy("CACHE")
    private static final Map<Project, Preferences> CACHE = new WeakHashMap<>();


    private JsTestDriverPreferences() {
    }

    public static boolean isEnabled(Project project) {
        return getPreferences(project).getBoolean(ENABLED, false);
    }

    public static void setEnabled(Project project, boolean enabled) {
        getPreferences(project).putBoolean(ENABLED, enabled);
    }

    @CheckForNull
    public static String getConfig(Project project) {
        return resolvePath(project, getPreferences(project).get(CONFIG, null));
    }

    public static void setConfig(Project project, String config) {
        getPreferences(project).put(CONFIG, relativizePath(project, config));
    }

    public static void addPreferenceChangeListener(Project project, PreferenceChangeListener listener) {
        getPreferences(project).addPreferenceChangeListener(listener);
    }

    public static void removePreferenceChangeListener(Project project, PreferenceChangeListener listener) {
        getPreferences(project).removePreferenceChangeListener(listener);
    }

    private static String relativizePath(Project project, String filePath) {
        if (filePath == null
                || filePath.trim().isEmpty()) {
            return ""; // NOI18N
        }
        File file = new File(filePath);
        String path = PropertyUtils.relativizeFile(FileUtil.toFile(project.getProjectDirectory()), file);
        if (path == null
                || path.startsWith("../")) { // NOI18N
            // cannot be relativized or outside project
            path = file.getAbsolutePath();
        }
        return path;
    }

    private static String resolvePath(Project project, String filePath) {
        if (filePath == null
                || filePath.trim().isEmpty()) {
            return null;
        }
        return PropertyUtils.resolveFile(FileUtil.toFile(project.getProjectDirectory()), filePath).getAbsolutePath();
    }

    private static Preferences getPreferences(Project project) {
        assert project != null;
        synchronized (CACHE) {
            Preferences preferences = CACHE.get(project);
            if (preferences == null) {
                preferences = ProjectUtils.getPreferences(project, JsTestDriverPreferences.class, false);
                CACHE.put(project, preferences);
            }
            assert preferences != null : project.getProjectDirectory();
            return preferences;
        }
    }

}
