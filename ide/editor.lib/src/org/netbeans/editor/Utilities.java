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

package org.netbeans.editor;

import java.awt.AWTKeyStroke;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Action;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JViewport;
import javax.swing.KeyStroke;
import javax.swing.LookAndFeel;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;
import javax.swing.text.BadLocationException;
import javax.swing.text.EditorKit;
import javax.swing.text.Document;
import javax.swing.text.Position.Bias;
import javax.swing.text.TextAction;
import javax.swing.text.Caret;
import javax.swing.plaf.TextUI;
import javax.swing.text.AbstractDocument;
import javax.swing.text.Element;
import javax.swing.text.Position;
import javax.swing.text.View;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.api.editor.document.LineDocument;
import org.netbeans.api.editor.document.LineDocumentUtils;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.settings.SimpleValueNames;
import org.netbeans.lib.editor.util.CharSequenceUtilities;
import org.netbeans.lib.editor.util.swing.DocumentListenerPriority;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.netbeans.modules.editor.indent.api.IndentUtils;
import org.netbeans.modules.editor.indent.api.Reformat;
import org.netbeans.modules.editor.indent.spi.CodeStylePreferences;
import org.netbeans.modules.editor.lib.BeforeSaveTasks;
import org.netbeans.modules.editor.lib.WcwdithUtil;
import org.netbeans.modules.editor.lib2.EditorPreferencesDefaults;
import org.netbeans.modules.editor.lib2.view.DocumentView;
import org.netbeans.modules.editor.lib2.view.EditorView;
import org.openide.awt.Actions;
import org.openide.util.Exceptions;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;

/**
* Various useful editor functions. Some of the methods have
* the same names and signatures like in javax.swing.Utilities but
* there is also many other useful methods.
* All the methods are static so there's no reason to instantiate Utilities.
*
* All the methods working with the document rely on that it is locked against
* modification so they don't acquire document read/write lock by themselves
* to guarantee the full thread safety of the execution.
* It's the user's task to lock the document appropriately
* before using methods described here.
*
* Most of the methods require org.netbeans.editor.BaseDocument instance
* not just the javax.swing.text.Document.
* The reason for that is to mark that the methods work on BaseDocument
* instances only, not on generic documents. To convert the Document
* to BaseDocument the simple conversion (BaseDocument)target.getDocument()
* can be done or the method getDocument(target) can be called.
* There are also other conversion methods like getEditorUI(), getKit()
* or getKitClass().
*
* @author Miloslav Metelka
* @version 0.10
*/

public class Utilities {

    private static final String WRONG_POSITION_LOCALE = "wrong_position"; // NOI18N

    /** Switch the case to capital letters. Used in changeCase() */
    public static final int CASE_UPPER = 0;

    /** Switch the case to small letters. Used in changeCase() */
    public static final int CASE_LOWER = 1;

    /** Switch the case to reverse. Used in changeCase() */
    public static final int CASE_SWITCH = 2;
    
    /** Fake TextAction for getting the info of the focused component */
    private static TextAction focusedComponentAction;

    private static final Logger logger = Logger.getLogger(Utilities.class.getName());
    
    private Utilities() {
        // instantiation has no sense
    }

    /** Get the starting position of the row.
    * @param c text component to operate on
    * @param offset position in document where to start searching
    * @return position of the start of the row or -1 for invalid position
    */
    public static int getRowStart(JTextComponent c, int offset)
    throws BadLocationException {
        Rectangle2D r = modelToView(c, offset);
        if (r == null){
            return -1;
        }
        EditorUI eui = getEditorUI(c);
        if (eui != null){
            return viewToModel(c, eui.textLeftMarginWidth, r.getY());
        }
        return -1;
    }
    
    /** Get visual column from position. This method can be used
    * only for superfixed font i.e. all characters of all font styles
    * have the same width.
    * @param offset position for which the visual column should be returned
    *   the function itself computes the begining of the line first
    */
    static int getVisColFromPos(LineDocument doc, int offset) throws BadLocationException {
        if (offset < 0 || offset > doc.getLength()) {
            throw new BadLocationException("Invalid offset", offset); // NOI18N
        }
        int startLineOffset =  LineDocumentUtils.getLineStart(doc, offset);
        int tabSize = IndentUtils.tabSize(doc);
        CharSequence docText = org.netbeans.lib.editor.util.swing.DocumentUtilities.getText(doc);
        int visCol = 0;
        for (int i = startLineOffset; i < offset; i++) {
            char ch = docText.charAt(i);
            if (ch == '\t') {
                visCol = (visCol + tabSize) / tabSize * tabSize;
            } else {
                // #17356
                int codePoint;
                if (Character.isHighSurrogate(ch) && i + 1 < docText.length()) {
                    codePoint = Character.toCodePoint(ch, docText.charAt(++i));
                } else {
                    codePoint = ch;
                }
                int w = WcwdithUtil.wcwidth(codePoint);
                visCol += w > 0 ? w : 0;
            }
        }
        return visCol;
    }
    
    /** Get the starting position of the row.
    * @param doc document to operate on
    * @param offset position in document where to start searching
    * @return position of the start of the row or -1 for invalid position
    * @deprecated use {@link LineDocumentUtils}
    */
    @Deprecated
    public static int getRowStart(BaseDocument doc, int offset)
    throws BadLocationException {
        return LineDocumentUtils.getLineStart(doc, offset);
    }

    /** Get the starting position of the row while providing relative count
    * of row how the given position should be shifted. This is the most
    * efficient way how to move by lines in the document based on some
    * position. There is no similair getRowEnd() method that would have
    * shifting parameter.
    * @param doc document to operate on
    * @param offset position in document where to start searching
    * @param lineShift shift the given offset forward/back relatively
    *  by some amount of lines
    * @return position of the start of the row or -1 for invalid position
    * @deprecated Deprecated without replacement
    */
    @Deprecated
    public static int getRowStart(BaseDocument doc, int offset, int lineShift)
    throws BadLocationException {
        checkOffsetValid(doc, offset);
        if (lineShift != 0) {
            Element lineRoot = doc.getParagraphElement(0).getParentElement();
            int line = lineRoot.getElementIndex(offset);
            line += lineShift;
            if (line < 0 || line >= lineRoot.getElementCount()) {
                return -1; // invalid line shift
            }
            return lineRoot.getElement(line).getStartOffset();

        } else { // no shift
            return doc.getParagraphElement(offset).getStartOffset();
        }
    }

    /** Get the first non-white character on the line.
    * The document.isWhitespace() is used to test whether the particular
    * character is white space or not.
    * @param doc document to operate on
    * @param offset position in document anywhere on the line
    * @return position of the first non-white char on the line or -1
    *   if there's no non-white character on that line.
    */
    public static int getRowFirstNonWhite(BaseDocument doc, int offset)
    throws BadLocationException {
        return LineDocumentUtils.getLineFirstNonWhitespace(doc, offset);
    }

    /** Get the last non-white character on the line.
    * The document.isWhitespace() is used to test whether the particular
    * character is white space or not.
    * @param doc document to operate on
    * @param offset position in document anywhere on the line
    * @return position of the last non-white char on the line or -1
    *   if there's no non-white character on that line.
    * @deprecated use {@link LineDocumentUtils}
    */
    @Deprecated
    public static int getRowLastNonWhite(BaseDocument doc, int offset)
    throws BadLocationException {
        return LineDocumentUtils.getLineLastNonWhitespace(doc, offset);
    }

    /** Get indentation on the current line. If this line is white then
    * return -1.
    * @param doc document to operate on
    * @param offset position in document anywhere on the line
    * @return indentation or -1 if the line is white
    */
    public static int getRowIndent(BaseDocument doc, int offset)
    throws BadLocationException {
        offset = getRowFirstNonWhite(doc, offset);
        if (offset == -1) {
            return -1;
        }
        return doc.getVisColFromPos(offset);
    }

