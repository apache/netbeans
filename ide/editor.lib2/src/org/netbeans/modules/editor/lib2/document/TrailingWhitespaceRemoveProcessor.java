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

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.netbeans.lib.editor.util.swing.PositionRegion;

/**
 * Physical removal of whitespace from lines.
 *
 * @author Miloslav Metelka
 */
public class TrailingWhitespaceRemoveProcessor {

    // -J-Dorg.netbeans.modules.editor.lib2.document.TrailingWhitespaceRemoveProcessor.level=FINE
    private static final Logger LOG = Logger.getLogger(TrailingWhitespaceRemoveProcessor.class.getName());
    
    private static final boolean REMOVE_WHITESPACE_ON_CURRENT_LINE = Boolean.getBoolean(
            "org.netbeans.editor.remove.whitespace.on.current.line");

    private static final int GET_ELEMENT_INDEX_THRESHOLD = 100;

    private static void removeWhitespaceOnLine(int lineStartOffset, int lineLastOffset, int caretRelativeOffset, int lineIndex, int caretLineIndex, Document doc, CharSequence docText) {
        int startOffset = lineStartOffset; // lowest offset where WS can be removed
        if (lineIndex == caretLineIndex) {
            startOffset += caretRelativeOffset;
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("Line index " + lineIndex + " contains caret at relative offset " + // NOI18N
                        caretRelativeOffset + ".\n"); // NOI18N
            }
        }
        int offset;
        for (offset = lineLastOffset - 1; offset >= startOffset; offset--) {
            char c = docText.charAt(offset);
            // Currently only remove ' ' and '\t' - may be revised
            if (c != ' ' && c != '\t') {
                break;
            }
        }
        // Increase offset (either below lineStartOffset or on non-white char)
        offset++;
        if (offset < lineLastOffset) {
            BadLocationException ble = null;
            try {
                doc.remove(offset, lineLastOffset - offset);
            } catch (BadLocationException e) {
                ble = e;
            }
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("Remove between " + DocumentUtilities.debugOffset(doc, offset) + // NOI18N
                        " and " + DocumentUtilities.debugOffset(doc, lineLastOffset) + // NOI18N
                        (ble == null ? " succeeded." : " failed.") + // NOI18N
                        '\n'
                );
                if (LOG.isLoggable(Level.FINEST)) {
                    LOG.log(Level.INFO, "Exception thrown during removal:", ble); // NOI18N
                }
            }
        }
    }

    private final Document doc;

    private final boolean removeFromModifiedLinesOnly;

    private final CharSequence docText;

    private final Element lineRootElement;

    private Element modRootElement;

    /** Index of current region. */
    private int modElementIndex;

    /** Start offset of the current region. */
    private int modElementStartOffset;

    /** End offset of the current region. */
    private int modElementEndOffset;

    /** Index of current line. */
    private int lineIndex;

    /** Start offset of the current line. */
    private int lineStartOffset;

    /** Offset of '\n' on the current line. */
    private int lineLastOffset;

    /**
     * Line index that should be excluded from whitespace removal (line with caret)
     * or -1 for none.
     */
    private final int caretLineIndex;

    /**
     * Shift offset of the caret relative to caretLineIndex's line beginning.
     */
    private final int caretRelativeOffset;
    
    private final AtomicBoolean canceled;

    public TrailingWhitespaceRemoveProcessor(Document doc, boolean removeFromModifiedLinesOnly, AtomicBoolean canceled) {
        this.doc = doc;
        this.removeFromModifiedLinesOnly = removeFromModifiedLinesOnly;
        this.canceled = canceled;
        this.docText = DocumentUtilities.getText(doc); // Persists for doc's lifetime
        lineRootElement = DocumentUtilities.getParagraphRootElement(doc);
        modRootElement = ModRootElement.get(doc);
        JTextComponent lastFocusedComponent = EditorRegistry.lastFocusedComponent();
        if (lastFocusedComponent != null && lastFocusedComponent.getDocument() == doc && !REMOVE_WHITESPACE_ON_CURRENT_LINE) {
            int caretOffset = lastFocusedComponent.getCaretPosition();
            caretLineIndex = lineRootElement.getElementIndex(caretOffset);
            // Assign the relativeCaretOffset since the subsequent modifications
            // done by physical whitespace removal would make the absolute offsets unusable.
            caretRelativeOffset = caretOffset - lineRootElement.getElement(caretLineIndex).getStartOffset();
        } else {
            caretLineIndex = -1;
            caretRelativeOffset = 0;
        }
    }

    public void removeWhitespace() {
        if (removeFromModifiedLinesOnly) {
            modElementIndex = modRootElement.getElementCount();
            lineStartOffset = Integer.MAX_VALUE; // Will cause line's bin-search
            while (fetchPreviousNonEmptyRegion()) {
                // Use last offset since someone may paste "blah \n" so the last offset point to '\n' here
                int regionLastOffset = modElementEndOffset - 1;
                int lastLineIndex = lineIndex;
                if (regionLastOffset + GET_ELEMENT_INDEX_THRESHOLD < lineStartOffset) {
                    // Too below - use binary search
                    lineIndex = lineRootElement.getElementIndex(modElementEndOffset - 1);
                    fetchLineElement();
                } else { // Within threshold - try to search sequentially
                    while (lineStartOffset > regionLastOffset) {
                        lineIndex--;
                        fetchLineElement();
                    }
                }

                if (lastLineIndex != lineIndex) {
                    removeWhitespaceOnLine(lineStartOffset, lineLastOffset, caretRelativeOffset, lineIndex, caretLineIndex, doc, docText);
                    while (modElementStartOffset < lineStartOffset) {
                        lineIndex--;
                        fetchLineElement();
                        removeWhitespaceOnLine(lineStartOffset, lineLastOffset, caretRelativeOffset, lineIndex, caretLineIndex, doc, docText);
                    }
                }
            }
        } else {
            // remove from all lines
            for(lineIndex = lineRootElement.getElementCount() - 1; lineIndex >= 0 ; lineIndex--) {
                fetchLineElement();
                removeWhitespaceOnLine(lineStartOffset, lineLastOffset, caretRelativeOffset, lineIndex, caretLineIndex, doc, docText);
            }
        }
    }

    private boolean fetchPreviousNonEmptyRegion() {
        while (--modElementIndex >= 0) {
            Element modElement = modRootElement.getElement(modElementIndex);
            modElementStartOffset = modElement.getStartOffset();
            modElementEndOffset = modElement.getEndOffset();
            if (modElementStartOffset >= modElementEndOffset)// Empty region - continue
                continue;
            return true;
        }
        return false;
    }

    private void fetchLineElement() {
        Element lineElement = lineRootElement.getElement(lineIndex);
        lineStartOffset = lineElement.getStartOffset();
        lineLastOffset = lineElement.getEndOffset() - 1;
    }

}
