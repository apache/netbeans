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
package org.netbeans.modules.java.api.common.project;

import java.awt.Dialog;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.EventListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.lang.model.element.TypeElement;
import javax.swing.JButton;
import javax.swing.SwingUtilities;
import org.apache.tools.ant.module.api.support.ActionUtils;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.fileinfo.NonRecursiveFolder;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.project.runner.JavaRunner;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.java.api.common.SourceRoots;
import org.netbeans.modules.java.api.common.ant.UpdateHelper;
import org.netbeans.modules.java.api.common.project.ui.customizer.MainClassChooser;
import org.netbeans.modules.java.api.common.project.ui.customizer.MainClassWarning;
import org.netbeans.modules.java.api.common.util.CommonProjectUtils;
import org.netbeans.spi.project.ActionProvider;
import static org.netbeans.spi.project.ActionProvider.COMMAND_BUILD;
import static org.netbeans.spi.project.ActionProvider.COMMAND_COPY;
import static org.netbeans.spi.project.ActionProvider.COMMAND_DELETE;
import static org.netbeans.spi.project.ActionProvider.COMMAND_MOVE;
import static org.netbeans.spi.project.ActionProvider.COMMAND_REBUILD;
import static org.netbeans.spi.project.ActionProvider.COMMAND_RENAME;
import org.netbeans.spi.project.ProjectConfiguration;
import org.netbeans.spi.project.SingleMethod;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.ui.CustomizerProvider2;
import org.netbeans.spi.project.ui.support.DefaultProjectOperations;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.MouseUtils;
import org.openide.execution.ExecutorTask;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.openide.util.Parameters;

/**
 * The simple {@link ActionProvider} for Java based project.
 * @since 1.102
 * @author Tomas Zezula
 */
public final class JavaActionProvider implements ActionProvider {
    private static final Logger LOG = Logger.getLogger(JavaActionProvider.class.getName());
    private static final FileObject[] EMPTY = new FileObject[0];

    /*test*/ static volatile String unitTestingSupport_fixClasses;

    private final Project prj;
    private final UpdateHelper updateHelper;
    private final PropertyEvaluator eval;
    private final Function<String,ClassPath> classpaths;
    private final Map<String,Action> supportedActions;
    private final List<AntTargetInvocationListener> listeners;
    private final Supplier<? extends JavaPlatform> jpp;
    private final BiFunction<String,Lookup,Map<String,String>> additionalPropertiesProvider;
    private final BiFunction<String,Lookup,Set<String>> concealedPropertiesProvider;
    private final Supplier<? extends Set<? extends CompileOnSaveOperation>> cosOpsProvider;
    private ActionProviderSupport.UserPropertiesPolicy userPropertiesPolicy;

    private JavaActionProvider(
            @NonNull final Project project,
            @NonNull final UpdateHelper updateHelper,
            @NonNull final PropertyEvaluator evaluator,
            @NonNull final Function<String,ClassPath> classpaths,
            @NonNull final Collection<? extends Action> actions,
            @NonNull Supplier<? extends JavaPlatform> javaPlatformProvider,
            @NullAllowed final BiFunction<String,Lookup,Map<String,String>> additionalPropertiesProvider,
            @NullAllowed final BiFunction<String,Lookup,Set<String>> concealedPropertiesProvider,
            @NonNull final Supplier<? extends Set<? extends CompileOnSaveOperation>> cosOpsProvider,
            @NonNull final ActionProviderSupport.ModifiedFilesSupport mfs) {
        Parameters.notNull("project", project); //NOI18N
        Parameters.notNull("updateHelper", updateHelper);   //NOI18N
        Parameters.notNull("evaluator", evaluator); //NOI18N
        Parameters.notNull("classpaths", classpaths);   //NOI18N
        Parameters.notNull("actions", actions); //NOI18N
        Parameters.notNull("javaPlatformProvider", javaPlatformProvider);   //NOI18N
        Parameters.notNull("cosOpsProvider", cosOpsProvider);   //NOI18N
        Parameters.notNull("mfs", mfs);
        this.prj = project;
        this.updateHelper = updateHelper;
        this.eval = evaluator;
        this.classpaths = classpaths;
        final Map<String,Action> abn = new HashMap<>();
        for (Action action : actions) {
            abn.put(action.getCommand(), action);
        }
        this.supportedActions = Collections.unmodifiableMap(abn);
        this.listeners = new CopyOnWriteArrayList<>();
        this.listeners.add(new AntTargetInvocationListener() {
            @Override
            public void antTargetInvocationStarted(String command, Lookup context) {
            }
            @Override
            public void antTargetInvocationFinished(String command, Lookup context, int result) {
                if (result != 0 || COMMAND_CLEAN.equals(command)) {
                    mfs.resetDirtyList();
                }
            }
            @Override
            public void antTargetInvocationFailed(String command, Lookup context) {
            }
        });
        this.jpp = javaPlatformProvider;
        this.additionalPropertiesProvider = additionalPropertiesProvider;
        this.concealedPropertiesProvider = concealedPropertiesProvider;
        this.cosOpsProvider = cosOpsProvider;
    }

    public static enum CompileOnSaveOperation {
        UPDATE,
        EXECUTE
    }

    public static interface AntTargetInvocationListener extends EventListener {
        void antTargetInvocationStarted(final String command, final Lookup context);
        void antTargetInvocationFinished(final String command, final Lookup context, int result);
        void antTargetInvocationFailed(final String command, final Lookup context);
    }

    public static interface Action {
        String getCommand();
        boolean isEnabled(@NonNull final Context context);
        void invoke(@NonNull final Context context);
    }

    public static final class Context {
        private final Project project;
        private final UpdateHelper updateHelper;
        private final PropertyEvaluator eval;
        private final Function<String,ClassPath> classpaths;
        private final String command;
        private final Lookup lkp;
        private boolean doJavaChecks;
        private Set<? extends CompileOnSaveOperation> cosOpsCache;
        private ActionProviderSupport.UserPropertiesPolicy userPropertiesPolicy;
        private final Supplier<? extends JavaPlatform> jpp;
        private final BiFunction<String,Lookup,Map<String,String>> additionalPropertiesProvider;
        private final BiFunction<String,Lookup,Set<String>> concealedPropertiesProvider;
        private final Supplier<? extends Set<? extends CompileOnSaveOperation>> cosOpsProvider;
        private final Collection<? extends AntTargetInvocationListener> listeners;
        private final Properties properties;
        private final Set<String> concealedProperties;

        Context(
                @NonNull final Project project,
                @NonNull final UpdateHelper updateHelper,
                @NonNull final PropertyEvaluator eval,
                @NonNull final Function<String,ClassPath> classpaths,
                @NonNull final String command,
                @NonNull final Lookup lookup,
                @NullAllowed final ActionProviderSupport.UserPropertiesPolicy userPropertiesPolicy,
                @NullAllowed final Supplier<? extends JavaPlatform> jpp,
                @NullAllowed final BiFunction<String,Lookup,Map<String,String>> additionalPropertiesProvider,
                @NullAllowed final BiFunction<String,Lookup,Set<String>> concealedPropertiesProvider,
                @NonNull final Supplier<? extends Set<? extends CompileOnSaveOperation>> cosOpsProvider,
                @NonNull final Collection<? extends AntTargetInvocationListener> listeners) {
            this.project = project;
            this.updateHelper = updateHelper;
            this.eval = eval;
            this.classpaths = classpaths;
            this.command = command;
            this.lkp = lookup;
            this.doJavaChecks = true;
            this.userPropertiesPolicy = userPropertiesPolicy;
            this.jpp = jpp;
            this.additionalPropertiesProvider = additionalPropertiesProvider;
            this.concealedPropertiesProvider = concealedPropertiesProvider;
            this.cosOpsProvider = cosOpsProvider;
            this.listeners = listeners;
            this.properties = new Properties();
            this.concealedProperties = new HashSet<>();
        }

        @NonNull
        public String getCommand() {
            return command;
        }

        @NonNull
        public Lookup getActiveLookup() {
            return lkp;
        }

        @NonNull
        public Project getProject() {
            return project;
        }

        @NonNull
        public UpdateHelper getUpdateHelper() {
            return updateHelper;
        }

        @NonNull
        public PropertyEvaluator getPropertyEvaluator() {
            return eval;
        }

        @CheckForNull
        public JavaPlatform getActiveJavaPlatform() {
            return Optional.ofNullable(jpp)
                    .map((p) -> p.get())
                    .orElse(null);
        }

        public boolean doJavaChecks () {
            return doJavaChecks;
        }

        @NonNull
        public Set<? extends CompileOnSaveOperation> getCompileOnSaveOperations() {
            Set<? extends CompileOnSaveOperation> res = cosOpsCache;
            if (res == null) {
                res = cosOpsCache = cosOpsProvider.get();
            }
            return res;
        }

