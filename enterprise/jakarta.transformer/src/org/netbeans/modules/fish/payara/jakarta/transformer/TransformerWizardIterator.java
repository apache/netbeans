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
package org.netbeans.modules.fish.payara.jakarta.transformer;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.templates.TemplateRegistration;
import org.netbeans.modules.maven.api.execute.RunUtils;
import org.netbeans.modules.maven.execute.ModelRunConfig;
import org.netbeans.modules.maven.execute.model.NetbeansActionMapping;
import org.netbeans.spi.project.ui.support.ProjectChooser;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.netbeans.modules.maven.api.archetype.Archetype;
import org.apache.maven.artifact.versioning.ComparableVersion;
import org.netbeans.modules.maven.indexer.api.NBVersionInfo;
import org.netbeans.modules.maven.indexer.api.RepositoryQueries;
import org.openide.execution.ExecutorTask;

public final class TransformerWizardIterator implements WizardDescriptor.BackgroundInstantiatingIterator<WizardDescriptor> {

    private int index;
    private WizardDescriptor.Panel<WizardDescriptor>[] panels;
    private WizardDescriptor wizardDescriptor;
    static final Archetype TRANSFORMER_ARCHETYPE;

    static {
        TRANSFORMER_ARCHETYPE = new Archetype();
        TRANSFORMER_ARCHETYPE.setGroupId(TransformerConstants.PAYARA_TRANSFORMER_GROUP_ID);
        TRANSFORMER_ARCHETYPE.setVersion(TransformerConstants.PAYARA_TRANSFORMER_VERSION);
        TRANSFORMER_ARCHETYPE.setArtifactId(TransformerConstants.PAYARA_TRANSFORMER_ARTIFACT_ID);
    }

    public TransformerWizardIterator() {
    }

    @TemplateRegistration(
            folder = "Project/Maven2",
            position = 1010,
            displayName = "#transformer_displayName",
            description = "transformerDescription.html",
            iconBase = "org/netbeans/modules/fish/payara/jakarta/transformer/JakartaEEIcon.png")
    @Messages("transformer_displayName=Transform to Jakarta EE 10")
    public static TransformerWizardIterator createWebAppIterator() {
        return new TransformerWizardIterator();
    }

    @SuppressWarnings("rawtypes")
    private WizardDescriptor.Panel<WizardDescriptor>[] createPanels() {
        return new WizardDescriptor.Panel[]{new TransformerWizardPanel()};
    }

    private String[] createSteps() {
        return new String[]{
                NbBundle.getMessage(TransformerWizardIterator.class, "LBL_CreateProjectStep")
        };
    }

    @Override
    public Set<FileObject> instantiate() throws IOException {
        Set<FileObject> resultSet = new LinkedHashSet<>();
        //validate condition to identify if the case is to transform a folder to a new directory
        String target = (String) wizardDescriptor.getProperty("targetSelected");
        String source = (String) wizardDescriptor.getProperty("sourceSelected");
        boolean override = (Boolean) wizardDescriptor.getProperty("override");
        NotifyDescriptor notifyDescriptorTarget = new NotifyDescriptor.Message(TransformerConstants.ERROR_MESSAGE_DIALOG_TARGET,
                NotifyDescriptor.ERROR_MESSAGE);
        NotifyDescriptor notifyDescriptorSource = new NotifyDescriptor.Message(TransformerConstants.ERROR_MESSAGE_DIALOG_SOURCE,
                NotifyDescriptor.ERROR_MESSAGE);
        if (target != null && !target.isEmpty() && source != null && !source.isEmpty()) {
            File targetFile = new File(target);
            File sourceFile = new File(source);

            if (!sourceFile.exists()) {
                DialogDisplayer.getDefault().notify(notifyDescriptorSource);
            }

            if (sourceFile.exists() && sourceFile.isDirectory() && targetFile.isDirectory()) {
                //calling process to transform folder content to a new location
                return transformFolderToNewDirectory(resultSet, target, source);
            } else if (sourceFile.exists() && sourceFile.isDirectory() && !targetFile.exists()) {
                DialogDisplayer.getDefault().notify(notifyDescriptorTarget);
            }

            //validate condition to identify if the case is to transform a file to a new directory
            if (sourceFile.exists() && sourceFile.isFile() && targetFile.isDirectory()) {
                transformFileToNewDirectory(target, source);
            } else if (sourceFile.exists() && sourceFile.isFile() && !targetFile.exists()) {
                DialogDisplayer.getDefault().notify(notifyDescriptorTarget);
            }

        }

        //override cases        
        if (source != null && override) {
            File sourceFile = new File(source);

            if (!sourceFile.exists()) {
                DialogDisplayer.getDefault().notify(notifyDescriptorSource);
            }

            //override folder on the same location
            if (sourceFile.exists() && sourceFile.isDirectory()) {
                return transformFolderOnSameLocation(resultSet, source);
            }

            //override file on the same location
            if (sourceFile.exists() && sourceFile.isFile()) {
                transformFileOnSameLocation(source);
            }
        }

        return Collections.emptySet();
    }

