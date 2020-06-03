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
package org.netbeans.modules.cnd.modelimpl.parser.apt;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import org.netbeans.modules.cnd.antlr.TokenStream;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmInclude;
import org.netbeans.modules.cnd.apt.structure.APTFile;
import org.netbeans.modules.cnd.apt.support.APTDriver;
import org.netbeans.modules.cnd.apt.support.APTFileCacheEntry;
import org.netbeans.modules.cnd.apt.support.APTHandlersSupport;
import org.netbeans.modules.cnd.apt.support.APTIncludeHandler;
import org.netbeans.modules.cnd.apt.support.APTWalker;
import org.netbeans.modules.cnd.apt.support.api.PPIncludeHandler;
import org.netbeans.modules.cnd.apt.support.api.PreprocHandler;
import org.netbeans.modules.cnd.apt.support.lang.APTLanguageFilter;
import org.netbeans.modules.cnd.apt.support.lang.APTLanguageSupport;
import org.netbeans.modules.cnd.apt.utils.APTUtils;
import org.netbeans.modules.cnd.debug.DebugUtils;
import org.netbeans.modules.cnd.modelimpl.content.file.FileContent;
import org.netbeans.modules.cnd.modelimpl.content.project.FileContainer;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileImpl;
import org.netbeans.modules.cnd.modelimpl.csm.core.FilePreprocessorConditionState;
import org.netbeans.modules.cnd.modelimpl.csm.core.ParserQueue;
import org.netbeans.modules.cnd.modelimpl.csm.core.ProjectBase;
import org.netbeans.modules.cnd.modelimpl.csm.core.Utils;
import org.netbeans.modules.cnd.modelimpl.parser.spi.TokenStreamProducer;
import org.netbeans.modules.cnd.modelimpl.debug.DiagnosticExceptoins;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;
import org.netbeans.modules.cnd.support.Interrupter;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.openide.filesystems.FileSystem;
import org.openide.util.Pair;

/**
 *
 */
public final class APTTokenStreamProducer extends TokenStreamProducer {
    private final APTFile fullAPT;
    private PreprocHandler curPreprocHandler;
    private APTBasedPCStateBuilder pcBuilder;
    private Pair<PreprocHandler.State, APTFileCacheEntry> cachePair;
    
    private APTTokenStreamProducer(FileImpl file, FileContent newFileContent, APTFile fullAPT, boolean fromEnsureParsed) {
        super(file, newFileContent, fromEnsureParsed);
        this.fullAPT = fullAPT;
    }

    public static TokenStreamProducer createImpl(FileImpl file, FileContent newFileContent, boolean fromEnsureParsed) {
        APTFile fullAPT = getFileAPT(file, true);
        if (fullAPT == null) {
            return null;
        }
        return new APTTokenStreamProducer(file, newFileContent, fullAPT, fromEnsureParsed);
    }

    @Override
    public TokenStream getTokenStreamOfIncludedFile(PreprocHandler.State includeOwnerState, CsmInclude include, Interrupter interrupter) {
        FileImpl includedFile = (FileImpl) include.getIncludeFile();
        if (includedFile == null) {
            // error 
            return null;
        }
        ProjectBase projectImpl = includedFile.getProjectImpl(true);
        if (projectImpl == null) {
            // error 
            return null;
        }
        LinkedList<PPIncludeHandler.IncludeInfo> reverseInclStack = APTHandlersSupport.extractIncludeStack(includeOwnerState);
        PPIncludeHandler.IncludeInfo inclInfo = createIncludeInfo(include);
        if (inclInfo == null) {
            // error 
            return null;
        } else {
            reverseInclStack.addLast(inclInfo);
        }
        FileImpl ownerFile = getInterestedFile();
        CharSequence ownerAbsPath = ownerFile.getAbsolutePath();
        PreprocHandler preprocHandler = projectImpl.createEmptyPreprocHandler(ownerAbsPath);
        PreprocHandler restorePreprocHandlerFromIncludeStack = restorePreprocHandlerFromIncludeStack(projectImpl, reverseInclStack, ownerAbsPath, preprocHandler, includeOwnerState, Interrupter.DUMMY);
        // using restored preprocessor handler, ask included file for parsing token stream filtered by language
        resetHandler(restorePreprocHandlerFromIncludeStack);
        TokenStream includedFileTS = createParsingTokenStreamForHandler(includedFile, restorePreprocHandlerFromIncludeStack, true);
        return includedFileTS;
    }

    private static final boolean REMEMBER_RESTORED = TraceFlags.CLEAN_MACROS_AFTER_PARSE && 
            (DebugUtils.getBoolean("cnd.remember.restored", false) || ProjectBase.TRACE_PP_STATE_OUT);// NOI18N
    private static volatile List<String> testRestoredFiles = null;
    public static List<String> testGetRestoredFiles() {
        return testRestoredFiles;
    }
    
