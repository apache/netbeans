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

package org.netbeans.modules.editor.lib2;

import java.util.Map;
import java.util.WeakHashMap;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Position;
import org.netbeans.modules.editor.lib2.highlighting.OffsetGapList;
import org.openide.util.WeakListeners;

/**
 *
 * @author Vita Stejskal
 */
public final class WeakPositions {

    /**
     * Gets a <code>Position</code> in the document at the given offset. The 
     * position is automatically updated when the document's contents is modified.
     * The <code>Position</code> does not reference the document in anyway that
     * would prevent the document from being garbage collected.
     * 
     * @param doc The document to get a position in.
     * @param offset The initial offset of the position.
     * 
     * @return A <code>Position</code> inside the document.
     * @throws BadLocationException If the offset is not valid for the document.
     */
    public static Position get(Document doc, int offset) throws BadLocationException {
        // Check that the offset is valid. This should excercise any rule imposed by
        // the document on its positions.
        doc.createPosition(offset);
        
        synchronized (OGLS) {
            OffsetGapList<WeakP> ogl = OGLS.get(doc);

            if (ogl == null) {
                ogl = new OffsetGapList<WeakPositions.WeakP>();
                OGLS.put(doc, ogl);
                doc.addDocumentListener(WeakListeners.document(documentsTracker, doc));
            }
            
            int index = ogl.findElementIndex(offset);
            WeakP pos = index >= 0 ? ogl.get(index) : null;

            if (pos == null) {
                pos = new WeakP(offset);
                ogl.add(pos);
            }
            
            return pos;
        }
    }
    
    // ----------------------------------------------
    // Private implementation
    // ----------------------------------------------

    private static final Map<Document,OffsetGapList<WeakP>> OGLS = new WeakHashMap<Document,OffsetGapList<WeakP>>();

    private static final DocumentListener documentsTracker = new DocumentListener() {

        @Override
        public void insertUpdate(DocumentEvent e) {
            synchronized (OGLS) {
                OffsetGapList<WeakPositions.WeakP> ogl = getOgl(e);
                if (ogl != null) {
                    ogl.defaultInsertUpdate(e.getOffset(), e.getLength());
                }
            }
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            synchronized (OGLS) {
                OffsetGapList<WeakPositions.WeakP> ogl = getOgl(e);
                if (ogl != null) {
                    ogl.defaultRemoveUpdate(e.getOffset(), e.getLength());
                }
            }
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            // attribute's changed, ignore
        }
        
        private OffsetGapList<WeakPositions.WeakP> getOgl(DocumentEvent e) {
            Document doc = e.getDocument();
            return OGLS.get(doc);
        }
    };
    
    private WeakPositions() {
        
    }

    private static final class WeakP extends OffsetGapList.Offset implements Position {
        public WeakP(int offset) {
            super(offset);
        }
    } // End of WeakP class
}