    /** Get indentation on the current line. If this line is white then
    * go either up or down an return indentation of the first non-white row.
    * The <tt>getRowFirstNonWhite()</tt> is used to find the indentation
    * on particular line.
    * @param doc document to operate on
    * @param offset position in document anywhere on the line
    * @param downDir if this flag is set to true then if the row is white
    *   then the indentation of the next first non-white row is returned. If it's
    *   false then the indentation of the previous first non-white row is returned.
    * @return indentation or -1 if there's no non-white line in the specified direction
    */
    public static int getRowIndent(BaseDocument doc, int offset, boolean downDir)
    throws BadLocationException {
        int p = getRowFirstNonWhite(doc, offset);
        if (p == -1) {
            p = getFirstNonWhiteRow(doc, offset, downDir);
            if (p == -1) {
                return -1; // non-white line not found
            }
            p = getRowFirstNonWhite(doc, p);
            if (p == -1) {
                return -1; // non-white line not found
            }
        }
        return doc.getVisColFromPos(p);
    }

    /** Get the end position of the row right before the new-line character.
    * @param c text component to operate on
    * @param offset position in document where to start searching
    * @return position of the end of the row or -1 for invalid position
    * @deprecated use {@link LineDocumentUtils}
    */
    @Deprecated
    public static int getRowEnd(JTextComponent c, int offset)
    throws BadLocationException {
        Rectangle2D r = modelToView(c, offset);
        if (r == null){
            return -1;
        }
        return viewToModel(c, Integer.MAX_VALUE, r.getY());
    }

    /**
     *
     * @param doc
     * @param offset
     * @return
     * @throws BadLocationException
     * @deprecated use {@link LineDocumentUtils}
     */
    @Deprecated
    public static int getRowEnd(BaseDocument doc, int offset)
    throws BadLocationException {
        return LineDocumentUtils.getLineEnd(doc, offset);
    }
    
    private static int findBestSpan(JTextComponent c, int lineBegin, int lineEnd, int x)
    throws BadLocationException{
        if (lineBegin == lineEnd){
            return lineEnd;
        } 
        int low = lineBegin;
        int high = lineEnd;
        while (low <= high) {
            
            if (high - low < 3){
                int bestSpan = Integer.MAX_VALUE;
                int bestPos = -1;
                for (int i = low; i<=high; i++){
                    Rectangle tempRect = c.modelToView(i);
                    if (Math.abs(x-tempRect.x) < bestSpan){
                        bestSpan = Math.abs(x-tempRect.x);
                        bestPos = i;
                    }
                }
                return bestPos;
            }
            
            int mid = (low + high) / 2;
            
            Rectangle tempRect = c.modelToView(mid);
            if (tempRect.x > x){
                high = mid;
            } else if (tempRect.x < x) {
                low = mid;
            } else {
                return mid;
            }
        }
        return lineBegin;
    }

    /** Get the position that is one line above and visually at some
    * x-coordinate value.
    * @param c component to operate on
    * @param offset position in document from which the current line is determined
    * @param x float x-coordinate value
    * @return position of the character that is at the one line above at
    *   the required x-coordinate value
    * @deprecated use {@link LineDocumentUtils}
    */
    @Deprecated
    public static int getPositionAbove(JTextComponent c, int offset, int x)
    throws BadLocationException {
        // Ignore returned bias
        offset = c.getUI().getNextVisualPositionFrom(c,
                          offset, Bias.Forward, SwingConstants.NORTH, null);
        return offset;
    }

    /** Get the position that is one line above and visually at some
    * x-coordinate value.
    * @param c text component to operate on
    * @param offset position in document from which the current line is determined
    * @param x float x-coordinate value
    * @return position of the character that is at the one line above at
    *   the required x-coordinate value
    * @deprecated use {@link LineDocumentUtils}
    */
    @Deprecated
    public static int getPositionBelow(JTextComponent c, int offset, int x)
    throws BadLocationException {
        // Ignore returned bias
        offset = c.getUI().getNextVisualPositionFrom(c,
                          offset, Bias.Forward, SwingConstants.SOUTH, null);
        return offset;
    }

    /** Get start of the current word. If there are no more words till
    * the beginning of the document, this method returns -1.
    * @param c text component to operate on
    * @param offset position in document from which the current line is determined
    * @deprecated use {@link LineDocumentUtils}
    */
    @Deprecated
    public static int getWordStart(JTextComponent c, int offset)
    throws BadLocationException {
        return getWordStart((BaseDocument)c.getDocument(), offset);
    }

    @Deprecated
    public static int getWordStart(BaseDocument doc, int offset)
    throws BadLocationException {
        return doc.find(new FinderFactory.PreviousWordBwdFinder(doc, false, true),
                        offset, 0);
    }

    @Deprecated
    public static int getWordEnd(JTextComponent c, int offset)
    throws BadLocationException {
        return getWordEnd((BaseDocument)c.getDocument(), offset);
    }

    @Deprecated
    public static int getWordEnd(BaseDocument doc, int offset)
    throws BadLocationException {
        int ret = doc.find(new FinderFactory.NextWordFwdFinder(doc, false, true),
                        offset, -1);
        return (ret > 0) ? ret : doc.getLength();
    }

    @Deprecated
    public static int getNextWord(JTextComponent c, int offset)
    throws BadLocationException {
        int nextWordOffset = getNextWord((BaseDocument)c.getDocument(), offset);
        int nextVisualPosition = -1;
        if (nextWordOffset > 0){
            nextVisualPosition = c.getUI().getNextVisualPositionFrom(c,
                    nextWordOffset - 1, Position.Bias.Forward, javax.swing.SwingConstants.EAST, null);
        }
        return (nextVisualPosition == -1) ? nextWordOffset : nextVisualPosition;
    }

    @Deprecated
    public static int getNextWord(BaseDocument doc, int offset)
    throws BadLocationException {
        return LineDocumentUtils.getNextWordStart(doc, offset);
    }

    @Deprecated
    public static int getPreviousWord(JTextComponent c, int offset)
    throws BadLocationException {
        int prevWordOffset = getPreviousWord((BaseDocument)c.getDocument(), offset);
        int nextVisualPosition = c.getUI().getNextVisualPositionFrom(c,
                              prevWordOffset, Position.Bias.Forward, javax.swing.SwingConstants.WEST, null);
        if (nextVisualPosition == 0 && prevWordOffset == 0){
            return 0;
        }
        return (nextVisualPosition + 1 == prevWordOffset) ?  prevWordOffset : nextVisualPosition + 1;
    }

    @Deprecated
    public static int getPreviousWord(BaseDocument doc, int offset)
    throws BadLocationException {
        return LineDocumentUtils.getPreviousWordStart(doc, offset);
    }

    /** Get first white character in document in forward direction
    * @param doc document to operate on
    * @param offset position in document where to start searching
    * @return position of the first white character or -1
    * @deprecated use {@link LineDocumentUtils}
    */
    @Deprecated
    public static int getFirstWhiteFwd(BaseDocument doc, int offset)
    throws BadLocationException {
        return LineDocumentUtils.getNextWhitespace(doc, offset);
    }

    /** Get first white character in document in forward direction
    * @param doc document to operate on
    * @param offset position in document where to start searching
    * @param limitPos position in document (greater or equal than offset) where
    *   the search will stop reporting unsuccessful search by returning -1
    * @return position of the first non-white character or -1
    * @deprecated use {@link LineDocumentUtils}
    */
    @Deprecated
    public static int getFirstWhiteFwd(BaseDocument doc, int offset, int limitPos)
    throws BadLocationException {
        return LineDocumentUtils.getNextWhitespace(doc, offset, limitPos);
    }