        @CheckForNull
        public ClassPath getProjectClassPath(@NonNull final String classPathId) {
            return classpaths.apply(classPathId);
        }

        @CheckForNull
        public String getProperty(@NonNull final String propName) {
            return properties.getProperty(propName);
        }

        public void setProperty(
                @NonNull final String propName,
                @NonNull final String propValue) {
            Parameters.notNull("propName", propName);   //NOI18N
            Parameters.notNull("propValue", propValue);   //NOI18N
            properties.put(propName, propValue);
        }

        public void addConcealedProperty(@NonNull final String propName) {
            Parameters.notNull("propName", propName);   //NOI18N
            concealedProperties.add(propName);
        }

        void removeProperty(
                @NonNull final String propName) {
            properties.remove(propName);
        }

        void setJavaChecks(final boolean doJavaChecks) {
            this.doJavaChecks = doJavaChecks;
        }

        @CheckForNull
        ActionProviderSupport.UserPropertiesPolicy getUserPropertiesPolicy() {
            return userPropertiesPolicy;
        }

        void setUserPropertiesPolicy(@NullAllowed final ActionProviderSupport.UserPropertiesPolicy p) {
            userPropertiesPolicy = p;
        }

        void fireAntTargetInvocationListener(final int state, final int res) {
            for (AntTargetInvocationListener l : listeners) {
                switch (state) {
                    case 0:
                        l.antTargetInvocationStarted(command, lkp);
                        break;
                    case 1:
                        l.antTargetInvocationFinished(command, lkp, res);
                        break;
                    case 2:
                        l.antTargetInvocationFailed(command, lkp);
                        break;
                    default:
                        throw new IllegalArgumentException(Integer.toString(state));
                }
            }
        }

        @CheckForNull
        Properties getProperties() {
            Optional.ofNullable(additionalPropertiesProvider)
                    .map((p) -> p.apply(getCommand(), getActiveLookup()))
                    .ifPresent(properties::putAll);
            return properties.keySet().isEmpty() ?
                    null :
                    properties;
        }

        @CheckForNull
        Set<String> getConcealedProperties() {
            Optional.ofNullable(concealedPropertiesProvider)
                    .map((p) -> p.apply(getCommand(), getActiveLookup()))
                    .ifPresent(concealedProperties::addAll);
            return concealedProperties.isEmpty() ?
                    null :
                    concealedProperties;
        }

        @CheckForNull
        Set<String> copyAdditionalProperties(@NonNull final Map<String,Object> into) {
            Optional.ofNullable(additionalPropertiesProvider)
                    .map((p) -> p.apply(getCommand(), getActiveLookup()))
                    .ifPresent(into::putAll);
            final Set<String> cps = new HashSet<>(concealedProperties);
            Optional.ofNullable(concealedPropertiesProvider)
                    .map((p) -> p.apply(getCommand(), getActiveLookup()))
                    .ifPresent(cps::addAll);
            return cps.isEmpty() ?
                    null :
                    cps;
        }
    }


    public abstract static class ScriptAction implements Action {
        private final String command;
        private final String displayName;
        private final Set<ActionProviderSupport.ActionFlag> actionFlags;
        private volatile BiFunction<Context,Map<String,Object>,Boolean> cosInterceptor;

        public static final class Result {
            private static final Result ABORT = new Result(null);
            private static final Result FOLLOW = new Result(null);

            private final ExecutorTask task;

            private Result(@NullAllowed final ExecutorTask task) {
                this.task = task;
            }

            ExecutorTask getTask() {
                return task;
            }

            @NonNull
            public static Result success(@NonNull final ExecutorTask task) {
                Parameters.notNull("task", task);   //NOI18N
                return new Result(task);
            }

            @NonNull
            public static Result abort() {
                return ABORT;
            }

            @NonNull
            public static Result follow() {
                return FOLLOW;
            }
        }

        protected ScriptAction (
                @NonNull final String command,
                @NullAllowed final String dispalyName,
                final boolean platformSensitive,
                final boolean javaModelSensitive,
                final boolean scanSensitive,
                final boolean cosEnabled) {
            Parameters.notNull("command", command); //NOI18N
            this.command = command;
            this.displayName = dispalyName;
            this.actionFlags = EnumSet.noneOf(ActionProviderSupport.ActionFlag.class);
            if (platformSensitive) {
                this.actionFlags.add(ActionProviderSupport.ActionFlag.PLATFORM_SENSITIVE);
            }
            if (javaModelSensitive) {
                this.actionFlags.add(ActionProviderSupport.ActionFlag.JAVA_MODEL_SENSITIVE);
            }
            if (scanSensitive) {
                this.actionFlags.add(ActionProviderSupport.ActionFlag.SCAN_SENSITIVE);
            }
            if (cosEnabled) {
                this.actionFlags.add(ActionProviderSupport.ActionFlag.COS_ENABLED);
            }
        }

        @CheckForNull
        public abstract String[] getTargetNames(@NonNull final Context context);

        @NonNull
        public Result performCompileOnSave(@NonNull final Context context, @NonNull final String[] targetNames) {
            return Result.follow();
        }

        @Override
        public boolean isEnabled(Context context) {
            if (!this.actionFlags.contains(ActionProviderSupport.ActionFlag.COS_ENABLED) &&
                    !context.getCompileOnSaveOperations().isEmpty() &&
                    !ActionProviderSupport.allowAntBuild(context.getPropertyEvaluator(), context.getUpdateHelper())) {
                return false;
            }
            return true;
        }

        @Override
        public final String getCommand() {
            return command;
        }

        @Override
        public final void invoke(@NonNull final Context context) {
            if (actionFlags.contains(ActionProviderSupport.ActionFlag.PLATFORM_SENSITIVE) && context.getActiveJavaPlatform() == null) {
                ActionProviderSupport.showPlatformWarning(context.getProject());
                return;
            }
            ActionProviderSupport.invokeTarget(this, context);
        }

        @NonNull
        final String getDisplayName() {
            String res = displayName;
            if (res == null) {
                res = getCommandDisplayName(command);
            }
            return res;
        }

        @NonNull
        final Set<ActionProviderSupport.ActionFlag> getActionFlags() {
            return actionFlags;
        }

        @CheckForNull
        final BiFunction<Context,Map<String,Object>,Boolean> getCoSInterceptor() {
            return cosInterceptor;
        }

        final void setCoSInterceptor(@NullAllowed final BiFunction<Context,Map<String,Object>,Boolean> cosInterceptor) {
            this.cosInterceptor = cosInterceptor;
        }

        @NbBundle.Messages({
        "ACTION_run=Run Project",
        "ACTION_run.single=Run File",
        "ACTION_run.single.method=Run File",
        "ACTION_debug=Debug Project",
        "ACTION_debug.single=Debug File",
        "ACTION_debug.single.method=Debug File",
        "ACTION_debug.stepinto=Debug Project",
        "ACTION_debug.fix=Apply Code Changes",
        "ACTION_debug.test.single=Debug Test",
        "ACTION_profile=Profile Project",
        "ACTION_profile.single=Profile File",
        "ACTION_profile.test.single=Profile Test",
        "ACTION_rebuild=Rebuild Project",
        "ACTION_build=Build Project",
        "ACTION_clean=Clean Project",
        "ACTION_compile.single=Compile File",
        "ACTION_javadoc=Generate JavaDoc",
        "ACTION_test=Test Project",
        "ACTION_test.single=Test File"
    })
    private static String getCommandDisplayName(String command) throws MissingResourceException {
        if (command.equals("run")) {
            return Bundle.ACTION_run();
        } else if (command.equals("run.single")) {
            return Bundle.ACTION_run_single();
        } else if (command.equals("run.single.method")) {
            return Bundle.ACTION_run_single_method();
        } else if (command.equals("debug")) {
            return Bundle.ACTION_debug();
        } else if (command.equals("debug.single")) {
            return Bundle.ACTION_debug_single();
        } else if (command.equals("debug.single.method")) {
            return Bundle.ACTION_debug_single_method();
        } else if (command.equals("debug.stepinto")) {
            return Bundle.ACTION_debug_stepinto();
        } else if (command.equals("debug.fix")) {
            return Bundle.ACTION_debug_fix();
        } else if (command.equals("debug.test.single")) {
            return Bundle.ACTION_debug_test_single();
        } else if (command.equals("profile")) {
            return Bundle.ACTION_profile();
        } else if (command.equals("profile.single")) {
            return Bundle.ACTION_profile_single();
        } else if (command.equals("profile.test.single")) {
            return Bundle.ACTION_profile_test_single();
        } else if (command.equals("rebuild")) {
            return Bundle.ACTION_rebuild();
        } else if (command.equals("build")) {
            return Bundle.ACTION_build();
        } else if (command.equals("clean")) {
            return Bundle.ACTION_clean();
        } else if (command.equals("compile.single")) {
            return Bundle.ACTION_compile_single();
        } else if (command.equals("javadoc")) {
            return Bundle.ACTION_javadoc();
        } else if (command.equals("test")) {
            return Bundle.ACTION_test();
        } else if (command.equals("test.single")) {
            return Bundle.ACTION_test_single();
        } else {
            return command;
        }
    }
    }

