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
package org.netbeans.modules.jshell.support;

import com.sun.jdi.connect.VMStartException;
import java.beans.PropertyChangeListener;
import org.netbeans.modules.jshell.tool.JShellLauncher;
import org.netbeans.modules.jshell.tool.JShellTool;
import org.netbeans.modules.jshell.parsing.ModelAccessor;
import org.netbeans.modules.jshell.parsing.LexerEmbeddingAdapter;
import org.netbeans.modules.jshell.model.Rng;
import org.netbeans.modules.jshell.model.ConsoleSection;
import org.netbeans.modules.jshell.model.ConsoleListener;
import org.netbeans.modules.jshell.model.ConsoleModel;
import org.netbeans.modules.jshell.model.ConsoleEvent;
import java.beans.PropertyChangeSupport;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Writer;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Position;
import javax.swing.text.StyledDocument;
import javax.tools.StandardJavaFileManager;
import org.netbeans.lib.nbjshell.RemoteJShellService;
import jdk.jshell.JShell;
import jdk.jshell.JShell.Subscription;
import org.netbeans.lib.nbjshell.JShellAccessor;
import jdk.jshell.Snippet;
import jdk.jshell.SnippetEvent;
import jdk.jshell.SourceCodeAnalysis;
import jdk.jshell.spi.ExecutionControl.ExecutionControlException;
import jdk.jshell.spi.ExecutionControlProvider;
import org.netbeans.api.editor.document.AtomicLockDocument;
import org.netbeans.api.editor.document.LineDocument;
import org.netbeans.api.editor.document.LineDocumentUtils;
import org.netbeans.api.editor.guards.GuardedSection;
import org.netbeans.api.editor.guards.GuardedSectionManager;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.ClasspathInfo.PathKind;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.netbeans.lib.nbjshell.NbExecutionControl;
import org.netbeans.modules.jshell.env.JShellEnvironment;
import org.netbeans.modules.jshell.model.ConsoleContents;
import org.netbeans.modules.jshell.model.SnippetHandle;
import org.netbeans.modules.jshell.parsing.ShellAccessBridge;
import org.netbeans.modules.jshell.parsing.SnippetRegistry;
import org.netbeans.modules.jshell.project.ShellProjectUtils;
import org.netbeans.modules.jshell.support.ShellHistory.Item;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.api.indexing.IndexingManager;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.spi.editor.guards.GuardedEditorSupport;
import org.netbeans.spi.editor.guards.support.AbstractGuardedSectionsProvider;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.modules.InstalledFileLocator;
import org.openide.modules.SpecificationVersion;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.Pair;
import org.openide.util.RequestProcessor;
import org.openide.util.Task;

import static org.netbeans.modules.jshell.tool.JShellLauncher.quote;

/**
 * The root object for any JShell session. A shell session consists of:
 * <ul>
 * <li>a Document, viewed by an editor
 * <li>an output window, where the JShell prints the results
 * <li>the JShell instance itself
 * <li>a process handle to the running target VM
 * <li>transient FileSystem where the sources for parsing are created
 * <li>ConsoleModel which divides the Document and maps JShell snippets to
 * document parts.
 * </ul>
 * <p/>
 * <b>Threading model:</b> The JShell executes single-threaded, so <b>all accesses</b>
 * to JShell must be serialized using {@link #post(java.lang.Runnable)}. The getter
 * for JShell asserts when accessed from outside such Runnable - do not abuse.
 * <p/>
 *
 * @author sdedic
 */
public class ShellSession  {
    private static final Logger LOG = Logger.getLogger(ShellSession.class.getName());
    
    public static final String PROP_ACTIVE = "active";
    public static final String PROP_ENGINE = "active";

    /**
     * Work root, contains console file and snippet files
     */
    private final FileObject      workRoot;

    /**
     * The document being operated upon
     */
    private final Document  consoleDocument;
    
    /**
     * The java platform and projectInfo may possibly change.
     */
    private final JavaPlatform    platform;
    
    /**
     * ClasspathInfo as set up by the Project
     */
    private final ClasspathInfo   projectInfo;
    
    private ClasspathInfo cpInfo;
    
    /**
     * The executing JShell
     */
    private JShell shell;
    
    private ConsoleModel    model;
    
    /**
     * The shell output stream, possibly null.
     * Will be initialized during startup
     */
    private PrintStream shellControlOutput;
    
    private String  displayName;

    /**
     * True, if the operation of Shell is closed. Closed ShellSession
     * should not receive anything from the JShell. Detached ShellSession
     * MAY receive something, but should not reflect it in the document.
     */
    private volatile boolean ignoreClose;
    
    private FileObject consoleFile;
    private JShellEnvironment env;
    private final FileSystem  editorSnippetsFileSystem;
    private final FileObject editorWorkRoot;
    
    private volatile Set<Snippet>    initialSetupSnippets = Collections.emptySet();

    /**
     * True, if the Session was detached from the document. Another session
     * now 'owns' the document. The flag cannot be reset back to false.
     */
    private volatile boolean detached;
    
    private static final RequestProcessor FORCE_CLOSE_RP = new RequestProcessor("JShell socket closer");
    
    /**
     * Mapps snippets to the timestamps of their snippet files. Only valid snippets will
     */
    private final Map<Snippet, Long>    snippetTimeStamps = new WeakHashMap<>();

    private final PropertyChangeSupport propSupport = new PropertyChangeSupport(this);
    
    // @GuardedBy(this)
    private SnippetRegistry     snippetRegistry;
        
    public ShellSession(JShellEnvironment env) {
        this(env.getDisplayName(), 
             env.getConsoleDocument(), 
             env.getClasspathInfo(),
             env.getPlatform(),
             env.getWorkRoot(), 
             env.getConsoleFile());
        this.env = env;
    }
    
    public JShellEnvironment getEnv() {
        return env;
    }

