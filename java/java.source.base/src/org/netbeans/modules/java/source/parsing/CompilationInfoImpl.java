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

package org.netbeans.modules.java.source.parsing;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.MethodTree;
import com.sun.tools.javac.api.ClientCodeWrapper.Trusted;
import com.sun.tools.javac.api.DiagnosticFormatter;
import com.sun.tools.javac.api.JavacTaskImpl;
import com.sun.tools.javac.util.JCDiagnostic;
import com.sun.tools.javac.util.Log;

import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;

import javax.swing.text.Document;
import javax.tools.Diagnostic;
import javax.tools.Diagnostic.Kind;
import javax.tools.DiagnosticListener;
import javax.tools.JavaFileObject;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.CompilationInfo.CacheClearPolicy;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.modules.java.source.JavaFileFilterQuery;
import org.netbeans.modules.java.source.indexing.JavaIndex;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.api.Source;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.Pair;

/**
 *
 * @author Tomas Zezula
 */
public final class CompilationInfoImpl {


    private JavaSource.Phase phase = JavaSource.Phase.MODIFIED;
    private CompilationUnitTree compilationUnit;

    private JavacTaskImpl javacTask;
    private DiagnosticListener<JavaFileObject> diagnosticListener;
    private final ClasspathInfo cpInfo;
    private Pair<DocPositionRegion,MethodTree> changedMethod;
    private final FileObject file;
    private final FileObject root;
    final AbstractSourceFileObject jfo;
    //@NotThreadSafe    //accessed under parser lock
    private Snapshot snapshot;
    private Reference<Snapshot> partialReparseLastGoodSnapshot;
    private final JavacParser parser;
    private final boolean isClassFile;
    private final boolean isDetached;
    JavaSource.Phase parserCrashed = JavaSource.Phase.UP_TO_DATE;      //When javac throws an error, the moveToPhase sets this to the last safe phase
    private final Map<CacheClearPolicy, Map<Object, Object>> userCache = new EnumMap<>(CacheClearPolicy.class);
    //cache of already parsed files
    private Map<JavaFileObject, CompilationUnitTree> parsedTrees;
    private Map<FileObject, AbstractSourceFileObject> ide2javacFileObject;
    private Map<FileObject, Snapshot> fileObject2Snapshot;
    private boolean incomplete;
    
    /**
     * Creates a new CompilationInfoImpl for given source file
     * @param parser used to parse the file
     * @param file to be parsed
     * @param root the owner of the parsed file
     * @param javacTask used javac or null if new one should be created
     * @param snapshot rendered content of the file
     * @param detached true if the CompilationInfoImpl is detached from parsing infrastructure.
     * @throws java.io.IOException
     */
    public CompilationInfoImpl (final JavacParser parser,
                         final FileObject file,
                         final FileObject root,
                         final JavacTaskImpl javacTask,
                         final DiagnosticListener<JavaFileObject> diagnosticListener,
                         final Snapshot snapshot,
                         final boolean detached,
                         final Map<FileObject, AbstractSourceFileObject> ide2javacFileObject,
                         final Map<FileObject, Snapshot> fileObject2Snapshot) throws IOException {
        assert parser != null;
        this.parser = parser;
        this.cpInfo = parser.getClasspathInfo();
        assert cpInfo != null;
        this.file = file;
        this.root = root;
        this.snapshot = snapshot;
        this.partialReparseLastGoodSnapshot = new SoftReference<>(snapshot);
        assert file == null || snapshot != null;
        this.javacTask = javacTask;
        this.diagnosticListener = diagnosticListener;
        this.isClassFile = false;
        this.isDetached = detached;
        this.ide2javacFileObject = ide2javacFileObject;
        this.fileObject2Snapshot = fileObject2Snapshot;
        this.jfo = file != null ?
            getSourceFileObject(file) :
            null;
    }