    public static final class Builder {
        private final Project project;
        private final UpdateHelper updateHelper;
        private final PropertyEvaluator evaluator;
        private final SourceRoots sourceRoots;
        private final SourceRoots testRoots;
        private final List<Action> actions;
        private final ActionProviderSupport.ModifiedFilesSupport mfs;
        private final Function<String,ClassPath> classpaths;
        private volatile Object[] mainClassServices;
        private Supplier<? extends JavaPlatform> jpp;
        private BiFunction<String,Lookup,Map<String,String>> additionalPropertiesProvider;
        private BiFunction<String,Lookup,Set<String>> concealedPropertiesProvider;
        private Supplier<? extends Set<? extends CompileOnSaveOperation>> cosOpsProvider;

        private Builder(
                @NonNull final Project project,
                @NonNull final UpdateHelper updateHelper,
                @NonNull final PropertyEvaluator evaluator,
                @NonNull final SourceRoots sourceRoots,
                @NonNull final SourceRoots testSourceRoots,
                @NonNull final Function<String,ClassPath> projectClassPaths) {
            Parameters.notNull("project", project); //NOI18N
            Parameters.notNull("updateHelper", updateHelper); //NOI18N
            Parameters.notNull("evaluator", evaluator); //NOI18N
            Parameters.notNull("sourceRoots", sourceRoots); //NOI18N
            Parameters.notNull("testSourceRoots", testSourceRoots); //NOI18N
            Parameters.notNull("projectClassPaths", projectClassPaths); //NOI18N
            this.project = project;
            this.updateHelper = updateHelper;
            this.evaluator = evaluator;
            this.sourceRoots = sourceRoots;
            this.testRoots = testSourceRoots;
            this.actions = new ArrayList<>();
            this.jpp = createJavaPlatformProvider(ProjectProperties.PLATFORM_ACTIVE);
            this.mfs = ActionProviderSupport.ModifiedFilesSupport.newInstance(project, updateHelper, evaluator);
            this.classpaths = projectClassPaths;
            final Function<Boolean,String> pmcp = (validate) -> ActionProviderSupport.getProjectMainClass(project, evaluator, sourceRoots, classpaths, validate);
            final Supplier<Boolean> mcc = () -> ActionProviderSupport.showCustomizer(project, updateHelper, evaluator, sourceRoots, classpaths);
            this.mainClassServices = new Object[] {pmcp, mcc};
        }

        public static interface CustomFileExecutor {
            @CheckForNull
            String[] getTargetNames(@NonNull final FileObject file, @NonNull Context context);

            @NonNull
            default ScriptAction.Result performCompileOnSave(@NonNull final Context context, @NonNull final String[] targetNames) {
                return ScriptAction.Result.follow();
            }
        }

        @NonNull
        public Builder addAction(@NonNull final Action action) {
            Parameters.notNull("action", action);   //NOI18N
            actions.add(action);
            return this;
        }

        @NonNull
        public Action createProjectOperation(String command) {
            final Consumer<? super Context> performer;
            switch (command) {
                case COMMAND_DELETE:
                    performer = (ctx) -> DefaultProjectOperations.performDefaultDeleteOperation(ctx.getProject());
                    break;
                case COMMAND_MOVE:
                    performer = (ctx) -> DefaultProjectOperations.performDefaultMoveOperation(ctx.getProject());
                    break;
                case COMMAND_COPY:
                    performer = (ctx) -> DefaultProjectOperations.performDefaultCopyOperation(ctx.getProject());
                    break;
                case COMMAND_RENAME:
                    performer = (ctx) -> DefaultProjectOperations.performDefaultRenameOperation(ctx.getProject(), null);
                    break;
                default:
                    throw new IllegalArgumentException(command);
            }
            return new SimpleAction(command, performer);
        }

        /**
         * Creates a simple {@link ScriptAction} for given command performing given targets.
         * The action just calls the given targets in project's build file.
         * The action does not support Compile On Save.
         * @param command the action command
         * @param requiresValidJavaPlatform if true the action is not executed when the project has invalid platform
         * @param javaModelSensitive if true the action requires java model
         * @param scanSensitive if true the action needs to wait for scan finish
         * @param enabledInCoS if true the action is enabled in compile on save mode
         * @param targets the targets to execute
         * @return the newly created {@link ScriptAction}
         * @since 1.109
         */
        @NonNull
        public ScriptAction createSimpleScriptAction(
                @NonNull final String command,
                final boolean requiresValidJavaPlatform,
                final boolean javaModelSensitive,
                final boolean scanSensitive,
                final boolean enabledInCoS,
                @NonNull final String... targets) {
            Parameters.notNull("targets", targets); //NOI18N
            return createSimpleScriptAction(
                    command,
                    requiresValidJavaPlatform,
                    javaModelSensitive,
                    scanSensitive,
                    enabledInCoS,
                    (ctx) -> true,
                    () -> targets);
        }

        /**
         * Creates a simple {@link ScriptAction} for given command performing given targets.
         * The action just calls the given targets in project's build file.
         * The action does not support Compile On Save.
         * @param command the action command
         * @param requiresValidJavaPlatform if true the action is not executed when the project has invalid platform
         * @param javaModelSensitive if true the action requires java model
         * @param scanSensitive if true the action needs to wait for scan finish
         * @param enabledInCoS if true the action is enabled in compile on save mode
         * @param enabled the {@link Predicate} to enable the action
         * @param targets the {@link Supplier} of targets to execute
         * @return the newly created {@link ScriptAction}
         * @since 1.109
         */
        @NonNull
        public ScriptAction createSimpleScriptAction(
                @NonNull final String command,
                final boolean requiresValidJavaPlatform,
                final boolean javaModelSensitive,
                final boolean scanSensitive,
                final boolean enabledInCoS,
                @NonNull final Predicate<Context> enabled,
                @NonNull final Supplier<? extends String[]> targets) {
            Parameters.notNull("enabled", enabled); //NOI18N
            Parameters.notNull("targets", targets); //NOI18N
            return new BaseScriptAction(
                    command,
                    requiresValidJavaPlatform,
                    javaModelSensitive,
                    scanSensitive,
                    enabledInCoS,
                    targets) {
                @Override
                public boolean isEnabled(Context context) {
                    return super.isEnabled(context) && enabled.test(context);
                }
            };
        }

        @NonNull
        public ScriptAction createDefaultScriptAction(
                @NonNull final String command,
                final boolean javaModelSensitive,
                final boolean scanSensitive,
                final boolean enabledInCoS,
                @NonNull final String... targets) {
            Parameters.notNull("targets", targets);     //NOI18N
            return createDefaultScriptAction(command, javaModelSensitive, scanSensitive, enabledInCoS, () -> targets, null);
        }

        @NonNull
        public ScriptAction createDefaultScriptAction(
                @NonNull final String command,
                final boolean javaModelSensitive,
                final boolean scanSensitive,
                final boolean enabledInCoS,
                @NonNull final Supplier<? extends String[]> targets) {
            return createDefaultScriptAction(command, javaModelSensitive, scanSensitive, enabledInCoS, targets, null);
        }

