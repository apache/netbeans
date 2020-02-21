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
package org.netbeans.modules.cnd.toolchain.compilerset;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.modules.cnd.api.remote.ServerList;
import org.netbeans.modules.cnd.api.remote.ServerRecord;
import org.netbeans.modules.cnd.api.toolchain.CompilerFlavor;
import org.netbeans.modules.cnd.api.toolchain.CompilerSet;
import org.netbeans.modules.cnd.api.toolchain.CompilerSetManager;
import org.netbeans.modules.cnd.api.toolchain.PlatformTypes;
import org.netbeans.modules.cnd.api.toolchain.PredefinedToolKind;
import org.netbeans.modules.cnd.api.toolchain.Tool;
import org.netbeans.modules.cnd.api.toolchain.ToolKind;
import org.netbeans.modules.cnd.api.toolchain.ToolchainManager.AlternativePath;
import org.netbeans.modules.cnd.api.toolchain.ToolchainManager.CompilerDescriptor;
import org.netbeans.modules.cnd.api.toolchain.ToolchainManager.ToolDescriptor;
import org.netbeans.modules.cnd.api.toolchain.ToolchainManager.ToolchainDescriptor;
import org.netbeans.modules.cnd.spi.toolchain.CompilerSetFactory;
import org.netbeans.modules.cnd.toolchain.support.ToolchainUtilities;
import org.netbeans.modules.cnd.spi.toolchain.CompilerSetManagerEvents;
import org.netbeans.modules.cnd.spi.toolchain.CompilerSetProvider;
import org.netbeans.modules.cnd.spi.toolchain.ToolChainPathProvider;
import org.netbeans.modules.cnd.utils.CndPathUtilities;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.cnd.utils.NamedRunnable;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.HostInfo;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.netbeans.modules.nativeexecution.api.util.Path;
import org.netbeans.modules.nativeexecution.api.util.WindowsSupport;
import org.openide.util.Cancellable;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;

/**
 * Manage a set of CompilerSets. The CompilerSets are dynamically created based on which compilers
 * are found in the user's $PATH variable.
 */
public final class CompilerSetManagerImpl extends CompilerSetManager {
    static final boolean DISABLED = Boolean.getBoolean("cnd.toolchain.disabled"); // NOI18N
    static final boolean PREFER_STUDIO = "true".equals(System.getProperty("cnd.toolchain.prefer.studio", "true")); // NOI18N;

    private static final Logger log = Logger.getLogger("cnd.remote.logger"); // NOI18N
    private static final RequestProcessor RP = new RequestProcessor(CompilerSetManagerImpl.class.getName(), 1);

    //private static final HashMap<ExecutionEnvironment, CompilerSetManagerImpl> managers = new HashMap<ExecutionEnvironment, CompilerSetManagerImpl>();
    //private static final Object MASTER_LOCK = new Object();

    // CopyOnWriteArrayList because of IZ#175647
    private final CopyOnWriteArrayList<CompilerSet> sets;

    private final ExecutionEnvironment executionEnvironment;
    private volatile State state;
    private int platform = -1;
    private Task initializationTask;
    private CompilerSetProvider provider;
    private boolean canceled;
    
    private final RequestProcessor requestProcessor;

    public CompilerSetManagerImpl(ExecutionEnvironment env) {
        this(env, true);
    }

    public CompilerSetManagerImpl(ExecutionEnvironment env, final boolean initialize) {
        //if (log.isLoggable(Level.FINEST)) {
        //    log.log(Level.FINEST, "CompilerSetManager CTOR A @" + System.identityHashCode(this) + ' ' + env + ' ' + initialize, new Exception()); //NOI18N
        //}
        sets = new CopyOnWriteArrayList<CompilerSet>();
        executionEnvironment = env;
        requestProcessor = new RequestProcessor("Compiler set manager " + env, 40); //NOI18N
        if (initialize && !DISABLED) {
            state = State.STATE_PENDING;
        } else {
            state = State.STATE_UNINITIALIZED;
            return;
        }
        if (executionEnvironment.isLocal()) {
            platform = ToolUtils.computeLocalPlatform();
            initCompilerSets(Path.getPath());
        } else {
            final AtomicReference<Thread> threadRef = new AtomicReference<Thread>();
            final String progressMessage = NbBundle.getMessage(getClass(), "PROGRESS_TEXT", env.getDisplayName());
            //use non-UI API for progress - in UI case UI  services will be turned on
            final ProgressHandle progressHandle = ProgressHandle.createHandle(
                    progressMessage,
                    new Cancellable() {
                @Override
                        public boolean cancel() {
                            Thread thread = threadRef.get();
                            if (thread != null) {
                                thread.interrupt();
                            }
                            return true;
                        }

            });
            log.log(Level.FINE, "CSM.init: initializing remote compiler set @{0} for: {1}", new Object[]{System.identityHashCode(CompilerSetManagerImpl.this), toString()});
            progressHandle.start();
            RP.post(new NamedRunnable(progressMessage) {
                protected @Override void runImpl() {
                    threadRef.set(Thread.currentThread());
                    try {
                        initRemoteCompilerSets(false, initialize);
                    } finally {
                        progressHandle.finish();
                    }
                }
            });
        }
    }

    CompilerSetManagerImpl(ExecutionEnvironment env, List<CompilerSet> sets, int platform) {
        //if (log.isLoggable(Level.FINEST)) {
        //    log.log(Level.FINEST, "CompilerSetManager CTOR B @" + System.identityHashCode(this) + ' '  + sets + ' ' + platform, new Exception()); //NOI18N
        //}
        this.executionEnvironment = env;
        requestProcessor = new RequestProcessor("Compiler set manager " + env, 4); //NOI18N
        this.sets =  new CopyOnWriteArrayList<CompilerSet>(sets);
        this.platform = platform;
        completeCompilerSets();
        if (DISABLED) {
            this.state = State.STATE_UNINITIALIZED;
            log.log(Level.FINE, "CSM DISABLED", toString());
        } else if(env.isRemote() && isEmpty()) {
            this.state = State.STATE_UNINITIALIZED;
            log.log(Level.FINE, "CSM restoring from pref: Adding empty CS to host {0}", toString());
        } else {
            this.state = State.STATE_COMPLETE;
        }
    }

