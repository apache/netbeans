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
package org.netbeans.modules.testng.maven;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.maven.project.MavenProject;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.java.testrunner.JavaUtils;
import org.netbeans.modules.maven.api.ModelUtils;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.api.execute.RunConfig;
import org.netbeans.modules.maven.api.execute.RunUtils;
import org.netbeans.modules.maven.model.ModelOperation;
import org.netbeans.modules.maven.model.Utilities;
import org.netbeans.modules.maven.model.pom.Dependency;
import org.netbeans.modules.maven.model.pom.DependencyManagement;
import org.netbeans.modules.maven.model.pom.POMModel;
import org.netbeans.modules.testng.api.TestNGSupport.Action;
import org.netbeans.modules.testng.spi.TestConfig;
import org.netbeans.modules.testng.spi.TestNGSupportImplementation;
import org.netbeans.modules.testng.spi.XMLSuiteSupport;
import org.openide.execution.ExecutorTask;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author lukas
 */
@ServiceProvider(service=TestNGSupportImplementation.class)
public class MavenTestNGSupport extends TestNGSupportImplementation {
    
    private static final Logger LOGGER = Logger.getLogger(MavenTestNGSupport.class.getName());
    private static final Set<Action> SUPPORTED_ACTIONS;

    static {
        Set<Action> s = new HashSet<Action>();
//        s.add(Action.CREATE_TEST);
//        s.add(Action.RUN_FAILED);
//        s.add(Action.RUN_TESTMETHOD);
        s.add(Action.RUN_TESTSUITE);
        s.add(Action.DEBUG_TESTSUITE);
        SUPPORTED_ACTIONS = Collections.unmodifiableSet(s);
    }

    @Override
    public boolean isSupportEnabled(FileObject[] activatedFOs) {
        return JavaUtils.isSupportEnabled(NbMavenProject.class, activatedFOs);
    }

    public boolean isActionSupported(Action action,Project p) {
        return p != null && p.getLookup().lookup(NbMavenProject.class) != null && SUPPORTED_ACTIONS.contains(action);
    }

    @NbBundle.Messages("remove_junit3_when_adding_testng=Removing JUnit 3.x dependency as TestNG has transitive dependency to JUnit 4.x.")
    public void configureProject(FileObject createdFile) {
        ClassPath cp = ClassPath.getClassPath(createdFile, ClassPath.COMPILE);
        FileObject ng = cp.findResource("org.testng.annotations.Test"); //NOI18N
        if (ng == null) {
            final Project p = FileOwnerQuery.getOwner(createdFile);
            FileObject pom = p.getProjectDirectory().getFileObject("pom.xml"); //NOI18N
            ModelOperation<POMModel> operation = new ModelOperation<POMModel>() {
                public @Override
                void performOperation(POMModel model) {
                    String groupID = "org.testng"; //NOI18N
                    String artifactID = "testng"; //NOI18N
                    if (!hasEffectiveDependency(groupID, artifactID, p.getLookup().lookup(NbMavenProject.class))) {
                        fixJUnitDependency(model, p.getLookup().lookup(NbMavenProject.class));
                        Dependency dep = ModelUtils.checkModelDependency(model, groupID, artifactID, true);
                        dep.setVersion("6.8.1"); //NOI18N
                        dep.setScope("test"); //NOI18N
                    }
                }
            };
            Utilities.performPOMModelOperations(pom, Collections.singletonList(operation));
            RequestProcessor RP = new RequestProcessor("Configure TestNG project task", 1, true); //NOI18N
            RP.post(new Runnable() {

                public void run() {
                    p.getLookup().lookup(NbMavenProject.class).downloadDependencyAndJavadocSource(true);
                }
            });
        }
    }
    
    private boolean hasEffectiveDependency(String groupId, String artifactId, NbMavenProject prj) {
        MavenProject mp = prj.getMavenProject();
        List<org.apache.maven.model.Dependency> dl = new ArrayList<org.apache.maven.model.Dependency>();
        dl.addAll(mp.getDependencies());
        org.apache.maven.model.DependencyManagement dm = mp.getDependencyManagement();
        if (dm != null) {
            dl.addAll(dm.getDependencies());
        }
        for (org.apache.maven.model.Dependency d : dl) {
            if (groupId.equals(d.getGroupId()) && artifactId.equals(d.getArtifactId())) {
                return true;
            }
        }
        return false;
    }

