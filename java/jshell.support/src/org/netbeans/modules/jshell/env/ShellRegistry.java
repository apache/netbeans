/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.jshell.env;

import java.io.File;
import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.netbeans.lib.nbjshell.LaunchJDIAgent;
import jdk.jshell.spi.ExecutionControlProvider;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.jshell.launch.ShellOptions;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.InstalledFileLocator;
import org.openide.modules.Places;
import org.openide.util.BaseUtilities;
import org.openide.util.NbBundle;


/**
 * Registration of all running JShells. Each project can have zero to many
 * JShell. The Registry keeps and maintains work area for individual shells; the
 * work area is cleaned up upon termination.
 * <p/>
 * The Registry attempts to clear everything in the work area on its first
 * usage, then deletes JShellEnvironment's workroot when the env instance disappears.
 * During operation, it tries to reuse existing undeleted folders.
 * 
 * @author sdedic
 */
public class ShellRegistry {
    private static final Logger LOG = Logger.getLogger(ShellRegistry.class.getName());
    
    /**
     * Prefix for individual JShell directories created in the work area
     */
    private static final String WORKAREA_PREFIX = "junk"; // NOI18N
    
    private static ShellRegistry INSTANCE = new ShellRegistry();
    
    private ShellRegistry() {}
    
    /**
     * Root of the trash area; individual JShell subtrees are beneath it.
     */
    private FileObject  trashRoot;
    
    private Set<String> ignoreNames = new HashSet<>();
    
    // @GuardedBy(this)
    private void createAndCleanTrashArea() throws IOException {
        if (trashRoot != null) {
            return;
        }
        FileObject r = FileUtil.toFileObject(Places.getCacheSubdirectory("jshell"));
        if (r == null) {
            throw new IOException("Unable to create cache for generated snippets");
        }
        LOG.log(Level.FINE, "Clearing trash area");
        trashRoot = r;
        for (FileObject f : r.getChildren()) {
            LOG.log(Level.FINE, "Deleting: {0}", f);
            try {
                f.delete();
            } catch (IOException ex) {
                LOG.log(Level.WARNING, "Could not delete Java Shell work area {0}: {1}", new Object[] { f, ex });
                ignoreNames.add(f.getNameExt());
            }
        }
    }
    
    public static ShellRegistry get() {
        return INSTANCE;
    }
    
    /**
     * Per-project clean VM sessions, initiated by {@link #openProjectSession}.
     */
    private Map<Project, JShellEnvironment>     projectSessions = new HashMap<>();
    private Map<FileObject, Reference<JShellEnvironment>>  fileIndex = new HashMap<>();
    private JShellEnvironment                   defaultSession;
    private final Collection<ShellListener>           shellListeners= new ArrayList<>();
    
    private void fireEnvCreated(JShellEnvironment env) {
        Collection<ShellListener> ll;
        synchronized (shellListeners) {
            if (shellListeners.isEmpty()) {
                return;
            }
            ll = new ArrayList<>(shellListeners);
        }
        ShellEvent ev = new ShellEvent(env);
        for (ShellListener l : ll) {
            l.shellCreated(ev);
        }
    }
    
    public void addShellListener(ShellListener ll) {
        synchronized (shellListeners) {
            if (!shellListeners.contains(ll)) {
                shellListeners.add(ll);
            }
        }
    }
    
    public void removeShellListener(ShellListener ll) {
        synchronized (shellListeners) {
            shellListeners.remove(ll);
        }
    }
    
    //@GuardedBy(this)
    private FileObject  createCacheRoot() throws IOException {
        List<FileObject> roots = fileIndex.keySet().stream().map((f) -> f.getParent()).collect(Collectors.toList());
        Set<FileObject> existing = new HashSet<>(Arrays.asList(trashRoot.getChildren()));
        existing.removeAll(roots);
        existing.removeAll(ignoreNames);
        if (!existing.isEmpty()) {
            // reuse an existing root
            FileObject r = existing.iterator().next();
            for (FileObject c : r.getChildren()) {
                c.delete();
            }
            return r;
        }
        while (true) {
            String n = FileUtil.findFreeFolderName(trashRoot, WORKAREA_PREFIX);
            try {
                return trashRoot.createFolder(n);
            } catch (IOException ex) {
                // only ignore exceptions where the 'free name' actually exists.
                if (trashRoot.getFileObject(n) == null) {
                    throw ex;
                }
            }
        }
    }
    
