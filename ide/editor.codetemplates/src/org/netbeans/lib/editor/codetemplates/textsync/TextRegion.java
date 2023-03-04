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

package org.netbeans.lib.editor.codetemplates.textsync;

import java.util.Arrays;
import java.util.List;
import javax.swing.text.Position;
import org.netbeans.lib.editor.util.GapList;
import org.netbeans.lib.editor.util.swing.PositionRegion;

/**
 * Text region inside a document held by a pair of swing text positions.
 * <br/>
 * A region can have another nested regions.
 *
 * @author Miloslav Metelka
 */
public final class TextRegion<I> {
    
    private static final Position FIXED_ZERO_POSITION = PositionRegion.createFixedPosition(0);
    
    public static Position createFixedPosition(int offset) {
        return PositionRegion.createFixedPosition(offset);
    }

    private Position startPos; // 12 bytes (8-super + 4)

    private Position endPos; // 16 bytes

    private TextSync textSync; // 20 bytes
    
    private TextRegion<?> parent; // 24 bytes
    
    /**
     * Child regions - managed by TextRegionManager.
     */
    private List<TextRegion<?>> regions; // 28 bytes
    
    /**
     * Client-specific information.
     */
    private I clientInfo; // 32 bytes
    
    public TextRegion() {
        this(FIXED_ZERO_POSITION, FIXED_ZERO_POSITION);
    }
    
    public TextRegion(int startOffset, int endOffset) {
        this(PositionRegion.createFixedPosition(startOffset), PositionRegion.createFixedPosition(endOffset));
    }
    
    public TextRegion(Position startPos, Position endPos) {
        if (startPos == null)
            throw new IllegalArgumentException("startPos cannot be null"); // NOI18N
        if (endPos == null)
            throw new IllegalArgumentException("endPos cannot be null"); // NOI18N
        this.startPos = startPos;
        this.endPos = endPos;
    }
    
    public int startOffset() {
        return startPos.getOffset();
    }

    public int endOffset() {
        return endPos.getOffset();
    }
    
    public void updateBounds(Position startPos, Position endPos) {
        if (textRegionManager() != null)
            throw new IllegalStateException("Change of bounds of region " + // NOI18N
                    "connected to textRegionManager prohibited."); // NOI18N
        if (startPos != null)
            setStartPos(startPos);
        if (endPos != null)
            setEndPos(endPos);
    }
    
    public I clientInfo() {
        return clientInfo;
    }
    
    public void setClientInfo(I clientInfo) {
        this.clientInfo = clientInfo;
    }

    public TextSync textSync() {
        return textSync;
    }

    void setTextSync(TextSync textSync) {
        this.textSync = textSync;
    }

    TextRegion<?> parent() {
        return parent;
    }

    void setParent(TextRegion<?> parent) {
        this.parent = parent;
    }

    List<TextRegion<?>> regions() {
        return regions;
    }

    List<TextRegion<?>> validRegions() {
        if (regions == null) {
            regions = new GapList<TextRegion<?>>(2);
        }
        return regions;
    }
    
    void initRegions(TextRegion<?>[] consumedRegions) {
        assert (regions == null || regions.size() == 0);
        regions = new GapList<TextRegion<?>>(Arrays.asList(consumedRegions));
    }
    
    void clearRegions() {
        regions = null;
    }
    
    void setStartPos(Position startPos) {
        this.startPos = startPos;
    }

    void setEndPos(Position endPos) {
        this.endPos = endPos;
    }
    
    TextRegionManager textRegionManager() {
        return (textSync != null) ? textSync.textRegionManager() : null;
    }

    @Override
    public String toString() {
        return "<" + startOffset() + "," + endOffset() + ") IHC=" +
                System.identityHashCode(this) + ", parent=" +
                ((parent != null) ? System.identityHashCode(parent) : "null") +
                ((clientInfo != null) ? (" clientInfo:" + clientInfo) : "");
    }
    
}
