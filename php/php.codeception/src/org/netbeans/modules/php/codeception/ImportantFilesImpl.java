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
