/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
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
