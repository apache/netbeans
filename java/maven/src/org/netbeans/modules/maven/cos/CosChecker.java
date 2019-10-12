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
package org.netbeans.modules.maven.cos;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.versioning.ComparableVersion;
import org.apache.maven.model.Build;
import org.apache.maven.model.Dependency;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.project.runner.JavaRunner;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.maven.ActionProviderImpl;
import org.netbeans.modules.maven.api.Constants;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.api.PluginPropertyUtils;
import org.netbeans.modules.maven.api.classpath.DependencyProjectsProvider;
import org.netbeans.modules.maven.api.customizer.ModelHandle2;
import org.netbeans.modules.maven.api.execute.*;
import org.netbeans.modules.maven.configurations.M2ConfigProvider;
import org.netbeans.modules.maven.configurations.M2Configuration;
import static org.netbeans.modules.maven.cos.Bundle.*;
import org.netbeans.modules.maven.customizer.CustomizerProviderImpl;
import org.netbeans.modules.maven.execute.BeanRunConfig;
import org.netbeans.modules.maven.spi.cos.CompileOnSaveSkipper;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.netbeans.spi.project.ui.ProjectOpenedHook;
import org.openide.awt.Notification;
import org.openide.awt.NotificationDisplayer;
import org.openide.execution.ExecutorTask;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;

/**
 *
 * @author mkleint
 */
