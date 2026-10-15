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
package org.netbeans.modules.css.editor.module.spi;

import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.csl.api.CodeCompletionHandler.QueryType;
import org.netbeans.modules.css.lib.api.CssParserResult;
import org.netbeans.modules.css.lib.api.CssTokenId;
import org.netbeans.modules.css.lib.api.CssTokenIdCategory;
import org.netbeans.modules.css.lib.api.Node;
import org.netbeans.modules.css.lib.api.NodeUtil;

/**
 * A code completion context. An instance of this class is passed to the CssModule.getCompletionProposals() method.
 * The context is basically parser result based and contains lots of useful information for
 * the completion result providers.
 *
 * @author mfukala@netbeans.org
 */
public class CompletionContext extends EditorFeatureContext {

    private final QueryType queryType;
    private final int anchorOffset, embeddedCaretOffset, embeddedAnchorOffset, activeTokenDiff;
    private final String prefix;
    private final Node activeNode;
    private final TokenSequence<CssTokenId> tokenSequence;
    private final Node activeTokenNode;
    private final int tsIndex;
    private final String sourceFileMimetype;

    /**
     * @doto use class accessor so clients cannot instantiate this.
     */
    public CompletionContext(Node activeNode, Node activeTokeNode, CssParserResult result, 
            TokenSequence<CssTokenId> tokenSequence, int tsIndex, int activeTokenDiff, 
            QueryType queryType, int caretOffset, int anchorOffset, int embeddedCaretOffset, 
            int embeddedAnchorOffset, String prefix, String sourceFileMimetype) {
        super(result, caretOffset);
        this.tokenSequence = tokenSequence;
        this.tsIndex = tsIndex;
        this.activeNode = activeNode;
        this.activeTokenNode = activeTokeNode;
        this.queryType = queryType;
        this.anchorOffset = anchorOffset;
        this.embeddedCaretOffset = embeddedCaretOffset;
        this.embeddedAnchorOffset = embeddedAnchorOffset;
        this.prefix = prefix;
        this.activeTokenDiff = activeTokenDiff;
        this.sourceFileMimetype = sourceFileMimetype;
    }

    /**
     * Gets mimetype of the fileobject if there's any, otherwise returns null.
     * 
     * @return the source file mimetype.
     */
    public String getSourceFileMimetype() {
        return sourceFileMimetype;
    }

    /**
     * Decides whether the CompletionContext belongs to a css preprocessor (SASS or LESS) source. 
     * 
     * Bit hacky - would be nice to have a SPI for that.
     * @return 
     */
    public boolean isCssPreprocessorSource() {
        return "text/scss".equals(sourceFileMimetype) || "text/less".equals(sourceFileMimetype);
    }
    
    /**
     * 
     * @return a TokenSequence of Css tokens created on top of the *virtual* css source.
     * The TokenSequence is positioned on a token laying at the getAnchorOffset() offset.
     * 
     * Clients using the context must reposition the token sequence back to the original state
     * before exiting!
     * @todo - ensure this automatically or at least check it
     */
    @Override
    public TokenSequence<CssTokenId> getTokenSequence() {
        return tokenSequence;
    }
    
    /**
     * @return the top most parse tree node for the getEmbeddedCaretOffset() position. 
     * The node is never of the NodeType.token type and is typically obtained by
     * getActiveTokenNode().parent().
     */
    public Node getActiveNode() {
        return activeNode;
    }
    
    /**
     * Gets the {@link CssTokenId} of the {@link Token} at the caret offset.
     * 
     * @since 1.51
     * @return the token id or null if no token can be achieved.
     */
    public CssTokenId getActiveTokenId() {
        try {
            TokenSequence<CssTokenId> ts = getTokenSequence();
            return ts.token() == null ? null : ts.token().id();
        } finally {
            restoreTokenSequence();
        }
    }
    
    /**
     * If the current token is WS, then this method scans tokens bacwards until it finds
     * a non white token. Then it finds corresponding parse tree node for the end offset
     * of the found non white token.
     * 
     * @since 1.51
     * @return the found node or root node, never null
     */
    @NonNull
    public Node getNodeForNonWhiteTokenBackward() {
        TokenSequence<CssTokenId> ts = getTokenSequence();
        restoreTokenSequence();
        try {
            for(;;) {
                Token t = ts.token();
                if(t == null) {
                    //empty file
                    return getParseTreeRoot();
                }
                if(!CssTokenIdCategory.WHITESPACES.name().equalsIgnoreCase(t.id().primaryCategory())) {
                    return NodeUtil.findNonTokenNodeAtOffset(getParseTreeRoot(), ts.offset() + t.length());
                } else {
                    if(!ts.movePrevious()) {
                        break;
                    }
                }
            }
            return getParseTreeRoot();
        } finally {
            //reposition the token sequence back
            restoreTokenSequence();
        }
    }

    /**
     * If the current token is WS, then this method scans tokens bacwards until it finds
     * a non white token. 
     * 
     * @since 1.57
     * @return the non-white token id or null if there isn't any.
     */
    public CssTokenId getNonWhiteTokenIdBackward() {
        TokenSequence<CssTokenId> ts = getTokenSequence();
        restoreTokenSequence();
        try {
            for(;;) {
                Token<CssTokenId> t = ts.token();
                if(t == null) {
                    //empty file
                    return null;
                }
                if(!CssTokenIdCategory.WHITESPACES.name().equalsIgnoreCase(t.id().primaryCategory())) {
                    return t.id();
                } else {
                    if(!ts.movePrevious()) {
                        break;
                    }
                }
            }
            return null;
        } finally {
            //reposition the token sequence back
            restoreTokenSequence();
        }
    }
    
    /**
     * Restores the {@link TokenSequence} obtained by {@link #getTokenSequence()} to the original state.
     * @since 1.51
     */
    public void restoreTokenSequence() {
        getTokenSequence().moveIndex(tsIndex);
        getTokenSequence().moveNext();
    }
    
    /**
     * @return the top most parse tree node of NodeType.token for the getEmbeddedCaretOffset() position
     */
    public Node getActiveTokenNode() {
        return activeTokenNode;
    }
    
    /**
     * anchor offset = caret offset - prefix length.
     * Relative to the edited document.
     * 
     */
    public int getAnchorOffset() {
        return anchorOffset;
    }

    /**
     * Same as getCaretOffset() but relative to the embedded css code.
     */
    public int getEmbeddedCaretOffset() {
        return embeddedCaretOffset;
    }

    /**
     * Same as getAnchorOffset() but relative to the embedded css code.
     */
    public int getEmbeddedAnchorOffset() {
        return embeddedAnchorOffset;
    }

    /**
     * @return computed completion prefix. Some clients may ignore this and compute
     * the prefix themselves. In such case the resulting CompletionProposal-s needs 
     * to use non-default anchor.
     */
    public String getPrefix() {
        return prefix;
    }

    /**
     * @return QueryType of the completion query.
     */
    public QueryType getQueryType() {
        return queryType;
    }

    /**
     * @return a diff from the getEmbeddedCaretOffset() and the token found for the position. 
     * Is result of getTokenSequence().move(getEmbeddedCaretOffset());
     */
    public int getActiveTokenDiff() {
        return activeTokenDiff;
    }
    
}