    public static PreprocHandler restorePreprocHandlerFromIncludeStack(ProjectBase project, LinkedList<APTIncludeHandler.IncludeInfo> reverseInclStack,
            CharSequence interestedFile, PreprocHandler preprocHandler, PreprocHandler.State state, final Interrupter interrupter) {
        // we need to reverse includes stack
        assert (!reverseInclStack.isEmpty()) : "state of stack is " + reverseInclStack;
        LinkedList<APTIncludeHandler.IncludeInfo> inclStack = Utils.reverse(reverseInclStack);
        ProjectBase.StartEntryInfo sei = project.getStartEntryInfo(preprocHandler, state);
        FileImpl csmFile = sei.csmFile;
        ProjectBase startProject = sei.startProject;
        preprocHandler = sei.preprocHandler;

        APTFile aptLight = null;
        try {
            aptLight = csmFile == null ? null : getAPTLight(csmFile);
        } catch (IOException ex) {
            System.err.println("can't restore preprocessor state for " + interestedFile + //NOI18N
                    "\nreason: " + ex.getMessage());//NOI18N
            DiagnosticExceptoins.register(ex);
        }
        boolean ppStateRestored = false;
        if (aptLight != null) {
            // for testing remember restored file
            long time = REMEMBER_RESTORED ? System.currentTimeMillis() : 0;
            int stackSize = inclStack.size();
            // create concurrent entry if absent
            APTFileCacheEntry cacheEntry = csmFile.getAPTCacheEntry(state, Boolean.FALSE);
            APTWalker walker = new APTRestorePreprocStateWalker(startProject, aptLight, csmFile, preprocHandler, inclStack, FileContainer.getFileKey(interestedFile, false).toString(), cacheEntry) {

                @Override
                protected boolean isStopped() {
                    return super.isStopped() || interrupter.cancelled();
                }

            };
            walker.visit();
            // we do not remember cache entry because it is stopped before end of file
            // fileImpl.setAPTCacheEntry(handler, cacheEntry, false);

            if (preprocHandler.isValid()) {
                if (REMEMBER_RESTORED) {
                    if (testRestoredFiles == null) {
                        testRestoredFiles = new ArrayList<>();
                    }
                    FileImpl interestedFileImpl = project.getFile(interestedFile, false);
                    assert interestedFileImpl != null;
                    String msg = interestedFile + " [" + (interestedFileImpl.isHeaderFile() ? "H" : interestedFileImpl.isSourceFile() ? "S" : "U") + "]"; // NOI18N
                    time = System.currentTimeMillis() - time;
                    msg = msg + " within " + time + "ms" + " stack " + stackSize + " elems"; // NOI18N
                    System.err.println("#" + testRestoredFiles.size() + " restored: " + msg); // NOI18N
                    testRestoredFiles.add(msg);
                }
                if (ProjectBase.TRACE_PP_STATE_OUT) {
                    System.err.println("after restoring " + preprocHandler); // NOI18N
                }
                ppStateRestored = true;
            }
        }
        if (!ppStateRestored) {
            // need to recover from the problem, when start file is invalid or absent
            // try to find project who can create default handler with correct
            // compiler settings
            // preferences is start project
            if (startProject == null) {
                // otherwise use the project owner
                startProject = project;
            }
            preprocHandler = startProject.createDefaultPreprocHandler(interestedFile);
            // remember
            // TODO: file container should accept all without checks
            // otherwise state will not be replaced
//                synchronized (getFileContainer().getLock(interestedFile)) {
//                    if (state.equals(getPreprocState(interestedFile))) {
//                        PreprocHandler.State recoveredState = preprocHandler.getState();
//                        assert !recoveredState.isCompileContext();
//                        putPreprocState(interestedFile, recoveredState);
//                    }
//                }
        }
        return preprocHandler;
    }

    private TokenStream createParsingTokenStreamForHandler(FileImpl fileImpl, PreprocHandler preprocHandler, boolean filterOutComments) {
        APTFile apt = APTTokenStreamProducer.getFileAPT(fileImpl, true);
        if (apt == null) {
            return null;
        }
        if (preprocHandler == null) {
            return null;
        }
        PreprocHandler.State ppState = preprocHandler.getState();
        ProjectBase startProject = Utils.getStartProject(ppState);
        if (startProject == null) {
            System.err.println(" null project for " + APTHandlersSupport.extractStartEntry(ppState) + // NOI18N
                    "\n while getting TS of file " + fileImpl.getAbsolutePath() + "\n of project " + fileImpl.getProject()); // NOI18N
            return null;
        }
        pcBuilder = new APTBasedPCStateBuilder(fileImpl.getAbsolutePath());
        // ask for concurrent entry if absent
        APTFileCacheEntry cacheEntry = fileImpl.getAPTCacheEntry(ppState, Boolean.FALSE);
        APTParseFileWalker walker = new APTParseFileWalker(startProject, apt, fileImpl, preprocHandler, false, pcBuilder, cacheEntry);
        return walker.getTokenStream(filterOutComments);
    }

    @Override
    public TokenStream getTokenStreamForParsingAndCaching(Interrupter interrupter) {
        //TokenStreamProducer.Parameters.createForParsingAndTokenStreamCaching()
        return getTokenStream(false, true, false, interrupter);
    }

