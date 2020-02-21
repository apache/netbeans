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

package org.netbeans.modules.cnd.apt.support;

import org.netbeans.modules.cnd.apt.support.api.PreprocHandler;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.Map;
import org.netbeans.modules.cnd.antlr.TokenStreamException;
import java.util.logging.Level;
import org.netbeans.modules.cnd.antlr.TokenStream;
import org.netbeans.modules.cnd.apt.impl.support.APTHandlersSupportImpl;
import org.netbeans.modules.cnd.apt.impl.support.APTPreprocessorToken;
import org.netbeans.modules.cnd.debug.DebugUtils;
import org.netbeans.modules.cnd.apt.structure.APT;
import org.netbeans.modules.cnd.apt.structure.APTDefine;
import org.netbeans.modules.cnd.apt.structure.APTFile;
import org.netbeans.modules.cnd.apt.structure.APTInclude;
import org.netbeans.modules.cnd.apt.structure.APTIncludeNext;
import org.netbeans.modules.cnd.apt.structure.APTPragma;
import org.netbeans.modules.cnd.apt.structure.APTUndefine;
import org.netbeans.modules.cnd.apt.support.api.PPIncludeHandler.IncludeState;
import org.netbeans.modules.cnd.apt.support.APTMacro.Kind;
import org.netbeans.modules.cnd.apt.support.api.PPIncludeHandler;
import org.netbeans.modules.cnd.apt.utils.APTUtils;
import org.netbeans.modules.cnd.apt.utils.TokenBasedTokenStream;
import org.netbeans.modules.cnd.utils.CndPathUtilities;
import org.netbeans.modules.cnd.utils.cache.TinyMaps;
import org.openide.filesystems.FileSystem;

/**
 * abstract Tree walker for APT
 */
public abstract class APTAbstractWalker extends APTWalker {
    private final APTPreprocHandler preprocHandler;
    private final CharSequence startPath;
    private final FileSystem startFileSystem;
    private final APTFileCacheEntry cacheEntry;
    
    protected APTAbstractWalker(APTFile apt, PreprocHandler preprocHandler, APTFileCacheEntry cacheEntry) {
        super(apt, preprocHandler == null ? null: (APTMacroMap)preprocHandler.getMacroMap());
        this.startPath = apt.getPath();
        this.startFileSystem = apt.getFileSystem();
        this.preprocHandler = (APTPreprocHandler)preprocHandler;
        this.cacheEntry = cacheEntry;
    }

    @Override
    protected void preInit() {
        super.preInit();
        if (preprocHandler != null) {
            APTIncludeHandler includeHandler = (APTIncludeHandler)preprocHandler.getIncludeHandler();
            if (APTHandlersSupportImpl.isFirstLevel(includeHandler)) {
                // special handling of "-include file" feature of preprocessor
                final Collection<IncludeDirEntry> extractIncludeFileEntries = APTHandlersSupportImpl.extractIncludeFileEntries(includeHandler);
                int index = 0 - extractIncludeFileEntries.size();
                for (IncludeDirEntry includeDirEntry : extractIncludeFileEntries) {
                    FileSystem fileSystem = includeDirEntry.getFileSystem();
                    boolean system = fileSystem != startFileSystem;
                    APT fake = new APTIncludeFake(fileSystem, includeDirEntry.getAsSharedCharSequence().toString(), system, index++);
                    onAPT(fake, false);
                }
            }
        }
    }

    @Override
    protected void onInclude(APT apt) {
        super.onInclude(apt);
        if (getIncludeHandler() != null) {
            FileSystem fs = startFileSystem;
            if (apt instanceof APTIncludeFake) {
                // when handling "-include " use it's FileSystem
                fs = ((APTIncludeFake) apt).getFileSystem();
            }
            APTIncludeResolver resolver = getIncludeHandler().getResolver(fs, startPath);
            ResolvedPath resolvedPath = resolver.resolveInclude((APTInclude)apt, getMacroMap());
            if (resolvedPath == null) {
                if (DebugUtils.STANDALONE) {
                    if (APTUtils.LOG.getLevel().intValue() <= Level.SEVERE.intValue()) {
                        System.err.println("FAILED INCLUDE: from " + CndPathUtilities.getBaseName(startPath.toString()) + " for:\n\t" + apt);// NOI18N
                    }
                } else {
                    APTUtils.LOG.log(Level.WARNING,
                            "failed resolving path from {0} for {1}", // NOI18N
                            new Object[] { startPath, apt });
                }
            }
            includeImpl(resolvedPath, (APTInclude)apt);
        }
    }
    
