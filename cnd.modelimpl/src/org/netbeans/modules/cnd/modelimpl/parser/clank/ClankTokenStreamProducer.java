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
package org.netbeans.modules.cnd.modelimpl.parser.clank;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.cnd.antlr.TokenStream;
import org.netbeans.modules.cnd.api.model.CsmInclude;
import org.netbeans.modules.cnd.api.model.CsmModelAccessor;
import org.netbeans.modules.cnd.api.model.xref.CsmReference;
import org.netbeans.modules.cnd.apt.support.APTHandlersSupport;
import org.netbeans.modules.cnd.apt.support.ClankDriver;
import org.netbeans.modules.cnd.apt.support.ClankDriver.ClankPreprocessorCallback;
import org.netbeans.modules.cnd.apt.support.ResolvedPath;
import org.netbeans.modules.cnd.apt.support.api.PPIncludeHandler;
import org.netbeans.modules.cnd.apt.support.api.PreprocHandler;
import org.netbeans.modules.cnd.apt.support.api.StartEntry;
import org.netbeans.modules.cnd.apt.support.lang.APTLanguageFilter;
import org.netbeans.modules.cnd.apt.utils.APTCommentsFilter;
import org.netbeans.modules.cnd.apt.utils.APTUtils;
import org.netbeans.modules.cnd.modelimpl.content.file.FileContent;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileBuffer;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileBufferFile;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileImpl;
import org.netbeans.modules.cnd.modelimpl.csm.core.FilePreprocessorConditionState;
import org.netbeans.modules.cnd.modelimpl.csm.core.Line2Offset;
import org.netbeans.modules.cnd.modelimpl.csm.core.PreprocessorStatePair;
import org.netbeans.modules.cnd.modelimpl.csm.core.ProjectBase;
import org.netbeans.modules.cnd.modelimpl.debug.DiagnosticExceptoins;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;
import org.netbeans.modules.cnd.modelimpl.parser.clank.ClankToCsmSupport.UnresolvedIncludeDirectiveAnnotation;
import org.netbeans.modules.cnd.modelimpl.parser.clank.ClankToCsmSupport.UnresolvedIncludeDirectiveReason;
import org.netbeans.modules.cnd.modelimpl.parser.clank.ClankTokenStreamProducerParameters.YesNoInterested;
import org.netbeans.modules.cnd.modelimpl.parser.spi.TokenStreamProducer;
import org.netbeans.modules.cnd.support.Interrupter;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.cnd.utils.cache.CharSequenceUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;

/**
 *
 */
public final class ClankTokenStreamProducer extends TokenStreamProducer {

    private int[] skipped;

    private ClankTokenStreamProducer(FileImpl file, FileContent newFileContent, boolean fromEnsureParsed) {
        super(file, newFileContent, fromEnsureParsed);
    }
    
    public static TokenStreamProducer createImpl(FileImpl file, FileContent newFileContent, boolean fromEnsureParsed) {
        return new ClankTokenStreamProducer(file, newFileContent, fromEnsureParsed);
    }

    public static List<CsmReference> getMacroUsages(FileImpl file, PreprocHandler handler, Interrupter interrupter) {
        FileContent newFileContent = FileContent.getHardReferenceBasedCopy(file.getCurrentFileContent(), true);
        // TODO: we do NOT need file content at all here
        ClankTokenStreamProducer tsp = new ClankTokenStreamProducer(file, newFileContent, false);
        PreprocHandler.State ppState = handler.getState();
        String contextLanguage = file.getContextLanguage(ppState);
        String contextLanguageFlavor = file.getContextLanguageFlavor(ppState);
        tsp.prepare(handler, contextLanguage, contextLanguageFlavor, false);
        ClankTokenStreamProducerParameters params = ClankTokenStreamProducerParameters.createForMacroUsages();
        List<CsmReference> res = tsp.getMacroUsages(params, interrupter);
        tsp.release();
        return res;
    }

    @Override
    public TokenStream getTokenStreamOfIncludedFile(PreprocHandler.State includeOwnerState, CsmInclude include, Interrupter interrupter) {
        FileImpl includeDirecitveFileOwner = getInterestedFile();
        FileImpl includedFile = (FileImpl) include.getIncludeFile();
        if (includedFile == null) {
            // error recovery
            return null;
        }
        ProjectBase projectImpl = includedFile.getProjectImpl(true);
        if (projectImpl == null) {
            // error recovery
            return null;
        }
        PPIncludeHandler.IncludeInfo inclInfo = createIncludeInfo(include);
        if (inclInfo == null) {
            // error recovery
            return null;
        }
        
        // prepare handler (which can be reset to default if smth goes wrong)
        PreprocHandler ppHandler = projectImpl.createPreprocHandlerFromState(includeDirecitveFileOwner.getAbsolutePath(), includeOwnerState);
        // retake state in case smth goes wrong above
        includeOwnerState = ppHandler.getState();
        LinkedList<PPIncludeHandler.IncludeInfo> includeChain = APTHandlersSupport.extractIncludeStack(includeOwnerState);
        if (CndUtils.isDebugMode()) {
            StartEntry startEntry = APTHandlersSupport.extractStartEntry(includeOwnerState);
            if (includeChain.isEmpty()) {
                assert startEntry.getFileSystem() == includeDirecitveFileOwner.getFileSystem();
                CndUtils.assertPathsEqualInConsole(startEntry.getStartFile(), includeDirecitveFileOwner.getAbsolutePath(), "different paths {0} vs. {1}", startEntry, includeDirecitveFileOwner);
            } else {
                PPIncludeHandler.IncludeInfo includer = includeChain.getLast();
                CndUtils.assertPathsEqualInConsole(includer.getIncludedPath(), includeDirecitveFileOwner.getAbsolutePath(), "different paths {0} vs. {1}", includer, includeDirecitveFileOwner);
                assert includer.getFileSystem() == includeDirecitveFileOwner.getFileSystem();
            }
        }
        // we've got include chain up to directive owner
        // add our include directive as the last entry point
        includeChain.addLast(inclInfo);
        
        // do preprocessing of include chain
        ClankTokenStreamProducerParameters params = ClankTokenStreamProducerParameters.createForIncludedTokenStream(getLanguage());
        VisitIncludeChainPreprocessorCallback callback = new VisitIncludeChainPreprocessorCallback(includeChain, params);
        boolean success = ClankDriver.preprocess(includeDirecitveFileOwner.getBuffer(), ppHandler, callback, interrupter);
        if (!success) {
            // error recovery
            return null;
        }
        ClankDriver.ClankPreprocessorOutput ppOutput = callback.getPreparedPreprocessorOutput();
        if (ppOutput == null) {
            // error recovery
            return null;
        }
        TokenStream tokenStream = ppOutput.getTokenStream();
        if (tokenStream == null) {
            // error recovery
            return null;
        }
        // unused, but init to prevent NPE
        skipped = new int[0];
        return tokenStream;
    }

