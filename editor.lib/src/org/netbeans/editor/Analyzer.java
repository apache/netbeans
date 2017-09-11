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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.editor;

import java.io.Reader;
import java.io.Writer;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Segment;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.modules.editor.lib2.EditorPreferencesKeys;
import org.netbeans.spi.lexer.MutableTextInput;

/**
* Various text analyzes over the document
*
* @author Miloslav Metelka
* @version 1.00
*/

public class Analyzer {

    /** Platform default line separator */
    private static Object platformLS;

    /** Empty char array */
    public static final char[] EMPTY_CHAR_ARRAY = new char[0];

    /** Buffer filled by spaces used for spaces filling and tabs expansion */
    private static char spacesBuffer[] = new char[] { ' ' };

    /** Buffer filled by tabs used for tabs filling */
    private static char tabsBuffer[] = new char[] { '\t' };

    /** Cache up to 50 spaces strings */
    private static final int MAX_CACHED_SPACES_STRING_LENGTH = 50;

    /** Spaces strings cache. */
    private static final String[] spacesStrings
        = new String[MAX_CACHED_SPACES_STRING_LENGTH + 1];

    static {
        spacesStrings[0] = "";
        spacesStrings[MAX_CACHED_SPACES_STRING_LENGTH]
            = new String(getSpacesBuffer(MAX_CACHED_SPACES_STRING_LENGTH),
                0, MAX_CACHED_SPACES_STRING_LENGTH);
    }

    private Analyzer() {
        // no instantiation
    }

    /** Get platform default line separator */
    public static Object getPlatformLS() {
        if (platformLS == null) {
            platformLS = System.getProperty("line.separator"); // NOI18N
        }
        return platformLS;
    }

    /** Test line separator on given semgment. This implementation simply checks
    * the first line of file but it can be redefined to do more thorough test.
    * @param seg segment where analyzes are performed
    * @return line separator type found in the file
    */
    public static String testLS(char chars[], int len) {
        for (int i = 0; i < len; i++) {
            switch (chars[i]) {
            case '\r':
                if (i + 1 < len && chars[i + 1] == '\n') {
                    return BaseDocument.LS_CRLF;
                } else {
                    return BaseDocument.LS_CR;
                }

            case '\n':
                return BaseDocument.LS_LF;
            }
        }
        return null; // signal unspecified line separator
    }

    /** Convert text with generic line separators to line feeds (LF).
    * As the linefeeds are one char long there is no need to allocate
    * another buffer since the only possibility is that the returned
    * length will be smaller than previous (if there were some CRLF separators.
    * @param chars char array with data to convert
    * @param len valid portion of chars array
    * @return new valid portion of chars array after conversion
    */   
    public static int convertLSToLF(char chars[], int len) {
        int tgtOffset = 0;
        short lsLen = 0; // length of separator found
        int moveStart = 0; // start of block that must be moved
        int moveLen; // length of data moved back in buffer

        for (int i = 0; i < len; i++) {
            // first of all - there's no need to handle single '\n'
            if (chars[i] == '\r') { // '\r' found
                if (i + 1 < len && chars[i + 1] == '\n') { // '\n' follows
                    lsLen = 2; // '\r\n'
                } else {
                    lsLen = 1; // only '\r'
                }
            } else if (chars[i] == LineSeparatorConversion.LS || chars[i] == LineSeparatorConversion.PS) {
                lsLen = 1;
            }

            if (lsLen > 0) {
                moveLen = i - moveStart;
                if (moveLen > 0) {
                    if (tgtOffset != moveStart) { // will need to arraycopy
                        System.arraycopy(chars, moveStart, chars, tgtOffset, moveLen);
                    }
                    tgtOffset += moveLen;
                }
                chars[tgtOffset++] = '\n';
                moveStart += moveLen + lsLen; // skip separator
                i += lsLen - 1; // possibly skip '\n'
                lsLen = 0; // signal no separator found
            }
        }

        // now move the rest if it's necessary
        moveLen = len - moveStart;
        if (moveLen > 0) {
            if (tgtOffset != moveStart) {
                System.arraycopy(chars, moveStart, chars, tgtOffset, moveLen);
            }
            tgtOffset += moveLen;
        }

        return tgtOffset; // return current length
    }