    @Override
    protected void onIncludeNext(APT apt) {
        super.onIncludeNext(apt);
        if (getIncludeHandler() != null) {
            APTIncludeResolver resolver = getIncludeHandler().getResolver(startFileSystem, startPath); 
            ResolvedPath resolvedPath = resolver.resolveIncludeNext((APTIncludeNext)apt, getMacroMap());
            if (resolvedPath == null) {
                if (DebugUtils.STANDALONE) {
                    if (APTUtils.LOG.getLevel().intValue() <= Level.SEVERE.intValue()) {
                        System.err.println("FAILED INCLUDE: from " + CndPathUtilities.getBaseName(startPath.toString()) + " for:\n\t" + apt);// NOI18N
                    }
                } else {
                    APTUtils.LOG.log(Level.WARNING,
                            "failed resolving path from {0} for {1}", // NOI18N
                            new Object[] { startPath, apt });
                }
            }
            includeImpl(resolvedPath, (APTInclude) apt);
        }
    }

    private void includeImpl(ResolvedPath resolvedPath, APTInclude aptInclude) {
        IncludeState inclState = beforeIncludeImpl(aptInclude, resolvedPath);
        try {
            if (cacheEntry != null) {
                if (!startPath.equals(cacheEntry.getFilePath())) {
                    System.err.println("using not expected entry " + cacheEntry + " when work with file " + startPath);
                }
                if (cacheEntry.isSerial()) {
                    serialIncludeImpl(aptInclude, resolvedPath, inclState);
                } else {
                    Object lock = cacheEntry.getIncludeLock(aptInclude);
                    synchronized (lock) {
                        serialIncludeImpl(aptInclude, resolvedPath, inclState);
                    }
                }
            } else {
                include(resolvedPath, inclState, aptInclude, null);
            }
        } finally {
            afterIncludeImpl(aptInclude, resolvedPath, inclState);
        }
    }

    /**
     * 
     * @param resolvedPath
     * @param aptInclude
     * @param postIncludeState cached information about visit of this include directive
     * @return true if need to cache post include state
     */
    abstract protected boolean include(ResolvedPath resolvedPath, IncludeState inclState, APTInclude aptInclude, PostIncludeData postIncludeState);
    abstract protected boolean hasIncludeActionSideEffects();

    @Override
    protected void onDefine(APT apt) {
        APTDefine define = (APTDefine)apt;
        if (define.isValid()) {
            getMacroMap().define(getCurFile(), define, Kind.DEFINED);
        } else {
            if (DebugUtils.STANDALONE) {
                if (APTUtils.LOG.getLevel().intValue() <= Level.SEVERE.intValue()) {
                    System.err.println("INCORRECT #define directive: in " + CndPathUtilities.getBaseName(startPath.toString()) + " for:\n\t" + apt);// NOI18N
                }
            } else {
                APTUtils.LOG.log(Level.SEVERE,
                        "INCORRECT #define directive: in {0} for:\n\t{1}", // NOI18N
                        new Object[] { CndPathUtilities.getBaseName(startPath.toString()), apt });
            }
        }
    }

    @Override
    protected void onPragmaNode(APT apt) {
        APTPragma pragma = (APTPragma) apt;
        APTToken name = pragma.getName();
        if (name != null && APTPragma.PRAGMA_ONCE.contentEquals(name.getTextID())) {
            if (getMacroMap().isDefined(getFileOnceMacroName())) {
                // if already included => stop
                super.stop();
            } else {
                APTDefine fileOnce = APTUtils.createAPTDefineOnce(getFileOnceMacroName());
                getMacroMap().define(getCurFile(), fileOnce, Kind.DEFINED);
            }
        }
    }
    
