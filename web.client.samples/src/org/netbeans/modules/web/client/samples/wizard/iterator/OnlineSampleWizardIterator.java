/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
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
