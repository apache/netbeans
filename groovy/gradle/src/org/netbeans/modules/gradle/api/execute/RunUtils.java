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

package org.netbeans.modules.gradle.api.execute;

import java.io.File;
import org.netbeans.modules.gradle.api.GradleBaseProject;
import org.netbeans.modules.gradle.api.NbGradleProject;
import org.netbeans.modules.gradle.execute.GradleDaemonExecutor;
import org.netbeans.modules.gradle.execute.GradleExecutor;
import org.netbeans.modules.gradle.execute.ProxyNonSelectableInputOutput;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.Project;
import org.openide.LifecycleManager;
import org.openide.execution.ExecutionEngine;
import org.openide.execution.ExecutorTask;
import org.openide.util.Lookup;
import org.openide.windows.IOColorPrint;
import org.openide.windows.IOColors;
import org.openide.windows.InputOutput;

import static org.netbeans.modules.gradle.api.execute.Bundle.*;
import org.netbeans.modules.gradle.spi.actions.ReplaceTokenProvider;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.prefs.Preferences;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.platform.Specification;

import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.gradle.GradleDistributionManager;
import org.netbeans.modules.gradle.spi.GradleSettings;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.SingleMethod;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.NbBundle;
import org.openide.util.Pair;

/**
 * Utilities, that allow to invoke Gradle.
 * @since 1.0
 * @author Laszlo Kishalmi
 */
public final class RunUtils {

    private static final Logger LOG = Logger.getLogger(RunUtils.class.getName());
    public static final String PROP_JDK_PLATFORM = "jdkPlatform"; //NOI18N
    public static final String PROP_COMPILE_ON_SAVE = "compile.on.save"; //NOI18N
    public static final String PROP_AUGMENTED_BUILD = "augmented.build"; //NOI18N
    public static final String PROP_DEFAULT_CLI = "gradle.cli"; //NOI18N

    private RunUtils() {}
    private static final Map<RunConfig, GradleExecutor> GRADLE_TASKS = new WeakHashMap<>();

    public static FileObject extractFileObjectfromLookup(Lookup lookup) {
        FileObject[] fos = extractFileObjectsfromLookup(lookup);
        return fos.length > 0 ? fos[0] : null;
    }

    public static FileObject[] extractFileObjectsfromLookup(Lookup lookup) {
        List<FileObject> files = new ArrayList<>();
        Iterator<? extends DataObject> it = lookup.lookupAll(DataObject.class).iterator();
        while (it.hasNext()) {
            DataObject d = it.next();
            FileObject f = d.getPrimaryFile();
            files.add(f);
        }
        Collection<? extends SingleMethod> methods = lookup.lookupAll(SingleMethod.class);
        if (methods.size() == 1) {
            SingleMethod method = methods.iterator().next();
            files.add(method.getFile());
        }
        return files.toArray(new FileObject[files.size()]);
    }

    /**
     * Executes a Gradle build with the given configuration. It can also take an
     * initial message, which is printed to the output tab before the actual
     * execution takes over the output handling.
     *
     * @param config the configuration of the Gradle execution
     * @param initialOutput the initial message to be displayed,
     *        can be {@code null} for no message.
     * @return The Gradle Execution task
     */
    public static ExecutorTask executeGradle(RunConfig config, String initialOutput) {
        LifecycleManager.getDefault().saveAll();

        GradleExecutor exec = new GradleDaemonExecutor(config);
        ExecutorTask task = executeGradleImpl(config.getTaskDisplayName(), exec, initialOutput);
        GRADLE_TASKS.put(config, exec);

        return task;
    }

    /**
     * Create Gradle execution configuration (context). It applies the default
     * setting from the project and the Global Gradle configuration on the
     * command line.
     *
     * @param project The Gradle project
     * @param action The name of the IDE action that's going to be executed
     * @param displayName The display name of the output tab
     * @param args Gradle command line arguments
     * @return the Gradle execution configuration.
     */
    public static RunConfig createRunConfig(Project project, String action, String displayName, String[] args) {
        GradleBaseProject gbp = GradleBaseProject.get(project);

        GradleCommandLine syscmd = GradleCommandLine.getDefaultCommandLine();
        GradleCommandLine prjcmd = getDefaultCommandLine(project);
        GradleCommandLine basecmd = syscmd;
        if (prjcmd != null) {
            basecmd = GradleCommandLine.combine(syscmd, prjcmd);
        }

        // Make sure we only exclude 'test' and 'check' by default if the
        // project allows this (has these tasks or root project with sub projects).
        validateExclude(basecmd, gbp, GradleCommandLine.TEST_TASK);
        validateExclude(basecmd, gbp, GradleCommandLine.CHECK_TASK); //NOI18N


        GradleCommandLine cmd = GradleCommandLine.combine(basecmd, new GradleCommandLine(args));
        RunConfig ret = new RunConfig(project, action, displayName, EnumSet.of(RunConfig.ExecFlag.REPEATABLE), cmd);
        return ret;
    }

