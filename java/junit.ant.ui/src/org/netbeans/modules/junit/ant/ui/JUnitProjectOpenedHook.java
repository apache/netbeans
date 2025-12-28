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
package org.netbeans.modules.junit.ant.ui;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.Collections;
import java.util.Properties;
import java.util.WeakHashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RunnableFuture;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.api.java.classpath.ClassPath;
import static org.netbeans.api.java.classpath.ClassPath.COMPILE;
import org.netbeans.api.java.project.classpath.ProjectClassPathModifier;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import static org.netbeans.modules.java.testrunner.JavaUtils.PROP_JUNIT_SELECTED_VERSION;
import org.netbeans.modules.junit.api.JUnitUtils;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.project.LookupProvider;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.ui.ProjectOpenedHook;
import org.netbeans.spi.project.ui.ProjectProblemResolver;
import org.netbeans.spi.project.ui.ProjectProblemsProvider;
import org.netbeans.spi.project.ui.ProjectProblemsProvider.ProjectProblem;
import org.netbeans.spi.project.ui.ProjectProblemsProvider.Result;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Theofanis Oikonomou
 */
@LookupProvider.Registration(projectType = {
    "org-netbeans-modules-j2ee-clientproject",
    "org-netbeans-modules-j2ee-ejbjarproject",
    "org-netbeans-modules-java-j2seproject",
    "org-netbeans-modules-web-project"
})
public class JUnitProjectOpenedHook implements LookupProvider {

    //full name of a file specific for the JUnit 3.8.x library
    private static final String JUNIT3_SPECIFIC = "junit/awtui/TestRunner.class"; //NOI18N
    // full name of a file specific for the JUnit 4.x library
    private static final String JUNIT4_SPECIFIC = "org/junit/Test.class"; //NOI18N
    // full name of a file specific for the Hamcrest library
    private static final String HAMCREST_SPECIFIC = "org/hamcrest/Matcher.class"; //NOI18N
    //constant for action.getValue() holding the text to show to users..
    public static final String ACT_START_MESSAGE = "START_MESSAGE";
    private static final String MISSING_JUNIT_BINARIES = "MISSINGJUNITBINARIES"; //NOI18N
    private static final String PROP_JAVAC_TEST_CLASSPATH = "javac.test.classpath"; //NOI18N
    private static final RequestProcessor RP = new RequestProcessor(JUnitProjectProblemsProvider.class);
    private final WeakHashMap<Project, WeakReference<JUnitProjectProblemsProvider>> map = new WeakHashMap<Project, WeakReference<JUnitProjectProblemsProvider>>();

    @NbBundle.Messages({"Error_display_name_junit=JUnit 3.8.2 binaries missing.",
        "Error_description_junit=Beginning with Netbeans 8.1, JUnit 3.8.2 binaries were removed.\n\n"
        + "Click the resolve button to automatically update the test dependency to JUnit 4.x library definition. "
        + "JUnit 4.x has a compatibility mode in order to handle JUnit 3.x testcases.\n\n"
        + "Alternatively download junit-3.8.2.jar and add it manually as your project's test dependency.",
        "Error_display_name_junit4=Hamcrest binaries missing",
        "Error_description_junit4=JUnit 4.13.2 does not bundle hamcrest matchers library.\n\n"
        + "Click the resolve button to automatically add hamcrest as your project's test dependency."})
    @Override
    public Lookup createAdditionalLookup(final Lookup lookup) {

        final Project p = lookup.lookup(Project.class);
        map.put(p, new WeakReference<JUnitProjectProblemsProvider>(new JUnitProjectProblemsProvider()));

        ProjectOpenedHook projectOpenedHook = new ProjectOpenedHook() {
            @Override
            protected void projectOpened() {
                final ClassPath classPath = getTestClassPath(p);
                if (classPath != null) {
                    if (classPath.findResource(JUNIT3_SPECIFIC) != null || 
                            classPath.toString().contains("${libs.junit.classpath}")) {
                        getJUnitProjectProblemsProvider(p).setProblem(ProjectProblem.createError(
                            Bundle.Error_display_name_junit(), Bundle.Error_description_junit(),
                            new JUnitProblemResolver(new UpdateToJUnit4Action(p, true), MISSING_JUNIT_BINARIES)));
                    }
                    if (classPath.findResource(JUNIT4_SPECIFIC) != null && classPath.findResource(HAMCREST_SPECIFIC) == null) {
                        getJUnitProjectProblemsProvider(p).setProblem(ProjectProblem.createError(
                            Bundle.Error_display_name_junit4(), Bundle.Error_description_junit4(),
                            new JUnitProblemResolver(new UpdateToJUnit4Action(p, false), MISSING_JUNIT_BINARIES)));
                    }
                }
            }

            @Override
            protected void projectClosed() {
                getJUnitProjectProblemsProvider(p).setProblem(null);
            }
        };
        return Lookups.fixed(projectOpenedHook, getJUnitProjectProblemsProvider(p));
    }
    
