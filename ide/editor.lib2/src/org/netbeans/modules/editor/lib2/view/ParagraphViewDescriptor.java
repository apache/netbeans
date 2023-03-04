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
package org.netbeans.modules.editor.lib2.view;

import java.awt.Shape;

/**
 * Get information about a particular paragraph view in view hierarchy
 * obtained by {@link LockedViewHierarchy#getParagraphViewDescriptor(int) }.
 *
 * @author mmetelka
 */
public final class ParagraphViewDescriptor {
    
    private final DocumentView docView;
    
    private final int pViewIndex;

    ParagraphViewDescriptor(DocumentView docView, int pViewIndex) {
        this.docView = docView;
        this.pViewIndex = pViewIndex;
    }
    
    /**
     * Get start offset of the paragraph view represented by this descriptor.
     * <br>
     * When a LockedViewHierarchy that provided this paragraph view descriptor
     * is unlocked then operation of this method is undefined.
     *
     * @return start offset of paragraph view.
     */
    public int getStartOffset() {
        return docView.getParagraphView(pViewIndex).getStartOffset();
    }

    /**
     * Get textual length of the paragraph view represented by this descriptor.
     * <br>
     * When a LockedViewHierarchy that provided this paragraph view descriptor
     * is unlocked then operation of this method is undefined.
     *
     * @return textual length paragraph view.
     */
    public int getLength() {
        return docView.getParagraphView(pViewIndex).getLength();
    }
    
    /**
     * Get visual allocation of the whole paragraph view (represented by this descriptor).
     * <br>
     * When a LockedViewHierarchy that provided this paragraph view descriptor
     * is unlocked then operation of this method is undefined.
     *
     * @return visual allocation of paragraph view.
     */
    public Shape getAllocation() {
        return docView.getChildAllocation(pViewIndex);
    }
    
    /**
     * Get ascent (useful for text rendering using a particular font)
     * of the paragraph view represented by this descriptor.
     * <br>
     * This method is useful when a tool (such as a side bar performing rendering of a line
     * number) wants to render a text that should vertically match the text
     * rendered by the paragraph view.
     * <br>
     * When a LockedViewHierarchy that provided this paragraph view descriptor
     * is unlocked then operation of this method is undefined.
     *
     * @return visual allocation of paragraph view.
     */
    public float getAscent() {
        return docView.op.getDefaultAscent(); // Currently the ascent is global
    }


}
