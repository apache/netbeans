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
package org.netbeans.modules.php.blade.editor.lexer;

import java.util.List;
import javax.swing.text.Document;
import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.php.blade.editor.BladeLanguage;
import org.netbeans.modules.php.editor.lexer.PHPTokenId;

/**
 *
 * @author bogdan
 */
public final class BladeLexerUtils {

    private BladeLexerUtils() {

    }
    
    public static TokenSequence<PHPTokenId> getLockedPhpTokenSequence(Document doc, int offset) {
        BaseDocument baseDoc = (BaseDocument) doc;
        TokenSequence<PHPTokenId> tokenSequence = null;
        baseDoc.readLock();
        try {
            TokenHierarchy<Document> hierarchy = TokenHierarchy.get(baseDoc);
            tokenSequence = hierarchy.tokenSequence(PHPTokenId.language());
        } finally {
            baseDoc.readUnlock();
        }
        if (tokenSequence != null) {
            tokenSequence.move(offset);
            tokenSequence.moveNext();
        }
        return tokenSequence;

    }

    public static TokenSequence<? extends PHPTokenId> getPhpTokenSequence(TokenHierarchy<Document> th, final int offset) {
        return getTokenSequence(th, offset, PHPTokenId.language());
    }
    

    public static TokenSequence<? extends PHPTokenId> getPhpTokenSequence(final Document document, final int offset) {
        TokenHierarchy<Document> th = TokenHierarchy.get(document);
        return getTokenSequence(th, offset, PHPTokenId.language());
    }

    public static TokenSequence<? extends HTMLTokenId> getHtmlTokenSequence(TokenHierarchy<?> th, final int offset) {
        return getTokenSequence(th, offset, HTMLTokenId.language());
    }

    public static TokenSequence<? extends HTMLTokenId> getHtmlTokenSequence(final Document document, final int offset) {
        TokenHierarchy<Document> th = TokenHierarchy.get(document);
        return getTokenSequence(th, offset, HTMLTokenId.language());
    }

    public static Token<? extends HTMLTokenId> getHtmlToken(TokenHierarchy<?> th, final int offset) {
        TokenSequence<? extends HTMLTokenId> tsHtml = BladeLexerUtils.getHtmlTokenSequence(th, offset);
        if (tsHtml == null) {
            return null;
        }
        tsHtml.move(offset);

        if (!tsHtml.moveNext() && !tsHtml.movePrevious()) {
            return null;
        }

        return tsHtml.token();
    }

    public static TokenSequence<BladeTokenId> getTokenSequence(final Document document, final int offset) {
        return getBladeTokenSequence(TokenHierarchy.get(document), offset);
    }

    public static TokenSequence<BladeTokenId> getBladeTokenSequence(TokenHierarchy<?> th, int offset) {
        BladeLanguage lang = new BladeLanguage();
        TokenSequence<BladeTokenId> ts = th.tokenSequence(lang.getLexerLanguage());

        return ts;
    }

    public static TokenSequence<BladeTokenId> getBladeTokenSequenceDoc(TokenHierarchy<?> th, int offset) {
        BladeLanguage lang = new BladeLanguage();
        TokenSequence<BladeTokenId> ts = th.tokenSequence(lang.getLexerLanguage());

        return ts;
    }
    
    public static Token<BladeTokenId> getBladeToken(final Document document, final int offset){
        TokenSequence<BladeTokenId> ts = getTokenSequence(document, offset);
        return getBladeToken(ts, offset);
    }
    
    public static Token<BladeTokenId> getBladeToken(TokenHierarchy<?> th, final int offset){
        TokenSequence<BladeTokenId> ts = getBladeTokenSequence(th, offset);
        return getBladeToken(ts, offset);
    }
    
    public static Token<BladeTokenId> getBladeToken(TokenSequence<BladeTokenId> ts, final int offset){
        if (ts == null){
            return null;
        }
        
        ts.move(offset);
        
        if (!ts.moveNext() && !ts.movePrevious()) {
            return null;
        }
        
        return ts.token();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <T> TokenSequence<? extends T> getTokenSequence(final TokenHierarchy<?> th,
            final int offset, final Language language) {
        TokenSequence<? extends T> ts = th.tokenSequence(language);
        if (ts == null) {
            List<TokenSequence<?>> list = th.embeddedTokenSequences(offset, true);
            for (TokenSequence t : list) {
                if (t.language() == language) {
                    ts = t;
                    break;
                }
            }
            if (ts == null) {
                list = th.embeddedTokenSequences(offset, false);
                for (TokenSequence t : list) {
                    if (t.language() == language) {
                        ts = t;
                        break;
                    }
                }
            }
        }
        return ts;
    }

}