    private AbstractSourceFileObject getSourceFileObject(FileObject file) throws IOException {
        AbstractSourceFileObject result = ide2javacFileObject.get(file);

        if (result == null) {
            Snapshot snapshot = fileObject2Snapshot.get(file);

            if (snapshot != null) {
                result = FileObjects.sourceFileObject(file, root, JavaFileFilterQuery.getFilter(file), snapshot.getText());
            } else {
                result = FileObjects.sourceFileObject(file, root); //TODO: filter?
            }

            ide2javacFileObject.put(file, result);
        }

        return result;
    }

    /**
     * Creates a new CompilationInfoImpl for classpaths
     * @param cpInfo classpaths
     */
    CompilationInfoImpl (
            @NonNull final ClasspathInfo cpInfo,
            @NullAllowed final FileObject root) {
        assert cpInfo != null;
        this.parser = null;
        this.file = null;
        this.root = root;
        this.jfo = null;
        this.snapshot = null;
        this.cpInfo = cpInfo;
        this.isClassFile = false;
        this.isDetached = false;
    }

    /**
     * Creates a new CompilationInfoImpl for a class file
     * @param cpInfo classpaths
     * @param file to be analyzed
     * @param root the owner of analyzed file
     */
    CompilationInfoImpl (final ClasspathInfo cpInfo,
                         final FileObject file,
                         final FileObject root) throws IOException {
        assert cpInfo != null;
        assert file != null;
        assert root != null;
        this.parser = null;
        this.file = file;
        this.root = root;
        this.jfo = FileObjects.sourceFileObject(file, root);
        this.snapshot = null;
        this.cpInfo = cpInfo;
        this.isClassFile = true;
        this.isDetached = false;
    }

    public void update (final Snapshot snapshot) throws IOException {
        assert snapshot != null;
        this.jfo.update(snapshot.getText());
        this.snapshot = snapshot;
    }
    
    public Snapshot getSnapshot () {
        return this.snapshot;
    }

    public Snapshot getPartialReparseLastGoodSnapshot() {
        return partialReparseLastGoodSnapshot != null ? partialReparseLastGoodSnapshot.get()
                                                      : null;
    }

    public void setPartialReparseLastGoodSnapshot(Snapshot snapshot) {
        this.partialReparseLastGoodSnapshot = new SoftReference<>(snapshot);
    }

    /**
     * Returns the current phase of the {@link JavaSource}.
     * @return {@link JavaSource.Phase} the state which was reached by the {@link JavaSource}.
     */
    public JavaSource.Phase getPhase() {
        return this.phase;
    }
    
    public Pair<DocPositionRegion,MethodTree> getChangedTree () {
        return this.changedMethod;
    }
    
    /**
     * Returns the javac tree representing the source file.
     * @return {@link CompilationUnitTree} the compilation unit cantaining the top level classes contained in the,
     * java source file. 
     * @throws java.lang.IllegalStateException  when the phase is less than {@link JavaSource.Phase#PARSED}
     */
    public CompilationUnitTree getCompilationUnit() {
        if (this.jfo == null) {
            throw new IllegalStateException ();
        }
        if (this.phase.compareTo (JavaSource.Phase.PARSED) < 0)
            throw new IllegalStateException("Cannot call getCompilationUnit() if current phase < JavaSource.Phase.PARSED. You must call toPhase(Phase.PARSED) first.");//NOI18N
        return this.compilationUnit;
    }
    
    /**
     * Returns the content of the file represented by the {@link JavaSource}.
     * @return String the java source
     */
    public String getText() {
        if (!hasSource()) {
            throw new IllegalStateException ();
        }
        try {
            return this.jfo.getCharContent(false).toString();
        } catch (IOException ioe) {
            //Should never happen
            Exceptions.printStackTrace(ioe);
            return null;
        }
    }
    
    /**
     * Returns the {@link TokenHierarchy} for the file represented by the {@link JavaSource}.
     * @return lexer TokenHierarchy
     */
    public TokenHierarchy<?> getTokenHierarchy() {
        if (!hasSource()) {
            throw new IllegalStateException ();
        }
        try {
            return this.jfo.getTokenHierarchy();
        } catch (IOException ioe) {
            //Should never happen
            Exceptions.printStackTrace(ioe);
            return null;
        }
    }

