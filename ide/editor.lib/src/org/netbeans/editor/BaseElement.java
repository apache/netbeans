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