    private JUnitProjectProblemsProvider getJUnitProjectProblemsProvider(Project p) {
        WeakReference<JUnitProjectProblemsProvider> problemsProviderRef = map.get(p);
        if(problemsProviderRef == null || problemsProviderRef.get() == null) {
            problemsProviderRef = new WeakReference<JUnitProjectProblemsProvider>(new JUnitProjectProblemsProvider());
            map.put(p, problemsProviderRef);
        }
        return problemsProviderRef.get();
    }

    /**
     * Finds classpath used for compilation of tests.
     *
     * @param project project whose classpath should be found
     * @return test classpath of the given project, or {@literal null} if it
     * could not be determined
     * @throws java.lang.IllegalStateException if no test folders were found in
     * the project
     */
    private static ClassPath getTestClassPath(final Project project) throws IllegalStateException {
        assert project != null;

        final Collection<FileObject> testFolders = JUnitUtils.getTestFolders(project);

        final ClassPathProvider cpProvider = project.getLookup().lookup(ClassPathProvider.class);
        if (cpProvider == null) {
            return null;
        }

        for (FileObject testRoot : testFolders) {
            ClassPath testClassPath = cpProvider.findClassPath(testRoot, COMPILE);
            if (testClassPath != null) {
                return testClassPath;
            }
        }
        return null;
    }

    private class JUnitProjectProblemsProvider implements ProjectProblemsProvider {

        private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
        private ProjectProblemsProvider.ProjectProblem pp;

        public JUnitProjectProblemsProvider() {
        }

        synchronized void setProblem(ProjectProblemsProvider.ProjectProblem pp) {
            this.pp = pp;
            if (pp == null && this.pp == null) {
                return; //ignore this case, dont' fire change..
            }
            pcs.firePropertyChange(ProjectProblemsProvider.PROP_PROBLEMS, null, null);
        }

        @Override
        public void addPropertyChangeListener(PropertyChangeListener listener) {
            pcs.addPropertyChangeListener(listener);
        }

        @Override
        public void removePropertyChangeListener(PropertyChangeListener listener) {
            pcs.removePropertyChangeListener(listener);
        }

        @Override
        public synchronized Collection<? extends ProjectProblemsProvider.ProjectProblem> getProblems() {
            if (pp != null) {
                return Collections.singleton(pp);
            } else {
                return Collections.emptyList();
            }
        }
    }

    private class JUnitProblemResolver implements ProjectProblemResolver {

        private final Action action;
        private final String id;

        public JUnitProblemResolver(Action correctiveAction, String id) {
            this.action = correctiveAction;
            this.id = id;
        }
        
