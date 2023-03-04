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
package org.netbeans.modules.html.editor;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.html.editor.api.HtmlKit;
import org.netbeans.modules.html.editor.api.Utils;
import org.netbeans.modules.html.editor.api.gsf.HtmlParserResult;
import org.netbeans.modules.html.editor.lib.api.elements.*;
import org.netbeans.modules.html.editor.lib.api.model.HtmlModel;
import org.netbeans.modules.html.editor.lib.api.model.HtmlModelFactory;
import org.netbeans.modules.html.editor.lib.api.model.HtmlTag;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser.Result;
import org.netbeans.modules.web.common.api.LexerUtils;
import org.netbeans.modules.web.common.api.WebUtils;
import org.netbeans.spi.editor.bracesmatching.BracesMatcher;
import org.netbeans.spi.editor.bracesmatching.BracesMatcherFactory;
import org.netbeans.spi.editor.bracesmatching.MatcherContext;
import org.openide.util.Exceptions;

/**
 * A HTML parser based implementation of BracesMatcher.
 *
 * @author Marek Fukala
 */
public class HtmlBracesMatching implements BracesMatcher, BracesMatcherFactory {

    private MatcherContext context;
    private static final String BLOCK_COMMENT_START = "<!--"; //NOI18N
    private static final String BLOCK_COMMENT_END = "-->"; //NOI18N
    static boolean testMode = false;

    public HtmlBracesMatching() {
        this(null);
    }

    private HtmlBracesMatching(MatcherContext context) {
        this.context = context;
    }

    @Override
    public int[] findOrigin() throws InterruptedException, BadLocationException {
        int searchOffset = context.getSearchOffset();
        ((AbstractDocument) context.getDocument()).readLock();
        try {
            if (!testMode && MatcherContext.isTaskCanceled()) {
                return null;
            }
            TokenSequence<HTMLTokenId> ts = Utils.getJoinedHtmlSequence(context.getDocument(), searchOffset);
            TokenHierarchy<Document> th = TokenHierarchy.get(context.getDocument());

            if (ts.language() == HTMLTokenId.language()) {
                while (searchOffset != context.getLimitOffset()) {
                    int diff = ts.move(searchOffset);
                    searchOffset = searchOffset + (context.isSearchingBackward() ? -1 : +1);

                    if (diff == 0 && context.isSearchingBackward()) {
                        //we are searching backward and the offset is at the token boundary
                        if (!ts.movePrevious()) {
                            continue;
                        }
                    } else {
                        if (!ts.moveNext()) {
                            continue;
                        }
                    }

                    Token<HTMLTokenId> t = ts.token();
                    int toffs = ts.offset();
                    if (tokenInTag(t)) {
                        //find the tag beginning
                        do {
                            Token<HTMLTokenId> t2 = ts.token();
                            int t2offs = ts.offset();
                            if (!tokenInTag(t2)) {
                                return null;
                            } else if (t2.id() == HTMLTokenId.TAG_OPEN_SYMBOL) {
                                //find end
                                int tagNameEnd = -1;
                                while (ts.moveNext()) {
                                    Token<HTMLTokenId> t3 = ts.token();
                                    int t3offs = ts.offset();
                                    if (!tokenInTag(t3) || t3.id() == HTMLTokenId.TAG_OPEN_SYMBOL) {
                                        return null;
                                    } else if (t3.id() == HTMLTokenId.TAG_CLOSE_SYMBOL) {
                                        if ("/>".equals(t3.text().toString())) {
                                            //do no match empty tags
                                            return null;
                                        } else {
                                            int from = t2offs;
                                            int to = t3offs + t3.length();
                                            if (tagNameEnd != -1) {
                                                return new int[]{from, to,
                                                    from, tagNameEnd,
                                                    to - 1, to};
                                            } else {
                                                return new int[]{from, to};
                                            }
                                        }
                                    } else if (t3.id() == HTMLTokenId.TAG_OPEN || t3.id() == HTMLTokenId.TAG_CLOSE) {
                                        tagNameEnd = t3offs + t3.length();
                                    }
                                }
                                break;
                            }
                        } while (ts.movePrevious());
                    } else if (t.id() == HTMLTokenId.BLOCK_COMMENT) {
                        String tokenImage = t.text().toString();
                        if (tokenImage.startsWith(BLOCK_COMMENT_START) && context.getSearchOffset() < toffs + BLOCK_COMMENT_START.length()) {
                            return new int[]{toffs, toffs + BLOCK_COMMENT_START.length()};
                        } else if (tokenImage.endsWith(BLOCK_COMMENT_END) && (context.getSearchOffset() >= toffs + tokenImage.length() - BLOCK_COMMENT_END.length())) {
                            return new int[]{toffs + t.length() - BLOCK_COMMENT_END.length(), toffs + t.length()};
                        }
                    }
                }
            }
            return null;
        } finally {
            ((AbstractDocument) context.getDocument()).readUnlock();
        }
    }

