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

import org.netbeans.modules.cnd.antlr.Token;
import org.netbeans.modules.cnd.antlr.TokenStream;
import org.netbeans.modules.cnd.antlr.TokenStreamException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import org.netbeans.modules.cnd.api.model.CsmMacro;
import org.netbeans.modules.cnd.apt.structure.APT;
import org.netbeans.modules.cnd.apt.structure.APTDefine;
import org.netbeans.modules.cnd.apt.structure.APTError;
import org.netbeans.modules.cnd.apt.structure.APTFile;
import org.netbeans.modules.cnd.apt.structure.APTInclude;
import org.netbeans.modules.cnd.apt.structure.APTPragma;
import org.netbeans.modules.cnd.apt.support.APTFileCacheEntry;
import org.netbeans.modules.cnd.apt.support.APTHandlersSupport;
import org.netbeans.modules.cnd.apt.support.api.PPIncludeHandler.IncludeState;
import org.netbeans.modules.cnd.apt.support.lang.APTLanguageFilter;
import org.netbeans.modules.cnd.apt.support.APTMacroExpandedStream;
import org.netbeans.modules.cnd.apt.support.api.PreprocHandler;
import org.netbeans.modules.cnd.apt.support.APTToken;
import org.netbeans.modules.cnd.apt.support.APTTokenAbstact;
import org.netbeans.modules.cnd.apt.support.APTTokenTypes;
import org.netbeans.modules.cnd.apt.support.PostIncludeData;
import org.netbeans.modules.cnd.apt.support.ResolvedPath;
import org.netbeans.modules.cnd.apt.support.lang.APTBaseLanguageFilter;
import org.netbeans.modules.cnd.apt.utils.APTCommentsFilter;
import org.netbeans.modules.cnd.apt.utils.APTUtils;
import org.netbeans.modules.cnd.modelimpl.accessors.CsmCorePackageAccessor;
import org.netbeans.modules.cnd.modelimpl.csm.IncludeImpl;
import org.netbeans.modules.cnd.modelimpl.csm.MacroImpl;
import org.netbeans.modules.cnd.modelimpl.csm.core.ErrorDirectiveImpl;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileImpl;
import org.netbeans.modules.cnd.modelimpl.content.file.FileContent;
import org.netbeans.modules.cnd.modelimpl.csm.core.FilePreprocessorConditionState;
import org.netbeans.modules.cnd.modelimpl.csm.core.PreprocessorStatePair;
import org.netbeans.modules.cnd.modelimpl.csm.core.ProjectBase;
import org.netbeans.modules.cnd.modelimpl.csm.core.SimpleOffsetableImpl;
import org.netbeans.modules.cnd.modelimpl.csm.core.Utils;
import org.netbeans.modules.cnd.modelimpl.debug.DiagnosticExceptoins;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;
import org.netbeans.modules.cnd.modelimpl.textcache.NameCache;

/**
 * implementation of walker used when parse files/collect macromap
 */
public class APTParseFileWalker extends APTProjectFileBasedWalker {

    /**
     * A callback that should be invoked
     * when each conditional is evaluated
     */
    public interface EvalCallback {

        void onEval(APT apt, boolean result);

        void onErrorDirective(APT apt);

        void onPragmaOnceDirective(APT apt);
    }
    private FileContent fileContent;
    private final boolean triggerParsingActivity;
    private final EvalCallback evalCallback;
    private static final EvalCallback EMPTY_EVAL_CALLBACK = new EvalCallback() {
        @Override
        public void onEval(APT apt, boolean result) { }
        @Override
        public void onErrorDirective(APT apt) { }
        public void onPragmaOnceDirective(APT apt) { }
    };
    private final CsmCorePackageAccessor csmCorePackageAccessor;


    public APTParseFileWalker(ProjectBase base, APTFile apt, FileImpl file, PreprocHandler preprocHandler, boolean triggerParsingActivity, EvalCallback evalCallback, APTFileCacheEntry cacheEntry) {
        super(base, apt, file, preprocHandler, cacheEntry);
        this.evalCallback = evalCallback != null ? evalCallback : EMPTY_EVAL_CALLBACK;
        this.triggerParsingActivity = triggerParsingActivity;
        csmCorePackageAccessor = CsmCorePackageAccessor.get();

    }

    public void setFileContent(FileContent content) {
        this.fileContent = content;
    }

    protected boolean needMacroAndIncludes() {
        return this.fileContent != null;
    }

    public final boolean isTriggerParsingActivity() {
        return triggerParsingActivity;
    }

