/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.codeception;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.codeception.commands.Codecept;
import org.netbeans.modules.php.codeception.preferences.CodeceptionPreferences;
import org.netbeans.modules.php.spi.phpmodule.ImportantFilesImplementation;
import org.netbeans.modules.php.spi.phpmodule.ImportantFilesSupport;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.filesystems.FileObject;
import org.openide.util.ChangeSupport;
import org.openide.util.WeakListeners;

@ProjectServiceProvider(service = ImportantFilesImplementation.class, projectType = "org-netbeans-modules-php-project") // NOI18N
public final class ImportantFilesImpl implements ImportantFilesImplementation, PreferenceChangeListener {

    private final Project project;
    private final ChangeSupport changeSupport = new ChangeSupport(this);
    private final ImportantFilesSupport defaultConfigSupport;

    // not perfect but should be fine, listener should not be added more times than once
    private volatile boolean initialized = false;


    public ImportantFilesImpl(Project project) {
        assert project != null;
        this.project = project;
        defaultConfigSupport = ImportantFilesSupport.create(project.getProjectDirectory(), Codecept.CODECEPTION_CONFIG_FILE_NAME, Codecept.CODECEPTION_DIST_CONFIG_FILE_NAME);
    }

    @Override
    public Collection<FileInfo> getFiles() {
        PhpModule phpModule = PhpModule.Factory.lookupPhpModule(project);
        if (phpModule == null) {
            return Collections.emptyList();
        }
        if (!initialized) {
            initialized = true;
            CodeceptionPreferences.addPreferenceChangeListener(phpModule, WeakListeners.create(PreferenceChangeListener.class, this, CodeceptionPreferences.class));
        }

        // global configuration
        List<FileInfo> files = new ArrayList<>();
        files.addAll(defaultConfigSupport.getFiles(null));
        if (CodeceptionPreferences.isCustomCodeceptionYmlEnabled(phpModule)) {
            List<FileObject> codeceptionYmls = Codecept.getCodeceptionYmls(phpModule);
            for (FileObject codeceptionYml : codeceptionYmls) {
                files.add(new FileInfo(codeceptionYml));
            }
        }
        return files;
    }

    @Override
    public void addChangeListener(ChangeListener listener) {
        changeSupport.addChangeListener(listener);
        defaultConfigSupport.addChangeListener(listener);
    }

    @Override
    public void removeChangeListener(ChangeListener listener) {
        changeSupport.removeChangeListener(listener);
        defaultConfigSupport.removeChangeListener(listener);
    }

    @Override
    public void preferenceChange(PreferenceChangeEvent evt) {
        if (CodeceptionPreferences.CUSTOM_CODECEPTION_YML_PATH.equals(evt.getKey())
                || CodeceptionPreferences.CUSTOM_CODECEPTION_YML_ENABLED.equals(evt.getKey())) {
            fireChange();
        }
    }

    void fireChange() {
        changeSupport.fireChange();
    }

}
