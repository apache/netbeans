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
package org.netbeans.modules.gradle.cos;

import org.netbeans.modules.gradle.api.execute.RunUtils;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.gradle.GradleProject;
import org.netbeans.modules.gradle.api.NbGradleProject;
import org.netbeans.modules.gradle.api.execute.ExecutionResultChecker;
import org.netbeans.modules.gradle.execute.PrerequisitesChecker;
import org.netbeans.modules.gradle.api.execute.RunConfig;
import static org.netbeans.modules.gradle.cos.Bundle.CosChecker_no_test_cos_details;
import static org.netbeans.modules.gradle.cos.Bundle.CosChecker_no_test_cos_title;

import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.netbeans.spi.project.ui.ProjectOpenedHook;
import org.openide.awt.Notification;
import org.openide.awt.NotificationDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author andres
 */
@ProjectServiceProvider(service = {PrerequisitesChecker.class}, projectType = "org-netbeans-modules-gradle")
public class CosChecker implements PrerequisitesChecker {

    static final String NB_COS = ".netbeans_automatic_build"; //NOI18N
    private static final Logger LOG = Logger.getLogger(CosChecker.class.getName());
    static final RequestProcessor RP = new RequestProcessor(CosChecker.class);
    // a maven property name denoting that the old, javarunner based execution is to be used.    
    public static final String USE_OLD_COS_EXECUTION = "use.old.cos.execution";
    public static final String NETBEANS_PROJECT_MAPPINGS = "netbeansProjectMappings";
    public static final String ENV_NETBEANS_PROJECT_MAPPINGS = "Env." + NETBEANS_PROJECT_MAPPINGS;
    public static final String MAVENEXTCLASSPATH = "maven.ext.class.path";

    @Override
    public boolean checkRunConfig(RunConfig config) {
        //deleting the timestamp before every action invokation means
        // we only can rely on Run via JavaRunner and via DeployOnSave
        // any other means of long term execution will not keep the CoS stamp around while running..
        // -->> ONLY DELETE FOR BUILD ACTION
        if (ActionProvider.COMMAND_BUILD.equals(config.getActionName())) {

        } else if (!ActionProvider.COMMAND_REBUILD.equals(config.getActionName())) {
            //now for all custom and non-build only related actions,
            //make sure we place the stamp files into all opened projects.
            Project[] opened = OpenProjects.getDefault().getOpenProjects();
            for (Project openprj : opened) {
                touchProject(openprj);
            }
            //new style Compile on Save
            if (!checkRunMainClass(config)) {
                return false;
            }
            checkRunTest(config);
        }
        return true;
    }

    /**
     * Returns {@code false} if execution should skip standard build phases.
     *
     * @param config run configuration
     * @param con execution context
     * @return {@code false} if execution should skip standard build phases,
     * {@code true} otherwise
     */
    private boolean checkRunMainClass(final RunConfig config) {
        final String actionName = config.getActionName();
        //compile on save stuff
        if (RunUtils.isCompileOnSaveEnabled(config.getProject())) {
            if (ActionProvider.COMMAND_RUN.equals(actionName)
                    || ActionProvider.COMMAND_DEBUG.equals(actionName)
                    || ActionProvider.COMMAND_PROFILE.equals(actionName)) {
                long stamp = getLastCoSLastTouch(config, false);
                //check the COS timestamp against critical files (pom.xml)
                // if changed, don't do COS.
                if (checkImportantFiles(stamp, config)) {
                    return true;
                }

            }
        }
        return true;
    }

    private void checkRunTest(final RunConfig config) {
        String actionName = config.getActionName();
        if (!(ActionProvider.COMMAND_TEST_SINGLE.equals(actionName)
                || ActionProvider.COMMAND_DEBUG_TEST_SINGLE.equals(actionName)
                || ActionProvider.COMMAND_PROFILE_TEST_SINGLE.equals(actionName))) {
            return;
        }
        if (RunUtils.isCompileOnSaveEnabled(config.getProject())) {

        }

        long stamp = getLastCoSLastTouch(config, true);
        //check the COS timestamp against critical files (pom.xml)
        // if changed, don't do COS.
        if (checkImportantFiles(stamp, config)) {
            return;
        }

    }