    @Override
    public TokenStream getTokenStreamForParsingAndCaching(Interrupter interrupter) {
        final ClankTokenStreamProducerParameters params = ClankTokenStreamProducerParameters.createForParsingAndTokenStreamCaching();
        assertParamsReadyForCache(params);
        return preprocessAndGetFileTokenStream(getInterestedFile(), params, interrupter);
    }

    @Override
    public TokenStream getTokenStreamForParsing(String language, Interrupter interrupter) {
        FileImpl interestedFile = getInterestedFile();
        assert interestedFile != null;
        PreprocHandler ppHandler = getCurrentPreprocHandler();
        ClankDriver.ClankPreprocessorOutput ppOutput = ClankDriver.extractPreprocessorOutput(ppHandler);
        assert ppOutput != null;
        TokenStream out;
        ClankTokenStreamProducerParameters params = ClankTokenStreamProducerParameters.createForParsing(language);
        // in this mode handler might already have cached TokenStream
        if (ppOutput.hasTokenStream()) {
            out = postProcessAndExtractTokenStream(interestedFile, ppOutput, params);
        } else {
            out = preprocessAndGetFileTokenStream(interestedFile, params, interrupter);
        }
        return out;
    }

    @Override
    public TokenStream getTokenStreamForCaching(Interrupter interrupter) {
        ClankTokenStreamProducerParameters params = ClankTokenStreamProducerParameters.createForTokenStreamCaching();
        assertParamsReadyForCache(params);
        return preprocessAndGetFileTokenStream(getInterestedFile(), params, interrupter);
    }

    @Override
    public FilePreprocessorConditionState release() {
        return FilePreprocessorConditionState.build(getInterestedFile().getAbsolutePath(), skipped);
    }
    
    private static void assertParamsReadyForCache(ClankTokenStreamProducerParameters params) {
        boolean ready = (params.needTokens != YesNoInterested.NEVER)
                && (params.needComments != YesNoInterested.NEVER)
                && (params.needMacroExpansion != YesNoInterested.NEVER)
                && (params.applyLanguageFilter == false);
        if (!ready) {
            CndUtils.assertTrue(false, "Should be ready for cahcing: " + params);
        }
    }

    private List<CsmReference> getMacroUsages(ClankTokenStreamProducerParameters parameters, Interrupter interrupter) {
        FileImpl interestedFile = getInterestedFile();
        ClankDriver.ClankPreprocessorOutput foundFileInfo = getPreprocessorOutputForInterestedFile(interestedFile, parameters, interrupter);
        List<CsmReference> out = ClankToCsmSupport.getMacroUsages(interestedFile, getStartFile(), foundFileInfo);
        return out;
    }

    private TokenStream preprocessAndGetFileTokenStream(FileImpl fileImpl, ClankTokenStreamProducerParameters parameters, Interrupter interrupter) {
        ClankDriver.ClankPreprocessorOutput ppOutput = getPreprocessorOutputForInterestedFile(fileImpl, parameters, interrupter);
        if (ppOutput == null) {
            return null;
        }
        TokenStream tokenStream = postProcessAndExtractTokenStream(fileImpl, ppOutput, parameters);
        return tokenStream;
    }

    private TokenStream postProcessAndExtractTokenStream(FileImpl fileImpl, ClankDriver.ClankPreprocessorOutput ppOutput, ClankTokenStreamProducerParameters parameters) {
        assert ppOutput != null;
        assert ppOutput.hasTokenStream() : "Only valid one can be asked to extract TS " + ppOutput;
        cacheMacroUsagesInFileIfNeed(fileImpl, parameters, ppOutput);
        
        if (super.isFromEnsureParsed()) {
            ClankToCsmSupport.addPreprocessorDirectives(fileImpl, getFileContent(), ppOutput);
            ClankToCsmSupport.addMacroExpansions(fileImpl, getFileContent(), getStartFile(), ppOutput);
            ClankToCsmSupport.setFileGuard(fileImpl, getFileContent(), ppOutput);
        }
        skipped = ppOutput.getSkippedRanges();
        TokenStream tokenStream = ppOutput.getTokenStream();
        if (tokenStream == null) {
            return null;
        }
        if (parameters.applyLanguageFilter) {
            PreprocHandler ppHandler = getCurrentPreprocHandler();
            APTLanguageFilter languageFilter = fileImpl.getLanguageFilter(ppHandler.getState());
            tokenStream = languageFilter.getFilteredStream(new APTCommentsFilter(tokenStream));
        }
        return tokenStream;
    }
    