    @Override
    protected boolean needPPTokens() {
        return TraceFlags.PARSE_HEADERS_WITH_SOURCES;
    }

    public TokenStream getFilteredTokenStream(APTLanguageFilter lang) {
        TokenStream ts = new LdScopeFilter(lang.getFilteredStream(getTokenStream()));
        // apply preprocessed text indexing
        // disabled for now
//        if (CndTraceFlags.TEXT_INDEX) {
//            ts = APTIndexingSupport.index(getStartProject().getFileSystem(), getFile().getAbsolutePath().toString(), ts);
//        }
        return ts;
    }

    @Override
    public TokenStream getTokenStream() {
        return getTokenStream(true);
    }

    public TokenStream getTokenStream(boolean filterOutComments) {
        setMode(ProjectBase.GATHERING_TOKENS);
        // get original
        TokenStream ts = super.getTokenStream();
        // expand macros
        ts = new APTMacroExpandedStream(ts, getMacroMap(), !filterOutComments);
        if (filterOutComments) {
            // remove comments
            ts = new APTCommentsFilter(ts);
        }
        return ts;
    }

    @Override
    protected void onDefine(APT apt) {
        super.onDefine(apt);
        if (needMacroAndIncludes()) {
            CsmMacro macro = createMacro((APTDefine) apt);
            this.fileContent.addMacro(macro);
        }
    }

    @Override
    protected void onErrorNode(APT apt) {
        super.onErrorNode(apt);
        evalCallback.onErrorDirective(apt);
        if (needMacroAndIncludes()) {
            this.fileContent.addError(createError((APTError)apt));
        }
    }