    /** Get first non-white character in document in forward direction
    * @param doc document to operate on
    * @param offset position in document where to start searching
    * @return position of the first non-white character or -1
    * @deprecated use {@link LineDocumentUtils}
    */
    @Deprecated
    public static int getFirstNonWhiteFwd(BaseDocument doc, int offset)
    throws BadLocationException {
        return LineDocumentUtils.getNextNonWhitespace(doc, offset);
    }

    /** Get first non-white character in document in forward direction
    * @param doc document to operate on
    * @param offset position in document where to start searching
    * @param limitPos position in document (greater or equal than offset) where
    *   the search will stop reporting unsuccessful search by returning -1
    * @return position of the first non-white character or -1
    * @deprecated use {@link LineDocumentUtils}
    */
    @Deprecated
    public static int getFirstNonWhiteFwd(BaseDocument doc, int offset, int limitPos)
    throws BadLocationException {
        return LineDocumentUtils.getNextNonWhitespace(doc, offset, limitPos);
    }

    /** Get first white character in document in backward direction.
    * The character right before the character at position offset will
    * be searched as first.
    * @param doc document to operate on
    * @param offset position in document where to start searching
    * @return position of the first white character or -1
    * @deprecated use {@link LineDocumentUtils}
    */
    @Deprecated
    public static int getFirstWhiteBwd(BaseDocument doc, int offset)
    throws BadLocationException {
        return LineDocumentUtils.getPreviousWhitespace(doc, offset);
    }

    /** Get first white character in document in backward direction.
    * The character right before the character at position offset will
    * be searched as first.
    * @param doc document to operate on
    * @param offset position in document where to start searching
    * @param limitPos position in document (lower or equal than offset) where
    *   the search will stop reporting unsuccessful search by returning -1
    * @return position of the first white character or -1
    * @deprecated use {@link LineDocumentUtils}
    */
    @Deprecated
    public static int getFirstWhiteBwd(BaseDocument doc, int offset, int limitPos)
    throws BadLocationException {
        return LineDocumentUtils.getPreviousWhitespace(doc, offset, limitPos);
    }

    /** Get first non-white character in document in backward direction.
    * The character right before the character at position offset will
    * be searched as first.
    * @param doc document to operate on
    * @param offset position in document where to start searching
    * @return position of the first non-white character or -1
    * @deprecated use {@link LineDocumentUtils}
    */
    @Deprecated
    public static int getFirstNonWhiteBwd(BaseDocument doc, int offset)
    throws BadLocationException {
        return LineDocumentUtils.getPreviousNonWhitespace(doc, offset);
    }

    /** Get first non-white character in document in backward direction.
    * The character right before the character at position offset will
    * be searched as first.
    * @param doc document to operate on
    * @param offset position in document where to start searching
    * @param limitPos position in document (lower or equal than offset) where
    *   the search will stop reporting unsuccessful search by returning -1
    * @return position of the first non-white character or -1
    * @deprecated use {@link LineDocumentUtils}
    */
    @Deprecated
    public static int getFirstNonWhiteBwd(BaseDocument doc, int offset, int limitPos)
    throws BadLocationException {
        return LineDocumentUtils.getPreviousNonWhitespace(doc, offset, limitPos);
    }

    /** Return line offset (line number - 1) for some position in the document
    * @param doc document to operate on
    * @param offset position in document where to start searching
    * @deprecated use {@link LineDocumentUtils}
    */
    @Deprecated
    public static int getLineOffset(BaseDocument doc, int offset)
    throws BadLocationException {
        return LineDocumentUtils.getLineIndex(doc, offset);
    }

    /** Return start offset of the line
    * @param lineIndex line index starting from 0
    * @return start position of the line or -1 if lineIndex was invalid
    * @deprecated use {@link LineDocumentUtils}
    */
    @Deprecated
    public static int getRowStartFromLineOffset(BaseDocument doc, int lineIndex) {
        return LineDocumentUtils.getLineStartFromIndex(doc, lineIndex);
    }

    /** Return visual column (with expanded tabs) on the line.
    * @param doc document to operate on
    * @param offset position in document for which the visual column should be found
    * @return visual column on the line determined by position
    */
    public static int getVisualColumn(BaseDocument doc, int offset)
    throws BadLocationException {
        
        int docLen = doc.getLength();
        if (offset == docLen + 1) { // at ending extra '\n' => make docLen to proceed without BLE
            offset = docLen;
        }

        return doc.getVisColFromPos(offset);
    }
    
    /** Return visual column (with expanded tabs) on the line.
    * @param doc document to operate on
    * @param offset position in document for which the visual column should be found
    * @return visual column on the line determined by position
    */
    public static int getVisualColumn(LineDocument doc, int offset)
    throws BadLocationException {
        
        int docLen = doc.getLength();
        if (offset == docLen + 1) { // at ending extra '\n' => make docLen to proceed without BLE
            offset = docLen;
        }

        return getVisColFromPos(doc,offset);
    }

    /** Get the identifier around the given position or null if there's no identifier
    * @see #getIdentifierBlock(BaseDocument,int)
    */
    public static String getIdentifier(BaseDocument doc, int offset)
    throws BadLocationException {
        int[] blk = getIdentifierBlock(doc, offset);
        return (blk != null) ? doc.getText(blk[0], blk[1] - blk[0]) : null;
    }


    /** Get the identifier around the given position or null if there's no identifier
     * around the given position. The identifier is not verified against SyntaxSupport.isIdentifier().
     * @param c JTextComponent to work on
     * @param offset position in document - usually the caret.getDot()
     * @return the block (starting and ending position) enclosing the identifier
     * or null if no identifier was found
     */
    public static int[] getIdentifierBlock(JTextComponent c, int offset)
    throws BadLocationException {
        CharSequence id = null;
        int[] ret = null;
        Document doc = c.getDocument();
        int idStart = javax.swing.text.Utilities.getWordStart(c, offset);
        if (idStart >= 0) {
            int idEnd = javax.swing.text.Utilities.getWordEnd(c, idStart);
            if (idEnd >= 0) {
                id = DocumentUtilities.getText(doc, idStart, idEnd - idStart);
                ret = new int[] { idStart, idEnd };
                CharSequence trim = CharSequenceUtilities.trim(id);
                if (trim.length() == 0 || (trim.length() == 1 && !Character.isJavaIdentifierPart(trim.charAt(0)))) {
                    int prevWordStart = javax.swing.text.Utilities.getPreviousWord(c, offset);
                    if (offset == javax.swing.text.Utilities.getWordEnd(c,prevWordStart )){
                        ret = new int[] { prevWordStart, offset };
                    } else {
                        return null;
                    }
                } else if ((id != null) && (id.length() != 0)  && (CharSequenceUtilities.indexOf(id, '.') != -1)){ //NOI18N
                    int index = offset - idStart;
                    int begin = CharSequenceUtilities.lastIndexOf(id.subSequence(0, index), '.');
                    begin = (begin == -1) ? 0 : begin + 1; //first index after the dot, if exists
                    int end = CharSequenceUtilities.indexOf(id, '.', index);
                    end = (end == -1) ? id.length() : end;
                    ret = new int[] { idStart+begin, idStart+end };
                }
            }
        }
        return ret;
    }
    
    
    
