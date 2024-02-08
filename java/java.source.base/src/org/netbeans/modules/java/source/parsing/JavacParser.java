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
import com.sun.source.util.JavacTask;
import com.sun.source.util.Trees;
import com.sun.tools.javac.api.JavacTaskImpl;
import com.sun.tools.javac.api.JavacTool;
import com.sun.tools.javac.comp.AttrContext;
import com.sun.tools.javac.comp.Env;
import com.sun.tools.javac.main.JavaCompiler;
import org.netbeans.lib.nbjavac.services.CancelService;

import com.sun.tools.javac.util.Context;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.annotation.processing.Processor;
import javax.swing.event.ChangeEvent;
import  javax.swing.event.ChangeListener;
import javax.swing.text.Document;
import javax.tools.DiagnosticListener;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;

import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.queries.CompilerOptionsQuery;
import org.netbeans.api.java.queries.SourceLevelQuery;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.ClasspathInfo.PathKind;
import org.netbeans.api.java.source.JavaParserResultTask;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.lib.editor.util.swing.PositionRegion;
import org.netbeans.modules.java.preprocessorbridge.spi.JavaFileFilterImplementation;
import org.netbeans.modules.java.source.JavaFileFilterQuery;
import org.netbeans.modules.java.source.JavaSourceAccessor;
import org.netbeans.modules.java.source.PostFlowAnalysis;
import org.netbeans.modules.java.source.indexing.APTUtils;
import org.netbeans.modules.java.source.indexing.FQN2Files;
import org.netbeans.lib.nbjavac.services.NBAttr;
import org.netbeans.lib.nbjavac.services.NBClassFinder;
import org.netbeans.lib.nbjavac.services.NBClassReader;
import org.netbeans.lib.nbjavac.services.NBEnter;
import org.netbeans.lib.nbjavac.services.NBJavaCompiler;
import org.netbeans.lib.nbjavac.services.NBMemberEnter;
import org.netbeans.lib.nbjavac.services.NBParserFactory;
import org.netbeans.lib.nbjavac.services.NBClassWriter;
import org.netbeans.lib.nbjavac.services.NBJavacTrees;
import org.netbeans.lib.nbjavac.services.NBLog;
import org.netbeans.lib.nbjavac.services.NBNames;
import org.netbeans.lib.nbjavac.services.NBResolve;
import org.netbeans.lib.nbjavac.services.NBTreeMaker;
import org.netbeans.modules.java.source.base.SourceLevelUtils;
import org.netbeans.modules.java.source.indexing.JavaIndex;
import org.netbeans.modules.java.source.tasklist.CompilerSettings;
import org.netbeans.modules.java.source.usages.ClassIndexImpl;
import org.netbeans.modules.java.source.usages.ClasspathInfoAccessor;
import org.netbeans.modules.java.source.util.AbortChecker;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.Task;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.impl.Utilities;
import org.netbeans.modules.parsing.spi.LowMemoryWatcher;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.parsing.spi.ParserResultTask;
import org.netbeans.modules.parsing.spi.SourceModificationEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.SpecificationVersion;
import org.openide.util.ChangeSupport;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.Pair;
import org.openide.util.WeakListeners;
import org.openide.util.lookup.ServiceProvider;

/**
 * Provides Parsing API parser built atop Javac (using JSR 199).
 * @author Tomas Zezula
 */
//@NotThreadSafe
@SuppressWarnings("ClassWithMultipleLoggers")
public class JavacParser extends Parser {
    public static final String OPTION_PATCH_MODULE = "--patch-module";          //NOI18N
    public static final String NB_X_MODULE = "-Xnb-Xmodule:";                   //NOI18N
    //Timer logger
    private static final Logger TIME_LOGGER = Logger.getLogger("TIMER");        //NOI18N
    //Debug logger
    private static final Logger LOGGER = Logger.getLogger(JavacParser.class.getName());
    //Java Mime Type
    public static final String MIME_TYPE = "text/x-java";
    //No output writer like /dev/null
    private static final PrintWriter DEV_NULL = new PrintWriter(new NullWriter(), false);
    //Max number of dump files
    private static final int MAX_DUMPS = Integer.getInteger("org.netbeans.modules.java.source.parsing.JavacParser.maxDumps", 255);  //NOI18N
    //Command line switch disabling partial reparse
    private static final boolean DISABLE_PARTIAL_REPARSE = Boolean.getBoolean("org.netbeans.modules.java.source.parsing.JavacParser.no_reparse");   //NOI18N
    private static final boolean DISABLE_PARAMETER_NAMES_READING = Boolean.getBoolean("org.netbeans.modules.java.source.parsing.JavacParser.no_parameter_names");   //NOI18N
    private static final Set<Reference<Object>> HUGE_SNAPSHOTS = new HashSet<>();
    private static final LowMemoryWatcher LOW_MEMORY_WATCHER = LowMemoryWatcher.getInstance();
    public static final String LOMBOK_DETECTED = "lombokDetected";

    /**
     * Helper map mapping the {@link Phase} to message for performance logger
     */
    private static final Map<Phase, String> phase2Message = new EnumMap<> (Phase.class);

    static {
        phase2Message.put (Phase.PARSED,"Parsed");                              //NOI18N
        phase2Message.put (Phase.ELEMENTS_RESOLVED,"Signatures Attributed");    //NOI18N
        phase2Message.put (Phase.RESOLVED, "Attributed");                       //NOI18N
    }

    //Listener support
    private final ChangeSupport listeners = new ChangeSupport(this);
    //Cancelling of parser
    private final AtomicBoolean parserCanceled = new AtomicBoolean();
    private final AtomicBoolean lowMemoryCancel = new AtomicBoolean();
    //Cancelling of index
    private final AtomicBoolean indexCanceled = new AtomicBoolean();

    //When true the parser is a private copy not used by the parsing API, see JavaSourceAccessor.createCompilationController
    private final boolean privateParser;
    //File processed by this javac
    private FileObject file;
    //Root owning the file
    private FileObject root;
    //ClassPaths used by the parser
    private ClasspathInfo cpInfo;
    //all the files for which parser was created for
    private final Collection<Snapshot> snapshots;
    //Incremental parsing support
    private final boolean supportsReparse;
    //Incremental parsing support
    private final List<Pair<DocPositionRegion,MethodTree>> positions =
            Collections.synchronizedList(new LinkedList<>());
    //Incremental parsing support
    private final AtomicReference<Pair<DocPositionRegion,MethodTree>> changedMethod = new AtomicReference<>();
    //J2ME preprocessor support
    private final FilterListener filterListener;
    //ClasspathInfo Listener
    private final ChangeListener cpInfoListener;
    private final SequentialParsing sequentialParsing;
    //Cached javac impl
    private CompilationInfoImpl ciImpl;
    //State of the parser, used only for single source parser, otherwise don't care.
    private boolean initialized;
    //Parser is invalidated, new parser impl need to be created, but keeps current classpath info.
    private boolean invalid;
    //Last used snapshot
    private Snapshot cachedSnapShot;
    //Lamport clock of parse calls
    private long parseId;
    //Weak Change listener on ClasspathInfo, created by init
    private ChangeListener weakCpListener;
    //Current source for parse optmalization of task with no Source (identity)
    private Reference<JavaSource> currentSource;
    private final boolean perFileProcessing;

    JavacParser (final Collection<Snapshot> snapshots, boolean privateParser) {
        this.privateParser = privateParser;
        this.snapshots = snapshots;
        final boolean singleJavaFile = this.snapshots.size() == 1 && MIME_TYPE.equals(snapshots.iterator().next().getSource().getMimeType());
        this.supportsReparse = singleJavaFile && !DISABLE_PARTIAL_REPARSE;
        JavaFileFilterImplementation filter = null;
        if (singleJavaFile) {
            final Source source = snapshots.iterator().next().getSource();
            FileObject fo = source.getFileObject();
            if (fo != null) {
                //fileless Source -- ie. debugger watch CC etc
                filter = JavaFileFilterQuery.getFilter(fo);
            }
        }
        this.filterListener = filter != null ? new FilterListener (filter) : null;
        this.cpInfoListener = new ClasspathInfoListener(
                listeners,
                () -> {
                    if (snapshots.isEmpty()) {
                        invalidate(true);
                    }
                });
        this.sequentialParsing = Lookup.getDefault().lookup(SequentialParsing.class);
        this.perFileProcessing = perFileProcessing();
    }