    private void deleteCacheRoot(Reference<JShellEnvironment> ref, FileObject f) {
        synchronized (this) {
            if (fileIndex.get(f) != ref) {
                // reused.
                return;
            }
            fileIndex.remove(f);
            try {
                f.delete();
            } catch (IOException ex) {
                LOG.log(Level.WARNING, "Could not delete work root {0}: {1}", new Object[] { f, ex });
                ignoreNames.add(f.getNameExt());
            }
        }
    }
    

    /**
     * Opens a new session for the project, or return an existin one. The method 
     * will open a clean VM session for the project. If a session has been already started,
     * that existing session will be returned. It is not possible to start multiple VMs for 
     * a single project.
     * 
     * @param p the project to use classes from
     * @return the environment instance
     * @throws IOException 
     */
    @NbBundle.Messages({
        "# {0} - project name",
        "ShellSession_CleanProject=Java Shell - project {0}"
    })
    public JShellEnvironment openProjectSession(Project p) throws IOException {
        JShellEnvironment s;
        synchronized (this) {
            s = projectSessions.get(p);
            if (s != null) {
                return s;
            }
            String dispName = Bundle.ShellSession_CleanProject(
                    ProjectUtils.getInformation(p).getDisplayName());
            s = new LaunchJShellEnv(p, dispName); // may throw IOE
            register(s);
            projectSessions.put(p, s);
        }
        fireEnvCreated(s);
        s.start();
        return s;
    }
    
    public Project findProject(FileObject consoleFile) {
        synchronized (this) {
            Reference<JShellEnvironment> refEnv = fileIndex.get(consoleFile);
            if (refEnv == null) {
                return null;
            }
            JShellEnvironment env = refEnv.get();
            if (env == null) {
                return null;
            }
            return env.getProject();
        }
    }
    
    public void startJShell(JShellEnvironment env) throws IOException {
        register(env);
        fireEnvCreated(env);
        env.start();
    }
    
    void register(JShellEnvironment env) throws IOException {
        synchronized (this) {
            createAndCleanTrashArea();
            FileObject r = createCacheRoot();
            env.init(r);
            fileIndex.put(env.getConsoleFile(), new CLR(r, env));
        }
    }
    
    public JShellEnvironment get(FileObject consoleFile) {
        synchronized (this) {
            Reference<JShellEnvironment> ref = fileIndex.get(consoleFile);
            return ref == null ? null : ref.get();
        }
    }
    
    void closed(JShellEnvironment env) {
        Project p = env.getProject();
        synchronized (this) {
            Reference<JShellEnvironment> ref = fileIndex.get(env.getConsoleFile());
            if (ref != null && ref.get() == env) {
                fileIndex.remove(env.getConsoleFile());
                projectSessions.remove(p);
            } else {
                return;
            }
        }
        GlobalPathRegistry.getDefault().unregister(ClassPath.SOURCE, new ClassPath[] { 
            env.getSnippetClassPath()
        });
    }
    
    private ShellOptions options = ShellOptions.get();
    
    @NbBundle.Messages({
        "# {0} JDK platform name",
        "TITLE_PlatformShell=Java Shell - {0}",
        "ERR_NoShellPlatform=No suitable Java Platform configured. Do you want to configure Java Shell now ?"
    })
    public JShellEnvironment openDefaultSession(JavaPlatform platform) throws IOException {
        JShellEnvironment current;
        boolean forceClose = false;
        synchronized (this) {
            current = defaultSession;
            if (current != null) {
                if (current.getPlatform() != platform) {
                    forceClose = true;
                    defaultSession = null;
                } else if (current.getStatus() == ShellStatus.SHUTDOWN ||
                           current.getStatus() == ShellStatus.DISCONNECTED) {
                    forceClose = true;
                    defaultSession = null;
                }
            }
        }
        if (forceClose) {
            current.closeEditor();
        } else if (current != null) {
            return current;
        }
        String dispName = Bundle.TITLE_PlatformShell(platform.getDisplayName());
        JShellEnvironment s = new LaunchJShellEnv(platform, dispName); // may throw IOE
        synchronized (this) {
            if (defaultSession != null) {
                return defaultSession;
            }
            register(s);
            defaultSession = s;
        }
        fireEnvCreated(s);
        return s;
    }
    