    /** Get the identifier around the given position or null if there's no identifier
    * around the given position. The identifier must be
    * accepted by SyntaxSupport.isIdnetifier() otherwise null is returned.
    * @param doc document to work on
    * @param offset position in document - usually the caret.getDot()
    * @return the block (starting and ending position) enclosing the identifier
    *   or null if no identifier was found
    */
    public static int[] getIdentifierBlock(BaseDocument doc, int offset)
    throws BadLocationException {
        int[] ret = null;
        int idStart = getWordStart(doc, offset);
        if (idStart >= 0) {
            int idEnd = getWordEnd(doc, idStart);
            if (idEnd >= 0) {
                String id = doc.getText(idStart, idEnd - idStart);
                if (doc.getSyntaxSupport().isIdentifier(id)) {
                    ret = new int[] { idStart, idEnd };
                } else { // not identifier by syntax support
                    id = getWord(doc, offset); // try right at offset
                    if (doc.getSyntaxSupport().isIdentifier(id)) {
                        ret = new int[] { offset, offset + id.length() };
                    }
                }
            }
        }
        return ret;
    }

    
    /** Get the word around the given position .
     * @param c component to work with
     * @param offset position in document - usually the caret.getDot()
     * @return the word.
     */
    public static String getWord(JTextComponent c, int offset)
    throws BadLocationException {
        int[] blk = getIdentifierBlock(c, offset);
        Document doc = c.getDocument();
        return (blk != null) ? doc.getText(blk[0], blk[1] - blk[0]) : null;
    }
    
    
    /** Get the selection if there's any or get the identifier around
    * the position if there's no selection.
    * @param c component to work with
    * @param offset position in document - usually the caret.getDot()
    * @return the block (starting and ending position) enclosing the identifier
    *   or null if no identifier was found
    */
    public static int[] getSelectionOrIdentifierBlock(JTextComponent c, int offset)
    throws BadLocationException {
        Document doc = c.getDocument();
        Caret caret = c.getCaret();
        int[] ret;
        if (Utilities.isSelectionShowing(caret)) {
            ret = new int[] { c.getSelectionStart(), c.getSelectionEnd() }; 
        } else if (doc instanceof BaseDocument){
            ret = getIdentifierBlock((BaseDocument)doc, offset);
        } else {
            ret = getIdentifierBlock(c, offset);
        }
        return ret;
    }

    /** Get the selection or identifier at the current caret position
     * @see #getSelectionOrIdentifierBlock(JTextComponent, int)
     */
    public static int[] getSelectionOrIdentifierBlock(JTextComponent c) {
        try {
            return getSelectionOrIdentifierBlock(c, c.getCaret().getDot());
        } catch (BadLocationException e) {
            return null;
        }
    }

    /** Get the identifier before the given position (ending at given offset)
    * or null if there's no identifier
    */
    public static String getIdentifierBefore(BaseDocument doc, int offset)
    throws BadLocationException {
        int wordStart = getWordStart(doc, offset);
        if (wordStart != -1) {
            String word = new String(doc.getChars(wordStart,
                                                  offset - wordStart), 0, offset - wordStart);
            if (doc.getSyntaxSupport().isIdentifier(word)) {
                return word;
            }
        }
        return null;
    }

    /** Get the selection if there's any or get the identifier around
    * the position if there's no selection.
    */
    public static String getSelectionOrIdentifier(JTextComponent c, int offset)
    throws BadLocationException {
        Document doc = c.getDocument();
        Caret caret = c.getCaret();
        String ret;
        if (Utilities.isSelectionShowing(caret)) {
            ret = c.getSelectedText();
	    if (ret != null) return ret;
        } 
	if (doc instanceof BaseDocument){
	    ret = getIdentifier((BaseDocument) doc, offset);
        } else {
	    ret = getWord(c, offset);
	}
        return ret;
    }

    /** Get the selection or identifier at the current caret position */
    public static String getSelectionOrIdentifier(JTextComponent c) {
        try {
            return getSelectionOrIdentifier(c, c.getCaret().getDot());
        } catch (BadLocationException e) {
            return null;
        }
    }

    /** Get the word at given position.
    */
    public static String getWord(BaseDocument doc, int offset)
    throws BadLocationException {
        int wordEnd = getWordEnd(doc, offset);
        if (wordEnd != -1) {
            return new String(doc.getChars(offset, wordEnd - offset), 0,
                              wordEnd - offset);
        }
        return null;
    }


    /** Change the case for specified part of document
    * @param doc document to operate on
    * @param offset position in document determines the changed area begining
    * @param len number of chars to change
    * @param type either CASE_CAPITAL, CASE_SMALL or CASE_SWITCH
    */
    public static boolean changeCase(final BaseDocument doc, final int offset, final int len, final int type)
    throws BadLocationException {
        final char[] orig = doc.getChars(offset, len);
        final char[] changed = (char[])orig.clone();
        for (int i = 0; i < orig.length; i++) {
            switch (type) {
            case CASE_UPPER:
                changed[i] = Character.toUpperCase(orig[i]);
                break;
            case CASE_LOWER:
                changed[i] = Character.toLowerCase(orig[i]);
                break;
            case CASE_SWITCH:
                if (Character.isUpperCase(orig[i])) {
                    changed[i] = Character.toLowerCase(orig[i]);
                } else if (Character.isLowerCase(orig[i])) {
                    changed[i] = Character.toUpperCase(orig[i]);
                }
                break;
            }
        }
        // check chars for difference and possibly change document
        for (int i = 0; i < orig.length; i++) {
            if (orig[i] != changed[i]) {
                final BadLocationException[] badLocationExceptions = new BadLocationException [1];
                doc.runAtomicAsUser (new Runnable () {
                    public @Override void run () {
                        try {
                            Position pos = doc.createPosition(offset);
                            doc.remove(pos.getOffset(), orig.length);
                            doc.insertString(pos.getOffset(), new String(changed), null);
                        } catch (BadLocationException ex) {
                            badLocationExceptions [0] = ex;
                        }
                    }
                });
                if (badLocationExceptions [0] != null)
                    throw badLocationExceptions [0];
                return true; // changed
            }
        }
        return false;
    }

    /** Tests whether the line contains no characters except the ending new-line.
    * @param doc document to operate on
    * @param offset position anywhere on the tested line
    * @return whether the line is empty or not
    * @deprecated use {@link LineDocumentUtils#isLineEmpty(org.netbeans.api.editor.document.LineDocument, int)}.
    */
    @Deprecated
    public static boolean isRowEmpty(BaseDocument doc, int offset)
    throws BadLocationException {
        return LineDocumentUtils.isLineEmpty(doc, offset);
    }

    @Deprecated
    public static int getFirstNonEmptyRow(BaseDocument doc, int offset, boolean downDir)
    throws BadLocationException {
        if (downDir) {
            return LineDocumentUtils.getNextNonNewline(doc, offset);
        } else {
            return LineDocumentUtils.getPreviousNonNewline(doc, offset);
        }
    }

    /** Tests whether the line contains only whitespace characters.
    * @param doc document to operate on
    * @param offset position anywhere on the tested line
    * @return whether the line is empty or not
    * @deprecated use {@link LineDocumentUtils}
    */
    @Deprecated
    public static boolean isRowWhite(BaseDocument doc, int offset)
    throws BadLocationException {
        return LineDocumentUtils.isLineWhitespace(doc, offset);
    }

    @Deprecated
    public static int getFirstNonWhiteRow(BaseDocument doc, int offset, boolean downDir)
    throws BadLocationException {
        if (downDir) {
            return LineDocumentUtils.getNextNonWhitespace(doc, offset);
        } else {
            return LineDocumentUtils.getPreviousNonWhitespace(doc, offset);
        }
    }

    /**
     * Reformat a block of code.
     * <br>
     * The document should not be locked prior entering of this method.
     * <br>
     * The method should be called from AWT thread so that the given offsets are more stable.
     * 
     * @param doc document to work with
     * @param startOffset offset at which the formatting starts
     * @param endOffset offset at which the formatting ends
     * @return length of the reformatted code
     */
    public static int reformat (final BaseDocument doc, final int startOffset, final int endOffset) throws BadLocationException {
        final Reformat formatter = Reformat.get(doc);
        formatter.lock();
        try {
            final Object[] result = new Object[1];
            doc.runAtomicAsUser(new Runnable() {
                public @Override void run() {
                    try {
                        Position endPos = doc.createPosition(endOffset);
                        formatter.reformat(startOffset, endOffset);
                        result[0] = Math.max(endPos.getOffset() - startOffset, 0);
                    } catch (BadLocationException ex) {
                        result[0] = ex;
                    }
                }
            });
            if (result[0] instanceof BadLocationException) {
                throw (BadLocationException) result[0];
            } else {
                return (Integer) result[0];
            }
        } finally {
            formatter.unlock();
        }
    }