    private ClankDriver.ClankPreprocessorOutput getPreprocessorOutputForInterestedFile(FileImpl interestedFile,
            ClankTokenStreamProducerParameters parameters, Interrupter interrupter) {
        // buffer might be patched externally
        FileBuffer buffer = interestedFile.getBuffer();
        if (getCodePatch() != null) {
            buffer = new PatchedFileBuffer(buffer, getCodePatch());
        }
        // start with prepared handler
        PreprocHandler ppHandler = getCurrentPreprocHandler();
        PreprocHandler.State state = ppHandler.getState();
        FileImpl startFile = getStartFile();
        ClankDriver.ClankPreprocessorOutput out = preprocessWithHandler(startFile, interestedFile, ppHandler, parameters, buffer, interrupter);
        if (out == null && super.isFromEnsureParsed()) {
            // TODO: would be great to invalidate state in file containers...
            
            // Fallback
            // we need to recover and try again with empty PPHandler
            ProjectBase projectImpl = interestedFile.getProjectImpl(true);
            if (projectImpl != null && projectImpl.isValid()) {
                ppHandler = projectImpl.createDefaultPreprocHandler(interestedFile.getAbsolutePath());
                super.resetHandler(ppHandler);
                out = preprocessWithHandler(interestedFile, interestedFile, ppHandler, parameters, buffer, interrupter);
            }
        }
        return out;
    }

    protected ClankDriver.ClankPreprocessorOutput preprocessWithHandler(FileImpl startFile, FileImpl interestedFile, 
            PreprocHandler ppHandler, ClankTokenStreamProducerParameters parameters, 
            FileBuffer buffer, Interrupter interrupter) {
        FileImplPreprocessorCallback callback = new FileImplPreprocessorCallback(
                startFile,
                interestedFile,
                ppHandler,
                parameters);
        if (ClankDriver.preprocess(buffer, ppHandler, callback, interrupter)) {
            return callback.getPreparedPreprocessorOutput();
        }
        return null;
    }

    private void cacheMacroUsagesInFileIfNeed(FileImpl fileImpl, ClankTokenStreamProducerParameters parameters, ClankDriver.ClankPreprocessorOutput foundFileInfo) {
        if (foundFileInfo == null) {
            return; // can this happen? should we assert? (softly!)
        }
        // TODO: shouldn't we introduce a special flag for this?
        if (parameters.needMacroExpansion == YesNoInterested.INTERESTED && parameters.needPPDirectives == YesNoInterested.INTERESTED) {
            List<CsmReference> macroUsages = ClankToCsmSupport.getMacroUsages(fileImpl, getStartFile(), foundFileInfo);
            // FIXME: we should put found macro usages into FileContent, because we could be called in the loop and
            // in this case each iteration would overwrite result of previous
            fileImpl.setLastMacroUsages(macroUsages);
        }
    }
    
    private static class VisitIncludeChainPreprocessorCallback implements ClankPreprocessorCallback {
        // parameters to be collected by preprocessor inside interested file
        protected final ClankTokenStreamProducerParameters parameters;
        
        // include chain we need to go till interested file
        private final LinkedList<PPIncludeHandler.IncludeInfo> remainingChainToInterestedFile;
        private PPIncludeHandler.IncludeInfo seekEnterToThisIncludeInfo;

        private ClankDriver.ClankFileInfo waitExitFromThisFileInfo = null;
        private ClankDriver.ClankFileInfo seenInterestedFileInfo = null;
        
        private ClankDriver.ClankPreprocessorOutput preparedPreprocessorOutput = null;
        private boolean insideInterestedFile = false;

        protected enum State {
          WAIT_COMPILATION_UNIT_FILE, // before Compilation Unit
          WAIT_EXIT_FROM_FILE, // uses waitExitFromThisFileInfo
          SEEK_ENTER_TO_INCLUDED_FILE, // uses seekEnterToThisIncludeInfo
          INSIDE_INITERESTED_FILE, // seenInterestedFileInfo
          CORRUPTED_INCLUDE_CHAIN, // smth goes wrong
          DONE // chain is OK and TokenStream is collected
        }
        
        private State state;
        private VisitIncludeChainPreprocessorCallback(LinkedList<PPIncludeHandler.IncludeInfo> includeChain,
                ClankTokenStreamProducerParameters params) {
            this.remainingChainToInterestedFile = includeChain;
            this.state = State.WAIT_COMPILATION_UNIT_FILE;
            this.parameters = params;
        }

        private boolean valueOf(/*YesNoInterested*/int param) {
            switch (param) {
                case YesNoInterested.ALWAYS:
                    return true;
                case YesNoInterested.NEVER:
                    return false;
                case YesNoInterested.INTERESTED:
                    return insideInterestedFile;
                default:
                    throw new AssertionError("unknown" + param);
            }
        }
        
        public boolean isCorruptedIncludeChain() {
            return state == State.CORRUPTED_INCLUDE_CHAIN;
        }
        
