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

import java.util.Map;
import java.util.Stack;
import javax.swing.text.AttributeSet;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.java.editor.base.semantic.ColoringAttributes.Coloring;
import org.netbeans.spi.editor.highlighting.HighlightsSequence;

/**
 *
 * @author Jan Lahoda
 */
public class EmbeddedLexerBasedHighlightSequence implements HighlightsSequence {
    
    private LexerBasedHighlightLayer layer;
    private Map<Token, Coloring> colorings;
    private Stack<TokenSequence> stack;
    private TokenSequence ts;
    private boolean started;
    
    public EmbeddedLexerBasedHighlightSequence(LexerBasedHighlightLayer layer, TokenSequence ts, Map<Token, Coloring> colorings) {
        this.layer = layer;
        this.ts = ts;
        this.colorings = colorings;
        
        this.stack = new Stack<TokenSequence>();
    }
    
    private boolean moveNextImpl2() {
        if (started) {
            return ts.moveNext();
        } else {
            started = true;
            
            return ts.moveNext();
        }
    }

    private boolean moveNextImpl() {
        if (moveNextImpl2()) {
            TokenSequence tseq = ts.embedded();
            
            if (tseq != null) {
                stack.push(ts);
                ts = tseq;
            }
            
            return true;
        }
        
        if (stack.isEmpty()) {
            return false;
        }
        
        ts = stack.pop();
        
        return moveNextImpl();
    }
    
    public boolean moveNext() {
        while (moveNextImpl()) {
            Token t = ts.token();
            if (t != null && t.id() == JavaTokenId.IDENTIFIER && colorings.containsKey(ts.token()))
                return true;
        }
        
        return false;
    }
    
    public int getStartOffset() {
        return ts.offset();
    }

    public int getEndOffset() {
        return ts.offset() + ts.token().length();
    }

    public AttributeSet getAttributes() {
        return layer.getColoring(colorings.get(ts.token()));
    }

}
