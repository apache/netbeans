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
package org.netbeans.modules.cnd.modelimpl.csm.core;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import org.netbeans.api.project.Project;
import org.netbeans.cnd.api.lexer.CndLexerUtilities;
import org.netbeans.modules.cnd.antlr.Parser;
import org.netbeans.modules.cnd.antlr.Token;
import org.netbeans.modules.cnd.antlr.TokenStream;
import org.netbeans.modules.cnd.antlr.collections.AST;
import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.api.model.CsmErrorDirective;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmInclude;
import org.netbeans.modules.cnd.api.model.CsmInstantiation;
import org.netbeans.modules.cnd.api.model.CsmMacro;
import org.netbeans.modules.cnd.api.model.CsmModelAccessor;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.model.CsmScopeElement;
import org.netbeans.modules.cnd.api.model.CsmUID;
import org.netbeans.modules.cnd.api.model.CsmVariable;
import org.netbeans.modules.cnd.api.model.CsmVisibility;
import org.netbeans.modules.cnd.api.model.services.CsmCacheManager;
import org.netbeans.modules.cnd.api.model.services.CsmSelect.CsmFilter;
import org.netbeans.modules.cnd.api.model.util.CsmTracer;
import org.netbeans.modules.cnd.api.model.xref.CsmReference;
import org.netbeans.modules.cnd.api.project.NativeFileItem;
import org.netbeans.modules.cnd.api.project.NativeProject;
import org.netbeans.modules.cnd.apt.structure.APTFile;
import org.netbeans.modules.cnd.apt.support.APTDriver;
import org.netbeans.modules.cnd.apt.support.APTFileCacheEntry;
import org.netbeans.modules.cnd.apt.support.APTFileCacheManager;
import org.netbeans.modules.cnd.apt.support.APTHandlersSupport;
import org.netbeans.modules.cnd.apt.support.APTToken;
import org.netbeans.modules.cnd.apt.support.ClankDriver;
import org.netbeans.modules.cnd.apt.support.api.PreprocHandler;
import org.netbeans.modules.cnd.apt.support.lang.APTLanguageFilter;
import org.netbeans.modules.cnd.apt.support.lang.APTLanguageSupport;
import org.netbeans.modules.cnd.apt.utils.APTUtils;
import org.netbeans.modules.cnd.debug.CndTraceFlags;
import org.netbeans.modules.cnd.indexing.api.CndTextIndexKey;
import org.netbeans.modules.cnd.modelimpl.content.file.FakeIncludePair;
import org.netbeans.modules.cnd.modelimpl.content.file.FileComponentDeclarations;
import org.netbeans.modules.cnd.modelimpl.content.file.FileComponentIncludes;
import org.netbeans.modules.cnd.modelimpl.content.file.FileComponentInstantiations;
import org.netbeans.modules.cnd.modelimpl.content.file.FileComponentMacros;
import org.netbeans.modules.cnd.modelimpl.content.file.FileComponentReferences;
import org.netbeans.modules.cnd.modelimpl.content.file.FileContent;
import org.netbeans.modules.cnd.modelimpl.content.file.FileContentSignature;
import org.netbeans.modules.cnd.modelimpl.csm.ClassImpl;
import org.netbeans.modules.cnd.modelimpl.csm.EnumImpl;
import org.netbeans.modules.cnd.modelimpl.csm.FunctionImplEx;
import org.netbeans.modules.cnd.modelimpl.csm.MutableDeclarationsContainer;
import org.netbeans.modules.cnd.modelimpl.csm.NamespaceDefinitionImpl;
import org.netbeans.modules.cnd.modelimpl.debug.Diagnostic;
import org.netbeans.modules.cnd.modelimpl.debug.DiagnosticExceptoins;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;
import org.netbeans.modules.cnd.modelimpl.impl.services.FileInfoQueryImpl;
import org.netbeans.modules.cnd.modelimpl.parser.CPPParserEx;
import org.netbeans.modules.cnd.modelimpl.parser.ParserProviderImpl;
import org.netbeans.modules.cnd.modelimpl.parser.spi.CsmParserProvider;
import org.netbeans.modules.cnd.modelimpl.parser.spi.CsmParserProvider.CsmParser;
import org.netbeans.modules.cnd.modelimpl.parser.spi.CsmParserProvider.CsmParserResult;
import org.netbeans.modules.cnd.modelimpl.parser.spi.CsmParserProvider.ParserError;
import org.netbeans.modules.cnd.modelimpl.parser.spi.TokenStreamProducer;
import org.netbeans.modules.cnd.modelimpl.repository.KeyUtilities;
import org.netbeans.modules.cnd.modelimpl.repository.PersistentUtils;
import org.netbeans.modules.cnd.modelimpl.repository.RepositoryUtils;
import org.netbeans.modules.cnd.modelimpl.syntaxerr.spi.ReadOnlyTokenBuffer;
import org.netbeans.modules.cnd.modelimpl.trace.TraceModel;
import org.netbeans.modules.cnd.modelimpl.trace.TraceUtils;
import org.netbeans.modules.cnd.modelimpl.uid.KeyBasedUID;
import org.netbeans.modules.cnd.modelimpl.uid.UIDCsmConverter;
import org.netbeans.modules.cnd.modelimpl.uid.UIDObjectFactory;
import org.netbeans.modules.cnd.modelimpl.uid.UIDUtilities;
import org.netbeans.modules.cnd.modelutil.Tracer;
import org.netbeans.modules.cnd.repository.spi.Persistent;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataInput;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataOutput;
import org.netbeans.modules.cnd.repository.support.SelfPersistent;
import org.netbeans.modules.cnd.support.Interrupter;
import org.netbeans.modules.cnd.utils.CndPathUtilities;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.dlight.libs.common.PathUtilities;
import org.netbeans.modules.dlight.libs.common.PerformanceLogger;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.util.CharSequences;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.Pair;

/**
 * CsmFile implementations
 */
