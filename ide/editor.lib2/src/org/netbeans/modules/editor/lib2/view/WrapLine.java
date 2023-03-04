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
 * Information about a single visual line in a wrapped paragraph.
 * 
 * @author Miloslav Metelka
 */

final class WrapLine {

    /**
     * Start view of this line that was obtained by breaking a view
     * at (firstViewIndex - 1). It may be null if this line starts at view boundary
     * with a view at viewIndex.
     */
    ViewPart startPart;

    /**
     * Ending view of this line that was obtained by breaking a view
     * at endViewIndex.
     * It may be null if the line ends at view boundary.
     */
    ViewPart endPart;

    /**
     * Index of a first view located at this line.
     * <br>
     * Logically if there's a non-null startPart then it comes from view
     * at (firstViewIndex - 1).
     */
    int firstViewIndex;

    /**
     * Index that follows last view located at this line.
     * <br>
     * It should be >= firstViewIndex.
     */
    int endViewIndex;
    
    WrapLine() {
    }

    boolean hasFullViews() {
        return firstViewIndex != endViewIndex;
    }

    /**
     * Get first view (or fragment) of this wrap line.
     *
     * @param pView paragraph view to which this wrap line belongs.
     * @return starting child view or fragment.
     */
    EditorView startView(ParagraphView pView) {
        return (startPart != null)
                ? startPart.view
                : ((firstViewIndex != endViewIndex)
                        ? pView.getEditorView(firstViewIndex)
                        : endPart.view);
    }
    
    float startPartWidth() {
        return (startPart != null) ? startPart.width : 0f;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("startPart=").append(startPart); // NOI18N
        sb.append(" [").append(firstViewIndex).append(",").append(endViewIndex).append("]"); // NOI18N
        sb.append(" endPart=").append(endPart); // NOI18N
        return sb.toString();
    }

}
