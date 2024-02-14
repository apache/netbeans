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

package org.netbeans.modules.web.indent.api;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.web.indent.api.embedding.JoinedTokenSequence;
import org.netbeans.modules.web.indent.api.embedding.JoinedTokenSequence.TokenSequenceWrapper;
import org.netbeans.modules.web.indent.api.embedding.VirtualSource;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;

/**
 *
 * @since org.netbeans.modules.css.editor/1 1.3
 */
public final class LexUtilities {

    private LexUtilities(){};

    /** Find given language token sequence (in case it's embedded in something else at the top level */
    public static <T extends TokenId> TokenSequence<T> getTokenSequence(BaseDocument doc, int offset, Language<T> language) {
        TokenHierarchy<Document> th = TokenHierarchy.get((Document)doc);
        return getTokenSequence(th, offset, language);
    }

    /** Find given language token sequence (in case it's embedded in something else at the top level */
    @SuppressWarnings( "unchecked" )
    public static <T extends TokenId>  TokenSequence<T> getTokenSequence(TokenHierarchy<Document> th, int offset, Language<T> language) {
        TokenSequence<T> ts = th.tokenSequence(language);

        if (ts == null) {
            // Possibly an embedding scenario such as an RHTML file
            // First try with backward bias true
            List<TokenSequence<?>> list = th.embeddedTokenSequences(offset, true);

            for (TokenSequence<?> t : list) {
                if (t.language() == language) {
                    ts = (TokenSequence<T>)t;

                    break;
                }
            }

            if (ts == null) {
                list = th.embeddedTokenSequences(offset, false);

                for (TokenSequence<?> t : list) {
                    if (t.language() == language) {
                        ts = (TokenSequence<T>)t;

                        break;
                    }
                }
            }
        }

        return ts;
    }

    public static Language<? extends TokenId> getLanguage(BaseDocument doc, int offset) {
        TokenHierarchy<BaseDocument> th = TokenHierarchy.get(doc);
        return getLanguage(th, offset);
    }

    public static Language<? extends TokenId> getLanguage(TokenHierarchy<BaseDocument> th, int offset) {
        List<TokenSequence<?>> list = th.embeddedTokenSequences(offset, true);

        if (list.isEmpty()) {
            return null;
        }
        return list.get(list.size()-1).language();
    }

    public static <T extends TokenId> TokenSequence<T> getPositionedSequence(BaseDocument doc, int offset, Language<T> language) {
        return getPositionedSequence(doc, offset, true, language);
    }

    public static <T extends TokenId>  TokenSequence<T> getPositionedSequence(BaseDocument doc, int offset, boolean lookBack, Language<T> language) {
        TokenSequence<T> ts = getTokenSequence(doc, offset, language);

        if (ts != null) {
            try {
                ts.move(offset);
            } catch (AssertionError e) {
                DataObject dobj = (DataObject)doc.getProperty(Document.StreamDescriptionProperty);

                if (dobj != null) {
                    Exceptions.attachMessage(e, FileUtil.getFileDisplayName(dobj.getPrimaryFile()));
                }

                throw e;
            }

            if (!lookBack && !ts.moveNext()) {
                return null;
            } else if (lookBack && !ts.moveNext() && !ts.movePrevious()) {
                return null;
            }

            return ts;
        }

        return null;
    }

    public static <T extends TokenId> Token<T> findNext(TokenSequence<T> ts, List<T> ignores) {
        if (ignores.contains(ts.token().id())) {
            while (ts.moveNext() && ignores.contains(ts.token().id())) {}
        }
        if (ts.token() == null || ignores.contains(ts.token().id())) {
            return null;
        }
        return ts.token();
    }

    public static <T extends TokenId> Token<T> findNext(JoinedTokenSequence<T> ts, List<T> ignores) {
        if (ignores.contains(ts.token().id())) {
            while (ts.moveNext() && ignores.contains(ts.token().id())) {}
        }
        if (ts.token() == null || ignores.contains(ts.token().id())) {
            return null;
        }
        return ts.token();
    }

    public static <T extends TokenId> Token<T> findPrevious(TokenSequence<T> ts, List<T> ignores) {
        if (ignores.contains(ts.token().id())) {
            while (ts.movePrevious() && ignores.contains(ts.token().id())) {}
        }
        if (ts.token() == null || ignores.contains(ts.token().id())) {
            return null;
        }
        return ts.token();
    }

    public static <T extends TokenId> Token<T> findPrevious(JoinedTokenSequence<T> ts, List<T> ignores) {
        if (ignores.contains(ts.token().id())) {
            while (ts.movePrevious() && ignores.contains(ts.token().id())) {}
        }
        if (ts.token() == null || ignores.contains(ts.token().id())) {
            return null;
        }
        return ts.token();
    }

    private static <T extends TokenId> TokenSequence<T> getVirtualTokens(
            VirtualSource virtualSource,
            int startOffset, int endOffset, Language<T> language) {
        if (virtualSource == null) {
            return null;
        }
        String source = virtualSource.getSource(startOffset, endOffset);
        if (source == null || source.length() == 0) {
            return null;
        }
        return TokenHierarchy.create(source, language).tokenSequence(language);
    }

