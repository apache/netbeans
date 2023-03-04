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
package org.netbeans.modules.editor.hints.projects.settings;

import java.awt.Toolkit;
import java.util.prefs.Preferences;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.editor.hints.settings.friend.FileHintPreferencesProvider;
import org.netbeans.spi.editor.hints.projects.ProjectSettings;
import org.netbeans.spi.project.ui.CustomizerProvider;
import org.netbeans.spi.project.ui.CustomizerProvider2;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.openide.filesystems.FileObject;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author lahvac
 */
@ServiceProvider(service=FileHintPreferencesProvider.class)
public class FileHintPreferencesProviderImpl implements FileHintPreferencesProvider {

    @Override
    public Preferences getFilePreferences(FileObject file, String mimeType) {
        Project prj = FileOwnerQuery.getOwner(file);
        
        if (prj == null) return null;
        
        ProjectSettings settings = prj.getLookup().lookup(ProjectSettings.class);
        
        if (settings == null || !settings.getUseProjectSettings()) return null;
        
        return settings.getProjectSettings(mimeType);
    }

    @Override
    public boolean openFilePreferences(FileObject file, String mimeType, String hintId) {
        Project prj = FileOwnerQuery.getOwner(file);

        if (prj == null) return false;

        ProjectSettings settings = prj.getLookup().lookup(ProjectSettings.class);

        if (settings == null || !settings.getUseProjectSettings()) return false;

        CustomizerProvider2 customizer2 = prj.getLookup().lookup(CustomizerProvider2.class);

        if (customizer2 != null) {
            customizer2.showCustomizer("editor.hints", null);
        } else {
            CustomizerProvider customizer = prj.getLookup().lookup(CustomizerProvider.class);

            if (customizer != null) {
                customizer.showCustomizer();
            } else {
                Toolkit.getDefaultToolkit().beep();
            }
        }

        return true;
    }

}
