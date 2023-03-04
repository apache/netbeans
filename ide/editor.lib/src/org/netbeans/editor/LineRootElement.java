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

import javax.swing.text.AbstractDocument;
import javax.swing.text.Element;
import javax.swing.text.Document;
import javax.swing.text.AttributeSet;
import javax.swing.text.Position;
import javax.swing.undo.UndoableEdit;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyleContext;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.netbeans.lib.editor.util.swing.GapBranchElement;
import org.openide.ErrorManager;

/**
 * Line root element implementation.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

final class LineRootElement extends GapBranchElement {
    
    private static final LineElement[] EMPTY_LINE_ELEMENT_ARRAY = new LineElement[0];
 
    private static final String NAME
        = AbstractDocument.SectionElementName;
    
    private BaseDocument doc;
    
    private LineElement[] addedLines = EMPTY_LINE_ELEMENT_ARRAY;
    
    LineRootElement(BaseDocument doc) {
        this.doc = doc;

        assert (doc.getLength() == 0) : "Cannot start with non-empty document"; // NOI18N
        
        Position startPos = doc.getStartPosition();
        assert (startPos.getOffset() == 0) : "Document.getStartPosition() != 0"; // NOI18N

        Position endPos = doc.getEndPosition();
        assert (endPos.getOffset() == 1) : "Document.getEndPosition() != 1"; // NOI18N

        Element line = new LineElement(this, startPos, endPos);
        replace(0, 0, new Element[]{ line });

        assert (getElement(0) != null);
    }
    
    /**
     * Double size of the addedLines array and return
     * the index value that corresponds to the original zero index.
     */
    private int doubleAddedLinesCapacity() {
        int addedLinesLength = addedLines.length;
        int newCapacity = Math.max(4, addedLinesLength * 2);
        LineElement[] newAddedLines = new LineElement[newCapacity];
        // Copy current contents to end of array
        System.arraycopy(addedLines, 0, newAddedLines,
            newCapacity - addedLinesLength, addedLinesLength);
        addedLines = newAddedLines;
        return (newCapacity - addedLinesLength); // value for original index zero
    }
    
    public @Override Element getElement(int index) {
        if (index < 0) {
            throw new IndexOutOfBoundsException("Invalid line index=" + index + " < 0"); // NOI18N
        }
        int elementCount = getElementCount();
        if (index >= elementCount) {
            throw new IndexOutOfBoundsException("Invalid line index=" + index // NOI18N
                + " >= lineCount=" + elementCount); // NOI18N
        }
        
        LineElement elem = (LineElement)super.getElement(index);
        if (elem == null) {
            // if the document is not locked elem may be null even after the initial checks (#159491)
            throw new IndexOutOfBoundsException("Can't find element, index=" + index //NOI18N
                + ", count=" + getElementCount() //NOI18N
                + ", documentLocked=" + (DocumentUtilities.isReadLocked(doc) || DocumentUtilities.isWriteLocked(doc))); //NOI18N
        }

        return elem;
    }

    UndoableEdit insertUpdate(int insertOffset, int insertLength) {
        int lastInsertedCharOffset = insertOffset + insertLength - 1;
        CharSequence text = DocumentUtilities.getText(doc);
        Edit edit = null;
        
        int index = -1; // Index of the elements modification
        Element[] removeElements = null; // Removed line elements
        // Index in the addedLines array - adding from last to first
        int firstAddedLineIndex = addedLines.length; // nothing added yet
        
        int offset = lastInsertedCharOffset;
        // insertAtPrevLineEndOffset - whether the insert was done at the end (after '\n')
        // of a previous line i.e. in fact at a begining of the next line.
        boolean insertAtPrevLineEndOffset;
        int beforeInsertOffset; // in fact Math.max(insertOffset - 1, 0)
        if (insertOffset == 0) { // [swing] marks (and elements) at offset zero do not move up
            beforeInsertOffset = 0;
            insertAtPrevLineEndOffset = false;
        } else { // inserting inside doc
            beforeInsertOffset = insertOffset - 1; // check char before offset for '\n'
            insertAtPrevLineEndOffset = (text.charAt(beforeInsertOffset) == '\n');
        }

        try {
            // Go through all the inserted lines plus the char at beforeInsertOffset (if exists)
            // and create new line elements at every occurrence of '\n'
            Position futureAddedLineEndPos = null;
            while (offset >= beforeInsertOffset) {
                if (text.charAt(offset) == '\n') { // line break at offset
                    boolean addLine = true; // whether line element should be added
                    if (futureAddedLineEndPos == null) {
                        // Find the first line element that will be removed
                        index = getElementIndex(insertOffset);
                        LineElement removeLine = (LineElement)getElement(index);
                        // If inserting at begining of line (insertAtPrevLineEndOffset == true)
                        // and the inserted chars do not end with '\n'
                        // then not only current line must be removed
                        // but the next one as well.
                        if (insertAtPrevLineEndOffset) { // '\n' at (insertOffset - 1)
                            if (offset == lastInsertedCharOffset) { // inserted 'xxx\n' 
                                removeElements = new Element[] { removeLine };
                                futureAddedLineEndPos = removeLine.getEndPosition();
                                addLine = false; // do not add new line in this case
                            } else {
                                LineElement nextRemoveLine = (LineElement)getElement(index + 1);
                                removeElements = new Element[] {
                                    removeLine,
                                    nextRemoveLine
                                };
                                futureAddedLineEndPos = nextRemoveLine.getEndPosition();
                            }
                                
                        } else { // otherwise use the next element as the next for added
                            removeElements = new Element[] { removeLine };
                            futureAddedLineEndPos = removeLine.getEndPosition();
                        }
                    }

                    if (addLine) {
                        if (firstAddedLineIndex == 0) { // no more space to add
                            firstAddedLineIndex = doubleAddedLinesCapacity();
                        }
                        firstAddedLineIndex--; // will fill in added line element soon
                        Position lineStartPos = doc.createPosition(offset + 1);
                        addedLines[firstAddedLineIndex] = new LineElement(
                            this, lineStartPos, futureAddedLineEndPos);
                        futureAddedLineEndPos = lineStartPos;
                    }
                }
                offset--;
            }

            if (futureAddedLineEndPos != null) { // will add (and remove) lines
                // Create array of added lines and add extra one at begining
                int addedLineCount = addedLines.length - firstAddedLineIndex;
                Element[] addElements = new Element[addedLineCount + 1];
                System.arraycopy(addedLines, firstAddedLineIndex, addElements, 1, addedLineCount);
                addElements[0] = new LineElement(
                    this,
                    ((LineElement)removeElements[0]).getStartPosition(),
                    futureAddedLineEndPos
                );

                replace(index, removeElements.length, addElements);
                edit = new Edit(index, removeElements, addElements);
            }

        } catch (BadLocationException e) {
            // Should never happen but in case it happens
            // retain the current consistent state (no replace is done)
            // and report this as serious error
            ErrorManager.getDefault().notify(ErrorManager.ERROR, e);
        }
        // checkConsistency();
        return edit;
    }

    UndoableEdit removeUpdate(int removeOffset, int removeLength) {
        // The algorithm here is similar to the one in PlainDocument.removeUpdate().
        // Unfortunately in case exactly a line element (or multiple line elements)
        // the algorithm removes extra line that follows the end of removed area.
        // That could be improved but compatibility with PlainDocument would be lost.
        Edit edit = null;
        int removeEndOffset = removeOffset + removeLength;
        int line0 = getElementIndex(removeOffset);
        int line1 = getElementIndex(removeEndOffset);
        if (line0 != line1) {
            // at least one line was removed
            line1++; // will remove the line where remove ends as well
            Element[] removeElements = new Element[line1 - line0];
            copyElements(line0, line1, removeElements, 0);
            Element[] addElements = new Element[] {
                new LineElement(this,
                    ((LineElement)removeElements[0]).getStartPosition(),
                    ((LineElement)removeElements[removeElements.length - 1]).getEndPosition()
                )
            };
            
            replace(line0, removeElements.length, addElements);
            edit = new Edit(line0, removeElements, addElements);
        }
        // checkConsistency();
        return edit;
    }