    protected final CharSequence getFileOnceMacroName() {
        return APTUtils.getFileOnceMacroName(getCurFile());
    }

    @Override
    protected void onUndef(APT apt) {
        APTUndefine undef = (APTUndefine)apt;
        getMacroMap().undef(getCurFile(), undef.getName());
    }
    
    @Override
    protected boolean onIf(APT apt) {
        return eval(apt);
    }
    
    @Override
    protected boolean onIfdef(APT apt) {
        return eval(apt);
    }
    
    @Override
    protected boolean onIfndef(APT apt) {
        return eval(apt);
    }
    
    @Override
    protected boolean onElif(APT apt, boolean wasInPrevBranch) {
        return !wasInPrevBranch && eval(apt);
    }
    
    @Override
    protected boolean onElse(APT apt, boolean wasInPrevBranch) {
        return !wasInPrevBranch;
    }
    
    @Override
    protected void onEndif(APT apt, boolean wasInBranch) {
    }

    protected void onEval(APT apt, boolean result) {
    }

    protected APTPreprocHandler getPreprocHandler() {
        return preprocHandler;
    }
    
    protected APTIncludeHandler getIncludeHandler() {
        return (APTIncludeHandler)(getPreprocHandler() == null ? null: getPreprocHandler().getIncludeHandler());
    }   

    protected boolean needPPTokens() {
        return false;
    }
//    
//    @Override
//    public TokenStream getTokenStream() {
//        if (needPPTokens()) {
//            return new PPTokensWrapper(super.getTokenStream());
//        } else {
//            return super.getTokenStream();
//        }
//    }
 
    ////////////////////////////////////////////////////////////////////////////
    // implementation details
   
    private boolean eval(APT apt) {
        if (APTUtils.LOG.isLoggable(Level.FINE)) {
            APTUtils.LOG.log(Level.FINE, "eval condition for {0}", new Object[] {apt});// NOI18N
        }
        boolean res = false;
        try {
            Boolean cachedRes = cacheEntry != null ? cacheEntry.getEvalResult(apt) : null;
            if (cachedRes != null) {
                res = cachedRes.booleanValue();
            } else {
                res = APTConditionResolver.evaluate(apt, getMacroMap());
                if (cacheEntry != null) {
                    cacheEntry.setEvalResult(apt, res);
                }
            }
        } catch (TokenStreamException ex) {
            APTUtils.LOG.log(Level.SEVERE, "error on evaluating condition node {0}\n{1}", new Object[] { apt, ex });// NOI18N
        }
        onEval(apt, res);
        return res;
    }

    private void serialIncludeImpl(APTInclude aptInclude, ResolvedPath resolvedPath, IncludeState inclState) {
        PostIncludeData postIncludeData = cacheEntry.getPostIncludeState(aptInclude);
        if (postIncludeData.hasPostIncludeMacroState() && !hasIncludeActionSideEffects()) {
            getPreprocHandler().getMacroMap().setState(postIncludeData.getPostIncludeMacroState());
            return;
        }
        if (include(resolvedPath, inclState, aptInclude, postIncludeData)) {
            APTMacroMap.State postIncludeMacroState = getPreprocHandler().getMacroMap().getState();
            PostIncludeData newData = new PostIncludeData(postIncludeMacroState, postIncludeData.getDeadBlocks());
            cacheEntry.setIncludeData(aptInclude, newData);
        } else if (!postIncludeData.hasPostIncludeMacroState()) {
            // clean what could be set in dead blocks, because of false include activity
            postIncludeData.setDeadBlocks(null);
        }
    }

    private PPIncludeHandler.IncludeState beforeIncludeImpl(APTInclude aptInclude, ResolvedPath resolvedPath) {
        IncludeState inclState = pushInclude(aptInclude, resolvedPath);
        if (isTokenProducer() && needPPTokens()) {
            // put pre-include marker into token stream
            pushTokenStream(new TokenBasedTokenStream(new APTPreprocessorToken(aptInclude, Boolean.TRUE, inclState, resolvedPath, nodeProperties)));            
        }
        return inclState;
    }
    
