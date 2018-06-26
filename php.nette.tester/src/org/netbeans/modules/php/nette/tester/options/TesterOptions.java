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
package org.netbeans.modules.php.nette.tester.options;

import java.util.List;
import java.util.prefs.Preferences;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.modules.php.api.util.FileUtils;
import org.netbeans.modules.php.nette.tester.commands.Tester;
import org.openide.util.NbPreferences;

public final class TesterOptions {

    // Do not change arbitrary - consult with layer's folder OptionsExport
    // Path to Preferences node for storing these preferences
    private static final String PREFERENCES_PATH = "nette-tester"; // NOI18N

    private static final TesterOptions INSTANCE = new TesterOptions();

    // path
    private static final String TESTER_PATH = "tester.path"; // NOI18N
    private static final String BINARY_EXECUTABLE = "binary.executable"; // NOI18N
    private static final String PHP_INI_PATH = "php.ini.path"; // NOI18N

    private volatile boolean testerSearched = false;


    private TesterOptions() {
    }

    public static TesterOptions getInstance() {
        return INSTANCE;
    }

    @CheckForNull
    public String getTesterPath() {
        String path = getPreferences().get(TESTER_PATH, null);
        if (path == null && !testerSearched) {
            testerSearched = true;
            List<String> scripts = FileUtils.findFileOnUsersPath(Tester.TESTER_FILE_NAME);
            if (!scripts.isEmpty()) {
                path = scripts.get(0);
                setTesterPath(path);
            }
        }
        return path;
    }

    public void setTesterPath(String path) {
        getPreferences().put(TESTER_PATH, path);
    }

    @CheckForNull
    public String getPhpIniPath() {
        return getPreferences().get(PHP_INI_PATH, null);
    }

    public void setPhpIniPath(String path) {
        getPreferences().put(PHP_INI_PATH, path);
    }

    @CheckForNull
    public String getBinaryExecutable() {
        return getPreferences().get(BINARY_EXECUTABLE, null);
    }

    public void setBinaryExecutable(@NullAllowed String binaryExecutable) {
        if (binaryExecutable == null) {
            getPreferences().remove(BINARY_EXECUTABLE);
        } else {
            getPreferences().put(BINARY_EXECUTABLE, binaryExecutable);
        }
    }

    private Preferences getPreferences() {
        return NbPreferences.forModule(TesterOptions.class).node(PREFERENCES_PATH);
    }

}
