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
package org.netbeans.modules.cnd.apt.impl.support.clank;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import org.clang.basic.FileEntry;
import org.clang.basic.IdentifierInfo;
import org.clang.basic.SourceManager;
import org.clang.lex.DirectoryLookup;
import org.clang.lex.MacroInfo;
import org.clang.lex.Preprocessor;
import org.clang.tools.services.support.FileInfo;
import org.clang.tools.services.support.Interrupter;
import org.clang.tools.services.support.FileInfoCallback;
import org.clank.java.std;
import org.clank.java.std.vector;
import org.clank.support.Casts;
import static org.clank.support.Casts.toJavaString;
import org.clank.support.Native;
import org.clank.support.aliases.char$ptr;
import org.llvm.adt.SmallString;
import org.llvm.adt.StringRef;
import org.llvm.adt.aliases.SmallVector;
import org.llvm.support.raw_ostream;
import org.netbeans.modules.cnd.antlr.TokenStream;
import org.netbeans.modules.cnd.apt.debug.APTTraceFlags;
import org.netbeans.modules.cnd.apt.impl.support.clank.ClankDriverImpl.ArrayBasedAPTTokenStream;
import org.netbeans.modules.cnd.apt.support.APTFileSearch;
import org.netbeans.modules.cnd.apt.support.APTHandlersSupport;
import org.netbeans.modules.cnd.apt.support.APTToken;
import org.netbeans.modules.cnd.apt.support.ClankDriver;
import org.netbeans.modules.cnd.apt.support.ClankDriver.ClankMacroDirective;
import org.netbeans.modules.cnd.apt.support.ClankDriver.FileGuard;
import org.netbeans.modules.cnd.apt.support.ClankDriver.MacroExpansion;
import org.netbeans.modules.cnd.apt.support.ClankDriver.MacroUsage;
import org.netbeans.modules.cnd.apt.support.ResolvedPath;
import org.netbeans.modules.cnd.apt.support.api.PreprocHandler;
import org.netbeans.modules.cnd.apt.utils.APTUtils;
import org.netbeans.modules.cnd.debug.DebugUtils;
import org.netbeans.modules.cnd.spi.utils.CndFileSystemProvider;
import org.netbeans.modules.cnd.utils.CndPathUtilities;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.cnd.utils.cache.CharSequenceUtils;
import org.netbeans.modules.cnd.utils.cache.FilePathCache;
import org.openide.filesystems.FileSystem;
import org.openide.util.CharSequences;
import org.openide.util.Exceptions;
import org.openide.util.Pair;
import org.openide.util.Utilities;
import static org.clang.basic.BasicClangGlobals.*;
import org.clang.lex.MacroDefinition;
import org.clank.support.NativePointer;
import static org.netbeans.modules.cnd.apt.impl.support.clank.ClankFileSystemProviderImpl.RFS_PREFIX;
import org.netbeans.modules.cnd.utils.FSPath;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.openide.filesystems.FileObject;

/**
 *
 */
public final class ClankPPCallback extends FileInfoCallback {

    public static final class CancellableInterrupter implements Interrupter {

        final org.netbeans.modules.cnd.support.Interrupter outerDelegate;
        private boolean stoppedState = false;

        public CancellableInterrupter(org.netbeans.modules.cnd.support.Interrupter outerDelegate) {
            this.outerDelegate = outerDelegate;
        }

        @Override
        public boolean isCancelled() {
            return stoppedState/* || outerDelegate.cancelled()*/;
        }

        private void stop() {
            stoppedState = true;
        }

        private void updateStateFromDelegate() {
            stoppedState |= outerDelegate.cancelled();
        }
    }

    private final ClankDriver.ClankPreprocessorCallback delegate;
    private final PreprocHandler ppHandler;
    private final ClankIncludeHandlerImpl includeHandler;
    private final FileSystem startFileSystem;
    private final ArrayList<ClankFileInfoWrapper> includeStack = new ArrayList<ClankFileInfoWrapper>(16);
    private final CancellableInterrupter interrupter;
    
    public ClankPPCallback(PreprocHandler ppHandler,
            raw_ostream traceOS,
            ClankDriver.ClankPreprocessorCallback delegate,
            ClankPPCallback.CancellableInterrupter interrupter) {
        super(traceOS);
        this.ppHandler = ppHandler;
        this.includeHandler = (ClankIncludeHandlerImpl)ppHandler.getIncludeHandler();
        this.startFileSystem = includeHandler.getStartEntry().getFileSystem();
        // reset include stack;
        // will be regenerated from scratch using onEnter/onExit
        this.includeHandler.resetIncludeStack();
        this.delegate = delegate;
        this.interrupter = interrupter;
    }

    @Override
    protected void onUserDiagnosticDirective(FileInfo curStackElement, UserDiagnosticDirectiveInfo directive) {
        if (!directive.isWarning()) {
            PreprocHandler.State stateWhenMetErrorDirective = APTHandlersSupport.createCleanPreprocState(this.ppHandler.getState());
            ClankErrorDirectiveWrapper errorDirectiveWrapper = new ClankErrorDirectiveWrapper(directive, stateWhenMetErrorDirective);
            directive.setAnnotation(errorDirectiveWrapper);
        }
        interrupter.updateStateFromDelegate();
    }