    private boolean perFileProcessing() {
        if (snapshots.size() <= 1) {
            return true;
        }

        boolean result = false;

        for (Iterator<Reference<Object>> it = HUGE_SNAPSHOTS.iterator(); it.hasNext();) {
            Reference<Object> ref = it.next();
            Object obj = ref.get();
            if (obj == null) {
                it.remove();
            } else if (obj == snapshots) {
                result = true;
            }
        }

        return result;
    }

    private void init (final Snapshot snapshot, final Task task, final boolean singleSource) {
        final boolean explicitCpInfo = (task instanceof ClasspathInfo.Provider) && ((ClasspathInfo.Provider)task).getClasspathInfo() != null;
        if (!initialized) {
            final Source source = snapshot.getSource();
            final FileObject sourceFile = source.getFileObject();
            assert sourceFile != null;
            this.file = sourceFile;
            final ClasspathInfo oldInfo = this.cpInfo;
            if (explicitCpInfo) {
                cpInfo = ((ClasspathInfo.Provider)task).getClasspathInfo();
            }
            else {
                cpInfo = ClasspathInfo.create(sourceFile);
            }
            final ClassPath cp = cpInfo.getClassPath(PathKind.SOURCE);
            assert cp != null;
            this.root = cp.findOwnerRoot(sourceFile);
            if (singleSource) {
                if (oldInfo != null && weakCpListener != null) {
                    oldInfo.removeChangeListener(weakCpListener);
                    this.weakCpListener = null;
                }
                if (!explicitCpInfo) {      //Don't listen on artificial classpahs
                    this.weakCpListener = WeakListeners.change(cpInfoListener, cpInfo);
                    cpInfo.addChangeListener (this.weakCpListener);
                }
                initialized = true;
            }
        } else if (singleSource && !explicitCpInfo) {     //tzezula: MultiSource should ever be explicitCpInfo, but JavaSource.create(CpInfo, List<Fo>) allows null!
            //Recheck ClasspathInfo if still valid
            assert this.file != null;
            assert cpInfo != null;
            ClassPath scp = ClassPath.getClassPath(this.file, ClassPath.SOURCE);
            if (scp == null) {
                scp = ClassPath.EMPTY;
            }
            if (scp != cpInfo.getClassPath(PathKind.SOURCE)) {
                //Revalidate
                final Project owner = FileOwnerQuery.getOwner(this.file);
                LOGGER.log(Level.WARNING, "ClassPath identity changed for {0}, class path owner: {1} original sourcePath: {2} new sourcePath: {3}", //NOI18N
                        new Object[]{
                            this.file,
                            owner == null ? "null" : (FileUtil.getFileDisplayName(owner.getProjectDirectory())+" ("+owner.getClass()+")"), cpInfo.getClassPath(PathKind.SOURCE),    //NOI18N
                            scp
                });
                if (this.weakCpListener != null) {
                    cpInfo.removeChangeListener(weakCpListener);
                }
                cpInfo = ClasspathInfo.create(this.file);
                final ClassPath cp = cpInfo.getClassPath(PathKind.SOURCE);
                assert cp != null;
                this.root = cp.findOwnerRoot(this.file);
                this.weakCpListener = WeakListeners.change(cpInfoListener, cpInfo);
                cpInfo.addChangeListener (this.weakCpListener);
                JavaSourceAccessor.getINSTANCE().invalidateCachedClasspathInfo(this.file);
            }
        }
    }

    @SuppressWarnings("NestedAssignment")
    private void init(final Task task) {
        if (!initialized) {
            ClasspathInfo _tmpInfo = null;
            if (task instanceof ClasspathInfo.Provider &&
                (_tmpInfo = ((ClasspathInfo.Provider)task).getClasspathInfo()) != null) {
                if (cpInfo != null && weakCpListener != null) {
                    cpInfo.removeChangeListener(weakCpListener);
                    this.weakCpListener = null;
                }
                cpInfo = _tmpInfo;
                this.weakCpListener = WeakListeners.change(cpInfoListener, cpInfo);
                cpInfo.addChangeListener (this.weakCpListener);
                root = Optional.ofNullable(cpInfo.getClassPath(PathKind.SOURCE))
                        .map((cp)-> {
                            FileObject[] roots = cp.getRoots();
                            return roots.length > 0 ? roots[0] : null;
                        })
                        .orElse(null);
            } else {
                throw new IllegalArgumentException("No classpath provided by task: " + task);
            }
            initialized = true;
        }
    }

    private void invalidate (final boolean reinit) {
        this.invalid = true;
        if (reinit) {
            this.initialized = false;
        }
    }

    //@GuardedBy (org.netbeans.modules.parsing.impl.TaskProcessor.parserLock)
    @Override
    public void parse(final Snapshot snapshot, final Task task, SourceModificationEvent event) throws ParseException {
        try {
            checkSourceModification(event);
            parseImpl(snapshot, task);
        } catch (FileObjects.InvalidFileException ife) {
            //pass - already invalidated in parseImpl
        } catch (IOException ioe) {
            throw new ParseException ("JavacParser failure", ioe); //NOI18N
        }
    }


    private boolean shouldParse(@NonNull Task task) {
        if (!(task instanceof MimeTask)) {
            currentSource = null;
            return true;
        }
        final JavaSource newSource = ((MimeTask)task).getJavaSource();
        if (invalid) {
            currentSource = new WeakReference<>(newSource);
            return true;
        }
        final JavaSource oldSource = currentSource == null ?
                null :
                currentSource.get();
        if (oldSource == null) {
            currentSource = new WeakReference<>(newSource);
            return true;
        }
        if (newSource.equals(oldSource)) {
            return false;
        }
        if (newSource.getClasspathInfo() == oldSource.getClasspathInfo()) {
            currentSource = new WeakReference<>(newSource);
            return false;
        }
        currentSource = new WeakReference<>(newSource);
        return true;
    }

