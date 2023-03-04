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

package org.netbeans.editor;

import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import javax.swing.event.DocumentEvent;
import javax.swing.text.Element;
import javax.swing.text.Document;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoableEdit;
import javax.swing.text.BadLocationException;
import javax.swing.text.Segment;
import org.netbeans.modules.editor.lib2.document.LineElement;

/**
 * Undoable edit that fixes syntax state infos
 * (stored at beginings of lines)
 * after each document modification.
 *
 * <p>
 * As the syntax state infos can only be fixed
 * AFTER the line elements get fixed there are
 * actually two instances of this class created
 * for each document modification.
 * One instance triggers the update during
 * regular modification or redo
 * (inserted after line elements undoable edit)
 * and the other one during undo operations
 * (inserted before line elements undoable edit).
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

final class FixLineSyntaxState {
    
    private static final boolean debug = false;
    
    private final DocumentEvent evt;
    
    private int syntaxUpdateOffset;
        
    private List syntaxUpdateTokenList = Collections.EMPTY_LIST;
    
    
    FixLineSyntaxState(DocumentEvent evt) {
        this.evt = evt;
    }
    
    final int getSyntaxUpdateOffset() {
        return syntaxUpdateOffset;
    }
        
    final List getSyntaxUpdateTokenList() {
        return syntaxUpdateTokenList;
    }


    static void invalidateAllSyntaxStateInfos(BaseDocument doc) {
        Element lineRoot = getLineRoot(doc);
        int elemCount = lineRoot.getElementCount();
        for (int i = elemCount - 1; i >= 0; i--) {
            LineElement line = (LineElement) lineRoot.getElement(i);
            line.legacySetAttributesObject(null);
        }
    }
    
    /** Prepare syntax scanner so that it's ready to scan from requested
     * position.
     * @param text text segment to be used. Method ensures it will
     *  be filled so that <CODE>text.array</CODE> contains the character data
     *  <BR><CODE>text.offset</CODE> logically points to <CODE>reqPos</CODE>
     *  <BR><CODE>text.count</CODE> equals to <CODE>reqLen</CODE>.
     * @param syntax syntax scanner to be used
     * @param reqPos position to which the syntax should be prepared
     * @param reqLen length that will be scanned by the caller after the syntax 
     *   is prepared. The prepareSyntax() automatically preloads this area
     *   into the given text segment.
     * @param forceLastBuffer force the syntax to think that the scanned area is the last
     *  in the document. This is useful for forcing the syntax to process all the characters
     *  in the given area.
     * @param forceNotLastBuffer force the syntax to think that the scanned area is NOT
     *  the last buffer in the document. This is useful when the syntax will continue
     *  scanning on another buffer.
     */
    static void prepareSyntax(BaseDocument doc, Segment text, Syntax syntax, int reqPos, int reqLen,
    boolean forceLastBuffer, boolean forceNotLastBuffer) throws BadLocationException {

        if (reqPos < 0 || reqLen < 0 || reqPos + reqLen > doc.getLength()) {
            throw new BadLocationException("reqPos=" + reqPos // NOI18N
                + ", reqLen=" + reqLen + ", doc.getLength()=" + doc.getLength(), // NOI18N
                -1 // getting rid of it
            );
        }

        // Find line element that covers the reqPos
        Element lineRoot = getLineRoot(doc);
        int reqPosLineIndex = lineRoot.getElementIndex(reqPos);
        Element reqPosLineElem = lineRoot.getElement(reqPosLineIndex);
        Syntax.StateInfo stateInfo = getValidSyntaxStateInfo(doc, reqPosLineIndex);
        int lineStartOffset = reqPosLineElem.getStartOffset();
        int preScan = (stateInfo != null) ? stateInfo.getPreScan() : 0;
//        if (debug) {
//            /*DEBUG*/System.err.println("prepareSyntax(): reqPos=" + reqPos + ", reqLen=" + reqLen
//                + ", lineIndex=" + reqPosLineIndex
//                + ", lineStartOffset=" + lineStartOffset
//                + ", preScan=" + preScan
//            );
//        }
        if (preScan > lineStartOffset) {
            // an error is occurring - preScan should not reach prior to document start
            if (debug) {
                /*DEBUG*/System.err.println(lineInfosToString(doc));
            }
            preScan = lineStartOffset; // Fix the invalid preScan
        }
        // load syntax segment
        int intraLineLength = reqPos - lineStartOffset;
        doc.getText(lineStartOffset - preScan, preScan + intraLineLength + reqLen, text);
        text.offset += preScan;
        text.count -= preScan;

        // load state into syntax scanner - will scan from mark up to reqPos
        syntax.load(stateInfo, text.array, text.offset, intraLineLength, false, reqPos);
// [CAUTION] instead of false used to be forceNotLastBuffer ? false : (reqPos >= docLen)

        // ignore tokens until reqPos is reached
        while (syntax.nextToken() != null) { }

        text.offset += intraLineLength;
        text.count -= intraLineLength;
        boolean forceLB = forceNotLastBuffer
            ? false
            : (forceLastBuffer || (reqPos + reqLen >= doc.getLength()));

        syntax.relocate(text.array, text.offset, text.count, forceLB, reqPos + reqLen);
    }

    /**
     * Make sure that the line element with the requested line index
     * has the valid syntax state info and return it.
     * <br>
     * Method first checks whether the line elemnt for the requested line index
     * has valid syntax state info. If not it goes back through the previous lines
     * until it finds a line element with valid state info. After that
     * it starts lexing and updates syntax infos in line elements until
     * it reaches originally requested line index.
     * <br>
     * For the first line the <code>null</code> state info is returned.
     */
    private static Syntax.StateInfo getValidSyntaxStateInfo(
    BaseDocument doc, int lineIndex) throws BadLocationException {
        
        if (lineIndex == 0) {
            return null;
        }

        Element lineRoot = getLineRoot(doc);
        LineElement lineElem = (LineElement)lineRoot.getElement(lineIndex);
        Syntax.StateInfo stateInfo = (Syntax.StateInfo) lineElem.legacyGetAttributesObject();

        if (lineIndex > 0 && stateInfo == null) { // need to update
            // Find the last line with the valid state info
            int validLineIndex = lineIndex - 1; // is >= 0
            LineElement validLineElem = null;
            while (validLineIndex > 0) {
                validLineElem = (LineElement) lineRoot.getElement(validLineIndex);
                stateInfo = (Syntax.StateInfo) validLineElem.legacyGetAttributesObject();
                if (stateInfo != null) {
                    break;
                }
                validLineIndex--;
            }

            /* validLineIndex now contains index of last line
             * that has valid syntax state info. Or it's zero (always valid).
             * stateInfo contains state info of last valid line
             * or undefined value if validLineIndex == 0.
             * validLineElem contains valid line element
             * or undefined value if validLineIndex == 0.
             */

            Segment text = new Segment();
            Syntax syntax = doc.getFreeSyntax();
            try {
                int lineElemOffset = lineElem.getStartOffset();
                int preScan = 0;
                int validLineOffset;
                if (validLineIndex > 0) {
                    validLineOffset = validLineElem.getStartOffset();
                    preScan = stateInfo.getPreScan();
                } else { // validLineIndex == 0
                    validLineOffset = 0;
                    stateInfo = null;
                }

                doc.getText(validLineOffset - preScan,
                    (lineElemOffset - validLineOffset) + preScan,
                    text
                );

                text.offset += preScan;
                text.count -= preScan;
                /* text segment contains all the required data including preScan
                 * but "officially" it points to validLineOffset offset.
                 */

                syntax.load(stateInfo, text.array, text.offset,
                    text.count, false, lineElemOffset);

                int textEndOffset = text.offset + text.count;
                do {
                    validLineIndex++;
                    validLineElem = (LineElement)lineRoot.getElement(validLineIndex);
                    int scanLength = validLineOffset; // get orig value
                    validLineOffset = validLineElem.getStartOffset();
                    scanLength = validLineOffset - scanLength;
                    syntax.relocate(text.array, syntax.getOffset(),
                        scanLength,
                        false, validLineOffset
                    );

                    while (syntax.nextToken() != null) {
                        // ignore returned tokens
                    }

                    
                    updateSyntaxStateInfo(syntax, validLineElem);

                } while (validLineIndex != lineIndex);

            } finally {
                doc.releaseSyntax(syntax);
            }
        }
        
        return (Syntax.StateInfo) lineElem.legacyGetAttributesObject();
    }
    
    static void updateSyntaxStateInfo(Syntax syntax, LineElement lineElement) {
        Syntax.StateInfo syntaxStateInfo = (Syntax.StateInfo) lineElement.legacyGetAttributesObject();
        if (syntaxStateInfo == null) {
            syntaxStateInfo = syntax.createStateInfo();
            assert (syntaxStateInfo != null);
            lineElement.legacySetAttributesObject(syntaxStateInfo);
        }
        syntax.storeState(syntaxStateInfo);
    }

    void update(boolean undo) {
        SyntaxUpdateTokens suTokens = (SyntaxUpdateTokens)evt.getDocument().getProperty(SyntaxUpdateTokens.class);
        if (suTokens != null) {
            suTokens.syntaxUpdateStart();
        }
        try {
            // Update syntax state infos (based on updated text and line structures
            syntaxUpdateOffset = fixSyntaxStateInfos(undo);
        } finally {
            if (suTokens != null) {
                syntaxUpdateTokenList = Collections.unmodifiableList(
                    new ArrayList(suTokens.syntaxUpdateEnd()));
            }
        }
    }

    /**
     * Fix state infos after insertion/removal.
     * @param offset offset of the modification
     * @param length length of the modification. It's lower than zero for removals.
     * @return offset of the last line where the syntax stateinfo was modified.
     */
    private int fixSyntaxStateInfos(boolean undo) {
        int offset = evt.getOffset();
        if (offset < 0) {
            throw new IllegalStateException("offset=" + offset); // NOI18N
        }

        BaseDocument doc = (BaseDocument)evt.getDocument();
        Element lineRoot = getLineRoot(doc);
        int lineCount = lineRoot.getElementCount();
        DocumentEvent.ElementChange lineChange = evt.getChange(lineRoot);
        int lineIndex;
        if (lineChange != null) {
            lineIndex = lineChange.getIndex();
        } else { // no change in line elements
            lineIndex = lineRoot.getElementIndex(offset);
        }
        
        // As done in AbstractDocument.ElementEdit.undo()/redo()
        // the childrenAdded and childrenRemoved fields
        // are switched during undo()/redo() so in fact
        // the added line elements should be always found
        // by getChildrenAdded().
        int addedLinesCount = (lineChange != null)
            ? lineChange.getChildrenAdded().length
            : 0;
        
        int maybeMatchLineIndex = lineIndex + addedLinesCount + 1;
        if (lineIndex > 0) {
            lineIndex--; // Move to previous line
        }
        if (lineIndex + 1 == lineCount) { // on last line -> no fixing
            return doc.getLength();
        }

        LineElement lineElem = (LineElement)lineRoot.getElement(lineIndex);
        
        Segment text = new Segment();
        try {
            Syntax.StateInfo stateInfo = getValidSyntaxStateInfo(doc, lineIndex);
            int lineStartOffset = lineElem.getStartOffset();
            int preScan = (stateInfo != null) ? stateInfo.getPreScan() : 0;
            
            if (debug) {
                /*DEBUG*/System.err.println("fixSyntaxStateInfos(): lineIndex=" + lineIndex
                    + ", maybeMatch=" + maybeMatchLineIndex // NOI18N
                    + ", lineStartOffset=" + lineStartOffset // NOI18N
                    + ", preScan=" + preScan // NOI18N
                    + ", addedLines=" + addedLinesCount // NOI18N
                    + ", lineCount=" + lineCount // NOI18N
                );
            }

            Syntax syntax = doc.getFreeSyntax();
            try {
                lineIndex++; // line index now points to line that follows the modified one
                LineElement nextLineElem = (LineElement)lineRoot.getElement(lineIndex); // should be valid
                int nextLineStartOffset = nextLineElem.getStartOffset();

                int len = (nextLineStartOffset - lineStartOffset) + preScan;
                if (len < 0) {
                    throw new IndexOutOfBoundsException("len=" + len + " < 0: nextLineStartOffset=" + // NOI18N
                            nextLineStartOffset + ", lineStartOffset=" + lineStartOffset + ", preScan=" + preScan); // NOI18N
                }
                doc.getText(lineStartOffset - preScan, len, text);

                text.offset += preScan;
                text.count -= preScan;

                syntax.load(stateInfo, text.array, text.offset, text.count,
                    false, nextLineStartOffset);

                SyntaxUpdateTokens suTokens = (SyntaxUpdateTokens)doc.getProperty(
                    SyntaxUpdateTokens.class);
                /* Fix of #39446 - slow editing of long comments
                 * Numerous doc.getText() have to be eliminated
                 * because they span gap in the content character buffer
                 * so character copying is being done.
                 * The area retrieved from the document is made wider
                 * than necessary and doubled
                 * with each next state info being fixed.
                 * This ensures maximum of log2(doc.getLength())
                 * doc.geText() operations.
                 */
                int textLength = -1;
                int textStartOffset = -1;
                int textBufferStartOffset = -1;
                while (true) {
                    // Go through all the found relexed tokens
                    int tbStartOffset = lineStartOffset - text.offset;
                    TokenID tokenID = syntax.nextToken();
                    while (tokenID != null) {
                        if (suTokens != null) {
                           // Report each relexed token
                            suTokens.syntaxUpdateToken(tokenID,
                                syntax.getTokenContextPath(),
                                tbStartOffset + syntax.getTokenOffset(),
                                syntax.getTokenLength()
                            );
                        }

                        tokenID = syntax.nextToken();
                    }

                    stateInfo = (Syntax.StateInfo) nextLineElem.legacyGetAttributesObject(); // original state info
                    if (lineIndex >= maybeMatchLineIndex) {
                        if (stateInfo != null 
                            && syntax.compareState(stateInfo) == Syntax.EQUAL_STATE
                        ) {
                            // Matched at the begining of nextLineElem
                            // therefore use nextLineStartOffset as the matching offset
                            lineStartOffset = nextLineStartOffset;
                            if (debug) {
                                /*DEBUG*/System.err.println("fixSyntaxStateInfos(): MATCHED at lineIndex="
                                    + lineIndex
                                    + ": preScan=" + syntax.getPreScan() // NOI18N
                                    + ", return lineStartOffset=" + lineStartOffset // NOI18N
                                );
                            }
                            break;
                        }
                    }
                    
                    updateSyntaxStateInfo(syntax, nextLineElem);
                    if (debug) {
                        /*DEBUG*/System.err.println("fixSyntaxStateInfos(): Updated info at line "
                            + lineIndex + " from " + stateInfo // NOI18N
                            + " to " + (Syntax.StateInfo) nextLineElem.legacyGetAttributesObject()); // NOI18N
                    }
                    
                    lineIndex++;
                    if (lineIndex >= lineCount) { // still not match at begining of last line
                        return doc.getLength(); 
                   }

                    lineElem = nextLineElem;
                    lineStartOffset = nextLineStartOffset;

                    nextLineElem = (LineElement)lineRoot.getElement(lineIndex);
                    nextLineStartOffset = nextLineElem.getStartOffset();
                    
                    preScan = syntax.getPreScan();
                    
                    /* (testing disabled)
                    Segment checkText = new Segment(); // Construct segment for testing
                    doc.getText(lineStartOffset - preScan,
                        (nextLineStartOffset - lineStartOffset) + preScan, checkText);
                     */
                    
                    int requestedTextLength = (nextLineStartOffset - lineStartOffset) + preScan;
                    // Fixed #39446 - slow editing of long comments
                    if (textLength == -1) { // not retrieved yet
                        textStartOffset = lineStartOffset - preScan;
                        textLength = requestedTextLength;
                        if (textLength < 0) {
                            throw new IndexOutOfBoundsException("len=" + textLength + " < 0: nextLineStartOffset=" + // NOI18N
                                nextLineStartOffset + ", lineStartOffset=" + lineStartOffset + ", preScan=" + preScan); // NOI18N
                        }
                        doc.getText(textStartOffset, textLength, text);
                        textBufferStartOffset = textStartOffset - text.offset;
                        
                    } else { // already retrieved previously

                        if (lineStartOffset - preScan < textStartOffset
                            || nextLineStartOffset > textStartOffset + textLength
                        ) { // outside of boundaries => another getText() must be done
                            textLength = Math.max(textLength, requestedTextLength);
                            textLength *= 2; // double to get logarithmic number of getText() calls
                            textStartOffset = lineStartOffset - preScan;
                            textLength = Math.min(textStartOffset + textLength,
                                doc.getLength()) - textStartOffset;
                            doc.getText(textStartOffset, textLength, text);
                            textBufferStartOffset = textStartOffset - text.offset;
                            // text.offset OK, update text.count

                        } else { //  the current text segment contains enough data
                            text.offset = lineStartOffset - preScan - textBufferStartOffset;
                        }
                        text.count = requestedTextLength;
                    }
                    
                    
                    /* (testing disabled)
                    // Verify that the characters are the same in both segments
                    if (checkText.count != text.count) {
                        throw new IllegalStateException();
                    }
                    for (int i = 0; i < checkText.count; i++) {
                        if (checkText.array[checkText.offset + i] != text.array[text.offset + i]) {
                            throw new IllegalStateException();
                        }
                    }
                    */

                    text.offset += preScan;
                    text.count -= preScan;
                    
                    syntax.relocate(text.array, text.offset, text.count,
                        false, nextLineStartOffset);
                }
                
                return lineStartOffset;
                
            } finally {
                doc.releaseSyntax(syntax);

                // The consistency check can fail although the actual state
                // is legal (there are some null states) - see #47484
                //checkConsistency(doc);
            }
        } catch (BadLocationException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * @param offset to be examined.
     * @return offset that will be high enough to ensure that the given offset
     *  will be covered by token that can be returned from the syntax.nextToken()
     *  assuming that the syntax will be prepared with the returned token.
     *  <BR>It's not guaranteed how much bigger the returned offset will be.
     */
    static int getTokenSafeOffset(BaseDocument doc, int offset) {
        if (offset == 0) { // no valid state-info at offset 0
            return offset;
        }

        try {
            Element lineRoot = getLineRoot(doc);
            int lineIndex = lineRoot.getElementIndex(offset);
            Element lineElem = lineRoot.getElement(lineIndex);
            int lineStartOffset = lineElem.getStartOffset();
            Syntax.StateInfo stateInfo = getValidSyntaxStateInfo(doc, lineIndex);
            if (offset == lineStartOffset && stateInfo.getPreScan() == 0) {
                // can be done with the given offset
                return offset;
            }

            // go to next line and maybe further for tokens
            // crossing several lines
            int lineCount = lineRoot.getElementCount();
            while (++lineIndex < lineCount) {
                lineElem = lineRoot.getElement(lineIndex);
                stateInfo = getValidSyntaxStateInfo(doc, lineIndex);
                lineStartOffset = lineElem.getStartOffset();
                if (lineStartOffset - stateInfo.getPreScan() >= offset) {
                    return lineStartOffset;
                }
            }
        } catch (BadLocationException e) {
            throw new IllegalStateException(e.toString());
        }

        return doc.getLength();
    }
    
    private static Element getLineRoot(Document doc) {
        return doc.getDefaultRootElement();
    }

    private static void checkConsistency(Document doc) {
        // Check whether all syntax state infos (except for the first line) are non-null
        Element lineRoot = getLineRoot(doc);
        int lineCount = lineRoot.getElementCount();
        for (int i = 1; i < lineCount; i++) { // skip the very first line
            LineElement elem = (LineElement)lineRoot.getElement(i);
            assert ((Syntax.StateInfo) elem.legacyGetAttributesObject() != null) :
                    "Syntax state null at line " + i + " of " + lineCount; // NOI18N
        }
    }
    
    public static String lineInfosToString(Document doc) {
        StringBuffer sb = new StringBuffer();
        Element lineRoot = getLineRoot(doc);
        int lineCount = lineRoot.getElementCount();
        for (int i = 0; i < lineCount; i++) {
            LineElement elem = (LineElement)lineRoot.getElement(i);
            sb.append("[" + i + "]: lineStartOffset=" + elem.getStartOffset() // NOI18N
                + ", info: " + (Syntax.StateInfo) elem.legacyGetAttributesObject() + "\n"); // NOI18N
        }
        return sb.toString();
    }
    
    UndoableEdit createBeforeLineUndo() {
        return new BeforeLineUndo();
    }
    
    UndoableEdit createAfterLineUndo() {
        return new AfterLineUndo();
    }

    final class BeforeLineUndo extends AbstractUndoableEdit {
        
        FixLineSyntaxState getMaster() {
            return FixLineSyntaxState.this;
        }
        
        public void undo() throws CannotUndoException {
            update(true);
            super.undo();
        }
        
    }
    
    final class AfterLineUndo extends AbstractUndoableEdit {
        
        public void redo() throws CannotRedoException {
            update(false);
            super.redo();
        }
        
    }
}