    @Override
    protected void onInclusionDirective(FileInfo curFile, InclusionDirectiveInfo directive) {
        final int stacksSize = includeStack.size();
        // find ResolvedPath for #include
        ResolvedPath resolvedPath = createResolvedPath(curFile, directive);
        StringRef fileNameSpelling = directive.getFileNameSpelling();
        String spelling = Casts.toCharSequence(fileNameSpelling.data(), fileNameSpelling.size()).toString();
        boolean system = directive.isAngled();
        if (spelling.startsWith(RFS_PREFIX)) {
            // this is system pre-include header file
            spelling = ClankFileSystemProviderImpl.getPathFromUrl(spelling);
            system = true;
        }
        ClankFileInfoWrapper currentFileWrapper = includeStack.get(stacksSize - 1);
        final int includeDirectiveIndex = currentFileWrapper.getNextIncludeDirectiveIndex();
        ClankInclusionDirectiveWrapper inclDirectiveWrapper = new ClankInclusionDirectiveWrapper(directive, system
                                                                                                ,includeDirectiveIndex
                                                                                                ,resolvedPath
                                                                                                ,spelling);
        // keep it as annotation 
        directive.setAnnotation(inclDirectiveWrapper);
        assert currentFileWrapper.current == curFile || !curFile.isFile();
        if (resolvedPath == null) {
            if (DebugUtils.STANDALONE) {
                if (APTUtils.LOG.getLevel().intValue() <= Level.SEVERE.intValue()) {
                    System.err.println("FAILED INCLUDE: from " + CndPathUtilities.getBaseName(currentFileWrapper.getFilePath().toString()) + " for:\n\t" + spelling);// NOI18N
                }
            } else {
                APTUtils.LOG.log(Level.WARNING,
                        "failed resolving path from {0} for {1}", // NOI18N
                        new Object[]{currentFileWrapper.getFilePath(), spelling});
            }
        }
        this.delegate.onInclusionDirective(currentFileWrapper, inclDirectiveWrapper);
        interrupter.updateStateFromDelegate();
    }
    
    @Override
    protected void onDeepInclusion() {
        ClankFileInfoWrapper fileInfo = findRecursiveInclusion(includeStack);
        if (fileInfo != null && fileInfo.getInclusionDirective() != null) {
            CharSequence recursivePath = fileInfo.getFilePath();
            ClankDriver.ClankInclusionDirective recursiveInclusionDirective = fileInfo.getInclusionDirective();
            for (ClankFileInfoWrapper file : includeStack) {
                ClankDriver.ClankInclusionDirective fileInclusionDirective = file.getInclusionDirective();
                if (Objects.equals(file.getFilePath(), recursivePath) 
                    && fileInclusionDirective != null 
                    && Objects.equals(fileInclusionDirective.getResolvedPath().getPath(), recursiveInclusionDirective.getResolvedPath().getPath()) 
                    && fileInclusionDirective.getDirectiveStartOffset() == recursiveInclusionDirective.getDirectiveStartOffset())
                {
                    if (fileInclusionDirective instanceof ClankInclusionDirectiveWrapper) {
                        ClankInclusionDirectiveWrapper mutableDirective = (ClankInclusionDirectiveWrapper) fileInclusionDirective;
                        mutableDirective.setRecursive(true);
                    }
                }
            }
        }
        interrupter.updateStateFromDelegate();
    }
    
    @Override
    protected void recoverFromErrorDirective() {
        // APT-style recovery from #error, cut only the current file
        getPreprocessor().cutOffCurFilePreprocessing();
    }

    private ResolvedPath createResolvedPath(FileInfo curFile, InclusionDirectiveInfo directive) {
        FileEntry fileEntry = directive.getFileEntry();
        if (fileEntry == null) {
            // unresolved #include
            return null;
        }
        FileSystem includeFs;
        String includedAbsPath;
        String searchedAbsPath;
        int searchPathSize = directive.getSearchPath().size();
        // search path might start with rfs for remote
        String searchPathUrl = Casts.toJavaString(directive.getSearchPath().data(), searchPathSize);
        // file path might start with rfs for remote
        String fileEntryPathUrl = Casts.toJavaString(fileEntry.getName());
        assert searchPathUrl.isEmpty() || (fileEntryPathUrl.startsWith(RFS_PREFIX) == searchPathUrl.startsWith(RFS_PREFIX)) :
                "local file resolved in remote folder " + searchPathUrl + ":\n" + fileEntry; // NOI18N
        if (searchPathUrl.startsWith(RFS_PREFIX)) {   
            includeFs = CndFileSystemProvider.urlToFileSystem(searchPathUrl);
            if (includeFs == null) {
                Exceptions.printStackTrace(new IllegalStateException("cannot resolve FS for " + searchPathUrl + " from " + curFile)); // NOI18N
                return null;
            }
            searchedAbsPath = ClankFileSystemProviderImpl.getPathFromUrl(searchPathUrl);
            includedAbsPath = ClankFileSystemProviderImpl.getPathFromUrl(fileEntryPathUrl);
            if (CndUtils.isDebugMode()) { 
                FileSystem includedFileFS = CndFileSystemProvider.urlToFileSystem(fileEntryPathUrl);
                assert includeFs == includedFileFS : "search dir fs=" + includeFs + "\n vs. file=" + fileEntryPathUrl + "\nfs=" + includedFileFS;
            }
        } else if (fileEntryPathUrl.startsWith(RFS_PREFIX)) {
            // this could be -include for system headers
            assert (searchPathSize == 0) : "expected emtpy " + searchPathUrl + " for " + fileEntryPathUrl;
            searchedAbsPath = "";
            // FS is in prefix
            includeFs = CndFileSystemProvider.urlToFileSystem(fileEntryPathUrl);
            // abs path in postfix
            includedAbsPath = ClankFileSystemProviderImpl.getPathFromUrl(fileEntryPathUrl);
        } else {
            includeFs = startFileSystem;
            searchedAbsPath = searchPathUrl;
            includedAbsPath = fileEntryPathUrl;
        }
        assert CndPathUtilities.isPathAbsolute(includedAbsPath) : "expected to be abs path [" + includedAbsPath + "]";
        assert (searchPathSize == 0) || CndPathUtilities.isPathAbsolute(searchedAbsPath) : "expected to be abs path [" + searchedAbsPath + "]";
        assert (searchPathSize == 0) == (searchedAbsPath.length() == 0) : "unexpected searchedAbsPath " + searchedAbsPath + " from " + directive.getSearchPath();
        // in NB VFS mode all paths are already normalized
        if (!APTTraceFlags.ALWAYS_USE_NB_FS) {
            includedAbsPath = CndFileUtils.normalizeAbsolutePath(includeFs, includedAbsPath);
            searchedAbsPath = (searchPathSize == 0) ? searchedAbsPath : CndFileUtils.normalizeAbsolutePath(includeFs, searchedAbsPath);
        }
        CndUtils.assertNormalized(includeFs, includedAbsPath);
        if (searchedAbsPath.isEmpty()) {
            // was resolved as absolute path (i.e -include directive)
            String parent = CndPathUtilities.getDirName(includedAbsPath);
            return new ResolvedPath(includeFs, FilePathCache.getManager().getString(parent), includedAbsPath, false, 0);
        } else {
            CndUtils.assertNormalized(includeFs, searchedAbsPath);
            assert curFile != null;
            // FIXME: now we consider search as default when included file is in the same foldler as includer
            // but it doesn't handle situaiton like #include "../dir/file.h" 
            String currentFileFolderUrl = CndPathUtilities.getDirName(Casts.toJavaString(curFile.getName()));
            boolean isDefaultSearchPath = currentFileFolderUrl.equals(searchPathUrl);
            return new ResolvedPath(includeFs, FilePathCache.getManager().getString(searchedAbsPath), includedAbsPath, isDefaultSearchPath, 0);
        }
    }

