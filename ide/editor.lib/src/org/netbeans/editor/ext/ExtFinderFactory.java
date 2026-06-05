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

package org.netbeans.editor.ext;

import javax.swing.text.BadLocationException;
import org.netbeans.api.editor.document.LineDocumentUtils;
import org.netbeans.editor.Analyzer;
import org.netbeans.editor.FinderFactory;
import org.netbeans.editor.BaseDocument;

/**
* Various finders are located here.
*
* @author Miloslav Metelka
* @version 1.00
*/

public class ExtFinderFactory {

    /** Finder that collects the whole lines and calls
    * the <code>lineFound()</code> method that can do a local find.
    * !!! Udelat to poradne i s vice bufferama
    */
    public abstract static class LineFwdFinder extends FinderFactory.AbstractFinder {

        private int origStartPos;

        private int origLimitPos;

        public LineFwdFinder() {
        }

        public int adjustStartPos(BaseDocument doc, int startPos) {
            origStartPos = startPos;
            try {
                return LineDocumentUtils.getLineStartOffset(doc, startPos);
            } catch (BadLocationException e) {
                return startPos;
            }
        }

        public int adjustLimitPos(BaseDocument doc, int limitPos) {
            origLimitPos = limitPos;
            try {
                return LineDocumentUtils.getLineEndOffset(doc, limitPos);
            } catch (BadLocationException e) {
                return limitPos;
            }
        }

        /** find function that must be defined by descendant */
        public int find(int bufferStartPos, char buffer[],
                        int offset1, int offset2, int reqPos, int limitPos) {
            int offset = reqPos - bufferStartPos; // !!! Udelat poradne s moznosti vice bufferu
            while (true) {
                int lfOffset = Analyzer.findFirstLFOffset(buffer, offset, offset2 - offset);
                boolean lfFound = (lfOffset >= 0);
                if (!lfFound) {
                    lfOffset = offset2;
                }

                int lineOffset = lineFound(buffer, offset, lfOffset,
                                           Math.max(origStartPos - bufferStartPos, offset),
                                           Math.min(origLimitPos - bufferStartPos, lfOffset));
                if (lineOffset >= 0) {
                    found = true;
                    return bufferStartPos + offset + lineOffset;
                }

                if (lfFound) {
                    offset = lfOffset + 1; // skip '\n'
                } else {
                    break;
                }
            }
            return bufferStartPos + offset2;
        }

        /** Line was found and is present in the given buffer. The given
        * buffer is either the original buffer passed to the <code>find()</code>
        * or constructed buffer if the line is at the border of the previous
        * and next buffer.
        * @return non-negative number means the target string was found and
        *   the returned number is offset on the line where the string was found.
        *   Negative number means the target string was not found on the line
        *   and the search will continue with the next line.
        */
        protected abstract int lineFound(char[] buffer, int lineStartOffset, int lineEndOffset,
                                         int startOffset, int endOffset);

    }

    /** Finder that collects the whole lines and calls
    * the <code>lineFound()</code> method that can do a local find.
    * !!! Udelat to poradne i s vice bufferama
    */
    public abstract static class LineBwdFinder extends FinderFactory.AbstractFinder {

        private int origStartPos;

        private int origLimitPos;

        public LineBwdFinder() {
        }

        public int adjustStartPos(BaseDocument doc, int startPos) {
            origStartPos = startPos;
            try {
                return LineDocumentUtils.getLineEndOffset(doc, startPos);
            } catch (BadLocationException e) {
                return startPos;
            }
        }

        public int adjustLimitPos(BaseDocument doc, int limitPos) {
            origLimitPos = limitPos;
            try {
                return LineDocumentUtils.getLineStartOffset(doc, limitPos);
            } catch (BadLocationException e) {
                return limitPos;
            }
        }

