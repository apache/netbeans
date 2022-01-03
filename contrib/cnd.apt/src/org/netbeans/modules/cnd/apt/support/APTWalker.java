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

import org.netbeans.modules.cnd.antlr.TokenStream;
import org.netbeans.modules.cnd.antlr.TokenStreamException;
import java.util.LinkedList;
import java.util.logging.Level;
import org.netbeans.modules.cnd.apt.structure.APT;
import org.netbeans.modules.cnd.apt.structure.APTFile;
import org.netbeans.modules.cnd.apt.structure.APTStream;
import org.netbeans.modules.cnd.apt.utils.APTTraceUtils;
import org.netbeans.modules.cnd.apt.utils.APTUtils;

/**
 * base Tree walker for APT
 */
public abstract class APTWalker {
    private final APTMacroMap macros;
    private final APTFile root;
    // walker can be used only in one of modes: produce TokenStream or visit nodes
    private Boolean walkerUsedForTokenStreamGeneration = null; 
    private boolean stopped = false;
    
    /**
     * Creates a new instance of APTWalker
     */
    public APTWalker(APTFile apt, APTMacroMap macros) {
        assert (apt != null) : "how can we work on null tree?"; // NOI18N
        this.root = apt;
        this.macros = macros;
    }
    
    /** fast visit APT without generating token stream */
    public void visit() {
        // non Recurse Visit
        init(false);
        while (!finished()) {
            toNextNode();
        }
        onEOF();
    }
    
    public TokenStream getTokenStream() {
        return new WalkerTokenStream();
    }

    protected final boolean isTokenProducer() {
        return walkerUsedForTokenStreamGeneration == Boolean.TRUE;
    }

    private final class WalkerTokenStream implements TokenStream, APTTokenStream {
        private WalkerTokenStream() {
            init(true);
        }
        
        @Override
        public APTToken nextToken() {
            try {
                return nextTokenImpl();
            } catch (TokenStreamException ex) {
                APTUtils.LOG.log(Level.WARNING, "{0}:{1}", new Object[] { curFile.getPath(), ex});
                return APTUtils.EOF_TOKEN;
            }
        }
    }    
    
    ////////////////////////////////////////////////////////////////////////////
    // template methods to override by extensions 

    protected void onInclude(APT apt) {
        assert curIncludeDirectiveFileIndex != INVALID_INDEX : "must not be invalid ";
        curIncludeDirectiveFileIndex++;
    }

    protected void onIncludeNext(APT apt) {
        assert curIncludeDirectiveFileIndex != INVALID_INDEX : "must not be invalid ";
        curIncludeDirectiveFileIndex++;
    }
        
    protected abstract void onDefine(APT apt);
    
    protected abstract void onUndef(APT apt);
    
    // preproc conditions
    
    /*
     * @return true if infrastructure can continue visiting children
     *         false if infrastructure should not visit children
     */
    protected abstract boolean onIf(APT apt);
    
    /*
     * @return true if infrastructure can continue visiting children
     *         false if infrastructure should not visit children
     */
    protected abstract boolean onIfdef(APT apt);
    
    /*
     * @return true if infrastructure can continue visiting children
     *         false if infrastructure should not visit children
     */
    protected abstract boolean onIfndef(APT apt);
    
    /*
     * @return true if infrastructure can continue visiting children
     *         false if infrastructure should not visit children
     */
    protected abstract boolean onElif(APT apt, boolean wasInPrevBranch);
    
    /*
     * @return true if infrastructure can continue visiting children
     *         false if infrastructure should not visit children
     */
    protected abstract boolean onElse(APT apt, boolean wasInPrevBranch);
    
    protected abstract void onEndif(APT apt, boolean wasInBranch);

     /**
     * Callback for #pragma node.
     */
    protected abstract void onPragmaNode(APT apt);

    // callback for stream node
    protected void onStreamNode(APT apt) {
        // do nothing
    }
    