    public void transformFileOnSameLocation(String source) throws IOException {
        File fileSource = new File(source);
        File parentDirectory = new File(fileSource.getParent());
        String pomFilePath = parentDirectory.getAbsolutePath()
                + File.separator + TransformerConstants.POM_FILE_NAME;
        File pomFile = new File(pomFilePath);
        if (parentDirectory.exists() && parentDirectory.isDirectory()) {
            FileObject directoryObject = FileUtil.toFileObject(parentDirectory);
            if (!pomFile.exists()) {
                copyTempPomFile(directoryObject);
            }
        }

        FileObject p = FileUtil.toFileObject(parentDirectory);
        if (p != null) {
            Project project = ProjectManager.getDefault().findProject(p);
            ExecutorTask task = callMavenProcess(source, source, project);
            if (task != null) {
                task.waitFinished();
                if (pomFile.exists()) {
                    pomFile.delete();
                }
            }
        }
    }

    public Set<FileObject> transformFolderOnSameLocation(Set<FileObject> resultSet, String source) throws IOException {
        File directory = new File(source);
        String pomFilePath = directory.getAbsolutePath()
                + File.separator + TransformerConstants.POM_FILE_NAME;
        File pomFile = new File(pomFilePath);
        FileObject directoryObject = FileUtil.toFileObject(directory);
        resultSet.add(directoryObject);
        ProjectChooser.setProjectsFolder(directory);
        if (!pomFile.exists()) {
            copyTempPomFile(directoryObject);
        }

        if (directoryObject != null) {
            Project project = ProjectManager.getDefault().findProject(directoryObject);
            ExecutorTask task = callMavenProcess(source, source, project);
            if (task != null) {
                task.waitFinished();
            }
        }

        showJakartaAdviseDialog();

        return resultSet;
    }

    public void transformFileToNewDirectory(String targetFolder, String sourceFolder) throws IOException {
        File directoryFile = createTargetDirectory(targetFolder);
        FileObject directoryObject = FileUtil.toFileObject(directoryFile);
        String pomFilePath = directoryFile.getAbsolutePath() + File.separator + TransformerConstants.POM_FILE_NAME;
        File pomFile = new File(pomFilePath);
        copyTempPomFile(directoryObject);

        File sourceFile = new File(sourceFolder);
        FileObject sourceToMove = FileUtil.toFileObject(sourceFile);
        String name = sourceFile.getName();
        if (name.contains(".")) {
            String[] parts = name.split("\\.");
            name = parts[0];
        }
        FileUtil.copyFile(sourceToMove, directoryObject, name);
        FileObject p = FileUtil.toFileObject(directoryFile);
        if (p != null) {
            Project project = ProjectManager.getDefault().findProject(p);
            String directoryName = directoryFile.getPath() + File.separator + sourceFile.getName();
            ExecutorTask task = callMavenProcess(directoryName, directoryName, project);
            if (task != null) {
                task.waitFinished();
                if (pomFile.exists()) {
                    pomFile.delete();
                }
            }
        }
    }

    public Set<FileObject> transformFolderToNewDirectory(Set<FileObject> resultSet,
                                                         String targetFolder, String sourceFolder) throws IOException {
        File directoryFile = createTargetDirectory(targetFolder);
        File tempDirectory = createTempDirectory(targetFolder);
        FileObject directoryObject = FileUtil.toFileObject(directoryFile);
        FileObject tempObject = FileUtil.toFileObject(tempDirectory);
        copyTempPomFile(tempObject);
        resultSet.add(directoryObject);

        File parent = directoryFile.getParentFile();
        if (parent != null && parent.exists()) {
            ProjectChooser.setProjectsFolder(parent);
        }

        String pomFilePath = tempDirectory.getAbsolutePath() + File.separator + TransformerConstants.POM_FILE_NAME;
        File pomFile = new File(pomFilePath);

        FileObject p = FileUtil.toFileObject(tempDirectory);
        if (p != null) {
            Project project = ProjectManager.getDefault().findProject(p);
            ExecutorTask task = callMavenProcess(sourceFolder, directoryFile.getPath(), project);
            if (task != null) {
                task.waitFinished();
                if (pomFile.exists()) {
                    pomFile.delete();
                }
                if (tempDirectory.exists()) {
                    tempDirectory.delete();
                }
            }
        }
        showJakartaAdviseDialog();
        return resultSet;
    }