    /** Convert string with generic line separators to line feeds (LF).
    * @param text string to convert
    * @return new string with converted LSs to LFs
    */   
    public static String convertLSToLF(String text) {
        char[] tgtChars = null;
        int tgtOffset = 0;
        short lsLen = 0; // length of separator found
        int moveStart = 0; // start of block that must be moved
        int moveLen; // length of data moved back in buffer
        int textLen = text.length();

        for (int i = 0; i < textLen; i++) {
            // first of all - there's no need to handle single '\n'
            if (text.charAt(i) == '\r') { // '\r' found
                if (i + 1 < textLen && text.charAt(i + 1) == '\n') { // '\n' follows
                    lsLen = 2; // '\r\n'
                } else {
                    lsLen = 1; // only '\r'
                }
            } else if (text.charAt(i) == LineSeparatorConversion.LS || text.charAt(i) == LineSeparatorConversion.PS) {
                lsLen = 1;
            }

            if (lsLen > 0) {
                if (tgtChars == null) {
                    tgtChars = new char[textLen];
                    text.getChars(0, textLen, tgtChars, 0); // copy whole array
                }
                moveLen = i - moveStart;
                if (moveLen > 0) {
                    if (tgtOffset != moveStart) { // will need to arraycopy
                        text.getChars(moveStart, moveStart + moveLen, tgtChars, tgtOffset);
                    }
                    tgtOffset += moveLen;
                }
                tgtChars[tgtOffset++] = '\n';
                moveStart += moveLen + lsLen; // skip separator
                i += lsLen - 1; // possibly skip '\n'
                lsLen = 0; // signal no separator found
            }
        }

        // now move the rest if it's necessary
        moveLen = textLen - moveStart;
        if (moveLen > 0) {
            if (tgtOffset != moveStart) {
                text.getChars(moveStart, moveStart + moveLen, tgtChars, tgtOffset);
            }
            tgtOffset += moveLen;
        }

        return (tgtChars == null) ? text : new String(tgtChars, 0, tgtOffset);
    }

    public static boolean isSpace(String s) {
        int len = s.length();
        for (int i = 0; i < len; i++) {
            if (s.charAt(i) != ' ') {
                return false;
            }
        }
        return true;
    }

    /** Return true if the array contains only space chars */
    public static boolean isSpace(char[] chars, int offset, int len) {
        assert offset + len <= chars.length : "Invalid parameters: " //NOI18N
                + "offset = " + offset //NOI18N
                + ", len = " + len //NOI18N
                + ", chars.length = " + chars.length; //NOI18N
        
        while (len > 0) {
            if (chars[offset++] != ' ') { //NOI18N
                return false;
            }
            len--;
        }
        return true;
    }

    /** Return true if the array contains only space or tab chars */
    public static boolean isWhitespace(char[] chars, int offset, int len) {
        assert offset + len <= chars.length : "Invalid parameters: " //NOI18N
                + "offset = " + offset //NOI18N
                + ", len = " + len //NOI18N
                + ", chars.length = " + chars.length; //NOI18N
        
        while (len > 0) {
            if (!Character.isWhitespace(chars[offset])) {
                return false;
            }
            offset++;
            len--;
        }
        return true;
    }

    /** Return the first index that is not space */
    public static int findFirstNonTab(char[] chars, int offset, int len) {
        assert offset + len <= chars.length : "Invalid parameters: " //NOI18N
                + "offset = " + offset //NOI18N
                + ", len = " + len //NOI18N
                + ", chars.length = " + chars.length; //NOI18N
        
        while (len > 0) {
            if (chars[offset] != '\t') { //NOI18N
                return offset;
            }
            offset++;
            len--;
        }
        return -1;
    }

    /** Return the first index that is not space */
    public static int findFirstNonSpace(char[] chars, int offset, int len) {
        assert offset + len <= chars.length : "Invalid parameters: " //NOI18N
                + "offset = " + offset //NOI18N
                + ", len = " + len //NOI18N
                + ", chars.length = " + chars.length; //NOI18N
        
        while (len > 0) {
            if (chars[offset] != ' ') { //NOI18N
                return offset;
            }
            offset++;
            len--;
        }
        return -1;
    }

    /** Return the first index that is not space or tab or new-line char */
    public static int findFirstNonWhite(char[] chars, int offset, int len) {
        assert offset + len <= chars.length : "Invalid parameters: " //NOI18N
                + "offset = " + offset //NOI18N
                + ", len = " + len //NOI18N
                + ", chars.length = " + chars.length; //NOI18N
        
        while (len > 0) {
            if (!Character.isWhitespace(chars[offset])) {
                return offset;
            }
            offset++;
            len--;
        }
        return -1;
    }