        @NonNull
        public ScriptAction createDefaultScriptAction(
                @NonNull final String command,
                final boolean javaModelSensitive,
                final boolean scanSensitive,
                final boolean enabledInCoS,
                @NonNull final Supplier<? extends String[]> targets,
                @NullAllowed final CustomFileExecutor customFileExecutor) {
            Parameters.notNull("command", command);         //NOI18N
            Parameters.notNull("targets", targets);         //NOI18N
            switch (command) {
                case ActionProvider.COMMAND_CLEAN:
                    return createNonCosAction(command, false, javaModelSensitive, scanSensitive, enabledInCoS, targets, Collections.emptyMap());
                case ActionProvider.COMMAND_REBUILD:
                    return createNonCosAction(command, true, javaModelSensitive, scanSensitive, enabledInCoS, targets, Collections.emptyMap());
                case ActionProvider.COMMAND_BUILD:
                    return createBuildAction(javaModelSensitive, scanSensitive, enabledInCoS, targets, mfs);
                case ActionProvider.COMMAND_RUN:
                case ActionProvider.COMMAND_DEBUG:
                case ActionProvider.COMMAND_DEBUG_STEP_INTO:
                case ActionProvider.COMMAND_PROFILE:
                    return createRunAction(command, javaModelSensitive, scanSensitive, enabledInCoS, targets, mfs, mainClassServices);
                case ActionProvider.COMMAND_TEST:
                    return createNonCosAction(command, true, javaModelSensitive, scanSensitive, enabledInCoS, targets, Collections.singletonMap("ignore.failing.tests", "true"));  //NOI18N);
                case ActionProvider.COMMAND_COMPILE_SINGLE:
                    return createCompileSingleAction(javaModelSensitive, scanSensitive, enabledInCoS, sourceRoots, testRoots, targets);
                case ActionProvider.COMMAND_RUN_SINGLE:
                case ActionProvider.COMMAND_DEBUG_SINGLE:
                case ActionProvider.COMMAND_PROFILE_SINGLE:
                    return createRunSingleAction(
                            command, javaModelSensitive, scanSensitive, enabledInCoS,
                            sourceRoots, testRoots, targets, customFileExecutor);
                case ActionProvider.COMMAND_TEST_SINGLE:
                    return createTestSingleAction(javaModelSensitive, scanSensitive, enabledInCoS, sourceRoots, testRoots, targets);
                case ActionProvider.COMMAND_DEBUG_TEST_SINGLE:
                case ActionProvider.COMMAND_PROFILE_TEST_SINGLE:
                    return createDebugTestSingleAction(command, javaModelSensitive, scanSensitive, enabledInCoS, sourceRoots, testRoots, targets);
                case SingleMethod.COMMAND_RUN_SINGLE_METHOD:
                case SingleMethod.COMMAND_DEBUG_SINGLE_METHOD:
                    return createRunSingleMethodAction(command, javaModelSensitive, scanSensitive, enabledInCoS, testRoots, targets);
                case JavaProjectConstants.COMMAND_DEBUG_FIX:
                    return createDebugFixAction(command, javaModelSensitive, scanSensitive, enabledInCoS, sourceRoots, testRoots, targets);
                default:
                    throw new UnsupportedOperationException(String.format("Unsupported command: %s", command)); //NOI18N
            }
        }

        @NonNull
        public Builder setActivePlatformProperty(@NonNull final String activePlatformProperty) {
            Parameters.notNull("activePlatformProperty", activePlatformProperty);   //NOI18N
            this.jpp = createJavaPlatformProvider(activePlatformProperty);
            return this;
        }

        @NonNull
        public Builder setActivePlatformProvider(@NonNull final Supplier<? extends JavaPlatform> javaPlatformProvider) {
            Parameters.notNull("javaPlatformProvider", javaPlatformProvider);   //NOI18N
            this.jpp = javaPlatformProvider;
            return this;
        }

        @NonNull
        public Builder setCompileOnSaveOperationsProvider(@NonNull final Supplier<? extends Set<? extends CompileOnSaveOperation>> cosOpsProvider) {
            Parameters.notNull("cosOpsProvider", cosOpsProvider);   //NOI18N
            this.cosOpsProvider = cosOpsProvider;
            return this;
        }

        @NonNull
        public Builder setProjectMainClassProvider(@NonNull final Function<Boolean,String> mainClassProvider) {
            Parameters.notNull("mainClassProvider", mainClassProvider);
            mainClassServices[0] = mainClassProvider;
            mainClassServices = mainClassServices;
            return this;
        }

        @NonNull
        public Builder setProjectMainClassSelector(@NonNull final Supplier<Boolean> selectMainClassAction) {
            Parameters.notNull("selectMainClassAction", selectMainClassAction);
            mainClassServices[1] = selectMainClassAction;
            mainClassServices = mainClassServices;
            return this;
        }

        @NonNull
        public Builder setAdditionalPropertiesProvider(@NonNull final BiFunction<String,Lookup,Map<String,String>> additionalPropertiesProvider) {
            Parameters.notNull("additionalPropertiesProvider", additionalPropertiesProvider);   //NOI18N
            this.additionalPropertiesProvider = additionalPropertiesProvider;
            return this;
        }

        @NonNull
        public Builder setConcealedPropertiesProvider(@NonNull final BiFunction<String,Lookup,Set<String>> concealedPropertiesProvider) {
            Parameters.notNull("concealedPropertiesProvider", concealedPropertiesProvider);   //NOI18N
            this.concealedPropertiesProvider = concealedPropertiesProvider;
            return this;
        }

        @NonNull
        public JavaActionProvider build() {
            final JavaActionProvider ap = new JavaActionProvider(
                    project,
                    updateHelper,
                    evaluator,
                    classpaths,
                    actions,
                    jpp,
                    additionalPropertiesProvider,
                    concealedPropertiesProvider,
                    cosOpsProvider,
                    mfs);
            mfs.start();
            return ap;
        }

        @NonNull
        private Supplier<? extends JavaPlatform> createJavaPlatformProvider(@NonNull final String activePlatformProperty) {
            return () -> {
                return ActionProviderSupport.getActivePlatform(project, evaluator, activePlatformProperty);
            };
        }

        @NonNull
        public static Builder newInstance(
                @NonNull final Project project,
                @NonNull final UpdateHelper updateHelper,
                @NonNull final PropertyEvaluator evaluator,
                @NonNull final SourceRoots sourceRoots,
                @NonNull final SourceRoots testSourceRoots,
                @NonNull final Function<String,ClassPath> projectClassPaths) {
            return new Builder(project, updateHelper, evaluator, sourceRoots, testSourceRoots, projectClassPaths);
        }
    }

    @Override
    public String[] getSupportedActions() {
        return supportedActions.keySet().toArray(new String[supportedActions.size()]);
    }

    @Override
    public boolean isActionEnabled(
            @NonNull final String command,
            @NonNull final Lookup context) throws IllegalArgumentException {
        return Optional.ofNullable(supportedActions.get(command))
                .map((act) -> act.isEnabled(new Context(
                        prj, updateHelper, eval, classpaths,
                        command, context, userPropertiesPolicy,
                        jpp, additionalPropertiesProvider, concealedPropertiesProvider, cosOpsProvider,
                        listeners)))
                .orElse(Boolean.FALSE);
    }

    @Override
    public void invokeAction(
            @NonNull final String command,
            @NonNull final Lookup context) throws IllegalArgumentException {
        assert SwingUtilities.isEventDispatchThread();
        Optional.ofNullable(supportedActions.get(command))
                .ifPresent((act) -> {
                    final Context ctx = new Context(
                            prj, updateHelper, eval, classpaths,
                            command, context, userPropertiesPolicy,
                            jpp, additionalPropertiesProvider, concealedPropertiesProvider, cosOpsProvider,
                            listeners);
                    try {
                        act.invoke(ctx);
                    } finally {
                        userPropertiesPolicy = ctx.getUserPropertiesPolicy();
                    }
                });
    }

    public void addAntTargetInvocationListener(@NonNull final AntTargetInvocationListener listener) {
        Parameters.notNull("listener", listener);
        listeners.add(listener);
    }

    public void removeAntTargetInvocationListener(@NonNull final AntTargetInvocationListener listener) {
        Parameters.notNull("listener", listener);
        listeners.remove(listener);
    }

    @CheckForNull
    Action getAction(@NonNull final String command) {
        return supportedActions.get(command);
    }