    public ExecutorTask callMavenProcess(String source, String target, Project project) {
        NetbeansActionMapping mapping = new NetbeansActionMapping();
        List<String> goals = new ArrayList<>();
        goals.add(getLastTransformerVersionOfPlugin(TRANSFORMER_ARCHETYPE));
        mapping.setGoals(goals);
        mapping.setActionName("Transform source");
        mapping.addProperty(TransformerConstants.SELECTED_SOURCE_PROPERTY_NAME,
                source);
        mapping.addProperty(TransformerConstants.SELECTED_TARGET_PROPERTY_NAME,
                target);
        ModelRunConfig rc = new ModelRunConfig(project, mapping,
                mapping.getActionName(), null,
                Lookup.EMPTY, false);
        return RunUtils.run(rc);
    }

    public void showJakartaAdviseDialog() {
        DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
                TransformerConstants.INFORMATION_MESSAGE + "\n"
                        + TransformerConstants.JAKARTA_10_DEPENDENCY_EE_API + "\n or \n"
                        + TransformerConstants.JAKARTA_10_DEPENDENCY_WEB_API));
    }

    public void copyTempPomFile(FileObject directoryObject) throws IOException {
        try (InputStream s = TransformerWizardIterator.class.getResourceAsStream("pom.txt")) {
            FileObject fo = FileUtil.createData(directoryObject, TransformerConstants.POM_FILE_NAME);
            writeFile(s, fo);
        }
    }

    public File createTargetDirectory(String targetFolder) {
        File fileTargetFolder = new File(targetFolder
                + File.separator + TransformerConstants.PROJECT_NAME);
        File directoryFile = FileUtil.normalizeFile(fileTargetFolder);
        directoryFile.mkdirs();
        return directoryFile;
    }

    public File createTempDirectory(String targetFolder) {
        File fileTargetFolder = new File(targetFolder + File.separator + "temp");
        File tempFile = FileUtil.normalizeFile(fileTargetFolder);
        tempFile.mkdirs();
        return tempFile;
    }

    private String getLastTransformerVersionOfPlugin(Archetype archetype) {
        RepositoryQueries.Result<NBVersionInfo> versionsResult = RepositoryQueries.
                getVersionsResult(archetype.getGroupId(), archetype.getArtifactId(), null);

        List<NBVersionInfo> results = versionsResult.getResults();
        for (NBVersionInfo result : results) {
            String lastVersion = result.getVersion();
            if (!lastVersion.contains("SNAPSHOT")) {
                if (new ComparableVersion(lastVersion).compareTo(new ComparableVersion(archetype.getVersion())) > 0) {
                    archetype.setVersion(lastVersion);
                }
                return archetype.getGroupId() + ":" + archetype.getArtifactId() + ":"
                        + archetype.getVersion() + ":" + TransformerConstants.PAYARA_TRANSFORMER_GOAL;
            }
        }
        return archetype.getGroupId() + ":" + archetype.getArtifactId() + ":"
                + archetype.getVersion() + ":" + TransformerConstants.PAYARA_TRANSFORMER_GOAL;
    }

    @Override
    public void initialize(WizardDescriptor wiz) {
        this.wizardDescriptor = wiz;
        index = 0;
        panels = createPanels();
        // Make sure list of steps is accurate.
        String[] steps = createSteps();
        for (int i = 0; i < panels.length; i++) {
            Component c = panels[i].getComponent();
            if (steps[i] == null) {
                // Default step name to component name of panel.
                // Mainly useful for getting the name of the target
                // chooser to appear in the list of steps.
                steps[i] = c.getName();
            }
            if (c instanceof JComponent) { // assume Swing components
                JComponent jc = (JComponent) c;
                // Step #.
                // TODO if using org.openide.dialogs >= 7.8, can use WizardDescriptor.PROP_*:
                jc.putClientProperty("WizardPanel_contentSelectedIndex", i);
                // Step name (actually the whole list for reference).
                jc.putClientProperty("WizardPanel_contentData", steps);
            }
        }
        wizardDescriptor.putProperty(WizardDescriptor.PROP_IMAGE,
                ImageUtilities.loadImage(TransformerConstants.BANNER_IMAGE, true));
    }

    @Override
    public void uninitialize(WizardDescriptor wiz) {
        if (this.wizardDescriptor != null) {
            this.wizardDescriptor.putProperty("targetSelected", null);
            this.wizardDescriptor.putProperty("sourceSelected", null);
            this.wizardDescriptor = null;
        }
        panels = null;
    }

    @Override
    public String name() {
        return MessageFormat.format("{0} of {1}",
                new Object[]{index + 1, panels.length});
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
    public WizardDescriptor.Panel<WizardDescriptor> current() {
        return panels[index];
    }

    @Override
    public final void addChangeListener(ChangeListener l) {
    }

    @Override
    public final void removeChangeListener(ChangeListener l) {
    }

    private static void writeFile(InputStream str, FileObject fo) throws IOException {
        try (OutputStream out = fo.getOutputStream()) {
            FileUtil.copy(str, out);
        }
    }

}
