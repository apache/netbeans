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
import java.awt.EventQueue;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.templates.TemplateRegistration;
import org.netbeans.modules.html.ojet.OJETUtils;
import org.netbeans.modules.web.clientproject.api.network.NetworkException;
import org.netbeans.modules.web.clientproject.api.network.NetworkSupport;
import org.netbeans.modules.web.clientproject.createprojectapi.ClientSideProjectGenerator;
import org.netbeans.modules.web.clientproject.createprojectapi.CreateProjectProperties;
import org.netbeans.modules.web.clientproject.createprojectapi.CreateProjectUtils;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.Pair;

public final class NewProjectWizardIterator implements WizardDescriptor.ProgressInstantiatingIterator<WizardDescriptor> {

    private static final Logger LOGGER = Logger.getLogger(NewProjectWizardIterator.class.getName());

    private static final String HTML_MIME_TYPE = "text/html"; // NOI18N
    private static final String XHTML_MIME_TYPE = "text/xhtml"; // NOI18N

    private final String displayName;
    private final String zipUrl;
    private final File tmpFile;
    private final Pair<WizardDescriptor.FinishablePanel<WizardDescriptor>, String> baseWizard;

    private int index;
    private WizardDescriptor.Panel<WizardDescriptor>[] panels;
    private WizardDescriptor wizardDescriptor;


    private NewProjectWizardIterator(String displayName, String projectDirName, String zipUrl, File tmpFile) {
        assert displayName != null;
        assert projectDirName != null;
        assert zipUrl != null;
        assert tmpFile != null;
        this.displayName = displayName;
        this.zipUrl = zipUrl;
        this.tmpFile = tmpFile;
        baseWizard = CreateProjectUtils.createBaseWizardPanel(projectDirName);
    }

    // @TemplateRegistration(
    //        folder = "Project/ClientSide",
    //        displayName = "#NewProjectWizardIterator.newOracleJETBaseDistribution.displayName",
    //        description = "../resources/NewOracleJETBaseDistributionDescription.html",
    //        iconBase = OJETUtils.OJET_ICON_PATH,
    //        position = 250)
    // @NbBundle.Messages("NewProjectWizardIterator.newOracleJETBaseDistribution.displayName=Oracle JET Base Distribution")
    // public static NewProjectWizardIterator newOracleJETBaseDistribution() {
    //    return new NewProjectWizardIterator(
    //            Bundle.NewProjectWizardIterator_newOracleJETBaseDistribution_displayName(),
    //            "OracleJETApplication", // NOI18N
    //            "http://www.oracle.com/webfolder/technetwork/jet/code/oraclejet.zip", // NOI18N
    //            new File(System.getProperty("java.io.tmpdir"), "oraclejet.zip") // NOI18N
    //    );
    // }

    // @TemplateRegistration(
    //        folder = "Project/Samples/HTML5",
    //        displayName = "#NewProjectWizardIterator.newOracleJETQuickStartBasic.displayName",
    //        description = "../resources/NewOracleJETQuickStartBasicDescription.html",
    //        iconBase = OJETUtils.OJET_ICON_PATH,
    //        position = 2990)
    // @NbBundle.Messages("NewProjectWizardIterator.newOracleJETQuickStartBasic.displayName=Oracle JET QuickStart Basic")
    // public static NewProjectWizardIterator newOracleJETQuickStartBasic() {
    //    return new NewProjectWizardIterator(
    //            Bundle.NewProjectWizardIterator_newOracleJETQuickStartBasic_displayName(),
    //            "OracleJETQuickStartBasic", // NOI18N
    //            "http://www.oracle.com/webfolder/technetwork/jet/public_samples/OracleJET_QuickStartBasic.zip", // NOI18N
    //            new File(System.getProperty("java.io.tmpdir"), "OracleJET_QuickStartBasic.zip") // NOI18N
    //    );
    // }