    private void parseImpl(
            final Snapshot snapshot,
            final Task task) throws IOException {
        assert task != null;
        assert privateParser || Utilities.holdsParserLock();
        parseId++;
        parserCanceled.set(false);
        lowMemoryCancel.set(false);
        indexCanceled.set(false);
        cachedSnapShot = snapshot;
        LOGGER.log(Level.FINE, "parse: task: {0}\n{1}", new Object[]{   //NOI18N
            task.toString(),
            snapshot == null ? "null" : snapshot.getText()});      //NOI18N
        final CompilationInfoImpl oldInfo = ciImpl;
        boolean success = false;
        try {
            switch (this.snapshots.size()) {
                case 0:
                    if (shouldParse(task)) {
                        init(task);
                        ciImpl = new CompilationInfoImpl(cpInfo, root);
                    }
                    break;
                case 1:
                    init (snapshot, task, true);
                    boolean needsFullReparse = true;
                    if (supportsReparse) {
                        final Pair<DocPositionRegion,MethodTree> _changedMethod = changedMethod.getAndSet(null);
                        if (_changedMethod != null && ciImpl != null) {
                            LOGGER.log(Level.FINE, "\t:trying partial reparse:\n{0}", _changedMethod.first().getText());                           //NOI18N
                            PartialReparser reparser = Lookup.getDefault().lookup(PartialReparser.class);
                            needsFullReparse = !reparser.reparseMethod(ciImpl, snapshot, _changedMethod.second(), _changedMethod.first().getText());
                            if (!needsFullReparse) {
                                ciImpl.setChangedMethod(_changedMethod);
                            }
                        }
                    }
                    if (needsFullReparse) {
                        positions.clear();
                        ciImpl = createCurrentInfo(this, file, root, snapshot, null, null, new HashMap<>(), new HashMap<>(), Collections.singletonMap(file, snapshot));
                        LOGGER.fine("\t:created new javac");                                    //NOI18N
                    }
                    break;
                default:
                    init(snapshot, task, false);
                    DiagnosticListener<JavaFileObject> diagnosticListener;
                    JavacTaskImpl javacTask;
                    Map<JavaFileObject, CompilationUnitTree> oldParsedTrees;
                    Map<FileObject, AbstractSourceFileObject> ide2javacFileObject;
                    Map<FileObject, Snapshot> file2Snapshot;

                    if (ciImpl != null && !perFileProcessing) {
                        diagnosticListener = ciImpl.getDiagnosticListener();
                        javacTask = ciImpl.getJavacTask();
                        oldParsedTrees = ciImpl.getParsedTrees();
                        ide2javacFileObject = ciImpl.getIde2javacFileObject();
                        file2Snapshot = ciImpl.getFileObject2Snapshot();
                    } else {
                        diagnosticListener = null;
                        javacTask = null;
                        oldParsedTrees = new HashMap<>();
                        ide2javacFileObject = new HashMap<>();
                        file2Snapshot = new HashMap<>();
                        for (Snapshot s : snapshots) {
                            file2Snapshot.put(s.getSource().getFileObject(), s);
                        }
                    }
                    ciImpl = createCurrentInfo(this, file, root, snapshot,
                        javacTask,
                        diagnosticListener, oldParsedTrees, ide2javacFileObject,
                        file2Snapshot);
            }
            success = true;
        } finally {
            invalid = !success;
            if (oldInfo != ciImpl && oldInfo != null) {
                oldInfo.dispose();
            }
        }
    }

    //@GuardedBy (org.netbeans.modules.parsing.impl.TaskProcessor.parserLock)
    @Override
    public JavacParserResult getResult (final Task task) throws ParseException {
        assert privateParser || Utilities.holdsParserLock();
        if (ciImpl == null && !invalid) {
            throw new IllegalStateException("No CompilationInfoImpl in valid parser");      //NOI18N
        }
        LOGGER.log (Level.FINE, "getResult: task:{0}", task.toString());                     //NOI18N

        final boolean isJavaParserResultTask = task instanceof JavaParserResultTask;
        final boolean isParserResultTask = task instanceof ParserResultTask;
        final boolean isUserTask = task instanceof UserTask;
        final boolean isClasspathInfoProvider = task instanceof ClasspathInfo.Provider;

        //Assumes that caller is synchronized by the Parsing API lock
        if (invalid || isClasspathInfoProvider) {
            if (invalid) {
                LOGGER.fine ("Invalid, reparse");    //NOI18N
            }
            if (isClasspathInfoProvider) {
                final ClasspathInfo providedInfo = ((ClasspathInfo.Provider)task).getClasspathInfo();
                if (providedInfo != null && !providedInfo.equals(cpInfo)) {
                    if (!snapshots.isEmpty()) {
                        LOGGER.log(Level.FINE, "Task {0} has changed ClasspathInfo form: {1} to:{2}", new Object[]{task, cpInfo, providedInfo}); //NOI18N
                    }
                    invalidate(true); //Reset initialized, world has changed.
                }
            }
            if (invalid) {
                assert cachedSnapShot != null || snapshots.isEmpty();
                try {
                    parseImpl(cachedSnapShot, task);
                } catch (FileObjects.InvalidFileException ife) {
                    //Deleted file
                    LOGGER.warning(ife.getMessage());
                    return null;
                } catch (IOException ioe) {
                    throw new ParseException ("JavacParser failure", ioe); //NOI18N
                }
            }
        }
        JavacParserResult result = null;
        if (isParserResultTask) {
            Phase requiredPhase;
            if (isJavaParserResultTask) {
                requiredPhase = ((JavaParserResultTask)task).getPhase();
            }
            else {
                requiredPhase = JavaSource.Phase.RESOLVED;
                LOGGER.log(Level.WARNING, "ParserResultTask: {0} doesn''t provide phase, assuming RESOLVED", task);                   //NOI18N
            }
            Phase reachedPhase;
            final DefaultCancelService cancelService = DefaultCancelService.instance(ciImpl.getJavacTask().getContext());
            if (cancelService != null) {
                cancelService.mayCancel.set(true);
            }
            try {
                reachedPhase = moveToPhase(requiredPhase, ciImpl, Collections.emptyList(), true);
            } catch (IOException ioe) {
                throw new ParseException ("JavacParser failure", ioe);      //NOI18N
            } finally {
                if (cancelService != null) {
                    cancelService.mayCancel.set(false);
                }
            }
            if (reachedPhase.compareTo(requiredPhase)>=0) {
                ClassIndexImpl.cancel.set(indexCanceled);
                result = new JavacParserResult(JavaSourceAccessor.getINSTANCE().createCompilationInfo(ciImpl));
            }
        }
        else if (isUserTask) {
            result = new JavacParserResult(JavaSourceAccessor.getINSTANCE().createCompilationController(ciImpl));
        }
        else {
            LOGGER.log(Level.WARNING, "Ignoring unknown task: {0}", task);                   //NOI18N
        }
        //Todo: shared = false should replace this
        //for now it creates a new parser and passes it outside the infrastructure
        //used by debugger private api
        if (task instanceof NewComilerTask) {
            final NewComilerTask nct = (NewComilerTask)task;
            if (nct.getCompilationController() == null || nct.getTimeStamp() != parseId) {
                try {
                    CompilationInfoImpl cii = new CompilationInfoImpl(this, file, root, null, null, cachedSnapShot, true, new HashMap<>(), new HashMap<>());
                    cii.setParsedTrees(new HashMap<>());
                    nct.setCompilationController(JavaSourceAccessor.getINSTANCE().createCompilationController(cii),
                        parseId);
                } catch (IOException ioe) {
                    throw new ParseException ("Javac Failure", ioe);
                }
            }
        }
        return result;
    }

    @Override
    public void cancel (final @NonNull CancelReason reason, final @NonNull SourceModificationEvent event) {
        indexCanceled.set(true);
        if (reason == CancelReason.SOURCE_MODIFICATION_EVENT && event.sourceChanged()) {
            parserCanceled.set(true);
        }
    }

    public void cancelParse() {
        parserCanceled.set(true);
    }

    public void resultFinished (boolean isCancelable) {
        if (isCancelable) {
            ClassIndexImpl.cancel.remove();
            indexCanceled.set(false);
        }
    }


    @Override
    public void addChangeListener(ChangeListener changeListener) {
        assert changeListener != null;
        this.listeners.addChangeListener(changeListener);
    }

    @Override
    public void removeChangeListener(ChangeListener changeListener) {
        assert changeListener != null;
        this.listeners.removeChangeListener(changeListener);
    }


    /**
     * Returns {@link ClasspathInfo} used by this javac
     * @return the ClasspathInfo
     */
    ClasspathInfo getClasspathInfo () {
        return this.cpInfo;
    }