    public boolean isValid() {
        return sets.size() > 0 && !sets.get(0).getName().equals(CompilerSetImpl.None);
    }

    @Override
    public boolean isPending() {
        return state == State.STATE_PENDING;
    }

    @Override
    public boolean isUninitialized() {
        return state == State.STATE_UNINITIALIZED;
    }

    public boolean isComplete() {
        return state == State.STATE_COMPLETE;
    }

    /** CAUTION: this is a slow method. It should NOT be called from the EDT thread */
    @Override
    public synchronized void initialize(boolean save, boolean runCompilerSetDataLoader, Writer reporter) {
        canceled = false;
        CompilerSetReporter.setWriter(reporter);
        ProgressHandle pHandle = null;
        try {
            CndUtils.assertNonUiThread();
            if (isUninitialized() && !DISABLED) {
                log.log(Level.FINE, "CSM.getDefault: Doing remote setup from EDT?{0}", SwingUtilities.isEventDispatchThread());
                pHandle = ProgressHandle.createHandle(NbBundle.getMessage(getClass(), "PROGRESS_TEXT", getExecutionEnvironment().getDisplayName())); // NOI18N
                pHandle.start();
                this.sets.clear();
                initRemoteCompilerSets(true, runCompilerSetDataLoader);
                if (initializationTask != null) {
                    initializationTask.waitFinished();
                    initializationTask = null;
                }
            }
            if (save && !DISABLED) {
                CompilerSetManagerAccessorImpl.save(this);
            }
        } finally {
            CompilerSetReporter.setWriter(null);
            if (pHandle != null) {
                pHandle.finish();
            }
            canceled = false;
        }
    }

    @Override
    public boolean cancel() {
        this.canceled = true;
        CompilerSetProvider aProvider = provider;
        if (aProvider != null) {
            return aProvider.cancel();
        }
        return false;
    }