        @Override
        public boolean needPPDirectives() {
            return valueOf(parameters.needPPDirectives);
        }

        @Override
        public boolean needTokens() {
            return valueOf(parameters.needTokens);
        }

        @Override
        public boolean needSkippedRanges() {
          return valueOf(parameters.needSkippedRanges);
        }

        @Override
        public boolean needMacroExpansion() {
            return valueOf(parameters.needMacroExpansion);
        }

        @Override
        public boolean needComments() {
            return valueOf(parameters.needComments);
        }

        protected final boolean isInsideInterestedFile() {
            return this.insideInterestedFile;
        }
        
        /**
         * returns <tt>true</tt> if processed enter into file.
         * @param enteredFrom
         * @param enteredTo
         * @return  <tt>true</tt> if processed enter into file. 
         *          <tt>false</tt> in case of errors
         */
        protected boolean pushEnteredFile(ClankDriver.ClankFileInfo enteredFrom, ClankDriver.ClankFileInfo enteredTo) {
            return true;
        }
        
        /**
         * returns <tt>true</tt> if processed exit from file.
         * @param exitedFrom
         * @param exitedTo
         * @param state
         * @param exitingFromInterestedFile
         * @return  <tt>true</tt> if processed enter into file.
         * <tt>false</tt> in case of errors
         */
        protected boolean popExitedFile(ClankDriver.ClankFileInfo exitedFrom, ClankDriver.ClankFileInfo exitedTo, State state, boolean exitingFromInterestedFile) {
            return true;
        }

        protected void include(ClankDriver.ClankInclusionDirective directive) {
            
        }
        
        @Override
        public final void onInclusionDirective(ClankDriver.ClankFileInfo directiveOwner, ClankDriver.ClankInclusionDirective directive) {
            switch (state) {
                case WAIT_EXIT_FROM_FILE:
                case SEEK_ENTER_TO_INCLUDED_FILE:
                case INSIDE_INITERESTED_FILE:
                    include(directive);
                    break;
                case WAIT_COMPILATION_UNIT_FILE:
                    assert false;
                    break;
                case CORRUPTED_INCLUDE_CHAIN:
                case DONE:
                    // no need to handle extra include when work is done or chain is broken
                    return;
                default:
                    throw new AssertionError(state.name());
                
            }
        }
        