    /**
     * Callback for #error node.
     */
    protected void onErrorNode(APT apt) {
        // do nothing
    }

    protected void onOtherNode(APT apt) {
        // do nothing
    }

    protected void onEOF() {
        // do nothing
    }
    
    /**
     * Determines whether the walker should stop or proceed
     * as soon as it encounteres #error directive
     *
     * @return true if the walker should stop on #error directive,
     * otherwise false
     */
    protected boolean stopOnErrorDirective() {
	return true;
    }
     
    ////////////////////////////////////////////////////////////////////////////
    // impl details    
    
    protected boolean onAPT(APT node, boolean wasInBranch) {
        boolean visitChild = false;
        switch(node.getType()) {
            case APT.Type.IF:
                visitChild = onIf(node);
                break;
            case APT.Type.IFDEF:
                visitChild = onIfdef(node);
                break;
            case APT.Type.IFNDEF:
                visitChild = onIfndef(node);
                break;
            case APT.Type.ELIF:
                visitChild = onElif(node, wasInBranch);
                break;
            case APT.Type.ELSE:
                visitChild = onElse(node, wasInBranch);
                break;
            case APT.Type.ENDIF:
                onEndif(node, wasInBranch);
                break;
            case APT.Type.DEFINE:
                onDefine(node);
                break;
            case APT.Type.UNDEF:
                onUndef(node);
                break;
            case APT.Type.INCLUDE:
                onInclude(node);
                break;
            case APT.Type.INCLUDE_NEXT:
                onIncludeNext(node);
                break;
            case APT.Type.TOKEN_STREAM:
                onStreamNode(node);
                break;
            case APT.Type.ERROR:
		onErrorNode(node);
		break;
            case APT.Type.PRAGMA:
                onPragmaNode(node);
                break;
            case APT.Type.INVALID:
            case APT.Type.LINE:
            case APT.Type.PREPROC_UNKNOWN:   
                onOtherNode(node);
                break;
            default:
                assert(false) : "unsupported " + APTTraceUtils.getTypeName(node); // NOI18N
        }
        if (APTUtils.LOG.isLoggable(Level.FINE)) {
            APTUtils.LOG.log(Level.FINE, "onAPT: {0}; {1} {2}",  // NOI18N
                    new Object[]    {
                                    node,
                                    (wasInBranch ? "Was before;" : ""), // NOI18N
                                    (visitChild ? "Will visit children" : "") // NOI18N
                                    }
                            );
        }
        fillTokensIfNeeded(node);
        return visitChild;
    }
    
    protected final void pushState() {
        visits.addLast(new WalkerState(curFile, curAPT, curWasInChild));
    }

    protected void preInit() {}

    private boolean popState() {
        if (visits.isEmpty()) {
            return false;
        }
        WalkerState state = visits.removeLast();
        curFile = state.lastFile;
        curAPT = state.lastNode;
        curWasInChild = state.wasInChild;
        return true;
    }
    
    private void init(boolean needStream) {
        if (walkerUsedForTokenStreamGeneration != null) {
            throw new IllegalStateException("walker could be used only once"); // NOI18N
        }
        walkerUsedForTokenStreamGeneration = Boolean.valueOf(needStream);
        // init index before preInit call which might cause "-include file" handling
        assert curIncludeDirectiveFileIndex == INVALID_INDEX : "must be invalid " + curIncludeDirectiveFileIndex;
        curIncludeDirectiveFileIndex = EMPTY_INDEX;
        preInit();
        curFile = root;
        curAPT = curFile.getFirstChild();
        curWasInChild = false;
        pushState();
    }    
    
    private APTToken nextTokenImpl() throws TokenStreamException {
        APTToken theRetToken;
        tokenLoop:
        for (;;) {           
            while (!tokens.isEmpty()) {
                TokenStream ts = tokens.peek();
                theRetToken = (APTToken) ts.nextToken();
                if (!APTUtils.isEOF(theRetToken)) {
                    return theRetToken;
                } else {
                    tokens.removeFirst();
                }
            }
            if (finished()) {
                onEOF();
                return APTUtils.EOF_TOKEN;
            } else {        
                toNextNode();
            }
        }
    }
    
