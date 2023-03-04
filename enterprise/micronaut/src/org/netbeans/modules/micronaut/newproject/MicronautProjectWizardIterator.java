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
package org.netbeans.modules.micronaut.newproject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.templates.TemplateRegistration;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.ui.support.ProjectChooser;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.AsyncGUIJob;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 * Wizard iterator for Micronaut projects.
 *
 * @author Dusan Balek
 */
@NbBundle.Messages("MicronautProject_DN=Micronaut Project")
public class MicronautProjectWizardIterator implements WizardDescriptor.ProgressInstantiatingIterator<WizardDescriptor> {

    private static final Logger LOG = Logger.getLogger(MicronautProjectWizardIterator.class.getName());

    @TemplateRegistration(folder = "Project/Maven2", position = 300, displayName = "#MicronautProject_DN", description = "MicronautMavenProjectDescription.html", iconBase = "org/netbeans/modules/micronaut/resources/micronaut.png")
    public static class MavenMicronautProject extends MicronautProjectWizardIterator {

        @Override
        public void initialize(WizardDescriptor wizard) {
            super.initialize(wizard);
            wizard.putProperty(BUILD_TOOL, "MAVEN");
        }
    }

    @TemplateRegistration(folder = "Project/Gradle", position = 300, displayName = "#MicronautProject_DN", description = "MicronautGradleProjectDescription.html", iconBase = "org/netbeans/modules/micronaut/resources/micronaut.png")
    public static class GradleMicronautProject extends MicronautProjectWizardIterator {

        @Override
        public void initialize(WizardDescriptor wizard) {
            super.initialize(wizard);
            wizard.putProperty(BUILD_TOOL, "GRADLE");
        }
    }

    public static final String PROJECT_NAME = "project.name";
    public static final String PROJECT_LOCATION = "project.location";
    public static final String SERVICE_URL = "service.url";
    public static final String MAVEN_GROUP = "maven.group";
    public static final String MAVEN_ARTIFACT = "maven.artifact";
    public static final String APPLICATION_TYPE = "application.type";
    public static final String JAVA_VERSION = "java.version";
    public static final String LANGUAGE = "language";
    public static final String BUILD_TOOL = "build.tool";
    public static final String TEST_FRAMEWORK = "test.framework";
    public static final String FEATURES = "features";

    private transient int index;
    private transient WizardDescriptor.Panel[] panels;
    private transient WizardDescriptor wiz;

    @Override
    public Set instantiate(ProgressHandle handle) throws IOException {
        try {
            handle.start(4);
            String projectName = (String) wiz.getProperty(PROJECT_NAME);
            File projFile = FileUtil.normalizeFile((File) wiz.getProperty(PROJECT_LOCATION));
            projFile.mkdirs();
            handle.progress(1);
            InputStream stream = MicronautLaunchService.getInstance().create((String) wiz.getProperty(SERVICE_URL),
                    (MicronautLaunchService.ApplicationType) wiz.getProperty(APPLICATION_TYPE),
                    (String) wiz.getProperty(MAVEN_GROUP) + '.' + (String) wiz.getProperty(MAVEN_ARTIFACT),
                    (String) wiz.getProperty(JAVA_VERSION),
                    (String) wiz.getProperty(LANGUAGE),
                    (String) wiz.getProperty(BUILD_TOOL),
                    (String) wiz.getProperty(TEST_FRAMEWORK),
                    (Set<MicronautLaunchService.Feature>) wiz.getProperty(FEATURES));
            handle.progress(2);
            FileObject projDir = FileUtil.toFileObject(projFile);
            unzip(stream, projDir, projectName + '/');
            handle.progress(3);
            ProjectManager.getDefault().clearNonProjectCache();
            Project prj = ProjectManager.getDefault().findProject(projDir);
            if (prj != null) {
                ActionProvider actionProvider = prj.getLookup().lookup(ActionProvider.class);
                if (actionProvider != null && actionProvider.isActionEnabled(ActionProvider.COMMAND_PRIME, Lookup.EMPTY)) {
                    actionProvider.invokeAction(ActionProvider.COMMAND_PRIME, Lookup.EMPTY);
                }
            }
            File parent = projFile.getParentFile();
            if (parent != null && parent.exists()) {
                ProjectChooser.setProjectsFolder(parent);
            }
            return Collections.singleton(projDir);
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        } finally {
            handle.finish();
        }
        return Collections.emptySet();
    }

    @Override
    public Set instantiate() throws IOException {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    @NbBundle.Messages("LBL_WizardTitle=Micronaut Maven Project")
    public void initialize(WizardDescriptor wizard) {
        this.wiz = wizard;
        wiz.putProperty("NewProjectWizard_Title", Bundle.LBL_WizardTitle()); //NOI18N
        index = 0;
        panels = new WizardDescriptor.Panel[] {
            new NameAndLocationWizardPanel(),
            new BasePropertiesWizardPanel(),
            new FeaturesWizardPanel()
        };
        String[] steps = new String[] {
            Bundle.LBL_NameAndLocationStep(),
            Bundle.LBL_BasePropertiesStep(),
            Bundle.LBL_Features()
        };
        for (int i = 0; i < panels.length; i++) {
            JComponent jc = (JComponent) panels[i].getComponent();
            jc.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, i);
            jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, steps);
        }
        Utilities.attachInitJob(panels[1].getComponent(), (AsyncGUIJob) panels[1].getComponent());
        Utilities.attachInitJob(panels[2].getComponent(), (AsyncGUIJob) panels[2].getComponent());
    }

    @Override
    public void uninitialize(WizardDescriptor wizard) {
    }

    @Override
    public WizardDescriptor.Panel<WizardDescriptor> current() {
        return panels[index];
    }

    @Override
    @NbBundle.Messages({"# {0} - index", "# {1} - length", "NameFormat={0} of {1}"})
    public String name() {
        return Bundle.NameFormat(index + 1, panels.length);
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
    }

    @Override
    public void removeChangeListener(ChangeListener l) {
    }

    private static void unzip(InputStream stream, FileObject folder, String prefix) throws IOException {
        try (ZipInputStream zis = new ZipInputStream(stream)) {
            ZipEntry zipEntry;
            while ((zipEntry = zis.getNextEntry()) != null) {
                String entryName = zipEntry.getName();
                if (entryName.startsWith(prefix)) {
                    entryName = entryName.substring(prefix.length());
                }
                if (zipEntry.isDirectory()) {
                    FileUtil.createFolder(folder, entryName);
                } else {
                    FileObject fo = FileUtil.createData(folder, entryName);
                    try (OutputStream out = fo.getOutputStream()) {
                        FileUtil.copy(zis, out);
                        File backingFile = FileUtil.toFile(fo);
                        if (backingFile != null) {
                            // Workaround for limit of JDK API:
                            // https://bugs.openjdk.java.net/browse/JDK-6194856
                            // The alternative would be to use commons-compress
                            // but at this time only these two elements need to
                            // be executable
                            if (entryName.equals("mvnw") || entryName.equals("gradlew")) {
                                backingFile.setExecutable(true);
                            }
                        } else {
                            LOG.log(Level.WARNING, "FileObject is not backed by file, can not adjust permissions: {0}", fo.getPath());
                        }
                    }
                }
            }
        }
    }
}