    @Override
    protected void onSkippedInclusionDirective(FileInfo curFile, InclusionDirectiveInfo directive) {

    }

    @Override
    protected boolean onNotFoundInclusionDirective(FileInfo curFile, StringRef FileName, SmallString RecoveryPath,
            vector<DirectoryLookup> SearchedDirs, int SearchedFromIndex) {
        if (!APTTraceFlags.FIX_NOT_FOUND_INCLUDES) {
            return false;
        }
        APTFileSearch fileSearch = includeHandler.getFileSearch();
        if (fileSearch != null) {
            char$ptr curFilePath = curFile.getName();
            String FileNameStr = Native.$toString(FileName.data(), FileName.size());
            FSPath path = fileSearch.searchInclude(FileNameStr, Native.$toString(curFilePath));
            if (path != null) {
                String headerPath = path.getPath();
                if (headerPath.endsWith(FileNameStr) && (headerPath.length() > FileNameStr.length())) {
                    headerPath = CndFileSystemProvider.toUrl(path).toString();
                    String recoveryDir = headerPath.substring(0, headerPath.length() - FileNameStr.length()-1/*slash*/);
                    RecoveryPath.$assign(NativePointer.create_char$ptr_utf8(recoveryDir));
                    return true;
                } else {
                    // FIXME: we found file, but can not correctly detect recovery dir
                }
            }
        }
        return super.onNotFoundInclusionDirective(curFile, FileName, RecoveryPath, SearchedDirs, SearchedFromIndex);
    }

    @Override
    protected void onEnter(FileInfo enteredFrom, FileInfo enteredTo) {
        if (ClankDriverImpl.TRACE) {
            traceOS.$out("Enter: " + enteredTo).$out("\n").flush(); // NOI18N
        }
        // sometimes we enter and leave some built-in buffers
        // usually it is predefines or cmd line options seen by main file
        // we'd prefer to stay in main file context
        if (enteredTo.isFile()) {
            ClankDriver.ClankFileInfo enteredFromWrapper;
            ClankFileInfoWrapper enteredToWrapper = new ClankFileInfoWrapper(enteredTo, ppHandler);
            // main file is not pushed as include, all others are
            if (includeStack.isEmpty()) {
//                assert includeHandler.getStartEntry().getStartFile().toString().contentEquals(Casts.toCharSequence(enteredTo.getName())) :
//                        includeHandler.getStartEntry() + " vs. " + enteredTo; // NOI18N
                if (CndUtils.isDebugMode()) {
                    CharSequence startUrl;
                    if (APTTraceFlags.USE_CLANK) {
                        startUrl = CndFileSystemProvider.toUrl(includeHandler.getStartEntry().getFileSystem(), includeHandler.getStartEntry().getStartFile());
                    } else {
                        startUrl = includeHandler.getStartEntry().getStartFile();
                    }
                    CndUtils.assertPathsEqualInConsole(startUrl, Casts.toCharSequence(enteredTo.getName()), "{0} vs. {1}", //NOI18N
                            includeHandler.getStartEntry(), enteredTo);
                }
                assert includeHandler.getInclStackIndex() == 0 : " expected zero: " + includeHandler.getInclStackIndex();
                assert enteredToWrapper.getFileIndex() == 0 : " expected zero: " + enteredToWrapper.getFileIndex();
                enteredFromWrapper = null;
            } else {
                ResolvedPath resolvedPath = enteredToWrapper.getResolvedPath();
                int includeDirectiveIndex = enteredToWrapper.getInclusionDirective().getIncludeDirectiveIndex();
                includeHandler.pushInclude(resolvedPath.getFileSystem(), resolvedPath.getPath(),
                        0/*should not be used by client*/, enteredTo.getIncludeStartOffset(), resolvedPath.getIndex(),
                        includeDirectiveIndex);
                includeHandler.cachePreprocessorOutputImplementation(enteredToWrapper);
                enteredFromWrapper = includeStack.get(includeStack.size() - 1);
            }
            // keep stack of active files
            includeStack.add(enteredToWrapper);
            if (!delegate.onEnter(enteredFromWrapper, enteredToWrapper)) {
                // client doesn't want to enter file or error detected by client, full stop
                interrupter.stop();
            }
        } else {
            assert includeStack.size() == 1 : "there should be only one main file";
            assert includeStack.get(0).current.isMainFile() : "there should be only main file";
            includeStack.get(0).onEnterToBuiltIn(enteredTo);
        }
        interrupter.updateStateFromDelegate();
    }

