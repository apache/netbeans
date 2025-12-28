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

import java.awt.EventQueue;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.project.runner.JavaRunner;
import org.netbeans.api.java.source.BuildArtifactMapper;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.java.api.common.SourceRoots;
import org.netbeans.modules.java.api.common.ant.UpdateHelper;
import org.netbeans.modules.java.api.common.applet.AppletSupport;
import org.netbeans.modules.java.api.common.classpath.ClassPathProviderImpl;
import org.netbeans.modules.java.api.common.util.CommonProjectUtils;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.SingleMethod;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;

/** Action provider which was originally written for J2SE project and later
 * refactored here so that other EE project types requiring handling of Java
 * related actions can reuse and extend it.
 *
 * @since org.netbeans.modules.java.api.common/1 1.20
 */
public abstract class BaseActionProvider implements ActionProvider {
    public static final String AUTOMATIC_BUILD_TAG = ".netbeans_automatic_build";

    private static final Logger LOG = Logger.getLogger(BaseActionProvider.class.getName());

    public static final String PROPERTY_RUN_SINGLE_ON_SERVER = "run.single.on.server";

    // Project
    private final Project project;

    private final AntProjectHelper antProjectHelper;

    private final Callback callback;
    private final Function<String,ClassPath> classpaths;

    // Ant project helper of the project
    private UpdateHelper updateHelper;
    //Property evaluator
    private final PropertyEvaluator evaluator;
    private volatile String buildXMLName;
    private SourceRoots projectSourceRoots;
    private SourceRoots projectTestRoots;

    private boolean serverExecution = false;
    private ActionProviderSupport.UserPropertiesPolicy userPropertiesPolicy;
    private final List<? extends JavaActionProvider.AntTargetInvocationListener> listeners;
    private final AtomicReference<JavaActionProvider> delegate;