    private void toNextNode() {
        popState();
        if (finished()) {
            return;
        }
        if (curAPT == null) {
            // we are in APT of incomplete file
            APTUtils.LOG.log(Level.SEVERE, "incomplete APT {0}", new Object[] { curFile });// NOI18N
            do {
                popState();
                if (curAPT != null) {
                    curAPT = curAPT.getNextSibling();
                }
            } while (curAPT == null && !finished());
            
            if (curAPT == null) {
                return;
            }
        }
        if (APTUtils.isStartConditionNode(curAPT.getType())) {
            curWasInChild = false;
        }

        // allow any actions in extension for the current node
        boolean visitChild = onAPT(curAPT, curWasInChild);
        curWasInChild |= visitChild;            
        if (visitChild) {          
            // move on next node to visit
            assert(APTUtils.isStartOrSwitchConditionNode(curAPT.getType()));   
            if (curAPT.getFirstChild() != null) {
                // push to have possibility move on it's sibling after visited children
                pushState();
                // node has children which are not yet visited
                curAPT = curAPT.getFirstChild();
                curWasInChild = false;
            } else {
                // move on sibling, as cur node has empty children
                curAPT = curAPT.getNextSibling();
            }
        } else {
            if (curAPT.getType() == APT.Type.ENDIF) {
                // end of condition block
            } else if( curAPT.getType() == APT.Type.ERROR ) {
		if (stopOnErrorDirective()) {
		    stop();
		    return;
		}
	    } else if( curAPT.getType() == APT.Type.PRAGMA && isStopped()) {
                if (stopOnErrorDirective()) {
                    return;
                }
            }
            curAPT = curAPT.getNextSibling();
            while (curAPT == null && !finished()) {
                popState();
                curAPT = curAPT.getNextSibling();
            }
        }
        if (!finished()) {
            pushState();
        }
    }
    
    private void fillTokensIfNeeded(APT node) {
        if (isTokenProducer()) {
            // only token stream nodes contain tokens as TokenStream
            if (node != null && node.getType() == APT.Type.TOKEN_STREAM) {
                pushTokenStream(((APTStream)node).getTokenStream());
            }
        }
    }
    
    protected final void pushTokenStream(TokenStream ts) {
        tokens.add(ts);
    }
    
    private boolean finished() {
        return (curAPT == null && visits.isEmpty()) || isStopped();
    }
    
    protected final APTMacroMap getMacroMap() {
        return macros;
    }
    
    protected final APTFile getCurFile() {
        return curFile;
    }
    
    protected final APT getCurNode() {
        return curAPT;
    }

    protected final int getCurIncludeDirectiveFileIndex() {
        assert curIncludeDirectiveFileIndex != INVALID_INDEX : "must not be invalid ";
        return curIncludeDirectiveFileIndex;
    }

    protected final APTFile getRootFile() {
        return root;
    }
    
    // fields to be used when generating token stream
    private APTFile curFile;
    private APT curAPT;
    private boolean curWasInChild;
    private static final int INVALID_INDEX = -2;
    private static final int EMPTY_INDEX = -1;
    private int curIncludeDirectiveFileIndex = INVALID_INDEX;
    private LinkedList<TokenStream> tokens = new LinkedList<TokenStream>();
    private LinkedList<WalkerState> visits = new LinkedList<WalkerState>();
    
    private static final class WalkerState {
        private final APT lastNode;
        private final APTFile lastFile;
        private final boolean wasInChild;
        private WalkerState(APTFile file, APT node, boolean wasInChildState) {
            this.lastFile = file;
            this.lastNode = node;
            this.wasInChild = wasInChildState;
        }
    }     

    protected boolean isStopped() {
        return stopped;
    }

    protected final void stop() {
        this.stopped = true;
    }
}