    @Override
    protected void onPragmaNode(APT apt) {
        super.onPragmaNode(apt);
        if (isStopped()) {
            evalCallback.onPragmaOnceDirective(apt);
        } else {
            APTPragma pragma = (APTPragma) apt;
            APTToken name = pragma.getName();
            if (name != null) {
                CharSequence textID = name.getTextID();
                if (DISABLE_LDSCOPE.contentEquals(textID)) {
                    ldScopeEnabled = false;
                } else if (ENABLE_LDSCOPE.contentEquals(textID)) {
                    ldScopeEnabled = true;
                }
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // impl of abstract methods
    @Override
    protected void postInclude(APTInclude apt, FileImpl included, IncludeState pushIncludeState) {
        if (needMacroAndIncludes()) {
            int includeDirectiveIndex = super.getCurIncludeDirectiveFileIndex();
            this.fileContent.addInclude(createInclude(apt, included, pushIncludeState == IncludeState.Recursive, includeDirectiveIndex), pushIncludeState != IncludeState.Success);
        }
    }

    @Override
    protected boolean hasIncludeActionSideEffects() {
        return needMacroAndIncludes();
    }

    @Override
    protected FileImpl includeAction(ProjectBase inclFileOwner, CharSequence inclPath, int mode, APTInclude apt, PostIncludeData postIncludeState) throws IOException {
        try {
            PreprocHandler preprocHandler = getPreprocHandler();
            FileImpl includedFile = inclFileOwner.prepareIncludedFile(inclFileOwner, inclPath, preprocHandler);
            if (includedFile != null) {
                ProjectBase startProject = getStartProject();
                if (inclFileOwner.isDisposing() || startProject.isDisposing()) {
                    if (TraceFlags.TRACE_VALIDATION || TraceFlags.TRACE_MODEL_STATE) {
                        System.err.printf("onFileIncluded: %s file [%s] is interrupted on disposing project%n", inclPath, inclFileOwner.getName());
                    }
                } else {
                    FileIncludeInParams params = new FileIncludeInParams(inclFileOwner, startProject, includedFile, inclPath, preprocHandler, postIncludeState, mode, isTriggerParsingActivity());
                    if (isTokenProducer() && TraceFlags.PARSE_HEADERS_WITH_SOURCES) {
                        FileIncludeOutParams inclInfo = includeFileWithTokens(params);
                        if (inclInfo != null) {
                            super.putNodeProperty(apt, FileIncludeOutParams.class, inclInfo);
                        }
                    } else {
                        FileIncludeOutParams inclInfo = includeFileWithoutTokens(params);
                        if (isTriggerParsingActivity() && inclInfo != null) {
                            inclFileOwner.postIncludeFile(startProject, includedFile, inclPath, inclInfo.getStatePair(), inclInfo.aptCacheEntry);
                        }
                    }
                }
            }
            return includedFile;
        } catch (NullPointerException ex) {
            APTUtils.LOG.log(Level.SEVERE, "NPE when processing file " + inclPath, ex);// NOI18N
            DiagnosticExceptoins.register(ex);
        }
        return null;
    }

    @Override
    protected void popInclude(APTInclude aptInclude, ResolvedPath resolvedPath, IncludeState pushState) {
        if (pushState == IncludeState.Success) {
            super.popInclude(aptInclude, resolvedPath, pushState);
            if (isTokenProducer() && TraceFlags.PARSE_HEADERS_WITH_SOURCES) {
                FileIncludeOutParams inclInfo = (FileIncludeOutParams) super.getNodeProperty(aptInclude, FileIncludeOutParams.class);
                if (inclInfo != null) {
                    inclInfo.inParams.inclFileOwner.postIncludeFile(inclInfo.inParams.startProject, inclInfo.inParams.includedFile, inclInfo.inParams.includedPath, inclInfo.getStatePair(), inclInfo.aptCacheEntry);
                }
            }
        }
    }
    
    /*package*/ static int[] getDefineOffsets(APTDefine define, APTToken lastParam) {
        int startOffset = define.getToken().getOffset();
        List<APTToken> bodyTokens = define.getBody();
        APTToken last;
        if (bodyTokens.isEmpty()) {
            if (false && lastParam != null && !APTUtils.isEOF(lastParam) && lastParam.getEndOffset() > 0) {
                // To make APT and Clank more similar for 
                // #define FUN_LIKE(param)
                // for closing ) use last param + 1
                final int lParentOffset = lastParam.getEndOffset()+1;
                last = new APTTokenAbstact() { 
                    public int getEndOffset() {
                        return lParentOffset;
                    }
                };
            } else {
                last = define.getName() ;
            }
        } else {
            last = bodyTokens.get(bodyTokens.size() - 1);
        }
        int endOffset = (last != null && !APTUtils.isEOF(last) && last.getEndOffset() > 0) ? last.getEndOffset() : startOffset;
        return new int[]{startOffset, endOffset};
    }

    ////////////////////////////////////////////////////////////////////////////
    // implementation details

    private static final class FileIncludeInParams {
        private final ProjectBase inclFileOwner;
        private final ProjectBase startProject;
        private final FileImpl includedFile;
        private final CharSequence includedPath;
        private final PreprocHandler preprocHandler;
        private final PostIncludeData postIncludeState;
        private final int mode;
        private final boolean triggerParsingActivity;

        public FileIncludeInParams(ProjectBase inclFileOwner, ProjectBase startProject, FileImpl includedFile, CharSequence includedFileName, PreprocHandler preprocHandler, PostIncludeData postIncludeState, int mode, boolean triggerParsingActivity) {
            this.inclFileOwner = inclFileOwner;
            this.startProject = startProject;
            this.includedFile = includedFile;
            this.includedPath = includedFileName;
            this.preprocHandler = preprocHandler;
            this.postIncludeState = postIncludeState;
            this.mode = mode;
            this.triggerParsingActivity = triggerParsingActivity;
        }
    }

    private static final class FileIncludeOutParams {
        private final FileIncludeInParams inParams;
        private final PreprocHandler.State ppState;
        private final FilePreprocessorConditionState pcState;
        private final APTBasedPCStateBuilder pcBuilder;
        private final APTFileCacheEntry aptCacheEntry;        

        public FileIncludeOutParams(FileIncludeInParams inParams, PreprocHandler.State ppState, FilePreprocessorConditionState pcState, APTFileCacheEntry aptCacheEntry) {
            this(inParams, ppState, pcState, null, aptCacheEntry);
            assert pcState != null;
        }

        public FileIncludeOutParams(FileIncludeInParams inParams, PreprocHandler.State ppState, APTBasedPCStateBuilder pcBuilder, APTFileCacheEntry aptCacheEntry) {
            this(inParams, ppState, null, pcBuilder, aptCacheEntry);
            assert pcBuilder != null;
        }

        private FileIncludeOutParams(FileIncludeInParams inParams, PreprocHandler.State ppState, FilePreprocessorConditionState pcState, APTBasedPCStateBuilder pcBuilder, APTFileCacheEntry aptCacheEntry) {
            this.inParams = inParams;
            this.ppState = ppState;
            this.pcState = pcState;
            this.pcBuilder = pcBuilder;
            this.aptCacheEntry = aptCacheEntry;
        }

        private PreprocessorStatePair getStatePair() {
            if (pcState != null) {
                return new PreprocessorStatePair(ppState, pcState);
            } else {
                assert pcBuilder != null;
                return new PreprocessorStatePair(ppState, pcBuilder.build());
            }
        }
    }

    private FileIncludeOutParams includeFileWithTokens(FileIncludeInParams params) throws IOException {
        APTFile aptFile = APTTokenStreamProducer.getFileAPT(params.includedFile, true);
        if (aptFile != null) {
            PreprocHandler.State ppIncludeState = params.preprocHandler.getState();
            // ask for exclusive entry if absent
            APTFileCacheEntry aptCacheEntry = params.includedFile.getAPTCacheEntry(ppIncludeState, Boolean.TRUE);
            // gather macro map from all includes and fill preprocessor conditions state
            APTBasedPCStateBuilder pcBuilder = new APTBasedPCStateBuilder(params.includedFile.getAbsolutePath());
            APTParseFileWalker walker = new APTParseFileWalker(params.startProject, aptFile, params.includedFile, params.preprocHandler, params.triggerParsingActivity, pcBuilder, aptCacheEntry);
            FileContent inclFileContent = params.includedFile.prepareIncludedFileParsingContent();
            walker.setFileContent(inclFileContent);
            includeStream(aptFile, walker);
            return new FileIncludeOutParams(params, ppIncludeState, pcBuilder, aptCacheEntry);
        } else {
            // in the case file was just removed
            Utils.LOG.log(Level.INFO, "Can not find or build APT for file {0}", params.includedFile); //NOI18N
        }
        return null;
    }

    private CsmCorePackageAccessor getCsmCorePackageAccessor() {
        return csmCorePackageAccessor;
    }
    /**
     * called to inform that file was #included from another file with specific
     * preprocHandler
     *
     * @param file included file path
     * @param preprocHandler preprocHandler with which the file is including
     * @param mode of walker forced onFileIncluded for #include directive
     * @return true if it's first time of file including false if file was
     * included before
     */
    private FileIncludeOutParams includeFileWithoutTokens(FileIncludeInParams params) throws IOException {
        assert params.preprocHandler != null : "null preprocHandler for " + params.includedPath;
        assert params.includedFile != null : "null FileImpl for " + params.includedPath;

        PreprocHandler.State newState = params.preprocHandler.getState();
        PreprocessorStatePair cachedOut = null;
        APTFileCacheEntry aptCacheEntry = null;
        FilePreprocessorConditionState pcState = null;
        boolean foundInCache = false;
        // check cache if it has complete post include state
        if (params.postIncludeState != null
                && params.postIncludeState.hasPostIncludeMacroState()
                && params.postIncludeState.hasDeadBlocks()) {
            pcState = FilePreprocessorConditionState.build(params.includedPath, params.postIncludeState.getDeadBlocks());
            params.preprocHandler.getMacroMap().setState(params.postIncludeState.getPostIncludeMacroState());
            foundInCache = true;
        }
        // check visited file cache
        boolean isFileCacheApplicable = (params.mode == ProjectBase.GATHERING_TOKENS) && (APTHandlersSupport.getIncludeStackDepth(newState) == 1);
        if (!foundInCache && isFileCacheApplicable) {

            cachedOut = getCsmCorePackageAccessor().getCachedVisitedState(params.includedFile, newState);
            if (cachedOut != null) {
                params.preprocHandler.getMacroMap().setState(APTHandlersSupport.extractMacroMapState(cachedOut.state));
                pcState = cachedOut.pcState;
                foundInCache = true;
            }
        }
        // if not found in caches => visit include file
        if (!foundInCache) {
            APTFile aptLight = APTTokenStreamProducer.getFileAPT(params.includedFile, false);
            if (aptLight == null) {
                // in the case file was just removed
                Utils.LOG.log(Level.INFO, "Can not find or build APT for file {0}", params.includedPath); //NOI18N
                return null;
            }

            // gather macro map from all includes and fill preprocessor conditions state
            APTBasedPCStateBuilder pcBuilder = new APTBasedPCStateBuilder(params.includedFile.getAbsolutePath());
            // ask for exclusive entry if absent
            aptCacheEntry = params.includedFile.getAPTCacheEntry(newState, Boolean.TRUE);
            APTParseFileWalker walker = new APTParseFileWalker(params.startProject, aptLight, params.includedFile, params.preprocHandler, params.triggerParsingActivity, pcBuilder, aptCacheEntry);
            walker.visit();
            pcState = pcBuilder.build();
        }
        // updated caches
        // update post include cache
        if (params.postIncludeState != null && !params.postIncludeState.hasDeadBlocks()) {
            int[] deadBlocks = getCsmCorePackageAccessor().getPCStateDeadBlocks(pcState);
            // cache info
            params.postIncludeState.setDeadBlocks(deadBlocks);
        }
        // updated visited file cache
        if (cachedOut == null && isFileCacheApplicable) {
            getCsmCorePackageAccessor().cacheVisitedState(params.includedFile, newState, params.preprocHandler, pcState);
        }
        return new FileIncludeOutParams(params, newState, pcState, aptCacheEntry);
    }

    private ErrorDirectiveImpl createError(APTError error) {
        APTToken token = error.getToken();
        SimpleOffsetableImpl pos = getOffsetable(token);
        setEndPosition(pos, token);
        return ErrorDirectiveImpl.create(this.getFile(), token.getTextID(), pos, getPreprocHandler().getState());
    }

    private CsmMacro createMacro(APTDefine define) {
        // create even for invalid macro (to have possibility of showing error HL)
        List<CharSequence> params = null;
        Collection<APTToken> paramTokens = define.getParams();
        APTToken lastParam = null;
        if (paramTokens != null) {
            params = new ArrayList<>(paramTokens.size());
            for (APTToken elem : paramTokens) {
                if (APTUtils.isID(elem)) {
                    params.add(NameCache.getManager().getString(elem.getTextID()));
                }
                if (!APTUtils.isVaArgsToken(elem)) {
                    // in APT VaArgs is shared token without real offsets 
                    lastParam = elem;
                }
            }
            if (params.isEmpty()) {
                params = Collections.<CharSequence>emptyList();
            }
        }
        
        int offsets[] = getDefineOffsets(define, lastParam);

        // FIXUP (performance/memory). For now:
        // 1) nobody uses macros.getText
        // 2) its realization is ineffective
        // so we temporarily switch this off
        String body = ""; //file.getText( start.getOffset(), last.getEndOffset());
        
        CsmMacro.Kind kind = define.isValid() ? CsmMacro.Kind.DEFINED : CsmMacro.Kind.INVALID;
        return MacroImpl.create(define.getName().getTextID(), params, body/*sb.toString()*/, getFile(), offsets[0], offsets[1], kind);
    }

    private IncludeImpl createInclude(final APTInclude apt, final FileImpl included, boolean recursive, int includedDirectiveIndex) {
        int startOffset = apt.getToken().getOffset();
        APTToken lastToken = getLastToken(apt.getInclude());
        if(lastToken == null || APTUtils.isEOF(lastToken)) {
            lastToken = apt.getToken();
        }
        int endOffset = (lastToken != null && !APTUtils.isEOF(lastToken)) ? lastToken.getEndOffset() : startOffset;
        IncludeImpl incImpl = IncludeImpl.create(apt.getFileName(getMacroMap()), apt.isSystem(getMacroMap()), recursive, included, getFile(), startOffset, endOffset, includedDirectiveIndex);
        return incImpl;
    }

    private SimpleOffsetableImpl getOffsetable(APTToken token) {
        return new SimpleOffsetableImpl(token.getLine(), token.getColumn(), token.getOffset());
    }

    private void setEndPosition(SimpleOffsetableImpl offsetable, APTToken token) {
        if (token != null && !APTUtils.isEOF(token)) {
            offsetable.setEndPosition(token.getEndLine(), token.getEndColumn(), token.getEndOffset());
        } else {
            assert offsetable.getStartPosition() != null;
            offsetable.setEndPosition(offsetable.getStartPosition());
        }
    }

    private APTToken getLastToken(TokenStream ts) {
        try {
            Token last = ts.nextToken();
            for (Token curr = null; !APTUtils.isEOF(curr = ts.nextToken());) {
                last = curr;
            }
            return (APTToken) last;
        } catch (TokenStreamException e) {
            DiagnosticExceptoins.register(e);
            return null;
        }
    }

    @Override
    protected void onEval(APT apt, boolean result) {
        evalCallback.onEval(apt, result);
    }

    // #pragme disable_ldscope
    // force the __global keyword to be just an identifier
    private static final String DISABLE_LDSCOPE = "disable_ldscope"; // NOI18N
    private static final String ENABLE_LDSCOPE = "enable_ldscope"; // NOI18N
    private boolean ldScopeEnabled = true;
    private final class LdScopeFilter implements TokenStream {

        private final TokenStream orig;

        public LdScopeFilter(TokenStream orig) {
            this.orig = orig;
        }

        @Override
        public Token nextToken() throws TokenStreamException {
            Token nextToken = orig.nextToken();
            if (!ldScopeEnabled) {
                if (nextToken.getType() == APTTokenTypes.LITERAL___global) {
                    nextToken = new APTBaseLanguageFilter.FilterToken((APTToken) nextToken, APTTokenTypes.IDENT);
                }
            }
            return nextToken;
        }
    }
}
