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

/**
 * Part of a view (but may also be used as a container for full view).
 */
final class ViewPart {

    /**
     * Part view.
     */
    final EditorView view;
    
    /**
     * Width of the part view (or full view).
     */
    final float width;
    
    /**
     * Relative X of the part against start of whole child view (from which view splitting
     * was initiated).
     * This needs to be included in a 'pos' parameter of a possible breakView()
     * so that e.g. tab widths are properly computed.
     * If this container is used for a full view then this field is 0f.
     */
    final float xShift;
    
    /**
     * Index of view part among other parts (starting at 0).
     * Full view has index -1.
     */
    final int index;

    /**
     * Constructor for whole view.
     */
    ViewPart(EditorView view, float width) {
        this(view, width, 0f, -1);
    }

    /**
     * Constructor for view part.
     */
    ViewPart(EditorView part, float width, float xShift, int index) {
        assert (part != null) : "Null view"; // NOI18N
        this.view = part;
        this.width = width;
        this.xShift = xShift;
        this.index = index;
    }

    boolean isPart() {
        return (index != -1);
    }
    
    boolean isFirstPart() {
        return (index == 0);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(100);
        sb.append("view=").append(view).append(", width=").append(width). // NOI18N
                append(", xShift=").append(xShift).append(", index=").append(index); // NOI18N
        return sb.toString();
    }

}