    @Override
    protected void onExit(FileInfo exitedFrom, FileInfo exitedTo) {
        if (ClankDriverImpl.TRACE) {
            traceOS.$out("Exit from "); // NOI18N
            if (exitedFrom.isFile()) {
                traceOS.$out(exitedFrom.getName());
            } else {
                traceOS.$out_int(exitedFrom.getFileID());
            }
            traceOS.$out(" with #Token: ").$out_int(exitedFrom.getNrTokens()).$out("\n"); // NOI18N
            int[] offs = exitedFrom.getSkippedRanges();
            if (offs.length > 0) {
                for (int i = 0; i < offs.length; i += 2) {
                    int st = offs[i];
                    int end = offs[i + 1];
                    traceOS.$out("[").$out_int(st).$out("-").$out_int(end).$out("] "); // NOI18N
                }
                traceOS.$out("\n"); // NOI18N
            }
            traceOS.flush();
        }
        // sometimes we enter and leave some built-in buffers
        // usually it is predefines or cmd line options seen by main file
        // we'd prefer to stay in main file context
        if (exitedFrom.isFile()) {
            assert includeStack.size() > 0 : "empty include stack?";
            ClankDriver.ClankFileInfo exitedToWrapper;
            ClankFileInfoWrapper exitedFromWrapper = includeStack.remove(includeStack.size() - 1);
            assert exitedFromWrapper.current == exitedFrom;
            // we cache possibly collected tokens in include handler
            // to allow delegate to use them
            exitedFromWrapper.exited();
            includeHandler.cachePreprocessorOutputImplementation(exitedFromWrapper);
            // init where we returned to
            if (includeStack.isEmpty()) {
                exitedToWrapper = null;
            } else {
                exitedToWrapper = includeStack.get(includeStack.size() - 1);
            }

            // ask if delegate wish to continue
            if (!delegate.onExit(exitedFromWrapper, exitedToWrapper)) {
                interrupter.stop();
            }
            if (exitedToWrapper != null) {
                includeHandler.popInclude();
            }
        } else {
            assert includeStack.size() == 1 : "there should be only one main file";
            assert includeStack.get(0).current.isMainFile() : "there should be only main file";
            includeStack.get(0).onExitFromBuiltIn(exitedFrom);
        }
        interrupter.updateStateFromDelegate();
    }

    @Override
    protected boolean needPPDirectives() {
        return delegate.needPPDirectives();
    }

    @Override
    protected boolean needTokens() {
        return delegate.needTokens();
    }

    @Override
    protected boolean needSkippedRanges() {
        return delegate.needSkippedRanges();
    }

    @Override
    protected boolean needMacroExpansion() {
        return delegate.needMacroExpansion();
    }
    
    @Override
    protected boolean needComments() {
        return delegate.needComments();
    }

    @Override
    protected boolean isStopped() {
        return interrupter.isCancelled();
    }
    
    private static ClankFileInfoWrapper findRecursiveInclusion(ArrayList<ClankFileInfoWrapper> stack) {
        if (!stack.isEmpty()) {
            ClankFileInfoWrapper best = null;
            int bestFrequency = 0;
            Map<String, Pair<ClankFileInfoWrapper, Integer>> mapping = new HashMap<String, Pair<ClankFileInfoWrapper, Integer>>();
            for (ClankFileInfoWrapper fileInfo : stack) {
                String path = fileInfo.getFilePath().toString();
                Pair<ClankFileInfoWrapper, Integer> pair = mapping.get(path);
                if (pair == null) {
                    pair = Pair.of(fileInfo, 1);
                } else {
                    pair = Pair.of(pair.first(), pair.second() + 1);
                }
                mapping.put(path, pair);
                if (bestFrequency <= pair.second()) {
                    best = pair.first();
                    bestFrequency = pair.second();
                }
            }
            return best;
        }
        return null;
    }

    private static abstract class ClankPreprocessorDirectiveWrapper implements ClankDriver.ClankPreprocessorDirective {

        private Object externalAnnotation;
        private final int startOffset;
        private final int endOffset;

        public ClankPreprocessorDirectiveWrapper(PreprocessorDirectiveInfo delegate) {
            this(delegate.getHashOffset(), delegate.getEodOffset());
        }

        public ClankPreprocessorDirectiveWrapper(int start, int end) {
            this.startOffset = start;
            this.endOffset = end;
        }

        @Override
        public int getDirectiveStartOffset() {
            return this.startOffset;
        }

        @Override
        public int getDirectiveEndOffset() {
            return this.endOffset;
        }

        @Override
        public void setAnnotation(Object annotation) {
            assert externalAnnotation == null : "replacing? " + externalAnnotation;
            this.externalAnnotation = annotation;
        }

        @Override
        public Object getAnnotation() {
            return externalAnnotation;
        }

        @Override
        public String toString() {
            return "[" + startOffset + "-" + endOffset + "] " + "annotation=" + externalAnnotation; // NOI18N
        }
    }

    private static final class ClankMacroDirectiveWrapper extends ClankPreprocessorDirectiveWrapper implements ClankMacroDirective {

