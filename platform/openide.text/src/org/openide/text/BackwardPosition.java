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

import org.openide.util.WeakListeners;

import javax.swing.event.*;
import javax.swing.text.*;


/** Position that stays at the same place if someone inserts
* directly to its offset.
*
* @author Jaroslav Tulach
*/
class BackwardPosition extends Object implements Position, DocumentListener {
    /** positions current offset */
    private int offset;

    /** Constructor.
    */
    private BackwardPosition(int offset) {
        this.offset = offset;
    }

    /** @param doc document
    * @param offset offset
    * @return new instance of the position
    */
    public static Position create(Document doc, int offset) {
        BackwardPosition p = new BackwardPosition(offset);
        doc.addDocumentListener(org.openide.util.WeakListeners.document(p, doc));

        return p;
    }

    //
    // Position
    //

    /** @return the offset
    */
    public int getOffset() {
        return offset;
    }

    //
    // document listener
    //

    /** Updates */
    public void insertUpdate(DocumentEvent e) {
        // less, not less and equal
        if (e.getOffset() < offset) {
            offset += e.getLength();
        }
    }

    /** Updates */
    public void removeUpdate(DocumentEvent e) {
        int o = e.getOffset();

        if (o < offset) {
            offset -= e.getLength();

            // was the position in deleted range? => go to its beginning
            if (offset < o) {
                offset = o;
            }
        }
    }

    /** Nothing */
    public void changedUpdate(DocumentEvent e) {
    }
}
