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

package org.netbeans.modules.projectimport.eclipse.j2se;

import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.java.api.common.project.ProjectProperties;
import org.netbeans.modules.java.j2seproject.J2SEProject;
import org.netbeans.modules.java.j2seproject.J2SEProjectGenerator;
import org.netbeans.modules.java.j2seproject.ui.customizer.J2SEProjectProperties;
import org.netbeans.modules.projectimport.eclipse.core.spi.LaunchConfiguration;
import org.netbeans.modules.projectimport.eclipse.core.spi.ProjectFactorySupport;
import org.netbeans.modules.projectimport.eclipse.core.spi.ProjectImportModel;
import org.netbeans.modules.projectimport.eclipse.core.spi.ProjectTypeUpdater;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.ImageUtilities;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 *
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.projectimport.eclipse.core.spi.ProjectTypeFactory.class, position=1000)
public class J2SEProjectFactory implements ProjectTypeUpdater {
    

    private static final String JAVA_NATURE = "org.eclipse.jdt.core.javanature"; // NOI18N
    private static final Icon J2SE_PROJECT_ICON = ImageUtilities.loadImageIcon("org/netbeans/modules/java/j2seproject/ui/resources/j2seProject.png", false); // NOI18N
    private static final String MANIFEST_FILE = "manifest.mf";

    public J2SEProjectFactory() {
    }

    public boolean canHandle(ProjectDescriptor descriptor) {
        return descriptor.getNatures().contains(JAVA_NATURE);
    }

    public Project createProject(final ProjectImportModel model, final List<String> importProblems) throws IOException {
        // calculate nb project location
        File nbProjectDir = model.getNetBeansProjectLocation(); // NOI18N

        if (ProjectFactorySupport.areSourceRootsOwned(model, nbProjectDir, importProblems)) {
            return null;
        }

        // create basic NB project
        String buildScript = null;
        if (new File(nbProjectDir, "build.xml").exists()) { //NOI18N
            buildScript = "nb-build.xml"; //NOI18N
        }
        final AntProjectHelper helper = J2SEProjectGenerator.createProject(
                nbProjectDir, model.getProjectName(), model.getEclipseSourceRootsAsFileArray(),
                model.getEclipseTestSourceRootsAsFileArray(), null, null, buildScript);

        EditableProperties ep = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        boolean changed = false;
        if (new File(nbProjectDir, "dist").exists()) { //NOI18N
            ep.setProperty("dist.dir", "nbdist"); //NOI18N
            changed = true;
        }
        if (new File(nbProjectDir, "build").exists()) { //NOI18N
            ep.setProperty("build.dir", "nbbuild"); //NOI18N
            changed = true;
        }
        if (changed) {
            helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
        }

        // get NB project
        J2SEProject nbProject = (J2SEProject) ProjectManager.getDefault().
                findProject(FileUtil.toFileObject(
                FileUtil.normalizeFile(nbProjectDir)));

        // set labels for source roots
        ProjectFactorySupport.updateSourceRootLabels(model.getEclipseSourceRoots(), nbProject.getSourceRoots());
        ProjectFactorySupport.updateSourceRootLabels(model.getEclipseTestSourceRoots(), nbProject.getTestSourceRoots());

        ProjectFactorySupport.setupSourceExcludes(helper, model, importProblems);

        setupCompilerProperties(helper, model);

        // update project classpath
        ProjectFactorySupport.updateProjectClassPath(helper, nbProject.getReferenceHelper(), model, importProblems);

        // set platform used by an Eclipse project
        if (model.getJavaPlatform() != null) {
            setExplicitJavaPlatform(helper, model);
        }

        addLaunchConfigurations(nbProject, model.getLaunchConfigurations());

        // save project
        ProjectManager.getDefault().saveProject(nbProject);
        return nbProject;
    }