    /**
     * Moves the Javac into the required {@link JavaSource#Phase}
     * Not synchronized, has to be called under Parsing API lock.
     * @param the required {@link JavaSource#Phase}
     * @param currentInfo - the javac
     * @param cancellable when true the method checks cancels
     * @return the reached phase
     * @throws IOException when the javac throws an exception
     */
    @SuppressWarnings("UseSpecificCatch")
    Phase moveToPhase (final Phase phase, final CompilationInfoImpl currentInfo, List<FileObject> forcedSources,
            final boolean cancellable) throws IOException {
        JavaSource.Phase parserError = currentInfo.parserCrashed;
        assert parserError != null;
        Phase currentPhase = currentInfo.getPhase();
        try {
            if (currentPhase.compareTo(Phase.PARSED) < 0 && phase.compareTo(Phase.PARSED) >= 0 && phase.compareTo(parserError) <= 0) {
                if (cancellable && parserCanceled.get()) {
                    //Keep the currentPhase unchanged, it may happen that an userActionTask
                    //runnig after the phace completion task may still use it.
                    return Phase.MODIFIED;
                }
                long start = System.currentTimeMillis();
                Iterable<? extends CompilationUnitTree> trees;
                Iterator<? extends CompilationUnitTree> it;
                CompilationUnitTree unit = null;
                if (currentInfo.getParsedTrees() != null && currentInfo.getParsedTrees().containsKey(currentInfo.jfo)) {
                    unit = currentInfo.getParsedTrees().get(currentInfo.jfo);
                } else {
                    if (sequentialParsing != null) {
                        trees = sequentialParsing.parse(currentInfo.getJavacTask(), currentInfo.jfo);
                    } else {
                        List<FileObject> files = new ArrayList<>();

                        if (perFileProcessing) {
                            files.add(file);
                        } else {
                            snapshots.stream()
                                     .map(s -> s.getSource().getFileObject())
                                     .forEach(files::add);
                        }

                        files.addAll(forcedSources);

                        JavacTaskImpl javacTask = currentInfo.getJavacTask(files);

                        trees = javacTask.parse();
                    }
                    if (unit == null) {
                        if (trees == null) {
                            LOGGER.log(Level.INFO, "Did not parse anything for: {0}", currentInfo.jfo.toUri()); //NOI18N
                            return Phase.MODIFIED;
                        }
                        it = trees.iterator();
                        if (!it.hasNext()) {
                            LOGGER.log(Level.INFO, "Did not parse anything for: {0}", currentInfo.jfo.toUri()); //NOI18N
                            return Phase.MODIFIED;
                        }

                        while (it.hasNext()) {
                            CompilationUnitTree oneFileTree = it.next();
                            currentInfo.getParsedTrees().put(oneFileTree.getSourceFile(), oneFileTree);
                        }
                        unit = trees.iterator().next();
                    }

                }

                currentInfo.setCompilationUnit(unit);

                final Document doc = currentInfo.getDocument();
                if (doc != null && supportsReparse) {
                    final FindMethodRegionsVisitor v = new FindMethodRegionsVisitor(doc,Trees.instance(currentInfo.getJavacTask()).getSourcePositions(),this.parserCanceled, unit);
                    doc.render(v);
                    synchronized (positions) {
                        positions.clear();
                        if (!parserCanceled.get()) {
                            positions.addAll(v.getResult());
                        }
                    }
                }
                currentPhase = Phase.PARSED;
                long end = System.currentTimeMillis();
                FileObject currentFile = currentInfo.getFileObject();
                TIME_LOGGER.log(Level.FINE, "Compilation Unit",
                    new Object[] {currentFile, unit});

                logTime (currentFile,currentPhase,(end-start));
            }
            if (currentPhase == Phase.PARSED && phase.compareTo(Phase.ELEMENTS_RESOLVED)>=0 && phase.compareTo(parserError)<=0) {
                if (cancellable && parserCanceled.get()) {
                    return Phase.MODIFIED;
                }
                long start = System.currentTimeMillis();
                Supplier<Object> setJavacHandler = () -> null;
                Consumer<Object> restoreHandler = h -> {};
                try {
                    //the DeferredCompletionFailureHandler should be set to javac mode:
                    Class<?> dcfhClass = Class.forName("com.sun.tools.javac.code.DeferredCompletionFailureHandler");
                    Class<?> dcfhHandlerClass = Class.forName("com.sun.tools.javac.code.DeferredCompletionFailureHandler$Handler");
                    Object dcfh = dcfhClass.getDeclaredMethod("instance", Context.class).invoke(null, currentInfo.getJavacTask().getContext());
                    Method setHandler = dcfhClass.getDeclaredMethod("setHandler", dcfhHandlerClass);
                    Object javacCodeHandler = dcfhClass.getDeclaredField("javacCodeHandler").get(dcfh);

                    setJavacHandler = () -> {
                        try {
                            return setHandler.invoke(dcfh, javacCodeHandler);
                        } catch (ReflectiveOperationException ex) {
                            LOGGER.log(Level.FINE, null, ex);
                            return null;
                        }
                    };
                    restoreHandler = h -> {
                        if (h != null) {
                            try {
                                setHandler.invoke(dcfh, h);
                            } catch (ReflectiveOperationException ex) {
                                LOGGER.log(Level.WARNING, null, ex);
                            }
                        }
                    };
                } catch (ReflectiveOperationException | SecurityException ex) {
                    //ignore
                    LOGGER.log(Level.FINEST, null, ex);
                }
                Object oldHandler = setJavacHandler.get();
                try {
                    currentInfo.getJavacTask().enter();
                } finally {
                    restoreHandler.accept(oldHandler);
                }
                currentPhase = Phase.ELEMENTS_RESOLVED;
                long end = System.currentTimeMillis();
                logTime(currentInfo.getFileObject(),currentPhase,(end-start));
           }
           if (currentPhase == Phase.ELEMENTS_RESOLVED && phase.compareTo(Phase.RESOLVED)>=0 && phase.compareTo(parserError)<=0) {
                if (cancellable && parserCanceled.get()) {
                    return Phase.MODIFIED;
                }
                long start = System.currentTimeMillis ();
                JavacTaskImpl jti = currentInfo.getJavacTask();
                JavaCompiler compiler = JavaCompiler.instance(jti.getContext());
                List<Env<AttrContext>> savedTodo = new ArrayList<>(compiler.todo);
                try {
                    List<FileObject> currentFileObjects = new ArrayList<>();
                    currentFileObjects.addAll(forcedSources);
                    currentFileObjects.add(file);
                    List<AbstractSourceFileObject> currentFiles = currentInfo.getFiles(currentFileObjects);
                    compiler.todo.retainFiles(currentFiles);
                    savedTodo.removeAll(compiler.todo);
                    PostFlowAnalysis.analyze(jti.analyze(), jti.getContext());
                } finally {
                    for (Env<AttrContext> env : savedTodo) {
                        compiler.todo.offer(env);
                    }
                }
                currentPhase = Phase.RESOLVED;
                long end = System.currentTimeMillis ();
                logTime(currentInfo.getFileObject(),currentPhase,(end-start));
            }
            if (currentPhase == Phase.RESOLVED && phase.compareTo(Phase.UP_TO_DATE)>=0) {
                currentPhase = Phase.UP_TO_DATE;
            }
        } catch (Throwable ex) {
            if (AbortChecker.isCancelAbort(ex)) {
                if (lowMemoryCancel.get()) {
                    currentInfo.markIncomplete();
                    HUGE_SNAPSHOTS.add(new WeakReference<>(snapshots));
                } else {
                    //real cancel
                    currentPhase = Phase.MODIFIED;
                    invalidate(false);
                }
            } else if (AbortChecker.isAbort(ex)) {
                parserError = currentPhase;
            } else {
                if (lowMemoryCancel.get()) {
                    currentInfo.markIncomplete();
                    HUGE_SNAPSHOTS.add(new WeakReference<>(snapshots));
                } else {
                    if (cancellable && parserCanceled.get()) {
                        currentPhase = Phase.MODIFIED;
                        invalidate(false);
                    } else {
                        parserError = currentPhase;
                        dumpSource(currentInfo, ex);
                        throw ex;
                    }
                }
            }
        } finally {
            currentInfo.setPhase(currentPhase);
            currentInfo.parserCrashed = parserError;
        }
        return currentPhase;
    }

    private static CompilationInfoImpl createCurrentInfo (final JavacParser parser,
            final FileObject file,
            final FileObject root,
            final Snapshot snapshot,
            final JavacTaskImpl javac,
            final DiagnosticListener<JavaFileObject> diagnosticListener,
            final Map<JavaFileObject, CompilationUnitTree> parsedTrees,
            final Map<FileObject, AbstractSourceFileObject> ide2javacFileObject,
            final Map<FileObject, Snapshot> file2Snapshot) throws IOException {
        CompilationInfoImpl info = new CompilationInfoImpl(parser, file, root, javac, diagnosticListener, snapshot, false, ide2javacFileObject, file2Snapshot);
        if (file != null) {
            Logger.getLogger("TIMER").log(Level.FINE, "CompilationInfo",    //NOI18N
                    new Object[] {file, info});
        }
        info.setParsedTrees(parsedTrees);
        return info;
    }

