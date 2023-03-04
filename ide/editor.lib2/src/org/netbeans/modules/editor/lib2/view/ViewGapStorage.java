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
 * Gap storage speeds up operations when a number of children views exceeds
 * certain number.
 * 
 * @author Miloslav Metelka
 */

final class ViewGapStorage {

    /**
     * Number of child views above which they will start to be managed
     * in a gap-storage way upon modification.
     * Below the threshold the views are updated without gap creation.
     */
    static final int GAP_STORAGE_THRESHOLD = 20;

    /**
     * Length of the visual gap in child view infos along their major axis.
     */
    static final double INITIAL_VISUAL_GAP_LENGTH = (1L << 40);

    static final int INITIAL_OFFSET_GAP_LENGTH = (Integer.MAX_VALUE >>> 1);

    /**
     * Start of the visual gap in child views along their major axis.
     * <br>
     * Place it above end of all views initially.
     */
    double visualGapStart; // 8-super + 8 = 16 bytes

    double visualGapLength; // 16 + 8 = 24 bytes
    
    /**
     * Index where the visual gap is located in children views.
     * This is to help to avoid checking of above/below gap members
     * when doing many single-view span updates.
     */
    int visualGapIndex; // 24 + 4 = 28 bytes

    /**
     * Start of the offset gap used for managing end offsets of HighlightsView views.
     * It is not used for paragraph views.
     * <br>
     * Place it above end of all views initially.
     */
    int offsetGapStart; // 28 + 4 = 32 bytes

    int offsetGapLength; // 32 + 4 = 36 bytes
    
    void initVisualGap(int visualGapIndex, double visualGapStart) {
        this.visualGapIndex = visualGapIndex;
        this.visualGapStart = visualGapStart;
        this.visualGapLength = INITIAL_VISUAL_GAP_LENGTH;
    }

    void initOffsetGap(int offsetGapStart) {
        this.offsetGapStart = offsetGapStart;
        this.offsetGapLength = INITIAL_OFFSET_GAP_LENGTH;
    }
    
    int raw2Offset(int rawEndOffset) {
        // Using <= allows to use prevView.getRawEndOffset() as offsetGapStart (assuming non-empty views)
        return (rawEndOffset <= offsetGapStart)
                ? rawEndOffset
                : rawEndOffset - offsetGapLength;
    }

    int offset2Raw(int offset) {
        // Using <= allows to use prevView.getRawEndOffset() as offsetGapStart (assuming non-empty views)
        return (offset <= offsetGapStart)
                ? offset
                : offset + offsetGapLength;
    }

    double raw2VisualOffset(double rawVisualOffset) {
        // Using <= allows to use prevView.getRawEndVisualOffset() as visualGapStart (assuming non-empty views)
        return (rawVisualOffset <= visualGapStart)
                ? rawVisualOffset
                : rawVisualOffset - visualGapLength;
    }

    double visualOffset2Raw(double visualOffset) {
        // Using <= allows to use prevView.getRawEndVisualOffset() as visualGapStart (assuming non-empty views)
        return (visualOffset <= visualGapStart)
                ? visualOffset
                : visualOffset + visualGapLength;
    }

    boolean isBelowVisualGap(double rawVisualOffset) {
        return (rawVisualOffset <= visualGapStart);
    }

    StringBuilder appendInfo(StringBuilder sb) {
        sb.append("<").append(offsetGapStart).append("|").append(offsetGapLength). // NOI18N
                append(", vis[").append(visualGapIndex).append("]<"). // NOI18N
                append(visualGapStart).append("|").append(visualGapLength); // NOI18N
        return sb;
    }

    @Override
    public String toString() {
        return appendInfo(new StringBuilder(100)).toString();
    }

}