        /** find function that must be defined by descendant */
        public int find(int bufferStartPos, char buffer[],
                        int offset1, int offset2, int reqPos, int limitPos) {
            int offset = reqPos - bufferStartPos + 1; // !!! Udelat poradne s moznosti vice bufferu
            while (true) {
                boolean lfFound = false;
                int lfOffsetP1 = offset;
                while (lfOffsetP1 > offset1) {
                    if (buffer[--lfOffsetP1] == '\n') {
                        lfFound = true;
                        lfOffsetP1++; // past '\n'
                        break;
                    }
                }
                if (!lfFound) {
                    lfOffsetP1 = offset1;
                }

                int lineOffset = lineFound(buffer, lfOffsetP1, offset,
                                           Math.max(origLimitPos - bufferStartPos, lfOffsetP1),
                                           Math.min(origStartPos - bufferStartPos, offset));
                if (lineOffset >= 0) {
                    found = true;
                    return bufferStartPos + offset + lineOffset;
                }

                if (lfFound) {
                    offset = lfOffsetP1 - 1; // skip '\n'
                } else {
                    break;
                }
            }
            return bufferStartPos + offset1 - 1;
        }

        /** Line was found and is present in the given buffer. The given
        * buffer is either the original buffer passed to the <code>find()</code>
        * or constructed buffer if the line is at the border of the previous
        * and next buffer.
        * @return non-negative number means the target string was found and
        *   the returned number is offset on the line where the string was found.
        *   Negative number means the target string was not found on the line
        *   and the search will continue with the next line.
        */
        protected abstract int lineFound(char[] buffer, int lineStartOffset, int lineEndOffset,
                                         int startOffset, int endOffset);

    }

    /** Finder that collects the whole lines and calls
    * the <code>lineFound()</code> method that can do a local find.
    * !!! Udelat to poradne i s vice bufferama
    */
    public abstract static class LineBlocksFinder extends FinderFactory.AbstractBlocksFinder {

        private int origStartPos;

        private int origLimitPos;

        public LineBlocksFinder() {
        }

        public int adjustStartPos(BaseDocument doc, int startPos) {
            origStartPos = startPos;
            try {
                return LineDocumentUtils.getLineStartOffset(doc, startPos);
            } catch (BadLocationException e) {
                return startPos;
            }
        }

        public int adjustLimitPos(BaseDocument doc, int limitPos) {
            origLimitPos = limitPos;
            try {
                return LineDocumentUtils.getLineEndOffset(doc, limitPos);
            } catch (BadLocationException e) {
                return limitPos;
            }
        }

        /** find function that must be defined by descendant */
        public int find(int bufferStartPos, char buffer[],
                        int offset1, int offset2, int reqPos, int limitPos) {
            int offset = reqPos - bufferStartPos; // !!! Udelat poradne s moznosti vice bufferu
            while (true) {
                int lfOffset = Analyzer.findFirstLFOffset(buffer, offset, offset2 - offset);
                boolean lfFound = (lfOffset >= 0);
                if (!lfFound) {
                    lfOffset = offset2;
                }

                int lineOffset = lineFound(buffer, offset, lfOffset,
                                           Math.max(origStartPos - bufferStartPos, offset),
                                           Math.min(origLimitPos - bufferStartPos, lfOffset));
                if (lineOffset >= 0) {
                    found = true;
                    return bufferStartPos + offset + lineOffset;
                }

                if (lfFound) {
                    offset = lfOffset + 1; // skip '\n'
                } else {
                    break;
                }
            }
            return bufferStartPos + offset2;
        }

        /** Line was found and is present in the given buffer. The given
        * buffer is either the original buffer passed to the <code>find()</code>
        * or constructed buffer if the line is at the border of the previous
        * and next buffer.
        * @return non-negative number means the target string was found and
        *   the returned number is offset on the line where the string was found.
        *   Negative number means the target string was not found on the line
        *   and the search will continue with the next line.
        */
        protected abstract int lineFound(char[] buffer, int lineStartOffset, int lineEndOffset,
                                         int startOffset, int endOffset);

    }



}
