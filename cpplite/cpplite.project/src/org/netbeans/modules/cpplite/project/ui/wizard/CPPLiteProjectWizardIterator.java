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
package org.netbeans.modules.cpplite.project.ui.wizard;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.prefs.Preferences;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.templates.TemplateRegistration;
import org.netbeans.modules.cpplite.project.BuildConfiguration;
import org.netbeans.modules.cpplite.project.CPPLiteProject;
import static org.netbeans.modules.cpplite.project.CPPLiteProject.KEY_COMPILE_COMMANDS;
import static org.netbeans.modules.cpplite.project.CPPLiteProject.getBuildPreferences;
import org.netbeans.modules.cpplite.project.ui.Build;
import org.netbeans.modules.cpplite.project.ui.Editor;
import org.netbeans.spi.project.ActionProvider;
import org.openide.WizardDescriptor;
import org.openide.WizardDescriptor.Panel;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle.Messages;

/**
 *
 * @author lahvac
 */
@TemplateRegistration(
    folder="Project/Native",
    position=1000000,
    displayName="#template_cpplite",
    iconBase="org/netbeans/modules/cpplite/project/resources/project.gif",
    description="CPPLiteProjectDescription.html"
)
@Messages({"template_cpplite=Lightweight C/C++ Project",
           "CAP_ProjectPath=Location",
           "CAP_Editor=Editor",
           "CAP_Build=Build"
})
public class CPPLiteProjectWizardIterator implements WizardDescriptor.InstantiatingIterator<WizardDescriptor> {

    private final Panel[] panels = new Panel[] {
        new CPPLiteProjectPathPanel.PanelImpl(),
        new EditorPanelImpl(),
        new BuildPanelImpl()
    };
    
    private WizardDescriptor wizard;
    private int idx;

    @Override
    public Set instantiate() throws IOException {
        CPPLiteProjectSettings settings = CPPLiteProjectSettings.get(wizard);
        FileObject projectDirectory = FileUtil.toFileObject(new File(settings.getProjectPath()));
        Preferences prefs = CPPLiteProject.getRootPreferences(projectDirectory);
        prefs.putBoolean(CPPLiteProject.KEY_IS_PROJECT, true);
        settings.getBuildConfig().save(getBuildPreferences(projectDirectory));
        prefs.put(KEY_COMPILE_COMMANDS, settings.getEditorConfigPath());
        return Collections.singleton(projectDirectory);
    }