    @Override
    public int getPlatform() {
        if (platform < 0) {
            if (executionEnvironment.isLocal()) {
                platform = ToolUtils.computeLocalPlatform();
            } else {
                if (isPending()) {
                    log.log(Level.WARNING, "calling getPlatform() on uninitialized {0}", getClass().getSimpleName());
                }
                HostInfo hostInfo = null;
                if (HostInfoUtils.isHostInfoAvailable(executionEnvironment)) {
                    try {
                        hostInfo = HostInfoUtils.getHostInfo(executionEnvironment);
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex); // should never occur since isHostInfoAvailable is checked
                    } catch (ConnectionManager.CancellationException ex) {
                        Exceptions.printStackTrace(ex); // should never occur since isHostInfoAvailable is checked
                    }
                }
                if (hostInfo != null) {
                    return PlatformTypes.getPlatformFromHostInfo(hostInfo);
                }
            }
        }
        return platform == -1 ? PlatformTypes.PLATFORM_NONE : platform;
    }

    public CompilerSetManagerImpl deepCopy() {
        if (isPending()) {
            log.log(Level.WARNING, "calling deepCopy() on uninitialized {0}", getClass().getSimpleName());
        }
        List<CompilerSet> setsCopy = new ArrayList<CompilerSet>();
        CompilerSet copyOfDefaultCompilerSet = null;
        for (CompilerSet set : getCompilerSets()) {
            final CompilerSetImpl copy = ((CompilerSetImpl) set).createCopy(executionEnvironment);
            setsCopy.add(copy);
            if (isDefaultCompilerSet(set)) {
                copyOfDefaultCompilerSet = copy;
            }
        }
        CompilerSetManagerImpl copy = new CompilerSetManagerImpl(executionEnvironment, setsCopy, this.platform);
        copy.setDefault(copyOfDefaultCompilerSet);
        return copy;
    }

    public String getUniqueCompilerSetName(String baseName) {
        int n = 0;
        String suggestedName = baseName;
        while (true) {
            suggestedName = baseName + (n > 0 ? ("_" + n) : ""); // NOI18N
            if (getCompilerSet(suggestedName) != null) {
                n++;
            } else {
                break;
            }
        }
        return suggestedName;
    }

    private Collection<FolderDescriptor> getPaths(ToolchainDescriptor d, CompilerFlavor flavor, ArrayList<String> dirlist) {
        LinkedHashSet<FolderDescriptor> dirs = new LinkedHashSet<FolderDescriptor>();
        // path from regestry
        String base = ToolUtils.getBaseFolder(d, getPlatform());
        if (base != null) {
            dirs.add(new FolderDescriptor(base, true));
        }
        // path from env
        for (String p : dirlist) {
            dirs.add(new FolderDescriptor(p, false));
        }
        // path from default location
        Map<String, List<String>> map = d.getDefaultLocations();
        if (map != null) {
            List<String> list = map.get(ToolUtils.getPlatformName(getPlatform()));
            if (list != null) {
                for (String p : list) {
                    dirs.add(new FolderDescriptor(p, true));
                }
            }
        }
        // path from plugins
        String path = ToolChainPathProvider.getDefault().getPath(flavor);
        if (path != null) {
            dirs.add(new FolderDescriptor(path, true));
        }
        return dirs;
    }

    /** Search $PATH for all desired compiler sets and initialize cbCompilerSet and spCompilerSets */
    private synchronized void initCompilerSets(final ArrayList<String> dirlist) {
        // NB: function itself is synchronized!
        if (state == State.STATE_COMPLETE) {
            return;
        }
        if (initializationTask != null) {
            return;
        }
        String progressMessage = NbBundle.getMessage(getClass(), "PROGRESS_TEXT", executionEnvironment.getDisplayName()); // NOI18N

        Runnable runnable = new Runnable() {

            @Override
            public void run() {
                initCompilerSetsImpl(dirlist);
            }
        };

        if (CndUtils.isStandalone() || CndUtils.isUnitTestMode()) { // this means we run in tests or standalone application
            runnable.run();
        } else {
            ProgressHandle progressHandle = ProgressHandle.createHandle(progressMessage);
            progressHandle.start();
            initializationTask = RP.post(runnable);
            initializationTask.waitFinished();
            initializationTask = null;
            progressHandle.finish();
        }
    }

    /** Search $PATH for all desired compiler sets and initialize cbCompilerSet and spCompilerSets */
    private void initCompilerSetsImpl(ArrayList<String> dirlist) {
        Set<CompilerFlavor> flavors = new HashSet<CompilerFlavor>();
        String SunStudioPath = System.getProperty("spro.bin");        // NB: function itself is synchronized!
        final boolean OSS_TOOLCHAIN_ONLY = "true".equals(System.getProperty("oss.toolchain.only")); //NOI18N
        final boolean DEBUGGER_ONLY = "on".equals(System.getProperty("PL_MODE")); //NOI18N
        
        if (SunStudioPath != null) {
            File folder = new File(SunStudioPath);
            if (folder.isDirectory()) {
                for(ToolchainDescriptor d : ToolchainManagerImpl.getImpl().getToolchains(getPlatform())) {
                    if (d.isAbstract() || !d.isAutoDetected()) {
                        continue;
                    }
                    CompilerFlavor flavor = CompilerFlavorImpl.toFlavor(d.getName(), getPlatform());
                    if (flavor == null) {
                        continue;
                    }
                    if (flavors.contains(flavor)) {
                        continue;
                    }
                    CompilerSetImpl cs = CompilerSetImpl.create(flavor, executionEnvironment, folder.getAbsolutePath());
                    cs.setAutoGenerated(true);
                    if (initCompilerSet(SunStudioPath, cs, true)){
                        flavors.add(flavor);
                        addUnsafe(cs);
                        if (cs.getCompilerFlavor().getToolchainDescriptor().getAliases().length > 0) {
                            cs.setSunStudioDefault(true);
                        }
                    } else if (DEBUGGER_ONLY){
                        if (initCompilerSet(SunStudioPath, cs, true, PredefinedToolKind.DebuggerTool)) {
                            flavors.add(flavor);
                            addUnsafe(cs);
                            if (cs.getCompilerFlavor().getToolchainDescriptor().getAliases().length > 0) {
                                cs.setSunStudioDefault(true);
                            }
                            // This is special tool collection that consists from debugger only.
                            // Debugger does not support version pattern. As result debbuder will fit for any studio flavor.
                            // Temporary solution is detect first descriptor (newest).
                            // TODO: add supporting debugger version pattern for all studio tool collections and remove next break;
                            break;
                        }
                    }
                }
            }
        }
        if (!OSS_TOOLCHAIN_ONLY) {
            Loop:for(ToolchainDescriptor d : ToolchainManagerImpl.getImpl().getToolchains(getPlatform())) {
                if (d.isAbstract() || !d.isAutoDetected()) {
                    continue;
                }
                CompilerFlavor flavor = CompilerFlavorImpl.toFlavor(d.getName(), getPlatform());
                if (flavor == null) {
                    continue;
                }
                if (flavors.contains(flavor)) {
                    continue;
                }
                for (FolderDescriptor folderDescriptor : getPaths(d, flavor, dirlist)) {
                    String path = folderDescriptor.path;
                    if (path.equals("/usr/ucb")) { // NOI18N
                        // Don't look here.
                        continue;
                    }
                    if (!CndPathUtilities.isAbsolute(path)) {
                        path = CndFileUtils.normalizeAbsolutePath(new File(path).getAbsolutePath());
                    }
                    File dir = new File(path);
                    if (dir.isDirectory()) {
                        if (ToolUtils.isMyFolder(dir.getAbsolutePath(), d, getPlatform(), folderDescriptor.knownFolder, PredefinedToolKind.CCompiler)){
                            if (d.getModuleID() == null && !d.isAbstract()) {
                                CompilerSetImpl cs = CompilerSetImpl.create(flavor, executionEnvironment, dir.getAbsolutePath());
                                cs.setAutoGenerated(true);                        
                                if (initCompilerSet(path, cs, folderDescriptor.knownFolder)){
                                    flavors.add(flavor);
                                    addUnsafe(cs);
                                    continue Loop;
                                }
                            }
                        }
                    }
                }
            }
        }
        removeSubstitutions();
        addFakeCompilerSets();
        completeCompilerSets();
        state = State.STATE_COMPLETE;
    }

    /**
     * Since many toolchains have default locations, append them to the path (on a per-platform basis)
     * if they aren't already in the list.
     *
     * @param platform The platform we're running on
     * @param dirlist An ArrayList of the current PATH
     * @return A possibly modified ArrayList
     */
    public static ArrayList<String> appendDefaultLocations(int platform, ArrayList<String> dirlist) {
        for (ToolchainDescriptor d : ToolchainManagerImpl.getImpl().getToolchains(platform)) {
            if (d.isAbstract() || !d.isAutoDetected()) {
                continue;
            }
            Map<String, List<String>> map = d.getDefaultLocations();
            if (map != null) {
                String pname = ToolUtils.getPlatformName(platform);
                List<String> list = map.get(pname);
                if (list != null ) {
                    for (String dir : list){
                        if (!dirlist.contains(dir)){
                            dirlist.add(dir);
                        }
                    }
                }
            }
        }
        return dirlist;
    }

    private void setDefaltCompilerSet() {
        for (CompilerSet cs : sets) {
            if (((CompilerSetImpl)cs).isDefault()) {
                return;
            }
        }
        CompilerSet bestCandidate = null;
        String defaultToolchain = System.getProperty("cnd.default.toolchain"); //NOI18N
        if (defaultToolchain != null) {
            for (CompilerSet cs : sets) {
                if (cs.getName().equalsIgnoreCase(defaultToolchain)) {
                    bestCandidate = cs;
                }
            }
        }        
        if (PREFER_STUDIO) {
            for (CompilerSet cs : sets) {
                if (cs.getCompilerFlavor().isSunStudioCompiler()) {
                    if ("OracleDeveloperStudio".equals(cs.getName())) { // NOI18N
                        setDefault(cs);
                        return;
                    }
                    if (bestCandidate == null) {
                        bestCandidate = cs;
                    }
                }
            }
        }
        if (bestCandidate != null) {
            setDefault(bestCandidate);
            return;
        }
        if (!sets.isEmpty()) {
            setDefault(sets.get(0));
        }
    }

        public List<CompilerSet> findRemoteCompilerSets(String path) {
        ServerRecord record = ServerList.get(executionEnvironment);
        assert record != null;
	record.validate(true);
	if (!record.isOnline()) {
            return Collections.<CompilerSet>emptyList();
        }
        String[] arData;
        try {
            provider = CompilerSetProviderFactoryImpl.createNew(executionEnvironment);
            arData = provider.getCompilerSetData(path);
        } finally {
            provider = null;
        }
        List<CompilerSet> css = new ArrayList<CompilerSet>();
        if (arData != null) {
            for (String data : arData) {
                if (data != null && data.length() > 0) {
                    if (platform < 0) {
                        HostInfo hostInfo = null;
                        if (HostInfoUtils.isHostInfoAvailable(executionEnvironment)) {
                            try {
                                hostInfo = HostInfoUtils.getHostInfo(executionEnvironment);
                            } catch (IOException ex) {
                                Exceptions.printStackTrace(ex); // should never occur since isHostInfoAvailable is checked
                            } catch (ConnectionManager.CancellationException ex) {
                                Exceptions.printStackTrace(ex); // should never occur since isHostInfoAvailable is checked
                            }
                        }
                        if (hostInfo != null) {
                            platform = PlatformTypes.getPlatformFromHostInfo(hostInfo);
                        }
                    }
                    CompilerSetImpl cs = parseCompilerSetString(platform, data);
                    if (cs != null) {
                        cs.setAutoGenerated(false);
                        css.add(cs);
                    } else {
                        log.log(Level.WARNING, "Not recognized CompilerSetString: {0}", data); // NOI18N
                    }
                }
            }
        }
        List<CompilerSet> all = new ArrayList<CompilerSet>(getCompilerSets());
        all.addAll(css);
        for(CompilerSet cs : css) {
            completeCompilerSet(executionEnvironment, (CompilerSetImpl)cs, all);
        }
        return css;
    }
    
    private CompilerSetImpl parseCompilerSetString(int platform, String data) {
        log.log(Level.FINE, "CSM.initRemoteCompileSets: line = [{0}]", data); // NOI18N
        String versionStart = ";version=";  // NOI18N
        int v = data.indexOf(versionStart);
        String version = null;
        if (v > 0) {
            version = data.substring(v + versionStart.length());
            data = data.substring(0, v);
        }
        String flavor;
        String path;
        StringTokenizer st = new StringTokenizer(data, ";"); // NOI18N
        try {
            flavor = st.nextToken();
            path = st.nextToken();
        } catch (NoSuchElementException ex) {
            log.log(Level.WARNING, "Malformed compilerSetString: {0}", data); // NOI18N
            return null;
        }
        CompilerFlavor compilerFlavor = CompilerFlavorImpl.toFlavor(flavor, platform);
        if (compilerFlavor == null) { // #158084
            log.log(Level.WARNING, "NULL compiler flavor for {0} on platform {1}", new Object[]{flavor, platform}); // NOI18N
            return null;
        }
        CompilerSetImpl cs = CompilerSetImpl.create(compilerFlavor, executionEnvironment, path);
        while (st.hasMoreTokens()) {
            String name = st.nextToken();
            int i = name.indexOf('='); // NOI18N
            if (i < 0) {
                continue;
            }
            String tool = name.substring(0,i);
            String p = name.substring(i + 1);
            i = name.lastIndexOf('/');
            if (i < 0) {
                i = name.lastIndexOf('\\');
            }
            if (i > 0) {
                name = name.substring(i+1);
            }
            PredefinedToolKind kind = PredefinedToolKind.UnknownTool;
            if (tool.equals("c")){ // NOI18N
                kind = PredefinedToolKind.CCompiler;
            } else if (tool.equals("cpp")){ // NOI18N
                kind = PredefinedToolKind.CCCompiler;
            } else if (tool.equals("fortran")){ // NOI18N
                kind = PredefinedToolKind.FortranCompiler;
            } else if (tool.equals("assembler")){ // NOI18N
                kind = PredefinedToolKind.Assembler;
            } else if (tool.equals("make")){ // NOI18N
                kind = PredefinedToolKind.MakeTool;
            } else if (tool.equals("debugger")){ // NOI18N
                kind = PredefinedToolKind.DebuggerTool;
            } else if (tool.equals("cmake")){ // NOI18N
                kind = PredefinedToolKind.CMakeTool;
            } else if (tool.equals("qmake")){ // NOI18N
                kind = PredefinedToolKind.QMakeTool;
            } else if (tool.equals("c(PATH)")){ // NOI18N
                cs.addPathCandidate(PredefinedToolKind.CCompiler, p);
            } else if (tool.equals("cpp(PATH)")){ // NOI18N
                cs.addPathCandidate(PredefinedToolKind.CCCompiler, p);
            } else if (tool.equals("fortran(PATH)")){ // NOI18N
                cs.addPathCandidate(PredefinedToolKind.FortranCompiler, p);
            } else if (tool.equals("assembler(PATH)")){ // NOI18N
                cs.addPathCandidate(PredefinedToolKind.Assembler, p);
            } else if (tool.equals("make(PATH)")){ // NOI18N
                cs.addPathCandidate(PredefinedToolKind.MakeTool, p);
            } else if (tool.equals("debugger(PATH)")){ // NOI18N
                cs.addPathCandidate(PredefinedToolKind.DebuggerTool, p);
            } else if (tool.equals("cmake(PATH)")){ // NOI18N
                cs.addPathCandidate(PredefinedToolKind.CMakeTool, p);
            } else if (tool.equals("qmake(PATH)")){ // NOI18N
                cs.addPathCandidate(PredefinedToolKind.QMakeTool, p);
            }
            if (kind != PredefinedToolKind.UnknownTool) {
                cs.addTool(executionEnvironment, name, p, kind, null);
            }
        }
        return cs;
    }

    /** Initialize remote CompilerSets */
    private synchronized void initRemoteCompilerSets(boolean connect, final boolean runCompilerSetDataLoader) {

        //if (log.isLoggable(Level.FINEST)) {
        //    String text = String.format("\n\n---------- IRCS @%d remoteInitialization=%s state=%s writer=%b\n", //NOI18N
        //            System.identityHashCode(this), remoteInitialization, state, CompilerSetReporter.canReport());
        //    new Exception(text).printStackTrace();
        //}

        // NB: function itself is synchronized!
        if (state == State.STATE_COMPLETE) {
            return;
        }
        if (initializationTask != null) {
            return;
        }
        ServerRecord record = ServerList.get(executionEnvironment);
        assert record != null;

        log.log(Level.FINE, "CSM.initRemoteCompilerSets for {0} [{1}]", new Object[]{executionEnvironment, state}); // NOI18N
        final boolean wasOffline = record.isOffline();
        if (wasOffline) {
            CompilerSetReporter.report("CSM_Conn", false, executionEnvironment.getHost()); //NOI18N
        }
        record.validate(connect);
        if (record.isOnline()) {
            if (wasOffline) {
                CompilerSetReporter.report("CSM_Done"); //NOI18N
            }
            // NB: function itself is synchronized!
            initializationTask = RP.post(new Runnable() {

                @SuppressWarnings("unchecked")
                @Override
                public void run() {
                    //if (log.isLoggable(Level.FINEST)) {
                    //    System.err.printf("\n\n###########\n###### %b @%d #######\n############\n\n",
                    //            CompilerSetReporter.canReport(),System.identityHashCode(CompilerSetManager.this));
                    //}
                    try {
                        provider = CompilerSetProviderFactoryImpl.createNew(executionEnvironment);
                        assert provider != null;
                        provider.init();
                        platform = provider.getPlatform();
                        CompilerSetReporter.report("CSM_ValPlatf", true, PlatformTypes.toString(platform)); //NOI18N
                        CompilerSetReporter.report("CSM_LFTC"); //NOI18N
                        log.log(Level.FINE, "CSM.initRemoteCompileSets: platform = {0}", platform); // NOI18N
                        CompilerSetPreferences.putEnv(executionEnvironment, platform);
                        while (provider.hasMoreCompilerSets()) {
                            String data = provider.getNextCompilerSetData();
                            CompilerSet cs = parseCompilerSetString(platform, data);
                            if (cs != null) {
                                CompilerSetReporter.report("CSM_Found", true, cs.getDisplayName(), cs.getDirectory()); //NOI18N
                                addUnsafe(cs);
                                final List<Tool> toolsCopy = cs.getTools();
                                requestProcessor.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        for (Tool tool : toolsCopy) {
                                            if (!tool.isReady()) {
                                                //CompilerSetReporter.report("CSM_Initializing_Tool", false, tool.getDisplayName()); //NOI18N
                                                tool.waitReady(true);
                                                //CompilerSetReporter.report("CSM_Done"); //NOI18N
                                            }
                                        }
                                    }
                                });
                            } else if(CompilerSetReporter.canReport()) {
                                CompilerSetReporter.report("CSM_Err", true, data);//NOI18N
                            }
                        }
                        removeSubstitutions();
                        completeCompilerSets(false);

                        log.log(Level.FINE, "CSM.initRemoteCompilerSets: Found {0} compiler sets", sets.size()); // NOI18N
                        if (sets.isEmpty()) {
                            CompilerSetReporter.report("CSM_Done_NF"); //NOI18N
                        } else {
                            CompilerSetReporter.report("CSM_Done_OK", true,  sets.size());//NOI18N
                        }
                        // NB: function itself is synchronized!
                        state = State.STATE_COMPLETE;
                        CompilerSetReporter.report(canceled ? "CSM_Canceled" : "CSM_Conigured");//NOI18N
                        if (runCompilerSetDataLoader) {
                            finishInitialization();
                        }
                    } catch (Throwable thr) {
                        // otherwise STATE_PENDING hangs forever - see #158088
                        // NB: function itself is synchronized!
                        state = State.STATE_UNINITIALIZED; //STATE_ERROR;
                        log.log(Level.FINE, "Error initiaizing compiler set @" + hashCode() + //NOI18N
                            " on " + executionEnvironment, thr); //NOI18N
                        CompilerSetReporter.report("CSM_Fail"); //NOI18N
                        completeCompilerSets();
                    } finally {
                        provider = null;
                    }
                }

            });
        } else {
            CompilerSetReporter.report("CSM_Fail");//NOI18N
            // create empty CSM
            log.log(Level.FINE, "CSM.initRemoteCompilerSets: Adding empty CS to OFFLINE host {0}", executionEnvironment);
            completeCompilerSets(false);
            // NB: function itself is synchronized!
            state = State.STATE_UNINITIALIZED; //STATE_ERROR;
        }
    }

    @Override
    public void finishInitialization() {
        log.log(Level.FINE, "Code Model Ready for {0}", CompilerSetManagerImpl.this.toString());
        // FIXUP: this server has been probably deleted; TODO: provide return statis from loader
        if (!ServerList.get(executionEnvironment).isDeleted()) {
            SPIAccessor.get().runTasks(CompilerSetManagerEvents.get(executionEnvironment));
        }
    }

    public void initCompilerSet(CompilerSet cs) {
        CompilerSetImpl impl = (CompilerSetImpl) cs;
        initCompilerSet(impl.getDirectory(), impl, false);
        completeCompilerSet(executionEnvironment, impl, sets);
    }

    public boolean initCompilerSet(String path, CompilerSetImpl cs, boolean known) {
        return initCompilerSet(path, cs, known, PredefinedToolKind.CCompiler);
    }
    
    private boolean initCompilerSet(String path, CompilerSetImpl cs, boolean known, ToolKind requiredTool) {
        CompilerFlavor flavor = cs.getCompilerFlavor();
        ToolchainDescriptor d = flavor.getToolchainDescriptor();
        if (d != null && ToolUtils.isMyFolder(path, d, getPlatform(), known, requiredTool)) {
            CompilerDescriptor compiler = d.getC();
            if (compiler != null && !compiler.skipSearch()) {
                initCompiler(PredefinedToolKind.CCompiler, path, cs, compiler.getNames());
            }
            compiler = d.getCpp();
            if (compiler != null && !compiler.skipSearch()) {
                initCompiler(PredefinedToolKind.CCCompiler, path, cs, compiler.getNames());
            }
            compiler = d.getFortran();
            if (compiler != null && !compiler.skipSearch()) {
                initCompiler(PredefinedToolKind.FortranCompiler, path, cs, compiler.getNames());
            }
            compiler = d.getAssembler();
            if (compiler != null && !compiler.skipSearch()) {
                initCompiler(PredefinedToolKind.Assembler, path, cs, compiler.getNames());
            }
            if (d.getMake() != null && !d.getMake().skipSearch()){
                initCompiler(PredefinedToolKind.MakeTool, path, cs, d.getMake().getNames());
            }
            if (d.getDebugger() != null && !d.getDebugger().skipSearch()){
                initCompiler(PredefinedToolKind.DebuggerTool, path, cs, d.getDebugger().getNames());
            }
            if (d.getQMake() != null && !d.getQMake().skipSearch()){
                initCompiler(PredefinedToolKind.QMakeTool, path, cs, d.getQMake().getNames());
            }
            if (d.getCMake() != null && !d.getCMake().skipSearch()){
                initCompiler(PredefinedToolKind.CMakeTool, path, cs, d.getCMake().getNames());
            }
            return true;
        }
        return false;
    }

    private void initCompiler(ToolKind kind, String path, CompilerSetImpl cs, String[] names) {
        File dir = new File(path);
        if (cs.findTool(kind) != null) {
            // Only one tool of each kind in a cs
            return;
        }
        for (String name : names) {
            File file = new File(dir, name);
            if (file.exists() && !file.isDirectory()) {
                cs.addTool(executionEnvironment, name, file.getAbsolutePath(), kind, null);
                return;
            }
            file = new File(dir, name + ".exe"); // NOI18N
            if (file.exists() && !file.isDirectory()) {
                cs.addTool(executionEnvironment, name, file.getAbsolutePath(), kind, null);
                return;
            }
            File file2 = new File(dir, name + ".exe.lnk"); // NOI18N
            if (file2.exists() && !file2.isDirectory()) {
                cs.addTool(executionEnvironment, name, file.getAbsolutePath(), kind, null);
                return;
            }
        }
    }

    private void removeSubstitutions() {
        Set<CompilerSet> toRemove = new HashSet<CompilerSet>();
        for (CompilerSet cs : sets) {
            String subsitute = cs.getCompilerFlavor().getToolchainDescriptor().getSubstitute();
            if (subsitute != null) {
                // cs is a numbered tool collection
                // find general tool collection that fit the numbered tool collection
                for (CompilerSet c : sets) {
                    if (c.isAutoGenerated()) {
                        if (subsitute.equals(c.getCompilerFlavor().getToolchainDescriptor().getName())) {
                            // c is a general tool collection
                            String general = c.getDirectory();
                            String numbered = cs.getDirectory();
                            if (general.equals(numbered)) {
                                toRemove.add(c);
                            }
                        }
                    }
                }
            }
        }
        for (CompilerSet cs : toRemove) {
            sets.remove(cs);
        }
    }
    
    private void addFakeCompilerSets() {
        for (CompilerFlavor flavor : CompilerFlavorImpl.getFlavors(getPlatform())) {
            ToolchainDescriptor descriptor = flavor.getToolchainDescriptor();
            if (descriptor.getUpdateCenterUrl() != null && descriptor.getModuleID() != null) {
                boolean found = false;
                mainLoop:for (CompilerSet cs : sets) {
                    for(String family : cs.getCompilerFlavor().getToolchainDescriptor().getFamily()){
                        for(String f : flavor.getToolchainDescriptor().getFamily()){
                            if (family.equals(f)) {
                                found = true;
                                break mainLoop;
                            }
                        }
                    }
                }
                if (!found) {
                    CompilerSetImpl fake = (CompilerSetImpl) CompilerSetFactory.getCustomCompilerSet(null, flavor, null, executionEnvironment);
                    fake.setAutoGenerated(true);
                    addUnsafe(fake);
                }
            }
        }
    }

    /**
     * If a compiler set doesn't have one of each compiler types, add a "No compiler"
     * tool. If selected, this will tell the build validation things are OK.
     */
    public void completeCompilerSets() {
        completeCompilerSets(true);
    }

    public void completeCompilerSets(boolean waitReady) {
        if (sets.isEmpty()) { // No compilers found
            addUnsafe(CompilerSetImpl.createEmptyCompilerSet(PlatformTypes.PLATFORM_NONE));
        }
        for (CompilerSet cs : sets) {
            completeCompilerSet(executionEnvironment, (CompilerSetImpl)cs, sets);
        }
        completeSunStudioCompilerSet(getPlatform());
        setDefaltCompilerSet();
        ArrayList<CompilerSet> toSort = new ArrayList<CompilerSet>(sets);
        Collections.<CompilerSet>sort(toSort, new Comparator<CompilerSet>(){
            @Override
            public int compare(CompilerSet o1, CompilerSet o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });
        sets.clear();
        sets.addAll(toSort);
        if (waitReady) {
            completeCompilerSetsSettings(false);
        }
    }

    private void completeCompilerSetsSettings(boolean reset) {
        for (CompilerSet cs : sets) {
            for (Tool tool : cs.getTools()) {
                if (!tool.isReady()) {
                    tool.waitReady(reset);
                }
            }
        }
    }

    private void completeSunStudioCompilerSet(int platform) {
        Set<String> aliases = new HashSet<String>();
        for(CompilerSet cs : sets) {
            aliases.addAll(Arrays.asList(cs.getCompilerFlavor().getToolchainDescriptor().getAliases()));
        }
        List<ToolchainDescriptor> platfomToolCollections = ToolchainManagerImpl.getImpl().getToolchains(platform);
        for(String alias : aliases) {
            if (getCompilerSet(alias) != null) {
                continue;
            }
            CompilerSetImpl bestCandidate = null;
            loop:for(ToolchainDescriptor tc : platfomToolCollections) {
                if (Arrays.asList(tc.getAliases()).contains(alias)) {
                    for(CompilerSet cs : sets) {
                        if (cs.getCompilerFlavor().getToolchainDescriptor().equals(tc)) {
                            bestCandidate = (CompilerSetImpl) cs;
                            break loop;
                        }
                    }
                }
            }
            if (bestCandidate == null) {
                continue;
            }
            if (bestCandidate.isUrlPointer()) {
                continue;
            }
            CompilerFlavor flavor = CompilerFlavorImpl.toFlavor(alias, platform);
            if (flavor != null) {
                CompilerSetImpl bestCandidateCopy = bestCandidate
                        .createCopy(executionEnvironment, flavor, bestCandidate.getDirectory(), alias, flavor.getToolchainDescriptor().getDisplayName(),
                                true, true, bestCandidate.getModifyBuildPath(), bestCandidate.getModifyRunPath());
                addUnsafe(bestCandidateCopy);
            }
        }
    }

    private static Tool autoComplete(ExecutionEnvironment env, CompilerSetImpl cs, List<CompilerSet> sets, ToolDescriptor descriptor, ToolKind tool){
        if (descriptor != null && !cs.isUrlPointer()) {
            AlternativePath[] paths = descriptor.getAlternativePath();
            if (paths != null && paths.length > 0) {
                for(AlternativePath p : paths){
                    switch(p.getKind()){
                        case PATH:
                        {
                            StringTokenizer st = new StringTokenizer(p.getPath(),";,"); // NOI18N
                            while(st.hasMoreTokens()){
                                String method = st.nextToken();
                                if ("$PATH".equals(method)){ // NOI18N
                                    if (env.isLocal()) {
                                        for(String name : descriptor.getNames()){
                                            String path = ToolUtils.findCommand(cs, name);
                                            if (path != null) {
                                                if (notSkipedName(cs, descriptor, path, name)) {
                                                    return cs.addNewTool(env, CndPathUtilities.getBaseName(path), path, tool, null);
                                                }
                                            }
                                        }
                                     } else {
                                        String path = cs.getPathCandidate(tool);
                                        if (path != null) {
                                            String name = CndPathUtilities.getBaseName(path);
                                            if (notSkipedName(cs, descriptor, path, name)) {
                                                return cs.addNewTool(env, name, path, tool, null);
                                            }
                                        }
                                    }
                                } else if ("$MSYS".equals(method)){ // NOI18N
                                    if (env.isLocal()) {
                                        for(String name : descriptor.getNames()){
                                            String dir = cs.getCommandFolder();
                                            if (dir != null) {
                                                String path = ToolUtils.findCommand(name, dir); // NOI18N
                                                if (path != null) {
                                                    if (notSkipedName(cs, descriptor, path, name)) {
                                                        return cs.addNewTool(env, CndPathUtilities.getBaseName(path), path, tool, null);
                                                    }
                                                }
                                            }
                                        }
                                     } else {
                                        // TODO
                                    }
                                } else {
                                    if (env.isLocal()) {
                                        for(String name : descriptor.getNames()){
                                            if (!CndPathUtilities.isAbsolute(method)) {
                                                String directory = cs.getDirectory();
                                                method = CndFileUtils.normalizeAbsolutePath(directory+"/"+method); // NOI18N
                                            }
                                            String path = ToolUtils.findCommand(name, method);
                                            if (path != null) {
                                                return cs.addNewTool(env, CndPathUtilities.getBaseName(path), path, tool, null);
                                            }
                                        }
                                    } else {
                                        // TODO
                                    }
                                }
                            }
                            break;
                        }
                        case TOOL_FAMILY:
                        {
                            StringTokenizer st = new StringTokenizer(p.getPath(),";,"); // NOI18N
                            while(st.hasMoreTokens()){
                                String method = st.nextToken();
                                for(CompilerSet s : sets){
                                    if (s != cs) {
                                        for(String family : s.getCompilerFlavor().getToolchainDescriptor().getFamily()){
                                            if (family.equals(method)){
                                                Tool other = s.findTool(tool);
                                                if (other != null){
                                                    return cs.addNewTool(env, other.getName(), other.getPath(), tool, other.getFlavor());
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            break;
                        }
                        case TOOL_NAME:
                        {
                            StringTokenizer st = new StringTokenizer(p.getPath(),";,"); // NOI18N
                            while(st.hasMoreTokens()){
                                String method = st.nextToken();
                                for(CompilerSet s : sets){
                                    if (s != cs) {
                                        String name = s.getCompilerFlavor().getToolchainDescriptor().getName();
                                        if (name.equals(method) || "*".equals(method)){ // NOI18N
                                            Tool other = s.findTool(tool);
                                            if (other != null){
                                                return cs.addNewTool(env, other.getName(), other.getPath(), tool, other.getFlavor());
                                            }
                                        }
                                    }
                                }
                            }
                            break;
                        }
                    }
                }
            }
        }
        return cs.addTool(env, "", "", tool, null); // NOI18N
    }

    private static boolean notSkipedName(CompilerSet cs, ToolDescriptor descriptor, String path, String name){
        if (!descriptor.skipSearch()) {
            return true;
        }
        String s = cs.getDirectory()+"/"+name; // NOI18N
        s = s.replace('\\', '/'); // NOI18N
        path = path.replace('\\', '/'); // NOI18N
        return !path.startsWith(s);
    }

    /*package-local*/ static void completeCompilerSet(ExecutionEnvironment env, CompilerSetImpl cs, List<CompilerSet> sets) {
        //if (cs.findTool(Tool.CCompiler) == null && cs.findTool(Tool.CCCompiler) == null) {
        //    // do not complete empty tool
        //    return;
        //}
        if (cs.findTool(PredefinedToolKind.CCompiler) == null) {
            autoComplete(env, cs, sets, cs.getCompilerFlavor().getToolchainDescriptor().getC(), PredefinedToolKind.CCompiler);
        }
        if (cs.findTool(PredefinedToolKind.CCCompiler) == null) {
            autoComplete(env, cs, sets, cs.getCompilerFlavor().getToolchainDescriptor().getCpp(), PredefinedToolKind.CCCompiler);
        }
        if (cs.findTool(PredefinedToolKind.FortranCompiler) == null) {
            autoComplete(env, cs, sets, cs.getCompilerFlavor().getToolchainDescriptor().getFortran(), PredefinedToolKind.FortranCompiler);
        }
        if (cs.findTool(PredefinedToolKind.Assembler) == null) {
            autoComplete(env, cs, sets, cs.getCompilerFlavor().getToolchainDescriptor().getAssembler(), PredefinedToolKind.Assembler);
        }
        if (cs.findTool(PredefinedToolKind.MakeTool) == null) {
            autoComplete(env, cs, sets, cs.getCompilerFlavor().getToolchainDescriptor().getMake(), PredefinedToolKind.MakeTool);
        }
        if (cs.findTool(PredefinedToolKind.DebuggerTool) == null) {
            autoComplete(env, cs, sets, cs.getCompilerFlavor().getToolchainDescriptor().getDebugger(), PredefinedToolKind.DebuggerTool);
        }
        if (cs.findTool(PredefinedToolKind.QMakeTool) == null) {
            autoComplete(env, cs, sets, cs.getCompilerFlavor().getToolchainDescriptor().getQMake(), PredefinedToolKind.QMakeTool);
        }
        if (cs.findTool(PredefinedToolKind.CMakeTool) == null) {
            autoComplete(env, cs, sets, cs.getCompilerFlavor().getToolchainDescriptor().getCMake(), PredefinedToolKind.CMakeTool);
        }
    }

    /**
     * Add a CompilerSet to this CompilerSetManager. Make sure it doesn't get added multiple times.
     *
     * @param cs The CompilerSet to (possibly) add
     */
    public void add(CompilerSet cs) {
        if (sets.size() == 1 && sets.get(0).getName().equals(CompilerSetImpl.None)) {
            sets.remove(0);
        }
        sets.add(cs);
        if (sets.size() == 1) {
            setDefault(cs);
        }
        completeCompilerSets();
        this.state = State.STATE_COMPLETE;
    }

    private void addUnsafe(CompilerSet cs) {
        if (sets.size() == 1 && sets.get(0).getName().equals(CompilerSetImpl.None)) {
            sets.remove(0);
        }
        sets.add(cs);
    }

    @Override
    public final boolean isEmpty() {
        if ((sets.isEmpty()) ||
                (sets.size() == 1 && sets.get(0).getName().equals(CompilerSetImpl.None))) {
            return true;
        }
        return false;
    }

    /**
     * Remove a CompilerSet from this CompilerSetManager. Use caution with this method. Its primary
     * use is to remove temporary CompilerSets which were added to represent missing compiler sets. In
     * that context, they're removed immediately after showing the ToolsPanel after project open.
     *
     * @param cs The CompilerSet to (possibly) remove
     */
    public void remove(CompilerSet cs) {
        int idx = sets.indexOf(cs);
        if (idx >= 0) {
            sets.remove(idx);
        }
    }

    @Override
    public CompilerSet getCompilerSet(String name) {
        for (CompilerSet cs : sets) {
            if (cs.getName().equals(name)) {
                return cs;
            }
        }
        return null;
    }

    public CompilerSet getCompilerSet(int idx) {
        //waitForCompletion();
        if (isPending()) {
            log.log(Level.WARNING, "calling getCompilerSet() on uninitialized {0}", getClass().getSimpleName());
        }
        if (idx >= 0 && idx < sets.size()) {
            return sets.get(idx);
        }
        return null;
    }

    @Override
    public List<CompilerSet> getCompilerSets() {
        return new ArrayList<CompilerSet>(sets);
    }

    @Override
    public void setDefault(CompilerSet newDefault) {
        boolean set = false;
        for (CompilerSet cs : getCompilerSets()) {
            ((CompilerSetImpl)cs).setAsDefault(false);
            if (cs == newDefault) {
                ((CompilerSetImpl)newDefault).setAsDefault(true);
                set = true;
            }
        }
        if (!set && sets.size() > 0) {
            ((CompilerSetImpl)getCompilerSet(0)).setAsDefault(true);
        }
    }

    @Override
    public CompilerSet getDefaultCompilerSet() {
        for (CompilerSet cs : getCompilerSets()) {
            if (((CompilerSetImpl)cs).isDefault()) {
                return cs;
            }
        }
        return null;
    }

    @Override
    public boolean isDefaultCompilerSet(CompilerSet cs) {
        if (cs == null) {
            return false;
        }
        return ((CompilerSetImpl)cs).isDefault();
    }

    @Override
    public String toString() {
        StringBuilder out = new StringBuilder();
        out.append("CSM for ").append(executionEnvironment.toString()); // NOI18N
        out.append(" with toolchains:["); // NOI18N
        for (CompilerSet compilerSet : sets) {
            out.append(compilerSet.getName()).append(" "); // NOI18N
        }
        out.append("]"); // NOI18N
        out.append(" platform:").append(PlatformTypes.toString(platform)); // NOI18N
        out.append(" in state ").append(state.toString()); // NOI18N
        return out.toString();
    }

    public ExecutionEnvironment getExecutionEnvironment() {
        return executionEnvironment;
    }

    private static enum State {
        STATE_PENDING,
        STATE_COMPLETE,
        STATE_UNINITIALIZED
    }

    private static final class FolderDescriptor {
        private final String path;
        private final boolean knownFolder;
        private FolderDescriptor(String path, boolean knownFolder){
            this.path = path;
            this.knownFolder = knownFolder;
        }

        @Override
        public int hashCode() {
            return path.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof FolderDescriptor) {
                return path.equals(((FolderDescriptor)obj).path);
            }
            return false;
        }
    }
}