    /**
     * Enable plugins to Cancel a currently running Gradle execution.
     * 
     * @param config the RunConfig with which the Gradle execution has been started.
     * @return {@code true} if the current execution was cancelled successfully,
     *         {@code false} if the execution was already cancelled or it cannot
     *         be cancelled for some reason.
     * @since 1.4
     */
    public static boolean cancelGradle(RunConfig config) {
        GradleExecutor exec = GRADLE_TASKS.get(config);
        return exec != null ? exec.cancel() : false;
    }

    private static ExecutorTask executeGradleImpl(String runtimeName, final GradleExecutor exec, String initialOutput) {
        InputOutput io = exec.getInputOutput();
        ExecutorTask task = ExecutionEngine.getDefault().execute(runtimeName, exec,
                new ProxyNonSelectableInputOutput(io));
        if (initialOutput != null) {
            try {
                if (IOColorPrint.isSupported(io)) {
                    IOColorPrint.print(io, initialOutput, IOColors.getColor(io, IOColors.OutputType.LOG_DEBUG));
                } else {
                    io.getOut().println(initialOutput);
                }
            } catch (IOException ex) {
                LOG.log(Level.WARNING, "Can't write initial output: " + initialOutput, ex);
            }
        }
        exec.setTask(task);
        return task;
    }

    public static boolean isCompileOnSaveEnabled(Project project) {
        return isOptionEnabled(project, PROP_COMPILE_ON_SAVE, false);
    }

    public static boolean isAugmentedBuildEnabled(Project project) {
        return isOptionEnabled(project, PROP_AUGMENTED_BUILD, true);
    }

    public static GradleCommandLine getDefaultCommandLine(Project project) {
        String args = NbGradleProject.getPreferences(project, true).get(PROP_DEFAULT_CLI, null);
        return args != null ? new GradleCommandLine(args) : null;
    }

    public static File evaluateGradleDistribution(Project project, boolean forceCompatibility) {
        File ret = null;

        GradleSettings settings = GradleSettings.getDefault();
        GradleDistributionManager mgr = GradleDistributionManager.get(settings.getGradleUserHome());

        GradleBaseProject gbp = GradleBaseProject.get(project);

        if ((gbp != null) && settings.isWrapperPreferred()) {
            GradleDistributionManager.NbGradleVersion ngv = mgr.evaluateGradleWrapperDistribution(gbp.getRootDir());
            if ( (ngv != null) && forceCompatibility && !ngv.isCompatibleWithSystemJava()) {
                ngv = mgr.defaultToolingVersion();
            }
            if ((ngv != null) && ngv.isAvailable()) {
                ret = ngv.distributionDir();
            }
        }

        if ((ret == null) && settings.useCustomGradle() && !settings.getDistributionHome().isEmpty()) {
            File f = FileUtil.normalizeFile(new File(settings.getDistributionHome()));
            if (f.isDirectory()) {
                ret = f;
            }
        }
        if (ret == null) {
            GradleDistributionManager.NbGradleVersion ngv = mgr.createVersion(settings.getGradleVersion());
            if ( (ngv != null) && forceCompatibility && !ngv.isCompatibleWithSystemJava()) {
                ngv = mgr.defaultToolingVersion();
            }
            if ((ngv != null) && ngv.isAvailable()) {
                ret = ngv.distributionDir();
            }
        }
        return ret;
    }


    private static boolean isOptionEnabled(Project project, String option, boolean defaultValue) {
        GradleBaseProject gbp = GradleBaseProject.get(project);
        if (gbp != null) {
            String value = gbp.getNetBeansProperty(option);
            if (value != null) {
                return Boolean.getBoolean(value);
            } else {
                return NbGradleProject.getPreferences(project, false).getBoolean(option, defaultValue);
            }
        }
        return false;
    }

    /**
     * Validate if a certain excluded task can be applied on a project.
     * Used for skipping 'test' and 'check' tasks.
     */
    private static void validateExclude(GradleCommandLine cmd, GradleBaseProject gbp, String task) {
        boolean exclude = gbp.getTaskNames().contains(task) || (gbp.isRoot() && !gbp.getSubProjects().isEmpty());
        exclude &= cmd.getExcludedTasks().contains(task) && !cmd.getTasks().contains(task);
        if (exclude) {
            cmd.addParameter(GradleCommandLine.Parameter.EXCLUDE_TASK, task);
        } else {
            cmd.removeParameter(GradleCommandLine.Parameter.EXCLUDE_TASK, task);
        }
    }

