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
package org.netbeans.modules.web.clientproject.ui.wizard;

import java.awt.Component;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.web.clientproject.ClientSideProject;
import org.netbeans.modules.web.clientproject.api.WebClientProjectConstants;
import org.netbeans.modules.web.clientproject.util.ClientSideProjectUtilities;
import org.netbeans.modules.web.clientproject.util.FileUtilities;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.WizardDescriptor.Panel;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;

/**
 * Wrapper for the standard New File wizard.
 */
public class NewFileWizardIterator implements WizardDescriptor.InstantiatingIterator<WizardDescriptor> {

    private static final Logger LOGGER = Logger.getLogger(NewFileWizardIterator.class.getName());

    private static final String JSON_MIME_TYPE = "text/x-json"; // NOI18N

    private WizardDescriptor descriptor;
    private WizardDescriptor.Panel<WizardDescriptor>[] panels;
    private int index;


    @Override
    public void initialize(WizardDescriptor wizard) {
        this.descriptor = wizard;
        setTargetFolder();
        panels = getPanels();

        // make sure list of steps is accurate.
        String[] beforeSteps = (String[]) wizard.getProperty(WizardDescriptor.PROP_CONTENT_DATA);
        int beforeStepLength = beforeSteps.length - 1;
        String[] steps = createSteps(beforeSteps);
        for (int i = 0; i < panels.length; i++) {
            Component c = panels[i].getComponent();
            if (c instanceof JComponent) { // assume Swing components
                JComponent jc = (JComponent) c;
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, Integer.valueOf(i + beforeStepLength - 1));
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, steps);
            }
        }
    }

    @Override
    public Set<FileObject> instantiate() throws IOException {
        FileObject dir = Templates.getTargetFolder(descriptor);
        FileObject template = Templates.getTemplate(descriptor);

        DataFolder dataFolder = DataFolder.findFolder(dir);
        DataObject dataTemplate = DataObject.find(template);
        DataObject createdFile = dataTemplate.createFromTemplate(dataFolder, Templates.getTargetName(descriptor));
        return Collections.singleton(createdFile.getPrimaryFile());
    }

    @Override
    public void uninitialize(WizardDescriptor wizard) {
        panels = null;
    }

    @Override
    public Panel<WizardDescriptor> current() {
        return panels[index];
    }

    @Override
    public String name() {
        return ""; // NOI18N
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
        Project project = Templates.getProject(descriptor);
        SourceGroup[] groups = getSourceGroups(project, Templates.getTemplate(descriptor));
        WizardDescriptor.Panel<WizardDescriptor> simpleTargetChooserPanel = Templates.buildSimpleTargetChooser(project, groups).create();

        @SuppressWarnings({"unchecked", "rawtypes"})
        WizardDescriptor.Panel<WizardDescriptor>[] panels = new WizardDescriptor.Panel[] {simpleTargetChooserPanel};
        return panels;
    }

    private SourceGroup[] getSourceGroups(Project project, FileObject file) {
        // #239027
        if (FileUtil.getMIMEType(file, JSON_MIME_TYPE, null) != null) {
            return ProjectUtils.getSources(project).getSourceGroups(Sources.TYPE_GENERIC);
        }
        ClientSideProject clientSideProject = getClientSideProject(project);
        if (clientSideProject == null) {
            // #231347
            return ProjectUtils.getSources(project).getSourceGroups(Sources.TYPE_GENERIC);
        }
        SourceGroup[] allGroups = ClientSideProjectUtilities.getSourceGroups(project);
        if (!FileUtilities.isHtmlFile(file)
                && !FileUtilities.isCssFile(file)) {
            // not html or css -> return all source groups
            return allGroups;
        }
        // html or css file -> return only sources or site root
        List<SourceGroup> groups = new ArrayList<>();
        groups.addAll(Arrays.asList(ClientSideProjectUtilities.getSourceGroups(project, WebClientProjectConstants.SOURCES_TYPE_HTML5_SITE_ROOT)));
        groups.addAll(Arrays.asList(ClientSideProjectUtilities.getSourceGroups(project, WebClientProjectConstants.SOURCES_TYPE_HTML5)));
        if (!groups.isEmpty()) {
            return groups.toArray(new SourceGroup[0]);
        }
        return allGroups;
    }

    private void setTargetFolder() {
        if (Templates.getTargetFolder(descriptor) != null) {
            // already set
            return;
        }
        Project project = Templates.getProject(descriptor);
        if (project == null) {
            // no project => ignore
            return;
        }
        ClientSideProject clientSideProject = getClientSideProject(project);
        if (clientSideProject == null) {
            // no client side project
            return;
        }
        FileObject siteRoot = clientSideProject.getSiteRootFolder();
        if (siteRoot != null) {
            Templates.setTargetFolder(descriptor, siteRoot);
        }
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

    @CheckForNull
    private ClientSideProject getClientSideProject(Project project) {
        return project.getLookup().lookup(ClientSideProject.class);
    }

}
