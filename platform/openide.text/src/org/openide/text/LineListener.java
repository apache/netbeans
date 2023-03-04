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
package org.openide.text;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import org.openide.util.WeakListeners;

import javax.swing.text.*;


/** Listener to changes in the document.
*
* @author Jaroslav Tulach
*/
final class LineListener extends Object implements javax.swing.event.DocumentListener {
    /** original count of lines */
    private int orig;

    /** root element of all lines */
    private Reference<Element> rootRef;

    /** last tested amount of lines */
    private int lines;

    /** operations on lines */
    private LineStruct struct;

    /** Support necessary for getting Set of lines*/
    CloneableEditorSupport support;

    /** Creates new LineListener */
    public LineListener(StyledDocument doc, CloneableEditorSupport support) {
        this.struct = new LineStruct();
        Element root = NbDocument.findLineRootElement(doc);
        orig = lines = root.getElementCount();
        rootRef = new WeakReference<Element>(root);
        this.support = support;

        doc.addDocumentListener(WeakListeners.document(this, doc));
    }

    /** Getter for amount of lines */
    public int getOriginalLineCount() {
        return orig;
    }

    /** Convertor between old and new line sets */
    public int getLine(int i) {
        return struct.convert(i, true /*originalToCurrent*/);
    }

    /** Convertor between old and new line sets */
    public int getOld(int i) {
        return struct.convert(i, false /*currentToOriginal*/);
    }

    public void removeUpdate(javax.swing.event.DocumentEvent p0) {
        Element root = rootRef.get();
        int elem = root.getElementCount();
        int delta = lines - elem;
        lines = elem;

        StyledDocument doc = support.getDocument();
        if (doc == null) {
            return;
        }
        int lineNumber = NbDocument.findLineNumber(doc, p0.getOffset());

        if (delta > 0) {
            struct.deleteLines(lineNumber, delta);
        }

        if (support == null) {
            return;
        }

        Line.Set set = support.getLineSet();

        if (!(set instanceof DocumentLine.Set)) {
            return;
        }

        // Notify lineSet there was changed range of lines.
        ((DocumentLine.Set) set).linesChanged(lineNumber, lineNumber + delta, p0);

        if (delta > 0) {
            // Notify Line.Set there was moved range of lines.
            ((DocumentLine.Set) set).linesMoved(lineNumber, elem);
        }
    }

    public void changedUpdate(javax.swing.event.DocumentEvent p0) {
    }

    public void insertUpdate(javax.swing.event.DocumentEvent p0) {
        Element root = rootRef.get();
        int elem = root.getElementCount();

        int delta = elem - lines;
        lines = elem;

        StyledDocument doc = support.getDocument();
        if (doc == null) {
            return;
        }
        int lineNumber = NbDocument.findLineNumber(doc, p0.getOffset());

        if (delta > 0) {
            struct.insertLines(lineNumber, delta);
        }

        if (support == null) {
            return;
        }

        Line.Set set = support.getLineSet();

        if (!(set instanceof DocumentLine.Set)) {
            return;
        }

        // Nptify Line.Set there was changed range of lines.
        ((DocumentLine.Set) set).linesChanged(lineNumber, lineNumber, p0);

        if (delta > 0) {
            // Notify Line.Set there was moved range of lines.
            ((DocumentLine.Set) set).linesMoved(lineNumber, elem);
        }
    }
}