    static JavacTaskImpl createJavacTask(
            final FileObject file,
            final Iterable<? extends JavaFileObject> jfos,
            final FileObject root,
            final ClasspathInfo cpInfo,
            final JavacParser parser,
            final DiagnosticListener<? super JavaFileObject> diagnosticListener,
            final boolean detached) {
        if (file != null) {
            if (LOGGER.isLoggable(Level.FINER)) {
                LOGGER.log(Level.FINER, "Created new JavacTask for: {0}", FileUtil.getFileDisplayName(file));
            }
        }
        FQN2Files fqn2Files = null;
        if (root != null) {
            try {
                fqn2Files = FQN2Files.forRoot(root.toURL());
            } catch (IOException ex) {
                LOGGER.log(Level.FINE, null, ex);
            }
        }
        final Set<ConfigFlags> flags = EnumSet.noneOf(ConfigFlags.class);
        final Optional<JavacParser> mayBeParser = Optional.ofNullable(parser);
        if (mayBeParser.filter((p) -> p.snapshots.size() > 1).isPresent()) {
            flags.add(ConfigFlags.MULTI_SOURCE);
        }
        if (Optional.ofNullable(mayBeParser.map(p->(p.file))
                .orElse(file))
                .filter((f)->FileObjects.MODULE_INFO.equals(f.getName())&&FileObjects.CLASS.equals(f.getExt())).isPresent()) {
            flags.add(ConfigFlags.MODULE_INFO);
        }
        try(final ModuleOraculum mo = ModuleOraculum.getInstance()) {
            final Set<String> additionalModules = new HashSet<>();
            if (!mo.installModuleName(root, file) && file == null) {
                ClassPath cp = cpInfo.getClassPath(PathKind.SOURCE);
                if (cp != null) {
                    cp.entries().stream()
                            .map((e) -> {
                                try {
                                    return JavaIndex.getAttribute(e.getURL(), JavaIndex.ATTR_MODULE_NAME, null);
                                } catch (IOException ioe) {
                                    return null;
                                }
                            })
                            .filter((modName) -> modName != null)
                            .forEach(additionalModules::add);
                }
            }
            final FileObject artefact = root != null ?
                    root :
                    file;
            final CompilerOptionsQuery.Result compilerOptions;
            final SourceLevelQuery.Result sourceLevel;
            if (artefact != null) {
                compilerOptions = CompilerOptionsQuery.getOptions(artefact);
                sourceLevel = SourceLevelQuery.getSourceLevel2(artefact);
            } else {
                compilerOptions = null;
                sourceLevel = null;
            }
            final JavacTaskImpl javacTask = createJavacTask(cpInfo,
                    diagnosticListener,
                    sourceLevel != null ? sourceLevel.getSourceLevel() : null,
                    sourceLevel != null ? sourceLevel.getProfile() : null,
                    flags,
                    fqn2Files,
                    parser == null ? null : new DefaultCancelService(parser),
                    APTUtils.get(root),
                    compilerOptions,
                    additionalModules,
                    jfos);
            Lookup.getDefault()
                  .lookupAll(TreeLoaderRegistry.class)
                  .stream()
                  .forEach(r -> r.enhance(javacTask.getContext(), cpInfo, detached));
            ParameterNameProviderImpl.register(javacTask, cpInfo);
            return javacTask;
        }
    }

    public static JavacTaskImpl createJavacTask (
            @NonNull final ClasspathInfo cpInfo,
            @NullAllowed final DiagnosticListener<? super JavaFileObject> diagnosticListener,
            @NullAllowed final String sourceLevel,
            @NullAllowed final SourceLevelQuery.Profile sourceProfile,
            @NullAllowed FQN2Files fqn2Files,
            @NullAllowed final CancelService cancelService,
            @NullAllowed final APTUtils aptUtils,
            @NullAllowed final CompilerOptionsQuery.Result compilerOptions,
            @NonNull Iterable<? extends JavaFileObject> files) {
        return createJavacTask(
                cpInfo,
                diagnosticListener,
                sourceLevel,
                sourceProfile,
                EnumSet.of(ConfigFlags.BACKGROUND_COMPILATION, ConfigFlags.MULTI_SOURCE),
                fqn2Files,
                cancelService,
                aptUtils,
                compilerOptions,
                Collections.emptySet(),
                files);
    }

    private static enum ConfigFlags {
        BACKGROUND_COMPILATION,
        MULTI_SOURCE,
        MODULE_INFO
    }

