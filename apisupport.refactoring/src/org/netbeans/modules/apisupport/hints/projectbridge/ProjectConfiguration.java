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

            FileObject nbbuildMisc = prj.getProjectDirectory().getFileObject("../nbbuild/misc");

            if (nbbuildMisc == null) {
                nbbuildMisc = prj.getProjectDirectory().getFileObject("../../nbbuild/misc");
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

            FileObject nbbuildMisc = prj.getProjectDirectory().getFileObject("../nbbuild/misc");

            if (nbbuildMisc == null) {
                nbbuildMisc = prj.getProjectDirectory().getFileObject("../../nbbuild/misc");
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