    /** Return the last index that is not space or tab or new-line char */
    public static int findLastNonWhite(char[] chars, int offset, int len) {
        assert offset + len <= chars.length : "Invalid parameters: " //NOI18N
                + "offset = " + offset //NOI18N
                + ", len = " + len //NOI18N
                + ", chars.length = " + chars.length; //NOI18N
        
        int i = offset + len - 1;
        while (i >= offset) {
            if (!Character.isWhitespace(chars[i])) {
                return i;
            }
            i--;
        }
        return -1;
    }

    /** Count the number of line feeds in char array.
    * @return number of LF characters contained in array.
    */
    public static int getLFCount(char chars[]) {
        return getLFCount(chars, 0, chars.length);
    }

    public static int getLFCount(char chars[], int offset, int len) {
        assert offset + len <= chars.length : "Invalid parameters: " //NOI18N
                + "offset = " + offset //NOI18N
                + ", len = " + len //NOI18N
                + ", chars.length = " + chars.length; //NOI18N
        
        int lfCount = 0;
        while (len > 0) {
            if (chars[offset++] == '\n') { //NOI18N
                lfCount++;
            }
            len--;
        }
        return lfCount;
    }

    public static int getLFCount(String s) {
        int lfCount = 0;
        int len = s.length();
        for (int i = 0; i < len; i++) {
            if (s.charAt(i) == '\n') { //NOI18N
                lfCount++;
            }
        }
        return lfCount;
    }

    public static int findFirstLFOffset(char[] chars, int offset, int len) {
        assert offset + len <= chars.length : "Invalid parameters: " //NOI18N
                + "offset = " + offset //NOI18N
                + ", len = " + len //NOI18N
                + ", chars.length = " + chars.length; //NOI18N
        
        while (len > 0) {
            if (chars[offset++] == '\n') { //NOI18N
                return offset - 1;
            }
            len--;
        }
        return -1;
    }

    public static int findFirstLFOffset(String s) {
        int len = s.length();
        for (int i = 0; i < len; i++) {
            if (s.charAt(i) == '\n') { //NOI18N
                return i;
            }
        }
        return -1;
    }

    public static int findFirstTab(char[] chars, int offset, int len) {
        assert offset + len <= chars.length : "Invalid parameters: " //NOI18N
                + "offset = " + offset //NOI18N
                + ", len = " + len //NOI18N
                + ", chars.length = " + chars.length; //NOI18N
        
        while (len > 0) {
            if (chars[offset++] == '\t') { //NOI18N
                return offset - 1;
            }
            len--;
        }
        return -1;
    }

    public static int findFirstTabOrLF(char[] chars, int offset, int len) {
        assert offset + len <= chars.length : "Invalid parameters: " //NOI18N
                + "offset = " + offset //NOI18N
                + ", len = " + len //NOI18N
                + ", chars.length = " + chars.length; //NOI18N

        while (len > 0) {
            switch (chars[offset++]) {
            case '\t': //NOI18N
            case '\n': //NOI18N
                return offset - 1;
            }
            len--;
        }
        return -1;
    }

    /** Reverses the order of characters in the array. It works from
    * the begining of the array, so no offset is given.
    */
    public static  void reverse(char[] chars, int len) {
        for (int i = ((--len - 1) >> 1); i >= 0; --i) {
            char ch = chars[i];
            chars[i] = chars[len - i];
            chars[len - i] = ch;
        }
    }

    public static boolean equals(String s, char[] chars) {
        return equals(s, chars, 0, chars.length);
    }

    public static boolean equals(String s, char[] chars, int offset, int len) {
        assert offset + len <= chars.length : "Invalid parameters: " //NOI18N
                + "offset = " + offset //NOI18N
                + ", len = " + len //NOI18N
                + ", chars.length = " + chars.length; //NOI18N
        
        if (s.length() != len) {
            return false;
        }
        for (int i = 0; i < len; i++) {
            if (s.charAt(i) != chars[offset + i]) {
                return false;
            }
        }
        return true;
    }

