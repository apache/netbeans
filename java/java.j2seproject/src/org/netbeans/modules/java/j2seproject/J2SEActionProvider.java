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

package org.netbeans.modules.java.j2seproject;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Queue;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import javax.swing.JButton;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.event.ChangeListener;
import org.apache.tools.ant.module.api.AntProjectCookie;
import org.apache.tools.ant.module.api.AntTargetExecutor;
import org.apache.tools.ant.module.api.support.AntScriptUtils;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.source.BuildArtifactMapper;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.java.api.common.SourceRoots;
import org.netbeans.modules.java.api.common.ant.UpdateHelper;
import org.netbeans.modules.java.api.common.project.ProjectProperties;
import org.netbeans.modules.java.api.common.project.BaseActionProvider;
import org.netbeans.modules.java.api.common.project.BaseActionProvider.Callback3;
import org.netbeans.modules.java.api.common.project.ProjectConfigurations;
import org.netbeans.modules.java.j2seproject.api.J2SEBuildPropertiesProvider;
import org.netbeans.modules.java.preprocessorbridge.spi.CompileOnSaveAction;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.LookupProvider;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.netbeans.spi.project.SingleMethod;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.LifecycleManager;
import org.openide.NotifyDescriptor;
import org.openide.execution.ExecutorTask;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.modules.Places;
import org.openide.util.BaseUtilities;
import org.openide.util.ChangeSupport;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Pair;
import org.openide.util.Parameters;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;
import org.openide.util.lookup.ServiceProvider;
import org.openide.xml.XMLUtil;

/** Action provider of the J2SE project. This is the place where to do
 * strange things to J2SE actions. E.g. compile-single.
 */
public class J2SEActionProvider extends BaseActionProvider {

    private static final Logger LOG = Logger.getLogger(J2SEActionProvider.class.getName());

    // Commands available from J2SE project
    private static final String[] supportedActions = {
        COMMAND_BUILD,
        COMMAND_CLEAN,
        COMMAND_REBUILD,
        COMMAND_COMPILE_SINGLE,
        COMMAND_RUN,
        COMMAND_RUN_SINGLE,
        COMMAND_DEBUG,
        COMMAND_DEBUG_SINGLE,
        COMMAND_PROFILE,
        COMMAND_PROFILE_SINGLE,
        JavaProjectConstants.COMMAND_JAVADOC,
        COMMAND_TEST,
        COMMAND_TEST_SINGLE,
        COMMAND_DEBUG_TEST_SINGLE,
        COMMAND_PROFILE_TEST_SINGLE,
        SingleMethod.COMMAND_RUN_SINGLE_METHOD,
        SingleMethod.COMMAND_DEBUG_SINGLE_METHOD,
        JavaProjectConstants.COMMAND_DEBUG_FIX,
        COMMAND_DEBUG_STEP_INTO,
        COMMAND_DELETE,
        COMMAND_COPY,
        COMMAND_MOVE,
        COMMAND_RENAME,
    };


    private static final String[] platformSensitiveActions = {
        COMMAND_BUILD,
        COMMAND_REBUILD,
        COMMAND_COMPILE_SINGLE,
        COMMAND_RUN,
        COMMAND_RUN_SINGLE,
        COMMAND_DEBUG,
        COMMAND_DEBUG_SINGLE,
        COMMAND_PROFILE,
        COMMAND_PROFILE_SINGLE,
        JavaProjectConstants.COMMAND_JAVADOC,
        COMMAND_TEST,
        COMMAND_TEST_SINGLE,
        COMMAND_DEBUG_TEST_SINGLE,
        COMMAND_PROFILE_TEST_SINGLE,
        SingleMethod.COMMAND_RUN_SINGLE_METHOD,
        SingleMethod.COMMAND_DEBUG_SINGLE_METHOD,
        JavaProjectConstants.COMMAND_DEBUG_FIX,
        COMMAND_DEBUG_STEP_INTO,
    };

    private static final String[] actionsDisabledForQuickRun = {
        COMMAND_COMPILE_SINGLE,
        JavaProjectConstants.COMMAND_DEBUG_FIX,
    };

    //Post compile on save actions
    private final CosAction cosAction;

    /** Map from commands to ant targets */
    private Map<String,String[]> commands;