        private final List<CharSequence> params;
        private final CharSequence macroName;
        private final boolean isDefined;
        private final /*SourceLocation*/int macroNameTokenSourceLocation;
        private final int macroNameOffset;
        private final CharSequence fileOwnerName; // lazy field based on fileOwner
        private final boolean isBuiltIn;

        static ClankMacroDirectiveWrapper create(IdentifierInfo II, MacroDefinition MD, SourceManager SM) {
            MacroInfo macroInfo = MD.getMacroInfo();
            final int hashLoc = macroInfo.getHashLoc();
            long/*<FileID, uint>*/ decomposedLocBegin = SM.getDecomposedLoc(hashLoc);
            final int eodLoc = macroInfo.getEodLoc();
            long/*<FileID, uint>*/ decomposedLocEnd = SM.getDecomposedLoc(eodLoc);
            int begOffset = $second_offset(decomposedLocBegin);
            int endOffset = $second_offset(decomposedLocEnd);
            List<CharSequence> params = null;
            boolean isVariadic = false;
            if (macroInfo.isFunctionLike()) {
                IdentifierInfo[] args = null;
                args = macroInfo.$args();
                if (args != null) { // the following macro will return null arguments: #define XXX() __xxx()
                    isVariadic = macroInfo.isVariadic();
                    params = new ArrayList<CharSequence>(args.length);
                    for (IdentifierInfo arg : args) {
                        CharSequence argName = ClankToAPTUtils.getIdentifierText(arg);
                        params.add(argName);
                    }
                    if (isVariadic) {
                        assert params.size() > 0;
                        params.set(params.size() - 1, APTUtils.VA_ARGS_TOKEN.getTextID());
                    }
                }
            }
            CharSequence strName = ClankToAPTUtils.getIdentifierText(II);
            if (strName == null) {
                strName = "???"; //NOI18N
                CndUtils.assertTrueInConsole(false, "Null macro name " + MD);
            }
            char$ptr bufferName = SM.getBufferName(macroInfo.getDefinitionLoc());
            return new ClankMacroDirectiveWrapper(strName, params, bufferName, begOffset, endOffset);
        }

        public ClankMacroDirectiveWrapper(CharSequence name,
                List<CharSequence> params, char$ptr bufferName, int begOffset, int endOffset) {
            super(begOffset, endOffset);
            this.params = params;
            this.macroName = name;
            this.isDefined = true;
            this.macroNameTokenSourceLocation = -1;
            macroNameOffset = begOffset;
            if (std.strcmp(bufferName, "<built-in>") == 0) { // NOI18N
                // predefined system or user macros
                this.fileOwnerName = null;
                isBuiltIn = true;
            } else if (std.strcmp(bufferName, "<invalid loc>") == 0) { // NOI18N
                // context dependent macros (__FILE__, __LINE__)
                // TODO: separate from other built-in macros
                this.fileOwnerName = null;
                isBuiltIn = true;
            } else if (std.strcmp(bufferName, "Unknown buffer") == 0) { // NOI18N
                assert false : "Unknown location of macro definition "+macroName;
                this.fileOwnerName = null;
                isBuiltIn = true;
            } else {
                this.fileOwnerName = getFileOwnerNameImpl(bufferName);
                if (!Utilities.isWindows() && CharSequences.indexOf(fileOwnerName, "/") < 0) { // NOI18N
                    CndUtils.assertTrueInConsole(false, "Strange fileOwnerName: '" + fileOwnerName + "");
                }
                isBuiltIn = false;
            }
        }

        public ClankMacroDirectiveWrapper(CharSequence macroName,
                List<CharSequence> params, MacroDefinitionInfo clankDelegate, /*uint*/int macroNameStartOffset) {
            super(clankDelegate);
            this.params = params;
            this.macroName = macroName;
            this.isDefined = clankDelegate.isDefined();
            this.macroNameTokenSourceLocation = clankDelegate.getMacroNameLocation();
            macroNameOffset = macroNameStartOffset;
            FileInfo fileOwner = clankDelegate.getFileOwner();
            if (fileOwner.isFile()) {
                this.isBuiltIn = false;
                this.fileOwnerName = getFileOwnerNameImpl(fileOwner.getName());
            } else {
                this.isBuiltIn = true;
                this.fileOwnerName = null;
            }
        }

        @Override
        public CharSequence getFile() {
            return isBuiltIn ? BUILD_IN_FILE : fileOwnerName;
        }

        public boolean isBuiltIn() {
            return isBuiltIn;
        }

        @Override
        public boolean isDefined() {
            return this.isDefined;
        }

        @Override
        public CharSequence getMacroName() {
            return this.macroName;
        }

        @Override
        public int getMacroNameOffset() {
            return macroNameOffset;
        }

        @Override
        public List<CharSequence> getParameters() {
            return this.params == null ? null : Collections.unmodifiableList(this.params);
        }
        
        @Override
        public /*SourceLocation*/int getMacroNameLocation() {
            return macroNameTokenSourceLocation;
        }

        @Override
        public String toString() {
            return super.toString() + this.fileOwnerName + (this.isDefined ? " #define " : " #undef ") + this.macroName + // NOI18N
                    (this.params == null ? "" : ("(" + this.params + ")")); // NOI18N
        }

        private static CharSequence getFileOwnerNameImpl(char$ptr fileOwner) {
            CharSequence res = ClankFileSystemProviderImpl.getPathFromUrl(toJavaString(fileOwner));
            if (CharSequenceUtils.startsWith(res, ClankFileSystemProviderImpl.RFS_PREFIX)) {
                Exceptions.printStackTrace(new IllegalArgumentException("File owner name should not contain protocol: " + res)); //NOI18N
               }
            return res;
           }
        }