    /** Do initial reading of document. Translate any line separators
    * found in document to line separators used by document. It also cares
    * for elements that were already created on the empty document. Although
    * the document must be empty there can be already marks created. Initial
    * read is equivalent to inserting the string array of the whole document
    * size at position 0 in the document. Therefore all the marks that are
    * not insertAfter are removed and reinserted to the end of the document
    * after the whole initial read is finished.
    * @param doc document for which the initialization is performed
    * @param reader reader from which document should be read
    * @param lsType line separator type
    * @param testLS test line separator of file and if it's consistent, use it
    * @param markDistance the distance between the new syntax mark is put
    */
    public static void initialRead(BaseDocument doc, Reader reader, boolean testLS)
    throws IOException {
        // document MUST be empty
        if (doc.getLength() > 0) {
            return;
        }

        // for valid reader read the document
        if (reader != null) {
            // Size of the read buffer
            int readBufferSize = ((Integer)doc.getProperty(EditorPreferencesKeys.READ_BUFFER_SIZE)).intValue();

            if (testLS) {
                // Construct a reader that searches for initial line separator type
                reader = new LineSeparatorConversion.InitialSeparatorReader(reader);
            }

            /* buffer into which the data from file will be read */
            LineSeparatorConversion.ToLineFeed toLF = new LineSeparatorConversion.ToLineFeed(reader, readBufferSize);
            
            boolean firstRead = true; // first cycle of reading from stream
            int pos = 0; // actual position in the document data
            int line = 0; // Line counter
            int maxLineLength = 0; // Longest line found
            int lineStartPos = 0; // Start offset of the last line
            int markCount = 0; // Total mark count - for debugging only

/*            // array for getting mark array from renderer inner class
            Mark[] origMarks = new Mark[doc.marks.getItemCount()];
            ObjectArrayUtilities.copyItems(doc.marks, 0, origMarks.length,
                origMarks, 0);

            // now remove all the marks that are not insert after
            for (int i = 0; i < origMarks.length; i++) {
                Mark mark = origMarks[i];
                if (!(mark.getInsertAfter()
                        || (mark instanceof MarkFactory.CaretMark))
                   ) {
                    try {
                        mark.remove();
                    } catch (InvalidMarkException e) {
                        if (Boolean.getBoolean("netbeans.debug.exceptions")) { // NOI18N
                            e.printStackTrace();
                        }
                    }
                }
            }
*/

            // Enter the loop where all data from reader will be read
            Segment text = toLF.nextConverted();
            // Switch off token hierarchy before loading in case the document is large
            // and it will be read by multiple buffers
            TokenHierarchy<?> hi = TokenHierarchy.get(doc);
            MutableTextInput<? extends Document> mti = (MutableTextInput<? extends Document>)doc.getProperty(MutableTextInput.class);
            boolean deactivateTokenHierarchy = (mti != null && toLF.isReadWholeBuffer()); // Likely a next chunk(s) will follow
            if (deactivateTokenHierarchy) {
                mti.tokenHierarchyControl().setActive(false);
            }
            try {
                while (text != null) {
                    try {
                        doc.insertString(pos, new String(text.array, text.offset, text.count), null);
                    } catch (BadLocationException e) {
                        throw new IllegalStateException(e.toString());
                    }
                    pos += text.count;
                    text = toLF.nextConverted();
                }
            } finally {
                if (deactivateTokenHierarchy) {
                    mti.tokenHierarchyControl().setActive(true);
                }
            }

            if (testLS) {
                doc.putProperty(BaseDocument.READ_LINE_SEPARATOR_PROP,
                    ((LineSeparatorConversion.InitialSeparatorReader)reader).getInitialSeparator());
//                            if (doc.getProperty(BaseDocument.WRITE_LINE_SEPARATOR_PROP) == null) {
//                                doc.putProperty(BaseDocument.WRITE_LINE_SEPARATOR_PROP, newLS);
//                            }
// The property above is left empty so the write() will default to the READ_LINE_SEPARATOR_PROP
            }

/*            // Now reinsert marks that were removed at begining to the end
            for (int i = 0; i < origMarks.length; i++) {
                Mark mark = origMarks[i];
                if (!(mark.getInsertAfter()
                        || (mark instanceof MarkFactory.CaretMark))
                   ) {
                    try {
                        origMarks[i].insert(doc, pos);
                    } catch (InvalidMarkException e) {
                        if (Boolean.getBoolean("netbeans.debug.exceptions")) { // NOI18N
                            e.printStackTrace();
                        }
                    } catch (BadLocationException e) {
                        if (Boolean.getBoolean("netbeans.debug.exceptions")) { // NOI18N
                            e.printStackTrace();
                        }
                    }
                }
            }
 */

                // Set the line limit document property
// [PENDING]                doc.putProperty(BaseDocument.LINE_LIMIT_PROP, new Integer(maxLineLength));

        }
    }

