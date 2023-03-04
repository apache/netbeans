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

package org.netbeans.modules.editor.guards;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.util.Collections;
import java.util.List;
import javax.swing.text.BadLocationException;
import javax.swing.text.Position;
import javax.swing.text.StyledDocument;
import org.netbeans.api.editor.document.LineDocumentUtils;
import org.netbeans.api.editor.guards.GuardedSection;
import org.netbeans.spi.editor.guards.GuardedRegionMarker;
import org.netbeans.spi.editor.guards.support.AbstractGuardedSectionsProvider.Result;

/** Represents one guarded section.
 */
public abstract class GuardedSectionImpl {
    /** Name of the section. */
    String name;
    
    /** If the section is valid or if it was removed. */
    boolean valid = false;
    
    final GuardedSectionsImpl guards;
    
    GuardedSection guard;
    
    /** Get the name of the section.
     * @return the name
     */
    public String getName() {
        return name;
    }
    
    /** Creates new section.
     * @param name Name of the new section.
     */
    GuardedSectionImpl(String name, GuardedSectionsImpl guards) {
        this.name = name;
        this.guards = guards;
    }
    
    public final void attach(GuardedSection guard) {
        this.guard = guard;
        valid = true;
    }
    
    /** Set the name of the section.
     * @param name the new name
     * @exception PropertyVetoException if the new name is already in use
     */
    public void setName(String name) throws PropertyVetoException {
        if (!this.name.equals(name)) {
            synchronized (this.guards.sections) {
                if (valid) {
                    if (this.guards.sections.get(name) != null)
                        throw new PropertyVetoException("", new PropertyChangeEvent(this, "name", this.name, name)); // NOI18N
                    this.guards.sections.remove(this.name);
                    this.name = name;
                    this.guards.sections.put(name, this);
                }
            }
        }
        
    }
    
    /** Deletes the text of the section and
     * removes it from the table. The section will then be invalid
     * and it will be impossible to use its methods.
     */
    public void deleteSection() {
        synchronized (this.guards.sections) {
            if (valid) {
                try {
                    this.guards.sections.remove(name);
                    // get document should always return the document, when section
                    // is deleted, because it is still valid (and valid is only
                    // when document is loaded.
                    unmarkGuarded(this.guards.getDocument());
                    deleteText();
                    valid = false;
                } catch (BadLocationException e) {
                    throw new IllegalStateException(e);
                }
            }
        }
    }
    
    /**
     * Tests if the section is still valid - it is not removed from the
     * source.
     */
    public boolean isValid() {
        return valid;
    }
    
    /**
     * Removes the section from the Document, but retains the text contained
     * within. The method should be used to unprotect a region of code
     * instead of calling NbDocument.
     * @return true if the operation succeeded.
     */
    public void removeSection() {
        synchronized (this.guards.sections) {
            if (valid) {
                this.guards.sections.remove(name);
                // get document should always return the document, when section
                // is deleted, because it is still valid (and valid is only
                // when document is loaded.
                unmarkGuarded(this.guards.getDocument());
                valid = false;
            }
        }
    }
    
