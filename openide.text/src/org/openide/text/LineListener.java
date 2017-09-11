/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
