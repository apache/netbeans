/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */

package org.netbeans.modules.editor.lib2.document;

import java.util.ArrayList;
import java.util.List;
import javax.swing.event.DocumentEvent;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.Position;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.undo.UndoableEdit;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.netbeans.lib.editor.util.swing.GapBranchElement;

/**
 * Line root element implementation.
 *
 * @author Miloslav Metelka
 * @since 1.46
 */

public final class LineRootElement extends GapBranchElement {
    
    private static final String NAME
        = AbstractDocument.SectionElementName;
    
    private Document doc;
    
    public LineRootElement(Document doc) {
        this.doc = doc;
        assert (doc.getLength() == 0) : "Cannot start with non-empty document"; // NOI18N
        Position startPos = doc.getStartPosition();
        assert (startPos.getOffset() == 0) : "Document.getStartPosition()=" + startPos + " != 0";
        Position endPos = doc.getEndPosition();
        assert (endPos.getOffset() == 1) : "Document.getEndPosition()=" + endPos + " != 1";
        Element line = new LineElement(this, startPos, endPos);
        replace(0, 0, new Element[]{ line });
    }
    
    @Override
    public Element getElement(int index) {
        if (index < 0) {
            throw new IndexOutOfBoundsException("Invalid line index=" + index + " < 0"); // NOI18N
        }
        int elementCount = getElementCount();
        if (index >= elementCount) {
            throw new IndexOutOfBoundsException("Invalid line index=" + index // NOI18N
                + " >= lineCount=" + elementCount); // NOI18N
        }
        
        return super.getElement(index);
    }
    
    @Override
    public int getElementIndex(int offset) {
        if (offset == 0) {
            // Optimization of a frequent pattern in NB sources:
            // LineRootElement root = styledDocument.getParagraphElement(0).getParentElement()
            return 0;
        }
        return super.getElementIndex(offset);
    }
    
    public void insertUpdate(DocumentEvent evt, UndoableEdit edit, AttributeSet attr) {
        int insertOffset = evt.getOffset();
        int insertEndOffset = insertOffset + evt.getLength();
        CharSequence text = DocumentUtilities.getText(doc);
        if (insertOffset > 0) { // [Swing] marks (and elements) at offset zero do not move up
            insertOffset--;
        }
        try {
            int index = -1; // Index of the elements modification - computed lazily
            List<LineElement> addedLines = null; // Collected added lines
            LineElement removedLine = null; // Removed line element
            Position lastAddedLineEndPos = null;
            for (int offset = insertOffset; offset < insertEndOffset; offset++) {
                if (text.charAt(offset) == '\n') {
                    if (index == -1) { // Not computed yet
                        index = getElementIndex(offset);
                        removedLine = (LineElement)getElement(index);
                        lastAddedLineEndPos = removedLine.getStartPosition();
                        addedLines = new ArrayList<LineElement>(2);
                    }
                    Position lineEndPos = doc.createPosition(offset + 1);
                    addedLines.add(new LineElement(this, lastAddedLineEndPos, lineEndPos));
                    lastAddedLineEndPos = lineEndPos;
                }
            }
            if (index != -1) { // Some lines were added
                // If the text was inserted at the line boundary i.e. right after existing '\n'
                // and the ending char of the inserted text was not '\n' (otherwise
                // it would be a "clean" line insert) then there must be two line elements
                // removed.
                Position removedLineEndPos = removedLine.getEndPosition();
                int removedLineEndOffset = removedLineEndPos.getOffset();
                Element[] removed; // removed line elements
                int lastAddedLineEndOffset = lastAddedLineEndPos.getOffset();
                if (insertEndOffset == removedLineEndOffset
                        && lastAddedLineEndOffset != removedLineEndOffset
//                        && index + 1 < getElementCount()
                ) {
                    LineElement removedLine2 = (LineElement)getElement(index + 1);
                    removed = new Element[] { removedLine, removedLine2 };
                    removedLineEndPos = removedLine2.getEndPosition();
                    removedLineEndOffset = removedLineEndPos.getOffset();
                } else { // just one line removed
                    removed = new Element[] { removedLine };
                }
                if (lastAddedLineEndOffset < removedLineEndOffset) {
                    addedLines.add(new LineElement(this, lastAddedLineEndPos, removedLineEndPos));
                }

                Element[] added = new Element[addedLines.size()];
                addedLines.toArray(added);

                edit.addEdit(new Edit(index, removed, added));
                replace(index, removed.length, added);
            }
        } catch (BadLocationException e) {
            throw new IllegalStateException(e.toString());
        }
    }
    
    public void removeUpdate(DocumentEvent evt, UndoableEdit edit2) {
        UndoableEdit edit = legacyRemoveUpdate(evt);
        if (edit != null) {
            edit2.addEdit(edit);
        }
    }
    
    public UndoableEdit legacyRemoveUpdate(DocumentEvent evt) {
        // The algorithm here is similar to the one in PlainDocument.removeUpdate().
        // Unfortunately in case exactly a line element (or multiple line elements)
        // the algorithm removes extra line that follows the end of removed area.
        // That could be improved but compatibility with PlainDocument would be lost.
        
        int removeOffset = evt.getOffset();
        int removeEndOffset = removeOffset + evt.getLength();
        int line0 = getElementIndex(removeOffset);
        int line1 = getElementIndex(removeEndOffset);
        if (line0 != line1) {
            // at least one line was removed
            line1++; // will remove the line where remove ends as well
            Element[] removed = new Element[line1 - line0];
            copyElements(line0, line1, removed, 0);
            Element[] added = new Element[] {
                new LineElement(this,
                    ((LineElement)removed[0]).getStartPosition(),
                    ((LineElement)removed[removed.length - 1]).getEndPosition()
                )
            };
            
            Edit edit = new Edit(line0, removed, added);
            replace(line0, removed.length, added);
            return edit;
        }
        return null;
    }

    @Override
    public Document getDocument() {
        return doc;
    }
    
    @Override
    public Element getParentElement() {
        return null;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public AttributeSet getAttributes() {
        // Do not return null since Swing's view factories assume that this is non-null.
        return SimpleAttributeSet.EMPTY;
    }

    @Override
    public int getStartOffset() {
        return 0;
    }

    @Override
    public int getEndOffset() {
        return doc.getLength() + 1;
    }

}