    // @TemplateRegistration(
    //        folder = "Project/Samples/HTML5",
    //        displayName = "#NewProjectWizardIterator.newComponentInteractionSample.displayName",
    //        description = "../resources/NewComponentInteractionSampleDescription.html",
    //        iconBase = OJETUtils.OJET_ICON_PATH,
    //        position = 3000)
    // @NbBundle.Messages("NewProjectWizardIterator.newComponentInteractionSample.displayName=Oracle JET Component Interaction Sample")
    // public static NewProjectWizardIterator newComponentInteractionSample() {
    //    return new NewProjectWizardIterator(
    //            Bundle.NewProjectWizardIterator_newComponentInteractionSample_displayName(),
    //            "OracleJETComponentInteraction", // NOI18N
    //            "http://www.oracle.com/webfolder/technetwork/jet/public_samples/JET-ComponentInteraction.zip", // NOI18N
    //            new File(System.getProperty("java.io.tmpdir"), "JET-ComponentInteraction.zip") // NOI18N
    //    );
    // }

    @NbBundle.Messages("NewProjectWizardIterator.progress.creating=Creating project...")
    @Override
    public Set<FileObject> instantiate(ProgressHandle handle) throws IOException {
        handle.start();
        handle.progress(Bundle.NewProjectWizardIterator_progress_creating());

        Set<FileObject> files = new HashSet<>();

        File projectDir = FileUtil.normalizeFile((File) wizardDescriptor.getProperty(CreateProjectUtils.PROJECT_DIRECTORY));
        if (!projectDir.isDirectory()
                && !projectDir.mkdirs()) {
            throw new IOException("Cannot create project directory: " + projectDir);
        }
        FileObject projectDirectory = FileUtil.toFileObject(projectDir);
        assert projectDirectory != null : "FileObject must be found for " + projectDir;
        files.add(projectDirectory);

        setupOjetFiles(handle, files, projectDirectory);

        CreateProjectProperties createProperties = new CreateProjectProperties(projectDirectory, (String) wizardDescriptor.getProperty(CreateProjectUtils.PROJECT_NAME))
                .setSiteRootFolder(""); // NOI18N
        ClientSideProjectGenerator.createProject(createProperties);

        hackIgnoreSCSSErrorsInOJET(projectDirectory);

        handle.finish();
        return files;
    }

