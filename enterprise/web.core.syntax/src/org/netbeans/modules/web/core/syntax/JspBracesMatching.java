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

package org.netbeans.modules.web.core.syntax;

import java.util.List;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.jsp.lexer.JspTokenId;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.spi.editor.bracesmatching.BracesMatcher;
import org.netbeans.spi.editor.bracesmatching.BracesMatcherFactory;
import org.netbeans.spi.editor.bracesmatching.MatcherContext;

/**
 * Super simple implementation of BracesMatcher. The performance is not good since
 * the logic break some rules defined in the SPI - findOrigin() method
 * is quite cost (uses two match searches) and the searches goes beyond the limited area.
 * Needs to be reimplemented later.
 *
 * @author Marek Fukala
 */
public class JspBracesMatching implements BracesMatcher, BracesMatcherFactory {

    private MatcherContext context;
    
    public JspBracesMatching() {
        this(null, null);
    }
    
    private JspBracesMatching(MatcherContext context, LanguagePath htmlLanguagePath) {
        this.context = context;
    }
    
    //use two searches to find the original area :-|
    public int[] findOrigin() throws InterruptedException, BadLocationException {
        ((AbstractDocument) context.getDocument()).readLock();
        try {
            JspSyntaxSupport syntaxSupport = JspSyntaxSupport.get(context.getDocument());
            int searchOffset = context.getSearchOffset();
            int[] found = syntaxSupport.findMatchingBlock(searchOffset, false);
            if(found == null) {
                return null;
            }
            int[] opposite = syntaxSupport.findMatchingBlock(found[0], false);
            return opposite;
        } finally {
            ((AbstractDocument) context.getDocument()).readUnlock();
        }
    }

    public int[] findMatches() throws InterruptedException, BadLocationException {
        ((AbstractDocument) context.getDocument()).readLock();
        try {
            JspSyntaxSupport syntaxSupport = JspSyntaxSupport.get(context.getDocument());
            int searchOffset = context.getSearchOffset();
            return syntaxSupport.findMatchingBlock(searchOffset, false);
        } finally {
            ((AbstractDocument) context.getDocument()).readUnlock();
        }
    }
    
    //BracesMatcherFactory implementation
    public BracesMatcher createMatcher(final MatcherContext context) {
        final JspBracesMatching[] ret = { null };
        context.getDocument().render(new Runnable() {
            public void run() {
                TokenHierarchy<Document> hierarchy = TokenHierarchy.get(context.getDocument());
                List<TokenSequence<?>> ets = hierarchy.embeddedTokenSequences(context.getSearchOffset(), context.isSearchingBackward());
                for (TokenSequence ts : ets) {
                    Language language = ts.language();
                    if (language == JspTokenId.language()) {
                        ret[0] = new JspBracesMatching(context, ts.languagePath());
                    }
                }
                // We might be trying to search at the end or beginning of a document. In which
                // case there is nothing to find and/or search through, so don't create a matcher.
                //        throw new IllegalStateException("No text/x-jsp language found on the MatcherContext's search offset! This should never happen!");
            }
        });
        return ret[0];
    }
    
}
