/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.editor.ext;

import javax.swing.text.BadLocationException;
import org.netbeans.editor.Analyzer;
import org.netbeans.editor.FinderFactory;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;

/**
* Various finders are located here.
*
* @author Miloslav Metelka
* @version 1.00
*/

public class ExtFinderFactory {

    /** Finder that collects the whole lines and calls
    * the <tt>lineFound()</tt> method that can do a local find.
    * !!! Udelat to poradne i s vice bufferama
    */
    public static abstract class LineFwdFinder extends FinderFactory.AbstractFinder {

        private int origStartPos;

        private int origLimitPos;

        public LineFwdFinder() {
        }

        public int adjustStartPos(BaseDocument doc, int startPos) {
            origStartPos = startPos;
            try {
                return Utilities.getRowStart(doc, startPos);
            } catch (BadLocationException e) {
                return startPos;
            }
        }

        public int adjustLimitPos(BaseDocument doc, int limitPos) {
            origLimitPos = limitPos;
            try {
                return Utilities.getRowEnd(doc, limitPos);
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
        * buffer is either the original buffer passed to the <tt>find()</tt>
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
    * the <tt>lineFound()</tt> method that can do a local find.
    * !!! Udelat to poradne i s vice bufferama
    */
    public static abstract class LineBwdFinder extends FinderFactory.AbstractFinder {

        private int origStartPos;

        private int origLimitPos;

        public LineBwdFinder() {
        }

        public int adjustStartPos(BaseDocument doc, int startPos) {
            origStartPos = startPos;
            try {
                return Utilities.getRowEnd(doc, startPos);
            } catch (BadLocationException e) {
                return startPos;
            }
        }

        public int adjustLimitPos(BaseDocument doc, int limitPos) {
            origLimitPos = limitPos;
            try {
                return Utilities.getRowStart(doc, limitPos);
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
        * buffer is either the original buffer passed to the <tt>find()</tt>
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
    * the <tt>lineFound()</tt> method that can do a local find.
    * !!! Udelat to poradne i s vice bufferama
    */
    public static abstract class LineBlocksFinder extends FinderFactory.AbstractBlocksFinder {

        private int origStartPos;

        private int origLimitPos;

        public LineBlocksFinder() {
        }

        public int adjustStartPos(BaseDocument doc, int startPos) {
            origStartPos = startPos;
            try {
                return Utilities.getRowStart(doc, startPos);
            } catch (BadLocationException e) {
                return startPos;
            }
        }

        public int adjustLimitPos(BaseDocument doc, int limitPos) {
            origLimitPos = limitPos;
            try {
                return Utilities.getRowEnd(doc, limitPos);
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
        * buffer is either the original buffer passed to the <tt>find()</tt>
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
