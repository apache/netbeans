/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeListener;
import org.apache.tools.ant.module.api.support.ActionUtils;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.source.BuildArtifactMapper;
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
import org.openide.execution.ExecutorTask;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.Places;
import org.openide.util.BaseUtilities;
import org.openide.util.ChangeSupport;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.Parameters;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;
import org.openide.util.lookup.ServiceProvider;

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
            CompileOnSaveAction, PropertyChangeListener {
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
                        try {
                                ActionUtils.runTarget(
                                    buildXml,
                                    new String[] {target},
                                    null,
                                        null);
                        } catch (IOException ioe) {
                            LOG.log(
                                    Level.WARNING,
                                    "Cannot execute pos compile on save target: {0} in: {1}",   //NOI18N
                                    new Object[]{
                                        target,
                                        FileUtil.getFileDisplayName(buildXml)
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
                            RUNNER.execute(()-> {
                                try {
                                    final ExecutorTask task = ActionUtils.runTarget(
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
                            });
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