    /**
     * Finds a JShell console file for the given JShell work file. If the passed
     * file is not in any active JShell work area, returns {@code null}. 
     * 
     * @param f
     * @return 
     */
    public JShellEnvironment getOwnerEnvironment(FileObject f) {
        if (trashRoot == null || !FileUtil.isParentOf(trashRoot, f)) {
            return null;
        }
        List<JShellEnvironment> env;
        
        synchronized (this) {
            env = new ArrayList<>(fileIndex.size());
            for (Iterator<Reference<JShellEnvironment>> it = fileIndex.values().iterator();
                    it.hasNext(); ) {
                Reference<JShellEnvironment> ref = it.next();
                JShellEnvironment e = ref.get();
                if (e != null) {
                    env.add(e);
                } else {
                    it.remove();
                }
            }
        }
        for (JShellEnvironment e : env) {
            FileObject wr = e.getWorkRoot();
            if (wr == f || FileUtil.isParentOf(e.getWorkRoot(), f)) {
                return e;
            }
        }
        return null;
    }
    
    private static class LaunchJShellEnv extends JShellEnvironment {
        private JavaPlatform platform;
        
        public LaunchJShellEnv(Project project, String displayName) {
            super(project, displayName);
        }
        
        public LaunchJShellEnv(JavaPlatform platform, String displayName) {
            super(null, displayName);
            this.platform = platform;
        }

        @Override
        public JavaPlatform getPlatform() {
            if (platform != null) {
                return platform;
            } else {
                return super.getPlatform();
            }
        }
        
        @Override
        public ExecutionControlProvider createExecutionEnv() {
            return LaunchJDIAgent.launch(getPlatform());
        }
        
        private String createClasspathString(String dummy) {
            File remoteProbeJar = InstalledFileLocator.getDefault().locate(
                    "modules/ext/nb-custom-jshell-probe.jar", "org.netbeans.libs.jshell", false);
            File replJar = InstalledFileLocator.getDefault().locate("modules/ext/nb-jshell.jar", "org.netbeans.libs.jshell", false);
            File toolsJar = null;

            for (FileObject jdkInstallDir : getPlatform().getInstallFolders()) {
                FileObject toolsJarFO = jdkInstallDir.getFileObject("lib/tools.jar");

                if (toolsJarFO == null) {
                    toolsJarFO = jdkInstallDir.getFileObject("../lib/tools.jar");
                }
                if (toolsJarFO != null) {
                    toolsJar = FileUtil.toFile(toolsJarFO);
                }
            }
            ClassPath compilePath = getClasspathInfo().getClassPath(ClasspathInfo.PathKind.COMPILE);

            FileObject[] roots = compilePath.getRoots();
            File[] urlFiles = new File[roots.length];
            int index = 0;
            for (FileObject fo : roots) {
                File f = FileUtil.toFile(fo);
                if (f != null) {
                    urlFiles[index++] = f;
                }
            }
            String cp = addClassPath(
                    toolsJar != null ? toClassPath(remoteProbeJar, toolsJar) : 
                                       toClassPath(remoteProbeJar),
                    urlFiles) + System.getProperty("path.separator") + " "; // NOI18N avoid REPL bug

            return "-classpath " + cp; // NOI18N
        }

        private static String addClassPath(String prefix, File... files) {
            String suffix = toClassPath(files);
            if (prefix != null && !prefix.isEmpty()) {
                return prefix + System.getProperty("path.separator") + suffix;
            }
            return suffix;
        }

        private static String toClassPath(File... files) {
            String sep = "";
            StringBuilder cp = new StringBuilder();

            for (File f : files) {
                if (f == null) continue;
                cp.append(sep);
                cp.append(f.getAbsolutePath());
                sep = System.getProperty("path.separator");
            }

            return cp.toString();
        }

    }
    
    private class CLR extends WeakReference<JShellEnvironment> implements Runnable {
        private final FileObject workRoot;

        public CLR(FileObject workRoot, JShellEnvironment referent) {
            super(referent, BaseUtilities.activeReferenceQueue());
            this.workRoot = workRoot;
        }

        @Override
        public void run() {
            LOG.log(Level.FINE, "Work root {0} expired, trying to delete.", workRoot);
            deleteCacheRoot(this, workRoot);
        }
    }
    
    public Collection<JShellEnvironment> openedShells(Project filter) {
        Collection<JShellEnvironment> ret;
        synchronized (this) {
            ret = new ArrayList<>(fileIndex.size());
            for (Iterator<Reference<JShellEnvironment>> it = fileIndex.values().iterator(); it.hasNext(); ) {
                Reference<JShellEnvironment> ref = it.next();
                JShellEnvironment e = ref.get();
                if (e != null) {
                    if (filter == null || filter == e.getProject()) {
                        ret.add(e);
                    }
                }
            }
        }
        return ret;
    }
}