    /**
     * Reformat the line around the given position.
     * <br>
     * The document should not be locked prior entering of this method.
     * <br>
     * The method should be called from AWT thread so that the given offsets are more stable.
     * 
     */
    public static void reformatLine(BaseDocument doc, int pos)
    throws BadLocationException {
        int lineStart = getRowStart(doc, pos);
        int lineEnd = getRowEnd(doc, pos);
        reformat(doc, lineStart, lineEnd);
    }

    /** Count of rows between these two positions */
    @Deprecated
    public static int getRowCount(BaseDocument doc, int startPos, int endPos)
    throws BadLocationException {
        return LineDocumentUtils.getLineCount(doc, startPos, endPos);
    }

    /** Get the total count of lines in the document */
    @Deprecated
    public static int getRowCount(BaseDocument doc) {
        return LineDocumentUtils.getLineCount(doc);
    }

    /** @deprecated
     * {@code Formatter#insertTabString()} editor.deprecated.pre65formatting
     */
    @Deprecated
    public static String getTabInsertString(BaseDocument doc, int offset)
    throws BadLocationException {
        int col = getVisualColumn(doc, offset);
        Preferences prefs = CodeStylePreferences.get(doc).getPreferences();
        boolean expandTabs = prefs.getBoolean(SimpleValueNames.EXPAND_TABS, EditorPreferencesDefaults.defaultExpandTabs);
        if (expandTabs) {
            int spacesPerTab = prefs.getInt(SimpleValueNames.SPACES_PER_TAB, EditorPreferencesDefaults.defaultSpacesPerTab);
            if (spacesPerTab <= 0) {
                return ""; //NOI18N
            }
            int len = (col + spacesPerTab) / spacesPerTab * spacesPerTab - col;
            return new String(Analyzer.getSpacesBuffer(len), 0, len);
        } else { // insert pure tab
            return "\t"; // NOI18N
        }
    }

    /** Get the visual column corresponding to the position after pressing
     * the TAB key.
     * @param doc document to work with
     * @param offset position at which the TAB was pressed
     */
    public static int getNextTabColumn(BaseDocument doc, int offset)
    throws BadLocationException {
        // FIXME -- this should be delegated to LineDocumentUtils.
        int col = getVisualColumn(doc, offset);
        Preferences prefs = CodeStylePreferences.get(doc).getPreferences();
        int tabSize = prefs.getInt(SimpleValueNames.SPACES_PER_TAB, EditorPreferencesDefaults.defaultSpacesPerTab);
        return tabSize <= 0 ? col : (col + tabSize) / tabSize * tabSize;
    }

    public static void setStatusText(JTextComponent c, String text) {
        EditorUI eui = getEditorUI(c);
        StatusBar sb = eui == null ? null : eui.getStatusBar();
        if (sb != null) {
            sb.setText(StatusBar.CELL_MAIN, text);
        }
    }

    public static void setStatusText(JTextComponent c, String text, int importance) {
        EditorUI eui = getEditorUI(c);
        StatusBar sb = eui == null ? null : eui.getStatusBar();
        if (sb != null) {
            sb.setText(text, importance);
        }
    }

    public static void setStatusText(JTextComponent c, String text,
                                     Coloring extraColoring) {
        EditorUI eui = getEditorUI(c);
        StatusBar sb = eui == null ? null : eui.getStatusBar();
        if (sb != null) {
            sb.setText(StatusBar.CELL_MAIN, text, extraColoring);
        }
    }

    public static void setStatusBoldText(JTextComponent c, String text) {
        EditorUI eui = getEditorUI(c);
        StatusBar sb = eui == null ? null : eui.getStatusBar();
        if (sb != null) {
            sb.setBoldText(StatusBar.CELL_MAIN, text);
        }
    }

    public static String getStatusText(JTextComponent c) {
        EditorUI eui = getEditorUI(c);
        StatusBar sb = eui == null ? null : eui.getStatusBar();
        return (sb != null) ? sb.getText(StatusBar.CELL_MAIN) : null;
    }

    public static void clearStatusText(JTextComponent c) {
        setStatusText(c, ""); // NOI18N
    }

    public static void insertMark(BaseDocument doc, Mark mark, int offset)
    throws BadLocationException, InvalidMarkException {
        mark.insert(doc, offset);
    }

    public static void moveMark(BaseDocument doc, Mark mark, int newOffset)
    throws BadLocationException, InvalidMarkException {
        mark.move(doc, newOffset);
    }

    public static void returnFocus() {
         JTextComponent c = getLastActiveComponent();
         if (c != null) {
             requestFocus(c);
         }
    }

    public static void requestFocus(JTextComponent c) {
        if (c != null) {
            if (!ImplementationProvider.getDefault().activateComponent(c)) {
                Frame f = EditorUI.getParentFrame(c);
                if (f != null) {
                    f.requestFocus();
                }
                c.requestFocus();
            }
        }
    }

    public static void runInEventDispatchThread(Runnable r) {
        if (SwingUtilities.isEventDispatchThread()) {
            r.run();
        } else {
            SwingUtilities.invokeLater(r);
        }
    }

    public static String debugPosition(BaseDocument doc, int offset) {
        return debugPosition(doc, offset, ":");
    }

    /**
     * @param doc non-null document.
     * @param offset offset to translate to line and column info.
     * @param separator non-null separator of line and column info (either single charater or a string).s
     * @return non-null line and column info for the given offset.
     * @since 1.40
     */
    public static String debugPosition(BaseDocument doc, int offset, String separator) {
        String ret;

        if (offset >= 0) {
            try {
                int line = getLineOffset(doc, offset) + 1;
                int col = getVisualColumn(doc, offset) + 1;
                ret = String.valueOf(line) + separator + String.valueOf(col); // NOI18N
            } catch (BadLocationException e) {
                ret = NbBundle.getBundle(BaseKit.class).getString( WRONG_POSITION_LOCALE )
                      + ' ' + offset + " > " + doc.getLength(); // NOI18N
            }
        } else {
            ret = String.valueOf(offset);
        }

        return ret;
    }
    
    public static String offsetToLineColumnString(BaseDocument doc, int offset) {
        return String.valueOf(offset) + "[" + debugPosition(doc, offset) + "]"; // NOI18N
    }

    /** Display the identity of the document together with the title property
     * and stream-description property.
     */
    public static String debugDocument(Document doc) {
        return "<" + System.identityHashCode(doc) // NOI18N
            + ", title='" + doc.getProperty(Document.TitleProperty)
            + "', stream='" + doc.getProperty(Document.StreamDescriptionProperty)
            + ", " + doc.toString() + ">"; // NOI18N
    }

    public static void performAction(Action a, ActionEvent evt, JTextComponent target) {
        if (a instanceof BaseAction) {
            ((BaseAction)a).actionPerformed(evt, target);
        } else {
            a.actionPerformed(evt);
        }
    }

    /** Returns last activated component. If the component was closed, 
     *  then previous component is returned */
    public static JTextComponent getLastActiveComponent() {
        return EditorRegistry.lastFocusedComponent();
    }
    
    /**
     * Fetches the text component that currently has focus. It delegates to 
     * TextAction.getFocusedComponent().
     * @return the component
     */
    public static JTextComponent getFocusedComponent() {
        /** Fake action for getting the focused component */
        class FocusedComponentAction extends TextAction {
            
            FocusedComponentAction() {
                super("focused-component"); // NOI18N
            }
            
            /** adding this method because of protected final getFocusedComponent */
            JTextComponent getFocusedComponent2() {
                return getFocusedComponent();
            }
            
            public @Override void actionPerformed(ActionEvent evt){}
        }
        
        if (focusedComponentAction == null) {
            focusedComponentAction = new FocusedComponentAction();
        }
        
        return ((FocusedComponentAction)focusedComponentAction).getFocusedComponent2();
    }