public final class FileImpl implements CsmFile,
        Disposable, Persistent, SelfPersistent, CsmIdentifiable {

    private final ThreadLocal<AtomicReference<FileContent>> parsingFileContentRef = new ThreadLocal<AtomicReference<FileContent>>() {

        @Override
        protected AtomicReference<FileContent> initialValue() {
            return new AtomicReference<>(null);
        }
    };

    public static boolean isFileBeingParsedInCurrentThread(CsmFile file) {
        if (file instanceof FileImpl) {
            return ((FileImpl)file).getParsingFileContent() != null;
        }
        return false;
    }

    /*package*/
    FileContent prepareLazyStatementParsingContent() {
        assert TraceFlags.PARSE_HEADERS_WITH_SOURCES;
        assert parsingFileContentRef.get().get() == null;
        FileContent out = FileContent.getHardReferenceBasedCopy(this.currentFileContent, false);
        parsingFileContentRef.get().set(out);
        return out;
    }

    /*package*/ void releaseLazyStatementParsingContent(FileContent tmpFileContent) {
        assert TraceFlags.PARSE_HEADERS_WITH_SOURCES;
        FileContent cur = parsingFileContentRef.get().get();
        if (cur == tmpFileContent) {
            // TODO: merge parse errors?
            parsingFileContentRef.get().set(null);
        }
    }

    public FileContent getParsingFileContent() {
        return parsingFileContentRef.get().get();
    }

    public FileContent prepareIncludedFileParsingContent() {
        assert TraceFlags.PARSE_HEADERS_WITH_SOURCES;
        if (getParsingFileContent() == null) {
            parsingFileContentRef.get().set(FileContent.getHardReferenceBasedCopy(this.currentFileContent, false));
        }
        return getParsingFileContent();
    }

    public static final boolean reportErrors = TraceFlags.REPORT_PARSING_ERRORS | TraceFlags.DEBUG;
    public static final int PARSE_FILE_TIMEOUT = 30;
    private static final boolean reportParse = Boolean.getBoolean("parser.log.parse");
    // the next flag(s) make sense only in the casew reportParse is true
    private static final boolean logState = Boolean.getBoolean("parser.log.state");
//    private static final boolean logEmptyTokenStream = Boolean.getBoolean("parser.log.empty");
    private static final boolean emptyAstStatictics = Boolean.getBoolean("parser.empty.ast.statistics");

    public static final int UNDEFINED_FILE = 0;
    public static final int SOURCE_FILE = 1;
    public static final int SOURCE_C_FILE = 2;
    public static final int SOURCE_CPP_FILE = 3;
    public static final int HEADER_FILE = 4;
    private static volatile AtomicLong parseCount = new AtomicLong(1);

    private Collection<ParserError> parsingErrors;

    public static void incParseCount() {
        parseCount.incrementAndGet();
    }

    public static int getParseCount() {
        return (int) (parseCount.get() & 0xFFFFFFFFL);
    }

    public static long getLongParseCount() {
        return parseCount.get();
    }

    private FileBuffer fileBuffer;
    /**
     * DUMMY_STATE and DUMMY_HANDLERS are used when we need to ensure that the file will be parsed.
     * Typically this happens when user edited buffer (after a delay), but also by clients request, etc. -
     * i.e. when we do not know the state to put in the parsing queue
     *
     * The issue here is that adding this file with default states (from container) does not suite,
     * since we don't know what is being done with the queue, file container and this file itself,
     * so there are a lot of sync issues on this way.
     *
     * Previously, null value was used instead; using null is much less clear an visible
     *
     * So, putting DUMMY_STATE into the queue
     *
     * 1) does not harm states that are in queue or will be put there (see ParserQueue code)
     *
     * 2) in the case DUMMY_STATE is popped from queue by the ParserThread,
     * it invokes ensureParsed(DUMMY_HANDLERS), which parses the file with all valid states from container.
     * This (2) might happen only when there are NO other states in queue
     */
    static final Collection<PreprocHandler> DUMMY_HANDLERS = new EmptyCollection<>();
    static final PreprocHandler.State DUMMY_STATE = new SpecialStateImpl();
    static final PreprocHandler.State PARTIAL_REPARSE_STATE = new SpecialStateImpl();
    static final Collection<PreprocHandler> PARTIAL_REPARSE_HANDLERS = new EmptyCollection<>();
    // only one of project/projectUID must be used (based on USE_UID_TO_CONTAINER)
    private Object projectRef;// can be set in onDispose or contstructor only
    private final CsmUID<CsmProject> projectUID;
    /**
     * It's a map since we need to eliminate duplications
     */
    private final ReentrantReadWriteLock projectLock = new ReentrantReadWriteLock();
    private int lastParseTime;

    FileContentSignature getSignature() {
        return FileContentSignature.create(this);
    }

    /*tests-only*/void debugInvalidate() {
        this.state = State.INITIAL;
        addRemoveModifiedFile(true);
    }

    void attachToProject(final ProjectBase project) {
        projectLock.writeLock().lock();
        try {
            if (projectRef == project) {
                return;
            }
            if (projectRef instanceof Reference<?> && ((Reference) projectRef).get() == project) {
                return;
            }
            projectRef = new WeakReference<>(project);
        } finally {
            projectLock.writeLock().unlock();
        }
    }

    public FileSystem getFileSystem() {
        return getProjectImpl(true).getFileSystem();
    }

    /*package*/static enum State {

        /** The file has never been parsed */
        INITIAL,
        /** The file has been completely parsed */
        PARSED,
        /** The file is parsed in one preprocessor state,
        but should be parsed in one or several other states */
        PARTIAL,
        /** The file is modified and needs to be reparsed */
        MODIFIED,
    }

    private static enum ParsingState {
        /** The file is not in parsing phase */
        NOT_BEING_PARSED,
        /** The file is modified during parsing */
        MODIFIED_WHILE_BEING_PARSED,
        /** The file is now being parsed */
        BEING_PARSED
    }
    private volatile State state;
    private volatile ParsingState parsingState;
    private FileType fileType = FileType.UNDEFINED_FILE;
    private static final class StateLock {}
    private final Object stateLock = new StateLock();
    private volatile FileContent currentFileContent;
    private FileContentSignature lastFileBasedSignature;
    private FileSnapshot fileSnapshot;
    private final Object snapShotLock = new Object();
    private int guardStart = -1;
    private int guardEnd = -1;

    private volatile boolean disposed = false; // convert to flag field as soon as new flags appear
    
    /** 
     * Contains reference to the project's  disposing flag.
     * Introduced for optimization purposes - for FileImpl to be able to make a fast check
     * without getting the project itself (nor hard-referencing of the project itself either).
     * This strongly relates on the fact that the project stays in memory
     * (we call RepositoryUtils.hang() but never RepositoryUtils.put() for the project)
     */
    private final AtomicBoolean projectDisposed;

    private final Interrupter interrupter = new Interrupter() {
        @Override
        public boolean cancelled() {
            return !isValid();
        }
    };
    
    private long lastParsed = Long.MIN_VALUE;
    private long lastParsedBufferCRC;
    private long lastParsedCompilationUnitCRC;

    /** Cache the hash code */
    private final int hash;
    private Reference<List<CsmReference>> lastMacroUsages = null;

    /** For test purposes only */
    private static TraceModel.TestHook hook = null;
    
    public static FileImpl createFileImpl(FileBuffer fileBuffer, ProjectBase project, FileType fileType, NativeFileItem nativeFileItem) {
        FileImpl fileImpl = new FileImpl(fileBuffer, project, fileType, nativeFileItem);
        fileImpl.currentFileContent = FileContent.createFileContent(fileImpl, fileImpl);
        if (nativeFileItem != null) {
            project.putNativeFileItem(fileImpl.getUID(), nativeFileItem);
        }
        Notificator.instance().registerNewFile(fileImpl);
        if (TraceFlags.TRACE_CPU_CPP && fileImpl.getAbsolutePath().toString().endsWith("cpu.cc")) { // NOI18N
            new Exception("cpu.cc file@" + System.identityHashCode(fileImpl) + " of prj@"  + System.identityHashCode(project) + ":UID@" + System.identityHashCode(fileImpl.projectUID) + fileImpl.projectUID).printStackTrace(System.err); // NOI18N
        }
        return fileImpl;
    }

    private FileImpl(FileBuffer fileBuffer, ProjectBase project, FileType fileType, NativeFileItem nativeFileItem) {
        CndPathUtilities.assertNoUrl(fileBuffer.getAbsolutePath());
        state = State.INITIAL;
        parsingState = ParsingState.NOT_BEING_PARSED;
        this.projectUID = UIDCsmConverter.projectToUID(project);
        assert (projectUID instanceof KeyBasedUID); // this fact is used in write() and getInitId()
        this.fileBuffer = fileBuffer;
        this.hash = calculateHashCode(project, fileBuffer.getAbsolutePath());
        // NB: the below strongly relates on the fact that the project stays in memory
        this.projectDisposed = project.getDisposingFlag();

        hasBrokenIncludes = new AtomicBoolean(false);
        this.projectRef = new WeakReference<>(project); // Suppress Warnings
        if (fileType == FileType.UNDEFINED_FILE && nativeFileItem != null) {
            fileType = Utils.getFileType(nativeFileItem);
        }
        this.fileType = fileType;
    }

    /** For test purposes only */
    /*package*/static void setTestHook(TraceModel.TestHook aHook) {
        hook = aHook;
    }

    public final NativeFileItem getNativeFileItem() {
        return getProjectImpl(true).getNativeFileItem(getUID());
    }

     private ProjectBase _getProject(boolean assertNotNull) {
        Object o = projectRef;
        if (o instanceof ProjectBase) {
            return (ProjectBase) o;
        } else if (o instanceof Reference<?>) {
            ProjectBase prj = (ProjectBase)((Reference<?>) o).get();
            if (prj != null) {
                return prj;
            }
        }
        projectLock.readLock().lock();
        try {
            ProjectBase prj = null;
            if (projectRef instanceof ProjectBase) {
                prj = (ProjectBase) projectRef;
            } else if (projectRef instanceof Reference<?>) {
                prj = (ProjectBase)((Reference<?>) projectRef).get();
            }
            if (prj == null) {
                prj = (ProjectBase) UIDCsmConverter.UIDtoProject(this.projectUID);
                if (assertNotNull) {
                    assert (prj != null || this.projectUID == null) : "empty project for UID " + this.projectUID;
                }
                projectRef = new WeakReference<>(prj);
            }
            return prj;
        } finally {
            projectLock.readLock().unlock();
        }
    }

    @Override
    public final boolean isSourceFile() {
        return isSourceFileType(fileType);
    }

    public static boolean isSourceFileType(FileType fileType) {
        switch (fileType) {
            case SOURCE_CPP_FILE:
            case SOURCE_C_FILE:
            case SOURCE_FILE:
            case SOURCE_FORTRAN_FILE:
                return true;
        }
        return false;
    }

    public boolean isCppFile() {
        return fileType == FileType.SOURCE_CPP_FILE;
    }

    /*package local*/ void setSourceFile() {
        if (!(fileType == FileType.SOURCE_C_FILE || fileType == FileType.SOURCE_CPP_FILE || fileType == FileType.SOURCE_FORTRAN_FILE)) {
            fileType = FileType.SOURCE_FILE;
        }
    }

    @Override
    public boolean isHeaderFile() {
        return fileType == FileType.HEADER_FILE;
    }

    @Override
    public FileType getFileType() {
        return fileType;
    }


    /*package local*/ void setHeaderFile() {
        if (fileType == FileType.UNDEFINED_FILE) {
            fileType = FileType.HEADER_FILE;
        }
    }

    // TODO: consider using macro map and __cplusplus here instead of just checking file name
    public APTLanguageFilter getLanguageFilter(PreprocHandler.State ppState) {
        FileImpl startFile = ppState == null ? null : Utils.getStartFile(ppState);
        if (startFile != null && startFile != this) {
            return startFile.getLanguageFilter(null);
        } else {
            return APTLanguageSupport.getInstance().getFilter(getFileLanguage(), getFileLanguageFlavor());
        }
    }

    public APTFile.Kind getAPTFileKind() {
        return APTDriver.langFlavorToAPTFileKind(getFileLanguage(), getFileLanguageFlavor());
    }
    
    // Returns language for current context (compilation unit)
    public String getContextLanguage(PreprocHandler.State ppState) {
        FileImpl startFile = ppState == null ? null : Utils.getStartFile(ppState);
        if (startFile != null && startFile != this) {
            return startFile.getFileLanguage();
        } else {
            return getFileLanguage();
        }
    }
    
    public String getContextLanguageFlavor(PreprocHandler.State ppState) {
        FileImpl startFile = ppState == null ? null : Utils.getStartFile(ppState);
        if (startFile != null && startFile != this) {
            return startFile.getFileLanguageFlavor();
        } else {
            return getFileLanguageFlavor();
        }
    }

    public String getFileLanguage() {
        return Utils.getLanguage(fileType, getAbsolutePath().toString());
    }

    public String getFileLanguageFlavor() {
        if(APTLanguageSupport.FORTRAN.equals(getFileLanguage())) {
            try {
                return CndLexerUtilities.detectFortranFormat(getBuffer().getText()) == CndLexerUtilities.FortranFormat.FIXED ?
                            APTLanguageSupport.FLAVOR_FORTRAN_FIXED :
                            APTLanguageSupport.FLAVOR_FORTRAN_FREE;
            } catch (IOException ex) {
                return APTLanguageSupport.FLAVOR_FORTRAN_FREE;
            }
        } else {
            if(CndTraceFlags.LANGUAGE_FLAVOR_CPP11) {
                return APTLanguageSupport.FLAVOR_CPP11;
            } else if (CndTraceFlags.LANGUAGE_FLAVOR_CPP14) {
                return APTLanguageSupport.FLAVOR_CPP14;
            } else if (CndTraceFlags.LANGUAGE_FLAVOR_CPP17) {
                return APTLanguageSupport.FLAVOR_CPP17;
            }
            NativeFileItem nativeFileItem = getNativeFileItem();
            if(nativeFileItem != null) {
                NativeFileItem.LanguageFlavor languageFlavor = nativeFileItem.getLanguageFlavor();
                if (languageFlavor == NativeFileItem.LanguageFlavor.UNKNOWN) {
                    Pair<NativeFileItem.Language, NativeFileItem.LanguageFlavor> fileLanguageFlavor
                            = FileInfoQueryImpl.getDefault().getFileLanguageFlavor(this);
                    languageFlavor = fileLanguageFlavor.second();
                }
                return Utils.getLanguageFlavor(languageFlavor);
            }
        }
        return APTLanguageSupport.FLAVOR_UNKNOWN;
    }
    
    public PreprocHandler getPreprocHandler(int offset) {
        PreprocessorStatePair bestStatePair = getContextPreprocStatePair(offset, offset);
        return getPreprocHandler(bestStatePair);
    }

    private PreprocHandler getPreprocHandler(PreprocessorStatePair statePair) {
        if (statePair == null) {
            return null;
        }
        final ProjectBase projectImpl = getProjectImpl(true);
        if (projectImpl == null) {
            return null;
        }
        return projectImpl.getPreprocHandler(fileBuffer.getAbsolutePath(), statePair);
    }

    public final boolean checkIfFileWasIncludedBeforeWithBetterOrEqualContent(PreprocHandler ppPreIncludeHandler) {
        final ProjectBase projectImpl = getProjectImpl(true);
        return projectImpl == null 
                ? true/*no need to include such file*/ 
                : projectImpl.checkIfFileWasIncludedBeforeWithBetterOrEqualContent(this, ppPreIncludeHandler);
    }
    
    public Collection<PreprocHandler> getPreprocHandlersForParse(Interrupter interrupter) {
        final ProjectBase projectImpl = getProjectImpl(true);
        return projectImpl == null ? Collections.<PreprocHandler>emptyList() : projectImpl.getPreprocHandlersForParse(this, interrupter);
    }

    public Collection<PreprocessorStatePair> getPreprocStatePairs() {
      ProjectBase projectImpl = getProjectImpl(true);
        if (projectImpl == null) {
            return Collections.<PreprocessorStatePair>emptyList();
        }
        return projectImpl.getPreprocessorStatePairs(this);
    }

    public Collection<PreprocHandler> getFileContainerOwnPreprocHandlersToDump() {
        final ProjectBase projectImpl = getProjectImpl(true);
        return projectImpl == null ? Collections.<PreprocHandler>emptyList() : projectImpl.getFileContainerPreprocHandlersToDump(this.getAbsolutePath());
    }

    public Collection<PreprocessorStatePair> getFileContainerOwnPreprocessorStatePairsToDump() {
        ProjectBase projectImpl = getProjectImpl(true);
        if (projectImpl == null) {
            return Collections.<PreprocessorStatePair>emptyList();
        }
        return projectImpl.getFileContainerStatePairsToDump(this.getAbsolutePath());
    }

    private PreprocessorStatePair getContextPreprocStatePair(int startContext, int endContext) {
        ProjectBase projectImpl = getProjectImpl(true);
        if (projectImpl == null) {
            return null;
        }
        Collection<PreprocessorStatePair> preprocStatePairs = projectImpl.getPreprocessorStatePairs(this);
        // select the best based on context offsets
        if (preprocStatePairs.size() > 1) {
            int bestCoverage = -1;
            PreprocessorStatePair bestStatePair = null;
            for (PreprocessorStatePair pair : preprocStatePairs) {
                if (pair.pcState != null) {
                    int coverage = pair.pcState.getActiveCoverage(startContext, endContext);
                    if (coverage > bestCoverage) {
                        bestCoverage = coverage;
                        bestStatePair = pair;
                        if (coverage == (endContext - startContext)) {
                            // max coverage is found
                            break;
                        }
                    }
                }
            }
            if (bestStatePair != null) {
                return bestStatePair;
            }
        } else if (!preprocStatePairs.isEmpty()) {
            PreprocessorStatePair statePair = preprocStatePairs.iterator().next();
            if (statePair.pcState != null && statePair.pcState.isInActiveBlock(startContext, endContext)) {
                return statePair;
            }
        }
        return null;
    }

    public void setBuffer(FileBuffer fileBuffer) {
        synchronized (changeStateLock) {
            this.fileBuffer = fileBuffer;
//            if (traceFile(getAbsolutePath())) {
//                new Exception("setBuffer: " + fileBuffer).printStackTrace(System.err);
//            }
            if (state != State.INITIAL || parsingState != ParsingState.NOT_BEING_PARSED) {
                if (reportParse || logState || TraceFlags.DEBUG || TraceFlags.TRACE_191307_BUG) {
                    System.err.printf("#setBuffer changing to MODIFIED %s is %s with current state %s %s%n", getAbsolutePath(), fileType, state, parsingState); // NOI18N
                }
                if (ProjectBase.WAIT_PARSE_LOGGER.isLoggable(Level.FINE)) {
                    ProjectBase.WAIT_PARSE_LOGGER.log(Level.FINE, String.format("##> MODIFIED %s %d", this, System.currentTimeMillis()), new Exception());
                }
                state = State.MODIFIED;
                addRemoveModifiedFile(true);
                postMarkedAsModified();
            }
        }
    }

    private void addRemoveModifiedFile(boolean add) {
        ProjectBase projectImpl = getProjectImpl(false);
        if (projectImpl != null) {
            if (add) {
                projectImpl.addModifiedFile(this);
            } else {
                projectImpl.removeModifiedFile(this);
            }
        }
    }

    private void postMarkedAsModified() {
        // must be called only changeStateLock
        assert Thread.holdsLock(changeStateLock) : "must be called under changeStateLock";
        tsRef.clear();
        if (parsingState == ParsingState.BEING_PARSED) {
            parsingState = ParsingState.MODIFIED_WHILE_BEING_PARSED;
        }
    }

    public FileBuffer getBuffer() {
        return this.fileBuffer;
    }

    private final AtomicInteger inEnsureParsed = new AtomicInteger(0);
    // ONLY FOR PARSER THREAD USAGE
    // Parser Queue ensures that the same file can be parsed at the same time
    // only by one thread.// ONLY FOR PARSER THREAD USAGE
    // Parser Queue ensures that the same file can be parsed at the same time
    // only by one thread.
    /*package*/ void ensureParsed(Collection<PreprocHandler> handlers) {
        if (ProjectBase.WAIT_PARSE_LOGGER.isLoggable(Level.FINE)) {
            ProjectBase.WAIT_PARSE_LOGGER.fine(String.format("##> ensureParsed %s %d", this, System.currentTimeMillis()));
        }
        try {
            ensureParsedImpl(handlers);
        } finally {
            if (ProjectBase.WAIT_PARSE_LOGGER.isLoggable(Level.FINE)) {
                ProjectBase.WAIT_PARSE_LOGGER.fine(String.format("##< ensureParsed %s %d", this, System.currentTimeMillis()));
            }
        }
    }

    private void ensureParsedImpl(Collection<PreprocHandler> handlers) {

        if (TraceFlags.PARSE_HEADERS_WITH_SOURCES && this.isHeaderFile()) {
            System.err.printf("HEADERS_WITH_SOURCES: ensureParsed: %s%n", this.getAbsolutePath());
        }
        try {
            if (inEnsureParsed.incrementAndGet() != 1) {
                assert false : "concurrent ensureParsed in file " + getAbsolutePath() + parsingState + state;
            }
            if (!CsmModelAccessor.isModelAlive()) {
                if (TraceFlags.TRACE_VALIDATION || TraceFlags.TRACE_MODEL_STATE) {
                    System.err.printf("ensureParsed: %s file is interrupted on closing model%n", this.getAbsolutePath());
                }
                synchronized (changeStateLock) {
                    state = State.INITIAL;
                    addRemoveModifiedFile(false);
                }
                return;
            }
            FileContentSignature newSignature = null;
            FileContentSignature oldSignature = null;
            boolean tryPartialReparse = (handlers == PARTIAL_REPARSE_HANDLERS);
            // when new project is opened it detects that can contribute better model into
            // library file of already opened Library project (used by others).
            // Currently for that from finishProjectFilesCreation we calls tryPartialReparseOnChangedFile
            // and needlessly reparse one project file to let it walk through all
            // it's included files and contribute extra/best states for shared
            // library files (or add new library files used only by this project).
            // That's why now triggerParsingActivity is always true except
            // editing document case when we don't want to force parsing of includes
            boolean triggerParsingActivity = (handlers != DUMMY_HANDLERS);
            final ProjectBase projectImpl = getProjectImpl(true);
            if (handlers == DUMMY_HANDLERS || handlers == PARTIAL_REPARSE_HANDLERS) {
                handlers = getPreprocHandlersForParse(Interrupter.DUMMY);
            }
            long time;
            synchronized (stateLock) {
                boolean hasParseIssue = true;
                try {
                    State curState;
                    synchronized (changeStateLock) {
                        curState = state;
                        parsingState = ParsingState.BEING_PARSED;
                    }
                    if (reportParse || logState || TraceFlags.DEBUG) {
                        if (traceFile(getAbsolutePath())) {
                            System.err.printf("#ensureParsed %s is %s, has %d handlers, state %s %s triggerParsingActivity=%s%n", getAbsolutePath(), fileType, handlers.size(), curState, parsingState, triggerParsingActivity); // NOI18N
                            int i = 0;
                            for (PreprocHandler PreprocHandler : handlers) {
                                logParse("EnsureParsed handler " + (i++), PreprocHandler); // NOI18N
                            }
                        }
                    }
                    TokenStreamProducer tsp = TokenStreamProducer.create(this, curState == State.MODIFIED, true);
                    if (tsp == null) {
                        // probably file was removed
                        return;
                    }
                    
                    final ParseDescriptor parseParams = new ParseDescriptor(this, tsp, null, triggerParsingActivity);
                    
                    switch (curState) {
                        case PARSED: // even if it was parsed, but there was entry in queue with handler => need additional parse
                        case INITIAL:
                        case PARTIAL:
                            if (TraceFlags.TIMING_PARSE_PER_FILE_FLAT && curState == State.PARSED) {
                                System.err.printf("additional parse with PARSED state " + parsingState + "for %s%n", getAbsolutePath()); // NOI18N
                            }
                            time = System.currentTimeMillis();
                            try {
                                long compUnitCRC = 0;
                                try {
                                    // initialize parsing file content before loop instead of doing it on each iteration
                                    parsingFileContentRef.get().set(parseParams.getFileContent());
                                    for (PreprocHandler preprocHandler : handlers) {
                                        compUnitCRC = APTHandlersSupport.getCompilationUnitCRC(preprocHandler);
                                        parseParams.prepare(preprocHandler, getContextLanguage(preprocHandler.getState()), getContextLanguageFlavor(preprocHandler.getState()));
                                        _parse(parseParams);
                                        if (parsingState == ParsingState.MODIFIED_WHILE_BEING_PARSED) {
                                            break; // does not make sense parsing old data
                                        }
                                    }
                                } finally {
                                    parsingFileContentRef.get().set(null);
                                }
                                if (parsingState == ParsingState.BEING_PARSED) {
                                    updateModelAfterParsing(parseParams.getFileContent(), compUnitCRC);
                                }
                            } finally {
                                postParse();
                                synchronized (changeStateLock) {
                                    if (parsingState == ParsingState.BEING_PARSED) {
                                        state = State.PARSED;
                                        addRemoveModifiedFile(false);
                                    }  // if not, someone marked it with new state
                                }
                                postParseNotify();
                                lastParseTime = (int)(System.currentTimeMillis() - time);
                                //System.err.println("Parse of "+getAbsolutePath()+" took "+lastParseTime+"ms");
                            }
                            if (TraceFlags.DUMP_PARSE_RESULTS) {
                                CsmCacheManager.enter();
                                try {
                                    new CsmTracer().dumpModel(this);
                                } finally {
                                    CsmCacheManager.leave();
                                }
                            }
                            break;
                        case MODIFIED:
                            boolean first = true;
                            time = System.currentTimeMillis();
                            try {
                                if (lastFileBasedSignature == null) {
                                    if (tryPartialReparse ||  !fileBuffer.isFileBased()) {
                                        // initialize file-based content signature
                                        lastFileBasedSignature = FileContentSignature.create(this);
                                    }
                                }
                                long compUnitCRC = 0;
                                try {
                                    // initialize parsing file content and snapshot before loop;
                                    parsingFileContentRef.get().set(parseParams.getFileContent());
                                    synchronized (snapShotLock) {
                                        // intialize snapshot out of loop
                                        // instead of doing it on each iteration which can create window for clients
                                        // for seeing empty snapshot between iterations, i.e.
                                        // when on the first iteration small part of file is parsed and on the second
                                        // remaining, but we already disposed content in the first "reparse" phase
                                        fileSnapshot = new FileSnapshot(this);
                                    }
                                    for (PreprocHandler preprocHandler : handlers) {
                                        parseParams.prepare(preprocHandler, getContextLanguage(preprocHandler.getState()), getContextLanguageFlavor(preprocHandler.getState()));
                                        if (first) {
                                            compUnitCRC = APTHandlersSupport.getCompilationUnitCRC(preprocHandler);
                                            _reparse(parseParams);
                                            first = false;
                                        } else {
                                            _parse(parseParams);
                                        }
                                        if (parsingState == ParsingState.MODIFIED_WHILE_BEING_PARSED) {
                                            break; // does not make sense parsing old data
                                        }
                                    }
                                } finally {
                                    fileSnapshot = null;
                                    parsingFileContentRef.get().set(null);
                                }
                                if (parsingState == ParsingState.BEING_PARSED) {
                                    updateModelAfterParsing(parseParams.getFileContent(), compUnitCRC);
                                    if (tryPartialReparse) {
                                        assert lastFileBasedSignature != null;
                                        newSignature = FileContentSignature.create(this);
                                        oldSignature = lastFileBasedSignature;
                                        lastFileBasedSignature = null;
                                    }
                                }
                            } finally {
                                postParse();
                                synchronized (changeStateLock) {
                                    if (parsingState == ParsingState.BEING_PARSED) {
                                        state = State.PARSED;
                                        addRemoveModifiedFile(false);
                                    } // if not, someone marked it with new state
                                }
                                postParseNotify();
                                lastParseTime = (int)(System.currentTimeMillis() - time);
                                //System.err.println("Parse of "+getAbsolutePath()+" took "+lastParseTime+"ms");
                            }
                            if (TraceFlags.DUMP_PARSE_RESULTS || TraceFlags.DUMP_REPARSE_RESULTS) {
                                CsmCacheManager.enter();
                                try {
                                    new CsmTracer().dumpModel(this);
                                } finally {
                                    CsmCacheManager.leave();
                                }
                            }
                            break;
                        default:
                            System.err.println("unexpected state in ensureParsed " + curState); // NOI18N
                    }
                    // clear flag if reached the end of parsing without exceptions
                    hasParseIssue = false;
                } finally {
                    synchronized (changeStateLock) {
                        parsingState = ParsingState.NOT_BEING_PARSED;
                        if (hasParseIssue) {
                            // TODO: introduce error on parse state
                            //
                            // For now we have to mark file as parsed, otherwise
                            // scheduleParsing(true) never finishes while(!isParsed()) loop
                            state = State.PARSED;
                            addRemoveModifiedFile(false);
                        }
                    }
                }
            }
            // check state at the end as well, because there could be interruption during parse of file
            if (!CsmModelAccessor.isModelAlive()) {
                if (TraceFlags.TRACE_VALIDATION || TraceFlags.TRACE_MODEL_STATE) {
                    System.err.printf("after ensureParsed: %s file is interrupted on closing model%n", this.getAbsolutePath());
                }
                disposeAll(true);
                projectImpl.invalidatePreprocState(this.getAbsolutePath());
                synchronized (changeStateLock) {
                    state = State.INITIAL;
                    addRemoveModifiedFile(false);
                }
            } else {
                // if was request for partial reparse and file state was not modified during parse
                if (tryPartialReparse && newSignature != null) {
                    assert oldSignature != null;
                    DeepReparsingUtils.finishPartialReparse(this, oldSignature, newSignature);
                }
            }
        } finally {
            if (inEnsureParsed.decrementAndGet() != 0) {
                CndUtils.assertTrueInConsole(false, "broken state in file " + getAbsolutePath() + parsingState + state);
            }
            // put parsed file into repository after all
            RepositoryUtils.put(this);
            // all exist points must have state change notifcation
            synchronized (stateLock) {
                stateLock.notifyAll();
            }
        }
    }

    private void postParse() {
        if (isValid()) {	// FIXUP: use a special lock here
            getProjectImpl(true).getGraph().putFile(this);
        }
    }

    private void postParseNotify() {
        if (isValid()) {   // FIXUP: use a special lock here
            Notificator.instance().registerChangedFile(this);
            Notificator.instance().flush();
        } else {
            // FIXUP: there should be a notificator per project instead!
            Notificator.instance().reset();
        }
    }

    /*package*/ void onProjectParseFinished(boolean prjLibsAlreadyParsed) {
        if (fixFakeRegistrations(true)) {
            if (isValid()) {   // FIXUP: use a special lock here
                RepositoryUtils.put(this);
            }
            if (isValid()) {   // FIXUP: use a special lock here
                Notificator.instance().registerChangedFile(this);
                Notificator.instance().flush();
                ProgressSupport.instance().fireFileParsingFinished(this);
            } else {
                // FIXUP: there should be a notificator per project instead!
                Notificator.instance().reset();
            }
        }
    }

    // returns parse/rearse time in milliseconds.
    public int getLastParseTime(){
        return lastParseTime;
    }

    /*package*/boolean validate() {
        synchronized (changeStateLock) {
            if (state == State.PARSED) {
                long lastModified = getBuffer().lastModified();
                // using "==" when comparison disallows offline index: in most cases timestamps differ
                if (TraceFlags.USE_CURR_PARSE_TIME ? (lastModified > lastParsed) : (lastModified != lastParsed)) {
                    if (lastParsedBufferCRC != getBuffer().getCRC()) {
                        if (TraceFlags.TRACE_VALIDATION || TraceFlags.TRACE_191307_BUG) {
                            System.err.printf("VALIDATED %s%n\t lastModified=%d%n\t   lastParsed=%d%n", getAbsolutePath(), lastModified, lastParsed);
                        }
                        if (reportParse || logState || TraceFlags.DEBUG) {
                            System.err.printf("#validate changing to MODIFIED %s is %s with current state %s %s%n", getAbsolutePath(), fileType, state, parsingState); // NOI18N
                        }
                        if (ProjectBase.WAIT_PARSE_LOGGER.isLoggable(Level.FINE)) {
                            ProjectBase.WAIT_PARSE_LOGGER.log(Level.FINE, String.format("##> MODIFIED %s %d", this, System.currentTimeMillis()), new Exception());
                        }
                        state = State.MODIFIED;
                        postMarkedAsModified();
                        return false;
                    }
                }
            }
            return true;
        }
    }

    private static final class ChangeStateLock {}
    private final Object changeStateLock = new ChangeStateLock();

    public final void markReparseNeeded(boolean invalidateCache) {
        synchronized (changeStateLock) {
            if (reportParse || logState || TraceFlags.DEBUG || TraceFlags.TRACE_191307_BUG) {
                System.err.printf("#markReparseNeeded %s is %s with current state %s, %s%n", getAbsolutePath(), fileType, state, parsingState); // NOI18N
                if (TraceFlags.TRACE_191307_BUG) {
                    new Exception("markReparseNeeded is called").printStackTrace(System.err);// NOI18N
                }// NOI18N
            }
            if (state != State.INITIAL || parsingState != ParsingState.NOT_BEING_PARSED) {
                if (ProjectBase.WAIT_PARSE_LOGGER.isLoggable(Level.FINE)) {
                    ProjectBase.WAIT_PARSE_LOGGER.log(Level.FINE, String.format("##> MODIFIED %s %d", this, System.currentTimeMillis()), new Exception());
                }
                state = State.MODIFIED;
                postMarkedAsModified();
            }
            if (invalidateCache) {
                final FileBuffer buf = this.getBuffer();
                APTDriver.invalidateAPT(buf);
                ClankDriver.invalidate(buf);
                APTFileCacheManager.getInstance(buf.getFileSystem()).invalidate(buf.getAbsolutePath());
            }
        }
    }

    /*package*/final void markMoreParseNeeded() {
        synchronized (changeStateLock) {
            if (reportParse || logState || TraceFlags.DEBUG) {
                System.err.printf("#markMoreParseNeeded %s is %s with current state %s, %s%n", getAbsolutePath(), fileType, state, parsingState); // NOI18N
            }
            switch (state) {
                case PARSED:
                    state = State.PARTIAL;
                    break;
                case INITIAL:
                case MODIFIED:
                case PARTIAL:
                // nothing
            }
        }
    }

    /*package*/final int getErrorCount() {
        checkNotInParsingThreadImpl();
        return currentFileContent.getErrorCount();
    }
    
    private void _reparse(ParseDescriptor parseParams) {
        if (TraceFlags.DEBUG) {
            Diagnostic.trace("------ reparsing " + fileBuffer.getUrl()); // NOI18N
        }
        if (reportParse || logState || TraceFlags.DEBUG) {
            logParse("ReParsing", parseParams.getCurrentPreprocHandler()); //NOI18N
        }
        disposeAll(false);
        CsmParserResult parsing = doParse(parseParams);
        if (parsing != null) {
            if (isValid()) {
                parsing.render(parseParams);
            }
        } else {
            //System.err.println("null ast for file " + getAbsolutePath());
        }
    }

    CsmFile getSnapshot(){
        synchronized(snapShotLock) {
            FileSnapshot res = fileSnapshot;
            if (res != null) {
                return res;
            }
            return new FileSnapshot(this);
        }
    }

    @Override
    public void dispose() {
        disposed = true;
        onDispose();
        Notificator.instance().registerRemovedFile(this);
        disposeAll(true);
    }

    public void onProjectClose() {
        onDispose();
    }

    private void onDispose() {
        RepositoryUtils.disposeUID(uid, this);
        projectLock.writeLock().lock();
        try {
            if (projectRef instanceof Reference<?>) {
                projectRef = ((Reference)projectRef).get();
            }
            if (projectRef == null) {
                // restore container from it's UID
                this.projectRef = (ProjectBase) UIDCsmConverter.UIDtoProject(this.projectUID);
                assert (this.projectRef != null || this.projectUID == null) : "empty project for UID " + this.projectUID;
            }
        } finally {
            projectLock.writeLock().unlock();
        }
    }

    private void disposeAll(boolean clearNonDisposable) {
        //NB: we're copying declarations, because dispose can invoke this.removeDeclaration
        //for( Iterator iter = declarations.values().iterator(); iter.hasNext(); ) {
        Collection<CsmUID<CsmOffsetableDeclaration>> uids = currentFileContent.cleanDeclarations();
        clearFakeRegistrations();

        hasBrokenIncludes.set(false);
        if (clearNonDisposable) {
            currentFileContent.cleanOther();
        }
        currentFileContent.put();
        Collection<CsmOffsetableDeclaration> arr = UIDCsmConverter.UIDsToDeclarations(uids);
        Utils.disposeAll(arr);
        RepositoryUtils.remove(uids);
    }

    /**encapsulates all parameters which should be used during parse or reparse of the file */
    /*package*/static final class ParseDescriptor implements CsmParserProvider.CsmParserParameters {

        private final CsmParserProvider.CsmParseCallback callback;
        private final boolean lazyCompound;
        private final TokenStreamProducer tsp;
        private final boolean triggerParsingActivity;
        // FIXME: it's worth to remember states before parse and reuse after
        private final long lastParsed;
        private final long lastParsedCRC;

        public ParseDescriptor(FileImpl fileImpl, TokenStreamProducer tsp, CsmParserProvider.CsmParseCallback callback, boolean triggerParsingActivity) {
            this(fileImpl, tsp, callback, TraceFlags.EXCLUDE_COMPOUND, triggerParsingActivity);
        }

        public ParseDescriptor(FileImpl fileImpl, TokenStreamProducer tsp,
                CsmParserProvider.CsmParseCallback callback,
                boolean lazyCompound, boolean triggerParsingActivity) {
            assert fileImpl != null : "null file is not allowed";
            assert tsp != null : "null TokenStreamProducer is not allowed";
            this.tsp = tsp;
            this.callback = callback;
            this.lazyCompound = lazyCompound;
            this.triggerParsingActivity = triggerParsingActivity;
            this.lastParsed = fileImpl.fileBuffer.lastModified();
            this.lastParsedCRC = fileImpl.fileBuffer.getCRC();
        }
        
        private PreprocHandler getCurrentPreprocHandler() {
            return this.tsp.getCurrentPreprocHandler();
        }

        public FileContent getFileContent() {
            return this.tsp.getFileContent();
        }

        @Override
        public String getLanguage() {
            return this.tsp.getLanguage();
        }        

        @Override
        public String getLanguageFlavor() {
            return this.tsp.getLanguageFlavor();
        }

        @Override
        public CsmFile getMainFile() {
            return this.tsp.getInterestedFile();
        }

        public void prepare(PreprocHandler preprocHandler, String contextLanguage, String contextLanguageFlavor) {
            this.tsp.prepare(preprocHandler, contextLanguage, contextLanguageFlavor, false);
        }
    }

    /** for debugging/tracing purposes only */
    public AST debugParse() {
        Collection<PreprocHandler> handlers = getFileContainerOwnPreprocHandlersToDump();
        if (handlers.isEmpty()) {
            return null;
        }
        TokenStreamProducer tsp = TokenStreamProducer.create(this, false, false);
        ParseDescriptor params = new ParseDescriptor(this, tsp, null, false, false);
        params.prepare(handlers.iterator().next(), getFileLanguage(), getFileLanguageFlavor());
        synchronized (stateLock) {
            try {
                parsingFileContentRef.get().set(params.getFileContent());
                CsmParserResult parsing = _parse(params);
                Object ast = parsing.getAST();
                if (ast instanceof AST) {
                    return (AST) ast;
                }
            } finally {
                parsingFileContentRef.get().set(null);
            }
        }
        return null;
    }

    private CsmParserResult _parse(ParseDescriptor parseParams) {
        PerformanceLogger.PerformaceAction performanceEvent = PerformanceLogger.getLogger().start(Tracer.PARSE_FILE_PERFORMANCE_EVENT, getFileObject());
        try {
            performanceEvent.setTimeOut(FileImpl.PARSE_FILE_TIMEOUT);
            Diagnostic.StopWatch sw = TraceFlags.TIMING_PARSE_PER_FILE_DEEP ? new Diagnostic.StopWatch() : null;
            if (reportParse || logState || TraceFlags.DEBUG) {
                logParse("Parsing", parseParams.getCurrentPreprocHandler()); //NOI18N
            }
            CsmParserResult parsing = doParse(parseParams);
            if (TraceFlags.TIMING_PARSE_PER_FILE_DEEP) {
                sw.stopAndReport("Parsing of " + fileBuffer.getUrl() + " took \t"); // NOI18N
            }
            if (parsing != null) {
                Diagnostic.StopWatch sw2 = TraceFlags.TIMING_PARSE_PER_FILE_DEEP ? new Diagnostic.StopWatch() : null;
                if (isValid()) {   // FIXUP: use a special lock here
                    parsing.render(parseParams);
                    if (TraceFlags.TIMING_PARSE_PER_FILE_DEEP) {
                        sw2.stopAndReport("Rendering of " + fileBuffer.getUrl() + " took \t"); // NOI18N
                    }
                }
            }
            return parsing;
        } finally {
            ProjectBase projectImpl = getProjectImpl(false);
            if (projectImpl != null) {
                if (projectImpl.isArtificial()) {
                    Collection<ProjectBase> dependentProjects = ((LibProjectImpl)projectImpl).getDependentProjects();
                    if (dependentProjects.size() > 0) {
                        projectImpl = dependentProjects.iterator().next();
                    }
                }
            }
            Object platformProject = null;
            if (projectImpl != null) {
                platformProject = projectImpl.getPlatformProject();
            }
            int lines = 0;
            try {
                lines = getBuffer().getLineCount();
            } catch (IOException ex) {
            }
            if (platformProject instanceof NativeProject) {
                Lookup.Provider project = ((NativeProject)platformProject).getProject();
                if (project instanceof Project) {
                    performanceEvent.log(lines, project);
                } else {
                    performanceEvent.log(lines);
                }
            } else {
                performanceEvent.log(lines);
            }
        }
    }

    private void logParse(String title, PreprocHandler preprocHandler) {
        if (reportParse || logState || TraceFlags.DEBUG) {
            System.err.printf("# %s %s %n#\t(%s %s %s) %n#\t(Thread=%s)%n", //NOI18N
                    title, fileBuffer.getUrl(),
                    TraceUtils.getPreprocStateString(preprocHandler.getState()),
                    TraceUtils.getMacroString(preprocHandler, TraceFlags.logMacros),
                    TraceUtils.getPreprocStartEntryString(preprocHandler.getState()),
                    Thread.currentThread().getName());
            if (logState) {
                System.err.printf("%s%n%n", preprocHandler.getState()); //NOI18N
            }
        }
    }

    // called under tokStreamLock
    private boolean createAndCacheFullTokenStream(int startContext, int endContext, /*in-out*/FileTokenStreamCache tsCache) {
        PreprocessorStatePair bestStatePair = getContextPreprocStatePair(startContext, endContext);
        PreprocHandler preprocHandler = getPreprocHandler(bestStatePair);
        if (preprocHandler == null) {
            return false;
        }
        PreprocHandler.State ppState = preprocHandler.getState();
        TokenStreamProducer tsp = TokenStreamProducer.create(this, true, false);
        if (tsp == null) {
            // probably file was removed
            return false;
        }
        String contextLanguage = this.getContextLanguage(ppState);
        String contextLanguageFlavor = this.getContextLanguageFlavor(ppState);
        tsp.prepare(preprocHandler, contextLanguage, contextLanguageFlavor, true);
        TokenStream tokenStream = tsp.getTokenStreamForCaching(interrupter);
        if (tokenStream == null) {
            return false;
        }
        APTLanguageFilter languageFilter = getLanguageFilter(ppState);
        // after the next call builder will be ready to create pc state
        List<APTToken> tokens = APTUtils.toList(tokenStream);
        // Only now we can create pcState and cache if possible
        FilePreprocessorConditionState pcState = tsp.release();
        // cache collected tokens associaited with PCState
        tsCache.cacheTokens(pcState, tokens, languageFilter);
        return true;
    }
    
    private static final class TokenStreamLock {}
    private final Object tokStreamLock = new TokenStreamLock();
    private Reference<FileTokenStreamCache> tsRef = new SoftReference<>(null);

    private FileTokenStreamCache getTokenStreamCache() {
        FileTokenStreamCache cache = tsRef.get();
        if (cache == null) {
            synchronized (tokStreamLock) {
                cache = tsRef.get();
                if (cache == null) {
                    cache = new FileTokenStreamCache();
                    tsRef = new WeakReference<>(cache);
                }
            }
        }
        return cache;
    }
    
    /**
     *
     * @param startOffset
     * @param endOffset
     * @param firstTokenIDIfExpandMacros pass 0 if not interested in particular token type
     * @param filtered
     * @return
     */
    public final TokenStream getTokenStream(int startContextOffset, int endContextOffset, int/*CPPTokenTypes*/ firstTokenIDIfExpandMacros, boolean filtered) {
        boolean trace = false;
        FileTokenStreamCache cache = tsRef.get();
        TokenStream stream;
        if (cache == null) {
            stream = null;
        } else {
            stream = cache.getTokenStreamInActiveBlock(filtered, startContextOffset, endContextOffset, firstTokenIDIfExpandMacros);
        }
        if (stream != null) {
            if (trace) {
                System.err.printf("found for %s %s stream [%d-%d]%n", getAbsolutePath(), (filtered ? "filtered" : ""), startContextOffset, endContextOffset); // NOI18N
            }
        } else {
            // we need to build new full token stream
            synchronized (tokStreamLock) {
                cache = tsRef.get();
                if (cache == null) {
                    cache = new FileTokenStreamCache();
                    tsRef = new WeakReference<>(cache);
                } else {
                    // could be already created by parallel thread
                    stream = cache.getTokenStreamInActiveBlock(filtered, startContextOffset, endContextOffset, firstTokenIDIfExpandMacros);
                }
                if (stream == null) {
                    if (trace) {
                        System.err.printf("creating for %s %s stream [%d-%d]%n", getAbsolutePath(), (filtered ? "filtered" : ""), startContextOffset, endContextOffset); // NOI18N
                    }
                    if (createAndCacheFullTokenStream(startContextOffset, endContextOffset, cache)) {
                        stream = cache.getTokenStreamInActiveBlock(filtered, startContextOffset, endContextOffset, firstTokenIDIfExpandMacros);
                    }
                } else {
                    if (trace) {
                        System.err.printf("found for just cached %s %s stream [%d-%d]%n", getAbsolutePath(), (filtered ? "filtered" : ""), startContextOffset, endContextOffset); // NOI18N
                    }
                }
            }
        }
        return stream;
    }

    /**
     * Token stream with replaced fragment
     *
     * @param startContextOffset
     * @param endContextOffset
     * @param context
     * @param filtered
     * @return
     */
    public final TokenStream getTokenStreamForMacroExpansion(int startContextOffset, int endContextOffset, String context, boolean filtered) {
        FileTokenStreamCache cache = new FileTokenStreamCache();
        PreprocessorStatePair bestStatePair = getContextPreprocStatePair(startContextOffset, endContextOffset);
        PreprocHandler preprocHandler = getPreprocHandler(bestStatePair);
        if (preprocHandler == null) {
            return null;
        }
        PreprocHandler.State ppState = preprocHandler.getState();
        TokenStreamProducer tsp = TokenStreamProducer.create(this, true, false);
        if (tsp == null) {
            // probably file was removed
            return null;
        }
        String contextLanguage = this.getContextLanguage(ppState);
        String contextLanguageFlavor = this.getContextLanguageFlavor(ppState);
        tsp.prepare(preprocHandler, contextLanguage, contextLanguageFlavor, true);
        tsp.setCodePatch(new TokenStreamProducer.CodePatch(startContextOffset, endContextOffset, context));
        TokenStream tokenStream = tsp.getTokenStreamForCaching(interrupter);
        if (tokenStream == null) {
            return null;
        }
        APTLanguageFilter languageFilter = getLanguageFilter(ppState);
        // after the next call builder will be ready to create pc state
        List<APTToken> tokens = APTUtils.toList(tokenStream);
        // Only now we can create pcState and cache if possible
        FilePreprocessorConditionState pcState = tsp.release();
        // cache collected tokens associaited with PCState
        cache.cacheTokens(pcState, tokens, languageFilter);
        return cache.getTokenStreamInActiveBlock(filtered, startContextOffset, startContextOffset+context.length(), 0);
    }

    private TokenStream getTokenStreamOfIncludedFile(final CsmInclude include) {
        FileImpl file = (FileImpl) include.getIncludeFile();
        if (file != null && file.isValid()) {
            // create ppHandler till #include directive
            PreprocessorStatePair includeContextPair = this.getContextPreprocStatePair(include.getStartOffset(), include.getEndOffset());
            if (includeContextPair == null) {
                return file.getTokenStream(0, Integer.MAX_VALUE, 0, true);
            }
            TokenStreamProducer tsp = TokenStreamProducer.create(this, true, false);
            if (tsp == null) {
                // probably file was removed
                return file.getTokenStream(0, Integer.MAX_VALUE, 0, true);
            }
            PreprocHandler.State thisFileStartState = includeContextPair.state;
            TokenStream includedFileTS = tsp.getTokenStreamOfIncludedFile(thisFileStartState, include, interrupter);
            if(includedFileTS != null) {
                APTLanguageFilter languageFilter = file.getLanguageFilter(thisFileStartState);
                return languageFilter.getFilteredStream(includedFileTS);
            }
        }
        return null;
    }
    
    /** For test purposes only */
    /*package*/ void testErrors(TraceModel.ErrorListener errorListener) {
        Collection<ParserError> parserErrors = new ArrayList<>();
        getErrors(parserErrors);
        for (ParserError e : parserErrors) {
            errorListener.error(e.message, e.line, e.column);
        }
    }

    private static class ParserBasedTokenBuffer implements ReadOnlyTokenBuffer {

        private final Parser parser;

        public ParserBasedTokenBuffer(Parser parser) {
            this.parser = parser;
        }

        @Override
        public int LA(int i) {
            return parser.LA(i);
        }

        @Override
        public Token LT(int i) {
            return parser.LT(i);
        }
    }

    public final APTFileCacheEntry getAPTCacheEntry(PreprocHandler.State ppState, Boolean createExclusiveIfAbsent) {
        if (!TraceFlags.APT_FILE_CACHE_ENTRY) {
            return null;
        }
        APTFileCacheEntry out = APTFileCacheManager.getInstance(getBuffer().getFileSystem()).getEntry(getAbsolutePath(), ppState, createExclusiveIfAbsent);
        assert createExclusiveIfAbsent == null || out != null;
        return out;
    }

    public final void setAPTCacheEntry(PreprocHandler.State ppState, APTFileCacheEntry entry, boolean cleanOthers) {
        if (TraceFlags.APT_FILE_CACHE_ENTRY) {
            final FileBuffer buf = getBuffer();
            APTFileCacheManager.getInstance(buf.getFileSystem()).setAPTCacheEntry(buf.getAbsolutePath(), ppState, entry, cleanOthers);
        }
    }

    public ReadOnlyTokenBuffer getErrors(final Collection<ParserError> result) {
        CsmParserProvider.ParserErrorDelegate delegate = new CsmParserProvider.ParserErrorDelegate() {

            @Override
            public void onError(ParserError e) {
                result.add(e);
            }
        };
        // FIXUP (up to the end of the function)
        // should be changed with setting appropriate flag and using common parsing mechanism
        // (Now doParse performs too many actions that should NOT be performed if parsing just for getting errors;
        // making this actions conditional will make doParse code spaghetty-like. That's why I use this fixup)
        // Another issue to be solved is threading and cancellation
        if (TraceFlags.TRACE_ERROR_PROVIDER) {
            System.err.printf("%n%n>>> Start parsing (getting errors) %s %n", getName());
        }
        long time = TraceFlags.TRACE_ERROR_PROVIDER ? System.currentTimeMillis() : 0;
        int flags = TraceFlags.TRACE_ERROR_PROVIDER ? 0 : CPPParserEx.CPP_SUPPRESS_ERRORS;
        String fileLanguage = getFileLanguage();
        String fileLanguageFlavor = getFileLanguageFlavor();
        flags = ParserProviderImpl.adjustAntlr2ParserFlagsForLanguage(flags, fileLanguage, fileLanguageFlavor);
        try {
            // use cached TS
            TokenStream tokenStream = getTokenStream(0, Integer.MAX_VALUE, 0, true);

            if (tokenStream != null) {
                if(TraceFlags.CPP_PARSER_NEW_GRAMMAR) {
                    CsmProject project = getProject();
                    if(parsingErrors != null) {
                        result.addAll(parsingErrors);
                    }
                    return new ParserBasedTokenBuffer(null);
                } else {
                    CPPParserEx parser = CPPParserEx.getInstance(this, tokenStream, flags);
                    parser.setErrorDelegate(delegate);
                    parser.setLazyCompound(false);
                    parser.translation_unit();
                    return new ParserBasedTokenBuffer(parser);
                }
            }
        } catch (Throwable ex) {
            System.err.println(ex.getClass().getName() + " at parsing file " + fileBuffer.getAbsolutePath()); // NOI18N
            CndUtils.printStackTraceOnce(ex);
        } finally {
            if (TraceFlags.TRACE_ERROR_PROVIDER) {
                System.err.printf("<<< Done parsing (getting errors) %s %d ms%n%n%n", getName(), System.currentTimeMillis() - time);
            }
        }
        return null;
    }

    private CsmParserResult doParse(ParseDescriptor parseParams) {

        if (reportErrors) {
            if (!ParserThreadManager.instance().isParserThread() && !ParserThreadManager.instance().isStandalone()) {
                String text = "Reparsing should be done only in a special Code Model Thread!!!"; // NOI18N
                Diagnostic.trace(text);
                new Throwable(text).printStackTrace(System.err);
            }
        }
        PreprocHandler preprocHandler = parseParams.getCurrentPreprocHandler();
        if (preprocHandler == null) {
            CndUtils.assertUnconditional("Null preprocessor handler"); //NOI18N
            return null;
        }

        ParseStatistics.getInstance().fileParsed(this);

//        if (TraceFlags.SUSPEND_PARSE_TIME != 0) {
//            if (getAbsolutePath().toString().endsWith(".h")) { // NOI18N
//                try {
//                    Thread.sleep(TraceFlags.SUSPEND_PARSE_TIME * 1000);
//                } catch (InterruptedException ex) {
//                    Exceptions.printStackTrace(ex);
//                }
//            }
//        }
        CsmParserResult parseResult = null;
        long time = (emptyAstStatictics) ? System.currentTimeMillis() : 0;
        // make real parse
        PreprocHandler.State ppState = preprocHandler.getState();
        ProjectBase startProject = Utils.getStartProject(ppState);
        if (startProject == null) {
            System.err.println(" null project for " + APTHandlersSupport.extractStartEntry(ppState) + // NOI18N
                    "%n while parsing file " + getAbsolutePath() + "%n of project " + getProject()); // NOI18N
            return null;
        }

        final boolean cacheTokens = !parseParams.triggerParsingActivity;
        TokenStream filteredTokenStream;
        FilePreprocessorConditionState pcState = null;
        
        if (cacheTokens) {
            TokenStream ts = parseParams.tsp.getTokenStreamForParsingAndCaching(interrupter);
            if (ts == null) { // can happen if the file became invalid
                return null;
            }
            List<APTToken> tokenList = APTUtils.toList(ts);
            pcState = parseParams.tsp.release();
            APTLanguageFilter languageFilter = APTLanguageSupport.getInstance().getFilter(parseParams.getLanguage(), parseParams.getLanguageFlavor());
            FileTokenStreamCache cache = getTokenStreamCache();            
            filteredTokenStream = cache.cacheTokensAndReturnFiltered(pcState, tokenList, languageFilter);
        } else {
            filteredTokenStream = parseParams.tsp.getTokenStreamForParsing(parseParams.getLanguage(), interrupter);
            if (filteredTokenStream == null) { // can happen if the file became invalid
                return null;
            }
        }

        if (filteredTokenStream == null) {
            System.err.println(" null token stream for " + APTHandlersSupport.extractStartEntry(ppState) + // NOI18N
                    "%n while parsing file " + getAbsolutePath() + "%n of project " + getProject()); // NOI18N
            return null;
        }        
        CsmParser parser = CsmParserProvider.createParser(parseParams);
        assert parser != null : "no parser for " + this;

        parser.init(this, filteredTokenStream, parseParams.callback);

        parseResult = parser.parse(parseParams.lazyCompound ? CsmParser.ConstructionKind.TRANSLATION_UNIT : CsmParser.ConstructionKind.TRANSLATION_UNIT_WITH_COMPOUND);
        PreprocHandler.State ppOrigState = ppState;
        PreprocHandler.State ppUsedState = parseParams.tsp.getPreprocHandlerStartState();
        if (!cacheTokens) {
            pcState = parseParams.tsp.release();
        }

        assert pcState != null;
        startProject.setParsedPCState(this, ppOrigState, ppUsedState, pcState);

        if (emptyAstStatictics) {
            time = System.currentTimeMillis() - time;
            boolean empty = parseResult.isEmptyAST();
            if(empty) {
                System.err.println("PARSED FILE " + getAbsolutePath() + " HAS EMPTY AST" + ' ' + time + " ms");
            }
        }
        if (TraceFlags.DUMP_AST) {
            parseResult.dumpAST();
        }
        parseParams.getFileContent().setErrorCount(parseResult.getErrorCount());
        if (parsingState == ParsingState.MODIFIED_WHILE_BEING_PARSED) {
            parseResult = null;
            if (TraceFlags.TRACE_CACHE) {
                System.err.println("CACHE: not save cache for file modified during parsing" + getAbsolutePath());
            }
        }
        if (!cacheTokens) {
            clearStateCache();
            lastMacroUsages = null;
        }
        TraceModel.TestHook aHook = hook;
        if (aHook != null) {
            aHook.parsingFinished(this, preprocHandler);
        }
//        parseCount++;
        return parseResult;
    }

    public List<CsmReference> getLastMacroUsages() {
        Reference<List<CsmReference>> ref = lastMacroUsages;
        return ref != null ? ref.get() : null;
    }

    public void setLastMacroUsages(List<CsmReference> res) {
        lastMacroUsages = new SoftReference<>(Collections.unmodifiableList(res));
    }

    public long getLastParsedTime() {
        return lastParsed;
    }

    long getLastParsedCompilationUnitCRC() {
        return lastParsedCompilationUnitCRC;
    }

    private void updateModelAfterParsing(FileContent fileContent, long compUnitCRC) {
        Map<CsmUID<FunctionImplEx<?>>, Pair<AST, MutableDeclarationsContainer>> fakeASTs = fileContent.getFakeFuncData();
        ProjectBase projectImpl = getProjectImpl(true);
        CsmUID<CsmFile> thisFileUID = getUID();
        for (Map.Entry<CsmUID<FunctionImplEx<?>>, Pair<AST, MutableDeclarationsContainer>> entry : fakeASTs.entrySet()) {
            projectImpl.trackFakeFunctionData(thisFileUID, entry.getKey(), entry.getValue());
        }
        hasBrokenIncludes.set(fileContent.hasBrokenIncludes());
        // handle file content
        currentFileContent = fileContent.toWeakReferenceBasedCopy();
        currentFileContent.put();
        lastParsed = fileBuffer.lastModified();
        lastParsedBufferCRC = fileBuffer.getCRC();
        lastParsedCompilationUnitCRC = compUnitCRC;
        // using file time as parse time disallows offline index: in most cases timestamps differ
        if (TraceFlags.USE_CURR_PARSE_TIME) {
            lastParsed = Math.max(System.currentTimeMillis(), fileBuffer.lastModified());
        }
        if (TraceFlags.TRACE_VALIDATION) {
            System.err.printf("PARSED    %s %n\tlastModified=%d%n\t  lastParsed=%d  diff=%d%n",
                    getAbsolutePath(), fileBuffer.lastModified(), lastParsed, fileBuffer.lastModified() - lastParsed);
        }
        if(TraceFlags.CPP_PARSER_NEW_GRAMMAR) {
            if(parsingErrors == null) {
                parsingErrors = new ArrayList<>();
            }
            parsingErrors.clear();
            if(currentFileContent != null) {
                parsingErrors.addAll(currentFileContent.getParserErrors());
            }
        }
        if (TraceFlags.PARSE_HEADERS_WITH_SOURCES) {
            for (FileContent includedFileContent : fileContent.getIncludedFileContents()) {
                FileImpl fileImplIncluded = includedFileContent.getFile();
                fileImplIncluded.updateModelAfterParsing(includedFileContent, compUnitCRC);
                fileImplIncluded.parsingFileContentRef.get().set(null);
                if(TraceFlags.CPP_PARSER_NEW_GRAMMAR) {
                    if(fileImplIncluded.parsingErrors == null) {
                        fileImplIncluded.parsingErrors = new ArrayList<>();
                    }
                    fileImplIncluded.parsingErrors.clear();
                    if(includedFileContent != null) {
                        fileImplIncluded.parsingErrors.addAll(includedFileContent.getParserErrors());
                    }
                }
                synchronized (fileImplIncluded.changeStateLock) {
                    fileImplIncluded.state = State.PARSED;
                    addRemoveModifiedFile(false);
                }
                RepositoryUtils.put(fileImplIncluded);
            }
        }
    }

    public void addInstantiation(CsmInstantiation inst) {
        getFileInstantiations().addInstantiation(inst);
    }

    public static final Comparator<CsmOffsetable> START_OFFSET_COMPARATOR = new Comparator<CsmOffsetable>() {

        @Override
        public int compare(CsmOffsetable o1, CsmOffsetable o2) {
            if (o1 == o2) {
                return 0;
            }
            int ofs1 = o1.getStartOffset();
            int ofs2 = o2.getStartOffset();
            if (ofs1 == ofs2) {
                return 0;
            } else {
                return (ofs1 - ofs2);
            }
        }
    };

    @Override
    public String getText(int start, int end) {
        try {
            return fileBuffer.getText(start, end);
        } catch (IOException e) {
            DiagnosticExceptoins.register(e);
            return "";
        }
    }

    @Override
    public CharSequence getText() {
        try {
            return fileBuffer.getText();
        } catch (IOException e) {
            DiagnosticExceptoins.register(e);
            return "";
        }
    }

    @Override
    public CsmProject getProject() {
        return _getProject(false);
    }

    public CsmUID<CsmProject> getProjectUID() {
        return projectUID;
    }

    /** Just a convenient shortcut to eliminate casts */
    public ProjectBase getProjectImpl(boolean assertNotNull) {
        return _getProject(assertNotNull);
    }

    @Override
    public CharSequence getName() {
        return CharSequences.create(PathUtilities.getBaseName(getAbsolutePath().toString()));
    }

    @Override
    public Collection<CsmInclude> getIncludes() {
        checkNotInParsingThreadImpl();
        return getFileIncludes().getIncludes();
    }

    @Override
    public Collection<CsmErrorDirective> getErrors() {
        checkNotInParsingThreadImpl();
        return new ArrayList<CsmErrorDirective>(currentFileContent.getErrors());
    }

    public Iterator<CsmInclude> getIncludes(CsmFilter filter) {
        checkNotInParsingThreadImpl();
        return getFileIncludes().getIncludes(filter);
    }

    public Collection<CsmInclude> getBrokenIncludes() {
        checkNotInParsingThreadImpl();
        return getFileIncludes().getBrokenIncludes();
    }

    public boolean hasBrokenIncludes() {
        checkNotInParsingThreadImpl();
        return hasBrokenIncludes.get();
    }

    /**
     * Gets the list of the static functions declarations (not definitions) This
     * is necessary for finding definitions/declarations since file-level static
     * functions (i.e. c-style static functions) aren't registered in project
     */
    public Collection<CsmFunction> getStaticFunctionDeclarations() {
        return getFileDeclarations().getStaticFunctionDeclarations();
    }

    public Iterator<CsmFunction> getStaticFunctionDeclarations(CsmFilter filter) {
        return getFileDeclarations().getStaticFunctionDeclarations(filter);
    }

    public Collection<CsmVariable> getStaticVariableDeclarations() {
        return getFileDeclarations().getStaticVariableDeclarations();
    }

    public Iterator<CsmVariable> getStaticVariableDeclarations(CsmFilter filter) {
        return getFileDeclarations().getStaticVariableDeclarations(filter);
    }

    public boolean hasDeclarations() {
        return getFileDeclarations().hasDeclarations();
    }

    @Override
    public Collection<CsmOffsetableDeclaration> getDeclarations() {
        return getFileDeclarations().getDeclarations();
    }

    /**
     * Returns number of declarations.
     * Does not fixFakeRegistrations, so this size could be inaccurate
     *
     * @return number of declarations
     */
    public int getDeclarationsSize(){
        return getFileDeclarations().getDeclarationsSize();
    }

    public Iterator<CsmOffsetableDeclaration> getDeclarations(CsmFilter filter) {
        return getFileDeclarations().getDeclarations(filter);
    }

    public Collection<CsmUID<CsmOffsetableDeclaration>> getDeclarations(CsmDeclaration.Kind[] kinds, CharSequence prefix) {
        return getFileDeclarations().getDeclarations(kinds, prefix);
    }

    public Collection<CsmUID<CsmOffsetableDeclaration>> getDeclarations(int startOffset, int endOffset) {
        return getFileDeclarations().getDeclarations(startOffset, endOffset);
    }

    public Iterator<CsmOffsetableDeclaration> getDeclarations(int offset) {
        return getFileDeclarations().getDeclarations(offset);
    }

    public Collection<CsmReference> getReferences() {
        return getFileReferences().getReferences();
    }

    public Collection<CsmReference> getReferences(Collection<CsmObject> objects) {
        return getFileReferences().getReferences(objects);
    }

    public boolean addReference(CsmReference ref, CsmObject referencedObject) {
        return getFileReferences().addReference(ref, referencedObject);
    }

    public CsmReference getReference(int offset) {
        return getFileReferences().getReference(offset);
    }

    public boolean addResolvedReference(CsmReference ref, CsmObject referencedObject) {
        return getFileReferences().addResolvedReference(ref, referencedObject);
    }

    public void removeResolvedReference(CsmReference ref) {
        getFileReferences().removeResolvedReference(ref);
    }

    public CsmReference getResolvedReference(CsmReference ref) {
        return getFileReferences().getResolvedReference(ref);
    }

    @Override
    public Collection<CsmMacro> getMacros() {
        checkNotInParsingThreadImpl();
        return getFileMacros().getMacros();
    }

    public Iterator<CsmMacro> getMacros(CsmFilter filter) {
        checkNotInParsingThreadImpl();
        return getFileMacros().getMacros(filter);
    }

    public Collection<CsmUID<CsmMacro>> findMacroUids(CharSequence name) {
        checkNotInParsingThreadImpl();
        return getFileMacros().findMacroUids(name);
    }

    @Override
    public CharSequence getAbsolutePath() {
        return fileBuffer.getAbsolutePath();
    }

    @Override
    public FileObject getFileObject() {
        return fileBuffer.getFileObject();
    }

    @Override
    public Collection<CsmScopeElement> getScopeElements() {
        checkNotInParsingThreadImpl();
        return currentFileContent.getScopeElements();
    }

    public void setFileGuard(int guardStart, int guardEnd) {
        this.guardStart = guardStart;
        this.guardEnd = guardEnd;
    }

    public boolean hasFileGuard() {
        return guardStart >= 0;
    }

    public CsmOffsetable getFileGuard() {
        if (guardStart >= 0) {
            return new Offsetable(this, guardStart, guardStart);
        }
        return null;
    }


    @Override
    public boolean isValid() {
        if (disposed) {
            return false;
        }
        // NB: the below strongly relates on the fact that the project stays in memory
        return !projectDisposed.get();
    }

    @Override
    public boolean isParsed() {
        synchronized (changeStateLock) {
            return state == State.PARSED;
        }
    }

    public void setLwmReady() {
        synchronized (changeStateLock) {
             state = State.PARSED;
             postParse();
             addRemoveModifiedFile(false);
        }
        RepositoryUtils.put(this);
    }

    /*package*/final State getState() {
        synchronized (changeStateLock) {
            return state;
        }
    }

    public final String getStateFromTest() {
        assert CndUtils.isUnitTestMode();
        return state.toString();
    }

    public final String getParsingStateFromTest() {
        assert CndUtils.isUnitTestMode();
        return parsingState.toString();
    }

    public boolean isParsingOrParsed() {
        synchronized (changeStateLock) {
            return state == State.PARSED || parsingState != ParsingState.NOT_BEING_PARSED;
        }
    }

    private static final boolean TRACE_SCHUDULE_PARSING = Boolean.getBoolean("cnd.trace.schedule.parsing"); // NOI18N
    @Override
    public void scheduleParsing(boolean wait) throws InterruptedException {
        synchronized (stateLock) {
            while (!isParsed()) {
                // when IDE exists during ensureParsed, then file is left in
                // INITIAL state which is not PARSED
                // check such a case to prevent infinite loop of code model clients
                if (!CsmModelAccessor.isModelAlive()) {
                    if (TraceFlags.TRACE_VALIDATION || TraceFlags.TRACE_MODEL_STATE) {
                        System.err.printf("scheduleParsing: %s file is interrupted on closing model%n", this.getAbsolutePath());
                    }
                    return;
                }
                String oldName = wait ? Thread.currentThread().getName() : "";
                try {
                    if (wait) {
                        StringBuilder name = new StringBuilder(oldName);
                        name.append(": scheduleParsing ").append(getAbsolutePath()); // NOI18N
                        name.append(" in states ").append(state).append(", ").append(parsingState); // NOI18N
                        Thread.currentThread().setName(name.toString());
                    }
                    if (!isParsingOrParsed()) {
                        if (TRACE_SCHUDULE_PARSING) {
                            System.err.printf("scheduleParsing: enqueue %s in states %s, %s%n", getAbsolutePath(), state, parsingState); // NOI18N
                        }
                        boolean added = ParserQueue.instance().addToBeParsedNext(this);
                        if (!added) {
                            return;
                        }
                    }
                    if (wait) {
                        if (TRACE_SCHUDULE_PARSING) {
                            System.err.printf("scheduleParsing: waiting for %s in states %s, %s%n", getAbsolutePath(), state, parsingState); // NOI18N
                        }
                        stateLock.wait();
                        if (TRACE_SCHUDULE_PARSING) {
                            System.err.printf("scheduleParsing: lock notified for %s in states %s, %s%n", getAbsolutePath(), state, parsingState); // NOI18N
                        }
                    } else {
                        return;
                    }
                } finally {
                    if (wait) {
                        Thread.currentThread().setName(oldName);
                    }
                }
            }
        }
    }

    private void clearFakeRegistrations() {
        getProjectImpl(true).cleanAllFakeFunctionAST(getUID());
    }

    private volatile boolean alreadyInFixFakeRegistrations = false;

    /**
     * Fixes ambiguities.
     *
     * @param clearFakes - indicates that we should clear list of fake registrations (all have been parsed and we have no chance to fix them in future)
     */
    private boolean fixFakeRegistrations(boolean projectParsedMode) {
        try {
            CsmCacheManager.enter();
            checkNotInParsingThreadImpl();
            boolean result = false;
            result |= fixFakeFunctionRegistrations(projectParsedMode);
            result |= fixFakeIncludeRegistrations(projectParsedMode);
            return result;
        } finally {
            CsmCacheManager.leave();
        }
    }

    private boolean fixFakeFunctionRegistrations(boolean projectParsedMode) {
        checkNotInParsingThreadImpl();
        boolean wereFakes = false;
        FileContent curContent = currentFileContent;
        List<CsmUID<FunctionImplEx<?>>> fakeFunctionRegistrations = curContent.getFakeFunctionRegistrations();
        synchronized (fakeFunctionRegistrations) {
            if (!alreadyInFixFakeRegistrations) {
                alreadyInFixFakeRegistrations = true;
                if (fakeFunctionRegistrations.isEmpty() || !isValid()) {
                    alreadyInFixFakeRegistrations = false;
                    return false;
                }
                if (fakeFunctionRegistrations.size() > 0) {
                    for (int i = 0; i < fakeFunctionRegistrations.size(); i++) {
                        CsmUID<FunctionImplEx<?>> fakeUid = fakeFunctionRegistrations.get(i);
                        Pair<AST, MutableDeclarationsContainer> fakeData = getProjectImpl(true).getFakeFunctionData(getUID(), fakeUid);
                        CsmDeclaration curElem = fakeUid.getObject();
                        if (curElem != null) {
                            if (curElem instanceof FunctionImplEx<?>) {
                                wereFakes = true;
                                incParseCount();
                                if (((FunctionImplEx<?>) curElem).fixFakeRegistration(curContent, projectParsedMode, fakeData)) {
                                    getProjectImpl(true).trackFakeFunctionData(getUID(), fakeUid, null);
                                }
                                incParseCount();
                            } else {
                                DiagnosticExceptoins.register(new Exception("Incorrect fake registration class: " + curElem.getClass() + " for fake UID:" + fakeUid)); // NOI18N
                            }
                        }
                    }
                }
                alreadyInFixFakeRegistrations = false;
            }
        }
        return wereFakes;
    }

    private boolean fixFakeIncludeRegistrations(boolean projectParsedMode) {
        checkNotInParsingThreadImpl();
        boolean wereFakes = false;
        FileContent fileContent = currentFileContent;
        for (FakeIncludePair fakeIncludePair : fileContent.getFakeIncludeRegistrations()) {
            synchronized (fakeIncludePair) {
                if (!fakeIncludePair.isFixed()) {
                    CsmInclude include = UIDCsmConverter.UIDtoIdentifiable(fakeIncludePair.getIncludeUid());
                    if (include != null) {
                        CsmOffsetableDeclaration container = UIDCsmConverter.UIDtoDeclaration(fakeIncludePair.getContainerUid());
                        if (container != null && container.isValid()) {
                            FileImpl includedFile = (FileImpl) include.getIncludeFile();
                            if (includedFile != null && includedFile.isValid()) {
                                FileContent includedFileContent = includedFile.currentFileContent;
                                TokenStream ts = this.getTokenStreamOfIncludedFile(include);
                                if (ts != null) {
                                    CsmParser parser = CsmParserProvider.createParser(includedFile);
                                    assert parser != null : "no parser for " + this;
                                    parser.init(this, ts, null);
                                    if (container instanceof EnumImpl) {
                                        EnumImpl enumImpl = (EnumImpl) container;
                                        CsmParserResult result = parser.parse(CsmParser.ConstructionKind.ENUM_BODY);
                                        result.render(includedFileContent, enumImpl, Boolean.FALSE);
                                        fakeIncludePair.markFixed();
                                        wereFakes = true;
                                    } else if (container instanceof ClassImpl) {
                                        ClassImpl cls = (ClassImpl) container;
                                        CsmParserResult result = parser.parse(CsmParser.ConstructionKind.CLASS_BODY);
                                        CsmDeclaration.Kind kind = cls.getKind();
                                        CsmVisibility visibility = CsmVisibility.PRIVATE;
                                        if(kind == CsmDeclaration.Kind.CLASS) {
                                            // FIXUP: it's better to have extra items in completion list
                                            // for crazy classes, than fail on resolving included
                                            // public methods
                                            // IZ#204951 - The c++ parser does not follow/parse #includes which are textually nested within a class definition
                                            visibility = CsmVisibility.PUBLIC;
                                        } else if( kind == CsmDeclaration.Kind.STRUCT ||
                                                kind == CsmDeclaration.Kind.UNION) {
                                            visibility = CsmVisibility.PUBLIC;
                                        }
                                        result.render(includedFileContent, cls, visibility, Boolean.FALSE);
                                        fakeIncludePair.markFixed();
                                        wereFakes = true;
                                    } else if (container instanceof NamespaceDefinitionImpl) {
                                        CsmParserResult result = parser.parse(CsmParser.ConstructionKind.NAMESPACE_DEFINITION_BODY);
                                        result.render(includedFileContent, (NamespaceDefinitionImpl) container);
                                        fakeIncludePair.markFixed();
                                        wereFakes = true;
                                    }
                                } else {
                                    APTUtils.LOG.log(Level.WARNING, "fixFakeIncludeRegistrations: file {0} has not tokens, probably empty or removed?", new Object[]{getBuffer().getUrl()});// NOI18N
                                }
                            }
                        }
                    }
                }
            }
        }
        return wereFakes;
    }

    public
    @Override
    String toString() {
        return "" + this.state + " FileImpl @" + hashCode() + ":" + super.hashCode() + ' ' + getAbsolutePath() + " prj:" + System.identityHashCode(this.projectUID) + this.projectUID + " " + this.parsingState; // NOI18N
    }

    @Override
    public final CsmUID<CsmFile> getUID() {
        CsmUID<CsmFile> out = uid;
        if (out == null) {
            synchronized (this) {
                if (uid == null) {
                    uid = out = UIDUtilities.createFileUID(this);
                }
            }
        }
        return uid;
    }
    private CsmUID<CsmFile> uid = null;
    
    public CndTextIndexKey getTextIndexKey() {
        return new CndTextIndexKey(getUnitId(), getFileId());
    }

    public int getFileId() {
        return KeyUtilities.getProjectFileIndex(((KeyBasedUID)getUID()).getKey());
    }

    public int getUnitId() {
        return ((KeyBasedUID)projectUID).getKey().getUnitId();
    }

    ////////////////////////////////////////////////////////////////////////////
    // impl of persistent
    @Override
    public void write(RepositoryDataOutput output) throws IOException {
        // not null UID
        assert this.projectUID != null;
        UIDObjectFactory.getDefaultFactory().writeUID(this.projectUID, output);
        if (TraceFlags.TRACE_CPU_CPP && getAbsolutePath().toString().endsWith("cpu.cc")) { // NOI18N
            new Exception("cpu.cc file@" + System.identityHashCode(this) + " of prjUID@" + System.identityHashCode(this.projectUID) + this.projectUID).printStackTrace(System.err); // NOI18N
        }
        PersistentUtils.writeBuffer(this.fileBuffer, output);
        output.writeBoolean(hasBrokenIncludes.get());
        currentFileContent.write(output);

        output.writeByte(fileType.ordinal());

        output.writeLong(lastParsed);
        output.writeInt(lastParseTime);
        output.writeLong(lastParsedBufferCRC);
        output.writeLong(lastParsedCompilationUnitCRC);
        State curState = state;
        if (curState != State.PARSED && curState != State.INITIAL) {
            if (TraceFlags.TIMING) {
                System.err.printf("file is written in intermediate state %s, switching to INITIAL: %s %n", curState, getAbsolutePath());
            }
            curState = State.INITIAL;
        }
        output.writeByte(curState.ordinal());
        output.writeInt(guardStart);
        output.writeInt(guardEnd);
    }
    //private static boolean firstDump = false;

    public FileImpl(RepositoryDataInput input) throws IOException {
        this.projectUID = UIDObjectFactory.getDefaultFactory().readUID(input);
        if (TraceFlags.TRACE_CPU_CPP && getAbsolutePath().toString().endsWith("cpu.cc")) { // NOI18N
            new Exception("cpu.cc file@" + System.identityHashCode(FileImpl.this) + " of prjUID@" + System.identityHashCode(this.projectUID) + this.projectUID).printStackTrace(System.err); // NOI18N
        }
        // not null UID
        assert this.projectUID != null;
        this.projectRef = null;

        this.fileBuffer = PersistentUtils.readBuffer(input);

        hasBrokenIncludes = new AtomicBoolean(input.readBoolean());
        currentFileContent = new FileContent(this, this, input);

        fileType = FileType.values()[input.readByte()];

        assert fileBuffer != null;
        lastParsed = input.readLong();
        lastParseTime = input.readInt();
        lastParsedBufferCRC = input.readLong();
        lastParsedCompilationUnitCRC = input.readLong();
        state = State.values()[input.readByte()];
        parsingState = ParsingState.NOT_BEING_PARSED;
        guardStart = input.readInt();
        guardEnd = input.readInt();
        ProjectBase project = _getProject(false);
        this.hash = calculateHashCode(project, fileBuffer.getAbsolutePath());
        // NB: the below strongly relates on the fact that the project stays in memory
        this.projectDisposed = (project == null) ? new AtomicBoolean(false) : project.getDisposingFlag();
    }

    public
    @Override
    int hashCode() {
        return hash;
    }

    private static int calculateHashCode(ProjectBase p, CharSequence absPath) {
        CharSequence projectId = (p == null) ? "" : p.getUniqueName(); // NOI18N
        String identityHashPath = projectId + "*" + absPath; // NOI18N
        return identityHashPath.hashCode();
    }

    public
    @Override
    boolean equals(Object obj) {
        if (obj == null || !(obj instanceof FileImpl)) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        FileImpl other = (FileImpl) obj;
        if (this.getAbsolutePath().equals(other.getAbsolutePath())) {
            return this.getProjectImpl(true).getUniqueName().equals(other.getProjectImpl(true).getUniqueName());
        }
        return false;
    }

    // for testing only
    public int getOffset(int line, int column) {
        if (line <= 0 || column <= 0) {
            throw new IllegalArgumentException("line and column are 1-based"); // NOI18N
        }
        try {
            return fileBuffer.getOffsetByLineColumn(line, column);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
            return -1;
        }
    }

    /**
     * returns 1-based line and column associated with offset
     * @param offset interested offset in file
     * @return returns pair {line, column}
     */
    public int[] getLineColumn(int offset) {
        if (offset == Integer.MAX_VALUE) {
            try {
                offset = fileBuffer.getCharBuffer().length;
            } catch (IOException e) {
                DiagnosticExceptoins.register(e);
                offset = 0;
            }
        }
        try {
            return fileBuffer.getLineColumnByOffset(offset);
        } catch (IOException ex) {
            ex.printStackTrace(System.err);
            return new int[]{0, 0};
        }
    }

    private final FileStateCache stateCache = new FileStateCache(this);
    /*package-local*/ void cacheVisitedState(PreprocHandler.State inputState, PreprocHandler outputHandler, FilePreprocessorConditionState pcState) {
        stateCache.cacheVisitedState(inputState, outputHandler, pcState);
    }

    /*package-local*/ PreprocessorStatePair getCachedVisitedState(PreprocHandler.State inputState) {
        return stateCache.getCachedVisitedState(inputState);
    }

    /*package-local*/ void clearStateCache() {
        tsRef.clear();
        stateCache.clearStateCache();
        final FileBuffer buf = this.getBuffer();
        APTFileCacheManager.getInstance(buf.getFileSystem()).invalidate(buf.getAbsolutePath());

    }

    private FileComponentDeclarations getFileDeclarations() {
        FileContent contentImpl = getThreadSensitiveContentImpl();
        FileComponentDeclarations fd = contentImpl.getFileDeclarations();
        return fd != null ? fd : FileComponentDeclarations.empty();
    }

    private FileComponentMacros getFileMacros() {
        checkNotInParsingThreadImpl();
        FileComponentMacros fd = currentFileContent.getFileMacros();
        return fd != null ? fd : FileComponentMacros.empty();
    }

    private final AtomicBoolean hasBrokenIncludes;
    private FileComponentIncludes getFileIncludes() {
        checkNotInParsingThreadImpl();
        FileComponentIncludes fd = currentFileContent.getFileIncludes();
        return fd != null ? fd : FileComponentIncludes.empty();
    }

    private FileComponentReferences getFileReferences() {
        FileContent contentImpl = getThreadSensitiveContentImpl();
        FileComponentReferences fd = contentImpl.getFileReferences();
        return fd != null ? fd : FileComponentReferences.empty();
    }

    private FileComponentInstantiations getFileInstantiations() {
        checkNotInParsingThreadImpl();
        FileComponentInstantiations fd = currentFileContent.getFileInstantiations();
        return fd != null ? fd : FileComponentInstantiations.empty();
    }

    private FileContent getThreadSensitiveContentImpl() {
        // in parse context we use current parsing FileContent
        // otherwise currentFileContent
        FileContent contentImpl = getParsingFileContent();
        if (contentImpl == null) {
            contentImpl = currentFileContent;
        }
        return contentImpl;
    }

    public FileContent getCurrentFileContent() {
        assert getParsingFileContent() == null;
        return currentFileContent;
    }
    
    private void checkNotInParsingThreadImpl() {
        if (true) {
            return;
        }
        assert getParsingFileContent() == null;
    }

    private static class EmptyCollection<T> extends AbstractCollection<T> {

        @Override
        public int size() {
            return 0;
        }

        public
        @Override
        boolean contains(Object obj) {
            return false;
        }

        @Override
        public Iterator<T> iterator() {
            return Collections.<T>emptyList().iterator();
        }
    }

    /*package*/static boolean traceFile(CharSequence file) {
        if (TraceFlags.TRACE_FILE_NAME != null) {
            if (TraceFlags.TRACE_FILE_NAME.length() == 0) {
                // trace all files
                return true;
            }
            return file.toString().endsWith(TraceFlags.TRACE_FILE_NAME);
        }
        return false;
    }

    public void dumpInfo(PrintWriter printOut) {
        ProjectBase projectImpl = this.getProjectImpl(false);
        printOut.printf("FI: %s, of %s prj=%s disposing=%s (%d)%n\tprjUID=(%d) %s%n\tfileType=%s, hasSnap=%s hasBroken=%s%n", getName(), // NOI18N
                projectImpl.getClass().getSimpleName(), projectImpl.getName(), projectImpl.isDisposing(), System.identityHashCode(projectImpl),
                System.identityHashCode(projectUID), projectUID,
                this.fileType, toYesNo(this.fileSnapshot!=null), toYesNo(hasBrokenIncludes()));
        printOut.printf("\tlastParsedTime=%d, lastParsed=%d %s %s%n", this.lastParseTime, this.lastParsed, this.parsingState, this.state);// NOI18N
        FileBuffer buffer = getBuffer();
        printOut.printf("\tfileBuf=%s lastModified=%d%n", toYesNo(buffer.isFileBased()), buffer.lastModified());// NOI18N
        String fileLanguage = this.getFileLanguage();
        String fileLanguageFlavor = this.getFileLanguageFlavor();
        if (fileLanguageFlavor.isEmpty()) {
            fileLanguageFlavor = "FLAVOR_UNKNOWN"; // NOI18N
        }
        printOut.printf("\tfileImplLanguage=%s fileImplLanguageFlavor=%s%n", fileLanguage, fileLanguageFlavor);// NOI18N
    }

    public void dumpIndex(PrintWriter printOut) {
        getFileReferences().dump(printOut);
    }

    public void dumpPPStates(PrintWriter printOut) {
        int i = 0;
        final Collection<PreprocessorStatePair> preprocStatePairs = this.getFileContainerOwnPreprocessorStatePairsToDump();
        printOut.printf("Has %d ppStatePairs:%n", preprocStatePairs.size());// NOI18N
        for (PreprocessorStatePair pair : preprocStatePairs) {
            printOut.printf("----------------Pair[%d]------------------------%n", ++i);// NOI18N
            printOut.printf("pc=%s%nstate=%s%n", pair.pcState, pair.state);// NOI18N
        }
        Collection<PreprocHandler> preprocHandlers = this.getFileContainerOwnPreprocHandlersToDump();
        printOut.printf("Converted into %d Handlers:%n", preprocHandlers.size());// NOI18N
        i = 0;
        for (PreprocHandler ppHandler : preprocHandlers) {
            printOut.printf("----------------Handler[%d]------------------------%n", ++i);// NOI18N
            printOut.printf("handler=%s%n", ppHandler);// NOI18N
        }
    }

    public void dumpIncludePPStates(PrintWriter printOut) {
        int i = 0;
        final Collection<PreprocessorStatePair> preprocStatePairs = this.getFileContainerOwnPreprocessorStatePairsToDump();
        printOut.printf("Has %d OWNED ppStatePairs:%n", preprocStatePairs.size());// NOI18N
        for (PreprocessorStatePair pair : preprocStatePairs) {
            printOut.printf("----------------Own Pair[%d]------------------------%n", ++i);// NOI18N
            printOut.printf("pc=%s%nstate=%s%n", pair.pcState, pair.state);// NOI18N
        }
        Collection<CsmProject> projects = CsmModelAccessor.getModel().projects();
        i = 0;
        for (CsmProject csmProject : projects) {
            if (csmProject instanceof ProjectBase) {
                ProjectBase prj = (ProjectBase) csmProject;
                Map<CsmUID<CsmProject> , Collection<PreprocessorStatePair>> includedStates = prj.getIncludedPreprocStatePairs(this);
                if (includedStates.size() > 1) {
                    printOut.printf("ALARM! the same file %s is included as library of %d projects%n", this.getAbsolutePath(), includedStates.size()); // NOI18N
                }
                for (Map.Entry<CsmUID<CsmProject>, Collection<PreprocessorStatePair>> entry : includedStates.entrySet()) {
                    Collection<PreprocessorStatePair> pairs = entry.getValue();
                    printOut.printf("in project %s included %s%n", prj, UIDUtilities.getProjectName(entry.getKey()));// NOI18N
                    for (PreprocessorStatePair pair : pairs) {
                        printOut.printf("----------------Included Pair[%d]------------------------%n", ++i);// NOI18N
                        printOut.printf("with pc=%s%nstate=%s%n", pair.pcState, pair.state);// NOI18N
                    }
                }
            }
        }
    }

    static String toYesNo(boolean b) {
        return b ? "yes" : "no"; // NOI18N
    }

    private static class SpecialStateImpl implements PreprocHandler.State {

        public SpecialStateImpl() {
        }

        @Override
        public boolean isCleaned() {
            return true;
        }

        @Override
        public boolean isCompileContext() {
            return false;
        }

        @Override
        public boolean isValid() {
            return false;
        }

        @Override
        public CharSequence getLanguage() {
            return CharSequences.empty();
        }

        @Override
        public CharSequence getLanguageFlavor() {
            return CharSequences.empty();
        }
    }
}
