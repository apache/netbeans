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
