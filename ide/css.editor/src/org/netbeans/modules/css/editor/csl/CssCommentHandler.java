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
package org.netbeans.modules.css.editor.csl;

import java.util.ArrayList;
import javax.swing.text.Document;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.lib.editor.util.CharSequenceUtilities;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.spi.CommentHandler;
import org.netbeans.modules.css.lib.api.CssTokenId;

/**
 *
 * @author mfukala@netbeans.org
 */
public class CssCommentHandler extends CommentHandler.DefaultCommentHandler {

    private static final String COMMENT_START_DELIMITER = "/*"; //NOI18N
    private static final String COMMENT_END_DELIMITER = "*/"; //NOI18N

    @Override
    public String getCommentStartDelimiter() {
        return COMMENT_START_DELIMITER;
    }

    @Override
    public String getCommentEndDelimiter() {
        return COMMENT_END_DELIMITER;
    }

    private OffsetRange getCssAreaRange(Document doc, int from, int to) {
        //limit the search just to one embedded css section
        TokenSequence<CssTokenId> ts = getCssTokenSequence(doc, from);
        if (ts == null) {
            return OffsetRange.NONE;
        }
        ts.moveStart();
        int limitedFrom = ts.moveNext() ? ts.offset() : from;
        ts.moveEnd();
        int limitedTo = ts.moveNext() ? ts.offset() + ts.token().length() : to;

        return new OffsetRange(limitedFrom, limitedTo);
    }

    public int[] getCommentBlocks(final Document doc, final int from, final int to) {
        final ArrayList<Integer> comments = new ArrayList<>();

        Runnable task = new Runnable() {

            public void run() {
                CharSequence text = DocumentUtilities.getText(doc); //shared instance, low cost

                int lastCommentStartIndex = CharSequenceUtilities.lastIndexOf(text, getCommentStartDelimiter(), from);
                // search from the LAST character in the working area; allows to include comment block whose end is just aligned with 'to'
                int lastCommentEndIndex = from > 0 ? CharSequenceUtilities.lastIndexOf(text, getCommentEndDelimiter(), from - 1) : -1;

                OffsetRange range = getCssAreaRange(doc, from, to);
                if(range != OffsetRange.NONE) {
                    if(lastCommentStartIndex < range.getStart()) {
                        lastCommentStartIndex = -1;
                    }
                }
                
                int searchFrom = from;
                if (lastCommentStartIndex > -1 && (lastCommentStartIndex > lastCommentEndIndex || lastCommentEndIndex == -1)) {
                    //we start in comment
                    int commentEndOffset = getCommentEnd(text, lastCommentStartIndex);
                    if (commentEndOffset > 0) {
                        comments.add(lastCommentStartIndex);
                        comments.add(commentEndOffset);
                        searchFrom = commentEndOffset;
                    }
                }

                while (true) {
                    int nextCommentStart = CharSequenceUtilities.indexOf(text, getCommentStartDelimiter(), searchFrom);
                    if (nextCommentStart != -1 && nextCommentStart < to) {
                        int nextCommentEnd = getCommentEnd(text, nextCommentStart);
                        if (nextCommentEnd > 0) {
                            comments.add(nextCommentStart);
                            comments.add(nextCommentEnd);
                            searchFrom = nextCommentEnd;
                            continue;
                        }
                    }
                    break;
                }

            }
        };

        if (doc instanceof BaseDocument) {
            ((BaseDocument) doc).runAtomic(task);
        } else {
            task.run();
        }

        int[] arr = new int[comments.size()];
        for (int i = 0; i < arr.length; i++) {
            arr[i] = comments.get(i);
        }
        return arr;
    }

    private int getCommentEnd(CharSequence text, int commentStartOffset) {
        int offset = CharSequenceUtilities.indexOf(text, getCommentEndDelimiter(), commentStartOffset);
        return offset == -1 ? -1 : offset + getCommentEndDelimiter().length();
    }

    private TokenSequence<CssTokenId> getCssTokenSequence(Document doc, int offset) {
        TokenHierarchy th = TokenHierarchy.get(doc);
        TokenSequence ts = th.tokenSequence();
        if (ts == null) {
            return null;
        }
        ts.move(offset);

        while (ts.moveNext() || ts.movePrevious()) {
            if (ts.language() == CssTokenId.language()) {
                return ts;
            }

            ts = ts.embedded();

            if (ts == null) {
                break;
            }

            //position the embedded ts so we can search deeper
            ts.move(offset);
        }

        return null;

    }
}
