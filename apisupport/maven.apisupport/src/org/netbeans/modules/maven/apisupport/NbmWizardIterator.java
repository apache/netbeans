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

package org.netbeans.modules.maven.apisupport;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.apache.maven.project.MavenProject;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.validation.adapters.WizardDescriptorAdapter;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.api.archetype.Archetype;
import org.netbeans.modules.maven.api.archetype.ArchetypeWizards;
import org.netbeans.modules.maven.api.archetype.ProjectInfo;
import org.netbeans.modules.maven.model.ModelOperation;
import org.netbeans.modules.maven.model.Utilities;
import org.netbeans.modules.maven.model.pom.POMModel;
import org.netbeans.modules.maven.model.pom.Repository;
import org.netbeans.validation.api.ui.ValidationGroup;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.netbeans.api.templates.TemplateRegistration;
import org.openide.util.NbBundle.Messages;
import static org.netbeans.modules.maven.apisupport.Bundle.*;
import static org.netbeans.modules.maven.apisupport.MavenNbModuleImpl.APACHE_SNAPSHOT_REPO_ID;
import org.netbeans.modules.maven.embedder.EmbedderFactory;
import org.netbeans.spi.project.ui.support.CommonProjectActions;

public class NbmWizardIterator implements WizardDescriptor.BackgroundInstantiatingIterator<WizardDescriptor> {

    public static final String NBM_ARTIFACTID = "nbm_artifactId";
    
    static final Archetype NB_MODULE_ARCH, NB_APP_ARCH;
    public static final String SNAPSHOT_VERSION = "dev-SNAPSHOT";
    static {
        NB_MODULE_ARCH = new Archetype();
        NB_MODULE_ARCH.setGroupId("org.apache.netbeans.archetypes"); //NOI18N
        NB_MODULE_ARCH.setVersion("1.16"); //NOI18N
        NB_MODULE_ARCH.setArtifactId("nbm-archetype"); //NOI18N

        NB_APP_ARCH = new Archetype();
        NB_APP_ARCH.setGroupId("org.apache.netbeans.archetypes"); //NOI18N
        NB_APP_ARCH.setVersion("1.21"); //NOI18N
        NB_APP_ARCH.setArtifactId("netbeans-platform-app-archetype"); //NOI18N

    }

    static final String OSGIDEPENDENCIES = "osgi.dependencies";
    static final String NB_VERSION = "nb.version"; // NOI18N

    private int index;
    private WizardDescriptor.Panel<WizardDescriptor>[] panels;
    private WizardDescriptor wiz;
    private final Archetype archetype;
    private final String title;

    private NbmWizardIterator(Archetype archetype, String title) {
        this.archetype = archetype;
        this.title = title;
    }

    @TemplateRegistration(folder=ArchetypeWizards.TEMPLATE_FOLDER, position=400, displayName="#template.module", iconBase="org/netbeans/modules/maven/apisupport/nbmicon.png", description="NbModuleDescription.html")
    @Messages("template.module=NetBeans Module")
    public static NbmWizardIterator createNbModuleIterator() {
        return new NbmWizardIterator(NB_MODULE_ARCH, template_module());
    }

    @TemplateRegistration(folder=ArchetypeWizards.TEMPLATE_FOLDER, position=450, displayName="#template.app", iconBase="org/netbeans/modules/maven/apisupport/suiteicon.png", description="NbAppDescription.html")
    @Messages("template.app=NetBeans Application")
    public static NbmWizardIterator createNbAppIterator() {
        return new NbmWizardIterator(NB_APP_ARCH, template_app());
    }

    @SuppressWarnings({"unchecked", "rawtypes"}) // XXX until rewrite panel storage
    private WizardDescriptor.Panel<WizardDescriptor>[] createPanels(ValidationGroup enabledVG, ValidationGroup errorMsgVG) {
            return new WizardDescriptor.Panel[] {
                ArchetypeWizards.basicWizardPanel(errorMsgVG, false, archetype),
                new NbmWizardPanel(enabledVG, errorMsgVG, archetype)
            };
    }
    
    @Messages("LBL_CreateProjectStep2=Name and Location")
    private String[] createSteps() {
            return new String[] {
                LBL_CreateProjectStep2(),
                LBL_CreateProjectStepNbm()
            };
    }
    