    private boolean tokenInTag(Token t) {
        return t.id() == HTMLTokenId.TAG_CLOSE_SYMBOL || t.id() == HTMLTokenId.TAG_OPEN_SYMBOL || t.id() == HTMLTokenId.TAG_OPEN || t.id() == HTMLTokenId.TAG_CLOSE || t.id() == HTMLTokenId.WS || t.id() == HTMLTokenId.ARGUMENT || t.id() == HTMLTokenId.VALUE || t.id() == HTMLTokenId.VALUE_CSS || t.id() == HTMLTokenId.VALUE_JAVASCRIPT || t.id() == HTMLTokenId.OPERATOR || t.id() == HTMLTokenId.EOL;
    }

    @Override
    public int[] findMatches() throws InterruptedException, BadLocationException {
        if (!testMode && MatcherContext.isTaskCanceled()) {
            return null;
        }
        //disabling the master matcher cool matching strategy - if forward scanning, decrease the offset by one
//        final int searchOffset = context.isSearchingBackward() ? context.getSearchOffset() : context.getSearchOffset() + 1;
        final Source source = Source.create(context.getDocument());
        if (source == null) {
            return null;
        }
        //Bug 226625 - HTML comments do not match start and ending tags
        //no comments in the parse tree -- we need to find the pairs using tokens
        Document document = context.getDocument();
        final AtomicReference<int[]> result = new AtomicReference<>();
        document.render(new Runnable() {

            @Override
            public void run() {
                TokenSequence<HTMLTokenId> ts = Utils.getJoinedHtmlSequence(context.getDocument(), context.getSearchOffset());
                if (ts != null) {
                    int searchOffset = context.getSearchOffset();
                    while (searchOffset != context.getLimitOffset()) {
                        int diff = ts.move(searchOffset);
                        searchOffset = searchOffset + (context.isSearchingBackward() ? -1 : +1);

                        if (diff == 0 && context.isSearchingBackward()) {
                            //we are searching backward and the offset is at the token boundary
                            if (!ts.movePrevious()) {
                                continue;
                            }
                        } else {
                            if (!ts.moveNext()) {
                                continue;
                            }
                        }

                        if (ts.token().id() == HTMLTokenId.BLOCK_COMMENT) {
                            if (LexerUtils.startsWith(ts.token().text(), BLOCK_COMMENT_START, false, true) 
                                    && context.getSearchOffset() < ts.offset() + BLOCK_COMMENT_START.length()) {
                                //in <!-- open delimiter
                                //multiline comments are cut to separated block comment tokens
                                int lastBlockCommentTokenEnd;
                                do {
                                    lastBlockCommentTokenEnd = ts.offset() + ts.token().length();
                                } while (ts.moveNext() && ts.token().id() == HTMLTokenId.BLOCK_COMMENT);
                                result.set(new int[]{lastBlockCommentTokenEnd - 3, lastBlockCommentTokenEnd});
                            } else if (LexerUtils.endsWith(ts.token().text(), BLOCK_COMMENT_END, false, true)  
                                    && (context.getSearchOffset() >= ts.offset() + ts.token().length() - BLOCK_COMMENT_END.length())) {
                                //in --> close delimiter
                                //multiline comments are cut to separated block comment tokens
                                int firstBlockCommentTokenStart;
                                do {
                                    firstBlockCommentTokenStart = ts.offset();
                                } while (ts.movePrevious() && ts.token().id() == HTMLTokenId.BLOCK_COMMENT);
                                result.set(new int[]{firstBlockCommentTokenStart, firstBlockCommentTokenStart + 4});
                            }
                            return;
                        }
                    }
                }
            }
        });

        if (result.get() != null) {
            return result.get();
        }

        //searching for elements' pair -- use the parse tree
        final int[][] ret = new int[1][];
        try {
            ParserManager.parse(Collections.singleton(source), new UserTask() {
                @Override
                public void run(ResultIterator resultIterator) throws Exception {
                    int searchOffset = context.getSearchOffset();
                    if (!testMode && MatcherContext.isTaskCanceled()) {
                        return;
                    }

                    if (!source.getMimeType().equals(HtmlKit.HTML_MIME_TYPE)) {
                        //find embedded result iterator
                        resultIterator = WebUtils.getResultIterator(resultIterator, HtmlKit.HTML_MIME_TYPE);
                    }

                    if (resultIterator == null) {
                        ret[0] = new int[]{searchOffset, searchOffset};
                        return;
                    }

                    HtmlParserResult result = (HtmlParserResult) resultIterator.getParserResult();
                    if (result == null) {
                        return;
                    }

                    HtmlModel model = HtmlModelFactory.getModel(result.getHtmlVersion());
                    if (model == null) {
                        return;
                    }

                    int searchOffsetLocal = searchOffset;
                    while (searchOffsetLocal != context.getLimitOffset()) {
                        int searched = result.getSnapshot().getEmbeddedOffset(searchOffsetLocal);
                        Element origin = result.findByPhysicalRange(searched, !context.isSearchingBackward());
                        if (origin != null) {
                            if (origin.type() == ElementType.OPEN_TAG) {
                                OpenTag origin_tag = (OpenTag) origin;
                                CloseTag match = origin_tag.matchingCloseTag();
                                if (match == null || ElementUtils.isVirtualNode(match)) {
                                    //no matched tag foud
                                    HtmlTag tag = model.getTag(origin_tag.unqualifiedName().toString().toLowerCase(Locale.ENGLISH));
                                    if (tag != null) {
                                        if (tag.hasOptionalEndTag()) {
                                            //valid
                                            ret[0] = new int[]{searchOffsetLocal, searchOffsetLocal}; //match nothing, origin will be yellow  - workaround
                                            return;
                                        }
                                    }
                                    //error
                                    ret[0] = null; //no match
                                } else {
                                    ret[0] = translate(new int[]{match.from(), match.to()}, result);

                                }

                            } else if (origin.type() == ElementType.CLOSE_TAG) {
                                CloseTag origin_tag = (CloseTag) origin;
                                OpenTag match = origin_tag.matchingOpenTag();
                                if (match == null || ElementUtils.isVirtualNode(match)) {
                                    //no matched tag foud
                                    HtmlTag tag = model.getTag(origin_tag.unqualifiedName().toString().toLowerCase(Locale.ENGLISH));
                                    if (tag != null) {
                                        if (tag.hasOptionalOpenTag()) {
                                            //valid
                                            ret[0] = new int[]{searchOffsetLocal, searchOffsetLocal}; //match nothing, origin will be yellow  - workaround
                                            return;
                                        }
                                    }
                                    //error
                                    ret[0] = null; //no match
                                } else {
                                    //match
                                    //match the '<tagname' part
                                    int f1 = match.from();
                                    int t1 = f1 + match.name().length() + 1; /* +1 == open tag symbol '<' length */
                                    //match the closing '>' symbol

                                    int f2 = match.to() - 1; // -1 == close tag symbol '>' length
                                    int t2 = match.to();

                                    ret[0] = translate(new int[]{f1, t1, f2, t2}, result);

                                }
                            } else if (origin.type() == ElementType.COMMENT) {
                                if (searched >= origin.from() && searched <= origin.from() + BLOCK_COMMENT_START.length()) {
                                    //complete end of comment
                                    ret[0] = translate(new int[]{origin.to() - BLOCK_COMMENT_END.length(), origin.to()}, result);
                                } else if (searched >= origin.to() - BLOCK_COMMENT_END.length() && searched <= origin.to()) {
                                    //complete start of comment
                                    ret[0] = translate(new int[]{origin.from(), origin.from() + BLOCK_COMMENT_START.length()}, result);
                                }
                            }
                        }

                        searchOffsetLocal = searchOffsetLocal + (context.isSearchingBackward() ? -1 : +1);

                    }
                }
            });

        } catch (ParseException ex) {
            Exceptions.printStackTrace(ex);
        }

        return ret[0];
    }

