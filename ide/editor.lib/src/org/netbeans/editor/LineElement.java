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

import javax.swing.text.Document;
import javax.swing.text.AbstractDocument;
import javax.swing.text.Element;
import javax.swing.text.AttributeSet;
import javax.swing.text.Position;
import javax.swing.text.SimpleAttributeSet;

/**
 * Line element implementation.
 * <BR>The implementation consist of only one backward bias mark.
 * There is a link to next mark to satisfy
 * {@link javax.swing.text.Element#getEndOffset()}.
 * <BR>This way allows to have just three objects
 * (element, element-finalizer, mark) per line of text
 * compared to seven (element, 2 * (position, position-finalizer, mark))
 * in regular leaf element.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */
final class LineElement implements Element, Position {
    
    /** Parent and root element */
    private final LineRootElement root;
    
    /** Position at the begining of the line */
    private final Position startPos;
    
    /** Next line or null if this is the last line. */
    private final Position endPos;
    
    /** Attributes of this line element */
    private AttributeSet attributes = null;
    
    private Syntax.StateInfo syntaxStateInfo;

    LineElement(LineRootElement root, Position startPos, Position endPos) {
        assert(startPos != null);
        assert(endPos != null);

        this.root = root;
        this.startPos = startPos;
        this.endPos = endPos;
    }

    public Document getDocument() {
        return root.getDocument();
    }

    public int getOffset() {
        return getStartOffset();
    }

    public int getStartOffset() {
        return startPos.getOffset();
    }
    
    Position getStartPosition() {
        return startPos;
    }

    public int getEndOffset() {
        return endPos.getOffset();
    }
    
    Position getEndPosition() {
        return endPos;
    }

    public Element getParentElement() {
        return root;
    }

    public String getName() {
        return AbstractDocument.ParagraphElementName;
    }

    public AttributeSet getAttributes() {
        AttributeSet as = attributes;
        return as == null ? SimpleAttributeSet.EMPTY : as;
    }
    
    public void setAttributes(AttributeSet attributes) {
        this.attributes = attributes;
    }

    public int getElementIndex(int offset) {
        return -1;
    }

    public int getElementCount() {
        return 0;
    }

    public Element getElement(int index) {
        return null;
    }

    public boolean isLeaf() {
        return true;
    }
    
    Syntax.StateInfo getSyntaxStateInfo() {
        return syntaxStateInfo;
    }

    void updateSyntaxStateInfo(Syntax syntax) {
        if (syntaxStateInfo == null) {
            syntaxStateInfo = syntax.createStateInfo();
            assert (syntaxStateInfo != null);
        }
        syntax.storeState(syntaxStateInfo);
    }

    void clearSyntaxStateInfo() {
        syntaxStateInfo = null;
    }

    public String toString() {
        return "getStartOffset()=" + getStartOffset() // NOI18N
            + ", getEndOffset()=" + getEndOffset() // NOI18N
            + ", syntaxStateInfo=" + getSyntaxStateInfo(); // NOI18N
    }

}