    /**Set of commands which are affected by background scanning*/
    private Set<String> bkgScanSensitiveActions;

    /**Set of commands which need java model up to date*/
    private Set<String> needJavaModelActions;

    public J2SEActionProvider(J2SEProject project, UpdateHelper updateHelper) {
        super(
            project,
            updateHelper,
            project.evaluator(),
            project.getSourceRoots(),
            project.getTestSourceRoots(),
            project.getAntProjectHelper(),
            new CallbackImpl(project));
        commands = new HashMap<String,String[]>();
        // treated specially: COMMAND_{,RE}BUILD
        commands.put(COMMAND_CLEAN, new String[] {"clean"}); // NOI18N
        commands.put(COMMAND_COMPILE_SINGLE, new String[] {"compile-single"}); // NOI18N
        commands.put(COMMAND_RUN, new String[] {"run"}); // NOI18N
        commands.put(COMMAND_RUN_SINGLE, new String[] {"run-single"}); // NOI18N
        commands.put(COMMAND_DEBUG, new String[] {"debug"}); // NOI18N
        commands.put(COMMAND_DEBUG_SINGLE, new String[] {"debug-single"}); // NOI18N
        commands.put(COMMAND_PROFILE, new String[] {"profile"}); // NOI18N
        commands.put(COMMAND_PROFILE_SINGLE, new String[] {"profile-single"}); // NOI18N
        commands.put(JavaProjectConstants.COMMAND_JAVADOC, new String[] {"javadoc"}); // NOI18N
        commands.put(COMMAND_TEST, new String[] {"test"}); // NOI18N
        commands.put(COMMAND_TEST_SINGLE, new String[] {"test-single"}); // NOI18N
        commands.put(COMMAND_DEBUG_TEST_SINGLE, new String[] {"debug-test"}); // NOI18N
        commands.put(COMMAND_PROFILE_TEST_SINGLE, new String[]{"profile-test"}); // NOI18N
        commands.put(JavaProjectConstants.COMMAND_DEBUG_FIX, new String[] {"debug-fix"}); // NOI18N
        commands.put(COMMAND_DEBUG_STEP_INTO, new String[] {"debug-stepinto"}); // NOI18N
        commands.put(SingleMethod.COMMAND_RUN_SINGLE_METHOD, new String[] {"test-single-method"}); // NOI18N
        commands.put(SingleMethod.COMMAND_DEBUG_SINGLE_METHOD, new String[] {"debug-single-method"}); // NOI18N

        this.bkgScanSensitiveActions = new HashSet<String>(Arrays.asList(
            COMMAND_RUN,
            COMMAND_RUN_SINGLE,
            COMMAND_DEBUG,
            COMMAND_DEBUG_SINGLE,
            COMMAND_DEBUG_STEP_INTO,
            SingleMethod.COMMAND_RUN_SINGLE_METHOD,
            SingleMethod.COMMAND_DEBUG_SINGLE_METHOD
        ));

        this.needJavaModelActions = new HashSet<String>(Arrays.asList(
            JavaProjectConstants.COMMAND_DEBUG_FIX
        ));
        this.cosAction = new CosAction(
                this,
                project.evaluator(),
                project.getSourceRoots(),
                project.getTestSourceRoots());
    }

    @Override
    protected String[] getPlatformSensitiveActions() {
        return platformSensitiveActions;
    }

    @Override
    protected String[] getActionsDisabledForQuickRun() {
        return actionsDisabledForQuickRun;
    }

    @Override
    public Map<String, String[]> getCommands() {
        return commands;
    }

    @Override
    protected Set<String> getScanSensitiveActions() {
        return bkgScanSensitiveActions;
    }

    @Override
    protected Set<String> getJavaModelActions() {
        return needJavaModelActions;
    }

    @Override
    protected boolean isCompileOnSaveEnabled() {
        return isCompileOnSaveUpdate() && cosAction.getTarget() == null;
    }
    
    @Override
    protected boolean isCompileOnSaveUpdate() {
        return J2SEProjectUtil.isCompileOnSaveEnabled((J2SEProject)getProject());
    }

    @Override
    public String[] getSupportedActions() {
        return supportedActions;
    }

