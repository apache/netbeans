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
package org.netbeans.modules.editor.lib2.document;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoableEdit;
import org.netbeans.lib.editor.util.ArrayUtilities;
import org.netbeans.lib.editor.util.GapList;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;

/**
 * Root element that contains child elements that designate modified regions
 * of document.
 * <br/>
 * This helps things like trailing whitespace removal to service proper lines
 * (when working for modified lines only).
 * <br/>
 * It produces undoable edits and it requires listening on an unclosed document events.
 *
 * @author Miloslav Metelka
 */
public final class ModRootElement extends AbstractRootElement<ModElement> implements DocumentListener {

    public static ModRootElement get(Document doc) {
        return (ModRootElement) doc.getProperty(NAME);
    }

    // -J-Dorg.netbeans.modules.editor.lib2.TrailingWhitespaceRemove.level=FINE
    static final Logger LOG = Logger.getLogger(ModRootElement.class.getName());

    public static final String NAME = "mods";

    CharSequence docText;

    private int lastModElementIndex;

    private boolean enabled;

    public ModRootElement(Document doc) {
        super(doc);
        docText = DocumentUtilities.getText(doc);
        doc.putProperty(NAME, this);
    }

    @Override
    public String getName() {
        return NAME;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Reset modifications and start new accounting.
     * @param compoundEdit compound edit to add the resulting edit to. When null no adding is done.
     */
    public void resetMods(UndoableEdit compoundEdit) {
        ResetModsEdit edit = new ResetModsEdit();
        if (compoundEdit != null) {
            compoundEdit.addEdit(edit);
        }
        edit.run();
    }

    GapList<ModElement> emptyMods() {
        return new GapList<ModElement>(4);
    }

    @Override
    public void changedUpdate(DocumentEvent evt) {
        if (enabled) {
            UndoableEdit compoundEdit = (UndoableEdit) evt;
            int offset = evt.getOffset();
            int length = evt.getLength();
            boolean covered = false;
            if (lastModElementIndex >= 0 && lastModElementIndex < children.size()) {
                covered = isCovered(offset, length);
            }
            if (!covered) {
                // Find by binary search
                lastModElementIndex = findModElementIndex(offset, false);
                if (lastModElementIndex >= 0) {
                    covered = isCovered(offset, length);
                }
            }
            if (!covered) {
                addModElement(compoundEdit, offset, offset + length);
                // lastModElementIndex populated by index of addition
            }
        }
    }

    @Override
    public void removeUpdate(DocumentEvent evt) {
        if (evt.getType() == DocumentEvent.EventType.REMOVE) {
            changedUpdate(evt);
        }
    }

    @Override
    public void insertUpdate(DocumentEvent evt) {
        if (evt.getType() == DocumentEvent.EventType.INSERT) {
            changedUpdate(evt);
        }
    }

    private boolean isCovered(int offset, int length) {
        ModElement modElem = children.get(lastModElementIndex);
        if (modElem.getStartOffset() <= offset &&
                offset + length <= modElem.getEndOffset()) {
            return true;
        }
        return false;
    }

    private ModElement addModElement(UndoableEdit compoundEdit, int startOffset, int endOffset) {
        ModElement modElement = new ModElement(this, startOffset, endOffset);
        lastModElementIndex = findModElementIndex(startOffset, true);
        AddModElementEdit edit = new AddModElementEdit(lastModElementIndex, modElement);
        edit.run();
        compoundEdit.addEdit(edit);
        return modElement;
    }

    void addModElement(int index, ModElement modElem) {
        children.add(index, modElem);
    }

    private int findModElementIndex(int offset, boolean forInsert) {
        int low = 0;
        int high = children.size() - 1;
        while (low <= high) {
            int mid = (low + high) >>> 1;
            int midStartOffset = children.get(mid).getStartOffset();
            if (midStartOffset < offset) {
                low = mid + 1;
            } else if (midStartOffset > offset) {
                high = mid - 1;
            } else {
                // offset == modElement.getStartOffset()
                while (++mid < children.size()) {
                    if (children.get(mid).getStartOffset() != offset)
                        break;
                }
                mid--;
                if (forInsert)
                    low = mid + 1;
                else
                    high = mid;
                break;
            }
        }
        return forInsert ? low : high;
    }

    GapList<ModElement> getModList() {
        return children;
    }

    public void checkConsistency() {
        int lastOffset = 0;
        for (int i = 0; i < children.size(); i++) {
            ModElement modElem = children.get(i);
            int offset = modElem.getStartOffset();
            if (offset < lastOffset) {
                throw new IllegalStateException("modElement[" + i + "].getStartOffset()=" + // NOI18N
                        offset + " < lastOffset=" + lastOffset); // NOI18N
            }
            lastOffset = offset;
            offset = modElem.getEndOffset();
            if (offset < lastOffset) {
                throw new IllegalStateException("modElement[" + i + "].getEndOffset()=" + // NOI18N
                        offset + " < modElement.getStartOffset()=" + lastOffset); // NOI18N
            }
            lastOffset = offset;
        }
    }

    @Override
    public String toString() {
        int size = children.size();
        int digitCount = String.valueOf(size).length();
        StringBuilder sb = new StringBuilder(100);
        for (int i = 0; i < size; i++) {
            ModElement modElem = children.get(i);
            ArrayUtilities.appendBracketedIndex(sb, i, digitCount);
            sb.append(modElem);
            sb.append('\n');
        }
        return sb.toString();
    }

    private final class AddModElementEdit extends AbstractUndoableEdit {

        private int index;

        private ModElement modElement;

        public AddModElementEdit(int index, ModElement modElem) {
            this.index = index;
            this.modElement = modElem;
        }

        public void run() {
            addModElement(index, modElement);
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("Added modElement " + modElement + " at index=" + index + '\n'); // NOI18N
                LOG.fine("ModElements:\n" + children + '\n'); // NOI18N
            }
        }

        @Override
        public void undo() throws CannotUndoException {
            super.undo();
            if (index >= children.size() || children.get(index) != modElement) {
                index = findModElementIndex(modElement.getStartOffset(), false);
            }
            if (index >= 0 && children.get(index) == modElement) { // For valid index
                children.remove(index);
            } else { // Safety fallback
                children.remove(modElement);
            }
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("Removed modElement " + modElement + " at index=" + index + '\n'); // NOI18N
                LOG.fine("ModElements:\n" + children + '\n'); // NOI18N
            }
        }

        @Override
        public void redo() throws CannotRedoException {
            super.redo();
            // #145588 - must recompute index according to current modList state
            index = findModElementIndex(modElement.getStartOffset(), true);
            run();
        }

    }

    private final class ResetModsEdit extends AbstractUndoableEdit {

        private GapList<ModElement> oldModRegions;

        private GapList<ModElement> newModRegions;

        public ResetModsEdit() {
            this.oldModRegions = getModList();
            this.newModRegions = emptyMods();
        }

        public void run() {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("Abandoning old regions\n" + children); // NOI18N
            }
            children = newModRegions;
        }

        @Override
        public void undo() throws CannotUndoException {
            super.undo();
            children = oldModRegions;
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("Restored old regions\n" + children); // NOI18N
            }
        }

        @Override
        public void redo() throws CannotRedoException {
            super.redo();
            run();
        }

    }

}
