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
package org.netbeans.modules.php.smarty.editor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.php.smarty.editor.lexer.TplTokenId;
import org.netbeans.modules.php.smarty.editor.lexer.TplTopTokenId;
import org.netbeans.modules.php.smarty.editor.parser.TplParserResult;
import org.netbeans.modules.php.smarty.editor.parser.TplParserResult.Block;
import org.netbeans.modules.php.smarty.editor.utlis.LexerUtils;
import org.netbeans.spi.editor.bracesmatching.BracesMatcher;
import org.netbeans.spi.editor.bracesmatching.BracesMatcherFactory;
import org.netbeans.spi.editor.bracesmatching.MatcherContext;
import org.openide.util.Exceptions;

/**
 * TPL parser based implementation of BracesMatcher. Inspired by HtmlBracesMatching.
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public class TplBracesMatching implements BracesMatcher, BracesMatcherFactory {

    private MatcherContext context;
    private static boolean testMode = false;

    public TplBracesMatching() {
        this(null);
    }

    protected static void setTestMode(boolean testMode) {
        TplBracesMatching.testMode = testMode;
    }

    private TplBracesMatching(MatcherContext context) {
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
            TokenSequence<TplTopTokenId> ts = LexerUtils.getTplTopTokenSequence(context.getDocument(), searchOffset);
            TokenHierarchy<Document> th = TokenHierarchy.get(context.getDocument());

            if (ts != null && ts.language() == TplTopTokenId.language()) {
                int[] delims = findDelimsLength(ts);
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

                    Token<TplTopTokenId> t = ts.token();
                    boolean afterComment = afterCommentTag(th, ts, delims, searchOffset);
                    if (tokenInTag(t) || afterComment) {
                        //find the tag beginning
                        do {
                            Token<TplTopTokenId> t2 = ts.token();
                            int t2offs = ts.offset();
                            if (!tokenInTag(t2) && !afterComment) {
                                return null;
                            } else if (t2.id() == TplTopTokenId.T_SMARTY_OPEN_DELIMITER) {
                                //find end
                                int tagNameEnd = -1;
                                while (ts.moveNext()) {
                                    Token<TplTopTokenId> t3 = ts.token();
                                    int t3offs = ts.offset();
                                    int from = t2offs;
                                    int to = t3offs + t3.length();
                                    if (!tokenInTag(t3) || t3.id() == TplTopTokenId.T_SMARTY_OPEN_DELIMITER) {
                                        return null;
                                    } else if (t3.id() == TplTopTokenId.T_SMARTY) {
                                        TokenSequence<TplTokenId> tplTS = LexerUtils.getTplTokenSequence(th, t3offs);
                                        if (tplTS == null) {
                                            return null;
                                        } else {
                                            if (tplTS.token().id() == TplTokenId.FUNCTION) {
                                                tagNameEnd = tplTS.token().offset(th) + tplTS.token().length();
                                            }
                                        }
                                    } else if (t3.id() == TplTopTokenId.T_SMARTY_CLOSE_DELIMITER) {
                                        if (tagNameEnd != -1) {
                                            return new int[]{from, to,
                                                        from, tagNameEnd,
                                                        to - t3.length(), to};
                                        } else if (atCommentTag(th, ts, delims, searchOffset)) {
                                            // highlight only delimiters with the starting/ending asterisks
                                            return new int[]{from, to, from, from + delims[0] + 1, to - delims[1] - 1, to};
                                        } else {
                                            return new int[]{from, to};
                                        }
                                    }
                                }
                                break;
                            }
                        } while (ts.movePrevious());
                    }
                }
                return null;
            }
            return null;
        } finally {
            ((AbstractDocument) context.getDocument()).readUnlock();
        }
    }

    private boolean tokenInTag(Token t) {
        return t.id() == TplTopTokenId.T_SMARTY || t.id() == TplTopTokenId.T_SMARTY_CLOSE_DELIMITER
                || t.id() == TplTopTokenId.T_SMARTY_OPEN_DELIMITER || t.id() == TplTopTokenId.T_LITERAL_DEL
                || t.id() == TplTopTokenId.T_PHP_DEL || t.id() == TplTopTokenId.T_COMMENT;
    }

    @Override
    public int[] findMatches() throws InterruptedException, BadLocationException {
        int[] delims = new int[]{1, 1};
        final Source source = Source.create(context.getDocument());
        final int searchOffset = context.getSearchOffset();

        ((AbstractDocument) context.getDocument()).readLock();
        try {
            if (!testMode && MatcherContext.isTaskCanceled()) {
                return null;
            }
            if (source == null) {
                return null;
            }

            // comments - do not color them as errors
            TokenSequence<TplTopTokenId> ts = LexerUtils.getTplTopTokenSequence(context.getDocument(), searchOffset);
            
            if (ts != null && ts.language() == TplTopTokenId.language()) {
                delims = findDelimsLength(ts);
                ts.move(searchOffset);
                ts.moveNext();
                ts.movePrevious();
                if (ts.token().id() == TplTopTokenId.T_COMMENT
                        || atCommentTag(TokenHierarchy.get(context.getDocument()), ts, delims, searchOffset)) {
                    return new int[]{searchOffset, searchOffset};
                }
            }
        } finally {
            ((AbstractDocument) context.getDocument()).readUnlock();
        }
        final int[] delimiterLengths = delims;
        final int[][] ret = new int[1][];
        try {
            ParserManager.parse(Collections.singleton(source), new UserTask() {
                @Override
                public void run(ResultIterator resultIterator) throws Exception {
                    if (!testMode && MatcherContext.isTaskCanceled()
                            || !source.getMimeType().equals(TplDataLoader.MIME_TYPE)) {
                        return;
                    }

                    if (resultIterator == null) {
                        ret[0] = new int[]{searchOffset, searchOffset};
                        return;
                    }

                    TplParserResult parserResult = (TplParserResult) resultIterator.getParserResult();
                    if (parserResult == null) {
                        return;
                    }
                    int searchOffsetLocal = searchOffset;
                    while (searchOffsetLocal != context.getLimitOffset()) {
                        int searched = parserResult.getSnapshot().getEmbeddedOffset(searchOffsetLocal);
                        Block block = getBlockForOffset(parserResult, searched, context.isSearchingBackward(), delimiterLengths);
                        if (block == null) {
                            return;
                        }
                        if (block.getSections().size() == 1) {
                            //just simple tag - was found by findOrigin()
                            ret[0] = new int[]{searchOffset, searchOffset};
                            return;
                        }

                        List<Integer> result = new ArrayList<>();
                        TplParserResult.Section lastSection = null;
                        for (TplParserResult.Section section : block.getSections()) {
                            OffsetRange or = section.getOffset();
                            or = new OffsetRange(or.getStart() - delimiterLengths[0], or.getEnd() + delimiterLengths[1]);
                            if (!or.containsInclusive(searchOffset)) {
                                insertMatchingSection(result, section, delimiterLengths);
                            } else {
                                if (lastSection == null) {
                                    lastSection = section;
                                } else {
                                    if ((section.getOffset().getStart() < lastSection.getOffset().getStart() && context.isSearchingBackward())
                                            || section.getOffset().getStart() > lastSection.getOffset().getStart() && !context.isSearchingBackward()) {
                                        insertMatchingSection(result, lastSection, delimiterLengths);
                                        lastSection = section;
                                    } else {
                                        insertMatchingSection(result, section, delimiterLengths);
                                    }
                                }
                            }
                        }
                        ret[0] = convertToIntegers(result);
                        searchOffsetLocal = searchOffsetLocal + (context.isSearchingBackward() ? -1 : +1);
                    }
                }
            });

        } catch (ParseException ex) {
            Exceptions.printStackTrace(ex);
        }

        return ret[0];
    }

    private static void insertMatchingSection(List<Integer> result, TplParserResult.Section section, int[] delimLengths) {
        OffsetRange offset = section.getOffset();
        result.add(offset.getStart() - delimLengths[0]);
        result.add(offset.getStart() + section.getFunctionNameLength());
        result.add(offset.getEnd());
        result.add(offset.getEnd() + delimLengths[1]);
    }

    private static int[] convertToIntegers(List<Integer> list) {
        int[] integers = new int[list.size()];
        Iterator<Integer> iterator = list.iterator();
        for (int i = 0; i < integers.length; i++) {
            integers[i] = iterator.next().intValue();
        }
        return integers;
    }

    /**
     * Gets block of tags for given offset.
     *
     * @param parserResult tplParserResult
     * @param offset examined offset
     * @return {@code TplParserResult.Block} where one of sections contain the offset, {@code null} otherwise - if no
     * such block was found
     */
    private static TplParserResult.Block getBlockForOffset(TplParserResult parserResult, int offset, boolean backwardSearching, int[] delimLengths) {
        Block lastBlock = null;
        int previousBlockOffset = -1;
        for (TplParserResult.Block block : parserResult.getBlocks()) {
            for (TplParserResult.Section section : block.getSections()) {
                OffsetRange or = section.getOffset();
                or = new OffsetRange(or.getStart() - delimLengths[0], or.getEnd() + delimLengths[1]);
                if (or.containsInclusive(offset)) {
                    if (lastBlock != null) {
                        if ((section.getOffset().getStart() < previousBlockOffset && backwardSearching)
                                || section.getOffset().getStart() > previousBlockOffset && !backwardSearching) {
                            return block;
                        } else {
                            return lastBlock;
                        }
                    } else {
                        lastBlock = block;
                        previousBlockOffset = section.getOffset().getStart();
                    }
                }
            }
        }
        return lastBlock;
    }

    @Override
    public BracesMatcher createMatcher(final MatcherContext context) {
        final TplBracesMatching[] ret = {null};
        context.getDocument().render(new Runnable() {
            @Override
            public void run() {
                TokenHierarchy<Document> hierarchy = TokenHierarchy.get(context.getDocument());

                //test if the tpl sequence is the top level one
                if (hierarchy.tokenSequence().language() == TplTopTokenId.language()) {
                    ret[0] = new TplBracesMatching(context);
                }
            }
        });
        return ret[0];
    }

    private int[] findDelimsLength(TokenSequence<TplTopTokenId> ts) {
        int[] delimLengths = new int[]{-1, -1};
        ts.moveStart();
        while (ts.moveNext()) {
            if (ts.token().id() == TplTopTokenId.T_SMARTY_OPEN_DELIMITER) {
                delimLengths[0] = ts.token().length();
            } else if (ts.token().id() == TplTopTokenId.T_SMARTY_CLOSE_DELIMITER) {
                delimLengths[1] = ts.token().length();
            }
            if (delimLengths[0] > 0 && delimLengths[1] > 0) {
                return delimLengths;
            }
        }
        return new int[]{1, 1};
    }

    private boolean afterCommentTag(TokenHierarchy<Document> th, TokenSequence<TplTopTokenId> ts, int[] delimsLength, int searchOffset) {
        if (ts.movePrevious()) {
            Token<TplTopTokenId> prevToken = ts.token();
            if (prevToken != null) {
                ts.moveNext();
                return searchOffset - prevToken.offset(th) <= delimsLength[1] + 1;
            }
        }
        return false;
    }

    private boolean beforeCommentTag(TokenHierarchy<Document> th, TokenSequence<TplTopTokenId> ts, int[] delimsLength, int searchOffset) {
        if (ts.moveNext()) {
            Token<TplTopTokenId> nextToken = ts.token();
            if (nextToken != null) {
                if (nextToken.id() == TplTopTokenId.T_COMMENT) {
                    ts.movePrevious();
                    return nextToken.offset(th) - searchOffset <= delimsLength[0];
                } else if (nextToken.id() == TplTopTokenId.T_SMARTY_OPEN_DELIMITER) {
                    if (ts.moveNext()) {
                        Token<TplTopTokenId> nextNextToken = ts.token();
                        ts.movePrevious();
                        if (nextNextToken != null) {
                            if (nextNextToken.id() == TplTopTokenId.T_COMMENT) {
                                ts.movePrevious();
                                return nextToken.offset(th) - searchOffset <= delimsLength[0];
                            }
                        }
                    }
                    ts.movePrevious();
                }
            }
        }
        return false;
    }

    private boolean atCommentTag(TokenHierarchy<Document> get, TokenSequence<TplTopTokenId> ts, int[] delims, int searchOffset) {
        boolean end = ts.token().id() == TplTopTokenId.T_SMARTY_CLOSE_DELIMITER
                && afterCommentTag(get, ts, delims, searchOffset);
        boolean start = (ts.token().id() == TplTopTokenId.T_SMARTY_OPEN_DELIMITER || ts.token().id() == TplTopTokenId.T_HTML)
                && beforeCommentTag(get, ts, delims, searchOffset);

        return end || start;
    }
}