    private void setExplicitJavaPlatform(final AntProjectHelper helper, final ProjectImportModel model) {
        Element pcd = helper.getPrimaryConfigurationData(true);
        NodeList sourceRootNodes = pcd.getElementsByTagNameNS(J2SEProject.PROJECT_CONFIGURATION_NAMESPACE, "source-roots"); //NOI18N
        assert sourceRootNodes.getLength() == 1 : "Broken project.xml file"; // NOI18N
        Element el = pcd.getOwnerDocument().createElementNS(J2SEProject.PROJECT_CONFIGURATION_NAMESPACE, "explicit-platform"); // NOI18N
        el.setAttribute("explicit-source-supported", "true"); // NOI18N
        pcd.insertBefore(el, sourceRootNodes.item(0));
        helper.putPrimaryConfigurationData(pcd, true);
        EditableProperties prop = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        String ver = model.getJavaPlatform().getSpecification().getVersion().toString();
        String normalizedName = model.getJavaPlatform().getProperties().get("platform.ant.name"); // NOI18N
        prop.setProperty(ProjectProperties.PLATFORM_ACTIVE, normalizedName);
        helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, prop);
    }

    public String calculateKey(ProjectImportModel model) {
        return ProjectFactorySupport.calculateKey(model);
    }

    public String update(Project project, ProjectImportModel model, String oldKey, List<String> importProblems) throws IOException {
        if (!(project instanceof J2SEProject)) {
            throw new IOException("is not java project: "+project.getClass().getName()); //NOI18N
        }

        String newKey = calculateKey(model);

        // update project classpath
        String actualKey = ProjectFactorySupport.synchronizeProjectClassPath(project,
                ((J2SEProject)project).getAntProjectHelper(),
                ((J2SEProject)project).getReferenceHelper(), model, oldKey, newKey, importProblems);

        setupCompilerProperties(((J2SEProject) project).getAntProjectHelper(), model);

        // TODO:
        // update source roots and platform

        // save project
        ProjectManager.getDefault().saveProject(project);

        return actualKey;
    }

    public Icon getProjectTypeIcon() {
        return J2SE_PROJECT_ICON;
    }

    public String getProjectTypeName() {
        return org.openide.util.NbBundle.getMessage(J2SEProjectFactory.class, "LABEL_Java_Project");
    }

    public List<WizardDescriptor.Panel<WizardDescriptor>> getAdditionalImportWizardPanels() {
        return Collections.<WizardDescriptor.Panel<WizardDescriptor>>emptyList();
    }

    private void setupCompilerProperties(AntProjectHelper helper, ProjectImportModel model) {
        EditableProperties ep = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        ep.setProperty(ProjectProperties.JAVAC_SOURCE, model.getSourceLevel());
        ep.setProperty(ProjectProperties.JAVAC_TARGET, model.getTargetLevel());
        ep.setProperty(ProjectProperties.JAVAC_DEPRECATION, Boolean.toString(model.isDeprecation()));
        ep.setProperty(ProjectProperties.JAVAC_COMPILERARGS, model.getCompilerArgs());
        ep.setProperty(ProjectProperties.SOURCE_ENCODING, model.getEncoding());
        helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
        ep = helper.getProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH);
        ep.setProperty(ProjectProperties.JAVAC_DEBUG, Boolean.toString(model.isDebug()));
        helper.putProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH, ep);
    }

    public File getProjectFileLocation(ProjectDescriptor descriptor, String token) {
        // N/A
        return null;
    }

    private void addLaunchConfigurations(J2SEProject nbProject, Collection<LaunchConfiguration> launchConfigurations) throws IOException {
        Iterator<LaunchConfiguration> it = launchConfigurations.iterator();
        while (it.hasNext()) {
            LaunchConfiguration config = it.next();
            if (!config.getType().equals(LaunchConfiguration.TYPE_LOCAL_JAVA_APPLICATION) || config.getMainType() == null) {
                it.remove();
            }
        }
        AntProjectHelper aph = nbProject.getAntProjectHelper();
        // Adding manifest.file property to ensure correct behavior of jar after build.
        if (!launchConfigurations.isEmpty()) {
            EditableProperties props = aph.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
            createManifest(nbProject.getProjectDirectory(), MANIFEST_FILE);
            props.put("manifest.file", MANIFEST_FILE);
            aph.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, props);
        }
        if (launchConfigurations.size() == 1) {
            // For just a single config, treat it as the default config.
            LaunchConfiguration config = launchConfigurations.iterator().next();
            EditableProperties props = aph.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
            storeConfig(config, props);
            aph.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, props);
        } else if (!launchConfigurations.isEmpty()) {
            // >1 config. No idea which is "default" so just create them all and let user pick if they want.
            for (LaunchConfiguration config : launchConfigurations) {
                String path = "nbproject/configs/" + config.getName() + ".properties"; // NOI18N
                EditableProperties props = aph.getProperties(path);
                storeConfig(config, props);
                aph.putProperties(path, props);
            }
            // Set one of them to current so that it is clear they got imported.
            String path = "nbproject/private/config.properties"; // NOI18N
            EditableProperties props = aph.getProperties(path);
            props.setProperty("config", launchConfigurations.iterator().next().getName()); // NOI18N
            aph.putProperties(path, props);
        }
    }
    private void storeConfig(LaunchConfiguration config, EditableProperties props) {
        props.setProperty(ProjectProperties.MAIN_CLASS, config.getMainType());
        // XXX normally APPLICATION_ARGS and RUN_VM_ARGS are put in private properties; does it matter here?
        if (config.getProgramArguments() != null) {
            props.setProperty(ProjectProperties.APPLICATION_ARGS, config.getProgramArguments());
        }
        if (config.getVmArguments() != null) {
            props.setProperty(ProjectProperties.RUN_JVM_ARGS, config.getVmArguments());
        }
    }

    /**
     * Create a new application manifest file with minimal initial contents.
     * @param dir the directory to create it in
     * @throws IOException in case of problems
     */
    private static void createManifest(final FileObject dir, final String manifestFile) throws IOException {

        FileObject manifest = dir.createData(manifestFile);
        FileLock lock = manifest.lock();
        try {
            OutputStream os = manifest.getOutputStream(lock);
            try {
                PrintWriter pw = new PrintWriter(os);
                pw.println("Manifest-Version: 1.0"); // NOI18N
                pw.println("X-COMMENT: Main-Class will be added automatically by build"); // NOI18N
                pw.println(); // safest to end in \n\n due to JRE parsing bug
                pw.flush();
            } finally {
                os.close();
            }
        } finally {
            lock.releaseLock();
        }

    }


}
