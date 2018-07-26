/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.html.ojet.ui.wizard;

import java.awt.Component;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.project.Project;
import org.netbeans.api.templates.TemplateRegistration;
import org.netbeans.api.templates.TemplateRegistrations;
import org.netbeans.modules.web.common.spi.ProjectWebRootQuery;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.util.NbBundle;

public final class NewJetModuleWizardIterator implements WizardDescriptor.AsynchronousInstantiatingIterator<WizardDescriptor> {

    private static final Logger LOGGER = Logger.getLogger(NewJetModuleWizardIterator.class.getName());

    private final Type type;

    private WizardDescriptor descriptor;
    private WizardDescriptor.Panel<WizardDescriptor>[] panels;
    private int index;


    private NewJetModuleWizardIterator(Type type) {
        assert type != null;
        this.type = type;
    }

    @TemplateRegistrations({
        @TemplateRegistration(
                folder = "ClientSide/OJET",
                content = "../resources/EmptyJETModule-js",
                scriptEngine = "freemarker",
                position = 100,
                displayName = "#Templates/ClientSide/OJET/EmptyJETModule-js",
                iconBase = "org/netbeans/modules/html/ojet/ui/resources/ojet-icon.png"),
        @TemplateRegistration(
                folder = "ClientSide/OJET",
                content = "../resources/EmptyJETModule-html",
                scriptEngine = "freemarker",
                position = 101,
                displayName = "#Templates/ClientSide/OJET/EmptyJETModule-html",
                iconBase = "org/netbeans/modules/html/ojet/ui/resources/ojet-icon.png"),
    })
    @NbBundle.Messages({
        "Templates/ClientSide/OJET/EmptyJETModule-js=Empty JET Module (JavaScript)",
        "Templates/ClientSide/OJET/EmptyJETModule-html=Empty JET Module (HTML)",
    })
    public static NewJetModuleWizardIterator empty() {
        return new NewJetModuleWizardIterator(Type.EMPTY);
    }

    @TemplateRegistrations({
        @TemplateRegistration(
                folder = "ClientSide/OJET",
                content = "../resources/KnockoutJETModule-js",
                scriptEngine = "freemarker",
                position = 200,
                displayName = "#Templates/ClientSide/OJET/KnockoutJETModule-js",
                iconBase = "org/netbeans/modules/html/ojet/ui/resources/ojet-icon.png"),
        @TemplateRegistration(
                folder = "ClientSide/OJET",
                content = "../resources/KnockoutJETModule-html",
                scriptEngine = "freemarker",
                position = 201,
                displayName = "#Templates/ClientSide/OJET/KnockoutJETModule-html",
                iconBase = "org/netbeans/modules/html/ojet/ui/resources/ojet-icon.png"),
    })
    @NbBundle.Messages({
        "Templates/ClientSide/OJET/KnockoutJETModule-js=Knockout JET Module (JavaScript)",
        "Templates/ClientSide/OJET/KnockoutJETModule-html=Knockout JET Module (HTML)",
    })
    public static NewJetModuleWizardIterator knockout() {
        return new NewJetModuleWizardIterator(Type.KNOCKOUT);
    }

