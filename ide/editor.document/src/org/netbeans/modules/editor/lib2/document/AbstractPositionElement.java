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
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.Position;
import javax.swing.text.SimpleAttributeSet;

/**
 * Abstract element (requires naming) consisting of two positions.
 *
 * @author Miloslav Metelka
 */
public abstract class AbstractPositionElement implements Element {
    
    public static Position createPosition(Document doc, int offset) {
        try {
            return doc.createPosition(offset);
        } catch (BadLocationException ex) {
            throw new IndexOutOfBoundsException(ex.getMessage());
        }
    }

    private final Element parent; // 8 + 4 = 12 bytes

    private final Position startPos; // 12 + 4 = 16 bytes

    private final Position endPos; // 16 + 4 = 20 bytes

    AbstractPositionElement(Element parent, Position startPos, Position endPos) {
        assert (startPos != null);
        assert (endPos != null);

        this.parent = parent;
        this.startPos = startPos;
        this.endPos = endPos;
    }

    AbstractPositionElement(Element parent, int startOffset, int endOffset) {
        this(
                parent,
                createPosition(parent.getDocument(), startOffset),
                createPosition(parent.getDocument(), endOffset)
        );
    }

    @Override
    public Document getDocument() {
        return parent.getDocument();
    }

    @Override
    public int getStartOffset() {
        return startPos.getOffset();
    }

    public Position getStartPosition() {
        return startPos;
    }

    @Override
    public int getEndOffset() {
        return endPos.getOffset();
    }

    public Position getEndPosition() {
        return endPos;
    }

    @Override
    public Element getParentElement() {
        return parent;
    }

    @Override
    public AttributeSet getAttributes() {
        // Do not return null since Swing's view factories assume that this is non-null.
        return SimpleAttributeSet.EMPTY;
    }

    @Override
    public int getElementIndex(int offset) {
        return -1;
    }

    @Override
    public int getElementCount() {
        return 0;
    }

    @Override
    public Element getElement(int index) {
        return null;
    }

    @Override
    public boolean isLeaf() {
        return true;
    }

    @Override
    public String toString() {
        return "getStartOffset()=" + getStartOffset() // NOI18N
            + ", getEndOffset()=" + getEndOffset(); // NOI18N
    }

}
