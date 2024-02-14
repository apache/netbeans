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

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import org.apache.maven.artifact.versioning.ComparableVersion;
import org.apache.maven.model.Dependency;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.netbeans.api.extexecution.startup.StartupExtender;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.runner.JavaRunner;
import org.netbeans.api.java.source.ui.ScanDialog;
import org.netbeans.api.project.Project;
import org.netbeans.modules.maven.ActionProviderImpl;
import org.netbeans.modules.maven.api.Constants;
import org.netbeans.modules.maven.api.FileUtilities;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.api.PluginPropertyUtils;
import org.netbeans.modules.maven.api.classpath.ProjectSourcesClassPathProvider;
import org.netbeans.modules.maven.api.execute.ActiveJ2SEPlatformProvider;
import org.netbeans.modules.maven.api.execute.RunConfig;
import org.netbeans.modules.maven.api.execute.RunUtils;
import org.netbeans.modules.maven.classpath.AbstractProjectClassPathImpl;
import org.netbeans.modules.maven.classpath.RuntimeClassPathImpl;
import org.netbeans.modules.maven.classpath.TestRuntimeClassPathImpl;
import org.netbeans.modules.maven.customizer.RunJarPanel;
import org.netbeans.modules.maven.execute.DefaultReplaceTokenProvider;
import org.netbeans.modules.maven.runjar.MavenExecuteUtils;
import org.netbeans.modules.maven.spi.cos.CompileOnSaveSkipper;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.SingleMethod;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.execution.ExecutorTask;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.Utilities;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 *
 * @author mkleint
 */
public class OldJavaRunnerCOS {
    private static final Logger LOG = Logger.getLogger(OldJavaRunnerCOS.class.getName());
    private static final String STARTUP_ARGS_KEY = "run.jvmargs.ide"; // NOI18N


    
    static boolean checkRunMainClass(final RunConfig config) {
        final String actionName = config.getActionName();
        //compile on save stuff
        if (RunUtils.hasApplicationCompileOnSaveEnabled(config)) {
            if ((NbMavenProject.TYPE_JAR.equals(
                    config.getProject().getLookup().lookup(NbMavenProject.class).getPackagingType()) &&
                    (ActionProvider.COMMAND_RUN.equals(actionName) ||
                    ActionProvider.COMMAND_DEBUG.equals(actionName) ||
                    ActionProvider.COMMAND_PROFILE.equals(actionName))) ||
                    ActionProviderImpl.COMMAND_RUN_MAIN.equals(actionName) ||
                    ActionProviderImpl.COMMAND_DEBUG_MAIN.equals(actionName) ||
                    ActionProviderImpl.COMMAND_PROFILE_MAIN.equals(actionName)) {
                long stamp = CosChecker.getLastCoSLastTouch(config, false);
                //check the COS timestamp against critical files (pom.xml)
                // if changed, don't do COS.
                if (CosChecker.checkImportantFiles(stamp, config)) {
                    return true;
                }
                //check the COS timestamp against resources etc.
                //if changed, perform part of the maven build. (or skip COS)
                for (CompileOnSaveSkipper skipper : Lookup.getDefault().lookupAll(CompileOnSaveSkipper.class)) {
                    if (skipper.skip(config, false, stamp)) {
                        return true;
                    }
                }
                return deprecatedJavaRunnerApproach(config, actionName);
            }
        }
        return true;
    }
    
