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

package org.netbeans.modules.web.client.samples.wizard.iterator;

import java.awt.EventQueue;
import java.io.File;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.project.Project;
import org.netbeans.modules.web.client.samples.wizard.WizardConstants;
import org.netbeans.modules.web.client.samples.wizard.ui.OnlineSamplePanel;
import org.netbeans.modules.web.clientproject.createprojectapi.ClientSideProjectGenerator;
import org.netbeans.modules.web.clientproject.createprojectapi.CreateProjectProperties;
import org.netbeans.spi.project.ui.support.ProjectChooser;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.WizardDescriptor;
import org.openide.WizardDescriptor.Panel;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

/**
 *
 * @author Martin Janicek
 */
public abstract class OnlineSampleWizardIterator extends AbstractWizardIterator {

    private static final String LIBRARIES_PATH = "LIBRARIES_PATH"; // NOI18N


    protected OnlineSampleWizardIterator() {
    }

    protected abstract OnlineSiteTemplate getSiteTemplate();
    protected abstract String getProjectName();
    protected abstract String getProjectZipURL();


    @Override
    protected Panel[] createPanels(WizardDescriptor wizard) {
        wizard.putProperty(WizardConstants.SAMPLE_PROJECT_NAME, getProjectName());
        wizard.putProperty(WizardConstants.SAMPLE_PROJECT_URL, getProjectZipURL());
        wizard.putProperty(WizardConstants.SAMPLE_TEMPLATE, getSiteTemplate());

        return new Panel[] {
            new OnlineSamplePanel(wizard)
        };
    }

    /*
     * The whole code bellow this comment is more or less a copy of the code from
     * the HTML5 Project, class ClientSideProjectWizardIterator. It wasn't possible
     * to reuse easily the code from the original module and we are to close to the
     * final release of NetBeans 7.3, so I don't want to change the original HTML5
     * Project at the moment.
     *
     * BUT the current design is quite shitty (HTML5 Sample module depends on the
     * HTML5 Project which was never designed as an API). We should move some classes
     * from HTML5 Project module to the HTML5 Project API/SPI and use those. Plus we
     * need to get rid of the public packages in HTML5 Project (it is only temporary
     * solution)
     */

    @NbBundle.Messages({
        "OnlineSampleWizardIterator.creatingProject=Creating project...",
        "OnlineSampleWizardIterator.applyingTemplate=Applying template..."
    })
    @Override
    public Set instantiate(ProgressHandle handle) throws IOException {
        handle.start();
        handle.progress(Bundle.OnlineSampleWizardIterator_creatingProject()); //NOI18N

        final Set<FileObject> files = new LinkedHashSet<FileObject>();
        final File projectDir = FileUtil.normalizeFile((File) descriptor.getProperty(WizardConstants.SAMPLE_PROJECT_DIR));
        final String name = (String) descriptor.getProperty(WizardConstants.SAMPLE_PROJECT_NAME);

        if (!projectDir.isDirectory() && !projectDir.mkdirs()) {
            throw new IOException("Cannot create project directory"); //NOI18N
        }
        final FileObject projectDirFO = FileUtil.toFileObject(projectDir);

        CreateProjectProperties props = new CreateProjectProperties(projectDirFO, name)
                .setStartFile("index.html"); // NOI18N

        OnlineSiteTemplate siteTemplate = getSiteTemplate();
        if (siteTemplate != null) {
            siteTemplate.configure(props);
        }

        Project project = ClientSideProjectGenerator.createProject(props);

        // Always open top dir as a project:
        files.add(projectDirFO);

        if (siteTemplate != null) {
            handle.progress(Bundle.OnlineSampleWizardIterator_applyingTemplate());
            applySiteTemplate(project.getProjectDirectory(), props, siteTemplate, handle);
        }

        FileObject siteRoot = project.getProjectDirectory().getFileObject(props.getSiteRootFolder());
        assert siteRoot != null;
        String startFile = props.getStartFile();
        if (startFile != null) {
            FileObject startFileFo = siteRoot.getFileObject(startFile);
            if (startFileFo != null) {
                files.add(startFileFo);
            }
        }

        File parent = projectDir.getParentFile();
        if (parent != null && parent.exists()) {
            ProjectChooser.setProjectsFolder(parent);
        }

        handle.finish();
        return files;
    }

    @NbBundle.Messages({
        "# {0} - template name",
        "OnlineSampleWizardIterator.error.applyingSiteTemplate=Cannot apply template \"{0}\"."
    })
    private void applySiteTemplate(
            final FileObject projectDir,
            final CreateProjectProperties props,
            final OnlineSiteTemplate siteTemplate,
            final ProgressHandle handle) {

        assert !EventQueue.isDispatchThread();
        final String templateName = siteTemplate.getName();
        try {
            siteTemplate.apply(projectDir, props, handle);
        } catch (IOException ex) {
            errorOccured(Bundle.OnlineSampleWizardIterator_error_applyingSiteTemplate(templateName));
        }
    }

    private void errorOccured(String message) {
        DialogDisplayer.getDefault().notifyLater(new NotifyDescriptor.Message(message, NotifyDescriptor.ERROR_MESSAGE));
    }
}