    @NonNull
    @Messages({
        "LBL_ProjectBuiltAutomatically=<html><b>This project's source files are compiled automatically when you save them.</b><br>You do not need to build the project to run or debug the project in the IDE.<br><br>If you need to build or rebuild the project's JAR file, use Clean and Build.<br>To disable the automatic compiling feature and activate the Build command,<br>go to Project Properties and disable Compile on Save.",
        "BTN_ProjectProperties=Project Properties...",
        "BTN_CleanAndBuild=Clean and Build",
        "BTN_OK=OK",
        "# {0} - project name", "TITLE_BuildProjectWarning=Build Project ({0})"
    })
    private static ScriptAction createBuildAction (
            final boolean javaModelSensitive,
            final boolean scanSensitive,
            final boolean enabledInCoS,
            @NonNull final Supplier<? extends String[]> targets,
            @NonNull final ActionProviderSupport.ModifiedFilesSupport mfs) {
        return new BaseScriptAction(COMMAND_BUILD, true, javaModelSensitive, scanSensitive, enabledInCoS, targets) {

            @Override
            public String[] getTargetNames(Context context) {
                String[] targets = super.getTargetNames(context);
                if (targets != null) {
                    final String includes = mfs.prepareDirtyList(true);
                    if (includes != null) {
                        context.setProperty(ProjectProperties.INCLUDES, includes);
                    }
                }
                return targets;
            }

            @Override
            public ScriptAction.Result performCompileOnSave(Context context, String[] targetNames) {
                if (!ActionProviderSupport.allowAntBuild(context.getPropertyEvaluator(), context.getUpdateHelper())) {
                    showBuildActionWarning(context);
                    return JavaActionProvider.ScriptAction.Result.abort();
                }
                return JavaActionProvider.ScriptAction.Result.follow();
            }

            @org.netbeans.api.annotations.common.SuppressWarnings("ES_COMPARING_STRINGS_WITH_EQ")
            private void showBuildActionWarning(Context context) {
                String projectProperties = Bundle.BTN_ProjectProperties();
                String cleanAndBuild = Bundle.BTN_CleanAndBuild();
                String ok = Bundle.BTN_OK();
                DialogDescriptor dd = new DialogDescriptor(Bundle.LBL_ProjectBuiltAutomatically(),
                       Bundle.TITLE_BuildProjectWarning(ProjectUtils.getInformation(context.getProject()).getDisplayName()),
                       true,
                       new Object[] {projectProperties, cleanAndBuild, ok},
                       ok,
                       DialogDescriptor.DEFAULT_ALIGN,
                       null,
                       null);

                dd.setMessageType(NotifyDescriptor.WARNING_MESSAGE);
                Object result = DialogDisplayer.getDefault().notify(dd);
                if (result == projectProperties) {
                    CustomizerProvider2 p = context.getProject().getLookup().lookup(CustomizerProvider2.class);
                    p.showCustomizer("Build", null); //NOI18N
                    return ;
                }
                if (result == cleanAndBuild) {
                    final ActionProvider ap = context.getProject().getLookup().lookup(ActionProvider.class);
                    if (ap != null) {
                        ap.invokeAction(COMMAND_REBUILD, context.getActiveLookup());
                    }
                }
            }
        };
    }

    @NonNull
    private static ScriptAction createCompileSingleAction(
            final boolean javaModelSensitive,
            final boolean scanSensitive,
            final boolean enabledInCoS,
            @NonNull final SourceRoots sourceRoots,
            @NonNull final SourceRoots testRoots,
            @NonNull final Supplier<? extends String[]> targets) {
        return new BaseScriptAction(COMMAND_COMPILE_SINGLE, true, javaModelSensitive, scanSensitive, enabledInCoS, targets) {

            @Override
            public boolean isEnabled(Context context) {
                return super.isEnabled(context) && (ActionProviderSupport.findSourcesAndPackages(context.getActiveLookup(), sourceRoots.getRoots()) != null
                    || ActionProviderSupport.findSourcesAndPackages(context.getActiveLookup(), testRoots.getRoots()) != null);
            }

            @Override
            public String[] getTargetNames(Context context) {
                String[] res = super.getTargetNames(context);
                if (res != null) {
                    final Lookup lkp = context.getActiveLookup();
                    FileObject[] srcRoots = sourceRoots.getRoots();
                    FileObject[] files = ActionProviderSupport.findSourcesAndPackages(lkp, srcRoots);
                    final boolean recursive = lkp.lookup(NonRecursiveFolder.class) == null;
                    if (files != null) {
                        context.setProperty(
                                "javac.includes",    // NOI18N
                                ActionUtils.antIncludesList(files, ActionProviderSupport.getRoot(srcRoots,files[0]), recursive));
                        final String[] cfgTargets = ActionProviderSupport.loadTargetsFromConfig(
                            context.getProject(),
                            context.getPropertyEvaluator())
                            .get(context.getCommand());
                        if (cfgTargets != null) {
                            res = cfgTargets;
                        }
                    } else {
                        srcRoots = testRoots.getRoots();
                        files = ActionProviderSupport.findSourcesAndPackages(context.getActiveLookup(), srcRoots);
                        if (files != null) {
                            context.setProperty(
                                "javac.includes",    // NOI18N
                                ActionUtils.antIncludesList(files, ActionProviderSupport.getRoot(srcRoots,files[0]), recursive));
                            res = new String[] {"compile-test-single"}; // NOI18N
                        } else {
                            res = null;
                        }
                    }
                }
                return res;
            }
        };
    }

    @NonNull
    @Messages({
        "# {0} - file name", "CTL_FileMultipleMain=The file {0} has more main classes.",
        "CTL_FileMainClass_Title=Run File"
    })
    private static ScriptAction createRunSingleAction(
            @NonNull final String command,
            final boolean javaModelSensitive,
            final boolean scanSensitive,
            final boolean enabledInCoS,
            final SourceRoots sr,
            final SourceRoots tr,
            @NonNull final Supplier<? extends String[]> targets,
            @NullAllowed final Builder.CustomFileExecutor customFileExecutor) {
        return new BaseRunSingleAction(command, true, javaModelSensitive, scanSensitive, enabledInCoS, sr, tr, targets) {
            private static final String PROP_CUSTOM_RUNNER = "JavaActionProvider.invokeByCustomExecutor";   //NOI18N

            @Override
            public boolean isEnabled(Context context) {
                if (super.isEnabled(context)) {
                    FileObject fos[] = ActionProviderSupport.findSources(getSourceRoots().getRoots(), context.getActiveLookup());
                    if (fos != null && fos.length == 1) {
                        return true;
                    }
                    fos = ActionProviderSupport.findTestSources(getSourceRoots().getRoots(), getTestRoots().getRoots(), context.getActiveLookup(), false);
                    if (fos != null && fos.length == 1) {
                        return true;
                    }
                    logNoFiles(getSourceRoots(), getTestRoots(), context);
                }
                return false;
            }

            @Override
            public ScriptAction.Result performCompileOnSave(Context context, String[] targetNames) {
                final boolean cr = context.getProperty(PROP_CUSTOM_RUNNER) != null;
                if (cr) {
                    context.removeProperty(PROP_CUSTOM_RUNNER);
                    return customFileExecutor.performCompileOnSave(context, targetNames);
                } else {
                    return super.performCompileOnSave(context, targetNames);
                }
            }

            @Override
            public String[] getTargetNames(Context context) {
                String[] res = super.getTargetNames(context);
                if (res != null) {
                    FileObject[] files = ActionProviderSupport.findTestSources(getSourceRoots().getRoots(), getTestRoots().getRoots(), context.getActiveLookup(), false);
                    FileObject[] rootz = getTestRoots().getRoots();
                    boolean isTest = true;
                    if (files == null) {
                        isTest = false;
                        files = ActionProviderSupport.findSources(getSourceRoots().getRoots(), context.getActiveLookup());
                        rootz = getSourceRoots().getRoots();
                    }
                    if (LOG.isLoggable(Level.FINE)) {
                        LOG.log(Level.FINE, "Is test: {0} Files: {1} Roots: {2}",    //NOI18N
                                new Object[] {
                                    isTest,
                                    asPath(files),
                                    asPath(rootz)
                        });
                    }
                    if (files == null) {
                        return null;
                    }
                    final FileObject file = files[0];
                    assert file != null;
                    if (!file.isValid()) {
                        LOG.log(Level.WARNING,
                                "FileObject to execute: {0} is not valid.",
                                FileUtil.getFileDisplayName(file));   //NOI18N
                        return null;
                    }
                    String[] pathFqn = ActionProviderSupport.pathAndFqn(file, rootz);
                    if (pathFqn == null) {
                        return null;
                    }
                    context.setProperty("javac.includes", pathFqn[0]); // NOI18N
                    String clazz = pathFqn[1];
                    LOG.log(Level.FINE, "Class to run: {0}", clazz);    //NOI18N
                    final boolean hasMainClassFromTest = MainClassChooser.unitTestingSupport_hasMainMethodResult == null ?
                            false :
                            MainClassChooser.unitTestingSupport_hasMainMethodResult.booleanValue();
                    if (context.doJavaChecks()) {
                        final Collection<ElementHandle<TypeElement>> mainClasses = CommonProjectUtils.getMainMethods (file);
                        LOG.log(Level.FINE, "Main classes: {0} ", mainClasses);
                        if (!hasMainClassFromTest && mainClasses.isEmpty()) {
                            final String[] customTargets = customFileExecutor == null ?
                                    null :
                                    customFileExecutor.getTargetNames(file, context);
                            if (customTargets != null) {
                                context.setProperty(PROP_CUSTOM_RUNNER, Boolean.TRUE.toString());
                                res = customTargets;
                            } else {
                                if (isTest) {
                                    res = ActionProviderSupport.setupTestSingle(context, files, getTestRoots().getRoots());
                                } else {
                                    final ActionProviderSupport.JavaMainAction javaMainAction = ActionProviderSupport.getJavaMainAction(context.getPropertyEvaluator());
                                    if (javaMainAction == null) {
                                        NotifyDescriptor nd = new NotifyDescriptor.Message(Bundle.LBL_No_Main_Class_Found(clazz), NotifyDescriptor.INFORMATION_MESSAGE);
                                        DialogDisplayer.getDefault().notify(nd);
                                        return null;
                                    } else if (javaMainAction == ActionProviderSupport.JavaMainAction.RUN) {
                                        res = updateTargets(res, clazz, false, context);
                                    } else if (javaMainAction == ActionProviderSupport.JavaMainAction.TEST) {
                                        res = ActionProviderSupport.setupTestSingle(context, files, getSourceRoots().getRoots());
                                    }
                                }
                            }
                        } else {
                            if (!hasMainClassFromTest) {
                                if (mainClasses.size() == 1) {
                                    //Just one main class
                                    clazz = mainClasses.iterator().next().getBinaryName();
                                } else {
                                    //Several main classes, let the user choose
                                    clazz = showMainClassWarning(file, mainClasses);
                                    if (clazz == null) {
                                        return null;
                                    }
                                }
                            }
                            res = updateTargets(res, clazz, isTest, context);
                        }
                    } else {
                        res = updateTargets(res, clazz, isTest, context);
                    }
                }
                return res;
            }

            private String[] updateTargets(
                    @NonNull String[] targets,
                    @NonNull final String clazz,
                    final boolean isTest,
                    @NonNull final Context context) {
                //The Java model is not ready, we cannot determine if the file is applet or main class or unit test
                //Acts like everything is main class, maybe for test folder junit is better default?
                switch (context.getCommand()) {
                    case COMMAND_RUN_SINGLE:
                        context.setProperty("run.class", clazz); // NOI18N
                        if (isTest) {
                            targets = new String[] {"run-test-with-main"};
                        }
                        break;
                    case COMMAND_DEBUG_SINGLE:
                        context.setProperty("debug.class", clazz); // NOI18N
                        if (isTest) {
                            targets = new String[] {"debug-test-with-main"};
                        }
                        break;
                    default:
                        context.setProperty("run.class", clazz); // NOI18N
                        if (isTest) {
                            targets = new String[] {"profile-test-with-main"};
                        }
                        break;
                }
                final String[] cfgTargets = ActionProviderSupport.loadTargetsFromConfig(
                    context.getProject(),
                    context.getPropertyEvaluator())
                    .get(command);
                if (cfgTargets != null) {
                    targets = cfgTargets;
                }
                return targets;
            }

            private String showMainClassWarning (final FileObject file, final Collection<ElementHandle<TypeElement>> mainClasses) {
                assert mainClasses != null;
                String mainClass = null;
                final JButton okButton = new JButton(Bundle.LBL_MainClassWarning_ChooseMainClass_OK());
                okButton.getAccessibleContext().setAccessibleDescription(Bundle.AD_MainClassWarning_ChooseMainClass_OK());
                final MainClassWarning panel = new MainClassWarning(Bundle.CTL_FileMultipleMain(file.getNameExt()), mainClasses);
                Object[] options = new Object[] {
                    okButton,
                    DialogDescriptor.CANCEL_OPTION
                };
                panel.addChangeListener ((e) -> {
                   if (e.getSource () instanceof MouseEvent && MouseUtils.isDoubleClick (((MouseEvent)e.getSource ()))) {
                       // click button and the finish dialog with selected class
                       okButton.doClick ();
                   } else {
                       okButton.setEnabled (panel.getSelectedMainClass () != null);
                   }
                });
                DialogDescriptor desc = new DialogDescriptor (panel,
                    Bundle.CTL_FileMainClass_Title(),
                    true, options, options[0], DialogDescriptor.BOTTOM_ALIGN, null, null);
                desc.setMessageType (DialogDescriptor.INFORMATION_MESSAGE);
                Dialog dlg = DialogDisplayer.getDefault ().createDialog (desc);
                dlg.setVisible (true);
                if (desc.getValue() == options[0]) {
                    mainClass = panel.getSelectedMainClass ();
                }
                dlg.dispose();
                return mainClass;
            }
        };
    }