    /**
     * Returns the errors in the file represented by the {@link JavaSource}.
     * @return an list of {@link Diagnostic} 
     */
    public List<Diagnostic> getDiagnostics() {
        if (this.jfo == null) {
            throw new IllegalStateException ();
        }
        DiagnosticListenerImpl.Diagnostics errors = ((DiagnosticListenerImpl)diagnosticListener).getErrors(jfo);
        List<Diagnostic<? extends JavaFileObject>> partialReparseErrors = ((DiagnosticListenerImpl)diagnosticListener).partialReparseErrors;
        List<Diagnostic<? extends JavaFileObject>> affectedErrors = ((DiagnosticListenerImpl)diagnosticListener).affectedErrors;
        int errorsSize = 0;

        for (Collection<DiagnosticListenerImpl.DiagNode> err : errors.values()) {
            errorsSize += err.size();
        }

        List<Diagnostic> localErrors = new ArrayList<Diagnostic>(errorsSize +
                (partialReparseErrors == null ? 0 : partialReparseErrors.size()) + 
                (affectedErrors == null ? 0 : affectedErrors.size()));
        DiagnosticFormatter<JCDiagnostic> formatter = Log.instance(javacTask.getContext()).getDiagnosticFormatter();
        
        DiagnosticListenerImpl.DiagNode node = errors.first;
        while(node != null) {
            localErrors.add(RichDiagnostic.wrap(node.diag, formatter));
            node = node.next;
        }
        if (partialReparseErrors != null) {
            for (Diagnostic<? extends JavaFileObject> d : partialReparseErrors) {
                localErrors.add(RichDiagnostic.wrap(d, formatter));
            }
        }
        if (affectedErrors != null) {
            for (Diagnostic<? extends JavaFileObject> d : affectedErrors) {
                localErrors.add(RichDiagnostic.wrap(d, formatter));
            }
        }
        return localErrors;
    }
    
                   
        
    /**
     * Returns {@link ClasspathInfo} for which this {@link CompilationInfoImpl} was created.
     * @return ClasspathInfo
     */
    public ClasspathInfo getClasspathInfo() {
	return this.cpInfo;
    }
    
    /**
     * Returns {@link JavacParser} which created this {@link CompilationInfoImpl}
     * or null when the {@link CompilationInfoImpl} was created for no files.
     * @return {@link JavacParser} or null
     */
    public JavacParser getParser () {
        return this.parser;
    }
    
    
    /**
     * Returns the {@link FileObject} represented by this {@link CompilationInfo}.
     * @return FileObject
     */
    public FileObject getFileObject () {
        return this.file;
    }
    
    public FileObject getRoot () {
        return this.root;
    }
    
    public boolean isClassFile () {
        return this.isClassFile;
    }
    
    /**
     * Returns {@link Document} of this {@link CompilationInfoImpl}
     * @return Document or null when the {@link DataObject} doesn't
     * exist or has no {@link EditorCookie}.
     * @throws java.io.IOException
     */
    public Document getDocument() {        
        if (this.file == null) {
            return null;
        }
        if (!this.file.isValid()) {
            return null;
        }
        Source source = this.snapshot != null ? this.snapshot.getSource() : null;
        if (source != null) {
            return source.getDocument(false);
        }
        return null;
    }

    public Map<JavaFileObject, CompilationUnitTree> getParsedTrees() {
        return this.parsedTrees;
    }

                                
    /**
     * Moves the state to required phase. If given state was already reached 
     * the state is not changed. The method will throw exception if a state is 
     * illegal required. Acceptable parameters for thid method are <BR>
     * <LI>{@link org.netbeans.api.java.source.JavaSource.Phase.PARSED}
     * <LI>{@link org.netbeans.api.java.source.JavaSource.Phase.ELEMENTS_RESOLVED}
     * <LI>{@link org.netbeans.api.java.source.JavaSource.Phase.RESOLVED}
     * <LI>{@link org.netbeans.api.java.source.JavaSource.Phase.UP_TO_DATE}   
     * @param phase The required phase
     * @return the reached state
     * @throws IllegalArgumentException in case that given state can not be 
     *         reached using this method
     * @throws IOException when the file cannot be red
     */    
    public JavaSource.Phase toPhase(JavaSource.Phase phase ) throws IOException {
        return toPhase(phase, Collections.emptyList());
    }

