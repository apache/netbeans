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

package org.netbeans.modules.groovy.editor.language;

import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId;
import org.netbeans.modules.groovy.editor.api.lexer.LexUtilities;
import org.netbeans.spi.editor.bracesmatching.BracesMatcher;
import org.netbeans.spi.editor.bracesmatching.MatcherContext;

/**
 * Implementation of BracesMatcher interface for Groovy. It is based on original code
 * from BracketCompleter.findMatching
 *
 * @author Marek Slama
 */
public final class GroovyBracesMatcher implements BracesMatcher {

    MatcherContext context;

    public GroovyBracesMatcher (MatcherContext context) {
        this.context = context;
    }

    public int [] findOrigin() throws InterruptedException, BadLocationException {
        ((AbstractDocument) context.getDocument()).readLock();
        try {
            BaseDocument doc = (BaseDocument) context.getDocument();

            int offset = context.getSearchOffset();

            TokenSequence<GroovyTokenId> ts = LexUtilities.getGroovyTokenSequence(doc, offset);

            if (ts != null) {
                ts.move(offset);

                if (!ts.moveNext()) {
                    return null;
                }

                Token<GroovyTokenId> token = ts.token();

                if (token == null) {
                    return null;
                }
                
                TokenId id = token.id();
                
                if (id == GroovyTokenId.LPAREN) {
                    return new int [] { ts.offset(), ts.offset() + token.length() };
                } else if (id == GroovyTokenId.RPAREN) {
                    return new int [] { ts.offset(), ts.offset() + token.length() };
                } else if (id == GroovyTokenId.LBRACE) {
                    return new int [] { ts.offset(), ts.offset() + token.length() };
                } else if (id == GroovyTokenId.RBRACE) {
                    return new int [] { ts.offset(), ts.offset() + token.length() };
                } else if (id == GroovyTokenId.LBRACKET) {
                    return new int [] { ts.offset(), ts.offset() + token.length() };
                } else if (id == GroovyTokenId.RBRACKET) {
                    return new int [] { ts.offset(), ts.offset() + token.length() };
                }
            }
            return null;
        } finally {
            ((AbstractDocument) context.getDocument()).readUnlock();
        }
    }
    
    public int [] findMatches() throws InterruptedException, BadLocationException {
        ((AbstractDocument) context.getDocument()).readLock();
        try {
            BaseDocument doc = (BaseDocument) context.getDocument();
            
            int offset = context.getSearchOffset();

            TokenSequence<GroovyTokenId> ts = LexUtilities.getGroovyTokenSequence(doc, offset);

            if (ts != null) {
                ts.move(offset);

                if (!ts.moveNext()) {
                    return null;
                }

                Token<GroovyTokenId> token = ts.token();

                if (token == null) {
                    return null;
                }
                
                TokenId id = token.id();
                
                OffsetRange r;
                if (id == GroovyTokenId.LPAREN) {
                    r = LexUtilities.findFwd(doc, ts, GroovyTokenId.LPAREN, GroovyTokenId.RPAREN);
                    return new int [] {r.getStart(), r.getEnd() };
                } else if (id == GroovyTokenId.RPAREN) {
                    r = LexUtilities.findBwd(doc, ts, GroovyTokenId.LPAREN, GroovyTokenId.RPAREN);
                    return new int [] {r.getStart(), r.getEnd() };
                } else if (id == GroovyTokenId.LBRACE) {
                    r = LexUtilities.findFwd(doc, ts, GroovyTokenId.LBRACE, GroovyTokenId.RBRACE);
                    return new int [] {r.getStart(), r.getEnd() };
                } else if (id == GroovyTokenId.RBRACE) {
                    r = LexUtilities.findBwd(doc, ts, GroovyTokenId.LBRACE, GroovyTokenId.RBRACE);
                    return new int [] {r.getStart(), r.getEnd() };
                } else if (id == GroovyTokenId.LBRACKET) {
                    r = LexUtilities.findFwd(doc, ts, GroovyTokenId.LBRACKET, GroovyTokenId.RBRACKET);
                    return new int [] {r.getStart(), r.getEnd() };
                } else if (id == GroovyTokenId.RBRACKET) {
                    r = LexUtilities.findBwd(doc, ts, GroovyTokenId.LBRACKET, GroovyTokenId.RBRACKET);
                    return new int [] {r.getStart(), r.getEnd() };
                }
            }
            return null;
        } finally {
            ((AbstractDocument) context.getDocument()).readUnlock();
        }
    }
    
}