    @Override
    public Set<FileObject> instantiate() throws IOException {
        ProjectInfo vi = new ProjectInfo((String) wiz.getProperty("groupId"), (String) wiz.getProperty("artifactId"), (String) wiz.getProperty("version"), (String) wiz.getProperty("package")); //NOI18N

        ArchetypeWizards.logUsage(archetype.getGroupId(), archetype.getArtifactId(), archetype.getVersion());

            String nbm_artifactId = (String) wiz.getProperty(NBM_ARTIFACTID);
            File projFile = FileUtil.normalizeFile((File) wiz.getProperty(CommonProjectActions.PROJECT_PARENT_FOLDER)); // NOI18N
            String version = (String) wiz.getProperty(NB_VERSION);
            assert version != null;
            Map<String,String> additional = Collections.singletonMap("netbeansVersion", version); // NOI18N
            
            if (archetype == NB_MODULE_ARCH) {
                NBMNativeMWI.instantiate(vi, projFile, version, Boolean.TRUE.equals(wiz.getProperty(OSGIDEPENDENCIES)), null);
                
            } else {
            
            ArchetypeWizards.createFromArchetype(projFile, vi, archetype, additional, true);
            List<ModelOperation<POMModel>> opers = new ArrayList<ModelOperation<POMModel>>();
            if (Boolean.TRUE.equals(wiz.getProperty(OSGIDEPENDENCIES))) {
                //now we have the nbm-archetype (or the netbeans platform one).
                ModelOperation<POMModel> osgi = addNbmPluginOsgiParameter(projFile);
                if (osgi != null) {
                    opers.add(osgi);
                }
            }
            if (SNAPSHOT_VERSION.equals(version)) { // NOI18N
                opers.add(addSnapshotRepo());
            }
            if (!opers.isEmpty()) {
                FileObject prjDir = FileUtil.toFileObject(projFile);
                if (prjDir != null) {
                    FileObject pom = prjDir.getFileObject("pom.xml");
                    if (pom != null) {
                        Project prj = ProjectManager.getDefault().findProject(prjDir);
                        if (prj != null) {
                           Utilities.performPOMModelOperations(pom, opers);
                        }
                    }
                }
            }
            
            if (nbm_artifactId != null && projFile.exists()) {
                //NOW we have the nbm-Platform or nbm suite template
                //create the nbm module
                
                //a bit of a hack, the archetype + modified parent project has not reloaded yet properly
                Project p = ProjectManager.getDefault().findProject(FileUtil.toFileObject(projFile));
                MavenProject mp = p.getLookup().lookup(NbMavenProject.class).loadAlternateMavenProject(EmbedderFactory.getProjectEmbedder(), Collections.<String>emptyList(), null);
                
                ProjectInfo nbm = new ProjectInfo(vi.groupId, nbm_artifactId, vi.version, vi.packageName);
                File nbm_folder = FileUtil.normalizeFile(new File(projFile, nbm_artifactId));
                NBMNativeMWI.instantiate(nbm, nbm_folder, version, Boolean.TRUE.equals(wiz.getProperty(OSGIDEPENDENCIES)), mp);
                if (archetype == NB_APP_ARCH) {
                    File appDir = new File(projFile, "application"); //NOI18N
                    addModuleToApplication(appDir, new ProjectInfo("${project.groupId}", nbm.artifactId, "${project.version}", nbm.packageName), null); // NOI18N
                }
            }
            }
            
            //TODO what is this supposed to do?
            Set<FileObject> projects = ArchetypeWizards.openProjects(projFile, new File(projFile, "application"));
            for (FileObject project : projects) {
                Project prj = ProjectManager.getDefault().findProject(project);
                if (prj == null) {
                    continue;
                }
                NbMavenProject mprj = prj.getLookup().lookup(NbMavenProject.class);
                if (mprj == null) {
                    continue;
                }
            }
            
            return projects;
    }
    
    @Override
    public void initialize(WizardDescriptor wiz) {
        index = 0;
        ValidationGroup enabledVG = ValidationGroup.create(new WizardDescriptorAdapter(wiz, WizardDescriptorAdapter.Type.VALID));
        ValidationGroup errorMsgVG = ValidationGroup.create(new WizardDescriptorAdapter(wiz, WizardDescriptorAdapter.Type.MESSAGE));
        enabledVG.addItem(errorMsgVG, false);
        panels = createPanels(enabledVG, errorMsgVG);
        this.wiz = wiz;
        wiz.putProperty ("NewProjectWizard_Title", title); // NOI18N
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
                jc.putClientProperty("WizardPanel_contentSelectedIndex", i); //NOI18N
                // Step name (actually the whole list for reference).
                jc.putClientProperty("WizardPanel_contentData", steps); //NOI18N
            }
        }
    }
    
    @Override
    public void uninitialize(WizardDescriptor wiz) {
        panels = null;
    }
    
    @Override
    @Messages("NameFormat={0} of {1}")
    public String name() {
        return NameFormat(index + 1, panels.length);
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
    public final void addChangeListener(ChangeListener l) {}

    @Override
    public final void removeChangeListener(ChangeListener l) {}

    private static ModelOperation<POMModel> addNbmPluginOsgiParameter(File projFile) throws IOException {
        FileObject prjDir = FileUtil.toFileObject(projFile);
        if (prjDir != null) {
            FileObject pom = prjDir.getFileObject("pom.xml");
            if (pom != null) {
                Project prj = ProjectManager.getDefault().findProject(prjDir);
                if (prj == null) {
                    return null; // invalid? #184466
                }
                NbMavenProject mav = prj.getLookup().lookup(NbMavenProject.class);
                return  new AddOSGiParamToNbmPluginConfiguration(true, mav.getMavenProject());
            }
        }
        //TODO report inability to create? or if the file doesn't exist, it was already
        //reported?
        return null;
   }

    private static ModelOperation<POMModel> addSnapshotRepo() throws IOException {
        return new ModelOperation<POMModel>() {
                    public @Override void performOperation(POMModel model) {
                        Repository repo = model.getFactory().createRepository();
                        repo.setId(APACHE_SNAPSHOT_REPO_ID); // NOI18N
                        repo.setName("Apache Development Snapshot Repository"); // NOI18N
                        /* Is the following necessary?
                        RepositoryPolicy policy = model.getFactory().createSnapshotRepositoryPolicy();
                        policy.setEnabled(true);
                        repo.setSnapshots(policy);
                         */
                        repo.setUrl("https://repository.apache.org/content/repositories/snapshots/"); // NOI18N
                        model.getProject().addRepository(repo);
                    }
                };
   }

    private static void addModuleToApplication(File file, ProjectInfo nbm, Object object) {
        FileObject appPrjFO = FileUtil.toFileObject(FileUtil.normalizeFile(file));
        if (appPrjFO == null) {
            return;
        }
        List<ModelOperation<POMModel>> operations = new ArrayList<ModelOperation<POMModel>>();
        operations.add(ArchetypeWizards.addDependencyOperation(nbm, null));
        Utilities.performPOMModelOperations(appPrjFO.getFileObject("pom.xml"), operations);
    }

}