        @Override
        public java.util.concurrent.Future<ProjectProblemsProvider.Result> resolve() {
            ProjectProblemsProvider.Result res;
            if (action != null) {
                action.actionPerformed(null);
                String text = (String) action.getValue(ACT_START_MESSAGE);
                if (text != null) {
                    res = ProjectProblemsProvider.Result.create(ProjectProblemsProvider.Status.RESOLVED, text);
                } else {
                    res = ProjectProblemsProvider.Result.create(ProjectProblemsProvider.Status.RESOLVED);
                }
            } else {
                res = ProjectProblemsProvider.Result.create(ProjectProblemsProvider.Status.UNRESOLVED, "No resolution for the problem");
            }
            RunnableFuture<ProjectProblemsProvider.Result> f = new FutureTask<>(new Runnable() {
                @Override
                public void run() {
                }
            }, res);
            f.run();
            return f;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 11 * hash + (this.id != null ? this.id.hashCode() : 0);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final JUnitProblemResolver other = (JUnitProblemResolver) obj;
            return !((this.id == null) ? (other.id != null) : !this.id.equals(other.id));
        }
    }

    private class UpdateToJUnit4Action extends AbstractAction {

        private final Project p;
        private final boolean isJUnit3specific;

        @NbBundle.Messages({"Action_display_name_junit=Update to JUnit 4.x",
            "Action_start_msg_junit=Test dependency was updated to JUnit 4.x library.",
            "Action_display_name_junit4=Add Hamcrest library",
            "Action_start_msg_junit4=Hamcrest library was added as test dependency."})
        UpdateToJUnit4Action(Project project, boolean isJUnit3specific) {
            putValue(Action.NAME, isJUnit3specific ? Bundle.Action_display_name_junit() : Bundle.Action_display_name_junit4());
            putValue(ACT_START_MESSAGE, isJUnit3specific ? Bundle.Action_start_msg_junit(): Bundle.Action_start_msg_junit4());
            p = project;
            this.isJUnit3specific = isJUnit3specific;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            FileObject buildXML = p.getProjectDirectory().getFileObject("build.xml"); //NOI18N
            Library junit4lib = LibraryManager.getDefault().getLibrary("junit_4"); //NOI18N
            Library hamcrestlib = LibraryManager.getDefault().getLibrary("hamcrest"); //NOI18N
            if (buildXML != null) {
                try {
                    updateProjectProperties(p);
                    Collection<FileObject> testFolders = JUnitUtils.getTestFolders(p);
                    ProjectClassPathModifier.addLibraries(new Library[]{junit4lib, hamcrestlib}, testFolders.toArray(new FileObject[0])[0], ClassPath.COMPILE);
                    getJUnitProjectProblemsProvider(p).setProblem(null);
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                } catch (UnsupportedOperationException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }

        private void updateProjectProperties(Project project) throws IOException {
            final FileObject projectDir = project.getProjectDirectory();
            ProjectManager.mutex().postWriteRequest(new Runnable() {
                @Override
                public void run() {
                    try {
                        FileObject projectProperties = FileUtil.createData(projectDir, AntProjectHelper.PROJECT_PROPERTIES_PATH);
                        Properties props = getProjectProperties(projectDir);

                        if (isJUnit3specific) {
                            String testClasspath = props.getProperty(PROP_JAVAC_TEST_CLASSPATH);
                            props.put(PROP_JAVAC_TEST_CLASSPATH, testClasspath.replace("${libs.junit.classpath}", ""));
                        }
                        props.put(PROP_JUNIT_SELECTED_VERSION, isJUnit3specific ? "3" : "4");
                        OutputStream propertiesOS = projectProperties.getOutputStream();
                        props.store(propertiesOS, null);
                        propertiesOS.close();
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            });
        }

        private Properties getProjectProperties(FileObject projectDir) throws IOException {
            FileObject projectProperties = FileUtil.createData(projectDir, AntProjectHelper.PROJECT_PROPERTIES_PATH);
            InputStream propertiesIS = projectProperties.getInputStream();
            Properties props = new Properties();
            props.load(propertiesIS);
            propertiesIS.close();
            return props;
        }
    }
}