    @Override
    public Set instantiate() throws IOException {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public void initialize(WizardDescriptor wizard) {
        wizardDescriptor = wizard;
        // #245975
        Mutex.EVENT.readAccess(new Runnable() {
            @Override
            public void run() {
                initializeInternal();
            }
        });
    }

    void initializeInternal() {
        assert EventQueue.isDispatchThread();
        index = 0;
        panels = new WizardDescriptor.Panel[] {
            baseWizard.first(),
        };
        // Make sure list of steps is accurate.
        List<String> steps = Arrays.asList(
            baseWizard.second()
        );

        // XXX should be lazy
        for (int i = 0; i < panels.length; i++) {
            Component c = panels[i].getComponent();
            assert steps.get(i) != null : "Missing name for step: " + i;
            if (c instanceof JComponent) { // assume Swing components
                JComponent jc = (JComponent) c;
                // Step #.
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, i);
                // Step name (actually the whole list for reference).
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, steps.toArray(new String[0]));
                // name
                jc.setName(steps.get(i));
            }
        }
    }

    @Override
    public void uninitialize(WizardDescriptor wizard) {
        wizardDescriptor.putProperty(CreateProjectUtils.PROJECT_DIRECTORY, null);
        wizardDescriptor.putProperty(CreateProjectUtils.PROJECT_NAME, null);
        panels = null;
    }

    @Override
    public WizardDescriptor.Panel<WizardDescriptor> current() {
        wizardDescriptor.putProperty("NewProjectWizard_Title", displayName); // NOI18N
        return panels[index];
    }

    @NbBundle.Messages({
        "# {0} - current step index",
        "# {1} - number of steps",
        "NewProjectWizardIterator.name={0} of {1}"
    })
    @Override
    public String name() {
        return Bundle.NewProjectWizardIterator_name(index + 1, panels.length);
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

    @NbBundle.Messages({
        "NewProjectWizardIterator.progress.downloading=Downloading archive...",
        "NewProjectWizardIterator.progress.unpacking=Unpacking archive...",
    })
    private void setupOjetFiles(ProgressHandle handle, Set<FileObject> files, FileObject projectDirectory) throws IOException {
        try {
            // download
            handle.progress(Bundle.NewProjectWizardIterator_progress_downloading());
            NetworkSupport.downloadWithProgress(zipUrl, tmpFile, Bundle.NewProjectWizardIterator_progress_downloading());

            // check
            if (isHtmlFile(tmpFile)) {
                // likely not in oracle network
                if (NetworkSupport.showNetworkErrorDialog(displayName)) {
                    setupOjetFiles(handle, files, projectDirectory);
                }
            } else {
                // unzip
                handle.progress(Bundle.NewProjectWizardIterator_progress_unpacking());
                unzip(tmpFile.getAbsolutePath(), FileUtil.toFile(projectDirectory));

                // index file?
                FileObject indexFile = projectDirectory.getFileObject("index.html"); // NOI18N
                if (indexFile != null) {
                    files.add(indexFile);
                } else {
                    // readme file
                    FileObject readmeFile = projectDirectory.getFileObject("README.md"); // NOI18N
                    if (readmeFile != null) {
                        files.add(readmeFile);
                    }
                }
            }
        } catch (NetworkException ex) {
            LOGGER.log(Level.INFO, "Failed to download OJET archive", ex);
            if (NetworkSupport.showNetworkErrorDialog(displayName)) {
                setupOjetFiles(handle, files, projectDirectory);
            }
        } catch (InterruptedException ex) {
            // cancelled, should not happen
            assert false;
        }
    }

    private static boolean isHtmlFile(File file) {
        assert file != null;
        if (!file.exists()) {
            return false;
        }
        String mimeType = FileUtil.getMIMEType(FileUtil.toFileObject(file), HTML_MIME_TYPE, XHTML_MIME_TYPE);
        return HTML_MIME_TYPE.equals(mimeType)
                || XHTML_MIME_TYPE.equals(mimeType);
    }

    private static void unzip(String zipPath, File targetDirectory) throws IOException {
        assert zipPath != null;
        assert targetDirectory != null;

        try (ZipFile zipFile = new ZipFile(zipPath)) {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry zipEntry = entries.nextElement();
                File destinationFile = new File(targetDirectory, zipEntry.getName());
                ensureParentExists(destinationFile);
                copyZipEntry(zipFile, zipEntry, destinationFile);
            }
        }
    }

    private static void ensureParentExists(File file) throws IOException {
        File parent = file.getParentFile();
        if (!parent.isDirectory()) {
            if (!parent.mkdirs()) {
                throw new IOException("Cannot create parent directories for " + file.getAbsolutePath());
            }
        }
    }

    private static void copyZipEntry(ZipFile zipFile, ZipEntry zipEntry, File destinationFile) throws IOException {
        if (zipEntry.isDirectory()) {
            return;
        }
        try (InputStream inputStream = zipFile.getInputStream(zipEntry); FileOutputStream outputStream = new FileOutputStream(destinationFile)) {
            FileUtil.copy(inputStream, outputStream);
        }
    }

    private void hackIgnoreSCSSErrorsInOJET(FileObject projectDirectory) {
        Enumeration<? extends FileObject> children = projectDirectory.getChildren(true);
        while (children.hasMoreElements()) {
            FileObject file = children.nextElement();
            if ("scss".equals(file.getName())) { //NOI18N
                try {
                    file.setAttribute("disable_error_checking_CSS", Boolean.TRUE.toString()); //NOI18N
                    break;
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
    }

}