    @Override
    public String[] getTargetNames(String command, Lookup context, Properties p, boolean doJavaChecks) throws IllegalArgumentException {
        String names[] = super.getTargetNames(command, context, p, doJavaChecks);
        ProjectConfigurations.Configuration c = context.lookup(ProjectConfigurations.Configuration.class);
        if (c != null) {
            String config;
            if (!c.isDefault()) {
                config = c.getName();
            } else {
                // Invalid but overrides any valid setting in config.properties.
                config = "";
            }
            p.setProperty(ProjectProperties.PROP_PROJECT_CONFIGURATION_CONFIG, config);
        }
        return names;
    }

    @Override
    public void invokeAction(String command, Lookup context) throws IllegalArgumentException {
        final Runnable superCall = () -> super.invokeAction(command, context);
        if (isCompileOnSaveUpdate() && cosAction.getTarget() != null && getScanSensitiveActions().contains(command)) {
            LifecycleManager.getDefault ().saveAll ();  //Need to do saveAll eagerly
            final JButton stopButton = new JButton(NbBundle.getMessage(J2SEActionProvider.class, "TXT_StopBuild"));
            stopButton.setMnemonic(NbBundle.getMessage(J2SEActionProvider.class, "MNE_StopBuild").charAt(0));
            stopButton.getAccessibleContext().setAccessibleName(NbBundle.getMessage(J2SEActionProvider.class, "AN_StopBuild"));
            stopButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(J2SEActionProvider.class, "AD_StopBuild"));

            final AtomicBoolean showState = new AtomicBoolean(true);
            final AtomicBoolean stopState = new AtomicBoolean();

            try {
                final JavaSource js = createSource();
                js.runWhenScanFinished((cc) -> {
                    cosAction.newSyncTask(() -> SwingUtilities.invokeLater(() -> {
                        showState.set(false);
                        final boolean cancelled = stopState.get();
                        stopButton.doClick();
                        if (!cancelled) {
                            superCall.run();
                        }
                    }));
                }, false);
                final Timer timer = new Timer(1_000, (e) -> {
                    if (showState.get()) {
                        final NotifyDescriptor nd = new NotifyDescriptor(
                            NbBundle.getMessage(J2SEActionProvider.class, "TXT_CosUpdateActive"),
                            command,
                            NotifyDescriptor.YES_NO_OPTION,
                            NotifyDescriptor.INFORMATION_MESSAGE,
                            new Object[] {stopButton},
                            null);
                        final Object res = DialogDisplayer.getDefault().notify(nd);
                        if (res == DialogDescriptor.CLOSED_OPTION || res == stopButton) {
                            stopState.set(true);
                        }
                    }
                });
                timer.setRepeats(false);
                timer.start();
            } catch (IOException ioe) {
                Exceptions.printStackTrace(ioe);
                superCall.run();    //Last resort - try to run it
            }
        } else {
            superCall.run();
        }
    }

    @NonNull
    private static JavaSource createSource() {
        final ClasspathInfo cpInfo = ClasspathInfo.create(
                ClassPath.EMPTY,
                ClassPath.EMPTY,
                ClassPath.EMPTY);
        final JavaSource js = JavaSource.create(cpInfo);
        return js;
    }

    @ProjectServiceProvider(
            service=ActionProvider.class,
            projectTypes={@LookupProvider.Registration.ProjectType(id="org-netbeans-modules-java-j2seproject",position=100)})
    public static J2SEActionProvider create(@NonNull final Lookup lkp) {
        Parameters.notNull("lkp", lkp); //NOI18N
        final J2SEProject project = lkp.lookup(J2SEProject.class);
        final J2SEActionProvider j2seActionProvider = new J2SEActionProvider(project, project.getUpdateHelper());
        j2seActionProvider.startFSListener();
        return j2seActionProvider;
    }

    private static final class CallbackImpl implements Callback3 {

        private final J2SEProject prj;

        CallbackImpl(@NonNull final J2SEProject project) {
            Parameters.notNull("project", project); //NOI18N
            this.prj = project;
        }

        @Override
        @NonNull
        public Map<String, String> createAdditionalProperties(@NonNull String command, @NonNull Lookup context) {
            final Map<String,String> result = new HashMap<>();
            for (J2SEBuildPropertiesProvider bpp : prj.getLookup().lookupAll(J2SEBuildPropertiesProvider.class)) {
                final Map<String,String> contrib = bpp.createAdditionalProperties(command, context);
                assert contrib != null;
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.log(
                        Level.FINE,
                        "J2SEBuildPropertiesProvider: {0} added following build properties: {1}",   //NOI18N
                        new Object[]{
                            bpp.getClass(),
                            contrib
                        });
                }
                result.putAll(contrib);
            }
            return Collections.unmodifiableMap(result);
        }

        @Override
        public Set<String> createConcealedProperties(String command, Lookup context) {
            final Set<String> result = new HashSet<>();
            for (J2SEBuildPropertiesProvider bpp : prj.getLookup().lookupAll(J2SEBuildPropertiesProvider.class)) {
                final Set<String> contrib = bpp.createConcealedProperties(command, context);
                assert contrib != null;
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.log(
                        Level.FINE,
                        "J2SEBuildPropertiesProvider: {0} added following concealed properties: {1}",   //NOI18N
                        new Object[]{
                            bpp.getClass(),
                            contrib
                        });
                }
                result.addAll(contrib);
            }
            return Collections.unmodifiableSet(result);
        }

        @Override
        public void antTargetInvocationStarted(@NonNull String command, @NonNull Lookup context) {
        }

        @Override
        public void antTargetInvocationFinished(@NonNull String command, @NonNull Lookup context, int result) {
        }

        @Override
        public void antTargetInvocationFailed(@NonNull String command, @NonNull Lookup context) {
        }

        @CheckForNull
        @Override
        public ClassPath getProjectSourcesClassPath(@NonNull String type) {
            return prj.getClassPathProvider().getProjectSourcesClassPath(type);
        }

        @CheckForNull
        @Override
        public ClassPath findClassPath(@NonNull FileObject file, @NonNull String type) {
            return prj.getClassPathProvider().findClassPath(file, type);
        }

    }

    private static final class CosAction implements BuildArtifactMapper.ArtifactsUpdated,
            CompileOnSaveAction, PropertyChangeListener, FileChangeListener {
        private static Map<Project,Reference<CosAction>> instances = new WeakHashMap<>();
        private static final String COS_UPDATED = "$cos.update";    //NOI18N
        private static final String COS_CUSTOM = "$cos.update.resources";    //NOI18N
        private static final String PROP_TARGET = "cos.update.target.internal";  //NOI18N
        private static final String PROP_SCRIPT = "cos.update.script.internal";  //NOI18N
        private static final String PROP_SRCDIR = "cos.src.dir.internal";   //NOI18N
        private static final String PROP_INCLUDES ="cos.includes.internal"; //NOI18N
        private static final String SNIPPETS = "executor-snippets"; //NOI18N
        private static final String SCRIPT = "cos-update.xml"; //NOI18N
        private static final String TARGET = "cos-update-internal"; //NOI18N
        private static final String SCRIPT_TEMPLATE = "/org/netbeans/modules/java/j2seproject/resources/cos-update-snippet.xml"; //NOI18N
        private static final Object NONE = new Object();
        private static final RequestProcessor RUNNER = new RequestProcessor(CosAction.class);
        private final J2SEActionProvider owner;
        private final PropertyEvaluator eval;
        private final SourceRoots src;
        private final SourceRoots tests;
        private final BuildArtifactMapper mapper;
        private final Map</*@GuardedBy("this")*/URL,BuildArtifactMapper.ArtifactsUpdated> currentListeners;
        private final ChangeSupport cs;
        private final AtomicReference<Pair<URI,Collection<File>>> importantFilesCache;
        //@GuardedBy("this")
        private final Queue<Runnable> deferred = new ArrayDeque<>();
        //@GuardedBy("this")
        private byte deferredGuard; //0 - unset, 1 - pending, 2 - set
        private volatile Object targetCache;
        private volatile Object updatedFSProp;

        private CosAction(
                @NonNull final J2SEActionProvider owner,
                @NonNull final PropertyEvaluator eval,
                @NonNull final SourceRoots src,
                @NonNull final SourceRoots tests) {
            this.owner = owner;
            this.eval = eval;
            this.src = src;
            this.tests = tests;
            this.mapper = new BuildArtifactMapper();
            this.currentListeners = new HashMap<>();
            this.cs = new ChangeSupport(this);
            this.importantFilesCache = new AtomicReference<>(Pair.of(null,null));
            this.eval.addPropertyChangeListener(WeakListeners.propertyChange(this, this.eval));
            this.src.addPropertyChangeListener(WeakListeners.propertyChange(this, this.src));
            this.tests.addPropertyChangeListener(WeakListeners.propertyChange(this, this.tests));
            updateRootsListeners();
            instances.put(owner.getProject(), new WeakReference<>(this));
        }

        @Override
        public boolean isEnabled() {
            return getTarget() != null && isCustomUpdate();
        }

        @Override
        public boolean isUpdateClasses() {
            return isEnabled();
        }

        @Override
        public boolean isUpdateResources() {
            return isEnabled();
        }

        @Override
        public Boolean performAction(Context ctx) throws IOException {
            switch (ctx.getOperation()) {
                case UPDATE:
                    return performUpdate(ctx);
                case CLEAN:
                    return performClean(ctx);
                case SYNC:
                    return performSync(ctx);
                default:
                    throw new IllegalArgumentException(String.valueOf(ctx.getOperation()));                 
            }
        }               

        @Override
        public void artifactsUpdated(@NonNull final Iterable<File> artifacts) {
            if (!isCustomUpdate()) {
                final String target = getTarget();
                if (target != null) {
                    final FileObject buildXml = owner.findBuildXml();
                    if (buildXml != null) {
                        if (checkImportantFiles(buildXml)) {
                            RUNNER.execute(() -> {
                                try {
                                    final ExecutorTask task = runTargetInDedicatedTab(
                                            NbBundle.getMessage(J2SEActionProvider.class, "LBL_CompileOnSaveUpdate"),
                                            buildXml,
                                            new String[] {target},
                                            null,
                                            null);
                                    task.result();
                                } catch (IOException | IllegalArgumentException ex) {
                                    LOG.log(
                                            Level.WARNING,
                                            "Cannot execute pos compile on save target: {0} in: {1} due to: {2}",   //NOI18N
                                            new Object[]{
                                                target,
                                                FileUtil.getFileDisplayName(buildXml),
                                                ex.getMessage()
                                            });
                                }
                            });
                        }
                    }
                }
            }
        }

        @Override
        public void propertyChange(@NonNull final PropertyChangeEvent evt) {
            final String name = evt.getPropertyName();
            if (name == null) {
                targetCache = null;
                updatedFSProp = null;
                cs.fireChange();
            } else if (COS_UPDATED.equals(name)) {
                targetCache = null;
                cs.fireChange();
            } else if (COS_CUSTOM.equals(name)) {
                updatedFSProp = null;
                cs.fireChange();
            }else if (SourceRoots.PROP_ROOTS.equals(name)) {
                updateRootsListeners();
            }
        }

        @Override
        public void addChangeListener(@NonNull final ChangeListener listener) {
            cs.addChangeListener(listener);
        }

        @Override
        public void removeChangeListener(@NonNull final ChangeListener listener) {
            cs.removeChangeListener(listener);
        }

        @Override
        public void fileDeleted(FileEvent fe) {
            resetImportantFilesCache();
        }

        @Override
        public void fileChanged(FileEvent fe) {
            resetImportantFilesCache();
        }

        @Override
        public void fileFolderCreated(FileEvent fe) {
            resetImportantFilesCache();
        }

        @Override
        public void fileDataCreated(FileEvent fe) {
            resetImportantFilesCache();
        }

        @Override
        public void fileRenamed(FileRenameEvent fe) {
            resetImportantFilesCache();
        }

        @Override
        public void fileAttributeChanged(FileAttributeEvent fe) {
            //Not important
        }

        @NonNull
        Future<?> newSyncTask(@NonNull final Runnable callback) {
            return RUNNER.submit(() -> {
                    drainDeferred();
                    callback.run();
                },
                null);
        }

        private void updateRootsListeners() {
            final Set<URL> newRoots = new HashSet<>();
            Collections.addAll(newRoots, this.src.getRootURLs());
            Collections.addAll(newRoots, this.tests.getRootURLs());
            synchronized (this) {
                final Set<URL> toRemove = new HashSet<>(currentListeners.keySet());
                toRemove.removeAll(newRoots);
                newRoots.removeAll(currentListeners.keySet());
                for (URL u : toRemove) {
                    final BuildArtifactMapper.ArtifactsUpdated l = currentListeners.remove(u);
                    mapper.removeArtifactsUpdatedListener(u, l);
                }
                for (URL u : newRoots) {
                    final BuildArtifactMapper.ArtifactsUpdated l = new WeakArtifactUpdated(this, mapper, u);
                    currentListeners.put(u, l);
                    mapper.addArtifactsUpdatedListener(u, l);
                }
            }
        }

        @CheckForNull
        private String getTarget() {
            Object target = targetCache;
            if (target == null) {
                final String val = eval.getProperty(COS_UPDATED);
                target = targetCache = val != null && !val.isEmpty() ?
                        val :
                        NONE;
            }
            if (target == NONE) {
                return null;
            }
            return owner.isCompileOnSaveUpdate()?
                    (String) target :
                    null;
        }
        
        @CheckForNull
        private String getUpdatedFileSetProperty() {
            Object res = updatedFSProp;
            if (res == null) {                
                final String val = eval.getProperty(COS_CUSTOM);
                res = updatedFSProp = val != null && !val.isEmpty() ?
                        val :
                        NONE;
            }
            if (res == NONE) {
                res = null;
            }
            return (String) res;
        }
        
        private boolean isCustomUpdate() {
            return getUpdatedFileSetProperty() != null;
        }
        
        
        @CheckForNull
        private Boolean performUpdate(@NonNull final Context ctx) {
            final String target = getTarget();
            if (target != null) {
                final FileObject buildXml = owner.findBuildXml();
                if (buildXml != null) {
                    if (checkImportantFiles(buildXml)) {
                        try {
                            final FileObject cosScript = getCosScript();
                            final Iterable<? extends File> updated = ctx.getUpdated();
                            final Iterable<? extends File> deleted = ctx.getDeleted();
                            final File root = ctx.isCopyResources() ?
                                    BaseUtilities.toFile(ctx.getSourceRoot().toURI()) :
                                    ctx.getCacheRoot();
                            final String includes = createIncludes(root, updated);
                            if (includes != null) {
                                final Properties props = new Properties();
                                props.setProperty(PROP_TARGET, target);
                                props.setProperty(PROP_SCRIPT, FileUtil.toFile(buildXml).getAbsolutePath());
                                props.setProperty(PROP_SRCDIR, root.getAbsolutePath());
                                props.setProperty(PROP_INCLUDES, includes);
                                props.setProperty(COS_CUSTOM, getUpdatedFileSetProperty());
                                final Runnable work = () -> {
                                    try {
                                        final ExecutorTask task = runTargetInDedicatedTab(
                                                NbBundle.getMessage(J2SEActionProvider.class, "LBL_CompileOnSaveUpdate"),
                                                cosScript,
                                                new String[] {TARGET},
                                                props,
                                                null);
                                        task.result();
                                    } catch (IOException | IllegalArgumentException ex) {
                                        LOG.log(
                                            Level.WARNING,
                                            "Cannot execute update targer: {0} in: {1} due to: {2}",   //NOI18N
                                            new Object[]{
                                                target,
                                                FileUtil.getFileDisplayName(buildXml),
                                                ex.getMessage()
                                            });
                                    }
                                };
                                if (ctx.isAllFilesIndexing()) {
                                    enqueueDeferred(work);
                                } else {
                                    RUNNER.execute(work);
                                }
                            } else {
                                LOG.warning("BuildArtifactMapper artifacts do not provide attributes.");    //NOI18N
                            }
                        } catch (IOException | URISyntaxException e) {
                            LOG.log(
                                    Level.WARNING,
                                    "Cannot execute update targer: {0} in: {1} due to: {2}",   //NOI18N
                                    new Object[]{
                                        target,
                                        FileUtil.getFileDisplayName(buildXml),
                                        e.getMessage()
                                    });
                        }
                    }
                }
            }
            return true;
        }
        
        @CheckForNull
        private Boolean performClean(@NonNull final Context ctx) {
            //Not sure what to do
            return null;
        }
        
        @CheckForNull
        private Boolean performSync(@NonNull final Context ctx) {
            //Not sure what to do
            return null;
        }
        
        @NonNull
        private FileObject getCosScript() throws IOException {
            final FileObject snippets = FileUtil.createFolder(
                    Places.getCacheSubdirectory(SNIPPETS));
            FileObject cosScript = snippets.getFileObject(SCRIPT);
            if (cosScript == null) {
                cosScript = FileUtil.createData(snippets, SCRIPT);
                final FileLock lock = cosScript.lock();
                try (InputStream in = getClass().getResourceAsStream(SCRIPT_TEMPLATE);
                        OutputStream out = cosScript.getOutputStream(lock)) {
                    FileUtil.copy(in, out);
                } finally {
                    lock.releaseLock();
                }
            }
            return cosScript;
        }

        private boolean checkImportantFiles(@NonNull final FileObject buildScript) {
            final URI currentURI = buildScript.toURI();
            final Pair<URI,Collection<File>> cacheLine = importantFilesCache.get();
            final URI lastURI = cacheLine.first();
            Collection<File> importantFiles = cacheLine.second();
            if (!currentURI.equals(lastURI) || importantFiles == null) {
                Optional.ofNullable(lastURI)
                        .map(BaseUtilities::toFile)
                        .filter((f) -> !currentURI.equals(lastURI))
                        .ifPresent(this::safeRemoveFileChangeListener);
                Optional.ofNullable(FileUtil.toFile(buildScript))
                        .filter((f) -> !currentURI.equals(lastURI))
                        .ifPresent(this::safeAddFileChangeListener);
                importantFiles = new ArrayList<>();
                try {
                    final File base = FileUtil.toFile(buildScript.getParent());
                    if (base != null) {
                        Optional.ofNullable(DataObject.find(buildScript).getLookup().lookup(AntProjectCookie.class))
                                .map(AntProjectCookie::getProjectElement)
                                .map(XMLUtil::findSubElements)
                                .map(Collection::stream)
                                .orElse(Stream.empty())
                                .filter((e) -> "import".equals(e.getNodeName()))    //NOI18N
                                .map((e) -> e.getAttribute("file"))                 //NOI18N
                                .filter((p) -> !p.isEmpty())
                                .map((p) -> PropertyUtils.resolveFile(base, p))
                                .forEach(importantFiles::add);
                    }
                } catch (DataObjectNotFoundException e) {
                    LOG.log(
                            Level.WARNING,
                            "No DataObject for: {0}, reason: {1}",  //NOI18N
                            new Object[]{
                                FileUtil.getFileDisplayName(buildScript),
                                e.getMessage()
                            });
                }
                importantFilesCache.set(Pair.of(currentURI, importantFiles));
            }
            for (File importantFile : importantFiles) {
                if (!importantFile.isFile()) {
                    return false;
                }
            }
            return true;
        }

        private void resetImportantFilesCache() {
            while (true) {
                final Pair<URI, Collection<File>> expected = importantFilesCache.get();
                final Pair<URI, Collection<File>> update = Pair.of(expected.first(), null);
                if (importantFilesCache.compareAndSet(expected, update)) {
                    break;
                }
            }
        }

        private void safeAddFileChangeListener(@NonNull final File f) {
            try {
                FileUtil.addFileChangeListener(this, f);
            } catch (IllegalArgumentException e) {
                //not important
            }
        }

        private void safeRemoveFileChangeListener(@NonNull final File f) {
            try {
                FileUtil.removeFileChangeListener(this, f);
            } catch (IllegalArgumentException e) {
                //not important
            }
        }

        @NonNull
        private static ExecutorTask runTargetInDedicatedTab(
                @NullAllowed final String tabName,
                @NonNull final FileObject buildXml,
                @NullAllowed final String[] targetNames,
                @NullAllowed final Properties properties,
                @NullAllowed final Set<String> concealedProperties) throws IOException, IllegalArgumentException {
            Parameters.notNull("buildXml", buildXml);   //NOI18N
            if (targetNames != null && targetNames.length == 0) {
                throw new IllegalArgumentException("No targets supplied"); // NOI18N
            }
            final AntProjectCookie apc = AntScriptUtils.antProjectCookieFor(buildXml);
            final AntTargetExecutor.Env execenv = new AntTargetExecutor.Env();
            if (properties != null) {
                Properties p = execenv.getProperties();
                p.putAll(properties);
                execenv.setProperties(p);
            }
            if (concealedProperties != null) {
                execenv.setConcealedProperties(concealedProperties);
            }
            execenv.setSaveAllDocuments(false);
            execenv.setPreferredName(tabName);
            final Predicate<String> p = (s) -> tabName == null ?
                    true :
                    tabName.equals(s);
            execenv.setTabReplaceStrategy(p, p);
            execenv.setUserAction(false);
            return AntTargetExecutor.createTargetExecutor(execenv)
                    .execute(apc, targetNames);
        }

        @CheckForNull
        private static String createIncludes(
                @NonNull final File root,
                @NonNull final Iterable<? extends File> artifacts) {
            final StringBuilder include = new StringBuilder();
            for (File f : artifacts) {
                if (include.length() > 0) {
                    include.append(','); //NOI18N
                }
                include.append(relativize(f,root));
            }
            return include.length() == 0 ?
                    null :
                    include.toString();
        }
        
        private static String relativize(
                @NonNull final File file,
                @NonNull final File folder) {
            final String folderPath = folder.getAbsolutePath();
            int start = folderPath.length();
            if (!folderPath.endsWith(File.separator)) {
                start++;
            }
            return file.getAbsolutePath().substring(start);
        }

        private void enqueueDeferred(final Runnable work) {
            boolean addGuard = false;
            synchronized (this) {
                this.deferred.offer(work);
                if (deferredGuard == 0) {
                     addGuard = true;
                    deferredGuard = 1;
                }
            }
            if (addGuard) {
                final JavaSource js = createSource();
                synchronized (this) {
                    if (deferredGuard == 1) {
                        deferredGuard = 2;
                        try {
                            js.runWhenScanFinished((cc) -> drainDeferred(), true);
                        } catch (IOException ioe) {
                            Exceptions.printStackTrace(ioe);
                        }
                    }
                }
            }
        }

        private void drainDeferred() {
            Runnable[] todo;
            synchronized (this) {
                todo = deferred.toArray(new Runnable[0]);
                deferred.clear();
                deferredGuard = 0;
            }
            for (Runnable r : todo) {
                r.run();
            }
        }

        @CheckForNull
        static CosAction getInstance(@NonNull final Project p) {
            final Reference<CosAction> r = instances.get(p);
            return r != null ?
                    r.get() :
                    null;
        }

        private static final class WeakArtifactUpdated extends WeakReference<BuildArtifactMapper.ArtifactsUpdated>
                implements BuildArtifactMapper.ArtifactsUpdated, Runnable {

            private final BuildArtifactMapper source;
            private final URL url;

            WeakArtifactUpdated(
                    @NonNull final BuildArtifactMapper.ArtifactsUpdated delegate,
                    @NonNull final BuildArtifactMapper source,
                    @NonNull final URL url) {
                super(delegate);
                Parameters.notNull("source", source);   //NOI18N
                Parameters.notNull("url", url); //NOI18N
                this.source = source;
                this.url = url;
            }

            @Override
            public void artifactsUpdated(
                    @NonNull final Iterable<File> artifacts) {
                final BuildArtifactMapper.ArtifactsUpdated delegate = get();
                if (delegate != null) {
                    delegate.artifactsUpdated(artifacts);
                }
            }

            @Override
            public void run() {
                source.removeArtifactsUpdatedListener(url, this);
            }
        }
    }
    
    @ServiceProvider(service = CompileOnSaveAction.Provider.class, position = 10_000)
    public static final class Provider implements CompileOnSaveAction.Provider {

        @Override
        public CompileOnSaveAction forRoot(URL root) {
            try {
                final Project p = FileOwnerQuery.getOwner(root.toURI());
                if (p != null) {
                    ActionProvider prov = p.getLookup().lookup(ActionProvider.class);  
                    if (prov != null) {
                        prov.getSupportedActions(); //Force initialization
                    }
                    final CosAction action = CosAction.getInstance(p);
                    return action;
                }
            } catch (URISyntaxException e) {
                Exceptions.printStackTrace(e);
            }
            return null;
        }
        
    }
}