   static boolean checkRunTest(final RunConfig config) {
        String actionName = config.getActionName();
        if (!(ActionProvider.COMMAND_TEST_SINGLE.equals(actionName) ||
                ActionProvider.COMMAND_DEBUG_TEST_SINGLE.equals(actionName) ||
                ActionProvider.COMMAND_PROFILE_TEST_SINGLE.equals(actionName))) {
            return true;
        }
        if (RunUtils.hasTestCompileOnSaveEnabled(config)) {
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
                return true;
            }
            String test = config.getProperties().get("test"); //NOI18N
            if (test == null) {
                //user somehow configured mapping in unknown way.
                return true;
            }

            long stamp = CosChecker.getLastCoSLastTouch(config, true);
            //check the COS timestamp against critical files (pom.xml)
            // if changed, don't do COS.
            if (CosChecker.checkImportantFiles(stamp, config)) {
                return true;
            }

            //check the COS timestamp against resources etc.
            //if changed, perform part of the maven build. (or skip COS)
            
            for (CompileOnSaveSkipper skipper : Lookup.getDefault().lookupAll(CompileOnSaveSkipper.class)) {
                if (skipper.skip(config, true, stamp)) {
                    return true;
                }
            }
            return OldJavaRunnerCOS.deprecatedJavaRunnerApproachTest(config, actionName);
        } else {
            CosChecker.warnNoTestCoS(config);
            return true;
        }
    }    
    
    
    static  boolean deprecatedJavaRunnerApproachTest(final RunConfig config, String actionName) {
        String test = config.getProperties().get("test");
        final Map<String, Object> params = new HashMap<String, Object>();
        FileObject selected = config.getSelectedFileObject();
        ProjectSourcesClassPathProvider cpp = config.getProject().getLookup().lookup(ProjectSourcesClassPathProvider.class);
        ClassPath srcs = cpp.getProjectSourcesClassPath(ClassPath.SOURCE);
        ClassPath[] cps = cpp.getProjectClassPaths(ClassPath.SOURCE);
        ClassPath testcp = ClassPathSupport.createProxyClassPath(cps);
        String path;
        if (selected != null) {
            path = srcs.getResourceName(selected);
            if (path != null) {
                String nameExt = selected.getNameExt().replace(".java", "Test.java");
                path = path.replace(selected.getNameExt(), nameExt);
                FileObject testFo = testcp.findResource(path);
                if (testFo != null) {
                    selected = testFo;
                } else {
                    //#160776 only files on source classpath pass through
                    return true;
                }
            } else {
                path = testcp.getResourceName(selected);
                if (path == null) {
                    //#160776 only files on source classpath pass through
                    return true;
                }
            }
        } else {
            test = test + ".java";
            selected = testcp.findResource(test);
            if (selected == null) {
                List<FileObject> mainSourceRoots = Arrays.asList(srcs.getRoots());
                TOP:
                for (FileObject root : testcp.getRoots()) {
                    if (mainSourceRoots.contains(root)) {
                        continue;
                    }
                    Enumeration<? extends FileObject> fos = root.getData(true);
                    while (fos.hasMoreElements()) {
                        FileObject fo = fos.nextElement();
                        if (fo.getNameExt().equals(test)) {
                            selected = fo;
                            break TOP;
                        }
                    }
                }
            }
        }
        if (selected == null) {
            return true;
        }
        params.put(JavaRunner.PROP_EXECUTE_FILE, selected);
        params.put(JavaRunner.PROP_PLATFORM, config.getProject().getLookup().lookup(ActiveJ2SEPlatformProvider.class).getJavaPlatform());
        List<String> jvmProps = new ArrayList<String>();
        Set<String> jvmPropNames = new HashSet<String>();
        params.put(JavaRunner.PROP_PROJECT_NAME, config.getExecutionName() + "/CoS");
        String dir = PluginPropertyUtils.getPluginProperty(config.getMavenProject(), Constants.GROUP_APACHE_PLUGINS, Constants.PLUGIN_SUREFIRE, "basedir", "test", "basedir");
        jvmPropNames.add("basedir");
        if (dir != null) {
            params.put(JavaRunner.PROP_WORK_DIR, dir);
            jvmProps.add("-Dbasedir=\"" + dir + "\"");
        } else {
            params.put(JavaRunner.PROP_WORK_DIR, config.getExecutionDirectory());
            jvmProps.add("-Dbasedir=\"" + config.getExecutionDirectory().getAbsolutePath() + "\"");
        }
        Properties sysProps = PluginPropertyUtils.getPluginPropertyParameter(config.getMavenProject(), Constants.GROUP_APACHE_PLUGINS, Constants.PLUGIN_SUREFIRE, "systemProperties", "test");
        if (sysProps != null) {
            for (Map.Entry key : sysProps.entrySet()) {
                jvmProps.add("-D" + key.getKey() + "=" + key.getValue());
                jvmPropNames.add((String) key.getKey());
            }
        }
        for (Map.Entry entry : config.getProperties().entrySet()) {
            if ("maven.surefire.debug".equals(entry.getKey())) {
                //NOI18N
                continue;
            }
            if (Constants.ACTION_PROPERTY_JPDALISTEN.equals(entry.getKey())) {
                continue;
            }
            if ("jpda.stopclass".equals(entry.getKey())) {
                //NOI18N
                continue;
            }
            if (DefaultReplaceTokenProvider.METHOD_NAME.equals(entry.getKey())) {
                params.put("methodname", entry.getValue());
                actionName = ActionProvider.COMMAND_TEST_SINGLE.equals(actionName) ? SingleMethod.COMMAND_RUN_SINGLE_METHOD : SingleMethod.COMMAND_DEBUG_SINGLE_METHOD;
                continue;
            }
            if (!jvmPropNames.contains((String) entry.getKey())) {
                jvmProps.add("-D" + entry.getKey() + "=" + entry.getValue());
                jvmPropNames.add((String) entry.getKey());
            }
        }
        String argLine = PluginPropertyUtils.getPluginProperty(config.getMavenProject(), Constants.GROUP_APACHE_PLUGINS, Constants.PLUGIN_SUREFIRE, "argLine", "test", "argLine");
        if (argLine != null) {
            try {
                String[] arr = CommandLineUtils.translateCommandline(argLine);
                jvmProps.addAll(Arrays.asList(arr));
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
        } else {
            argLine = config.getProperties().get("argLine");
            if (argLine != null) {
                try {
                    jvmProps.addAll(CosChecker.extractDebugJVMOptions(argLine));
                } catch (CommandLineException cli) {
                    LOG.log(Level.INFO, "error parsing argLine property:" + argLine, cli);
                    if (ActionProvider.COMMAND_DEBUG_TEST_SINGLE.equals(actionName)) {
                        NotifyDescriptor.Message msg = new NotifyDescriptor.Message("Error parsing argLine property, arguments will not be passed to internal execution. Error: " + cli.getLocalizedMessage(), NotifyDescriptor.ERROR_MESSAGE);
                        DialogDisplayer.getDefault().notifyLater(msg);
                    }
                } catch (Exception ex) {
                    LOG.log(Level.INFO, "error extracting debug params from argLine property:" + argLine, ex);
                }
            }
        }
        String[] additionals = PluginPropertyUtils.getPluginPropertyList(config.getMavenProject(), Constants.GROUP_APACHE_PLUGINS, Constants.PLUGIN_SUREFIRE, "additionalClasspathElements", "additionalClasspathElement", "test");
        ClassPath cp = createRuntimeClassPath(config.getMavenProject(), true);
        if (additionals != null) {
            List<URL> roots = new ArrayList<URL>();
            File base = FileUtil.toFile(config.getProject().getProjectDirectory());
            for (String add : additionals) {
                File root = FileUtilities.resolveFilePath(base, add);
                if (root != null) {
                    try {
                        URL url = Utilities.toURI(root).toURL();
                        if (FileUtil.isArchiveFile(url)) {
                            url = FileUtil.getArchiveRoot(url);
                        }
                        roots.add(url);
                    } catch (MalformedURLException ex) {
                        Logger.getLogger(CosChecker.class.getName()).log(Level.INFO, "Cannot convert ''{0}'' to URL", add);
                    }
                } else {
                    Logger.getLogger(CosChecker.class.getName()).log(Level.INFO, "Cannot convert ''{0}'' to URL.", add);
                }
            }
            ClassPath addCp = ClassPathSupport.createClassPath(roots.toArray(new URL[0]));
            cp = ClassPathSupport.createProxyClassPath(cp, addCp);
        }
        params.put(JavaRunner.PROP_EXECUTE_CLASSPATH, cp);
        params.put(JavaRunner.PROP_RUN_JVMARGS, jvmProps);
        params.put("maven.disableSources", Boolean.TRUE);
        final String action2Quick = action2Quick(actionName);
        boolean supported = JavaRunner.isSupported(action2Quick, params);
        if (supported) {
            try {
                SwingUtilities.invokeAndWait(new Runnable() {
                    @Override
                    public void run() {
                        ScanDialog.runWhenScanFinished(new Runnable() {
                            @Override
                            public void run() {
                                if (SwingUtilities.isEventDispatchThread()) {
                                    CosChecker.RP.post(this);
                                    return;
                                }
                                try {
                                    collectStartupArgs(config, params);
                                    final ExecutorTask tsk = JavaRunner.execute(action2Quick, params);
                                    CosChecker.warnCoSInOutput(tsk, config);
                                } catch (IOException ex) {
                                    Exceptions.printStackTrace(ex);
                                } catch (UnsupportedOperationException ex) {
                                    Exceptions.printStackTrace(ex);
                                } finally {
                                    CosChecker.touchCoSTimeStamp(config, true);
                                    if (RunUtils.hasApplicationCompileOnSaveEnabled(config)) {
                                        CosChecker.touchCoSTimeStamp(config, false);
                                    } else {
                                        CosChecker.deleteCoSTimeStamp(config, false);
                                    }
                                }
                            }
                        }, config.getTaskDisplayName());
                    }
                });
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            } catch (InvocationTargetException ex) {
                Exceptions.printStackTrace(ex);
            }
            return false;
        }
        return true;
    }

    static boolean deprecatedJavaRunnerApproach(final RunConfig config, String actionName) {
        final Map<String, Object> params = new HashMap<String, Object>();
        params.put(JavaRunner.PROP_PROJECT_NAME, config.getExecutionName() + "/CoS");
        String proppath = config.getProperties().get("exec.workingdir");
        if (proppath != null) {
            params.put(JavaRunner.PROP_WORK_DIR, FileUtil.normalizeFile(new File(proppath)));
        } else {
            params.put(JavaRunner.PROP_WORK_DIR, config.getExecutionDirectory());
        }
        if (ActionProviderImpl.COMMAND_RUN_MAIN.equals(actionName) || ActionProviderImpl.COMMAND_DEBUG_MAIN.equals(actionName) || ActionProviderImpl.COMMAND_PROFILE_MAIN.equals(actionName)) {
            FileObject selected = config.getSelectedFileObject();
            ClassPath srcs = config.getProject().getLookup().lookup(ProjectSourcesClassPathProvider.class).getProjectSourcesClassPath(ClassPath.SOURCE);
            String path = srcs.getResourceName(selected);
            if (path == null) {
                //#160776 only files on source classpath pass through
                return true;
            }
            params.put(JavaRunner.PROP_EXECUTE_FILE, selected);
        } else {
            params.put(JavaRunner.PROP_EXECUTE_CLASSPATH, createRuntimeClassPath(config.getMavenProject(), false));
        }
        String exargs = config.getProperties().get("exec.args");
        if (exargs != null) {
            String[] args = MavenExecuteUtils.splitAll(exargs, true);
            if (params.get(JavaRunner.PROP_EXECUTE_FILE) == null) {
                params.put(JavaRunner.PROP_CLASSNAME, args[1]);
            }
            String[] appargs = args[2].split(" ");
            params.put(JavaRunner.PROP_APPLICATION_ARGS, Arrays.asList(appargs));
            try {
                params.put(JavaRunner.PROP_RUN_JVMARGS, CosChecker.extractDebugJVMOptions(args[0]));
            } catch (CommandLineException cli) {
                LOG.log(Level.INFO, "error parsing exec.args property:" + args[0], cli);
                if (ActionProviderImpl.COMMAND_DEBUG_MAIN.equals(actionName) || ActionProvider.COMMAND_DEBUG.equals(actionName)) {
                    NotifyDescriptor.Message msg = new NotifyDescriptor.Message("Error parsing exec.args property, arguments will not be passed to internal execution. Error: " + cli.getLocalizedMessage(), NotifyDescriptor.ERROR_MESSAGE);
                    DialogDisplayer.getDefault().notifyLater(msg);
                }
            } catch (Exception ex) {
                LOG.log(Level.INFO, "error extracting debug params from exec.args property:" + args[0], ex);
            }
        }
        params.put(JavaRunner.PROP_PLATFORM, config.getProject().getLookup().lookup(ActiveJ2SEPlatformProvider.class).getJavaPlatform());
        params.put("maven.disableSources", Boolean.TRUE);
        if (params.get(JavaRunner.PROP_EXECUTE_FILE) != null || params.get(JavaRunner.PROP_CLASSNAME) != null) {
            final String action2Quick = action2Quick(actionName);
            boolean supported = JavaRunner.isSupported(action2Quick, params);
            if (supported) {
                try {
                    SwingUtilities.invokeAndWait(new Runnable() {
                        @Override
                        public void run() {
                            ScanDialog.runWhenScanFinished(new Runnable() {
                                @Override
                                public void run() {
                                    if (SwingUtilities.isEventDispatchThread()) {
                                        CosChecker.RP.post(this);
                                        return;
                                    }
                                    try {
                                        collectStartupArgs(config, params);
                                        ExecutorTask tsk = JavaRunner.execute(action2Quick, params);
                                        CosChecker.warnCoSInOutput(tsk, config);
                                    } catch (IOException ex) {
                                        Exceptions.printStackTrace(ex);
                                    } catch (UnsupportedOperationException ex) {
                                        Exceptions.printStackTrace(ex);
                                    } finally {
                                        if (RunUtils.hasApplicationCompileOnSaveEnabled(config)) {
                                            CosChecker.touchCoSTimeStamp(config, false);
                                        }
                                    }
                                }
                            }, config.getTaskDisplayName());
                        }
                    });
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                } catch (InvocationTargetException ex) {
                    Exceptions.printStackTrace(ex);
                }
                return false;
            }
        } else {
            //TODO what to do now? skip?
        }
        return true;
    }

    
    //create a special runtime classpath here as the resolved mavenproject in execution
    // can be different from the one in loaded project
    private static  ClassPath createRuntimeClassPath(MavenProject prj, boolean test) {
        List<URI> roots;
        if (test) {
            roots = TestRuntimeClassPathImpl.createPath(prj);
        } else {
            roots = RuntimeClassPathImpl.createPath(prj);
        }
        return ClassPathSupport.createClassPath(AbstractProjectClassPathImpl.getPath(roots.toArray(new URI[0]), null));
    }    
    
    
   private static void collectStartupArgs(RunConfig config, Map<String, Object> params) {
        String actionName = config.getActionName();
        StartupExtender.StartMode mode;
        
        if (ActionProvider.COMMAND_RUN.equals(actionName) || ActionProviderImpl.COMMAND_RUN_MAIN.equals(actionName)) {
            mode = StartupExtender.StartMode.NORMAL;
        } else if (ActionProvider.COMMAND_DEBUG.equals(actionName) || ActionProviderImpl.COMMAND_DEBUG_MAIN.equals(actionName)) {
            mode = StartupExtender.StartMode.DEBUG;
        } else if (ActionProvider.COMMAND_PROFILE.equals(actionName) || ActionProvider.COMMAND_PROFILE_SINGLE.equals(actionName) || ActionProviderImpl.COMMAND_PROFILE_MAIN.equals(actionName)) {
            mode = StartupExtender.StartMode.PROFILE;
        } else if (ActionProvider.COMMAND_PROFILE_TEST_SINGLE.equals(actionName)) {
            mode = StartupExtender.StartMode.TEST_PROFILE;
        } else {
            // XXX could also set argLine for COMMAND_TEST and relatives (StartMode.TEST_*); need not be specific to TYPE_JAR
            return;
        }

        InstanceContent ic = new InstanceContent();
        Project p = config.getProject();
        if (p != null) {
            ic.add(p);
            ActiveJ2SEPlatformProvider pp = p.getLookup().lookup(ActiveJ2SEPlatformProvider.class);
            if (pp != null) {
                ic.add(pp.getJavaPlatform());
            }
        }
        Set<String> args = new HashSet<String>();

        for (StartupExtender group : StartupExtender.getExtenders(new AbstractLookup(ic), mode)) {
            args.addAll(group.getArguments());
        }
        
        if (!args.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for(String arg : args) {
                sb.append(arg).append(' ');
            }
            params.put(STARTUP_ARGS_KEY, sb.toString());
        }
    }    

    static String action2Quick(String actionName) {
        if (ActionProvider.COMMAND_CLEAN.equals(actionName)) {
            return JavaRunner.QUICK_CLEAN;
        } else if (ActionProvider.COMMAND_RUN.equals(actionName) || ActionProviderImpl.COMMAND_RUN_MAIN.equals(actionName)) {
            return JavaRunner.QUICK_RUN;
        } else if (ActionProvider.COMMAND_DEBUG.equals(actionName) || ActionProviderImpl.COMMAND_DEBUG_MAIN.equals(actionName)) {
            return JavaRunner.QUICK_DEBUG;
        } else if (ActionProvider.COMMAND_PROFILE.equals(actionName) || ActionProviderImpl.COMMAND_PROFILE_MAIN.equals(actionName)) {
            return JavaRunner.QUICK_PROFILE;
        } else if (ActionProvider.COMMAND_TEST.equals(actionName) || ActionProvider.COMMAND_TEST_SINGLE.equals(actionName) || SingleMethod.COMMAND_RUN_SINGLE_METHOD.equals(actionName)) {
            return JavaRunner.QUICK_TEST;
        } else if (ActionProvider.COMMAND_DEBUG_TEST_SINGLE.equals(actionName) || SingleMethod.COMMAND_DEBUG_SINGLE_METHOD.equals(actionName)) {
            return JavaRunner.QUICK_TEST_DEBUG;
        } else if (ActionProvider.COMMAND_PROFILE_TEST_SINGLE.equals(actionName)) {
            return JavaRunner.QUICK_TEST_PROFILE;
        }
        assert false : "Cannot convert " + actionName + " to quick actions.";
        return null;
    }
}