    private static final class ClankErrorDirectiveWrapper extends ClankPreprocessorDirectiveWrapper implements ClankDriver.ClankErrorDirective {

        private final CharSequence msg;
        private final PreprocHandler.State stateWhenMetErrorDirective;

        public ClankErrorDirectiveWrapper(UserDiagnosticDirectiveInfo clankDelegate,
                PreprocHandler.State stateWhenMetErrorDirective) {
            super(clankDelegate);
            assert stateWhenMetErrorDirective != null;
            this.stateWhenMetErrorDirective = stateWhenMetErrorDirective;
            StringRef message = clankDelegate.getMessage();
            String spelling = Casts.toCharSequence(message.data(), message.size()).toString();
            this.msg = spelling;
        }

        @Override
        public CharSequence getMessage() {
            return this.msg;
        }

        @Override
        public PreprocHandler.State getStateWhenMetErrorDirective() {
            return this.stateWhenMetErrorDirective;
        }

        @Override
        public String toString() {
            return "ClankErrorDirectiveWrapper{" + super.toString() + ",\n" // NOI18N
                    + " msg=" + msg + '}'; // NOI18N
        }
    }

    private static final class ClankInclusionDirectiveWrapper extends ClankPreprocessorDirectiveWrapper implements ClankDriver.ClankInclusionDirective {

        private final ResolvedPath resolvedPath;
        private final String spelling;
        private final boolean isAngled;
        private final int includeDirectiveIndex;
        private boolean recursive;

        public ClankInclusionDirectiveWrapper(InclusionDirectiveInfo clankDelegate, boolean system, int includeDirectiveIndex, ResolvedPath resolvedPath, String spelling) {
            super(clankDelegate);
            this.isAngled = system;
            this.includeDirectiveIndex = includeDirectiveIndex;
            this.resolvedPath = resolvedPath;
            this.spelling = spelling;
            this.recursive = false;
        }

        private ClankInclusionDirectiveWrapper(int start, int end, ClankInclusionDirectiveWrapper copyFrom) {
            super(start, end);
            this.isAngled = copyFrom.isAngled;
            this.includeDirectiveIndex = copyFrom.includeDirectiveIndex;
            this.resolvedPath = copyFrom.resolvedPath;
            this.spelling = copyFrom.spelling;
            this.recursive = copyFrom.recursive;
            super.setAnnotation(copyFrom.getAnnotation());
        }

        // like APTIncludeFake
        private static final String INCLUDE_FILE="-include"; // NOI18N
        private ClankInclusionDirectiveWrapper convertToIncludeFile(int start) {
            return new ClankInclusionDirectiveWrapper(start, start + INCLUDE_FILE.length(), this);
        }

        @Override
        public ResolvedPath getResolvedPath() {
            return resolvedPath;
        }

        @Override
        public String getSpellingName() {
            return spelling;
        }

        @Override
        public boolean isAngled() {
            return isAngled;
        }

        @Override
        public boolean isRecursive() {
            return recursive;
        }

        @Override
        public int getIncludeDirectiveIndex() {
            return includeDirectiveIndex;
        }
                
        public void setRecursive(boolean recursive) {
            this.recursive = recursive;
        }

        @Override
        public String toString() {
            return "ClankInclusionDirective{\n" + super.toString() + ",\n" // NOI18N
                    + "#" + includeDirectiveIndex + ";" // NOI18N
                    + "resolvedPath=" + resolvedPath + ",\n" // NOI18N
                    + "spelling=" + spelling + ",\n" // NOI18N
                    + '}'; // NOI18N
        }

    }

    private static final class ClankFileInfoWrapper implements ClankDriver.ClankFileInfo, ClankDriverImpl.ClankPreprocessorOutputImplementation {

        private final boolean needLineColumnsForToken;
        private final FileInfo current;
        private final ClankInclusionDirectiveWrapper includeDirective;
        private final CharSequence filePath;
        private final int includeIndex;
        private APTToken[] convertedTokens;
        private List<ClankDriver.ClankPreprocessorDirective> convertedPPDirectives;
        private List<MacroExpansion> convertedMacroExpansions;
        private List<MacroUsage> convertedMacroUsages;
        private FileGuard convertedGuard;
        private boolean hasTokenStream = false;
        private int[] skippedRanges = null;
        private boolean convertedToAPT = false;
        private int NextIncludeDirectiveIndex = -1;
        private Collection<FileInfo> visitedBuiltIns;
        
        public ClankFileInfoWrapper(FileInfo current,
                PreprocHandler ppHandler) {
            assert current != null;
            this.needLineColumnsForToken = APTToClankCompilationDB.isFortran(ppHandler);
            this.current = current;
            this.includeIndex = current.getIncludeIndex();
            if (current.getInclusionDirective() == null) {
                assert current.isMainFile() : "forgot to set up include?" + current;
                this.includeDirective = null;
                this.filePath = ClankFileSystemProviderImpl.getPathFromUrl(toJavaString(current.getName()));
            } else {
                this.includeDirective = (ClankInclusionDirectiveWrapper)current.getInclusionDirective().getAnnotation();
                assert this.includeDirective != null : "forgot to set up include?" + current;
                assert this.includeDirective.getResolvedPath() != null;
                this.filePath = this.includeDirective.getResolvedPath().getPath();
            }
        }

        @SuppressWarnings("ReturnOfCollectionOrArrayField")
        public APTToken[] getConvertedTokens() {
            assert convertedToAPT : "was not prepared yet";
            assert (convertedTokens != null);
            return convertedTokens;
        }

