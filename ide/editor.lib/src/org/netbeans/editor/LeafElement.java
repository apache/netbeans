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

package org.netbeans.editor;

import javax.swing.text.Element;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;

/**
* Leaf element is used on the leaf level of element tree.
*
* @author Miloslav Metelka
* @version 0.10
*/

public class LeafElement extends BaseElement {

    /** Mark giving start offset of this element */
    protected Mark startMark;

    /** Mark giving end offset of this element */
    protected Mark endMark;

    /** Does this view begin at line begining */
    protected boolean bol;

    /** Does this view end at line end */
    protected boolean eol;

    /** Create new document instance */
    public LeafElement(BaseDocument doc, BaseElement parent, AttributeSet attrs,
                       int startOffset, int endOffset, boolean bol, boolean eol) {
        super(doc, parent, attrs);
        this.bol = bol;
        this.eol = eol;
        // create marks for element start and end
        try {
            startMark = new Mark(true);
            endMark = new Mark(false);
            startMark.insert(doc, startOffset);
            endMark.insert(doc, endOffset);
        } catch (BadLocationException e) {
            Utilities.annotateLoggable(e);
        } catch (InvalidMarkException e) {
            throw new IllegalStateException(e.toString());
        }
    }

    protected void finalize() throws Throwable {
        try {
            startMark.remove();
            endMark.remove();
        } catch (InvalidMarkException e) {
        }
        super.finalize();
    }

    /** Get start mark of this element */
    public final Mark getStartMark() {
        return startMark;
    }

    /** Get start offset of this element */
    public final int getStartOffset() {
        try {
            return startMark.getOffset();
        } catch (InvalidMarkException e) {
            return 0;
        }
    }

    /** Get end mark of this element */
    public final Mark getEndMark() {
        return endMark;
    }

    /** Get end offset of this element */
    public final int getEndOffset() {
        try {
            return endMark.getOffset();
        } catch (InvalidMarkException e) {
            return 0;
        }
    }

    /** Is this view begining at begin of line */
    public final boolean isBOL() {
        return bol;
    }

    /** Is this view ending at end of line ? */
    public final boolean isEOL() {
        return eol;
    }

    /** Gets the child element index closest to the given offset.
    * For leaf element this returns -1.
    */
    public int getElementIndex(int offset) {
        return -1;
    }

    /** Get number of children of this element */
    public int getElementCount() {
        return 0;
    }

    /** Get child of this element at specified index or itself
    * if the index is too big
    */
    public Element getElement(int index) {
        return null;
    }

    /** Does this element have any children? */
    public boolean isLeaf() {
        return true;
    }

    public String toString() {
        return "startOffset=" + getStartOffset() // NOI18N
               + ", endOffset=" + getEndMark(); // NOI18N
    }

}
