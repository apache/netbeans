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
package org.netbeans.modules.selenium2.webclient.mocha.preferences;

import java.util.prefs.Preferences;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Theofanis Oikonomou
 */
public final class MochaJSPreferences {

    private static final String ENABLED = "js.enabled"; // NOI18N
    private static final String MOCHA_DIR = "js.mocha.dir"; // NOI18N
    private static final String TIMEOUT = "js.timeout"; // NOI18N
    private static final int TIMEOUT_DEFAULT = 10000; // 10 seconds
    public static final String AUTO_WATCH = "js.auto.watch"; // NOI18N 

    private MochaJSPreferences() {
    }

    public static boolean isEnabled(Project project) {
        return getPreferences(project).getBoolean(ENABLED, false);
    }

    public static void setEnabled(Project project, boolean enabled) {
        getPreferences(project).putBoolean(ENABLED, enabled);
    }

    @CheckForNull
    public static String getMochaDir(Project project) {
        return resolvePath(project, getPreferences(project).get(MOCHA_DIR, null));
    }

    public static void setMochaDir(Project project, String installDir) {
        getPreferences(project).put(MOCHA_DIR, installDir);
    }
    
    public static int getTimeout(Project project) {
        return getPreferences(project).getInt(TIMEOUT, TIMEOUT_DEFAULT);
    }

    public static void setTimeout(Project project, int timeout) {
        getPreferences(project).putInt(TIMEOUT, timeout);
    }

    public static boolean isAutoWatch(Project project) {
        return getPreferences(project).getBoolean(AUTO_WATCH, false);
    }

    public static void setAutoWatch(Project project, boolean autoWatch) {
        getPreferences(project).putBoolean(AUTO_WATCH, autoWatch);
    }

    private static String resolvePath(Project project, String filePath) {
        if (filePath == null
                || filePath.trim().isEmpty()) {
            return null;
        }
        return PropertyUtils.resolveFile(FileUtil.toFile(project.getProjectDirectory()), filePath).getAbsolutePath();
    }

    private static Preferences getPreferences(Project project) {
        return ProjectUtils.getPreferences(project, MochaJSPreferences.class, false);
    }
    
}
