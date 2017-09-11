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

package org.netbeans.modules.editor.lib2.document;

import javax.swing.text.Segment;
import org.netbeans.lib.editor.util.CharSequenceUtilities;

/**
 * Content of the document that can also be treated as CharSequence.
 * <br/>
 * It contains an extra '\n' at the end just like AbstractDocument's descendants.
 *
 * @author Miloslav Metelka
 * @since 1.46
 */

final class CharContent implements CharSequence {
    
    private static final char[] NEWLINE_CHAR_ARRAY = new char[] { '\n' };

    /** array with gap holding the text of the document */
    private char[] buffer;

    /** Start index of the gap */
    private int gapStart;

    /** Length of the gap */
    private int gapLength;
    
    CharContent() {
        // In compliance with AbstractDocument the content has one extra unmodifiable '\n' at the end
        buffer = NEWLINE_CHAR_ARRAY;
    }
    
    // Implements CharSequence
    @Override
    public char charAt(int index) {
        return buffer[rawOffset(index)];
    }
    
    /**
     * Total length of the CharSequnce (includes the extra newline at the end).
     */
    // Implements CharSequence
    @Override
    public int length() {
        return (buffer.length - gapLength);
    }

    // Implements CharSequence
    @Override
    public CharSequence subSequence(int start, int end) {
        CharSequenceUtilities.checkIndexesValid(start, end, length());
        return new SubSequence(start, end);
    }

    // Implements CharSequence
    @Override
    public String toString() {
        return getString(0, length());
    }
    
    void insertText(int offset, String text) {
        int textLength = text.length();
        moveGapForInsert(offset, textLength);
        // Copy from String directly into target char buffer
        text.getChars(0, textLength, buffer, gapStart);
        // Optimize gap start for subsequent insertion
        gapStart += textLength;
        gapLength -= textLength;
    }
    
    void insertText(int offset, char[] text) {
        moveGapForInsert(offset, text.length);
        // Copy from String directly into target char buffer
        System.arraycopy(text, 0, buffer, gapStart, text.length);
        // Optimize gap start for subsequent insertion
        gapStart += text.length;
        gapLength -= text.length;
    }
    
    private void moveGapForInsert(int offset, int textLength) {
        if (textLength > gapLength) {
            // Add extra 1/8 of buffer length. This is optimized for inserting initial document content at once
            // which is done so by ReadWriteUtils.read() etc.
            reallocate((buffer.length >>> 3) + textLength);
        }
        if (offset != gapStart) {
            moveGap(offset);
        }
    }
    
    void removeText(int offset, int length) {
        int endOffset = offset + length;
        if (offset < 0) {
            throw new IndexOutOfBoundsException("offset=" + offset + " < 0"); // NOI18N
        }
        if (endOffset > length()) {
            throw new IndexOutOfBoundsException("(offset=" + offset + // NOI18N
                    " + length=" + length + ")=" + endOffset + " > length()=" + length()); // NOI18N
        }
        if (offset >= gapStart) { // completely over gap
            if (offset > gapStart) {
                moveGap(offset);
            }
        } else { // completely below gap or spans the gap
            if (endOffset <= gapStart) {
                if (endOffset < gapStart) {
                    moveGap(endOffset);
                }
                gapStart -= length;
            } else { // spans gap
                gapStart = offset;
            }
        }

        gapLength += length;
    }
    
    String getString(int offset, int length) {
        String ret;
        if ((offset + length) <= gapStart) { // completely below gap
            ret = new String(buffer, offset, length);
            
        } else if (offset >= gapStart) { // completely above gap
            ret = new String(buffer, offset + gapLength, length);
            
        } else { // spans the gap, must copy
            ret = new String(getChars(offset, length));
        }
        
        return ret;
    }

    void getChars(int offset, int length, Segment txt) {
        if ((offset + length) <= gapStart) { // completely below gap
            txt.array = buffer;
            txt.offset = offset;
            
        } else if (offset >= gapStart) { // completely above gap
            txt.array = buffer;
            txt.offset = offset + gapLength;
            
        } else { // spans the gap, must copy
            txt.array = getChars(offset, length);
            txt.offset = 0;
        }
        txt.count = length;
    }

    char[] getChars(int offset, int length) {
        char[] ret = new char[length];
        int belowGap = gapStart - offset;
        System.arraycopy(buffer, offset, ret, 0, belowGap);
        System.arraycopy(buffer, gapStart + gapLength,
            ret, belowGap, length - belowGap);
        return ret;
    }

    void compact() {
        if (gapLength > 0) { // Fully compact
            reallocate(0);
            // Make whole area continuous to allow efficient getString()
            gapStart = buffer.length;
        }
    }
    
    int gapStart() {
        return gapStart;
    }
    
    private int rawOffset(int index) {
        return (index < gapStart) ? index : (index + gapLength);
    }
    
    private void moveGap(int index) {
        if (index <= gapStart) { // move gap down
            int moveSize = gapStart - index;
            System.arraycopy(buffer, index, buffer, gapStart + gapLength - moveSize, moveSize);

        } else { // above gap
            int moveSize = index - gapStart;
            System.arraycopy(buffer, gapStart + gapLength, buffer, gapStart, moveSize);
        }
        gapStart = index;
    }
    
    private void reallocate(int newGapLength) {
        int gapEnd = gapStart + gapLength;
        int aboveGapLength = (buffer.length - gapEnd);
        int newLength = gapStart + aboveGapLength + newGapLength;
        char[] newBuffer = new char[newLength];
        System.arraycopy(buffer, 0, newBuffer, 0, gapStart);
        System.arraycopy(buffer, gapEnd, newBuffer, newLength - aboveGapLength, aboveGapLength);
        // gapStart is same
        gapLength = newGapLength;
        buffer = newBuffer;
    }
    
    String consistencyError() {
        String err = null;
        if (gapStart < 0) {
            err = "gapStart=" + gapStart + " < 0"; // NOI18N
        } else if (gapLength < 0) {
            err = "gapLength=" + gapLength + " < 0"; // NOI18N
        } else if (gapStart + gapLength > buffer.length) {
            err = "gapStart=" + gapStart + " + gapLength=" + gapLength + " > buffer.length=" + buffer.length; // NOI18N
        }
        return err;
    }

    static String gapToString(int arrayLength, int gapStart, int gapLength) {
        return "[0," + gapStart + ")" + gapLength + '[' + (gapStart+gapLength) + // NOI18N
                ',' + Integer.toString(arrayLength) + ']'; // NOI18N
    }
    
    public String toStringDescription() {
        return gapToString(buffer.length, gapStart, gapLength);
    }

    private final class SubSequence implements CharSequence {

        final int start;

        final int end;

        public SubSequence(int start, int end) {
            this.start = start;
            this.end = end;
        }

        @Override
        public int length() {
            return (end - start);
        }

        @Override
        public char charAt(int index) {
            return CharContent.this.charAt(start + index);
        }

        @Override
        public CharSequence subSequence(int start, int end) {
            CharSequenceUtilities.checkIndexesValid(start, end, length());
            return new SubSequence(this.start + start, this.start + end);
        }

        @Override
        public String toString() {
            return CharContent.this.getString(start, end - start);
        }

    }

}