    @NonNull
    private static ScriptAction createTestSingleAction(
            final boolean javaModelSensitive,
            final boolean scanSensitive,
            final boolean enabledInCoS,
            @NonNull final SourceRoots sr,
            @NonNull final SourceRoots tr,
            @NonNull final Supplier<? extends String[]> targets) {
        return new BaseRunSingleAction(COMMAND_TEST_SINGLE, true, javaModelSensitive, scanSensitive, enabledInCoS, sr, tr, targets) {

            @Override
            public boolean isEnabled(Context context) {
                return super.isEnabled(context) &&
                        ActionProviderSupport.findTestSourcesForFiles(getSourceRoots().getRoots(), getTestRoots().getRoots(), context.getActiveLookup()) != null;
            }

            @Override
            public String[] getTargetNames(Context context) {
                context.setProperty("ignore.failing.tests", "true");  //NOI18N
                final FileObject[] files = ActionProviderSupport.findTestSourcesForFiles(getSourceRoots().getRoots(), getTestRoots().getRoots(), context.getActiveLookup());
                if (files == null) {
                    return null;
                }
                if(files.length == 1 && files[0].isData()) {
                    //one file or a package containing one file selected
                    return ActionProviderSupport.setupTestSingle(context, files, getTestRoots().getRoots());
                } else {
                    //multiple files or package(s) selected
                    if (files != null) {
                        FileObject root = ActionProviderSupport.getRoot(getTestRoots().getRoots(), files[0]);
                        // the replace part is so that we can test everything under a package recusively
                        context.setProperty("includes", ActionUtils.antIncludesList(files, root).replace("**", "**/*Test.java")); // NOI18N
                    }
                    return new String[]{"test"}; // NOI18N
                }
            }
        };
    }

    @NonNull
    private static ScriptAction createDebugTestSingleAction(
            @NonNull final String command,
            final boolean javaModelSensitive,
            final boolean scanSensitive,
            final boolean enabledInCoS,
            @NonNull final SourceRoots sr,
            @NonNull final SourceRoots tr,
            @NonNull final Supplier<? extends String[]> targets) {
        return new BaseRunSingleAction(command, true, javaModelSensitive, scanSensitive, enabledInCoS, sr, tr, targets) {

            @Override
            public boolean isEnabled(Context context) {
                if (!super.isEnabled(context)) {
                    return false;
                }
                FileObject[] fos = ActionProviderSupport.findTestSources(getSourceRoots().getRoots(), getTestRoots().getRoots(), context.getActiveLookup(), true);
                return fos != null && fos.length == 1;
            }

            @Override
            public String[] getTargetNames(Context context) {
                final FileObject[] files = ActionProviderSupport.findTestSources(getSourceRoots().getRoots(), getTestRoots().getRoots(), context.getActiveLookup(), true);
                if (files != null) {
                    return ActionProviderSupport.setupTestSingle(context, files, getTestRoots().getRoots());
                }
                return null;
            }
        };
    }

