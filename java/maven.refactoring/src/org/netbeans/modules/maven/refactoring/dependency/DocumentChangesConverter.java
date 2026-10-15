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
package org.netbeans.modules.maven.refactoring.dependency;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NavigableSet;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.lsp.TextEdit;
import org.openide.util.Exceptions;

/**
 * Converts a series of edits of a Document to a series of TextEdits.
 * The class acts as a Listener on a document, watching changes. At the end,
 * it computes list of TextEdits from those changes. It supports even overlapping edits:
 * it merges the overlapping changes into a single TextEdit (or several non-overlapping TextEdits)
 * since LSP specification does not like overlaps.
 * <p>
 * The code is originally written as a DocumentListener, so it accepts document edits as they are generated
 * and captures them as TextEdits, but still uses the original offsets, which shift from the original
 * state as the document is modified.
 * <p>
 * It could be also used to normalize a sequence of overlapping TextEdits.
 * @author sdedic
 */
public class DocumentChangesConverter implements DocumentListener {
    private final Document document;
    private boolean adjustPositions;

    public DocumentChangesConverter(Document document) {
        this.document = document;
    }
    
    /**
     * Edits captured from the document.
     */
    private List<TextEdit> recordedEdits = new ArrayList<>();
    
    @Override
    public void insertUpdate(DocumentEvent e) {
        try {
            String text = document.getText(e.getOffset(), e.getLength());
            TextEdit edit = new TextEdit(e.getOffset(), e.getOffset(), text);
            recordedEdits.add(edit);
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        TextEdit edit = new TextEdit(e.getOffset(), e.getOffset() + e.getLength(), null);
        recordedEdits.add(edit);
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        // no op
    }
    
    /**
     * Order of edits in the sequence they have been applied to the document.
     * When an edit is modified or coalesced (a new instance is created), the edit (or all coalesced edits) must be replaced by
     * that new instance.
     */
    private List<TextEdit> ordered = new ArrayList<>();
    
    // must process events in the edit queue. Must insert into 'ordered' first !
    private TreeSet<TextEdit> condensed = new TreeSet<TextEdit>((TextEdit t1, TextEdit t2) -> {
        int d = t1.getStartOffset() - t2.getStartOffset();
        if (d != 0) {
            return d;
        }
        int n1 = ordered.indexOf(t1);
        int n2 = ordered.indexOf(t2);
        if (n1 == -1) {
            return 1;
        } else if (n2 == -1) {
            return 1;
        }
        return n1 - n2;
    });
    
    /**
     * Replaces one or more edits in the collection that defines order with the new one.
     * The order will be computed as the position of first (in therms of the original order) 
     * of the edits, that inserted new text to the document; or any of the remove edits.
     * @param remove edits to remove
     * @param replaceWith the replacement.
     */
    void replaceOrderedEdits(Collection<TextEdit> remove, TextEdit replaceWith) {
        int pos = -1;
        
        for (TextEdit candidate : remove) {
            int p = ordered.indexOf(candidate);
            if (pos == -1) {
                pos = p;
            } else {
                if ((candidate.getNewText() != null) && (p < pos)) {
                    pos = p;
                }
            }
        }
        
        ordered.set(pos, replaceWith);
        ordered.removeAll(remove);
    }
    
    /**
     * Turns the recorded edits into a series of non-overlapping TextEdits that
     * make the same modification to the document. The edits may be coalesced, though
     * not completely (some adjacent edits that may be further collapsed may be present).
     * 
     * @return series of non-overlapping edits.
     */
    public List<TextEdit> makeTextEdits() {
        ordered.clear();
        ordered.addAll(recordedEdits);
        
        condensed.clear();
        
        T: for (int i = 0; i < recordedEdits.size(); i++) {
            TextEdit t = recordedEdits.get(i);
            
            int adjustedStart = t.getStartOffset();
            int adjustedEnd = t.getEndOffset();
            int l = t.getEndOffset() - t.getStartOffset();

            NavigableSet<TextEdit> priorEdits = condensed.headSet(t, false).descendingSet();
            
            for (Iterator<TextEdit> it = priorEdits.iterator(); it.hasNext(); ) {
                TextEdit p = it.next();
                int added = p.getNewText() != null ? p.getNewText().length() : 0;
                if (p.getStartOffset() + added >= adjustedStart) {
                    // if "p" specifies insert, but this edit delete in the middle or after the text, do not merge
                    //if ((t.getStartOffset() <= p.getEndOffset()) || (t.getEndOffset() == t.getStartOffset())) {
                    if ((l == 0) || (adjustedStart < p.getStartOffset() + added)) {
                        mergePrecedingOverlap(t, p, adjustedStart, it);
                        continue T;
                    }
                }
                int editLength = p.getNewText() != null ? p.getNewText().length() : 0;
                editLength -= (p.getEndOffset() - p.getStartOffset());
                adjustedStart -= editLength;
                adjustedEnd = adjustedStart + l;
            }
            
            SortedSet<TextEdit> nextEdits = condensed.tailSet(t);
            int sz = t.getNewText() == null ? 0 : t.getNewText().length();
            
            if (!nextEdits.isEmpty()) {
                List<TextEdit> toMerge = new ArrayList<>();
                for (Iterator<TextEdit> it = nextEdits.iterator(); it.hasNext(); ) {
                    TextEdit n = it.next();
                    if (n.getStartOffset() <= adjustedEnd + sz) {
                        toMerge.add(n);
                        it.remove();
                    } else {
                        break;
                    }
                }
                
                if (!toMerge.isEmpty()) {
                    mergeOverlappingEdits(toMerge, t, adjustedStart, adjustedEnd);
                    continue T;
                }
            }
            if (adjustedStart != t.getStartOffset()) {
                TextEdit edit = new TextEdit(adjustedStart, adjustedEnd, t.getNewText());
                replaceOrderedEdits(Collections.singletonList(t), edit);
                condensed.add(edit);
            } else {
                // already is in the ordered list
                condensed.add(t);
            }
        }
        
        return new ArrayList<>(condensed);
    }
    
    /**
     * Merges the new edit with edits that follow this one. Note that the only way how 
     * we could overlap with following edits is to delete part of text.
     * @param toMerge
     * @param newEdit 
     */
    private void mergeOverlappingEdits(List<TextEdit> toMerge, TextEdit newEdit, int adjustedStart, int adjustedEnd) {
        TextEdit remainder = null;
        
        for (int i = 0; i < toMerge.size(); i++) {
            TextEdit t = toMerge.get(i);
            int l = t.getNewText() == null ? 0 : t.getNewText().length();
            int nl = newEdit.getNewText() == null ? 0 : newEdit.getNewText().length();
            if (adjustedEnd >= t.getStartOffset() + l) {
                // whole text edit was deleted by newEdit
                adjustedEnd += (t.getEndOffset() - t.getStartOffset());
                // compensate for the inserted text
                adjustedEnd -= l;
            } else {
                if (adjustedEnd > t.getStartOffset()) {
                    // part of the t's text was deleted by newEdit, rest of text is appended to newEdit's text
                    int delPart = adjustedEnd - t.getStartOffset();
                    String nt = t.getNewText().substring(delPart);
                    if (newEdit.getNewText() != null) {
                        // this is probably unreachable from plain document events, since we have remove (so no insert can be done)
                        nt = newEdit.getNewText() + t.getNewText().substring(delPart);
                    } else {
                        nt = t.getNewText().substring(delPart);
                    }
                    if (nt.isEmpty()) {
                        nt = null;
                    }
                    TextEdit edit = new TextEdit(adjustedStart, t.getEndOffset(), nt);
                    replaceOrderedEdits(toMerge, edit);
                    condensed.add(edit);
                    return;
                } 
                if (adjustedEnd + nl == t.getStartOffset()) {
                    // t immediately follows newEdit, will be merged unless there are two deletes separeted by newEdit's text
                    if (nl == 0 || (t.getEndOffset() == t.getStartOffset() && t.getNewText() != null)) {
                        TextEdit edit = new TextEdit(adjustedStart, adjustedEnd, 
                            nl == 0 ? "" : newEdit.getNewText() + t.getNewText());
                        
                        replaceOrderedEdits(toMerge, edit);
                        condensed.add(edit);
                        return;
                    }
                }
                remainder = t;
                break;
            }
        }
        
        TextEdit edit = new TextEdit(adjustedStart, adjustedEnd, newEdit.getNewText());
        if (remainder != null) {
            // unchanged, is in the ordered list
            condensed.add(remainder);
        } else {
            replaceOrderedEdits(toMerge, edit);
        }
        condensed.add(edit);
    }

    /**
     * Merges a subsequent `newEdit` with preceding `conflicting` one. Note that 'newEdit'
     * is always a delete-only, or insert-only instruction as Swing Document reports only those.
     * 
     * @param newEdit
     * @param conflicting 
     */
    private void mergePrecedingOverlap(TextEdit newEdit, TextEdit conflicting, int adjustedStart, Iterator<TextEdit> it) {
        /*
            |---------XXXXXXXX|
                         cL
                      ^
                      conf.endOffset

                      __ <- off    
            |---------XXXXXXXX|
                        |-------YYY|
                          del    nL
                        ^
                      adjStart
        */
        // the overlapping edit just inserts, mold the insert into the previous one.
        int del = newEdit.getEndOffset() - newEdit.getStartOffset();
        String confText = conflicting.getNewText();
        String newText = newEdit.getNewText();
        int confLen = confText == null ? 0 : confText.length();
        int off = adjustedStart - conflicting.getStartOffset(); //conflicting.getEndOffset();
        
        /*
            |------XXXXXX|
                     |YYY|
                    |---YYY|
        */
        boolean deleteContained = conflicting.getStartOffset()+ confLen >= adjustedStart + del;
        if (deleteContained) {
            String text;
            if (off == confLen) {
                // just append
                if (confText != null) {
                    if (newText != null) {
                        text = confText + newText;
                    } else {
                        text = confText;
                    }
                } else {
                    text = newText;
                }
            } else {
                if (newText != null) {
                    if (confText != null) {
                        text = confText.substring(0, off) + newText + confText.substring(off + del);
                    } else {
                        // should never happen, as confContext == null => confLen = 0 => off == confLenn
                        text = newText + confText.substring(off + del);
                    }
                }  else {
                    text = confText.substring(0, off) + confText.substring(off + del);
                }
            }
            if (text != null && text.isEmpty()) {
                text = null;
            }
            it.remove();
            if (conflicting.getStartOffset() != conflicting.getEndOffset() || text != null) {
                TextEdit edit = new TextEdit(conflicting.getStartOffset(), conflicting.getEndOffset(), text);
                replaceOrderedEdits(Collections.singleton(conflicting), edit);
                condensed.add(edit);
            }
            return;
        }
        
        if (off == 0) {
            /*
                |------XXXXXX|
                |------|
                       |-----------YYY|
            */
            int mergeDel = conflicting.getEndOffset() - conflicting.getStartOffset() + (del - confLen);
            it.remove();
            TextEdit edit = new TextEdit(conflicting.getStartOffset(), 
                    conflicting.getStartOffset() + mergeDel, newText);
            replaceOrderedEdits(Collections.singleton(conflicting), edit);
            condensed.add(edit);
            return;
        }
        /*
            |------XXXXXX|
                     |-------YYY|
        */
        it.remove();
        TextEdit edit = new TextEdit(conflicting.getStartOffset(), conflicting.getEndOffset(), 
                confText.substring(0, off));
        replaceOrderedEdits(Collections.singleton(conflicting), edit);
        condensed.add(edit);
        int del2 = del - (confLen - off);
        
        TextEdit edit2 = new TextEdit(adjustedStart, adjustedStart + del2, newText);
        ordered.add(edit2);
        condensed.add(edit2);
    }
}