    /** Read from some reader and insert into document */
    static void read(BaseDocument doc, Reader reader, int pos)
    throws BadLocationException, IOException {
        int readBufferSize = ((Integer)doc.getProperty(EditorPreferencesKeys.READ_BUFFER_SIZE)).intValue();
        LineSeparatorConversion.ToLineFeed toLF
            = new LineSeparatorConversion.ToLineFeed(reader, readBufferSize);
        
        Segment text = toLF.nextConverted();
        while (text != null) {
            doc.insertString(pos, new String(text.array, text.offset, text.count), null);
            pos += text.count;
            text = toLF.nextConverted();
        }
    }

    /** Write from document to some writer */
    static void write(BaseDocument doc, Writer writer, int pos, int len)
    throws BadLocationException, IOException {
        String lsType = (String)doc.getProperty(BaseDocument.WRITE_LINE_SEPARATOR_PROP);
        if (lsType == null) {
            lsType = (String)doc.getProperty(BaseDocument.READ_LINE_SEPARATOR_PROP);
            if (lsType == null) {
                lsType = BaseDocument.LS_LF;
            }
        }
        int writeBufferSize = ((Integer)doc.getProperty(EditorPreferencesKeys.WRITE_BUFFER_SIZE)).intValue();
        char[] getBuf = new char[writeBufferSize];
        char[] writeBuf = new char[2 * writeBufferSize];
        int actLen = 0;

        while (len > 0) {
            actLen = Math.min(len, writeBufferSize);
            doc.getChars(pos, getBuf, 0, actLen);
            int tgtLen = convertLFToLS(getBuf, actLen, writeBuf, lsType);
            writer.write(writeBuf, 0, tgtLen);
            pos += actLen;
            len -= actLen;
        }

        // Append new-line if not the last char
/*        if (actLen > 0 && getBuf[actLen - 1] != '\n') {
            writer.write(new char[] { '\n' }, 0, 1);
        }
 */

    }

    /** Get visual column. */
    public static int getColumn(char buffer[], int offset,
                                int len, int tabSize, int startCol) {
        int col = startCol;
        int endOffset = offset + len;

        // Check wrong tab values
        if (tabSize <= 0) {
            new Exception("Wrong tab size=" + tabSize).printStackTrace(); // NOI18N
            tabSize = 8;
        }

        while (offset < endOffset) {
            switch (buffer[offset++]) {
            case '\t':
                col = (col + tabSize) / tabSize * tabSize;
                break;
            default:
                col++;
            }
        }
        return col;
    }

    /** Get buffer filled with appropriate number of spaces. The buffer
    * can have actually more spaces than requested.
    * @param numSpaces number of spaces
    */
    public static synchronized char[] getSpacesBuffer(int numSpaces) {
        // check if there's enough space in white space array
        while (numSpaces > spacesBuffer.length) {
            char tmpBuf[] = new char[spacesBuffer.length * 2]; // new buffer
            System.arraycopy(spacesBuffer, 0, tmpBuf, 0, spacesBuffer.length);
            System.arraycopy(spacesBuffer, 0, tmpBuf, spacesBuffer.length, spacesBuffer.length);
            spacesBuffer = tmpBuf;
        }

        return spacesBuffer;
    }

    /** Get string filled with space characters. There is optimization to return
     * the same string instance for up to ceratin number of spaces.
     * @param numSpaces number of spaces determining the resulting size of the string.
     */
    public static synchronized String getSpacesString(int numSpaces) {
        if (numSpaces <= MAX_CACHED_SPACES_STRING_LENGTH) { // Cached
            String ret = spacesStrings[numSpaces];
            if (ret == null) {
                ret = spacesStrings[MAX_CACHED_SPACES_STRING_LENGTH].substring(0, numSpaces);
                spacesStrings[numSpaces] = ret;
            }

            return ret;

        } else { // non-cached
            return new String(getSpacesBuffer(numSpaces), 0, numSpaces);
        }
    }

    /** Get buffer of the requested size filled entirely with space character.
     * @param numSpaces number of spaces in the returned character buffer.
     */
    public static char[] createSpacesBuffer(int numSpaces) {
        char[] ret = new char[numSpaces];
        System.arraycopy(getSpacesBuffer(numSpaces), 0, ret, 0, numSpaces);
        return ret;
    }