        @Override
        public final boolean onEnter(ClankDriver.ClankFileInfo enteredFrom, ClankDriver.ClankFileInfo enteredTo) {            
            assert enteredTo != null;
            insideInterestedFile = false;
            assert state != null : "null state when enter from\n" + enteredFrom + "\nTo\n" + enteredTo;
            boolean continuePreprocessing;
            switch (state) {
                case CORRUPTED_INCLUDE_CHAIN:
                    // keep state and dont' allow to enter
                    continuePreprocessing = false;
                    break;
                case DONE:
                    // all activity was done; keep state and no need to enter
                    continuePreprocessing = false;
                    break;
                case WAIT_COMPILATION_UNIT_FILE:
                    // enter compilation unit
                    assert enteredFrom == null : "expected null instead of " + enteredFrom;
                    assert this.waitExitFromThisFileInfo == null : "expected null instead of " + this.waitExitFromThisFileInfo;
                    if (remainingChainToInterestedFile.isEmpty()) {
                        // main file itself is what we are looking for
                        state = State.INSIDE_INITERESTED_FILE;
                        seenInterestedFileInfo = enteredTo;
                        insideInterestedFile = true;
                    } else {
                        // inside compilation unit file we are going to seek entrance into the head of include chain
                        // remove it from chain
                        seekEnterToThisIncludeInfo = this.remainingChainToInterestedFile.removeFirst();
                        assert seekEnterToThisIncludeInfo != null;
                        // need to find entrance into include chain
                        state = State.SEEK_ENTER_TO_INCLUDED_FILE;
                    }
                    // allow to enter
                    continuePreprocessing = true;
                    break;
                case INSIDE_INITERESTED_FILE:
                    assert this.remainingChainToInterestedFile.isEmpty() : "we are inside interested file only when walked whole chain: " + remainingChainToInterestedFile;
                    // inside interested file we met #include directive
                    // visit full include branch and come back to our file
                    assert this.waitExitFromThisFileInfo == null : "expected null instead of " + this.waitExitFromThisFileInfo;
                    // set up exit-from marker object
                    this.waitExitFromThisFileInfo = enteredTo;
                    state = State.WAIT_EXIT_FROM_FILE;
                    // enter included file, state will be changed in onExit
                    continuePreprocessing = true;
                    break;
                case WAIT_EXIT_FROM_FILE:
                    // we are inside #include branch which is before or inside interested #include
                    // this state is changed only in onExit
                    assert this.waitExitFromThisFileInfo != null;
                    assert this.waitExitFromThisFileInfo != enteredTo : "unexpected to enter into file " + enteredTo + " which we wait to exit from";
                    // continue traversing this include path
                    // state will be changed in onExit
                    continuePreprocessing = true;
                    break;
                case SEEK_ENTER_TO_INCLUDED_FILE:
                    assert this.seekEnterToThisIncludeInfo != null;
                    assert this.waitExitFromThisFileInfo == null : "expected null instead of " + this.waitExitFromThisFileInfo;
                    assert enteredFrom != null;
                    ClankDriver.ClankInclusionDirective inclDirective = enteredTo.getInclusionDirective();
                    assert inclDirective != null : "main file is the only one without include directive, but had to be handled above " + enteredTo;
                    // see if met onEnter into interested #include directive from include chain
                    int includeDirectiveIndex = inclDirective.getIncludeDirectiveIndex();
                    if (includeDirectiveIndex == seekEnterToThisIncludeInfo.getIncludeDirectiveIndex()) {
                        // consistency check that included file is as expected
                        if (CharSequenceUtils.contentEquals(seekEnterToThisIncludeInfo.getIncludedPath(), enteredTo.getFilePath())) {
                            if (this.remainingChainToInterestedFile.isEmpty()) {
                                assert seenInterestedFileInfo == null : "can not enter twice " + seenInterestedFileInfo;
                                // update state
                                state = State.INSIDE_INITERESTED_FILE;
                                seenInterestedFileInfo = enteredTo;
                                insideInterestedFile = true;
                            } else {
                                // remove new entrance from chain
                                seekEnterToThisIncludeInfo = this.remainingChainToInterestedFile.removeFirst();
                                assert seekEnterToThisIncludeInfo != null;
                                // need to find entrance into next level of include chain
                                // keep seeking state
                                state = State.SEEK_ENTER_TO_INCLUDED_FILE;
                            }
                            // let's enter
                            continuePreprocessing = true;
                        } else {
                            // this is corrupted include stack, we don't want to go this way anymore
                            state = State.CORRUPTED_INCLUDE_CHAIN;
                            assert preparedPreprocessorOutput == null;
                            // do not really enter
                            continuePreprocessing = false;
                        }
                    } else if (includeDirectiveIndex > seekEnterToThisIncludeInfo.getIncludeDirectiveIndex()) {
                        // i.e. we skipped onEnter hook due to skipByGuard optimization
                        // so we've got index 1 without getting index 0
                        // this is corrupted include stack, we don't want to go this way anymore
                        state = State.CORRUPTED_INCLUDE_CHAIN;
                        assert preparedPreprocessorOutput == null;
                        // do not really enter
                        continuePreprocessing = false;
                    } else {
                        assert includeDirectiveIndex < seekEnterToThisIncludeInfo.getIncludeDirectiveIndex() : "why hasn't stopped after interested file? " + seekEnterToThisIncludeInfo;
                        // before interested file we met #include directive
                        // have to visit full include branch and come back to our file
                        assert this.waitExitFromThisFileInfo == null : "expected null instead of " + this.waitExitFromThisFileInfo;
                        // set up exit-from marker object
                        waitExitFromThisFileInfo = enteredTo;
                        state = State.WAIT_EXIT_FROM_FILE;
                        // let's enter
                        continuePreprocessing = true;
                    }   
                    break;
                default:
                    assert false : "unexpected state = " + state;
                    state = State.CORRUPTED_INCLUDE_CHAIN;
                    // do not really enter
                    continuePreprocessing = false;
                    break;
            }
            if (!pushEnteredFile(enteredFrom, enteredTo)) {
                state = State.CORRUPTED_INCLUDE_CHAIN;
                insideInterestedFile = false;
                preparedPreprocessorOutput = null;
                continuePreprocessing = false;                
            }
            return continuePreprocessing;
        }

        @Override
        public final boolean onExit(ClankDriver.ClankFileInfo exitedFrom, ClankDriver.ClankFileInfo exitedTo) {
            assert state != State.WAIT_COMPILATION_UNIT_FILE : "can not exit before entering compilation unit file ";
            boolean exitingFromInterestedFile = insideInterestedFile;
            insideInterestedFile = false;
            boolean continuePreprocessing;
            if (preparedPreprocessorOutput != null) {
                // already gathered token stream
                assert state == State.DONE;
                // can stop all
                continuePreprocessing = false;
            } else if (state == State.CORRUPTED_INCLUDE_CHAIN) {
                assert preparedPreprocessorOutput == null;
                // continue exit
                continuePreprocessing = false;
            } else {
                assert exitedFrom != null;
                assert seenInterestedFileInfo != null || waitExitFromThisFileInfo != null || (state == State.SEEK_ENTER_TO_INCLUDED_FILE) : 
                        "in state " + state + " we exit from enexpected include branch ? " + exitedFrom + "\nback to\n" + exitedTo; // NOI18N         
                if (exitedFrom == seenInterestedFileInfo) {
                    assert waitExitFromThisFileInfo == null;
                    // stop all activity on exit from interested file
                    preparedPreprocessorOutput = ClankDriver.extractPreparedPreprocessorOutput(exitedFrom);
                    assert preparedPreprocessorOutput != null;
                    state = State.DONE;
                    // stop after exit
                    assert exitingFromInterestedFile;
                    continuePreprocessing = false;
                } else if (exitedFrom == waitExitFromThisFileInfo) {
                    assert (state == State.WAIT_EXIT_FROM_FILE);
                    // clear exit-from marker object 
                    waitExitFromThisFileInfo = null;
                    if (seenInterestedFileInfo == null) {
                        // we met #include before expected include chain entry point
                        // switch back to seek of entry point
                        state = State.SEEK_ENTER_TO_INCLUDED_FILE;
                    } else {
                        assert seenInterestedFileInfo != null;
                        assert exitedTo == seenInterestedFileInfo : "unexpected to exit back to file " + exitedTo + "\nwhen we wait to exit into " + seenInterestedFileInfo;
                        assert this.remainingChainToInterestedFile.isEmpty() : "we are inside interested file only when walked whole chain: " + remainingChainToInterestedFile;
                        state = State.INSIDE_INITERESTED_FILE;
                        // gather information again when come back to interested file
                        insideInterestedFile = true;
                    }
                    continuePreprocessing = true;
                } else if (state == State.SEEK_ENTER_TO_INCLUDED_FILE) {
                    // we were seeking for onEnter into deeper level, but instead got earlier onExit
                    // it could be the case when inside included file there is no more #include 
                    // directives and we exit from just entered file;
                    // this is corrupted include stack, we don't want to go this way anymore;
                    state = State.CORRUPTED_INCLUDE_CHAIN;
                    continuePreprocessing = false;
                } else {
                    // in all other cases exit but continue 
                    // till we meet FileInfoForExitFrom or seenInterestedFile
                    // state can be update in further onEnter hook as well
                    continuePreprocessing = true;
                }
            }
            if (!popExitedFile(exitedFrom, exitedTo, state, exitingFromInterestedFile)) {
                state = State.CORRUPTED_INCLUDE_CHAIN;
                insideInterestedFile = false;
                preparedPreprocessorOutput = null;
                continuePreprocessing = false;
            }
            return continuePreprocessing;
        }

