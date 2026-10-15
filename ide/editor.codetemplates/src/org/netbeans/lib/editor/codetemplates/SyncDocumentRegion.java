/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.lib.editor.codetemplates;

import java.util.ArrayList;
import java.util.List;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Position;
import org.netbeans.lib.editor.util.CharSequenceUtilities;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.netbeans.lib.editor.util.swing.MutablePositionRegion;
import org.netbeans.lib.editor.util.swing.PositionRegion;
import org.openide.util.Exceptions;

/**
 * Maintain the same text in the selected regions of the text document.
 *
 * @author Miloslav Metelka
 */
public final class SyncDocumentRegion {
    
    private final Document doc;
    
    private final List<? extends MutablePositionRegion> regions;
    
    private final List<? extends MutablePositionRegion> sortedRegions;
    
    private final boolean regionsSortPerformed;
    
    /**
     * Construct synchronized document regions.
     *
     * @param doc document on which to operate.
     * @param regions regions that should be kept synchronized.
     *  The first region is the master. All the regions need to have
     *  the initial position to have the backward bias.
     */
    public SyncDocumentRegion(Document doc, List<? extends MutablePositionRegion> regions) {
        this.doc = doc;
        this.regions = regions;
        // Check bounds correctness and whether they are sorted
        regionsSortPerformed = PositionRegion.isRegionsSorted(regions);
        if (regionsSortPerformed) {
            sortedRegions = regions;
        } else {
            sortedRegions = new ArrayList<MutablePositionRegion>(regions);
            sortedRegions.sort(PositionRegion.getComparator());
        }
    }
    
    public int getRegionCount() {
        return regions.size();
    }
    
    public MutablePositionRegion getRegion(int regionIndex) {
        return regions.get(regionIndex);
    }

    public int getFirstRegionStartOffset() {
        return getRegion(0).getStartOffset();
    }
    
    public int getFirstRegionEndOffset() {
        return getRegion(0).getEndOffset();
    }
    
    public int getFirstRegionLength() {
        return getFirstRegionEndOffset() - getFirstRegionStartOffset();
    }
    
    /**
     * Get region in a sorted list of the regions.
     *
     * @param regionIndex of the region.
     * @return region in a sorted list of the regions.
     */
    public MutablePositionRegion getSortedRegion(int regionIndex) {
         return sortedRegions.get(regionIndex);
    }

    /**
     * Propagate text of the first region into all other regions.
     *
     * @param moveStartDownLength how much to move starting position
     *  down. It may be 0 to signal that the startng position should
     *  stay as is.
     */
    public void sync(int moveStartDownLength) {
        if (moveStartDownLength != 0) {
            // Move first region's start offset down
            MutablePositionRegion firstRegion = getRegion(0);
            try {
                Position newStartPos = doc.createPosition(
                        firstRegion.getStartOffset() - moveStartDownLength);
                firstRegion.setStartPosition(newStartPos);

            } catch (BadLocationException e) {
                Exceptions.printStackTrace(e);
            }

        }

        String firstRegionText = getFirstRegionText();
        if (firstRegionText != null) {
            int regionCount = getRegionCount();
            for (int i = 1; i < regionCount; i++) {
                MutablePositionRegion region = getRegion(i);
                int offset = region.getStartOffset();
                int length = region.getEndOffset() - offset;
                try {
                    final CharSequence old = DocumentUtilities.getText(doc, offset, length);
                    if (!CharSequenceUtilities.textEquals(firstRegionText, old)) {
                        int res = -1;
                        for(int k = 0; k < Math.min(old.length(), firstRegionText.length()); k++) {
                            if (old.charAt(k) == firstRegionText.charAt(k)) {
                                res = k;
                            } else {
                                break;
                            }
                        }
                        String insert = firstRegionText.substring(res+1);
                        CharSequence remove = old.subSequence(res + 1, old.length());
                        if (insert.length() > 0) {
                            doc.insertString(offset + res + 1, insert, null);
                        }
                        if (remove.length() > 0) {
                            doc.remove(offset + res + 1 + insert.length(), remove.length());
                        }
                    }
                    // Recreate the start position as the position are put together
                    Position newStartPos = doc.createPosition(offset);
                    region.setStartPosition(newStartPos);
                } catch (BadLocationException e) {
                    Exceptions.printStackTrace(e);
                }

            }
        }
    }

    private String getFirstRegionText() {
        return getRegionText(0);
    }
    
    private String getRegionText(int regionIndex) {
        try {
            MutablePositionRegion region = getRegion(regionIndex);
            int offset = region.getStartOffset();
            int length = region.getEndOffset() - offset;
            return doc.getText(offset, length);
        } catch (BadLocationException e) {
            Exceptions.printStackTrace(e);
            return null;
        }
    }
    
}
