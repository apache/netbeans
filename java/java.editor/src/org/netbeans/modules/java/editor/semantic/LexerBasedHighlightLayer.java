/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.java.editor.semantic;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.swing.text.AttributeSet;
import javax.swing.text.Document;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.java.editor.base.semantic.ColoringAttributes.Coloring;
import org.netbeans.spi.editor.highlighting.HighlightsSequence;
import org.netbeans.spi.editor.highlighting.support.AbstractHighlightsContainer;

/**
 *
 * @author Jan Lahoda
 */
public class LexerBasedHighlightLayer extends AbstractHighlightsContainer {
    
    private Map<Token, Coloring> colorings;
    private Map<Coloring, AttributeSet> CACHE = new HashMap<Coloring, AttributeSet>();
    private Document doc;

    public static LexerBasedHighlightLayer getLayer(Class id, Document doc) {
        LexerBasedHighlightLayer l = (LexerBasedHighlightLayer) doc.getProperty(id);
        
        if (l == null) {
            doc.putProperty(id, l = new LexerBasedHighlightLayer(doc));
        }
        
        return l;
    }
    
    private LexerBasedHighlightLayer(Document doc) {
        this.doc = doc;
        this.colorings = Collections.emptyMap();
    }
    
    public void setColorings(final Map<Token, Coloring> colorings, final Set<Token> addedTokens, final Set<Token> removedTokens) {
        doc.render(new Runnable() { // hopefully doc.render() is ok
            public void run() {
                synchronized (LexerBasedHighlightLayer.this) {
                    LexerBasedHighlightLayer.this.colorings = colorings;
                    
                    if (addedTokens.isEmpty()) {
                        //need to fire anything here?
                    } else {
                        if (addedTokens.size() < 30) {
                            int startOffset = Integer.MAX_VALUE;
                            int endOffset = -1;
                            for (Token t : addedTokens) {
                                int tOffset = t.offset(null);
                                startOffset = Math.min(tOffset, startOffset);
                                endOffset = Math.max(endOffset, tOffset + t.length());
                            }
                            fireHighlightsChange(startOffset, endOffset);
                        } else { // Too many tokens => repaint all
                            fireHighlightsChange(0, doc.getLength()); //XXX: locking
                        }
                    }
                }
            }
        });
    }
    
    public synchronized Map<Token, Coloring> getColorings() {
        return colorings;
    }
    
    public synchronized HighlightsSequence getHighlights(int startOffset, int endOffset) {
        if (colorings.isEmpty()) {
            return HighlightsSequence.EMPTY;
        }
        
        TokenHierarchy th = TokenHierarchy.get(doc);
        TokenSequence<? extends TokenId> seq = th.tokenSequence();
        if (seq == null) { // Null when token hierarchy is inactive
            return HighlightsSequence.EMPTY;
        }
        
        if (seq.language() == JavaTokenId.language()) {
            return new LexerBasedHighlightSequence(this, seq.subSequence(startOffset, endOffset), colorings);
        } else {
            return new EmbeddedLexerBasedHighlightSequence(this, seq.subSequence(startOffset, endOffset), colorings);
        }
    }

    public synchronized void clearColoringCache() {
        CACHE.clear();
    }
    
    synchronized AttributeSet getColoring(Coloring c) {
        AttributeSet a = CACHE.get(c);
        
        if (a == null) {
            CACHE.put(c, a = ColoringManager.getColoringImpl(c));
        }
        
        return a;
    }
}