    private ShellSession(String displayName, Document doc, ClasspathInfo cpInfo, 
            JavaPlatform platform, FileObject workRoot, FileObject consoleFile) {
        this.consoleDocument = doc;
        this.projectInfo = cpInfo;
        this.displayName = displayName;
        this.platform = platform;
        this.consoleFile = consoleFile;
        this.workRoot = workRoot;
        
        this.editorSnippetsFileSystem = FileUtil.createMemoryFileSystem();
        this.editorWorkRoot = editorSnippetsFileSystem.getRoot();
        this.shellControlOutput = new PrintStream(
            new WriterOutputStream(
                    // delegate to whatever Writer will be set
                    new Writer() {
                        @Override
                        public void write(char[] cbuf, int off, int len) throws IOException {
                            documentWriter.write(cbuf, off, len);
                        }

                        @Override
                        public void flush() throws IOException {
                            documentWriter.flush();
                        }

                        @Override
                        public void close() throws IOException {
                            documentWriter.close();
                        }

                    })
        );
    }
    
    public void addPropertyChangeListener(PropertyChangeListener pcl) {
        propSupport.addPropertyChangeListener(pcl);
    }
    
    public void removePropertyChangeListener(PropertyChangeListener pcl) {
        propSupport.removePropertyChangeListener(pcl);
    }
    
    public boolean isActive() {
        return !detached;
    }

    private Task detach() {
        Task t;
        synchronized (allSessions) {
            Reference<ShellSession> refS = allSessions.get(consoleDocument);
            if (refS != null && refS.get() == this) {
                allSessions.remove(consoleDocument);
                detached = true;
            } else {
                return Task.EMPTY;
            }
        }
//        closed = true;
        model.detach();
        closed();
        if (exec != null) {
            FORCE_CLOSE_RP.post(this::forceCloseStreams, 300);
        }
        // leave the model
        gsm.getGuardedSections().forEach((GuardedSection gs) -> gs.removeSection());
        return sendJShellClose();
    }
    
    private synchronized void forceCloseStreams() {
        if (exec != null) {
            exec.closeStreams();
        }
    }

    JShell getJShell() {
        assert evaluator.isRequestProcessorThread();
        if (shell == null) {
            initJShell();
        }
        return shell;
    }
    
    public FileObject getConsoleFile() {
        return consoleFile;
    }
    
    public boolean isValid() {
        Launcher l = this.launcher;
        return l != null && l.isLive() && !detached;
    }
    
    /**
     * Writer implementation over the document. Informs the ConsoleModel,
     * that an event worth parsing is coming.
     */
    private class DocumentOutput extends Writer {
        private Throwable exception;
        
        @Override
        public void write(char[] cbuf, int off, int len) throws IOException {
            if (!isActive()) {
                // do not write from a closed JShell
                return;
            }
            AtomicLockDocument ald = LineDocumentUtils.asRequired(consoleDocument, AtomicLockDocument.class);
            try {
                ald.runAtomic(()-> {
                    try {
                        int offset = consoleDocument.getLength();
                        model.insertResponseString(offset,
                                String.copyValueOf(cbuf, off, len), null);
                    } catch (BadLocationException ex) {
                        exception = ex;
                    }
                });
                if (exception != null) {
                    throw new IOException(exception);
                }
            } finally {
                exception = null;
            }
        }

        @Override
        public void flush() throws IOException {
        }

        @Override
        public void close() throws IOException {
            // FIXME: the JShell session should be terminated.
        }
    }
    
    /**
     * Creates a snippet file. If the `editedSnippetIndex' is not >= 0, it generates
     * a file NOT seen by repository updater; if the edited section contains multiple
     * snippets, then it is possible to give the snippet explicit name.
     * 
     * @param snippet
     * @param editedSnippetIndex
     * @return 
     */
    public FileObject   snippetFile(SnippetHandle snippet, int editedSnippetIndex) {
        if (launcher == null) {
            return null;
        }
        return snippetRegistry.snippetFile(snippet, editedSnippetIndex);
    }
    
    public synchronized JShellTool   getJShellTool() {
        return launcher;
    }
    
    /**
     * Stream backed by a Writer. Uses UTF-8 to decode characters from the stream.
     */
    private static class WriterOutputStream extends OutputStream {
        private boolean writeImmediately = true;
        
        private final CharsetDecoder decoder;
        private final ByteBuffer decoderIn = ByteBuffer.allocate(128);
        private final CharBuffer decoderOut;
        private final Writer writer;
        
        public WriterOutputStream(Writer out) {
            this.writer = out;
            this.decoder = StandardCharsets.UTF_8.
                    newDecoder().
                    onMalformedInput(CodingErrorAction.REPLACE).
                    onUnmappableCharacter(CodingErrorAction.REPLACE).
                    replaceWith("?"); //NOI18N
            this.decoderOut = CharBuffer.allocate(2048);
        }
        
        @Override
        public void write(int b) throws IOException {
            decoderIn.put((byte)b);
            processInput(false);
            if (writeImmediately) {
                flushOutput();
            }
        }
        
        public void write(final byte[] b, int off, int len) throws IOException {
            while (len > 0) {
                final int c = Math.min(len, decoderIn.remaining());
                decoderIn.put(b, off, c);
                processInput(false);
                len -= c;
                off += c;
            }
            if (writeImmediately) {
                flushOutput();
            }
        }
        
        private void flushOutput() throws IOException {
            if (decoderOut.position() > 0) {
                writer.write(decoderOut.array(), 0, decoderOut.position());
                decoderOut.rewind();
            }
        }

        @Override
        public void close() throws IOException {
            processInput(true);
            flushOutput();
            writer.close();
        }

        @Override
        public void flush() throws IOException {
            flushOutput();
            writer.flush();
        }        
        
        private void processInput(final boolean endOfInput) throws IOException {
            // Prepare decoderIn for reading
            decoderIn.flip();
            CoderResult coderResult;
            while (true) {
                coderResult = decoder.decode(decoderIn, decoderOut, endOfInput);
                if (coderResult.isOverflow()) {
                    flushOutput();
                } else if (coderResult.isUnderflow()) {
                    break;
                } else {
                    // The decoder is configured to replace malformed input and unmappable characters,
                    // so we should not get here.
                    throw new IOException("Unexpected coder result"); //NOI18N
                }
            }
            // Discard the bytes that have been read
            decoderIn.compact();
        }
    }

    private volatile Launcher launcher;
    
