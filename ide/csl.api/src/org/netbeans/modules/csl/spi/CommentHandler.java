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
package org.netbeans.modules.csl.spi;

import java.util.ArrayList;
import javax.swing.text.Document;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.editor.document.AtomicLockDocument;
import org.netbeans.api.editor.document.LineDocumentUtils;
import org.netbeans.lib.editor.util.CharSequenceUtilities;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;

/**
 *
 * @author marekfukala
 */
public interface CommentHandler {

    /*
     * @return a list of integer pairs determining comment start and end offsets. The blocks may go beyond the
     * given range in case a comment starts before from offset or ends after the to offset. The implementation
     * is supposed to return an empty array if there isn't any comment. Null value is not allowed.
     *
     * The code block should be exclusive to the given range so following comments must not be in the list:
     * <verbatim>
     *
     * ---<...>------<...>---
     *         |     |
     *         From  To
     *
     * </verbatim>
     */
    @NonNull
    public int[] getCommentBlocks(@NonNull Document doc, int from, int to);

    /**
     * Allows to modify the selection range.
     *
     * @return adjusted range or empty array if no un/comment operation is allowed
     * in the range
     */

    @NonNull
    public int[] getAdjustedBlocks(@NonNull Document doc, int from, int to);

    @NonNull
    public String getCommentStartDelimiter();

    @NonNull
    public String getCommentEndDelimiter();

    /**
     * Default implementation using simple document text search.
     * 
     */
    public abstract static class DefaultCommentHandler implements CommentHandler {

        public int[] getAdjustedBlocks(@NonNull Document doc, int from, int to) {
            return new int[]{from,to};
        }

        public int[] getCommentBlocks(final Document doc, final int from, final int to) {
            final ArrayList<Integer> comments = new ArrayList<Integer>();

            Runnable task = new Runnable() {

                public void run() {
                    CharSequence text = DocumentUtilities.getText(doc); //shared instance, low cost

                    int lastCommentStartIndex = CharSequenceUtilities.lastIndexOf(text, getCommentStartDelimiter(), from);
                    // search from the LAST character in the working area; allows to include comment block whose end is just aligned with 'to'
                    int lastCommentEndIndex = from > 0 ? CharSequenceUtilities.lastIndexOf(text, getCommentEndDelimiter(), from - 1) : -1;

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
            AtomicLockDocument ald = LineDocumentUtils.as(doc, AtomicLockDocument.class);
            if(ald != null) {
                ald.runAtomic(task);
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
    }

}
