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
package org.netbeans.spi.editor.hints.projects.support;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import javax.swing.JComponent;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.editor.tools.storage.api.ToolPreferences;
import org.netbeans.spi.editor.hints.projects.ProjectSettings;
import org.netbeans.spi.editor.hints.projects.support.StandardProjectSettings.Standard;
import org.netbeans.spi.project.ui.support.ProjectCustomizer.Category;
import org.netbeans.spi.project.ui.support.ProjectCustomizer.CompositeCategoryProvider;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;

/**
 *
 * @author lahvac
 */
@Messages("CAP_Hints=Hints")
class ProjectCustomizer implements CompositeCategoryProvider {
    
    private final String customizersFolder;

    public ProjectCustomizer(String customizersFolder) {
        this.customizersFolder = customizersFolder;
    }
    
    @Override
    public Category createCategory(Lookup context) {
        Project prj = context.lookup(Project.class);
        
        if (prj == null) return null;
        
        Standard settings = findStandardSettings(prj.getLookup());
        
        if (settings == null) return null;
        
        return Category.create("editor.hints", Bundle.CAP_Hints(), null);
    }

    @Override
    public JComponent createComponent(Category category, Lookup context) {
        final Project prj = context.lookup(Project.class);
        
        assert prj != null;
        
        final ProjectHintSettingPanel settingsPanel = new ProjectHintSettingPanel(findStandardSettings(prj.getLookup()), customizersFolder);
        
        final ToolPreferences[] toSave = new ToolPreferences[1];
        
        category.setOkButtonListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                toSave[0] = settingsPanel.commit();
            }
        });
        
        category.setStoreListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                try {
                    if (toSave[0] != null) {
                        toSave[0].save();
                        toSave[0] = null;
                    }
                    
                    ProjectManager.getDefault().saveProject(prj);
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        });
        
        return settingsPanel;
    }
    
    private static Standard findStandardSettings(Lookup where) {
        ProjectSettings settings = where.lookup(ProjectSettings.class);
        
        return settings instanceof Standard ? (Standard) settings : null;
    }
    
}