    /** Get buffer filled with appropriate number of tabs. The buffer
    * can have actually more tabs than requested.
    * @param numSpaces number of spaces
    */
    public static char[] getTabsBuffer(int numTabs) {
        // check if there's enough space in white space array
        if (numTabs > tabsBuffer.length) {
            char tmpBuf[] = new char[numTabs * 2]; // new buffer

            // initialize new buffer with spaces
            for (int i = 0; i < tmpBuf.length; i += tabsBuffer.length) {
                System.arraycopy(tabsBuffer, 0, tmpBuf, i,
                                 Math.min(tabsBuffer.length, tmpBuf.length - i));
            }
            tabsBuffer = tmpBuf;
        }

        return tabsBuffer;
    }

    /** Get the string that should be used for indentation of the given level.
     * @param indent indentation level
     * @param expandTabs whether tabs should be expanded to spaces or not
     * @param tabSize size substituted visually for the '\t' character
     */
    public static String getIndentString(int indent, boolean expandTabs, int tabSize) {
        return getWhitespaceString(0, indent, expandTabs, tabSize);
    }

    /** Get the string that should be used for indentation of the given level.
     * @param indent indentation level
     * @param expandTabs whether tabs should be expanded to spaces or not
     * @param tabSize size of the '\t' character
     */
    public static String getWhitespaceString(int startCol, int endCol,
    boolean expandTabs, int tabSize) {
        return (expandTabs || tabSize <= 0)
            ? getSpacesString(endCol - startCol)
            : new String(createWhiteSpaceFillBuffer(startCol, endCol, tabSize));
    }

    /** createWhitespaceFillBuffer() with the non-capital 's' should be used.
     * @deprecated
     */
    public static char[] createWhiteSpaceFillBuffer(int startCol, int endCol,
    int tabSize) {
        return createWhitespaceFillBuffer(startCol, endCol, tabSize);
    }

    /** Get buffer filled with spaces/tabs so that it reaches from
    * some column to some other column.
    * @param startCol starting visual column of the whitespace on the line
    * @param endCol ending visual column of the whitespace on the line
    * @param tabSize size substituted visually for the '\t' character
    */
    public static char[] createWhitespaceFillBuffer(int startCol, int endCol,
    int tabSize) {
        if (startCol >= endCol) {
            return EMPTY_CHAR_ARRAY;
        }

        // Check wrong tab values
        if (tabSize <= 0) {
            new Exception("Wrong tab size=" + tabSize).printStackTrace(); // NOI18N
            tabSize = 8;
        }

        int tabs = 0;
        int spaces = 0;
        int nextTab = (startCol + tabSize) / tabSize * tabSize;
        if (nextTab > endCol) { // only spaces
            spaces += endCol - startCol;
        } else { // at least one tab
            tabs++; // jump to first tab
            int endSpaces = endCol - endCol / tabSize * tabSize;
            tabs += (endCol - endSpaces - nextTab) / tabSize;
            spaces += endSpaces;
        }

        char[] ret = new char[tabs + spaces];
        if (tabs > 0) {
            System.arraycopy(getTabsBuffer(tabs), 0, ret, 0, tabs);
        }
        if (spaces > 0) {
            System.arraycopy(getSpacesBuffer(spaces), 0, ret, tabs, spaces);
        }
        return ret;
    }

    /** Loads the file and performs conversion of line separators to LF.
    * This method can be used in debuging of syntax scanner or somewhere else.
    * @param fileName the name of the file to load
    * @return array of loaded characters with '\n' as line separator
    */

    public static char[] loadFile(String fileName) throws IOException {
        File file = new File(fileName);
        char chars[] = new char[(int)file.length()];
        FileReader reader = new FileReader(file);
        reader.read(chars);
        reader.close();
        int len = Analyzer.convertLSToLF(chars, chars.length);
        if (len != chars.length) {
            char copyChars[] = new char[len];
            System.arraycopy(chars, 0, copyChars, 0, len);
            chars = copyChars;
        }
        return chars;
    }