    private int[] translate(int[] match, Result source) {

        int[] translation = new int[match.length];
        for (int i = 0; i < match.length; i++) {
            translation[i] = source.getSnapshot().getOriginalOffset(match[i]);
        }
        return translation;
    }

    //BracesMatcherFactory implementation
    @Override
    public BracesMatcher createMatcher(final MatcherContext context) {
        final HtmlBracesMatching[] ret = {null};
        context.getDocument().render(new Runnable() {
            @Override
            public void run() {
                TokenHierarchy<Document> hierarchy = TokenHierarchy.get(context.getDocument());

                //test if the html sequence is the top level one
                if (hierarchy.tokenSequence().language() == HTMLTokenId.language()) {
                    ret[0] = new HtmlBracesMatching(context);
                    return;
                }

                //test for embeedded html 
                List<TokenSequence<?>> ets = hierarchy.embeddedTokenSequences(context.getSearchOffset(), context.isSearchingBackward());
                for (TokenSequence ts : ets) {
                    Language language = ts.language();
                    if (language == HTMLTokenId.language()) {
                        ret[0] = new HtmlBracesMatching(context);
                        return;
                    }
                }
                // We might be trying to search at the end or beginning of a document. In which
                // case there is nothing to find and/or search through, so don't create a matcher.
                //        throw new IllegalStateException("No text/html language found on the MatcherContext's search offset! This should never happen!");
            }
        });
        return ret[0];
    }
}
