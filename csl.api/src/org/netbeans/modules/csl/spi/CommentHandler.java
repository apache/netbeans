/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.csl.spi;

import java.util.ArrayList;
import javax.swing.text.Document;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.editor.BaseDocument;
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
    public static abstract class DefaultCommentHandler implements CommentHandler {

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

            if(doc instanceof BaseDocument) {
                ((BaseDocument)doc).runAtomic(task);
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