    /** Helper method to obtain instance of EditorUI (extended UI)
     * from the existing JTextComponent.
     * It doesn't require any document locking.
     * @param target JTextComponent for which the extended UI should be obtained
     * @return extended ui instance or null if the component.getUI()
     *   does not return BaseTextUI instance.
     */
    public static EditorUI getEditorUI(JTextComponent target) {
        TextUI ui = target.getUI();
        return (ui instanceof BaseTextUI) 
            ? ((BaseTextUI)ui).getEditorUI()
            : null;
    }

    /** Helper method to obtain instance of editor kit from existing JTextComponent.
    * If the kit of the component is not an instance
    * of the <tt>org.netbeans.editor.BaseKit</tt> the method returns null.
    * The method doesn't require any document locking.
    * @param target JTextComponent for which the editor kit should be obtained
    * @return BaseKit instance or null
    */
    public static BaseKit getKit(JTextComponent target) {
        if (target == null) return null; // #19574
        EditorKit ekit = target.getUI().getEditorKit(target);
        return (ekit instanceof BaseKit) ? (BaseKit)ekit : null;
    }

    /** 
     * Gets the class of an editor kit installed in <code>JTextComponent</code>.
     * The method doesn't require any document locking.
     * 
     * <div class="nonnormative">
     * <p>WARNING: The implementation class of an editor kit is most likely
     * not what you want. Please see {@link BaseKit#getKit(Class)} for more
     * details.
     * 
     * <p>Unfortunatelly, there are still places in editor libraries where
     * an editor kit class is required.
     * One of them is the editor settings infrastructure built around the
     * <code>Settings</code> class. So, if you really need it go ahead and use it,
     * there is nothing wrong with the method itself.
     * </div>
     * 
     * @param target The <code>JTextComponent</code> to get the kit class for.
     *   Can be <code>null</code>.
     * @return The implementation class of the editor kit or <code>null</code>
     *   if the <code>target</code> is <code>null</code>.
     */
    public static Class getKitClass(JTextComponent target) {
        EditorKit kit = (target != null) ? target.getUI().getEditorKit(target) : null;
        return (kit != null) ? kit.getClass() : null;
    }

    /** Helper method to obtain instance of BaseDocument from JTextComponent.
    * If the document of the component is not an instance
    * of the <tt>org.netbeans.editor.BaseDocument</tt> the method returns null.
    * The method doesn't require any document locking.
    * @param target JTextComponent for which the document should be obtained
    * @return BaseDocument instance or null
    */
    public static BaseDocument getDocument(JTextComponent target) {
        Document doc = target.getDocument();
        return (doc instanceof BaseDocument) ? (BaseDocument)doc : null;
    }

    /** Get the syntax-support class that belongs to the document of the given
    * component. Besides using directly this method, the <tt>SyntaxSupport</tt>
    * can be obtained by calling <tt>doc.getSyntaxSupport()</tt>.
    * The method can return null in case the document is not
    * an instance of the BaseDocument.
    * The method doesn't require any document locking.
    * @param target JTextComponent for which the syntax-support should be obtained
    * @return SyntaxSupport instance or null
    */
    public static SyntaxSupport getSyntaxSupport(JTextComponent target) {
        Document doc = target.getDocument();
        return (doc instanceof BaseDocument) ? ((BaseDocument)doc).getSyntaxSupport() : null;
    }

    /**
     * Get first view in the hierarchy that is an instance of the given class.
     * It allows to skip various wrapper-views around the doc-view that holds
     * the child views for the lines.
     *
     * @param component component from which the root view is fetched.
     * @param rootViewClass class of the view to return.
     * @return view being instance of the requested class or null if there
     *  is not one.
     */
    public static View getRootView(JTextComponent component, Class rootViewClass) {
        View view = null;
        TextUI textUI = component.getUI();
        if (textUI != null) {
            view = textUI.getRootView(component);
            while (view != null && !rootViewClass.isInstance(view)
                && view.getViewCount() == 1 // must be wrapper view
            ) {
                view = view.getView(0); // get the only child
            }
        }
        
        return view;
    }
    
    /**
     * Get the view that covers the whole area of the document
     * and holds a child view for each line in the document
     * (or for a bunch of lines in case there is a code folding present).
     */
    public static View getDocumentView(JTextComponent component) {
        return getRootView(component, DocumentView.class);
    }

    /**
     * Execute the given runnable with view hierarchy being locked.
     * This is necessary when exploring the view hierarchy by views' methods
     * since the views changes may happen due to changes from highlighting layers
     * that cause the views to be rebuilt.
     * @param component non-null text component of which the view hierarchy is being explored.
     * @param readLockDocument if true lock the document before locking the view hierarchy.
     *  This parameter should only be false if it's known that the document was already read/write-locked
     *  prior calling this method.
     * @param r  non-null runnable to execute.
     */
    public static void runViewHierarchyTransaction(final JTextComponent component,
            boolean readLockDocument, final Runnable r)
    {
        Runnable wrapRun = new Runnable() {
            @Override
            public void run() {
                View documentView = getDocumentView(component);
                if (documentView != null) {
                    ((DocumentView) documentView).runTransaction(r);
                }
            }
        };
        Document doc;
        if (readLockDocument && (doc = component.getDocument()) != null) {
            doc.render(wrapRun);
        } else {
            wrapRun.run();
        }
        
    }

    /**
     * Creates nice textual description of sequence of KeyStrokes. Usable for
     * displaying MultiKeyBindings. The keyStrokes are delimited by space.
     * @param seq Array of KeyStrokes representing the actual sequence.
     * @return String describing the KeyStroke sequence.
     */
    public static String keySequenceToString( KeyStroke[] seq ) {
        StringBuilder sb = new StringBuilder();
        for( int i=0; i<seq.length; i++ ) {
            if( i>0 ) sb.append( ' ' );  // NOI18N
            sb.append( keyStrokeToString( seq[i] ) );
        }
        return sb.toString();
    }

    /**
     * Creates nice textual representation of KeyStroke.
     * Modifiers and an actual key label are concated per the platform-specific convention
     * @param stroke the KeyStroke to get description of
     * @return String describing the KeyStroke
     */
    public static String keyStrokeToString( KeyStroke stroke ) {
        /* The related logic has now been moved into org.openide.awt.Actions, so that it can be used
        by modules that do not depend on the editor infrastructure. */
        return Actions.keyStrokeToString(stroke);
    }

    private static void checkOffsetValid(Document doc, int offset) throws BadLocationException {
        checkOffsetValid(offset, doc.getLength() + 1);
    }

    private static void checkOffsetValid(int offset, int limitOffset) throws BadLocationException {
        if (offset < 0 || offset > limitOffset) { 
            throw new BadLocationException("Invalid offset=" + offset // NOI18N
                + " not within <0, " + limitOffset + ">", // NOI18N
                offset);
        }
    }

    /** 
     * Writes a <code>Throwable</code> to a log file.
     * 
     * <p class="nonnormative">The method is internally using 
     * <code>org.netbeans.editor</code> logger and <code>Level.INFO</code>.
     * 
     * @param t The exception that will be logged.
     * @deprecated Use java.util.logging.Logger instead with the proper name,
     * log level and message.
     */
    @Deprecated
    public static void annotateLoggable(Throwable t) {
        Logger.getLogger("org.netbeans.editor").log(Level.INFO, null, t); //NOI18N
    }
    
