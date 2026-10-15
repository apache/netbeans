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
package org.netbeans.modules.jshell.env;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FilterReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.text.Document;
import jdk.jshell.JShell;
import jdk.jshell.spi.ExecutionControlProvider;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.queries.SourceLevelQuery;
import org.netbeans.api.java.queries.SourceLevelQuery.Result;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.ClasspathInfo.PathKind;
import org.netbeans.api.project.Project;
import org.netbeans.modules.jshell.model.ConsoleEvent;
import org.netbeans.modules.jshell.model.ConsoleListener;
import org.netbeans.modules.jshell.project.ShellProjectUtils;
import org.netbeans.modules.jshell.support.ShellSession;
import org.netbeans.spi.java.classpath.ClassPathFactory;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.modules.SpecificationVersion;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.Pair;
import org.openide.util.Task;
import org.openide.util.WeakListeners;
import org.openide.util.io.ReaderInputStream;
import org.openide.util.lookup.ProxyLookup;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;

/**
 * Encapsulates the IDE environment for the JShell. There are two implementations; one which works with a
 * Project and another, which works without it.
 * 
 * @author sdedic
 */
public class JShellEnvironment {
    public static final String PROP_DOCUMENT = "document";
    
    /**
     * filesystem which holds Snippets and the console file
     */
    private FileObject        workRoot;
    
    /**
     * The console file
     */
    private FileObject        consoleFile;
    
    private ShellSession      shellSession;
    
    private final Project     project;
    
    private ClasspathInfo     classpathInfo;

    private JavaPlatform      platform;
    
    private String            displayName;
    
    private ConfigurableClasspath  userClassPathImpl = new ConfigurableClasspath();
    
    private ClassPath         userLibraryPath = ClassPathFactory.createClassPath(userClassPathImpl);
    
    private ClassPath         snippetClassPath;
    
    private InputOutput       inputOutput;
    
    /**
     * True, if this environment controls the IO
     */
    private boolean           controlsIO;
    
    private boolean           closed;
    
    private List<ShellListener>   shellListeners = new ArrayList<>();
    
    private Lookup            envLookup;
    
    private final ShellL            shellL = new ShellL();
    
    private Document document;
    
    protected JShellEnvironment(Project project, String displayName) {
        this.project = project;
        this.displayName = displayName;
    }
    
    public void appendClassPath(FileObject f) {
        userClassPathImpl.append(f);
    }
    
    public Project getProject() {
        return project;
    }

    public JavaPlatform getPlatform() {
        return platform;
    }

    public String getDisplayName() {
        return displayName;
    }
    
