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
package org.netbeans.modules.php.symfony2.options;

import java.util.List;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.modules.php.api.util.FileUtils;
import org.netbeans.modules.php.symfony2.commands.InstallerExecutable;
import org.openide.util.ChangeSupport;
import org.openide.util.NbPreferences;

/**
 * Symfony 2/3 options.
 */
public final class SymfonyOptions {

    // Do not change arbitrary - consult with layer's folder OptionsExport
    // Path to Preferences node for storing these preferences
    private static final String PREFERENCES_PATH = "symfony2"; // NOI18N

    private static final SymfonyOptions INSTANCE = new SymfonyOptions();

    // properties
    static final String INSTALLER = "installer"; // NOI18N
    static final String SANDBOX = "sandbox"; // NOI18N
    private static final String NEW_PROJECT_METHOD = "new.project.method"; // NOI18N
    private static final String IGNORE_CACHE = "ignore.cache"; // NOI18N

    final ChangeSupport changeSupport = new ChangeSupport(this);

    private volatile boolean installerSearched = false;


    private SymfonyOptions() {
        getPreferences().addPreferenceChangeListener(new PreferenceChangeListener() {
            @Override
            public void preferenceChange(PreferenceChangeEvent evt) {
                changeSupport.fireChange();
            }
        });
    }

    public static SymfonyOptions getInstance() {
        return INSTANCE;
    }

    public void addChangeListener(ChangeListener listener) {
        changeSupport.addChangeListener(listener);
    }

    public void removeChangeListener(ChangeListener listener) {
        changeSupport.removeChangeListener(listener);
    }

    @CheckForNull
    public String getInstaller() {
        String path = getPreferences().get(INSTALLER, null);
        if (path == null
                && !installerSearched) {
            installerSearched = true;
            List<String> files = FileUtils.findFileOnUsersPath(InstallerExecutable.NAME);
            if (!files.isEmpty()) {
                path = files.get(0);
                setInstaller(path);
            }
        }
        return path;
    }

    public void setInstaller(String installer) {
        getPreferences().put(INSTALLER, installer);
    }

    public String getSandbox() {
        return getPreferences().get(SANDBOX, null);
    }

    public void setSandbox(String sandbox) {
        getPreferences().put(SANDBOX, sandbox);
    }

    public boolean isUseInstaller() {
        return getPreferences().get(NEW_PROJECT_METHOD, INSTALLER).equals(INSTALLER);
    }

    public void setUseInstaller(boolean useInstaller) {
        getPreferences().put(NEW_PROJECT_METHOD, useInstaller ? INSTALLER : SANDBOX);
    }

    public boolean getIgnoreCache() {
        return getPreferences().getBoolean(IGNORE_CACHE, true);
    }

    public void setIgnoreCache(boolean ignoreCache) {
        getPreferences().putBoolean(IGNORE_CACHE, ignoreCache);
    }

    private Preferences getPreferences() {
        return NbPreferences.forModule(SymfonyOptions.class).node(PREFERENCES_PATH);
    }

}