        private void prepareConvertedMacroExpansions() {
            Map<MacroDefinition, ClankMacroDirective> directives = new HashMap<MacroDefinition, ClankMacroDirective>();
            {
                SmallVector<FileInfoCallback.MacroExpansionInfo> macroExpansions = current.getMacroExpansions();
                int size = macroExpansions.size();
                Object[] expansions = macroExpansions.$array();
                convertedMacroExpansions = new ArrayList<MacroExpansion>(size);
                for (int i = 0; i < size; i++) {
                    FileInfoCallback.MacroExpansionInfo e = (FileInfoCallback.MacroExpansionInfo)expansions[i];
                    MacroDefinition MD = e.getReferencedMacroDefinition();
                    ClankMacroDirective referencedMacro = directives.get(MD);
                    if (referencedMacro == null) {
                        if (MD.$bool()) {
                            referencedMacro = ClankMacroDirectiveWrapper.create(e.getExpandedMacroName(), MD, current.getSourceManager());
                            directives.put(MD, referencedMacro);
                        } else {
                           CndUtils.assertTrueInConsole(false, "Should be defined MacroDefinition: " + MD);
                           continue;
                        }
                    }
                    MacroExpansion macroExpansion = new MacroExpansion(e, referencedMacro);
                    convertedMacroExpansions.add(macroExpansion);
                }
            }
            {
                SmallVector<FileInfoCallback.MacroUsageInfo> macroUsages = current.getMacroUsages();
                int size = macroUsages.size();
                Object[] expansions = macroUsages.$array();
                convertedMacroUsages = new ArrayList<MacroUsage>(size);
                SourceManager srcMgr = current.getSourceManager();                
                for (int i = 0; i < size; i++) {
                    FileInfoCallback.MacroUsageInfo e = (FileInfoCallback.MacroUsageInfo)expansions[i];
                    MacroDefinition MD = e.getReferencedMacroDefinition();
                    ClankMacroDirective referencedMacro = directives.get(MD);
                    if (referencedMacro == null) {
                        if (MD.$bool()) {
                            referencedMacro = ClankMacroDirectiveWrapper.create(e.getUsedMacroName(), MD, srcMgr);
                            directives.put(MD, referencedMacro);
                        } else {
                           CndUtils.assertTrueInConsole(false, "Should be defined MacroDefinition: " + MD);
                           continue;
                        }
                    }               
                    int macroUsageStartOffset = $second_offset(srcMgr.getDecomposedLoc(e.getUsedMacroNameLocation()));
                    int startOfset = macroUsageStartOffset;
                    int endOfset = startOfset + e.getUsedMacroNameLength();
                    MacroUsage macroUsage = new MacroUsage(startOfset, endOfset, referencedMacro);
                    convertedMacroUsages.add(macroUsage);
                }
            }
        }
        
        private void prepareConvertedGuard() {
            SmallVector<FileGuardInfo> guards = current.getFileGuardsInfo();
            assert guards != null;
            if (!guards.empty()) {
                // TODO: use the last for now
                FileGuardInfo fileGuardInfo = guards.$at(guards.size()-1);
                SourceManager srcMgr = current.getSourceManager();
                int start = $second_offset(srcMgr.getDecomposedLoc(fileGuardInfo.getIfDefMacroLocation()));
                convertedGuard = new FileGuard(start, start+fileGuardInfo.getIfDefMacro().getLength());
            }
        }

        private void prepareConvertedPPDirectives() {
            assert Thread.holdsLock(this);
            SmallVector<PreprocessorDirectiveInfo> ppDirectives = current.getPreprocessorDirectives();
            Object[] directives = ppDirectives.$array();
            int nrDirectives = ppDirectives.size();
            assert this.convertedPPDirectives == null;
            this.convertedPPDirectives = new ArrayList<ClankDriver.ClankPreprocessorDirective>(nrDirectives);
            Preprocessor PP = current.getPreprocessor();
            SourceManager SM = PP.getSourceManager();
            if (current.isMainFile()) {
                // visited built-ins might contain "-include /file/path" directives
                // attach them to the main file
                if (this.visitedBuiltIns != null) {
                    for (FileInfo builtIn : visitedBuiltIns) {
                        SmallVector<PreprocessorDirectiveInfo> builtInPPDirectives = builtIn.getPreprocessorDirectives();
                        Object[] builtInPPArray = builtInPPDirectives.$array();
                        int nrBuiltInPPDirectives = builtInPPDirectives.size();
                        Collection<ClankInclusionDirectiveWrapper> includeFiles = new ArrayList<ClankInclusionDirectiveWrapper>(1);
                        for (int i = 0; i < nrBuiltInPPDirectives; i++) {
                            PreprocessorDirectiveInfo curBuiltInDirective = (PreprocessorDirectiveInfo)builtInPPArray[i];
                            // filter out only "-include file" directives
                            if (curBuiltInDirective instanceof InclusionDirectiveInfo) {
                                ClankInclusionDirectiveWrapper includedFile = (ClankInclusionDirectiveWrapper) curBuiltInDirective.getAnnotation();
                                assert includedFile != null;
                                includeFiles.add(includedFile);
                            }
                        }
                        // like in APTAbstractWalker.preInit
                        for (ClankInclusionDirectiveWrapper directiveWrapper : includeFiles) {
                            // change offsets to be fake and associated with the main file, not the offset from built-in
                            // also we ordered them by their include directive indices
                            int fakeStartOffset = directiveWrapper.getIncludeDirectiveIndex() - includeFiles.size();
                            ClankInclusionDirectiveWrapper includedFile = directiveWrapper.convertToIncludeFile(fakeStartOffset);
                            fakeStartOffset++;
                            this.convertedPPDirectives.add(includedFile);
                        }
                    }
                }
            } else {
                assert this.visitedBuiltIns == null : "built-ins can be only in main file " + this.visitedBuiltIns;
            }
            
            for (int i = 0; i < nrDirectives; i++) {
                PreprocessorDirectiveInfo curDirective = (PreprocessorDirectiveInfo)directives[i];
                if (curDirective instanceof InclusionDirectiveInfo) {
                    ClankInclusionDirectiveWrapper wrapper = (ClankInclusionDirectiveWrapper)curDirective.getAnnotation();
                    assert wrapper != null;
                    this.convertedPPDirectives.add(wrapper);
                } else if (curDirective instanceof UserDiagnosticDirectiveInfo) {
                    if (!((UserDiagnosticDirectiveInfo)curDirective).isWarning()) {
                        // old model tracked only #error, not #warning
                        ClankErrorDirectiveWrapper wrapper = (ClankErrorDirectiveWrapper)curDirective.getAnnotation();
                        assert wrapper != null;
                        this.convertedPPDirectives.add(wrapper);
                    }
                } else if (curDirective instanceof MacroDefinitionInfo) {
                    MacroDefinitionInfo macroDirective = (MacroDefinitionInfo)curDirective;
                    if (macroDirective.isDefined()) {
                        // old model tracked only #define and not #undef
                        CharSequence macroName = ClankToAPTUtils.getIdentifierText(macroDirective.getMacroName());
                        List<CharSequence> params = null;
                        if (macroDirective.isFunctionLike()) {
                            IdentifierInfo[] arguments = macroDirective.getArguments();
                            params = new ArrayList<CharSequence>(arguments.length);
                            for (IdentifierInfo arg : arguments) {
                                CharSequence argName = ClankToAPTUtils.getIdentifierText(arg);
                                params.add(argName);
                            }
                            if (macroDirective.isVariadic()) {
                                // replace the last param name by variadic marker
                                assert params.size() > 0;
                                params.set(params.size() - 1, APTUtils.VA_ARGS_TOKEN.getTextID());
                            }
                        }
                        int macroNameStartOffset = $second_offset(SM.getDecomposedLoc(macroDirective.getMacroNameLocation()));
                        ClankMacroDirectiveWrapper wrapper = new ClankMacroDirectiveWrapper(macroName, params, macroDirective, macroNameStartOffset);
                        this.convertedPPDirectives.add(wrapper);
                    }
                }
            }
        }