    private static JavacTaskImpl createJavacTask(
            @NonNull final ClasspathInfo cpInfo,
            @NullAllowed final DiagnosticListener<? super JavaFileObject> diagnosticListener,
            @NullAllowed final String sourceLevel,
            @NullAllowed SourceLevelQuery.Profile sourceProfile,
            @NonNull final Set<? extends ConfigFlags> flags,
            @NullAllowed FQN2Files fqn2Files,
            @NullAllowed final CancelService cancelService,
            @NullAllowed final APTUtils aptUtils,
            @NullAllowed final CompilerOptionsQuery.Result compilerOptions,
            @NonNull final Collection<? extends String> additionalModules,
            @NonNull Iterable<? extends JavaFileObject> files) {
        final boolean backgroundCompilation = flags.contains(ConfigFlags.BACKGROUND_COMPILATION);
        final boolean multiSource = flags.contains(ConfigFlags.MULTI_SOURCE);
        final List<String> options = new ArrayList<>();
        String lintOptions = CompilerSettings.getCommandLine(cpInfo);
        com.sun.tools.javac.code.Source validatedSourceLevel = validateSourceLevel(
                sourceLevel,
                cpInfo,
                flags.contains(ConfigFlags.MODULE_INFO));
        String useRelease = useRelease(sourceLevel, validatedSourceLevel);
        if (lintOptions.length() > 0) {
            options.addAll(Arrays.asList(lintOptions.split(" ")));
        }
        if (!backgroundCompilation) {
            options.add("-Xjcov"); //NOI18N, Make the compiler store end positions
            options.add("-XDallowStringFolding=false"); //NOI18N
            options.add("-XDkeepComments=true"); //NOI18N
            assert options.add("-XDdev") || true; //NOI18N
        } else {
            options.add("-XDbackgroundCompilation");    //NOI18N
            options.add("-XDcompilePolicy=byfile");     //NOI18N
            options.add("-XD-Xprefer=source");     //NOI18N
            if (useRelease == null) {
                options.add("-target");                     //NOI18N
                options.add(validatedSourceLevel.requiredTarget().name);
            }
        }
        options.add("-XDide");   // NOI18N, javac runs inside the IDE
        if (!DISABLE_PARAMETER_NAMES_READING) {
            options.add("-XDsave-parameter-names");   // NOI18N, javac runs inside the IDE
            options.add("-parameters");   // NOI18N, save and read parameter names
        }
        options.add("-XDsuppressAbortOnBadClassFile");   // NOI18N, when a class file cannot be read, produce an error type instead of failing with an exception
        options.add("-XDshould-stop.at=GENERATE");   // NOI18N, parsing should not stop in phase where an error is found
        options.add("-g:source"); // NOI18N, Make the compiler to maintian source file info
        options.add("-g:lines"); // NOI18N, Make the compiler to maintain line table
        options.add("-g:vars");  // NOI18N, Make the compiler to maintain local variables table
        if (useRelease != null) {
            options.add("--release");  // NOI18N
            options.add(useRelease);
        } else {
            options.add("-source");  // NOI18N
            options.add(validatedSourceLevel.name);
        }
        if (sourceProfile != null &&
            sourceProfile != SourceLevelQuery.Profile.DEFAULT) {
            options.add("-profile");    //NOI18N, Limit JRE to required compact profile
            options.add(sourceProfile.getName());
        }
        options.add("-XDdiags.formatterOptions=-source");  // NOI18N
        options.add("-XDdiags.layout=%L%m|%L%m|%L%m");  // NOI18N
        options.add("-XDbreakDocCommentParsingOnError=false");  // NOI18N
        boolean aptEnabled = aptUtils != null &&
                aptUtils.aptEnabledOnScan() &&
                (backgroundCompilation || (aptUtils.aptEnabledInEditor() && !multiSource)) &&
                hasSourceCache(cpInfo, aptUtils);
        Collection<? extends Processor> processors = null;
        if (aptEnabled) {
            assert aptUtils != null;
            processors = aptUtils.resolveProcessors(backgroundCompilation);
            if (processors.isEmpty()) {
                aptEnabled = false;
            } else {
                for (Processor p : processors) {
                    if ("lombok.core.AnnotationProcessor".equals(p.getClass().getName())) {
                        options.add("-XD" + LOMBOK_DETECTED);
                        break;
                    }
                }
            }
        }
        if (aptEnabled) {
            assert aptUtils != null;
            for (Map.Entry<? extends String, ? extends String> entry : aptUtils.processorOptions().entrySet()) {
                StringBuilder sb = new StringBuilder();
                sb.append("-A").append(entry.getKey()); //NOI18N
                if (entry.getValue() != null) {
                    sb.append('=').append(entry.getValue()); //NOI18N
                }
                options.add(sb.toString());
            }
            options.add("-proc:full");
        } else {
            options.add("-proc:none"); // NOI18N, Disable annotation processors
        }
        if (compilerOptions != null) {
            for (String compilerOption : validateCompilerOptions(compilerOptions.getArguments(), validatedSourceLevel)) {
                options.add(compilerOption);
            }
        }
        if (!additionalModules.isEmpty()) {
            options.add("--add-modules");       //NOI18N
            options.add(additionalModules.stream().collect(Collectors.joining(",")));   //NOI18N
        }

        //filter out classfiles:
        files = StreamSupport.stream(files.spliterator(), false)
                             .filter(file -> file.getKind() == Kind.SOURCE)
                             .collect(Collectors.toList());

        Context context = new Context();
        //need to preregister the Messages here, because the getTask below requires Log instance:
        NBLog.preRegister(context, DEV_NULL);
        NBNames.preRegister(context);
        JavacTaskImpl task = (JavacTaskImpl)JavacTool.create().getTask(null,
                ClasspathInfoAccessor.getINSTANCE().createFileManager(cpInfo, validatedSourceLevel.name),
                diagnosticListener, options, files.iterator().hasNext() ? null : Arrays.asList("java.lang.Object"), files,
                context);
        if (aptEnabled) {
            task.setProcessors(processors);
            ProcessorHolder.instance(context).setProcessors(processors);
        }
        NBClassReader.preRegister(context);
        Lookup.getDefault()
              .lookupAll(ContextEnhancer.class)
              .stream()
              .forEach(r -> r.enhance(context, backgroundCompilation));
        Lookup.getDefault()
              .lookupAll(DuplicateClassRegistry.class)
              .stream()
              .forEach(r -> r.enhance(context, fqn2Files));
        if (cancelService != null) {
            DefaultCancelService.preRegister(context, cancelService);
        }
        NBAttr.preRegister(context);
        NBClassWriter.preRegister(context);
        NBParserFactory.preRegister(context);
        NBTreeMaker.preRegister(context);
        NBJavacTrees.preRegister(context);
        if (!backgroundCompilation) {
            JavacFlowListener.preRegister(context, task);
        }
        NBResolve.preRegister(context);
        NBEnter.preRegister(context);
        NBMemberEnter.preRegister(context, backgroundCompilation);
        TIME_LOGGER.log(Level.FINE, "JavaC", context);
        return task;
    }

    private static String useRelease(final String requestedSource, com.sun.tools.javac.code.Source validatedSourceLevel) {
        if (requestedSource == null || validatedSourceLevel == null) {
            return null;
        }
        com.sun.tools.javac.code.Source sourceLevel = com.sun.tools.javac.code.Source.lookup(requestedSource);
        if (sourceLevel == null) {
            return null;
        }
        if (validatedSourceLevel.equals(sourceLevel)) {
            return null;
        }
        if (sourceLevel.compareTo(com.sun.tools.javac.code.Source.JDK7) <= 0) {
            sourceLevel = com.sun.tools.javac.code.Source.JDK7;
        }
        return sourceLevel.isSupported() ? sourceLevel.requiredTarget().multiReleaseValue() : null;
    }

    @SuppressWarnings("PublicField") // test
    public static boolean DISABLE_SOURCE_LEVEL_DOWNGRADE = false;
    static @NonNull com.sun.tools.javac.code.Source validateSourceLevel(
            @NullAllowed String sourceLevel,
            @NonNull final ClasspathInfo cpInfo,
            final boolean isModuleInfo) {
        return validateSourceLevel(
                sourceLevel,
                cpInfo.getClassPath(PathKind.BOOT),
                cpInfo.getClassPath(PathKind.COMPILE),
                cpInfo.getClassPath(PathKind.SOURCE),
                cpInfo.getClassPath(PathKind.MODULE_BOOT),
                cpInfo.getClassPath(PathKind.MODULE_COMPILE),
                cpInfo.getClassPath(PathKind.MODULE_CLASS),
                isModuleInfo);
    }

