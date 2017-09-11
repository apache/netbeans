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

import org.netbeans.modules.java.editor.base.semantic.ColoringAttributes;

import java.util.Map;

import javax.swing.text.AttributeSet;

import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.lexer.JavadocTokenId;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.java.editor.base.semantic.ColoringAttributes.Coloring;
import org.netbeans.spi.editor.highlighting.HighlightsSequence;

/**
 *
 * @author Jan Lahoda
 */
public class LexerBasedHighlightSequence implements HighlightsSequence {
    
    private LexerBasedHighlightLayer layer;
    private Map<Token, Coloring> colorings;
    private TokenSequence ts;
    private TokenSequence java;
    
    public LexerBasedHighlightSequence(LexerBasedHighlightLayer layer, TokenSequence ts, Map<Token, Coloring> colorings) {
        this.layer = layer;
        this.ts = ts;
        this.java = ts;
        this.colorings = colorings;
    }
    
    public boolean moveNext() {
        if (ts != java) {
            while (ts.moveNext()) {
                Token t = ts.token();
                if (t.id() == JavadocTokenId.IDENT && t.getProperty("javadoc-identifier") != null) { //NOI18N
                    return true;
        }
    }
            ts = java;
        }
        while (ts.moveNext()) {
            Token t = ts.token();
            if (t.id() == JavaTokenId.JAVADOC_COMMENT) {
                ts = ts.embedded();
                return moveNext();
            }
            if (t.id() == JavaTokenId.IDENTIFIER && colorings.containsKey(ts.token())) {
                return true;
        }
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
        if (ts.token().id() == JavadocTokenId.IDENT) {
            return layer.getColoring(ColoringAttributes.add(ColoringAttributes.empty(), ColoringAttributes.JAVADOC_IDENTIFIER));
        }
        return layer.getColoring(colorings.get(ts.token()));
    }
}