    /**
     * Moves the state to required phase. If given state was already reached 
     * the state is not changed. The method will throw exception if a state is 
     * illegal required. Acceptable parameters for thid method are <BR>
     * <LI>{@link org.netbeans.api.java.source.JavaSource.Phase.PARSED}
     * <LI>{@link org.netbeans.api.java.source.JavaSource.Phase.ELEMENTS_RESOLVED}
     * <LI>{@link org.netbeans.api.java.source.JavaSource.Phase.RESOLVED}
     * <LI>{@link org.netbeans.api.java.source.JavaSource.Phase.UP_TO_DATE}   
     * @param phase The required phase
     * @return the reached state
     * @throws IllegalArgumentException in case that given state can not be 
     *         reached using this method
     * @throws IOException when the file cannot be red
     */    
    public JavaSource.Phase toPhase(JavaSource.Phase phase, List<FileObject> forcedSources ) throws IOException {
        if (phase == JavaSource.Phase.MODIFIED) {
            throw new IllegalArgumentException( "Invalid phase: " + phase );    //NOI18N
        }
        if (!hasSource()) {
            JavaSource.Phase currentPhase = getPhase();
            if (currentPhase.compareTo(phase)<0) {
                setPhase(phase);
                if (currentPhase == JavaSource.Phase.MODIFIED)
                    getJavacTask().getElements().getTypeElement("java.lang.Object"); // Ensure proper javac initialization
            }
            return phase;
        } else {
            JavaSource.Phase currentPhase = parser.moveToPhase(phase, this, forcedSources, false);
            return currentPhase.compareTo (phase) < 0 ? currentPhase : phase;
        }
    }

    /**
     * Returns {@link JavacTaskImpl}, when it doesn't exist
     * it's created.
     * @return JavacTaskImpl
     */
    public synchronized JavacTaskImpl getJavacTask() {
        try {
            return getJavacTask(Collections.emptyList());
        } catch (IOException ex) {
            //should not happen
            throw new IllegalStateException(ex);
        }
    }

    /**
     * Returns {@link JavacTaskImpl}, when it doesn't exist
     * it's created.
     * @return JavacTaskImpl
     */
    public synchronized JavacTaskImpl getJavacTask(List<FileObject> forcedSources) throws IOException {
        if (javacTask == null) {
            List<JavaFileObject> jfos = new ArrayList<>();
            if (jfo != null) {
                jfos.add(jfo);
                forcedSources.stream()
                             .map(fo -> runAndThrow(this::getSourceFileObject, fo))
                             .forEach(jfos::add);
            }
            diagnosticListener = new DiagnosticListenerImpl(this.root, this.jfo, this.cpInfo);
            javacTask = JavacParser.createJavacTask(this.file, jfos, this.root, this.cpInfo,
                    this.parser, diagnosticListener, isDetached);
        }
	return javacTask;
    }

    List<FileObject> getForcedSources() {
        return Collections.emptyList();
    }

    public Object getCachedValue(Object key) {
        for (Map<Object, Object> c : userCache.values()) {
            Object res = c.get(key);

            if (res != null) return res;
        }

        return null;
    }

    public void putCachedValue(Object key, Object value, CacheClearPolicy clearPolicy) {
        for (Map<Object, Object> c : userCache.values()) {
            c.remove(key);
        }
        userCache.computeIfAbsent(clearPolicy, k -> new HashMap<>())
                 .put(key, value);
    }

    public void taskFinished() {
        userCache.remove(CacheClearPolicy.ON_TASK_END);
    }

    public void dispose() {
        userCache.clear();
    }
    