    public BaseActionProvider(Project project, UpdateHelper updateHelper, PropertyEvaluator evaluator, 
            SourceRoots sourceRoots, SourceRoots testRoots, AntProjectHelper antProjectHelper, Callback callback) {
        this.antProjectHelper = antProjectHelper;
        this.callback = callback;
        this.classpaths = (id) -> getCallback().getProjectSourcesClassPath(id);
        this.updateHelper = updateHelper;
        this.project = project;
        this.evaluator = evaluator;
        this.projectSourceRoots = sourceRoots;
        this.projectTestRoots = testRoots;
        this.evaluator.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(final PropertyChangeEvent evt) {
                synchronized (BaseActionProvider.class) {
                    final String propName = evt.getPropertyName();
                    if (propName == null || BUILD_SCRIPT.equals(propName)) {
                        buildXMLName = null;
                    }
                }
            }
        });
        this.listeners = Collections.singletonList(new EventAdaptor());
        this.delegate = new AtomicReference<>();
    }

    protected abstract String[] getPlatformSensitiveActions();

    protected abstract String[] getActionsDisabledForQuickRun();

    /** Return map from commands to ant targets */
    public abstract Map<String,String[]> getCommands();

    /**Return set of commands which are affected by background scanning*/
    protected abstract Set<String> getScanSensitiveActions();

    /**Return set of commands which need java model up to date*/
    protected abstract Set<String> getJavaModelActions();

    protected abstract boolean isCompileOnSaveEnabled();
    
    /**
     * Returns CoS update status.
     * @return true if CoS update is enabled
     * @since 1.82
     */
    protected boolean isCompileOnSaveUpdate() {
        return isCompileOnSaveEnabled();
    }

    protected void setServerExecution(boolean serverExecution) {
        this.serverExecution = serverExecution;
    }

    protected boolean isServerExecution() {
        return serverExecution;
    }

    protected PropertyEvaluator getEvaluator() {
        return evaluator;
    }

    protected UpdateHelper getUpdateHelper() {
        return updateHelper;
    }

    protected AntProjectHelper getAntProjectHelper() {
        return antProjectHelper;
    }

    /**
     * Callback for project private data.
     *
     * @return
     * @see Callback
     * @see Callback2
     */
    protected Callback getCallback() {
        return callback;
    }

    public void startFSListener () {
    }

    // Main build.xml location
    public static final String BUILD_SCRIPT = ProjectProperties.BUILD_SCRIPT;

    @NonNull
    public static String getBuildXmlName (final Project project, PropertyEvaluator evaluator) {
        return CommonProjectUtils.getBuildXmlName(evaluator, null);
    }

    public static FileObject getBuildXml (final Project project, PropertyEvaluator evaluator) {
        return getBuildXml(project, getBuildXmlName(project, evaluator));
    }
    
    private static FileObject getBuildXml(
            @NonNull final Project project,
            @NonNull final String buildXmlName) {
        return project.getProjectDirectory().getFileObject (buildXmlName);
    }

    @CheckForNull
    protected final FileObject findBuildXml() {
        String name = buildXMLName;
        if (name == null) {
            buildXMLName = name = getBuildXmlName(project, evaluator);
        }
        assert name != null;
        return getBuildXml(project, name);
    }

    protected final Project getProject() {
        return project;
    }

    @Messages("LBL_No_Build_XML_Found=The project does not have a build script.")
    @Override
    public void invokeAction( final String command, final Lookup context ) throws IllegalArgumentException {
        assert EventQueue.isDispatchThread();
        if (isSupportedByDelegate(command)) {
            getDelegate().invokeAction(command, context);
            return;
        }
        final JavaActionProvider.Context ctx = new JavaActionProvider.Context(
                project,
                updateHelper,
                evaluator,
                classpaths,
                command,
                context,
                userPropertiesPolicy,
                this::getProjectPlatform,
                this::getAdditionalProperties,
                this::getConcealedProperties,
                this::getCompileOnSaveOperations,
                listeners);
        try {
            ActionProviderSupport.invokeTarget(new JavaActionProvider.ScriptAction(
                    command,
                    null,
                    false,
                    getJavaModelActions().contains(command),
                    getScanSensitiveActions().contains(command),
                    !Arrays.asList(getActionsDisabledForQuickRun()).contains(command)) {

                @Override
                public String[] getTargetNames(JavaActionProvider.Context context) {
                    final Properties p = new Properties();
                    final String[] result = BaseActionProvider.this.getTargetNames(
                            ctx.getCommand(),
                            ctx.getActiveLookup(),
                            p,
                            ctx.doJavaChecks());
                    for (Map.Entry<Object,Object> e : p.entrySet()) {
                        ctx.setProperty((String)e.getKey(), (String)e.getValue());
                    }
                    return result;
                }

                @Override
                public boolean isEnabled(JavaActionProvider.Context context) {
                    return BaseActionProvider.this.isActionEnabled(command, context.getActiveLookup());
                }
            }, ctx);
        } finally {
            userPropertiesPolicy = ctx.getUserPropertiesPolicy();
        }
    }

    protected void updateJavaRunnerClasspath(String command, Map<String, Object> execProperties) {
    }
    /**
     * Compatibility
     *
     */
    public String[] getTargetNames(String command, Lookup context, Properties p) throws IllegalArgumentException {
        return getTargetNames(command, context, p, true);
    }

    /**
     * @return array of targets or null to stop execution; can return empty array
     */
    @Messages({"# {0} - class name", "LBL_No_Main_Class_Found=Class \"{0}\" does not have a main method."})
    @org.netbeans.api.annotations.common.SuppressWarnings("PZLA_PREFER_ZERO_LENGTH_ARRAYS")
    public @CheckForNull String[] getTargetNames(String command, Lookup context, Properties p, boolean doJavaChecks) throws IllegalArgumentException {
        final JavaActionProvider.Action action = getDelegate().getAction(command);
        if (action != null) {
            //Handled by delegate
            return Optional.of(action)
                .map((a) -> a instanceof JavaActionProvider.ScriptAction ?
                        ((JavaActionProvider.ScriptAction) a) :
                        null)
                .map((sa) -> {
                    final JavaActionProvider.Context ctx = new JavaActionProvider.Context(
                            project,
                            updateHelper,
                            evaluator,
                            classpaths,
                            command,
                            context,
                            null,
                            null,
                            null,
                            null,
                            this::getCompileOnSaveOperations,
                            Collections.emptyList());
                    final String[] targetNames = sa.getTargetNames(ctx);
                    if (targetNames != null) {
                        Optional.ofNullable(ctx.getProperties())
                                .ifPresent(p::putAll);
                    }
                    return targetNames;
                })
                .orElse(null);
        } else {
            //Custom unknown command
            if (Arrays.asList(getPlatformSensitiveActions()).contains(command)) {
                if (getProjectPlatform() == null) {
                    ActionProviderSupport.showPlatformWarning(project);
                    return null;
                }
            }
            LOG.log(Level.FINE, "COMMAND: {0}", command);       //NOI18N
            String[] targetNames = new String[0];
            Map<String,String[]> targetsFromConfig = ActionProviderSupport.loadTargetsFromConfig(project, evaluator);
            String[] targets = targetsFromConfig.get(command);
            targetNames = (targets != null) ? targets : getCommands().get(command);
            if (targetNames == null) {
                throw new IllegalArgumentException(command);
            }
            return targetNames;
        }
    }

    /**
     * Returns the project's {@link JavaPlatform}.
     * @return the project's {@link JavaPlatform} or null when project's
     * {@link JavaPlatform} is broken.
     * @since 1.66
     */
    @CheckForNull
    protected JavaPlatform getProjectPlatform() {
        return ActionProviderSupport.getActivePlatform(project, evaluator, ProjectProperties.PLATFORM_ACTIVE);
    }

    /**
     * @param targetNames caller of this method must set this parameter to empty 
     *  modifiable array; implementor of this method can return alternative target
     *  names to be used to handle this Java class
     */
    protected boolean handleJavaClass(Properties p, FileObject javaFile, String command, List<String> targetNames) {
        return false;
    }

    /**
     * Gets the project main class to be executed.
     * @param verify if true the java checks should be performed
     * and the main class should be returned only if it's valid
     * @return the main class
     * @since 1.66
     */
    @CheckForNull
    protected String getProjectMainClass(final boolean verify) {
        return ActionProviderSupport.getProjectMainClass(
                project,
                evaluator,
                projectSourceRoots,
                classpaths,
                verify);
    }

    private boolean isSupportedByDelegate(final String command) {
        for (String action : getDelegate().getSupportedActions()) {
            if (action.equals(command)) {
                return true;
            }
        }
        return false;
    }

    @NonNull
    private JavaActionProvider getDelegate() {
        JavaActionProvider jap = delegate.get();
        if (jap == null) {
            jap = createDelegate();
            if (!delegate.compareAndSet(null, jap)) {
                jap = delegate.get();
                assert jap != null : "Transition non-null -> null for delegate.";   //NOI18N
            }
        }
        return jap;
    }

    @NonNull
    private JavaActionProvider createDelegate() {
        final JavaActionProvider.Builder builder = JavaActionProvider.Builder.newInstance(project, updateHelper, evaluator, projectSourceRoots, projectTestRoots, classpaths)
                .setAdditionalPropertiesProvider(this::getAdditionalProperties)
                .setConcealedPropertiesProvider(this::getConcealedProperties)
                .setCompileOnSaveOperationsProvider(this::getCompileOnSaveOperations)
                .setActivePlatformProvider(this::getProjectPlatform)
                .setProjectMainClassProvider(this::getProjectMainClass)
                .setProjectMainClassSelector(this::showMainClassSelector);
        final Set<? extends String> supported = new HashSet<>(Arrays.asList(getSupportedActions()));
        for (String op : new String[] {COMMAND_DELETE, COMMAND_RENAME, COMMAND_MOVE, COMMAND_COPY}) {
            if (supported.contains(op)) {
                builder.addAction(builder.createProjectOperation(op));
            }
        }
        final Map<String,String[]> cmds = getCommands();
        final Set<String> scanSensitive = getScanSensitiveActions();
        final Set<String> modelSensitive = getJavaModelActions();
        final Set<String> disabledForCos = new HashSet<>();
        Collections.addAll(disabledForCos, getActionsDisabledForQuickRun());
        final CustomRunner cr = new CustomRunner();
        final Set<String> disabledByServerExecuion = new HashSet<>(Arrays.asList(
                COMMAND_RUN, COMMAND_DEBUG, COMMAND_PROFILE, COMMAND_DEBUG_STEP_INTO));
        final String[] commands = {
            COMMAND_CLEAN, COMMAND_BUILD, COMMAND_REBUILD,
            COMMAND_RUN, COMMAND_DEBUG, COMMAND_PROFILE, COMMAND_DEBUG_STEP_INTO,
            COMMAND_TEST,
            COMMAND_COMPILE_SINGLE,
            COMMAND_RUN_SINGLE, COMMAND_DEBUG_SINGLE, COMMAND_PROFILE_SINGLE,
            COMMAND_TEST_SINGLE, COMMAND_DEBUG_TEST_SINGLE, COMMAND_PROFILE_TEST_SINGLE,
            SingleMethod.COMMAND_RUN_SINGLE_METHOD, SingleMethod.COMMAND_DEBUG_SINGLE_METHOD,
            JavaProjectConstants.COMMAND_DEBUG_FIX
        };
        final boolean brokenAPI = overridesGetTargetNames();
        for (String cmd : commands) {
            if (supported.contains(cmd)) {
                String[] targets = cmds.get(cmd);
                JavaActionProvider.ScriptAction action;
                if (targets != null) {
                    action = builder.createDefaultScriptAction(
                        cmd,
                        modelSensitive.contains(cmd),
                        scanSensitive.contains(cmd),
                        !disabledForCos.contains(cmd),
                        () -> targets,
                        cr);
                    action.setCoSInterceptor((c,m) -> {
                        updateJavaRunnerClasspath(c.getCommand(), m);
                        return true;
                    });
                } else {
                    String[] jarEnabledTargets, jarDisabledTargets;
                    switch (cmd) {
                        case COMMAND_BUILD:
                            jarEnabledTargets = new String[] {"jar"};   //NOI18N
                            jarDisabledTargets = new String[] {"compile"};  //NOI18N
                            break;
                        case COMMAND_REBUILD:
                            jarEnabledTargets = new String[] {"clean","jar"};   //NOI18N
                            jarDisabledTargets = new String[] {"clean","compile"};  //NOI18N
                            break;
                        default:
                            jarEnabledTargets = jarDisabledTargets = null;
                    }
                    action = jarEnabledTargets != null ? builder.createDefaultScriptAction(
                            cmd,
                            modelSensitive.contains(cmd),
                            scanSensitive.contains(cmd),
                            !disabledForCos.contains(cmd),
                            ActionProviderSupport.createConditionalTarget(
                                    evaluator,
                                    ActionProviderSupport.createJarEnabledPredicate(),
                                    jarEnabledTargets,
                                    jarDisabledTargets
                            )) : null;
                }
                if (action != null) {
                    if (disabledByServerExecuion.contains(cmd)) {
                        action = new ServerExecutionAwareAction(action);
                    }
                    if (brokenAPI) {
                        action = new BrokenAPIActionDecorator(action);
                    }
                    builder.addAction(action);
                }
            }
        }
        return builder.build();
    }

    private boolean overridesGetTargetNames() {
        boolean vote = false;
        try {
            Method m = this.getClass().getMethod("getTargetNames", String.class, Lookup.class, Properties.class); //NOI18N
            vote |= m.getDeclaringClass() != BaseActionProvider.class;
            m = this.getClass().getDeclaredMethod("getTargetNames", String.class, Lookup.class, Properties.class, Boolean.TYPE);    //NOI18N
            vote |= m.getDeclaringClass() != BaseActionProvider.class;
        } catch (NoSuchMethodException e) {
            vote = true;
        }
        return vote;
    }

    @NonNull
    private Set<String> getConcealedProperties(
            @NonNull final String command,
            @NonNull final Lookup context) {
        final Callback clb = getCallback();
        if (clb instanceof Callback3) {
            return ((Callback3)clb).createConcealedProperties(command, context);
        }
        return Collections.emptySet();
    }

    @NonNull
    private Map<String,String> getAdditionalProperties(
            @NonNull final String command,
            @NonNull final Lookup context) {
        final Callback clb = getCallback();
        if (clb instanceof Callback3) {
            return ((Callback3)clb).createAdditionalProperties(command, context);
        }
        return Collections.emptyMap();
    }

    @NonNull
    private Set<? extends JavaActionProvider.CompileOnSaveOperation> getCompileOnSaveOperations() {
        if (!BuildArtifactMapper.isCompileOnSaveSupported())
            return Collections.emptySet();
        final Set<JavaActionProvider.CompileOnSaveOperation> ops = EnumSet.noneOf(JavaActionProvider.CompileOnSaveOperation.class);
        if (isCompileOnSaveEnabled()) {
            ops.add(JavaActionProvider.CompileOnSaveOperation.EXECUTE);
        }
        if (isCompileOnSaveUpdate()) {
            ops.add(JavaActionProvider.CompileOnSaveOperation.UPDATE);
        }
        return Collections.unmodifiableSet(ops);
    }

    /**
     * Shows a selector of project main class.
     * @return true if main class was selected, false when project execution was canceled.
     * @since 1.66
     */
    protected boolean showMainClassSelector() {
        return ActionProviderSupport.showCustomizer(
            project,
            updateHelper,
            evaluator,
            projectSourceRoots,
            classpaths);
    }

    @Override
    public boolean isActionEnabled( String command, Lookup context ) {
        if (isSupportedByDelegate(command)) {
            return getDelegate().isActionEnabled(command, context);
        }
        if (Arrays.asList(getActionsDisabledForQuickRun()).contains(command)
            && isCompileOnSaveUpdate()
            && !ActionProviderSupport.allowAntBuild(evaluator, updateHelper)) {
            return false;
        }
        return true;
    }

    /**
     * Callback for accessing project private data.
     */
    public static interface Callback {
        ClassPath getProjectSourcesClassPath(String type);
        ClassPath findClassPath(FileObject file, String type);
    }

    /**
     * Callback for accessing project private data and supporting
     * ant invocation hooks.
     * 
     * @since 1.29
     */
    public static interface Callback2 extends Callback {

        /**
         * Called before an <i>ant</i> target is invoked. Note that call to
         * {@link #invokeAction(java.lang.String, org.openide.util.Lookup)} does
         * not necessarily means call to ant.
         *
         * @param command the command to be invoked
         * @param context the invocation context
         */
        void antTargetInvocationStarted(final String command, final Lookup context);

        /**
         * Called after the <i>ant</i> target invocation. This does not reflect
         * whether the ant target returned error or not, just successful invocation.
         * Note that call to {@link #invokeAction(java.lang.String, org.openide.util.Lookup)}
         * does not necessarily means call to ant.
         *
         * @param command executed command
         * @param context the invocation context
         */
        void antTargetInvocationFinished(final String command, final Lookup context, int result);

        /**
         * Called when the <i>ant</i> target invocation failed. Note that call to
         * {@link #invokeAction(java.lang.String, org.openide.util.Lookup)} does
         * not necessarily means call to ant.
         *
         * @param command failed command
         * @param context the invocation context
         */
        void antTargetInvocationFailed(final String command, final Lookup context);

    }

    /**
     * Callback for accessing project private data and supporting
     * ant invocation hooks.
     *
     * @since 1.58
     */
    public static interface Callback3 extends Callback2 {
        /**
         * Creates additional properties passed to the <i>ant</i>.
         * Called before an <i>ant</i> target is invoked. Note that call to
         * {@link #invokeAction(java.lang.String, org.openide.util.Lookup)} does
         * not necessarily means call to ant.
         *
         * @param command the command to be invoked
         * @param context the invocation context
         * @return the {@link Map} of additional properties.
         */
        @NonNull
        Map<String,String> createAdditionalProperties(@NonNull String command, @NonNull Lookup context);


        /**
         * Returns names of concealed properties.
         * Values of such properties are not printed into UI.
         *
         * @param command the command to be invoked
         * @param context the invocation context
         * @return the {@link Set} of property names.
         */
        @NonNull
        Set<String> createConcealedProperties(@NonNull String command, @NonNull Lookup context);
    }

    public static final class CallbackImpl implements Callback {

        private ClassPathProviderImpl cp;

        public CallbackImpl(ClassPathProviderImpl cp) {
            this.cp = cp;
        }

        @Override
        public ClassPath getProjectSourcesClassPath(String type) {
            return cp.getProjectSourcesClassPath(type);
        }

        @Override
        public ClassPath findClassPath(FileObject file, String type) {
            return cp.findClassPath(file, type);
        }

    }

    private static @CheckForNull Collection<? extends String> asPaths(final @NullAllowed FileObject[] fos) {
        if (fos == null) {
            return null;
        }
        final Collection<String> result = new ArrayList<String>(fos.length);
        for (FileObject fo : fos) {
            result.add(FileUtil.getFileDisplayName(fo));
        }
        return result;
    }

    private static @NonNull Collection<? extends String> asPaths(final @NonNull Lookup context) {
        final Collection<? extends DataObject> dobjs = context.lookupAll(DataObject.class);
        final Collection<String> result = new ArrayList<String>(dobjs.size());
        for (DataObject dobj : dobjs) {
            result.add(FileUtil.getFileDisplayName(dobj.getPrimaryFile()));
        }
        return result;
    }

    private class EventAdaptor implements JavaActionProvider.AntTargetInvocationListener {

        @Override
        public void antTargetInvocationStarted(String command, Lookup context) {
            Optional.ofNullable((getCallback()))
                    .map((cb) -> cb instanceof Callback2 ? (Callback2) cb : null)
                    .ifPresent((cb) -> cb.antTargetInvocationStarted(command, context));
        }

        @Override
        public void antTargetInvocationFinished(String command, Lookup context, int result) {
            Optional.ofNullable((getCallback()))
                    .map((cb) -> cb instanceof Callback2 ? (Callback2) cb : null)
                    .ifPresent((cb) -> cb.antTargetInvocationFinished(command, context, result));
        }

        @Override
        public void antTargetInvocationFailed(String command, Lookup context) {
            Optional.ofNullable((getCallback()))
                    .map((cb) -> cb instanceof Callback2 ? (Callback2) cb : null)
                    .ifPresent((cb) -> cb.antTargetInvocationFailed(command, context));
        }
    }

    private final class ServerExecutionAwareAction extends JavaActionProvider.ScriptAction {
        private final JavaActionProvider.ScriptAction delegate;

        ServerExecutionAwareAction(
                @NonNull final JavaActionProvider.ScriptAction delegate) {
            super(
                    delegate.getCommand(),
                    null,
                    delegate.getActionFlags().contains(ActionProviderSupport.ActionFlag.PLATFORM_SENSITIVE),
                    delegate.getActionFlags().contains(ActionProviderSupport.ActionFlag.JAVA_MODEL_SENSITIVE),
                    delegate.getActionFlags().contains(ActionProviderSupport.ActionFlag.SCAN_SENSITIVE),
                    delegate.getActionFlags().contains(ActionProviderSupport.ActionFlag.COS_ENABLED));
            this.delegate = delegate;
        }

        @Override
        public String[] getTargetNames(JavaActionProvider.Context context) {
            final boolean justAnotherServerExecution = context.getProperty(PROPERTY_RUN_SINGLE_ON_SERVER) != null;
            context.removeProperty(PROPERTY_RUN_SINGLE_ON_SERVER);
            if (!isServerExecution() && ! justAnotherServerExecution) {
                return delegate.getTargetNames(context);
            } else {
                final Map<String,String[]> targetsFromConfig = ActionProviderSupport.loadTargetsFromConfig(project, evaluator);
                String[] targets = targetsFromConfig.get(this.getCommand());
                if (targets == null) {
                    targets = getCommands().get(this.getCommand());
                }
                if (targets == null) {
                    throw new IllegalArgumentException(this.getCommand());
                }
                return targets;
            }
        }

        @Override
        public Result performCompileOnSave(JavaActionProvider.Context context, String[] targetNames) {
            if (!isServerExecution()) {
                return delegate.performCompileOnSave(context, targetNames);
            } else {
                return JavaActionProvider.ScriptAction.Result.follow();
            }
        }

        @Override
        public boolean isEnabled(JavaActionProvider.Context context) {
            return delegate.isEnabled(context);
        }
    }

    private final class BrokenAPIActionDecorator extends JavaActionProvider.ScriptAction {
        private final JavaActionProvider.ScriptAction delegate;
        private final ThreadLocal<JavaActionProvider.Context> frame;

        BrokenAPIActionDecorator(@NonNull final JavaActionProvider.ScriptAction delegate) {
            super(
                    delegate.getCommand(),
                    null,
                    delegate.getActionFlags().contains(ActionProviderSupport.ActionFlag.PLATFORM_SENSITIVE),
                    delegate.getActionFlags().contains(ActionProviderSupport.ActionFlag.JAVA_MODEL_SENSITIVE),
                    delegate.getActionFlags().contains(ActionProviderSupport.ActionFlag.SCAN_SENSITIVE),
                    delegate.getActionFlags().contains(ActionProviderSupport.ActionFlag.COS_ENABLED));
            this.delegate = delegate;
            this.frame = new ThreadLocal<>();
        }

        @Override
        public String[] getTargetNames(JavaActionProvider.Context context) {
            final JavaActionProvider.Context enclosingContext = frame.get();
            if (enclosingContext == null) {
                frame.set(context);
                try {
                    final Properties p = new Properties();
                    final String[] res = BaseActionProvider.this.getTargetNames(
                            context.getCommand(),
                            context.getActiveLookup(),
                            p,
                            context.doJavaChecks());
                    p.entrySet().forEach((e) -> context.setProperty(
                            (String) e.getKey(),
                            (String) e.getValue()));
                    return res;
                } finally {
                    frame.remove();
                }
            } else {
                return delegate.getTargetNames(enclosingContext);
            }
        }

        @Override
        public Result performCompileOnSave(JavaActionProvider.Context context, String[] targetNames) {
            return delegate.performCompileOnSave(context, targetNames);
        }

        @Override
        public boolean isEnabled(JavaActionProvider.Context context) {
            return delegate.isEnabled(context);
        }
    }

    private class CustomRunner implements JavaActionProvider.Builder.CustomFileExecutor {
        @CheckForNull
        public String[] getTargetNames(
                @NonNull final FileObject fileToExecute,
                @NonNull final JavaActionProvider.Context context) {
            final boolean isTest = ActionProviderSupport.findTestSources(
                    projectSourceRoots.getRoots(),
                    projectTestRoots.getRoots(),
                    context.getActiveLookup(),
                    false) != null;
            if (!isTest) {
                String[] res = getTargetsForApplet(fileToExecute, context);
                if (res != null) {
                    return res;
                }
                final Properties p = new Properties();
                final List<String> targetNames = new ArrayList<>();
                if (handleJavaClass(p, fileToExecute, context.getCommand(), targetNames)) {
                    for (Map.Entry<Object,Object> e : p.entrySet()) {
                        context.setProperty((String)e.getKey(), (String)e.getValue());
                    }
                    return targetNames.isEmpty() ?
                            null :  //throw exc?
                            targetNames.toArray(new String[0]);
                }
            }
            return null;
        }

        @Override
        public JavaActionProvider.ScriptAction.Result performCompileOnSave(JavaActionProvider.Context context, String[] targetNames) {
            if (targetNames.length == 1 && (JavaRunner.QUICK_RUN_APPLET.equals(targetNames[0]) || JavaRunner.QUICK_DEBUG_APPLET.equals(targetNames[0]) || JavaRunner.QUICK_PROFILE_APPLET.equals(targetNames[0]))) {
                try {
                    final Map<String, Object> execProperties = ActionProviderSupport.createBaseCoSProperties(context);
                    final FileObject[] selectedFiles = ActionProviderSupport.findSources(projectSourceRoots.getRoots(), context.getActiveLookup());
                    if (selectedFiles != null) {
                        FileObject file = selectedFiles[0];
                        String url = context.getProperty("applet.url");
                        execProperties.put("applet.url", url);
                        execProperties.put(JavaRunner.PROP_EXECUTE_FILE, file);
                        ActionProviderSupport.prepareSystemProperties(context, execProperties, false);
                        return JavaActionProvider.ScriptAction.Result.success(JavaRunner.execute(targetNames[0], execProperties));
                    }
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                    return JavaActionProvider.ScriptAction.Result.abort();
                }
            }
            return JavaActionProvider.ScriptAction.Result.follow();
        }

        private String[] getTargetsForApplet(
                @NonNull final FileObject file,
                @NonNull final JavaActionProvider.Context context) {
            String[] res = null;
            if (AppletSupport.isApplet(file)) {
                EditableProperties ep = updateHelper.getProperties (AntProjectHelper.PROJECT_PROPERTIES_PATH);
                String jvmargs = ep.getProperty(ProjectProperties.RUN_JVM_ARGS);
                // do this only when security policy is not set manually
                if ((jvmargs == null) || !(jvmargs.indexOf("java.security.policy") != -1)) {  //NOI18N
                    AppletSupport.generateSecurityPolicy(project.getProjectDirectory());
                    if ((jvmargs == null) || (jvmargs.length() == 0)) {
                        ep.setProperty(ProjectProperties.RUN_JVM_ARGS, "-Djava.security.policy=applet.policy"); //NOI18N
                    } else {
                        ep.setProperty(ProjectProperties.RUN_JVM_ARGS, jvmargs + " -Djava.security.policy=applet.policy"); //NOI18N
                    }
                    updateHelper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
                    try {
                        ProjectManager.getDefault().saveProject(project);
                    } catch (IOException e) {
                        ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL, "Error while saving project: " + e);
                    }
                }
                URL url;
                if (file.existsExt("html") || file.existsExt("HTML")) { //NOI18N
                    url = copyAppletHTML(file, "html"); //NOI18N
                } else {
                    url = generateAppletHTML(file);
                }
                if (url == null) {
                    return null;
                }
                context.setProperty("applet.url", url.toString()); // NOI18N
                String[] pathFqn = ActionProviderSupport.pathAndFqn(file, projectSourceRoots.getRoots());
                if (pathFqn != null) {
                    switch(context.getCommand()) {
                        case COMMAND_RUN_SINGLE:
                            res = new String[] {"run-applet"}; // NOI18N
                            break;
                        case COMMAND_DEBUG_SINGLE:
                            context.setProperty("debug.class", pathFqn[1]); // NOI18N
                            res = new String[] {"debug-applet"}; // NOI18N
                            break;
                        case COMMAND_PROFILE_SINGLE:
                            context.setProperty("run.class", pathFqn[1]); // NOI18N
                            res = new String[]{"profile-applet"}; // NOI18N
                            break;
                    }
                }
            }
            return res;
        }

        private URL copyAppletHTML(FileObject file, String ext) {
            URL url = null;
            try {
                String buildDirProp = evaluator.getProperty("build.dir"); //NOI18N
                FileObject buildDir = buildDirProp != null ? updateHelper.getAntProjectHelper().resolveFileObject(buildDirProp) : null;

                if (buildDir == null) {
                    buildDir = FileUtil.createFolder(project.getProjectDirectory(), buildDirProp);
                }

                FileObject htmlFile = file.getParent().getFileObject(file.getName(), "html"); //NOI18N
                if (htmlFile == null) {
                    htmlFile = file.getParent().getFileObject(file.getName(), "HTML"); //NOI18N
                }
                if (htmlFile == null) {
                    return null;
                }

                FileObject existingFile = buildDir.getFileObject(htmlFile.getName(), htmlFile.getExt());
                if (existingFile != null) {
                    existingFile.delete();
                }

                FileObject targetHtml = htmlFile.copy(buildDir, file.getName(), ext);

                if (targetHtml != null) {
                    String activePlatformName = evaluator.getProperty("platform.active"); //NOI18N
                    url = AppletSupport.getHTMLPageURL(targetHtml, activePlatformName);
                }
            } catch (IOException ioe) {
                ErrorManager.getDefault().notify(ioe);
                return null;
            }
            return url;
        }

        private URL generateAppletHTML(FileObject file) {
            try {
                String buildDirProp = evaluator.getProperty("build.dir"); //NOI18N
                String classesDirProp = evaluator.getProperty("build.classes.dir"); //NOI18N
                FileObject buildDir = buildDirProp != null ? updateHelper.getAntProjectHelper().resolveFileObject(buildDirProp) : null;
                FileObject classesDir = classesDirProp != null ? updateHelper.getAntProjectHelper().resolveFileObject(classesDirProp) : null;

                if (buildDir == null) {
                    buildDir = FileUtil.createFolder(project.getProjectDirectory(), buildDirProp);
                }

                if (classesDir == null) {
                    classesDir = FileUtil.createFolder(project.getProjectDirectory(), classesDirProp);
                }
                String activePlatformName = evaluator.getProperty("platform.active"); //NOI18N
                return AppletSupport.generateHtmlFileURL(file, buildDir, classesDir, activePlatformName);
            } catch (IOException ioe) {
                ErrorManager.getDefault().notify(ioe);
                return null;
            }
        }
    }
}
