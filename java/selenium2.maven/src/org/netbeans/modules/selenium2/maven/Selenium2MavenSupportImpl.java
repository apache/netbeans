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
package org.netbeans.modules.selenium2.maven;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.maven.project.MavenProject;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.maven.api.ModelUtils;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.api.execute.RunConfig;
import org.netbeans.modules.maven.api.execute.RunUtils;
import org.netbeans.modules.maven.model.ModelOperation;
import org.netbeans.modules.maven.model.Utilities;
import org.netbeans.modules.maven.model.pom.Dependency;
import org.netbeans.modules.maven.model.pom.Exclusion;
import org.netbeans.modules.maven.model.pom.POMModel;
import org.netbeans.modules.maven.model.pom.POMModelFactory;
import org.netbeans.modules.maven.spi.actions.AbstractMavenActionsProvider;
import org.netbeans.modules.selenium2.java.api.Utils;
import org.netbeans.modules.selenium2.spi.Selenium2SupportImpl;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.spi.java.project.support.ui.templates.JavaTemplates;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.execution.ExecutorTask;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Theofanis Oikonomou
 */
@ServiceProvider(service = Selenium2SupportImpl.class)
public class Selenium2MavenSupportImpl extends Selenium2SupportImpl {

    private static final String JUNIT_GROUP_ID = "junit";      //NOI18N
    private static final String JUNIT_ARTIFACT_ID = "junit";      //NOI18N
    private static final String SELENIUM_GROUP_ID = "org.seleniumhq.selenium";      //NOI18N
    private static final String SELENIUM_ARTIFACT_ID = "selenium-java";   //NOI18N
    private static final String SELENIUM_REMOTE_DRIVER_ARTIFACT_ID = "selenium-remote-driver";   //NOI18N
    private static final String OPERA_GROUP_ID = "com.opera";      //NOI18N
    private static final String OPERA_ARTIFACT_ID = "operadriver";   //NOI18N

    @Override
    public boolean isSupportActive(Project p) {
        return isMavenProject(p);
    }

    @Override
    public void configureProject(FileObject targetFolder) {
        final Project p = FileOwnerQuery.getOwner(targetFolder);
        if (p == null || isProjectReady(p)) {
            return;
        }

        ModelOperation<POMModel> operation = new ModelOperation<POMModel>() {
            public @Override
            void performOperation(POMModel model) {
                if (!ModelUtils.hasModelDependency(model, SELENIUM_GROUP_ID, SELENIUM_ARTIFACT_ID)) {
                    Dependency dep = ModelUtils.checkModelDependency(model, SELENIUM_GROUP_ID, SELENIUM_ARTIFACT_ID, true);
                    dep.setScope("test"); //NOI18N
                    dep.setVersion("2.44.0"); //NOI18N
                    dep = ModelUtils.checkModelDependency(model, OPERA_GROUP_ID, OPERA_ARTIFACT_ID, true);
                    dep.setScope("test"); //NOI18N
                    dep.setVersion("1.5"); //NOI18N
                    Exclusion exclusion = model.getFactory().createExclusion();
                    exclusion.setGroupId(SELENIUM_GROUP_ID);
                    exclusion.setArtifactId(SELENIUM_REMOTE_DRIVER_ARTIFACT_ID);
                    dep.addExclusion(exclusion);
                }
                if (!ModelUtils.hasModelDependency(model, JUNIT_GROUP_ID, JUNIT_ARTIFACT_ID)) {
                    Dependency dep = ModelUtils.checkModelDependency(model, JUNIT_GROUP_ID, JUNIT_ARTIFACT_ID, true);
                    dep.setScope("test"); //NOI18N
                    dep.setVersion("4.11"); //NOI18N
                }
            }
        };
        Utilities.performPOMModelOperations(getPomFile(p), Collections.singletonList(operation));
        RequestProcessor RP = new RequestProcessor("Configure Selenium 2.0 project task", 1, true); //NOI18N
        RP.post(new Runnable() {
            @Override
            public void run() {
                p.getLookup().lookup(NbMavenProject.class).downloadDependencyAndJavadocSource(true);
            }
        });
    }

    @Override
    public WizardDescriptor.Panel createTargetChooserPanel(WizardDescriptor wiz) {
        // Ask for Java folders
        Project project = Templates.getProject(wiz);
        Sources sources = ProjectUtils.getSources(project);
        SourceGroup[] groups = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        assert groups != null : "Cannot return null from Sources.getSourceGroups: " + sources; //NOI18N
        if (groups.length == 0) {
            groups = sources.getSourceGroups(Sources.TYPE_GENERIC);
            return Templates.buildSimpleTargetChooser(project, groups).create();
        } else {
            FileObject testDir = getTestRoot(project);
            // fetch source groups again in case test root was just created
            groups = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
            for (SourceGroup selGroup : groups) {
                if (selGroup.getRootFolder().equals(testDir)){
                    return JavaTemplates.createPackageChooser(project, new SourceGroup[]{selGroup});
                }
            }
            return JavaTemplates.createPackageChooser(project, groups);
        }
    }