    private void fixJUnitDependency(POMModel model, NbMavenProject prj) {
        String junitGroupID = "junit"; //NOI18N
        String junitArtifactID = "junit"; //NOI18N
        MavenProject mp = prj.getMavenProject();
        List<org.apache.maven.model.Dependency> dl = new ArrayList<org.apache.maven.model.Dependency>();
        dl.addAll(mp.getDependencies());
        dl.add(null); //null is the marker to separate managed from dependencies
        org.apache.maven.model.DependencyManagement dm = mp.getDependencyManagement();
        if (dm != null) {
            dl.addAll(dm.getDependencies());
        }
        boolean has3xJUnit = false;
        boolean hasJUnit = false;
        boolean hasManaged = false;
        boolean inManagedList = false;
        for (org.apache.maven.model.Dependency d : dl) {
            if (d == null) {
                inManagedList = true;
                continue;
            }

            if (junitGroupID.equals(d.getGroupId()) && junitArtifactID.equals(d.getArtifactId())) {
                hasJUnit = true;
                if (inManagedList) {
                    hasManaged = true;
                }
                if (d.getVersion() != null && d.getVersion().startsWith("3.")) {
                    has3xJUnit = true;
                }
            }
        }
        org.netbeans.modules.maven.model.pom.Project pomProject = model.getProject();
        DependencyManagement dependencyManagement = pomProject.getDependencyManagement();

        if (hasManaged) {
            if (dependencyManagement != null) {//1.a
                Dependency managed = dependencyManagement.findDependencyById(junitGroupID, junitArtifactID, null);
                if (managed != null) {
                    if (has3xJUnit) {//1.a.aa
                        LOGGER.log(Level.FINE, Bundle.remove_junit3_when_adding_testng());
                        dependencyManagement.removeDependency(managed);
                    } else {//1.a.bb.bbb
                        Dependency dep = pomProject.getModel().getFactory().createDependency();
                        dep.setGroupId(junitGroupID);
                        dep.setArtifactId(junitArtifactID);
                        dep.setVersion("4.10"); //NOI18N
                        pomProject.addDependency(dep);
                    }
                    return;
                }
            }
        }

        Dependency unmanaged = pomProject.findDependencyById(junitGroupID, junitArtifactID, null);
        if (unmanaged != null) {//2.a.aa
            if (unmanaged.getVersion() != null) {
                LOGGER.log(Level.FINE, Bundle.remove_junit3_when_adding_testng());
                pomProject.removeDependency(unmanaged);
            }
        } else {//2.a.ab
            //dependency defined somewhere in parent poms..
            if (has3xJUnit || hasManaged) {
                Dependency dep = pomProject.getModel().getFactory().createDependency();
                dep.setGroupId(junitGroupID);
                dep.setArtifactId(junitArtifactID);
                dep.setVersion("4.10"); //NOI18N
                pomProject.addDependency(dep);
            }
        }

    }

    public TestExecutor createExecutor(Project p) {
        return new MavenExecutor(p);
    }

    private static class MavenExecutor implements TestExecutor {

        private static final String failedConfPath = "target/surefire-reports/testng-failed.xml"; //NOI18N
        private static final String failedConfPath2 = "target/surefire-reports/testng-native-results/testng-failed.xml"; //NOI18N
        private static final String resultsPath = "target/surefire-reports/testng-native-results/testng-results.xml"; //NOI18N
        private Project p;

        public MavenExecutor(Project p) {
            this.p = p;
        }

        public boolean hasFailedTests() {
            return getFailedConfig() != null;
        }

        public void execute(Action action, TestConfig config) throws IOException {
            RunConfig rc;
            if (Action.DEBUG_TESTSUITE.equals(action)
                    || Action.DEBUG_TEST.equals(action)
                    || Action.DEBUG_TESTMETHOD.equals(action)) {
                rc = new TestNGActionsProvider().createConfigForDefaultAction("testng.debug", p, Lookups.singleton(config.getTest()));
            } else {
                rc = new TestNGActionsProvider().createConfigForDefaultAction("testng.test", p, Lookups.singleton(config.getTest()));
            }
//            MavenProject mp = rc.getMavenProject();
            rc.setProperty("netbeans.testng.action", "true"); //NOI18N
            if (config.doRerun()) {
                copy(getFailedConfig());
//                mp.addPlugin(createPluginDef(failedConfPath));
            } else {
                File f = null;
                if (Action.RUN_TESTSUITE.equals(action) || Action.DEBUG_TESTSUITE.equals(action)) {
                    f = FileUtil.toFile(config.getTest());
                } else {
                   f = XMLSuiteSupport.createSuiteforMethod(
                        new File(System.getProperty("java.io.tmpdir")), //NOI18N
                        ProjectUtils.getInformation(p).getDisplayName(),
                        config.getPackageName(),
                        config.getClassName(),
                        config.getMethodName());
                }
                f = FileUtil.normalizeFile(f);
                copy(FileUtil.toFileObject(f));
//                mp.addPlugin(createPluginDef(FileUtil.getRelativePath(p.getProjectDirectory(), FileUtil.toFileObject(f))));
            }
            ExecutorTask task = RunUtils.executeMaven(rc);

        }

        private FileObject getFailedConfig() {
            FileObject fo = p.getProjectDirectory();
            //XXX - should rather listen on a fileobject??
            FileUtil.refreshFor(FileUtil.toFile(fo));
            FileObject cfg = fo.getFileObject(failedConfPath);
            if (cfg == null || !cfg.isValid()) {
               cfg = fo.getFileObject(failedConfPath2);
            }
            return cfg;
        }

        private FileObject copy(FileObject source) throws IOException {
            FileObject fo = p.getProjectDirectory();
            //target/nb-private/tesng-suite.xml
            FileObject folder = FileUtil.createFolder(fo, "target/nb-private"); //NOI18N
            FileObject cfg = folder.getFileObject("testng-suite", "xml"); //NOI18N
            if (cfg != null) {
                cfg.delete();
            }
            return FileUtil.copyFile(source, folder, "testng-suite"); //NOI18N
        }

//        private Plugin createPluginDef(String testDesc) {
//            Plugin plugin = new Plugin();
//            plugin.setGroupId("org.apache.maven.plugins");
//            plugin.setArtifactId("maven-surefire-plugin");
//            plugin.setVersion("2.4.2");
//
//            Xpp3Dom dom = (Xpp3Dom) plugin.getConfiguration();
//            if (dom == null) {
//                dom = new Xpp3Dom("configuration");
//                plugin.setConfiguration(dom);
//            }
//
//            Xpp3Dom dom2 = dom.getChild("suiteXmlFiles");
//            if (dom2 == null) {
//                dom2 = new Xpp3Dom("suiteXmlFiles");
//                dom.addChild(dom2);
//            }
//            Xpp3Dom dom3 = dom2.getChild("suiteXmlFile");
//            if (dom3 == null) {
//                dom3 = new Xpp3Dom("suiteXmlFile");
//                dom3.setValue(testDesc);
//                dom2.addChild(dom3);
//            }
//            return plugin;
//        }
    }
}