    private void afterIncludeImpl(APTInclude aptInclude, ResolvedPath resolvedPath, IncludeState inclState) {
        if (isTokenProducer() && needPPTokens()) {
            APTPreprocessorToken afterInclToken = new APTPreprocessorToken(aptInclude, Boolean.FALSE, inclState, resolvedPath, nodeProperties);
            // put after-include marker into token stream
            // popInclude will be called before producing after-include marker token
            pushTokenStream(new AfterIncludeTokenStream(afterInclToken));
        } else {
            popInclude(aptInclude, resolvedPath, inclState);
        }
    }
    
    protected PPIncludeHandler.IncludeState pushInclude(APTInclude aptInclude, ResolvedPath resolvedPath) {
        PPIncludeHandler.IncludeState pushIncludeState = PPIncludeHandler.IncludeState.Fail;
        if (resolvedPath != null) {
            APTIncludeHandler includeHandler = getIncludeHandler();
            if (includeHandler != null) {
                pushIncludeState = includeHandler.pushInclude(resolvedPath.getFileSystem(), resolvedPath.getPath(), 
                        aptInclude.getToken().getLine(), aptInclude.getToken().getOffset(), resolvedPath.getIndex(), getCurIncludeDirectiveFileIndex());
            }
        }
//        System.out.println("\nPUSH from " + getCurFile() + " at Line " + aptInclude.getToken().getLine() + " " + pushIncludeState + ":" + resolvedPath);
        return pushIncludeState;
    }

    protected void popInclude(APTInclude aptInclude, ResolvedPath resolvedPath, IncludeState pushState) {
//        System.out.println("\nPOP  from " + getCurFile() + " at Line " + aptInclude.getToken().getLine() + " " + pushState + ":" + resolvedPath);
        if (pushState == IncludeState.Success) {
            APTIncludeHandler includeHandler = getIncludeHandler();
            if (includeHandler != null) {
                includeHandler.popInclude();
            }
        }
    }
    
    private final Map<APT, Map<Object, Object>> nodeProperties = new IdentityHashMap<APT, Map<Object, Object>>();

    protected final void putNodeProperty(APT node, Object key, Object value) {
        Map<Object, Object> props = nodeProperties.get(node);
        if (props == null) {
            nodeProperties.put(node, props = TinyMaps.createMap(2));
        } else {
            Map<Object, Object> expanded = TinyMaps.expandForNextKey(props, node);
            if (expanded != props) {
                // was replacement
                props = expanded;
                nodeProperties.put(node, props);
            }
        }
        props.put(key, value);
    }
    
    protected final Object getNodeProperty(APT node, Object key) {
        Map<Object, Object> props = nodeProperties.get(node);
        return props == null ? null : props.get(key);
    }
    
    protected final void includeStream(APTFile apt, APTWalker walker) {
        TokenStream incTS = walker.getTokenStream();
        pushTokenStream(incTS);
    }
    
    private final class AfterIncludeTokenStream implements TokenStream, APTTokenStream {

        private APTPreprocessorToken token;
        private boolean first;

        /**
         * Creates a new instance of TokenBasedTokenStream
         */
        public AfterIncludeTokenStream(APTPreprocessorToken token) {
            if (token == null) {
                throw new NullPointerException("not possible to create token stream for null token"); // NOI18N
            }
            this.token = token;
            this.first = true;
        }

        @Override
        public APTToken nextToken() {
            APTToken ret;
            if (first) {
                ret = token;
                first = false;
                invokePopInclude(token);
            } else {
                ret = APTUtils.EOF_TOKEN;
            }
            return ret;
        }

        @Override
        public String toString() {
            String retValue;

            retValue = "PPIS " + token.toString(); // NOI18N
            return retValue;
        }
        
        private void invokePopInclude(APTPreprocessorToken ppToken) {
            // pop include
            assert (ppToken.getProperty(Boolean.class) == Boolean.FALSE);
            IncludeState pushState = (IncludeState) ppToken.getProperty(IncludeState.class);
            APTInclude aptInclude = (APTInclude) ppToken.getProperty(APT.class);
            ResolvedPath resolvedPath = (ResolvedPath) ppToken.getProperty(ResolvedPath.class);
            popInclude(aptInclude, resolvedPath, pushState);
        }        
    }    
}
