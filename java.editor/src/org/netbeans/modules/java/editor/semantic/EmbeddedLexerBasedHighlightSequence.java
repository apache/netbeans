/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
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
