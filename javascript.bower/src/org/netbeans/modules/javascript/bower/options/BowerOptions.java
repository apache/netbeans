/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2016 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2016 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javascript.bower.options;

import java.util.List;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.modules.javascript.bower.exec.BowerExecutable;
import org.netbeans.modules.javascript.bower.util.FileUtils;
import org.openide.util.NbPreferences;

public final class BowerOptions {

    public static final String BOWER_PATH = "bower.path"; // NOI18N
    public static final String IGNORE_BOWER_COMPONENTS = "ignore.bower_components"; // NOI18N

    // Do not change arbitrary - consult with layer's folder OptionsExport
    // Path to Preferences node for storing these preferences
    private static final String PREFERENCES_PATH = "bower"; // NOI18N

    private static final BowerOptions INSTANCE = new BowerOptions();

    private final Preferences preferences;

    private volatile boolean bowerSearched = false;

    private BowerOptions() {
        preferences = NbPreferences.forModule(BowerOptions.class).node(PREFERENCES_PATH);
    }

    public static BowerOptions getInstance() {
        return INSTANCE;
    }

    public void addPreferenceChangeListener(PreferenceChangeListener listener) {
        preferences.addPreferenceChangeListener(listener);
    }

    public void removePreferenceChangeListener(PreferenceChangeListener listener) {
        preferences.removePreferenceChangeListener(listener);
    }

    @CheckForNull
    public String getBower() {
        String path = preferences.get(BOWER_PATH, null);
        if (path == null
                && !bowerSearched) {
            bowerSearched = true;
            List<String> files = FileUtils.findFileOnUsersPath(BowerExecutable.BOWER_NAME);
            if (!files.isEmpty()) {
                path = files.get(0);
                setBower(path);
            }
        }
        return path;
    }

    public void setBower(String bower) {
        preferences.put(BOWER_PATH, bower);
    }

    public boolean isIgnoreBowerComponents() {
        return preferences.getBoolean(IGNORE_BOWER_COMPONENTS, true);
    }

    public void setIgnoreBowerComponents(boolean ignoreBowerComponents) {
        preferences.putBoolean(IGNORE_BOWER_COMPONENTS, ignoreBowerComponents);
    }

}
