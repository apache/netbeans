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
package org.netbeans.modules.javascript.jstestdriver;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.modules.javascript.jstestdriver.preferences.JsTestDriverPreferences;
import org.netbeans.modules.javascript.jstestdriver.preferences.JsTestDriverPreferencesValidator;
import org.netbeans.modules.web.common.api.ValidationResult;
import org.netbeans.modules.web.common.spi.ImportantFilesImplementation;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.ChangeSupport;
import org.openide.util.WeakListeners;

public final class ImportantFilesImpl implements ImportantFilesImplementation, PreferenceChangeListener {

    private final Project project;
    private final ChangeSupport changeSupport = new ChangeSupport(this);


    private ImportantFilesImpl(Project project) {
        assert project != null;
        this.project = project;
    }

    @ProjectServiceProvider(service = ImportantFilesImplementation.class, projectType = "org-netbeans-modules-web-clientproject") // NOI18N
    public static ImportantFilesImplementation createImportantFiles(Project project) {
        ImportantFilesImpl importantFiles = new ImportantFilesImpl(project);
        JsTestDriverPreferences.addPreferenceChangeListener(project, WeakListeners.create(PreferenceChangeListener.class, importantFiles, JsTestDriverPreferences.class));
        return importantFiles;
    }

    @Override
    public Collection<ImportantFilesImplementation.FileInfo> getFiles() {
        String config = JsTestDriverPreferences.getConfig(project);
        ValidationResult result = new JsTestDriverPreferencesValidator()
                .validateConfig(config)
                .getResult();
        if (!result.isFaultless()) {
            return Collections.emptyList();
        }
        FileObject file = FileUtil.toFileObject(new File(config));
        assert file != null : config;
        return Collections.singleton(new ImportantFilesImplementation.FileInfo(file));
    }

    @Override
    public void addChangeListener(ChangeListener listener) {
        changeSupport.addChangeListener(listener);
    }

    @Override
    public void removeChangeListener(ChangeListener listener) {
        changeSupport.removeChangeListener(listener);
    }

    @Override
    public void preferenceChange(PreferenceChangeEvent evt) {
        if (JsTestDriverPreferences.CONFIG.equals(evt.getKey())) {
            changeSupport.fireChange();
        }
    }

}
