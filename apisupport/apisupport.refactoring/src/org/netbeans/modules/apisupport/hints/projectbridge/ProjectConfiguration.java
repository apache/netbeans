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

package org.netbeans.modules.apisupport.hints.projectbridge;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.prefs.Preferences;
import javax.swing.JComponent;
import javax.swing.JLabel;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.editor.tools.storage.api.ToolPreferences;
import org.netbeans.spi.editor.hints.projects.PerProjectHintsPanel;
import org.netbeans.spi.editor.hints.projects.ProjectSettings;
import static org.netbeans.spi.editor.hints.projects.ProjectSettings.HINTS_TOOL_ID;
import org.netbeans.spi.editor.hints.projects.support.StandardProjectSettings;
import org.netbeans.spi.editor.hints.settings.FileHintPreferences;
import org.netbeans.spi.project.LookupProvider;
import org.netbeans.spi.project.LookupProvider.Registration.ProjectType;
import org.netbeans.spi.project.ui.support.ProjectCustomizer.Category;
import org.netbeans.spi.project.ui.support.ProjectCustomizer.CompositeCategoryProvider;
import org.netbeans.spi.project.ui.support.ProjectCustomizer.CompositeCategoryProvider.Registration;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.EditableProperties;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.InstanceContent.Convertor;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author lahvac
 */
public class ProjectConfiguration {
    
    @LookupProvider.Registration(projectType = "org-netbeans-modules-apisupport-project")
    public static class ProjectSettingsProvider implements LookupProvider {

        @Override
        public Lookup createAdditionalLookup(Lookup baseContext) {
            Project prj = baseContext.lookup(Project.class);

            FileObject nbbuildMisc = prj.getProjectDirectory().getFileObject("../../nbbuild/misc");

            if (nbbuildMisc == null) {
                nbbuildMisc = prj.getProjectDirectory().getFileObject("../../../nbbuild/misc");
            }

            if (nbbuildMisc != null) {
                //nb.org:
                final FileObject nbbuildMiscFin = nbbuildMisc;

                return Lookups.fixed(new ProjectSettings() {
                    @Override public boolean getUseProjectSettings() {
                        return true;
                    }
                    @Override public Preferences getProjectSettings(String mimeType) {
                        URI settingsLocation = nbbuildMiscFin.toURI().resolve("hints-settings.xml");
                        return ToolPreferences.from(settingsLocation).getPreferences(HINTS_TOOL_ID, mimeType);
                    }
                });
            }
            
            InstanceContent ic = new InstanceContent();
            
            ic.add(prj, new Convertor<Project, ProjectSettings>() {
                @Override public ProjectSettings convert(Project prj) {
                    Project suiteProject = suiteProject(prj);

                    if (suiteProject != null && suiteProject != prj) {
                        System.err.println("suiteProject=" + suiteProject.getProjectDirectory());
                        ProjectSettings settings = suiteProject.getLookup().lookup(ProjectSettings.class);

                        return settings;
                    }

                    //standalone:
                    return StandardProjectSettings.createSettings(null, null, null).createAdditionalLookup(Lookups.fixed(prj)).lookup(ProjectSettings.class);
                }
                @Override public Class<? extends ProjectSettings> type(Project obj) {
                    return ProjectSettings.class;
                }
                @Override public String id(Project prj) {
                    return "prj";
                }
                @Override public String displayName(Project prj) {
                    return "Hint Project Settings";
                }
            });
            
            return new AbstractLookup(ic);
        }
        
    }
    
    @Messages("CAP_Hints=Hints")
    @Registration(projectType="org-netbeans-modules-apisupport-project", position=2000)
    public static class ProjectCustomizer implements CompositeCategoryProvider {

        @Override
        public Category createCategory(Lookup context) {
            Project prj = context.lookup(Project.class);

            if (prj == null) return null;

            return Category.create("editor.hints", Bundle.CAP_Hints(), null);
        }

        @Override
        @Messages("LBL_ConfigureInSuite=Configure in Module Suite")
        public JComponent createComponent(Category category, Lookup context) {
            Project prj = context.lookup(Project.class);

            assert prj != null;

            FileObject customizersFolder;

            try {
                //XXX:
                customizersFolder = FileUtil.createFolder(FileUtil.getConfigRoot(), "Projects/hints/java-based");
            } catch (IOException ex) {
                throw new IllegalStateException(ex);
            }

            final PerProjectHintsPanel panel = PerProjectHintsPanel.create(customizersFolder);

            FileObject nbbuildMisc = prj.getProjectDirectory().getFileObject("../../nbbuild/misc");

            if (nbbuildMisc == null) {
                nbbuildMisc = prj.getProjectDirectory().getFileObject("../../../nbbuild/misc");
            }

            if (nbbuildMisc != null) {
                URI settingsLocation = nbbuildMisc.toURI().resolve("hints-settings.xml");
                final ToolPreferences prefs = ToolPreferences.from(settingsLocation);

                panel.setPerProjectSettings(prefs);

                category.setOkButtonListener(new ActionListener() {
                    @Override public void actionPerformed(ActionEvent e) {
                        panel.applyChanges();
                    }
                });

                category.setStoreListener(new ActionListener() {
                    @Override public void actionPerformed(ActionEvent e) {
                        try {
                            prefs.save();
                        } catch (IOException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                        FileHintPreferences.fireChange();
                    }
                });

                return panel.getPanel();
            }
            
            Project suiteProject = suiteProject(prj);
            
            if (suiteProject != null && suiteProject != prj) {
                JLabel configureInModuleSuite = new JLabel(Bundle.LBL_ConfigureInSuite());
                
                return configureInModuleSuite;
            }
            
            //standalone:
            return StandardProjectSettings.createCustomizerProvider("Projects/hints/java-based").createComponent(category, context);
        }

    }
    
    private static Project suiteProject(Project prj) {
        try {
            FileObject suiteProperties = prj.getProjectDirectory().getFileObject("nbproject/suite.properties");

            if (suiteProperties == null) return null; //not a suite component

            EditableProperties p = new EditableProperties(false);

            try (InputStream is = suiteProperties.getInputStream()) {
                p.load(is);
            }

            String suiteDir = p.get("suite.dir").replace("${basedir}/", "");
            FileObject suiteProjectLoc = suiteDir != null ? prj.getProjectDirectory().getFileObject(suiteDir) : null;

            return suiteProjectLoc != null ? ProjectManager.getDefault().findProject(suiteProjectLoc) : null;
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
            return null;
        }
    }
    
}