    /**
     * Returns current {@link DiagnosticListener}
     * @return listener
     */
    public DiagnosticListener<JavaFileObject> getDiagnosticListener() {
        return diagnosticListener;
    }
    
    /**
     * Sets the current {@link JavaSource.Phase}
     * @param phase
     */
    void setPhase(final JavaSource.Phase phase) {
        assert phase != null;
        this.phase = phase;
    }
    
    /**
     * Sets changed method
     * @param changedMethod
     */
    void setChangedMethod (final Pair<DocPositionRegion,MethodTree> changedMethod) {
        this.changedMethod = changedMethod;
        userCache.remove(CacheClearPolicy.ON_TASK_END);
        userCache.remove(CacheClearPolicy.ON_CHANGE);
    }
    
    /**
     * Sets the {@link CompilationUnitTree}
     * @param compilationUnit
     */
    void setCompilationUnit(final CompilationUnitTree compilationUnit) {
        assert compilationUnit != null;
        this.compilationUnit = compilationUnit;
    }

    public void setParsedTrees(Map<JavaFileObject, CompilationUnitTree> parsedTrees) {
        this.parsedTrees = parsedTrees;
    }

    private boolean hasSource() {
        return this.jfo != null && !isClassFile;
    }

    List<AbstractSourceFileObject> getFiles(List<FileObject> sourceFiles) throws IOException {
        return sourceFiles.stream()
                          .map(fo -> runAndThrow(this::getSourceFileObject, fo))
                          .collect(Collectors.toList());
    }

    private <P, R> R runAndThrow(Convert<P, R> run, P p) {
        try {
            return run.run(p);
        } catch (Exception ex) {
            throw this.<RuntimeException>thrw(ex);
        }
    }

    private <T extends Exception> RuntimeException thrw(Exception e) throws T {
        throw (T) e;
    }

    interface Convert<P, R> {
        public R run(P p) throws Exception;
    }

    public Map<FileObject, AbstractSourceFileObject> getIde2javacFileObject() {
        return ide2javacFileObject;
    }

    public Map<FileObject, Snapshot> getFileObject2Snapshot() {
        return fileObject2Snapshot;
    }

    public boolean isIncomplete() {
        return incomplete;
    }

    public void markIncomplete() {
        this.incomplete = true;
    }

    // Innerclasses ------------------------------------------------------------
    @Trusted
    public static class DiagnosticListenerImpl implements DiagnosticListener<JavaFileObject> {
        
        private final Map<JavaFileObject, Diagnostics> source2Errors;
        private final FileObject root;
        private final JavaFileObject jfo;
        private final ClasspathInfo cpInfo;
        private volatile List<Diagnostic<? extends JavaFileObject>> partialReparseErrors;
        /**
         * true if the partialReparseErrors contain some non-warning
         */
        private volatile boolean partialReparseRealErrors;
        private volatile List<Diagnostic<? extends JavaFileObject>> affectedErrors;
        private volatile int currentDelta;
        
        public DiagnosticListenerImpl(
                @NullAllowed final FileObject root,
                @NullAllowed final JavaFileObject jfo,
                @NonNull final ClasspathInfo cpInfo) {
            this.root = root;
            this.jfo = jfo;
            this.cpInfo = cpInfo;
            this.source2Errors = new HashMap<>();
        }
        
        @Override
        public void report(Diagnostic<? extends JavaFileObject> message) {
            if (partialReparseErrors != null) {
                if (this.jfo != null && this.jfo == message.getSource()) {
                    partialReparseErrors.add(message);
                    if (message.getKind() == Kind.ERROR) {
                        partialReparseRealErrors = true;
                    }
                }
            } else {
                Diagnostics errors = getErrors(message.getSource());
                errors.add((int) message.getPosition(), message);
            }
        }

