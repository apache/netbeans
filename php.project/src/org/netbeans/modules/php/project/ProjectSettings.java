/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.php.project;

import java.util.List;
import java.util.prefs.Preferences;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.php.api.util.StringUtils;

/**
 * Helper class to get miscellaneous properties related to single PHP project
 * (like timestamp when a project has been uploaded last time etc.).
 * @author Tomas Mysik
 */
public final class ProjectSettings {
    // Do not change arbitrary - consult with layer's folder OptionsExport
    // Path to Preferences node for storing these preferences
    private static final String PROJECT_PREFERENCES_PATH = "projects"; // NOI18N

    private static final String LAST_UPLOAD = "lastUpload"; // NOI18N
    private static final String LAST_DONWLOAD = "lastDownload"; // NOI18N
    private static final String DEBUG_URLS = "debugUrls"; // NOI18N
    private static final String DEBUG_URLS_DELIMITER = "??NB??"; // NOI18N
    private static final int DEBUG_URLS_LIMIT = 10;
    // remote synchronization
    private static final String SYNC_TIMESTAMP = "sync.%s"; // NOI18N


    private ProjectSettings() {
    }

    private static Preferences getPreferences(Project project) {
        return PhpPreferences.getPreferences(false).node(PROJECT_PREFERENCES_PATH).
                node(ProjectUtils.getInformation(project).getName());
    }

    /**
     * @return timestamp <b>in seconds</b> of the last upload of a project or <code>-1</code> if not found.
     */
    public static long getLastUpload(Project project) {
        return getPreferences(project).getLong(LAST_UPLOAD, -1);
    }

    public static void setLastUpload(Project project, long timestamp) {
        getPreferences(project).putLong(LAST_UPLOAD, timestamp);
    }

    public static void resetLastUpload(Project project) {
        setLastUpload(project, -1);
    }

    /**
     * @return timestamp <b>in seconds</b> of the last download of a project or <code>-1</code> if not found.
     */
    public static long getLastDownload(Project project) {
        return getPreferences(project).getLong(LAST_DONWLOAD, -1);
    }

    public static void setLastDownload(Project project, long timestamp) {
        getPreferences(project).putLong(LAST_DONWLOAD, timestamp);
    }

    public static void resetLastDownload(Project project) {
        setLastDownload(project, -1);
    }

    public static List<String> getDebugUrls(Project project) {
        return StringUtils.explode(getPreferences(project).get(DEBUG_URLS, null), DEBUG_URLS_DELIMITER);
    }

    public static void setDebugUrls(Project project, List<String> debugUrls) {
        if (debugUrls.size() > DEBUG_URLS_LIMIT) {
            debugUrls = debugUrls.subList(0, DEBUG_URLS_LIMIT);
        }
        getPreferences(project).put(DEBUG_URLS, StringUtils.implode(debugUrls, DEBUG_URLS_DELIMITER));
    }

    public static long getSyncTimestamp(Project project, String key) {
        return getPreferences(project).getLong(String.format(SYNC_TIMESTAMP, key), -1);
    }

    public static void setSyncTimestamp(Project project, String key, long value) {
        getPreferences(project).putLong(String.format(SYNC_TIMESTAMP, key), value);
    }

}