    public Pair<ShellSession, Task> start() {
        ShellSession previous  = null;
        
        synchronized (allSessions) {
            Reference<ShellSession> sr = allSessions.get(env.getConsoleDocument());
            ShellSession s = null;
            
            if (sr != null) {
                previous = sr.get();
            }
        }
        if (previous != null) {
            previous.detach();
            AtomicLockDocument ald = LineDocumentUtils.asRequired(consoleDocument, AtomicLockDocument.class);
            ald.runAtomic(() -> {
                try {
                    consoleDocument.remove(0, consoleDocument.getLength());
                } catch (BadLocationException ex) {
                    Exceptions.printStackTrace(ex);
                }
            });
        }
        init(previous);
        try {
            refreshGuardedSection();
        } catch (BadLocationException ex) {
        }
        synchronized (allSessions) {
            allSessions.put(consoleDocument, new WeakReference<>(this));
        }
        GlobalPathRegistry.getDefault().register(ClassPath.SOURCE, new ClassPath[] { 
            env.getSnippetClassPath()
        });
        GlobalPathRegistry.getDefault().register(ClassPath.COMPILE, new ClassPath[] { 
            env.getUserLibraryPath()
        });

        return Pair.of(previous, evaluator.post(() -> {

            ModelAccessor.INSTANCE.execute(model, false, () -> {
                try {
                    URL url = URLMapper.findURL(workRoot, URLMapper.INTERNAL);
                    IndexingManager.getDefault().refreshIndexAndWait(url, null, true);
                    getJShell();
                } catch (Exception ex) {
                    LOG.log(Level.FINE, "Thrown error: ", ex);
                    reportErrorMessage(ex);
                } finally {
                    ensureInputSectionAvailable();
                }
            }, this::getPromptAfterError);
        }));
    }
    
    /**
     * Writer to the console document.
     */
    private Writer documentWriter = new DocumentOutput();

    public Writer getDocumentWriter() {
        return documentWriter;
    }
    
    private RemoteJShellService exec;
    