    public static int getTokenSequenceEndOffset(TokenSequence<? extends TokenId> ts) {
        int currentIndex = ts.index();
        ts.moveEnd();
        ts.movePrevious();
        int offset = ts.offset() + ts.token().length();
        ts.moveIndex(currentIndex);
        return offset;
    }

    public static int getTokenSequenceEndOffset(JoinedTokenSequence<? extends TokenId> ts) {
        int[] currentIndex = ts.index();
        ts.moveEnd();
        ts.movePrevious();
        int offset = ts.offset() + ts.token().length();
        ts.moveIndex(currentIndex);
        return offset;
    }

    public static int getTokenSequenceStartOffset(TokenSequence<? extends TokenId> ts) {
        int currentIndex = ts.index();
        ts.moveStart();
        ts.moveNext();
        int offset = ts.offset();
        ts.moveIndex(currentIndex);
        return offset;
    }

    public static int getTokenSequenceStartOffset(JoinedTokenSequence<? extends TokenId> ts) {
        int[] currentIndex = ts.index();
        ts.moveStart();
        ts.moveNext();
        int offset = ts.offset();
        ts.moveIndex(currentIndex);
        return offset;
    }

    public static <T1 extends TokenId> Token<T1> getTokenAtOffset(JoinedTokenSequence<T1> ts, int offset) {
        int[] currentIndex = ts.index();
        ts.move(offset);
        ts.moveNext();
        Token<T1> t = ts.token();
        ts.moveIndex(currentIndex);
        return t;
    }

    @SuppressWarnings( "unchecked" )
    public static <T1 extends TokenId> List<TokenSequence<T1>> getEmbeddedTokenSequences(BaseDocument doc, Language<T1> language, int start, int end) {
        TokenHierarchy<BaseDocument> th = TokenHierarchy.get(doc);
        List<LanguagePath> lps = new ArrayList<LanguagePath>();
        for (LanguagePath lp : th.languagePaths()) {
            if (lp.endsWith(LanguagePath.get(language))) {
                lps.add(lp);
            }
        }
        List<TokenSequence<T1>> tss = new ArrayList<TokenSequence<T1>>();
        for (LanguagePath lp : lps) {
            List<TokenSequence<?>> tss2 = th.tokenSequenceList(lp, start, end);
            if (tss2 != null) {
                for (TokenSequence<?> ts2 : tss2) {
                    ts2.moveStart();
                    if (ts2.moveNext()) {
                        tss.add((TokenSequence<T1>)ts2);
                    }
                }
            }
        }
        tss.sort(new Comparator<TokenSequence<T1>>() {
            @Override
            public int compare(TokenSequence<T1> o1, TokenSequence<T1> o2) {
                assert o1.offset() != o2.offset() : "should never have two equal TokenSequence " + o1.toString() + " " +  o2.language().toString(); //NOI18N
                return o1.offset() - o2.offset();
            }
        });
        return tss;
    }

    private static <T1 extends TokenId> List<JoinedTokenSequence.CodeBlock<T1>> calculateCodeBlock(List<TokenSequence<T1>> tss,
            VirtualSource virtualSource
        ) throws BadLocationException {

        List<JoinedTokenSequence.CodeBlock<T1>> blocks = new ArrayList<JoinedTokenSequence.CodeBlock<T1>>();

        for (int i=0; i<tss.size(); i++) {
            TokenSequence<T1> ts =tss.get(i);

            List<TokenSequenceWrapper<T1>> tss2 = new ArrayList<TokenSequenceWrapper<T1>>();
            tss2.add(new TokenSequenceWrapper<T1>(ts, false));

            // try to find additional token sequences which comprise this language block:
            for (int j=i+1; j<tss.size(); j++) {
                TokenSequence<T1> prev = tss.get(j-1);
                prev.moveEnd();
                prev.movePrevious();
                TokenSequence<T1> next = tss.get(j);
                next.moveStart();
                next.moveNext();
                // check whether current token sequence is continuation of previous one:
                TokenSequence<T1> tsVirtual = LexUtilities.getVirtualTokens(virtualSource, prev.offset()+prev.token().length(), next.offset(), ts.language());
                if (tsVirtual != null) {
                    tss2.add(new TokenSequenceWrapper<T1>(tsVirtual, true));
                    tss2.add(new TokenSequenceWrapper<T1>(next, false));
                    i++;
                } else {
                    break;
                }
            }


            blocks.add(new JoinedTokenSequence.CodeBlock<T1>(tss2));
        }

        return blocks;
    }

    public static <T1 extends TokenId> List<JoinedTokenSequence.CodeBlock<T1>> createCodeBlocks(
            BaseDocument doc, Language<T1> language, VirtualSource virtualSource) throws BadLocationException {
        List<TokenSequence<T1>> tss = LexUtilities.getEmbeddedTokenSequences(doc, language, 0, doc.getLength());
        if (tss.isEmpty()) {
            return null;
        }
        return LexUtilities.calculateCodeBlock(tss, virtualSource);
    }

}
