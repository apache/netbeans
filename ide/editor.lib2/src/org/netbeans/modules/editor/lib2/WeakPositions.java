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