    public static ReplaceTokenProvider simpleReplaceTokenProvider(final String token, final String value) {
        return new ReplaceTokenProvider() {
            @Override
            public Set<String> getSupportedTokens() {
                return Collections.singleton(token);
            }

            @Override
            public Map<String, String> createReplacements(String action, Lookup context) {
                return Collections.singletonMap(token, value);
            }
        };
    }

    @NbBundle.Messages({
        "# {0} - artifactId", "TXT_Run=Run ({0})",
        "# {0} - artifactId", "TXT_Debug=Debug ({0})",
        "# {0} - artifactId", "TXT_ApplyCodeChanges=Apply Code Changes ({0})",
        "# {0} - artifactId", "TXT_Profile=Profile ({0})",
        "# {0} - artifactId", "TXT_Test=Test ({0})",
        "# {0} - artifactId", "TXT_Build=Build ({0})"
    })
    private static String taskName(String action, Lookup lkp) {
        String title;
        DataObject dobj = lkp.lookup(DataObject.class);
        String dobjName = dobj != null ? dobj.getName() : "";
        Project prj = lkp.lookup(Project.class);
        String prjLabel = prj != null ? ProjectUtils.getInformation(prj).getDisplayName() : "No Project on Lookup";
        switch (action) {
            case ActionProvider.COMMAND_RUN:
                title = TXT_Run(prjLabel);
                break;
            case ActionProvider.COMMAND_DEBUG:
                title = TXT_Debug(prjLabel);
                break;
            case ActionProvider.COMMAND_PROFILE:
                title = TXT_Profile(prjLabel);
                break;
            case ActionProvider.COMMAND_TEST:
                title = TXT_Test(prjLabel);
                break;
            case ActionProvider.COMMAND_RUN_SINGLE:
                title = TXT_Run(dobjName);
                break;
            case ActionProvider.COMMAND_DEBUG_SINGLE:
            case ActionProvider.COMMAND_DEBUG_TEST_SINGLE:
                title = TXT_Debug(dobjName);
                break;
            case ActionProvider.COMMAND_PROFILE_SINGLE:
            case ActionProvider.COMMAND_PROFILE_TEST_SINGLE:
                title = TXT_Profile(dobjName);
                break;
            case ActionProvider.COMMAND_TEST_SINGLE:
                title = TXT_Test(dobjName);
                break;
            case "debug.fix":
                title = TXT_ApplyCodeChanges(prjLabel);
                break;
            default:
                title = TXT_Build(prjLabel);
        }
        return title;
    }

 /**
     * Returns the active platform used by the project or null if the active
     * project platform is broken.
     * @param activePlatformId the name of platform used by Ant script or null
     * for default platform.
     * @return active {@link JavaPlatform} or null if the project's platform
     * is broken
     */
    public static Pair<String, JavaPlatform> getActivePlatform(final String activePlatformId) {
        final JavaPlatformManager pm = JavaPlatformManager.getDefault();
        if (activePlatformId == null) {
            JavaPlatform p = pm.getDefaultPlatform();
            return Pair.of(p.getProperties().get("platform.ant.name"), p);
        } else {
            JavaPlatform[] installedPlatforms = pm.getPlatforms(null, new Specification("j2se", null)); //NOI18N
            for (JavaPlatform installedPlatform : installedPlatforms) {
                String antName = installedPlatform.getProperties().get("platform.ant.name"); //NOI18N
                if (antName != null && antName.equals(activePlatformId)) {
                    return Pair.of(activePlatformId, installedPlatform);
                }
            }
            return Pair.of(activePlatformId, null);
        }
    }

    public static Pair<String, JavaPlatform> getActivePlatform(Project project) {
        Preferences prefs = NbGradleProject.getPreferences(project, false);
        String platformId = prefs.get(PROP_JDK_PLATFORM, null);
        if (platformId == null) {
            GradleBaseProject gbp = GradleBaseProject.get(project);
            platformId = gbp != null ? gbp.getNetBeansProperty(PROP_JDK_PLATFORM) : null;
        }
        return getActivePlatform(platformId);
    }

    private static String stringsInCurly(List<String> l) {
        StringBuilder sb = new StringBuilder("(");
        Iterator<String> it = l.iterator();
        while (it.hasNext()) {
            sb.append(it.next());
            sb.append(it.hasNext() ? ", " : ")");
        }
        return sb.toString();
    }

}