    private static void cleanGeneratedClassfiles(Project p) throws IOException { // #145243

    }

    static boolean checkImportantFiles(long stamp, RunConfig rc) {
        assert rc.getProject() != null;
        FileObject prjDir = rc.getProject().getProjectDirectory();
        if (isNewer(stamp, prjDir.getFileObject("build.gradle"))) { //NOI18N
            return true;
        }
        if (isNewer(stamp, prjDir.getFileObject("nbactions.xml"))) { //NOI18N
            return true;
        }
        // the nbactions.xml file belonging to active configuration?

        //TODO what other files/folders to check?
        return false;
    }

    private static boolean isNewer(long stamp, FileObject fo) {
        if (fo != null) {
            File fl = FileUtil.toFile(fo);
            if (fl.lastModified() >= stamp) {
                return true;
            }
        }
        return false;
    }

    private static File getCoSFile(RunConfig rc, boolean test) {
        if (rc.getProject() == null) {
            return null;
        }
        return getCoSFile(rc.getProject().getLookup().lookup(NbGradleProject.class).getGradleProject(), test);
    }

    private static File getCoSFile(GradleProject mp, boolean test) {
       
        if (mp == null) {
            return null;
        }
       
        
        String path = "";
        for (File outputdir : mp.getBaseProject().getOutputPaths()) {
            if (test && outputdir.getPath().contains("test") && outputdir.getPath().contains("classes")) {
                path = outputdir.getPath();
                break;
            } else if (!test && outputdir.getPath().contains("main") && outputdir.getPath().contains("classes")) {
                path = outputdir.getPath();
                break;
            }
        }


        
        if (path.equals("")) {
            return null;
        }
        File fl = new File(path);
        fl = FileUtil.normalizeFile(fl);
        return new File(fl, NB_COS);
    }

    /**
     * returns the
     *
     * @param rc
     * @param test
     * @return
     */
    static long getLastCoSLastTouch(RunConfig rc, boolean test) {
        File fl = getCoSFile(rc, test);
        if (fl == null) {
            return 0;
        }
        if (fl.getParentFile() == null || !(fl.getParentFile().exists())) {
            //the project was not built
            return 0;
        }
        if (!fl.exists()) {
            //wasn't yet run with CoS, assume it's been built correctly.
            // if CoS fails, we probably want to remove the file to trigger
            // rebuilding by maven
            return Long.MAX_VALUE;
        }
        return fl.lastModified();
    }

    static boolean touchCoSTimeStamp(RunConfig rc, boolean test) {
        return touchCoSTimeStamp(rc, test, System.currentTimeMillis());
    }

    private static boolean touchCoSTimeStamp(RunConfig rc, boolean test, long stamp) {
        if (rc.getProject() == null) {
            return false;
        }
        return touchCoSTimeStamp(rc.getProject().getLookup().lookup(NbGradleProject.class).getGradleProject(), test, stamp);
    }

    static boolean touchCoSTimeStamp(GradleProject mvn, boolean test) {
        return touchCoSTimeStamp(mvn, test, System.currentTimeMillis());
    }

    private static boolean touchCoSTimeStamp(GradleProject mvn, boolean test, long stamp) {
        File fl = getCoSFile(mvn, test);
        if (fl == null) {
            return false;
        }
        if (fl.getParentFile() == null || !(fl.getParentFile().exists())) {
            // The project was not built or it doesn't contains any source file
            // and thus the target/classes folder isn't created yet - see #219916
            boolean folderCreated = fl.getParentFile().mkdir();
            if (!folderCreated) {
                return false;
            }
        }
        if (!fl.exists()) {
            try {
                return fl.createNewFile();
            } catch (IOException ex) {
                return false;
            }
        }
        return fl.setLastModified(stamp);
    }

    static void deleteCoSTimeStamp(RunConfig rc, boolean test) {
        File fl = getCoSFile(rc, test);
        if (fl != null && fl.exists()) {
            fl.delete();
        }
    }

    private static void deleteCoSTimeStamp(GradleProject mp, boolean test) {
        File fl = getCoSFile(mp, test);
        if (fl != null && fl.exists()) {
            fl.delete();
        }
    }