    @NonNull
    public static com.sun.tools.javac.code.Source validateSourceLevel(
            @NullAllowed String sourceLevel,
            @NullAllowed final ClassPath bootClassPath,
            @NullAllowed final ClassPath classPath,
            @NullAllowed final ClassPath srcClassPath,
            @NullAllowed final ClassPath moduleBoot,
            @NullAllowed final ClassPath moduleCompile,
            @NullAllowed final ClassPath moduleAllUnnamed,
            final boolean isModuleInfo) {
        com.sun.tools.javac.code.Source[] sources = com.sun.tools.javac.code.Source.values();
        Level warnLevel;
        if (sourceLevel == null) {
            //automatically use highest source level that is satisfied by the given boot classpath:
            sourceLevel = sources[sources.length-1].name;
            warnLevel = Level.FINE;
        } else {
            if (isModuleInfo) {
                //Module info requires at least 9 otherwise module.compete fails with ISE.
                final com.sun.tools.javac.code.Source java9 = SourceLevelUtils.JDK1_9;
                final com.sun.tools.javac.code.Source required = com.sun.tools.javac.code.Source.lookup(sourceLevel);
                if (required == null || required.compareTo(java9) < 0) {
                    sourceLevel = java9.name;
                }
            }
            warnLevel = Level.WARNING;
        }
        for (com.sun.tools.javac.code.Source source : sources) {
            if (source == com.sun.tools.javac.code.Source.lookup(sourceLevel)) {
                if (DISABLE_SOURCE_LEVEL_DOWNGRADE || isModuleInfo) {
                    return source;
                }
                if (source.compareTo(com.sun.tools.javac.code.Source.JDK1_4) >= 0) {
                    if (bootClassPath != null && bootClassPath.findResource("java/lang/AssertionError.class") == null) { //NOI18N
                        final boolean checkCp = bootClassPath.findResource("java/lang/Object.class") == null;
                        if (!hasResource("java/lang/AssertionError", new ClassPath[] {ClassPath.EMPTY}, new ClassPath[] {checkCp ? classPath : ClassPath.EMPTY}, new ClassPath[] {srcClassPath})) { // NOI18N
                            LOGGER.log(warnLevel,
                                       "Even though the source level of {0} is set to: {1}, java.lang.AssertionError cannot be found on the bootclasspath: {2}\n", // NOI18N
                                       new Object[]{srcClassPath, sourceLevel, bootClassPath}); //NOI18N
                            return com.sun.tools.javac.code.Source.JDK1_3;
                        }
                    }
                }
                if (source.compareTo(SourceLevelUtils.JDK1_5) >= 0 &&
                    !hasResource("java/lang/StringBuilder", new ClassPath[] {bootClassPath}, new ClassPath[] {classPath}, new ClassPath[] {srcClassPath})) { //NOI18N
                    LOGGER.log(warnLevel,
                               "Even though the source level of {0} is set to: {1}, java.lang.StringBuilder cannot be found on the bootclasspath: {2}\n",
                               new Object[]{srcClassPath, sourceLevel, bootClassPath}); //NOI18N
                    return com.sun.tools.javac.code.Source.JDK1_4;
                }
                if (source.compareTo(SourceLevelUtils.JDK1_7) >= 0 &&
                    !hasResource("java/lang/AutoCloseable", new ClassPath[] {bootClassPath}, new ClassPath[] {classPath}, new ClassPath[] {srcClassPath})) { //NOI18N
                    LOGGER.log(warnLevel,
                               "Even though the source level of {0} is set to: {1}, java.lang.AutoCloseable cannot be found on the bootclasspath: {2}\n", //NOI18N
                               new Object[]{srcClassPath, sourceLevel, bootClassPath}); //NOI18N
                }
                if (source.compareTo(SourceLevelUtils.JDK1_8) >= 0 &&
                    !hasResource("java/lang/invoke/LambdaMetafactory", new ClassPath[] {bootClassPath}, new ClassPath[] {classPath}, new ClassPath[] {srcClassPath})) { //NOI18N
                    LOGGER.log(warnLevel,
                               "Even though the source level of {0} is set to: {1}, java.lang.invoke.LambdaMetafactory cannot be found on the bootclasspath: {2}\n", //NOI18N
                               new Object[]{srcClassPath, sourceLevel, bootClassPath}); //NOI18N
                    return SourceLevelUtils.JDK1_7;
                }
                if (source.compareTo(SourceLevelUtils.JDK1_9) >= 0 &&
                    !hasResource("java/util/zip/CRC32C", new ClassPath[] {moduleBoot}, new ClassPath[] {moduleCompile, moduleAllUnnamed}, new ClassPath[] {srcClassPath})) { //NOI18N
                    LOGGER.log(warnLevel,
                               "Even though the source level of {0} is set to: {1}, java.util.zip.CRC32C cannot be found on the system module path: {2}\n", //NOI18N
                               new Object[]{srcClassPath, sourceLevel, moduleBoot}); //NOI18N
                    return SourceLevelUtils.JDK1_8;
                }
                if (source.compareTo(SourceLevelUtils.JDK15) >= 0 &&
                    !hasResource("java/lang/Record", new ClassPath[] {moduleBoot}, new ClassPath[] {moduleCompile, moduleAllUnnamed}, new ClassPath[] {srcClassPath})) { //NOI18N
                    LOGGER.log(warnLevel,
                               "Even though the source level of {0} is set to: {1}, java.lang.Record cannot be found on the system module path: {2}\n", //NOI18N
                               new Object[]{srcClassPath, sourceLevel, moduleBoot}); //NOI18N
                    return SourceLevelUtils.JDK14;
                }
                return source;
            }
        }
        SpecificationVersion specVer = new SpecificationVersion (sourceLevel);
        SpecificationVersion JAVA_12 = new SpecificationVersion ("1.2");   //NOI18N
        if (JAVA_12.compareTo(specVer)>0) {
            //Some SourceLevelQueries return 1.1 source level which is invalid, use 1.2
            return sources[0];
        }
        else {
            return sources[sources.length-1];
        }
    }

    @NonNull
    @SuppressWarnings({"AssignmentToForLoopParameter", "ValueOfIncrementOrDecrementUsed"})
    public static List<? extends String> validateCompilerOptions(@NonNull final List<? extends String> options, @NullAllowed com.sun.tools.javac.code.Source sourceLevel) {
        final List<String> res = new ArrayList<>();
        boolean allowModularOptions = sourceLevel == null || com.sun.tools.javac.code.Source.lookup("9").compareTo(sourceLevel) <= 0;
        boolean xmoduleSeen = false;
        for (int i = 0; i < options.size(); i++) {
            String option = options.get(i);
            if (option.startsWith("-Xmodule:") && !xmoduleSeen) {   //NOI18N
                LOGGER.log(
                        Level.WARNING,
                        "Removed javac option -Xmodule: {0}",   //NOI18N
                        option);
                //Compatibility handle -Xmodule
                res.add(NB_X_MODULE + option.substring("-Xmodule:".length()));  //NOI18N
                xmoduleSeen = true;
            } else if (option.startsWith("-XD-Xmodule:") && !xmoduleSeen) { //NOI18N
                //Compatibility handle -XD-Xmodule:
                res.add(NB_X_MODULE + option.substring("-XD-Xmodule:".length()));  //NOI18N
                xmoduleSeen = true;
            } else if (option.startsWith(NB_X_MODULE) && !xmoduleSeen) { //NOI18N
                res.add(option);  //NOI18N
                xmoduleSeen = true;
            } else if (option.equals("-parameters") || option.startsWith("-Xlint")) {     //NOI18N
                res.add(option);
            } else if (option.equals("--enable-preview")) {     //NOI18N
                res.add(option);
            } else if (option.equals("-XDrawDiagnostics")) {     //NOI18N
                res.add(option);
            } else if ((
                    option.startsWith("--add-modules") ||   //NOI18N
                    option.startsWith("--limit-modules") || //NOI18N
                    option.startsWith("--add-exports") ||   //NOI18N
                    option.startsWith("--add-reads")  ||
                    option.startsWith(OPTION_PATCH_MODULE)) &&
                    allowModularOptions) {
                int idx = option.indexOf('=');
                if (idx > 0) {
                   res.add(option);
                } else if (i+1 < options.size()) {
                    res.add(option);
                    option = options.get(++i);
                    res.add(option);
                }
            }
        }
        return res;
    }

    private static boolean hasResource(
        @NonNull String resourceBase,
        @NonNull ClassPath[] boot,
        @NonNull ClassPath[] compile,
        @NonNull ClassPath[] source) {
        final String resourceClass = String.format("%s.class", resourceBase);    //NOI18N
        final String resourceJava = String.format("%s.java", resourceBase);      //NOI18N
        if (!hasResource(resourceClass, boot)) {
            if (!hasResource(resourceJava, source)) {
                if (!hasResource(resourceClass, compile)) {
                    return false;
                }
            }
        }
        return true;
    }

    private static boolean hasSourceCache(
            @NonNull final ClasspathInfo cpInfo,
            @NonNull final APTUtils aptUtils) {
        final List<? extends ClassPath.Entry> entries = ClasspathInfoAccessor.getINSTANCE().getCachedClassPath(cpInfo, PathKind.SOURCE).entries();
        if (entries.isEmpty()) {
            return false;
        }
        final URL sourceRoot = aptUtils.getRoot().toURL();
        for (ClassPath.Entry entry : entries) {
            if (sourceRoot.equals(entry.getURL())) {
                return true;
            }
        }
        return false;
    }

     private static boolean hasResource(
             @NonNull final String resource,
             @NonNull final ClassPath... cps) {
         for (ClassPath cp : cps) {
            if (cp != null && cp.findResource(resource) != null) {
                return true;
            }
         }
         return false;
     }

    public static void logTime (FileObject source, Phase phase, long time) {
        assert source != null && phase != null;
        String message = phase2Message.get(phase);
        assert message != null;
        TIME_LOGGER.log(Level.FINE, message, new Object[] {source, time});
    }

    public static File createDumpFile(CompilationInfoImpl info) {
        String userDir = System.getProperty("netbeans.user");
        if (userDir == null) {
            return null;
        }
        String dumpDir =  userDir + "/var/log/"; //NOI18N
        FileObject file = info.getFileObject();
        String origName = file.getName();
        File f = new File(dumpDir + origName + ".dump"); // NOI18N
        int i = 1;
        while (i < MAX_DUMPS) {
            if (!f.exists()) {
                break;
            }
            f = new File(dumpDir + origName + '_' + i + ".dump"); // NOI18N
            i++;
        }
        return !f.exists() ? f : null;
    }

