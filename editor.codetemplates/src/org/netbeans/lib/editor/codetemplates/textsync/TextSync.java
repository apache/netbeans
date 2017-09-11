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