    /**
     * Check whether caret's selection is visible and there is at least
     * one selected character showing.
     * 
     * @param caret non-null caret.
     * @return true if selection is visible and there is at least one selected character.
     */
    public static boolean isSelectionShowing(Caret caret) {
        return caret.isSelectionVisible() && caret.getDot() != caret.getMark();
    }
    
    /**
     * @see #isSelectionShowing(Caret)
     * @param component non-null component.
     * @return if selection is showing for component's caret.
     */
    public static boolean isSelectionShowing(JTextComponent component) {
        Caret caret = component.getCaret();
        return (caret != null) && isSelectionShowing(caret);
    }
     
    /**
     * Gets the mime type of a document. If the mime type can't be determined
     * this method will return <code>null</code>. This method should work reliably
     * for Netbeans documents that have their mime type stored in a special
     * property. For any other documents it will probably just return <code>null</code>.
     * 
     * @param doc The document to get the mime type for.
     * 
     * @return The mime type of the document or <code>null</code>.
     * @see NbEditorDocument#MIME_TYPE_PROP
     */
    /* package */ static String getMimeType(Document doc) {
        return (String)doc.getProperty(BaseDocument.MIME_TYPE_PROP); //NOI18N
    }

    /**
     * Gets the mime type of a document in <code>JTextComponent</code>. If
     * the mime type can't be determined this method will return <code>null</code>.
     * It tries to determine the document's mime type first and if that does not
     * work it uses mime type from the <code>EditorKit</code> attached to the
     * component.
     * 
     * @param component The component to get the mime type for.
     * 
     * @return The mime type of a document opened in the component or <code>null</code>.
     */
    /* package */ static String getMimeType(JTextComponent component) {
        Document doc = component.getDocument();
        String mimeType = getMimeType(doc);
        if (mimeType == null) {
            EditorKit kit = component.getUI().getEditorKit(component);
            if (kit != null) {
                mimeType = kit.getContentType();
            }
        }
        return mimeType;
    }

    //#182648: JTextComponent.modelToView returns a Rectangle, which contains integer positions,
    //but the views are layed-out using doubles. The rounding (truncating) truncating errors case problems
    //with navigation (up/down, end line). Below are methods that return exact double-based rectangle
    //for the given position, and also double-based viewToModel method:
    static Rectangle2D modelToView(JTextComponent tc, int pos) throws BadLocationException {
	return modelToView(tc, pos, Position.Bias.Forward);
    }

    static Rectangle2D modelToView(JTextComponent tc, int pos, Position.Bias bias) throws BadLocationException {
	Document doc = tc.getDocument();
	if (doc instanceof AbstractDocument) {
	    ((AbstractDocument)doc).readLock();
	}
	try {
	    Rectangle alloc = getVisibleEditorRect(tc);
	    if (alloc != null) {
                View rootView = tc.getUI().getRootView(tc);
		rootView.setSize(alloc.width, alloc.height);
		Shape s = rootView.modelToView(pos, alloc, bias);
		if (s != null) {
		  return s.getBounds2D();
		}
	    }
	} finally {
	    if (doc instanceof AbstractDocument) {
		((AbstractDocument)doc).readUnlock();
	    }
	}
	return null;
    }

    private static Position.Bias[] discardBias = new Position.Bias[1];
    static int viewToModel(JTextComponent tc, double x, double y) {
	return viewToModel(tc, x, y, discardBias);
    }

    static int viewToModel(JTextComponent tc, double x, double y, Position.Bias[] biasReturn) {
	int offs = -1;
	Document doc = tc.getDocument();
	if (doc instanceof AbstractDocument) {
	    ((AbstractDocument)doc).readLock();
	}
	try {
	    Rectangle alloc = getVisibleEditorRect(tc);
	    if (alloc != null) {
                View rootView = tc.getUI().getRootView(tc);
                View documentView = rootView.getView(0);
                if (documentView instanceof EditorView) {
                    documentView.setSize(alloc.width, alloc.height);
                    offs = ((EditorView) documentView).viewToModelChecked(x, y, alloc, biasReturn);
                } else {
                    rootView.setSize(alloc.width, alloc.height);
                    offs = rootView.viewToModel((float) x, (float) y, alloc, biasReturn);
                }
	    }
	} finally {
	    if (doc instanceof AbstractDocument) {
		((AbstractDocument)doc).readUnlock();
	    }
	}
        return offs;
    }
    
    private static Rectangle getVisibleEditorRect(JTextComponent tc) {
	Rectangle alloc = tc.getBounds();
	if ((alloc.width > 0) && (alloc.height > 0)) {
	    alloc.x = alloc.y = 0;
	    Insets insets = tc.getInsets();
	    alloc.x += insets.left;
	    alloc.y += insets.top;
	    alloc.width -= insets.left + insets.right;
	    alloc.height -= insets.top + insets.bottom;
	    return alloc;
	}
	return null;
    }

    /**
     * Creates a single line editor pane. Can be called from AWT event thread only.
     *
     * @param mimeType The mimetype of the editor's content.
     *
     * @return Two components, the first one is a visual <code>JComponent</code> and
     *   the second one is the editor <code>JTextComponent</code>.
     * @throws IllegalArgumentException when EditorKit is not found for the given mime type.
     *
     * @since 2.7
     */
    public static JComponent [] createSingleLineEditor(String mimeType) throws IllegalArgumentException {
        assert SwingUtilities.isEventDispatchThread()
                : "Utilities.createSingleLineEditor must be called from AWT thread only"; // NOI18N

        EditorKit kit = MimeLookup.getLookup(mimeType).lookup(EditorKit.class);
        if (kit == null) {
            throw new IllegalArgumentException("No EditorKit for '" + mimeType + "' mimetype."); //NOI18N
        }

        final JEditorPane editorPane = new JEditorPane();
        editorPane.putClientProperty(
            "HighlightsLayerExcludes", //NOI18N
            ".*(?<!TextSelectionHighlighting)$" //NOI18N
        );
        editorPane.putClientProperty("AsTextField", Boolean.TRUE);
        editorPane.setEditorKit(kit);
        
        getEditorUI(editorPane).textLimitLineVisible = false;

        KeyStroke enterKs = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
        KeyStroke escKs = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
        KeyStroke tabKs = KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0);
        InputMap im = editorPane.getInputMap();
        im.put(enterKs, NO_ACTION);
        im.put(escKs, NO_ACTION);
        im.put(tabKs, NO_ACTION);

        // The editor pane must not have any insets because otherwise the caret
        // is not positions correctly (maybe a bug somewhere in class DocumentView
        // where `allocation.x` and `allocation.y` are always zero, but should be
        // insets left and top).
        editorPane.setBorder (
            new EmptyBorder (0, 0, 0, 0)
        );