    @NonNull
    private static ScriptAction createRunSingleMethodAction(
            @NonNull final String command,
            final boolean javaModelSensitive,
            final boolean scanSensitive,
            final boolean enabledInCoS,
            @NonNull final SourceRoots testRoots,
            @NonNull final Supplier<? extends String[]> targets) {
        return new BaseScriptAction(command, true, javaModelSensitive, scanSensitive, enabledInCoS, targets) {

            @Override
            public boolean isEnabled(Context context) {
                if (!super.isEnabled(context)) {
                    return false;
                }
                SingleMethod[] methodSpecs = findTestMethods(context);
                return (methodSpecs != null) && (methodSpecs.length == 1);
            }

            @Override
            @CheckForNull
            public String[] getTargetNames(Context context) {
                String[] res = super.getTargetNames(context);
                if (res != null) {
                    SingleMethod[] methodSpecs = findTestMethods(context);
                    if ((methodSpecs == null) || (methodSpecs.length != 1)) {
                        return null;
                    }
                    final FileObject testFile = methodSpecs[0].getFile();
                    final String[] pathFqn = ActionProviderSupport.pathAndFqn(testFile, testRoots.getRoots());
                    if (pathFqn == null) {
                        return null;
                    }
                    context.setProperty("javac.includes", pathFqn[0]); // NOI18N
                    context.setProperty("test.class", pathFqn[1]); // NOI18N
                    context.setProperty("test.method", methodSpecs[0].getMethodName()); // NOI18N
                }
                return res;
            }

            @Override
            @NonNull
            public ScriptAction.Result performCompileOnSave(@NonNull final Context context, @NonNull final String[] targetNames) {
                SingleMethod[] methodSpecs = findTestMethods(context);
                if (methodSpecs != null) {
                    try {
                        final Map<String,Object> execProperties = ActionProviderSupport.createBaseCoSProperties(context);
                        execProperties.put("methodname", methodSpecs[0].getMethodName());//NOI18N
                        execProperties.put(JavaRunner.PROP_EXECUTE_FILE, methodSpecs[0].getFile());
                        final String buildDir = context.getPropertyEvaluator().getProperty(ProjectProperties.BUILD_DIR);
                        if (buildDir != null) {
                            execProperties.put("tmp.dir", context.getUpdateHelper().getAntProjectHelper().resolvePath(buildDir));
                        }
                        boolean vote = true;
                        final BiFunction<Context, Map<String, Object>, Boolean> cosi = getCoSInterceptor();
                        if (cosi != null) {
                            vote = cosi.apply(context, execProperties);
                        }
                        if (vote) {
                            return JavaActionProvider.ScriptAction.Result.success(JavaRunner.execute(
                                    command.equals(SingleMethod.COMMAND_RUN_SINGLE_METHOD) ? JavaRunner.QUICK_TEST : JavaRunner.QUICK_TEST_DEBUG,
                                                  execProperties));
                        }
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
                return JavaActionProvider.ScriptAction.Result.abort();
            }

            @CheckForNull
            @org.netbeans.api.annotations.common.SuppressWarnings("PZLA_PREFER_ZERO_LENGTH_ARRAYS")
            private SingleMethod[] findTestMethods(@NonNull final Context context) {
                final Collection<? extends SingleMethod> methodSpecs
                        = context.getActiveLookup().lookupAll(SingleMethod.class);
                if (methodSpecs.isEmpty()) {
                    return null;
                }
                final FileObject[] testSrcPath = testRoots.getRoots();
                if ((testSrcPath == null) || (testSrcPath.length == 0)) {
                    return null;
                }
                Collection<SingleMethod> specs = new LinkedHashSet<>(); //#50644: remove dupes
                for (FileObject testRoot : testSrcPath) {
                    for (SingleMethod spec : methodSpecs) {
                        FileObject f = spec.getFile();
                        if (FileUtil.toFile(f) == null) {
                            continue;
                        }
                        if ((f != testRoot) && !FileUtil.isParentOf(testRoot, f)) {
                            continue;
                        }
                        if (!f.getNameExt().endsWith(".java")) {                //NOI18N
                            continue;
                        }
                        specs.add(spec);
                    }
                }
                if (specs.isEmpty()) {
                    return null;
                }
                return specs.toArray(new SingleMethod[0]);
            }
        };
    }

    private static ScriptAction createDebugFixAction(
            @NonNull final String command,
            final boolean javaModelSensitive,
            final boolean scanSensitive,
            final boolean enabledInCoS,
            @NonNull final SourceRoots sourceRoots,
            @NonNull final SourceRoots testRoots,
            @NonNull final Supplier<? extends String[]> targets) {
        return new BaseScriptAction(command, true, javaModelSensitive, scanSensitive, enabledInCoS, targets) {

            @Override
            public boolean isEnabled(Context context) {
                if (super.isEnabled(context)) {
                    FileObject fos[] = ActionProviderSupport.findSources(sourceRoots.getRoots(), context.getActiveLookup());
                    if (fos != null && fos.length == 1) {
                        return true;
                    }
                    fos = ActionProviderSupport.findTestSources(sourceRoots.getRoots(), testRoots.getRoots(), context.getActiveLookup(), false);
                    if (fos != null && fos.length == 1) {
                        return true;
                    }
                    if (LOG.isLoggable(Level.FINE)) {
                        LOG.log(Level.FINE, "Source Roots: {0} Test Roots: {1} Lookup Content: {2}",    //NOI18N
                                new Object[]{
                                    asPath(sourceRoots.getRoots()),
                                    asPath(testRoots.getRoots()),
                                    asPath(context.getActiveLookup())
                                });
                    }
                }
                return false;
            }

            @Override
            public String[] getTargetNames(Context context) {
                String[] res = super.getTargetNames(context);
                if (res != null) {
                    FileObject[] files = ActionProviderSupport.findSources(sourceRoots.getRoots(), context.getActiveLookup());
                    String path;
                    String classes = "";    //NOI18N
                    if (files != null) {
                        path = FileUtil.getRelativePath(ActionProviderSupport.getRoot(sourceRoots.getRoots(),files[0]), files[0]);
                        res = new String[] {"debug-fix"}; // NOI18N
                        classes = getTopLevelClasses(files[0]);
                    } else {
                        files = ActionProviderSupport.findTestSources(sourceRoots.getRoots(), testRoots.getRoots(), context.getActiveLookup(), false);
                        assert files != null : "findTestSources () can't be null: " + Arrays.toString(testRoots.getRoots());   //NOI18N
                        path = FileUtil.getRelativePath(ActionProviderSupport.getRoot(testRoots.getRoots(),files[0]), files[0]);
                        res = new String[] {"debug-fix-test"}; // NOI18N
                    }
                    // Convert foo/FooTest.java -> foo/FooTest
                    if (path.endsWith(".java")) { // NOI18N
                        path = path.substring(0, path.length() - 5);
                    }
                    context.setProperty("fix.includes", path); // NOI18N
                    context.setProperty("fix.classes", classes); // NOI18N
                }
                return res;
            }

            private String getTopLevelClasses (final FileObject file) {
                assert file != null;
                final String utfc = JavaActionProvider.unitTestingSupport_fixClasses;
                if (utfc != null) {
                    return utfc;
                }
                final String[] classes = new String[] {""}; //NOI18N
                JavaSource js = JavaSource.forFileObject(file);
                if (js != null) {
                    try {
                        js.runUserActionTask((ci) -> {
                            if (ci.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED).compareTo(JavaSource.Phase.ELEMENTS_RESOLVED) < 0) {
                                LOG.log(
                                        Level.WARNING,
                                        "Unable to resolve {0} to phase {1}, current phase = {2}\nDiagnostics = {3}\nFree memory = {4}",    //NOI18N
                                        new Object[]{
                                            ci.getFileObject(),
                                            JavaSource.Phase.RESOLVED,
                                            ci.getPhase(),
                                            ci.getDiagnostics(),
                                            Runtime.getRuntime().freeMemory()
                                        });
                                return;
                            }
                            final List<? extends TypeElement> types = ci.getTopLevelElements();
                            if (types.size() > 0) {
                                for (TypeElement type : types) {
                                    if (classes[0].length() > 0) {
                                        classes[0] = classes[0] + " ";            // NOI18N
                                    }
                                    classes[0] = classes[0] + type.getQualifiedName().toString().replace('.', '/') + "*.class";  // NOI18N
                                }
                            }
                        }, true);
                    } catch (java.io.IOException ioex) {
                        Exceptions.printStackTrace(ioex);
                    }
                }
                return classes[0];
            }
        };
    }

    @NonNull
    private static ScriptAction createRunAction(
            @NonNull final String command,
            final boolean javaModelSensitive,
            final boolean scanSensitive,
            final boolean enabledInCoS,
            @NonNull final Supplier<? extends String[]> targets,
            @NonNull final ActionProviderSupport.ModifiedFilesSupport mfs,
            @NonNull final Object[] mainClassServices) {
        return new BaseScriptAction(command, true, javaModelSensitive, scanSensitive, enabledInCoS, targets) {
            @Override
            public String[] getTargetNames(Context context) {
                String[] targets = super.getTargetNames(context);
                if (targets != null) {
                    // check project's main class
                    // Check whether main class is defined in this config. Note that we use the evaluator,
                    // not ep.getProperty(MAIN_CLASS), since it is permissible for the default pseudoconfig
                    // to define a main class - in this case an active config need not override it.

                    // If a specific config was selected, just skip this check for now.
                    // XXX would ideally check that that config in fact had a main class.
                    // But then evaluator.getProperty(MAIN_CLASS) would be inaccurate.
                    // Solvable but punt on it for now.
                    final boolean hasCfg = context.getActiveLookup().lookup(ProjectConfiguration.class) != null;
                    final boolean verifyMain = context.doJavaChecks() && !hasCfg && ActionProviderSupport.getJavaMainAction(context.getPropertyEvaluator()) == null;
                    String mainClass = ((Function<Boolean,String>)mainClassServices[0]).apply(verifyMain);
                    if (mainClass == null) {
                        do {
                            // show warning, if cancel then return
                            if (!((Supplier<Boolean>)mainClassServices[1]).get()) {
                                return null;
                            }
                            // No longer use the evaluator: have not called putProperties yet so it would not work.
                            mainClass = context.getPropertyEvaluator().getProperty(ProjectProperties.MAIN_CLASS);
                            mainClass = ((Function<Boolean,String>)mainClassServices[0]).apply(verifyMain);
                        } while (mainClass == null);
                    }
                    if (mainClass != null) {
                        switch (command) {
                            case COMMAND_PROFILE:
                                context.setProperty("run.class", mainClass); // NOI18N
                                break;
                            case COMMAND_DEBUG:
                            case COMMAND_DEBUG_STEP_INTO:
                                context.setProperty("debug.class", mainClass); // NOI18N
                                break;
                        }
                    }
                    final String includes = mfs.prepareDirtyList(false);
                    if (includes != null) {
                        context.setProperty(ProjectProperties.INCLUDES, includes);
                    }
                    final String[] cfgTargets = ActionProviderSupport.loadTargetsFromConfig(
                            context.getProject(),
                            context.getPropertyEvaluator())
                            .get(command);
                    if (cfgTargets != null) {
                        targets = cfgTargets;
                    }
                }
                return targets;
            }

            @Override
            public ScriptAction.Result performCompileOnSave(Context context, String[] targetNames) {
                final Map<String,Object> execProperties = ActionProviderSupport.createBaseCoSProperties(context);
                ActionProviderSupport.prepareSystemProperties(
                        context,
                        execProperties,
                        false);
                AtomicReference<ExecutorTask> _task = new AtomicReference<>();
                ActionProviderSupport.bypassAntBuildScript(
                        context,
                        execProperties,
                        EMPTY,  //not needed
                        EMPTY,  //not needed
                        _task,
                        getCoSInterceptor());
                final ExecutorTask t = _task.get();
                return t == null ?
                        JavaActionProvider.ScriptAction.Result.abort() :
                        JavaActionProvider.ScriptAction.Result.success(t);
            }
        };
    }

    @NonNull
    private static ScriptAction createNonCosAction (
            final String command,
            final boolean platformSensitive,
            final boolean javaModelSensitive,
            final boolean scanSensitive,
            final boolean enabledInCoS,
            @NonNull final Supplier<? extends String[]> targets,
            @NonNull final Map<String,String> props) {
        return new BaseScriptAction(command, platformSensitive, javaModelSensitive, scanSensitive, enabledInCoS, targets, props);
    }

    private static void logNoFiles(
            @NonNull final SourceRoots sourceRoots,
            @NonNull final SourceRoots testRoots,
            @NonNull final Context context) {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.log(Level.FINE, "Source Roots: {0} Test Roots: {1} Lookup Content: {2}",    //NOI18N
                    new Object[]{
                        asPath(sourceRoots.getRoots()),
                        asPath(testRoots.getRoots()),
                        asPath(context.getActiveLookup())
                    });
        }
    }