    private class L implements PropertyChangeListener {
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (EditorCookie.Observable.PROP_DOCUMENT.equals(evt.getPropertyName())) {
            }
        }
    }
    
    private L inst;
    
    void init(FileObject workRoot) throws IOException {
        this.workRoot = workRoot;
        workRoot.setAttribute("jshell.scratch", true);
        consoleFile = workRoot.createData("console.jsh");
        
        EditorCookie.Observable eob = consoleFile.getLookup().lookup(EditorCookie.Observable.class);
        inst = new L();
        eob.addPropertyChangeListener(WeakListeners.propertyChange(inst, eob));

        platform = org.netbeans.modules.jshell.project.ShellProjectUtils.findPlatform(project);
    }
    
    protected InputOutput createInputOutput() {
        return null;
    }
    
    private PrintStream outStream;
    private PrintStream errStream;
    
    public InputStream getInputStream() throws IOException {
        if (inputOutput == null) {
            throw new IllegalStateException("not started");
        }
        return new ReaderInputStream(
                new FilterReader(inputOutput.getIn()) {
                    @Override
                    public void close() throws IOException {
                        // do not close the input, JShell may be reset.
                    }
                }, "UTF-8" // NOI18N
        );
    }
    
    public Lookup getLookup() {
        synchronized (this) {
            if (envLookup == null) {
                envLookup = new ProxyLookup(
                        consoleFile.getLookup(),
                        project == null ? 
                                Lookup.getDefault() :
                                project.getLookup()
                );
            }
        }
        return envLookup;
    }
    
    public PrintStream getOutputStream() throws IOException {
        if (inputOutput == null) {
            throw new IllegalStateException("not started");
        }
        synchronized (this) {
            if (outStream == null) {
                outStream = new PrintStream(new WriterOutputStream(inputOutput.getOut())) {
                    @Override
                    public void close() {
                        // suppress close
                    }
                };
            }
        }
        return outStream;
    }
    
    public PrintStream getErrorStream() throws IOException  {
        if (inputOutput == null) {
            throw new IllegalStateException("not started");
        }
        synchronized (this) {
            if (errStream == null) {
                errStream = new PrintStream(
                    new WriterOutputStream(inputOutput.getOut())) {
                    @Override
                    public void close() {
                        // suppress close
                    }
                    
                };
            }
        }
        return errStream;
    }
    
    public SpecificationVersion getSourceLevel() {
        Project p = getProject();
        if (p == null) {
            return getTargetLevel();
        }
        Result r = SourceLevelQuery.getSourceLevel2(p.getProjectDirectory());
        String s = r.getSourceLevel();
        if (s != null) {
            return new SpecificationVersion(s);
        } else {
            return getTargetLevel();
        }
    }
    
    public SpecificationVersion getTargetLevel() {
        return getPlatform().getSpecification().getVersion();
    }
    
    /**
     * @return modules which should be added to the compiler, or {@code null},
     * if not modular
     */
    public List<String> getCompilerRequiredModules() {
        return requiredModules;
    }
    
    public JShell.Builder customizeJShell(JShell.Builder b) {
        if (ShellProjectUtils.isModularProject(project)) {
            if (requiredModules != null) {
                b.compilerOptions("--add-modules", String.join(",", requiredModules)); // NOI18N
                
            }
            // extra options to include the modules:
            List<String> opts = ShellProjectUtils.launchVMOptions(project);
            b.remoteVMOptions(opts.toArray(String[]::new));
            
            String modPath = addRoots("", ShellProjectUtils.projectRuntimeModulePath(project));
            if (!modPath.isEmpty()) {
                b.remoteVMOptions("--module-path", modPath);
            }
        }
        return b;
    }
    
    public ClassPath    getVMClassPath() {
        return ShellProjectUtils.projecRuntimeClassPath(project);
    }

    public ClassPath    getCompilerClasspath() {
        if (ShellProjectUtils.isModularProject(project)) {
            return getClasspathInfo().getClassPath(PathKind.MODULE_COMPILE);
        } else {
            return getClasspathInfo().getClassPath(PathKind.COMPILE);
        }
    }
    
    private String addRoots(String prev, ClassPath cp) {
        FileObject[] roots = cp.getRoots();
        StringBuilder sb = new StringBuilder(prev);
        
        for (FileObject r : roots) {
            FileObject ar = FileUtil.getArchiveFile(r);
            if (ar == null) {
                ar = r;
            }
            File f = FileUtil.toFile(ar);
            if (f != null) {
                if (sb.length() > 0) {
                    sb.append(File.pathSeparatorChar);
                }
                sb.append(f.getPath());
            }
        }
        return sb.toString();
    }
    
    private volatile boolean starting;
    
    private List<String>    requiredModules;

    public synchronized void start() throws IOException {
        assert workRoot != null;
        if (shellSession != null) {
            return;
        }
        inputOutput = createInputOutput();
        if (inputOutput == null) {
            inputOutput = IOProvider.getDefault().getIO(displayName, false);
            controlsIO = true;
        }
        JavaPlatform platformTemp = getPlatform();
        final List<URL> roots = new ArrayList<>();
        FileObject root = ShellProjectUtils.findProjectRoots(getProject(), roots);
        ClasspathInfo cpi;
        
        snippetClassPath = ClassPathSupport.createClassPath(workRoot);

        if (root != null) {
            // assume project
            boolean modular = ShellProjectUtils.isModularProject(project);
            ClasspathInfo projectInfo = ClasspathInfo.create(root);
            ClasspathInfo.Builder bld = new ClasspathInfo.Builder(
                    projectInfo.getClassPath(ClasspathInfo.PathKind.BOOT)
            );
            ClassPath classesFromProject = ClassPathSupport.createClassPath(roots.toArray(URL[]::new));
            ClassPath modBoot = projectInfo.getClassPath(ClasspathInfo.PathKind.MODULE_BOOT);
            ClassPath modClassRaw = projectInfo.getClassPath(ClasspathInfo.PathKind.MODULE_CLASS);
            ClassPath modCompileRaw = projectInfo.getClassPath(ClasspathInfo.PathKind.MODULE_COMPILE);
            
            // TODO: Possible duplicate entries on CP + ModuleCP in case of modular project. May impact
            // refactoring or usages.
            ClassPath compile = ClassPathSupport.createProxyClassPath(
                classesFromProject,
                userLibraryPath,
                projectInfo.getClassPath(ClasspathInfo.PathKind.COMPILE)
            );
            ClassPath modClass;
            ClassPath modCompile;
            
            if (modular) {
                modClass = ClassPathSupport.createProxyClassPath(
                            classesFromProject,
                            userLibraryPath,
                            modClassRaw);
                modCompile = ClassPathSupport.createProxyClassPath(
                            classesFromProject,
                            modCompileRaw
                        );
            } else {
                modClass = modClassRaw;
                modCompile = modCompileRaw;
            }
            
            bld.setClassPath(compile)
                //.setSourcePath(source)
                    ;
            
            bld.
                setModuleBootPath(modBoot).
                setModuleClassPath(modClass).
                setModuleCompilePath(modCompile);
            cpi = bld.build();

            if (ShellProjectUtils.isModularProject(project)) {
                List<String> sortedModules = new ArrayList<>(ShellProjectUtils.findProjectImportedModules(project, null));
                if (!sortedModules.isEmpty()) {
                    Collections.sort(sortedModules);
                    this.requiredModules = sortedModules;
                }
            }
        } else {
            ClasspathInfo.Builder bld = new ClasspathInfo.Builder(platformTemp.getBootstrapLibraries());
            bld.setClassPath(platformTemp.getStandardLibraries());
            if (ShellProjectUtils.isModularJDK(platformTemp)) {
                bld.setModuleBootPath(platformTemp.getBootstrapLibraries());
            }
            /*
            cpi = ClasspathInfo.create(
                    platformTemp.getBootstrapLibraries(),
                    platformTemp.getStandardLibraries(),
                    ClassPath.EMPTY);
            */
            cpi = bld.build();
        }
        this.classpathInfo = cpi;
        forceOpenDocument();
        // createSession will get opened document, shutdown runs under lock
        doStartAndFire(ShellSession.createSession(this));
    }

    private void fireShellStatus(ShellEvent event) {
        List<ShellListener> ll;
        synchronized (this) {
            ll = new ArrayList<>(this.shellListeners);
        }
        if (ll.isEmpty()) {
            return;
        }
        ll.stream().forEach(l -> l.shellStatusChanged(event));
    }

    private void fireShellStarted(ShellEvent event) {
        List<ShellListener> ll;
        synchronized (this) {
            ll = new ArrayList<>(this.shellListeners);
        }
        if (ll.isEmpty()) {
            return;
        }
        ShellEvent e = event == null ? new ShellEvent(this) : event;
        ll.stream().forEach(l -> l.shellStarted(e));
    }
    
    /**
     * Determines if the environment can be reset. If it returns false,
     * then {@link #reset} will possibly reset the JShell, but will not
     * put it in a usable shape. For example without a running VM, the JShell
     * will not be able to define or execute snippets (or even its own startup).
     * 
     * @return if the environment can be reset.
     */
    public boolean canReset() {
        return true;
    }
    
    public void reset() {
        assert workRoot != null;
        doStartAndFire(ShellSession.createSession(this));
    }
    
    private void fireExecuting(ShellSession session, boolean start) {
        List<ShellListener> ll;
        synchronized (this) {
            ll = new ArrayList<>(this.shellListeners);
        }
        if (ll.isEmpty()) {
            return;
        }
        ShellEvent e = new ShellEvent(this, session, 
                start ? ShellStatus.EXECUTE : ShellStatus.READY, false);
        ll.stream().forEach(l -> l.shellStatusChanged(e));
    }
    
    private void doStartAndFire(ShellSession nss) {
        synchronized (this) {
            this.shellSession = nss;
            starting = true;
        }
        Pair<ShellSession, Task> res = nss.start();
        nss.getModel().addConsoleListener(new ConsoleListener() {
            @Override
            public void executing(ConsoleEvent e) {
                fireExecuting(nss, e.isStart());
            }

            @Override
            public void sectionCreated(ConsoleEvent e) {}

            @Override
            public void sectionUpdated(ConsoleEvent e) {}

            @Override
            public void closed(ConsoleEvent e) {}
        });
        ShellSession previous = res.first();
        if (previous != null) {
            previous.removePropertyChangeListener(shellL);
        }
        nss.addPropertyChangeListener(shellL);
        ShellEvent event = new ShellEvent(this, nss, previous);
        fireShellStatus(event);
        
        res.second().addTaskListener(e -> {
            synchronized (this) {
                starting = false;
                if (shellSession != nss) {
                    return;
                }
            }
            if (nss.isValid() && nss.isActive()) {
                fireShellStarted(event);
            }
            fireShellStatus(event);
        });
        
    }
    
    public synchronized ShellStatus getStatus() {
        ShellSession session = this.shellSession;
        
        if (session == null) {
            return ShellStatus.INIT;
        }
        if (starting) {
            return ShellStatus.STARTING;
        } else if (closed) {
            return ShellStatus.SHUTDOWN;
        }
        if (session.getModel().isExecute()) {
            return ShellStatus.EXECUTE;
        } else if (session.isValid()) {
            return ShellStatus.READY;
        } else {
            return ShellStatus.DISCONNECTED;
        }
    }
    
    public ShellSession getSession() {
        return shellSession;
    }

    public ClasspathInfo getClasspathInfo() {
        return classpathInfo;
    }
    
    public FileObject getWorkRoot() {
        return workRoot;
    }
    
    public FileObject getConsoleFile() {
        return consoleFile;
    }
    
    private Document forceOpenDocument() throws IOException {
        EditorCookie cake = consoleFile.getLookup().lookup(EditorCookie.class);
        if (cake == null) {
            return null;
        }
        Document d =  cake.openDocument();
        synchronized (this) {
            this.document = d;
        }
        return d;
    }

    public synchronized Document getConsoleDocument() {
        return document;
    }
    
    public ClassPath getSnippetClassPath() {
        return snippetClassPath;
    }
    
    public ClassPath getUserLibraryPath() {
        return userLibraryPath;
    }
    
    /**
     * Must be called on JShell shutdown to clean up resources. Should
     * be called after all 
     */
    public Task shutdown() throws IOException {
        Task t = shellSession.closeSession();
        t.addTaskListener((e) -> {
            postCloseCleanup();
        });
        return t;
    }
    
    private void postCloseCleanup() {
        try {
            // try to close the dataobject
            DataObject d = DataObject.find(getConsoleFile());
            EditorCookie cake = d.getLookup().lookup(EditorCookie.class);
            cake.close();
            // discard the dataobject
            synchronized (this) {
                if (document == null) {
                    return;
                }
                document = null;
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        if (controlsIO) {
            inputOutput.closeInputOutput();
        }
        ShellRegistry.get().closed(this);
    }
    
    public void open() throws IOException {
        assert workRoot != null;
        DataObject d = DataObject.find(getConsoleFile());
        EditorCookie cake = d.getLookup().lookup(EditorCookie.class);
        // force open
        if (shellSession == null) {
            start();
            cake.open();
        } else {
            cake.open();
            document = cake.openDocument();
            return;
        }
        if (inputOutput != null) {
            inputOutput.select();
        }
        EditorCookie.Observable oo = d.getLookup().lookup(EditorCookie.Observable.class);
        assert oo != null;
        oo.addPropertyChangeListener((e) -> {
            if (EditorCookie.Observable.PROP_OPENED_PANES.equals(e.getPropertyName())) {
                if (cake.getOpenedPanes() == null) {
                    try {
                        shutdown();
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
                
            }
        });
        inputOutput.select();
    }
    
    public void notifyDisconnected(ShellSession old, boolean remoteClose) {
        List<ShellListener> ll;
        ShellSession s;
        synchronized (this) {
            s = this.shellSession;
            if (s == null || closed) {
                return;
            }
            ll = new ArrayList<>(shellListeners);
        }
        old.notifyClosed(this, remoteClose);
        ShellEvent e = new ShellEvent(this, s, ShellStatus.DISCONNECTED, remoteClose);
        ll.stream().forEach(l -> l.shellStatusChanged(e));
    }
    
    protected void notifyShutdown(boolean remote) {
        List<ShellListener> ll;
        ShellSession s;
        
        synchronized (this) {
            if (closed) {
                return;
            }
            closed = true;
            ll = new ArrayList<>(shellListeners);
            s = this.shellSession;
        }
        if (s != null) {
            s.notifyClosed(this, remote);
        }
        ShellEvent e = new ShellEvent(this);
        ll.stream().forEach(l -> l.shellShutdown(e));
        if (controlsIO && inputOutput != null) {
            try {
                inputOutput.getIn().close();
                inputOutput.getOut().close();
                inputOutput.getErr().close();
            } catch (IOException ex) {
                // expected
            }
        }
    }
    
    public boolean isClosed() {
        return closed;
    }
    
    public void addShellListener(ShellListener l) {
        synchronized (this) {
            this.shellListeners.add(l);
            if (!closed) {
                return;
            }
        }
        // notify the listener as soon as it is registered, since we're closed already.
        l.shellShutdown(new ShellEvent(this));
    }
    
    public synchronized void removeShellListener(ShellListener l) {
        this.shellListeners.remove(l);
    }
    
    public ExecutionControlProvider createExecutionEnv() {
        return null;
    }
    
    public boolean closeDeadEditor() {
        if (getStatus() == ShellStatus.SHUTDOWN) {
            return closeEditor();
        } else {
            return false;
        }
    }
    
    public boolean closeEditor() {
        EditorCookie cake = getConsoleFile().getLookup().lookup(EditorCookie.class);
        if (cake == null) {
            return true;
        }
        return cake.close();
    }
    
    public String getMode() {
        return "launch"; // NOI18N
    }
    
    public JShell getShell() {
        ShellSession s = shellSession;
        return s == null ? null : s.getShell();
    }
    
    private class ShellL implements PropertyChangeListener {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (ShellSession.PROP_ENGINE.equals(evt.getPropertyName())) {
                ShellSession s = shellSession;
                if (s != null && s.isValid() && s.isActive()) {
                    ShellEvent ev = new ShellEvent(JShellEnvironment.this, shellSession, shellSession);
                    fireShellStarted(ev);
                }
            }
        }
        
    }
}