        private void prepareConvertedTokensIfAny() {
            assert Thread.holdsLock(this);
            if (current.hasTokens()) {
                convertedTokens = ClankToAPTToken.convertToAPT(current.getPreprocessor(), current.getTokens(), needLineColumnsForToken);
            }
        }

        @Override
        public CharSequence getFilePath() {
            return filePath;
        }

        @Override
        public Collection<ClankDriver.ClankPreprocessorDirective> getPreprocessorDirectives() {
            prepareCachesIfPossible();
            return Collections.unmodifiableList(convertedPPDirectives);
        }

        @Override
        public Collection<MacroExpansion> getMacroExpansions() {
            prepareCachesIfPossible();
            return Collections.unmodifiableList(convertedMacroExpansions);
        }

        @Override
        public Collection<MacroUsage> getMacroUsages() {
            prepareCachesIfPossible();
            return Collections.unmodifiableList(convertedMacroUsages);
        }

        @Override
        public FileGuard getFileGuard() {
            prepareCachesIfPossible();
            return convertedGuard;
        }

        @Override
        public TokenStream getTokenStream() {
            return new ArrayBasedAPTTokenStream(getConvertedTokens());
        }

        @Override
        public int getFileIndex() {
            return includeIndex;
        }

        @Override
        public ClankDriver.ClankInclusionDirective getInclusionDirective() {
            return includeDirective;
        }

        @Override
        @SuppressWarnings("ReturnOfCollectionOrArrayField")
        public int[] getSkippedRanges() {
            return skippedRanges;
        }

        @Override
        public boolean hasTokenStream() {
            return hasTokenStream;
        }

        @Override
        public synchronized ClankDriverImpl.ClankPreprocessorOutputImplementation prepareCachesIfPossible() {
            prepareCaches();
            return this;
        }

        private void prepareCaches() {
            if (!convertedToAPT) {
                prepareConvertedTokensIfAny();
                prepareConvertedPPDirectives();
                prepareConvertedMacroExpansions();
                prepareConvertedGuard();
                convertedToAPT = true;
            }
        }

        @Override
        public String toString() {
            return "ClankFileInfoImpl{" + "convertedToAPT=" + convertedToAPT + ";"// NOI18N
                    + " hasTokenStream=" + hasTokenStream + ", current=" + current + ",\n"// NOI18N
                    + "currentInclude=" + includeDirective + '}';// NOI18N
        }

        private ResolvedPath getResolvedPath() {
            assert includeDirective != null;
            return includeDirective.getResolvedPath();
        }

        private void exited() {
            hasTokenStream = current.hasTokens();
            skippedRanges = current.getSkippedRanges();
        }

        private int getNextIncludeDirectiveIndex() {
            return ++NextIncludeDirectiveIndex;
        }

        private void onEnterToBuiltIn(FileInfo enteredTo) {
            assert current.isMainFile() : "current wrapper must be Compilation Unit: " + current;
        }

        private void onExitFromBuiltIn(FileInfo exitedFrom) {
            assert current.isMainFile() : "current wrapper must be Compilation Unit: " + current;
            // keep built-in's PP directives for possible further conversion
            if (visitedBuiltIns == null) {
                visitedBuiltIns = new ArrayList<FileInfo>(2);
            }
            visitedBuiltIns.add(exitedFrom);
        }
    }
}
