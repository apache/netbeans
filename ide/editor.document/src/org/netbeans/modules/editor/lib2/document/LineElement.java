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

import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.Position;
import javax.swing.text.SimpleAttributeSet;

/**
 * Line element implementation.
 * <br>
 * It only holds the starting position.The ending position
 * is obtained by being connected to another line-element chain member
 * or by having a link to position.
 *
 * @author Miloslav Metelka
 * @since 1.46
 */

public final class LineElement extends AbstractPositionElement implements Position {
    
    /**
     * Attributes of this line element
     */
    private Object attributes; // 20(super) + 4 = 24 bytes
    
    LineElement(LineRootElement root, Position startPos, Position endPos) {
        super(root, startPos, endPos);
    }

    @Override
    public int getOffset() {
        return getStartOffset();
    }

    @Override
    public String getName() {
        return AbstractDocument.ParagraphElementName;
    }

    @Override
    public AttributeSet getAttributes() {
        // Do not return null since Swing's view factories assume that this is non-null.
        return (attributes instanceof AttributeSet) ? (AttributeSet) attributes : SimpleAttributeSet.EMPTY;
    }
    
    public void setAttributes(AttributeSet attributes) {
        this.attributes = attributes;
    }
    
    public Object legacyGetAttributesObject() {
        return attributes;
    }
    
    public void legacySetAttributesObject(Object attributes) {
        this.attributes = attributes;
    }

}