        public ClankDriver.ClankPreprocessorOutput getPreparedPreprocessorOutput() {
            return preparedPreprocessorOutput;
        }
    }
    
    private static final class FileImplPreprocessorCallback extends VisitIncludeChainPreprocessorCallback {
        private final FileImpl startFile;
        private final FileImpl interestedFile;
        private final ProjectBase startProject;
        private final PreprocHandler ppHandler;

        // chain of current include stack as FileImpls
        private final List<FileImpl> curFiles = new ArrayList<>();
                
        public FileImplPreprocessorCallback(FileImpl startFile, FileImpl interestedFile, 
                PreprocHandler ppHandler, 
                ClankTokenStreamProducerParameters params) {
            super(APTHandlersSupport.extractIncludeStack(ppHandler.getState()), params);
            this.startFile = startFile;
            this.interestedFile = interestedFile;
            this.startProject = startFile.getProjectImpl(true);
            this.ppHandler = ppHandler;
        }

        /**
         * in the stack on tracked files return top one and pop if needed.
         * @param pop true to pop, false to peek only
         * @return non null top file
         */
        private FileImpl getCurFile(boolean pop) {
          assert curFiles.size() > 0;
          FileImpl curFile;
          if (pop) {
            curFile = curFiles.remove(curFiles.size() - 1);
          } else {
            curFile = curFiles.get(curFiles.size() - 1);
          }
          assert curFile != null;
          return curFile;
        }
        
        private void pushCurrentFile(FileImpl enteredToFileImpl) {
            curFiles.add(enteredToFileImpl);
        }
        
        @Override
        public void include(ClankDriver.ClankInclusionDirective directive) {
            // always resolve path to have behavior like in APT, where file resolution
            // includes query to library manager which creates libraries on demand
            ResolvedPath resolvedPath = directive.getResolvedPath();
            if (resolvedPath == null) {
                // broken #include path
                directive.setAnnotation(UnresolvedIncludeDirectiveReason.NULL_PATH);
                return;
            }
            // peek file from onEnter
            FileImpl curFile = getCurFile(false);
            CharSequence path = resolvedPath.getPath();
            ProjectBase aStartProject = startProject;
            if (aStartProject != null) {
                // resolve if not interrupted
                if (aStartProject.isValid() && curFile.isValid()) {
                    ProjectBase inclFileOwner = aStartProject.getLibraryManager().resolveFileProjectOnInclude(aStartProject, curFile, resolvedPath);
                    if (inclFileOwner == null) {
                        // resolveFileProjectOnInclude() javadoc reads: "Can return NULL !"; and it asserts itself
                        if (aStartProject.getFileSystem() == resolvedPath.getFileSystem()) {
                            // if file systems do match, then use start project as fallback
                            inclFileOwner = aStartProject;
                        }
                    }
                    if (inclFileOwner == null) {
                        // error case
                        directive.setAnnotation(new UnresolvedIncludeDirectiveAnnotation(UnresolvedIncludeDirectiveReason.UNRESOLVED_FILE_OWNER, resolvedPath));
                        return;
                    }
                    if (CndUtils.isDebugMode()) {
                        CndUtils.assertTrue(inclFileOwner.getFileSystem() == resolvedPath.getFileSystem(), "Different FS for " + path + ": " + inclFileOwner.getFileSystem() + " vs " + resolvedPath.getFileSystem()); // NOI18N
                    }
                    // when owner of included file is detected we can ask it for FileImpl instance
                    FileImpl includedFile = inclFileOwner.prepareIncludedFile(aStartProject, path, ppHandler);
                    if (includedFile == null) {
                        if (CsmModelAccessor.isModelAlive() && inclFileOwner.isValid()) {
                            if (aStartProject.isValid()) {
                                // error case
                                APTUtils.LOG.log(Level.INFO, "something wrong when including {0} from {1}", new Object[]{path, curFile});
                                directive.setAnnotation(new UnresolvedIncludeDirectiveAnnotation(UnresolvedIncludeDirectiveReason.START_PROJECT_CLOSED, startProject, resolvedPath, curFile));
                            } else {
                                // error case
                                APTUtils.LOG.log(Level.INFO, "invalid start project {0} when including {1} from {2}", new Object[]{aStartProject, path, curFile});
                                directive.setAnnotation(new UnresolvedIncludeDirectiveAnnotation(UnresolvedIncludeDirectiveReason.INVALID_START_PROJECT, aStartProject, resolvedPath, curFile));
                            }
                        } else {
                            // error case
                            APTUtils.LOG.log(Level.INFO, "Start project {0} can not create by path {1} from {2}", new Object[]{aStartProject, path, curFile});
                            directive.setAnnotation(new UnresolvedIncludeDirectiveAnnotation(UnresolvedIncludeDirectiveReason.START_PROJECT_CANNOT_CREATE_FILE, aStartProject, resolvedPath, curFile));
                        }
                    } else {
                        // The only one good branch:
                        // annotated include directive to have access to FileImpl from onEnter which follows all resolved #includes
                        directive.setAnnotation(includedFile);
                    }
                } else {
                    // error case
                    APTUtils.LOG.log(Level.INFO, "invalid start project {0} or file when including {1} from {2}", new Object[]{aStartProject, path, curFile});
                    directive.setAnnotation(new UnresolvedIncludeDirectiveAnnotation(UnresolvedIncludeDirectiveReason.INVALID_START_PROJECT, aStartProject, resolvedPath, curFile));
                    // assert false : "invalid start project when including " + path + " from " + curFile;
                }
            } else {
                // error case
                APTUtils.LOG.log(Level.SEVERE, "FileTokenStreamCallback: file {0} without project!!!", new Object[]{path});// NOI18N
                directive.setAnnotation(new UnresolvedIncludeDirectiveAnnotation(UnresolvedIncludeDirectiveReason.NULL_START_PROJECT, resolvedPath, curFile));
            }            
        }