        JTextField referenceTextField = new JTextField("M"); //NOI18N
        Set<AWTKeyStroke> tfkeys = referenceTextField.getFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS);
        editorPane.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, tfkeys);
        tfkeys = referenceTextField.getFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS);
        editorPane.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, tfkeys);

        final Insets textFieldInsets = referenceTextField.getInsets();

        // Because the insets include the margin, temporary clear margin to get
        // the real border insets. Subtracting margin from insets does not work
        // correctly because FlatLaf does some scaling on HiDPI screens
        // (getInsets() returns scaled values, but getMargin() unscaled values).
        final Insets oldMargin = referenceTextField.getMargin();
        referenceTextField.setMargin(new Insets(0, 0, 0, 0));
        final Insets borderInsets = referenceTextField.getInsets();
        referenceTextField.setMargin(oldMargin);

        // Compute insets used for scrollpane view, which is textFieldInsets minus
        // borderInsets because the scrollpane gets the border of the textfield.
        final Insets viewInsets = new Insets(
                textFieldInsets.top - borderInsets.top,
                textFieldInsets.left - borderInsets.left,
                textFieldInsets.bottom - borderInsets.bottom,
                textFieldInsets.right - borderInsets.right);

        // This view panel is only needed to have some margin between
        // the scrollpane border and the editor pane.
        final JPanel viewPanel = new JPanel(new BorderLayout()) {
            // overridden so that FlatLaf changes scrollpane border color if editor pane is focused
            @Override
            public boolean hasFocus() {
                return editorPane.hasFocus();
            }
        };
        viewPanel.setBorder(new EmptyBorder(viewInsets));
        viewPanel.setOpaque(true);
        viewPanel.setBackground(referenceTextField.getBackground());
        viewPanel.add(editorPane, BorderLayout.NORTH);

        //logger.fine("createSingleLineEditor(): margin = "+margin+", borderInsets = "+borderInsets);
        final JScrollPane sp = new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_NEVER,
                                               JScrollPane.HORIZONTAL_SCROLLBAR_NEVER) {
            @Override
            public void setViewportView(Component view) {
                adjustScrollPaneSize(this, editorPane);
                super.setViewportView(view);
            }

        };
        editorPane.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if ("editorKit".equals(evt.getPropertyName())) { // NOI18N
                    adjustScrollPaneSize(sp, editorPane);
                }
            }
        });

        // Repaint scrollpane on focus gained/lost to update border color in case
        // the current LaF uses different border color for focused state.
        editorPane.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                sp.repaint();
            }
            @Override
            public void focusLost(FocusEvent e) {
                sp.repaint();
            }
        });

        sp.setBorder(new DelegatingBorder(referenceTextField.getBorder(), borderInsets));
        sp.setBackground(referenceTextField.getBackground());

        int preferredHeight = referenceTextField.getPreferredSize().height;
        Dimension spDim = sp.getPreferredSize();
        spDim.height = preferredHeight;
        sp.setPreferredSize(spDim);
        sp.setMinimumSize(spDim);
        sp.setMaximumSize(spDim);

        sp.setViewportView(viewPanel);

        final DocumentListener manageViewListener = new ManageViewPositionListener(editorPane, sp);
        DocumentUtilities.addDocumentListener(editorPane.getDocument(), manageViewListener, DocumentListenerPriority.AFTER_CARET_UPDATE);
        editorPane.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if ("document".equals(evt.getPropertyName())) { // NOI18N
                    Document oldDoc = (Document) evt.getOldValue();
                    if (oldDoc != null) {
                        DocumentUtilities.removeDocumentListener(oldDoc, manageViewListener, DocumentListenerPriority.AFTER_CARET_UPDATE);
                    }
                    Document newDoc = (Document) evt.getNewValue();
                    if (newDoc != null) {
                        DocumentUtilities.addDocumentListener(newDoc, manageViewListener, DocumentListenerPriority.AFTER_CARET_UPDATE);
                    }
                }
            }
        });

        return new JComponent [] { sp, editorPane };
    }
    
    /**Don't use unless you know what you are doing.
     * 
     * @since 3.34
     */
    public static <T> T runWithOnSaveTasksDisabled(Mutex.Action<T> run) {
        return BeforeSaveTasks.runWithOnSaveTasksDisabled(run);
    }

    private static void adjustScrollPaneSize(JScrollPane sp, JEditorPane editorPane) {
        int height;
        //logger.fine("createSingleLineEditor(): editorPane's margin = "+editorPane.getMargin());
        //logger.fine("createSingleLineEditor(): editorPane's insets = "+editorPane.getInsets());
        Dimension prefSize = sp.getPreferredSize();
        Insets borderInsets = sp.getBorder().getBorderInsets(sp);//sp.getInsets();
        EditorUI eui = org.netbeans.editor.Utilities.getEditorUI(editorPane);
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("createSingleLineEditor(): editor UI = "+eui);
            if (eui != null) {
                logger.fine("createSingleLineEditor(): editor UI's line height = "+eui.getLineHeight());
                logger.fine("createSingleLineEditor(): editor UI's line ascent = "+eui.getLineAscent());
            }
        }
        if (eui != null) {
            height = eui.getLineHeight();
            if (height < eui.getLineAscent()) {
                height = (eui.getLineAscent()*4)/3; // Hack for the case when line height = 1
            }
        } else {
            java.awt.Font font = editorPane.getFont();
            java.awt.FontMetrics fontMetrics = editorPane.getFontMetrics(font);
            height = fontMetrics.getHeight();
            //logger.fine("createSingleLineEditor(): editor's font = "+font+" with metrics = "+fontMetrics+", leading = "+fontMetrics.getLeading());
            //logger.fine("createSingleLineEditor(): font's height = "+height);
        }
        height += getLFHeightAdjustment();
        //height += 2; // 2 for border
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("createSingleLineEditor(): border vertical insets = "+borderInsets.bottom+" + "+borderInsets.top);
            logger.fine("createSingleLineEditor(): computed height = "+height+", prefSize = "+prefSize);
        }
        if (prefSize.height < height) {
            prefSize.height = height;
            sp.setPreferredSize(prefSize);
            sp.setMinimumSize(prefSize);
            sp.setMaximumSize(prefSize);
            java.awt.Container c = sp.getParent();
            logger.fine("createSingleLineEditor(): setting a new height of ScrollPane = "+height);
            if (c instanceof JComponent) {
                ((JComponent) c).revalidate();
            }
        }
    }

    private static int getLFHeightAdjustment() {
        LookAndFeel lf = UIManager.getLookAndFeel();
        String lfID = lf.getID();
        logger.fine("createSingleLineEditor(): current L&F = '"+lfID+"'");
        if ("Metal".equals(lfID)) {
            return 0;
        }
        if ("GTK".equals(lfID)) {
            return 2;
        }
        if ("Motif".equals(lfID)) {
            return 3;
        }
        if ("Nimbus".equals(lfID)) {
            return 0;
        }
        if ("Aqua".equals(lfID)) {
            return -2;
        }
        return 0;
    }

    private static final class ManageViewPositionListener implements DocumentListener {

        private JEditorPane editorPane;
        private JScrollPane sp;

        public ManageViewPositionListener(JEditorPane editorPane, JScrollPane sp) {
            this.editorPane = editorPane;
            this.sp = sp;
        }

        @Override
        public void insertUpdate(DocumentEvent e) {
            changed();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            changed();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            changed();
        }

        private void changed() {
            JViewport viewport = sp.getViewport();
            Point viewPosition = viewport.getViewPosition();
            if (viewPosition.x > 0) {
                try {
                    Rectangle textRect = editorPane.getUI().modelToView(editorPane, editorPane.getDocument().getLength());
                    int textLength = textRect.x + textRect.width;
                    int viewLength = viewport.getExtentSize().width;
                    //System.out.println("Utilities.createSingleLineEditor(): spLength = "+sp.getSize().width+", viewLength = "+viewLength+", textLength = "+textLength+", viewPosition = "+viewPosition);
                    if (textLength < (viewPosition.x + viewLength)) {
                        viewPosition.x = Math.max(textLength - viewLength, 0);
                        viewport.setViewPosition(viewPosition);
                        //System.out.println("Utilities.createSingleLineEditor(): setting new view position = "+viewPosition);
                    }
                } catch (BadLocationException blex) {
                    Exceptions.printStackTrace(blex);
                }
            }
        }
    }

    private static final class DelegatingBorder implements Border {

        private Border delegate;
        private Insets insets;

        public DelegatingBorder(Border delegate, Insets insets) {
            this.delegate = delegate;
            this.insets = insets;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            //logger.fine("Delegate paintBorder("+c+", "+g+", "+x+", "+y+", "+width+", "+height+")");
            delegate.paintBorder(c, g, x, y, width, height);
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return insets;
        }

        @Override
        public boolean isBorderOpaque() {
            return delegate.isBorderOpaque();
        }

    }

    private static final String NO_ACTION = "no-action"; //NOI18N
}