    /**
     * Dumps the source code to the file. Used for parser debugging. Only a limited number
     * of dump files is used. If the last file exists, this method doesn't dump anything.
     *
     * @param  info  CompilationInfo for which the error occurred.
     * @param  exc  exception to write to the end of dump file
     */
    @SuppressWarnings("LoggerStringConcat")
    public static void dumpSource(CompilationInfoImpl info, Throwable exc) {
        String src = info.getText();
        FileObject file = info.getFileObject();
        String fileName = FileUtil.getFileDisplayName(file);
        File f = createDumpFile(info);
        boolean dumpSucceeded = false;
        if (f != null) {
            try {
                OutputStream os = new FileOutputStream(f);
                try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(os, StandardCharsets.UTF_8))) {
                    writer.println(src);
                    writer.println("----- Classpath: ---------------------------------------------"); // NOI18N

                    final ClassPath bootPath   = info.getClasspathInfo().getClassPath(ClasspathInfo.PathKind.BOOT);
                    final ClassPath classPath  = info.getClasspathInfo().getClassPath(ClasspathInfo.PathKind.COMPILE);
                    final ClassPath sourcePath = info.getClasspathInfo().getClassPath(ClasspathInfo.PathKind.SOURCE);

                    writer.println("bootPath: " + (bootPath != null ? bootPath.toString() : "null"));
                    writer.println("classPath: " + (classPath != null ? classPath.toString() : "null"));
                    writer.println("sourcePath: " + (sourcePath != null ? sourcePath.toString() : "null"));

                    writer.println("----- Original exception ---------------------------------------------"); // NOI18N
                    exc.printStackTrace(writer);
                } finally {
                    dumpSucceeded = true;
                }
            } catch (IOException ioe) {
                LOGGER.log(Level.INFO, "Error when writing parser dump file!", ioe); // NOI18N
            }
        }
        if (dumpSucceeded) {
            assert f != null;
            try {
                Throwable t = Exceptions.attachMessage(exc, "An error occurred during parsing of \'" + fileName + "\'. Please report a bug against java/source and attach dump file '"  // NOI18N
                        + f.getAbsolutePath() + "'."); // NOI18N
                Exceptions.printStackTrace(t);
            } catch (RuntimeException re) {
                //There was already a call exc.initCause(null) which causes RE when a another initCause() is called.
                //Print at least the original exception
                Exceptions.printStackTrace(exc);
            }
        } else {
            LOGGER.log(Level.WARNING,
                    "Dump could not be written. Either dump file could not " + // NOI18N
                    "be created or all dump files were already used. Please " + // NOI18N
                    "check that you have write permission to '" + (f != null ? f.getParent() : "var/log") + "' and " + // NOI18N
                    "clean all *.dump files in that directory."); // NOI18N
        }
    }

    //Helper classes
    private static class DefaultCancelService extends CancelService {

        //May be the parser canceled inside javac?
        final AtomicBoolean mayCancel = new AtomicBoolean();
        private final JavacParser parser;

        private DefaultCancelService(final JavacParser parser) {
            this.parser = parser;
        }

        public static void preRegister(Context context, CancelService cancelServiceToRegister) {
            context.put(cancelServiceKey, cancelServiceToRegister);
        }

        public static DefaultCancelService instance(final Context ctx) {
            assert ctx != null;
            final CancelService cancelService = CancelService.instance(ctx);
            return (cancelService instanceof DefaultCancelService) ? (DefaultCancelService) cancelService : null;
        }

        @Override
        @SuppressWarnings("AccessingNonPublicFieldOfAnotherObject")
        public boolean isCanceled() {
            if (mayCancel.get() && parser.parserCanceled.get()) {
                return true;
            }
            if (!parser.perFileProcessing && LOW_MEMORY_WATCHER.isLowMemory()) {
                parser.lowMemoryCancel.set(true);
                return true;
            }
            return false;
        }
    }

    private void checkSourceModification(SourceModificationEvent evt) {
        if (evt instanceof SourceModificationEvent.Composite) {
            evt = ((SourceModificationEvent.Composite)evt).getWriteEvent();
        }
        if (evt != null && evt.sourceChanged()) {
            Pair<DocPositionRegion,MethodTree> changedMethod = null;
            if (supportsReparse) {
                int start = evt.getAffectedStartOffset();
                int end = evt.getAffectedEndOffset();
                synchronized (positions) {
                    for (Pair<DocPositionRegion,MethodTree> pe : positions) {
                        PositionRegion p = pe.first();
                        if (start > p.getStartOffset() && end < p.getEndOffset()) {
                            changedMethod = pe;
                            break;
                        }
                    }
                    // PENDING - changed method lbrace/rbrace handling ?
                    positions.clear();
                    if (changedMethod!=null) {
                        positions.add (changedMethod);
                    }
                    JavacParser.this.changedMethod.set(changedMethod);
                }
            }
        } else {
            positions.clear();
            JavacParser.this.changedMethod.set(null);
        }
    }

    /**
     * For unit tests only
     * Used by JavaSourceTest.testIncrementalReparse
     * @param changedMethod
     */
    public synchronized void setChangedMethod (final Pair<DocPositionRegion,MethodTree> changedMethod) {
        assert changedMethod != null;
        this.changedMethod.set(changedMethod);
    }

    /**
     * Filter listener to listen on j2me preprocessor
     */
    private final class FilterListener implements ChangeListener {

        @SuppressWarnings("LeakingThisInConstructor")
        public FilterListener (final JavaFileFilterImplementation filter) {
            filter.addChangeListener(WeakListeners.change(this, filter));
        }

        @Override
        public void stateChanged(ChangeEvent event) {
            listeners.fireChange();
        }
    }
    
    public static interface PartialReparser {
        public boolean reparseMethod (final CompilationInfoImpl ci,
                final Snapshot snapshot,
                final MethodTree orig,
                final String newBody) throws IOException;
    }
    
    @ServiceProvider(service = PartialReparser.class, position = 1000)
    public static class DefaultPartialReparser implements PartialReparser {

        @Override
        public boolean reparseMethod(CompilationInfoImpl ci, Snapshot snapshot, MethodTree orig, String newBody) throws IOException {
            return false;
        }
        
    }
    
    public static class ProcessorHolder {
        public static ProcessorHolder instance(Context ctx) {
            ProcessorHolder instance = ctx.get(ProcessorHolder.class);

            if (instance == null) {
                instance = new ProcessorHolder();
                ctx.put(ProcessorHolder.class, instance);
            }

            return instance;
        }

        private Collection<? extends Processor> processors;

        @SuppressWarnings("AssignmentToCollectionOrArrayFieldFromParameter")
        public void setProcessors(Collection<? extends Processor> processors) {
            this.processors = processors;
        }

        @SuppressWarnings("ReturnOfCollectionOrArrayField")
        public Collection<? extends Processor> getProcessors() {
            return processors;
        }
    }

    public static interface TreeLoaderRegistry {
        public void enhance(Context context, ClasspathInfo cpInfo, boolean detached);
    }
    
    public static interface DuplicateClassRegistry {
        public void enhance(Context context, FQN2Files fqn2Files);
    }
    
    public static interface ContextEnhancer {
        public void enhance(Context context, boolean backgroundCompilation);
    }

    @ServiceProvider(service=ContextEnhancer.class)
    public static class VanillaJavacContextEnhancer implements ContextEnhancer {
        @Override
        public void enhance(Context context, boolean backgroundCompilation) {
            NBClassFinder.preRegister(context);
            NBJavaCompiler.preRegister(context);
        }
    }

    public static interface SequentialParsing {
        public Iterable<? extends CompilationUnitTree> parse(JavacTask task, JavaFileObject file) throws IOException;
    }
}