        @Override
        protected boolean pushEnteredFile(ClankDriver.ClankFileInfo enteredFrom, ClankDriver.ClankFileInfo enteredTo) {
            assert enteredTo != null;
            ClankDriver.ClankInclusionDirective enteredAsInclusion = enteredTo.getInclusionDirective();
            assert (enteredFrom == null) == (enteredAsInclusion == null) : "inclusion directive is null if and only if entering main file " + enteredFrom + " vs. " + enteredAsInclusion;
            FileImpl enteredToFileImpl;
            // prepare "entered to file"
            if (enteredFrom == null) {
                // main file case: entered to start file
                enteredToFileImpl = startFile;
            } else {
                // entered through #include directive: ask file from annotation initialized in onInclusionDirective/include
                Object inclusionAnnotation = enteredAsInclusion.getAnnotation();
                if (inclusionAnnotation instanceof FileImpl) {
                    // successfully resolved #include followed by this onEnter call
                    enteredToFileImpl = (FileImpl)inclusionAnnotation;
                } else {
                    // it is suspicious to see unresolved include followed by onEnter hook
                    // it might be in case of cancelled/interrupted query
                    // error recovery is: report and full stop
                    APTUtils.LOG.log(Level.INFO, inclusionAnnotation.toString());
                    return false;
                }
            }
            if (CndUtils.isDebugMode()) {
                final boolean anAssert = !isInsideInterestedFile() || this.interestedFile.equals(enteredToFileImpl);
                if (!anAssert) {
                    CndUtils.assertTrueInConsole(anAssert, "" + isInsideInterestedFile() + ": inconsistency between " + this.interestedFile + " and ", enteredToFileImpl);
                }
                CndUtils.assertPathsEqualInConsole(enteredToFileImpl.getAbsolutePath(), enteredTo.getFilePath(), "Expected {0}\n got {1}", enteredToFileImpl, enteredTo);
            }
            pushCurrentFile(enteredToFileImpl);
            return true;
        }

        @Override
        protected boolean popExitedFile(ClankDriver.ClankFileInfo exitedFrom, ClankDriver.ClankFileInfo exitedTo, State state, boolean exitingFromInterestedFile) {
            assert exitedFrom != null;
            // on exit pop current file from stack
            FileImpl exitedFromFileImpl = getCurFile(true);
            assert exitedFromFileImpl != null;
            ClankDriver.ClankInclusionDirective exitedInclusion = exitedFrom.getInclusionDirective();
            assert (exitedInclusion == null) == (exitedTo == null) : "inclusion directive is null if and only if exiting main file " + exitedTo + " vs. " + exitedInclusion;
            if (state == State.CORRUPTED_INCLUDE_CHAIN) {
                // nothing can be done
                return false;
            }
            CharSequence exitedFromFileImplPath = exitedFromFileImpl.getAbsolutePath();
            CharSequence exitedFromFileInfoPath = exitedFrom.getFilePath();
            if (!CharSequenceUtils.contentEquals(exitedFromFileImplPath, exitedFromFileInfoPath)) {
                CndUtils.assertPathsEqualInConsole(exitedFromFileImplPath, exitedFromFileInfoPath, "Expected Exit From {0}\n got {1}", exitedFromFileImpl, exitedFrom);
                // nothing can be done in corrupted stack
                return false;
            }
            if (CndUtils.isDebugMode()) {                
                if (exitedTo != null) {
                    if (curFiles.size() > 0) {
                        FileImpl exitedToFileImpl = getCurFile(false);
                        CndUtils.assertPathsEqualInConsole(exitedToFileImpl.getAbsolutePath(), exitedTo.getFilePath(), "Expected Exit To {0}\n got {1}", exitedToFileImpl, exitedTo);
                    } else {
                        CndUtils.assertTrueInConsole(false, "no more files in stack for ", exitedTo);
                    }
                } else {
                    assert exitedFromFileImpl == startFile : "exited from " + exitedFromFileImpl + "\nexpected " + startFile;
                }
                if (exitingFromInterestedFile) {
                    // on exit must always be correct, otherwise on enter hasn't tracked correctly erroneous enter
                    CndUtils.assertPathsEqualInConsole(exitedFromFileInfoPath, interestedFile.getAbsolutePath(),
                            "Expected {0}\n got {1}", interestedFile, exitedFrom);// NOI18N
                    assert state == State.DONE : "expected DONE instead of " + state + " for " + this.interestedFile;
                }
            }
            if (exitedInclusion != null) {
                // if already done, then just exit
                if (state == State.DONE && !exitingFromInterestedFile) {
                    return true;
                }
                // the exit from #include
                return postIncludeAction(exitedFromFileImpl, exitedFrom);
            } else {
                // just exit from start file
                return true;
            }
        }