    @NonNull
    private static CharSequence asPath(final @NonNull Lookup context) {
        final Set<FileObject> fos = new HashSet<>();
        context.lookupAll(DataObject.class).stream()
                .map(DataObject::getPrimaryFile)
                .forEach(fos::add);
        context.lookupAll(FileObject.class).stream()
                .forEach(fos::add);
        return asPath(fos.toArray(new FileObject[0]));
    }

    @NonNull
    private static CharSequence asPath(@NonNull final FileObject[] fos) {
        if (fos == null) {
            return null;
        }
        return Arrays.stream(fos)
                .map(FileUtil::getFileDisplayName)
                .collect(Collectors.joining(File.pathSeparator));
    }

    private static final class SimpleAction implements Action {
        private final String name;
        private final Consumer<? super Context> performer;

        SimpleAction(
                @NonNull final String name,
                final Consumer<? super Context> performer) {
            Parameters.notNull("name", name);           //NOI18N
            Parameters.notNull("performer", performer); //NOI18N
            this.name = name;
            this.performer = performer;
        }

        @Override
        @NonNull
        public final String getCommand() {
            return this.name;
        }

        @Override
        public boolean isEnabled(@NonNull final Context context) {
            return true;
        }

        @Override
        public void invoke(@NonNull final Context context) {
            performer.accept(context);
        }
    }

    private static class BaseScriptAction extends ScriptAction {
        private final Supplier<? extends String[]> targetNames;
        private final Map<String,String> initialProps;

        BaseScriptAction(
                @NonNull final String command,
                final boolean ps,
                final boolean jms,
                final boolean sc,
                final boolean cos,
                Supplier<? extends String[]> targetNames) {
            this(command, ps, jms, sc, cos, targetNames, Collections.emptyMap());
        }

        BaseScriptAction(
                @NonNull final String command,
                final boolean ps,
                final boolean jms,
                final boolean sc,
                final boolean cos,
                @NonNull Supplier<? extends String[]> targetNames,
                @NonNull Map<String,String> initialProps) {
            super(command, null, ps, jms, sc, cos);
            this.targetNames = targetNames;
            this.initialProps = initialProps;
        }

        @Override
        public String[] getTargetNames(JavaActionProvider.Context context) {
            for (Map.Entry<String,String> e : initialProps.entrySet()) {
                context.setProperty(e.getKey(), e.getValue());
            }
            return targetNames.get();
        }
    }

    private static class BaseRunSingleAction extends BaseScriptAction {
        private final SourceRoots sourceRoots;
        private final SourceRoots testRoots;

        BaseRunSingleAction(
                @NonNull final String command,
                final boolean ps,
                final boolean jms,
                final boolean sc,
                final boolean cos,
                final SourceRoots sourceRoots,
                final SourceRoots testRoots,
                final Supplier<? extends String[]> targetNames) {
            super(command, ps, jms, sc, cos, targetNames);
            this.sourceRoots = sourceRoots;
            this.testRoots = testRoots;
        }

        @Override
        public Result performCompileOnSave(Context context, String[] targetNames) {
            if (Arrays.equals(targetNames, new String[]{"test"})) {
                return Result.follow();
            }
            final String command = context.getCommand();
            final Map<String,Object> execProperties = ActionProviderSupport.createBaseCoSProperties(context);
            if (COMMAND_RUN_SINGLE.equals(command) || COMMAND_DEBUG_SINGLE.equals(command) || COMMAND_PROFILE_SINGLE.equals(command)) {
                ActionProviderSupport.prepareSystemProperties(
                        context,
                        execProperties,
                        false);
                switch (command) {
                    case COMMAND_RUN_SINGLE:
                        execProperties.put(JavaRunner.PROP_CLASSNAME, context.getProperty("run.class"));    //NOI18N
                        break;
                    case COMMAND_DEBUG_SINGLE:
                        execProperties.put(JavaRunner.PROP_CLASSNAME, context.getProperty("debug.class"));  //NOI18N
                        break;
                    case COMMAND_PROFILE_SINGLE:
                        execProperties.put(JavaRunner.PROP_CLASSNAME, context.getProperty("profile.class"));
                        break;
                    default:
                        throw new IllegalStateException(command);
                }
                final AtomicReference<ExecutorTask> _task = new AtomicReference<>();
                ActionProviderSupport.bypassAntBuildScript(
                        context,
                        execProperties,
                        sourceRoots.getRoots(),
                        testRoots.getRoots(),
                        _task,
                        getCoSInterceptor());
                final ExecutorTask t = _task.get();
                return t == null ?
                        JavaActionProvider.ScriptAction.Result.abort() :
                        JavaActionProvider.ScriptAction.Result.success(t);
            } else if (COMMAND_TEST_SINGLE.equals(command) || COMMAND_DEBUG_TEST_SINGLE.equals(command) || COMMAND_PROFILE_TEST_SINGLE.equals(command)) {
                final FileObject[] files = ActionProviderSupport.findTestSources(sourceRoots.getRoots(), testRoots.getRoots(), context.getActiveLookup(), true);
                try {
                    ActionProviderSupport.prepareSystemProperties(context, execProperties, true);
                    execProperties.put(JavaRunner.PROP_EXECUTE_FILE, files[0]);
                    String buildDir = context.getPropertyEvaluator().getProperty(ProjectProperties.BUILD_DIR);
                    if (buildDir != null) { // #211543
                        context.setProperty("tmp.dir", context.getUpdateHelper().getAntProjectHelper().resolvePath(buildDir));  //NOI18N
                    }
                    boolean vote = true;
                    final BiFunction<Context, Map<String, Object>, Boolean> cosi = getCoSInterceptor();
                    if (cosi != null) {
                        vote = cosi.apply(context, execProperties);
                    }
                    if (vote) {
                        return JavaActionProvider.ScriptAction.Result.success(JavaRunner.execute(
                                command.equals(COMMAND_TEST_SINGLE) ? JavaRunner.QUICK_TEST : (COMMAND_DEBUG_TEST_SINGLE.equals(command) ? JavaRunner.QUICK_TEST_DEBUG :JavaRunner.QUICK_TEST_PROFILE),
                                           execProperties));
                    }
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
                return JavaActionProvider.ScriptAction.Result.abort();
            } else {
                assert false : "Unhandled command:" + command;
            }
            return Result.follow();
        }

        @NonNull
        final SourceRoots getSourceRoots() {
            return sourceRoots;
        }

        @NonNull
        final SourceRoots getTestRoots() {
            return testRoots;
        }
    }
}