        private Diagnostics getErrors(JavaFileObject file) {
            Diagnostics errors;
            if (isIncompleteClassPath()) {
                if (root != null && JavaIndex.hasSourceCache(root.toURL(), false)) {
                    errors = source2Errors.get(file);
                    if (errors == null) {
                        source2Errors.put(file, errors = new Diagnostics());
                        if (this.jfo != null && this.jfo == file) {
                            errors.add(0, new IncompleteClassPath(this.jfo));
                        }
                    }
                } else {
                    errors = new Diagnostics();
                    if (this.jfo != null && this.jfo == file) {
                        errors.add(0, new IncompleteClassPath(this.jfo));
                    }
                }
            } else {
                errors = source2Errors.get(file);
                if (errors == null) {
                    source2Errors.put(file, errors = new Diagnostics());
                }
            }
            return errors;
        }

        public final boolean hasPartialReparseErrors () {
            // #236654: warnings should not stop processing of the reparsed method
            return this.partialReparseErrors != null && partialReparseRealErrors;
        }
        
        public final void startPartialReparse (int from, int to) {
            if (partialReparseErrors == null) {
                partialReparseErrors = new ArrayList<>();
                Diagnostics errors = getErrors(jfo);
                SortedMap<Integer, Collection<DiagNode>> subMap = errors.subMap(from, to);
                subMap.values().forEach((value) -> {
                    value.forEach((node) -> {
                        errors.unlink(node);
                    });
                });
                subMap.clear();       //Remove errors in changed method durring the partial reparse
                Map<Integer, Collection<DiagNode>> tail = errors.tailMap(to);
                this.affectedErrors = new ArrayList<>(tail.size());
                HashSet<DiagNode> tailNodes = new HashSet<>();
                for (Iterator<Entry<Integer,Collection<DiagNode>>> it = tail.entrySet().iterator(); it.hasNext();) {
                    Entry<Integer, Collection<DiagNode>> e = it.next();
                    tailNodes.addAll(e.getValue());
                    it.remove();
                }
                DiagNode node = errors.first;
                while(node != null) {
                    if (tailNodes.contains(node)) {
                        errors.unlink(node);
                        final JCDiagnostic diagnostic = (JCDiagnostic) node.diag;
                        if (diagnostic == null) {
                            throw new IllegalStateException("#184910: diagnostic == null " + mapArraysToLists(Thread.getAllStackTraces())); //NOI18N
                        }
                        this.affectedErrors.add(new D (diagnostic));
                    }
                    node = node.next;
                }
            }
            else {
                this.partialReparseErrors.clear();
            }
            partialReparseRealErrors = false;
        }
        
        public final void endPartialReparse (final int delta) {
            this.currentDelta+=delta;
        }
        
        private static <A,B> Map<A,List<B>> mapArraysToLists (final Map<? extends A, B[]> map) {
            final Map<A,List<B>> result = new HashMap<A, List<B>>();
            for (Map.Entry<? extends A,B[]> entry : map.entrySet()) {
                result.put(entry.getKey(), Arrays.asList(entry.getValue()));
            }
            return result;
        } 
        
        private boolean isIncompleteClassPath() {
            return cpInfo.getClassPath(ClasspathInfo.PathKind.BOOT).getFlags().contains(ClassPath.Flag.INCOMPLETE) ||
            cpInfo.getClassPath(ClasspathInfo.PathKind.COMPILE).getFlags().contains(ClassPath.Flag.INCOMPLETE) ||
            cpInfo.getClassPath(ClasspathInfo.PathKind.SOURCE).getFlags().contains(ClassPath.Flag.INCOMPLETE);
        }

        private final class D implements Diagnostic {
            
            private final JCDiagnostic delegate;
            
            public D (final JCDiagnostic delegate) {
                assert delegate != null;
                this.delegate = delegate;
            }

            @Override
            public Kind getKind() {
                return this.delegate.getKind();
            }

            @Override
            public Object getSource() {
                return this.delegate.getSource();
            }

            @Override
            public long getPosition() {
                long ret = this.delegate.getPosition();
                return ret;
            }

            @Override
            public long getStartPosition() {
                long ret = this.delegate.getStartPosition();
                return ret;
            }