/*    protected void compact() {
        super.compact();
        addedLines = EMPTY_LINE_ELEMENT_ARRAY;
    }
 */

    public Document getDocument() {
        return doc;
    }
    
    public Element getParentElement() {
        return null;
    }

    public String getName() {
        return NAME;
    }

    public AttributeSet getAttributes() {
        return StyleContext.getDefaultStyleContext().getEmptySet();
    }

    public int getStartOffset() {
        return 0;
    }

    public int getEndOffset() {
        return doc.getLength() + 1;
    }

    public @Override int getElementIndex(int offset) {
        if (offset == 0) { // NB uses this frequently to just get the parent
            return 0;
        }

        return super.getElementIndex(offset);
    }

    private void checkConsistency() {
        int lineCount = getElementCount();
        assert (lineCount > 0); // Should be 1 or greater
        int prevLineEndOffset = 0;
        for (int i = 0; i < lineCount; i++) {
            LineElement elem = (LineElement)getElement(i);
            assert (prevLineEndOffset == elem.getStartOffset());
            assert (prevLineEndOffset < elem.getEndOffset())
                : "Line " + i + " of " + lineCount + ": " + lineToString(elem); // NOI18N
            prevLineEndOffset = elem.getEndOffset();
        }
        assert (prevLineEndOffset == (doc.getLength() + 1));
    }
    
    private String lineToString(Element line) {
        return "<" + line.getStartOffset() + ", " // NOI18N
            + line.getEndOffset() + ">"; // NOI18N
    }

}
