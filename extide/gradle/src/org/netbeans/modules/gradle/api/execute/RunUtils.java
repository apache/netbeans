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
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Path;
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

import org.netbeans.modules.gradle.spi.actions.ReplaceTokenProvider;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.function.Function;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;

import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.gradle.ProjectTrust;
import org.netbeans.modules.gradle.actions.ActionToTaskUtils;
import org.netbeans.modules.gradle.api.execute.GradleDistributionManager.GradleDistribution;
import org.netbeans.modules.gradle.api.execute.RunConfig.ExecFlag;
import static org.netbeans.modules.gradle.customizer.GradleExecutionPanel.HINT_JDK_PLATFORM;
import org.netbeans.modules.gradle.execute.ConfigurableActionProvider;
import org.netbeans.modules.gradle.spi.GradleSettings;
import org.netbeans.modules.gradle.execute.ProjectConfigurationSupport;
import org.netbeans.modules.gradle.options.GradleExperimentalSettings;
import org.netbeans.modules.gradle.spi.actions.ProjectActionMappingProvider;
import org.netbeans.modules.gradle.spi.execute.GradleDistributionProvider;
import org.netbeans.modules.gradle.spi.execute.JavaRuntimeManager;
import org.netbeans.modules.gradle.spi.execute.JavaRuntimeManager.JavaRuntime;
import org.netbeans.spi.project.AuxiliaryProperties;
import org.netbeans.spi.project.ProjectConfiguration;
import org.netbeans.spi.project.SingleMethod;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.BaseUtilities;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
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
    public static final String PROP_INCLUDE_OPEN_PROJECTS = "include.open.projects"; //NOI18N
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
        if (files.isEmpty()) {
            files.addAll(lookup.lookupAll(FileObject.class));
        }
        return files.toArray(new FileObject[0]);
    }
    
    /**
     * Testing support: test can replace and mock the execution.
     */
    static Function<RunConfig, GradleExecutor> EXECUTOR_FACTORY = GradleDaemonExecutor::new;
    
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
        
        if (config.getExecConfig() == null) {
            // enhance the RunConfig with the active Configuration.
            config = new RunConfig(config.getProject(), config.getActionName(), 
                    config.getTaskDisplayName(), config.getExecFlags(), config.getCommandLine(), 
                    ProjectConfigurationSupport.getEffectiveConfiguration(config.getProject(), Lookup.EMPTY)
            );
        }

        GradleExecutor exec = EXECUTOR_FACTORY.apply(config);
        ExecutorTask task = executeGradleImpl(config.getTaskDisplayName(), exec, initialOutput);
        GRADLE_TASKS.put(config, exec);

        return exec.createTask(task);
    }

    /**
     * Create Gradle execution configuration (context). It applies the default
     * setting from the project and the Global Gradle configuration on the
     * command line.
     *
     * @param project The Gradle project
     * @param action The name of the IDE action that's going to be executed
     * @param displayName The display name of the output tab
     * @param flags Execution flags.
     * @param args Gradle command line arguments
     * @return the Gradle execution configuration.
     * @since 1.5
     */
    public static RunConfig createRunConfig(Project project, String action, String displayName, Set<ExecFlag> flags, String... args) {
        return createRunConfig(project, action, displayName, Lookup.EMPTY, ProjectConfigurationSupport.getEffectiveConfiguration(project, Lookup.EMPTY), flags, args);
    }
    
    /**
     * Create Gradle execution configuration (context). It applies the default
     * setting from the project and the Global Gradle configuration on the
     * command line. The passed {@link GradleExecConfiguration} is recorded in
     * the {@link RunConfig#getExecConfig()}. If {@code null} is passed, the
     * active project's configuration is recorded.
     * 
     * @param project The Gradle project
     * @param action The name of the IDE action that's going to be executed
     * @param displayName The display name of the output tab
     * @param flags Execution flags.
     * @param args Gradle command line arguments
     * @return the Gradle execution configuration.
     * @param context action infocation context
     * @param cfg the desired configuration, or {@code null}.
     * @since 2.13
     */
    public static RunConfig createRunConfig(Project project, String action, String displayName, Lookup context, 
            GradleExecConfiguration cfg, Set<ExecFlag> flags, String... args) {
        if (cfg == null) {
            cfg = ProjectConfigurationSupport.getEffectiveConfiguration(project, context);
        }
        GradleBaseProject gbp = GradleBaseProject.get(project);

        GradleCommandLine syscmd = GradleCommandLine.getDefaultCommandLine();
        GradleCommandLine prjcmd = getDefaultCommandLine(project);
        GradleCommandLine basecmd = syscmd;
        if (prjcmd != null) {
            basecmd = GradleCommandLine.combine(syscmd, prjcmd);
        }

        if (isIncludeOpenProjectsEnabled(project)) {
            GradleCommandLine include = getIncludedOpenProjects(project);
            basecmd = GradleCommandLine.combine(basecmd, include);
        }

        // Make sure we only exclude 'test' and 'check' by default if the
        // project allows this (has these tasks or root project with sub projects).
        validateExclude(basecmd, gbp, GradleCommandLine.TEST_TASK);
        validateExclude(basecmd, gbp, GradleCommandLine.CHECK_TASK); //NOI18N


        GradleCommandLine cmd = GradleCommandLine.combine(basecmd, new GradleCommandLine(args));
        RunConfig ret = new RunConfig(project, action, displayName, flags, cmd, cfg);
        return ret;
    }
    
    /**
     * Finds an action mapping for the given action and (optional) configuration. The configuration may be {@code null},
     * which means the project's active configuration.
     * 
     * @param project the project
     * @param action action name
     * @param cfg optional configuration or {@code null}
     * @return action mapping instance, or {@code null}, if the action is not defined for the project or is disabled.
     * @since 2.28
     */
    public static ActionMapping findActionMapping(Project project, String action, GradleExecConfiguration cfg) {
        return ActionToTaskUtils.getActiveMapping(action, project, cfg);
    }

    /**
     * Create Gradle execution configuration (context). It applies the specified configuration
     * (default / active, if the parameter is null). The RunConfig is further configured using the
     * action mapping for the project action.
     * 
     * @param project The Gradle project
     * @param action The name of the IDE action 
     * @param displayName The display name of the output tab
     * @param flags Execution flags.
     * @param args Gradle command line arguments
     * @return the Gradle execution configuration.
     * @param context action infocation context
     * @param cfg the desired configuration, or {@code null}.
     * @since 2.28
     */
    public static RunConfig createRunConfigForAction(Project project, String action, String displayName, Lookup context, 
            GradleExecConfiguration cfg, Set<ExecFlag> flags, boolean allowDisabled, String... args) {
        NbGradleProject gp = NbGradleProject.get(project);
        GradleExecConfiguration execCfg = ProjectConfigurationSupport.getEffectiveConfiguration(project, context);
        return ProjectConfigurationSupport.executeWithConfiguration(project, execCfg, () -> {
            ActionMapping mapping = ActionToTaskUtils.getActiveMapping(action, project, execCfg);
            if (ActionMapping.isDisabled(mapping) && !allowDisabled) {
                return null;
            }
            if (!ActionToTaskUtils.isActionEnabled(action, mapping, project, context) && !allowDisabled) {
                return null;
            }
            String argLine = mapping.getArgs();
            final StringWriter writer = new StringWriter();
            PrintWriter out = new PrintWriter(writer);
            final String[] evalArgs = RunUtils.evaluateActionArgs(project, action, argLine, Lookup.EMPTY);
            return RunUtils.createRunConfig(project, action, displayName, Lookup.EMPTY, execCfg, flags, evalArgs);
        });
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
     * @deprecated use {@link #createRunConfig(org.netbeans.api.project.Project, java.lang.String, java.lang.String, java.util.Set, java.lang.String...) } instead.
     */
    @Deprecated
    public static RunConfig createRunConfig(Project project, String action, String displayName, String[] args) {
        return createRunConfig(project, action, displayName, EnumSet.of(RunConfig.ExecFlag.REPEATABLE), args);
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

    /**
     * Returns the GradleDistribution for the given project which is compatible
     * with the JVM runtime, the IDE is running on
     * .
     * @param prj the project
     * @return The project Gradle distribution or the current tooling
     *         distribution if the runtime JVM is not supported by the project
     *         specified distribution.
     * @since 2.23
     */
    public static GradleDistribution getCompatibleGradleDistribution(Project prj) {
        GradleDistributionProvider pvd = prj.getLookup().lookup(GradleDistributionProvider.class);
        GradleDistribution ret = pvd != null ? pvd.getGradleDistribution() : GradleDistributionManager.get().defaultDistribution();
        ret = ret != null ? ret : GradleDistributionManager.get().defaultDistribution();
        ret = ret.isCompatibleWithSystemJava() ? ret : GradleDistributionManager.get().defaultDistribution();
        return ret;

    }

    private static ExecutorTask executeGradleImpl(String runtimeName, final GradleExecutor exec, String initialOutput) {
        InputOutput io = exec.getInputOutput();
        ExecutorTask task = ExecutionEngine.getDefault().execute(runtimeName, exec,
                new ProxyNonSelectableInputOutput(io));
        if (initialOutput != null) {
            try {
                if (IOColorPrint.isSupported(io) && IOColors.isSupported(io)) {
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

    /**
     * Compile on Save is a yet to be implemented feature. It's implemetation
     * details and necessity is still a question. Most probably this method is
     * in a wrong place here., kepping it around for binary compatibility only.
     *
     * @param project
     * @return
     * @deprecated In order to discourage the usage of this call.
     */
    @Deprecated
    public static boolean isCompileOnSaveEnabled(Project project) {
        return isOptionEnabled(project, PROP_COMPILE_ON_SAVE, false);
    }

    public static boolean isAugmentedBuildEnabled(Project project) {
        return isOptionEnabled(project, PROP_AUGMENTED_BUILD, true);
    }

    /**
     * Returns true if the include open projects checkbox is marked
     * in the project configuration.
     *
     * @param project the given project.
     * @return true if the settings has been enabled.
     * @since 1.5
     */
    public static boolean isIncludeOpenProjectsEnabled(Project project) {
        return isOptionEnabled(project, PROP_INCLUDE_OPEN_PROJECTS, false);
    }

    /**
     * Check if the given project is trusted for execution. If the project is not
     * trusted invoking this method can ask for temporal trust for one execution
     * only by displaying a dialog.
     * <p>
     * <b>Starting with version 2.8</b>, the presence and order of individual options
     * can be customized by branding. This allows integrators to select appropriate choices.
     * The supported options are:
     * <ul>
     * <li>{@code org.netbeans.modules.gradle.api.execute.TrustProjectOption.TrustOnce} - trusts just in this session. Equivalent of {@link ProjectTrust#trustProject(org.netbeans.api.project.Project, boolean)} 
     * called with <code>permanently = false</code>.
     * <li>{@code org.netbeans.modules.gradle.api.execute.TrustProjectOption.PermanentTrust} - trusts the project permanently. Equivalent of {@link ProjectTrust#trustProject(org.netbeans.api.project.Project, boolean)} 
     * called with {@code permanently = true}. This option is only present if the passed {@code project} is not {@code null}and has {@link ProjectInformation} in it.
     * <li>{@code org.netbeans.modules.gradle.api.execute.TrustProjectOption.RunAlways} - sets total trust in gradle scripts, see {@link GradleSettings#setGradleExecutionRule}.
     * </ul>
     * The presence and order of those options can be altered by keys in {@code Bundle.properties} branding in {@code org.netbeans.modules.gradle.api.execute} package:
     * <ol>
     * <li>if the key does not exist, is empty or has a non-numeric value, the option will be hidden.
     * <li>negative value flags the default (pre-selected) option. Value is treated as positive for ordering. If more negative values are present one of them is selected.
     * <li>the visible options will be selected in ascending order of their numeric values
     * <li>Cancel is always present and is the last one.
     * </ol>
     * <div class="nonnormative">
     * Example of branding: 
     * {@snippet file="org/netbeans/modules/gradle/api/execute/TestBundle.properties" region="trustDialogBranding"}
     * This branding enables all supported options, and will make the "Trust Permanently" the default one.
     * </div>
     * 
     * @param project the project to be checked
     * @param interactive ask for permission from UI.
     * @since 2.8 the dialog options can be customized.
     * @return if the execution is trusted.
     */
    public static boolean isProjectTrusted(Project project, boolean interactive) {
        boolean ret = GradleSettings.getDefault().getGradleExecutionRule() == GradleSettings.GradleExecutionRule.ALWAYS
                || ProjectTrust.getDefault().isTrusted(project);
        if (ret == false && interactive) {
            Boolean q = askToTrustProject(project);
            if (Boolean.FALSE == q) {
                return false;
            }
            ProjectTrust.getDefault().trustProject(project, Boolean.TRUE == q);
            ret = true;
        }
        return ret;
    }

    /**
     * Prefix for option labels. THe prefix must match the messages listed below.
     */
    private static final String OPTION_MESSAGE_PREFIX = "TrustProjectPanel."; // NOI18N
    
    /**
     * Prefix for the 'branding API' keys. The resource bundle is used to enable / disable / order
     * choices in the dialog.
     */
    private static final String BRANDING_API_PREFIX = "org.netbeans.modules.gradle.api.execute.TrustProjectOption."; // NOI18N

    @Messages({
        "ProjectTrustDlg.TITLE=Not a Trusted Project",
        "# {0} = Project name",
        "TrustProjectPanel.INFO=<html><p>NetBeans is about to invoke a Gradle build process of the project: <b>{0}</b>.</p>"
            + " <p>Executing Gradle can be potentially un-safe as it"
            + " allows arbitrary code execution.</p>",
        "TrustProjectPanel.INFO_UNKNOWN=<html><p>NetBeans is about to invoke a Gradle build process.</p>"
            + " <p>Executing Gradle can be potentially un-safe as it"
            + " allows arbitrary code execution.</p>",
        "# Labels for trust dialog opptions. Do not change the \"TrustProjectPanel.\" prefix",
        "TrustProjectPanel.TrustOnce=&OK",
        "TrustProjectPanel.PermanentTrust=Trust &Permanently",
        "TrustProjectPanel.RunAlways=Trust &All Projects",
    })
    /**
     * Asks the user to trust the project, returns tri-state answer.
     * <ul>
     * <li>Boolean.TRUE to permanently trust the project.
     * <li>Boolean.FALSE to not run the project
     * <li>{@code null} to trust the project, but not mark it as trusted.
     * </ul>
     */
    private static Boolean askToTrustProject(Project project) {
        ProjectInformation info = project != null ? project.getLookup().lookup(ProjectInformation.class) : null;
        String msg;
        Object[] options;
        Object defaultOption;
        String permanentOption = Bundle.TrustProjectPanel_PermanentTrust();
        String runAlways = Bundle.TrustProjectPanel_RunAlways();
        String ok = Bundle.TrustProjectPanel_TrustOnce();

        if (info == null) {
            msg = Bundle.TrustProjectPanel_INFO_UNKNOWN();
        } else {
            msg = Bundle.TrustProjectPanel_INFO(info.getDisplayName());
        }
        Pair<Object[], Object> opts = brandedOptions(info != null);
        options = opts.first();
        defaultOption = opts.second();
        NotifyDescriptor dsc = new NotifyDescriptor(msg, Bundle.ProjectTrustDlg_TITLE(), 
                NotifyDescriptor.OK_CANCEL_OPTION, NotifyDescriptor.QUESTION_MESSAGE, 
                options, defaultOption);
        
        Object result = DialogDisplayer.getDefault().notify(dsc);
        if (result == runAlways) {
             GradleSettings.getDefault().setGradleExecutionRule(GradleSettings.GradleExecutionRule.ALWAYS);
             return null;
        }
        if (result == ok) {
            return null;
        } else if (result == permanentOption) {
            return true;
        } else {
            return false;
        }
    }
    
    private static Pair<Object[], Object> brandedOptions(boolean allowPermanent) {
        Map<Object, Integer>  options = new HashMap<>();
        Object def = null;
        for (String opt : TRUST_DIALOG_OPTION_IDS) {
            if (opt == TRUST_DIALOG_OPTION_IDS.get(2) && !allowPermanent) {
                continue;
            }
            String key = BRANDING_API_PREFIX + opt;
            String m;
            int pos;
            try  {
                m = NbBundle.getMessage(RunUtils.class, OPTION_MESSAGE_PREFIX + opt);
                String v = NbBundle.getMessage(RunUtils.class, key);
                if ("".equals(v)) {
                    continue;
                }
                pos = Integer.parseInt(v);
                if (pos == 0) {
                    continue;
                } else if (pos < 0) {
                    def = m;
                    pos = -pos;
                }
            } catch (MissingResourceException ex) {
                // just ignore
                continue;
            } catch (IllegalArgumentException ex) {
                // non-numeric
                continue;
            }
            if (def == null) {
                def = m;
            }
            options.put(m, pos);
        }
        if (options.isEmpty()) {
            // fall back to the default:
            return Pair.of(new Object[] { 
                    Bundle.TrustProjectPanel_TrustOnce(),
                    Bundle.TrustProjectPanel_PermanentTrust(),
                    DialogDescriptor.CANCEL_OPTION
                }, 
                Bundle.TrustProjectPanel_TrustOnce()
            );
        }
        List<Object> ordered = new ArrayList<>(options.keySet());
        ordered.sort((a, b) -> options.get(a) - options.get(b));
        ordered.add(DialogDescriptor.CANCEL_OPTION);
        return Pair.of(ordered.toArray(new Object[0]), def);
    };
    
    private static final List<String> TRUST_DIALOG_OPTION_IDS = Arrays.asList(
        "TrustOnce", "PermanentTrust", "RunAlways"
    );
    
    public static GradleCommandLine getDefaultCommandLine(Project project) {
        String args = NbGradleProject.getPreferences(project, true).get(PROP_DEFAULT_CLI, null);
        return args != null ? new GradleCommandLine(args) : null;
    }

    @Deprecated
    public static File evaluateGradleDistribution(Project project, boolean forceCompatibility) {

        GradleDistributionProvider pvd = project != null ? project.getLookup().lookup(GradleDistributionProvider.class) : null;
        GradleDistribution dist = pvd != null ? pvd.getGradleDistribution() : null;
        if (dist != null && (dist.isCompatibleWithSystemJava() || !forceCompatibility)) {
            return dist.getDistributionDir();
        } else {
            GradleSettings settings = GradleSettings.getDefault();
            dist = GradleDistributionManager.get(dist != null ? dist.getGradleUserHome() : settings.getGradleUserHome()).defaultDistribution();
            return dist.getDistributionDir();
        }
    }

    private static boolean isOptionEnabled(Project project, String option, boolean defaultValue) {
        Project root = ProjectUtils.rootOf(project);
        GradleBaseProject gbp = GradleBaseProject.get(root);
        if (gbp != null) {
            String value = gbp.getNetBeansProperty(option);
            if (value != null) {
                return Boolean.valueOf(value);
            } else {
                return NbGradleProject.getPreferences(root, false).getBoolean(option, defaultValue);
            }
        }
        return false;
    }

    /**
     * Replace the tokens in <code>argLine</code> provided by the <code>project</code> for
     * the action using the given context;
     * 
     * @param project the that holds the {@link ReplaceTokenProvider}-s.
     * @param argLine a string which might hold tokens to be replaced.
     * @param action  the action name to do the replacement for. It can be <code>null</code>
     * @param context the context of the action.
     *
     * @return the <code>argLine</code> where the tokens are replaced.
     * @since 2.6
     */
    public static String[] evaluateActionArgs(Project project, String action, String argLine, Lookup context) {
        ReplaceTokenProvider tokenProvider = project.getLookup().lookup(ReplaceTokenProvider.class);
        String repLine = ReplaceTokenProvider.replaceTokens(argLine, tokenProvider.createReplacements(action, context));
        return BaseUtilities.parseParameters(repLine);
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
    
    /**
     * Returns an action mapping provider for the specified project and context. Specifically 
     * supports {@link ProjectConfiguration}s, so the returned provider will 
     * @param p the project
     * @param context the action invocation context
     * @return action provider suitable for the project/context.
     * @since 2.14
     */
    public static ProjectActionMappingProvider findActionProvider(Project p, Lookup context) {
        ConfigurableActionProvider cap = p.getLookup().lookup(ConfigurableActionProvider.class);
        if (cap == null) {
            return p.getLookup().lookup(ProjectActionMappingProvider.class);
        }
        GradleExecConfiguration cfg = ProjectConfigurationSupport.getEffectiveConfiguration(p, context);
        return cap.findActionProvider(cfg.getId());
    }

    /**
     * Returns the active platform id, platform pair used by the project.
     * The platform can be {@code null} if the active project platform is broken.
     *
     * As this module is no longer dependent on the java platform module,
     * this method always returns {@code null} as a platform since 2.3
     *
     * @param activePlatformId the name of platform used by Ant script or null
     * for default platform.
     * @return Pair of {@literal <acivePlatformId, null>}
     *
     * @deprecated This method has been deprecated without having a
     * replacement in this module. The current implementation serves
     * binary compatibility purposes only.
     */
    @Deprecated
    public static Pair getActivePlatform(final String activePlatformId) {
        return Pair.of(activePlatformId, null);
    }

    /**
     * Returns the active platform id, platform pair used by the project.
     * The platform can be null if the active project platform is broken.
     *
     * As this module is no longer dependent on the java platform module,
     * this method always returns {@code null} as a platform since 2.3
     *
     * @param project the project to check.
     * @return Pair of {@literal <"deprecated", null>}
     *
     * @deprecated This method has been deprecated without having a
     * replacement in this module. The current implementation serves
     * binary compatibility purposes only.
     */
    @Deprecated
    public static Pair getActivePlatform(Project project) {
        return getActivePlatform("deprecated"); //NOI18N
    }

    /**
     * Return the current active JavaRuntime used by the specified project.
     * JavaRuntime is defined in the root project level. If there is no
     * runtime specified on the root project, then this method would return the
     * default runtime. Usually the one, which is used by the IDE.
     * <p>
     * It is possible that the ID representing the project runtime, has no
     * associated runtime in the IDE. In this case a broken JavaRuntime would be
     * returned with the ID, that would be used.
     *
     * @param project the project which root project specifies the runtime.
     * @return the JavaRuntime to be used by the project, could be broken, but
     *         not {@code null}
     *
     * @since 2.32
     */
    public static JavaRuntime getActiveRuntime(Project project) {
        return ProjectManager.mutex().readAccess(() -> {
            GradleExperimentalSettings experimental = GradleExperimentalSettings.getDefault();

            Project root = ProjectUtils.rootOf(project);
            AuxiliaryProperties aux = root.getLookup().lookup(AuxiliaryProperties.class);
            String id = aux.get(HINT_JDK_PLATFORM, true);
            if (id == null) {
                return experimental.getDefaultJavaRuntime();
            } else {
                JavaRuntimeManager mgr = Lookup.getDefault().lookup(JavaRuntimeManager.class);
                Map<String, JavaRuntime> runtimes = mgr.getAvailableRuntimes();
                if (runtimes.containsKey(id)) {
                    return runtimes.get(id);
                }
                return JavaRuntimeManager.createJavaRuntime(id, null);
            }
        });
    }

    /**
     * Sets the active JavaRuntime on the specified project root.
     * 
     * @param project the project , which root project shall be set the runtime on
     * @param runtime The JavaRuntime to activate on the project or {@code null}
     *                can be used to set the default runtime.
     * 
     * @since 2.32
     */
    public static void setActiveRuntime(Project project, JavaRuntime runtime) {
        ProjectManager.mutex().postWriteRequest(() -> {
            GradleExperimentalSettings experimental = GradleExperimentalSettings.getDefault();
            
            Project root = ProjectUtils.rootOf(project);
            AuxiliaryProperties aux = root.getLookup().lookup(AuxiliaryProperties.class);
            String id = (runtime != null) && experimental.getDefaultJavaRuntime() != runtime ? runtime.getId() : null;
            aux.put(HINT_JDK_PLATFORM, id, true);
        });
    }

    static GradleCommandLine getIncludedOpenProjects(Project project) {
        GradleCommandLine ret = new GradleCommandLine();
        Set<File> openRoots = new HashSet<>();
        for (Project openProject : OpenProjects.getDefault().getOpenProjects()){
            GradleBaseProject gpb = GradleBaseProject.get(openProject);
            if (gpb != null) {
                openRoots.add(gpb.getRootDir());
            }
        }
        GradleBaseProject gbp = GradleBaseProject.get(project);
        if (gbp != null) {
            //Removing ourself
            openRoots.remove(gbp.getRootDir());
            openRoots.removeAll(gbp.getIncludedBuilds().values());

            Path projectPath = gbp.getProjectDir().toPath();
            for (File openRoot : openRoots) {
                Path root = openRoot.toPath();
                String ib = root.toString();
                try {
                    Path rel = projectPath.relativize(root);

                    if (rel.getNameCount() < root.getNameCount()) {
                        ib = rel.toString();
                    }
                } catch (IllegalArgumentException ex) {
                    // Relative path cannot be computed, just use the full path then.
                }
                ret.addParameter(GradleCommandLine.Parameter.INCLUDE_BUILD, ib);
            }
        }
        return ret;
    }

}
