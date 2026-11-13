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

package org.netbeans.modules.generic.project.ui.wizard;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.templates.TemplateRegistration;
import org.netbeans.modules.generic.project.GenericProject;
import org.netbeans.spi.project.ui.support.ProjectChooser;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle.Messages;

/**
 * The "New Generic Project" wizard. Instantiating it creates the project
 * directory with the {@link GenericProject#MARKER_FILE} — or, if "keep
 * repository clean" is selected, as a local project in NB's user config.
 */
@TemplateRegistration(
        folder = "Project/Generic", // NOI18N
        displayName = "#GpWizard_displayName", // NOI18N
        description = "GpTemplateDescription.html", // NOI18N
        iconBase = GenericProject.ICON,
        position = 1000
)
@Messages("GpWizard_displayName=New Generic Project")
public final class GpWizardIterator implements WizardDescriptor.InstantiatingIterator<WizardDescriptor> {

    private WizardDescriptor wiz;
    private List<WizardDescriptor.Panel<WizardDescriptor>> panels;
    private int index;

    private List<WizardDescriptor.Panel<WizardDescriptor>> createPanels() {
        return List.of(new GpWizardPanel());
    }

    @Override
    public Set<FileObject> instantiate() throws IOException {
        File projectDir = FileUtil.normalizeFile((File) wiz.getProperty("projdir")); // NOI18N
        boolean localMarker = Boolean.TRUE.equals(wiz.getProperty("localMarker")); // NOI18N
        Set<FileObject> created = createProject(projectDir, localMarker);
        File parent = projectDir.getParentFile();
        if (parent != null && parent.exists()) {
            ProjectChooser.setProjectsFolder(parent);
        }
        return created;
    }

    /**
     * Creates the project directory containing the marker file, or —
     * for {@code localMarker} — marks it as a local project by the
     * FileObject attribute only.
     */
    public static Set<FileObject> createProject(File projectDir, boolean localMarker) throws IOException {
        if (!projectDir.exists() && !projectDir.mkdirs()) {
            throw new IOException("Cannot create folder: " + projectDir); // NOI18N
        }
        FileObject dir = FileUtil.toFileObject(projectDir);
        if (dir == null) {
            throw new IOException("Cannot map folder to the file system: " + projectDir); // NOI18N
        }
        // local project: recognition attribute only,
        // otherwise the marker file in the working tree
        GenericProject.setMarkerState(dir, !localMarker);
        return Set.of(dir);
    }

    @Override
    public void initialize(WizardDescriptor wiz) {
        this.wiz = wiz;
        index = 0;
        panels = createPanels();
        for (int i = 0; i < panels.size(); i++) {
            if (panels.get(i).getComponent() instanceof JComponent jc) {
                // Step #.
                jc.putClientProperty("WizardPanel_contentSelectedIndex", i); // NOI18N
                // The step names are the component names of the panels.
                jc.putClientProperty("WizardPanel_contentData", jc.getName()); // NOI18N
            }
        }
    }

    @Override
    public void uninitialize(WizardDescriptor wiz) {
        this.wiz.putProperty("projdir", null); // NOI18N
        this.wiz.putProperty("name", null); // NOI18N
        this.wiz.putProperty("localMarker", null); // NOI18N
        this.wiz = null;
        panels = null;
    }

    @Override
    public String name() {
        return "%d of %d".formatted(index + 1, panels.size()); // NOI18N
    }

    @Override
    public boolean hasNext() {
        return index < panels.size() - 1;
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
    public WizardDescriptor.Panel<WizardDescriptor> current() {
        return panels.get(index);
    }

    @Override
    public final void addChangeListener(ChangeListener l) {
    }

    @Override
    public final void removeChangeListener(ChangeListener l) {
    }

}