@ProjectServiceProvider(service={PrerequisitesChecker.class, LateBoundPrerequisitesChecker.class}, projectType="org-netbeans-modules-maven")
public class CosChecker implements PrerequisitesChecker, LateBoundPrerequisitesChecker {

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
        if (config.getProject() == null) {
            return true;
        }
        Properties javarunnerCheckprops = config.getMavenProject().getProperties();
        if ((javarunnerCheckprops != null && javarunnerCheckprops.containsKey(USE_OLD_COS_EXECUTION)) || config.getProperties().containsKey(USE_OLD_COS_EXECUTION)) {
            //old style Compile on Save
            if (!OldJavaRunnerCOS.checkRunMainClass(config)) {
                return false;
            }

            if (!OldJavaRunnerCOS.checkRunTest(config)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public boolean checkRunConfig(RunConfig config, ExecutionContext con) {
        //deleting the timestamp before every action invokation means
        // we only can rely on Run via JavaRunner and via DeployOnSave
        // any other means of long term execution will not keep the CoS stamp around while running..
        // -->> ONLY DELETE FOR BUILD ACTION
        if (ActionProvider.COMMAND_BUILD.equals(config.getActionName())) {
            
//commented out because deleting the timestamp makes the createGeneratedClassfiles action a noop.
//            deleteCoSTimeStamp(config, true);
//            deleteCoSTimeStamp(config, false);
//            //do clean the generated class files everytime, that won't hurt anything
//            // unless the classes are missing then ?!? if the action doesn't perform the compilation step?
//            try {
//                cleanGeneratedClassfiles(config.getProject());
//            } catch (IOException ex) {
//                if (!"clean".equals(config.getGoals().get(0))) { //NOI18N
//                    config.getGoals().add(0, "clean"); //NOI18N
//                    }
//                Logger.getLogger(CosChecker.class.getName()).log(Level.INFO, "Compile on Save Clean failed", ex);
//            }
        } else if (!ActionProvider.COMMAND_REBUILD.equals(config.getActionName())) {
            //now for all custom and non-build only related actions,
            //make sure we place the stamp files into all opened projects.
            Project[] opened = OpenProjects.getDefault().getOpenProjects();
            for (Project openprj : opened) {
                touchProject(openprj);
            }
            //new style Compile on Save
            if (!checkRunMainClass(config, con)) {
                return false;
            }
            checkRunTest(config, con);
        } 
        return true;
    }

    /**
     * Returns {@code false} if execution should skip standard build phases.
     *
     * @param config run configuration
     * @param con execution context
     * @return {@code false} if execution should skip standard build phases,
     *         {@code true} otherwise
     */
    private boolean checkRunMainClass(final RunConfig config, ExecutionContext con) {
        final String actionName = config.getActionName();
        //compile on save stuff
        if (RunUtils.isCompileOnSaveEnabled(config)) {
            if (ActionProvider.COMMAND_RUN.equals(actionName) ||
                ActionProvider.COMMAND_DEBUG.equals(actionName) ||
                ActionProvider.COMMAND_PROFILE.equals(actionName) ||
                ActionProviderImpl.COMMAND_RUN_MAIN.equals(actionName) ||
                ActionProviderImpl.COMMAND_DEBUG_MAIN.equals(actionName) ||
                ActionProviderImpl.COMMAND_PROFILE_MAIN.equals(actionName))
            {
                long stamp = getLastCoSLastTouch(config, false);
                //check the COS timestamp against critical files (pom.xml)
                // if changed, don't do COS.
                if (checkImportantFiles(stamp, config)) {
                    return true;
                }
                //check the COS timestamp against resources etc.
                //if changed, perform part of the maven build. (or skip COS)
                for (CompileOnSaveSkipper skipper : Lookup.getDefault().lookupAll(CompileOnSaveSkipper.class)) {
                    if (skipper.skip(config, false, stamp)) {
                        return true;
                    }
                }
                
                Properties javarunnerCheckprops = config.getMavenProject().getProperties();
                if ((javarunnerCheckprops != null && javarunnerCheckprops.containsKey(USE_OLD_COS_EXECUTION)) || config.getProperties().containsKey(USE_OLD_COS_EXECUTION)) {
                    LOG.fine("use.old.cos.execution found, using JavaRunner to execute.");
                } else {
                    // #230565
                    if (CoSAlternativeExecutor.execute(config, con)) {
                        // If at least on of registered executors was successfull, skip the default CoS execution
                        return false;
                    }

                    //now attempt to extract
                    if (config instanceof BeanRunConfig) {
                        BeanRunConfig brc = (BeanRunConfig) config;
                        List<String> processedGoals = new ArrayList<String>();
                        for (String goal : brc.getGoals()) {
                            if (Constants.DEFAULT_PHASES.contains(goal) || Constants.CLEAN_PHASES.contains(goal)) {
                                continue;
                            }
                            processedGoals.add(goal);
                        }
                        /*#232323 nasty but necessary because of the hack to add switches among the  list of goals */
                        boolean containsResumeFrom = processedGoals.contains("--resume-from") || processedGoals.contains("-rf");
                        if (processedGoals.size() > (containsResumeFrom ? 2 : 0)) {
                            brc.setGoals(processedGoals);
                            injectDependencyProjects(brc, false, con);
                        }
                    } else {
                        //#221781 we might need to add -DlastModGranularityMs=-10000000000 to force re-compilation of CoS created class files.
                        // or clear the IDe generated files in some other way
                        LOG.log(Level.INFO, "could not strip phase goals from RunConfig subclass {0}", config.getClass().getName());
                    }
                }
            }
        }
        return true;
    }

    private void checkRunTest(final RunConfig config, ExecutionContext con) {
        String actionName = config.getActionName();
        if (!(ActionProvider.COMMAND_TEST_SINGLE.equals(actionName) ||
                ActionProvider.COMMAND_DEBUG_TEST_SINGLE.equals(actionName) ||
                ActionProvider.COMMAND_PROFILE_TEST_SINGLE.equals(actionName))) {
            return;
        }
        if (RunUtils.isCompileOnSaveEnabled(config)) {
            String testng = PluginPropertyUtils.getPluginProperty(config.getMavenProject(), Constants.GROUP_APACHE_PLUGINS,
                    Constants.PLUGIN_SUREFIRE, "testNGArtifactName", "test", "testNGArtifactName"); //NOI18N
            if (testng == null) {
                testng = "org.testng:testng"; //NOI18N
            }
            List<Dependency> deps = config.getMavenProject().getTestDependencies();
            boolean haveJUnit = false, haveTestNG = false;
            String testngVersion = null;
            for (Dependency d : deps) {
                if (d.getManagementKey().startsWith(testng)) {
                    testngVersion = d.getVersion();
                    haveTestNG = true;
                } else if (d.getManagementKey().startsWith("junit:junit")) { //NOI18N
                    haveJUnit = true;
                }
            }
            if (haveJUnit && haveTestNG && new ComparableVersion("6.5.1").compareTo(new ComparableVersion(testngVersion)) >= 0) {
                //CoS requires at least TestNG 6.5.2-SNAPSHOT if JUnit is present
                return;
            }

            long stamp = getLastCoSLastTouch(config, true);
            //check the COS timestamp against critical files (pom.xml)
            // if changed, don't do COS.
            if (checkImportantFiles(stamp, config)) {
                return;
            }

            //check the COS timestamp against resources etc.
            //if changed, perform part of the maven build. (or skip COS)
            
            for (CompileOnSaveSkipper skipper : Lookup.getDefault().lookupAll(CompileOnSaveSkipper.class)) {
                if (skipper.skip(config, true, stamp)) {
                    return;
                }
            }
                //now attempt to extract
                if (config instanceof BeanRunConfig) {
                    BeanRunConfig brc = (BeanRunConfig) config;
                    List<String> processedGoals = new ArrayList<String>();
                    for (String goal : brc.getGoals()) {
                        if (Constants.DEFAULT_PHASES.contains(goal) || Constants.CLEAN_PHASES.contains(goal)) {
                            continue;
                        }
                        processedGoals.add(goal);
                    }
                    if (processedGoals.size() > 0) {
                        brc.setGoals(processedGoals);
                        injectDependencyProjects(brc, true, con);
                    }
                } else {
                    LOG.log(Level.INFO, "could not strip phase goals from RunConfig subclass {0}", config.getClass().getName());
                }
        } else {
            warnNoTestCoS(config);
        }
    }

    private static void cleanGeneratedClassfiles(Project p) throws IOException { // #145243
        //we execute normal maven build, but need to clean any
        // CoS classes present.
        List<ClassPath> executePaths = new ArrayList<ClassPath>();
        for (SourceGroup g : ProjectUtils.getSources(p).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA)) {
            FileObject root = g.getRootFolder();
            ClassPath cp = ClassPath.getClassPath(root, ClassPath.EXECUTE);
            if (cp != null) {
                executePaths.add(cp);
            }
        }
        int res = JavaRunner.execute(JavaRunner.QUICK_CLEAN, Collections.singletonMap(
                JavaRunner.PROP_EXECUTE_CLASSPATH, ClassPathSupport.createProxyClassPath(executePaths.toArray(new ClassPath[0])))).
                result();
        if (res != 0) {
            throw new IOException("Failed to clean NetBeans-generated classes");
        }
    }

    static boolean checkImportantFiles(long stamp, RunConfig rc) {
        assert rc.getProject() != null;
        FileObject prjDir = rc.getProject().getProjectDirectory();
        if (isNewer(stamp, prjDir.getFileObject("pom.xml"))) { //NOI18N
            return true;
        }
        if (isNewer(stamp, prjDir.getFileObject("nbactions.xml"))) { //NOI18N
            return true;
        }
        // the nbactions.xml file belonging to active configuration?
        M2ConfigProvider prov = rc.getProject().getLookup().lookup(M2ConfigProvider.class);
        if (prov != null) {
            M2Configuration m2c = prov.getActiveConfiguration();
            if (m2c != null) {
                String name = M2Configuration.getFileNameExt(m2c.getId());
                if (isNewer(stamp, prjDir.getFileObject(name))) {
                    return true;
                }
            }
        }
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
        return getCoSFile(rc.getMavenProject(), test);
    }

    private static File getCoSFile(MavenProject mp, boolean test) {
        if (mp == null) {
            return null;
        }
        Build build = mp.getBuild();
        if (build == null) {
            return null;
        }
        String path = test ? build.getTestOutputDirectory() : build.getOutputDirectory();
        if (path == null) {
            return null;
        }
        File fl = new File(path);
        fl = FileUtil.normalizeFile(fl);
        return  new File(fl, NB_COS);
    }

    /**
     * returns the
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
        return touchCoSTimeStamp(rc.getMavenProject(), test, stamp);
    }

    static boolean touchCoSTimeStamp(MavenProject mvn, boolean test) {
        return touchCoSTimeStamp(mvn, test, System.currentTimeMillis());
    }

    private static boolean touchCoSTimeStamp(MavenProject mvn, boolean test, long stamp) {
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

    private static void deleteCoSTimeStamp(MavenProject mp, boolean test) {
        File fl = getCoSFile(mp, test);
        if (fl != null && fl.exists()) {
            fl.delete();
        }
    }


    static List<String> extractDebugJVMOptions(String argLine) throws Exception {
        String[] split = CommandLineUtils.translateCommandline(argLine);
        List<String> toRet = new ArrayList<String>();
        for (String arg : split) {
            if ("-Xdebug".equals(arg)) { //NOI18N
                continue;
            }
            if ("-Djava.compiler=none".equals(arg)) { //NOI18N
                continue;
            }
            if ("-Xnoagent".equals(arg)) { //NOI18N
                continue;
            }
            if (arg.startsWith("-Xrunjdwp")) { //NOI18N
                continue;
            }
            if (arg.equals("-agentlib:jdwp")) { //NOI18N
                continue;
            }
            if (arg.startsWith("-agentlib:jdwp=")) { //NOI18N
                continue;
            }
            if (arg.trim().length() == 0) {
                continue;
            }
            toRet.add(arg);
        }
        return toRet;
    }
    
    static void touchProject(Project project) {
        NbMavenProject prj = project.getLookup().lookup(NbMavenProject.class);
        if (prj != null) {
            MavenProject mvn = prj.getMavenProject();
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

    @StaticResource private static final String SUGGESTION = "org/netbeans/modules/maven/resources/suggestion.png";
    private static boolean warnedNoCoS;
    @Messages({
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
            @Override public void actionPerformed(ActionEvent e) {
                showCompilePanel(project);
            }
        }, NotificationDisplayer.Priority.LOW);
        RequestProcessor.getDefault().post(new Runnable() {
            @Override public void run() {
                n.clear();
            }
        }, 15 * 1000);
        warnedNoCoS = true;
    }
    
//    private static boolean warnedCoS;
//    @Messages({
//        "CosChecker.test_cos.title=Using Compile on Save (CoS)",
//        "CosChecker.test_cos.details=CoS mode is not executing tests through Maven. Disable if causing problems."
//    })
//    private static void warnTestCoS(RunConfig config) {
//        if (warnedCoS) {
//            return;
//        }
//        final Project project = config.getProject();
//        if (project == null) {
//            return;
//        }
//        final Notification n = NotificationDisplayer.getDefault().notify(CosChecker_test_cos_title(), ImageUtilities.loadImageIcon(SUGGESTION, true), CosChecker_test_cos_details(), new ActionListener() {
//            @Override public void actionPerformed(ActionEvent e) {
//                showCompilePanel(project);
//            }
//
//        }, NotificationDisplayer.Priority.LOW);
//        RequestProcessor.getDefault().post(new Runnable() {
//            @Override public void run() {
//                n.clear();
//            }
//        }, 15 * 1000);
//        warnedCoS = true;
//    }
    
    private static void showCompilePanel(Project project) {
        CustomizerProviderImpl prv = project.getLookup().lookup(CustomizerProviderImpl.class);
        if (prv != null) {
            prv.showCustomizer(ModelHandle2.PANEL_COMPILE);
        }
    }

    //this method has a problem of timing. There is the executor's task running in some other thread and
    // and we try to write to it. In reality for short lived executions it either prints first or never at all.
    // I suppose that for long running tasks we cannot guarantee the position at which the warning appears.
    static void warnCoSInOutput(final ExecutorTask tsk, final RunConfig config) throws IOException {
//        if (IOColorPrint.isSupported(tsk.getInputOutput())) {
//            IOColorPrint.print(tsk.getInputOutput(), "NetBeans: Compile on Save Execution is not done through Maven.", null, false, Color.GRAY);
//            IOColorPrint.print(tsk.getInputOutput(), "Disable if it's causing problems.\n", new OutputListener() {
//
//                @Override
//                public void outputLineSelected(OutputEvent ev) {
//                }
//                
//                @Override
//                public void outputLineAction(OutputEvent ev) {
//                    showCompilePanel(config.getProject());
//                }
//
//                @Override
//                public void outputLineCleared(OutputEvent ev) {
//                }
//            }, false, Color.BLUE.darker());
//        }
    }

    private void injectDependencyProjects(BeanRunConfig brc, boolean test, ExecutionContext con) {
        if (brc.getProject() == null) {
            return;
        }
        File jar = InstalledFileLocator.getDefault().locate("maven-nblib/netbeans-cos.jar", "org.netbeans.modules.maven", false);
        assert jar != null;
        
        DependencyProjectsProvider dep = brc.getProject().getLookup().lookup(DependencyProjectsProvider.class);
        assert dep != null;
        
        if(dep == null) {
            // issue #245181
            Project p = brc.getProject();
            LOG.log(Level.WARNING, "no DependencyProjectsProvider available for project {0} in {1}", new Object[] {p, p.getProjectDirectory()});
        }
        
        StringBuilder value = new StringBuilder();
        for (DependencyProjectsProvider.Pair pair : dep.getDependencyProjects()) {
            if (!test && "test".equals(pair.getArtifact().getScope())) { //TODO finetune what about provided?
                continue;
            }
            Artifact a = pair.getArtifact();
            if ((!pair.getArtifact().hasClassifier() || "tests".equals(pair.getArtifact().getClassifier())) 
                    && RunUtils.isCompileOnSaveEnabled(pair.getProject()) 
                    && OpenProjects.getDefault().isProjectOpen(pair.getProject())) { //#237528
                if (value.length() != 0) {
                    value.append(",");
                }
                value.append(a.getGroupId()).append(":").append(a.getArtifactId()).append(":").append(a.getBaseVersion());
                File f = FileUtil.toFile(pair.getProject().getProjectDirectory());
                value.append("=").append(f.getAbsolutePath());
            }
        }
        if (value.length() > 0) {
            String mavenPath = brc.getProperties().get(CosChecker.MAVENEXTCLASSPATH);
            String delimiter = Utilities.isWindows() ? ";" : ":";
            if (mavenPath == null) {
                mavenPath = "";
            } else {
                if(mavenPath.contains(jar + delimiter)) {
                    // invoked by output view > rerun? see also issue #249971
                    return;
                }
                mavenPath += delimiter;
            }
            mavenPath += jar.getAbsolutePath();
            brc.setProperty(MAVENEXTCLASSPATH, mavenPath);
        }
        brc.setProperty(ENV_NETBEANS_PROJECT_MAPPINGS, value.toString()); //always put ti recognize later and print warnings
    }

    
    @ProjectServiceProvider(service=ProjectOpenedHook.class, projectType="org-netbeans-modules-maven")
    public static class CosPOH extends ProjectOpenedHook {

        private final Project project;
        
        private final PropertyChangeListener listener;

        public CosPOH(Project prj) {
            project = prj;
            listener = new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    if (NbMavenProject.PROP_PROJECT.equals(evt.getPropertyName())) {
                        touchProject(project);
                    }
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
            NbMavenProject prj = project.getLookup().lookup(NbMavenProject.class);
            if (prj != null) {
                prj.addPropertyChangeListener(listener);
            }
        }

        @Override
        protected void projectClosed() {
            NbMavenProject prj = project.getLookup().lookup(NbMavenProject.class);
            if (prj != null) {
                prj.removePropertyChangeListener(listener);
                final MavenProject mvn = prj.getMavenProject();
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

    @ProjectServiceProvider(service=ExecutionResultChecker.class, projectType="org-netbeans-modules-maven")
    public static class COSExChecker implements ExecutionResultChecker {

        @Override
        public void executionResult(RunConfig config, ExecutionContext res, int resultCode) {
            // after each build put the Cos stamp in the output folder to have
            // the classes really compiled on save.
            if (RunUtils.isCompileOnSaveEnabled(config)) {
                touchCoSTimeStamp(config, false);
                touchCoSTimeStamp(config, true);
            } else {
                deleteCoSTimeStamp(config, false);
                deleteCoSTimeStamp(config, true);
            }
        }
    }
}
