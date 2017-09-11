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