    @Override
    public boolean isSupportEnabled(FileObject[] activatedFOs) {
        return Utils.isSupportEnabled(NbMavenProject.class, activatedFOs);
    }
    
    private FileObject getTestRoot(Project project) {
        NbMavenProject nbProject = project.getLookup().lookup(NbMavenProject.class);
        MavenProject mvp = nbProject.getMavenProject();
        @SuppressWarnings("unchecked")
        List<String> testRoots = mvp.getTestCompileSourceRoots();
        if (testRoots.isEmpty()) {
            return null;
        }
        File testRoot = new File(testRoots.get(0));
        FileObject result = null;
        try {
            result = FileUtil.createFolder(testRoot);
        } catch (IOException ex) {
            Logger.getLogger(Selenium2MavenSupportImpl.class.getName()).log(Level.SEVERE, "Impossible to create test root file object", ex); //NOI18N
        }
        return result;
    }

    private boolean isProjectReady(Project project) {
        POMModel model = getPOMModel(project);
        return ModelUtils.hasModelDependency(model, SELENIUM_GROUP_ID, SELENIUM_ARTIFACT_ID);
    }

    private boolean isMavenProject(Project project) {
        return project.getLookup().lookup(NbMavenProject.class) != null;
    }

    private POMModel getPOMModel(Project project) {
        FileObject pom = getPomFile(project);
        ModelSource source = Utilities.createModelSource(pom);
        return POMModelFactory.getDefault().getModel(source);
    }

    private FileObject getPomFile(Project project) {
        return project.getProjectDirectory().getFileObject("pom.xml"); //NOI18N
    }

    @Override
    public List<Object> getTestSourceRoots(Collection<SourceGroup> createdSourceRoots, FileObject refFileObject) {
        return Arrays.asList(Utils.getTestSourceRoots(createdSourceRoots, refFileObject));
    }

    @Override
    public String[] getSourceAndTestClassNames(FileObject fo, boolean isTestNG, boolean isSelenium) {
        return Utils.getSourceAndTestClassNames(fo, isTestNG, isSelenium);
    }

    @Override
    public void runTests(FileObject[] activatedFOs, boolean isSelenium) {
        Project project = FileOwnerQuery.getOwner(activatedFOs[0]);
        if(project == null) {
            return;
        }
        FileObject testDir = getTestRoot(project);
        RunConfig rc = new Selenium2ActionsProvider().createConfigForDefaultAction("selenium2.test", project, Lookups.singleton(testDir));
        String testParameter = getTestParameter(activatedFOs, project, testDir);
        
        rc.setProperty("test", testParameter);
        ExecutorTask executeMaven = RunUtils.executeMaven(rc);
    }
    
    private String getTestParameter(FileObject[] activatedFOs, Project project, FileObject testDir) {
        if(activatedFOs.length == 1 && project.getProjectDirectory().equals(activatedFOs[0])) {
            return Utils.RUN_SELENIUM_TESTS_REGEXP;
        }
        String testParameter = "";
        SourceGroup[] sourceGroups = ProjectUtils.getSources(project).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        SourceGroup sourceGroup = findGroup(sourceGroups, activatedFOs);
        if (sourceGroup != null) {
            for (FileObject fo : activatedFOs) {
                String relativePath = FileUtil.getRelativePath(sourceGroup.getRootFolder(), fo);
                if (relativePath == null) {
                    continue;
                }

                if (!testParameter.isEmpty()) {
                    testParameter = testParameter.concat(",");
                }
                testParameter = testParameter + relativePath + "/" + Utils.RUN_SELENIUM_TESTS_REGEXP;
            }
        }
        return testParameter;
    }
    
    /** Finds the one source group, if any, which contains all of the listed files. */
    private static @CheckForNull SourceGroup findGroup(SourceGroup[] groups, FileObject[] files) {
        SourceGroup selected = null;
        for (FileObject file : files) {
            for (SourceGroup group : groups) {
                FileObject root = group.getRootFolder();
                if (file == root || FileUtil.isParentOf(root, file)) { // or group.contains(file)?
                    if (selected == null) {
                        selected = group;
                    } else if (selected != group) {
                        return null;
                    }
                }
            }
        }
        return selected;
    }

    @Override
    public String getTemplateID() {
        return "Templates/SeleniumTests/SeleneseIT.java";
    }
    
    private class Selenium2ActionsProvider extends AbstractMavenActionsProvider {
        
        @StaticResource private static final String MAPPINGS = "org/netbeans/modules/selenium2/maven/selenium2ActionMappings.xml";

        public Selenium2ActionsProvider() {
        }

        @Override
        public boolean isActionEnable(String action, Project project, Lookup lookup) {
            if (action.startsWith("selenium2.")) { //NOI18N
                return true;
            }
            return super.isActionEnable(action, project, lookup);
        }

        @Override
        protected InputStream getActionDefinitionStream() {
            return Selenium2ActionsProvider.class.getClassLoader().getResourceAsStream(MAPPINGS);
        }

    }

}
