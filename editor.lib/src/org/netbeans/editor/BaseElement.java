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
import javax.swing.text.Element;
import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;

/**
* Element implementation. It serves as parent class
* for both leaf and branch elements.
*
* @author Miloslav Metelka
* @version 1.00
*/

public abstract class BaseElement implements Element {

    /** Element name attribute */
    public static final String ElementNameAttribute = "$ename"; // NOI18N

    /** Reference to document this element is part of */
    protected BaseDocument doc;

    /** Parent element */
    protected BaseElement parent;

    /** Atributes of this element */
    protected AttributeSet attrs;

    public BaseElement(BaseDocument doc, BaseElement parent, AttributeSet attrs) {
        this.doc = doc;
        this.parent = parent;
        this.attrs = attrs;
    }

    /** Get document this element is part of */
    public Document getDocument() {
        return doc;
    }

    /** Get parent element */
    public Element getParentElement() {
        return parent;
    }

    /** Get element name if defined */
    public String getName() {
        AttributeSet as = attrs;
        if (as != null && as.isDefined(ElementNameAttribute)) {
            return (String)as.getAttribute(ElementNameAttribute);
        } else {
            return null;
        }
    }

    /** Get attributes of this element */
    public AttributeSet getAttributes() {
        AttributeSet as = attrs;
        return as == null ? SimpleAttributeSet.EMPTY : as;
    }

    /** Get start offset of this element */
    public abstract int getStartOffset();

    /** Get start mark of this element */
    public abstract Mark getStartMark();

    /** Get end offset of this element */
    public abstract int getEndOffset();

    /** Get end mark of this element */
    public abstract Mark getEndMark();

    /** Get child of this element at specified index */
    public abstract Element getElement(int index);

    /** Gets the child element index closest to the given offset. */
    public abstract int getElementIndex(int offset);

    /** Get number of children of this element */
    public abstract int getElementCount();

    /** Does this element have any children? */
    public abstract boolean isLeaf();

}