    private String addRoots(String prev, ClassPath cp) {
        if (cp == null) {
            return prev;
        }
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
    
    private void setupJShellClasspath(JShell jshell) throws ExecutionControlException {
        ClassPath compile = getEnv().getCompilerClasspath();
        String cp = addRoots("", compile);
        JShellAccessor.resetCompileClasspath(jshell, cp);
    }
    
    private boolean initializing;
    
    private String createProjectClasspath() {
        //ClassPath bcp = getClasspathInfo().getClassPath(PathKind.BOOT);
        ClassPath compile = getClasspathInfo().getClassPath(PathKind.COMPILE);
        ClassPath source = getClasspathInfo().getClassPath(PathKind.SOURCE);
        
        String cp = addRoots("", compile);
        //cp = addRoots(cp, compile);
        return cp;
    }

    /**
     * Finds appropriate source (language) version for the environment.
     * @return 
     */
    private SpecificationVersion findSourceVersion() {
        return env.getSourceLevel();
    }
    
    private SpecificationVersion findTargetVersion() {
        return env.getTargetLevel();
    }
    
    private Preferences createShellPreferences() {
        Project p = env.getProject();
        if (p != null) {
            return ProjectUtils.getPreferences(p, ShellSession.class, false).node("jshell");
        } else {
            return NbPreferences.forModule(ShellSession.class).node("jshell");
        }
    }
    
    private class Launcher extends JShellLauncher implements Consumer<SnippetEvent> {
        Subscription subscription;
        
        public Launcher(ExecutionControlProvider execEnv) throws IOException {
            super(
                createShellPreferences(),
                shellControlOutput, 
                shellControlOutput, 
                env.getInputStream(),
                env.getOutputStream(),
                env.getErrorStream(),
                execEnv);
        }

        @Override
        protected List<String>  historyItems() {
            return ShellSession.this.historyItems().stream().map((i) -> i.getContents()).collect(Collectors.toList());
        }

        @Override
        protected JShell.Builder makeBuilder() {
            return customizeBuilder(super.makeBuilder());
        }

        @Override
        protected JShell createJShellInstance() {
            JShell shell = super.createJShellInstance();
            try {
                setupJShellClasspath(shell);
            } catch (ExecutionControlException ex) {
                Exceptions.printStackTrace(ex);
            }
            synchronized (ShellSession.this) {
                snippetRegistry = new SnippetRegistry(
                        shell, bridgeImpl, workRoot, editorWorkRoot, 
                        snippetRegistry);
                // replace for fresh instance !
                JShell oldShell = ShellSession.this.shell;
                ShellSession.this.shell = shell;
                if (oldShell != null) {
                    FORCE_CLOSE_RP.post(() -> {
                        propSupport.firePropertyChange(PROP_ENGINE, oldShell, shell);
                    });
                }
            }
            this.subscription = shell.onSnippetEvent(this);
            // it's possible that the shell's startup will terminate the session
            if (!detached) {
                shell.onShutdown(sh -> closedDelayed());
                ignoreClose = false;
            }
            return shell;
        }

        @Override
        public void accept(SnippetEvent e) {
            SnippetHandle handle = snippetRegistry.installSnippet(
                e.snippet(), null, 0, true);
            // create an indexed file for the snippet.
            snippetRegistry.snippetFile(handle, 0);
        }

        @Override
        protected void classpathAdded(String arg) {
            super.classpathAdded(arg);
            File f = new File(arg);
            FileObject fob = FileUtil.toFileObject(f);
            if (fob != null) {
                env.appendClassPath(fob);
            }
        }

        @Override
        protected Path toPathResolvingUserHome(String pathString) {
            Path homeResolvedPath = super.toPathResolvingUserHome(pathString);
            
            if (!homeResolvedPath.isAbsolute()) {
                // prepend project's directory
                Project p = env.getProject();
                if (p != null) {
                    File f = FileUtil.toFile(p.getProjectDirectory());
                    if (f == null) {
                        return homeResolvedPath;
                    }
                    Path projectPath = f.toPath();
                    return projectPath.resolve(homeResolvedPath);
                }
            }
            return homeResolvedPath;
        }

        @Override
        protected NbExecutionControl execControlCreated(NbExecutionControl ctrl) {
            if (ctrl instanceof RemoteJShellService) {
                exec = (RemoteJShellService)ctrl;
            }
            return super.execControlCreated(ctrl);
        }
        
        
    }
    
    private SwitchingJavaFileManger fileman;
    
    private JShell.Builder customizeBuilder(JShell.Builder b) {
        SpecificationVersion v = findSourceVersion();
        if (v != null) {
            b.compilerOptions("-source", v.toString()); // NOI18N
        }
        v = findTargetVersion();
        if (v != null) {
            b.compilerOptions("-target", v.toString()); // NOI18N
        }
        b.remoteVMOptions("-classpath", quote(createClasspathString())); // NOI18N
        ClasspathInfo cpI = getClasspathInfo();
        if (LOG.isLoggable(Level.FINEST)) {
            StringBuilder sb = new StringBuilder("Starting jshell with ClasspathInfo:");
            for (PathKind kind : PathKind.values()) {
                if (kind == PathKind.OUTPUT) {
                    continue;
                }
                sb.append(kind);
                sb.append(": ");
                ClassPath cp;
                try {
                    cp = cpI.getClassPath(kind);
                } catch (IllegalArgumentException ex) {
                    sb.append("<not supported>\n");
                    continue;
                }
                if (cp == null) {
                    sb.append("<null>\n");
                    continue;
                }
                sb.append("\n");
                for (ClassPath.Entry e : cp.entries()) {
                    sb.append("\t");
                    sb.append(e.getURL());
                    sb.append("\n");
                }
                sb.append("---------------\n");
                LOG.log(Level.FINEST, sb.toString());
            }
        }
        customizeBuilderOnJDK9(b);
        b.fileManager((Function)this::createJShellFileManager);
        return getEnv().customizeJShell(b);
    }

    /**
     * In case the JFM comes from JDK9 runtime, use reflection to parametrize the
     * JFM
     * @return the original object
     */
    private Object createJShellFileManager(Object original) {
        if (original instanceof StandardJavaFileManager) {
            return fileman = new SwitchingJavaFileManger(getClasspathInfo());
        } else {
            return original;
        }
    }
    
    private void customizeBuilderOnJDK9(JShell.Builder builder) {
        try {
            Class c = JShell.Builder.class.getClassLoader().loadClass("javax.tools.StandardJavaFileManager");
            if (c.isAssignableFrom(StandardJavaFileManager.class)) {
                return;
            }
        } catch (ClassNotFoundException ex) {
        }
        fileman = null;
        Project p = env.getProject();
        
        String systemHome = platform.getSystemProperties().get("java.home"); // NOI18N
        if (systemHome != null) {
            if (ShellProjectUtils.isModularJDK(platform)) {
                builder.compilerOptions("--system", systemHome); // NOI18N
            } else {
                builder.compilerOptions("--boot-class-path", getClasspathAsString(PathKind.BOOT));
            }
        }

        String classpath;
        String modulepath = "";
        
        if (p != null && ShellProjectUtils.isModularProject(p)) {
            List<String[]> opts = ShellProjectUtils.compilerPathOptions(p);
            for (String[] o : opts) {
                if (o[1] != null) {
                    builder.compilerOptions(o[0], o[1]);
                } else {
                    builder.compilerOptions(o[0]);
                }
            }
            modulepath = getClasspathAsString(PathKind.MODULE_COMPILE);
            classpath = getClasspathAsString(PathKind.MODULE_CLASS);
        } else {
            classpath = getClasspathAsString(PathKind.COMPILE);
        }
        
        if (!classpath.isEmpty()) {
            builder.compilerOptions("-classpath", classpath); // NOI18N
        }
        if (!modulepath.isEmpty()) {
            builder.compilerOptions("--module-path", modulepath); // NOI18N
        }
    }
    
    private synchronized Launcher initShellLauncher() throws IOException {
        if (launcher != null) {
            return launcher;
        }
        try {
            launcher = new Launcher(env.createExecutionEnv());
        } catch (IOException | RuntimeException | Error e) {
            LOG.log(Level.INFO, null, e);
        }
        return launcher;
    }
    
    private void initJShell() {
        if (shell != null) {
            return;
        }
        Launcher l = null;
        JShell shell = null;
        Subscription sub = null;
        try {
            initializing = true;
            l = initShellLauncher();
            shell = launcher.getJShell();
            // not necessary to launch  the shell, but WILL display the initial prompt
            launcher.start();
            initialSetupSnippets = new HashSet<>(shell.snippets().collect(Collectors.toList()));
        } catch (IOException | InternalError err) {
            Throwable t = err.getCause();
            if (t == null) {
                t = err;
            }
            reportErrorMessage(t);
            closed();
            env.notifyDisconnected(this, false);
            return;
        } finally {
            initializing = false;
            if (l != null && l.subscription != null && shell != null) {
                shell.unsubscribe(l.subscription);
            }
        }
    }
    
    private final ShellAccessBridge bridgeImpl = new ShellAccessBridge() {
        @Override
        public <T> T execute(Callable<T> xcode) throws Exception {
            if (fileman == null || evaluator.isRequestProcessorThread()) {
                return xcode.call();
            } else {
                return fileman.withLocalManager(xcode);
            }
        }

        @Override
        public SourceCodeAnalysis.CompletionInfo analyzeInput(String input) {
            try {
                return execute(() -> ensureShell().sourceCodeAnalysis().analyzeCompletion(input));
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }

        @Override
        public boolean isInitialized() {
            return shell != null;
        }
        
    };
    
    public synchronized SnippetRegistry getSnippetRegistry() {
        return snippetRegistry;
    }

    @NbBundle.Messages({
            "# {0} - the original exception reason",
            "ERR_CannotInitializeShell=Could not initialize JShell: {0}",
            "# {0} - original error reason",
            "ERR_CannotInitializeBecause=\n\tcaused by: {0}",
    })
    private String buildErrorMessage(Throwable t) {
        String locMessage = t.getLocalizedMessage();
        if (locMessage == null) {
            locMessage = t.getClass().getName();
        }
        if (t.getCause() == t) {
            return Bundle.ERR_CannotInitializeShell(locMessage);
        }
        StringBuilder sb = new StringBuilder(Bundle.ERR_CannotInitializeShell(locMessage));
        while (t.getCause() != t) {
            t = t.getCause();
            if (t == null) {
                break;
            }
            locMessage = t.getLocalizedMessage();
            if (locMessage == null) {
                locMessage = t.getClass().getName();
            }
            sb.append(Bundle.ERR_CannotInitializeBecause(t.getLocalizedMessage()));
        }
        return sb.toString();
    }

    private void ensureInputSectionAvailable() {
        ConsoleSection s = model.processInputSection(false);
        if (s != null) {
            return;
        }
        String promptText = "\n" + launcher.prompt(false); // NOI18N
        writeToShellDocument(promptText);
    }
    
    @NbBundle.Messages({
        "MSG_JShellClosed=The Java Shell VM has closed the connection. You may use shell slash-commands, or re-run the process.",
        "MSG_JShellCannotStart=The Java Shell VM is not reachable. You may only use shell commands, or re-run the target process.",
        "MSG_JShellDisconnected=The remote Java Shell has terminated. Restart the Java Shell to continue"
    })
    public void notifyClosed(JShellEnvironment env, boolean remote) {
        synchronized (this) {
            if (ignoreClose) {
                return;
            }
            ignoreClose = true;
        }
        String s;
        if (initializing) {
            s = Bundle.MSG_JShellCannotStart();
        } else if (!remote) {
            // somewhat expected, do not report
            return;
        } else if (env.isClosed()) {
            s = Bundle.MSG_JShellClosed();
        } else {
            s = Bundle.MSG_JShellDisconnected();
        }
        reportShellMessage(s);
    }

    @NbBundle.Messages({
        "MSG_JShell_Start_STDOUT=Standard output:",
        "MSG_JShell_Start_STDERR=Error output:"
    })
    public void reportErrorMessage(Throwable t) {
        StringBuilder processOutput = null;
        VMStartException startException = findVMStartException(t);
        if(startException != null) {
            processOutput = new StringBuilder();
            // VMStartException is raised when target VM fails to start. At time
            // of writing com.sun.tools.jdi.AbstractLauncher does not read the
            // process output, so diagnostic output is lost, try to recover it.
            assert !startException.process().isAlive() : "Process expected to be not alive"; // NOI18N
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try {
                startException.process().getInputStream().transferTo(baos);
            } catch (IOException ex) {}
            processOutput.append(Bundle.MSG_JShell_Start_STDOUT());
            processOutput.append("\n");
            processOutput.append(baos.toString(StandardCharsets.UTF_8));
            baos.reset();
            processOutput.append(Bundle.MSG_JShell_Start_STDERR());
            try {
                startException.process().getErrorStream().transferTo(baos);
            } catch (IOException ex) {}
            processOutput.append(baos.toString(StandardCharsets.UTF_8));
        }
        if(processOutput == null) {
            LOG.log(Level.INFO, "Error in JSHell", t); // NOI18N
            reportShellMessage(buildErrorMessage(t));
        } else {
            LOG.log(Level.INFO, "Error in JSHell\n" + processOutput.toString(), t); // NOI18N
            reportShellMessage(buildErrorMessage(t) + "\n\n" + processOutput.toString()); // NOI18N
        }
    }
    
    private void reportShellMessage(String msg) {
        if (!isActive()) {
            return;
        }
        StringBuilder sb = new StringBuilder();
        if (!scrollbackEndsWithNewline()) {
            sb.append("\n");
        }
        sb.append("|  ");
        if (msg.endsWith("\n")) {
            msg = msg.substring(0, msg.length() - 1);
        }
        sb.append(msg.replace("\n", "\n|  ")); // NOI18N
        sb.append("\n"); // NOI18N
        writeToShellDocument(sb.toString());
    }

    private static VMStartException findVMStartException(Throwable thrw) {
        Throwable curr = thrw;
        while (true) {
            if(curr instanceof VMStartException) {
                return (VMStartException) curr;
            } else if (curr == null) {
                return null;
            }
            curr = curr.getCause();
        }
    }

    private boolean scrollbackEndsWithNewline() {
        boolean[] ret = new boolean[1];
        ConsoleSection s = model.getInputSection();
        int end = s == null ? -1 : s.getStart();
        consoleDocument.render(() -> {
            int l = consoleDocument.getLength();
            if (l == 0) {
                ret[0] = true;
            } else {
                int e = end == -1 ? consoleDocument.getLength() : end;
                try {
                    ret[0] = consoleDocument.getText(e - 1, 1).charAt(0) == '\n';
                } catch (BadLocationException ex) {
                    ret[0] = false;
                }
            }
        });
        return ret[0];
    }
    
    private void writeToShellDocument(String text) {
        model.writeToShellDocument(text);
    }
    
    private final Set<Snippet> excludedSnippets = new HashSet<>();
    
    private String getClasspathAsString(PathKind pk) {
        return addRoots("", getClasspathInfo().getClassPath(pk));
    }
    
    private String createClasspathString() {
        String sep = System.getProperty("path.separator");
        boolean modular = ShellProjectUtils.isModularJDK(platform);
        String agentJar = 
                modular ?
                    "modules/ext/nb-mod-jshell-probe.jar" :
                    "modules/ext/nb-custom-jshell-probe.jar";
        
                
        File remoteProbeJar = InstalledFileLocator.getDefault().locate(agentJar, 
                "org.netbeans.lib.jshell.agent", false);
        StringBuilder sb = new StringBuilder(remoteProbeJar.getAbsolutePath());
        
        if (!modular) {
//            File replJar = 
//                    InstalledFileLocator.getDefault().locate(
//                            "modules/ext/nb-jshell.jar", 
//                            "org.netbeans.libs.jshell", false);
//            sb.append(sep).append(replJar.getAbsolutePath());

            File toolsJar = null;
            for (FileObject jdkInstallDir : platform.getInstallFolders()) {
                FileObject toolsJarFO = jdkInstallDir.getFileObject("lib/tools.jar");

                if (toolsJarFO == null) {
                    toolsJarFO = jdkInstallDir.getFileObject("../lib/tools.jar");
                }
                if (toolsJarFO != null) {
                    toolsJar = FileUtil.toFile(toolsJarFO);
                    break;
                }
            }
            if (toolsJar != null) {
                sb.append(sep).append(toolsJar);
            }
        }
        
        // classpath construction
        
        ClassPath compile = getEnv().getVMClassPath();
        String projectCp = addRoots("", compile); // NOi18N
        sb.append(sep).append(projectCp);
        return sb.toString();
    }

    private void closed() {
        synchronized (this) {
            if (ignoreClose) {
                return;
            }
        }
        env.notifyDisconnected(this, false);
        propSupport.firePropertyChange(PROP_ACTIVE, true, false);
        
        // save the history
        ShellHistory h = env.getLookup().lookup(ShellHistory.class);
        if (h != null) {
            saveInputSections(h);
        }
    }
    
    private void saveInputSections(ShellHistory history) {
        history.pushItems(historyItems());
    }
    
    private void closedDelayed() {
        FORCE_CLOSE_RP.post(() -> { closed(); }, 300);
    }

    private synchronized void init(ShellSession prev) {
        ConsoleModel.initModel();
        evaluator = new RequestProcessor("Evaluator for " + displayName);
        initClasspath();
        model = ModelAccessor.INSTANCE.createModel((LineDocument)consoleDocument, evaluator, bridgeImpl);
        model.addConsoleListener(new LexerEmbeddingAdapter());
        model.addConsoleListener(new GuardedSectionUpdater());
        
        // missing API to create a GuardedSectionManager against a plain document:
        AbstractGuardedSectionsProvider hack = new AbstractGuardedSectionsProvider(
                new GuardedEditorSupport() {
            @Override
                    public StyledDocument getDocument() {
                        return (StyledDocument)consoleDocument;
                    }
                }
            ) {
            
            @Override
            public char[] writeSections(List<GuardedSection> sections, char[] content) {
                return null;
            }
            
            @Override
            public AbstractGuardedSectionsProvider.Result readSections(char[] content) {
                return null;
            }
        };
        // this crates and registers a GuardedSectionManager in the doc properties
        hack.createGuardedReader(new ByteArrayInputStream(new byte[0]), Charset.defaultCharset());
        gsm = GuardedSectionManager.getInstance((StyledDocument)consoleDocument);
        gsProvider = hack;
        
    }
    private AbstractGuardedSectionsProvider gsProvider;
    private GuardedSectionManager gsm;

    private void initClasspath() {
        ClasspathInfo.Builder bld = new ClasspathInfo.Builder(
                projectInfo.getClassPath(ClasspathInfo.PathKind.BOOT)
        );
        
        ClassPath snippetSource = ClassPathSupport.createProxyClassPath(
                projectInfo.getClassPath(PathKind.SOURCE),
                ClassPathSupport.createClassPath(editorWorkRoot),
                ClassPathSupport.createClassPath(workRoot)
        );
        
        ClassPath compileClasspath = projectInfo.getClassPath(PathKind.COMPILE);

        ClassPath modBoot = projectInfo.getClassPath(ClasspathInfo.PathKind.MODULE_BOOT);
        ClassPath modClass = projectInfo.getClassPath(ClasspathInfo.PathKind.MODULE_CLASS);
        ClassPath modCompile = projectInfo.getClassPath(ClasspathInfo.PathKind.MODULE_COMPILE);
        
        bld.
            setClassPath(compileClasspath).
            setSourcePath(snippetSource).
                
            setModuleBootPath(modBoot).
            setModuleClassPath(modClass).
            setModuleCompilePath(modCompile);
        /*
        this.cpInfo = ClasspathInfo.create(
                projectInfo.getClassPath(PathKind.BOOT),
                compileClasspath,
                snippetSource
        );
        */
        this.cpInfo = bld.build();
        this.consoleDocument.putProperty("java.classpathInfo", this.cpInfo);
    }

    public Document getConsoleDocument() {
        return consoleDocument;
    }

    public ClasspathInfo getClasspathInfo() {
        return cpInfo;
    }
    
    public JShell ensureShell() {
        initJShell();
        return shell;
    }

    public JShell getShell() {
        return shell;
    }

    public static ShellSession createSession(
            JShellEnvironment env) {
        return new ShellSession(env);
    }
    
    public static ShellSession get(FileObject f) {
        EditorCookie cake = f.getLookup().lookup(EditorCookie.class);
        if (cake == null) {
            return null;
        }
        Document d = cake.getDocument();
        if (d == null) {
            return null;
        }
        return get(d);
    }

    public static ShellSession get(Document d) {
        if (d == null) {
            return null;
        }
        synchronized (allSessions) {
            Reference<ShellSession> sr = allSessions.get(d);
            return sr == null ? null : sr.get();
        }
    }

    public static Collection<ShellSession> allSessions() {
        Collection<Reference<ShellSession>> ll;
        synchronized (allSessions) {
            ll = allSessions.values();
        }
        Collection<ShellSession> res = new ArrayList<>(ll.size());
        for (Iterator<Reference<ShellSession>> it = ll.iterator(); it.hasNext(); ) {
            Reference<ShellSession> sr = it.next();
            ShellSession s = sr.get();
            if (s != null) {
                res.add(s);
            }
        }
        return res;
    }
    
    private void addNewline(int offset) {
        AtomicLockDocument ald = LineDocumentUtils.asRequired(consoleDocument, AtomicLockDocument.class);
        ald.runAtomic(() -> {
            try {
                DocumentUtilities.setTypingModification(consoleDocument, false);
                consoleDocument.insertString(offset, "\n", null);
            } catch (BadLocationException ex) {
                
            }
        });
    }

    private void clearAndAddNewline(int offset) {
        AtomicLockDocument ald = LineDocumentUtils.asRequired(consoleDocument, AtomicLockDocument.class);
        ald.runAtomic(() -> {
            try {
                DocumentUtilities.setTypingModification(consoleDocument, false);
                consoleDocument.remove(offset, consoleDocument.getLength() - offset);
                consoleDocument.insertString(offset, "\n", null);
            } catch (BadLocationException ex) {
                
            }
        });
    }

    private boolean recordNoSave = false;
    
    private String executionLabel;

    public String getExecutionLabel() {
        return executionLabel;
    }
    
    public void evaluate(String command) throws IOException {
        evaluate(command, false, null);
    }
    
    public void clearInputAndEvaluateExternal(String command, String label) throws IOException {
        // evaluate even though the connection is not active; perhaps
        // we get access to the history.
        post(() -> {
            ConsoleSection s = model.processInputSection(true);
            if (s == null) {
                return;
            }
            clearAndAddNewline(s.getPartBegin());
            boolean saveSave = this.recordNoSave;
            try {
                recordNoSave = true;
                this.executionLabel = label;
                doExecuteCommands(command);
            } finally {
                this.executionLabel = null;
                this.recordNoSave = saveSave;
            }
        });
    }
    
    public void evaluate(String command, boolean excludeFromSave, String label) throws IOException {
        // evaluate even though the connection is not active; perhaps
        // we get access to the history.
        post(() -> {
            ConsoleSection s = model.processInputSection(true);
            if (s == null) {
                return;
            }
            String c = s.getContents(consoleDocument);
            if (!c.endsWith("\n")) { // NOI18N
                addNewline(s.getEnd());
            }
            boolean saveSave = this.recordNoSave;
            try {
                recordNoSave = excludeFromSave;
                this.executionLabel = label;
                doExecuteCommands(command);
            } finally {
                this.executionLabel = null;
                this.recordNoSave = saveSave;
            }
        });
    }

    /**
     * Executes commands in input buffer. Executes one by one, since some
     * snippets may be redundant and will not be reported at all by JShell, so
     * replacement of individual Snippets will be assisted by setting up a
     * start position of the to-be-executed snippet in the buffer.
     * <p/>
     * Since JShell stops executin after first error, a 'erroneous' flag is raised
     * by {@link #acceptSnippet} when it sees a REJECTED snippet (an error).
     */
    @NbBundle.Messages({
        "MSG_ErrorExecutingCommand=Note: You may need to restart the Java Shell to resume proper operation",
        "MSG_JShellCannotExecute=Java Shell cannot execute commands. Restart Java Shell or its host process."
    })
    private void doExecuteCommands(final String cmd) {
        ConsoleSection sec = model.processInputSection(true);
        if (sec == null) {
            return;
        }
        // rely on JShell's own parsing from the input section
        // just for case:
        ModelAccessor.INSTANCE.execute(model, cmd != null, () -> {
            Executor executor = new Executor(cmd, model.getExecutingSection());
            executor.execute();
        }, this::getPromptAfterError);
    }
    
    private class Executor implements Runnable, Consumer<SnippetEvent> {
        private final String          cmd;
        private final ConsoleSection  exec;
        
        private final List<String>    toExec = new ArrayList<>();
        private boolean         erroneous;
        private int             execOffset;

        public Executor(String cmd, ConsoleSection exec) {
            this.cmd = cmd;
            this.exec = exec;
        }
        
        private boolean isExternal() {
            return cmd != null;
        }
        
        @Override
        public void accept(SnippetEvent e) {
            switch (e.status()) {
                case REJECTED:
                    erroneous = true;
                case VALID:
                case RECOVERABLE_DEFINED:
                case RECOVERABLE_NOT_DEFINED:
                case NONEXISTENT:
                if (recordNoSave) {
                    excludedSnippets.add(e.snippet());
                }

                // register in the registry:
                SnippetHandle handle;
                
                if (isExternal()) {
                    handle = snippetRegistry.installSnippet(
                        e.snippet(), null, 0, true);
                } else {
                    handle = snippetRegistry.installSnippet(
                        e.snippet(), exec, execOffset, false);
                }
                // create an indexed file for the snippet.
                snippetRegistry.snippetFile(handle, 0);
            }
        }
        
        @Override
        public void run() {
            if (exec.getType() == ConsoleSection.Type.COMMAND) {
                toExec.add(exec.getContents(consoleDocument));
            } else {
                for (Rng r : exec.getAllSnippetBounds()) {
                    toExec.add(exec.getRangeContents(consoleDocument, r));
                }
            }
        }
        
        void execute() {
            try {
                if (cmd != null) {
                    toExec.add(cmd);
                } else {
                    // fill toExec
                    consoleDocument.render(this);
                }
                if (toExec.isEmpty()) {
                    return;
                }
                Rng[] ranges = cmd == null ? exec.getAllSnippetBounds() : null;
                int index = 0;
                execOffset = 0;
                Subscription sub = null;
                JShell sh = null;
                try {
                    for (String s : toExec) {
                            launcher.ensureLive();
                            if (!launcher.isLive()) {
                                RemoteJShellService ec = ShellSession.this.exec;
                                if (ec != null) {
                                    ExecutionControlException ee = 
                                            ec.getBrokenException();
                                    if (ee != null) {
                                        throw ee;
                                    }
                                }
                                break;
                            }
                            sh = launcher.getJShell();
                            if (sub == null) {
                                String t = s.trim();
                                if (!t.isEmpty() && t.charAt(0) != '/') { // shell commands
                                    sub = sh.onSnippetEvent(this);
                                }
                            }
                            if (ranges != null) {
                                execOffset = exec.offsetToContents(ranges[index].start, true);
                            }
                            launcher.evaluate(s, index == toExec.size() - 1);
                            if (erroneous) {
                                break;
                            }
                        index++;
                    }
                } catch (IllegalStateException | ExecutionControlException ex) {
                    reportShellMessage(Bundle.MSG_JShellCannotExecute());
                } catch (RuntimeException | IOException ex) {
                    reportErrorMessage(ex);
                    reportShellMessage(Bundle.MSG_ErrorExecutingCommand());
                } finally {
                    if (sh != null && sub != null) {
                        sh.unsubscribe(sub);
                    }
                    ensureInputSectionAvailable();
                }
            } finally {
                
            }
        }
        
    }
    
    private String getPromptAfterError() {
        return launcher.prompt(false);
    }
    
    private synchronized Task sendJShellClose() {
        RemoteJShellService e;
        synchronized (this) {
            if (launcher == null) {
                return Task.EMPTY;
            }
            e = this.exec;
        }
        if (e != null) {
            e.requestShutdown();
        }
        // possibly delayed, if the evaluator is just processing some remote call.
        return evaluator.post(() -> {
            try {
                launcher.closeState();
            } catch (InternalError ex) {
                // ignore
            }
            forceCloseStreams();
        });
    }
    
    /**
     * Terminates the session and disconnects it from the Document.
     * @return Task where the Jshell termination runs.
     */
    public Task closeSession() {
        return detach();
    }
    
    public ConsoleModel getModel() {
        return model;
    }
    
    private void refreshGuardedSection() throws BadLocationException {
        if (!isActive()) {
            return;
        }
        gsm.getGuardedSections().forEach((GuardedSection gs) -> gs.removeSection());
        ConsoleSection s = model.getInputSection();
        LineDocument ld = LineDocumentUtils.asRequired(consoleDocument, LineDocument.class);
        if (s == null) {
            // protected including the final newline, so an insertion at the end will
            // expand the guarded block automatically
            int l = consoleDocument.getLength() + 1;       
            gsm.protectSimpleRegion(ld.createPosition(0, Position.Bias.Forward),
                    ld.createPosition(l, Position.Bias.Forward),
                    "scrollback"); // NOI18N
        } else {
            int wr = s.getPartBegin() - 1;
            gsm.protectSimpleRegion(ld.createPosition(0, Position.Bias.Forward),
                    ld.createPosition(wr, Position.Bias.Backward),
                    "scrollback"); // NOI18N
        }
    }

    private static final Map<Document, Reference<ShellSession>> allSessions = new WeakHashMap<>();
    
    private class GuardedSectionUpdater implements ConsoleListener {
        @Override
        public void sectionCreated(ConsoleEvent e) {
            List<ConsoleSection> aff = e.getAffectedSections();
            for (ConsoleSection s : aff) {
                // if an input section has been created, the document BEFORE the section
                // should become guarded
                if (s == model.getLastInputSection()) {
                    // redefine the guarded block, if any, to span from the 
                    // start to the prompt end.
                    refresh();
                }
            }
        }
        
        private void refresh() {
            try {
                refreshGuardedSection();
            } catch (BadLocationException ex) {
                //
            }
        }
        
        @Override
        public void sectionUpdated(ConsoleEvent e) {
            for (ConsoleSection s : e.getAffectedSections()) {
                if (s == model.getLastInputSection()) {
                    refresh();
                    break;
                }
            }
        }

        @Override
        public void executing(ConsoleEvent e) {
        }

        @Override
        public void closed(ConsoleEvent e) {
        }
    }
    
    /**
     * All accesses to the shell must go through the request processor.
     */
    private RequestProcessor evaluator;
    
    public Task    post(Runnable r) {
        return evaluator.post(r);
    }
    
    /**
     * Returns the user-entered snippets. Does not return snippets, which are run
     * during the initial startup of JShell, just snippets executed afterwards.
     * 
     * @param onlyValid
     * @return 
     */
    public List<Snippet> getSnippets(boolean onlyUser, boolean onlyValid) {
        Set<Snippet> initial = this.initialSetupSnippets;
        JShell sh = shell;
        if (sh == null) {
            return Collections.emptyList();
        }
        
        List<Snippet> snips = new ArrayList<>(sh.snippets().collect(Collectors.toList()));
        if (onlyUser) {
            snips.removeAll(initial);
            snips.removeAll(excludedSnippets);
        }
        if (onlyValid) {
            for (Iterator<Snippet> it = snips.iterator(); it.hasNext(); ) {
                Snippet s = it.next();
                if (!validSnippet(s)) {
                    it.remove();
                }
            }
        }
        return snips;
    }

    private boolean validSnippet(Snippet s) {
        Snippet.Status status = shell.status(s);
        return !(status == Snippet.Status.DROPPED ||
            status == Snippet.Status.OVERWRITTEN ||
            status == Snippet.Status.REJECTED);
    }
    
    public void stopExecutingCode() {
        JShell shell = this.shell;
        if (shell == null || !model.isExecute()) {
            return;
        }
        shell.stop();
    }

    public List<ShellHistory.Item> historyItems() {
        final List<Item> historyLines = new ArrayList<>();
        try {
            ParserManager.parse(Collections.singleton(Source.create(getConsoleDocument())), new UserTask() {
                @Override
                public void run(ResultIterator resultIterator) throws Exception {
                    ConsoleContents console = ConsoleContents.get(resultIterator);
                    ConsoleSection input = console.getInputSection();
                    ConsoleSection exec = console.getSectionModel().getExecutingSection();
                    for (ConsoleSection s : console.getSectionModel().getSections()) {
                        if (!s.getType().input) {
                            continue;
                        }
                        if (s == input || s == exec) {
                            // do not save current input
                            continue;
                        }
                        String contents = s.getContents(consoleDocument);
                        // ignore such lines, which contain just history command
                        if (contents.startsWith("/") && contents.length() > 2) {
                            if (contents.charAt(1) == '-' || Character.isDigit(contents.charAt(1))) {
                                continue;
                            }
                        }
                        List<SnippetHandle> handles = console.getHandles(s);
                        
                        Snippet.Kind sectionKind;
                        boolean command;
                        if (s.getType() == ConsoleSection.Type.COMMAND) {
                            command = true;
                            sectionKind = null;
                        } else {
                            command = false;
                            if (handles.isEmpty()) {
                                sectionKind = Snippet.Kind.ERRONEOUS;
                            } else {
                                sectionKind = handles.get(0).getKind();
                            }
                        }
                        contents = contents.trim();
                        if (contents.isEmpty()) {
                            continue;
                        }
                        historyLines.add(new ShellHistory.Item(sectionKind, command, contents));
                    }
                }
            });
        } catch (ParseException ex) {
            return Collections.emptyList();
        }
        return historyLines;
    }
    
    public Path resolvePath(String s) {
        return launcher == null ? Paths.get(s) : launcher.toPathResolvingUserHome(s);
    }
}