            @Override
            public long getEndPosition() {
                long ret = this.delegate.getEndPosition();
                return ret;
            }

            @Override
            public long getLineNumber() {
                return -1;
            }

            @Override
            public long getColumnNumber() {
                return -1;
            }

            @Override
            public String getCode() {
                return this.delegate.getCode();
            }

            @Override
            public String getMessage(Locale locale) {
                return this.delegate.getMessage(locale);
            }
            
        }

        private static final class IncompleteClassPath implements Diagnostic<JavaFileObject> {

            private final JavaFileObject file;

            IncompleteClassPath(final JavaFileObject file) {
                this.file = file;
            }

            @Override
            public Kind getKind() {
                return Kind.WARNING;
            }

            @Override
            public JavaFileObject getSource() {
                return file;
            }

            @Override
            public long getPosition() {
                return 0;
            }

            @Override
            public long getStartPosition() {
                return getPosition();
            }

            @Override
            public long getEndPosition() {
                return getPosition();
            }

            @Override
            public long getLineNumber() {
                return getPosition();
            }

            @Override
            public long getColumnNumber() {
                return getPosition();
            }

            @Override
            public String getCode() {
                return "nb.classpath.incomplete";   //NOI18N
            }

            @Override
            public String getMessage(Locale locale) {
                return NbBundle.getMessage(
                    CompilationInfoImpl.class,
                    "ERR_IncompleteClassPath");
            }
        }
        
        private static final class Diagnostics extends TreeMap<Integer, Collection<DiagNode>> {
            private DiagNode first;
            private DiagNode last;

            public void add(int pos, Diagnostic<? extends JavaFileObject> diag) {
                DiagNode node = new DiagNode(last, diag, null);
                computeIfAbsent((int)diag.getPosition(), k -> new ArrayList<>()).add(node);
                if (last != null) {
                    last.next = node;
                }
                last = node;
                if (first == null) {
                    first = node;
                }
            }
            
            private void unlink(DiagNode node) {
                if (node.next == null) {
                    last = node.prev;
                } else {
                    node.next.prev = node.prev;

                }
                if (node.prev == null) {
                    first = node.next;
                } else {
                    node.prev.next = node.next;
                }
            }
        }
        
        private static final class DiagNode {
            private Diagnostic<? extends JavaFileObject> diag;
            private DiagNode next;
            private DiagNode prev;

            private DiagNode(DiagNode prev, Diagnostic<? extends JavaFileObject> diag, DiagNode next) {
                this.diag = diag;
                this.next = next;
                this.prev = prev;
            }            
        }
    }

    static final class RichDiagnostic implements Diagnostic {

        private final JCDiagnostic delegate;
        private final DiagnosticFormatter<JCDiagnostic> formatter;

        public RichDiagnostic(JCDiagnostic delegate, DiagnosticFormatter<JCDiagnostic> formatter) {
            this.delegate = delegate;
            this.formatter = formatter;
        }

        @Override
        public Kind getKind() {
            return delegate.getKind();
        }

        @Override
        public Object getSource() {
            return delegate.getSource();
        }

        @Override
        public long getPosition() {
            return delegate.getPosition();
        }

        @Override
        public long getStartPosition() {
            return delegate.getStartPosition();
        }

        @Override
        public long getEndPosition() {
            return delegate.getEndPosition();
        }

        @Override
        public long getLineNumber() {
            return delegate.getLineNumber();
        }

        @Override
        public long getColumnNumber() {
            return delegate.getColumnNumber();
        }

        @Override
        public String getCode() {
            return delegate.getCode();
        }

        @Override
        public String getMessage(Locale locale) {
            return formatter.format(delegate, locale);
        }

        @Override
        public String toString() {
            return delegate.toString();
        }

        JCDiagnostic getDelegate() {
            return delegate;
        }

        public static Diagnostic wrap(Diagnostic d, DiagnosticFormatter<JCDiagnostic> df) {
            return d instanceof JCDiagnostic jd ? new RichDiagnostic(jd, df) : d;
        }
    }    
}
