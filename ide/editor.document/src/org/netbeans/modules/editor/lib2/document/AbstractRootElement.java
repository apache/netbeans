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

import javax.swing.text.AttributeSet;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.SimpleAttributeSet;
import org.netbeans.lib.editor.util.GapList;

/**
 * Abstract root element implementation.
 *
 * @author Miloslav Metelka
 */
public abstract class AbstractRootElement<E extends Element> implements Element {

    protected final Document doc;

    protected GapList<E> children;

    public AbstractRootElement(Document doc) {
        this.doc = doc;
        children = new GapList<E>();
    }

    @Override
    public int getElementCount() {
        return children.size();
    }

    @Override
    public Element getElement(int index) {
        return children.get(index);
    }

    public void copyElements(int srcBegin, int srcEnd, Element dst[], int dstBegin) {
        children.copyElements(srcBegin, srcEnd, dst, dstBegin);
    }

    /**
     * Gets the child element index closest to the given offset.
     * The offset is specified relative to the beginning of the
     * document.  Returns <code>-1</code> if the
     * <code>Element</code> is a leaf, otherwise returns
     * the index of the <code>Element</code> that best represents
     * the given location.  Returns <code>0</code> if the location
     * is less than the start offset. Returns
     * <code>getElementCount() - 1</code> if the location is
     * greater than or equal to the end offset.
     *
     * <p>
     * This implementation is in sync with the original
     * <code>Element.getElementIndex()</code> specification
     * but it differs
     * from <code>AbstractDocument.BranchElement.getElementIndex()</code>
     * which returns 0 in case it does not have any children.
     * <br>
     * This implementation returns -1 in that case because in fact
     * the element act as a leaf element in such case.
     * <br>
     * Nonetheless there should be no difference in functionality
     * if this implementation is used for line elements
     * because there is always at least one line element even
     * for empty doc because of the extra '\n' after the end
     * of the AbstractDocument-based implementations.
     *
     * @param offset the specified offset >= 0
     * @return the element index >= 0
     */
    public int getElementIndex(int offset) {
        int low = 0;
        int high = getElementCount() - 1;

        if (high == -1) { // no children => return -1
            return -1;
        }

        while (low <= high) {
            int mid = (low + high) / 2;
            int elemStartOffset = getElement(mid).getStartOffset();

            if (elemStartOffset < offset) {
                low = mid + 1;
            } else if (elemStartOffset > offset) {
                high = mid - 1;
            } else { // element starts at offset
                return mid;
            }
        }

        if (high < 0) { // if offset < getElement(0).getStartOffset()
            high = 0;
        }
        return high;
    }

    @Override
    public boolean isLeaf() {
        return false;
    }

    protected void replace(int index, int removeCount, Element[] addedElems) {
        if (removeCount > 0) {
            children.remove(index, removeCount);
        }
        if (addedElems != null) {
            children.addArray(index, addedElems);
        }
    }

    @Override
    public Document getDocument() {
        return doc;
    }

    @Override
    public Element getParentElement() {
        return null;
    }

    @Override
    public AttributeSet getAttributes() {
        return SimpleAttributeSet.EMPTY;
    }

    @Override
    public int getStartOffset() {
        return 0;
    }

    @Override
    public int getEndOffset() {
        return doc.getLength() + 1;
    }

    /** Get info about <CODE>DocMarks</CODE>. */
    @Override
    public String toString() {
        return children.toString();
    }

}
