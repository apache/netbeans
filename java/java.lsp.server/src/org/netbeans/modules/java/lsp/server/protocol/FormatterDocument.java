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
package org.netbeans.modules.java.lsp.server.protocol;

import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;
import javax.swing.event.DocumentListener;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Segment;
import javax.swing.text.Style;
import javax.swing.text.StyledDocument;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;
import org.netbeans.api.editor.document.AtomicLockDocument;
import org.netbeans.api.editor.document.AtomicLockListener;
import org.netbeans.api.editor.document.LineDocument;
import org.netbeans.api.editor.document.LineDocumentUtils;
import org.netbeans.modules.java.lsp.server.Utils;

final class FormatterDocument implements StyledDocument, LineDocument, AtomicLockDocument {
    private final StyledDocument doc;
    private final List<TextEdit> edits = new ArrayList<>();
    private TextEdit last = null;

    FormatterDocument(StyledDocument lineDocument) {
        this.doc = lineDocument;
    }

    List<TextEdit> getEdits() {
        return edits;
    }

    @Override
    public Style addStyle(String nm, Style parent) {
        return doc.addStyle(nm, parent);
    }

    @Override
    public void removeStyle(String nm) {
        doc.removeStyle(nm);
    }

    @Override
    public Style getStyle(String nm) {
        return doc.getStyle(nm);
    }

    @Override
    public void setCharacterAttributes(int offset, int length, AttributeSet s, boolean replace) {
        doc.setCharacterAttributes(offset, length, s, replace);
    }

    @Override
    public void setParagraphAttributes(int offset, int length, AttributeSet s, boolean replace) {
        doc.setParagraphAttributes(offset, length, s, replace);
    }

    @Override
    public void setLogicalStyle(int pos, Style s) {
        doc.setLogicalStyle(pos, s);
    }

    @Override
    public Style getLogicalStyle(int p) {
        return doc.getLogicalStyle(p);
    }

    @Override
    public javax.swing.text.Element getParagraphElement(int pos) {
        return doc.getParagraphElement(pos);
    }

    @Override
    public javax.swing.text.Element getCharacterElement(int pos) {
        return doc.getCharacterElement(pos);
    }

    @Override
    public Color getForeground(AttributeSet attr) {
        return doc.getForeground(attr);
    }

    @Override
    public Color getBackground(AttributeSet attr) {
        return doc.getBackground(attr);
    }

    @Override
    public Font getFont(AttributeSet attr) {
        return doc.getFont(attr);
    }

    @Override
    public int getLength() {
        return doc.getLength();
    }

    @Override
    public void addDocumentListener(DocumentListener listener) {
        doc.addDocumentListener(listener);
    }

    @Override
    public void removeDocumentListener(DocumentListener listener) {
        doc.removeDocumentListener(listener);
    }

    @Override
    public void addUndoableEditListener(UndoableEditListener listener) {
        doc.addUndoableEditListener(listener);
    }

    @Override
    public void removeUndoableEditListener(UndoableEditListener listener) {
        doc.removeUndoableEditListener(listener);
    }

    @Override
    public Object getProperty(Object key) {
        return doc.getProperty(key);
    }

    @Override
    public void putProperty(Object key, Object value) {
    }

    @Override
    public void remove(int offs, int len) throws BadLocationException {
        Position pos = Utils.createPosition(doc, offs);
        if (last != null && pos.equals(last.getRange().getStart()) && pos.equals(last.getRange().getEnd())) {
            last.getRange().setEnd(Utils.createPosition(doc, offs + len));
        } else {
            last = new TextEdit(new Range(pos, Utils.createPosition(doc, offs + len)), "");
            edits.add(last);
        }
    }

    @Override
    public void insertString(int offset, String str, AttributeSet a) throws BadLocationException {
        Position pos = Utils.createPosition(doc, offset);
        if (last != null && pos.equals(last.getRange().getStart())) {
            if (str != null) {
                last.setNewText(last.getNewText() + str);
            }
        } else {
            last = new TextEdit(new Range(pos, pos), str != null ? str : "");
            edits.add(last);
        }
    }

    @Override
    public String getText(int offset, int length) throws BadLocationException {
        return doc.getText(offset, length);
    }

    @Override
    public void getText(int offset, int length, Segment txt) throws BadLocationException {
        doc.getText(offset, length, txt);
    }

    @Override
    public javax.swing.text.Position getStartPosition() {
        return doc.getStartPosition();
    }

    @Override
    public javax.swing.text.Position getEndPosition() {
        return doc.getEndPosition();
    }

    @Override
    public javax.swing.text.Position createPosition(int offs) throws BadLocationException {
        return doc.createPosition(offs);
    }

    @Override
    public javax.swing.text.Element[] getRootElements() {
        return doc.getRootElements();
    }

    @Override
    public javax.swing.text.Element getDefaultRootElement() {
        return doc.getDefaultRootElement();
    }

    @Override
    public void render(Runnable r) {
        doc.render(r);
    }

    @Override
    public javax.swing.text.Position createPosition(int offset, javax.swing.text.Position.Bias bias) throws BadLocationException {
        LineDocument ldoc = LineDocumentUtils.as(doc, LineDocument.class);
        return ldoc.createPosition(offset, bias);
    }

    @Override
    public Document getDocument() {
        return this;
    }

    @Override
    public void atomicUndo() {
        AtomicLockDocument bdoc = LineDocumentUtils.as(doc, AtomicLockDocument.class);
        bdoc.atomicUndo();
    }

    @Override
    public void runAtomic(Runnable r) {
        AtomicLockDocument bdoc = LineDocumentUtils.as(doc, AtomicLockDocument.class);
        bdoc.runAtomic(r);
    }

    @Override
    public void runAtomicAsUser(Runnable r) {
        AtomicLockDocument bdoc = LineDocumentUtils.as(doc, AtomicLockDocument.class);
        bdoc.runAtomicAsUser(r);
    }

    @Override
    public void addAtomicLockListener(AtomicLockListener l) {
        AtomicLockDocument bdoc = LineDocumentUtils.as(doc, AtomicLockDocument.class);
        bdoc.addAtomicLockListener(l);
    }

    @Override
    public void removeAtomicLockListener(AtomicLockListener l) {
        AtomicLockDocument bdoc = LineDocumentUtils.as(doc, AtomicLockDocument.class);
        bdoc.removeAtomicLockListener(l);
    }
}