    @Override
    public void initialize(WizardDescriptor wizard) {
        this.descriptor = wizard;
        init();
        panels = getPanels();

        // make sure list of steps is accurate.
        String[] beforeSteps = (String[]) wizard.getProperty(WizardDescriptor.PROP_CONTENT_DATA);
        int beforeStepLength = beforeSteps.length - 1;
        String[] steps = createSteps(beforeSteps);
        for (int i = 0; i < panels.length; i++) {
            Component c = panels[i].getComponent();
            if (c instanceof JComponent) { // assume Swing components
                JComponent jc = (JComponent) c;
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, i + beforeStepLength - 1);
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, steps);
            }
        }
    }

    @Override
    public Set<FileObject> instantiate() throws IOException {
        Project project = Templates.getProject(descriptor);
        if (project == null) {
            LOGGER.log(Level.INFO, "Project is required");
            return Collections.emptySet();
        }
        Set<FileObject> files = new HashSet<>();
        FileObject projectDirectory = project.getProjectDirectory();
        String name = (String) descriptor.getProperty(NewJetModuleWizardPanel.FILE_NAME);
        String jsFolder = (String) descriptor.getProperty(NewJetModuleWizardPanel.JS_FOLDER);
        String htmlFolder = (String) descriptor.getProperty(NewJetModuleWizardPanel.HTML_FOLDER);
        Map<String, Object> templateParams = Collections.<String, Object>singletonMap("name", name); // NOI18N
        // js
        FileObject folder = FileUtil.createFolder(projectDirectory, jsFolder);
        DataFolder dataFolder = DataFolder.findFolder(folder);
        DataObject dataTemplate = DataObject.find(FileUtil.getConfigFile(type.getJsTemplatePath()));
        DataObject createdFile = dataTemplate.createFromTemplate(dataFolder, name + ".js", templateParams); // NOI18N
        files.add(createdFile.getPrimaryFile());
        // html
        folder = FileUtil.createFolder(projectDirectory, htmlFolder);
        dataFolder = DataFolder.findFolder(folder);
        dataTemplate = DataObject.find(FileUtil.getConfigFile(type.getHtmlTemplatePath()));
        createdFile = dataTemplate.createFromTemplate(dataFolder, name + ".html", templateParams); // NOI18N
        files.add(createdFile.getPrimaryFile());
        return files;
    }

    @Override
    public void uninitialize(WizardDescriptor wizard) {
        panels = null;
    }

    @Override
    public WizardDescriptor.Panel<WizardDescriptor> current() {
        return panels[index];
    }

    @Override
    public String name() {
        return "";
    }

    @Override
    public boolean hasNext() {
        return index < panels.length - 1;
    }

    @Override
    public boolean hasPrevious() {
        return index > 0;
    }

    @Override
    public void nextPanel() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        index++;
    }

    @Override
    public void previousPanel() {
        if (!hasPrevious()) {
            throw new NoSuchElementException();
        }
        index--;
    }

    @Override
    public void addChangeListener(ChangeListener l) {
        // noop
    }

    @Override
    public void removeChangeListener(ChangeListener l) {
        // noop
    }

    private WizardDescriptor.Panel<WizardDescriptor>[] getPanels() {
        return new WizardDescriptor.Panel[] {new NewJetModuleWizardPanel()};
    }

    private void init() {
        Project project = Templates.getProject(descriptor);
        if (project == null) {
            // no project => ignore (should not happen)
            LOGGER.log(Level.INFO, "Project is required");
            return;
        }
        String jsFolder = resolveWebRootPath(project, "js/viewModels"); // NOI18N
        String htmlFolder = resolveWebRootPath(project, "js/views"); // NOI18N
        descriptor.putProperty(NewJetModuleWizardPanel.FILE_NAME, findFreeFilename(project.getProjectDirectory(), jsFolder, htmlFolder, "home")); // NOI18N
        descriptor.putProperty(NewJetModuleWizardPanel.PROJECT, project);
        descriptor.putProperty(NewJetModuleWizardPanel.JS_FOLDER, jsFolder);
        descriptor.putProperty(NewJetModuleWizardPanel.HTML_FOLDER, htmlFolder);
    }

    private String[] createSteps(String[] beforeSteps) {
        int beforeStepLength = beforeSteps.length - 1;
        String[] res = new String[beforeStepLength + panels.length];
        for (int i = 0; i < res.length; i++) {
            if (i < (beforeStepLength)) {
                res[i] = beforeSteps[i];
            } else {
                res[i] = panels[i - beforeStepLength].getComponent().getName();
            }
        }
        return res;
    }

    private String resolveWebRootPath(Project project, String path) {
        FileObject webRoot = getWebRoot(project);
        String relativePath = FileUtil.getRelativePath(project.getProjectDirectory(), webRoot);
        if (relativePath == null) {
            return path;
        }
        if (relativePath.isEmpty()) {
            return path;
        }
        return relativePath + "/" + path; // NOI18N
    }

    @NonNull
    private FileObject getWebRoot(Project project) {
        Collection<FileObject> webRoots = ProjectWebRootQuery.getWebRoots(project);
        if (webRoots.isEmpty()) {
            return project.getProjectDirectory();
        }
        return webRoots.iterator().next();
    }

    private String findFreeFilename(FileObject projectDirectory, String jsFolder, String htmlFolder, String name) {
        String tmpname = name;
        for (int i = 0; i < 100; i++) {
            if (projectDirectory.getFileObject(jsFolder + "/" + tmpname + ".js") == null // NOI18N
                    && projectDirectory.getFileObject(htmlFolder + "/" + tmpname + ".html") == null) { // NOI18N
                return tmpname;
            }
            tmpname = name + i;
        }
        return name;
    }

    //~ Inner classes

    private enum Type {
        EMPTY {
            @Override
            String getJsTemplatePath() {
                return "Templates/ClientSide/OJET/EmptyJETModule-js"; // NOI18N
            }

            @Override
            String getHtmlTemplatePath() {
                return "Templates/ClientSide/OJET/EmptyJETModule-html"; // NOI18N
            }
        },
        KNOCKOUT {
            @Override
            String getJsTemplatePath() {
                return "Templates/ClientSide/OJET/KnockoutJETModule-js"; // NOI18N
            }

            @Override
            String getHtmlTemplatePath() {
                return "Templates/ClientSide/OJET/KnockoutJETModule-html"; // NOI18N
            }
        };


        abstract String getJsTemplatePath();
        abstract String getHtmlTemplatePath();

    }

}