    static List<String> extractDebugJVMOptions(String argLine) throws Exception {

        List<String> toRet = new ArrayList<String>();

        return toRet;
    }

    static void touchProject(Project project) {
        NbGradleProject prj = project.getLookup().lookup(NbGradleProject.class);

        if (prj != null) {
            GradleProject mvn = prj.getGradleProject();
            if (RunUtils.isCompileOnSaveEnabled(project)) {
                touchCoSTimeStamp(mvn, false);
                touchCoSTimeStamp(mvn, true);
            } else {
                File f = getCoSFile(mvn, false);
                boolean doClean = f != null && f.exists();
                if (doClean) {
                    try {
                        cleanGeneratedClassfiles(project);
                    } catch (IOException ex) {
                        LOG.log(Level.FINE, "Error cleaning up", ex);
                    }
                }
                deleteCoSTimeStamp(mvn, false);
                deleteCoSTimeStamp(mvn, true);
            }
        }
    }

    @StaticResource
    private static final String SUGGESTION = "org/netbeans/modules/gradle/resources/suggestion.png";
    private static boolean warnedNoCoS;

    @NbBundle.Messages({
        "CosChecker.no_test_cos.title=Not using Compile on Save",
        "CosChecker.no_test_cos.details=Compile on Save mode can speed up single test execution for many projects."
    })
    static void warnNoTestCoS(RunConfig config) {
        if (warnedNoCoS) {
            return;
        }
        final Project project = config.getProject();
        if (project == null) {
            return;
        }
        final Notification n = NotificationDisplayer.getDefault().notify(CosChecker_no_test_cos_title(), ImageUtilities.loadImageIcon(SUGGESTION, true), CosChecker_no_test_cos_details(), new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showCompilePanel(project);
            }
        }, NotificationDisplayer.Priority.LOW);
        RequestProcessor.getDefault().post(new Runnable() {
            @Override
            public void run() {
                n.clear();
            }
        }, 15 * 1000);
        warnedNoCoS = true;
    }

    private static void showCompilePanel(Project project) {

    }

    @ProjectServiceProvider(service = ProjectOpenedHook.class, projectType = "org-netbeans-modules-gradle")
    public static class CosPOH extends ProjectOpenedHook {

        private final Project project;

        private final PropertyChangeListener listener;

        public CosPOH(Project prj) {
            project = prj;
            listener = new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent evt) {

                    touchProject(project);

                }
            };
        }

        @Override
        protected void projectOpened() {
            RP.post(new Runnable() {
                @Override
                public void run() {
                    touchProject(project);
                }
            });
            NbGradleProject prj = project.getLookup().lookup(NbGradleProject.class);
            if (prj != null) {
                prj.addPropertyChangeListener(listener);
            }
        }

        @Override
        protected void projectClosed() {
            NbGradleProject prj = project.getLookup().lookup(NbGradleProject.class);
            if (prj != null) {
                prj.removePropertyChangeListener(listener);
                final GradleProject mvn = prj.getGradleProject();
                //TODO do we want to reconsider this?
                RP.post(new Runnable() {
                    @Override
                    public void run() {
//                        try {
//                            //also delete the IDE generated class files now?
//                            cleanGeneratedClassfiles(project);
//                        } catch (IOException ex) {
//                            LOG.log(Level.FINE, "Error cleaning up", ex);
//                        } finally {
                        deleteCoSTimeStamp(mvn, true);
                        deleteCoSTimeStamp(mvn, false);
//                        }
                    }
                });
            }
        }
    }

    @ProjectServiceProvider(service = ExecutionResultChecker.class, projectType = "org-netbeans-modules-gradle")
    public static class COSExChecker implements ExecutionResultChecker {

        @Override
        public void executionResult(RunConfig config, int resultCode) {
            // after each build put the Cos stamp in the output folder to have
            // the classes really compiled on save.
            if (RunUtils.isCompileOnSaveEnabled(config.getProject())) {
                touchCoSTimeStamp(config, false);
                touchCoSTimeStamp(config, true);
            } else {
                deleteCoSTimeStamp(config, false);
                deleteCoSTimeStamp(config, true);
            }
        }
    }
}