    @Override
    public TokenStream getTokenStreamForParsing(String language, Interrupter interrupter) {
        //TokenStreamProducer.Parameters.createForParsing(language)
        return getTokenStream(true, APTLanguageSupport.FORTRAN.equals(language), true, interrupter);
    }

    @Override
    public TokenStream getTokenStreamForCaching(Interrupter interrupter) {
        //TokenStreamProducer.Parameters.createForTokenStreamCaching()
        return getTokenStream(false, true, false, interrupter);
    }

    private TokenStream getTokenStream(boolean triggerParsingActivity, boolean needComments, boolean applyLanguageFilter, Interrupter interrupter) {
        FileImpl fileImpl = getInterestedFile();
        PreprocHandler preprocHandler = getCurrentPreprocHandler();
        // use full APT for generating token stream
        if (TraceFlags.TRACE_CACHE) {
            System.err.println("CACHE: parsing using full APT for " + fileImpl.getAbsolutePath());
        }        
        // make real parse
        PreprocHandler.State ppState = preprocHandler.getState();
        ProjectBase startProject = Utils.getStartProject(ppState);
        if (startProject == null) {
            System.err.println(" null project for " + APTHandlersSupport.extractStartEntry(ppState) + // NOI18N
                    "\n while parsing file " + fileImpl.getAbsolutePath() + "\n of project " + fileImpl.getProject()); // NOI18N
            return null;
        }        
        // We gather conditional state here as well, because sources are not included anywhere
        pcBuilder = new APTBasedPCStateBuilder(fileImpl.getAbsolutePath());
        // ask for concurrent entry if absent
        APTFileCacheEntry aptCacheEntry = fileImpl.getAPTCacheEntry(ppState, Boolean.FALSE);
        APTParseFileWalker walker = new APTParseFileWalker(startProject, fullAPT, fileImpl, preprocHandler, triggerParsingActivity, pcBuilder, aptCacheEntry);
        walker.setFileContent(getFileContent()); // NO
        if (TraceFlags.DEBUG) {
            System.err.println("doParse " + fileImpl.getAbsolutePath() + " with " + ParserQueue.tracePreprocState(ppState));
        }
        TokenStream tsOut;
        if (applyLanguageFilter) {
            APTLanguageFilter languageFilter = fileImpl.getLanguageFilter(ppState);
            tsOut = walker.getFilteredTokenStream(languageFilter);
        } else {
            boolean filterOutComments = !needComments;
            tsOut = walker.getTokenStream(filterOutComments);
        }
        if (isAllowedToCacheOnRelease()) {
          cachePair = Pair.of(ppState, aptCacheEntry);
        }
        return tsOut;
    }

    @Override
    public FilePreprocessorConditionState release() {
        if (isAllowedToCacheOnRelease()) {
          assert cachePair != null;
          // remember walk info
          FileImpl fileImpl = getInterestedFile();
          fileImpl.setAPTCacheEntry(cachePair.first(), cachePair.second(), false);
        }
        return pcBuilder.build();
    }

    final static APTFile getAPTLight(CsmFile csmFile) throws IOException {
        FileImpl fileImpl = (FileImpl) csmFile;
        APTFile aptLight = APTTokenStreamProducer.getFileAPT(fileImpl, false);
        if (aptLight != null && APTUtils.LOG.isLoggable(Level.FINE)) {
            CharSequence guardMacro = aptLight.getGuardMacro();
            if (guardMacro.length() == 0 && !fileImpl.isSourceFile()) {
                APTUtils.LOG.log(Level.FINE, "FileImpl: file {0} does not have guard", new Object[]{fileImpl.getAbsolutePath()});// NOI18N
            }
        }

        return aptLight;
    }

    public static APTFile getFileAPT(FileImpl file, boolean full) {
        APTFile fileAPT = null;
        //FileBufferDoc.ChangedSegment changedSegment = null;
        try {
            if (full) {
                fileAPT = APTDriver.findAPT(file.getBuffer(), file.getAPTFileKind());
            } else {
                fileAPT = APTDriver.findAPTLight(file.getBuffer(), file.getAPTFileKind());
            }
            //if (file.getBuffer() instanceof FileBufferDoc) {
            //    changedSegment = ((FileBufferDoc) file.getBuffer()).getLastChangedSegment();
            //}
        } catch (FileNotFoundException ex) {
            APTUtils.LOG.log(Level.WARNING, "FileImpl: file {0} not found, probably removed", new Object[]{file.getBuffer().getAbsolutePath()});// NOI18N
        } catch (IOException ex) {
            DiagnosticExceptoins.register(ex);
        }
        if (fileAPT != null && APTUtils.LOG.isLoggable(Level.FINE)) {
            CharSequence guardMacro = fileAPT.getGuardMacro();
            if (guardMacro.length() == 0 && !file.isSourceFile()) {
                APTUtils.LOG.log(Level.FINE, "FileImpl: file {0} does not have guard", new Object[]{file.getBuffer().getAbsolutePath()});// NOI18N
            }
        }
        return fileAPT;
    }

}
