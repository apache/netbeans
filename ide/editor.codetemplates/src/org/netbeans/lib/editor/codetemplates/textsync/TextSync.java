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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * List of regions which have their text synchronized.
 * <br/>
 * One of the regions is called master and its text will be replicated
 * to all other regions.
 *
 * @author Miloslav Metelka
 */
public final class TextSync {
    
    private static int EDITABLE_FLAG = 1;
    private static int CARET_MARKER_FLAG = 2;
    private static int COMPLETION_INVOKE_FLAG = 4;

    private TextSyncGroup<?> textSyncGroup;
    
    private List<TextRegion<?>> regions;
    
    private int masterRegionIndex;
    
    private int flags;
    
    public TextSync(TextRegion<?>... regions) {
        initRegions(regions.length);
        for (TextRegion<?> region : regions)
            addRegion(region);
    }
    
    public TextSync() {
        initRegions(4);
    }

    private void initRegions(int size) {
        this.regions = new ArrayList<TextRegion<?>>(size);
    }
    
    /**
     * Get all regions managed by this text sync.
     * 
     * @return non-null unmodifiable list of text regions.
     */
    public List<TextRegion<?>> regions() {
        return Collections.unmodifiableList(regions);
    }

    public <I> TextRegion<I> region(int index) {
        @SuppressWarnings("unchecked")
        TextRegion<I> region = (TextRegion<I>)regions.get(index);
        return region;
    }

    /**
     * Get region (also contained in {@link #regions()}) that will primarily be edited
     * (its text will be replicated into all other regions).
     * 
     * @return non-null text region.
     */
    public <I> TextRegion<I> masterRegion() {
        if (masterRegionIndex < 0 || masterRegionIndex >= regions.size())
            return null;
        @SuppressWarnings("unchecked")
        TextRegion<I> region = (TextRegion<I>)regions.get(masterRegionIndex);
        return region;
    }
    
    public <I> TextRegion<I> validMasterRegion() {
        TextRegion<I> masterRegion = masterRegion();
        if (masterRegion == null) {
            throw new IllegalStateException("masterRegion expected to be non-null");
        }
        return masterRegion;
    }
    
    public int masterRegionIndex() {
        return masterRegionIndex;
    }
    
    public void setMasterRegionIndex(int masterRegionIndex) {
        this.masterRegionIndex = masterRegionIndex;
    }

    public void syncByMaster() {
        validTextRegionManager().syncByMaster(this);
    }
    
    public void setText(String text) {
        validTextRegionManager().setText(this, text);
    }

    /**
     * Whether this text sync is editable by the user.
     * <br/>
     * Newly created text syncs are not editable by default.
     * 
     * @return true if editable false otherwise.
     */
    public boolean isEditable() {
        return (flags & EDITABLE_FLAG) != 0;
    }
    
    public void setEditable(boolean editable) {
        if (editable)
            flags |= EDITABLE_FLAG;
        else
            flags &= ~EDITABLE_FLAG;
    }
    
    public boolean isCaretMarker() {
        return (flags & CARET_MARKER_FLAG) != 0;
    }
    
    public void setCaretMarker(boolean caretMarker) {
        if (caretMarker)
            flags |= CARET_MARKER_FLAG;
        else
            flags &= ~CARET_MARKER_FLAG;
    }

    /**
     * Returns whether the code completion should be invoked when this text sync becomes active.
     * 
     * @return  {@code true} if the code completion should be invoked, {@code false} otherwise.
     * 
     * @since 1.53
     */
    public boolean isCompletionInvoke() {
        return (flags & COMPLETION_INVOKE_FLAG) != 0;
    }

    /**
     * Sets whether the code completion should be invoked when this text sync becomes active.
     * 
     * @param completionInvoke determines whether the code completion should be invoked.
     * 
     * @since 1.53
     */
    public void setCompletionInvoke(boolean completionInvoke) {
        if (completionInvoke) {
            flags |= COMPLETION_INVOKE_FLAG;
        } else {
            flags &= ~COMPLETION_INVOKE_FLAG;
        }
    }

    public void addRegion(TextRegion<?> region) {
        if (region == null)
            throw new IllegalArgumentException("region cannot be null");
        if (region.textSync() != null)
            throw new IllegalArgumentException("region " + region + // NOI18N
                    " already assigned to textSync=" + region.textSync()); // NOI18N
        regions.add(region);
        region.setTextSync(this);
        // masterRegionIndex should not be affected
    }

    public void removeRegion(TextRegion region) {
        int index = regions.indexOf(region);
        if (index == -1)
            throw new IllegalArgumentException("region " + region + // NOI18N
                    " not part of textSync " + this); // NOI18N
        regions.remove(index);
        region.setTextSync(null);
        if (index == masterRegionIndex)
            index = -1;
        else if (index < masterRegionIndex)
            masterRegionIndex--;
    }
    
    List<TextRegion<?>> regionsModifiable() {
        return regions;
    }
    
    public <T> TextSyncGroup<T> group() {
        @SuppressWarnings("unchecked")
        TextSyncGroup<T> group = (TextSyncGroup<T>)textSyncGroup;
        return group;
    }
    
    void setGroup(TextSyncGroup<?> textSyncGroup) {
        this.textSyncGroup = textSyncGroup;
    }

    TextRegionManager textRegionManager() {
        return (textSyncGroup != null) ? textSyncGroup.textRegionManager() : null;
    }
    
    private TextRegionManager validTextRegionManager() {
        TextRegionManager textRegionManager = textRegionManager();
        if (textRegionManager == null)
            throw new IllegalStateException("Only possible for textSync connected to a TextRegionManager"); // NOI18N
        return textRegionManager;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(regions.size() * 8 + 2);
        TextRegion masterRegion = masterRegion();
        for (TextRegion textRegion : regions) {
            sb.append("    ");
            if (textRegion == masterRegion)
                sb.append("M:");
            sb.append(textRegion).append('\n');
        }
        return sb.toString();
    }
    
}