    /** Convert text with LF line separators to text that uses
    * line separators of the document. This function is used when
    * saving text into the file. Segment's data are converted inside
    * the segment's data or new segment's data array is allocated.
    * NOTE: Source segment must have just LFs as separators! Otherwise
    *   the conversion won't work correctly.
    * @param src source chars to convert from
    * @param len length of valid part of src data
    * @param tgt target chars to convert to. The array MUST have twice
    *   the size of src otherwise index exception can be thrown
    * @param lsType line separator type to be used i.e. LS_LF, LS_CR, LS_CRLF
    * @return length of valid chars in tgt array
     */
    public static int convertLFToLS(char[] src, int len, char[] tgt, String lsType) {
        if (lsType != null && lsType.length() == 1) {
            char ls = lsType.charAt(0);
            if (ls == '\r' || ls == LineSeparatorConversion.LS || ls == LineSeparatorConversion.PS) {
                // now do conversion for LS_CR and Unicode LS, PS
                for (int i = 0; i < len; i++) {
                    if (src[i] == '\n') {
                        tgt[i] = ls;
                    } else {
                        tgt[i] = src[i];
                    }
                }
                return len;
            }
        } else if (lsType.equals(BaseDocument.LS_CRLF)) {
            int tgtLen = 0;
            int moveStart = 0; // start of block that must be moved
            int moveLen; // length of chars moved

            for (int i = 0; i < len; i++) {
                if (src[i] == '\n') { // '\n' found
                    moveLen = i - moveStart;
                    if (moveLen > 0) { // will need to arraycopy
                        System.arraycopy(src, moveStart, tgt, tgtLen, moveLen);
                        tgtLen += moveLen;
                    }
                    tgt[tgtLen++] = '\r';
                    tgt[tgtLen++] = '\n';
                    moveStart = i + 1; // skip separator
                }
            }

            // now move the rest if it's necessary
            moveLen = len - moveStart;
            if (moveLen > 0) {
                System.arraycopy(src, moveStart, tgt, tgtLen, moveLen);
                tgtLen += moveLen;
            }
            return tgtLen;
        }
        // Using either \n or line separator is unknown
        System.arraycopy(src, 0, tgt, 0, len);
        return len;
    }

    public static boolean startsWith(char[] chars, char[] prefix) {
        if (chars == null || chars.length < prefix.length) {
            return false;
        }
        for (int i = 0; i < prefix.length; i++) {
            if (chars[i] != prefix[i]) {
                return false;
            }
        }
        return true;
    }

    public static boolean endsWith(char[] chars, char[] suffix) {
        if (chars == null || chars.length < suffix.length) {
            return false;
        }
        for (int i = chars.length - suffix.length; i < chars.length; i++) {
            if (chars[i] != suffix[i]) {
                return false;
            }
        }
        return true;
    }

    public static char[] concat(char[] chars1, char[] chars2) {
        if (chars1 == null || chars1.length == 0) {
            return chars2;
        }
        if (chars2 == null || chars2.length == 0) {
            return chars1;
        }
        char[] ret = new char[chars1.length + chars2.length];
        System.arraycopy(chars1, 0, ret, 0, chars1.length);
        System.arraycopy(chars2, 0, ret, chars1.length, chars2.length);
        return ret;
    }

    public static char[] extract(char[] chars, int offset, int len) {
        char[] ret = new char[len];
        System.arraycopy(chars, offset, ret, 0, len);
        return ret;
    }

    public static boolean blocksHit(int[] blocks, int startPos, int endPos) {
        return (blocksIndex(blocks, startPos, endPos) >= 0);
    }

    public static int blocksIndex(int[] blocks, int startPos, int endPos) {
        if (blocks.length > 0) {
            int onlyEven = ~1;
            int low = 0;
            int high = blocks.length - 2;

            while (low <= high) {
                int mid = ((low + high) / 2) & onlyEven;

                if (blocks[mid + 1] <= startPos) {
                    low = mid + 2;
                } else if (blocks[mid] >= endPos) {
                    high = mid - 2;
                } else {
                    return low; // found
                }
            }
        }

        return -1;
    }

    /** Remove all spaces from the given string.
     * @param s original string
     * @return string with all spaces removed
     */
    public static String removeSpaces(String s) {
        int spcInd = s.indexOf(' ');
        if (spcInd >= 0) {
            StringBuffer sb = new StringBuffer(s.substring(0, spcInd));
            int sLen = s.length();
            for (int i = spcInd + 1; i < sLen; i++) {
                char ch = s.charAt(i);
                if (ch != ' ') {
                    sb.append(ch);
                }
            }
            return sb.toString();
        }
        return s;
    }

}