    @Override
    public void initialize(WizardDescriptor wizard) {
        this.wizard = wizard;
        this.idx = 0;
        int i = 0;
        String[] captions = new String[]{
            Bundle.CAP_ProjectPath(),
            Bundle.CAP_Editor(),
            Bundle.CAP_Build()
        };
        for (Panel p : panels) {
            ((JComponent) p.getComponent()).putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, i++);
            ((JComponent) p.getComponent()).putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, captions);
        }
    }

    @Override
    public void uninitialize(WizardDescriptor wizard) {
        this.wizard = null;
    }

    @Override
    public WizardDescriptor.Panel<WizardDescriptor> current() {
        return panels[idx];
    }

    @Override
    public String name() {
        return "TODO - wizard name";
    }

    @Override
    public boolean hasNext() {
        return idx < panels.length - 1;
    }

    @Override
    public boolean hasPrevious() {
        return idx > 0;
    }

    @Override
    public void nextPanel() {
        idx++;
    }

    @Override
    public void previousPanel() {
        idx--;
    }

    @Override
    public void addChangeListener(ChangeListener l) {
    }

    @Override
    public void removeChangeListener(ChangeListener l) {
    }
    
    public static final class CPPLiteProjectSettings {
        public static CPPLiteProjectSettings get(WizardDescriptor desc) {
            CPPLiteProjectSettings setting = (CPPLiteProjectSettings) desc.getProperty(CPPLiteProjectSettings.class.getName());
            if (setting == null) {
                desc.putProperty(CPPLiteProjectSettings.class.getName(), setting = new CPPLiteProjectSettings());
            }
            return setting;
        }
        private String projectPath;
        private String editorConfigPath;
        private BuildConfiguration buildConfig;

        public String getProjectPath() {
            return projectPath;
        }

        public void setProjectPath(String projectPath) {
            this.projectPath = projectPath;
        }

        public String getEditorConfigPath() {
            if (editorConfigPath != null) {
                return editorConfigPath;
            }
            FileObject projectDirectory = FileUtil.toFileObject(new File(projectPath));
            if (projectDirectory != null && projectDirectory.getFileObject("compile_commands.json") != null) {
                return FileUtil.toFile(projectDirectory.getFileObject("compile_commands.json")).getAbsolutePath();
            }
            return "";
        }

        public void setEditorConfigPath(String editorConfigPath) {
            this.editorConfigPath = editorConfigPath;
        }

        public BuildConfiguration getBuildConfig() {
            if (buildConfig != null) {
                return buildConfig;
            }
            FileObject projectDirectory = FileUtil.toFileObject(new File(projectPath));
            if (projectDirectory != null && projectDirectory.getFileObject("CMakeLists.txt") != null) {
                return cmakeBuildConfiguration("Release");
            }
            return new BuildConfiguration("", Collections.emptyMap());
        }

        private static BuildConfiguration cmakeBuildConfiguration(String name) {
            Map<String, List<List<String>>> action2Comments = new HashMap<>();
            action2Comments.put(ActionProvider.COMMAND_BUILD,
                                Arrays.asList(Arrays.asList("cmake", "-H.", "-B" + name, "-DCMAKE_BUILD_TYPE=" + name),
                                              Arrays.asList("cmake", "--build", name)));
            return new BuildConfiguration("Release", action2Comments);
        }

        public void setBuildConfig(BuildConfiguration buildConfig) {
            this.buildConfig = buildConfig;
        }
        
    }

    private static class EditorPanelImpl implements WizardDescriptor.FinishablePanel<WizardDescriptor> {

        private Editor panel;

        @Override
        public Editor getComponent() {
            if (panel == null) {
                panel = new Editor();
            }
            return panel;
        }

        @Override
        public HelpCtx getHelp() {
            return HelpCtx.DEFAULT_HELP;
        }

        @Override
        public void readSettings(WizardDescriptor settings) {
            getComponent().load(CPPLiteProjectSettings.get(settings).getEditorConfigPath());
        }

        @Override
        public void storeSettings(WizardDescriptor settings) {
            if (panel != null) {
                CPPLiteProjectSettings.get(settings).setEditorConfigPath(panel.save());
            }
        }

        @Override
        public boolean isValid() {
            return true;
        }

        @Override
        public void addChangeListener(ChangeListener l) {
        }

        @Override
        public void removeChangeListener(ChangeListener l) {
        }

        @Override
        public boolean isFinishPanel() {
            return true;
        }
        
    }

    private static class BuildPanelImpl implements WizardDescriptor.FinishablePanel<WizardDescriptor> {

        private Build panel;

        @Override
        public Build getComponent() {
            if (panel == null) {
                panel = new Build();
            }
            return panel;
        }

        @Override
        public HelpCtx getHelp() {
            return HelpCtx.DEFAULT_HELP;
        }

        @Override
        public void readSettings(WizardDescriptor settings) {
            getComponent().load(CPPLiteProjectSettings.get(settings).getBuildConfig());
        }

        @Override
        public void storeSettings(WizardDescriptor settings) {
            if (panel != null) {
                CPPLiteProjectSettings.get(settings).setBuildConfig(panel.save());
            }
        }

        @Override
        public boolean isValid() {
            return true;
        }

        @Override
        public void addChangeListener(ChangeListener l) {
        }

        @Override
        public void removeChangeListener(ChangeListener l) {
        }

        @Override
        public boolean isFinishPanel() {
            return true;
        }
        
    }
}