        protected boolean postIncludeAction(FileImpl exitedFromFileImpl, ClankDriver.ClankFileInfo exitedFrom) {
            // when exit from included file notify project if requested
            if (parameters.triggerParsingActivity) {
                try {
                    assert ClankDriver.extractPreprocessorOutput(ppHandler).hasTokenStream();
                    PreprocHandler.State inclState = ppHandler.getState();
                    assert !inclState.isCleaned();
                    CharSequence inclPath = exitedFrom.getFilePath();
                    ProjectBase inclFileOwnerProject = exitedFromFileImpl.getProjectImpl(true);
                    ProjectBase aStartProject = startProject;
                    if (inclFileOwnerProject.isDisposing() || aStartProject.isDisposing()) {
                        if (TraceFlags.TRACE_VALIDATION || TraceFlags.TRACE_MODEL_STATE) {
                            System.err.printf("onFileIncluded: %s file [%s] is interrupted on disposing project%n", inclPath, inclFileOwnerProject.getName());
                        }
                        return false;
                    } else {
                        FilePreprocessorConditionState pcState = FilePreprocessorConditionState.build(inclPath, exitedFrom.getSkippedRanges());
                        PreprocessorStatePair ppStatePair = new PreprocessorStatePair(inclState, pcState);
                        inclFileOwnerProject.postIncludeFile(aStartProject, exitedFromFileImpl, inclPath, ppStatePair, null);
                    }
                } catch (Exception ex) {
                    APTUtils.LOG.log(Level.SEVERE, "MyClankPreprocessorCallback: error on including {0}:%n{1}", new Object[]{exitedFrom.getFilePath(), ex});
                    DiagnosticExceptoins.register(ex);
                    return false;
                }
            }
            return true;
        }
    }    

    private static final class PatchedFileBuffer implements FileBuffer {
        private final FileBuffer delegate;
        private final CodePatch codePatch;
        private char[] res;
        private Line2Offset lines;

        private PatchedFileBuffer(FileBuffer delegate, CodePatch patchCode) {
            this.delegate = delegate;
            this.codePatch = patchCode;
        }

        @Override
        public void addChangeListener(ChangeListener listener) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void removeChangeListener(ChangeListener listener) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isFileBased() {
            return delegate.isFileBased();
        }

        @Override
        public FileObject getFileObject() {
            return delegate.getFileObject();
        }

        @Override
        public CharSequence getUrl() {
            return delegate.getUrl();
        }

        @Override
        public String getText(int start, int end) throws IOException {
            return new String(getCharBuffer(), start, end - start);
        }

        @Override
        public CharSequence getText() throws IOException {
            return new FileBufferFile.MyCharSequence(getCharBuffer());
        }

        @Override
        public long lastModified() {
            return delegate.lastModified()+1;
        }

        @Override
        public long getCRC() {
            throw new UnsupportedOperationException();
        }

        @Override
        public int[] getLineColumnByOffset(int offset) throws IOException {
            if (lines == null) {
                lines = new Line2Offset(getCharBuffer());
            }

            return lines.getLineColumnByOffset(offset);
        }

        @Override
        public int getLineCount() throws IOException {
            if (lines == null) {
                lines = new Line2Offset(getCharBuffer());
            }
            return lines.getLineCount();
        }

        @Override
        public int getOffsetByLineColumn(int line, int column) throws IOException {
            if (lines == null) {
                lines = new Line2Offset(getCharBuffer());
            }
            return lines.getOffsetByLineColumn(line, column);
        }

        @Override
        public CharSequence getAbsolutePath() {
            return delegate.getAbsolutePath();
        }

        @Override
        public FileSystem getFileSystem() {
            return delegate.getFileSystem();
        }

        @Override
        public char[] getCharBuffer() throws IOException {
            if (res == null) {
                char[] charBuffer = delegate.getCharBuffer();
                char[] patch = codePatch.getPatch().toCharArray();
                res = new char[charBuffer.length-(codePatch.getEndOffset()-codePatch.getStartOffset())+patch.length];
                System.arraycopy(charBuffer, 0, res, 0, codePatch.getStartOffset());
                System.arraycopy(patch, 0, res, codePatch.getStartOffset(), patch.length);
                System.arraycopy(charBuffer, codePatch.getEndOffset(), res, codePatch.getStartOffset()+patch.length, charBuffer.length - codePatch.getEndOffset());
            }
            return res;
        }

        @Override
        public BufferType getType() {
            return delegate.getType();
        }

    }
}