    /** Set the text contained in this section.
     * Newlines are automatically added to all text segments handled,
     * unless there was already one.
     * All guarded blocks must consist of entire lines.
     * This applies to the contents of specific guard types as well.
     * @param bounds the bounds indicating where the text should be set
     * @param text the new text
     * @param minLen If true the text has to have length more than 2 chars.
     * @return <code>true</code> if the operation was successful, otherwise <code>false</code>
     */
    protected boolean setText(PositionBounds bounds, String text, boolean minLen, ContentGetter contentGetter) {
        if (!valid)
            return false;
        
        // modify the text - has to contain at least a space and the length
        // has to be at least 1 character
        if (minLen) {
            if (text.length() == 0 || text.length() == 1 && text.equals("\n"))
                text = " "; // NOI18N
        }
        
        if (text.endsWith("\n")) // NOI18N
            text = text.substring(0, text.length() - 1);
        
        try {
            bounds.setText(text);
            if (guards.gr != null) {
                int offset = getStartPosition().getOffset();
                char[] data = guards.gr.writeSections(Collections.singletonList(GuardsAccessor.DEFAULT.clone(guard, offset - 1)), ("\n" + new PositionBounds(getStartPosition(), getEndPosition(), guards).getText() + "\n").toCharArray());
                Result result = guards.gr.readSections(data);
                List<GuardedSection> guardedSections = result.getGuardedSections();
                if (guardedSections.size() == 1) {
                    PositionBounds contentBounds = contentGetter.getContent(GuardsAccessor.DEFAULT.getImpl(guardedSections.get(0)));
                    bounds.setText(new String(result.getContent(), contentBounds.getBegin().getOffset(), contentBounds.getEnd().getOffset() - contentBounds.getBegin().getOffset()));
                }
            }
            return true;
        } catch (BadLocationException e) {
        }
        return false;
    }
    
    interface ContentGetter<T extends GuardedSectionImpl> {
        public PositionBounds getContent(GuardedSectionImpl t);
    }
    
    /** Marks or unmarks the section as guarded.
     * @param doc The styled document where this section placed in.
     * @param bounds The rangeof text which should be marked or unmarked.
     * @param mark true means mark, false unmark.
     */
    void markGuarded(StyledDocument doc, PositionBounds bounds, boolean mark) {
        int begin = bounds.getBegin().getOffset();
        int end = bounds.getEnd().getOffset();
        
        if (end == doc.getLength() + 1) {
            end--;
        }
        GuardedRegionMarker marker = LineDocumentUtils.as(doc, GuardedRegionMarker.class);
        if (marker != null) {
            if (mark) {
                marker.protectRegion(begin, end - begin + 1);
            } else {
                marker.unprotectRegion(begin, end - begin + 1);
            }
        }
    }
    
    /** Marks the section as guarded.
     * @param doc The styled document where this section placed in.
     */
    abstract void markGuarded(StyledDocument doc);
    
    /** Unmarks the section as guarded.
     * @param doc The styled document where this section placed in.
     */
    abstract void unmarkGuarded(StyledDocument doc);
    
    /** Deletes the text in the section.
     * @exception BadLocationException
     */
    final void deleteText() throws BadLocationException {
        if (valid) {
            final StyledDocument doc = guards.getDocument();
            final BadLocationException[] blex = new BadLocationException[1];
            Runnable r = new Runnable() {
                public void run() {
                    try {
                        int start = getStartPosition().getOffset();
                        if (start > 0 && "\n".equals(doc.getText(start - 1, 1))) { // NOI18N
                            start--;
                        }
                        doc.remove(start, getEndPosition().getOffset() - start + 1);
                    } catch (BadLocationException ex) {
                        blex[0] = ex;
                    }
                }
            };
            
            GuardedSectionsImpl.doRunAtomic(doc, r);
            
            if (blex[0] != null) {
                throw blex[0];
            }
        }
    }
    
    /** Gets the begin of section. To this position is set the caret
     * when section is open in the editor.
     */
    public abstract Position getCaretPosition();
    
    /** Gets the text contained in the section.
     * @return The text contained in the section.
     */
    public abstract String getText();

    /** Assures that a position is not inside the guarded section. Complex guarded sections
     * that contain portions of editable text can return true if the tested position is
     * inside one of such portions provided that permitHoles is true.
     * @param pos position in question
     * @param permitHoles if false, guarded section is taken as a monolithic block
     * without any holes in it regardless of its complexity.
     */
    public abstract boolean contains(Position pos, boolean permitHoles);
    /** Returns a position after the whole guarded block that is safe for insertions.
     */
    public abstract Position getEndPosition();
    /** Returns position before the whole guarded block that is safe for insertions.
     */
    public abstract Position getStartPosition();
    
    public abstract void resolvePositions() throws BadLocationException;

}
